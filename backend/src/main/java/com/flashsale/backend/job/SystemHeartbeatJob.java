package com.flashsale.backend.job;

import lombok.extern.slf4j.Slf4j;
import org.quartz.JobExecutionContext;
import org.springframework.lang.NonNull;
import org.springframework.scheduling.quartz.QuartzJobBean;
import org.springframework.stereotype.Component;

/**
 * @description Quartz job for system heartbeat — periodic health check logging
 * @author Yang-Hsu
 * @date 2026/7/9
 */
@Slf4j
@Component
public class SystemHeartbeatJob extends QuartzJobBean {
    @Override
    /**
     * @description Log a periodic heartbeat to confirm scheduler is alive
     * @author Yang-Hsu
     * @date 2026/7/9
     */
    protected void executeInternal(@NonNull JobExecutionContext context) {
        String jobName = context.getJobDetail().getKey().getName();
        String triggerName = context.getTrigger().getKey().getName();

        Runtime runtime = Runtime.getRuntime();
        long totalMemory = runtime.totalMemory() / 1024 / 1024;
        long freeMemory = runtime.freeMemory() / 1024 / 1024;

        log.info("======= [Quartz Heartbeat] =======");
        log.info("Job Name    : {}", jobName);
        log.info("Trigger     : {}", triggerName);
        log.info("Execution ID: {}", context.getFireInstanceId());
        log.info("Thread Name : {}", Thread.currentThread().getName());
        log.info("Memory Usage: {}MB / {}MB (Free/Total)", freeMemory, totalMemory);
        log.info("Next Fire At: {}", context.getNextFireTime());
        log.info("==================================");
    }
}
