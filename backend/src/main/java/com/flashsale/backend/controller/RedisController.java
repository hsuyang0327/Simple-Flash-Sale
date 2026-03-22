package com.flashsale.backend.controller;

import com.flashsale.backend.common.ApiResponse;
import com.flashsale.backend.common.ResultCode;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Tag(name = "Redis Data Access", description = "APIs for directly accessing Redis data, such as preheated products and cached order status.")
@RestController
@RequestMapping("/api/client/open/redis")
@RequiredArgsConstructor
public class RedisController {

    @Qualifier("redisTemplateDb0")
    private final RedisTemplate<String, Object> redisTemplateForStock;

    @Qualifier("redisTemplateDb1")
    private final RedisTemplate<String, Object> redisTemplateForOrder;

    private static final String PREHEATED_PRODUCT_KEYS = "preheated_product_keys";

    @Operation(summary = "Get Preheated Products", description = "Retrieves a paginated list of products that have been preheated into Redis cache.")
    @GetMapping("/preheated-products")
    public ResponseEntity<ApiResponse<Page<Map<Object, Object>>>> getPreheatedProducts(
            @Parameter(description = "Pagination information") Pageable pageable) {
        long start = pageable.getOffset();
        long end = start + pageable.getPageSize() - 1;
        Long total = redisTemplateForStock.opsForList().size(PREHEATED_PRODUCT_KEYS);
        if (total == null) {
            total = 0L;
        }
        List<Object> productKeys = redisTemplateForStock.opsForList().range(PREHEATED_PRODUCT_KEYS, start, end);
        List<Map<Object, Object>> products = List.of();
        if (productKeys != null && !productKeys.isEmpty()) {
            products = productKeys.stream()
                    .map(key -> redisTemplateForStock.opsForHash().entries((String) key))
                    .collect(Collectors.toList());
        }
        Page<Map<Object, Object>> page = new PageImpl<>(products, pageable, total);
        return ResponseEntity.ok(new ApiResponse<>(ResultCode.SUCCESS, page));
    }

    // Removed: GET /order-status/{memberId} — public endpoint with IDOR vulnerability.
    // Use GET /api/client/orders/status?eventId={eventId} instead (JWT-protected, memberId from token).

    @Operation(summary = "Get Single Preheated Product", description = "Retrieves a single preheated product from Redis cache by productId.")
    @GetMapping("/product/{productId}")
    public ResponseEntity<ApiResponse<Map<Object, Object>>> getPreheatedProduct(
            @PathVariable String productId) {
        String key = "productId:" + productId;
        Map<Object, Object> product = redisTemplateForStock.opsForHash().entries(key);
        if (product == null || product.isEmpty()) {
            return ResponseEntity.ok(ApiResponse.of(ResultCode.EVENT_NOT_FOUND));
        }
        return ResponseEntity.ok(new ApiResponse<>(ResultCode.SUCCESS, product));
    }
}
