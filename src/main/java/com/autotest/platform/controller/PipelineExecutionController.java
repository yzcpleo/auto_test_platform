package com.autotest.platform.controller;

import com.autotest.platform.common.core.controller.BaseController;
import com.autotest.platform.common.core.domain.AjaxResult;
import com.autotest.platform.common.core.page.TableDataInfo;
import com.autotest.platform.domain.cicd.PipelineExecution;
import com.autotest.platform.service.IPipelineExecutionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * 流水线执行Controller
 *
 * @author autotest
 * @date 2024-01-01
 */
@RestController
@RequestMapping("/cicd/execution")
public class PipelineExecutionController extends BaseController {

    @Autowired
    private IPipelineExecutionService pipelineExecutionService;

    /**
     * 查询流水线执行列表
     */
    @PreAuthorize("@ss.hasPermi('cicd:execution:list')")
    @GetMapping("/list")
    public TableDataInfo list(PipelineExecution pipelineExecution) {
        startPage();
        List<PipelineExecution> list = pipelineExecutionService.selectPipelineExecutionList(pipelineExecution);
        return getDataTable(list);
    }

    /**
     * 获取流水线执行详细信息
     */
    @PreAuthorize("@ss.hasPermi('cicd:execution:query')")
    @GetMapping(value = "/{executionId}")
    public AjaxResult getInfo(@PathVariable("executionId") Long executionId) {
        return success(pipelineExecutionService.selectPipelineExecutionByExecutionId(executionId));
    }

    /**
     * 删除流水线执行记录
     */
    @PreAuthorize("@ss.hasPermi('cicd:execution:remove')")
    @DeleteMapping("/{executionIds}")
    public AjaxResult remove(@PathVariable Long[] executionIds) {
        return toAjax(pipelineExecutionService.deletePipelineExecutionByExecutionIds(executionIds));
    }

    /**
     * 停止执行
     */
    @PreAuthorize("@ss.hasPermi('cicd:execution:stop')")
    @PostMapping("/{executionId}/stop")
    public AjaxResult stopExecution(@PathVariable Long executionId) {
        try {
            boolean success = pipelineExecutionService.stopExecution(executionId);
            if (success) {
                return success("执行已停止");
            } else {
                return error("停止执行失败");
            }
        } catch (Exception e) {
            return error("停止执行失败: " + e.getMessage());
        }
    }

    /**
     * 重新执行
     */
    @PreAuthorize("@ss.hasPermi('cicd:execution:retry')")
    @PostMapping("/{executionId}/retry")
    public AjaxResult retryExecution(@PathVariable Long executionId) {
        try {
            PipelineExecution newExecution = pipelineExecutionService.retryExecution(executionId);
            return success("重新执行已启动", newExecution);
        } catch (Exception e) {
            return error("重新执行失败: " + e.getMessage());
        }
    }

    /**
     * 获取流水线的执行记录
     */
    @PreAuthorize("@ss.hasPermi('cicd:execution:query')")
    @GetMapping("/pipeline/{pipelineId}")
    public AjaxResult getExecutionsByPipeline(@PathVariable Long pipelineId,
                                              @RequestParam(required = false) String status,
                                              @RequestParam(required = false) Integer limit) {
        List<PipelineExecution> executions = pipelineExecutionService.getExecutionsByPipeline(pipelineId, status, limit);
        return success(executions);
    }

    /**
     * 获取正在运行的执行记录
     */
    @PreAuthorize("@ss.hasPermi('cicd:execution:monitor')")
    @GetMapping("/running/{projectId}")
    public AjaxResult getRunningExecutions(@PathVariable Long projectId) {
        List<PipelineExecution> executions = pipelineExecutionService.getRunningExecutions(projectId);
        return success(executions);
    }

