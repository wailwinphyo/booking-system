package com.app.booking_system.controller;


import com.app.booking_system.dto.ClassScheduleResponse;
import com.app.booking_system.entity.Booking;
import com.app.booking_system.entity.ClassSchedule;
import com.app.booking_system.service.BookingService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/bookings")
@RequiredArgsConstructor
@SecurityRequirement(name = "bearerAuth")
@Tag(name = "Bookings", description = "Endpoints for booking operations")
public class BookingController {

    private final BookingService bookingService;

    @Operation(summary = "Book a class", description = "Book a class for a user by classId and email.")
    @PostMapping("/book/{classId}")
    public String book(@PathVariable Long classId) {
        Authentication authentication = SecurityContextHolder
                .getContext()
                .getAuthentication();
        String email = authentication.getName(); 

        return bookingService.book(email, classId);
    }

    @Operation(summary = "Cancel a booking", description = "Cancel a booking by bookingId and user email.")
    @PostMapping("/cancel/{bookingId}")
    public void cancel(@PathVariable Long bookingId) {
        Authentication authentication = SecurityContextHolder
                .getContext()
                .getAuthentication();
        String email = authentication.getName(); 

        bookingService.cancel(email, bookingId);
    }

    @Operation(summary = "Get user bookings", description = "Get all bookings for a user by email.")
    @GetMapping("/user")
    public List<Booking> getUserBookings() {
        Authentication authentication = SecurityContextHolder
                .getContext()
                .getAuthentication();
        String email = authentication.getName(); 

        return bookingService.getUserBookings(email);
    }

    @Operation(summary = "Get class schedules", description = "Get all class schedules for a country.")
    @GetMapping("/classes/{country}")
    public List<ClassScheduleResponse> getClassSchedules(@PathVariable String country) {        
        return bookingService.getClassSchedules(country);
    }

    @Operation(summary = "Check in to a booking", description = "Check in to a booking by bookingId and user email.")
    @PostMapping("/checkin/{bookingId}")
    public boolean checkIn(@PathVariable Long bookingId) {
        Authentication authentication = SecurityContextHolder
                .getContext()
                .getAuthentication();
        String email = authentication.getName(); 

        return bookingService.checkIn(email, bookingId);
    }

    @Operation(summary = "Add to waitlist", description = "Add a user to the waitlist for a class by classId and email.")
    @PostMapping("/waitlist/{classId}")
    public void addToWaitlist(@PathVariable Long classId) {
        Authentication authentication = SecurityContextHolder
                .getContext()
                .getAuthentication();
        String email = authentication.getName(); 
        bookingService.addToWaitlist(email, classId);
    }
}
