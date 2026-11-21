package com.campus.erp.application;

import com.campus.erp.common.BizException;
import com.campus.erp.project.Project;
import com.campus.erp.project.ProjectMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

/**
 * 申请单业务层 —— 系统的大脑。
 *
 * 五个操作都遵循同一个模子：
 *   ① 查：把要用的数据全部查出来
 *   ② 判：状态 → 权限 → 内容 → 金额，全部通过才继续
 *   ③ 改：动钱 → 改状态 → 写日志
 *
 * 整个方法包在 @Transactional 里：任何一步失败，三步写入全部回滚。
 */
@Service
public class ApplicationService {

    private final ApplicationMapper applicationMapper;
    private final ProjectMapper projectMapper;
    private final OperationLogMapper operationLogMapper;

    public ApplicationService(ApplicationMapper applicationMapper,
                              ProjectMapper projectMapper,
                              OperationLogMapper operationLogMapper) {
        this.applicationMapper = applicationMapper;
        this.projectMapper = projectMapper;
        this.operationLogMapper = operationLogMapper;
    }

    // ============================================================
    // 提交：草稿 → 待审批，冻结经费
    // 金额变化：可用 -申请额，冻结 +申请额
    // ============================================================
    @Transactional
    public void submit(Long applicationId, Long operatorId) {

        FundApplication app = requireApplication(applicationId);
        Project project = requireProject(app.getProjectId());

        // ① 状态
        if (app.getStatus() != ApplicationStatus.DRAFT) {
            throw new BizException("只有草稿可以提交");
        }
        // ② 权限
        if (!app.getApplicantId().equals(operatorId)) {
            throw new BizException("只能提交自己发起的申请");
        }
        // ③ 用途
        String purpose = app.getPurpose();
        if (purpose == null || purpose.trim().isEmpty()) {
            throw new BizException("用途不能为空");
        }
        // ④ 金额
        BigDecimal amount = app.getRequestedAmount();
        if (amount == null || amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new BizException("申请金额必须大于 0");
        }
        if (amount.scale() > 2) {
            throw new BizException("申请金额最多两位小数");
        }
        // ⑤ 余额
        if (project.getAvailable().compareTo(amount) < 0) {
            throw new BizException("可用经费不足，当前可用 " + project.getAvailable() + " 元");
        }

        projectMapper.freeze(project.getId(), amount);
        applicationMapper.updateStatus(applicationId, ApplicationStatus.PENDING_L1);
        operationLogMapper.insert(applicationId, operatorId, "SUBMIT", null);
    }

    // ============================================================
    // 批准：待审批 → 已批准
    // 金额变化：减额的部分【立刻】从冻结释放回可用
    // 例：申请 600，批准 500 → 释放 100（可用 +100，冻结 -100）
    // ============================================================
    @Transactional
    public void approve(Long applicationId, Long operatorId, BigDecimal approvedAmount) {

        FundApplication app = requireApplication(applicationId);
        Project project = requireProject(app.getProjectId());

        if (app.getStatus() != ApplicationStatus.PENDING_L1) {
            throw new BizException("只有待审批的申请可以批准");
        }
        if (approvedAmount == null || approvedAmount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new BizException("批准金额必须大于 0；不同意请使用拒绝");
        }
        if (approvedAmount.scale() > 2) {
            throw new BizException("批准金额最多两位小数");
        }
        if (approvedAmount.compareTo(app.getRequestedAmount()) > 0) {
            throw new BizException("批准金额不能超过申请金额 " + app.getRequestedAmount() + " 元");
        }

        // 减额差额立即释放：冻结里只保留批准金额
        BigDecimal diff = app.getRequestedAmount().subtract(approvedAmount);
        if (diff.compareTo(BigDecimal.ZERO) > 0) {
            projectMapper.release(project.getId(), diff);
        }

        applicationMapper.approve(applicationId, approvedAmount, ApplicationStatus.APPROVED);
        operationLogMapper.insert(applicationId, operatorId, "APPROVE", null);
    }

