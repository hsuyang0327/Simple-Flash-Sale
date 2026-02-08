package com.flashsale.backend.service;

import com.flashsale.backend.common.ResultCode;
import com.flashsale.backend.common.util.BeanCopyUtil;
import com.flashsale.backend.dto.request.MemberRegistRequest;
import com.flashsale.backend.dto.request.MemberUpdateRequest;
import com.flashsale.backend.entity.Member;
import com.flashsale.backend.exception.BusinessException;
import com.flashsale.backend.repository.MemberRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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
    public Member addMember(MemberRegistRequest req) {
        log.info("Creating member: {}", req.getMemberEmail());
        if (memberRepository.existsByMemberEmail(req.getMemberEmail())) {
            log.warn("Registration failed: memberEmail {} already exists", req.getMemberEmail());
            throw new BusinessException(ResultCode.MEMBER_ALREADY_EXISTS);
        }
        Member member = new Member();
        BeanUtils.copyProperties(req, member);
        Member savedMember = memberRepository.save(member);
        log.info("Member created successfully with ID: {}", savedMember.getMemberId());
        return savedMember;
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
    public Member updateMember(String memberId, MemberUpdateRequest req) {
        log.info("Updating member with ID: {}", memberId);
        Member existingMember = this.getMemberById(memberId);
        BeanUtils.copyProperties(req, existingMember, BeanCopyUtil.getNullPropertyNames(req));
        Member updatedMember = memberRepository.save(existingMember);
        log.info("Member updated successfully: {}", memberId);
        return updatedMember;
    }

    /**
     * @description Delete member by memberId
     * @author Yang-Hsu
     * @date 2026/1/9 下午 01:56
     */
    @Transactional
    public void deleteMember(String memberId) {
        log.info("Deleting member with ID: {}", memberId);
        if (!memberRepository.existsById(memberId)) {
            log.warn("Delete failed: memberId {} does not exist", memberId);
            throw new BusinessException(ResultCode.MEMBER_NOT_FOUND);
        }
        memberRepository.deleteById(memberId);
        log.info("Member deleted successfully: {}", memberId);
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
