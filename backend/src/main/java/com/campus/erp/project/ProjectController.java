package com.campus.erp.project;

import com.campus.erp.common.BizException;
import com.campus.erp.common.Result;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * 项目接口。
 *
 * 这是前端"经费看板"的数据来源：
 *   GET /api/projects        项目列表
 *   GET /api/projects/{id}   单个项目（含可用/冻结/已用三个金额）
 *
 * 前端把它渲染成卡片：
 *   总预算 1000  | 可用 460 | 冻结 0 | 已用 540
 * 三个数加起来必须等于总额，演示时可以当场算给老师看。
 */
@RestController
@RequestMapping("/api/projects")
public class ProjectController {

    private final ProjectMapper projectMapper;

    public ProjectController(ProjectMapper projectMapper) {
        this.projectMapper = projectMapper;
    }

    @GetMapping
    public Result<List<Project>> list() {
        return Result.ok(projectMapper.findAll());
    }

    @GetMapping("/{id}")
    public Result<Project> detail(@PathVariable Long id) {
        Project project = projectMapper.findById(id);
        if (project == null) {
            throw new BizException("项目不存在：" + id);
        }
        return Result.ok(project);
    }
}
