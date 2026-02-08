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
    MEMBER_IS_UPDATED_BY_OTHERS(4105, "member_is_updated_by_others"),

    // Product
    PRODUCT_NOT_FOUND(4201, "product_not_found"),

    //Order
    STOCK_NOT_ENOUGH(4301,"stock_not_enough"),

    // Validation
    EMAIL_EMPTY(4401, "email_is_required"),
    INVALID_EMAIL(4402, "invalid_email_format"),
    PASSWORD_EMPTY(4403, "password_is_required"),
    NAME_EMPTY(4404, "name_is_required"), //need to modify
    PRODUCT_NAME_EMPTY(4405, "product_name_is_required"),
    PRICE_EMPTY(4406, "price_is_required"),
    PRICE_INVALID(4407, "price_invalid"),
    STOCK_EMPTY(4408, "stock_is_required"),
    STOCK_INVALID(4409, "stock_invalid"),
    STATUS_INVALID(4410, "status_invalid"),
    START_TIME_EMPTY(4411, "start_time_is_required"),
    END_TIME_EMPTY(4412, "end_time_is_required"),
    END_TIME_INVALID(4413, "end_time_must_be_after_start_time"),
    DESCRIPTION_TOO_LONG(4414,"description_too_long"),
    MEMBER_ID_EMPTY(4415,"member_id_is_required"),
    PRODUCT_ID_EMPTY(4416,"product_id_is_required"),
    QUANTITY_EMPTY(4417,"quantity_is_required"),
    STATUS_EMPTY(4418,"status_is_required"),
    TOTAL_PRICE_EMPTY(4419,"total_price_is_required"),

    // System
    SYSTEM_ERROR(5000, "server_error");

    private final int code;
    private final String message;

    ResultCode(int code, String message) {
        this.code = code;
        this.message = message;
    }
}
