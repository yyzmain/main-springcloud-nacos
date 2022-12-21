package com.main.exception;

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

    private static final String DEFAULT_ALERT = "服务器内部错误,请联系管理员.";

    public MainConcealRuntimeException() {
        super(DEFAULT_ALERT);
        log.error("==> {}", DEFAULT_ALERT);
    }

}
