package com.flashsale.backend.dto.response;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * @description OrderClientDetailResponse
 * @author Yang-Hsu
 * @date 2026/2/23 下午1:49
 */
@Data
@Builder
public class OrderClientDetailResponse {
    private String orderId;
    private String productId;
    private String productName;
    private Integer quantity;
    private BigDecimal totalPrice;
    private String status;
    private LocalDateTime createdAt;
}
