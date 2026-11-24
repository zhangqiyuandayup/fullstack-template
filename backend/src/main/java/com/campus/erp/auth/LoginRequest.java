package com.campus.erp.auth;

/**
 * 登录请求体。
 *
 * 对应前端 POST /api/auth/login 的 JSON：
 *   {"username":"xiaolin","password":"123456"}
 */
public class LoginRequest {

    private String username;
    private String password;

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }
}
