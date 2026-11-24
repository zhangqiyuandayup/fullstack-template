package com.campus.erp.auth;

/**
 * 登录时从数据库取出的一行：比 Member 多一个密码哈希。
 *
 * 为什么单独建一个类，而不是给 Member 加字段：
 *   Member 是"对外返回"的对象，一旦它有 passwordHash 字段，
 *   哪天有人不小心直接返回 Member，密码哈希就泄露了。
 *   所以把"能看密码的类"限制在认证流程内部使用。
 */
public class LoginMember {

    private Long id;
    private String username;
    private String displayName;
    private String passwordHash;
    private String role;

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

    public String getPasswordHash() {
        return passwordHash;
    }

    public void setPasswordHash(String passwordHash) {
        this.passwordHash = passwordHash;
    }

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }
}
