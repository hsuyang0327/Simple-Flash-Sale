package com.flashsale.backend.mq.consumer;

import com.flashsale.backend.config.RabbitConfig;
import com.flashsale.backend.entity.Order;
import com.flashsale.backend.repository.EventRepository;
import com.flashsale.backend.repository.OrderRepository;
import com.flashsale.backend.service.RedisStockService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.AmqpRejectAndDontRequeueException;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Slf4j
@Component
@RequiredArgsConstructor
public class OrderConsumer {

    private final OrderRepository orderRepository;
    private final EventRepository eventRepository;
    private final RedisStockService redisStockService;

    @Qualifier("redisTemplateDb0")
    private final RedisTemplate<String, Object> redisTemplateForStock;

    @Qualifier("redisTemplateDb1")
    private final RedisTemplate<String, Object> redisTemplateForOrder;

    private final RabbitTemplate rabbitTemplate;

    /**
     * @description Rabbit MQ Consumer (For Create Order)
     * @author Yang-Hsu
     * @date 2026/2/19 下午8:27
     */
    @RabbitListener(queues = RabbitConfig.ORDER_QUEUE)
    @Transactional
    public void processCreateOrder(Order order) {
        log.info("Processing order from MQ for member: {}", order.getMemberId());
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
            String memberOrderKey = "member:order:" + savedOrder.getMemberId();
            redisTemplateForOrder.opsForValue().set(memberOrderKey, savedOrder);
            log.info("Order processed and cached in Redis: {}", savedOrder.getOrderId());

            // 4. Send to TTL queue for delayed cancellation check
            rabbitTemplate.convertAndSend(
                    RabbitConfig.ORDER_EXCHANGE,
                    RabbitConfig.TTL_ROUTING_KEY,
                    savedOrder
            );
            log.info("Order {} sent to TTL queue for cancellation check.", savedOrder.getOrderId());

        } catch (Exception e) {
            log.error("Error processing create order: {}. Restoring Redis stock.", order.getOrderId(), e);
            redisStockService.increaseStock(order.getProductId(), order.getQuantity());
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

                // 3. Update status to FAILED
                order.setStatus("FAILED");
                orderRepository.save(order);

                // 4. Restore Stock
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
}
