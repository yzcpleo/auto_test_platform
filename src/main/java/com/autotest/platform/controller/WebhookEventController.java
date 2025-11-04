package com.autotest.platform.controller;

import com.autotest.platform.common.core.controller.BaseController;
import com.autotest.platform.common.core.domain.AjaxResult;
import com.autotest.platform.common.core.page.TableDataInfo;
import com.autotest.platform.domain.cicd.WebhookEvent;
import com.autotest.platform.service.IWebhookEventService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * Webhook事件Controller
 *
 * @author autotest
 * @date 2024-01-01
 */
@RestController
@RequestMapping("/cicd/webhook")
public class WebhookEventController extends BaseController {

    @Autowired
    private IWebhookEventService webhookEventService;

    /**
     * 查询Webhook事件列表
     */
    @PreAuthorize("@ss.hasPermi('cicd:webhook:list')")
    @GetMapping("/list")
    public TableDataInfo list(WebhookEvent webhookEvent) {
        startPage();
        List<WebhookEvent> list = webhookEventService.selectWebhookEventList(webhookEvent);
        return getDataTable(list);
    }

    /**
     * 获取Webhook事件详细信息
     */
    @PreAuthorize("@ss.hasPermi('cicd:webhook:query')")
    @GetMapping(value = "/{eventId}")
    public AjaxResult getInfo(@PathVariable("eventId") Long eventId) {
        return success(webhookEventService.selectWebhookEventByEventId(eventId));
    }

    /**
     * 删除Webhook事件
     */
    @PreAuthorize("@ss.hasPermi('cicd:webhook:remove')")
    @DeleteMapping("/{eventIds}")
    public AjaxResult remove(@PathVariable Long[] eventIds) {
        return toAjax(webhookEventService.deleteWebhookEventByEventIds(eventIds));
    }

    /**
     * 重新处理事件
     */
    @PreAuthorize("@ss.hasPermi('cicd:webhook:retry')")
    @PostMapping("/{eventId}/retry")
    public AjaxResult retryEvent(@PathVariable Long eventId) {
        try {
            WebhookEvent result = webhookEventService.retryEvent(eventId, getUserId());
            return success("事件重新处理已启动", result);
        } catch (Exception e) {
            return error("重新处理事件失败: " + e.getMessage());
        }
    }

    /**
     * 批量重新处理事件
     */
    @PreAuthorize("@ss.hasPermi('cicd:webhook:retry')")
    @PostMapping("/batch-retry")
    public AjaxResult batchRetryEvents(@RequestBody Long[] eventIds) {
        try {
            List<Map<String, Object>> results = webhookEventService.batchRetryEvents(eventIds, getUserId());
            return success("批量重新处理已启动", Map.of("results", results));
        } catch (Exception e) {
            return error("批量重新处理失败: " + e.getMessage());
        }
    }

    /**
     * 获取未处理的事件列表
     */
    @PreAuthorize("@ss.hasPermi('cicd:webhook:list')")
    @GetMapping("/pending")
    public AjaxResult getPendingEvents(@RequestParam(required = false) Long projectId,
                                      @RequestParam(defaultValue = "10") Integer limit) {
        List<WebhookEvent> events = webhookEventService.getPendingEvents(projectId, limit);
        return success(events);
    }

    /**
     * 获取处理中的事件列表
     */
    @PreAuthorize("@ss.hasPermi('cicd:webhook:list')")
    @GetMapping("/processing")
    public AjaxResult getProcessingEvents(@RequestParam(required = false) Long projectId) {
        List<WebhookEvent> events = webhookEventService.getProcessingEvents(projectId);
        return success(events);
    }

    /**
     * 获取失败的事件列表
     */
    @PreAuthorize("@ss.hasPermi('cicd:webhook:list')")
    @GetMapping("/failed")
    public AjaxResult getFailedEvents(@RequestParam(required = false) Long projectId,
                                     @RequestParam(defaultValue = "7") Integer days,
                                     @RequestParam(defaultValue = "20") Integer limit) {
        List<WebhookEvent> events = webhookEventService.getFailedEvents(projectId, days, limit);
        return success(events);
    }

    /**
     * 根据仓库查询事件
     */
    @PreAuthorize("@ss.hasPermi('cicd:webhook:query')")
    @GetMapping("/repository")
    public AjaxResult getEventsByRepository(@RequestParam String repositoryUrl,
                                           @RequestParam(required = false) String eventType,
                                           @RequestParam(defaultValue = "20") Integer limit) {
        List<WebhookEvent> events = webhookEventService.getEventsByRepository(repositoryUrl, eventType, limit);
        return success(events);
    }

