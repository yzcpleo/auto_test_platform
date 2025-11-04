package com.autotest.platform.service;

import com.autotest.platform.domain.report.TestReport;
import com.baomidou.mybatisplus.extension.service.IService;

import java.util.List;
import java.util.Map;

/**
 * 测试报告Service接口
 *
 * @author autotest
 * @date 2024-01-01
 */
public interface ITestReportService extends IService<TestReport> {

    /**
     * 查询测试报告
     *
     * @param reportId 测试报告主键
     * @return 测试报告
     */
    TestReport selectTestReportByReportId(Long reportId);

    /**
     * 查询测试报告列表
     *
     * @param testReport 测试报告
     * @return 测试报告集合
     */
    List<TestReport> selectTestReportList(TestReport testReport);

    /**
     * 新增测试报告
     *
     * @param testReport 测试报告
     * @return 结果
     */
    int insertTestReport(TestReport testReport);

    /**
     * 修改测试报告
     *
     * @param testReport 测试报告
     * @return 结果
     */
    int updateTestReport(TestReport testReport);

    /**
     * 批量删除测试报告
     *
     * @param reportIds 需要删除的测试报告主键集合
     * @return 结果
     */
    int deleteTestReportByReportIds(Long[] reportIds);

    /**
     * 删除测试报告信息
     *
     * @param reportId 测试报告主键
     * @return 结果
     */
    int deleteTestReportByReportId(Long reportId);

    /**
     * 生成执行报告
     *
     * @param executionId 执行ID
     * @param reportConfig 报告配置
     * @param generatorId 生成人ID
     * @return 报告信息
     */
    TestReport generateExecutionReport(Long executionId, Map<String, Object> reportConfig, Long generatorId);

    /**
     * 生成趋势报告
     *
     * @param projectId 项目ID
     * @param timeRange 时间范围
     * @param reportConfig 报告配置
     * @param generatorId 生成人ID
     * @return 报告信息
     */
    TestReport generateTrendReport(Long projectId, String timeRange, Map<String, Object> reportConfig, Long generatorId);

    /**
     * 生成摘要报告
     *
     * @param projectId 项目ID
     * @param reportConfig 报告配置
     * @param generatorId 生成人ID
     * @return 报告信息
     */
    TestReport generateSummaryReport(Long projectId, Map<String, Object> reportConfig, Long generatorId);

    /**
     * 异步生成报告
     *
     * @param reportRequest 报告请求
     * @return 报告ID
     */
    Long generateReportAsync(Map<String, Object> reportRequest);

    /**
     * 获取报告生成状态
     *
     * @param reportId 报告ID
     * @return 生成状态
     */
    String getReportGenerationStatus(Long reportId);

    /**
     * 获取报告统计数据
     *
     * @param projectId 项目ID
     * @param timeRange 时间范围
     * @return 统计数据
     */
    Map<String, Object> getReportStatistics(Long projectId, String timeRange);

    /**
     * 获取趋势数据
     *
     * @param projectId 项目ID
     * @param days 天数
     * @return 趋势数据
     */
    List<Map<String, Object>> getTrendData(Long projectId, Integer days);

    /**
     * 获取热门失败用例
     *
     * @param projectId 项目ID
     * @param timeRange 时间范围
     * @param limit 限制数量
     * @return 失败用例列表
     */
    List<Map<String, Object>> getTopFailedCases(Long projectId, String timeRange, Integer limit);

    /**
     * 获取性能指标
     *
     * @param projectId 项目ID
     * @param timeRange 时间范围
     * @return 性能指标
     */
    Map<String, Object> getPerformanceMetrics(Long projectId, String timeRange);

    /**
     * 导出报告
     *
     * @param reportId 报告ID
     * @param format 导出格式
     * @return 导出文件路径
     */
    String exportReport(Long reportId, String format);

    /**
     * 分享报告
     *
     * @param reportId 报告ID
     * @param shareConfig 分享配置
     * @return 分享链接
     */
    String shareReport(Long reportId, Map<String, Object> shareConfig);

    /**
     * 清理过期报告
     *
     * @param projectId 项目ID
     * @param days 保留天数
     * @return 清理数量
     */
    int cleanExpiredReports(Long projectId, Integer days);

    /**
     * 获取报告模板列表
     *
     * @param reportType 报告类型
     * @return 模板列表
     */
    List<Map<String, Object>> getReportTemplates(String reportType);

    /**
     * 预览报告
     *
     * @param reportId 报告ID
     * @return 预览内容
     */
    String previewReport(Long reportId);
}