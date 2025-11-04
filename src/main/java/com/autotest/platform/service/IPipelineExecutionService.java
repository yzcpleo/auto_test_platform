package com.autotest.platform.service;

import com.autotest.platform.domain.cicd.PipelineExecution;
import com.baomidou.mybatisplus.extension.service.IService;

import java.util.List;
import java.util.Map;

/**
 * 流水线执行Service接口
 *
 * @author autotest
 * @date 2024-01-01
 */
public interface IPipelineExecutionService extends IService<PipelineExecution> {

    /**
     * 查询流水线执行
     *
     * @param executionId 流水线执行主键
     * @return 流水线执行
     */
    PipelineExecution selectPipelineExecutionByExecutionId(Long executionId);

    /**
     * 查询流水线执行列表
     *
     * @param pipelineExecution 流水线执行
     * @return 流水线执行集合
     */
    List<PipelineExecution> selectPipelineExecutionList(PipelineExecution pipelineExecution);

    /**
     * 新增流水线执行
     *
     * @param pipelineExecution 流水线执行
     * @return 结果
     */
    int insertPipelineExecution(PipelineExecution pipelineExecution);

    /**
     * 修改流水线执行
     *
     * @param pipelineExecution 流水线执行
     * @return 结果
     */
    int updatePipelineExecution(PipelineExecution pipelineExecution);

    /**
     * 批量删除流水线执行
     *
     * @param executionIds 需要删除的流水线执行主键集合
     * @return 结果
     */
    int deletePipelineExecutionByExecutionIds(Long[] executionIds);

    /**
     * 删除流水线执行信息
     *
     * @param executionId 流水线执行主键
     * @return 结果
     */
    int deletePipelineExecutionByExecutionId(Long executionId);

    /**
     * 停止执行
     *
     * @param executionId 执行ID
     * @return 是否成功
     */
    boolean stopExecution(Long executionId);

    /**
     * 重新执行
     *
     * @param executionId 执行ID
     * @return 新执行记录
     */
    PipelineExecution retryExecution(Long executionId);

    /**
     * 获取流水线的执行记录
     *
     * @param pipelineId 流水线ID
     * @param status 状态
     * @param limit 限制数量
     * @return 执行记录列表
     */
    List<PipelineExecution> getExecutionsByPipeline(Long pipelineId, String status, Integer limit);

    /**
     * 获取正在运行的执行记录
     *
     * @param projectId 项目ID
     * @return 执行记录列表
     */
    List<PipelineExecution> getRunningExecutions(Long projectId);

    /**
     * 获取失败的执行记录
     *
     * @param projectId 项目ID
     * @param days 天数
     * @param limit 限制数量
     * @return 执行记录列表
     */
    List<PipelineExecution> getFailedExecutions(Long projectId, Integer days, Integer limit);

    /**
     * 获取执行时间最长的记录
     *
     * @param projectId 项目ID
     * @param limit 限制数量
     * @return 执行记录列表
     */
    List<PipelineExecution> getLongestExecutions(Long projectId, Integer limit);

    /**
     * 获取执行步骤详情
     *
     * @param executionId 执行ID
     * @return 步骤详情列表
     */
    List<Map<String, Object>> getExecutionStepDetails(Long executionId);

    /**
     * 获取执行日志
     *
     * @param executionId 执行ID
     * @param stepName 步骤名称
     * @return 日志内容
     */
    String getExecutionLogs(Long executionId, String stepName);

    /**
     * 添加执行日志
     *
     * @param executionId 执行ID
     * @param stepName 步骤名称
     * @param logContent 日志内容
     */
    void addExecutionLog(Long executionId, String stepName, String logContent);

    /**
     * 更新步骤状态
     *
     * @param executionId 执行ID
     * @param stepName 步骤名称
     * @param status 状态
     * @param output 输出
     * @param errorMessage 错误信息
     */
    void updateStepStatus(Long executionId, String stepName, String status, String output, String errorMessage);

    /**
     * 获取执行统计信息
     *
     * @param projectId 项目ID
     * @param timeRange 时间范围
     * @return 统计信息
     */
    Map<String, Object> getExecutionStatistics(Long projectId, String timeRange);

    /**
     * 获取成功率趋势
     *
     * @param projectId 项目ID
     * @param startTime 开始时间
     * @param endTime 结束时间
     * @return 成功率趋势
     */
    List<Map<String, Object>> getSuccessTrend(Long projectId, String startTime, String endTime);

    /**
     * 获取执行时长统计
     *
     * @param projectId 项目ID
     * @param timeRange 时间范围
     * @return 时长统计
     */
    List<Map<String, Object>> getExecutionDurationStats(Long projectId, String timeRange);

