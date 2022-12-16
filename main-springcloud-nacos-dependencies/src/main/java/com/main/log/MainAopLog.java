package com.main.log;

import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.Signature;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.springframework.context.annotation.Configuration;

/**
 * 使用AOP将执行方法以及耗时记录进日志
 * 一个方法中可设置多个切面
 */

@Aspect
@Configuration
@Slf4j
public class MainAopLog {

    /**
     * 申明切面
     */
    @Pointcut("execution(*  com.main..controller..*.*(..))")
    public void controllerPointcut() {
    }


    @Around("controllerPointcut()")
    public Object advice(ProceedingJoinPoint joinPoint) throws Throwable {
        final String fullName = getFullName(joinPoint);
        if (log.isDebugEnabled()) {
            log.debug("start request:{} ", fullName);
        }
        long start = System.currentTimeMillis();
        Object result = joinPoint.proceed();
        long end = System.currentTimeMillis();
        if (log.isDebugEnabled()) {
            log.debug("end request:{},time:{}ms", fullName, (end - start));
        }
        return result;
    }

    private String getFullName(ProceedingJoinPoint joinPoint) {
        try {
            final Signature signature = joinPoint.getSignature();
            String className = signature.getDeclaringTypeName();
            className = className.substring(className.lastIndexOf(".") + 1);
            final String methodName = signature.getName();
            return className + "." + methodName;
        } catch (Exception e) {
            log.error("mainAopLog run is error! msg:{}", e.getMessage());
            return "";
        }
    }

}