    /**
     * 根据分支查询事件
     */
    @PreAuthorize("@ss.hasPermi('cicd:webhook:query')")
    @GetMapping("/branch")
    public AjaxResult getEventsByBranch(@RequestParam Long projectId,
                                       @RequestParam String branch,
                                       @RequestParam(required = false) String eventType,
                                       @RequestParam(defaultValue = "20") Integer limit) {
        List<WebhookEvent> events = webhookEventService.getEventsByBranch(projectId, branch, eventType, limit);
        return success(events);
    }

    /**
     * 根据提交SHA查询事件
     */
    @PreAuthorize("@ss.hasPermi('cicd:webhook:query')")
    @GetMapping("/commit")
    public AjaxResult getEventsByCommitSha(@RequestParam String commitSha) {
        List<WebhookEvent> events = webhookEventService.getEventsByCommitSha(commitSha);
        return success(events);
    }

    /**
     * 获取事件统计信息
     */
    @PreAuthorize("@ss.hasPermi('cicd:webhook:statistics')")
    @GetMapping("/statistics/{projectId}")
    public AjaxResult getEventStatistics(@PathVariable Long projectId,
                                        @RequestParam(required = false) String timeRange) {
        Map<String, Object> statistics = webhookEventService.getEventStatistics(projectId, timeRange);
        return success(statistics);
    }

    /**
     * 获取事件类型分布
     */
    @PreAuthorize("@ss.hasPermi('cicd:webhook:statistics')")
    @GetMapping("/distribution/type/{projectId}")
    public AjaxResult getEventTypeDistribution(@PathVariable Long projectId,
                                              @RequestParam(required = false) String timeRange) {
        List<Map<String, Object>> distribution = webhookEventService.getEventTypeDistribution(projectId, timeRange);
        return success(distribution);
    }

    /**
     * 获取事件源分布
     */
    @PreAuthorize("@ss.hasPermi('cicd:webhook:statistics')")
    @GetMapping("/distribution/source/{projectId}")
    public AjaxResult getEventSourceDistribution(@PathVariable Long projectId,
                                                @RequestParam(required = false) String timeRange) {
        List<Map<String, Object>> distribution = webhookEventService.getEventSourceDistribution(projectId, timeRange);
        return success(distribution);
    }

    /**
     * 获取事件处理时间统计
     */
    @PreAuthorize("@ss.hasPermi('cicd:webhook:statistics')")
    @GetMapping("/performance/{projectId}")
    public AjaxResult getEventProcessingTimeStats(@PathVariable Long projectId,
                                                 @RequestParam(required = false) String timeRange) {
        List<Map<String, Object>> stats = webhookEventService.getEventProcessingTimeStats(projectId, timeRange);
        return success(stats);
    }

    /**
     * 获取失败原因统计
     */
    @PreAuthorize("@ss.hasPermi('cicd:webhook:statistics')")
    @GetMapping("/failures/{projectId}")
    public AjaxResult getFailureReasonStats(@PathVariable Long projectId,
                                           @RequestParam(required = false) String timeRange) {
        List<Map<String, Object>> stats = webhookEventService.getFailureReasonStats(projectId, timeRange);
        return success(stats);
    }

    /**
     * 获取每日事件统计
     */
    @PreAuthorize("@ss.hasPermi('cicd:webhook:statistics')")
    @GetMapping("/daily/{projectId}")
    public AjaxResult getDailyEventStats(@PathVariable Long projectId,
                                        @RequestParam(defaultValue = "7") Integer days) {
        List<Map<String, Object>> stats = webhookEventService.getDailyEventStats(projectId, days);
        return success(stats);
    }

    /**
     * 获取仓库活跃度
     */
    @PreAuthorize("@ss.hasPermi('cicd:webhook:statistics')")
    @GetMapping("/activity/{projectId}")
    public AjaxResult getRepositoryActivity(@PathVariable Long projectId,
                                           @RequestParam(required = false) String timeRange) {
        List<Map<String, Object>> activity = webhookEventService.getRepositoryActivity(projectId, timeRange);
        return success(activity);
    }

    /**
     * 获取触发器性能统计
     */
    @PreAuthorize("@ss.hasPermi('cicd:webhook:statistics')")
    @GetMapping("/trigger-performance/{projectId}")
    public AjaxResult getTriggerPerformanceStats(@PathVariable Long projectId,
                                                @RequestParam(required = false) String timeRange) {
        Map<String, Object> stats = webhookEventService.getTriggerPerformanceStats(projectId, timeRange);
        return success(stats);
    }

