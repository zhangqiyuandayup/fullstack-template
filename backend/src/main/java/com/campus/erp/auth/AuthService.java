package com.campus.erp.auth;

import com.campus.erp.common.BizException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

/**
 * 认证业务：校验用户名密码，通过后返回会话用的用户对象。
 */
@Service
public class AuthService {

    private final AuthMapper authMapper;
    private final PasswordEncoder passwordEncoder;

    public AuthService(AuthMapper authMapper, PasswordEncoder passwordEncoder) {
        this.authMapper = authMapper;
        this.passwordEncoder = passwordEncoder;
    }

    public CurrentUser login(String username, String rawPassword) {
        if (username == null || username.trim().isEmpty()
                || rawPassword == null || rawPassword.isEmpty()) {
            throw new BizException(401, "用户名和密码不能为空");
        }

        LoginMember member = authMapper.findByUsername(username.trim());

        // 注意：用户不存在与密码错误返回【同一句】提示。
        // 若区分，攻击者就能通过提示判断出哪些用户名是真实存在的。
        if (member == null) {
            throw new BizException(401, "用户名或密码错误");
        }
        if (!passwordEncoder.matches(rawPassword, member.getPasswordHash())) {
            throw new BizException(401, "用户名或密码错误");
        }

        return new CurrentUser(member.getId(), member.getUsername(),
                member.getDisplayName(), member.getRole());
    }
}
