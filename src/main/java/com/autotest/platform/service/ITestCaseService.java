package com.autotest.platform.service;

import com.autotest.platform.domain.testcase.TestCase;
import com.baomidou.mybatisplus.extension.service.IService;

import java.util.List;

/**
 * 测试用例Service接口
 *
 * @author autotest
 * @date 2024-01-01
 */
public interface ITestCaseService extends IService<TestCase> {

    /**
     * 查询测试用例
     *
     * @param caseId 测试用例主键
     * @return 测试用例
     */
    TestCase selectTestCaseByCaseId(Long caseId);

    /**
     * 查询测试用例列表
     *
     * @param testCase 测试用例
     * @return 测试用例集合
     */
    List<TestCase> selectTestCaseList(TestCase testCase);

    /**
     * 新增测试用例
     *
     * @param testCase 测试用例
     * @return 结果
     */
    int insertTestCase(TestCase testCase);

    /**
     * 修改测试用例
     *
     * @param testCase 测试用例
     * @return 结果
     */
    int updateTestCase(TestCase testCase);

    /**
     * 批量删除测试用例
     *
     * @param caseIds 需要删除的测试用例主键集合
     * @return 结果
     */
    int deleteTestCaseByCaseIds(Long[] caseIds);

    /**
     * 删除测试用例信息
     *
     * @param caseId 测试用例主键
     * @return 结果
     */
    int deleteTestCaseByCaseId(Long caseId);

    /**
     * 生成用例编码
     *
     * @param projectId 项目ID
     * @param categoryId 分类ID
     * @return 用例编码
     */
    String generateCaseCode(Long projectId, Long categoryId);

    /**
     * 检查用例编码是否唯一
     *
     * @param testCase 测试用例信息
     * @return 结果
     */
    boolean checkCaseCodeUnique(TestCase testCase);

    /**
     * 更新用例状态
     *
     * @param caseId 用例ID
     * @param status 状态
     * @return 结果
     */
    int updateCaseStatus(Long caseId, String status);

    /**
     * 批量更新用例状态
     *
     * @param caseIds 用例ID数组
     * @param status 状态
     * @return 结果
     */
    int batchUpdateCaseStatus(Long[] caseIds, String status);

    /**
     * 复制测试用例
     *
     * @param caseId 原用例ID
     * @param categoryId 目标分类ID
     * @param caseTitle 新用例标题
     * @return 结果
     */
    int copyTestCase(Long caseId, Long categoryId, String caseTitle);

    /**
     * 移动测试用例到其他分类
     *
     * @param caseIds 用例ID数组
     * @param targetCategoryId 目标分类ID
     * @return 结果
     */
    int moveTestCases(Long[] caseIds, Long targetCategoryId);
}