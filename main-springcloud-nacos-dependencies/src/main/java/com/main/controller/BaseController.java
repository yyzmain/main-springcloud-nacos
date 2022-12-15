package com.main.controller;

import com.main.result.MainResult;
import com.main.result.MainResultCode;
import com.main.result.MainResultGenerator;
import org.apache.commons.lang.StringUtils;
import org.springframework.web.bind.annotation.RequestMapping;

/**
 * <p>
 * 基类控制器，用来快速设置返回类型
 */
public class BaseController {

    /**
     * 根据类的@RequestMapping跟函数的@RequestMapping来映射路径，返回指定的页面
     */
    protected String render(String name) {
        if (StringUtils.isBlank(name)) {
            return null;
        }
        RequestMapping rm = getClass().getAnnotation(RequestMapping.class);
        if (rm != null) {
            return (new StringBuilder(String.valueOf(rm.value()[0]))).append("/").append(name).toString();
        } else {
            return name;
        }
    }


    protected <T> MainResult<T> restResult(Integer code, String msg, T data) {
        return MainResultGenerator.createResult(code, msg, data);
    }

    /**
     * 返回rest固定格式 成功（请求操作成功）
     */
    protected <T> MainResult<T> restOkResult(String msg, T data) {
        return restResult(MainResultCode.OK.getCode(), msg, data);
    }

    protected <T> MainResult<T> restOkResult(T data) {
        return restOkResult(MainResultCode.OK.getMsg(), data);
    }

    protected <T> MainResult<T> restOkResult() {
        return restOkResult(null);
    }

    /**
     * 返回rest固定格式 失败（请求操作失败）
     */
    protected <T> MainResult<T> restFailResult(String msg, T data) {
        return restResult(MainResultCode.FAIL.getCode(), msg, data);
    }

    protected <T> MainResult<T> restFailResult(T data) {
        return restOkResult(MainResultCode.FAIL.getMsg(), data);
    }

    protected <T> MainResult<T> restFailResult() {
        return restOkResult(null);
    }

    /**
     * 重定向到指定路径下
     */
    protected String redirect(String url) {
        if (StringUtils.isBlank(url)) {
            return null;

        } else {
            return (new StringBuilder("redirect:")).append(url).toString();
        }
    }


}
