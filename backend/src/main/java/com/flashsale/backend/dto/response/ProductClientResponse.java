package com.flashsale.backend.dto.response;

import lombok.Builder;
import lombok.Data;

/**
 * @description ProductClientResponse
 * @author Yang-Hsu
 * @date 2026/2/17 下午1:35
 */
@Data
@Builder
public class ProductClientResponse {
    private String productId;
    private String productName;
    private String description;
}
