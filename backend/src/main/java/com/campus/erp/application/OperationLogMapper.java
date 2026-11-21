package com.campus.erp.application;

import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

/**
 * 操作日志数据访问。
 *
 * 每次成功操作都要留一条记录：谁、在什么时候、对哪张单子、做了什么。
 * 这也是模板进阶需求 E2 的基础，现在先能写就行。
 */
@Mapper
public interface OperationLogMapper {

    @Insert("""
            INSERT INTO operation_log (application_id, operator_id, action, reason)
            VALUES (#{applicationId}, #{operatorId}, #{action}, #{reason})
            """)
    int insert(@Param("applicationId") Long applicationId,
               @Param("operatorId") Long operatorId,
               @Param("action") String action,
               @Param("reason") String reason);
}
