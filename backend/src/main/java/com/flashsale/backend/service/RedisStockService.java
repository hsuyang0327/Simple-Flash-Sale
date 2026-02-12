package com.flashsale.backend.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.stereotype.Service;

import java.util.Collections;

@Slf4j
@Service
@RequiredArgsConstructor
public class RedisStockService {

    // Injecting the custom Bean by name to ensure String serialization is used
    private final RedisTemplate<String, Object> redisTemplate;

    /**
     * LUA Script for Atomic Stock Reduction:
     * 1. Get current stock by Key.
     * 2. If Key doesn't exist or stock is insufficient, return -1.
     * 3. Otherwise, decrement the stock and return the remaining amount.
     */
    private static final String DECREASE_STOCK_LUA =
            "local currentStock = tonumber(redis.call('get', KEYS[1])); " +
                    "if not currentStock or currentStock < tonumber(ARGV[1]) then " +
                    "   return -1; " +
                    "end; " +
                    "return redis.call('decrby', KEYS[1], ARGV[1]);";

    public boolean decreaseStock(String productId, int quantity) {
        String key = "product:stock:" + productId;
        try {
            DefaultRedisScript<Long> script = new DefaultRedisScript<>(DECREASE_STOCK_LUA, Long.class);

            // Execute the script
            // KEYS[1] = key, ARGV[1] = quantity
            Long result = redisTemplate.execute(
                    script,
                    Collections.singletonList(key),
                    String.valueOf(quantity)
            );

            if (result == -1) {
                log.warn("Stock reduction failed. Product ID: {}, Requested Quantity: {}, Reason: Insufficient stock or Key not found", productId, quantity);
                return false;
            }

            log.info("Stock reduced successfully. Product ID: {}, Reduced By: {}, Remaining Stock: {}", productId, quantity, result);
            return true;

        } catch (Exception e) {
            log.error("Exception occurred during Redis stock reduction for Product ID: {}", productId, e);
            return false;
        }
    }
}
