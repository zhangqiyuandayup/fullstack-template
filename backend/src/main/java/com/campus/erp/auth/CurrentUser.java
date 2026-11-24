package com.campus.erp.auth;

import java.io.Serializable;

/**
 * 登录成功后放进会话的用户信息。
 *
 * 注意：不含密码，也不含任何敏感字段。
 * 它会被序列化进 Session，也可能被直接返回给前端（/api/auth/me）。
 */
public class CurrentUser implements Serializable {

    private static final long serialVersionUID = 1L;

    private Long id;
    private String username;
    private String displayName;
    private String role;

    public CurrentUser() {
    }

    public CurrentUser(Long id, String username, String displayName, String role) {
        this.id = id;
        this.username = username;
        this.displayName = displayName;
        this.role = role;
    }

    public boolean isTeacher() {
        return "TEACHER".equals(role);
    }

    public boolean isFinance() {
        return "FINANCE".equals(role);
    }

    public boolean isMember() {
        return "MEMBER".equals(role);
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getDisplayName() {
        return displayName;
    }

    public void setDisplayName(String displayName) {
        this.displayName = displayName;
    }

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }
}
