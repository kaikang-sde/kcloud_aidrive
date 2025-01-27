package com.kang.kcloud_aidrive.config;

import com.kang.kcloud_aidrive.interceptor.LoginInterceptor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;


/**
 * Configure interceptor whitelist path development settings
 * Author: Kai Kang
 */
@Slf4j
@Component
public class InterceptorConfig implements WebMvcConfigurer {
    private final LoginInterceptor loginInterceptor;

    public InterceptorConfig(LoginInterceptor loginInterceptor) {
        this.loginInterceptor = loginInterceptor;
    }

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(loginInterceptor)
                // Add intercepted paths
                .addPathPatterns("/api/accounts/*/**", "/api/files/*/**", "/api/share/*/**")

                //Exclude from interception
                .excludePathPatterns("/api/accounts/*/registration", "/api/accounts/*/login", "/api/accounts/*/avatar",
                        "/api/share/*/check_share_code", "/api/share/*/visit", "/api/share/*/detail_no_code", "/api/share/*/detail_with_code");
    }
}
