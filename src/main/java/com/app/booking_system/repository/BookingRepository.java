package com.app.booking_system.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.app.booking_system.entity.Booking;
import com.app.booking_system.entity.ClassSchedule;
import com.app.booking_system.entity.UserEntity;

import java.util.List;
import java.util.Optional;

public interface BookingRepository extends JpaRepository<Booking, Long> {
    List<Booking> findByUser(UserEntity user);
    List<Booking> findByClassSchedule(ClassSchedule classSchedule);
    Optional<Booking> findByUserAndClassSchedule(UserEntity user, ClassSchedule classSchedule);
    long countByClassScheduleAndStatus(ClassSchedule classSchedule, String status);
}
