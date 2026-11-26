package com.campus.erp.application;

import java.math.BigDecimal;

/**
 * 新建申请的请求体。
 *
 * 对应前端 POST /api/applications 的 JSON：
 *   {"projectId":1,"purpose":"买硬盘","requestedAmount":120.00}
 *
 * 注意：没有 applicantId。
 * 申请人由后端从会话里取，前端无法指定"替谁申请"。
 */
public class CreateApplicationRequest {

    private Long projectId;
    private String purpose;
    private BigDecimal requestedAmount;

    public Long getProjectId() {
        return projectId;
    }

    public void setProjectId(Long projectId) {
        this.projectId = projectId;
    }

    public String getPurpose() {
        return purpose;
    }

    public void setPurpose(String purpose) {
        this.purpose = purpose;
    }

    public BigDecimal getRequestedAmount() {
        return requestedAmount;
    }

    public void setRequestedAmount(BigDecimal requestedAmount) {
        this.requestedAmount = requestedAmount;
    }
}
