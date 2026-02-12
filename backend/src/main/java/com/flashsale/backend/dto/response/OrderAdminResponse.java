package com.flashsale.backend.dto.response;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * @description Order Response for Admin
 * @author Yang-Hsu
 */
@Data
@Builder
public class OrderAdminResponse {
    private String orderId;
    private String memberId;
    private String memberName;
    private String productId;
    private String productName;
    private Integer quantity;
    private BigDecimal totalPrice;
    private String status;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
