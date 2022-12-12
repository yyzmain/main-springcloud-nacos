package com.main.result;

/**
 * 返回类构造器
 *
 * @author song
 * @date 2017/8/4
 */
public class RestResultGenerator {


    public static <T> RestResult<T> createResult(Integer code, String msg, T data) {

        RestResult<T> result = RestResult.newInstance();
        result.setCode(code);
        result.setMsg(msg);
        result.setData(data);
        return result;
    }

    public static <T> RestResult<T> createOkResult() {
        return createResult(RestResultCode.OK.getCode(), RestResultCode.OK.getMsg(), null);
    }

    /**
     * 成功，默认提示
     */
    public static <T> RestResult<T> createOkResult(T data) {
        return createResult(RestResultCode.OK.getCode(), RestResultCode.OK.getMsg(), data);
    }


    /**
     * 成功，自定义提示
     */
    public static <T> RestResult<T> createOkResult(String msg, T data) {
        return createResult(RestResultCode.OK.getCode(), msg, data);
    }

    /**
     * 失败，默认提示
     */
    public static <T> RestResult<T> createFailResult() {
        return createResult(RestResultCode.FAIL.getCode(), RestResultCode.FAIL.getMsg(), null);
    }

    /**
     * 失败，自定义提示
     */
    public static <T> RestResult<T> createFailResult(String msg) {
        return createResult(RestResultCode.FAIL.getCode(), msg, null);
    }

    /**
     * 失败，自定义提示 & 返回失败详情
     */
    public static <T> RestResult<T> createFailResult(String msg, T data) {
        return createResult(RestResultCode.FAIL.getCode(), msg, data);
    }

}
