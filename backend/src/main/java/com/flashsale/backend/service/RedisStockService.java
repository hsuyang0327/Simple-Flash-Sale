package com.flashsale.backend.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.stereotype.Service;

import java.util.Collections;

@Slf4j
@Service
@RequiredArgsConstructor
public class RedisStockService {

    private final RedisTemplate<String, Object> redisTemplate;

    private static final String DECREASE_STOCK_LUA = "local currentStock = redis.call('HGET', KEYS[1], 'stock') " +
            "if not currentStock then return -1 end " +
            "currentStock = tonumber(currentStock) " +
            "local requestQty = tonumber(ARGV[1]) " +
            "if currentStock <= 0 then return -3 end " +
            "if currentStock >= requestQty then " +
            "  local newStock = currentStock - requestQty " +
            "  redis.call('HSET', KEYS[1], 'stock', tostring(newStock)) " +
            "  return newStock " +
            "else return -2 end";

    private static final String INCREASE_STOCK_LUA = "local currentStock = redis.call('HGET', KEYS[1], 'stock') " +
            "if not currentStock then return -1 end " +
            "currentStock = tonumber(currentStock) " +
            "local requestQty = tonumber(ARGV[1]) " +
            "local newStock = currentStock + requestQty " +
            "redis.call('HSET', KEYS[1], 'stock', tostring(newStock)) " +
            "return newStock";

    /**
     * @return >= 0 成功 (剩餘庫存) | -1 key 不存在 | -2 數量不足 | -3 已售完 (stock=0)
     */
    public long decreaseStock(String productId, int quantity) {
        String key = "productId:" + productId;
        try {
            DefaultRedisScript<Long> script = new DefaultRedisScript<>(DECREASE_STOCK_LUA, Long.class);
            Long result = redisTemplate.execute(
                    script,
                    Collections.singletonList(key),
                    String.valueOf(quantity)
            );
            if (result == null) {
                log.error("Redis Lua script returned null for productId: {}", productId);
                return -1L;
            }
            if (result >= 0) {
                log.info("Stock reduced successfully. Event ID: {}, Reduced By: {}, Remaining Stock: {}", productId, quantity, result);
            } else {
                log.warn("Stock reduction failed. Event ID: {}, Requested Quantity: {}, result={}", productId, quantity, result);
            }
            return result;
        } catch (Exception e) {
            log.error("Exception occurred during Redis stock reduction for Event ID: {}", productId, e);
            return -1L;
        }
    }

    public void increaseStock(String productId, int quantity) {
        String key = "productId:" + productId;
        try {
            DefaultRedisScript<Long> script = new DefaultRedisScript<>(INCREASE_STOCK_LUA, Long.class);
            Long result = redisTemplate.execute(
                    script,
                    Collections.singletonList(key),
                    String.valueOf(quantity)
            );
            if (result == -1) {
                log.warn("Stock increase failed. Event ID: {}, Reason: Key not found", productId);
            } else {
                log.info("Stock increased successfully. Event ID: {}, Increased By: {}, New Stock: {}", productId, quantity, result);
            }
        } catch (Exception e) {
            log.error("Exception occurred during Redis stock increase for Event ID: {}", productId, e);
        }
    }
}
