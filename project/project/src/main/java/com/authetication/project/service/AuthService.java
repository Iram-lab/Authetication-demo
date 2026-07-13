package com.authetication.project.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional; // 🌟 1. Added Transaction import

import com.authetication.project.model.User;
import com.authetication.project.repository.UserRepository;

import java.util.Optional;

@Service
public class AuthService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private JwtService jwtService;

    private final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    @Transactional(rollbackFor = Exception.class) // 🌟 2. Added to force a clean database commit
    public String registerUser(User user) throws Exception {
        Optional<User> existingUser = userRepository.findByEmail(user.getEmail());
        if (existingUser.isPresent()) {
            throw new Exception("Email address is already registered.");
        }
        user.setPassword(passwordEncoder.encode(user.getPassword()));
        
        // 🌟 3. Changed .save() to .saveAndFlush() to force H2 to write the row right now
        User savedUser = userRepository.saveAndFlush(user); 
        
        return jwtService.generateToken(savedUser);
    }

    public String loginUser(String email, String password) throws Exception {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new Exception("Invalid email address."));

        if (!passwordEncoder.matches(password, user.getPassword())) {
            throw new Exception("Incorrect password.");
        }
        return jwtService.generateToken(user);
    }

    public User getUserByEmail(String email) {
        return userRepository.findByEmail(email).orElse(null);
    }
}
