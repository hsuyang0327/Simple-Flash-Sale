package com.flashsale.backend.common;

import lombok.Data;

/**
 * @description Unified specifications for Response
 * @author Yang-Hsu
 * @date 2026/1/9
 */
@Data
public class ApiResponse<T> {
    private int code;         
    private String message;   
    private T data;           
    private long timestamp;

    /**
     * @description Construct API response with result code and data payload
     * @author Yang-Hsu
     * @date 2026/7/9
     */
    public ApiResponse(ResultCode resultCode, T data) {
        this.code = resultCode.getCode();
        this.message = resultCode.getMessage();
        this.data = data;
        this.timestamp = System.currentTimeMillis();
    }

    /**
     * @description Construct API response with result code and no data
     * @author Yang-Hsu
     * @date 2026/7/9
     */
    public static <T> ApiResponse<T> of(ResultCode resultCode) {
        return new ApiResponse<>(resultCode, null);
    }
}
