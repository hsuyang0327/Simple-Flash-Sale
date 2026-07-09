package com.flashsale.backend.service;

import com.flashsale.backend.entity.Order;

/**
 * @description Redis order cache service interface (DB1) — cache and retrieve order after MQ persistence
 * @author Yang-Hsu
 * @date 2026/7/9
 */
public interface RedisOrderService {

    void setOrderCache(String memberId, String eventId, Order order);

    Order getOrderCache(String memberId, String eventId);

    void deleteOrderCache(String memberId, String eventId);
}
