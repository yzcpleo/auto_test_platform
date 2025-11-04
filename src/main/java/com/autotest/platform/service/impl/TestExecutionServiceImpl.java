package com.autotest.platform.service.impl;

import com.autotest.platform.domain.testcase.TestExecution;
import com.autotest.platform.domain.testcase.TestExecutionCase;
import com.autotest.platform.domain.testcase.TestCase;
import com.autotest.platform.execution.parallel.ParallelExecutionController;
import com.autotest.platform.mapper.TestExecutionCaseMapper;
import com.autotest.platform.mapper.TestExecutionMapper;
import com.autotest.platform.service.ITestCaseService;
import com.autotest.platform.service.ITestExecutionService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

/**
 * 测试执行Service业务层处理
 *
 * @author autotest
 * @date 2024-01-01
 */
@Slf4j
@Service
public class TestExecutionServiceImpl extends ServiceImpl<TestExecutionMapper, TestExecution> implements ITestExecutionService {

    @Autowired
    private TestExecutionMapper testExecutionMapper;

    @Autowired
    private TestExecutionCaseMapper testExecutionCaseMapper;

    @Autowired
    private ITestCaseService testCaseService;

    @Autowired
    private ParallelExecutionController parallelExecutionController;

    @Autowired
    private ObjectMapper objectMapper;

    /**
     * 查询测试执行
     *
     * @param executionId 测试执行主键
     * @return 测试执行
     */
    @Override
    public TestExecution selectTestExecutionByExecutionId(Long executionId) {
        return testExecutionMapper.selectTestExecutionByExecutionId(executionId);
    }

    /**
     * 查询测试执行列表
     *
     * @param testExecution 测试执行
     * @return 测试执行
     */
    @Override
    public List<TestExecution> selectTestExecutionList(TestExecution testExecution) {
        return testExecutionMapper.selectTestExecutionList(testExecution);
    }

    /**
     * 新增测试执行
     *
     * @param testExecution 测试执行
     * @return 结果
     */
    @Override
    @Transactional
    public int insertTestExecution(TestExecution testExecution) {
        testExecution.setCreateTime(new Date());
        return testExecutionMapper.insertTestExecution(testExecution);
    }

    /**
     * 修改测试执行
     *
     * @param testExecution 测试执行
     * @return 结果
     */
    @Override
    @Transactional
    public int updateTestExecution(TestExecution testExecution) {
        testExecution.setUpdateTime(new Date());
        return testExecutionMapper.updateTestExecution(testExecution);
    }

    /**
     * 批量删除测试执行
     *
     * @param executionIds 需要删除的测试执行主键
     * @return 结果
     */
    @Override
    @Transactional
    public int deleteTestExecutionByExecutionIds(Long[] executionIds) {
        // 删除执行用例详情
        for (Long executionId : executionIds) {
            testExecutionCaseMapper.deleteByExecutionId(executionId);
        }
        return testExecutionMapper.deleteTestExecutionByExecutionIds(executionIds);
    }

    /**
     * 删除测试执行信息
     *
     * @param executionId 测试执行主键
     * @return 结果
     */
    @Override
    @Transactional
    public int deleteTestExecutionByExecutionId(Long executionId) {
        // 删除执行用例详情
        testExecutionCaseMapper.deleteByExecutionId(executionId);
        return testExecutionMapper.deleteTestExecutionByExecutionId(executionId);
    }

