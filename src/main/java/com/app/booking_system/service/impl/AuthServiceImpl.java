package com.app.booking_system.service.impl;

import java.util.List;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.app.booking_system.entity.Role;
import com.app.booking_system.entity.UserEntity;
import com.app.booking_system.repository.UserRepository;
import com.app.booking_system.security.JwtUtil;
import com.app.booking_system.service.AuthService;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

    private final UserRepository userRepo;

    private final JwtUtil jwt;

    private final PasswordEncoder encoder;

    @Override
    public String register(String email, String password) {
        if (email == null || email.isEmpty()) {
            throw new RuntimeException("Email is required");
        }
        if (password == null || password.length() < 6) {
            throw new RuntimeException("Password must be at least 6 characters");
        }
        if (userRepo.findByEmail(email).isPresent()) {
            throw new RuntimeException("User already exists");
        }
        UserEntity user = new UserEntity();
        user.setEmail(email);
        user.setPassword(encoder.encode(password));
        user.setVerified(true); // Set to true for testing
        // Mock send verification email
        if (!sendVerifyEmail(email)) {
            throw new RuntimeException("Failed to send verification email");
        }
        userRepo.save(user);
        return "Registration successful, please verify your email";
    }

    @Override
    public String login(String email, String password) {
        
        UserEntity user = userRepo.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));
        if (!encoder.matches(password, user.getPassword())) {
            throw new RuntimeException("Invalid password");
        }
        System.out.println(user.isVerified() + " >> ");
        if (!user.isVerified()) {
            throw new RuntimeException("Email not verified");
        }
        List<String> roles = user.getRoles().stream().map(Role::getName).toList();
        return jwt.generate(email, roles);
    }

    @Override
    public boolean verifyEmail(String email, String token) {
        UserEntity user = userRepo.findByEmail(email).orElseThrow(() -> new RuntimeException("User not found"));
        // Mock token check, assume token is "verify"
        if ("verify".equals(token)) {
            user.setVerified(true);
            userRepo.save(user);
            return true;
        }
        return false;
    }

    @Override
    public String getProfile(String email) {
        UserEntity user = userRepo.findByEmail(email).orElseThrow(() -> new RuntimeException("User not found"));
        return "Email: " + user.getEmail() + ", Verified: " + user.isVerified();
    }

    @Override
    public boolean changePassword(String email, String oldPassword, String newPassword) {
        if (newPassword == null || newPassword.length() < 6) {
            throw new RuntimeException("New password must be at least 6 characters");
        }

        UserEntity user = userRepo.findByEmail(email).orElseThrow(() -> new RuntimeException("User not found"));
        if (!encoder.matches(oldPassword, user.getPassword())) {
            return false;
        }

        user.setPassword(encoder.encode(newPassword));
        userRepo.save(user);
        return true;
    }

    @Override
    public boolean resetPassword(String newPassword, String token) {
        // Dummy check: assume token is email
        if (token != null && !token.isEmpty()) {
            UserEntity user = userRepo.findByEmail(token).orElseThrow(() -> new RuntimeException("User not found"));
            user.setPassword(encoder.encode(newPassword));
            userRepo.save(user);
            return true;
        }
        return false;
    }

    private boolean sendVerifyEmail(String email) {
        return true;
    }
}