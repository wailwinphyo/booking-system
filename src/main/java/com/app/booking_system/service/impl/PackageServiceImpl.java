package com.app.booking_system.service.impl;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.stereotype.Service;

import com.app.booking_system.dto.UserPackageResponse;
import com.app.booking_system.entity.PackageEntity;
import com.app.booking_system.entity.UserEntity;
import com.app.booking_system.entity.UserPackage;
import com.app.booking_system.repository.PackageRepository;
import com.app.booking_system.repository.UserPackageRepository;
import com.app.booking_system.repository.UserRepository;
import com.app.booking_system.service.PackageService;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class PackageServiceImpl implements PackageService {

    private final PackageRepository packageRepository;
    private final UserPackageRepository userPackageRepository;
    private final UserRepository userRepository;

    @Override
    public List<PackageEntity> getAllPackages() {
        return packageRepository.findAll();
    }

    @Override
    public List<PackageEntity> getPackagesByCountry(String country) {
        return packageRepository.findByCountry(country);
    }

    @Override
    public List<UserPackageResponse> getUserPackages(String email) {
        UserEntity user = userRepository.findByEmail(email).orElseThrow(() -> new RuntimeException("User not found"));
        List<UserPackage> userPackages = userPackageRepository.findByUserId(user.getId());
        return userPackages.stream().map(UserPackageResponse::from).toList();
    }

    @Override
    public boolean purchasePackage(String email, Long packageId, String cardDetails) {
        UserEntity user = userRepository.findByEmail(email).orElseThrow(() -> new RuntimeException("User not found"));
        PackageEntity pkg = packageRepository.findById(packageId).orElseThrow(() -> new RuntimeException("Package not found"));

        // Mock payment
        if (!addPaymentCard(cardDetails) || !paymentCharge(pkg.getPrice())) {
            return false;
        }

        UserPackage userPackage = new UserPackage();
        userPackage.setUser(user);
        userPackage.setPackageEntity(pkg);
        userPackage.setRemainingCredits(pkg.getCredits());
        userPackage.setExpiryDate(LocalDateTime.now().plusMonths(1)); // Assume 1 month expiry
        userPackageRepository.save(userPackage);
        return true;
    }

    private boolean addPaymentCard(String cardDetails) {
        // Mock
        return true;
    }

    private boolean paymentCharge(double amount) {
        // Mock
        return true;
    }
}