    /**
     * 创建执行记录
     */
    @Override
    @Transactional
    public TestExecution createExecution(Long projectId, String executionName, List<Long> caseIds,
                                       String executionConfig, Long executorId) {
        try {
            // 创建执行记录
            TestExecution execution = new TestExecution();
            execution.setExecutionCode(generateExecutionCode());
            execution.setProjectId(projectId);
            execution.setExecutionName(executionName);
            execution.setExecutionType("MANUAL");
            execution.setStatus("PENDING");
            execution.setPriority("MEDIUM");
            execution.setTotalCases(caseIds.size());
            execution.setSuccessCases(0);
            execution.setFailedCases(0);
            execution.setSkippedCases(0);
            execution.setProgress(0);
            execution.setExecutionConfig(executionConfig);
            execution.setExecutorId(executorId);
            execution.setCreateTime(new Date());

            // 插入执行记录
            testExecutionMapper.insertTestExecution(execution);

            // 创建执行用例详情记录
            List<TestExecutionCase> executionCases = new ArrayList<>();
            for (Long caseId : caseIds) {
                TestExecutionCase executionCase = new TestExecutionCase();
                executionCase.setExecutionId(execution.getExecutionId());
                executionCase.setCaseId(caseId);
                executionCase.setStatus("PENDING");
                executionCase.setRetryCount(0);
                executionCase.setCreateTime(new Date());
                executionCases.add(executionCase);
            }

            // 批量插入执行用例详情
            testExecutionCaseMapper.batchInsertExecutionCase(executionCases);

            log.info("Created execution: {} with {} cases", execution.getExecutionCode(), caseIds.size());
            return execution;

        } catch (Exception e) {
            log.error("Failed to create execution", e);
            throw new RuntimeException("Failed to create execution: " + e.getMessage());
        }
    }

    /**
     * 启动测试执行
     */
    @Override
    @Transactional
    public Map<String, Object> startExecution(Long executionId) {
        Map<String, Object> result = new HashMap<>();

        try {
            TestExecution execution = testExecutionMapper.selectTestExecutionByExecutionId(executionId);
            if (execution == null) {
                result.put("success", false);
                result.put("message", "Execution not found");
                return result;
            }

            if (!"PENDING".equals(execution.getStatus())) {
                result.put("success", false);
                result.put("message", "Execution is not in pending status");
                return result;
            }

            // 更新执行状态
            execution.setStatus("RUNNING");
            execution.setActualStartTime(new Date());
            testExecutionMapper.updateTestExecution(execution);

            // 获取测试用例
            List<TestExecutionCase> executionCases = testExecutionCaseMapper.selectByExecutionId(executionId);
            List<Long> caseIds = new ArrayList<>();
            for (TestExecutionCase executionCase : executionCases) {
                caseIds.add(executionCase.getCaseId());
            }

            List<TestCase> testCases = new ArrayList<>();
            for (Long caseId : caseIds) {
                TestCase testCase = testCaseService.selectTestCaseByCaseId(caseId);
                if (testCase != null) {
                    testCases.add(testCase);
                }
            }

            // 解析执行配置
            Map<String, Object> config = parseExecutionConfig(execution.getExecutionConfig());
            Integer parallelCount = (Integer) config.getOrDefault("parallelCount", 1);

            // 初始化并行执行控制器
            if (parallelExecutionController.getRunningTaskCount() == 0) {
                parallelExecutionController.initialize(
                    Math.max(parallelCount, 2),
                    parallelCount * 2,
                    100
                );
            }

            // 启动并行执行
            ParallelExecutionController.ExecutionResult executionResult =
                parallelExecutionController.executeTestCases(execution, testCases, parallelCount, config);

            // 更新执行结果
            execution.setStatus(executionResult.isSuccess() ? "SUCCESS" : "FAILED");
            execution.setActualEndTime(new Date());
            execution.setProgress(100);
            execution.setSuccessCases(executionResult.getSuccessCases());
            execution.setFailedCases(executionResult.getFailedCases());
            execution.setSkippedCases(executionResult.getSkippedCases());

            if (!executionResult.isSuccess()) {
                execution.setErrorMessage(executionResult.getErrorMessage());
            }

            testExecutionMapper.updateTestExecution(execution);

            // 更新执行用例详情
            updateExecutionCaseResults(executionId, executionResult);

            result.put("success", true);
            result.put("message", "Execution completed successfully");
            result.put("executionId", executionId);
            result.put("result", executionResult);

            log.info("Execution completed: {} - success: {}, duration: {}ms",
                    execution.getExecutionCode(), executionResult.isSuccess(), executionResult.getDuration());

        } catch (Exception e) {
            log.error("Failed to start execution: " + executionId, e);

            // 更新执行状态为失败
            TestExecution execution = testExecutionMapper.selectTestExecutionByExecutionId(executionId);
            if (execution != null) {
                execution.setStatus("FAILED");
                execution.setActualEndTime(new Date());
                execution.setErrorMessage("Execution failed: " + e.getMessage());
                testExecutionMapper.updateTestExecution(execution);
            }

            result.put("success", false);
            result.put("message", "Execution failed: " + e.getMessage());
        }

        return result;
    }

