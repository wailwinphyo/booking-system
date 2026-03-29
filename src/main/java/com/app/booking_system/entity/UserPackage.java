package com.app.booking_system.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.JoinColumn;
import lombok.Data;

import java.time.LocalDateTime;

@Entity
@Data
public class UserPackage {
    
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @ManyToOne
    @JoinColumn(name = "user_id")
    private UserEntity user;
    
    @ManyToOne
    @JoinColumn(name = "package_id")
    private PackageEntity packageEntity;
    
    private int remainingCredits;
    
    private LocalDateTime expiryDate;
}