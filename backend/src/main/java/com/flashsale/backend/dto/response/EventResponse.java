package com.flashsale.backend.dto.response;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * @description EventResponse
 * @author Yang-Hsu
 * @date 2026/2/21 下午4:30
 */
@Data
@Builder
public class EventResponse {
    private String eventId;
    private BigDecimal price;
    private Integer stock;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private Integer status;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
