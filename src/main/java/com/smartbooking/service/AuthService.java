package com.smartbooking.service;

import com.smartbooking.domain.Role;
import com.smartbooking.domain.User;
import com.smartbooking.persistence.UserRepository;
import com.smartbooking.util.PasswordHasher;

import java.util.Optional;

public class AuthService {
    private final UserRepository userRepository;

    public AuthService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public User register(String username, String password) {
        if (username == null || username.isBlank()) {
            throw new IllegalArgumentException("Username is required");
        }
        if (password == null || password.length() < 4) {
            throw new IllegalArgumentException("Password must be at least 4 characters");
        }
        Optional<User> existing = userRepository.findByUsername(username);
        if (existing.isPresent()) {
            throw new IllegalArgumentException("Username already exists");
        }
        return userRepository.create(username, PasswordHasher.hash(password), Role.CUSTOMER);
    }

    public User login(String username, String password) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new IllegalArgumentException("Invalid credentials"));
        String hashed = PasswordHasher.hash(password);
        if (!hashed.equals(user.getPasswordHash())) {
            throw new IllegalArgumentException("Invalid credentials");
        }
        return user;
    }
}
