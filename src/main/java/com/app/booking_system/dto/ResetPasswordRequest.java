package com.app.booking_system.dto;

public record ResetPasswordRequest(String newPassword, String token) {
}