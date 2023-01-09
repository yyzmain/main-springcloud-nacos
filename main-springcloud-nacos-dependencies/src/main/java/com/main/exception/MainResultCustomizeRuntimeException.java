package com.main.exception;

import com.main.result.ResultCode;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

/**
 * 自定义异常
 */
@Slf4j
@Getter
@Setter
public class MainResultCustomizeRuntimeException extends RuntimeException {

    private ResultCode resultCode;

    public MainResultCustomizeRuntimeException(ResultCode resultCode) {
        super(resultCode.getMsg());
        this.resultCode = resultCode;
        log.error("==> resultCode:  {}", resultCode.getMsg());
    }

}
