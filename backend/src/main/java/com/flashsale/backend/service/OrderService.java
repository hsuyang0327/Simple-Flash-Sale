package com.flashsale.backend.service;

import com.flashsale.backend.dto.request.OrderRequest;
import com.flashsale.backend.dto.request.PaymentRequest;
import com.flashsale.backend.dto.response.OrderClientDetailResponse;
import com.flashsale.backend.dto.response.OrderStatusResponse;
import com.flashsale.backend.entity.Order;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface OrderService {

    Order createOrderDB(OrderRequest request);

    Order createOrderRedis(OrderRequest request);

    Order createOrder(OrderRequest request);

    OrderStatusResponse getOrderStatusFromRedis(String memberId, String eventId);

    Order cancelOrder(String orderId, String memberId);

    Page<Order> getOrdersByMemberId(String memberId, Pageable pageable);

    Order getOrderDetailsByIdClient(String memberId, String orderId);

    Page<Order> searchOrders(String productName, String memberName, Pageable pageable);

    Order payOrder(PaymentRequest request);

    Order getOrderDetailsByIdAdmin(String orderId);

    OrderClientDetailResponse convertToClientResponse(Order order);
}
