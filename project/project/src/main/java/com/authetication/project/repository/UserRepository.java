package com.authetication.project.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.authetication.project.model.User;

import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {
    // Automated query lookup method
    Optional<User> findByEmail(String email);
}
