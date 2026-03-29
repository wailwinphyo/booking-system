package com.app.booking_system.dto;

import com.app.booking_system.entity.PackageEntity;
import com.app.booking_system.entity.UserPackage;

import java.time.LocalDateTime;

public record UserPackageResponse(
    Long id,
    PackageEntity packageEntity,
    int remainingCredits,
    LocalDateTime expiryDate,
    String status
) {
    public static UserPackageResponse from(UserPackage userPackage) {
        String status = userPackage.getExpiryDate().isBefore(LocalDateTime.now()) ? "EXPIRED" : "ACTIVE";
        return new UserPackageResponse(
            userPackage.getId(),
            userPackage.getPackageEntity(),
            userPackage.getRemainingCredits(),
            userPackage.getExpiryDate(),
            status
        );
    }
}