    /**
     * 获取失败的执行记录
     */
    @PreAuthorize("@ss.hasPermi('cicd:execution:query')")
    @GetMapping("/failed/{projectId}")
    public AjaxResult getFailedExecutions(@PathVariable Long projectId,
                                         @RequestParam(defaultValue = "7") Integer days,
                                         @RequestParam(defaultValue = "20") Integer limit) {
        List<PipelineExecution> executions = pipelineExecutionService.getFailedExecutions(projectId, days, limit);
        return success(executions);
    }

    /**
     * 获取执行时间最长的记录
     */
    @PreAuthorize("@ss.hasPermi('cicd:execution:query')")
    @GetMapping("/longest/{projectId}")
    public AjaxResult getLongestExecutions(@PathVariable Long projectId,
                                           @RequestParam(defaultValue = "10") Integer limit) {
        List<PipelineExecution> executions = pipelineExecutionService.getLongestExecutions(projectId, limit);
        return success(executions);
    }

    /**
     * 获取执行步骤详情
     */
    @PreAuthorize("@ss.hasPermi('cicd:execution:query')")
    @GetMapping("/{executionId}/steps")
    public AjaxResult getExecutionStepDetails(@PathVariable Long executionId) {
        List<Map<String, Object>> steps = pipelineExecutionService.getExecutionStepDetails(executionId);
        return success(steps);
    }

    /**
     * 获取执行日志
     */
    @PreAuthorize("@ss.hasPermi('cicd:execution:query')")
    @GetMapping("/{executionId}/logs")
    public AjaxResult getExecutionLogs(@PathVariable Long executionId,
                                       @RequestParam(required = false) String stepName) {
        String logs = pipelineExecutionService.getExecutionLogs(executionId, stepName);
        return success(logs);
    }

    /**
     * 添加执行日志
     */
    @PreAuthorize("@ss.hasPermi('cicd:execution:log')")
    @PostMapping("/{executionId}/logs")
    public AjaxResult addExecutionLog(@PathVariable Long executionId,
                                      @RequestParam(required = false) String stepName,
                                      @RequestBody String logContent) {
        try {
            pipelineExecutionService.addExecutionLog(executionId, stepName, logContent);
            return success("日志添加成功");
        } catch (Exception e) {
            return error("添加日志失败: " + e.getMessage());
        }
    }

    /**
     * 更新步骤状态
     */
    @PreAuthorize("@ss.hasPermi('cicd:execution:edit')")
    @PutMapping("/{executionId}/step/{stepName}/status")
    public AjaxResult updateStepStatus(@PathVariable Long executionId,
                                       @PathVariable String stepName,
                                       @RequestBody Map<String, Object> params) {
        try {
            String status = (String) params.get("status");
            String output = (String) params.get("output");
            String errorMessage = (String) params.get("errorMessage");

            pipelineExecutionService.updateStepStatus(executionId, stepName, status, output, errorMessage);
            return success("步骤状态更新成功");
        } catch (Exception e) {
            return error("更新步骤状态失败: " + e.getMessage());
        }
    }

    /**
     * 获取执行统计信息
     */
    @PreAuthorize("@ss.hasPermi('cicd:execution:statistics')")
    @GetMapping("/statistics/{projectId}")
    public AjaxResult getExecutionStatistics(@PathVariable Long projectId,
                                            @RequestParam(required = false) String timeRange) {
        Map<String, Object> statistics = pipelineExecutionService.getExecutionStatistics(projectId, timeRange);
        return success(statistics);
    }

    /**
     * 获取成功率趋势
     */
    @PreAuthorize("@ss.hasPermi('cicd:execution:statistics')")
    @GetMapping("/success-trend/{projectId}")
    public AjaxResult getSuccessTrend(@PathVariable Long projectId,
                                      @RequestParam(required = false) String startTime,
                                      @RequestParam(required = false) String endTime) {
        List<Map<String, Object>> trend = pipelineExecutionService.getSuccessTrend(projectId, startTime, endTime);
        return success(trend);
    }

