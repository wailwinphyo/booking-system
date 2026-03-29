package com.app.booking_system.service.impl;

import java.time.ZoneId;
import java.util.Date;

import org.quartz.JobBuilder;
import org.quartz.JobDetail;
import org.quartz.JobKey;
import org.quartz.Scheduler;
import org.quartz.SchedulerException;
import org.quartz.Trigger;
import org.quartz.TriggerBuilder;
import org.springframework.stereotype.Service;

import com.app.booking_system.entity.ClassSchedule;
import com.app.booking_system.job.ClassEndJob;
import com.app.booking_system.service.ClassSchedulerService;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ClassSchedulerServiceImpl implements ClassSchedulerService {

    private final Scheduler scheduler;

    @Override
    public void scheduleClassEndJob(ClassSchedule classSchedule) {
        try {
            JobKey jobKey = JobKey.jobKey("classEndJob_" + classSchedule.getId(), "classEndGroup");

            // Check if job already exists
            if (scheduler.checkExists(jobKey)) {
                return; // Job already scheduled
            }

            // Create job detail
            JobDetail jobDetail = JobBuilder.newJob(ClassEndJob.class)
                    .withIdentity(jobKey)
                    .usingJobData("classId", classSchedule.getId())
                    .build();

            // Create trigger to run at class end time
            Date endTime = Date.from(classSchedule.getEndTime().atZone(ZoneId.systemDefault()).toInstant());
            Trigger trigger = TriggerBuilder.newTrigger()
                    .withIdentity("classEndTrigger_" + classSchedule.getId(), "classEndGroup")
                    .startAt(endTime)
                    .build();

            // Schedule the job
            scheduler.scheduleJob(jobDetail, trigger);

        } catch (SchedulerException e) {
            throw new RuntimeException("Failed to schedule class end job for class: " + classSchedule.getId(), e);
        }
    }
}