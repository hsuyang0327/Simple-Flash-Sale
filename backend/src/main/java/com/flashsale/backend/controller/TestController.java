package com.flashsale.backend.controller;

import com.flashsale.backend.common.ApiResponse;
import com.flashsale.backend.common.ResultCode;
import com.flashsale.backend.dto.request.OrderRequest;
import com.flashsale.backend.dto.response.OrderClientDetailResponse;
import com.flashsale.backend.entity.Order;
import com.flashsale.backend.service.OrderService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * @description Test controller for development and QA — order flow testing endpoints
 * @author Yang-Hsu
 * @date 2026/7/9
 */
@Slf4j
@RestController
@RequestMapping("/api/test")
@RequiredArgsConstructor
public class TestController {

    private final OrderService orderService;

    @GetMapping("/hello")
    /**
     * @description Health check endpoint
     * @author Yang-Hsu
     * @date 2026/7/9
     */
    public ResponseEntity<ApiResponse<String>> hello() {
        return  ResponseEntity.ok().body(ApiResponse.of(ResultCode.SUCCESS));
    }

    @PostMapping("/ordersDB")
    /**
     * @description Test order creation via direct DB write (bypass MQ)
     * @author Yang-Hsu
     * @date 2026/7/9
     */
    public ResponseEntity<ApiResponse<OrderClientDetailResponse>> createOrderDB(@Valid @RequestBody OrderRequest request) {
        log.info("API: Create ordersDB (Client): memberId={}", request.getMemberId());
        Order order = orderService.createOrderDB(request);
        return ResponseEntity.ok(new ApiResponse<>(ResultCode.SUCCESS, orderService.convertToClientResponse(order)));
    }

    @PostMapping("/ordersRedis")
    /**
     * @description Test order creation via Redis stock decrement only (bypass MQ)
     * @author Yang-Hsu
     * @date 2026/7/9
     */
    public ResponseEntity<ApiResponse<OrderClientDetailResponse>> createOrderRedis(@Valid @RequestBody OrderRequest request) {
        log.info("API: Create order (Client): memberId={}", request.getMemberId());
        Order order = orderService.createOrderRedis(request);
        return ResponseEntity.ok(new ApiResponse<>(ResultCode.SUCCESS, orderService.convertToClientResponse(order)));
    }

}