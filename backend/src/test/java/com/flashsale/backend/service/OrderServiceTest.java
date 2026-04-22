package com.flashsale.backend.service;

import com.flashsale.backend.common.ResultCode;
import com.flashsale.backend.config.RabbitConfig;
import com.flashsale.backend.dto.request.OrderRequest;
import com.flashsale.backend.entity.Event;
import com.flashsale.backend.entity.Order;
import com.flashsale.backend.entity.Product;
import com.flashsale.backend.exception.BusinessException;
import com.flashsale.backend.repository.EventRepository;
import com.flashsale.backend.repository.OrderRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.amqp.AmqpException;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

/**
 * @description OrderServiceTest(By using mock not for db)
 * @author Yang-Hsu
 * @date 2026/2/22 下午3:06
 */
@ExtendWith(MockitoExtension.class)
class OrderServiceTest {

    @Mock
    private EventRepository eventRepository;

    @Mock
    private OrderRepository orderRepository;

    @Mock
    private RedisStockService redisStockService;

    @Mock
    private RedisOrderService redisOrderService;

    @Mock
    private RabbitTemplate rabbitTemplate;

    @InjectMocks
    private OrderService orderService;

    @Test
    @DisplayName("建立訂單成功 - 庫存足夠")
    void createOrder_Success() {
        // --- Arrange ---
        String eventId = UUID.randomUUID().toString();
        String memberId = UUID.randomUUID().toString();
        String productId = UUID.randomUUID().toString();
        int quantity = 1;

        OrderRequest request = new OrderRequest();
        request.setEventId(eventId);
        request.setMemberId(memberId);
        request.setQuantity(quantity);

        Product product = new Product();
        product.setProductId(productId);

        Event event = new Event();
        event.setEventId(eventId);
        event.setProduct(product);
        event.setPrice(new BigDecimal("100"));

        when(eventRepository.findById(eventId)).thenReturn(Optional.of(event));
        when(redisStockService.decreaseStock(productId, quantity)).thenReturn(1L);

        Order result = orderService.createOrder(request); // create order action

        //Judge the result is right or not
        assertNotNull(result);
        assertEquals("PENDING", result.getStatus());
        assertEquals(memberId, result.getMemberId());

        verify(rabbitTemplate, times(1)).convertAndSend(
                eq(RabbitConfig.ORDER_EXCHANGE),
                eq(RabbitConfig.ORDER_ROUTING_KEY),
                any(Order.class)
        ); //make sure will send to MQ
    }

    @Test
    @DisplayName("建立訂單失敗 - Redis 庫存不足")
    void createOrder_StockInvalid() {
        String eventId = UUID.randomUUID().toString();
        String productId = UUID.randomUUID().toString();
        int quantity = 100;

        OrderRequest request = new OrderRequest();
        request.setEventId(eventId);
        request.setQuantity(quantity);

        Product product = new Product();
        product.setProductId(productId);
        Event event = new Event();
        event.setEventId(eventId);
        event.setProduct(product);

        when(eventRepository.findById(eventId)).thenReturn(Optional.of(event));
        when(redisStockService.decreaseStock(productId, quantity)).thenReturn(-1L);

        BusinessException exception = assertThrows(BusinessException.class, () -> {
            orderService.createOrder(request);
        });

        assertEquals(ResultCode.STOCK_INVALID, exception.getResultCode()); //will return error code
        verify(rabbitTemplate, never()).convertAndSend(anyString(), anyString(), any(Object.class));
    }

    @Test
    @DisplayName("建立訂單失敗 - MQ 發送異常時應回補 Redis 庫存")
    void createOrder_MqFailure_Should_Restore_Redis_Stock() {
        String eventId = UUID.randomUUID().toString();
        String memberId = UUID.randomUUID().toString();
        String productId = UUID.randomUUID().toString();
        int quantity = 1;

        OrderRequest request = new OrderRequest();
        request.setEventId(eventId);
        request.setMemberId(memberId);
        request.setQuantity(quantity);

        Product product = new Product();
        product.setProductId(productId);
        
        Event event = new Event();
        event.setEventId(eventId);
        event.setProduct(product);
        event.setPrice(new BigDecimal("100"));

        when(eventRepository.findById(eventId)).thenReturn(Optional.of(event));
        when(redisStockService.decreaseStock(productId, quantity)).thenReturn(1L);
        
        // Simulate MQ failure
        doThrow(new AmqpException("MQ connection failed")).when(rabbitTemplate)
                .convertAndSend(eq(RabbitConfig.ORDER_EXCHANGE), eq(RabbitConfig.ORDER_ROUTING_KEY), any(Order.class));

        BusinessException exception = assertThrows(BusinessException.class, () -> {
            orderService.createOrder(request);
        });
        assertEquals(ResultCode.SYSTEM_ERROR, exception.getResultCode());

        verify(redisStockService, times(1)).increaseStock(productId, quantity);
    }

    @Test
    @DisplayName("查詢自己的訂單列表成功 - 依 memberId 分頁")
    void getOrdersByMemberId_Success() {
        String memberId = UUID.randomUUID().toString();
        Pageable pageable = PageRequest.of(0, 10);

        Order order = new Order();
        order.setOrderId(UUID.randomUUID().toString());
        order.setMemberId(memberId);
        Page<Order> expectedPage = new PageImpl<>(List.of(order), pageable, 1);

        when(orderRepository.findByMemberIdExcludingPending(memberId, pageable)).thenReturn(expectedPage);

        Page<Order> result = orderService.getOrdersByMemberId(memberId, pageable);

        assertNotNull(result);
        assertEquals(1, result.getTotalElements());
        assertEquals(memberId, result.getContent().get(0).getMemberId());
        verify(orderRepository, times(1)).findByMemberIdExcludingPending(memberId, pageable);
    }

    @Test
    @DisplayName("查詢下單狀態成功 - Redis 有訂單資料")
    void getOrderStatusFromRedis_Success() {
        String memberId = UUID.randomUUID().toString();
        String eventId = UUID.randomUUID().toString();
        Order order = new Order();
        order.setOrderId(UUID.randomUUID().toString());
        order.setMemberId(memberId);

        when(redisOrderService.getOrderCache(memberId, eventId)).thenReturn(order);

        var result = orderService.getOrderStatusFromRedis(memberId, eventId);

        assertNotNull(result);
        assertEquals("SUCCESS", result.getStatus());
        assertEquals(order, result.getOrder());
    }

    @Test
    @DisplayName("查詢下單狀態等待中 - Redis 尚無訂單資料")
    void getOrderStatusFromRedis_Pending() {
        String memberId = UUID.randomUUID().toString();
        String eventId = UUID.randomUUID().toString();

        when(redisOrderService.getOrderCache(memberId, eventId)).thenReturn(null);

        var result = orderService.getOrderStatusFromRedis(memberId, eventId);

        assertNotNull(result);
        assertEquals("PENDING", result.getStatus());
        assertNull(result.getOrder());
    }
}
