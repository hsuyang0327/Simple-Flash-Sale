package com.flashsale.backend.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * @description JobCronRequest
 * @author Yang-Hsu
 * @date 2026/2/17 下午1:37
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class JobCronRequest extends JobRequest {

    @NotBlank(message = "CRON_EXPRESSION_EMPTY")
    private String cronExpression;
}
