package com.flashsale.backend.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class PaymentRequest {

    @NotBlank(message = "ORDER_ID_EMPTY")
    private String orderId;

    @NotBlank(message = "MEMBER_ID_EMPTY")
    private String memberId;
}
