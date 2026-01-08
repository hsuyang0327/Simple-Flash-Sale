package com.flashsale.backend.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @description JwtResponse
 * @author Yang-Hsu
 * @date 2026/1/12 上午 10:40
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class JwtResponse {
    private String accessToken;
    private String refreshToken;
}