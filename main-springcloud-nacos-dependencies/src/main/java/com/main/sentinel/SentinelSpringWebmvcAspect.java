package com.main.sentinel;

import com.alibaba.csp.sentinel.Entry;
import com.alibaba.csp.sentinel.EntryType;
import com.alibaba.csp.sentinel.SphU;
import com.alibaba.csp.sentinel.adapter.spring.webmvc.callback.BlockExceptionHandler;
import com.alibaba.csp.sentinel.adapter.spring.webmvc.callback.DefaultBlockExceptionHandler;
import com.alibaba.csp.sentinel.annotation.aspectj.AbstractSentinelAspectSupport;
import com.alibaba.csp.sentinel.context.ContextUtil;
import com.alibaba.csp.sentinel.slots.block.BlockException;
import org.apache.commons.lang3.StringUtils;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import org.springframework.web.servlet.HandlerMapping;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * @Describtion
 * sentinel的param flow规则不支持根据url进行配置，因为在源码中，根据url进行规则处理时由springmvc拦截器进行的，该拦截器并没有对参数进行处理
 * 而param flow规则是通过对参数的顺序进行限流的，从理论上来说，在springmvc拦截器中进行处理正常也是不合理的，因为在拦截器处理时，参数是无序的，故而无法使用param flow规则
 * 解决方法：参考SentinelResourceAspect类，对所有基于注解RequestMapping（排除FeignClient）的类进行拦截，
 *   新增一个资源节点，节点名称组成：由常量前缀$param_flow+资源名称（获取sentinel mvc拦截器中处理的资源名称），在sentinel控制台上可以对该前缀的资源进行param flow规则配置
 */
@Configuration
@Aspect
public class SentinelSpringWebmvcAspect extends AbstractSentinelAspectSupport {
    public SentinelSpringWebmvcAspect(){
    }

    //controller注解切面，指向controller所有方法
    @Pointcut("@target(org.springframework.stereotype.Controller)")
    public void controllerPointcut(){}
    @Pointcut("@target(org.springframework.web.bind.annotation.RestController)")
    public void restControllerPointcut(){}
    //controller总切面
    @Pointcut("controllerPointcut()||restControllerPointcut()")
    public void controllerAllAnnotationPointcut(){
    }
    //requestMapping注解方法切面
    @Pointcut("@annotation(org.springframework.web.bind.annotation.RequestMapping)")
    public void annotationRequestMappingPointcut() {
    }
    @Pointcut("@annotation(org.springframework.web.bind.annotation.GetMapping)")
    public void annotationGetMappingPointcut() {
    }
    @Pointcut("@annotation(org.springframework.web.bind.annotation.PutMapping)")
    public void annotationPutMappingPointcut() {
    }
    @Pointcut("@annotation(org.springframework.web.bind.annotation.PostMapping)")
    public void annotationPostMappingPointcut() {
    }
    @Pointcut("@annotation(org.springframework.web.bind.annotation.DeleteMapping)")
    public void annotationDeleteMappingPointcut() {
    }
    @Pointcut("@annotation(org.springframework.web.bind.annotation.PatchMapping)")
    public void annotationPatchMappingPointcut() {
    }
    //请求方法总切面
    @Pointcut("annotationRequestMappingPointcut()||annotationGetMappingPointcut()||annotationPutMappingPointcut()||annotationPostMappingPointcut()||annotationDeleteMappingPointcut()||annotationPatchMappingPointcut()")
    public void requestMappingAnnotationPointcut(){
    }
    //sentinel注解切面
    @Pointcut("@annotation(com.alibaba.csp.sentinel.annotation.SentinelResource)")
    public void sentinelResourceAnnotationPointcut() {
    }

    //最终切面：该切面的含义为对所有被@Controller/@RestController注解的类中被@Requestmapping系列注解方法的且非被@SentinelResource注解的方法进行切面
    @Pointcut("controllerAllAnnotationPointcut() && requestMappingAnnotationPointcut() && !sentinelResourceAnnotationPointcut()")
    public void sentinelSpringWebMvcPointcut(){
    }

    protected BlockExceptionHandler blockExceptionHandler = new DefaultBlockExceptionHandler();

    @Around("sentinelSpringWebMvcPointcut()")
    public Object invokeResourceWithSentinel(ProceedingJoinPoint pjp) throws Throwable {
        String resourceName = getResourceName(pjp);
        if(StringUtils.isEmpty(resourceName)){
            return pjp.proceed();
        }
        EntryType entryType = EntryType.OUT;
        int resourceType = 0;
        Entry entry = null;
        try {
            entry = SphU.entry(resourceName, resourceType, entryType, pjp.getArgs());
            return pjp.proceed();
        } catch (BlockException blockException){
            try{
                ServletRequestAttributes requestAttributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
                HttpServletRequest request = requestAttributes.getRequest();
                HttpServletResponse response = requestAttributes.getResponse();
                this.blockExceptionHandler.handle(request,response,blockException);
                return null;
            }finally {
                ContextUtil.exit();
            }
        } catch (Throwable ex) {
            throw ex;
        } finally {
            if (entry != null) {
                entry.exit(1, pjp.getArgs());
            }
        }
    }

    protected String getResourceName(ProceedingJoinPoint pjp) throws Exception {
        ServletRequestAttributes requestAttributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        if(requestAttributes == null){
            return null;
        }
        String resourceName = (String) requestAttributes.getRequest().getAttribute(HandlerMapping.BEST_MATCHING_PATTERN_ATTRIBUTE);
        return "$param_flow"+resourceName;
    }

}
