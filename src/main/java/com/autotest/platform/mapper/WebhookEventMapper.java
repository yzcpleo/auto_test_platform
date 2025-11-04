package com.autotest.platform.mapper;

import com.autotest.platform.domain.cicd.WebhookEvent;
import com.autotest.platform.common.core.mapper.BaseMapperPlus;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * Webhook事件Mapper接口
 *
 * @author autotest
 * @date 2024-01-01
 */
@Mapper
public interface WebhookEventMapper extends BaseMapperPlus<WebhookEventMapper, WebhookEvent> {

    /**
     * 查询未处理的Webhook事件
     *
     * @param projectId 项目ID
     * @param limit 限制数量
     * @return 事件列表
     */
    List<WebhookEvent> selectUnhandledEvents(@Param("projectId") Long projectId,
                                           @Param("limit") Integer limit);

    /**
     * 查询处理中的Webhook事件
     *
     * @param projectId 项目ID
     * @return 事件列表
     */
    List<WebhookEvent> selectProcessingEvents(@Param("projectId") Long projectId);

    /**
     * 查询失败的事件
     *
     * @param projectId 项目ID
     * @param days 天数
     * @param limit 限制数量
     * @return 事件列表
     */
    List<WebhookEvent> selectFailedEvents(@Param("projectId") Long projectId,
                                        @Param("days") Integer days,
                                        @Param("limit") Integer limit);

    /**
     * 根据仓库URL查询事件
     *
     * @param repositoryUrl 仓库URL
     * @param eventType 事件类型
     * @param limit 限制数量
     * @return 事件列表
     */
    List<WebhookEvent> selectEventsByRepository(@Param("repositoryUrl") String repositoryUrl,
                                              @Param("eventType") String eventType,
                                              @Param("limit") Integer limit);

    /**
     * 根据分支查询事件
     *
     * @param projectId 项目ID
     * @param branch 分支名称
     * @param eventType 事件类型
     * @param limit 限制数量
     * @return 事件列表
     */
    List<WebhookEvent> selectEventsByBranch(@Param("projectId") Long projectId,
                                          @Param("branch") String branch,
                                          @Param("eventType") String eventType,
                                          @Param("limit") Integer limit);

    /**
     * 根据提交SHA查询事件
     *
     * @param commitSha 提交SHA
     * @return 事件列表
     */
    List<WebhookEvent> selectEventsByCommitSha(@Param("commitSha") String commitSha);

    /**
     * 更新事件处理状态
     *
     * @param eventId 事件ID
     * @param status 状态
     * @param errorMessage 错误信息
     * @return 影响行数
     */
    int updateEventStatus(@Param("eventId") Long eventId,
                         @Param("status") String status,
                         @Param("errorMessage") String errorMessage);

    /**
     * 批量更新事件状态
     *
     * @param eventIds 事件ID列表
     * @param status 状态
     * @return 影响行数
     */
    int updateEventStatusBatch(@Param("eventIds") List<Long> eventIds,
                              @Param("status") String status);

    /**
     * 查询事件统计
     *
     * @param projectId 项目ID
     * @param startTime 开始时间
     * @return 统计信息
     */
    Map<String, Object> selectEventStatistics(@Param("projectId") Long projectId,
                                            @Param("startTime") LocalDateTime startTime);

    /**
     * 查询事件类型分布
     *
     * @param projectId 项目ID
     * @param timeRange 时间范围
     * @return 类型分布
     */
    List<Map<String, Object>> selectEventTypeDistribution(@Param("projectId") Long projectId,
                                                        @Param("timeRange") String timeRange);

    /**
     * 查询事件源分布
     *
     * @param projectId 项目ID
     * @param timeRange 时间范围
     * @return 源分布
     */
    List<Map<String, Object>> selectEventSourceDistribution(@Param("projectId") Long projectId,
                                                          @Param("timeRange") String timeRange);

    /**
     * 查询事件处理时间统计
     *
     * @param projectId 项目ID
     * @param timeRange 时间范围
     * @return 处理时间统计
     */
    List<Map<String, Object>> selectEventProcessingTimeStats(@Param("projectId") Long projectId,
                                                           @Param("timeRange") String timeRange);

    /**
     * 查询失败事件原因统计
     *
     * @param projectId 项目ID
     * @param timeRange 时间范围
     * @return 失败原因统计
     */
    List<Map<String, Object>> selectEventFailureReasonStats(@Param("projectId") Long projectId,
                                                           @Param("timeRange") String timeRange);

    /**
     * 查询每日事件统计
     *
     * @param projectId 项目ID
     * @param days 天数
     * @return 每日统计
     */
    List<Map<String, Object>> selectDailyEventStats(@Param("projectId") Long projectId,
                                                   @Param("days") Integer days);

    /**
     * 查询仓库活跃度
     *
     * @param projectId 项目ID
     * @param timeRange 时间范围
     * @return 仓库活跃度
     */
    List<Map<String, Object>> selectRepositoryActivity(@Param("projectId") Long projectId,
                                                     @Param("timeRange") String timeRange);