    /**
     * 获取执行时长统计
     */
    @PreAuthorize("@ss.hasPermi('cicd:execution:statistics')")
    @GetMapping("/duration-stats/{projectId}")
    public AjaxResult getExecutionDurationStats(@PathVariable Long projectId,
                                               @RequestParam(required = false) String timeRange) {
        List<Map<String, Object>> stats = pipelineExecutionService.getExecutionDurationStats(projectId, timeRange);
        return success(stats);
    }

    /**
     * 获取步骤执行统计
     */
    @PreAuthorize("@ss.hasPermi('cicd:execution:statistics')")
    @GetMapping("/step-stats/{projectId}")
    public AjaxResult getStepExecutionStats(@PathVariable Long projectId,
                                           @RequestParam(required = false) String timeRange) {
        List<Map<String, Object>> stats = pipelineExecutionService.getStepExecutionStats(projectId, timeRange);
        return success(stats);
    }

    /**
     * 获取失败原因统计
     */
    @PreAuthorize("@ss.hasPermi('cicd:execution:statistics')")
    @GetMapping("/failure-reasons/{projectId}")
    public AjaxResult getFailureReasonStats(@PathVariable Long projectId,
                                           @RequestParam(required = false) String timeRange) {
        List<Map<String, Object>> stats = pipelineExecutionService.getFailureReasonStats(projectId, timeRange);
        return success(stats);
    }

    /**
     * 获取每日执行统计
     */
    @PreAuthorize("@ss.hasPermi('cicd:execution:statistics')")
    @GetMapping("/daily-stats/{projectId}")
    public AjaxResult getDailyExecutionStats(@PathVariable Long projectId,
                                            @RequestParam(defaultValue = "7") Integer days) {
        List<Map<String, Object>> stats = pipelineExecutionService.getDailyExecutionStats(projectId, days);
        return success(stats);
    }

    /**
     * 获取执行队列
     */
    @PreAuthorize("@ss.hasPermi('cicd:execution:monitor')")
    @GetMapping("/queue/{projectId}")
    public AjaxResult getExecutionQueue(@PathVariable Long projectId) {
        List<PipelineExecution> queue = pipelineExecutionService.getExecutionQueue(projectId);
        return success(queue);
    }

    /**
     * 获取执行资源使用情况
     */
    @PreAuthorize("@ss.hasPermi('cicd:execution:query')")
    @GetMapping("/{executionId}/resource-usage")
    public AjaxResult getResourceUsage(@PathVariable Long executionId) {
        Map<String, Object> resourceUsage = pipelineExecutionService.getResourceUsage(executionId);
        return success(resourceUsage);
    }

    /**
     * 添加资源使用记录
     */
    @PreAuthorize("@ss.hasPermi('cicd:execution:edit')")
    @PostMapping("/{executionId}/resource-usage")
    public AjaxResult addResourceUsage(@PathVariable Long executionId,
                                       @RequestBody Map<String, Object> resourceUsage) {
        try {
            pipelineExecutionService.addResourceUsage(executionId, resourceUsage);
            return success("资源使用记录添加成功");
        } catch (Exception e) {
            return error("添加资源使用记录失败: " + e.getMessage());
        }
    }

    /**
     * 获取执行产物
     */
    @PreAuthorize("@ss.hasPermi('cicd:execution:query')")
    @GetMapping("/{executionId}/artifacts")
    public AjaxResult getExecutionArtifacts(@PathVariable Long executionId) {
        List<Map<String, Object>> artifacts = pipelineExecutionService.getExecutionArtifacts(executionId);
        return success(artifacts);
    }

    /**
     * 添加执行产物
     */
    @PreAuthorize("@ss.hasPermi('cicd:execution:edit')")
    @PostMapping("/{executionId}/artifacts")
    public AjaxResult addExecutionArtifact(@PathVariable Long executionId,
                                           @RequestBody Map<String, Object> artifact) {
        try {
            pipelineExecutionService.addExecutionArtifact(executionId, artifact);
            return success("执行产物添加成功");
        } catch (Exception e) {
            return error("添加执行产物失败: " + e.getMessage());
        }
    }

