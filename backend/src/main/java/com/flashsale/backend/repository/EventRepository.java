package com.flashsale.backend.repository;

import com.flashsale.backend.dto.response.EventProductDTO;
import com.flashsale.backend.entity.Event;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

/**
 * @author Yang-Hsu
 * @description EventRepository
 * @date 2026/2/17 下午9:09
 */
public interface EventRepository extends JpaRepository<Event, String> {

    /**
     * @description decreaseStock for orderService DataBase
     * @author Yang-Hsu
     * @date 2026/2/17 下午9:09
     */
    @Modifying
    @Transactional
    @Query("UPDATE Event e SET e.stock = e.stock - :qty WHERE e.eventId = :id AND e.stock >= :qty")
    int decreaseStock(@Param("id") String id, @Param("qty") Integer qty);

    /**
     * @description
     * @author Yang-Hsu
     * @date 2026/2/22 上午2:36
     */
    @Modifying
    @Transactional
    @Query("UPDATE Event e SET e.stock = e.stock + :qty WHERE e.eventId = :id")
    void increaseStock(@Param("id") String id, @Param("qty") Integer qty);

    /**
     * @description When update Product Information check
     * @author Yang-Hsu
     * @date 2026/2/17 下午9:10
     */
    @Query("SELECT COUNT(e) > 0 FROM Event e WHERE e.product.productId = :productId")
    boolean existsByProductId(@Param("productId") String productId);

    /**
     * @description find by product for event lisy
     * @author Yang-Hsu
     * @date 2026/2/17 下午9:11
     */
    @Query("SELECT e FROM Event e WHERE e.product.productId = :productId")
    Page<Event> findByProductId(@Param("productId") String productId, Pageable pageable);

    /**
     * @description when delete product, delete event first
     * @author Yang-Hsu
     * @date 2026/2/17 下午9:12
     */
    @Modifying
    @Transactional
    @Query("DELETE FROM Event e WHERE e.product.productId = :productId")
    void deleteByProductId(@Param("productId") String productId);

    /**
     * @description countActiveEventsByProductId
     * @author Yang-Hsu
     * @date 2026/2/17 下午11:22
     */
    @Query("SELECT COUNT(e) FROM Event e WHERE e.product.productId = :productId AND e.status = 1")
    long countActiveEventsByProductId(@Param("productId") String productId);

    /**
     * @description findActiveEventsWithActiveProducts
     * @author Yang-Hsu
     * @date 2026/2/18 下午7:13
     */
    @Query("SELECT new com.flashsale.backend.dto.response.EventProductDTO(" +
            "p.productId, " +
            "p.productName, " +
            "p.description, " +
            "e.eventId, " +
            "e.price, " +
            "e.stock, " +
            "e.startTime, " +
            "e.endTime) " +
            "FROM Event e " +
            "JOIN e.product p " +
            "WHERE e.status = 1 AND p.status = 1 " +
            "AND e.startTime BETWEEN :startTime AND :endTime")
    List<EventProductDTO> findPreheatEvents(@Param("startTime") LocalDateTime startTime, @Param("endTime") LocalDateTime endTime);
}
