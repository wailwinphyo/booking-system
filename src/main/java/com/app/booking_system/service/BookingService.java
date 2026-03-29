package com.app.booking_system.service;

import java.util.List;

import com.app.booking_system.dto.ClassScheduleResponse;
import com.app.booking_system.entity.Booking;

public interface BookingService {
    String book(String email, Long classId);
    void cancel(String email, Long bookingId);
    List<Booking> getUserBookings(String email);
    List<ClassScheduleResponse> getClassSchedules(String country);
    boolean checkIn(String email, Long bookingId);
    void addToWaitlist(String email, Long classId);
    void refundWaitlistCredits(Long classId);
    void processRefundEndedClasses();
}
