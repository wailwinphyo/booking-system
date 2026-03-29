package com.app.booking_system.service;

import com.app.booking_system.dto.UserProfileDto;

public interface AuthService {

    String register(String email, String password);

    String login(String email, String password);

    boolean verifyEmail(String email, String token);

    UserProfileDto getProfile(String email);

    boolean changePassword(String email, String oldPassword, String newPassword);

    boolean resetPassword(String newPassword, String token);

}
