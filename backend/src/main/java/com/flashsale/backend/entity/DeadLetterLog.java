package com.flashsale.backend.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serial;

/**
 * @description DeadLetterLog — records orders that exhausted all MQ retries and landed in DLQ
 * @author Yang-Hsu
 * @date 2026/7/9
 */
@Data
@EqualsAndHashCode(callSuper = true)
@Entity
@Table(name = "dead_letter_log")
public class DeadLetterLog extends BaseEntity {

    @Serial
    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id", nullable = false, length = 36)
    private String id;

    @Column(name = "member_id", length = 36)
    private String memberId;

    @Column(name = "event_id", length = 36)
    private String eventId;

    @Column(name = "product_id", length = 36)
    private String productId;

    @Column(name = "quantity")
    private Integer quantity;

    @Column(name = "source_queue", length = 100)
    private String sourceQueue;

    @Column(name = "fail_reason", columnDefinition = "TEXT")
    private String failReason;
}