    /**
     * 获取重复事件
     */
    @PreAuthorize("@ss.hasPermi('cicd:webhook:monitor')")
    @GetMapping("/duplicates/{projectId}")
    public AjaxResult getDuplicateEvents(@PathVariable Long projectId,
                                        @RequestParam(defaultValue = "5") Integer timeWindow) {
        List<Map<String, Object>> duplicates = webhookEventService.getDuplicateEvents(projectId, timeWindow);
        return success(duplicates);
    }

    /**
     * 获取事件处理队列
     */
    @PreAuthorize("@ss.hasPermi('cicd:webhook:monitor')")
    @GetMapping("/queue/{projectId}")
    public AjaxResult getEventQueue(@PathVariable Long projectId) {
        List<WebhookEvent> queue = webhookEventService.getEventQueue(projectId);
        return success(queue);
    }

    /**
     * 获取事件重试记录
     */
    @PreAuthorize("@ss.hasPermi('cicd:webhook:query')")
    @GetMapping("/{eventId}/retries")
    public AjaxResult getEventRetries(@PathVariable Long eventId) {
        List<WebhookEvent> retries = webhookEventService.getEventRetries(eventId);
        return success(retries);
    }

    /**
     * 获取事件关联的执行记录
     */
    @PreAuthorize("@ss.hasPermi('cicd:webhook:query')")
    @GetMapping("/{eventId}/executions")
    public AjaxResult getRelatedExecutions(@PathVariable Long eventId) {
        List<Map<String, Object>> executions = webhookEventService.getRelatedExecutions(eventId);
        return success(executions);
    }

    /**
     * 获取触发器配置
     */
    @PreAuthorize("@ss.hasPermi('cicd:webhook:config')")
    @GetMapping("/trigger-config/{projectId}")
    public AjaxResult getTriggerConfig(@PathVariable Long projectId,
                                      @RequestParam String triggerType) {
        Map<String, Object> config = webhookEventService.getTriggerConfig(projectId, triggerType);
        return success(config);
    }

    /**
     * 更新触发器配置
     */
    @PreAuthorize("@ss.hasPermi('cicd:webhook:config')")
    @PutMapping("/trigger-config/{projectId}")
    public AjaxResult updateTriggerConfig(@PathVariable Long projectId,
                                         @RequestParam String triggerType,
                                         @RequestBody Map<String, Object> config) {
        try {
            webhookEventService.updateTriggerConfig(projectId, triggerType, config);
            return success("触发器配置更新成功");
        } catch (Exception e) {
            return error("更新触发器配置失败: " + e.getMessage());
        }
    }

    /**
     * 获取事件处理历史
     */
    @PreAuthorize("@ss.hasPermi('cicd:webhook:query')")
    @GetMapping("/{eventId}/history")
    public AjaxResult getEventProcessingHistory(@PathVariable Long eventId) {
        List<Map<String, Object>> history = webhookEventService.getEventProcessingHistory(eventId);
        return success(history);
    }

    /**
     * 清理过期事件
     */
    @PreAuthorize("@ss.hasPermi('cicd:webhook:cleanup')")
    @PostMapping("/cleanup/{projectId}")
    public AjaxResult cleanExpiredEvents(@PathVariable Long projectId,
                                        @RequestParam(defaultValue = "30") Integer days) {
        try {
            int cleanedCount = webhookEventService.cleanExpiredEvents(projectId, days);
            return success("清理完成", Map.of("cleanedCount", cleanedCount));
        } catch (Exception e) {
            return error("清理过期事件失败: " + e.getMessage());
        }
    }

    /**
     * 归档事件
     */
    @PreAuthorize("@ss.hasPermi('cicd:webhook:archive')")
    @PostMapping("/{eventId}/archive")
    public AjaxResult archiveEvent(@PathVariable Long eventId) {
        try {
            webhookEventService.archiveEvent(eventId);
            return success("事件归档成功");
        } catch (Exception e) {
            return error("归档事件失败: " + e.getMessage());
        }
    }

    /**
     * 获取归档事件列表
     */
    @PreAuthorize("@ss.hasPermi('cicd:webhook:list')")
    @GetMapping("/archived/{projectId}")
    public AjaxResult getArchivedEvents(@PathVariable Long projectId,
                                        @RequestParam(defaultValue = "20") Integer limit) {
        List<WebhookEvent> events = webhookEventService.getArchivedEvents(projectId, limit);
        return success(events);
    }

    /**
     * 恢复归档事件
     */
    @PreAuthorize("@ss.hasPermi('cicd:webhook:archive')")
    @PostMapping("/{eventId}/restore")
    public AjaxResult restoreArchivedEvent(@PathVariable Long eventId) {
        try {
            webhookEventService.restoreArchivedEvent(eventId);
            return success("归档事件恢复成功");
        } catch (Exception e) {
            return error("恢复归档事件失败: " + e.getMessage());
        }
    }

