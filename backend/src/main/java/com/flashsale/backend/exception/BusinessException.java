package com.flashsale.backend.exception;

import lombok.Getter;
import com.flashsale.backend.common.ResultCode;


/**
 * @description service customized exception
 * @author Yang-Hsu
 * @date 2026/1/9
 */
@Getter
public class BusinessException extends RuntimeException {
    private final ResultCode resultCode;

    /**
     * @description Construct business exception with a specific result code
     * @author Yang-Hsu
     * @date 2026/7/9
     */
    public BusinessException(ResultCode resultCode) {
        super(resultCode.getMessage());
        this.resultCode = resultCode;
    }
}
