package com.autotest.platform.service.impl;

import com.autotest.platform.common.utils.DateUtils;
import com.autotest.platform.common.utils.SecurityUtils;
import com.autotest.platform.domain.testcase.TestCase;
import com.autotest.platform.domain.testcase.TestCaseVersion;
import com.autotest.platform.mapper.TestCaseMapper;
import com.autotest.platform.mapper.TestCaseVersionMapper;
import com.autotest.platform.service.ITestCaseService;
import com.autotest.platform.service.ITestCaseVersionService;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * 测试用例版本历史Service业务层处理
 *
 * @author autotest
 */
@Service
public class TestCaseVersionServiceImpl extends ServiceImpl<TestCaseVersionMapper, TestCaseVersion> implements ITestCaseVersionService {

    @Autowired
    private TestCaseVersionMapper testCaseVersionMapper;

    @Autowired
    private ITestCaseService testCaseService;

    @Autowired
    private TestCaseMapper testCaseMapper;

    private static final ObjectMapper objectMapper = new ObjectMapper();

    /**
     * 查询测试用例版本历史
     *
     * @param versionId 测试用例版本历史主键
     * @return 测试用例版本历史
     */
    @Override
    public TestCaseVersion selectTestCaseVersionByVersionId(Long versionId) {
        return testCaseVersionMapper.selectTestCaseVersionByVersionId(versionId);
    }

    /**
     * 查询测试用例版本历史列表
     *
     * @param testCaseVersion 测试用例版本历史
     * @return 测试用例版本历史
     */
    @Override
    public List<TestCaseVersion> selectTestCaseVersionList(TestCaseVersion testCaseVersion) {
        return testCaseVersionMapper.selectTestCaseVersionList(testCaseVersion);
    }

    /**
     * 根据用例ID查询版本历史列表
     *
     * @param caseId 用例ID
     * @return 版本历史列表
     */
    @Override
    public List<TestCaseVersion> selectVersionsByCaseId(Long caseId) {
        List<TestCaseVersion> versions = testCaseVersionMapper.selectVersionsByCaseId(caseId);
        // 解析测试步骤JSON
        for (TestCaseVersion version : versions) {
            if (version.getTestSteps() != null) {
                try {
                    List<TestCase.TestStep> steps = objectMapper.readValue(version.getTestSteps(),
                            objectMapper.getTypeFactory().constructCollectionType(List.class, TestCase.TestStep.class));
                    version.setTestStepList(steps);
                } catch (JsonProcessingException e) {
                    e.printStackTrace();
                }
            }
        }
        return versions;
    }

    /**
     * 获取用例的最新版本
     *
     * @param caseId 用例ID
     * @return 最新版本
     */
    @Override
    public TestCaseVersion getLatestVersion(Long caseId) {
        TestCaseVersion latestVersion = testCaseVersionMapper.selectLatestVersionByCaseId(caseId);
        if (latestVersion != null && latestVersion.getTestSteps() != null) {
            try {
                List<TestCase.TestStep> steps = objectMapper.readValue(latestVersion.getTestSteps(),
                        objectMapper.getTypeFactory().constructCollectionType(List.class, TestCase.TestStep.class));
                latestVersion.setTestStepList(steps);
            } catch (JsonProcessingException e) {
                e.printStackTrace();
            }
        }
        return latestVersion;
    }

    /**
     * 创建新版本（用例修改时调用）
     *
     * @param caseId 用例ID
     * @param oldVersion 旧版本信息
     * @param newVersion 新版本信息
     * @param changeLog 变更日志
     * @return 版本ID
     */
    @Override
    @Transactional
    public Long createNewVersion(Long caseId, TestCaseVersion oldVersion, TestCaseVersion newVersion, String changeLog) {
        TestCaseVersion version = new TestCaseVersion();
        version.setCaseId(caseId);
        version.setCaseTitle(newVersion.getCaseTitle());
        version.setVersionNumber(oldVersion != null ? oldVersion.getVersionNumber() + 1 : 1);
        version.setTestSteps(newVersion.getTestSteps());
        version.setExpectedResult(newVersion.getExpectedResult());
        version.setChangeLog(changeLog);
        version.setCreateTime(DateUtils.getNowDate());
        version.setCreateBy(SecurityUtils.getUsername());

        testCaseVersionMapper.insertTestCaseVersion(version);

        // 限制版本数量，清理旧版本
        cleanOldVersions(caseId, 10);

        return version.getVersionId();
    }

    /**
     * 比较两个版本的差异
     *
     * @param versionId1 版本1 ID
     * @param versionId2 版本2 ID
     * @return 差异信息
     */
    @Override
    public TestCaseVersion.VersionDiff compareVersions(Long versionId1, Long versionId2) {
        TestCaseVersion v1 = selectTestCaseVersionByVersionId(versionId1);
        TestCaseVersion v2 = selectTestCaseVersionByVersionId(versionId2);

        if (v1 == null || v2 == null) {
            return null;
        }

        TestCaseVersion.VersionDiff diff = new TestCaseVersion.VersionDiff();

        // 比较期望结果
        if (!v1.getExpectedResult().equals(v2.getExpectedResult())) {
            diff.setExpectedResultChange("期望结果已修改");
        }

        // 比较测试步骤
        List<TestCase.TestStep> steps1 = v1.getTestStepList();
        List<TestCase.TestStep> steps2 = v2.getTestStepList();

        if (steps1 != null && steps2 != null) {
            // 简单的步骤差异比较
            if (steps1.size() != steps2.size()) {
                diff.setExpectedResultChange("测试步骤数量已修改：" +
                        (steps1.size() + " -> " + steps2.size()));
            }
        }

        return diff;
    }

    /**
     * 回滚到指定版本
     *
     * @param caseId 用例ID
     * @param versionNumber 目标版本号
     * @return 结果
     */
    @Override
    @Transactional
    public boolean rollbackToVersion(Long caseId, Integer versionNumber) {
        // 查询目标版本
        TestCaseVersion version = testCaseVersionMapper.selectOne(new QueryWrapper<TestCaseVersion>()
                .eq("case_id", caseId)
                .eq("version_number", versionNumber));

        if (version == null) {
            return false;
        }

        // 更新当前用例信息
        TestCase testCase = new TestCase();
        testCase.setCaseId(caseId);
        testCase.setCaseTitle(version.getCaseTitle());
        testCase.setTestSteps(version.getTestSteps());
        testCase.setExpectedResult(version.getExpectedResult());
        testCase.setUpdateBy(SecurityUtils.getUsername());
        testCase.setUpdateTime(DateUtils.getNowDate());

        int result = testCaseMapper.updateTestCase(testCase);

        // 创建回滚记录的新版本
        if (result > 0) {
            TestCaseVersion currentVersion = getLatestVersion(caseId);
            createNewVersion(caseId, currentVersion, version, "回滚到版本 " + versionNumber);
        }

        return result > 0;
    }

    /**
     * 清理旧版本
     *
     * @param caseId 用例ID
     * @param keepCount 保留版本数
     * @return 清理的版本数
     */
    @Override
    @Transactional
    public int cleanOldVersions(Long caseId, Integer keepCount) {
        return testCaseVersionMapper.cleanOldVersions(caseId, keepCount);
    }

    /**
     * 删除测试用例版本历史信息
     *
     * @param versionId 测试用例版本历史主键
     * @return 结果
     */
    @Override
    public int deleteTestCaseVersionByVersionId(Long versionId) {
        return testCaseVersionMapper.deleteTestCaseVersionByVersionId(versionId);
    }
}