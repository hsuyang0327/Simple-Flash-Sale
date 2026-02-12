package com.flashsale.backend.service;

import com.flashsale.backend.common.ResultCode;
import com.flashsale.backend.dto.response.JobResponse;
import com.flashsale.backend.exception.BusinessException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.quartz.*;
import org.quartz.impl.matchers.GroupMatcher;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Set;

/**
 * @author Yang-Hsu
 * @description JobService
 * @date 2026/2/17 下午1:22
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class JobService {

    private final Scheduler scheduler;

    /**
     * @description Get all jobs (Admin)
     * @author Yang-Hsu
     * @date 2026/2/17 下午1:22
     */
    public List<JobResponse> getAllJobs() {
        List<JobResponse> jobList = new ArrayList<>();
        try {
            Set<JobKey> jobKeys = scheduler.getJobKeys(GroupMatcher.anyJobGroup());
            for (JobKey jobKey : jobKeys) {
                JobDetail jobDetail = scheduler.getJobDetail(jobKey);
                List<? extends Trigger> triggers = scheduler.getTriggersOfJob(jobKey);

                for (Trigger trigger : triggers) {
                    Trigger.TriggerState triggerState = scheduler.getTriggerState(trigger.getKey());
                    String cronExpression = "";
                    if (trigger instanceof CronTrigger cronTrigger) {
                        cronExpression = cronTrigger.getCronExpression();
                    }

                    jobList.add(JobResponse.builder()
                            .jobName(jobKey.getName())
                            .jobGroup(jobKey.getGroup())
                            .jobStatus(triggerState.name())
                            .cronExpression(cronExpression)
                            .previousFireTime(toLocalDateTime(trigger.getPreviousFireTime()))
                            .nextFireTime(toLocalDateTime(trigger.getNextFireTime()))
                            .description(jobDetail.getDescription())
                            .build());
                }
            }
        } catch (SchedulerException e) {
            log.error("Failed to get all jobs", e);
            throw new BusinessException(ResultCode.SYSTEM_ERROR);
        }
        return jobList;
    }

    /**
     * @description Pause job (Admin)
     * @author Yang-Hsu
     * @date 2026/2/17 下午1:23
     */
    public void pauseJob(String jobName, String jobGroup) {
        try {
            scheduler.pauseJob(JobKey.jobKey(jobName, jobGroup));
            log.info("Paused job: {}.{}", jobGroup, jobName);
        } catch (SchedulerException e) {
            log.error("Failed to pause job", e);
            throw new BusinessException(ResultCode.JOB_ACTION_FAILED);
        }
    }

    /**
     * @description Resume job (Admin)
     * @author Yang-Hsu
     * @date 2026/2/17 下午1:23
     */
    public void resumeJob(String jobName, String jobGroup) {
        try {
            scheduler.resumeJob(JobKey.jobKey(jobName, jobGroup));
            log.info("Resumed job: {}.{}", jobGroup, jobName);
        } catch (SchedulerException e) {
            log.error("Failed to resume job", e);
            throw new BusinessException(ResultCode.JOB_ACTION_FAILED);
        }
    }

    /**
     * @description Trigger job (Admin)
     * @author Yang-Hsu
     * @date 2026/2/17 下午1:24
     */
    public void triggerJob(String jobName, String jobGroup) {
        try {
            scheduler.triggerJob(JobKey.jobKey(jobName, jobGroup));
            log.info("Triggered job: {}.{}", jobGroup, jobName);
        } catch (SchedulerException e) {
            log.error("Failed to trigger job", e);
            throw new BusinessException(ResultCode.JOB_ACTION_FAILED);
        }
    }

    /**
     * @description Update job cron (Admin)
     * @author Yang-Hsu
     * @date 2026/2/17 下午1:24
     */
    public void updateJobCron(String jobName, String jobGroup, String cronExpression) {
        try {
            JobKey jobKey = JobKey.jobKey(jobName, jobGroup);
            List<? extends Trigger> triggers = scheduler.getTriggersOfJob(jobKey);
            if (triggers.isEmpty()) {
                throw new BusinessException(ResultCode.JOB_NOT_FOUND);
            }

            // Assuming one trigger per job for simplicity, or update all cron triggers
            for (Trigger trigger : triggers) {
                if (trigger instanceof CronTrigger cronTrigger) {
                    String oldCron = cronTrigger.getCronExpression();
                    if (!oldCron.equalsIgnoreCase(cronExpression)) {
                        CronScheduleBuilder scheduleBuilder = CronScheduleBuilder.cronSchedule(cronExpression);
                        CronTrigger newTrigger = TriggerBuilder.newTrigger()
                                .withIdentity(trigger.getKey())
                                .withSchedule(scheduleBuilder)
                                .build();
                        scheduler.rescheduleJob(trigger.getKey(), newTrigger);
                        log.info("Updated cron for job {}.{}: {} -> {}", jobGroup, jobName, oldCron, cronExpression);
                    }
                }
            }
        } catch (SchedulerException e) {
            log.error("Failed to update job cron", e);
            throw new BusinessException(ResultCode.JOB_ACTION_FAILED);
        }
    }

    /**
     * @description Convert Date to LocalDateTime
     * @author Yang-Hsu
     * @date 2026/2/17 下午1:24
     */
    private LocalDateTime toLocalDateTime(Date date) {
        if (date == null) return null;
        return date.toInstant().atZone(ZoneId.systemDefault()).toLocalDateTime();
    }
}
