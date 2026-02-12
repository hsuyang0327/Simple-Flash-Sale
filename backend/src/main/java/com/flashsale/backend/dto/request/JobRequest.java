package com.flashsale.backend.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/**
 * @description JobRequest
 * @author Yang-Hsu
 * @date 2026/2/17 下午1:37
 */
@Data
public class JobRequest {

    @NotBlank(message = "JOB_NAME_EMPTY")
    private String jobName;

    @NotBlank(message = "JOB_GROUP_EMPTY")
    private String jobGroup;
}
