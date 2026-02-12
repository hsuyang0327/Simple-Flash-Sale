package com.flashsale.backend.dto.request;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * @description Order Request (Just For Client)
 * @author Yang-Hsu
 */
@Data
public class OrderRequest {

    @NotBlank(message = "PRODUCT_ID_EMPTY")
    private String productId;

    @NotNull(message = "QUANTITY_EMPTY")
    @Min(value = 1, message = "QUANTITY_INVALID")
    private Integer quantity;

    private String memberId; //Just for Jmeter Test
}
