package com.flashsale.backend.service;

/**
 * @description Redis stock management service interface (DB0) — Lua script atomic decrement/increment
 * @author Yang-Hsu
 * @date 2026/7/9
 */
public interface RedisStockService {

    long decreaseStock(String productId, int quantity);

    void increaseStock(String productId, int quantity);
}
