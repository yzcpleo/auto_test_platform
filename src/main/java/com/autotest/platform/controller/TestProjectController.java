package com.autotest.platform.controller;

import java.util.List;
import javax.servlet.http.HttpServletResponse;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import com.autotest.platform.common.annotation.Log;
import com.autotest.platform.common.core.controller.BaseController;
import com.autotest.platform.common.core.domain.AjaxResult;
import com.autotest.platform.common.enums.BusinessType;
import com.autotest.platform.domain.project.TestProject;
import com.autotest.platform.service.ITestProjectService;
import com.autotest.platform.common.utils.poi.ExcelUtil;
import com.autotest.platform.common.core.page.TableDataInfo;

/**
 * 测试项目Controller
 *
 * @author autotest
 * @date 2024-01-01
 */
@RestController
@RequestMapping("/test/project")
public class TestProjectController extends BaseController {
    @Autowired
    private ITestProjectService testProjectService;

    /**
     * 查询测试项目列表
     */
    @PreAuthorize("@ss.hasPermi('test:project:list')")
    @GetMapping("/list")
    public TableDataInfo list(TestProject testProject) {
        startPage();
        List<TestProject> list = testProjectService.selectTestProjectList(testProject);
        return getDataTable(list);
    }

    /**
     * 查询我的项目列表
     */
    @PreAuthorize("@ss.hasPermi('test:project:list')")
    @GetMapping("/my-projects")
    public AjaxResult myProjects() {
        List<TestProject> list = testProjectService.selectProjectsByUserId(getUserId());
        return AjaxResult.success(list);
    }

    /**
     * 导出测试项目列表
     */
    @PreAuthorize("@ss.hasPermi('test:project:export')")
    @Log(title = "测试项目", businessType = BusinessType.EXPORT)
    @PostMapping("/export")
    public void export(HttpServletResponse response, TestProject testProject) {
        List<TestProject> list = testProjectService.selectTestProjectList(testProject);
        ExcelUtil<TestProject> util = new ExcelUtil<TestProject>(TestProject.class);
        util.exportExcel(response, list, "测试项目数据");
    }

    /**
     * 获取测试项目详细信息
     */
    @PreAuthorize("@ss.hasPermi('test:project:query')")
    @GetMapping(value = "/{projectId}")
    public AjaxResult getInfo(@PathVariable("projectId") Long projectId) {
        TestProject project = testProjectService.selectTestProjectByProjectId(projectId);
        // 获取项目统计信息
        TestProject statistics = testProjectService.getProjectStatistics(projectId);
        if (statistics != null) {
            project.setMemberCount(statistics.getMemberCount());
            project.setCaseCount(statistics.getCaseCount());
            project.setLastExecutionTime(statistics.getLastExecutionTime());
            project.setLastExecutionStatus(statistics.getLastExecutionStatus());
        }
        return AjaxResult.success(project);
    }

    /**
     * 新增测试项目
     */
    @PreAuthorize("@ss.hasPermi('test:project:add')")
    @Log(title = "测试项目", businessType = BusinessType.INSERT)
    @PostMapping
    public AjaxResult add(@RequestBody TestProject testProject) {
        if (!testProjectService.checkProjectNameUnique(testProject)) {
            return AjaxResult.error("新增项目'" + testProject.getProjectName() + "'失败，项目名称已存在");
        }
        return toAjax(testProjectService.insertTestProject(testProject));
    }

    /**
     * 修改测试项目
     */
    @PreAuthorize("@ss.hasPermi('test:project:edit')")
    @Log(title = "测试项目", businessType = BusinessType.UPDATE)
    @PutMapping
    public AjaxResult edit(@RequestBody TestProject testProject) {
        testProjectService.checkProjectNameUnique(testProject);
        return toAjax(testProjectService.updateTestProject(testProject));
    }

    /**
     * 删除测试项目
     */
    @PreAuthorize("@ss.hasPermi('test:project:remove')")
    @Log(title = "测试项目", businessType = BusinessType.DELETE)
    @DeleteMapping("/{projectIds}")
    public AjaxResult remove(@PathVariable Long[] projectIds) {
        return toAjax(testProjectService.deleteTestProjectByProjectIds(projectIds));
    }
}