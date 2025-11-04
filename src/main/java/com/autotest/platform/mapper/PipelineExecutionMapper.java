package com.autotest.platform.mapper;

import com.autotest.platform.domain.cicd.PipelineExecution;
import com.autotest.platform.common.core.mapper.BaseMapperPlus;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * 流水线执行Mapper接口
 *
 * @author autotest
 * @date 2024-01-01
 */
@Mapper
public interface PipelineExecutionMapper extends BaseMapperPlus<PipelineExecutionMapper, PipelineExecution> {

    /**
     * 查询执行统计信息
     *
     * @param projectId 项目ID
     * @param startTime 开始时间
     * @return 统计信息
     */
    Map<String, Object> selectExecutionStatistics(@Param("projectId") Long projectId,
                                                @Param("startTime") LocalDateTime startTime);

    /**
     * 查询成功率趋势
     *
     * @param projectId 项目ID
     * @param startTime 开始时间
     * @param endTime 结束时间
     * @return 成功率趋势
     */
    List<Map<String, Object>> selectSuccessTrend(@Param("projectId") Long projectId,
                                               @Param("startTime") LocalDateTime startTime,
                                               @Param("endTime") LocalDateTime endTime);

    /**
     * 查询流水线的执行记录
     *
     * @param pipelineId 流水线ID
     * @param status 状态
     * @param limit 限制数量
     * @return 执行记录列表
     */
    List<PipelineExecution> selectExecutionsByPipeline(@Param("pipelineId") Long pipelineId,
                                                      @Param("status") String status,
                                                      @Param("limit") Integer limit);

    /**
     * 查询正在运行的执行记录
     *
     * @param projectId 项目ID
     * @return 执行记录列表
     */
    List<PipelineExecution> selectRunningExecutions(@Param("projectId") Long projectId);

    /**
     * 查询失败的执行记录
     *
     * @param projectId 项目ID
     * @param days 天数
     * @param limit 限制数量
     * @return 执行记录列表
     */
    List<PipelineExecution> selectFailedExecutions(@Param("projectId") Long projectId,
                                                 @Param("days") Integer days,
                                                 @Param("limit") Integer limit);

    /**
     * 查询执行时间最长的记录
     *
     * @param projectId 项目ID
     * @param limit 限制数量
     * @return 执行记录列表
     */
    List<PipelineExecution> selectLongestExecutions(@Param("projectId") Long projectId,
                                                   @Param("limit") Integer limit);

    /**
     * 查询执行步骤详情
     *
     * @param executionId 执行ID
     * @return 步骤详情列表
     */
    List<Map<String, Object>> selectExecutionStepDetails(@Param("executionId") Long executionId);

    /**
     * 查询执行日志
     *
     * @param executionId 执行ID
     * @param stepName 步骤名称
     * @return 日志内容
     */
    String selectExecutionLog(@Param("executionId") Long executionId,
                             @Param("stepName") String stepName);

    /**
     * 插入执行日志
     *
     * @param executionId 执行ID
     * @param stepName 步骤名称
     * @param logContent 日志内容
     * @return 影响行数
     */
    int insertExecutionLog(@Param("executionId") Long executionId,
                          @Param("stepName") String stepName,
                          @Param("logContent") String logContent);

    /**
     * 更新执行状态
     *
     * @param executionId 执行ID
     * @param status 状态
     * @param errorMessage 错误信息
     * @return 影响行数
     */
    int updateExecutionStatus(@Param("executionId") Long executionId,
                             @Param("status") String status,
                             @Param("errorMessage") String errorMessage);

    /**
     * 更新执行步骤状态
     *
     * @param executionId 执行ID
     * @param stepName 步骤名称
     * @param status 状态
     * @param output 输出
     * @param errorMessage 错误信息
     * @return 影响行数
     */
    int updateStepStatus(@Param("executionId") Long executionId,
                        @Param("stepName") String stepName,
                        @Param("status") String status,
                        @Param("output") String output,
                        @Param("errorMessage") String errorMessage);

    /**
     * 查询执行时长统计
     *
     * @param projectId 项目ID
     * @param timeRange 时间范围
     * @return 时长统计
     */
    List<Map<String, Object>> selectExecutionDurationStats(@Param("projectId") Long projectId,
                                                          @Param("timeRange") String timeRange);

    /**
     * 查询步骤执行统计
     *
     * @param projectId 项目ID
     * @param timeRange 时间范围
     * @return 步骤统计
     */
    List<Map<String, Object>> selectStepExecutionStats(@Param("projectId") Long projectId,
                                                      @Param("timeRange") String timeRange);

