package com.main.feign;

import feign.RequestInterceptor;
import okhttp3.ConnectionPool;
import okhttp3.Interceptor;
import okhttp3.Request;
import org.apache.commons.lang3.StringUtils;
import org.springframework.boot.autoconfigure.AutoConfigureBefore;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cloud.client.ServiceInstance;
import org.springframework.cloud.client.discovery.DiscoveryClient;
import org.springframework.cloud.commons.httpclient.OkHttpClientConnectionPoolFactory;
import org.springframework.cloud.commons.httpclient.OkHttpClientFactory;
import org.springframework.cloud.openfeign.FeignClientProperties;
import org.springframework.cloud.openfeign.clientconfig.OkHttpFeignConfiguration;
import org.springframework.cloud.openfeign.support.FeignHttpClientProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpHeaders;

import javax.annotation.PreDestroy;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.concurrent.TimeUnit;

/*
 * 替代OkHttpFeignConfiguration来初始化okhttp客户端
 * 目的：基于okhttp拦截器(默认的客户端没有添加拦截器)技术，拦截请求，在请求发起前修改请求头，注入请求目标的Authorization请求头
 * */

@Configuration(
        proxyBeanMethods = false
)
@AutoConfigureBefore(OkHttpFeignConfiguration.class)
@ConditionalOnProperty({"feign.okhttp.enabled"})
public class OkHttpFeignConfig {

    public static final String TARGET_KEY = "$$CALL_TARGET";

    private okhttp3.OkHttpClient okHttpClient;

    protected OkHttpFeignConfig() {
    }

    @Bean
    @ConditionalOnMissingBean({ConnectionPool.class})
    public ConnectionPool httpClientConnectionPool(FeignHttpClientProperties httpClientProperties, OkHttpClientConnectionPoolFactory connectionPoolFactory) {
        Integer maxTotalConnections = httpClientProperties.getMaxConnections();
        Long timeToLive = httpClientProperties.getTimeToLive();
        TimeUnit ttlUnit = httpClientProperties.getTimeToLiveUnit();
        return connectionPoolFactory.create(maxTotalConnections, timeToLive, ttlUnit);
    }

    /**
     * OkHttpFeignLoadBalancedConfiguration会再包一层
     *
     * @param httpClientFactory
     * @param connectionPool
     * @param httpClientProperties
     * @param interceptor
     * @return
     */
    @Bean
    @ConditionalOnMissingBean({okhttp3.OkHttpClient.class})
    public okhttp3.OkHttpClient client(OkHttpClientFactory httpClientFactory, ConnectionPool connectionPool, FeignHttpClientProperties httpClientProperties, Interceptor interceptor) {
        Boolean followRedirects = httpClientProperties.isFollowRedirects();
        Integer connectTimeout = httpClientProperties.getConnectionTimeout();
        Boolean disableSslValidation = httpClientProperties.isDisableSslValidation();
        this.okHttpClient = httpClientFactory.createBuilder(disableSslValidation)
                .connectTimeout((long) connectTimeout, TimeUnit.MILLISECONDS)
                .followRedirects(followRedirects)
                .connectionPool(connectionPool)
                //添加拦截器
                .addInterceptor(interceptor)
                .build();
        return this.okHttpClient;
    }

    /*
     * feign传递target name（被调用服务的名称）
     * */
    @Bean
    public RequestInterceptor transmitTargetRequestInterceptor() {
        return requestTemplate -> {
            requestTemplate.header(TARGET_KEY, requestTemplate.feignTarget().name());
        };
    }


    /*
     * okhttp拦截器，添加被调用服务的访问账号密码，该账号密码动态注册到注册中心
     * */
    @Bean
    public Interceptor requestInterceptor(DiscoveryClient discoveryClient) {
        return (chain -> {
            Request request = chain.request();
            //根据服务名获取服务实例信息
            List<ServiceInstance> instances = discoveryClient.getInstances(request.header(TARGET_KEY));
            //过滤出符合的服务
            ServiceInstance targetService = instances.stream().filter(instance -> {
                if (instance.getHost().equals(request.url().host()) && instance.getPort() == request.url().port()) {
                    return true;
                }
                return false;
            }).findFirst().orElse(null);
            Request.Builder requestBuilder = request.newBuilder().removeHeader(TARGET_KEY);
            if (targetService != null) {
                //获取用户名和密码
                String username = targetService.getMetadata().get("username");
                String password = targetService.getMetadata().get("password");
                //设置服务实例的访问认证头信息
                if (StringUtils.isNoneEmpty(username, password)) {
                    String base64Credentials = getBase64Credentials(username, password);
                    requestBuilder = requestBuilder.removeHeader(HttpHeaders.AUTHORIZATION)
                            .addHeader(HttpHeaders.AUTHORIZATION, base64Credentials);
                }
            }
            return chain.proceed(requestBuilder.build());
        });
    }

    private String getBase64Credentials(String username, String password) {
        String plainCreds = username + ":" + password;
        byte[] plainCredsBytes = plainCreds.getBytes(StandardCharsets.UTF_8);
        byte[] base64CredsBytes = org.apache.commons.codec.binary.Base64.encodeBase64(plainCredsBytes);
        return "Basic " + new String(base64CredsBytes, StandardCharsets.UTF_8);
    }

    @PreDestroy
    public void destroy() {
        if (this.okHttpClient != null) {
            this.okHttpClient.dispatcher().executorService().shutdown();
            this.okHttpClient.connectionPool().evictAll();
        }

    }
}
