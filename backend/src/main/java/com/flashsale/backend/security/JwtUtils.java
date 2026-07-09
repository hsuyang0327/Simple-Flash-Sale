package com.flashsale.backend.security;

import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import io.jsonwebtoken.security.SignatureException;
import jakarta.annotation.PostConstruct;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseCookie;
import org.springframework.stereotype.Component;
import org.springframework.web.util.WebUtils;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;


/**
 * @description Tool for JWT
 * @author Yang-Hsu
 * @date 2026/1/8
 */
@Slf4j
@Component
public class JwtUtils {

    @Value("${flashsale.jwt.secret}") //Need to use env to set in prod, for dev can set in application.properties
    private String jwtSecret;

    @Value("${flashsale.jwt.accessExpirationMs:900000}") //15 min
    private long accessExpirationMs;

    @Value("${flashsale.jwt.refreshExpirationMs:3600000}") // 1 hour
    private long refreshExpirationMs;

    @Value("${flashsale.jwt.cookieSecure:false}") // Default false for dev, set true in prod (need to notice)
    private boolean cookieSecure;

    private SecretKey key;

    @PostConstruct
    /**
     * @description Initialize JWT signing key from application properties
     * @author Yang-Hsu
     * @date 2026/7/9
     */
    public void init() {
        this.key = Keys.hmacShaKeyFor(jwtSecret.getBytes(StandardCharsets.UTF_8));
    }

    /**
     * @description Gen AT and Rt for login, refresh, register success
     * @author Yang-Hsu
     * @date 2026/1/8
     */
    public String generateAccessToken(String memberId, String username, String memberName) {
        return createToken(memberId, username, memberName, accessExpirationMs);
    }

    /**
     * @description Generate a signed refresh JWT token with member claims
     * @author Yang-Hsu
     * @date 2026/7/9
     */
    public String generateRefreshToken(String memberId, String username, String memberName) {
        return createToken(memberId, username, memberName, refreshExpirationMs);
    }

    private String createToken(String memberId, String username, String memberName, long expirationMs) {
        return Jwts.builder().setSubject(username).claim("id", memberId).claim("name", memberName)
                .claim("role", "ROLE_USER").setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + expirationMs))
                .signWith(key, SignatureAlgorithm.HS256).compact();
    }

    /**
     * @description Gen and clean http only cookie for login, refresh, register, logout success
     * @author Yang-Hsu
     * @date 2026/1/8
     */
    public ResponseCookie generateAccessResponseCookie(String jwt) {
        // access_token is intentionally a session cookie (no maxAge) — expires when browser closes.
        // The frontend detects 4004 (TOKEN_MISSING) and silently refreshes using refresh_token.
        return ResponseCookie.from("access_token", jwt).path("/").httpOnly(true)
                .secure(cookieSecure)
                .sameSite("Lax")
                .build();
    }

    /**
     * @description Build HttpOnly refresh token cookie for browser
     * @author Yang-Hsu
     * @date 2026/7/9
     */
    public ResponseCookie generateRefreshResponseCookie(String jwt) {
        return ResponseCookie.from("refresh_token", jwt).path("/api/client/auth/refresh").httpOnly(true)
                .secure(cookieSecure)
                .sameSite("Strict")
                .maxAge(refreshExpirationMs / 1000).build();
    }

    /**
     * @description Build expired access token cookie to clear from browser
     * @author Yang-Hsu
     * @date 2026/7/9
     */
    public ResponseCookie getCleanAccessCookie() {
        return ResponseCookie.from("access_token", "").path("/").maxAge(0).build();
    }

    /**
     * @description Build expired refresh token cookie to clear from browser
     * @author Yang-Hsu
     * @date 2026/7/9
     */
    public ResponseCookie getCleanRefreshCookie() {
        return ResponseCookie.from("refresh_token", "").path("/api/client/auth/refresh").maxAge(0).build();
    }

    /**
     * @description When request pass by filter need to get->valid
     * @author Yang-Hsu
     * @date 2026/1/8
     */
    public String getJwtFromCookies(HttpServletRequest request, String name) {
        Cookie cookie = WebUtils.getCookie(request, name);
        return (cookie != null) ? cookie.getValue() : null;
    }

    /**
     * @description getMemberIdFromToken
     * @author Yang-Hsu
     * @date 2026/2/9
     */
    public String getMemberIdFromToken(String token) {
        try {
            return Jwts.parserBuilder().setSigningKey(key).build().parseClaimsJws(token).getBody().get("id", String.class);
        } catch (ExpiredJwtException e) {
            return e.getClaims().get("id", String.class);
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * @description Validate JWT signature, expiry, and structure
     * @author Yang-Hsu
     * @date 2026/7/9
     */
    public void validateJwtToken(String authToken) throws ExpiredJwtException, SignatureException, MalformedJwtException, UnsupportedJwtException, IllegalArgumentException {
        Jwts.parserBuilder()
                .setSigningKey(key)
                .build()
                .parseClaimsJws(authToken);
    }

    /**
     * @description Parse and return all claims from a JWT token
     * @author Yang-Hsu
     * @date 2026/7/9
     */
    public Claims getClaimsFromToken(String token) {
        return Jwts.parserBuilder().setSigningKey(key).build().parseClaimsJws(token).getBody();
    }

}