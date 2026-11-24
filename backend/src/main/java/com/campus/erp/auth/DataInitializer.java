package com.campus.erp.auth;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

/**
 * 启动时初始化演示账号的密码。
 *
 * 背景：D3 的 seed 数据里 password_hash 是占位符 'placeholder-not-a-real-hash'，
 * 不是真正的哈希，无法用来登录。这里在启动时把它替换成 BCrypt 哈希。
 *
 * 安全性：只更新仍是占位符的记录，已经设置过真实密码的账号不会被覆盖。
 */
@Component
public class DataInitializer implements ApplicationRunner {

    private static final Logger log = LoggerFactory.getLogger(DataInitializer.class);

    /** 演示用统一初始密码，正式环境必须改掉 */
    private static final String DEFAULT_PASSWORD = "123456";

    private final AuthMapper authMapper;
    private final PasswordEncoder passwordEncoder;

    public DataInitializer(AuthMapper authMapper, PasswordEncoder passwordEncoder) {
        this.authMapper = authMapper;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public void run(ApplicationArguments args) {
        int updated = authMapper.replacePlaceholderHash(passwordEncoder.encode(DEFAULT_PASSWORD));
        if (updated > 0) {
            log.warn("已将 {} 个演示账号的密码初始化为 {}，正式环境请尽快修改", updated, DEFAULT_PASSWORD);
        }
    }
}
