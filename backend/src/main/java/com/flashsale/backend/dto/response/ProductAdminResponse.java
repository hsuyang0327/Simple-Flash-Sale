package com.flashsale.backend.dto.response;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * @description ProductAdminResponse
 * @author Yang-Hsu
 * @date 2026/2/17
 */
@Data
@Builder
public class ProductAdminResponse {
    private String productId;
    private String productName;
    private String description;
    private Integer status;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
