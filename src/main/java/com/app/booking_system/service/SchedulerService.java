package com.app.booking_system.service;

import org.springframework.stereotype.Service;

@Service
public interface SchedulerService {
    void processEndedClasses();
}