package com.main.feign;

import com.main.security.SecurityProperties;
import feign.RequestInterceptor;
import feign.auth.BasicAuthRequestInterceptor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cloud.client.ServiceInstance;
import org.springframework.cloud.client.discovery.DiscoveryClient;
import org.springframework.cloud.openfeign.loadbalancer.FeignLoadBalancerAutoConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.servlet.http.HttpServletRequest;
import java.util.Enumeration;

/**
 * 把类注册到spring ioc容器中
 * (class中不需要@Component，这样如果这个configuration被exclude，那么这个class也不会被初始化)
 * 并且可以把class注入到@Bean注解的方法参数中
 */
@Configuration
@EnableConfigurationProperties(SecurityProperties.class)
@Slf4j
public class FeignConfig {



    /**
     * 配置feign调用服务的security账号密码(静态)
     *
     */
    @Bean
    public BasicAuthRequestInterceptor basicAuthRequestInterceptor(SecurityProperties feignSecurityProperties) {
        return new BasicAuthRequestInterceptor(feignSecurityProperties.getName(), feignSecurityProperties.getPassword());
    }

    /**
     * 配置feign调用时传递收到的请求的头部内容
     */
    @Bean
    public RequestInterceptor transmitHeaderRequestInterceptor() {
        return template -> {
            //获取前一个发来的用户信息，传递给下一个
            ServletRequestAttributes requestAttributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
            if (requestAttributes != null) {
                HttpServletRequest request = requestAttributes.getRequest();
                if (log.isDebugEnabled()) {
                    log.debug("feign transmitHeaderRequestInterceptor:{\"source\":\"{}\",\"target\":\"{}\"}"
                            , request.getRequestURI()
                            , template.url());
                }
                Enumeration<String> headers = request.getHeaderNames();
                while (headers.hasMoreElements()) {
                    String key = headers.nextElement();
                    String value = request.getHeader(key);
                    //Content-Type不能传递下去，否则直接抛出异常：Incomplete output stream
                    if (!StringUtils.equalsIgnoreCase("Content-Length", key)
                            && !StringUtils.equalsIgnoreCase("Content-Type", key)
                            && !StringUtils.equalsIgnoreCase("accept", key)
                            && !StringUtils.equalsIgnoreCase("Authorization",key)) {
                        log.debug("header key = {},value = {}",key,value);
                        template.header(key, value);
                    }
                }
                return;
            }
            if (log.isWarnEnabled()) {
                log.warn("无header传递");
            }
        };
    }

    @Bean
    @ConditionalOnProperty({"feign.hystrix.enabled"})
    public FeignHystrixConcurrencyStrategy feignHystrixConcurrencyStrategy() {
        return new FeignHystrixConcurrencyStrategy();
    }
}