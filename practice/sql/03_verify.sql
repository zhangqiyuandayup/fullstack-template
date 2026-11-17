-- ============================================================
-- 校园经费管理 - 数据验收
-- 执行: mysql -u root -p -e "source practice/sql/03_verify.sql"
--
-- 每段查询都标注了期望结果，逐条对照即可
-- ============================================================

USE campus_erp;

SET NAMES utf8mb4;

-- ------------------------------------------------------------
-- 查询 1: 经费守恒（最重要）
-- 期望: available=140.00 frozen=400.00 used=460.00
--       sum_check=1000.00  balanced=1
-- ------------------------------------------------------------
SELECT '--- 1. 经费守恒 ---' AS section;
SELECT
    name         AS 项目,
    total_budget AS 总预算,
    available    AS 可用,
    frozen       AS 冻结,
    used         AS 已用,
    (available + frozen + used)                 AS 三项之和,
    (available + frozen + used = total_budget)  AS 是否守恒
FROM project;

-- ------------------------------------------------------------
-- 查询 2: 三个金额分列 + NULL 与 0 的区别
-- 期望: 4 行
--       id=2 草稿:   approved=NULL  settled=NULL
--       id=4 已批准: approved=100.00 settled=NULL   <- 未结算
--       id=1 已结算: settled=460.00                  <- 已结算
-- ------------------------------------------------------------
SELECT '--- 2. 申请单金额明细 ---' AS section;
SELECT
    id               AS 单号,
    purpose          AS 用途,
    requested_amount AS 申请额,
    approved_amount  AS 批准额,
    settled_amount   AS 结算额,
    status           AS 状态
FROM fund_application
ORDER BY id;

-- ------------------------------------------------------------
-- 查询 3: 多对多关联
-- 期望: 3 行 —— 小林(MEMBER)、王老师(TEACHER)、李财务(FINANCE)
-- ------------------------------------------------------------
SELECT '--- 3. 项目成员（多对多）---' AS section;
SELECT
    p.name         AS 项目,
    m.display_name AS 成员,
    m.role         AS 角色
FROM project p
JOIN project_member pm ON pm.project_id = p.id
JOIN member m          ON m.id = pm.member_id
WHERE p.id = 1
ORDER BY m.id;

-- ------------------------------------------------------------
-- 查询 4: 状态分布
-- 期望: APPROVED=1  DRAFT=1  PENDING_L1=1  SETTLED=1
-- ------------------------------------------------------------
SELECT '--- 4. 状态分布 ---' AS section;
SELECT
    status AS 状态,
    COUNT(*) AS 数量
FROM fund_application
GROUP BY status
ORDER BY status;

-- ------------------------------------------------------------
-- 查询 5: 操作日志
-- 期望: 3 行 —— SUBMIT / APPROVE / SETTLE
-- ------------------------------------------------------------
SELECT '--- 5. 操作日志 ---' AS section;
SELECT
    l.id             AS 日志号,
    l.application_id AS 申请号,
    m.display_name   AS 操作人,
    l.action         AS 动作
FROM operation_log l
JOIN member m ON m.id = l.operator_id
ORDER BY l.id;

-- ------------------------------------------------------------
-- 查询 6: 数据未被污染确认
-- 期望: 140.00 / 400.00 / 460.00  是否守恒=1
-- 说明: 约束测试已拆到 04_constraint_test.sql，
--       那个脚本会故意报错，用来证明约束生效
-- ------------------------------------------------------------
SELECT '--- 6. 数据完整性确认 ---' AS section;
SELECT
    name      AS 项目,
    available AS 可用,
    frozen    AS 冻结,
    used      AS 已用,
    (available + frozen + used = total_budget) AS 是否守恒
FROM project;

SELECT 'OK: 03_verify.sql 全部检查完成' AS result;
