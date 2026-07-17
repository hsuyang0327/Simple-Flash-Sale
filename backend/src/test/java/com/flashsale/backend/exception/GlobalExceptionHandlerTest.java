package com.flashsale.backend.exception;

import com.flashsale.backend.common.ApiResponse;
import com.flashsale.backend.common.ResultCode;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.validation.ObjectError;
import org.springframework.web.bind.MethodArgumentNotValidException;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * @description GlobalExceptionHandlerTest(By using pure unit test, no Spring context)
 * @author Yang-Hsu
 * @date 2026/7/17
 */
class GlobalExceptionHandlerTest {

    private final GlobalExceptionHandler handler = new GlobalExceptionHandler();

    @Test
    void handleBusinessException_nonTokenError_returns200WithMappedCode() {
        BusinessException e = new BusinessException(ResultCode.PRODUCT_NOT_FOUND);

        ResponseEntity<ApiResponse<Void>> result = handler.handleBusinessException(e);

        assertEquals(HttpStatus.OK, result.getStatusCode());
        assertEquals(ResultCode.PRODUCT_NOT_FOUND.getCode(), result.getBody().getCode());
        assertEquals(ResultCode.PRODUCT_NOT_FOUND.getMessage(), result.getBody().getMessage());
    }

    @ParameterizedTest
    @EnumSource(value = ResultCode.class, names = {
            "ACCESS_TOKEN_EXPIRED", "REFRESH_TOKEN_EXPIRED", "TOKEN_INVALID", "TOKEN_MISSING"
    })
    void handleBusinessException_tokenRelatedError_returns401(ResultCode resultCode) {
        BusinessException e = new BusinessException(resultCode);

        ResponseEntity<ApiResponse<Void>> result = handler.handleBusinessException(e);

        assertEquals(HttpStatus.UNAUTHORIZED, result.getStatusCode());
        assertEquals(resultCode.getCode(), result.getBody().getCode());
    }

    @Test
    void handleValidationException_defaultMessageMatchesEnumName_mapsToResultCode() {
        MethodArgumentNotValidException e = mockValidationException("PRODUCT_NAME_EMPTY");

        ResponseEntity<ApiResponse<Void>> result = handler.handleValidationException(e);

        assertEquals(HttpStatus.BAD_REQUEST, result.getStatusCode());
        assertEquals(ResultCode.PRODUCT_NAME_EMPTY.getCode(), result.getBody().getCode());
    }

    @Test
    void handleValidationException_defaultMessageNotAnEnumName_fallsBackToSystemError() {
        MethodArgumentNotValidException e = mockValidationException("NOT_A_REAL_RESULT_CODE");

        ResponseEntity<ApiResponse<Void>> result = handler.handleValidationException(e);

        assertEquals(HttpStatus.BAD_REQUEST, result.getStatusCode());
        assertEquals(ResultCode.SYSTEM_ERROR.getCode(), result.getBody().getCode());
    }

    @Test
    void handleException_unexpectedException_returnsSystemError() {
        ApiResponse<Void> result = handler.handleException(new RuntimeException("boom"));

        assertEquals(ResultCode.SYSTEM_ERROR.getCode(), result.getCode());
        assertEquals(ResultCode.SYSTEM_ERROR.getMessage(), result.getMessage());
    }

    private MethodArgumentNotValidException mockValidationException(String defaultMessage) {
        BindingResult bindingResult = mock(BindingResult.class);
        ObjectError error = mock(ObjectError.class);
        when(error.getDefaultMessage()).thenReturn(defaultMessage);
        when(bindingResult.getAllErrors()).thenReturn(List.of(error));

        MethodArgumentNotValidException e = mock(MethodArgumentNotValidException.class);
        when(e.getBindingResult()).thenReturn(bindingResult);
        return e;
    }
}
