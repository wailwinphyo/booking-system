package com.app.booking_system.config;

import org.quartz.*;
import org.quartz.spi.JobFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.quartz.SchedulerFactoryBean;
import org.springframework.scheduling.quartz.SpringBeanJobFactory;

import com.app.booking_system.job.ClassEndJob;

@Configuration
public class QuartzConfig {

    @Bean
    public JobDetail classEndJobDetail() {
        return JobBuilder.newJob(ClassEndJob.class)
                .withIdentity("classEndJob")
                .storeDurably()
                .build();
    }

    @Bean
    public JobFactory jobFactory(ApplicationContext applicationContext) {
        SpringBeanJobFactory jobFactory = new SpringBeanJobFactory();
        jobFactory.setApplicationContext(applicationContext);
        return jobFactory;
    }

    @Bean
    public SchedulerFactoryBean schedulerFactoryBean(JobFactory jobFactory) {
        SchedulerFactoryBean schedulerFactoryBean = new SchedulerFactoryBean();
        schedulerFactoryBean.setJobDetails(classEndJobDetail());
        schedulerFactoryBean.setJobFactory(jobFactory);
        schedulerFactoryBean.setAutoStartup(true);
        return schedulerFactoryBean;
    }
}