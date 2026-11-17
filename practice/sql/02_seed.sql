-- ============================================================
-- 校园经费管理 - 初始数据（可重复执行）
-- 执行: mysql -u root -p -e "source practice/sql/02_seed.sql"
--
-- 场景: 项目 A，总预算 1000 元
--   申请1: 600 申请 -> 批准 500 -> 结算 460  已结算  => 已用 460
--   申请2: 200 元                             草稿    => 不占钱
--   申请3: 300 元                             待审批  => 冻结 300
--   申请4: 100 元 批准 100                    已批准  => 冻结 100
--
-- 结算校验: 可用 140 + 冻结 400 + 已用 460 = 1000  ✅
-- ============================================================

USE campus_erp;

-- 关键：Windows 中文系统的 mysql 客户端默认字符集可能是 gbk，
-- 会把 UTF-8 脚本里的中文按 gbk 解读，导致
--   ERROR 1366 (HY000): Incorrect string value: ... for column 'display_name'
-- 这一行强制本次会话使用 utf8mb4。
SET NAMES utf8mb4;

SET FOREIGN_KEY_CHECKS = 0;
DELETE FROM operation_log;
DELETE FROM fund_application;
DELETE FROM project_member;
DELETE FROM member;
DELETE FROM project;
SET FOREIGN_KEY_CHECKS = 1;

-- ① 项目：1000 元预算，终态 140 / 400 / 460
INSERT INTO project (id, name, total_budget, available, frozen, used)
VALUES (1, '科研项目 A', 1000.00, 140.00, 400.00, 460.00);

-- ② 四个成员，覆盖四种角色
-- 注意: password_hash 只是占位符，不是真实密码
INSERT INTO member (id, username, display_name, password_hash, role) VALUES
(1, 'xiaolin', '小林',   'placeholder-not-a-real-hash', 'MEMBER'),
(2, 'wang',    '王老师', 'placeholder-not-a-real-hash', 'TEACHER'),
(3, 'licaiwu', '李财务', 'placeholder-not-a-real-hash', 'FINANCE'),
(4, 'admin',   '管理员', 'placeholder-not-a-real-hash', 'ADMIN');

-- ③ 谁参加了项目 A
INSERT INTO project_member (project_id, member_id) VALUES
(1, 1),
(1, 2),
(1, 3);

-- ④ 申请单：四种状态各一张
INSERT INTO fund_application
(id, project_id, applicant_id, purpose, requested_amount, approved_amount, settled_amount, status)
VALUES
-- 已结算：600 申请，批准 500，实际花 460
(1, 1, 1, '采购传感器', 600.00, 500.00, 460.00, 'SETTLED'),
-- 草稿：200 元，不占钱，批准额与结算额都是 NULL
(2, 1, 1, '打印文献',   200.00, NULL,   NULL,   'DRAFT'),
-- 待审批：300 元，冻结中
(3, 1, 1, '购买硬盘',   300.00, NULL,   NULL,   'PENDING_L1'),
-- 已批准未结算：100 元，冻结中，结算额 NULL（区别于"结算 0 元"）
(4, 1, 1, '服务器续费', 100.00, 100.00, NULL,   'APPROVED');

-- ⑤ 操作日志：记录申请 1 走过的三步
INSERT INTO operation_log (application_id, operator_id, action, reason) VALUES
(1, 1, 'SUBMIT',  NULL),
(1, 2, 'APPROVE', NULL),
(1, 3, 'SETTLE',  NULL);

SELECT 'OK: seed data inserted' AS result;
