import java.math.BigDecimal;

public class Project {
    private final BigDecimal total;
    private BigDecimal available;
    private BigDecimal frozen;
    private BigDecimal used;

    public Project(BigDecimal total) {
        this.total = total;
        this.available = total;
        this.frozen = BigDecimal.ZERO;
        this.used = BigDecimal.ZERO;
    }

    public void freeze(BigDecimal amount) {
        this.available = this.available.subtract(amount);
        this.frozen = this.frozen.add(amount);
    }

    public void unfreeze(BigDecimal amount) {
        this.frozen = this.frozen.subtract(amount);
        this.available = this.available.add(amount);
    }

    // 【修复 问题3】：增加入参校验
    public void settleAmount(BigDecimal actualAmount, BigDecimal approvedAmount) {
        if (actualAmount.compareTo(BigDecimal.ZERO) < 0 || actualAmount.compareTo(approvedAmount) > 0) {
            throw new IllegalArgumentException("结算金额异常！实际花费：" + actualAmount + "，批准金额：" + approvedAmount);
        }
        this.frozen = this.frozen.subtract(approvedAmount);
        this.used = this.used.add(actualAmount);
        BigDecimal refund = approvedAmount.subtract(actualAmount);
        this.available = this.available.add(refund);
    }

    public String snapshot() {
        return String.format("%s / %s / %s", available, frozen, used);
    }

    // 【修复 问题1】：报错信息带上具体数字
    public void checkInvariant() {
        if (available.compareTo(BigDecimal.ZERO) < 0 || frozen.compareTo(BigDecimal.ZERO) < 0 || used.compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException("金额不能为负数！");
        }
        if (available.add(frozen).add(used).compareTo(total) != 0) {
            throw new IllegalArgumentException("总预算不平衡！可用=" + available + " 冻结=" + frozen + " 已用=" + used + " 总额=" + total);
        }
    }
    
    public BigDecimal getAvailable() { return available; }
    public BigDecimal getFrozen() { return frozen; }
    public BigDecimal getUsed() { return used; }
}