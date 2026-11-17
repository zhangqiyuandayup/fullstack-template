-- ============================================================
-- 校园经费管理 - 数据库表结构（完整版）
-- 数据库: campus_erp
-- 执行:   mysql -u root -p -e "source practice/sql/01_schema.sql"
--
-- 说明: 本脚本可重复执行。每次执行会先删除旧表再重建，
--       不会删除数据库本身，也不影响其他数据库。
-- ============================================================

CREATE DATABASE IF NOT EXISTS campus_erp
    DEFAULT CHARACTER SET utf8mb4
    COLLATE utf8mb4_unicode_ci;

USE campus_erp;

-- Windows 中文系统的 mysql 客户端默认可能是 gbk，
-- 读取 UTF-8 脚本里的中文会得到乱码并触发 ERROR 1366。
-- 这一行强制本次会话使用 utf8mb4。
SET NAMES utf8mb4;

-- 先关外键检查，这样删表顺序无关；避免因外键依赖删不掉
SET FOREIGN_KEY_CHECKS = 0;
DROP TABLE IF EXISTS operation_log;
DROP TABLE IF EXISTS fund_application;
DROP TABLE IF EXISTS project_member;
DROP TABLE IF EXISTS member;
DROP TABLE IF EXISTS project;
SET FOREIGN_KEY_CHECKS = 1;

-- ============================================================
-- ① 项目表
-- 业务规则: 总预算 = 可用 + 冻结 + 已使用，三项都必须非负
--           守恒约束放在数据库层，后端算错会被拒绝写入
-- ============================================================
CREATE TABLE project (
    id           BIGINT        NOT NULL AUTO_INCREMENT COMMENT '项目主键',
    name         VARCHAR(100)  NOT NULL                COMMENT '项目名称',
    total_budget DECIMAL(12,2) NOT NULL                COMMENT '总预算',
    available    DECIMAL(12,2) NOT NULL                COMMENT '可用',
    frozen       DECIMAL(12,2) NOT NULL                COMMENT '冻结',
    used         DECIMAL(12,2) NOT NULL                COMMENT '已使用',
    created_at   DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    PRIMARY KEY (id),
    CONSTRAINT chk_project_total     CHECK (total_budget > 0),
    CONSTRAINT chk_project_available CHECK (available >= 0),
    CONSTRAINT chk_project_frozen    CHECK (frozen    >= 0),
    CONSTRAINT chk_project_used      CHECK (used      >= 0),
    -- 核心不变式：任何时刻三项之和必须等于总预算
    CONSTRAINT chk_project_balanced  CHECK (available + frozen + used = total_budget)
) COMMENT '项目';

-- ============================================================
-- ② 成员表
-- 业务规则: 四种角色；用户名唯一；只存密码哈希，不存明文
-- ============================================================
CREATE TABLE member (
    id            BIGINT       NOT NULL AUTO_INCREMENT COMMENT '成员主键',
    username      VARCHAR(50)  NOT NULL                COMMENT '登录名',
    display_name  VARCHAR(50)  NOT NULL                COMMENT '显示名',
    password_hash VARCHAR(100) NOT NULL                COMMENT '密码哈希（禁止存明文）',
    role          VARCHAR(20)  NOT NULL                COMMENT '角色',
    created_at    DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    PRIMARY KEY (id),
    UNIQUE KEY uk_member_username (username),
    CONSTRAINT chk_member_role CHECK (role IN ('MEMBER', 'TEACHER', 'FINANCE', 'ADMIN')),
    CONSTRAINT chk_member_username CHECK (TRIM(username) <> '')
) COMMENT '成员';

-- ============================================================
-- ③ 项目成员关联表（解决多对多）
-- 业务规则: 一个人可参加多个项目；一个项目有多个成员
--           同一人不能重复加入同一项目
-- ============================================================
CREATE TABLE project_member (
    id         BIGINT NOT NULL AUTO_INCREMENT COMMENT '关联主键',
    project_id BIGINT NOT NULL                COMMENT '项目',
    member_id  BIGINT NOT NULL                COMMENT '成员',
    PRIMARY KEY (id),
    UNIQUE KEY uk_project_member (project_id, member_id),
    CONSTRAINT fk_pm_project FOREIGN KEY (project_id) REFERENCES project(id),
    CONSTRAINT fk_pm_member  FOREIGN KEY (member_id)  REFERENCES member(id),
    INDEX idx_pm_member (member_id)
) COMMENT '项目成员关联';

