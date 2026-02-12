package com.flashsale.backend.dto.request;

import com.flashsale.backend.common.validation.DateRangeAware;
import com.flashsale.backend.common.validation.ValidDateRange;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * @description ProductRequest(Just For Admin)
 * @author Yang-Hsu
 * @date 2026/2/8 下午4:46
 */
@Data
@ValidDateRange(message = "END_TIME_INVALID")
public class ProductRequest implements DateRangeAware {

    @NotBlank(message = "PRODUCT_NAME_EMPTY")
    private String productName;

    @NotNull(message = "PRICE_EMPTY")
    @Min(value = 0, message = "PRICE_INVALID")
    private BigDecimal price;

    @NotNull(message = "STOCK_EMPTY")
    @Min(value = 0, message = "STOCK_INVALID")
    private Integer stock;

    @Size(max = 255, message = "DESCRIPTION_TOO_LONG")
    private String description;

    @NotNull(message = "STATUS_INVALID")
    @Min(value = 0, message = "STATUS_INVALID")
    @Max(value = 2, message = "STATUS_INVALID")
    private Integer status;

    @NotNull(message = "START_TIME_EMPTY")
    private LocalDateTime startTime;

    @NotNull(message = "END_TIME_EMPTY")
    private LocalDateTime endTime;
}