    /**
     * 获取事件监控指标
     */
    @PreAuthorize("@ss.hasPermi('cicd:webhook:monitor')")
    @GetMapping("/monitoring/{projectId}")
    public AjaxResult getEventMonitoringMetrics(@PathVariable Long projectId,
                                               @RequestParam(required = false) String timeRange) {
        Map<String, Object> metrics = webhookEventService.getEventMonitoringMetrics(projectId, timeRange);
        return success(metrics);
    }

    /**
     * 获取异常事件模式
     */
    @PreAuthorize("@ss.hasPermi('cicd:webhook:monitor')")
    @GetMapping("/patterns/{projectId}")
    public AjaxResult getAbnormalEventPatterns(@PathVariable Long projectId,
                                              @RequestParam(required = false) String timeRange) {
        List<Map<String, Object>> patterns = webhookEventService.getAbnormalEventPatterns(projectId, timeRange);
        return success(patterns);
    }

    /**
     * 获取事件负载情况
     */
    @PreAuthorize("@ss.hasPermi('cicd:webhook:monitor')")
    @GetMapping("/load/{projectId}")
    public AjaxResult getEventLoadStats(@PathVariable Long projectId,
                                       @RequestParam(required = false) String timeRange) {
        List<Map<String, Object>> loadStats = webhookEventService.getEventLoadStats(projectId, timeRange);
        return success(loadStats);
    }

    /**
     * 获取事件处理能力
     */
    @PreAuthorize("@ss.hasPermi('cicd:webhook:monitor')")
    @GetMapping("/capacity/{projectId}")
    public AjaxResult getEventProcessingCapacity(@PathVariable Long projectId) {
        Map<String, Object> capacity = webhookEventService.getEventProcessingCapacity(projectId);
        return success(capacity);
    }

    /**
     * 获取事件关联关系
     */
    @PreAuthorize("@ss.hasPermi('cicd:webhook:query')")
    @GetMapping("/{eventId}/related")
    public AjaxResult getRelatedEvents(@PathVariable Long eventId) {
        List<WebhookEvent> relatedEvents = webhookEventService.getRelatedEvents(eventId);
        return success(relatedEvents);
    }

    /**
     * 获取根事件
     */
    @PreAuthorize("@ss.hasPermi('cicd:webhook:query')")
    @GetMapping("/{eventId}/root")
    public AjaxResult getRootEvent(@PathVariable Long eventId) {
        WebhookEvent rootEvent = webhookEventService.getRootEvent(eventId);
        return success(rootEvent);
    }

    /**
     * 获取事件链
     */
    @PreAuthorize("@ss.hasPermi('cicd:webhook:query')")
    @GetMapping("/chain/{rootEventId}")
    public AjaxResult getEventChain(@PathVariable Long rootEventId) {
        List<WebhookEvent> eventChain = webhookEventService.getEventChain(rootEventId);
        return success(eventChain);
    }

    /**
     * 批量更新事件状态
     */
    @PreAuthorize("@ss.hasPermi('cicd:webhook:edit')")
    @PutMapping("/status")
    public AjaxResult batchUpdateEventStatus(@RequestBody Map<String, Object> params) {
        try {
            @SuppressWarnings("unchecked")
            List<Long> eventIds = (List<Long>) params.get("eventIds");
            String status = (String) params.get("status");

            int count = webhookEventService.batchUpdateEventStatus(eventIds, status);
            return success("批量更新状态成功", Map.of("updatedCount", count));
        } catch (Exception e) {
            return error("批量更新状态失败: " + e.getMessage());
        }
    }

    /**
     * 忽略事件
     */
    @PreAuthorize("@ss.hasPermi('cicd:webhook:edit')")
    @PostMapping("/{eventId}/ignore")
    public AjaxResult ignoreEvent(@PathVariable Long eventId) {
        try {
            webhookEventService.ignoreEvent(eventId);
            return success("事件已忽略");
        } catch (Exception e) {
            return error("忽略事件失败: " + e.getMessage());
        }
    }

    /**
     * 批量忽略事件
     */
    @PreAuthorize("@ss.hasPermi('cicd:webhook:edit')")
    @PostMapping("/batch-ignore")
    public AjaxResult batchIgnoreEvents(@RequestBody Long[] eventIds) {
        try {
            int count = webhookEventService.batchIgnoreEvents(eventIds);
            return success("批量忽略成功", Map.of("ignoredCount", count));
        } catch (Exception e) {
            return error("批量忽略失败: " + e.getMessage());
        }
    }
}