-- ============================================================
-- ④ 申请单表（核心业务表）
-- 业务规则:
--   - 原申请额、批准额、结算额分别保留
--   - approved_amount / settled_amount 允许 NULL：
--       NULL = 尚未发生；0.00 = 发生了且金额为零
--   - 批准额 > 0 且 <= 申请额
--   - 结算额 >= 0 且 <= 批准额
--   - 状态只能是六个值之一
-- ============================================================
CREATE TABLE fund_application (
    id               BIGINT        NOT NULL AUTO_INCREMENT COMMENT '申请主键',
    project_id       BIGINT        NOT NULL                COMMENT '所属项目',
    applicant_id     BIGINT        NOT NULL                COMMENT '申请人',
    purpose          VARCHAR(200)  NOT NULL                COMMENT '用途',
    requested_amount DECIMAL(12,2) NOT NULL                COMMENT '原申请金额',
    approved_amount  DECIMAL(12,2) NULL                    COMMENT '批准金额（未批准为 NULL）',
    settled_amount   DECIMAL(12,2) NULL                    COMMENT '结算金额（未结算为 NULL）',
    status           VARCHAR(20)   NOT NULL DEFAULT 'DRAFT' COMMENT '状态',
    created_at       DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at       DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP
                                   ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (id),
    CONSTRAINT fk_fa_project   FOREIGN KEY (project_id)   REFERENCES project(id),
    CONSTRAINT fk_fa_applicant FOREIGN KEY (applicant_id) REFERENCES member(id),
    CONSTRAINT chk_fa_purpose   CHECK (TRIM(purpose) <> ''),
    CONSTRAINT chk_fa_requested CHECK (requested_amount > 0),
    CONSTRAINT chk_fa_approved  CHECK (
        approved_amount IS NULL
        OR (approved_amount > 0 AND approved_amount <= requested_amount)
    ),
    CONSTRAINT chk_fa_settled   CHECK (
        settled_amount IS NULL
        OR (approved_amount IS NOT NULL
            AND settled_amount >= 0
            AND settled_amount <= approved_amount)
    ),
    CONSTRAINT chk_fa_status    CHECK (
        status IN ('DRAFT', 'PENDING_L1', 'APPROVED', 'REJECTED', 'CANCELED', 'SETTLED')
    ),
    INDEX idx_fa_status (status),
    INDEX idx_fa_project (project_id),
    INDEX idx_fa_applicant (applicant_id)
) COMMENT '经费申请单';

-- ============================================================
-- ⑤ 操作日志表
-- 业务规则: 每次提交/批准/拒绝/取消/结算都留一条记录
-- ============================================================
CREATE TABLE operation_log (
    id             BIGINT       NOT NULL AUTO_INCREMENT COMMENT '日志主键',
    application_id BIGINT       NOT NULL                COMMENT '对应申请',
    operator_id    BIGINT       NOT NULL                COMMENT '操作人',
    action         VARCHAR(30)  NOT NULL                COMMENT '动作',
    reason         VARCHAR(200) NULL                    COMMENT '原因（拒绝/取消时必填）',
    created_at     DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '操作时间',
    PRIMARY KEY (id),
    CONSTRAINT fk_log_application FOREIGN KEY (application_id) REFERENCES fund_application(id),
    CONSTRAINT fk_log_operator    FOREIGN KEY (operator_id)    REFERENCES member(id),
    CONSTRAINT chk_log_action CHECK (
        action IN ('SUBMIT', 'APPROVE', 'REJECT', 'CANCEL', 'SETTLE')
    ),
    INDEX idx_log_application (application_id)
) COMMENT '操作日志';

SELECT 'OK: 5 tables created' AS result;
