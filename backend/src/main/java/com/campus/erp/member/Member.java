package com.campus.erp.member;

import java.time.LocalDateTime;

/**
 * 成员实体：数据库 member 表的一行 ↔ 一个 Java 对象。
 *
 * 注意：这里故意不包含 password_hash 字段。
 * 密码哈希属于敏感数据，任何接口都不应该把它返回给前端。
 */
public class Member {

    private Long id;
    private String username;
    private String displayName;   // 对应列 display_name（靠 map-underscore-to-camel-case 自动映射）
    private String role;
    private LocalDateTime createdAt;  // 对应列 created_at

    // MyBatis 需要一个无参构造器来实例化对象
    public Member() {
    }

    public Member(Long id, String username, String displayName, String role, LocalDateTime createdAt) {
        this.id = id;
        this.username = username;
        this.displayName = displayName;
        this.role = role;
        this.createdAt = createdAt;
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

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
}
