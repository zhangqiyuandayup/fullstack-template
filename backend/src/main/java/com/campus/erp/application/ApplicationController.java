package com.campus.erp.application;

import com.campus.erp.auth.CurrentUser;
import com.campus.erp.common.Result;
import com.campus.erp.common.SessionUtils;
import jakarta.servlet.http.HttpSession;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.math.BigDecimal;
import java.util.List;

/**
 * 申请单接口。
 *
 * 五个动作对应五个端点：
 *   POST /api/applications/{id}/submit   ?           提交（申请人本人）
 *   POST /api/applications/{id}/approve  ?approvedAmount=  批准（项目里的老师）
 *   POST /api/applications/{id}/reject   ?reason=          拒绝（项目里的老师）
 *   POST /api/applications/{id}/cancel   ?reason=          取消（申请人本人）
 *   POST /api/applications/{id}/settle   ?actualAmount=    结算（项目里的财务）
 *
 * 【D7 关键变化】
 *   参数里不再有 operatorId。
 *   操作人一律从 HttpSession 里取，前端无法冒充别人。
 *   要"以王老师的身份操作"，就得真的登录成王老师。
 */
@RestController
@RequestMapping("/api/applications")
public class ApplicationController {

    private final ApplicationService applicationService;

    public ApplicationController(ApplicationService applicationService) {
        this.applicationService = applicationService;
    }

    /**
     * 查询某个项目下的申请单。
     * 可见范围由 Service 按角色决定：成员只看自己的，老师与财务看全项目。
     */
    @GetMapping
    public Result<List<FundApplication>> listByProject(@RequestParam Long projectId,
                                                       HttpSession session) {
        CurrentUser user = SessionUtils.requireUser(session);
        return Result.ok(applicationService.listVisible(projectId, user));
    }

    /** 查询单张申请单（同样按角色校验可见性） */
    @GetMapping("/{id}")
    public Result<FundApplication> detail(@PathVariable Long id, HttpSession session) {
        CurrentUser user = SessionUtils.requireUser(session);
        return Result.ok(applicationService.getDetail(id, user));
    }

    /**
     * 新建申请草稿：POST /api/applications
     * Body: {"projectId":1,"purpose":"买硬盘","requestedAmount":120.00}
     * 返回：新申请单的 id
     */
    @PostMapping
    public Result<Long> create(@RequestBody CreateApplicationRequest request, HttpSession session) {
        CurrentUser user = SessionUtils.requireUser(session);
        Long id = applicationService.create(
                user, request.getProjectId(), request.getPurpose(), request.getRequestedAmount());
        return Result.ok(id);
    }

    /** 提交申请 */
    @PostMapping("/{id}/submit")
    public Result<Void> submit(@PathVariable Long id, HttpSession session) {
        CurrentUser user = SessionUtils.requireUser(session);
        applicationService.submit(id, user);
        return Result.ok();
    }

    /** 批准（可减额） */
    @PostMapping("/{id}/approve")
    public Result<Void> approve(@PathVariable Long id,
                                @RequestParam BigDecimal approvedAmount,
                                HttpSession session) {
        CurrentUser user = SessionUtils.requireUser(session);
        applicationService.approve(id, user, approvedAmount);
        return Result.ok();
    }

    /** 拒绝 */
    @PostMapping("/{id}/reject")
    public Result<Void> reject(@PathVariable Long id,
                               @RequestParam String reason,
                               HttpSession session) {
        CurrentUser user = SessionUtils.requireUser(session);
        applicationService.reject(id, user, reason);
        return Result.ok();
    }

    /** 取消（申请人本人） */
    @PostMapping("/{id}/cancel")
    public Result<Void> cancel(@PathVariable Long id,
                               @RequestParam String reason,
                               HttpSession session) {
        CurrentUser user = SessionUtils.requireUser(session);
        applicationService.cancel(id, user, reason);
        return Result.ok();
    }

    /** 结算（财务） */
    @PostMapping("/{id}/settle")
    public Result<Void> settle(@PathVariable Long id,
                               @RequestParam BigDecimal actualAmount,
                               HttpSession session) {
        CurrentUser user = SessionUtils.requireUser(session);
        applicationService.settle(id, user, actualAmount);
        return Result.ok();
    }
}
