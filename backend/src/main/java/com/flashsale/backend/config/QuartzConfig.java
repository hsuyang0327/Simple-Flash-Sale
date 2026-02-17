package com.flashsale.backend.config;

import com.flashsale.backend.job.SystemHeartbeatJob;
import org.quartz.*;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class QuartzConfig {
    @Bean
    public JobDetail testJobDetail() {
        return JobBuilder.newJob(SystemHeartbeatJob.class)
                .withIdentity("systemHeartbeatJob","SYSTEM_GROUP")
                .storeDurably()
                .build();
    }
}
