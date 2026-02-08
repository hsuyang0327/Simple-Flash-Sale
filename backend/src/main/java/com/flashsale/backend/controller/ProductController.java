package com.flashsale.backend.controller;

import com.flashsale.backend.common.ApiResponse;
import com.flashsale.backend.common.ResultCode;
import com.flashsale.backend.dto.request.ProductRequest;
import com.flashsale.backend.dto.response.ProductAdminResponse;
import com.flashsale.backend.dto.response.ProductClientResponse;
import com.flashsale.backend.entity.Product;
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
 * @description ProductController
 * @date 2026/2/8 下午10:46
 */
@Slf4j
@RestController
@RequiredArgsConstructor
public class ProductController {

    private final ProductService productService;

    /**
     * @description Get all products (Client) - Only available products
     * @author Yang-Hsu
     * @date 2026/2/8 下午10:47
     */
    @GetMapping("/api/client/open/products")
    public ResponseEntity<ApiResponse<Page<ProductClientResponse>>> listProducts(
            @PageableDefault(page = 0, size = 10, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable) {
        log.info("API: Get all available products (Client)");
        Page<Product> products = productService.getAvailableProducts(pageable);
        Page<ProductClientResponse> response = products.map(this::convertToClientResponse);
        return ResponseEntity.ok(new ApiResponse<>(ResultCode.SUCCESS, response));
    }

    /**
     * @description
     * @description Get product by ID (Client)
     * @date 2026/2/8 下午10:47
     */
    @GetMapping("/api/client/open/products/{id}")
    public ResponseEntity<ApiResponse<ProductClientResponse>> getProduct(@PathVariable String id) {
        log.info("API: Get product by ID (Client): {}", id);
        Product product = productService.getProductById(id);
        return ResponseEntity.ok(new ApiResponse<>(ResultCode.SUCCESS, convertToClientResponse(product)));
    }

    /**
     * @description Get product by ID (Admin)
     * @author Yang-Hsu
     * @date 2026/2/8 下午10:47
     */
    @GetMapping("/api/admin/products/{id}")
    public ResponseEntity<ApiResponse<ProductAdminResponse>> getProductAdmin(@PathVariable String id) {
        log.info("API: Get product by ID (Admin): {}", id);
        Product product = productService.getProductById(id);
        return ResponseEntity.ok(new ApiResponse<>(ResultCode.SUCCESS, convertToAdminResponse(product)));
    }

    /**
     * @description Create product (Admin)
     * @author Yang-Hsu
     * @date 2026/2/8 下午10:47
     */
    @PostMapping("/api/admin/products")
    public ResponseEntity<ApiResponse<ProductAdminResponse>> createProduct(@Valid @RequestBody ProductRequest request) {
        log.info("API: Create product (Admin)");
        Product createdProduct = productService.createProduct(request);
        return ResponseEntity.ok(new ApiResponse<>(ResultCode.SUCCESS, convertToAdminResponse(createdProduct)));
    }

    /**
     * @description
     * @description Update product (Admin)
     * @date 2026/2/8 下午10:48
     */
    @PutMapping("/api/admin/products/{productId}")
    public ResponseEntity<ApiResponse<ProductAdminResponse>> updateProduct(@PathVariable String productId, @Valid @RequestBody ProductRequest request) {
        log.info("API: Update product (Admin): {}", productId);
        Product updatedProduct = productService.updateProduct(productId, request);
        return ResponseEntity.ok(new ApiResponse<>(ResultCode.SUCCESS, convertToAdminResponse(updatedProduct)));
    }

    /**
     * @description Delete product (Admin)
     * @author Yang-Hsu
     * @date 2026/2/8 下午10:48
     */
    @DeleteMapping("/api/admin/products/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteProduct(@PathVariable String id) {
        log.info("API: Delete product (Admin): {}", id);
        productService.deleteProduct(id);
        return ResponseEntity.ok(ApiResponse.of(ResultCode.SUCCESS));
    }

    /**
     * @description
     * @description Search products by name (Admin)
     * @date 2026/2/8 下午10:48
     */
    @GetMapping("/api/admin/products/search")
    public ResponseEntity<ApiResponse<Page<ProductAdminResponse>>> searchProducts(
            @RequestParam String productName,
            @PageableDefault(page = 0, size = 10, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable) {
        log.info("API: Search products (Admin): {}", productName);
        Page<Product> products = productService.searchProducts(productName, pageable);
        Page<ProductAdminResponse> response = products.map(this::convertToAdminResponse);
        return ResponseEntity.ok(new ApiResponse<>(ResultCode.SUCCESS, response));
    }

    private ProductClientResponse convertToClientResponse(Product product) {
        return ProductClientResponse.builder()
                .productId(product.getProductId())
                .productName(product.getProductName())
                .price(product.getPrice())
                .stock(product.getStock())
                .description(product.getDescription())
                .startTime(product.getStartTime())
                .endTime(product.getEndTime())
                .build();
    }

    private ProductAdminResponse convertToAdminResponse(Product product) {
        return ProductAdminResponse.builder()
                .productId(product.getProductId())
                .productName(product.getProductName())
                .price(product.getPrice())
                .stock(product.getStock())
                .description(product.getDescription())
                .status(product.getStatus())
                .startTime(product.getStartTime())
                .endTime(product.getEndTime())
                .createdAt(product.getCreatedAt())
                .updatedAt(product.getUpdatedAt())
                .build();
    }
}
