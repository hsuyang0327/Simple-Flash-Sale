package com.flashsale.backend.common;

import lombok.Getter;

/**
 * @description Code for Api Response
 * @author Yang-Hsu
 * @date 2026/1/9 上午 09:50
 */
@Getter
public enum ResultCode {
    SUCCESS(200, "success"),

    // JWT
    TOKEN_INVALID(4001, "invalid_token"),
    ACCESS_TOKEN_EXPIRED(4002, "access_token_expired"),
    REFRESH_TOKEN_EXPIRED(4003, "refresh_token_expired"),
    TOKEN_MISSING(4004, "missing_token"),

    //Member
    MEMBER_NOT_FOUND(4101, "member_not_found"),
    MEMBER_ALREADY_EXISTS(4102, "member_already_exists"),
    LOGIN_FAILED(4103, "login_failed_invalid_credentials"),
    MEMBER_STATUS_LOCKED(4104, "member_account_locked"),

    // Validation
    EMAIL_EMPTY(4201, "email_is_required"),
    INVALID_EMAIL(4202, "invalid_email_format"),
    PASSWORD_EMPTY(4203, "password_is_required"),
    NAME_EMPTY(4204, "name_is_required"),

    // System

    SYSTEM_ERROR(5000, "server_error");

    private final int code;
    private final String message;

    ResultCode(int code, String message) {
        this.code = code;
        this.message = message;
    }
}
