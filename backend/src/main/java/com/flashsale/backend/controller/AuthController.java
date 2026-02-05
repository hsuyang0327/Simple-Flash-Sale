package com.flashsale.backend.controller;

import com.flashsale.backend.common.ApiResponse;
import com.flashsale.backend.common.ResultCode;
import com.flashsale.backend.dto.request.LoginRequest;
import com.flashsale.backend.dto.response.JwtResponse;
import com.flashsale.backend.security.JwtUtils;
import com.flashsale.backend.service.AuthService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * @author Yang-Hsu
 * @description For Certification Controller
 * @date 2026/1/12 上午 10:44
 */
@Slf4j
@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;
    private final JwtUtils jwtUtils;

    /**
     * @description Api for login logical
     * @author Yang-Hsu
     * @date 2026/1/12 上午 11:05
     */
    @PostMapping("/login")
    public ResponseEntity<ApiResponse<String>> login(@Valid @RequestBody LoginRequest req) {

        JwtResponse jwtResponse = authService.login(req.getEmail(), req.getPassword());

        ResponseCookie accessCookie = jwtUtils.generateAccessResponseCookie(jwtResponse.getAccessToken());
        ResponseCookie refreshCookie = jwtUtils.generateRefreshResponseCookie(jwtResponse.getRefreshToken());

        log.info("User {} logged in successfully, cookies generated.", req.getEmail());

        return ResponseEntity.ok()
                .header(HttpHeaders.SET_COOKIE, accessCookie.toString())
                .header(HttpHeaders.SET_COOKIE, refreshCookie.toString())
                .body(ApiResponse.of(ResultCode.SUCCESS));
    }

    /**
     * @description API when need to refresh token and generate both at rt http only cookie
     * @author Yang-Hsu
     * @date 2026/1/12 上午 11:03
     */
    @PostMapping("/refresh")
    public ResponseEntity<ApiResponse<String>> refresh(HttpServletRequest request) {

        String refreshToken = jwtUtils.getJwtFromCookies(request, "refresh_token");
        JwtResponse jwtResponse = authService.refresh(refreshToken);

        ResponseCookie newAccessCookie = jwtUtils.generateAccessResponseCookie(jwtResponse.getAccessToken());
        ResponseCookie newRefreshCookie = jwtUtils.generateRefreshResponseCookie(jwtResponse.getRefreshToken());

        log.info("Token rotation completed for a refresh request.");

        return ResponseEntity.ok()
                .header(HttpHeaders.SET_COOKIE, newAccessCookie.toString())
                .header(HttpHeaders.SET_COOKIE, newRefreshCookie.toString())
                .body(ApiResponse.of(ResultCode.SUCCESS));
    }

    /**
     * @description API when is LogOut clear the cookie in backend
     * @author Yang-Hsu
     * @date 2026/1/12 上午 10:46
     */
    @PostMapping("/logout")
    public ResponseEntity<ApiResponse<String>> logout() {

        ResponseCookie cleanAccess = jwtUtils.getCleanAccessCookie();
        ResponseCookie cleanRefresh = jwtUtils.getCleanRefreshCookie();

        return ResponseEntity.ok()
                .header(HttpHeaders.SET_COOKIE, cleanAccess.toString())
                .header(HttpHeaders.SET_COOKIE, cleanRefresh.toString())
                .body(ApiResponse.of(ResultCode.SUCCESS));
    }
}