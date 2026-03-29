package com.app.booking_system.dto;

import java.util.List;

public record UserProfileDto(
    Long id,
    String email,
    String name,
    List<String> roles,
    boolean enabled
){}
