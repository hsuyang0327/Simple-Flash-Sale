package com.flashsale.backend.mq.consumer;

import com.flashsale.backend.config.RabbitConfig;
import com.flashsale.backend.entity.Order;
import com.flashsale.backend.repository.EventRepository;
import com.flashsale.backend.repository.OrderRepository;
import com.flashsale.backend.service.RedisStockService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;

import java.util.Optional;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class OrderConsumerTest {

    @Mock
    private OrderRepository orderRepository;

    @Mock
    private EventRepository eventRepository;

    @Mock
    private RedisStockService redisStockService;

    @Mock
    private RedisTemplate<String, Object> redisTemplateForOrder;

    @Mock
    private ValueOperations<String, Object> valueOperations;

    @Mock
    private RabbitTemplate rabbitTemplate;

    @InjectMocks
    private OrderConsumer orderConsumer;

    @Test
    @DisplayName("處理訂單成功 - 應儲存訂單並扣減 MySQL 庫存")
    void processCreateOrder_Success() {
        String orderId = UUID.randomUUID().toString();
        String memberId = UUID.randomUUID().toString();
        String eventId = UUID.randomUUID().toString();
        String productId = UUID.randomUUID().toString();
        int quantity = 1;

        Order order = new Order();
        order.setOrderId(orderId);
        order.setMemberId(memberId);
        order.setEventId(eventId);
        order.setProductId(productId);
        order.setQuantity(quantity);

        // Mock repository save
        when(orderRepository.save(any(Order.class))).thenAnswer(invocation -> invocation.getArgument(0));
        
        // Mock MySQL stock decrease
        when(eventRepository.decreaseStock(eq(eventId), eq(quantity))).thenReturn(1);

        // Mock Redis operations (not need to connect redis)
        when(redisTemplateForOrder.opsForValue()).thenReturn(valueOperations);

        orderConsumer.processCreateOrder(order); //action

        //Judgement
        verify(orderRepository, times(1)).save(any(Order.class)); //step1 : save order success return order

        verify(eventRepository, times(1)).decreaseStock(eq(eventId), eq(quantity)); // step2 : decrease stock in event

        String memberOrderKey = "member:order:" + memberId;
        verify(valueOperations, times(1)).set(eq(memberOrderKey), any(Order.class)); //step3 : add pending order to redis

        verify(rabbitTemplate, times(1)).convertAndSend(
                eq(RabbitConfig.ORDER_EXCHANGE),
                eq(RabbitConfig.TTL_ROUTING_KEY),
                any(Order.class)
        ); //step 4 : send to ttl queue(MQ)
    }

    @Test
    @DisplayName("處理取消訂單 - 當訂單為 PENDING 時應取消並回補庫存")
    void processCancelOrder_PendingOrder_ShouldCancelAndRestoreStock() {
        String orderId = UUID.randomUUID().toString();
        String eventId = UUID.randomUUID().toString();
        String productId = UUID.randomUUID().toString();
        int quantity = 1;

        Order orderMessage = new Order();
        orderMessage.setOrderId(orderId);

        Order existingOrder = new Order();
        existingOrder.setOrderId(orderId);
        existingOrder.setEventId(eventId);
        existingOrder.setProductId(productId);
        existingOrder.setQuantity(quantity);
        existingOrder.setStatus("PENDING");

        when(orderRepository.findByIdForUpdate(orderId)).thenReturn(Optional.of(existingOrder)); // Mock findByIdForUpdate

        orderConsumer.processCancelOrder(orderMessage); //action

        //Judgement
        verify(orderRepository, times(1)).save(argThat(o -> "FAILED".equals(o.getStatus()))); // step1 : Verify status updated to FAILED

        verify(redisStockService, times(1)).increaseStock(productId, quantity); // step2 : Verify Redis stock restored

        verify(eventRepository, times(1)).increaseStock(eventId, quantity); // step3 : Verify MySQL stock restored
    }

    @Test
    @DisplayName("處理取消訂單 - 當訂單已支付時不應做任何事")
    void processCancelOrder_PaidOrder_ShouldDoNothing() {
        String orderId = UUID.randomUUID().toString();
        
        Order orderMessage = new Order();
        orderMessage.setOrderId(orderId);

        Order existingOrder = new Order();
        existingOrder.setOrderId(orderId);
        existingOrder.setStatus("PAID");

        when(orderRepository.findByIdForUpdate(orderId)).thenReturn(Optional.of(existingOrder));

        orderConsumer.processCancelOrder(orderMessage); // action

        //Judgement
        verify(orderRepository, never()).save(any());
        verify(redisStockService, never()).increaseStock(anyString(), anyInt());
        verify(eventRepository, never()).increaseStock(anyString(), anyInt());
    }
}
