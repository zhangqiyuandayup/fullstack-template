package com.campus.erp.application;

import com.campus.erp.common.Result;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.math.BigDecimal;
import java.util.List;

/**
 * 申请单接口。
 *
 * 五个动作对应五个端点：
 *   POST /api/applications/{id}/submit   ?operatorId=  提交
 *   POST /api/applications/{id}/approve  ?operatorId=&approvedAmount=  批准
 *   POST /api/applications/{id}/reject   ?operatorId=&reason=          拒绝
 *   POST /api/applications/{id}/cancel   ?operatorId=&reason=          取消
 *   POST /api/applications/{id}/settle   ?operatorId=&actualAmount=    结算
 *
 * ⚠️ operatorId 目前由请求参数传入，仅用于把功能跑通。
 *    正式版必须改成从登录会话（HttpSession）中取当前用户，
 *    否则任何人改一下参数就能冒充别人操作。
 */
@RestController
@RequestMapping("/api/applications")
public class ApplicationController {

    private final ApplicationService applicationService;
    private final ApplicationMapper applicationMapper;

    public ApplicationController(ApplicationService applicationService,
                                 ApplicationMapper applicationMapper) {
        this.applicationService = applicationService;
        this.applicationMapper = applicationMapper;
    }

    /** 查询某个项目下的所有申请单 */
    @GetMapping
    public Result<List<FundApplication>> listByProject(@RequestParam Long projectId) {
        return Result.ok(applicationMapper.findByProjectId(projectId));
    }

    /** 查询单张申请单 */
    @GetMapping("/{id}")
    public Result<FundApplication> detail(@PathVariable Long id) {
        return Result.ok(applicationMapper.findById(id));
    }

    /** 提交申请 */
    @PostMapping("/{id}/submit")
    public Result<Void> submit(@PathVariable Long id, @RequestParam Long operatorId) {
        applicationService.submit(id, operatorId);
        return Result.ok();
    }

    /** 批准（可减额） */
    @PostMapping("/{id}/approve")
    public Result<Void> approve(@PathVariable Long id,
                                @RequestParam Long operatorId,
                                @RequestParam BigDecimal approvedAmount) {
        applicationService.approve(id, operatorId, approvedAmount);
        return Result.ok();
    }

    /** 拒绝 */
    @PostMapping("/{id}/reject")
    public Result<Void> reject(@PathVariable Long id,
                               @RequestParam Long operatorId,
                               @RequestParam String reason) {
        applicationService.reject(id, operatorId, reason);
        return Result.ok();
    }

    /** 取消（申请人本人） */
    @PostMapping("/{id}/cancel")
    public Result<Void> cancel(@PathVariable Long id,
                               @RequestParam Long operatorId,
                               @RequestParam String reason) {
        applicationService.cancel(id, operatorId, reason);
        return Result.ok();
    }

    /** 结算（财务） */
    @PostMapping("/{id}/settle")
    public Result<Void> settle(@PathVariable Long id,
                               @RequestParam Long operatorId,
                               @RequestParam BigDecimal actualAmount) {
        applicationService.settle(id, operatorId, actualAmount);
        return Result.ok();
    }
}
