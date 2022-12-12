package com.main.controller;

import com.main.result.RestResult;
import com.main.result.RestResultCode;
import com.main.result.RestResultGenerator;
import org.apache.commons.lang.StringUtils;
import org.springframework.web.bind.annotation.RequestMapping;

/**
 * Created by song on 2017/6/25.
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


    protected <T> RestResult<T> restResult(Integer code, String msg, T data) {
        return RestResultGenerator.createResult(code, msg, data);
    }

    /**
     * 返回rest固定格式 成功（请求操作成功）
     */
    protected <T> RestResult<T> restOkResult(String msg, T data) {
        return restResult(RestResultCode.OK.getCode(), msg, data);
    }

    protected <T> RestResult<T> restOkResult(T data) {
        return restOkResult(RestResultCode.OK.getMsg(), data);
    }

    protected <T> RestResult<T> restOkResult() {
        return restOkResult(null);
    }

    /**
     * 返回rest固定格式 失败（请求操作失败）
     */
    protected <T> RestResult<T> restFailResult(String msg, T data) {
        return restResult(RestResultCode.FAIL.getCode(), msg, data);
    }

    protected <T> RestResult<T> restFailResult(T data) {
        return restOkResult(RestResultCode.FAIL.getMsg(), data);
    }

    protected <T> RestResult<T> restFailResult() {
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
