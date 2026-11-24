package com.campus.erp.common;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * 全局配置：密码加密器 + 登录拦截器注册。
 */
@Configuration
public class AppConfig implements WebMvcConfigurer {

    private final AuthInterceptor authInterceptor;

    public AppConfig(AuthInterceptor authInterceptor) {
        this.authInterceptor = authInterceptor;
    }

    /**
     * BCrypt 密码加密器。
     *
     * 特点：同一个密码每次加密结果都不同（内置随机盐），无法从哈希反推原文；
     *      校验时用 matches(明文, 哈希)，由它内部把盐取出来重算比对。
     */
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(authInterceptor)
                .addPathPatterns("/api/**")          // 所有业务接口都要求登录
                .excludePathPatterns(
                        "/api/auth/login",           // 登录接口本身不能要求登录
                        "/api/auth/me"               // 允许匿名查询“我是谁”，返回 null
                );
    }
}
