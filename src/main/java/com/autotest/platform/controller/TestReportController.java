package com.autotest.platform.controller;

import com.autotest.platform.common.core.controller.BaseController;
import com.autotest.platform.common.core.domain.AjaxResult;
import com.autotest.platform.common.core.page.TableDataInfo;
import com.autotest.platform.domain.report.TestReport;
import com.autotest.platform.service.ITestReportService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * 测试报告Controller
 *
 * @author autotest
 * @date 2024-01-01
 */
@RestController
@RequestMapping("/test/report")
public class TestReportController extends BaseController {

    @Autowired
    private ITestReportService testReportService;

    /**
     * 查询测试报告列表
     */
    @PreAuthorize("@ss.hasPermi('test:report:list')")
    @GetMapping("/list")
    public TableDataInfo list(TestReport testReport) {
        startPage();
        List<TestReport> list = testReportService.selectTestReportList(testReport);
        return getDataTable(list);
    }

    /**
     * 获取测试报告详细信息
     */
    @PreAuthorize("@ss.hasPermi('test:report:query')")
    @GetMapping(value = "/{reportId}")
    public AjaxResult getInfo(@PathVariable("reportId") Long reportId) {
        TestReport report = testReportService.selectTestReportByReportId(reportId);
        return success(report);
    }

    /**
     * 删除测试报告
     */
    @PreAuthorize("@ss.hasPermi('test:report:remove')")
    @DeleteMapping("/{reportIds}")
    public AjaxResult remove(@PathVariable Long[] reportIds) {
        return toAjax(testReportService.deleteTestReportByReportIds(reportIds));
    }

    /**
     * 生成执行报告
     */
    @PreAuthorize("@ss.hasPermi('test:report:generate')")
    @PostMapping("/execution")
    public AjaxResult generateExecutionReport(@RequestBody Map<String, Object> params) {
        try {
            Long executionId = Long.valueOf(params.get("executionId").toString());
            @SuppressWarnings("unchecked")
            Map<String, Object> reportConfig = (Map<String, Object>) params.getOrDefault("config", new java.util.HashMap<>());
            Long generatorId = getUserId();

            TestReport report = testReportService.generateExecutionReport(executionId, reportConfig, generatorId);
            return success("执行报告生成成功", report);
        } catch (Exception e) {
            return error("生成执行报告失败: " + e.getMessage());
        }
    }

    /**
     * 生成趋势报告
     */
    @PreAuthorize("@ss.hasPermi('test:report:generate')")
    @PostMapping("/trend")
    public AjaxResult generateTrendReport(@RequestBody Map<String, Object> params) {
        try {
            Long projectId = Long.valueOf(params.get("projectId").toString());
            String timeRange = (String) params.getOrDefault("timeRange", "7d");
            @SuppressWarnings("unchecked")
            Map<String, Object> reportConfig = (Map<String, Object>) params.getOrDefault("config", new java.util.HashMap<>());
            Long generatorId = getUserId();

            TestReport report = testReportService.generateTrendReport(projectId, timeRange, reportConfig, generatorId);
            return success("趋势报告生成成功", report);
        } catch (Exception e) {
            return error("生成趋势报告失败: " + e.getMessage());
        }
    }

    /**
     * 生成摘要报告
     */
    @PreAuthorize("@ss.hasPermi('test:report:generate')")
    @PostMapping("/summary")
    public AjaxResult generateSummaryReport(@RequestBody Map<String, Object> params) {
        try {
            Long projectId = Long.valueOf(params.get("projectId").toString());
            @SuppressWarnings("unchecked")
            Map<String, Object> reportConfig = (Map<String, Object>) params.getOrDefault("config", new java.util.HashMap<>());
            Long generatorId = getUserId();

            TestReport report = testReportService.generateSummaryReport(projectId, reportConfig, generatorId);
            return success("摘要报告生成成功", report);
        } catch (Exception e) {
            return error("生成摘要报告失败: " + e.getMessage());
        }
    }

    /**
     * 异步生成报告
     */
    @PreAuthorize("@ss.hasPermi('test:report:generate')")
    @PostMapping("/async")
    public AjaxResult generateReportAsync(@RequestBody Map<String, Object> params) {
        try {
            // 添加生成人信息
            params.put("generatorId", getUserId());

            // 异步生成报告
            java.util.concurrent.CompletableFuture<Long> future =
                testReportService.generateReportAsync(params);

            return success("报告生成任务已提交", Map.of("taskId", future));
        } catch (Exception e) {
            return error("提交报告生成任务失败: " + e.getMessage());
        }
    }

    /**
     * 获取报告生成状态
     */
    @PreAuthorize("@ss.hasPermi('test:report:query')")
    @GetMapping("/status/{reportId}")
    public AjaxResult getReportStatus(@PathVariable Long reportId) {
        String status = testReportService.getReportGenerationStatus(reportId);
        return success(Map.of("reportId", reportId, "status", status));
    }

    /**
     * 获取报告统计数据
     */
    @PreAuthorize("@ss.hasPermi('test:report:statistics')")
    @GetMapping("/statistics/{projectId}")
    public AjaxResult getStatistics(@PathVariable Long projectId,
                                   @RequestParam(required = false) String timeRange) {
        Map<String, Object> statistics = testReportService.getReportStatistics(projectId, timeRange);
        return success(statistics);
    }

