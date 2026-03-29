package com.app.booking_system.config;

import com.app.booking_system.entity.ClassSchedule;
import com.app.booking_system.repository.ClassScheduleRepository;
import com.app.booking_system.service.ClassSchedulerService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collector;
import java.util.stream.Collectors;

@Configuration
@RequiredArgsConstructor
@Slf4j
public class ClassSchedulerInitializer {

    private final ClassScheduleRepository classScheduleRepository;
    private final ClassSchedulerService classSchedulerService;

    @Bean
    @Order(1)
    public ApplicationRunner scheduleExistingClasses() {
        return args -> {
            try {
                log.info("Scheduling jobs for existing future classes...");
                // Schedule jobs for all future classes
                List<ClassSchedule> futureClasses = classScheduleRepository.findAll().stream()
                    .filter(cs -> cs.getEndTime().isAfter(LocalDateTime.now()))
                    .toList();

                List<String> scheduledJobs = futureClasses.stream()
                    .map(cs -> {
                        
                        classSchedulerService.scheduleClassEndJob(cs);                       
                        return "Scheduling job for class: " + cs.getName() + " (ID: " + cs.getId() + ", Time: " + cs.getEndTime() + ")";
                    })
                    .collect(Collectors.toList());
                    
                log.info("Successfully scheduled {} jobs for future classes", scheduledJobs.size());
            } catch (Exception e) {
                log.error("Failed to schedule existing classes during startup", e);
                // Don't fail the application startup for this
            }
        };
    }
}