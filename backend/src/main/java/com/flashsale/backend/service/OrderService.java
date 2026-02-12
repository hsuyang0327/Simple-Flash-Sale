package com.flashsale.backend.service;

import com.flashsale.backend.common.ResultCode;
import com.flashsale.backend.dto.request.OrderRequest;
import com.flashsale.backend.dto.response.OrderAdminResponse;
import com.flashsale.backend.dto.response.OrderClientResponse;
import com.flashsale.backend.entity.Member;
import com.flashsale.backend.entity.Order;
import com.flashsale.backend.entity.Product;
import com.flashsale.backend.exception.BusinessException;
import com.flashsale.backend.repository.MemberRepository;
import com.flashsale.backend.repository.OrderRepository;
import com.flashsale.backend.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.orm.ObjectOptimisticLockingFailureException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

/**
 * @author Yang-Hsu
 * @description OrderService
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class OrderService {

    private final OrderRepository orderRepository;
    private final ProductRepository productRepository;
    private final MemberRepository memberRepository;
    private final RedisStockService redisStockService;

    @Transactional
    public Order createOrderDB(OrderRequest request) {
        log.info("Creating order and product from DB for member: {}, product: {}", request.getMemberId(), request.getProductId());
        Product product = productRepository.findById(request.getProductId())
                .orElseThrow(() -> new BusinessException(ResultCode.PRODUCT_NOT_FOUND));
        if (product.getStock() < request.getQuantity()) {
            throw new BusinessException(ResultCode.STOCK_INVALID);
        }
        int updatedRows = productRepository.decreaseStock(product.getProductId(), request.getQuantity());
        if (updatedRows == 0) {
            throw new BusinessException(ResultCode.STOCK_INVALID);
        }
        Order order = new Order();
        order.setMemberId(request.getMemberId());
        order.setProductId(request.getProductId());
        order.setQuantity(request.getQuantity());
        order.setTotalPrice(product.getPrice().multiply(BigDecimal.valueOf(request.getQuantity())));
        order.setStatus("PENDING");

        Order savedOrder = orderRepository.save(order);
        log.info("Order From DB created successfully: {}", savedOrder.getOrderId());
        return savedOrder;
    }

    @Transactional
    public Order createOrderRedis(OrderRequest request) {
        log.info("Creating order and product from Redis for member: {}, product: {}", request.getMemberId(), request.getProductId());
        Product product = productRepository.findById(request.getProductId())
                .orElseThrow(() -> new BusinessException(ResultCode.PRODUCT_NOT_FOUND));
        boolean success = redisStockService.decreaseStock(request.getProductId(), request.getQuantity());
        if (!success) {
            throw new BusinessException(ResultCode.STOCK_INVALID);
        }
        Order order = new Order();
        order.setMemberId(request.getMemberId());
        order.setProductId(request.getProductId());
        order.setQuantity(request.getQuantity());
        order.setTotalPrice(product.getPrice().multiply(BigDecimal.valueOf(request.getQuantity())));
        order.setStatus("PENDING");
        Order savedOrder = orderRepository.save(order);
        log.info("Order from Redis created successfully: {}", savedOrder.getOrderId());
        return savedOrder;
    }


    /**
     * @description Update Order (Client) - e.g. cancel
     */
    @Transactional
    public Order updateOrder(String memberId, String orderId, OrderRequest request) {
        log.info("Updating order: {}", orderId);
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new BusinessException(ResultCode.ORDER_NOT_FOUND));

        if (!order.getMemberId().equals(memberId)) {
            throw new BusinessException(ResultCode.TOKEN_INVALID);
        }

        order.setQuantity(request.getQuantity());

        Product product = productRepository.findById(order.getProductId())
                .orElseThrow(() -> new BusinessException(ResultCode.PRODUCT_NOT_FOUND));
        order.setTotalPrice(product.getPrice().multiply(BigDecimal.valueOf(request.getQuantity())));

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
     */
    @Transactional(readOnly = true)
    public Order getOrderByIdAdmin(String orderId) {
        return orderRepository.findById(orderId)
                .orElseThrow(() -> new BusinessException(ResultCode.ORDER_NOT_FOUND));
    }

    /**
     * @description Search Orders (Admin)
     */
    @Transactional(readOnly = true)
    public Page<Order> searchOrders(String productName, String memberName, Pageable pageable) {
        return orderRepository.searchOrders(productName, memberName, pageable);
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
