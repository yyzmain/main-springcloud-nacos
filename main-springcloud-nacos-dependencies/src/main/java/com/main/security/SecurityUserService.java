package com.main.security;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.security.core.authority.AuthorityUtils;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Component;

@Component
@Slf4j
@EnableConfigurationProperties(SecurityProperties.class)
public class SecurityUserService implements UserDetailsService {

    @Autowired
    private SecurityProperties securityProperties;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        if (log.isDebugEnabled()) {
            log.debug("HTTP Basic Security:{\"username\":\"{}\",\"user\":\"{}\"}"
                    , username
                    , securityProperties.getName());
        }
        if (StringUtils.equals(username, securityProperties.getName())) {
            return new User(username, "{noop}"+securityProperties.getPassword(), AuthorityUtils.commaSeparatedStringToAuthorityList("user"));
        }
        return null;
    }
}
