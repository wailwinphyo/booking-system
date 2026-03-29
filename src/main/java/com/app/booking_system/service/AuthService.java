package com.app.booking_system.service;

public interface AuthService {

    String register(String email, String password);

    String login(String email, String password);

    boolean verifyEmail(String email, String token);

    String getProfile(String email);

    boolean changePassword(String email, String oldPassword, String newPassword);

    boolean resetPassword(String newPassword, String token);

}
