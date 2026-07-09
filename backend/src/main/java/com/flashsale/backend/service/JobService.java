package com.flashsale.backend.service;

import com.flashsale.backend.dto.response.JobResponse;

import java.util.List;

/**
 * @description Quartz job management service interface
 * @author Yang-Hsu
 * @date 2026/7/9
 */
public interface JobService {

    List<JobResponse> getAllJobs();

    void pauseJob(String jobName, String jobGroup);

    void resumeJob(String jobName, String jobGroup);

    void triggerJob(String jobName, String jobGroup);

    void updateJobCron(String jobName, String jobGroup, String cronExpression);
}
