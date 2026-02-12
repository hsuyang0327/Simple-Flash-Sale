package com.flashsale.backend.dto.request;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * @description ProductRequest
 * @author Yang-Hsu
 * @date 2026/2/17 下午1:36
 */
@Data
public class ProductRequest {
    @NotBlank(message = "PRODUCT_NAME_EMPTY")
    private String productName;
    private String description;
    @NotNull(message = "STATUS_INVALID")
    @Min(value = 0, message = "STATUS_INVALID")
    @Max(value = 2, message = "STATUS_INVALID")
    private Integer status;
}
