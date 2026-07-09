package com.flashsale.backend.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.flashsale.backend.entity.Order;
import lombok.Builder;
import lombok.Data;

/**
 * @description Response DTO for polling flash-sale order status from Redis cache
 * @author Yang-Hsu
 * @date 2026/7/9
 */
@Data
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class OrderStatusResponse {
    private String status;
    private Order order;
}
