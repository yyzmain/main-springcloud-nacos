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
public class MainConcealRuntimeException extends RuntimeException {

    public MainConcealRuntimeException() {
        super(ResultCode.ERROR.getMsg());
        log.error("==> {}", ResultCode.ERROR.getMsg());
    }

}
