package com.campus.erp.common;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

/**
 * 登录拦截器：在请求进入 Controller 之前先检查有没有登录。
 *
 * 执行时机：DispatcherServlet 找到处理方法后、调用方法前。
 *
 * 三个位置的区别：
 *   preHandle        —— 进 Controller 之前（这里用这个）
 *   postHandle       —— Controller 返回之后、渲染之前
 *   afterCompletion  —— 整个请求结束后（适合做耗时统计、清理）
 *
 * 为什么用拦截器而不是在每个方法里判空：
 *   一处配置，所有 /api/** 自动生效，不会漏。
 */
@Component
public class AuthInterceptor implements HandlerInterceptor {

    @Override
    public boolean preHandle(HttpServletRequest request,
                             HttpServletResponse response,
                             Object handler) {

        // 浏览器的跨域预检请求不带 Cookie，必须放行，否则前端所有请求都会失败
        if ("OPTIONS".equalsIgnoreCase(request.getMethod())) {
            return true;
        }

        // 注意用 getSession(false)：没有会话时不要新建一个空会话
        HttpSession session = request.getSession(false);
        Object user = (session == null) ? null : session.getAttribute(SessionUtils.LOGIN_USER);

        if (user == null) {
            // 抛出的异常会被 GlobalExceptionHandler 捕获，转成 401 JSON
            throw new BizException(401, "请先登录");
        }
        return true;
    }
}