    /**
     * 获取步骤执行统计
     *
     * @param projectId 项目ID
     * @param timeRange 时间范围
     * @return 步骤统计
     */
    List<Map<String, Object>> getStepExecutionStats(Long projectId, String timeRange);

    /**
     * 获取失败原因统计
     *
     * @param projectId 项目ID
     * @param timeRange 时间范围
     * @return 失败原因统计
     */
    List<Map<String, Object>> getFailureReasonStats(Long projectId, String timeRange);

    /**
     * 获取每日执行统计
     *
     * @param projectId 项目ID
     * @param days 天数
     * @return 每日统计
     */
    List<Map<String, Object>> getDailyExecutionStats(Long projectId, Integer days);

    /**
     * 获取执行队列
     *
     * @param projectId 项目ID
     * @return 队列中的执行记录
     */
    List<PipelineExecution> getExecutionQueue(Long projectId);

    /**
     * 获取执行资源使用情况
     *
     * @param executionId 执行ID
     * @return 资源使用情况
     */
    Map<String, Object> getResourceUsage(Long executionId);

    /**
     * 添加资源使用记录
     *
     * @param executionId 执行ID
     * @param resourceUsage 资源使用情况
     */
    void addResourceUsage(Long executionId, Map<String, Object> resourceUsage);

    /**
     * 获取执行产物
     *
     * @param executionId 执行ID
     * @return 产物列表
     */
    List<Map<String, Object>> getExecutionArtifacts(Long executionId);

    /**
     * 添加执行产物
     *
     * @param executionId 执行ID
     * @param artifact 产物信息
     */
    void addExecutionArtifact(Long executionId, Map<String, Object> artifact);

    /**
     * 删除执行产物
     *
     * @param executionId 执行ID
     * @param artifactId 产物ID
     */
    void deleteExecutionArtifact(Long executionId, Long artifactId);

    /**
     * 获取重试记录
     *
     * @param originalExecutionId 原始执行ID
     * @return 重试记录
     */
    List<PipelineExecution> getRetryExecutions(Long originalExecutionId);

    /**
     * 获取执行链
     *
     * @param rootExecutionId 根执行ID
     * @return 执行链
     */
    List<PipelineExecution> getExecutionChain(Long rootExecutionId);

    /**
     * 获取并发执行统计
     *
     * @param projectId 项目ID
     * @param timeRange 时间范围
     * @return 并发统计
     */
    Map<String, Object> getConcurrentExecutionStats(Long projectId, String timeRange);

    /**
     * 获取执行性能指标
     *
     * @param projectId 项目ID
     * @param timeRange 时间范围
     * @return 性能指标
     */
    List<Map<String, Object>> getExecutionPerformanceMetrics(Long projectId, String timeRange);

    /**
     * 清理过期执行记录
     *
     * @param projectId 项目ID
     * @param days 保留天数
     * @return 清理数量
     */
    int cleanExpiredExecutions(Long projectId, Integer days);

    /**
     * 清理执行日志
     *
     * @param executionId 执行ID
     */
    void cleanExecutionLogs(Long executionId);

    /**
     * 归档执行记录
     *
     * @param executionId 执行ID
     */
    void archiveExecution(Long executionId);

    /**
     * 获取归档的执行记录
     *
     * @param projectId 项目ID
     * @param limit 限制数量
     * @return 归档记录列表
     */
    List<PipelineExecution> getArchivedExecutions(Long projectId, Integer limit);

    /**
     * 恢复归档的执行记录
     *
     * @param executionId 执行ID
     */
    void restoreArchivedExecution(Long executionId);

    /**
     * 获取执行环境变量
     *
     * @param executionId 执行ID
     * @return 环境变量
     */
    Map<String, String> getExecutionEnvironment(Long executionId);

    /**
     * 设置执行环境变量
     *
     * @param executionId 执行ID
     * @param envVars 环境变量
     */
    void setExecutionEnvironment(Long executionId, Map<String, String> envVars);

    /**
     * 批量停止执行
     *
     * @param executionIds 执行ID列表
     * @return 停止结果
     */
    Map<String, Object> batchStopExecutions(Long[] executionIds);

    /**
     * 批量重试执行
     *
     * @param executionIds 执行ID列表
     * @return 重试结果列表
     */
    List<Map<String, Object>> batchRetryExecutions(Long[] executionIds);

    /**
     * 导出执行记录
     *
     * @param projectId 项目ID
     * @param timeRange 时间范围
     * @param format 导出格式
     * @return 文件路径
     */
    String exportExecutions(Long projectId, String timeRange, String format);

    /**
     * 获取执行详情摘要
     *
     * @param executionId 执行ID
     * @return 摘要信息
     */
    Map<String, Object> getExecutionSummary(Long executionId);
}