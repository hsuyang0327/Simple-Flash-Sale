package com.flashsale.backend.controller;

import com.flashsale.backend.common.ApiResponse;
import com.flashsale.backend.common.ResultCode;
import com.flashsale.backend.dto.request.OrderRequest;
import com.flashsale.backend.dto.response.OrderClientResponse;
import com.flashsale.backend.entity.Order;
import com.flashsale.backend.security.SecurityUtils;
import com.flashsale.backend.service.OrderService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping("/api/test")
@RequiredArgsConstructor
public class TestController {

    private final OrderService orderService;

    @GetMapping("/hello")
    public ResponseEntity<ApiResponse<String>> hello() {
        return  ResponseEntity.ok().body(ApiResponse.of(ResultCode.SUCCESS));
    }

    @PostMapping("/ordersDB")
    public ResponseEntity<ApiResponse<OrderClientResponse>> createOrderDB(@Valid @RequestBody OrderRequest request) {
        log.info("API: Create ordersDB (Client): memberId={}", request.getMemberId());
        Order order = orderService.createOrderDB(request);
        return ResponseEntity.ok(new ApiResponse<>(ResultCode.SUCCESS, orderService.convertToClientResponse(order)));
    }

    @PostMapping("/ordersRedis")
    public ResponseEntity<ApiResponse<OrderClientResponse>> createOrderRedis(@Valid @RequestBody OrderRequest request) {
        log.info("API: Create order (Client): memberId={}", request.getMemberId());
        Order order = orderService.createOrderRedis(request);
        return ResponseEntity.ok(new ApiResponse<>(ResultCode.SUCCESS, orderService.convertToClientResponse(order)));
    }

}