package com.flashsale.backend.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.flashsale.backend.common.ResultCode;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.MalformedJwtException;
import io.jsonwebtoken.security.SignatureException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.security.core.context.SecurityContextHolder;

import java.io.PrintWriter;
import java.io.StringWriter;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * @description AuthJwtFilterTest(By using mock, not booting Spring context)
 * @author Yang-Hsu
 * @date 2026/7/17
 */
@ExtendWith(MockitoExtension.class)
class AuthJwtFilterTest {

    @Mock
    private JwtUtils jwtUtils;

    @Mock
    private HttpServletRequest request;

    @Mock
    private HttpServletResponse response;

    @Mock
    private FilterChain filterChain;

    @InjectMocks
    private AuthJwtFilter authJwtFilter;

    private StringWriter responseBody;

    @BeforeEach
    void setUp() throws Exception {
        responseBody = new StringWriter();
        lenient().when(response.getWriter()).thenReturn(new PrintWriter(responseBody));
    }

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    @ParameterizedTest
    @ValueSource(strings = {
            "/api/client/auth/login",
            "/api/client/auth/refresh",
            "/api/client/auth/logout",
            "/api/client/open/events",
            "/api/admin/products",
            "/api/test/reset",
            "/swagger-ui/index.html",
            "/v3/api-docs",
            "/swagger-resources/configuration"
    })
    void doFilterInternal_whitelistedPath_bypassesJwtValidation(String path) throws Exception {
        when(request.getRequestURI()).thenReturn(path);

        authJwtFilter.doFilterInternal(request, response, filterChain);

        verify(filterChain).doFilter(request, response);
        verifyNoInteractions(jwtUtils);
    }

    @Test
    void doFilterInternal_protectedPathMissingCookie_returnsTokenMissing() throws Exception {
        when(request.getRequestURI()).thenReturn("/api/client/orders/status");
        when(jwtUtils.getJwtFromCookies(request, "access_token")).thenReturn(null);

        authJwtFilter.doFilterInternal(request, response, filterChain);

        verify(response).setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        assertResultCode(ResultCode.TOKEN_MISSING);
        verify(filterChain, never()).doFilter(any(), any());
    }

    @Test
    void doFilterInternal_validToken_setsAuthenticationAndContinuesChain() throws Exception {
        when(request.getRequestURI()).thenReturn("/api/client/orders/status");
        when(jwtUtils.getJwtFromCookies(request, "access_token")).thenReturn("valid-jwt");

        Claims claims = mock(Claims.class);
        when(claims.getSubject()).thenReturn("user1");
        when(claims.get("id", String.class)).thenReturn("member-123");
        when(claims.get("role", String.class)).thenReturn("ROLE_USER");
        when(jwtUtils.getClaimsFromToken("valid-jwt")).thenReturn(claims);

        authJwtFilter.doFilterInternal(request, response, filterChain);

        var authentication = SecurityContextHolder.getContext().getAuthentication();
        assertNotNull(authentication);
        assertEquals("user1", authentication.getName());
        assertEquals("member-123", authentication.getDetails());
        assertTrue(authentication.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_USER")));
        verify(filterChain).doFilter(request, response);
        verify(response, never()).setStatus(anyInt());
    }

    @Test
    void doFilterInternal_expiredToken_returnsAccessTokenExpired() throws Exception {
        when(request.getRequestURI()).thenReturn("/api/client/orders/status");
        when(jwtUtils.getJwtFromCookies(request, "access_token")).thenReturn("expired-jwt");
        doThrow(mock(ExpiredJwtException.class)).when(jwtUtils).validateJwtToken("expired-jwt");

        authJwtFilter.doFilterInternal(request, response, filterChain);

        verify(response).setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        assertResultCode(ResultCode.ACCESS_TOKEN_EXPIRED);
        verify(filterChain, never()).doFilter(any(), any());
    }

    @Test
    void doFilterInternal_signatureException_returnsTokenInvalid() throws Exception {
        when(request.getRequestURI()).thenReturn("/api/client/orders/status");
        when(jwtUtils.getJwtFromCookies(request, "access_token")).thenReturn("tampered-jwt");
        doThrow(new SignatureException("bad signature")).when(jwtUtils).validateJwtToken("tampered-jwt");

        authJwtFilter.doFilterInternal(request, response, filterChain);

        verify(response).setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        assertResultCode(ResultCode.TOKEN_INVALID);
        verify(filterChain, never()).doFilter(any(), any());
    }

    @Test
    void doFilterInternal_malformedJwtException_returnsTokenInvalid() throws Exception {
        when(request.getRequestURI()).thenReturn("/api/client/orders/status");
        when(jwtUtils.getJwtFromCookies(request, "access_token")).thenReturn("garbage");
        doThrow(new MalformedJwtException("malformed")).when(jwtUtils).validateJwtToken("garbage");

        authJwtFilter.doFilterInternal(request, response, filterChain);

        verify(response).setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        assertResultCode(ResultCode.TOKEN_INVALID);
        verify(filterChain, never()).doFilter(any(), any());
    }

    @Test
    void doFilterInternal_unexpectedException_returnsSystemError() throws Exception {
        when(request.getRequestURI()).thenReturn("/api/client/orders/status");
        when(jwtUtils.getJwtFromCookies(request, "access_token")).thenReturn("valid-jwt");
        doThrow(new RuntimeException("unexpected failure")).when(jwtUtils).getClaimsFromToken("valid-jwt");

        authJwtFilter.doFilterInternal(request, response, filterChain);

        verify(response).setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        assertResultCode(ResultCode.SYSTEM_ERROR);
        verify(filterChain, never()).doFilter(any(), any());
    }

    private void assertResultCode(ResultCode expected) throws Exception {
        var node = new ObjectMapper().readTree(responseBody.toString());
        assertEquals(expected.getCode(), node.get("code").asInt());
    }
}
