package com.main.result;

/**
 * 通用返回类
 *
 * @author song
 * @date 2017/8/3
 */
public class RestResult<T> {

    private Integer code;
    private String msg;
    private T data;

    private RestResult() {
    }

    public static <T> RestResult<T> newInstance() {
        return new RestResult<T>();
    }

    public Integer getCode() {
        return code;
    }

    public void setCode(Integer code) {
        this.code = code;
    }

    public String getMsg() {
        return msg;
    }

    public void setMsg(String msg) {
        this.msg = msg;
    }

    public T getData() {
        return data;
    }

    public void setData(T data) {
        this.data = data;
    }

    @Override
    public String toString() {
        return "{" +
                "code:" + code +
                ", msg:'" + msg + '\'' +
                ", data:" + data +
                '}';
    }
}
