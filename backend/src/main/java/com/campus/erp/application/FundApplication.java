package com.campus.erp.application;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 经费申请单实体，对应 fund_application 表。
 *
 * 三个金额分开存（模板硬要求，不能合成一列）：
 *   requestedAmount 原申请金额   —— 提交后不可改
 *   approvedAmount  批准金额     —— 未批准为 null
 *   settledAmount   结算金额     —— 未结算为 null
 *
 * 为什么批准额和结算额允许 null 而不是 0：
 *   null = 这件事还没发生
 *   0.00 = 这件事发生了，金额为零（例如结算时实际没花钱）
 *   两者必须能区分，否则前端无法显示"尚未结算"与"0 元"。
 */
public class FundApplication {

    private Long id;
    private Long projectId;            // project_id
    private Long applicantId;          // applicant_id
    private String purpose;
    private BigDecimal requestedAmount; // requested_amount
    private BigDecimal approvedAmount;  // approved_amount，可空
    private BigDecimal settledAmount;   // settled_amount，可空
    private ApplicationStatus status;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public FundApplication() {
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getProjectId() {
        return projectId;
    }

    public void setProjectId(Long projectId) {
        this.projectId = projectId;
    }

    public Long getApplicantId() {
        return applicantId;
    }

    public void setApplicantId(Long applicantId) {
        this.applicantId = applicantId;
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

    public BigDecimal getApprovedAmount() {
        return approvedAmount;
    }

    public void setApprovedAmount(BigDecimal approvedAmount) {
        this.approvedAmount = approvedAmount;
    }

    public BigDecimal getSettledAmount() {
        return settledAmount;
    }

    public void setSettledAmount(BigDecimal settledAmount) {
        this.settledAmount = settledAmount;
    }

    public ApplicationStatus getStatus() {
        return status;
    }

    public void setStatus(ApplicationStatus status) {
        this.status = status;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }
}
