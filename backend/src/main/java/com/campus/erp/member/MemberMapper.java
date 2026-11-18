package com.campus.erp.member;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * 成员数据访问接口（Mapper）。
 *
 * 这里只写两样东西：方法签名 + SQL。
 * MyBatis 负责：
 *   1. 生成 PreparedStatement（NULL，
 *      用 #{} 是占位符，防 SQL 注入）
 *   2. 把结果集的列映射到 Member 的字段
 *   3. 借连接、还连接
 *   4. 把 SQLException 转成 Spring 的异常
 *
 * 注意：SELECT 里不查 password_hash，从源头避免泄露。
 */
@Mapper
public interface MemberMapper {

    @Select("""
            SELECT id,
                   username,
                   display_name,
                   role,
                   created_at
            FROM member
            ORDER BY id
            """)
    List<Member> findAll();
}
