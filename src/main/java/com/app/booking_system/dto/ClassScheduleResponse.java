package com.app.booking_system.dto;

import com.app.booking_system.entity.ClassSchedule;

import java.time.LocalDateTime;

public record ClassScheduleResponse(
    Long id,
    String name,
    String country,
    LocalDateTime startTime,
    LocalDateTime endTime,
    int capacity,
    int requiredCredits,
    int availableSpots
) {
    public static ClassScheduleResponse from(ClassSchedule classSchedule, int bookedCount) {
        int available = classSchedule.getCapacity() - bookedCount;
        return new ClassScheduleResponse(
            classSchedule.getId(),
            classSchedule.getName(),
            classSchedule.getCountry(),
            classSchedule.getStartTime(),
            classSchedule.getEndTime(),
            classSchedule.getCapacity(),
            classSchedule.getRequiredCredits(),
            available
        );
    }
}