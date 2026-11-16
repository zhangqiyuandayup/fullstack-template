import java.math.BigDecimal;

public class FundService {

    public void submit(Project p, FundApplication a) {
        if (a.getStatus() != ApplicationStatus.DRAFT) {
            throw new IllegalArgumentException("只有草稿可以提交");
        }
        if (a.getPurpose() == null || a.getPurpose().trim().isEmpty()) {
            throw new IllegalArgumentException("用途不能为空");
        }
        if (a.getRequestedAmount().compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("金额必须大于0");
        }
        if (a.getRequestedAmount().scale() > 2) {
            throw new IllegalArgumentException("金额最多两位小数");
        }
        if (p.getAvailable().compareTo(a.getRequestedAmount()) < 0) {
            throw new IllegalArgumentException("可用余额不足");
        }

        a.submit();
        p.freeze(a.getRequestedAmount());
        p.checkInvariant();
    }

    public void approve(Project p, FundApplication a, BigDecimal approvedAmount) {
        if (a.getStatus() != ApplicationStatus.PENDING_L1) {
            throw new IllegalArgumentException("状态不对，无法审批");
        }
        if (approvedAmount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("批准金额必须大于0");
        }
        if (approvedAmount.compareTo(a.getRequestedAmount()) > 0) {
            throw new IllegalArgumentException("批准金额不能大于申请金额");
        }

        a.approve(approvedAmount);
        BigDecimal diff = a.getRequestedAmount().subtract(approvedAmount);
        if (diff.compareTo(BigDecimal.ZERO) > 0) {
            p.unfreeze(diff);
        }
        p.checkInvariant();
    }

    public void reject(Project p, FundApplication a, String reason) {
        if (a.getStatus() != ApplicationStatus.PENDING_L1) {
            throw new IllegalArgumentException("状态不对，无法拒绝");
        }
        if (reason == null || reason.trim().isEmpty()) {
            throw new IllegalArgumentException("原因必填");
        }

        a.reject();
        p.unfreeze(a.getRequestedAmount());
        p.checkInvariant();
    }

    public void cancel(Project p, FundApplication a, String reason) {
        if (reason == null || reason.trim().isEmpty()) {
            throw new IllegalArgumentException("原因必填");
        }
        
        BigDecimal toUnfreeze = BigDecimal.ZERO;
        if (a.getStatus() == ApplicationStatus.PENDING_L1) {
            toUnfreeze = a.getRequestedAmount();
        } else if (a.getStatus() == ApplicationStatus.APPROVED) {
            toUnfreeze = a.getApprovedAmount();
        } else {
            throw new IllegalArgumentException("当前状态无法取消");
        }

        a.cancel();
        p.unfreeze(toUnfreeze);
        p.checkInvariant();
    }

    public void settle(Project p, FundApplication a, BigDecimal actualAmount) {
        if (a.getStatus() != ApplicationStatus.APPROVED) {
            throw new IllegalArgumentException("状态不对，无法结算");
        }
        if (actualAmount.compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException("实际花费不能为负");
        }
        if (actualAmount.compareTo(a.getApprovedAmount()) > 0) {
            throw new IllegalArgumentException("实际花费不能超过批准金额");
        }

        a.settle(actualAmount);
        p.settleAmount(actualAmount, a.getApprovedAmount());
        p.checkInvariant();
    }
}