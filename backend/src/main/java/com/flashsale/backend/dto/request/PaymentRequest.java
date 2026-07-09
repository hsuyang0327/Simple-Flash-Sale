package com.flashsale.backend.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/**
 * @description PaymentRequest
 * @author Yang-Hsu
 * @date 2026/2/21
 */
@Data
public class PaymentRequest {

    @NotBlank(message = "ORDER_ID_EMPTY")
    private String orderId;

    private String memberId;
}
