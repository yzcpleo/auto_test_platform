package com.autotest.platform.controller;

import com.autotest.platform.domain.project.TestProject;
import com.autotest.platform.service.ITestProjectService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 测试项目控制器
 *
 * @author autotest
 * @date 2024-01-01
 */
@RestController
@RequestMapping("/api/projects")
public class TestProjectController {

    @Autowired
    private ITestProjectService testProjectService;

    /**
     * 查询测试项目列表
     */
    @GetMapping
    public List<TestProject> list(TestProject testProject) {
        return testProjectService.selectTestProjectList(testProject);
    }

    /**
     * 获取测试项目详细信息
     */
    @GetMapping("/{projectId}")
    public TestProject getInfo(@PathVariable("projectId") Long projectId) {
        return testProjectService.selectTestProjectByProjectId(projectId);
    }

    /**
     * 新增测试项目
     */
    @PostMapping
    public int add(@RequestBody TestProject testProject) {
        return testProjectService.insertTestProject(testProject);
    }

    /**
     * 修改测试项目
     */
    @PutMapping
    public int edit(@RequestBody TestProject testProject) {
        return testProjectService.updateTestProject(testProject);
    }

    /**
     * 删除测试项目
     */
    @DeleteMapping("/{projectIds}")
    public int remove(@PathVariable Long[] projectIds) {
        return testProjectService.deleteTestProjectByProjectIds(projectIds);
    }
}