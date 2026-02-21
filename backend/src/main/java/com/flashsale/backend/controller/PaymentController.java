package com.flashsale.backend.controller;

import com.flashsale.backend.common.ApiResponse;
import com.flashsale.backend.common.ResultCode;
import com.flashsale.backend.dto.request.PaymentRequest;
import com.flashsale.backend.dto.response.OrderClientResponse;
import com.flashsale.backend.entity.Order;
import com.flashsale.backend.security.SecurityUtils;
import com.flashsale.backend.service.OrderService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * @description
 * @author Yang-Hsu
 * @date 2026/2/20 下午7:19
 */
@Slf4j
@RestController
@RequestMapping("/api/client/payment")
@RequiredArgsConstructor
public class PaymentController {

    private final OrderService orderService;

    /**
     * @description
     * @author Yang-Hsu
     * @date 2026/2/20 下午7:19
     */
    @PostMapping("/pay")
    public ResponseEntity<ApiResponse<OrderClientResponse>> payOrder(@Valid @RequestBody PaymentRequest request) {
        String memberId = SecurityUtils.getCurrentUserId();
        log.info("API: Pay order (Client): orderId={}, memberId={}", request.getOrderId(), memberId);
        Order order = orderService.payOrder(request);
        return ResponseEntity.ok(new ApiResponse<>(ResultCode.SUCCESS, orderService.convertToClientResponse(order)));
    }
}
