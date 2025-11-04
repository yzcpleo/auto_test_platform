package com.autotest.platform.controller;

import com.autotest.platform.common.core.controller.BaseController;
import com.autotest.platform.common.core.domain.AjaxResult;
import com.autotest.platform.common.core.page.TableDataInfo;
import com.autotest.platform.domain.testcase.TestExecution;
import com.autotest.platform.execution.monitor.ExecutionMonitor;
import com.autotest.platform.service.ITestExecutionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * 测试执行Controller
 *
 * @author autotest
 * @date 2024-01-01
 */
@RestController
@RequestMapping("/test/execution")
public class TestExecutionController extends BaseController {

    @Autowired
    private ITestExecutionService testExecutionService;

    @Autowired
    private ExecutionMonitor executionMonitor;

    /**
     * 查询测试执行列表
     */
    @PreAuthorize("@ss.hasPermi('test:execution:list')")
    @GetMapping("/list")
    public TableDataInfo list(TestExecution testExecution) {
        startPage();
        List<TestExecution> list = testExecutionService.selectTestExecutionList(testExecution);
        return getDataTable(list);
    }

    /**
     * 获取测试执行详细信息
     */
    @PreAuthorize("@ss.hasPermi('test:execution:query')")
    @GetMapping(value = "/{executionId}")
    public AjaxResult getInfo(@PathVariable("executionId") Long executionId) {
        TestExecution execution = testExecutionService.selectExecutionDetail(executionId);
        return success(execution);
    }

    /**
     * 创建测试执行
     */
    @PreAuthorize("@ss.hasPermi('test:execution:add')")
    @PostMapping
    public AjaxResult add(@RequestBody Map<String, Object> params) {
        try {
            Long projectId = Long.valueOf(params.get("projectId").toString());
            String executionName = params.get("executionName").toString();
            @SuppressWarnings("unchecked")
            List<Long> caseIds = (List<Long>) params.get("caseIds");
            String executionConfig = params.get("executionConfig") != null ?
                params.get("executionConfig").toString() : null;
            Long executorId = getUserId();

            TestExecution execution = testExecutionService.createExecution(
                projectId, executionName, caseIds, executionConfig, executorId);

            return success("Execution created successfully", execution);
        } catch (Exception e) {
            return error("Failed to create execution: " + e.getMessage());
        }
    }

    /**
     * 启动测试执行
     */
    @PreAuthorize("@ss.hasPermi('test:execution:start')")
    @PostMapping("/{executionId}/start")
    public AjaxResult startExecution(@PathVariable Long executionId) {
        Map<String, Object> result = testExecutionService.startExecution(executionId);
        if ((Boolean) result.get("success")) {
            return success(result.get("message"));
        } else {
            return error(result.get("message").toString());
        }
    }

    /**
     * 停止测试执行
     */
    @PreAuthorize("@ss.hasPermi('test:execution:stop')")
    @PostMapping("/{executionId}/stop")
    public AjaxResult stopExecution(@PathVariable Long executionId) {
        Map<String, Object> result = testExecutionService.stopExecution(executionId);
        if ((Boolean) result.get("success")) {
            return success(result.get("message"));
        } else {
            return error(result.get("message").toString());
        }
    }

    /**
     * 重新执行失败的用例
     */
    @PreAuthorize("@ss.hasPermi('test:execution:retry')")
    @PostMapping("/{executionId}/retry")
    public AjaxResult retryFailedCases(@PathVariable Long executionId) {
        try {
            TestExecution retryExecution = testExecutionService.retryFailedCases(executionId);
            return success("Retry execution created successfully", retryExecution);
        } catch (Exception e) {
            return error("Failed to create retry execution: " + e.getMessage());
        }
    }

    /**
     * 生成执行报告
     */
    @PreAuthorize("@ss.hasPermi('test:execution:report')")
    @PostMapping("/{executionId}/report")
    public AjaxResult generateReport(@PathVariable Long executionId) {
        try {
            String reportPath = testExecutionService.generateExecutionReport(executionId);
            return success("Report generated successfully", reportPath);
        } catch (Exception e) {
            return error("Failed to generate report: " + e.getMessage());
        }
    }

    /**
     * 删除测试执行
     */
    @PreAuthorize("@ss.hasPermi('test:execution:remove')")
    @DeleteMapping("/{executionIds}")
    public AjaxResult remove(@PathVariable Long[] executionIds) {
        return toAjax(testExecutionService.deleteTestExecutionByExecutionIds(executionIds));
    }

    /**
     * 获取执行统计信息
     */
    @PreAuthorize("@ss.hasPermi('test:execution:statistics')")
    @GetMapping("/statistics/{projectId}")
    public AjaxResult getStatistics(@PathVariable Long projectId,
                                   @RequestParam(required = false) String timeRange) {
        Map<String, Object> statistics = testExecutionService.getExecutionStatistics(projectId, timeRange);
        return success(statistics);
    }

    /**
     * 获取执行历史趋势
     */
    @PreAuthorize("@ss.hasPermi('test:execution:trend')")
    @GetMapping("/trend/{projectId}")
    public AjaxResult getTrend(@PathVariable Long projectId,
                              @RequestParam(defaultValue = "7") Integer days) {
        List<Map<String, Object>> trend = testExecutionService.getExecutionTrend(projectId, days);
        return success(trend);
    }

    /**
     * 获取监控状态
     */
    @PreAuthorize("@ss.hasPermi('test:execution:monitor')")
    @GetMapping("/monitor/status")
    public AjaxResult getMonitorStatus() {
        ExecutionMonitor.MonitorStatistics statistics = executionMonitor.getStatistics();
        List<ExecutionMonitor.ExecutionSession> activeSessions = executionMonitor.getActiveSessions();

        Map<String, Object> result = new java.util.HashMap<>();
        result.put("statistics", statistics);
        result.put("activeSessions", activeSessions);

        return success(result);
    }

    /**
     * 获取执行会话详情
     */
    @PreAuthorize("@ss.hasPermi('test:execution:monitor')")
    @GetMapping("/monitor/session/{executionCode}")
    public AjaxResult getExecutionSession(@PathVariable String executionCode) {
        ExecutionMonitor.ExecutionSession session = executionMonitor.getSession(executionCode);
        if (session != null) {
            return success(session);
        } else {
            return error("Execution session not found");
        }
    }

    /**
     * 停止监控
     */
    @PreAuthorize("@ss.hasPermi('test:execution:monitor')")
    @PostMapping("/monitor/stop/{executionCode}")
    public AjaxResult stopMonitoring(@PathVariable String executionCode) {
        executionMonitor.stopMonitoring(executionCode);
        return success("Monitoring stopped successfully");
    }

    /**
     * 清理过期执行记录
     */
    @PreAuthorize("@ss.hasPermi('test:execution:cleanup')")
    @PostMapping("/cleanup/{projectId}")
    public AjaxResult cleanupExpiredExecutions(@PathVariable Long projectId,
                                               @RequestParam(defaultValue = "30") Integer days) {
        int cleanedCount = testExecutionService.cleanExpiredExecutions(projectId, days);
        return success("Cleaned " + cleanedCount + " expired executions");
    }
}