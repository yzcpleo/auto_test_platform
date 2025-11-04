package com.autotest.platform.service;

import com.autotest.platform.domain.project.TestProject;
import com.baomidou.mybatisplus.extension.service.IService;

import java.util.List;

/**
 * 测试项目Service接口
 *
 * @author autotest
 * @date 2024-01-01
 */
public interface ITestProjectService extends IService<TestProject> {

    /**
     * 查询测试项目
     *
     * @param projectId 测试项目主键
     * @return 测试项目
     */
    TestProject selectTestProjectByProjectId(Long projectId);

    /**
     * 查询测试项目列表
     *
     * @param testProject 测试项目
     * @return 测试项目集合
     */
    List<TestProject> selectTestProjectList(TestProject testProject);

    /**
     * 新增测试项目
     *
     * @param testProject 测试项目
     * @return 结果
     */
    int insertTestProject(TestProject testProject);

    /**
     * 修改测试项目
     *
     * @param testProject 测试项目
     * @return 结果
     */
    int updateTestProject(TestProject testProject);

    /**
     * 批量删除测试项目
     *
     * @param projectIds 需要删除的测试项目主键集合
     * @return 结果
     */
    int deleteTestProjectByProjectIds(Long[] projectIds);

    /**
     * 删除测试项目信息
     *
     * @param projectId 测试项目主键
     * @return 结果
     */
    int deleteTestProjectByProjectId(Long projectId);
}