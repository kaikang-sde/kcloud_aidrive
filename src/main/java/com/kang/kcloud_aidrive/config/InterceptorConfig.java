package com.kang.kcloud_aidrive.config;

import com.kang.kcloud_aidrive.interceptor.LoginInterceptor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;


/**
 * Configure interceptor whitelist path development settings
 *
 * @author Kai Kang
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
                .addPathPatterns("/api/accounts/*/**", "/api/files/*/**", "/api/shares/*/**", "/api/recycle-bin/*/**")

                //Exclude from interception
                .excludePathPatterns("/api/accounts/*/registration", "/api/accounts/*/login", "/api/accounts/*/avatar",
                        "/api/shares/*/shared/code", "/api/shares/*/shared", "/api/shares/*/detail_no_code", "/api/shares/*/detail_with_code");
    }
}
