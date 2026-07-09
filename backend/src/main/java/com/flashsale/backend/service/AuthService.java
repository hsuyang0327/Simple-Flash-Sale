package com.flashsale.backend.service;

import com.flashsale.backend.dto.response.JwtResponse;

public interface AuthService {

    JwtResponse login(String memberEmail, String memberPwd);

    JwtResponse refresh(String refreshToken);
}