    /**
     * 删除执行产物
     */
    @PreAuthorize("@ss.hasPermi('cicd:execution:remove')")
    @DeleteMapping("/{executionId}/artifacts/{artifactId}")
    public AjaxResult deleteExecutionArtifact(@PathVariable Long executionId,
                                              @PathVariable Long artifactId) {
        try {
            pipelineExecutionService.deleteExecutionArtifact(executionId, artifactId);
            return success("执行产物删除成功");
        } catch (Exception e) {
            return error("删除执行产物失败: " + e.getMessage());
        }
    }

    /**
     * 获取重试记录
     */
    @PreAuthorize("@ss.hasPermi('cicd:execution:query')")
    @GetMapping("/{executionId}/retries")
    public AjaxResult getRetryExecutions(@PathVariable Long executionId) {
        List<PipelineExecution> retries = pipelineExecutionService.getRetryExecutions(executionId);
        return success(retries);
    }

    /**
     * 获取执行链
     */
    @PreAuthorize("@ss.hasPermi('cicd:execution:query')")
    @GetMapping("/chain/{rootExecutionId}")
    public AjaxResult getExecutionChain(@PathVariable Long rootExecutionId) {
        List<PipelineExecution> chain = pipelineExecutionService.getExecutionChain(rootExecutionId);
        return success(chain);
    }

    /**
     * 获取并发执行统计
     */
    @PreAuthorize("@ss.hasPermi('cicd:execution:statistics')")
    @GetMapping("/concurrent-stats/{projectId}")
    public AjaxResult getConcurrentExecutionStats(@PathVariable Long projectId,
                                                 @RequestParam(required = false) String timeRange) {
        Map<String, Object> stats = pipelineExecutionService.getConcurrentExecutionStats(projectId, timeRange);
        return success(stats);
    }

    /**
     * 获取执行性能指标
     */
    @PreAuthorize("@ss.hasPermi('cicd:execution:statistics')")
    @GetMapping("/performance/{projectId}")
    public AjaxResult getExecutionPerformanceMetrics(@PathVariable Long projectId,
                                                    @RequestParam(required = false) String timeRange) {
        List<Map<String, Object>> metrics = pipelineExecutionService.getExecutionPerformanceMetrics(projectId, timeRange);
        return success(metrics);
    }

    /**
     * 清理过期执行记录
     */
    @PreAuthorize("@ss.hasPermi('cicd:execution:cleanup')")
    @PostMapping("/cleanup/{projectId}")
    public AjaxResult cleanExpiredExecutions(@PathVariable Long projectId,
                                            @RequestParam(defaultValue = "30") Integer days) {
        try {
            int cleanedCount = pipelineExecutionService.cleanExpiredExecutions(projectId, days);
            return success("清理完成", Map.of("cleanedCount", cleanedCount));
        } catch (Exception e) {
            return error("清理过期执行记录失败: " + e.getMessage());
        }
    }

    /**
     * 清理执行日志
     */
    @PreAuthorize("@ss.hasPermi('cicd:execution:cleanup')")
    @PostMapping("/{executionId}/cleanup-logs")
    public AjaxResult cleanExecutionLogs(@PathVariable Long executionId) {
        try {
            pipelineExecutionService.cleanExecutionLogs(executionId);
            return success("执行日志清理成功");
        } catch (Exception e) {
            return error("清理执行日志失败: " + e.getMessage());
        }
    }

    /**
     * 归档执行记录
     */
    @PreAuthorize("@ss.hasPermi('cicd:execution:archive')")
    @PostMapping("/{executionId}/archive")
    public AjaxResult archiveExecution(@PathVariable Long executionId) {
        try {
            pipelineExecutionService.archiveExecution(executionId);
            return success("执行记录归档成功");
        } catch (Exception e) {
            return error("归档执行记录失败: " + e.getMessage());
        }
    }

