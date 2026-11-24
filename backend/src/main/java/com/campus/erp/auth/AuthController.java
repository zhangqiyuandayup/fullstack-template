package com.campus.erp.auth;

import com.campus.erp.common.Result;
import com.campus.erp.common.SessionUtils;
import jakarta.servlet.http.HttpSession;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 认证接口：登录、登出、我是谁。
 *
 * 会话（Session）怎么工作：
 *   1. 登录成功 → 服务端创建一个会话，把用户信息存进去
 *   2. Tomcat 给浏览器回一个 Cookie：JSESSIONID=xxxx
 *   3. 浏览器之后每次请求都自动带上这个 Cookie
 *   4. 服务端凭 JSESSIONID 找到对应会话，就知道“你是谁”了
 *
 * 所以：用户信息存在服务端，Cookie 里只有一个随机 ID。
 * 这就是它比“把用户 id 放在请求参数里”安全的原因——id 是用户能改的，会话 ID 他改不了。
 */
@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    /** 登录：POST /api/auth/login  {"username":"xiaolin","password":"123456"} */
    @PostMapping("/login")
    public Result<CurrentUser> login(@RequestBody LoginRequest request, HttpSession session) {
        CurrentUser user = authService.login(request.getUsername(), request.getPassword());
        session.setAttribute(SessionUtils.LOGIN_USER, user);
        return Result.ok(user);
    }

    /** 登出：销毁会话，Cookie 立即失效 */
    @PostMapping("/logout")
    public Result<Void> logout(HttpSession session) {
        session.invalidate();
        return Result.ok();
    }

    /** 我是谁：未登录时 data 为 null */
    @GetMapping("/me")
    public Result<CurrentUser> me(HttpSession session) {
        return Result.ok(SessionUtils.getUser(session));
    }
}
