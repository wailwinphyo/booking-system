package com.app.booking_system.service.impl;

import java.time.LocalDateTime;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.app.booking_system.dto.ClassScheduleResponse;
import com.app.booking_system.entity.Booking;
import com.app.booking_system.entity.ClassSchedule;
import com.app.booking_system.entity.UserEntity;
import com.app.booking_system.entity.UserPackage;
import com.app.booking_system.entity.Waitlist;
import com.app.booking_system.repository.BookingRepository;
import com.app.booking_system.repository.ClassScheduleRepository;
import com.app.booking_system.repository.UserPackageRepository;
import com.app.booking_system.repository.UserRepository;
import com.app.booking_system.repository.WaitlistRepository;
import com.app.booking_system.service.BookingService;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class BookingServiceImpl implements BookingService {

    private final StringRedisTemplate redis;
    private final BookingRepository bookingRepository;
    private final WaitlistRepository waitlistRepository;
    private final UserRepository userRepository;
    private final ClassScheduleRepository classScheduleRepository;
    private final UserPackageRepository userPackageRepository;

    @Override
    @Transactional
    public String book(String email, Long classId) {
        UserEntity user = userRepository.findByEmail(email).orElseThrow(() -> new RuntimeException("User not found"));
        ClassSchedule classSchedule = classScheduleRepository.findById(classId)
                .orElseThrow(() -> new RuntimeException("Class not found"));

        // Check if user already booked
        if (bookingRepository.findByUserAndClassSchedule(user, classSchedule).isPresent()) {
            throw new RuntimeException("Already booked");
        }

        // Check overlapping bookings
        List<Booking> userBookings = bookingRepository.findByUser(user);
        for (Booking b : userBookings) {
            if (b.getStatus().equals("BOOKED") &&
                    b.getClassSchedule().getStartTime().isBefore(classSchedule.getEndTime()) &&
                    b.getClassSchedule().getEndTime().isAfter(classSchedule.getStartTime())) {
                throw new RuntimeException("Overlapping booking");
            }
        }

        // Check user has package for the country with credits
        List<UserPackage> userPackages = userPackageRepository.findByUserIdAndPackageEntityCountry(user.getId(),
                classSchedule.getCountry());
        // Sort by expiry date ascending to use the one expiring soonest
        userPackages.sort((a, b) -> a.getExpiryDate().compareTo(b.getExpiryDate()));
        List<UserPackage> validPackages = userPackages.stream()
                .filter(up -> up.getRemainingCredits() > 0 && up.getExpiryDate().isAfter(LocalDateTime.now()))
                .collect(Collectors.toList());
        long totalCredits = validPackages.stream().mapToLong(UserPackage::getRemainingCredits).sum();
        if (totalCredits < classSchedule.getRequiredCredits()) {
            throw new RuntimeException("No valid package with sufficient credits");
        }

        String lockKey = "lock:class:" + classId;
        String countKey = "booked:class:" + classId;
        
        try {
            // Acquire lock
            Boolean locked = redis.opsForValue().setIfAbsent(lockKey, "locked", 10, TimeUnit.SECONDS);
            if (!Boolean.TRUE.equals(locked)) {
                throw new RuntimeException("Concurrent booking, try again");
            }

            // Get current booked count from Redis, or initialize from DB
            String currentCountStr = redis.opsForValue().get(countKey);
            long currentBookings;
            if (currentCountStr == null) {
                currentBookings = bookingRepository.countByClassScheduleAndStatus(classSchedule, "BOOKED");
                redis.opsForValue().set(countKey, String.valueOf(currentBookings));
            } else {
                currentBookings = Long.parseLong(currentCountStr);
            }

            if (currentBookings >= classSchedule.getCapacity()) {
                // Add to waitlist
                addToWaitlist(email, classId);
                return "ADDED_TO_WAITLIST";
            }

            // Increment booked count in Redis
            redis.opsForValue().increment(countKey);

            // Deduct credits
            long toDeduct = classSchedule.getRequiredCredits();
            for (UserPackage up : validPackages) {
                if (toDeduct <= 0) break;
                long deductFromThis = Math.min(toDeduct, up.getRemainingCredits());
                up.setRemainingCredits(up.getRemainingCredits() - deductFromThis);
                toDeduct -= deductFromThis;
                userPackageRepository.save(up);
            }

            // Create booking
            Booking booking = new Booking();
            booking.setUser(user);
            booking.setClassSchedule(classSchedule);
            booking.setStatus("BOOKED");
            booking.setBookingTime(LocalDateTime.now());
            booking.setCheckedIn(false);
            bookingRepository.save(booking);

            return "BOOKED";
        } finally {
            redis.delete(lockKey);
        }
    }

    @Override
    @Transactional
    public void cancel(String email, Long bookingId) {
        UserEntity user = userRepository.findByEmail(email).orElseThrow(() -> new RuntimeException("User not found"));
        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new RuntimeException("Booking not found"));
        if (!booking.getUser().getId().equals(user.getId())) {
            throw new RuntimeException("Not your booking");
        }
        if (!booking.getStatus().equals("BOOKED")) {
            throw new RuntimeException("Cannot cancel");
        }

        LocalDateTime now = LocalDateTime.now();
        LocalDateTime classStart = booking.getClassSchedule().getStartTime();
        boolean refund = classStart.isAfter(now.plusHours(4));

        booking.setStatus("CANCELLED");
        bookingRepository.save(booking);

        // Decrement booked count in Redis
        redis.opsForValue().decrement("booked:class:" + booking.getClassSchedule().getId());

        if (refund) {
            // Refund credits
            List<UserPackage> userPackages = userPackageRepository.findByUserIdAndPackageEntityCountry(user.getId(),
                    booking.getClassSchedule().getCountry());
            for (UserPackage up : userPackages) {
                if (up.getPackageEntity().getId().equals(booking.getClassSchedule().getRequiredCredits())) { // Assuming
                                                                                                             // credits
                                                                                                             // match
                    up.setRemainingCredits(up.getRemainingCredits() + booking.getClassSchedule().getRequiredCredits());
                    userPackageRepository.save(up);
                    break;
                }
            }
        }

        // Promote waitlist
        promoteWaitlist(booking.getClassSchedule().getId());
    }

    @Override
    public List<Booking> getUserBookings(String email) {
        UserEntity user = userRepository.findByEmail(email).orElseThrow(() -> new RuntimeException("User not found"));
        return bookingRepository.findByUser(user);
    }

    @Override
    public List<ClassScheduleResponse> getClassSchedules(String country) {
        List<ClassSchedule> schedules = classScheduleRepository.findByCountry(country);
        return schedules.stream().map(schedule -> {
            String countKey = "booked:class:" + schedule.getId();
            String currentCountStr = redis.opsForValue().get(countKey);
            long bookedCount;
            if (currentCountStr == null) {
                bookedCount = bookingRepository.countByClassScheduleAndStatus(schedule, "BOOKED");
                redis.opsForValue().set(countKey, String.valueOf(bookedCount));
            } else {
                bookedCount = Long.parseLong(currentCountStr);
            }
            return ClassScheduleResponse.from(schedule, (int) bookedCount);
        }).toList();
    }

    @Override
    public boolean checkIn(String email, Long bookingId) {
        UserEntity user = userRepository.findByEmail(email).orElseThrow(() -> new RuntimeException("User not found"));
        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new RuntimeException("Booking not found"));
        if (!booking.getUser().getId().equals(user.getId()) || !booking.getStatus().equals("BOOKED")) {
            return false;
        }
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime classStart = booking.getClassSchedule().getStartTime();
        if (now.isBefore(classStart) || now.isAfter(classStart.plusHours(1))) {
            return false;
        }
        booking.setCheckedIn(true);
        bookingRepository.save(booking);
        return true;
    }

    @Override
    @Transactional
    public void addToWaitlist(String email, Long classId) {
        UserEntity user = userRepository.findByEmail(email).orElseThrow(() -> new RuntimeException("User not found"));
        ClassSchedule classSchedule = classScheduleRepository.findById(classId)
                .orElseThrow(() -> new RuntimeException("Class not found"));

        // Check if already in waitlist
        List<Waitlist> existing = waitlistRepository.findByClassScheduleIdOrderByPositionAsc(classId);
        for (Waitlist w : existing) {
            if (w.getUser().getId().equals(user.getId())) {
                throw new RuntimeException("Already in waitlist");
            }
        }

        Waitlist waitlist = new Waitlist();
        waitlist.setUser(user);
        waitlist.setClassSchedule(classSchedule);
        waitlist.setPosition(existing.size() + 1);
        waitlistRepository.save(waitlist);
    }

    private void promoteWaitlist(Long classId) {
        List<Waitlist> waitlists = waitlistRepository.findByClassScheduleIdOrderByPositionAsc(classId);
        if (!waitlists.isEmpty()) {
            Waitlist first = waitlists.get(0);
            // Check if user has credits
            List<UserPackage> userPackages = userPackageRepository.findByUserIdAndPackageEntityCountry(
                    first.getUser().getId(), first.getClassSchedule().getCountry());
            UserPackage validPackage = null;
            for (UserPackage up : userPackages) {
                if (up.getRemainingCredits() >= first.getClassSchedule().getRequiredCredits()
                        && up.getExpiryDate().isAfter(LocalDateTime.now())) {
                    validPackage = up;
                    break;
                }
            }
            if (validPackage != null) {
                validPackage.setRemainingCredits(
                        validPackage.getRemainingCredits() - first.getClassSchedule().getRequiredCredits());
                userPackageRepository.save(validPackage);

                Booking booking = new Booking();
                booking.setUser(first.getUser());
                booking.setClassSchedule(first.getClassSchedule());
                booking.setStatus("BOOKED");
                booking.setBookingTime(LocalDateTime.now());
                booking.setCheckedIn(false);
                bookingRepository.save(booking);

                // Increment booked count in Redis
                redis.opsForValue().increment("booked:class:" + first.getClassSchedule().getId());

                waitlistRepository.delete(first);
                // Update positions
                for (int i = 1; i < waitlists.size(); i++) {
                    waitlists.get(i).setPosition(i);
                    waitlistRepository.save(waitlists.get(i));
                }
            }
        }
    }
}
