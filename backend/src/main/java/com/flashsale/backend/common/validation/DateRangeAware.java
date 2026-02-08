package com.flashsale.backend.common.validation;

import java.time.LocalDateTime;

/**
 * @description DateRangeAware interface for validation
 * @author Yang-Hsu
 * @date 2026/2/8 下午4:48
 */
public interface DateRangeAware {
    LocalDateTime getStartTime();
    LocalDateTime getEndTime();
}
