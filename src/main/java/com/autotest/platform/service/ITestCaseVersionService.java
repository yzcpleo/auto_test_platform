package com.autotest.platform.service;

import com.autotest.platform.domain.testcase.TestCaseVersion;
import com.baomidou.mybatisplus.extension.service.IService;

import java.util.List;

/**
 * 测试用例版本历史Service接口
 *
 * @author autotest
 * @date 2024-01-01
 */
public interface ITestCaseVersionService extends IService<TestCaseVersion> {

    /**
     * 查询测试用例版本历史
     *
     * @param versionId 测试用例版本历史主键
     * @return 测试用例版本历史
     */
    TestCaseVersion selectTestCaseVersionByVersionId(Long versionId);

    /**
     * 查询测试用例版本历史列表
     *
     * @param testCaseVersion 测试用例版本历史
     * @return 测试用例版本历史集合
     */
    List<TestCaseVersion> selectTestCaseVersionList(TestCaseVersion testCaseVersion);

    /**
     * 根据用例ID查询版本历史列表
     *
     * @param caseId 用例ID
     * @return 版本历史列表
     */
    List<TestCaseVersion> selectVersionsByCaseId(Long caseId);

    /**
     * 获取用例的最新版本
     *
     * @param caseId 用例ID
     * @return 最新版本
     */
    TestCaseVersion getLatestVersion(Long caseId);

    /**
     * 创建新版本（用例修改时调用）
     *
     * @param caseId 用例ID
     * @param oldVersion 旧版本信息
     * @param newVersion 新版本信息
     * @param changeLog 变更日志
     * @return 版本ID
     */
    Long createNewVersion(Long caseId, TestCaseVersion oldVersion, TestCaseVersion newVersion, String changeLog);

    /**
     * 比较两个版本的差异
     *
     * @param versionId1 版本1 ID
     * @param versionId2 版本2 ID
     * @return 差异信息
     */
    TestCaseVersion.VersionDiff compareVersions(Long versionId1, Long versionId2);

    /**
     * 回滚到指定版本
     *
     * @param caseId 用例ID
     * @param versionNumber 目标版本号
     * @return 结果
     */
    boolean rollbackToVersion(Long caseId, Integer versionNumber);

    /**
     * 清理旧版本
     *
     * @param caseId 用例ID
     * @param keepCount 保留版本数
     * @return 清理的版本数
     */
    int cleanOldVersions(Long caseId, Integer keepCount);

    /**
     * 删除测试用例版本历史信息
     *
     * @param versionId 测试用例版本历史主键
     * @return 结果
     */
    int deleteTestCaseVersionByVersionId(Long versionId);
}