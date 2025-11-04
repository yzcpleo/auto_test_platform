package com.autotest.platform.mapper;

import com.autotest.platform.domain.testcase.TestCase;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 测试用例Mapper接口
 *
 * @author autotest
 * @date 2024-01-01
 */
public interface TestCaseMapper extends BaseMapper<TestCase> {

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
     * 根据分类ID查询用例列表
     *
     * @param categoryId 分类ID
     * @return 用例列表
     */
    List<TestCase> selectCasesByCategoryId(@Param("categoryId") Long categoryId);

    /**
     * 根据项目ID查询用例列表
     *
     * @param projectId 项目ID
     * @return 用例列表
     */
    List<TestCase> selectCasesByProjectId(@Param("projectId") Long projectId);

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
     * 删除测试用例
     *
     * @param caseId 测试用例主键
     * @return 结果
     */
    int deleteTestCaseByCaseId(Long caseId);

    /**
     * 批量删除测试用例
     *
     * @param caseIds 需要删除的数据主键集合
     * @return 结果
     */
    int deleteTestCaseByCaseIds(Long[] caseIds);

    /**
     * 检查用例编码是否存在
     *
     * @param caseCode 用例编码
     * @param projectId 项目ID
     * @return 数量
     */
    int checkCaseCodeExists(@Param("caseCode") String caseCode, @Param("projectId") Long projectId);

    /**
     * 生成用例编码
     *
     * @param projectId 项目ID
     * @param categoryId 分类ID
     * @return 用例编码
     */
    String generateCaseCode(@Param("projectId") Long projectId, @Param("categoryId") Long categoryId);

    /**
     * 获取用例统计信息
     *
     * @param projectId 项目ID
     * @return 统计信息
     */
    List<TestCase> getCaseStatistics(@Param("projectId") Long projectId);

    /**
     * 更新用例状态
     *
     * @param caseId 用例ID
     * @param status 状态
     * @return 结果
     */
    int updateCaseStatus(@Param("caseId") Long caseId, @Param("status") String status);
}