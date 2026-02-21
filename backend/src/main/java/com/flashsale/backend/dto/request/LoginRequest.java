package com.flashsale.backend.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/**
 * @description LoginRequest
 * @author Yang-Hsu
 * @date 2026/2/21 下午4:29
 */
@Data
public class LoginRequest {
    @NotBlank(message = "EMAIL_EMPTY")
    @Email(message = "INVALID_EMAIL")
    private String email;

    @NotBlank(message = "PASSWORD_EMPTY")
    private String password;
}