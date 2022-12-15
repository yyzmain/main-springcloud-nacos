package com.main.sentinel;

import com.alibaba.csp.sentinel.adapter.spring.webmvc.callback.RequestOriginParser;
import feign.RequestInterceptor;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * @description: 适配sentinel热点规则处理逻辑
 * sentinel客户端在进行热点规则处理时，通过origin字段识别调用者，而sentinel客户端本身在与springcloud结合时没有生成origin属性
 * 因此在服务接收到请求时，需要生成origin或者获取上一跳的origin，并且在调用下一跳服务时传递下去
 **/
@Configuration
public class SentinelConfig {

    public static final String ORIGIN_KEY = "$$CALL_ORIGIN";
    @Value("${spring.application.name}")
    public String applicationName;

    /*
     * feign传递origin,origin代表服务调用方
     * */
    @Bean
    public RequestInterceptor transmitOriginRequestInterceptor() {
        return requestTemplate -> {
            requestTemplate.header(ORIGIN_KEY, applicationName);
        };
    }

    /*
     * 生成origin
     * */
    @Bean
    public RequestOriginParser newOriginParser() {
        return (request -> {
            String origin = request.getHeader(ORIGIN_KEY);
            if (StringUtils.isEmpty(origin)) {
                origin = "unknown";
            }
            return origin;
        });
    }


}