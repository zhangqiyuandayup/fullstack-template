import java.math.BigDecimal;

public class FundApplication {
    private ApplicationStatus status;
    private final BigDecimal requestedAmount; 
    private BigDecimal approvedAmount;        
    private BigDecimal settledAmount;         
    private final String purpose;             

    public FundApplication(String purpose, BigDecimal requestedAmount) {
        this.purpose = purpose;
        this.requestedAmount = requestedAmount;
        this.status = ApplicationStatus.DRAFT; 
    }

    // 【修复 问题2】：去掉了 public，只允许同包下的 Service 调用
    void submit() { this.status = ApplicationStatus.PENDING_L1; }
    void approve(BigDecimal approvedAmount) {
        this.status = ApplicationStatus.APPROVED;
        this.approvedAmount = approvedAmount;
    }
    void reject() { this.status = ApplicationStatus.REJECTED; }
    void cancel() { this.status = ApplicationStatus.CANCELED; }
    void settle(BigDecimal actualAmount) {
        this.status = ApplicationStatus.SETTLED;
        this.settledAmount = actualAmount;
    }

    public ApplicationStatus getStatus() { return status; }
    public BigDecimal getRequestedAmount() { return requestedAmount; }
    public BigDecimal getApprovedAmount() { return approvedAmount; }
    public BigDecimal getSettledAmount() { return settledAmount; }
    public String getPurpose() { return purpose; }
}