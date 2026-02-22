package com.flashsale.backend.controller;

import com.flashsale.backend.common.ApiResponse;
import com.flashsale.backend.common.ResultCode;
import com.flashsale.backend.dto.request.JobCronRequest;
import com.flashsale.backend.dto.request.JobRequest;
import com.flashsale.backend.dto.response.JobResponse;
import com.flashsale.backend.service.JobService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
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
@Tag(name = "Job Management", description = "Admin APIs for managing scheduled jobs (Quartz).")
@Slf4j
@RestController
@RequestMapping("/api/admin/jobs")
@RequiredArgsConstructor
public class JobController {

    private final JobService jobService;

    @Operation(summary = "List All Jobs", description = "Retrieves a list of all scheduled jobs and their current status.")
    @GetMapping
    public ResponseEntity<ApiResponse<List<JobResponse>>> listJobs() {
        log.info("API: List all jobs (Admin)");
        List<JobResponse> jobs = jobService.getAllJobs();
        return ResponseEntity.ok(new ApiResponse<>(ResultCode.SUCCESS, jobs));
    }

    @Operation(summary = "Pause Job", description = "Pauses a specific scheduled job.")
    @PostMapping("/pause")
    public ResponseEntity<ApiResponse<Void>> pauseJob(@Valid @RequestBody JobRequest request) {
        log.info("API: Pause job (Admin): {}.{}", request.getJobGroup(), request.getJobName());
        jobService.pauseJob(request.getJobName(), request.getJobGroup());
        return ResponseEntity.ok(ApiResponse.of(ResultCode.SUCCESS));
    }

    @Operation(summary = "Resume Job", description = "Resumes a paused scheduled job.")
    @PostMapping("/resume")
    public ResponseEntity<ApiResponse<Void>> resumeJob(@Valid @RequestBody JobRequest request) {
        log.info("API: Resume job (Admin): {}.{}", request.getJobGroup(), request.getJobName());
        jobService.resumeJob(request.getJobName(), request.getJobGroup());
        return ResponseEntity.ok(ApiResponse.of(ResultCode.SUCCESS));
    }

    @Operation(summary = "Trigger Job", description = "Manually triggers the execution of a specific job immediately.")
    @PostMapping("/trigger")
    public ResponseEntity<ApiResponse<Void>> triggerJob(@Valid @RequestBody JobRequest request) {
        log.info("API: Trigger job (Admin): {}.{}", request.getJobGroup(), request.getJobName());
        jobService.triggerJob(request.getJobName(), request.getJobGroup());
        return ResponseEntity.ok(ApiResponse.of(ResultCode.SUCCESS));
    }

    @Operation(summary = "Update Job Cron Schedule", description = "Updates the cron expression for a specific job to change its schedule.")
    @PostMapping("/cron")
    public ResponseEntity<ApiResponse<Void>> updateJobCron(@Valid @RequestBody JobCronRequest request) {
        log.info("API: Update job cron (Admin): {}.{} -> {}", request.getJobGroup(), request.getJobName(), request.getCronExpression());
        jobService.updateJobCron(request.getJobName(), request.getJobGroup(), request.getCronExpression());
        return ResponseEntity.ok(ApiResponse.of(ResultCode.SUCCESS));
    }
}
