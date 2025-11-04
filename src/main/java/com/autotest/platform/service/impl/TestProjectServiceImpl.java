package com.autotest.platform.service.impl;

import com.autotest.platform.domain.project.TestProject;
import com.autotest.platform.mapper.TestProjectMapper;
import com.autotest.platform.service.ITestProjectService;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 测试项目Service业务层处理
 *
 * @author autotest
 * @date 2024-01-01
 */
@Service
public class TestProjectServiceImpl extends ServiceImpl<TestProjectMapper, TestProject> implements ITestProjectService {

    @Autowired
    private TestProjectMapper testProjectMapper;

    /**
     * 查询测试项目
     *
     * @param projectId 测试项目主键
     * @return 测试项目
     */
    @Override
    public TestProject selectTestProjectByProjectId(Long projectId) {
        return testProjectMapper.selectById(projectId);
    }

    /**
     * 查询测试项目列表
     *
     * @param testProject 测试项目
     * @return 测试项目
     */
    @Override
    public List<TestProject> selectTestProjectList(TestProject testProject) {
        QueryWrapper<TestProject> queryWrapper = new QueryWrapper<>();
        if (testProject.getProjectName() != null) {
            queryWrapper.like("project_name", testProject.getProjectName());
        }
        if (testProject.getStatus() != null) {
            queryWrapper.eq("status", testProject.getStatus());
        }
        if (testProject.getTenantId() != null) {
            queryWrapper.eq("tenant_id", testProject.getTenantId());
        }
        queryWrapper.eq("del_flag", 0);
        return testProjectMapper.selectList(queryWrapper);
    }

    /**
     * 新增测试项目
     *
     * @param testProject 测试项目
     * @return 结果
     */
    @Override
    public int insertTestProject(TestProject testProject) {
        testProject.setDelFlag(0);
        return testProjectMapper.insert(testProject);
    }

    /**
     * 修改测试项目
     *
     * @param testProject 测试项目
     * @return 结果
     */
    @Override
    public int updateTestProject(TestProject testProject) {
        return testProjectMapper.updateById(testProject);
    }

    /**
     * 批量删除测试项目
     *
     * @param projectIds 需要删除的测试项目主键
     * @return 结果
     */
    @Override
    public int deleteTestProjectByProjectIds(Long[] projectIds) {
        QueryWrapper<TestProject> queryWrapper = new QueryWrapper<>();
        queryWrapper.in("project_id", projectIds);
        return testProjectMapper.delete(queryWrapper);
    }

    /**
     * 删除测试项目信息
     *
     * @param projectId 测试项目主键
     * @return 结果
     */
    @Override
    public int deleteTestProjectByProjectId(Long projectId) {
        return testProjectMapper.deleteById(projectId);
    }
}