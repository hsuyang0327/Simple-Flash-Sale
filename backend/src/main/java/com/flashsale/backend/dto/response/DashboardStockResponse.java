package com.flashsale.backend.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @description DashboardStockResponse — Redis DB0 stock vs MySQL Event stock
 * @author Yang-Hsu
 * @date 2026/4/2
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class DashboardStockResponse {
    private String productId;
    private String productName;
    private String eventId;
    private Integer redisStock;
    private Integer dbStock;
}
