package com.flashsale.backend.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.flashsale.backend.common.ApiResponse;
import com.flashsale.backend.common.ResultCode;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.MalformedJwtException;
import io.jsonwebtoken.security.SignatureException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.lang.NonNull;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;

/**
 * @author Yang-Hsu
 * @description Filter every request is Rt JWT valid ot not
 * @date 2026/1/8 下午 04:26
 */
@Slf4j
@Component
public class AuthJwtFilter extends OncePerRequestFilter {

    @Autowired
    private JwtUtils jwtUtils;

    @Override
    protected void doFilterInternal(@NonNull HttpServletRequest request, @NonNull HttpServletResponse response,
                                    @NonNull FilterChain filterChain) throws ServletException, IOException {

        String path = request.getRequestURI();

        if (path.startsWith("/api/client/auth/") ||
                path.startsWith("/api/client/open/") ||
                path.startsWith("/api/admin/")) {
            filterChain.doFilter(request, response);
            return;
        }

        try {
            String jwt = jwtUtils.getJwtFromCookies(request, "access_token");

            if (jwt == null) {
                filterChain.doFilter(request, response);
                return;
            }
            jwtUtils.validateJwtToken(jwt);
            Claims claims = jwtUtils.getClaimsFromToken(jwt);
            String username = claims.getSubject();
            String memberId = claims.get("id", String.class);
            String role = claims.get("role", String.class);

            List<SimpleGrantedAuthority> authorities = List.of(new SimpleGrantedAuthority(role));
            UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(
                    username, null, authorities);
            authentication.setDetails(memberId);
            SecurityContextHolder.getContext().setAuthentication(authentication);
        } catch (ExpiredJwtException e) {
            log.info("Access token expired for URI: {}", request.getRequestURI());
            handleError(response, ResultCode.ACCESS_TOKEN_EXPIRED);
            return;
        } catch (SignatureException | MalformedJwtException e) {
            log.warn("Invalid JWT signature attempt from IP: {}", request.getRemoteAddr());
            handleError(response, ResultCode.TOKEN_INVALID);
            return;
        } catch (Exception e) {
            log.error("Internal Auth Error: ", e);
            handleError(response, ResultCode.SYSTEM_ERROR);
            return;
        }

        filterChain.doFilter(request, response);
    }

    private void handleError(HttpServletResponse response, ResultCode rc) throws IOException {
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        response.setContentType("application/json;charset=UTF-8");
        ApiResponse<Object> apiRes = ApiResponse.of(rc);
        String json = new ObjectMapper().writeValueAsString(apiRes);
        response.getWriter().write(json);
    }
}