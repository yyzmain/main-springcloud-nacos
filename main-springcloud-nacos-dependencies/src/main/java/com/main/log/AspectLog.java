package com.main.log;

import com.main.utils.SysUtil;
import org.aspectj.lang.annotation.Aspect;

import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.*;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.servlet.http.HttpServletRequest;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * 使用AOP将执行方法以及耗时记录进日志
 * 一个方法中可设置多个切面
 *
 * @author linds
 */

@Aspect
@Configuration
@Slf4j
public class AspectLog {

    /**
     * 申明切面
     */
    @Pointcut("execution(*  com.main..controller..*.*(..))")
    public void controllerPointcut() {
    }

    @Pointcut("execution(*  com.main..service..*.*(..))")
    public void servicePointcut() {
    }

    @Pointcut("execution(*  com.mian.mapper..*.*(..))")
    public void repositoryPointcut() {
    }

    /**
     * 合并多个切面
     */
    @Pointcut("controllerPointcut()||servicePointcut()||repositoryPointcut()")
    public void methodPointcut() {
    }

    /**
     * 打印每个方法的耗时
     */
    @Around("methodPointcut()")
    public Object doAround(ProceedingJoinPoint joinPoint) throws Throwable {
        long start = System.currentTimeMillis();
        Object result = joinPoint.proceed();
        long end = System.currentTimeMillis();

        if (log.isDebugEnabled()) {
            log.debug("方法执行耗时：{\"method\":\"{}.{}\",\"executionTime\":\"{}\"}",
                    joinPoint.getSignature().getDeclaringTypeName(),
                    joinPoint.getSignature().getName(),
                    (end - start));
        }

        return result;
    }


    /**
     * 拦截http request
     */
    @Before("controllerPointcut()")
    public void httpBefore(JoinPoint joinPoint) {

        if (log.isDebugEnabled()) {
            log.debug("Controller开始:{\"clientIp\":\"{}\",\"userId\":\"{}\",\"groupId\":\"{}\",\"datetime\":\"{}\",\"method\":\"{}.{}\",\"args\":{}}",
                    SysUtil.getClientIp(),
                    SysUtil.getUserId(),
                    SysUtil.getGroupId(),
                    LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy/MM/dd HH:mm:ss.SSS")),
                    joinPoint.getSignature().getDeclaringTypeName(),
                    joinPoint.getSignature().getName(),
                    joinPoint.getArgs());
        }

    }


    /**
     * 拦截http response
     */
    @AfterReturning(returning = "object", pointcut = "controllerPointcut()")
    public void doAfterReturning(JoinPoint joinPoint,Object object) {

        if (log.isDebugEnabled()) {
            log.debug("Controller结束:{\"clientIp\":\"{}\",\"userId\":\"{}\",\"groupId\":\"{}\",\"datetime\":\"{}\",\"method\":\"{}.{}\",\"result\":{}}",
                    SysUtil.getClientIp(),
                    SysUtil.getUserId(),
                    SysUtil.getGroupId(),
                    LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy/MM/dd HH:mm:ss.SSS")),
                    joinPoint.getSignature().getDeclaringTypeName(),
                    joinPoint.getSignature().getName(),
                    object);
        }
    }


}