package com.flashsale.backend.repository;

import com.flashsale.backend.entity.Product;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * @description ProductRepository
 * @author Yang-Hsu
 * @date 2026/2/7
 */
public interface ProductRepository extends JpaRepository<Product, String> {

    /**
     * @description Search products by name
     * @author Yang-Hsu
     * @date 2026/2/17
     */
    Page<Product> findByProductNameContaining(String productName, Pageable pageable);
}
