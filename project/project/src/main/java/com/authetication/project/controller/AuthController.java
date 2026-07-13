package com.authetication.project.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.authetication.project.model.User;
import com.authetication.project.model.AuthResponse;
import com.authetication.project.service.AuthService;
import com.authetication.project.service.JwtService;

import java.util.Map;

@RestController
@RequestMapping("/api/auth")
@CrossOrigin(origins = "http://localhost:4200") 
// Lombok automatically creates the 'log' variable behind the scenes
public class AuthController {

    @Autowired
    private AuthService authService;

    @Autowired
    private JwtService jwtService;
    @PostMapping("/register")
    public ResponseEntity<?> register(@RequestBody User user) {
        // Log the entry with user email (Avoid printing sensitive raw data like passwords)
        
        
        try {
            String token = authService.registerUser(user);
       
            
            return ResponseEntity.ok(new AuthResponse(token, "Registration successful!", user.getName()));
            
        } catch (Exception e) {
            // Log the warning/error message. We use warn since it's likely a user validation error (e.g., email already exists)
            
            return ResponseEntity.badRequest().body(Map.of("message", e.getMessage()));
        }
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody Map<String, String> credentials) {
        String email = credentials.get("email");
        
        
        try {
            String password = credentials.get("password");
            String token = authService.loginUser(email, password);
            User user = authService.getUserByEmail(email);
            
            
            return ResponseEntity.ok(new AuthResponse(token, "Welcome back!", user.getName()));
            
        } catch (Exception e) {
            // Log unauthorized access warnings explicitly for security monitoring audits
          
            return ResponseEntity.status(401).body(Map.of("message", e.getMessage()));
        }
    }

        /**
     * 🌟 UPDATED: GET Method that generates and returns a fresh JWT token in the AuthResponse.
     * URL Example: GET http://localhost:8080/api/auth/profile?email=john@example.com
     */
    @GetMapping("/profile")
public ResponseEntity<?> getUserProfile(@RequestParam String email) {
    try {
        User user = authService.getUserByEmail(email);
        if (user == null) {
            return ResponseEntity.status(404).body(Map.of("message", "User not found."));
        }
        
        String token = jwtService.generateToken(user);
        
        // 🌟 This return object triggers EncryptResponseBodyAdvice which wraps and encrypts it
        return ResponseEntity.ok(new AuthResponse(token, "Profile and token fetched successfully!", user.getName()));
        
    } catch (Exception e) {
        return ResponseEntity.internalServerError().body(Map.of("message", e.getMessage()));
    }
}


}
