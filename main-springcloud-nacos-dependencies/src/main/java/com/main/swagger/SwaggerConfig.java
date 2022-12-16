package com.main.swagger;


import com.main.utils.SysUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.web.ServerProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import springfox.documentation.PathProvider;
import springfox.documentation.builders.ApiInfoBuilder;
import springfox.documentation.builders.PathSelectors;
import springfox.documentation.builders.RequestHandlerSelectors;
import springfox.documentation.service.ApiInfo;
import springfox.documentation.service.ApiKey;
import springfox.documentation.service.AuthorizationScope;
import springfox.documentation.service.SecurityReference;
import springfox.documentation.spi.DocumentationType;
import springfox.documentation.spi.service.contexts.SecurityContext;
import springfox.documentation.spring.web.plugins.Docket;
import springfox.documentation.swagger2.annotations.EnableSwagger2;

import java.util.ArrayList;
import java.util.List;

/**
 * 使用Swagger展现Api文档
 * 在createRestApi设置Api中方法所在的包
 * 在apiInfo设置Api文档基本信息
 **/
@Configuration
@EnableSwagger2
@EnableConfigurationProperties(SwaggerProperties.class)
public class SwaggerConfig {

    @Autowired
    private ServerProperties serverProperties;

    @Autowired
    private SwaggerProperties swaggerProperties;

    @Bean
    public Docket createRestApi() {

        return new Docket(DocumentationType.SWAGGER_2)
                .apiInfo(apiInfo())
                .pathProvider(pathProvider())
                .select()
                .apis(RequestHandlerSelectors.basePackage(swaggerProperties.getBasePackage()))
                .paths(PathSelectors.any())
                .build()
                .securitySchemes(securitySchemes())
                .securityContexts(securityContexts());
    }

    private ApiInfo apiInfo() {
        return new ApiInfoBuilder()
                .title(swaggerProperties.getTitle())
                .description(swaggerProperties.getDescription())
                .version(swaggerProperties.getVersion())
                .build();
    }

    private PathProvider pathProvider() {
        return new PathProvider() {

            /**根路径：默认就是contextPath，但是在网关因为ui服务的页面上的连接问题而在网关加入了serviceIdFilter后，需要加入../来往上追溯一层*/
            @Override
            public String getApplicationBasePath() {
                return "/.." + serverProperties.getServlet().getContextPath();
            }

            /**具体服务的地址:controller里@RequestMapping对应的地址*/
            @Override
            public String getOperationPath(String operationPath) {
                return operationPath;
            }

            @Override
            public String getResourceListingPath(String s, String controllerName) {
                return null;
            }
        };
    }

    private List<ApiKey> securitySchemes() {
        //设置请求头信息
        List<ApiKey> result = new ArrayList<>();
        ApiKey apiKey = new ApiKey(SysUtil.SysConstants.ACCESS_TOKEN_KEY, SysUtil.SysConstants.ACCESS_TOKEN_KEY, "header");
        ApiKey userId = new ApiKey(SysUtil.SysConstants.USER_ID_KEY, SysUtil.SysConstants.USER_ID_KEY, "header");
        ApiKey groupId = new ApiKey(SysUtil.SysConstants.GROUP_ID_KEY, SysUtil.SysConstants.GROUP_ID_KEY, "header");
        result.add(apiKey);
        result.add(userId);
        result.add(groupId);
        return result;
    }

    private List<SecurityContext> securityContexts() {
        List<SecurityContext> securityContextList = new ArrayList<>();
        List<SecurityReference> securityReferenceList = new ArrayList<>();
        securityReferenceList.add(new SecurityReference(SysUtil.SysConstants.ACCESS_TOKEN_KEY, scopes()));
        securityReferenceList.add(new SecurityReference(SysUtil.SysConstants.USER_ID_KEY, scopes()));
        securityReferenceList.add(new SecurityReference(SysUtil.SysConstants.GROUP_ID_KEY, scopes()));
        securityContextList.add(SecurityContext
                .builder()
                .securityReferences(securityReferenceList)
                .forPaths(PathSelectors.any())
                .build()
        );
        return securityContextList;
    }

    private AuthorizationScope[] scopes() {
        return new AuthorizationScope[]{new AuthorizationScope("global", "accessAnything")};
    }
}