    /**
     * 查询执行失败原因统计
     *
     * @param projectId 项目ID
     * @param timeRange 时间范围
     * @return 失败原因统计
     */
    List<Map<String, Object>> selectFailureReasonStats(@Param("projectId") Long projectId,
                                                      @Param("timeRange") String timeRange);

    /**
     * 查询每日执行统计
     *
     * @param projectId 项目ID
     * @param days 天数
     * @return 每日统计
     */
    List<Map<String, Object>> selectDailyExecutionStats(@Param("projectId") Long projectId,
                                                       @Param("days") Integer days);

    /**
     * 查询执行队列
     *
     * @param projectId 项目ID
     * @return 队列中的执行记录
     */
    List<PipelineExecution> selectExecutionQueue(@Param("projectId") Long projectId);

    /**
     * 查询执行资源使用情况
     *
     * @param executionId 执行ID
     * @return 资源使用情况
     */
    Map<String, Object> selectResourceUsage(@Param("executionId") Long executionId);

    /**
     * 插入资源使用记录
     *
     * @param executionId 执行ID
     * @param resourceUsage 资源使用情况
     * @return 影响行数
     */
    int insertResourceUsage(@Param("executionId") Long executionId,
                           @Param("resourceUsage") Map<String, Object> resourceUsage);

    /**
     * 查询执行产物
     *
     * @param executionId 执行ID
     * @return 产物列表
     */
    List<Map<String, Object>> selectExecutionArtifacts(@Param("executionId") Long executionId);

    /**
     * 插入执行产物
     *
     * @param executionId 执行ID
     * @param artifact 产物信息
     * @return 影响行数
     */
    int insertExecutionArtifact(@Param("executionId") Long executionId,
                               @Param("artifact") Map<String, Object> artifact);

    /**
     * 删除执行产物
     *
     * @param executionId 执行ID
     * @param artifactId 产物ID
     * @return 影响行数
     */
    int deleteExecutionArtifact(@Param("executionId") Long executionId,
                               @Param("artifactId") Long artifactId);

    /**
     * 查询重试记录
     *
     * @param originalExecutionId 原始执行ID
     * @return 重试记录列表
     */
    List<PipelineExecution> selectRetryExecutions(@Param("originalExecutionId") Long originalExecutionId);

    /**
     * 查询执行链
     *
     * @param rootExecutionId 根执行ID
     * @return 执行链
     */
    List<PipelineExecution> selectExecutionChain(@Param("rootExecutionId") Long rootExecutionId);

    /**
     * 查询并发执行统计
     *
     * @param projectId 项目ID
     * @param timeRange 时间范围
     * @return 并发统计
     */
    Map<String, Object> selectConcurrentExecutionStats(@Param("projectId") Long projectId,
                                                      @Param("timeRange") String timeRange);

    /**
     * 查询执行性能指标
     *
     * @param projectId 项目ID
     * @param timeRange 时间范围
     * @return 性能指标
     */
    List<Map<String, Object>> selectExecutionPerformanceMetrics(@Param("projectId") Long projectId,
                                                               @Param("timeRange") String timeRange);

    /**
     * 清理过期执行记录
     *
     * @param projectId 项目ID
     * @param cutoffTime 截止时间
     * @return 清理数量
     */
    int cleanExpiredExecutions(@Param("projectId") Long projectId,
                              @Param("cutoffTime") LocalDateTime cutoffTime);

    /**
     * 清理执行日志
     *
     * @param executionId 执行ID
     * @return 清理数量
     */
    int cleanExecutionLogs(@Param("executionId") Long executionId);

    /**
     * 归档执行记录
     *
     * @param executionId 执行ID
     * @return 影响行数
     */
    int archiveExecution(@Param("executionId") Long executionId);

    /**
     * 查询归档的执行记录
     *
     * @param projectId 项目ID
     * @param limit 限制数量
     * @return 归档记录列表
     */
    List<PipelineExecution> selectArchivedExecutions(@Param("projectId") Long projectId,
                                                   @Param("limit") Integer limit);

    /**
     * 恢复归档的执行记录
     *
     * @param executionId 执行ID
     * @return 影响行数
     */
    int restoreArchivedExecution(@Param("executionId") Long executionId);

    /**
     * 查询执行环境变量
     *
     * @param executionId 执行ID
     * @return 环境变量
     */
    Map<String, String> selectExecutionEnvironment(@Param("executionId") Long executionId);

    /**
     * 设置执行环境变量
     *
     * @param executionId 执行ID
     * @param envVars 环境变量
     * @return 影响行数
     */
    int setExecutionEnvironment(@Param("executionId") Long executionId,
                               @Param("envVars") Map<String, String> envVars);
}