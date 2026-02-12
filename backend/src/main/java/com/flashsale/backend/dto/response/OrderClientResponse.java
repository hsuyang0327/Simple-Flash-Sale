package com.flashsale.backend.dto.response;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * @author Yang-Hsu
 * @description OrderClientResponse
 * @date 2026/2/23 下午1:49
 */
@Data
@Builder
public class OrderClientResponse {
    private String orderId;
    private String productId;
    private Integer quantity;
    private BigDecimal totalPrice;
    private String status;
    private LocalDateTime createdAt;
}
