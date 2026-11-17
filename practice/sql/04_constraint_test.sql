-- ============================================================
-- 约束测试（故意让数据库报错，证明约束真的生效）
-- 执行: mysql -u root -p --default-character-set=utf8mb4 ^
--         -e "source practice/sql/04_constraint_test.sql"
--
-- 本脚本预期结果: 报错 ERROR 3819
--   Check constraint 'chk_project_balanced' is violated.
--
-- 报错 = 约束在起作用 = 正确
-- 不报错 = 约束没加上 = 需要重跑 01_schema.sql
--
-- 每条测试都用事务包住并回滚，不会污染数据。
-- ============================================================

USE campus_erp;
SET NAMES utf8mb4;

-- ------------------------------------------------------------
-- 测试 1: 破坏守恒（可用 + 冻结 + 已用 != 总预算）
-- 期望: ERROR 3819 chk_project_balanced is violated
-- ------------------------------------------------------------
SELECT '--- 测试 1: 破坏守恒约束 ---' AS section;
START TRANSACTION;
UPDATE project SET available = 9999.00 WHERE id = 1;
ROLLBACK;

-- ------------------------------------------------------------
-- 测试 2: 负数额（可用变成 -1）
-- 期望: ERROR 3819 chk_project_available is violated
--       注意这次连守恒也破了，但先触发哪一条看 MySQL 实现
-- ------------------------------------------------------------
SELECT '--- 测试 2: 负数金额 ---' AS section;
START TRANSACTION;
UPDATE project SET available = -1.00, frozen = 401.00 WHERE id = 1;
ROLLBACK;

-- ------------------------------------------------------------
-- 测试 3: 非法状态值
-- 期望: ERROR 3819 chk_fa_status is violated
-- ------------------------------------------------------------
SELECT '--- 测试 3: 非法状态 ---' AS section;
START TRANSACTION;
UPDATE fund_application SET status = 'PENDING' WHERE id = 2;
ROLLBACK;

-- ------------------------------------------------------------
-- 测试 4: 批准额大于申请额
-- 期望: ERROR 3819 chk_fa_approved is violated
-- ------------------------------------------------------------
SELECT '--- 测试 4: 批准额超过申请额 ---' AS section;
START TRANSACTION;
UPDATE fund_application SET approved_amount = 999.00 WHERE id = 2;
ROLLBACK;

-- ------------------------------------------------------------
-- 测试 5: 结算额大于批准额
-- 期望: ERROR 3819 chk_fa_settled is violated
-- ------------------------------------------------------------
SELECT '--- 测试 5: 结算额超过批准额 ---' AS section;
START TRANSACTION;
UPDATE fund_application SET settled_amount = 999.00 WHERE id = 4;
ROLLBACK;

-- ------------------------------------------------------------
-- 最终确认：数据仍然完好
-- 期望: 140.00 / 400.00 / 460.00  是否守恒=1
-- ------------------------------------------------------------
SELECT '--- 最终确认：数据完好 ---' AS section;
SELECT
    name      AS 项目,
    available AS 可用,
    frozen    AS 冻结,
    used      AS 已用,
    (available + frozen + used = total_budget) AS 是否守恒
FROM project;