    /**
     * 查询触发器性能统计
     *
     * @param projectId 项目ID
     * @param timeRange 时间范围
     * @return 性能统计
     */
    Map<String, Object> selectTriggerPerformanceStats(@Param("projectId") Long projectId,
                                                     @Param("timeRange") String timeRange);

    /**
     * 查询重复事件
     *
     * @param projectId 项目ID
     * @param timeWindow 时间窗口（分钟）
     * @return 重复事件列表
     */
    List<Map<String, Object>> selectDuplicateEvents(@Param("projectId") Long projectId,
                                                   @Param("timeWindow") Integer timeWindow);

    /**
     * 查询事件处理队列
     *
     * @param projectId 项目ID
     * @return 队列中的事件
     */
    List<WebhookEvent> selectEventQueue(@Param("projectId") Long projectId);

    /**
     * 查询事件重试记录
     *
     * @param originalEventId 原始事件ID
     * @return 重试记录
     */
    List<WebhookEvent> selectEventRetries(@Param("originalEventId") Long originalEventId);

    /**
     * 插入事件重试记录
     *
     * @param originalEventId 原始事件ID
     * @param retryEventId 重试事件ID
     * @return 影响行数
     */
    int insertEventRetry(@Param("originalEventId") Long originalEventId,
                        @Param("retryEventId") Long retryEventId);

    /**
     * 查询事件关联的流水线执行
     *
     * @param eventId 事件ID
     * @return 关联的执行记录
     */
    List<Map<String, Object>> selectRelatedExecutions(@Param("eventId") Long eventId);

    /**
     * 查询触发器配置
     *
     * @param projectId 项目ID
     * @param triggerType 触发器类型
     * @return 触发器配置
     */
    Map<String, Object> selectTriggerConfig(@Param("projectId") Long projectId,
                                           @Param("triggerType") String triggerType);

    /**
     * 更新触发器配置
     *
     * @param projectId 项目ID
     * @param triggerType 触发器类型
     * @param config 配置
     * @return 影响行数
     */
    int updateTriggerConfig(@Param("projectId") Long projectId,
                           @Param("triggerType") String triggerType,
                           @Param("config") Map<String, Object> config);

    /**
     * 查询事件处理历史
     *
     * @param eventId 事件ID
     * @return 处理历史
     */
    List<Map<String, Object>> selectEventProcessingHistory(@Param("eventId") Long eventId);

    /**
     * 插入事件处理历史
     *
     * @param eventId 事件ID
     * @param step 处理步骤
     * @param status 状态
     * @param message 消息
     * @return 影响行数
     */
    int insertEventProcessingHistory(@Param("eventId") Long eventId,
                                    @Param("step") String step,
                                    @Param("status") String status,
                                    @Param("message") String message);

    /**
     * 清理过期事件
     *
     * @param projectId 项目ID
     * @param cutoffTime 截止时间
     * @return 清理数量
     */
    int cleanExpiredEvents(@Param("projectId") Long projectId,
                          @Param("cutoffTime") LocalDateTime cutoffTime);

    /**
     * 归档事件
     *
     * @param eventId 事件ID
     * @return 影响行数
     */
    int archiveEvent(@Param("eventId") Long eventId);

    /**
     * 查询归档事件
     *
     * @param projectId 项目ID
     * @param limit 限制数量
     * @return 归档事件列表
     */
    List<WebhookEvent> selectArchivedEvents(@Param("projectId") Long projectId,
                                           @Param("limit") Integer limit);

    /**
     * 恢复归档事件
     *
     * @param eventId 事件ID
     * @return 影响行数
     */
    int restoreArchivedEvent(@Param("eventId") Long eventId);

    /**
     * 查询事件监控指标
     *
     * @param projectId 项目ID
     * @param timeRange 时间范围
     * @return 监控指标
     */
    Map<String, Object> selectEventMonitoringMetrics(@Param("projectId") Long projectId,
                                                    @Param("timeRange") String timeRange);

    /**
     * 查询异常事件模式
     *
     * @param projectId 项目ID
     * @param timeRange 时间范围
     * @return 异常模式
     */
    List<Map<String, Object>> selectAbnormalEventPatterns(@Param("projectId") Long projectId,
                                                         @Param("timeRange") String timeRange);

    /**
     * 查询事件负载情况
     *
     * @param projectId 项目ID
     * @param timeRange 时间范围
     * @return 负载情况
     */
    List<Map<String, Object>> selectEventLoadStats(@Param("projectId") Long projectId,
                                                  @Param("timeRange") String timeRange);

    /**
     * 查询事件处理能力
     *
     * @param projectId 项目ID
     * @return 处理能力指标
     */
    Map<String, Object> selectEventProcessingCapacity(@Param("projectId") Long projectId);

    /**
     * 查询事件关联关系
     *
     * @param eventId 事件ID
     * @return 关联事件
     */
    List<WebhookEvent> selectRelatedEvents(@Param("eventId") Long eventId);

    /**
     * 查询根事件
     *
     * @param eventId 事件ID
     * @return 根事件
     */
    WebhookEvent selectRootEvent(@Param("eventId") Long eventId);

    /**
     * 查询事件链
     *
     * @param rootEventId 根事件ID
     * @return 事件链
     */
    List<WebhookEvent> selectEventChain(@Param("rootEventId") Long rootEventId);
}