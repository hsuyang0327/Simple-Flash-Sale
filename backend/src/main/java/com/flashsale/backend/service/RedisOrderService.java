package com.flashsale.backend.service;

import com.flashsale.backend.entity.Order;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.concurrent.TimeUnit;

/**
 * @description Redis Order Cache Service (db1)
 * @author Yang-Hsu
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class RedisOrderService {

    @Qualifier("redisTemplateDb1")
    private final RedisTemplate<String, Object> redisTemplate;

    private static final String KEY_PREFIX = "member:event:";

    private String buildKey(String memberId, String eventId) {
        return KEY_PREFIX + memberId + ":" + eventId;
    }

    /**
     * 儲存搶購成功的訂單快取
     */
    public void setOrderCache(String memberId, String eventId, Order order) {
        String key = buildKey(memberId, eventId);
        redisTemplate.opsForValue().set(key, order, 30, TimeUnit.MINUTES);
        log.info("Order cached in Redis (TTL=30min): key={}", key);
    }

    /**
     * 查詢搶購結果（輪詢用）
     */
    public Order getOrderCache(String memberId, String eventId) {
        String key = buildKey(memberId, eventId);
        return (Order) redisTemplate.opsForValue().get(key);
    }

    /**
     * 刪除訂單快取（回滾用）
     */
    public void deleteOrderCache(String memberId, String eventId) {
        String key = buildKey(memberId, eventId);
        redisTemplate.delete(key);
        log.info("Order cache deleted from Redis: key={}", key);
    }
}
