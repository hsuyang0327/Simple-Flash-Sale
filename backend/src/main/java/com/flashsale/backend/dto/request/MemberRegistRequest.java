package com.flashsale.backend.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/**
 * @description MemberRegistRequest
 * @author Yang-Hsu
 * @date 2026/2/6 下午2:31
 */
@Data
public class MemberRegistRequest {

    @NotBlank(message = "EMAIL_EMPTY")
    @Email(message = "INVALID_EMAIL_FORMAT")
    private String memberEmail;

    @NotBlank(message = "PASSWORD_EMPTY")
    private String memberPwd;

    @NotBlank(message = "NAME_EMPTY")
    private String memberName;
}