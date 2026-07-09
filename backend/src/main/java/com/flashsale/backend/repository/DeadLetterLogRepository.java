package com.flashsale.backend.repository;

import com.flashsale.backend.entity.DeadLetterLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * @description Repository for dead letter log — persists orders that exhausted all MQ retries
 * @author Yang-Hsu
 * @date 2026/7/9
 */
@Repository
public interface DeadLetterLogRepository extends JpaRepository<DeadLetterLog, String> {
}
