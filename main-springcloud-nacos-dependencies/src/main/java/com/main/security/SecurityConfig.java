package com.main.security;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.actuate.autoconfigure.security.servlet.EndpointRequest;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.authority.AuthorityUtils;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;


@Configuration
@EnableWebSecurity
@Slf4j
@EnableConfigurationProperties(SecurityProperties.class)
public class SecurityConfig extends WebSecurityConfigurerAdapter {

    @Autowired
    private UserDetailsService userDetailsService;

    //是否启用身份认证
    @Value("${spring.security.basic:true}")
    private boolean enabled;

    @Value("${spring.security.csrf:true}")
    private boolean csrf;

    @Value("${spring.security.cors:false}")
    private boolean cors;

    @Override
    protected void configure(HttpSecurity http) throws Exception {
        if (log.isDebugEnabled()) {
            log.debug("配置security,csrf:{},cors:{},basic:{}", csrf, cors, enabled);
        }
        //spring security 不主动创建session
        http.sessionManagement().sessionCreationPolicy(SessionCreationPolicy.NEVER);
        //允许在iframe中加载同源页面
        http.headers().frameOptions().sameOrigin();
        if (!csrf) { //跨站请求伪造防护是否关闭
            http.csrf().disable();
        }
        if (!cors) { //不允许跨域资源请求，即非本服务访问本服务资源将被拒绝
            http.cors().disable();
        }
        if (enabled) {
            http.authorizeRequests()//配置权限
                    .anyRequest()//所有请求
                    .authenticated()//需要登录后才能访问
                    .and()
                    .httpBasic()
                    .and()
                    .formLogin().permitAll();//登录页面无需授权
        } else {
            http.authorizeRequests().anyRequest().permitAll();//全部请求无需授权
        }

    }

    @Override
    protected void configure(AuthenticationManagerBuilder auth) throws Exception {
        auth.userDetailsService(userDetailsService);
    }


    @Bean
    public UserDetailsService userDetailsService(SecurityProperties securityProperties) {
        return (username -> {
            if (log.isDebugEnabled()) {
                log.debug("HTTP Basic Security:{\"username\":\"{}\",\"user\":\"{}\"}"
                        , username
                        , securityProperties.getName());
            }
            if (StringUtils.equals(username, securityProperties.getName())) {
                return new User(username, "{noop}" + securityProperties.getPassword(), AuthorityUtils.commaSeparatedStringToAuthorityList("user"));
            }
            return null;
        });
    }




}