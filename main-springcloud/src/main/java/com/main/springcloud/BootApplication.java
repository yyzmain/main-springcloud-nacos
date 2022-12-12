package com.main.springcloud;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.scheduling.annotation.EnableAsync;

@SpringBootApplication(scanBasePackages = "com.main")
@EnableFeignClients
@EnableDiscoveryClient
/*@EnableCircuitBreaker*/
@RefreshScope
@EnableAsync
public class BootApplication {

    public static void main(String[] args) {
        //设置log4j2日志异步
        System.setProperty("Log4jContextSelector", "org.apache.logging.log4j.core.async.AsyncLoggerContextSelector");
        SpringApplication.run(BootApplication.class, args);
    }
}
