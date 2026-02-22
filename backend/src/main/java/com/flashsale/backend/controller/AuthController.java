package com.flashsale.backend.controller;

import com.flashsale.backend.common.ApiResponse;
import com.flashsale.backend.common.ResultCode;
import com.flashsale.backend.dto.request.LoginRequest;
import com.flashsale.backend.dto.response.JwtResponse;
import com.flashsale.backend.security.JwtUtils;
import com.flashsale.backend.service.AuthService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * @author Yang-Hsu
 * @description For Certification Controller
 * @date 2026/1/12 上午 10:44
 */
@Tag(name = "Authentication", description = "APIs for user authentication, including login, logout, and token refresh.")
@Slf4j
@RestController
@RequestMapping("/api/client/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;
    private final JwtUtils jwtUtils;

    @Operation(summary = "User Login", description = "Authenticates a user and returns JWT access and refresh tokens in secure HTTP-only cookies.")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Login successful"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "Invalid credentials")
    })
    @PostMapping("/login")
    public ResponseEntity<ApiResponse<String>> login(@Valid @RequestBody LoginRequest req) {
        log.info("API: Login (Client): {}", req.getEmail());

        JwtResponse jwtResponse = authService.login(req.getEmail(), req.getPassword());

        ResponseCookie accessCookie = jwtUtils.generateAccessResponseCookie(jwtResponse.getAccessToken());
        ResponseCookie refreshCookie = jwtUtils.generateRefreshResponseCookie(jwtResponse.getRefreshToken());

        log.info("User {} logged in successfully, cookies generated.", req.getEmail());

        return ResponseEntity.ok()
                .header(HttpHeaders.SET_COOKIE, accessCookie.toString())
                .header(HttpHeaders.SET_COOKIE, refreshCookie.toString())
                .body(ApiResponse.of(ResultCode.SUCCESS));
    }

    @Operation(summary = "Refresh Token", description = "Refreshes an expired access token using a valid refresh token. Both tokens are rotated and returned in new HTTP-only cookies.")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Token refreshed successfully"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "Invalid or expired refresh token")
    })
    @PostMapping("/refresh")
    public ResponseEntity<ApiResponse<String>> refresh(HttpServletRequest request) {
        log.info("API: Refresh token (Client)");

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

    @Operation(summary = "User Logout", description = "Logs out the user by clearing the access and refresh token cookies.")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Logout successful")
    })
    @PostMapping("/logout")
    public ResponseEntity<ApiResponse<String>> logout(HttpServletRequest request) {
        String token = jwtUtils.getJwtFromCookies(request, "access_token");
        String userId = (token != null && !token.isEmpty()) ? jwtUtils.getMemberIdFromToken(token) : null;
        log.info("API: Logout (Client): {}", userId != null ? userId : "Anonymous/Expired");

        ResponseCookie cleanAccess = jwtUtils.getCleanAccessCookie();
        ResponseCookie cleanRefresh = jwtUtils.getCleanRefreshCookie();
        return ResponseEntity.ok()
                .header(HttpHeaders.SET_COOKIE, cleanAccess.toString())
                .header(HttpHeaders.SET_COOKIE, cleanRefresh.toString())
                .body(ApiResponse.of(ResultCode.SUCCESS));
    }
}
