package com.flashsale.backend.repository;

import com.flashsale.backend.entity.Product;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * @author Yang-Hsu
 * @description
 * @date 2026/2/7 下午10:32
 */
public interface ProductRepository extends JpaRepository<Product, String> {

    /**
     * @description Search products by name
     * @author Yang-Hsu
     * @date 2026/2/17 下午1:32
     */
    Page<Product> findByProductNameContaining(String productName, Pageable pageable);
}
