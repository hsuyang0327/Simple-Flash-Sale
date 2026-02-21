package com.flashsale.backend.controller;

import com.flashsale.backend.common.ApiResponse;
import com.flashsale.backend.common.ResultCode;
import com.flashsale.backend.dto.request.JobCronRequest;
import com.flashsale.backend.dto.request.JobRequest;
import com.flashsale.backend.dto.response.JobResponse;
import com.flashsale.backend.service.JobService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * @author Yang-Hsu
 * @description JobController
 * @date 2026/2/17 下午1:38
 */
@Slf4j
@RestController
@RequestMapping("/api/admin/jobs")
@RequiredArgsConstructor
public class JobController {

    private final JobService jobService;

    /**
     * @description listJobs
     * @author Yang-Hsu
     * @date 2026/2/17 下午1:38
     */
    @GetMapping
    public ResponseEntity<ApiResponse<List<JobResponse>>> listJobs() {
        log.info("API: List all jobs (Admin)");
        List<JobResponse> jobs = jobService.getAllJobs();
        return ResponseEntity.ok(new ApiResponse<>(ResultCode.SUCCESS, jobs));
    }

    /**
     * @description pauseJob
     * @author Yang-Hsu
     * @date 2026/2/17 下午1:38
     */
    @PostMapping("/pause")
    public ResponseEntity<ApiResponse<Void>> pauseJob(@Valid @RequestBody JobRequest request) {
        log.info("API: Pause job (Admin): {}.{}", request.getJobGroup(), request.getJobName());
        jobService.pauseJob(request.getJobName(), request.getJobGroup());
        return ResponseEntity.ok(ApiResponse.of(ResultCode.SUCCESS));
    }

    /**
     * @description resumeJob
     * @author Yang-Hsu
     * @date 2026/2/17 下午1:38
     */
    @PostMapping("/resume")
    public ResponseEntity<ApiResponse<Void>> resumeJob(@Valid @RequestBody JobRequest request) {
        log.info("API: Resume job (Admin): {}.{}", request.getJobGroup(), request.getJobName());
        jobService.resumeJob(request.getJobName(), request.getJobGroup());
        return ResponseEntity.ok(ApiResponse.of(ResultCode.SUCCESS));
    }

    /**
     * @description triggerJob
     * @author Yang-Hsu
     * @date 2026/2/17 下午1:39
     */
    @PostMapping("/trigger")
    public ResponseEntity<ApiResponse<Void>> triggerJob(@Valid @RequestBody JobRequest request) {
        log.info("API: Trigger job (Admin): {}.{}", request.getJobGroup(), request.getJobName());
        jobService.triggerJob(request.getJobName(), request.getJobGroup());
        return ResponseEntity.ok(ApiResponse.of(ResultCode.SUCCESS));
    }

    /**
     * @description updateJobCron
     * @author Yang-Hsu
     * @date 2026/2/17 下午1:39
     */
    @PostMapping("/cron")
    public ResponseEntity<ApiResponse<Void>> updateJobCron(@Valid @RequestBody JobCronRequest request) {
        log.info("API: Update job cron (Admin): {}.{} -> {}", request.getJobGroup(), request.getJobName(), request.getCronExpression());
        jobService.updateJobCron(request.getJobName(), request.getJobGroup(), request.getCronExpression());
        return ResponseEntity.ok(ApiResponse.of(ResultCode.SUCCESS));
    }
}
