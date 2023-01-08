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
public class MainCodeCustomizeRuntimeException extends RuntimeException {

    private Integer code;

    public MainCodeCustomizeRuntimeException(Integer code, String message) {
        super(message);
        this.code = code;
        log.error("==> {}", message);
    }

}
