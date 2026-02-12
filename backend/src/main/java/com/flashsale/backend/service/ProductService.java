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
import org.springframework.orm.ObjectOptimisticLockingFailureException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * @description ProductService
 * @author Yang-Hsu
 * @date 2026/2/10 下午3:20
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ProductService {

    private final ProductRepository productRepository;

    /**
     * @description getAvailableProducts
     * @author Yang-Hsu
     * @date 2026/2/10 下午3:20
     */
    @Transactional(readOnly = true)
    public Page<Product> getAvailableProducts(Pageable pageable) {
        return productRepository.findAvailableProducts(pageable);
    }

    /**
     * @description getProductById
     * @author Yang-Hsu
     * @date 2026/2/10 下午3:20
     */
    @Transactional(readOnly = true)
    public Product getProductById(String id) {
        return productRepository.findById(id)
                .orElseThrow(() -> {
                    log.warn("Product not found: {}", id);
                    return new BusinessException(ResultCode.PRODUCT_NOT_FOUND);
                });
    }

    /**
     * @description createProduct
     * @author Yang-Hsu
     * @date 2026/2/10 下午3:20
     */
    @Transactional
    public Product createProduct(ProductRequest request) {
        log.info("Creating product: {}", request.getProductName());
        Product product = new Product();
        BeanUtils.copyProperties(request, product);
        return productRepository.save(product);
    }

  /**
   * @description updateProduct(Optimistic Lock)
   * @author Yang-Hsu
   * @date 2026/2/10 下午3:21
   */
    @Transactional
    public Product updateProduct(String productId, ProductRequest request) {
        log.info("Updating product: {}", productId);
        try {
            Product existingProduct = getProductById(productId);
            BeanUtils.copyProperties(request, existingProduct, BeanCopyUtil.getNullPropertyNames(request));
            Product updatedProduct = productRepository.save(existingProduct);
            log.info("Product updated successfully: {}", productId);
            return updatedProduct;

        } catch (ObjectOptimisticLockingFailureException e) {
            log.warn("Update product failed due to concurrent modification: {}", productId);
            throw new BusinessException(ResultCode.PRODUCT_IS_UPDATED_BY_OTHERS);
        }
    }

    /**
     * @description deleteProduct
     * @author Yang-Hsu
     * @date 2026/2/10 下午3:21
     */
    @Transactional
    public void deleteProduct(String id) {
        log.info("Deleting product: {}", id);
        if (!productRepository.existsById(id)) {
            log.warn("Delete product failed, id not found: {}", id);
            throw new BusinessException(ResultCode.PRODUCT_NOT_FOUND);
        }
        productRepository.deleteById(id);
    }

    /**
     * @description searchProducts
     * @author Yang-Hsu
     * @date 2026/2/10 下午3:22
     */
    @Transactional(readOnly = true)
    public Page<Product> searchProducts(String productName, Pageable pageable) {
        return productRepository.findByProductNameContaining(productName, pageable);
    }

}