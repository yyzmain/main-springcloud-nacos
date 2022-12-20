package com.main.utils;

import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public class MainCookieUtil {

    /*根据cookie的name获取value*/
    public static String getByCName(HttpServletRequest request, String cookieName) {
        String cookieVal = "";
        if (request.getCookies() != null) {
            for (Cookie cookie : request.getCookies()) {
                if (cookie.getName().equals(cookieName)) {
                    cookieVal = cookie.getValue();
                    break;
                }
            }
        }
        return cookieVal;
    }

    /*根据cookieName清除cookie中的值*/
    public static void cleanByCName(HttpServletRequest request, String cookieName) {
        if (request.getCookies() != null) {
            for (Cookie cookie : request.getCookies()) {
                if (cookie.getName().equals(cookieName)) {
                    Cookie cookieNew = new Cookie(cookieName, null);
                    cookieNew.setPath("/");
                    cookieNew.setMaxAge(0); //清空cookie
                    getResponse().addCookie(cookieNew);
                    break;
                }
            }
        }
    }

    public static void addCookie(String name, String value, String path) {
        Cookie cookie = new Cookie(name, value);
        cookie.setPath(path);
        cookie.setHttpOnly(true);
        cookie.setMaxAge(-1);
        getResponse().addCookie(cookie);
    }

    public static void addCookie(String name, String value) {
        addCookie(name, value, "/");
    }

    public static void addCookie(HttpServletResponse resp, String name, String value, String path) {
        Cookie cookie = new Cookie(name, value);
        cookie.setPath(path);
        cookie.setHttpOnly(true);
        cookie.setMaxAge(-1);
        resp.addCookie(cookie);
    }

    public static HttpServletResponse getResponse() {
        ServletRequestAttributes requestAttributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        return requestAttributes.getResponse();
    }

    public static HttpServletRequest getRequest() {
        ServletRequestAttributes requestAttributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        return requestAttributes.getRequest();
    }

}
