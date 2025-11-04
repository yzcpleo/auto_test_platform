package com.autotest.platform.service;

import com.autotest.platform.domain.cicd.WebhookEvent;
import com.baomidou.mybatisplus.extension.service.IService;

import java.util.List;
import java.util.Map;

/**
 * Webhook事件Service接口
 *
 * @author autotest
 * @date 2024-01-01
 */
public interface IWebhookEventService extends IService<WebhookEvent> {

    /**
     * 查询Webhook事件
     *
     * @param eventId Webhook事件主键
     * @return Webhook事件
     */
    WebhookEvent selectWebhookEventByEventId(Long eventId);

    /**
     * 查询Webhook事件列表
     *
     * @param webhookEvent Webhook事件
     * @return Webhook事件集合
     */
    List<WebhookEvent> selectWebhookEventList(WebhookEvent webhookEvent);

    /**
     * 新增Webhook事件
     *
     * @param webhookEvent Webhook事件
     * @return 结果
     */
    int insertWebhookEvent(WebhookEvent webhookEvent);

    /**
     * 修改Webhook事件
     *
     * @param webhookEvent Webhook事件
     * @return 结果
     */
    int updateWebhookEvent(WebhookEvent webhookEvent);

    /**
     * 批量删除Webhook事件
     *
     * @param eventIds 需要删除的Webhook事件主键集合
     * @return 结果
     */
    int deleteWebhookEventByEventIds(Long[] eventIds);

    /**
     * 删除Webhook事件信息
     *
     * @param eventId Webhook事件主键
     * @return 结果
     */
    int deleteWebhookEventByEventId(Long eventId);

    /**
     * 重新处理事件
     *
     * @param eventId 事件ID
     * @param userId 用户ID
     * @return 新事件
     */
    WebhookEvent retryEvent(Long eventId, Long userId);

    /**
     * 批量重新处理事件
     *
     * @param eventIds 事件ID列表
     * @param userId 用户ID
     * @return 重试结果列表
     */
    List<Map<String, Object>> batchRetryEvents(Long[] eventIds, Long userId);

    /**
     * 获取未处理的事件列表
     *
     * @param projectId 项目ID
     * @param limit 限制数量
     * @return 事件列表
     */
    List<WebhookEvent> getPendingEvents(Long projectId, Integer limit);

    /**
     * 获取处理中的事件列表
     *
     * @param projectId 项目ID
     * @return 事件列表
     */
    List<WebhookEvent> getProcessingEvents(Long projectId);

    /**
     * 获取失败的事件列表
     *
     * @param projectId 项目ID
     * @param days 天数
     * @param limit 限制数量
     * @return 事件列表
     */
    List<WebhookEvent> getFailedEvents(Long projectId, Integer days, Integer limit);

    /**
     * 根据仓库查询事件
     *
     * @param repositoryUrl 仓库URL
     * @param eventType 事件类型
     * @param limit 限制数量
     * @return 事件列表
     */
    List<WebhookEvent> getEventsByRepository(String repositoryUrl, String eventType, Integer limit);

    /**
     * 根据分支查询事件
     *
     * @param projectId 项目ID
     * @param branch 分支名称
     * @param eventType 事件类型
     * @param limit 限制数量
     * @return 事件列表
     */
    List<WebhookEvent> getEventsByBranch(Long projectId, String branch, String eventType, Integer limit);

    /**
     * 根据提交SHA查询事件
     *
     * @param commitSha 提交SHA
     * @return 事件列表
     */
    List<WebhookEvent> getEventsByCommitSha(String commitSha);

    /**
     * 获取事件统计信息
     *
     * @param projectId 项目ID
     * @param timeRange 时间范围
     * @return 统计信息
     */
    Map<String, Object> getEventStatistics(Long projectId, String timeRange);

    /**
     * 获取事件类型分布
     *
     * @param projectId 项目ID
     * @param timeRange 时间范围
     * @return 类型分布
     */
    List<Map<String, Object>> getEventTypeDistribution(Long projectId, String timeRange);

    /**
     * 获取事件源分布
     *
     * @param projectId 项目ID
     * @param timeRange 时间范围
     * @return 源分布
     */
    List<Map<String, Object>> getEventSourceDistribution(Long projectId, String timeRange);

    /**
     * 获取事件处理时间统计
     *
     * @param projectId 项目ID
     * @param timeRange 时间范围
     * @return 处理时间统计
     */
    List<Map<String, Object>> getEventProcessingTimeStats(Long projectId, String timeRange);

    /**
     * 获取失败原因统计
     *
     * @param projectId 项目ID
     * @param timeRange 时间范围
     * @return 失败原因统计
     */
    List<Map<String, Object>> getFailureReasonStats(Long projectId, String timeRange);

