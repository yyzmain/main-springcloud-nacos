package com.main.result;

public enum MainResultCode {

    OK(0, "操作成功"), FAIL(1, "操作失败");

    private Integer code;
    private String msg;

    MainResultCode(Integer code, String msg) {
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
