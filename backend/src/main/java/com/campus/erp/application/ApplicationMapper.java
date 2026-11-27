package com.campus.erp.application;

import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Options;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import java.math.BigDecimal;
import java.util.List;

/**
 * 申请单数据访问。
 */
@Mapper
public interface ApplicationMapper {

    @Select("""
            SELECT id, project_id, applicant_id, purpose,
                   requested_amount, approved_amount, settled_amount,
                   status, created_at, updated_at
            FROM fund_application
            WHERE id = #{id}
            """)
    FundApplication findById(@Param("id") Long id);

    @Select("""
            SELECT id, project_id, applicant_id, purpose,
                   requested_amount, approved_amount, settled_amount,
                   status, created_at, updated_at
            FROM fund_application
            WHERE project_id = #{projectId}
            ORDER BY id DESC
            """)
    List<FundApplication> findByProjectId(@Param("projectId") Long projectId);

    /**
     * 只查某个成员自己的申请。
     * 用途：docs/02 要求「成员……不能因此看别人申请的详情」。
     */
    @Select("""
            SELECT id, project_id, applicant_id, purpose,
                   requested_amount, approved_amount, settled_amount,
                   status, created_at, updated_at
            FROM fund_application
            WHERE project_id = #{projectId}
              AND applicant_id = #{applicantId}
            ORDER BY id DESC
            """)
    List<FundApplication> findByProjectIdAndApplicant(@Param("projectId") Long projectId,
                                                      @Param("applicantId") Long applicantId);

    /** 只改状态（提交、拒绝、取消时用） */
    @Update("""
            UPDATE fund_application
            SET status = #{status}
            WHERE id = #{id}
            """)
    int updateStatus(@Param("id") Long id, @Param("status") ApplicationStatus status);

    /** 批准：同时写批准金额与状态 */
    @Update("""
            UPDATE fund_application
            SET approved_amount = #{approvedAmount},
                status = #{status}
            WHERE id = #{id}
            """)
    int approve(@Param("id") Long id,
                @Param("approvedAmount") BigDecimal approvedAmount,
                @Param("status") ApplicationStatus status);

    /** 结算：同时写结算金额与状态 */
    @Update("""
            UPDATE fund_application
            SET settled_amount = #{settledAmount},
                status = #{status}
            WHERE id = #{id}
            """)
    int settle(@Param("id") Long id,
               @Param("settledAmount") BigDecimal settledAmount,
               @Param("status") ApplicationStatus status);

    /**
     * 新建申请（草稿）。
     * useGeneratedKeys 让 MyBatis 把数据库生成的自增 id 回填到对象的 id 字段。
     */
    @Insert("""
            INSERT INTO fund_application
                (project_id, applicant_id, purpose, requested_amount, status)
            VALUES
                (#{projectId}, #{applicantId}, #{purpose}, #{requestedAmount}, #{status})
            """)
    @Options(useGeneratedKeys = true, keyProperty = "id")
    int insert(FundApplication application);
}
