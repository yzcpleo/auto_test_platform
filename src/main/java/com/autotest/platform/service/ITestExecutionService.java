package com.autotest.platform.service;

import com.autotest.platform.domain.testcase.TestExecution;
import com.autotest.platform.domain.testcase.TestExecutionCase;
import com.baomidou.mybatisplus.extension.service.IService;

import java.util.List;
import java.util.Map;

/**
 * 测试执行Service接口
 *
 * @author autotest
 * @date 2024-01-01
 */
public interface ITestExecutionService extends IService<TestExecution> {

    /**
     * 查询测试执行
     *
     * @param executionId 测试执行主键
     * @return 测试执行
     */
    TestExecution selectTestExecutionByExecutionId(Long executionId);

    /**
     * 查询测试执行列表
     *
     * @param testExecution 测试执行
     * @return 测试执行集合
     */
    List<TestExecution> selectTestExecutionList(TestExecution testExecution);

    /**
     * 新增测试执行
     *
     * @param testExecution 测试执行
     * @return 结果
     */
    int insertTestExecution(TestExecution testExecution);

    /**
     * 修改测试执行
     *
     * @param testExecution 测试执行
     * @return 结果
     */
    int updateTestExecution(TestExecution testExecution);

    /**
     * 批量删除测试执行
     *
     * @param executionIds 需要删除的测试执行主键集合
     * @return 结果
     */
    int deleteTestExecutionByExecutionIds(Long[] executionIds);

    /**
     * 删除测试执行信息
     *
     * @param executionId 测试执行主键
     * @return 结果
     */
    int deleteTestExecutionByExecutionId(Long executionId);

    /**
     * 创建执行记录
     *
     * @param projectId 项目ID
     * @param executionName 执行名称
     * @param caseIds 用例ID列表
     * @param executionConfig 执行配置
     * @param executorId 执行人ID
     * @return 执行记录
     */
    TestExecution createExecution(Long projectId, String executionName, List<Long> caseIds,
                                 String executionConfig, Long executorId);

    /**
     * 启动测试执行
     *
     * @param executionId 执行ID
     * @return 执行结果
     */
    Map<String, Object> startExecution(Long executionId);

    /**
     * 停止测试执行
     *
     * @param executionId 执行ID
     * @return 执行结果
     */
    Map<String, Object> stopExecution(Long executionId);

    /**
     * 重新执行失败的用例
     *
     * @param executionId 原执行ID
     * @return 新执行记录
     */
    TestExecution retryFailedCases(Long executionId);

    /**
     * 查询执行详情(包含用例列表)
     *
     * @param executionId 执行ID
     * @return 执行详情
     */
    TestExecution selectExecutionDetail(Long executionId);

    /**
     * 获取执行统计信息
     *
     * @param projectId 项目ID
     * @param timeRange 时间范围
     * @return 统计信息
     */
    Map<String, Object> getExecutionStatistics(Long projectId, String timeRange);

    /**
     * 获取执行历史趋势
     *
     * @param projectId 项目ID
     * @param days 天数
     * @return 历史趋势
     */
    List<Map<String, Object>> getExecutionTrend(Long projectId, Integer days);

    /**
     * 生成执行报告
     *
     * @param executionId 执行ID
     * @return 报告路径
     */
    String generateExecutionReport(Long executionId);

    /**
     * 清理过期的执行记录
     *
     * @param projectId 项目ID
     * @param days 保留天数
     * @return 清理数量
     */
    int cleanExpiredExecutions(Long projectId, Integer days);
}