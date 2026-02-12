package com.flashsale.backend.repository;

import com.flashsale.backend.entity.Order;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

/**
 * @author Yang-Hsu
 * @description OrderRepository
 * @date 2026/2/7 下午10:40
 */
public interface OrderRepository extends JpaRepository<Order, String> {

    /**
     * @description Search Orders (Admin)
     * @author Yang-Hsu
     * @date 2026/2/12 下午9:53
     */
    @Query(value = "SELECT o.* FROM orders o " +
            "LEFT JOIN member m ON o.member_id = m.member_id " +
            "LEFT JOIN product p ON o.product_id = p.product_id " +
            "WHERE (:productName IS NULL OR p.product_name LIKE CONCAT('%', :productName, '%')) " +
            "AND (:memberName IS NULL OR m.member_name LIKE CONCAT('%', :memberName, '%'))",
            countQuery = "SELECT count(*) FROM orders o " +
                    "LEFT JOIN member m ON o.member_id = m.member_id " +
                    "WHERE (:productName IS NULL OR p.product_name LIKE CONCAT('%', :productName, '%')) " +
                    "AND (:memberName IS NULL OR m.member_name LIKE CONCAT('%', :memberName, '%'))",
            nativeQuery = true)
    Page<Order> searchOrders(@Param("productName") String productName, @Param("memberName") String memberName, Pageable pageable);
}
