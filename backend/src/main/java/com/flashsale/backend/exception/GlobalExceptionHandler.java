package com.flashsale.backend.exception;

import com.flashsale.backend.common.ApiResponse;
import com.flashsale.backend.common.ResultCode;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

/**
 * @author Yang-Hsu
 * @description intercept service share exception
 * @date 2026/1/9 下午 12:00
 */
@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(BusinessException.class)
    public ResponseEntity<ApiResponse<Void>> handleBusinessException(BusinessException e) {
        ResultCode rc = e.getResultCode();
        // We use warn level for business logic violations as they are usually not system failures
        log.warn("Business exception occurred: [Code: {}] Message: {}", rc.getCode(), e.getMessage());
        if (rc == ResultCode.ACCESS_TOKEN_EXPIRED ||
                rc == ResultCode.REFRESH_TOKEN_EXPIRED ||
                rc == ResultCode.TOKEN_INVALID ||
                rc == ResultCode.TOKEN_MISSING) {
            return ResponseEntity
                    .status(org.springframework.http.HttpStatus.UNAUTHORIZED)
                    .body(ApiResponse.of(rc));
        }
        return ResponseEntity.ok(ApiResponse.of(rc));
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiResponse<Void>> handleValidationException(MethodArgumentNotValidException e) {
        String errorTagName = e.getBindingResult().getAllErrors().get(0).getDefaultMessage();

        ResultCode resultCode;
        try {
            resultCode = ResultCode.valueOf(errorTagName);
        } catch (Exception ex) {
            resultCode = ResultCode.SYSTEM_ERROR;
        }
        log.warn("Validation failed: {} (Code: {})", resultCode.getMessage(), resultCode.getCode());
        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(ApiResponse.of(resultCode));
    }

    @ExceptionHandler(Exception.class)
    public ApiResponse<Void> handleException(Exception e) {
        // Use error level for unexpected system failures and include the stack trace
        log.error("Unexpected system error occurred: ", e);
        return ApiResponse.of(ResultCode.SYSTEM_ERROR);
    }
}
