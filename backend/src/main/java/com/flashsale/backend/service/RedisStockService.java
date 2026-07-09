package com.flashsale.backend.service;

public interface RedisStockService {

    long decreaseStock(String productId, int quantity);

    void increaseStock(String productId, int quantity);
}
