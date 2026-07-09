package com.flashsale.backend.service;

import com.flashsale.backend.entity.Order;

public interface RedisOrderService {

    void setOrderCache(String memberId, String eventId, Order order);

    Order getOrderCache(String memberId, String eventId);

    void deleteOrderCache(String memberId, String eventId);
}
