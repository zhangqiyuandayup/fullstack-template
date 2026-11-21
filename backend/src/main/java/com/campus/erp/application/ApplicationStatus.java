package com.campus.erp.application;

/**
 * 申请单状态。
 *
 * 主流程：
 *   DRAFT --提交--> PENDING_L1 --批准--> APPROVED --结算--> SETTLED
 *                      |                   |
 *                   拒绝|                取消|
 *                      ↓                   ↓
 *                  REJECTED            CANCELED
 *
 * 最后三个状态（REJECTED / CANCELED / SETTLED）表示这次办理已结束，
 * 不能再改金额、不能再办理。
 */
public enum ApplicationStatus {

    /** 草稿：不占经费 */
    DRAFT,

    /** 待审批：经费已冻结 */
    PENDING_L1,

    /** 已批准：经费仍冻结，等待结算 */
    APPROVED,

    /** 已拒绝：结束 */
    REJECTED,

    /** 已取消：结束 */
    CANCELED,

    /** 已结算：结束 */
    SETTLED;

    /** 是否为"已结束"的三个终态 */
    public boolean isFinished() {
        return this == REJECTED || this == CANCELED || this == SETTLED;
    }
}
