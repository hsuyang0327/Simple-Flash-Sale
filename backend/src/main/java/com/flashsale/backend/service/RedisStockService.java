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
            "if currentStock >= requestQty then " +
            "  local newStock = currentStock - requestQty " +
            "  redis.call('HSET', KEYS[1], 'stock', tostring(newStock)) " +
            "  return newStock " +
            "else return -2 end";

    public boolean decreaseStock(String productId, int quantity) {
        String key = "productId:" + productId;
        try {
            DefaultRedisScript<Long> script = new DefaultRedisScript<>(DECREASE_STOCK_LUA, Long.class);
            Long result = redisTemplate.execute(
                    script,
                    Collections.singletonList(key),
                    String.valueOf(quantity)
            );
            if (result == -1) {
                log.warn("Stock reduction failed. Event ID: {}, Requested Quantity: {}, Reason: Insufficient stock or Key not found", productId, quantity);
                return false;
            }
            log.info("Stock reduced successfully. Event ID: {}, Reduced By: {}, Remaining Stock: {}", productId, quantity, result);
            return true;

        } catch (Exception e) {
            log.error("Exception occurred during Redis stock reduction for Event ID: {}", productId, e);
            return false;
        }
    }
}