    /**
     * 获取趋势数据
     */
    @PreAuthorize("@ss.hasPermi('test:report:trend')")
    @GetMapping("/trend/{projectId}")
    public AjaxResult getTrendData(@PathVariable Long projectId,
                                  @RequestParam(defaultValue = "7") Integer days) {
        List<Map<String, Object>> trendData = testReportService.getTrendData(projectId, days);
        return success(trendData);
    }

    /**
     * 获取热门失败用例
     */
    @PreAuthorize("@ss.hasPermi('test:report:analysis')")
    @GetMapping("/failed-cases/{projectId}")
    public AjaxResult getTopFailedCases(@PathVariable Long projectId,
                                       @RequestParam(required = false) String timeRange,
                                       @RequestParam(defaultValue = "10") Integer limit) {
        List<Map<String, Object>> failedCases = testReportService.getTopFailedCases(projectId, timeRange, limit);
        return success(failedCases);
    }

    /**
     * 获取性能指标
     */
    @PreAuthorize("@ss.hasPermi('test:report:analysis')")
    @GetMapping("/performance/{projectId}")
    public AjaxResult getPerformanceMetrics(@PathVariable Long projectId,
                                           @RequestParam(required = false) String timeRange) {
        Map<String, Object> metrics = testReportService.getPerformanceMetrics(projectId, timeRange);
        return success(metrics);
    }

    /**
     * 导出报告
     */
    @PreAuthorize("@ss.hasPermi('test:report:export')")
    @PostMapping("/{reportId}/export")
    public AjaxResult exportReport(@PathVariable Long reportId, @RequestParam String format) {
        try {
            String exportPath = testReportService.exportReport(reportId, format);
            return success("报告导出成功", Map.of("exportPath", exportPath));
        } catch (Exception e) {
            return error("导出报告失败: " + e.getMessage());
        }
    }

    /**
     * 分享报告
     */
    @PreAuthorize("@ss.hasPermi('test:report:share')")
    @PostMapping("/{reportId}/share")
    public AjaxResult shareReport(@PathVariable Long reportId, @RequestBody Map<String, Object> shareConfig) {
        try {
            String shareUrl = testReportService.shareReport(reportId, shareConfig);
            return success("报告分享成功", Map.of("shareUrl", shareUrl));
        } catch (Exception e) {
            return error("分享报告失败: " + e.getMessage());
        }
    }

    /**
     * 预览报告
     */
    @PreAuthorize("@ss.hasPermi('test:report:preview')")
    @GetMapping("/{reportId}/preview")
    public AjaxResult previewReport(@PathVariable Long reportId) {
        try {
            String previewUrl = testReportService.previewReport(reportId);
            return success(Map.of("previewUrl", previewUrl));
        } catch (Exception e) {
            return error("预览报告失败: " + e.getMessage());
        }
    }

    /**
     * 获取报告模板列表
     */
    @PreAuthorize("@ss.hasPermi('test:report:template')")
    @GetMapping("/templates")
    public AjaxResult getReportTemplates(@RequestParam(required = false) String reportType) {
        List<Map<String, Object>> templates = testReportService.getReportTemplates(reportType);
        return success(templates);
    }

    /**
     * 清理过期报告
     */
    @PreAuthorize("@ss.hasPermi('test:report:cleanup')")
    @PostMapping("/cleanup/{projectId}")
    public AjaxResult cleanupExpiredReports(@PathVariable Long projectId,
                                           @RequestParam(defaultValue = "30") Integer days) {
        int cleanedCount = testReportService.cleanExpiredReports(projectId, days);
        return success("清理完成", Map.of("cleanedCount", cleanedCount));
    }

    /**
     * 批量生成报告
     */
    @PreAuthorize("@ss.hasPermi('test:report:batch')")
    @PostMapping("/batch")
    public AjaxResult batchGenerateReports(@RequestBody Map<String, Object> params) {
        try {
            @SuppressWarnings("unchecked")
            List<Map<String, Object>> reportRequests =
                (List<Map<String, Object>>) params.get("reports");

            if (reportRequests == null || reportRequests.isEmpty()) {
                return error("报告请求列表不能为空");
            }

            List<java.util.concurrent.CompletableFuture<Long>> futures = new java.util.ArrayList<>();

            for (Map<String, Object> request : reportRequests) {
                // 添加生成人信息
                request.put("generatorId", getUserId());

                // 异步生成报告
                java.util.concurrent.CompletableFuture<Long> future =
                    testReportService.generateReportAsync(request);
                futures.add(future);
            }

            return success("批量报告生成任务已提交",
                Map.of("taskCount", futures.size(), "tasks", futures));
        } catch (Exception e) {
            return error("批量生成报告失败: " + e.getMessage());
        }
    }

    /**
     * 获取报告下载链接
     */
    @PreAuthorize("@ss.hasPermi('test:report:download')")
    @GetMapping("/{reportId}/download")
    public AjaxResult getDownloadUrl(@PathVariable Long reportId) {
        try {
            TestReport report = testReportService.selectTestReportByReportId(reportId);
            if (report == null) {
                return error("报告不存在");
            }

            String downloadUrl = "/api/test/report/download/file/" + reportId;
            return success(Map.of("downloadUrl", downloadUrl, "fileName",
                report.getReportName() + "." + report.getFormat().toLowerCase()));
        } catch (Exception e) {
            return error("获取下载链接失败: " + e.getMessage());
        }
    }
}