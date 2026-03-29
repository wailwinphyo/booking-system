package com.app.booking_system.repository;

import com.app.booking_system.entity.ClassSchedule;
import java.time.LocalDateTime;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

public interface ClassScheduleRepository extends JpaRepository<ClassSchedule, Long> {
    List<ClassSchedule> findByCountry(String country);
    List<ClassSchedule> findByEndTimeBefore(LocalDateTime time);
}