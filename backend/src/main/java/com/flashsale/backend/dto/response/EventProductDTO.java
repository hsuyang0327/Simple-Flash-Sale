package com.flashsale.backend.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class EventProductDTO {
    private String productId;
    private String productName;
    private String description;
    private String eventId;
    private BigDecimal price;
    private Integer stock;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
}
