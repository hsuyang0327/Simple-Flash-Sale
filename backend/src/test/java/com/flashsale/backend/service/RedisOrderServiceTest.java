package com.flashsale.backend.service;

import com.flashsale.backend.entity.Order;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;

import java.util.UUID;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * @description RedisOrderServiceTest(By using mock not for db)
 * @author Yang-Hsu
 * @date 2026/7/17
 */
@ExtendWith(MockitoExtension.class)
class RedisOrderServiceTest {

    @Mock
    private RedisTemplate<String, Object> redisTemplate;

    @Mock
    private ValueOperations<String, Object> valueOperations;

    @InjectMocks
    private RedisOrderServiceImpl redisOrderService;

    @Test
    @DisplayName("儲存訂單快取 - 使用正確的 key 與 TTL")
    void setOrderCache_writesValueWithTtl() {
        String memberId = UUID.randomUUID().toString();
        String eventId = UUID.randomUUID().toString();
        Order order = new Order();
        order.setOrderId(UUID.randomUUID().toString());

        when(redisTemplate.opsForValue()).thenReturn(valueOperations);

        redisOrderService.setOrderCache(memberId, eventId, order);

        String expectedKey = "member:event:" + memberId + ":" + eventId;
        verify(valueOperations, times(1)).set(expectedKey, order, 30, TimeUnit.MINUTES);
    }

    @Test
    @DisplayName("查詢訂單快取 - 回傳快取的訂單")
    void getOrderCache_returnsCachedOrder() {
        String memberId = UUID.randomUUID().toString();
        String eventId = UUID.randomUUID().toString();
        Order order = new Order();
        order.setOrderId(UUID.randomUUID().toString());
        String expectedKey = "member:event:" + memberId + ":" + eventId;

        when(redisTemplate.opsForValue()).thenReturn(valueOperations);
        when(valueOperations.get(expectedKey)).thenReturn(order);

        Order result = redisOrderService.getOrderCache(memberId, eventId);

        assertEquals(order, result);
    }

    @Test
    @DisplayName("刪除訂單快取 - 刪除正確的 key")
    void deleteOrderCache_deletesKey() {
        String memberId = UUID.randomUUID().toString();
        String eventId = UUID.randomUUID().toString();
        String expectedKey = "member:event:" + memberId + ":" + eventId;

        redisOrderService.deleteOrderCache(memberId, eventId);

        verify(redisTemplate, times(1)).delete(expectedKey);
    }
}
