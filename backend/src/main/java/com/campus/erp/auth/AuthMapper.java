package com.campus.erp.auth;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

/**
 * 认证相关数据访问。
 *
 * 只有这个 Mapper 会查 password_hash，且结果只给 AuthService 用。
 */
@Mapper
public interface AuthMapper {

    @Select("""
            SELECT id, username, display_name, password_hash, role
            FROM member
            WHERE username = #{username}
            """)
    LoginMember findByUsername(@Param("username") String username);

    /**
     * 把演示用的占位密码替换成 BCrypt 哈希。
     * 只在首次启动、密码还是占位符时才会真正更新。
     */
    @Update("""
            UPDATE member
            SET password_hash = #{hash}
            WHERE password_hash = 'placeholder-not-a-real-hash'
            """)
    int replacePlaceholderHash(@Param("hash") String hash);
}
