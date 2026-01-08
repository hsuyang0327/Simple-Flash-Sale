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
 * @author Yang-Hsu
 * @description Tool for JWT
 * @date 2026/1/8 下午 03:47
 */
@Slf4j
@Component
public class JwtUtils {

    @Value("${flashsale.jwt.secret:your-super-secret-key-at-least-32-chars-long}")
    private String jwtSecret;

    @Value("${flashsale.jwt.accessExpirationMs:900000}") //15 min
    private long accessExpirationMs;

    @Value("${flashsale.jwt.refreshExpirationMs:3600000}") // 1 hour
    private long refreshExpirationMs;

    private SecretKey key;

    @PostConstruct
    public void init() {
        this.key = Keys.hmacShaKeyFor(jwtSecret.getBytes(StandardCharsets.UTF_8));
    }

    /**
     * @description Gen AT and Rt for login, refresh, register success
     * @author Yang-Hsu
     * @date 2026/1/8 下午 03:48
     */
    public String generateAccessToken(String memberId, String username, String memberName) {
        return createToken(memberId, username, memberName, accessExpirationMs);
    }

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
     * @date 2026/1/8 下午 03:49
     */
    public ResponseCookie generateAccessResponseCookie(String jwt) {
        return ResponseCookie.from("access_token", jwt).path("/").httpOnly(true)
                .secure(false)      // localhost 開發設 false，正式環境 HTTPS 務必設 true
                .sameSite("Lax")    // 允許一般跨站點連結攜帶，兼顧體驗與安全
                .maxAge(accessExpirationMs / 1000).build();
    }

    public ResponseCookie generateRefreshResponseCookie(String jwt) {
        return ResponseCookie.from("refresh_token", jwt).path("/api/auth/refresh") // 金卡隔離：只有換票請求才會攜帶
                .httpOnly(true).secure(false).sameSite("Strict") // 最高安全性：禁止任何跨站點請求攜帶
                .maxAge(refreshExpirationMs / 1000).build();
    }

    public ResponseCookie getCleanAccessCookie() {
        return ResponseCookie.from("access_token", null).path("/").maxAge(0).build();
    }

    public ResponseCookie getCleanRefreshCookie() {
        return ResponseCookie.from("refresh_token", null).path("/auth/refresh").maxAge(0).build();
    }

    /**
     * @description When request pass by filter need to get->valid
     * @author Yang-Hsu
     * @date 2026/1/8 下午 03:53
     */
    public String getJwtFromCookies(HttpServletRequest request, String name) {
        Cookie cookie = WebUtils.getCookie(request, name);
        return (cookie != null) ? cookie.getValue() : null;
    }

    public boolean validateJwtToken(String authToken) {
        try {
            Jwts.parserBuilder().setSigningKey(key).build().parseClaimsJws(authToken);
            return true;
        } catch (ExpiredJwtException e) {
            log.error("JWT token is expired: {}", e.getMessage());
        } catch (SignatureException e) {
            log.error("Invalid JWT signature: {}", e.getMessage());
        } catch (MalformedJwtException e) {
            log.error("Invalid JWT token format: {}", e.getMessage());
        } catch (Exception e) {
            log.error("JWT validation error: {}", e.getMessage());
        }
        return false;
    }

    public Claims getClaimsFromToken(String token) {
        return Jwts.parserBuilder().setSigningKey(key).build().parseClaimsJws(token).getBody();
    }
}