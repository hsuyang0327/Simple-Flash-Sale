package com.flashsale.backend.controller;

import com.flashsale.backend.common.ApiResponse;
import com.flashsale.backend.common.ResultCode;
import com.flashsale.backend.dto.request.ProductRequest;
import com.flashsale.backend.dto.response.ProductAdminResponse;
import com.flashsale.backend.dto.response.ProductClientResponse;
import com.flashsale.backend.entity.Product;
import com.flashsale.backend.mapper.ProductMapper;
import com.flashsale.backend.service.ProductService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
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
 * @description ProductController
 * @author Yang-Hsu
 * @date 2026/2/8
 */
@Tag(name = "Product Management", description = "APIs for managing products.")
@Slf4j
@RestController
@RequiredArgsConstructor
public class ProductController {

    private final ProductService productService;
    private final ProductMapper productMapper;

    @Operation(summary = "List Products (Client)", description = "Retrieves a paginated list of available products for clients. This is a public endpoint.")
    @GetMapping("/api/client/open/products")
    /**
     * @description List available products with pagination for client
     * @author Yang-Hsu
     * @date 2026/7/9
     */
    public ResponseEntity<ApiResponse<Page<ProductClientResponse>>> listProducts(
            @Parameter(description = "Pagination information") @PageableDefault(page = 0, size = 10, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable) {
        log.info("API: Get all products (Client)");
        Page<Product> products = productService.getAllProducts(pageable);
        Page<ProductClientResponse> response = products.map(productMapper::toClientResponse);
        return ResponseEntity.ok(new ApiResponse<>(ResultCode.SUCCESS, response));
    }

    @Operation(summary = "Get Product (Client)", description = "Retrieves detailed information about a specific product. This is a public endpoint.")
    @GetMapping("/api/client/open/products/{id}")
    /**
     * @description Get product detail by ID for client
     * @author Yang-Hsu
     * @date 2026/7/9
     */
    public ResponseEntity<ApiResponse<ProductClientResponse>> getProduct(
            @Parameter(description = "ID of the product to retrieve") @PathVariable String id) {
        log.info("API: Get product by ID (Client): {}", id);
        Product product = productService.getProductById(id);
        return ResponseEntity.ok(new ApiResponse<>(ResultCode.SUCCESS, productMapper.toClientResponse(product)));
    }

    @Operation(summary = "Get Product (Admin)", description = "Retrieves detailed admin-level information about a specific product. Requires admin privileges.")
    @GetMapping("/api/admin/products/{id}")
    /**
     * @description Get product detail by ID for client
     * @author Yang-Hsu
     * @date 2026/7/9
     */
    public ResponseEntity<ApiResponse<ProductAdminResponse>> getProductAdmin(
            @Parameter(description = "ID of the product to retrieve") @PathVariable String id) {
        log.info("API: Get product by ID (Admin): {}", id);
        Product product = productService.getProductById(id);
        return ResponseEntity.ok(new ApiResponse<>(ResultCode.SUCCESS, productMapper.toAdminResponse(product)));
    }

    @Operation(summary = "Create Product (Admin)", description = "Creates a new product. Requires admin privileges.")
    @PostMapping("/api/admin/products")
    /**
     * @description Create a new product for admin
     * @author Yang-Hsu
     * @date 2026/7/9
     */
    public ResponseEntity<ApiResponse<ProductAdminResponse>> createProduct(@Valid @RequestBody ProductRequest request) {
        log.info("API: Create product (Admin)");
        Product createdProduct = productService.createProduct(request);
        return ResponseEntity.ok(new ApiResponse<>(ResultCode.SUCCESS, productMapper.toAdminResponse(createdProduct)));
    }

    @Operation(summary = "Update Product (Admin)", description = "Updates an existing product. Requires admin privileges.")
    @PutMapping("/api/admin/products/{productId}")
    /**
     * @description Update an existing product for admin
     * @author Yang-Hsu
     * @date 2026/7/9
     */
    public ResponseEntity<ApiResponse<ProductAdminResponse>> updateProduct(
            @Parameter(description = "ID of the product to update") @PathVariable String productId,
            @Valid @RequestBody ProductRequest request) {
        log.info("API: Update product (Admin): {}", productId);
        Product updatedProduct = productService.updateProduct(productId, request);
        return ResponseEntity.ok(new ApiResponse<>(ResultCode.SUCCESS, productMapper.toAdminResponse(updatedProduct)));
    }

    @Operation(summary = "Delete Product (Admin)", description = "Deletes a product by its ID. Requires admin privileges.")
    @DeleteMapping("/api/admin/products/{id}")
    /**
     * @description Delete a product by ID for admin
     * @author Yang-Hsu
     * @date 2026/7/9
     */
    public ResponseEntity<ApiResponse<Void>> deleteProduct(
            @Parameter(description = "ID of the product to delete") @PathVariable String id) {
        log.info("API: Delete product (Admin): {}", id);
        productService.deleteProduct(id);
        return ResponseEntity.ok(ApiResponse.of(ResultCode.SUCCESS));
    }

    @Operation(summary = "Search Products (Admin)", description = "Searches for products by name. Requires admin privileges.")
    @GetMapping("/api/admin/products/search")
    /**
     * @description Search products by name with pagination for admin
     * @author Yang-Hsu
     * @date 2026/7/9
     */
    public ResponseEntity<ApiResponse<Page<ProductAdminResponse>>> searchProducts(
            @Parameter(description = "The name of the product to search for") @RequestParam String productName,
            @Parameter(description = "Pagination information") @PageableDefault(page = 0, size = 10, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable) {
        log.info("API: Search products (Admin): {}", productName);
        Page<Product> products = productService.searchProducts(productName, pageable);
        Page<ProductAdminResponse> response = products.map(productMapper::toAdminResponse);
        return ResponseEntity.ok(new ApiResponse<>(ResultCode.SUCCESS, response));
    }
}
