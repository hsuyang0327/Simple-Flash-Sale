package com.flashsale.backend.service;

import com.flashsale.backend.common.ResultCode;
import com.flashsale.backend.dto.JwtResponse;
import com.flashsale.backend.entity.Member;
import com.flashsale.backend.exception.BusinessException;
import com.flashsale.backend.repository.MemberRepository;
import com.flashsale.backend.security.JwtUtils;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * @author Yang-Hsu
 * @description Service for Certification
 * @date 2026/1/12 上午 10:50
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AuthService {

    private final MemberRepository memberRepository;
    private final JwtUtils jwtUtils;


    /**
     * @description Login logical for generate at and rt
     * @author Yang-Hsu
     * @date 2026/1/12 上午 10:51
     */
    @Transactional(readOnly = true)
    public JwtResponse login(String memberEmail, String memberPwd) {
        Member member = memberRepository.findByMemberEmail(memberEmail).orElseThrow(() -> {
            log.warn("Login failed: User email {} not found", memberEmail);
            return new BusinessException(ResultCode.MEMBER_NOT_FOUND);
        });

        if (!member.getMemberPwd().equals(memberPwd)) {
            log.warn("Login failed: Password mismatch for user {}", memberEmail);
            throw new BusinessException(ResultCode.LOGIN_FAILED);
        }

        String accessToken = jwtUtils.generateAccessToken(member.getMemberId(), member.getMemberEmail(),
                member.getMemberName());
        String refreshToken = jwtUtils.generateRefreshToken(member.getMemberId(), member.getMemberEmail(),
                member.getMemberName());

        log.info("User {} logged in successfully", memberEmail);

        return JwtResponse.builder().accessToken(accessToken).refreshToken(refreshToken).build();
    }

    /**
     * @description Refresh logical, base on token rotation , it will generate new at and rt
     * @author Yang-Hsu
     * @date 2026/1/12 上午 10:52
     */
    public JwtResponse refresh(String refreshToken) {
        if (refreshToken == null || refreshToken.isEmpty()) {
            throw new BusinessException(ResultCode.TOKEN_MISSING);
        }

        Claims claims;
        try {
            claims = jwtUtils.getClaimsFromToken(refreshToken);
        } catch (ExpiredJwtException e) {
            log.warn("Refresh failed: Refresh token expired");
            throw new BusinessException(ResultCode.REFRESH_TOKEN_EXPIRED);
        } catch (Exception e) {
            log.error("Refresh failed: Invalid token format or signature: {}", e.getMessage());
            throw new BusinessException(ResultCode.TOKEN_INVALID);
        }

        String memberId = claims.get("id", String.class);

        Member member = memberRepository.findById(memberId).orElseThrow(() -> {
            log.error("Refresh failed: Member ID {} not found in database", memberId);
            return new BusinessException(ResultCode.MEMBER_NOT_FOUND);
        });

        String newAccess = jwtUtils.generateAccessToken(member.getMemberId(), member.getMemberEmail(),
                member.getMemberName());
        String newRefresh = jwtUtils.generateRefreshToken(member.getMemberId(), member.getMemberEmail(),
                member.getMemberName());

        log.info("Tokens successfully rotated for Member ID: {}", memberId);

        return JwtResponse.builder().accessToken(newAccess).refreshToken(newRefresh).build();
    }
}