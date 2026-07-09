package com.flashsale.backend.service;

import com.flashsale.backend.dto.response.JobResponse;

import java.util.List;

public interface JobService {

    List<JobResponse> getAllJobs();

    void pauseJob(String jobName, String jobGroup);

    void resumeJob(String jobName, String jobGroup);

    void triggerJob(String jobName, String jobGroup);

    void updateJobCron(String jobName, String jobGroup, String cronExpression);
}
