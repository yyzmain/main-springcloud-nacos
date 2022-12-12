package com.main.swagger;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * swagger-ui的api信息
 *
 * @author song
 * @date 2018/5/7
 */
@ConfigurationProperties(prefix = "swagger.api-info")
@Data
public class SwaggerProperties {

    private String title = "MAIN RESTful APIs";
    private String description = "MAIN 微服务API文档";
    private String version = "2.0";

    private String basePackage = "com";

}