    /**
     * 获取每日事件统计
     *
     * @param projectId 项目ID
     * @param days 天数
     * @return 每日统计
     */
    List<Map<String, Object>> getDailyEventStats(Long projectId, Integer days);

    /**
     * 获取仓库活跃度
     *
     * @param projectId 项目ID
     * @param timeRange 时间范围
     * @return 仓库活跃度
     */
    List<Map<String, Object>> getRepositoryActivity(Long projectId, String timeRange);

    /**
     * 获取触发器性能统计
     *
     * @param projectId 项目ID
     * @param timeRange 时间范围
     * @return 性能统计
     */
    Map<String, Object> getTriggerPerformanceStats(Long projectId, String timeRange);

    /**
     * 获取重复事件
     *
     * @param projectId 项目ID
     * @param timeWindow 时间窗口（分钟）
     * @return 重复事件列表
     */
    List<Map<String, Object>> getDuplicateEvents(Long projectId, Integer timeWindow);

    /**
     * 获取事件处理队列
     *
     * @param projectId 项目ID
     * @return 队列中的事件
     */
    List<WebhookEvent> getEventQueue(Long projectId);

    /**
     * 获取事件重试记录
     *
     * @param originalEventId 原始事件ID
     * @return 重试记录
     */
    List<WebhookEvent> getEventRetries(Long originalEventId);

    /**
     * 获取事件关联的流水线执行
     *
     * @param eventId 事件ID
     * @return 关联的执行记录
     */
    List<Map<String, Object>> getRelatedExecutions(Long eventId);

    /**
     * 获取触发器配置
     *
     * @param projectId 项目ID
     * @param triggerType 触发器类型
     * @return 触发器配置
     */
    Map<String, Object> getTriggerConfig(Long projectId, String triggerType);

    /**
     * 更新触发器配置
     *
     * @param projectId 项目ID
     * @param triggerType 触发器类型
     * @param config 配置
     */
    void updateTriggerConfig(Long projectId, String triggerType, Map<String, Object> config);

    /**
     * 获取事件处理历史
     *
     * @param eventId 事件ID
     * @return 处理历史
     */
    List<Map<String, Object>> getEventProcessingHistory(Long eventId);

    /**
     * 清理过期事件
     *
     * @param projectId 项目ID
     * @param days 保留天数
     * @return 清理数量
     */
    int cleanExpiredEvents(Long projectId, Integer days);

    /**
     * 归档事件
     *
     * @param eventId 事件ID
     */
    void archiveEvent(Long eventId);

    /**
     * 获取归档事件
     *
     * @param projectId 项目ID
     * @param limit 限制数量
     * @return 归档事件列表
     */
    List<WebhookEvent> getArchivedEvents(Long projectId, Integer limit);

    /**
     * 恢复归档事件
     *
     * @param eventId 事件ID
     */
    void restoreArchivedEvent(Long eventId);

    /**
     * 获取事件监控指标
     *
     * @param projectId 项目ID
     * @param timeRange 时间范围
     * @return 监控指标
     */
    Map<String, Object> getEventMonitoringMetrics(Long projectId, String timeRange);

    /**
     * 获取异常事件模式
     *
     * @param projectId 项目ID
     * @param timeRange 时间范围
     * @return 异常模式
     */
    List<Map<String, Object>> getAbnormalEventPatterns(Long projectId, String timeRange);

    /**
     * 获取事件负载情况
     *
     * @param projectId 项目ID
     * @param timeRange 时间范围
     * @return 负载情况
     */
    List<Map<String, Object>> getEventLoadStats(Long projectId, String timeRange);

    /**
     * 获取事件处理能力
     *
     * @param projectId 项目ID
     * @return 处理能力指标
     */
    Map<String, Object> getEventProcessingCapacity(Long projectId);

    /**
     * 获取事件关联关系
     *
     * @param eventId 事件ID
     * @return 关联事件
     */
    List<WebhookEvent> getRelatedEvents(Long eventId);

    /**
     * 获取根事件
     *
     * @param eventId 事件ID
     * @return 根事件
     */
    WebhookEvent getRootEvent(Long eventId);

    /**
     * 获取事件链
     *
     * @param rootEventId 根事件ID
     * @return 事件链
     */
    List<WebhookEvent> getEventChain(Long rootEventId);

    /**
     * 批量更新事件状态
     *
     * @param eventIds 事件ID列表
     * @param status 状态
     * @return 更新数量
     */
    int batchUpdateEventStatus(List<Long> eventIds, String status);

    /**
     * 忽略事件
     *
     * @param eventId 事件ID
     */
    void ignoreEvent(Long eventId);

    /**
     * 批量忽略事件
     *
     * @param eventIds 事件ID列表
     * @return 忽略数量
     */
    int batchIgnoreEvents(Long[] eventIds);
}