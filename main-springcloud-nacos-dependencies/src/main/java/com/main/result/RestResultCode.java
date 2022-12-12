package com.main.result;

/**
 * 统一返回信息编码枚举
 *
 * @author song
 * @date 2018/5/7
 */
public enum RestResultCode {

    OK(0, "操作成功"), FAIL(1, "操作失败");

    private Integer code;
    private String msg;

    private RestResultCode(Integer code, String msg) {
        this.code = code;
        this.msg = msg;
    }

    public Integer getCode() {
        return code;
    }

    public String getMsg() {
        return msg;
    }
}