    // ============================================================
    // 拒绝：待审批 → 已拒绝
    // 金额变化：释放全部冻结（待审批时冻结的就是申请额）
    // ============================================================
    @Transactional
    public void reject(Long applicationId, Long operatorId, String reason) {

        FundApplication app = requireApplication(applicationId);
        Project project = requireProject(app.getProjectId());

        if (app.getStatus() != ApplicationStatus.PENDING_L1) {
            throw new BizException("只有待审批的申请可以拒绝");
        }
        if (reason == null || reason.trim().isEmpty()) {
            throw new BizException("拒绝原因必填");
        }

        projectMapper.release(project.getId(), app.getRequestedAmount());
        applicationMapper.updateStatus(applicationId, ApplicationStatus.REJECTED);
        operationLogMapper.insert(applicationId, operatorId, "REJECT", reason);
    }

    // ============================================================
    // 取消：待审批 / 已批准 → 已取消（只有申请人自己可以取消）
    // 金额变化：只释放【剩余】冻结
    //   待审批 → 释放申请额
    //   已批准 → 释放批准额（减额部分在批准时已经释放过，不能重复释放）
    // ============================================================
    @Transactional
    public void cancel(Long applicationId, Long operatorId, String reason) {

        FundApplication app = requireApplication(applicationId);
        Project project = requireProject(app.getProjectId());

        if (reason == null || reason.trim().isEmpty()) {
            throw new BizException("取消原因必填");
        }
        if (!app.getApplicantId().equals(operatorId)) {
            throw new BizException("只能取消自己发起的申请");
        }

        BigDecimal toRelease;
        if (app.getStatus() == ApplicationStatus.PENDING_L1) {
            toRelease = app.getRequestedAmount();
        } else if (app.getStatus() == ApplicationStatus.APPROVED) {
            toRelease = app.getApprovedAmount();
        } else {
            throw new BizException("只有待审批或已批准的申请可以取消");
        }

        projectMapper.release(project.getId(), toRelease);
        applicationMapper.updateStatus(applicationId, ApplicationStatus.CANCELED);
        operationLogMapper.insert(applicationId, operatorId, "CANCEL", reason);
    }

    // ============================================================
    // 结算：已批准 → 已结算
    // 金额变化：冻结清零，实际花费进已使用，剩余回可用
    // 例：批准 500，实际 460 → 冻结 -500，已用 +460，可用 +40
    // ============================================================
    @Transactional
    public void settle(Long applicationId, Long operatorId, BigDecimal actualAmount) {

        FundApplication app = requireApplication(applicationId);
        Project project = requireProject(app.getProjectId());

        if (app.getStatus() != ApplicationStatus.APPROVED) {
            throw new BizException("只有已批准的申请可以结算");
        }
        if (actualAmount == null || actualAmount.compareTo(BigDecimal.ZERO) < 0) {
            throw new BizException("实际花费不能为负数（可以是 0）");
        }
        if (actualAmount.scale() > 2) {
            throw new BizException("实际花费最多两位小数");
        }
        if (actualAmount.compareTo(app.getApprovedAmount()) > 0) {
            throw new BizException("实际花费不能超过批准金额 " + app.getApprovedAmount() + " 元");
        }

        projectMapper.settle(project.getId(), app.getApprovedAmount(), actualAmount);
        applicationMapper.settle(applicationId, actualAmount, ApplicationStatus.SETTLED);
        operationLogMapper.insert(applicationId, operatorId, "SETTLE", null);
    }

    // ============================================================
    // 辅助：查不到就报错，避免后续空指针
    // ============================================================
    private FundApplication requireApplication(Long applicationId) {
        FundApplication app = applicationMapper.findById(applicationId);
        if (app == null) {
            throw new BizException("申请单不存在：" + applicationId);
        }
        return app;
    }

    private Project requireProject(Long projectId) {
        Project project = projectMapper.findById(projectId);
        if (project == null) {
            throw new BizException("项目不存在：" + projectId);
        }
        return project;
    }
}
