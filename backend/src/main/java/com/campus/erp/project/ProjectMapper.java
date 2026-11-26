package com.campus.erp.project;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import java.math.BigDecimal;

/**
 * 项目数据访问。
 *
 * 关键设计：每个"动钱"的方法，都是【一条 SQL 同时改多个字段】。
 *
 * 原因：数据库上有 chk_project_balanced 约束
 *   CHECK (available + frozen + used = total_budget)
 * 如果拆成两条 UPDATE，第一条执行完、第二条还没执行的那一瞬间，
 * 三项之和就不等于总额，数据库会立刻拒绝并报错。
 */
@Mapper
public interface ProjectMapper {

    @Select("""
            SELECT id, name, total_budget, available, frozen, used, created_at
            FROM project
            WHERE id = #{id}
            """)
    Project findById(@Param("id") Long id);

    /** 查全部项目（前端首页展示用） */
    @Select("""
            SELECT id, name, total_budget, available, frozen, used, created_at
            FROM project
            ORDER BY id
            """)
    java.util.List<Project> findAll();

    /**
     * 冻结：可用 → 冻结。
     * 用于"提交申请"。
     */
    @Update("""
            UPDATE project
            SET available = available - #{amount},
                frozen    = frozen + #{amount}
            WHERE id = #{id}
            """)
    int freeze(@Param("id") Long id, @Param("amount") BigDecimal amount);

    /**
     * 释放：冻结 → 可用。
     * 用于"拒绝""取消"以及"批准时的减额差额"。
     */
    @Update("""
            UPDATE project
            SET frozen    = frozen - #{amount},
                available = available + #{amount}
            WHERE id = #{id}
            """)
    int release(@Param("id") Long id, @Param("amount") BigDecimal amount);

    /**
     * 结算：冻结清零，实际花费进已使用，剩余回可用。
     *
     * 例：批准 500，实际花 460
     *   frozen    -= 500        → 冻结清零
     *   used      += 460        → 实际花费
     *   available += 500 - 460  → 退回 40
     */
    @Update("""
            UPDATE project
            SET frozen    = frozen - #{approved},
                used      = used + #{actual},
                available = available + #{approved} - #{actual}
            WHERE id = #{id}
            """)
    int settle(@Param("id") Long id,
               @Param("approved") BigDecimal approved,
               @Param("actual") BigDecimal actual);

    /**
     * 查某人是否属于某项目。
     * 返回 1 表示是本项目成员，0 表示不是。
     *
     * 用途：老师只能审批自己负责的项目，财务只能结算自己负责的项目。
     */
    @Select("""
            SELECT COUNT(*)
            FROM project_member
            WHERE project_id = #{projectId}
              AND member_id = #{memberId}
            """)
    int countMembership(@Param("projectId") Long projectId, @Param("memberId") Long memberId);
}
