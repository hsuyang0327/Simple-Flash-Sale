package com.flashsale.backend.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/**
 * @description PaymentRequest
 * @author Yang-Hsu
 * @date 2026/2/21 下午4:30
 */
@Data
public class PaymentRequest {

    @NotBlank(message = "ORDER_ID_EMPTY")
    private String orderId;

    @NotBlank(message = "MEMBER_ID_EMPTY")
    private String memberId;
}
