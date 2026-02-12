package com.flashsale.backend.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.flashsale.backend.entity.Order;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class OrderStatusResponse {
    private String status;
    private Order order;
}