    /**
     * 获取归档的执行记录
     */
    @PreAuthorize("@ss.hasPermi('cicd:execution:list')")
    @GetMapping("/archived/{projectId}")
    public AjaxResult getArchivedExecutions(@PathVariable Long projectId,
                                           @RequestParam(defaultValue = "20") Integer limit) {
        List<PipelineExecution> executions = pipelineExecutionService.getArchivedExecutions(projectId, limit);
        return success(executions);
    }

    /**
     * 恢复归档的执行记录
     */
    @PreAuthorize("@ss.hasPermi('cicd:execution:archive')")
    @PostMapping("/{executionId}/restore")
    public AjaxResult restoreArchivedExecution(@PathVariable Long executionId) {
        try {
            pipelineExecutionService.restoreArchivedExecution(executionId);
            return success("归档执行记录恢复成功");
        } catch (Exception e) {
            return error("恢复归档执行记录失败: " + e.getMessage());
        }
    }

    /**
     * 获取执行环境变量
     */
    @PreAuthorize("@ss.hasPermi('cicd:execution:query')")
    @GetMapping("/{executionId}/environment")
    public AjaxResult getExecutionEnvironment(@PathVariable Long executionId) {
        Map<String, String> envVars = pipelineExecutionService.getExecutionEnvironment(executionId);
        return success(envVars);
    }

    /**
     * 设置执行环境变量
     */
    @PreAuthorize("@ss.hasPermi('cicd:execution:edit')")
    @PutMapping("/{executionId}/environment")
    public AjaxResult setExecutionEnvironment(@PathVariable Long executionId,
                                             @RequestBody Map<String, String> envVars) {
        try {
            pipelineExecutionService.setExecutionEnvironment(executionId, envVars);
            return success("环境变量设置成功");
        } catch (Exception e) {
            return error("设置环境变量失败: " + e.getMessage());
        }
    }

    /**
     * 批量停止执行
     */
    @PreAuthorize("@ss.hasPermi('cicd:execution:stop')")
    @PostMapping("/batch-stop")
    public AjaxResult batchStopExecutions(@RequestBody Long[] executionIds) {
        try {
            Map<String, Object> results = pipelineExecutionService.batchStopExecutions(executionIds);
            return success("批量停止完成", results);
        } catch (Exception e) {
            return error("批量停止失败: " + e.getMessage());
        }
    }

    /**
     * 批量重试执行
     */
    @PreAuthorize("@ss.hasPermi('cicd:execution:retry')")
    @PostMapping("/batch-retry")
    public AjaxResult batchRetryExecutions(@RequestBody Long[] executionIds) {
        try {
            List<Map<String, Object>> results = pipelineExecutionService.batchRetryExecutions(executionIds);
            return success("批量重试完成", Map.of("results", results));
        } catch (Exception e) {
            return error("批量重试失败: " + e.getMessage());
        }
    }

    /**
     * 导出执行记录
     */
    @PreAuthorize("@ss.hasPermi('cicd:execution:export')")
    @GetMapping("/export/{projectId}")
    public AjaxResult exportExecutions(@PathVariable Long projectId,
                                      @RequestParam(required = false) String timeRange,
                                      @RequestParam(defaultValue = "json") String format) {
        try {
            String filePath = pipelineExecutionService.exportExecutions(projectId, timeRange, format);
            return success("导出成功", Map.of("filePath", filePath));
        } catch (Exception e) {
            return error("导出失败: " + e.getMessage());
        }
    }

    /**
     * 获取执行详情摘要
     */
    @PreAuthorize("@ss.hasPermi('cicd:execution:query')")
    @GetMapping("/{executionId}/summary")
    public AjaxResult getExecutionSummary(@PathVariable Long executionId) {
        Map<String, Object> summary = pipelineExecutionService.getExecutionSummary(executionId);
        return success(summary);
    }
}