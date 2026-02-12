package com.flashsale.backend.dto.response;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * @description JobResponse
 * @author Yang-Hsu
 * @date 2026/2/17 下午1:36
 */
@Data
@Builder
public class JobResponse {
    private String jobName;
    private String jobGroup;
    private String jobStatus; // NONE, NORMAL, PAUSED, COMPLETE, ERROR, BLOCKED
    private String cronExpression;
    private LocalDateTime previousFireTime;
    private LocalDateTime nextFireTime;
    private String description;
}
