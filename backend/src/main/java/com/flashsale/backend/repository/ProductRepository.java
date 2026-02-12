package com.flashsale.backend.repository;

import com.flashsale.backend.entity.Product;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

/**
 * @author Yang-Hsu
 * @description
 * @date 2026/2/7 下午10:32
 */
public interface ProductRepository extends JpaRepository<Product, String> {

    /**
     * @description product update avoid quantity is minus
     * @author Yang-Hsu
     * @date 2026/2/7 下午10:33
     */
// 關鍵：使用 SQL 原子操作扣減，並加入 stock >= qty 的判斷
    @Modifying
    @Transactional
    @Query("UPDATE Product p SET p.stock = p.stock - :qty WHERE p.productId = :id AND p.stock >= :qty")
    int decreaseStock(@Param("id") String id, @Param("qty") Integer qty);
    /**
     * @description find by product name containing
     * @author Yang-Hsu
     * @date 2026/2/8 下午10:45
     */
    Page<Product> findByProductNameContaining(String productName, Pageable pageable);

    /**
     * @description Find products that are on shelf and not expired
     * @author Yang-Hsu
     * @date 2026/2/8 下午10:46
     */
    @Query("SELECT p FROM Product p WHERE p.status = 1 AND p.endTime > CURRENT_TIMESTAMP")
    Page<Product> findAvailableProducts(Pageable pageable);

}
