package com.flashsale.backend.security;

import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.MalformedJwtException;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import io.jsonwebtoken.security.SignatureException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.ResponseCookie;
import org.springframework.test.util.ReflectionTestUtils;

import javax.crypto.SecretKey;
import java.util.Date;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * @description JwtUtilsTest(By using pure unit test, no Spring context)
 * @author Yang-Hsu
 * @date 2026/7/17
 */
class JwtUtilsTest {

    private JwtUtils jwtUtils;
    private static final String SECRET = "test-secret-key-for-jwt-utils-test-must-be-long-enough-hs256";

    @BeforeEach
    void setUp() {
        jwtUtils = new JwtUtils();
        ReflectionTestUtils.setField(jwtUtils, "jwtSecret", SECRET);
        ReflectionTestUtils.setField(jwtUtils, "accessExpirationMs", 900000L);
        ReflectionTestUtils.setField(jwtUtils, "refreshExpirationMs", 3600000L);
        ReflectionTestUtils.setField(jwtUtils, "cookieSecure", false);
        jwtUtils.init();
    }

    @Test
    void generateAccessToken_validInput_containsCorrectClaims() {
        String token = jwtUtils.generateAccessToken("member-1", "user1", "User One");

        var claims = jwtUtils.getClaimsFromToken(token);

        assertEquals("user1", claims.getSubject());
        assertEquals("member-1", claims.get("id", String.class));
        assertEquals("User One", claims.get("name", String.class));
        assertEquals("ROLE_USER", claims.get("role", String.class));
    }

    @Test
    void generateRefreshToken_validInput_containsCorrectClaims() {
        String token = jwtUtils.generateRefreshToken("member-2", "user2", "User Two");

        var claims = jwtUtils.getClaimsFromToken(token);

        assertEquals("user2", claims.getSubject());
        assertEquals("member-2", claims.get("id", String.class));
    }

    @Test
    void validateJwtToken_validToken_doesNotThrow() {
        String token = jwtUtils.generateAccessToken("member-1", "user1", "User One");

        assertDoesNotThrow(() -> jwtUtils.validateJwtToken(token));
    }

    @Test
    void validateJwtToken_expiredToken_throwsExpiredJwtException() {
        SecretKey key = (SecretKey) ReflectionTestUtils.getField(jwtUtils, "key");
        String expiredToken = Jwts.builder()
                .setSubject("user1")
                .claim("id", "member-1")
                .setIssuedAt(new Date(System.currentTimeMillis() - 20000))
                .setExpiration(new Date(System.currentTimeMillis() - 10000))
                .signWith(key, SignatureAlgorithm.HS256)
                .compact();

        assertThrows(ExpiredJwtException.class, () -> jwtUtils.validateJwtToken(expiredToken));
    }

    @Test
    void validateJwtToken_wrongSigningKey_throwsSignatureException() {
        SecretKey differentKey = Keys.hmacShaKeyFor("a-completely-different-secret-key-for-signature-mismatch".getBytes());
        String tokenSignedWithDifferentKey = Jwts.builder()
                .setSubject("user1")
                .claim("id", "member-1")
                .setExpiration(new Date(System.currentTimeMillis() + 900000))
                .signWith(differentKey, SignatureAlgorithm.HS256)
                .compact();

        assertThrows(SignatureException.class, () -> jwtUtils.validateJwtToken(tokenSignedWithDifferentKey));
    }

    @Test
    void validateJwtToken_malformedToken_throwsMalformedJwtException() {
        String garbage = "not.a.valid.jwt.token";

        assertThrows(MalformedJwtException.class, () -> jwtUtils.validateJwtToken(garbage));
    }

    @Test
    void getMemberIdFromToken_validToken_returnsId() {
        String token = jwtUtils.generateAccessToken("member-1", "user1", "User One");

        assertEquals("member-1", jwtUtils.getMemberIdFromToken(token));
    }

    @Test
    void getMemberIdFromToken_expiredToken_stillReturnsId() {
        SecretKey key = (SecretKey) ReflectionTestUtils.getField(jwtUtils, "key");
        String expiredToken = Jwts.builder()
                .setSubject("user1")
                .claim("id", "member-1")
                .setIssuedAt(new Date(System.currentTimeMillis() - 20000))
                .setExpiration(new Date(System.currentTimeMillis() - 10000))
                .signWith(key, SignatureAlgorithm.HS256)
                .compact();

        assertEquals("member-1", jwtUtils.getMemberIdFromToken(expiredToken));
    }

    @Test
    void getMemberIdFromToken_garbageToken_returnsNull() {
        assertNull(jwtUtils.getMemberIdFromToken("garbage-token-value"));
    }

    @Test
    void getJwtFromCookies_cookiePresent_returnsValue() {
        HttpServletRequest request = mock(HttpServletRequest.class);
        Cookie cookie = new Cookie("access_token", "jwt-value-123");
        when(request.getCookies()).thenReturn(new Cookie[]{cookie});

        assertEquals("jwt-value-123", jwtUtils.getJwtFromCookies(request, "access_token"));
    }

    @Test
    void getJwtFromCookies_cookieAbsent_returnsNull() {
        HttpServletRequest request = mock(HttpServletRequest.class);
        when(request.getCookies()).thenReturn(new Cookie[]{});

        assertNull(jwtUtils.getJwtFromCookies(request, "access_token"));
    }

    @Test
    void generateAccessResponseCookie_returnsSessionCookieWithoutMaxAge() {
        ResponseCookie cookie = jwtUtils.generateAccessResponseCookie("jwt-value");

        assertEquals("access_token", cookie.getName());
        assertEquals("jwt-value", cookie.getValue());
        assertEquals("/", cookie.getPath());
        assertTrue(cookie.isHttpOnly());
        assertEquals("Lax", cookie.getSameSite());
        assertTrue(cookie.getMaxAge().isNegative());
    }

    @Test
    void generateRefreshResponseCookie_returnsScopedCookieWithMaxAge() {
        ResponseCookie cookie = jwtUtils.generateRefreshResponseCookie("refresh-jwt-value");

        assertEquals("refresh_token", cookie.getName());
        assertEquals("/api/client/auth/refresh", cookie.getPath());
        assertTrue(cookie.isHttpOnly());
        assertEquals("Strict", cookie.getSameSite());
        assertEquals(3600, cookie.getMaxAge().getSeconds());
    }

    @Test
    void getCleanAccessCookie_returnsExpiredCookie() {
        ResponseCookie cookie = jwtUtils.getCleanAccessCookie();

        assertEquals("access_token", cookie.getName());
        assertEquals("", cookie.getValue());
        assertEquals(0, cookie.getMaxAge().getSeconds());
    }

    @Test
    void getCleanRefreshCookie_returnsExpiredCookie() {
        ResponseCookie cookie = jwtUtils.getCleanRefreshCookie();

        assertEquals("refresh_token", cookie.getName());
        assertEquals("/api/client/auth/refresh", cookie.getPath());
        assertEquals(0, cookie.getMaxAge().getSeconds());
    }
}
