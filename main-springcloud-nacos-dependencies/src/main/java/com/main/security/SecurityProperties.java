package com.main.security;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * springsecurity 的账号密码属性文档
 *
 * @author song
 * @date 2018/5/3
 */
@Data
@ConfigurationProperties(prefix = "spring.security.user")
public class SecurityProperties {
    private String name = "main";
    private String password = "main";
}
