package com.flashsale.backend.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serial;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * @description Product Entity
 * @author Yang-Hsu
 * @date 2026/2/8 下午4:47
 */
@Data
@EqualsAndHashCode(callSuper = true)
@Entity
@Table(name = "product")
public class Product extends BaseEntity {

    @Serial
    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "product_id", nullable = false, length = 36)
    private String productId;

    @Column(name = "product_name", nullable = false, length = 100)
    private String productName;

    @Column(name = "price", nullable = false)
    private BigDecimal price;

    @Column(name = "stock", nullable = false)
    private Integer stock;

    @Column(name = "description", length = 255)
    private String description;

    @Column(name = "status", nullable = false)
    private Integer status = 1; //Product Status: 0-OFF_SHELF, 1-ON_SHELF, 2-BANNED

    @Column(name = "start_time")
    private LocalDateTime startTime;

    @Column(name = "end_time")
    private LocalDateTime endTime;

    @Version
    private Long version;
}
