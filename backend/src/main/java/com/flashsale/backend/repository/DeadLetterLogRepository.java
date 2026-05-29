package com.flashsale.backend.repository;

import com.flashsale.backend.entity.DeadLetterLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface DeadLetterLogRepository extends JpaRepository<DeadLetterLog, String> {
}
