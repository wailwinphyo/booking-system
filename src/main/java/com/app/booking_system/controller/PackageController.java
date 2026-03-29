package com.app.booking_system.controller;

import com.app.booking_system.dto.UserPackageResponse;
import com.app.booking_system.entity.PackageEntity;
import com.app.booking_system.service.PackageService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/packages")
@RequiredArgsConstructor
@Tag(name = "Packages", description = "Endpoints for package management and purchase")
@SecurityRequirement(name = "bearerAuth")
public class PackageController {

    private final PackageService packageService;

    @Operation(summary = "Get all packages", description = "Retrieve all available packages.")
    @GetMapping
    public List<PackageEntity> getAllPackages() {
        return packageService.getAllPackages();
    }

    @Operation(summary = "Get packages by country", description = "Retrieve packages available for a specific country.")
    @GetMapping("/country/{country}")
    public List<PackageEntity> getPackagesByCountry(@PathVariable String country) {
        return packageService.getPackagesByCountry(country);
    }

    @Operation(summary = "Get user packages", description = "Retrieve all packages purchased by a user.")
    @GetMapping("/user")
    public List<UserPackageResponse> getUserPackages() {
        Authentication authentication = SecurityContextHolder
                .getContext()
                .getAuthentication();
        String email = authentication.getName(); 

        return packageService.getUserPackages(email);
    }

    @Operation(summary = "Purchase a package", description = "Purchase a package for a user using card details.")
    @PostMapping("/purchase")
    public boolean purchasePackage(@RequestParam String email, @RequestParam Long packageId,
            @RequestParam String cardDetails) {
        return packageService.purchasePackage(email, packageId, cardDetails);
    }
}