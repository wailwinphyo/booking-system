package com.app.booking_system.controller;


import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.app.booking_system.dto.ResetPasswordRequest;
import com.app.booking_system.dto.UserProfileDto;
import com.app.booking_system.service.AuthService;
import io.swagger.v3.oas.annotations.Operation;
import org.springframework.web.bind.annotation.RequestBody;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;

import jakarta.servlet.http.HttpServletRequest;
import java.util.Base64;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
@Tag(name = "Auth", description = "Endpoints for authentication and user management")
public class AuthController {
    private final AuthService service;

    @Operation(summary = "Register user", description = "Register a new user with email and password.", security = {})
    @PostMapping("/register")
    public String register(@RequestParam String email,
                           @RequestParam String password) {
        return service.register(email, password);
    }

    @Operation(summary = "Login user", description = "Login a user with Basic Auth and return a JWT token.", security = {})
    @SecurityRequirement(name = "basicAuth")
    @PostMapping("/login")
    public String login(HttpServletRequest request) {
        String auth = request.getHeader("Authorization");
        
        if (auth == null || !auth.startsWith("Basic ")) {
            throw new RuntimeException("Basic authorization required");
        }
        String base64Credentials = auth.substring(6);
        String credentials = new String(Base64.getDecoder().decode(base64Credentials));
        String[] parts = credentials.split(":", 2);
        if (parts.length != 2) {
            throw new RuntimeException("Invalid credentials");
        }
        String email = parts[0];
        String password = parts[1];
        
        return service.login(email, password);
    }

    @Operation(summary = "Verify email", description = "Verify a user's email with a token.", security = {})
    @PostMapping("/verify")
    public boolean verifyEmail(@RequestParam String email, @RequestParam String token) {
        return service.verifyEmail(email, token);
    }

    @Operation(summary = "Get user profile", description = "Get the profile of a user by email.")
    @GetMapping("/profile")
    @SecurityRequirement(name = "bearerAuth")
    public UserProfileDto getProfile() {
        Authentication authentication = SecurityContextHolder
                .getContext()
                .getAuthentication();
        String email = authentication.getName(); 

        // Assuming service.getProfile(email) returns a DTO or entity, not a String
        return service.getProfile(email);
    }

    @Operation(summary = "Change password", description = "Change a user's password.")
    @PostMapping("/change-password")
    public boolean changePassword(@RequestParam String oldPassword, @RequestParam String newPassword) {
        Authentication authentication = SecurityContextHolder
                .getContext()
                .getAuthentication();
        String email = authentication.getName(); 

        return service.changePassword(email, oldPassword, newPassword);
    }

    @Operation(summary = "Reset password", description = "Reset a user's password.", security = {})
    @PostMapping("/reset-password")
    public boolean resetPassword(@RequestBody ResetPasswordRequest request) {
        return service.resetPassword(request.newPassword(), request.token());
    }    
}
