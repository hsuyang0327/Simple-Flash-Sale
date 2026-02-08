package com.flashsale.backend.service;

import com.flashsale.backend.common.ResultCode;
import com.flashsale.backend.common.util.BeanCopyUtil;
import com.flashsale.backend.dto.request.ProductRequest;
import com.flashsale.backend.entity.Product;
import com.flashsale.backend.exception.BusinessException;
import com.flashsale.backend.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * @description ProductService
 * @author Yang-Hsu
 * @date 2026/2/8 上午1:06
 */
@Slf4j
@Service
@RequiredArgsConstructor //lombok auto for constructor if declare with final (not for autowired)
public class ProductService {
    private final ProductRepository productRepository;

    /**
     * @description GetAllProducts
     * @author Yang-Hsu
     * @date 2026/2/8 上午1:06
     */
    @Transactional(readOnly = true)
    public Page<Product> getAllProducts(Pageable pageable) {
        return productRepository.findAll(pageable);
    }

    /**
     * @description GetProductById
     * @author Yang-Hsu
     * @date 2026/2/8 上午1:07
     */
    @Transactional(readOnly = true)
    public Product getProductById(String productId) {
        return productRepository.findById(productId)
                .orElseThrow(() -> new BusinessException(ResultCode.PRODUCT_NOT_FOUND));
    }

    /**
     * @description CreateProduct
     * @author Yang-Hsu
     * @date 2026/2/8 上午1:07
     */
    @Transactional
    public Product createProduct(ProductRequest request) {
        log.info("Creating product: {}", request.getProductName());
        Product product = new Product();
        BeanUtils.copyProperties(request, product);
        Product savedProduct = productRepository.save(product);
        log.info("Product created successfully with ID: {}", savedProduct.getProductId());
        return savedProduct;
    }

    /**
     * @description UpdateProduct
     * @author Yang-Hsu
     * @date 2026/2/8 上午1:07
     */
    @Transactional
    public Product updateProduct(String productId, ProductRequest request) {
        log.info("Updating product with ID: {}", productId);
        Product existingProduct = getProductById(productId);
        BeanUtils.copyProperties(request, existingProduct, BeanCopyUtil.getNullPropertyNames(request));
        Product updatedProduct = productRepository.save(existingProduct);
        log.info("Product updated successfully: {}", productId);
        return updatedProduct;
    }

    /**
     * @description DeleteProduct
     * @author Yang-Hsu
     * @date 2026/2/8 上午1:08
     */
    @Transactional
    public void deleteProduct(String productId) {
        log.info("Deleting product with ID: {}", productId);
        Product product = getProductById(productId);
        productRepository.delete(product);
        log.info("Product deleted successfully: {}", productId);
    }

    /**
     * @description Search products by name
     * @author Yang-Hsu
     */
    @Transactional(readOnly = true)
    public Page<Product> searchProducts(String productName, Pageable pageable) {
        return productRepository.findByProductNameContaining(productName, pageable);
    }

    /**
     * @description Get available products (On shelf and not expired)
     * @author Yang-Hsu
     */
    @Transactional(readOnly = true)
    public Page<Product> getAvailableProducts(Pageable pageable) {
        return productRepository.findAvailableProducts(pageable);
    }
}
