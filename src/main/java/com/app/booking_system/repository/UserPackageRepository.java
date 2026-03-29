package com.app.booking_system.repository;

import com.app.booking_system.entity.UserPackage;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface UserPackageRepository extends JpaRepository<UserPackage, Long> {
    List<UserPackage> findByUserId(Long userId);
    List<UserPackage> findByUserIdAndPackageEntityCountry(Long userId, String country);
}