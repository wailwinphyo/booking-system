package com.app.booking_system.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.app.booking_system.entity.Waitlist;

public interface WaitlistRepository extends JpaRepository<Waitlist, Long> {
    List<Waitlist> findByClassScheduleIdOrderByPositionAsc(Long id);
}
