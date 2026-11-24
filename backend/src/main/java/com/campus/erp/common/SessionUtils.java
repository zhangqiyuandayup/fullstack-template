package com.campus.erp.common;

import com.campus.erp.auth.CurrentUser;
import jakarta.servlet.http.HttpSession;

/**
 * 会话工具：统一从这里取"当前登录用户"。
 *
 * 为什么要有这个类：
 *   如果每个 Controller 都自己写 session.getAttribute("LOGIN_USER") 然后判空，
 *   字符串容易写错、判空容易漏。集中到一处，改一处即可。
 */
public final class SessionUtils {

    /** 会话中保存当前用户的 key */
    public static final String LOGIN_USER = "LOGIN_USER";

    private SessionUtils() {
    }

    /**
     * 取当前登录用户；没登录直接抛 401。
     * 业务方法里用它，就不用每处都判空。
     */
    public static CurrentUser requireUser(HttpSession session) {
        Object value = session == null ? null : session.getAttribute(LOGIN_USER);
        if (value == null) {
            throw new BizException(401, "请先登录");
        }
        return (CurrentUser) value;
    }

    /** 取当前登录用户；没登录返回 null（用于 /me 这类允许匿名判断的接口） */
    public static CurrentUser getUser(HttpSession session) {
        Object value = session == null ? null : session.getAttribute(LOGIN_USER);
        return value == null ? null : (CurrentUser) value;
    }
}
