package com.flashsale.backend.service;

import com.flashsale.backend.dto.request.MemberRegistRequest;
import com.flashsale.backend.dto.request.MemberUpdateRequest;
import com.flashsale.backend.entity.Member;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

/**
 * @description Member management service interface — registration, profile, admin CRUD
 * @author Yang-Hsu
 * @date 2026/7/9
 */
public interface MemberService {

    Member addMember(MemberRegistRequest req);

    Member getMemberById(String memberId);

    Member updateMember(String memberId, MemberUpdateRequest req);

    void deleteMember(String memberId);

    Page<Member> getAllMembers(Pageable pageable);
}
