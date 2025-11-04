package com.autotest.platform.mapper;

import com.autotest.platform.domain.testcase.TestExecutionCase;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Map;

/**
 * 测试执行用例详情Mapper接口
 *
 * @author autotest
 * @date 2024-01-01
 */
public interface TestExecutionCaseMapper {

    /**
     * 查询测试执行用例详情
     *
     * @param executionCaseId 测试执行用例详情主键
     * @return 测试执行用例详情
     */
    public TestExecutionCase selectTestExecutionCaseByExecutionCaseId(Long executionCaseId);

    /**
     * 查询测试执行用例详情列表
     *
     * @param testExecutionCase 测试执行用例详情
     * @return 测试执行用例详情集合
     */
    public List<TestExecutionCase> selectTestExecutionCaseList(TestExecutionCase testExecutionCase);

    /**
     * 新增测试执行用例详情
     *
     * @param testExecutionCase 测试执行用例详情
     * @return 结果
     */
    public int insertTestExecutionCase(TestExecutionCase testExecutionCase);

    /**
     * 修改测试执行用例详情
     *
     * @param testExecutionCase 测试执行用例详情
     * @return 结果
     */
    public int updateTestExecutionCase(TestExecutionCase testExecutionCase);

    /**
     * 删除测试执行用例详情
     *
     * @param executionCaseId 测试执行用例详情主键
     * @return 结果
     */
    public int deleteTestExecutionCaseByExecutionCaseId(Long executionCaseId);

    /**
     * 批量删除测试执行用例详情
     *
     * @param executionCaseIds 需要删除的数据主键集合
     * @return 结果
     */
    public int deleteTestExecutionCaseByExecutionCaseIds(Long[] executionCaseIds);

    /**
     * 根据执行ID查询用例详情列表
     *
     * @param executionId 执行ID
     * @return 用例详情列表
     */
    public List<TestExecutionCase> selectByExecutionId(Long executionId);

    /**
     * 根据用例ID查询执行记录
     *
     * @param caseId 用例ID
     * @param limit 限制数量
     * @return 执行记录列表
     */
    public List<TestExecutionCase> selectByCaseId(@Param("caseId") Long caseId, @Param("limit") Integer limit);

    /**
     * 批量插入执行用例详情
     *
     * @param executionCases 执行用例详情列表
     * @return 结果
     */
    public int batchInsertExecutionCase(List<TestExecutionCase> executionCases);

    /**
     * 更新用例执行状态
     *
     * @param executionCaseId 执行用例ID
     * @param status 状态
     * @param result 结果
     * @param errorMessage 错误信息
     * @return 结果
     */
    public int updateCaseStatus(@Param("executionCaseId") Long executionCaseId,
                               @Param("status") String status,
                               @Param("result") String result,
                               @Param("errorMessage") String errorMessage);

    /**
     * 统计执行用例状态分布
     *
     * @param executionId 执行ID
     * @return 状态统计
     */
    public List<Map<String, Object>> statisticsCaseStatus(Long executionId);

    /**
     * 查询执行失败的用例
     *
     * @param executionId 执行ID
     * @return 失败用例列表
     */
    public List<TestExecutionCase> selectFailedCases(Long executionId);

    /**
     * 删除执行ID下的所有用例详情
     *
     * @param executionId 执行ID
     * @return 结果
     */
    public int deleteByExecutionId(Long executionId);
}