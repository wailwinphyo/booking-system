package com.app.booking_system.controller;


import java.util.List;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.app.booking_system.dto.BookingResponseDto;
import com.app.booking_system.dto.ClassScheduleResponse;
import com.app.booking_system.service.BookingService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;

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
    public List<BookingResponseDto> getUserBookings() {
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
}
