package com.flashsale.backend.service;

import com.flashsale.backend.common.ResultCode;
import com.flashsale.backend.entity.Member;
import com.flashsale.backend.exception.BusinessException;
import com.flashsale.backend.repository.MemberRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * @author Yang-Hsu
 * @description MemberService
 * @date 2026/1/9 下午 01:54
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class MemberService {

    private final MemberRepository memberRepository;

    /**
     * @description Register a new member
     * @author Yang-Hsu
     * @date 2026/1/9 下午 01:54
     */
    @Transactional
    public Member addMember(Member member) {
        if (memberRepository.existsByMemberEmail(member.getMemberEmail())) {
            log.warn("Registration failed: memberEmail {} already exists", member.getMemberEmail());
            throw new BusinessException(ResultCode.MEMBER_ALREADY_EXISTS);
        }

        log.info("Successfully registering member: {}", member.getMemberEmail());
        return memberRepository.save(member);
    }

    /**
     * @description Find by memberId
     * @author Yang-Hsu
     * @date 2026/1/9 下午 01:55
     */
    @Transactional(readOnly = true)
    public Member getMemberById(String memberId) {
        return memberRepository.findById(memberId)
                .orElseThrow(() -> {
                    log.warn("Search failed: memberId {} not found", memberId);
                    return new BusinessException(ResultCode.MEMBER_NOT_FOUND);
                });
    }

    /**
     * @description
     * @author Yang-Hsu
     * @date 2026/1/9 下午 01:55
     */
    @Transactional
    public Member updateMember(String memberId, Member updatedData) {
        Member existingMember = this.getMemberById(memberId);
        if (updatedData.getMemberName() != null && !updatedData.getMemberName().isBlank()) {
            existingMember.setMemberName(updatedData.getMemberName());
        }
        if (updatedData.getMemberPwd() != null && !updatedData.getMemberPwd().isBlank()) {
            existingMember.setMemberPwd(updatedData.getMemberPwd());
        }
        log.info("Successfully updated memberId: {}", memberId);
        return memberRepository.save(existingMember);
    }

    /**
     * @description Delete member by memberId
     * @author Yang-Hsu
     * @date 2026/1/9 下午 01:56
     */
    @Transactional
    public void deleteMember(String memberId) {
        if (!memberRepository.existsById(memberId)) {
            log.warn("Delete failed: memberId {} does not exist", memberId);
            throw new BusinessException(ResultCode.MEMBER_NOT_FOUND);
        }

        log.info("Deleting memberId: {}", memberId);
        memberRepository.deleteById(memberId);
    }

    /**
     * @description Find all Member
     * @author Yang-Hsu
     * @date 2026/1/9 下午 01:57
     */
    @Transactional(readOnly = true)
    public Page<Member> getAllMembers(Pageable pageable) {
        return memberRepository.findAll(pageable);
    }
}