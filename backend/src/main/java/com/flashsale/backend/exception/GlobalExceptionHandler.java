package com.flashsale.backend.exception;

import com.flashsale.backend.common.ApiResponse;
import com.flashsale.backend.common.ResultCode;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

/**
 * @description  intercept service share exception
 * @author Yang-Hsu
 * @date 2026/1/9 下午 12:00
 */
@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(BusinessException.class)
    public ApiResponse<Void> handleBusinessException(BusinessException e) {
        // We use warn level for business logic violations as they are usually not system failures
        log.warn("Business exception occurred: [Code: {}] Message: {}",
                e.getResultCode().getCode(), e.getMessage());

        return ApiResponse.of(e.getResultCode());
    }

    @ExceptionHandler(Exception.class)
    public ApiResponse<Void> handleException(Exception e) {
        // Use error level for unexpected system failures and include the stack trace
        log.error("Unexpected system error occurred: ", e);

        return ApiResponse.of(ResultCode.SYSTEM_ERROR);
    }
}
