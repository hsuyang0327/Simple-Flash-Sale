package com.flashsale.backend.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serial;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * @description Event Entity
 * @author Yang-Hsu
 * @date 2026/2/17 下午10:04
 */
@Data
@EqualsAndHashCode(callSuper = true)
@Entity
@Table(name = "events")
public class Event extends BaseEntity {

    @Serial
    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "event_id", nullable = false, length = 36)
    private String eventId;

    @Column(name = "product_id", nullable = false, length = 36)
    private String productId;

    @Column(name = "price", nullable = false)
    private BigDecimal price;

    @Column(name = "stock", nullable = false)
    private Integer stock;

    @Column(name = "start_time", nullable = false)
    private LocalDateTime startTime;

    @Column(name = "end_time", nullable = false)
    private LocalDateTime endTime;

    @Column(name = "status", nullable = false)
    private Integer status = 0; // 0-INACTIVE, 1-ACTIVE, 2-ENDED

    @Version
    private Long version;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id", insertable = false, updatable = false)
    private Product product;
}
