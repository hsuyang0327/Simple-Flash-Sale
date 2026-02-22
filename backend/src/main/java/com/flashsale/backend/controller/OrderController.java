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
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
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
@Tag(name = "Order Management", description = "APIs for creating, viewing, and managing orders.")
@Slf4j
@RestController
@RequiredArgsConstructor
public class OrderController {

    private final OrderService orderService;
    private final MemberService memberService;
    private final ProductService productService;

    @Operation(summary = "Create Order (Client)", description = "Creates a new order for a flash sale event. Requires JWT authentication.",
            security = @SecurityRequirement(name = "bearerAuth"))
    @PostMapping("/api/client/orders")
    public ResponseEntity<ApiResponse<OrderClientResponse>> createOrder(@Valid @RequestBody OrderRequest request) {
        String memberId = SecurityUtils.getCurrentUserId();
        SecurityUtils.checkPermission(memberId);
        log.info("API: Create order (Client): memberId={}", memberId);
        Order order = orderService.createOrder(request);
        return ResponseEntity.ok(new ApiResponse<>(ResultCode.SUCCESS, convertToClientResponse(order)));
    }

    @Operation(summary = "Update Order (Client)", description = "Updates an existing order. Requires JWT authentication.",
            security = @SecurityRequirement(name = "bearerAuth"))
    @PutMapping("/api/client/orders/{id}")
    public ResponseEntity<ApiResponse<OrderClientResponse>> updateOrder(
            @Parameter(description = "ID of the order to update") @PathVariable String id,
            @Valid @RequestBody OrderRequest request) {
        String memberId = SecurityUtils.getCurrentUserId();
        SecurityUtils.checkPermission(memberId);
        log.info("API: Update order (Client): orderId={}, memberId={}", id, memberId);

        Order order = orderService.updateOrder(id, request);
        return ResponseEntity.ok(new ApiResponse<>(ResultCode.SUCCESS, convertToClientResponse(order)));
    }

    @Operation(summary = "Get Order (Client)", description = "Retrieves details of a specific order for the authenticated user. Requires JWT authentication.",
            security = @SecurityRequirement(name = "bearerAuth"))
    @GetMapping("/api/client/orders/{id}")
    public ResponseEntity<ApiResponse<OrderClientResponse>> getOrder(
            @Parameter(description = "ID of the order to retrieve") @PathVariable String id) {
        String memberId = SecurityUtils.getCurrentUserId();
        SecurityUtils.checkPermission(memberId);
        log.info("API: Get order (Client): orderId={}, memberId={}", id, memberId);
        Order order = orderService.getOrderById(memberId, id);
        return ResponseEntity.ok(new ApiResponse<>(ResultCode.SUCCESS, convertToClientResponse(order)));
    }

    @Operation(summary = "Get Order Status (Client)", description = "Checks the status of an order creation process from Redis. Requires JWT authentication.",
            security = @SecurityRequirement(name = "bearerAuth"))
    @GetMapping("/api/client/orders/status/{memberId}")
    public ResponseEntity<ApiResponse<OrderStatusResponse>> getOrderStatus(
            @Parameter(description = "ID of the member whose order status is to be checked") @PathVariable String memberId) {
        String currentUserId = SecurityUtils.getCurrentUserId();
        log.info("API: Get order status for memberId: {}", currentUserId);
        OrderStatusResponse response = orderService.getOrderStatusFromRedis(memberId);

        return ResponseEntity.ok(new ApiResponse<>(ResultCode.SUCCESS, response));
    }

    @Operation(summary = "Search Orders (Admin)", description = "Searches and retrieves a paginated list of orders based on product or member name. Requires admin privileges.")
    @GetMapping("/api/admin/orders")
    public ResponseEntity<ApiResponse<Page<OrderAdminResponse>>> searchOrders(
            @Parameter(description = "Filter by product name (optional)") @RequestParam(required = false) String productName,
            @Parameter(description = "Filter by member name (optional)") @RequestParam(required = false) String memberName,
            @Parameter(description = "Pagination information") @PageableDefault(page = 0, size = 10, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable) {
        log.info("API: Search orders (Admin): productName={}, memberName={}", productName, memberName);

        Page<Order> orders = orderService.searchOrders(productName, memberName, pageable);
        Page<OrderAdminResponse> response = orders.map(orderService::convertToAdminResponse);
        return ResponseEntity.ok(new ApiResponse<>(ResultCode.SUCCESS, response));
    }

    @Operation(summary = "Get Order (Admin)", description = "Retrieves detailed information about a specific order by its ID. Requires admin privileges.")
    @GetMapping("/api/admin/orders/{id}")
    public ResponseEntity<ApiResponse<OrderAdminResponse>> getOrderAdmin(
            @Parameter(description = "ID of the order to retrieve") @PathVariable String id) {
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
