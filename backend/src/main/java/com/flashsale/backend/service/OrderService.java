package com.flashsale.backend.service;

import com.flashsale.backend.common.ResultCode;
import com.flashsale.backend.config.RabbitConfig;
import com.flashsale.backend.dto.request.OrderRequest;
import com.flashsale.backend.dto.request.PaymentRequest;
import com.flashsale.backend.dto.response.OrderAdminResponse;
import com.flashsale.backend.dto.response.OrderClientResponse;
import com.flashsale.backend.dto.response.OrderStatusResponse;
import com.flashsale.backend.entity.Event;
import com.flashsale.backend.entity.Member;
import com.flashsale.backend.entity.Order;
import com.flashsale.backend.entity.Product;
import com.flashsale.backend.exception.BusinessException;
import com.flashsale.backend.repository.EventRepository;
import com.flashsale.backend.repository.MemberRepository;
import com.flashsale.backend.repository.OrderRepository;
import com.flashsale.backend.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;


import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.orm.ObjectOptimisticLockingFailureException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

/**
 * @author Yang-Hsu
 * @description OrderService
 * @date 2026/2/17 下午1:25
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class OrderService {

    private final OrderRepository orderRepository;
    private final ProductRepository productRepository;
    private final MemberRepository memberRepository;
    private final EventRepository eventRepository;
    private final RedisStockService redisStockService;

    @Qualifier("redisTemplateDb0")
    private final RedisTemplate<String, Object> redisTemplateForStock;

    @Qualifier("redisTemplateDb1")
    private final RedisTemplate<String, Object> redisTemplateForOrder;

    private final RabbitTemplate rabbitTemplate;

    /**
     * @description Create Order (Client)
     * @author Yang-Hsu
     * @date 2026/2/17 下午1:26
     */
    @Transactional
    public Order createOrderDB(OrderRequest request) {
        log.info("Creating order for member: {}, event: {}", request.getMemberId(), request.getEventId());
        Event event = eventRepository.findById(request.getEventId())
                .orElseThrow(() -> new BusinessException(ResultCode.EVENT_NOT_FOUND));
        if (event.getStock() < request.getQuantity()) {
            throw new BusinessException(ResultCode.STOCK_INVALID);
        }
        int updatedRows = eventRepository.decreaseStock(event.getEventId(), request.getQuantity());
        if (updatedRows == 0) {
            throw new BusinessException(ResultCode.STOCK_INVALID);
        }
        Order order = new Order();
        order.setMemberId(request.getMemberId());
        order.setEventId(request.getEventId());
        order.setProductId(event.getProduct().getProductId());
        order.setQuantity(request.getQuantity());
        order.setTotalPrice(event.getPrice().multiply(BigDecimal.valueOf(request.getQuantity())));
        order.setStatus("PENDING");
        Order savedOrder = orderRepository.save(order);
        log.info("Order created successfully: {}", savedOrder.getOrderId());
        return savedOrder;
    }

    /**
     * @description Create Order (Redis)
     * @author Yang-Hsu
     * @date 2026/2/17 下午1:28
     */
    @Transactional
    public Order createOrderRedis(OrderRequest request) {
        log.info("Creating order (Redis) for member: {}, event: {}", request.getMemberId(), request.getEventId());
        Event event = eventRepository.findById(request.getEventId())
                .orElseThrow(() -> new BusinessException(ResultCode.EVENT_NOT_FOUND));
        boolean success = redisStockService.decreaseStock(event.getEventId(), request.getQuantity());
        if (!success) {
            throw new BusinessException(ResultCode.STOCK_INVALID);
        }
        Order order = new Order();
        order.setMemberId(request.getMemberId());
        order.setEventId(request.getEventId());
        order.setProductId(event.getProduct().getProductId());
        order.setQuantity(request.getQuantity());
        order.setTotalPrice(event.getPrice().multiply(BigDecimal.valueOf(request.getQuantity())));
        order.setStatus("PENDING");
        return orderRepository.save(order);
    }

    /**
     * @description createOrder(Not for Test)
     * @author Yang-Hsu
     * @date 2026/2/19 下午8:27
     */
    public Order createOrder(OrderRequest request) {
        log.info("Creating order (MQ) for member: {}, event: {}", request.getMemberId(), request.getEventId());

        // 1. Check Event existence and info
        Event event = eventRepository.findById(request.getEventId())
                .orElseThrow(() -> new BusinessException(ResultCode.EVENT_NOT_FOUND));
        // 2. Deduct Redis Stock
        boolean success = redisStockService.decreaseStock(event.getProduct().getProductId(), request.getQuantity());
        if (!success) {
            log.warn("Create Order (MQ) failed: Insufficient stock for product: {}", event.getProduct().getProductId());
            throw new BusinessException(ResultCode.STOCK_INVALID);
        }
        // 3. Prepare Order Object (Status: PENDING)
        Order order = new Order();
        order.setMemberId(request.getMemberId());
        order.setEventId(request.getEventId());
        order.setProductId(event.getProduct().getProductId());
        order.setQuantity(request.getQuantity());
        order.setTotalPrice(event.getPrice().multiply(BigDecimal.valueOf(request.getQuantity())));
        order.setStatus("PENDING");
        try {
            rabbitTemplate.convertAndSend(RabbitConfig.ORDER_EXCHANGE, RabbitConfig.ORDER_ROUTING_KEY, order);
            log.info("Order request sent to queue for member: {}", request.getMemberId());
        } catch (Exception e) {
            log.error("Failed to send order to MQ, restoring Redis stock.", e);
            redisStockService.increaseStock(event.getProduct().getProductId(), request.getQuantity());
            throw new BusinessException(ResultCode.SYSTEM_ERROR);
        }
        return order;
    }

    /**
     * @description Get Order Status from Redis
     * @author Yang-Hsu
     */
    public OrderStatusResponse getOrderStatusFromRedis(String memberId) {
        String orderKey = "member:order:" + memberId;
        Order order = (Order) redisTemplateForOrder.opsForValue().get(orderKey);

        if (order != null) {
            return OrderStatusResponse.builder()
                    .status("SUCCESS")
                    .order(order)
                    .build();
        } else {
            return OrderStatusResponse.builder()
                    .status("PENDING")
                    .build();
        }
    }

    /**
     * @description Update Order (Client)
     * @author Yang-Hsu
     * @date 2026/2/17 下午1:28
     */
    @Transactional
    public Order updateOrder(String orderId, OrderRequest request) {
        log.info("Updating order: {}", orderId);
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new BusinessException(ResultCode.ORDER_NOT_FOUND));
        if (!order.getMemberId().equals(request.getMemberId())) {
            throw new BusinessException(ResultCode.TOKEN_INVALID);
        }
        order.setQuantity(request.getQuantity());
        Event event = eventRepository.findById(order.getEventId())
                .orElseThrow(() -> new BusinessException(ResultCode.EVENT_NOT_FOUND));
        order.setTotalPrice(event.getPrice().multiply(BigDecimal.valueOf(request.getQuantity())));
        try {
            Order updatedOrder = orderRepository.save(order);
            log.info("Order updated successfully: {}", orderId);
            return updatedOrder;
        } catch (ObjectOptimisticLockingFailureException e) {
            log.warn("Update order failed due to concurrent modification: {}", orderId);
            throw new BusinessException(ResultCode.ORDER_IS_UPDATED_BY_OTHERS);
        }
    }

    /**
     * @description Get Order by ID (Client)
     * @author Yang-Hsu
     * @date 2026/2/17 下午1:30
     */
    @Transactional(readOnly = true)
    public Order getOrderById(String memberId, String orderId) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new BusinessException(ResultCode.ORDER_NOT_FOUND));

        if (!order.getMemberId().equals(memberId)) {
            throw new BusinessException(ResultCode.TOKEN_INVALID);
        }
        return order;
    }

    /**
     * @description Get Order by ID (Admin)
     * @author Yang-Hsu
     * @date 2026/2/17 下午1:31
     */
    @Transactional(readOnly = true)
    public Order getOrderByIdAdmin(String orderId) {
        return orderRepository.findById(orderId)
                .orElseThrow(() -> new BusinessException(ResultCode.ORDER_NOT_FOUND));
    }

    /**
     * @description Search Orders (Admin)
     * @author Yang-Hsu
     * @date 2026/2/17 下午1:31
     */
    @Transactional(readOnly = true)
    public Page<Order> searchOrders(String productName, String memberName, Pageable pageable) {
        return orderRepository.searchOrders(productName, memberName, pageable);
    }

    /**
     * @description Simulate payment success
     * @author Yang-Hsu
     * @date 2026/2/21 上午12:06
     */
    @Transactional
    public Order payOrder(PaymentRequest request) {
        log.info("Processing payment for order: {}, member: {}", request.getOrderId(), request.getMemberId());

        Order order = orderRepository.findByIdForUpdate(request.getOrderId())
                .orElseThrow(() -> new BusinessException(ResultCode.ORDER_NOT_FOUND));

        if (!order.getMemberId().equals(request.getMemberId())) {
            throw new BusinessException(ResultCode.TOKEN_INVALID);
        }

        if (!"PENDING".equals(order.getStatus())) {
            log.warn("Payment failed for order {}: status is not PENDING", request.getOrderId());
            throw new BusinessException(ResultCode.ORDER_STATUS_INVALID);
        }

        order.setStatus("PAID");
        Order savedOrder = orderRepository.save(order);
        log.info("Order {} status updated to PAID", request.getOrderId());
        return savedOrder;
    }

    public OrderClientResponse convertToClientResponse(Order order) {
        Product product = productRepository.findById(order.getProductId()).orElse(null);
        String productName = product != null ? product.getProductName() : "Unknown";

        return OrderClientResponse.builder()
                .orderId(order.getOrderId())
                .productName(productName)
                .quantity(order.getQuantity())
                .totalPrice(order.getTotalPrice())
                .status(order.getStatus())
                .createdAt(order.getCreatedAt())
                .build();
    }

    public OrderAdminResponse convertToAdminResponse(Order order) {
        Product product = productRepository.findById(order.getProductId()).orElse(null);
        Member member = memberRepository.findById(order.getMemberId()).orElse(null);
        return OrderAdminResponse.builder()
                .orderId(order.getOrderId())
                .memberId(order.getMemberId())
                .memberName(member != null ? member.getMemberName() : "Unknown")
                .productId(order.getProductId())
                .productName(product != null ? product.getProductName() : "Unknown")
                .quantity(order.getQuantity())
                .totalPrice(order.getTotalPrice())
                .status(order.getStatus())
                .createdAt(order.getCreatedAt())
                .updatedAt(order.getUpdatedAt())
                .build();
    }

}
