package com.flashsale.backend.service;

import com.flashsale.backend.dto.response.JwtResponse;

/**
 * @description Authentication service interface — login, token refresh, logout
 * @author Yang-Hsu
 * @date 2026/7/9
 */
public interface AuthService {

    JwtResponse login(String memberEmail, String memberPwd);

    JwtResponse refresh(String refreshToken);
}
