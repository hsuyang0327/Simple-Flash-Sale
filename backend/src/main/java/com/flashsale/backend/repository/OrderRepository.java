package com.flashsale.backend.repository;

import com.flashsale.backend.entity.Order;
import jakarta.persistence.LockModeType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

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
    @Query("SELECT o FROM Order o " +
            "LEFT JOIN FETCH o.member m " +
            "LEFT JOIN FETCH o.product p " +
            "WHERE (:productName IS NULL OR :productName = '' OR p.productName LIKE %:productName%) " +
            "AND (:memberName IS NULL OR :memberName = '' OR m.memberName LIKE %:memberName%)")
    Page<Order> searchOrders(
            @Param("productName") String productName,
            @Param("memberName") String memberName,
            Pageable pageable
    );

    /**
     * @description findByIdForUpdate
     * @author Yang-Hsu
     * @date 2026/2/20 下午11:41
     */
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT o FROM Order o WHERE o.orderId = :orderId")
    Optional<Order> findByIdForUpdate(@Param("orderId") String orderId);

    /**
     * @description findByIdWithDetails
     * @author Yang-Hsu
     * @date 2026/2/23 上午12:43
     */
    @Query("SELECT o FROM Order o " +
            "LEFT JOIN FETCH o.member " +
            "LEFT JOIN FETCH o.product " +
            "WHERE o.orderId = :id")
    Optional<Order> findByIdWithDetails(@Param("id") String id);
}
