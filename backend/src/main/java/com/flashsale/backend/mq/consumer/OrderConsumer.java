package com.flashsale.backend.mq.consumer;

import com.flashsale.backend.config.RabbitConfig;
import com.flashsale.backend.entity.DeadLetterLog;
import com.flashsale.backend.entity.Order;
import com.flashsale.backend.repository.DeadLetterLogRepository;
import com.flashsale.backend.repository.EventRepository;
import com.flashsale.backend.repository.OrderRepository;
import com.flashsale.backend.service.RedisOrderService;
import com.flashsale.backend.service.RedisStockService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.AmqpRejectAndDontRequeueException;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import java.util.Optional;

@Slf4j
@Component
@RequiredArgsConstructor
public class OrderConsumer {

    private final OrderRepository orderRepository;
    private final EventRepository eventRepository;
    private final RedisStockService redisStockService;
    private final RedisOrderService redisOrderService;
    private final RabbitTemplate rabbitTemplate;
    private final DeadLetterLogRepository deadLetterLogRepository;

    /**
     * @description Rabbit MQ Consumer (For Create Order)
     * @author Yang-Hsu
     * @date 2026/2/19
     */
    @RabbitListener(containerFactory = "noRetryContainerFactory", queues = RabbitConfig.ORDER_QUEUE)
    @Transactional
    public void processCreateOrder(Order order) {
        log.info("Processing order from MQ for member: {}", order.getMemberId());
        // Idempotency check: skip if already persisted (e.g. duplicate delivery after retry)
        if (order.getOrderId() != null && orderRepository.existsById(order.getOrderId())) {
            log.warn("Duplicate order message, skipping: {}", order.getOrderId());
            return;
        }
        try {
            // 1. Save the order
            Order savedOrder = orderRepository.save(order);

            // 2. Decrease stock in MySQL
            int updatedRows = eventRepository.decreaseStock(order.getEventId(), order.getQuantity());
            if (updatedRows == 0) {
                // This case should be rare if Redis stock is accurate, but it's a good safeguard.
                log.error("Failed to decrease MySQL stock for event: {}. Stock might be insufficient.", order.getEventId());
                // Manually trigger rollback and restore Redis stock
                throw new IllegalStateException("MySQL stock inconsistency detected.");
            }

            // 3. Cache order status in Redis
            redisOrderService.setOrderCache(savedOrder.getMemberId(), savedOrder.getEventId(), savedOrder);
            log.info("Order processed and cached in Redis: {}", savedOrder.getOrderId());

            // 4. Send to TTL queue after DB commit to prevent orphan TTL messages on rollback
            final Order committedOrder = savedOrder;
            if (TransactionSynchronizationManager.isSynchronizationActive()) {
                TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
                    @Override
                    public void afterCommit() {
                        rabbitTemplate.convertAndSend(
                                RabbitConfig.ORDER_EXCHANGE,
                                RabbitConfig.TTL_ROUTING_KEY,
                                committedOrder
                        );
                        log.info("Order {} sent to TTL queue for cancellation check.", committedOrder.getOrderId());
                    }
                });
            } else {
                // fallback for non-transactional context (e.g. tests)
                rabbitTemplate.convertAndSend(RabbitConfig.ORDER_EXCHANGE, RabbitConfig.TTL_ROUTING_KEY, committedOrder);
                log.info("Order {} sent to TTL queue for cancellation check.", committedOrder.getOrderId());
            }

        } catch (Exception e) {
            log.error("Error processing create order: {}. Restoring Redis stock.", order.getOrderId(), e);
            redisStockService.increaseStock(order.getProductId(), order.getQuantity());
            redisOrderService.deleteOrderCache(order.getMemberId(), order.getEventId());
            throw new AmqpRejectAndDontRequeueException("Error processing create order", e);
        }
    }

    /**
     * @description Rabbit MQ Consumer for Cancel Order
     * @author Yang-Hsu
     */
    @RabbitListener(queues = RabbitConfig.CANCEL_QUEUE)
    @Transactional
    public void processCancelOrder(Order orderMessage) {
        log.info("Processing cancel order check for order: {}", orderMessage.getOrderId());
        try {
            // 1. Lock the order row for update
            Optional<Order> orderOpt = orderRepository.findByIdForUpdate(orderMessage.getOrderId());
            if (orderOpt.isEmpty()) {
                log.warn("Order not found during cancel check: {}", orderMessage.getOrderId());
                return; // Order already deleted or never existed, acknowledge and finish.
            }
            Order order = orderOpt.get();

            // 2. Check status
            if ("PENDING".equals(order.getStatus())) {
                log.info("Order {} is still PENDING. Cancelling...", order.getOrderId());

                // 3. Update status to TIMEOUT (payment window expired)
                order.setStatus("TIMEOUT");
                orderRepository.save(order);

                // 4. Evict Redis order cache so the next read falls through to MySQL
                redisOrderService.deleteOrderCache(order.getMemberId(), order.getEventId());

                // 5. Restore Stock
                eventRepository.increaseStock(order.getEventId(), order.getQuantity());
                redisStockService.increaseStock(order.getProductId(), order.getQuantity());
                log.info("Order {} cancelled and stock restored.", order.getOrderId());
            } else {
                log.info("Order {} status is {}, no need to cancel.", order.getOrderId(), order.getStatus());
            }

        } catch (Exception e) {
            log.error("Error processing cancel order: {}. It will be retried.", orderMessage.getOrderId(), e);
            throw e;
        }
    }

    /**
     * @description DLQ consumer — records dead-lettered orders into dead_letter_log and restores stock
     */
    @RabbitListener(queues = { RabbitConfig.ORDER_DLQ, RabbitConfig.CANCEL_DLQ })
    @Transactional
    public void handleDeadLetter(Order order, Message message) {
        String sourceQueue = message.getMessageProperties().getConsumerQueue();
        Object xDeath = message.getMessageProperties().getHeaders().get("x-death");
        String failReason = xDeath != null ? xDeath.toString() : "unknown";

        log.error("[DLQ] Dead-lettered order: {}, from: {}", order.getOrderId(), sourceQueue);

        DeadLetterLog record = new DeadLetterLog();
        record.setMemberId(order.getMemberId());
        record.setEventId(order.getEventId());
        record.setProductId(order.getProductId());
        record.setQuantity(order.getQuantity());
        record.setSourceQueue(sourceQueue);
        record.setFailReason(failReason);
        deadLetterLogRepository.save(record);

        // CANCEL_DLQ: processCancelOrder failed mid-way, @Transactional rolled back.
        // The order may still be PENDING in DB — restore stock to prevent permanent stock loss.
        if (RabbitConfig.CANCEL_DLQ.equals(sourceQueue) && order.getOrderId() != null) {
            orderRepository.findById(order.getOrderId()).ifPresent(dbOrder -> {
                if ("PENDING".equals(dbOrder.getStatus())) {
                    log.warn("[DLQ] Order {} still PENDING after cancel failure. Forcing TIMEOUT and restoring stock.", dbOrder.getOrderId());
                    dbOrder.setStatus("TIMEOUT");
                    orderRepository.save(dbOrder);
                    eventRepository.increaseStock(dbOrder.getEventId(), dbOrder.getQuantity());
                    redisStockService.increaseStock(dbOrder.getProductId(), dbOrder.getQuantity());
                    redisOrderService.deleteOrderCache(dbOrder.getMemberId(), dbOrder.getEventId());
                    log.warn("[DLQ] Stock restored for order: {}", dbOrder.getOrderId());
                }
            });
        }
    }
}
