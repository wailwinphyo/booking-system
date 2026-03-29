package com.app.booking_system.job;

import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import com.app.booking_system.service.BookingService;

import java.util.concurrent.TimeUnit;

@Component
public class ClassEndJob implements Job {

    @Autowired
    private BookingService bookingService;

    @Autowired
    private StringRedisTemplate redis;

    @Override
    public void execute(JobExecutionContext context) throws JobExecutionException {
        // Get classId from job data
        Object classIdObj = context.getJobDetail().getJobDataMap().get("classId");
        Long classId = null;

        if (classIdObj instanceof Long) {
            classId = (Long) classIdObj;
        } else if (classIdObj instanceof Integer) {
            classId = ((Integer) classIdObj).longValue();
        } else if (classIdObj instanceof String) {
            try {
                classId = Long.parseLong((String) classIdObj);
            } catch (NumberFormatException e) {
                // Invalid classId format
            }
        }

        if (classId == null) {
            // Fallback to processing all ended classes (for backward compatibility)
            processAllEndedClasses();
            return;
        }

        // Process specific class
        String lockKey = "lock:classEndJob:" + classId;
        try {
            // Acquire lock to prevent concurrent execution for this class
            Boolean locked = redis.opsForValue().setIfAbsent(lockKey, "locked", 5, TimeUnit.MINUTES);
            if (!Boolean.TRUE.equals(locked)) {
                // Job is already running for this class, skip
                return;
            }

            // Process refunds for this specific class
            bookingService.refundWaitlistCredits(classId);
        } finally {
            redis.delete(lockKey);
        }
    }

    private void processAllEndedClasses() {
        String lockKey = "lock:classEndJob";
        try {
            // Acquire lock to prevent concurrent execution
            Boolean locked = redis.opsForValue().setIfAbsent(lockKey, "locked", 5, TimeUnit.MINUTES);
            if (!Boolean.TRUE.equals(locked)) {
                // Job is already running, skip
                return;
            }

            // Process refunds for all ended classes
            bookingService.processRefundEndedClasses();
        } finally {
            redis.delete(lockKey);
        }
    }
}