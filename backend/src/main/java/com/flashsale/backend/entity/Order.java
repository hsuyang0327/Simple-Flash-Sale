package com.flashsale.backend.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serial;
import java.math.BigDecimal;

/**
 * @description Order Entity
 * @author Yang-Hsu
 * @date 2026/2/12 下午9:45
 */
@Data
@EqualsAndHashCode(callSuper = true)
@Entity
@Table(name = "orders")
public class Order extends BaseEntity {

    @Serial
    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "order_id", nullable = false, length = 36)
    private String orderId;

    @Column(name = "member_id", nullable = false, length = 36)
    private String memberId;

    @Column(name = "product_id", nullable = false, length = 36)
    private String productId;

    @Column(name = "quantity", nullable = false)
    private Integer quantity;

    @Column(name = "total_price", nullable = false)
    private BigDecimal totalPrice;

    @Column(name = "status", nullable = false, length = 20)
    private String status; // e.g., PENDING, COMPLETED, CANCELLED

    @Version
    private Long version;
}
