package com.flashsale.backend.service;

import com.flashsale.backend.common.ResultCode;
import com.flashsale.backend.dto.response.JwtResponse;
import com.flashsale.backend.entity.Member;
import com.flashsale.backend.exception.BusinessException;
import com.flashsale.backend.repository.MemberRepository;
import com.flashsale.backend.security.JwtUtils;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.MalformedJwtException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

/**
 * @description AuthServiceTest(By using mock not for db)
 * @author Yang-Hsu
 * @date 2026/7/17
 */
@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock
    private MemberRepository memberRepository;

    @Mock
    private JwtUtils jwtUtils;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private AuthServiceImpl authService;

    @Test
    @DisplayName("登入成功 - 帳號密碼正確")
    void login_validCredentials_returnsJwtResponse() {
        String email = "user@example.com";
        String rawPwd = "password123";
        Member member = new Member();
        member.setMemberId(UUID.randomUUID().toString());
        member.setMemberEmail(email);
        member.setMemberPwd("encodedPwd");
        member.setMemberName("Tester");

        when(memberRepository.findByMemberEmail(email)).thenReturn(Optional.of(member));
        when(passwordEncoder.matches(rawPwd, member.getMemberPwd())).thenReturn(true);
        when(jwtUtils.generateAccessToken(member.getMemberId(), member.getMemberEmail(), member.getMemberName()))
                .thenReturn("access-token");
        when(jwtUtils.generateRefreshToken(member.getMemberId(), member.getMemberEmail(), member.getMemberName()))
                .thenReturn("refresh-token");

        JwtResponse result = authService.login(email, rawPwd);

        assertNotNull(result);
        assertEquals("access-token", result.getAccessToken());
        assertEquals("refresh-token", result.getRefreshToken());
    }

    @Test
    @DisplayName("登入失敗 - 找不到會員")
    void login_memberNotFound_throwsBusinessException() {
        String email = "missing@example.com";
        when(memberRepository.findByMemberEmail(email)).thenReturn(Optional.empty());

        BusinessException exception = assertThrows(BusinessException.class,
                () -> authService.login(email, "anyPwd"));

        assertEquals(ResultCode.MEMBER_NOT_FOUND, exception.getResultCode());
    }

    @Test
    @DisplayName("登入失敗 - 密碼錯誤")
    void login_passwordMismatch_throwsBusinessException() {
        String email = "user@example.com";
        Member member = new Member();
        member.setMemberEmail(email);
        member.setMemberPwd("encodedPwd");

        when(memberRepository.findByMemberEmail(email)).thenReturn(Optional.of(member));
        when(passwordEncoder.matches("wrongPwd", member.getMemberPwd())).thenReturn(false);

        BusinessException exception = assertThrows(BusinessException.class,
                () -> authService.login(email, "wrongPwd"));

        assertEquals(ResultCode.LOGIN_FAILED, exception.getResultCode());
    }

    @Test
    @DisplayName("刷新成功 - Token 輪替")
    void refresh_validToken_returnsRotatedJwtResponse() {
        String refreshToken = "valid-refresh-token";
        String memberId = UUID.randomUUID().toString();

        Claims claims = mock(Claims.class);
        when(claims.get("id", String.class)).thenReturn(memberId);
        when(jwtUtils.getClaimsFromToken(refreshToken)).thenReturn(claims);

        Member member = new Member();
        member.setMemberId(memberId);
        member.setMemberEmail("user@example.com");
        member.setMemberName("Tester");
        when(memberRepository.findById(memberId)).thenReturn(Optional.of(member));

        when(jwtUtils.generateAccessToken(memberId, member.getMemberEmail(), member.getMemberName()))
                .thenReturn("new-access-token");
        when(jwtUtils.generateRefreshToken(memberId, member.getMemberEmail(), member.getMemberName()))
                .thenReturn("new-refresh-token");

        JwtResponse result = authService.refresh(refreshToken);

        assertNotNull(result);
        assertEquals("new-access-token", result.getAccessToken());
        assertEquals("new-refresh-token", result.getRefreshToken());
    }

    @Test
    @DisplayName("刷新失敗 - Token 為空字串")
    void refresh_emptyToken_throwsRefreshTokenExpired() {
        BusinessException exception = assertThrows(BusinessException.class,
                () -> authService.refresh(""));

        assertEquals(ResultCode.REFRESH_TOKEN_EXPIRED, exception.getResultCode());
        verifyNoInteractions(memberRepository);
    }

    @Test
    @DisplayName("刷新失敗 - Token 已過期")
    void refresh_expiredToken_throwsRefreshTokenExpired() {
        String refreshToken = "expired-refresh-token";
        when(jwtUtils.getClaimsFromToken(refreshToken)).thenThrow(mock(ExpiredJwtException.class));

        BusinessException exception = assertThrows(BusinessException.class,
                () -> authService.refresh(refreshToken));

        assertEquals(ResultCode.REFRESH_TOKEN_EXPIRED, exception.getResultCode());
    }

    @Test
    @DisplayName("刷新失敗 - Token 格式或簽章錯誤")
    void refresh_invalidTokenFormat_throwsTokenInvalid() {
        String refreshToken = "malformed-token";
        when(jwtUtils.getClaimsFromToken(refreshToken)).thenThrow(new MalformedJwtException("bad token"));

        BusinessException exception = assertThrows(BusinessException.class,
                () -> authService.refresh(refreshToken));

        assertEquals(ResultCode.TOKEN_INVALID, exception.getResultCode());
    }

    @Test
    @DisplayName("刷新失敗 - Token 中的會員不存在")
    void refresh_memberNotFound_throwsBusinessException() {
        String refreshToken = "valid-refresh-token";
        String memberId = UUID.randomUUID().toString();

        Claims claims = mock(Claims.class);
        when(claims.get("id", String.class)).thenReturn(memberId);
        when(jwtUtils.getClaimsFromToken(refreshToken)).thenReturn(claims);
        when(memberRepository.findById(memberId)).thenReturn(Optional.empty());

        BusinessException exception = assertThrows(BusinessException.class,
                () -> authService.refresh(refreshToken));

        assertEquals(ResultCode.MEMBER_NOT_FOUND, exception.getResultCode());
    }
}