    /**
     * 停止测试执行
     */
    @Override
    @Transactional
    public Map<String, Object> stopExecution(Long executionId) {
        Map<String, Object> result = new HashMap<>();

        try {
            TestExecution execution = testExecutionMapper.selectTestExecutionByExecutionId(executionId);
            if (execution == null) {
                result.put("success", false);
                result.put("message", "Execution not found");
                return result;
            }

            if (!"RUNNING".equals(execution.getStatus())) {
                result.put("success", false);
                result.put("message", "Execution is not running");
                return result;
            }

            // 停止并行执行
            boolean stopped = parallelExecutionController.stopExecution(execution.getExecutionCode());

            if (stopped) {
                execution.setStatus("CANCELLED");
                execution.setActualEndTime(new Date());
                execution.setErrorMessage("Execution cancelled by user");
                testExecutionMapper.updateTestExecution(execution);

                result.put("success", true);
                result.put("message", "Execution stopped successfully");
            } else {
                result.put("success", false);
                result.put("message", "Failed to stop execution");
            }

        } catch (Exception e) {
            log.error("Failed to stop execution: " + executionId, e);
            result.put("success", false);
            result.put("message", "Failed to stop execution: " + e.getMessage());
        }

        return result;
    }

    /**
     * 重新执行失败的用例
     */
    @Override
    @Transactional
    public TestExecution retryFailedCases(Long executionId) {
        try {
            TestExecution originalExecution = testExecutionMapper.selectTestExecutionByExecutionId(executionId);
            if (originalExecution == null) {
                throw new RuntimeException("Original execution not found");
            }

            // 获取失败的用例
            List<TestExecutionCase> failedCases = testExecutionCaseMapper.selectFailedCases(executionId);
            if (failedCases.isEmpty()) {
                throw new RuntimeException("No failed cases found for retry");
            }

            List<Long> caseIds = new ArrayList<>();
            for (TestExecutionCase failedCase : failedCases) {
                caseIds.add(failedCase.getCaseId());
            }

            // 创建新的执行记录
            String retryName = originalExecution.getExecutionName() + " (Retry)";
            TestExecution retryExecution = createExecution(
                originalExecution.getProjectId(),
                retryName,
                caseIds,
                originalExecution.getExecutionConfig(),
                originalExecution.getExecutorId()
            );

            retryExecution.setExecutionType("RETRY");
            testExecutionMapper.updateTestExecution(retryExecution);

            log.info("Created retry execution: {} for {} failed cases",
                    retryExecution.getExecutionCode(), caseIds.size());

            return retryExecution;

        } catch (Exception e) {
            log.error("Failed to create retry execution", e);
            throw new RuntimeException("Failed to create retry execution: " + e.getMessage());
        }
    }

    /**
     * 查询执行详情
     */
    @Override
    public TestExecution selectExecutionDetail(Long executionId) {
        TestExecution execution = testExecutionMapper.selectTestExecutionByExecutionId(executionId);
        if (execution != null) {
            // 获取执行用例详情
            List<TestExecutionCase> executionCases = testExecutionCaseMapper.selectByExecutionId(executionId);
            execution.setExecutionCases(executionCases);
        }
        return execution;
    }

