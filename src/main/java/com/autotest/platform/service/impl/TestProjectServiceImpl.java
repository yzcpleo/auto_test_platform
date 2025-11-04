package com.autotest.platform.service.impl;

import com.autotest.platform.common.utils.DateUtils;
import com.autotest.platform.common.utils.SecurityUtils;
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
        return testProjectMapper.selectTestProjectByProjectId(projectId);
    }

    /**
     * 查询测试项目列表
     *
     * @param testProject 测试项目
     * @return 测试项目
     */
    @Override
    public List<TestProject> selectTestProjectList(TestProject testProject) {
        return testProjectMapper.selectTestProjectList(testProject);
    }

    /**
     * 根据用户ID查询项目列表
     *
     * @param userId 用户ID
     * @return 项目列表
     */
    @Override
    public List<TestProject> selectProjectsByUserId(Long userId) {
        return testProjectMapper.selectProjectsByUserId(userId);
    }

    /**
     * 新增测试项目
     *
     * @param testProject 测试项目
     * @return 结果
     */
    @Override
    public int insertTestProject(TestProject testProject) {
        testProject.setCreateTime(DateUtils.getNowDate());
        testProject.setCreateBy(SecurityUtils.getUsername());
        testProject.setProjectCode(generateProjectCode());
        testProject.setOwnerId(SecurityUtils.getUserId());
        testProject.setDelFlag("0");
        testProject.setStatus("0");
        return testProjectMapper.insertTestProject(testProject);
    }

    /**
     * 修改测试项目
     *
     * @param testProject 测试项目
     * @return 结果
     */
    @Override
    public int updateTestProject(TestProject testProject) {
        testProject.setUpdateTime(DateUtils.getNowDate());
        testProject.setUpdateBy(SecurityUtils.getUsername());
        return testProjectMapper.updateTestProject(testProject);
    }

    /**
     * 批量删除测试项目
     *
     * @param projectIds 需要删除的测试项目主键
     * @return 结果
     */
    @Override
    public int deleteTestProjectByProjectIds(Long[] projectIds) {
        return testProjectMapper.deleteTestProjectByProjectIds(projectIds);
    }

    /**
     * 删除测试项目信息
     *
     * @param projectId 测试项目主键
     * @return 结果
     */
    @Override
    public int deleteTestProjectByProjectId(Long projectId) {
        return testProjectMapper.deleteTestProjectByProjectId(projectId);
    }

    /**
     * 生成项目编码
     *
     * @return 项目编码
     */
    @Override
    public String generateProjectCode() {
        // 获取当天项目数量
        QueryWrapper<TestProject> wrapper = new QueryWrapper<>();
        wrapper.like("project_code", DateUtils.dateTimeNow("yyyyMMdd"))
               .eq("del_flag", "0");
        int count = Math.toIntExact(count(wrapper));

        // 生成项目编码：PRJ + 日期 + 3位序号
        return "PRJ" + DateUtils.dateTimeNow("yyyyMMdd") + String.format("%03d", count + 1);
    }

    /**
     * 检查项目名称是否唯一
     *
     * @param testProject 测试项目信息
     * @return 结果
     */
    @Override
    public boolean checkProjectNameUnique(TestProject testProject) {
        Long projectId = testProject.getProjectId() == null ? -1L : testProject.getProjectId();
        TestProject info = testProjectMapper.selectOne(new QueryWrapper<TestProject>()
                .eq("project_name", testProject.getProjectName())
                .eq("del_flag", "0"));
        if (info != null && info.getProjectId().longValue() != projectId.longValue()) {
            return false;
        }
        return true;
    }

    /**
     * 获取项目统计信息
     *
     * @param projectId 项目ID
     * @return 统计信息
     */
    @Override
    public TestProject getProjectStatistics(Long projectId) {
        return testProjectMapper.getProjectStatistics(projectId);
    }
}