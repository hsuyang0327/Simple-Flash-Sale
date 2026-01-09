package com.flashsale.backend.repository;

import com.flashsale.backend.entity.Member;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * @description repository method for member
 * @author Yang-Hsu
 * @date 2026/1/9 上午 11:22
 */
@Repository
public interface MemberRepository extends JpaRepository<Member, String> {
    Optional<Member> findByMemberEmail(String memberEmail);
    boolean existsByMemberEmail(String memberEmail);
}
