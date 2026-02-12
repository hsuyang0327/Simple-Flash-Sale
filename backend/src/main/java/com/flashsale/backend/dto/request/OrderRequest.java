package com.flashsale.backend.dto.request;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * @description OrderRequest
 * @author Yang-Hsu
 * @date 2026/2/17 下午1:35
 */
@Data
public class OrderRequest {

    @NotBlank(message = "MEMBER_ID_EMPTY")
    private String memberId;

    @NotBlank(message = "EVENT_ID_EMPTY")
    private String eventId;

    @NotNull(message = "QUANTITY_EMPTY")
    @Min(value = 1, message = "QUANTITY_INVALID")
    private Integer quantity;
}
