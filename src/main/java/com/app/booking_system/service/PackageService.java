package com.app.booking_system.service;

import java.util.List;

import com.app.booking_system.dto.UserPackageResponse;
import com.app.booking_system.entity.PackageEntity;

public interface PackageService {
    List<PackageEntity> getAllPackages();
    List<PackageEntity> getPackagesByCountry(String country);
    List<UserPackageResponse> getUserPackages(String email);
    boolean purchasePackage(String email, Long packageId, String cardDetails);
}