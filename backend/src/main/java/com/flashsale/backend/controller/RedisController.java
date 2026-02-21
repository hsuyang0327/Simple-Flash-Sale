package com.flashsale.backend.controller;

import com.flashsale.backend.common.ApiResponse;
import com.flashsale.backend.common.ResultCode;
import com.flashsale.backend.entity.Order;
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

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/client/open/redis")
@RequiredArgsConstructor
public class RedisController {

    @Qualifier("redisTemplateDb0")
    private final RedisTemplate<String, Object> redisTemplateForStock;

    @Qualifier("redisTemplateDb1")
    private final RedisTemplate<String, Object> redisTemplateForOrder;

    private static final String PREHEATED_PRODUCT_KEYS = "preheated_product_keys";

    /**
     * @description Get preheated products
     * @author Yang-Hsu
     * @date 2026/2/21 下午4:28
     */
    @GetMapping("/preheated-products")
    public ResponseEntity<ApiResponse<Page<Map<Object, Object>>>> getPreheatedProducts(Pageable pageable) {
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

    /**
     * @description Get order status
     * @author Yang-Hsu
     * @date 2026/2/21 下午4:28
     */
    @GetMapping("/order-status/{memberId}")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getOrderStatus(@PathVariable String memberId) {
        String orderKey = "member:order:" + memberId;
        Order order = (Order) redisTemplateForOrder.opsForValue().get(orderKey);

        Map<String, Object> response = new HashMap<>();
        if (order != null) {
            response.put("status", "SUCCESS");
            response.put("order", order);
        } else {
            response.put("status", "PENDING");
        }

        return ResponseEntity.ok(new ApiResponse<>(ResultCode.SUCCESS, response));
    }
}
