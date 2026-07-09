package com.flashsale.backend.service;

import com.flashsale.backend.dto.request.ProductRequest;
import com.flashsale.backend.entity.Product;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface ProductService {

    Page<Product> getAllProducts(Pageable pageable);

    Product getProductById(String productId);

    Product createProduct(ProductRequest request);

    Product updateProduct(String productId, ProductRequest request);

    void deleteProduct(String productId);

    Page<Product> searchProducts(String productName, Pageable pageable);
}
