package com.flashsale.backend.repository;

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
     * @description When update Product Information check
     * @author Yang-Hsu
     * @date 2026/2/17 下午9:10
     */
    boolean existsByProductId(String productId);

    /**
     * @description find by product for event lisy
     * @author Yang-Hsu
     * @date 2026/2/17 下午9:11
     */
    Page<Event> findByProductId(String productId, Pageable pageable);

    /**
     * @description when delete product, delete event first
     * @author Yang-Hsu
     * @date 2026/2/17 下午9:12
     */
    @Modifying
    @Transactional
    void deleteByProductId(String productId);
}
