package com.autotest.platform.mapper;

import com.autotest.platform.domain.report.TestReport;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Map;

/**
 * 测试报告Mapper接口
 *
 * @author autotest
 * @date 2024-01-01
 */
public interface TestReportMapper {

    /**
     * 查询测试报告
     *
     * @param reportId 测试报告主键
     * @return 测试报告
     */
    public TestReport selectTestReportByReportId(Long reportId);

    /**
     * 查询测试报告列表
     *
     * @param testReport 测试报告
     * @return 测试报告集合
     */
    public List<TestReport> selectTestReportList(TestReport testReport);

    /**
     * 新增测试报告
     *
     * @param testReport 测试报告
     * @return 结果
     */
    public int insertTestReport(TestReport testReport);

    /**
     * 修改测试报告
     *
     * @param testReport 测试报告
     * @return 结果
     */
    public int updateTestReport(TestReport testReport);

    /**
     * 删除测试报告
     *
     * @param reportId 测试报告主键
     * @return 结果
     */
    public int deleteTestReportByReportId(Long reportId);

    /**
     * 批量删除测试报告
     *
     * @param reportIds 需要删除的数据主键集合
     * @return 结果
     */
    public int deleteTestReportByReportIds(Long[] reportIds);

    /**
     * 根据项目ID查询报告列表
     *
     * @param projectId 项目ID
     * @return 报告列表
     */
    public List<TestReport> selectByProjectId(Long projectId);

    /**
     * 根据执行ID查询报告
     *
     * @param executionId 执行ID
     * @return 报告列表
     */
    public List<TestReport> selectByExecutionId(Long executionId);

    /**
     * 根据报告类型查询报告
     *
     * @param reportType 报告类型
     * @param projectId 项目ID
     * @return 报告列表
     */
    public List<TestReport> selectByReportType(@Param("reportType") String reportType,
                                             @Param("projectId") Long projectId);

    /**
     * 统计报告数量
     *
     * @param projectId 项目ID
     * @param timeRange 时间范围
     * @return 统计结果
     */
    public Map<String, Object> statisticsByProject(@Param("projectId") Long projectId,
                                                 @Param("timeRange") String timeRange);

    /**
     * 查询热门失败用例
     *
     * @param projectId 项目ID
     * @param timeRange 时间范围
     * @param limit 限制数量
     * @return 失败用例列表
     */
    public List<Map<String, Object>> selectTopFailedCases(@Param("projectId") Long projectId,
                                                       @Param("timeRange") String timeRange,
                                                       @Param("limit") Integer limit);

    /**
     * 查询性能指标
     *
     * @param projectId 项目ID
     * @param timeRange 时间范围
     * @return 性能指标
     */
    public Map<String, Object> selectPerformanceMetrics(@Param("projectId") Long projectId,
                                                      @Param("timeRange") String timeRange);

    /**
     * 查询趋势数据
     *
     * @param projectId 项目ID
     * @param days 天数
     * @return 趋势数据
     */
    public List<Map<String, Object>> selectTrendData(@Param("projectId") Long projectId,
                                                   @Param("days") Integer days);

    /**
     * 更新报告状态
     *
     * @param reportId 报告ID
     * @param status 状态
     * @return 结果
     */
    public int updateStatus(@Param("reportId") Long reportId, @Param("status") String status);

    /**
     * 更新报告文件路径
     *
     * @param reportId 报告ID
     * @param filePath 文件路径
     * @return 结果
     */
    public int updateFilePath(@Param("reportId") Long reportId, @Param("filePath") String filePath);

    /**
     * 查询过期报告
     *
     * @param projectId 项目ID
     * @param days 保留天数
     * @return 过期报告ID列表
     */
    public List<Long> selectExpiredReports(@Param("projectId") Long projectId,
                                          @Param("days") Integer days);
}