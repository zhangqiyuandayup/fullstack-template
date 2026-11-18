package com.campus.erp.member;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * 成员接口（控制器）。
 *
 * 职责只有三件：
 *   1. 接收请求、取参数
 *   2. 调用下层（这里是 Mapper；等业务变复杂后再引入 Service）
 *   3. 把返回值交给 Spring 转成 JSON
 *
 * 不在这里写业务规则，也不在这里写 SQL。
 *
 * 对应的接口：GET /api/members
 */
@RestController
@RequestMapping("/api/members")
public class MemberController {

    private final MemberMapper memberMapper;

    // 构造器注入：Spring 启动时会自动把 MemberMapper 的实现传进来
    // （注解 @Mapper 让 MyBatis 生成实现类并注册为 Spring Bean）
    public MemberController(MemberMapper memberMapper) {
        this.memberMapper = memberMapper;
    }

    /**
     * GET /api/members
     * 返回数据库中所有成员，自动序列化为 JSON 数组。
     */
    @GetMapping
    public List<Member> list() {
        return memberMapper.findAll();
    }
}
