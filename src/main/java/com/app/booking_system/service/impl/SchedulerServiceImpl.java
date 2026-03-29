package com.app.booking_system.service.impl;

import com.app.booking_system.entity.ClassSchedule;
import com.app.booking_system.entity.Waitlist;
import com.app.booking_system.repository.ClassScheduleRepository;
import com.app.booking_system.repository.UserPackageRepository;
import com.app.booking_system.repository.WaitlistRepository;
import com.app.booking_system.service.SchedulerService;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class SchedulerServiceImpl implements SchedulerService {

    private final ClassScheduleRepository classScheduleRepository;
    private final WaitlistRepository waitlistRepository;
    private final UserPackageRepository userPackageRepository;

    @Override
    @Scheduled(fixedRate = 60000) // Every minute
    @Transactional
    public void processEndedClasses() {
        LocalDateTime now = LocalDateTime.now();
        List<ClassSchedule> endedClasses = classScheduleRepository.findAll().stream()
            .filter(cs -> cs.getEndTime().isBefore(now))
            .toList();

        for (ClassSchedule cs : endedClasses) {
            List<Waitlist> waitlists = waitlistRepository.findByClassScheduleIdOrderByPositionAsc(cs.getId());
            for (Waitlist w : waitlists) {
                // Refund credits
                userPackageRepository.findByUserId(w.getUser().getId()).stream()
                    .filter(up -> up.getPackageEntity().getCountry().equals(cs.getCountry()))
                    .forEach(up -> {
                        up.setRemainingCredits(up.getRemainingCredits() + cs.getRequiredCredits());
                        userPackageRepository.save(up);
                    });
                waitlistRepository.delete(w);
            }
        }
    }
}