    /**
     * 获取执行统计信息
     */
    @Override
    public Map<String, Object> getExecutionStatistics(Long projectId, String timeRange) {
        return testExecutionMapper.statisticsByProject(projectId, timeRange);
    }

    /**
     * 获取执行历史趋势
     */
    @Override
    public List<Map<String, Object>> getExecutionTrend(Long projectId, Integer days) {
        return testExecutionMapper.selectExecutionHistory(projectId, days);
    }

    /**
     * 生成执行报告
     */
    @Override
    public String generateExecutionReport(Long executionId) {
        try {
            TestExecution execution = selectExecutionDetail(executionId);
            if (execution == null) {
                throw new RuntimeException("Execution not found");
            }

            // 生成HTML报告 (这里简化实现)
            String reportPath = "/reports/execution_" + execution.getExecutionCode() + ".html";

            // TODO: 实现详细的HTML报告生成逻辑

            log.info("Generated execution report: {} for execution: {}", reportPath, execution.getExecutionCode());
            return reportPath;

        } catch (Exception e) {
            log.error("Failed to generate execution report: " + executionId, e);
            throw new RuntimeException("Failed to generate report: " + e.getMessage());
        }
    }

    /**
     * 清理过期的执行记录
     */
    @Override
    @Transactional
    public int cleanExpiredExecutions(Long projectId, Integer days) {
        try {
            // TODO: 实现清理逻辑
            log.info("Cleaned expired executions for project: {}, older than {} days", projectId, days);
            return 0;
        } catch (Exception e) {
            log.error("Failed to clean expired executions", e);
            return 0;
        }
    }

    /**
     * 生成执行编号
     */
    private String generateExecutionCode() {
        return "EXEC-" + System.currentTimeMillis();
    }

    /**
     * 解析执行配置
     */
    private Map<String, Object> parseExecutionConfig(String executionConfig) {
        try {
            if (executionConfig != null && !executionConfig.trim().isEmpty()) {
                return objectMapper.readValue(executionConfig, Map.class);
            }
        } catch (Exception e) {
            log.warn("Failed to parse execution config: " + executionConfig, e);
        }

        // 返回默认配置
        Map<String, Object> defaultConfig = new HashMap<>();
        defaultConfig.put("parallelCount", 1);
        defaultConfig.put("timeoutMinutes", 60);
        defaultConfig.put("continueOnFailure", true);
        defaultConfig.put("generateDetailedReport", true);
        defaultConfig.put("retryCount", 0);
        return defaultConfig;
    }

    /**
     * 更新执行用例结果
     */
    private void updateExecutionCaseResults(Long executionId,
                                          ParallelExecutionController.ExecutionResult executionResult) {
        try {
            List<TestExecutionCase> executionCases = testExecutionCaseMapper.selectByExecutionId(executionId);

            for (int i = 0; i < executionCases.size() && i < executionResult.getCaseResults().size(); i++) {
                TestExecutionCase executionCase = executionCases.get(i);
                ParallelExecutionController.TestCaseResult caseResult = executionResult.getCaseResults().get(i);

                executionCase.setStatus(caseResult.isSuccess() ? "SUCCESS" : "FAILED");
                executionCase.setStartTime(new Date(caseResult.getStartTime()));
                executionCase.setEndTime(new Date(caseResult.getEndTime()));
                executionCase.setDuration(caseResult.getDuration());
                executionCase.setResult(caseResult.getOutput());
                executionCase.setErrorMessage(caseResult.getErrorMessage());
                executionCase.setScreenshotPath(caseResult.getScreenshotPath());
                executionCase.setLogPath(caseResult.getLogPath());

                // 转换步骤结果
                if (caseResult.getStepResults() != null) {
                    executionCase.setStepResults(objectMapper.writeValueAsString(caseResult.getStepResults()));
                }

                testExecutionCaseMapper.updateTestExecutionCase(executionCase);
            }
        } catch (Exception e) {
            log.error("Failed to update execution case results", e);
        }
    }
}