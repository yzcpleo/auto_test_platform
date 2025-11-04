package com.autotest.platform.mapper;

import com.autotest.platform.domain.testcase.TestCaseVersion;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 测试用例版本历史Mapper接口
 *
 * @author autotest
 * @date 2024-01-01
 */
public interface TestCaseVersionMapper extends BaseMapper<TestCaseVersion> {

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
    List<TestCaseVersion> selectVersionsByCaseId(@Param("caseId") Long caseId);

    /**
     * 查询用例的最新版本
     *
     * @param caseId 用例ID
     * @return 最新版本
     */
    TestCaseVersion selectLatestVersionByCaseId(@Param("caseId") Long caseId);

    /**
     * 新增测试用例版本历史
     *
     * @param testCaseVersion 测试用例版本历史
     * @return 结果
     */
    int insertTestCaseVersion(TestCaseVersion testCaseVersion);

    /**
     * 修改测试用例版本历史
     *
     * @param testCaseVersion 测试用例版本历史
     * @return 结果
     */
    int updateTestCaseVersion(TestCaseVersion testCaseVersion);

    /**
     * 删除测试用例版本历史
     *
     * @param versionId 测试用例版本历史主键
     * @return 结果
     */
    int deleteTestCaseVersionByVersionId(Long versionId);

    /**
     * 批量删除测试用例版本历史
     *
     * @param versionIds 需要删除的数据主键集合
     * @return 结果
     */
    int deleteTestCaseVersionByVersionIds(Long[] versionIds);

    /**
     * 删除指定用例的所有版本历史
     *
     * @param caseId 用例ID
     * @return 结果
     */
    int deleteVersionsByCaseId(@Param("caseId") Long caseId);

    /**
     * 清理旧版本（保留最近N个版本）
     *
     * @param caseId 用例ID
     * @param keepCount 保留版本数
     * @return 删除数量
     */
    int cleanOldVersions(@Param("caseId") Long caseId, @Param("keepCount") Integer keepCount);
}