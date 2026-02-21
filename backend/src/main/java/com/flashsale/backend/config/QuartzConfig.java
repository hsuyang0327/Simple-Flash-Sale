package com.flashsale.backend.config;

import com.flashsale.backend.job.PreloadEventsJob;
import com.flashsale.backend.job.SystemHeartbeatJob;
import org.quartz.*;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * @description Quartz Register
 * @author Yang-Hsu
 * @date 2026/2/18 下午7:26
 */
@Configuration
public class QuartzConfig {

    @Bean
    public JobDetail testJobDetail() {
        return JobBuilder.newJob(SystemHeartbeatJob.class)
                .withIdentity("systemHeartbeatJob", "SYSTEM_GROUP")
                .storeDurably()
                .build();
    }

    @Bean
    public JobDetail preloadEventsJobDetail() {
        return JobBuilder.newJob(PreloadEventsJob.class)
                .withIdentity("preloadEventsJob", "EVENT_GROUP")
                .storeDurably()
                .build();
    }
}
