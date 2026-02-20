package com.flashsale.backend.controller;

import com.flashsale.backend.common.ApiResponse;
import com.flashsale.backend.common.ResultCode;
import com.flashsale.backend.dto.request.OrderRequest;
import com.flashsale.backend.dto.response.OrderAdminResponse;
import com.flashsale.backend.dto.response.OrderClientResponse;
import com.flashsale.backend.dto.response.OrderStatusResponse;
import com.flashsale.backend.entity.Member;
import com.flashsale.backend.entity.Order;
import com.flashsale.backend.entity.Product;
import com.flashsale.backend.security.SecurityUtils;
import com.flashsale.backend.service.MemberService;
import com.flashsale.backend.service.OrderService;
import com.flashsale.backend.service.ProductService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * @author Yang-Hsu
 * @description OrderController
 * @date 2026/2/12 下午9:43
 */
@Slf4j
@RestController
@RequiredArgsConstructor
public class OrderController {

    private final OrderService orderService;
    private final MemberService memberService;
    private final ProductService productService;

    /**
     * @description Create order (Client)
     * @author Yang-Hsu
     * @date 2026/2/19 下午8:27
     */
    @PostMapping("/api/client/orders")
    public ResponseEntity<ApiResponse<OrderClientResponse>> createOrder(@Valid @RequestBody OrderRequest request) {
        String memberId = SecurityUtils.getCurrentUserId();
        SecurityUtils.checkPermission(memberId);
        log.info("API: Create order (Client): memberId={}", memberId);
        Order order = orderService.createOrder(request);
        return ResponseEntity.ok(new ApiResponse<>(ResultCode.SUCCESS, convertToClientResponse(order)));
    }

    /**
     * @description Update Order (Client)
     * @author Yang-Hsu
     * @date 2026/2/12 下午9:44
     */
    @PutMapping("/api/client/orders/{id}")
    public ResponseEntity<ApiResponse<OrderClientResponse>> updateOrder(@PathVariable String id, @Valid @RequestBody OrderRequest request) {
        String memberId = SecurityUtils.getCurrentUserId();
        SecurityUtils.checkPermission(memberId);
        log.info("API: Update order (Client): orderId={}, memberId={}", id, memberId);

        Order order = orderService.updateOrder(id, request);
        return ResponseEntity.ok(new ApiResponse<>(ResultCode.SUCCESS, convertToClientResponse(order)));
    }

    /**
     * @description Get Order by ID (Client)
     * @author Yang-Hsu
     * @date 2026/2/12 下午9:44
     */
    @GetMapping("/api/client/orders/{id}")
    public ResponseEntity<ApiResponse<OrderClientResponse>> getOrder(@PathVariable String id) {
        String memberId = SecurityUtils.getCurrentUserId();
        SecurityUtils.checkPermission(memberId);
        log.info("API: Get order (Client): orderId={}, memberId={}", id, memberId);
        Order order = orderService.getOrderById(memberId, id);
        return ResponseEntity.ok(new ApiResponse<>(ResultCode.SUCCESS, convertToClientResponse(order)));
    }

    /**
     * @description
     * @author Yang-Hsu
     * @date 2026/2/20 下午6:59
     */
    @GetMapping("/api/client/orders/status/{memberId}")
    public ResponseEntity<ApiResponse<OrderStatusResponse>> getOrderStatus(@PathVariable String memberId) {
        String currentUserId = SecurityUtils.getCurrentUserId();
        log.info("API: Get order status for memberId: {}", currentUserId);
        OrderStatusResponse response = orderService.getOrderStatusFromRedis(memberId);

        return ResponseEntity.ok(new ApiResponse<>(ResultCode.SUCCESS, response));
    }

    /**
     * @description Search Orders (Admin)
     * @author Yang-Hsu
     * @date 2026/2/12 下午9:44
     */
    @GetMapping("/api/admin/orders")
    public ResponseEntity<ApiResponse<Page<OrderAdminResponse>>> searchOrders(
            @RequestParam(required = false) String productName,
            @RequestParam(required = false) String memberName,
            @PageableDefault(page = 0, size = 10, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable) {
        log.info("API: Search orders (Admin): productName={}, memberName={}", productName, memberName);

        Page<Order> orders = orderService.searchOrders(productName, memberName, pageable);
        Page<OrderAdminResponse> response = orders.map(orderService::convertToAdminResponse);
        return ResponseEntity.ok(new ApiResponse<>(ResultCode.SUCCESS, response));
    }

    /**
     * @description Get Order by ID (Admin)
     * @author Yang-Hsu
     * @date 2026/2/12 下午9:44
     */
    @GetMapping("/api/admin/orders/{id}")
    public ResponseEntity<ApiResponse<OrderAdminResponse>> getOrderAdmin(@PathVariable String id) {
        log.info("API: Get order (Admin): orderId={}", id);

        Order order = orderService.getOrderByIdAdmin(id);
        return ResponseEntity.ok(new ApiResponse<>(ResultCode.SUCCESS, convertToAdminResponse(order)));
    }

    public OrderClientResponse convertToClientResponse(Order order) {
        Product product = productService.getProductById(order.getProductId());
        String productName = product != null ? product.getProductName() : "Unknown";

        return OrderClientResponse.builder()
                .orderId(order.getOrderId())
                .productName(productName)
                .quantity(order.getQuantity())
                .totalPrice(order.getTotalPrice())
                .status(order.getStatus())
                .createdAt(order.getCreatedAt())
                .build();
    }

    public OrderAdminResponse convertToAdminResponse(Order order) {
        Product product = productService.getProductById(order.getProductId());
        Member member = memberService.getMemberById(order.getMemberId());

        return OrderAdminResponse.builder()
                .orderId(order.getOrderId())
                .memberId(order.getMemberId())
                .memberName(member != null ? member.getMemberName() : "Unknown")
                .productId(order.getProductId())
                .productName(product != null ? product.getProductName() : "Unknown")
                .quantity(order.getQuantity())
                .totalPrice(order.getTotalPrice())
                .status(order.getStatus())
                .createdAt(order.getCreatedAt())
                .updatedAt(order.getUpdatedAt())
                .build();
    }
}
