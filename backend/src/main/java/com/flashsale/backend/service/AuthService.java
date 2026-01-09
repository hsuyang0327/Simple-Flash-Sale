package com.flashsale.backend.service;

import com.flashsale.backend.common.ResultCode;
import com.flashsale.backend.entity.Member;
import com.flashsale.backend.exception.BusinessException;
import com.flashsale.backend.repository.MemberRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

/**
 * @author Yang-Hsu
 * @description AuthService for login logical
 * @date 2026/1/9 下午 02:07
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AuthService {

    private final MemberRepository memberRepository;

    /**
     * @description Member login
     * @author Yang-Hsu
     * @date 2026/1/9 下午 02:07
     */
    public Member login(String memberEmail, String memberPwd) {
        Member member = memberRepository.findByMemberEmail(memberEmail).orElseThrow(() -> {
            log.warn("Login failed: memberEmail {} not found", memberEmail);
            return new BusinessException(ResultCode.MEMBER_NOT_FOUND);
        });
        if (!member.getMemberPwd().equals(memberPwd)) {
            log.warn("Login failed: password mismatch for memberEmail {}", memberEmail);
            throw new BusinessException(ResultCode.LOGIN_FAILED);
        }
        log.info("Member {} login success", memberEmail);
        return member;
    }
}