package com.flashsale.backend.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.springframework.data.domain.Persistable;

import java.io.Serial;
import java.math.BigDecimal;

/**
 * @description Order
 * @author Yang-Hsu
 * @date 2026/2/17
 */
@Data
@EqualsAndHashCode(callSuper = true)
@Entity
@Table(name = "orders")
public class Order extends BaseEntity implements Persistable<String> {

    @Serial
    private static final long serialVersionUID = 1L;

    @Id
    @Column(name = "order_id", nullable = false, length = 36)
    private String orderId;

    @Column(name = "member_id", nullable = false, length = 36)
    private String memberId;

    @Column(name = "product_id", nullable = false, length = 36)
    private String productId;

    @Column(name = "event_id", nullable = false, length = 36)
    private String eventId;

    @Column(name = "quantity", nullable = false)
    private Integer quantity;

    @Column(name = "total_price", nullable = false)
    private BigDecimal totalPrice;

    @Column(name = "status", nullable = false, length = 20)
    private String status; // e.g., PENDING, COMPLETED, CANCELLED

    @Version
    private Long version;

    @Transient
    @JsonIgnore
    private boolean newEntity = true;

    @Override
    @JsonIgnore
    /**
     * @description Return the order UUID — required by Persistable interface
     * @author Yang-Hsu
     * @date 2026/7/9
     */
    public String getId() {
        return orderId;
    }

    @Override
    @JsonIgnore
    /**
     * @description Return true if order has not been persisted yet
     * @author Yang-Hsu
     * @date 2026/7/9
     */
    public boolean isNew() {
        return newEntity;
    }

    @PostLoad
    @PostPersist
    void markNotNew() {
        this.newEntity = false;
    }

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id", insertable = false, updatable = false)
    private Member member;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id", insertable = false, updatable = false)
    private Product product;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "event_id", insertable = false, updatable = false)
    private Event event;
}
