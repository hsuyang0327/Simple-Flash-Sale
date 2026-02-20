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
    PRODUCT_IS_UPDATED_BY_OTHERS(4202, "product_is_updated_by_others"),
    PRODUCT_NO_EVENT(4203, "product_no_event"),
    PRODUCT_EVENT_DUPLICATED(4204, "product_no_active_event"),

    // Order
    ORDER_NOT_FOUND(4301, "order_not_found"),
    ORDER_IS_UPDATED_BY_OTHERS(4302, "order_is_updated_by_others"),
    ORDER_STATUS_INVALID(4303, "order_status_invalid"),

    // Event
    EVENT_NOT_FOUND(4401, "event_not_found"),
    EVENT_EXPIRED(4402, "event_expired"),

    // Job
    JOB_NOT_FOUND(4501, "job_not_found"),
    JOB_ACTION_FAILED(4502, "job_action_failed"),
    JOB_NAME_EMPTY(4503, "job_name_is_required"),
    JOB_GROUP_EMPTY(4504, "job_group_is_required"),
    CRON_EXPRESSION_EMPTY(4505, "cron_expression_is_required"),

    // Validation
    EMAIL_EMPTY(4601, "email_is_required"),
    INVALID_EMAIL(4602, "invalid_email_format"),
    PASSWORD_EMPTY(4603, "password_is_required"),
    NAME_EMPTY(4604, "name_is_required"), //need to modify
    PRODUCT_NAME_EMPTY(4605, "product_name_is_required"),
    PRICE_EMPTY(4606, "price_is_required"),
    PRICE_INVALID(4607, "price_invalid"),
    STOCK_EMPTY(4608, "stock_is_required"),
    STOCK_INVALID(4609, "stock_invalid"),
    STATUS_INVALID(4610, "status_invalid"),
    START_TIME_EMPTY(4611, "start_time_is_required"),
    END_TIME_EMPTY(4612, "end_time_is_required"),
    END_TIME_INVALID(4613, "end_time_must_be_after_start_time"),
    PRODUCT_ID_EMPTY(4614, "product_id_is_required"),
    QUANTITY_EMPTY(4615, "quantity_is_required"),
    QUANTITY_INVALID(4616, "quantity_invalid"),
    EVENT_ID_EMPTY(4617, "event_id_is_required"),
    ORDER_ID_EMPTY(4618, "order_id_is_required"),
    MEMBER_ID_EMPTY(4619, "member_id_is_required"),

    // System
    SYSTEM_ERROR(5000, "server_error");

    private final int code;
    private final String message;

    ResultCode(int code, String message) {
        this.code = code;
        this.message = message;
    }
}
