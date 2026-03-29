package com.app.booking_system.dto;

import java.time.LocalDateTime;

public record BookingResponseDto(
    Long id,
    String className,
    LocalDateTime startTime,
    LocalDateTime endTime,
    String status,
    String country
    // Add other fields as needed, but do NOT include sensitive info
) {}
