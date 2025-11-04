package com.autotest.platform.websocket;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.*;

import java.io.IOException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArraySet;

/**
 * 报告WebSocket处理器
 *
 * @author autotest
 * @date 2024-01-01
 */
@Slf4j
@Component
public class ReportWebSocketHandler implements WebSocketHandler {

    @Autowired
    private ObjectMapper objectMapper;

    // 存储会话信息
    private Map<String, WebSocketSession> sessions = new ConcurrentHashMap<>();
    // 存储订阅关系
    private Map<String, CopyOnWriteArraySet<String>> subscriptions = new ConcurrentHashMap<>();

    @Override
    public void afterConnectionEstablished(WebSocketSession session) throws Exception {
        String sessionId = session.getId();
        sessions.put(sessionId, session);
        log.info("WebSocket connection established: {}", sessionId);

        // 发送连接成功消息
        sendMessage(sessionId, createMessage("CONNECTION", "WebSocket连接已建立", null));
    }

    @Override
    public void handleMessage(WebSocketSession session, WebSocketMessage<?> message) throws Exception {
        String sessionId = session.getId();
        String payload = message.getPayload().toString();

        try {
            @SuppressWarnings("unchecked")
            Map<String, Object> request = objectMapper.readValue(payload, Map.class);

            String type = (String) request.get("type");
            String action = (String) request.get("action");

            log.debug("Received WebSocket message: {} from session: {}", type, sessionId);

            switch (type) {
                case "SUBSCRIBE":
                    handleSubscribe(sessionId, request);
                    break;
                case "UNSUBSCRIBE":
                    handleUnsubscribe(sessionId, request);
                    break;
                case "GET_STATUS":
                    handleGetStatus(sessionId, request);
                    break;
                case "PING":
                    handlePing(sessionId);
                    break;
                default:
                    log.warn("Unknown message type: {}", type);
            }

        } catch (Exception e) {
            log.error("Failed to handle WebSocket message", e);
            sendMessage(sessionId, createMessage("ERROR", "消息处理失败", e.getMessage()));
        }
    }

    @Override
    public void handleTransportError(WebSocketSession session, Throwable exception) throws Exception {
        String sessionId = session.getId();
        log.error("WebSocket transport error for session: {}", sessionId, exception);
        cleanupSession(sessionId);
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus closeStatus) throws Exception {
        String sessionId = session.getId();
        log.info("WebSocket connection closed: {} - {}", sessionId, closeStatus);
        cleanupSession(sessionId);
    }

    @Override
    public boolean supportsPartialMessages() {
        return false;
    }

    /**
     * 处理订阅请求
     */
    private void handleSubscribe(String sessionId, Map<String, Object> request) {
        String channel = (String) request.get("channel");
        String reportId = (String) request.get("reportId");

        if (channel != null && reportId != null) {
            String subscriptionKey = channel + ":" + reportId;
            subscriptions.computeIfAbsent(subscriptionKey, k -> new CopyOnWriteArraySet<>()).add(sessionId);

            sendMessage(sessionId, createMessage("SUBSCRIBE_SUCCESS", "订阅成功",
                Map.of("channel", channel, "reportId", reportId)));

            log.info("Session {} subscribed to {}:{}", sessionId, channel, reportId);
        }
    }

    /**
     * 处理取消订阅请求
     */
    private void handleUnsubscribe(String sessionId, Map<String, Object> request) {
        String channel = (String) request.get("channel");
        String reportId = (String) request.get("reportId");

        if (channel != null && reportId != null) {
            String subscriptionKey = channel + ":" + reportId;
            CopyOnWriteArraySet<String> subscribers = subscriptions.get(subscriptionKey);
            if (subscribers != null) {
                subscribers.remove(sessionId);
                if (subscribers.isEmpty()) {
                    subscriptions.remove(subscriptionKey);
                }
            }

            sendMessage(sessionId, createMessage("UNSUBSCRIBE_SUCCESS", "取消订阅成功",
                Map.of("channel", channel, "reportId", reportId)));

            log.info("Session {} unsubscribed from {}:{}", sessionId, channel, reportId);
        }
    }

    /**
     * 处理状态查询请求
     */
    private void handleGetStatus(String sessionId, Map<String, Object> request) {
        String reportId = (String) request.get("reportId");

        // TODO: 查询报告生成状态
        String status = "COMPLETED"; // 临时状态
        int progress = 100;

        Map<String, Object> statusData = new java.util.HashMap<>();
        statusData.put("reportId", reportId);
        statusData.put("status", status);
        statusData.put("progress", progress);

        sendMessage(sessionId, createMessage("STATUS_UPDATE", "状态更新", statusData));
    }

    /**
     * 处理心跳请求
     */
    private void handlePing(String sessionId) {
        sendMessage(sessionId, createMessage("PONG", "心跳响应", System.currentTimeMillis()));
    }

    /**
     * 发送消息给指定会话
     */
    public boolean sendMessage(String sessionId, Object message) {
        WebSocketSession session = sessions.get(sessionId);
        if (session != null && session.isOpen()) {
            try {
                String messageJson = objectMapper.writeValueAsString(message);
                session.sendMessage(new TextMessage(messageJson));
                return true;
            } catch (Exception e) {
                log.error("Failed to send message to session: {}", sessionId, e);
                return false;
            }
        }
        return false;
    }

    /**
     * 广播消息给所有订阅者
     */
    @Async
    public void broadcast(String channel, String reportId, Object message) {
        String subscriptionKey = channel + ":" + reportId;
        CopyOnWriteArraySet<String> subscribers = subscriptions.get(subscriptionKey);

        if (subscribers != null && !subscribers.isEmpty()) {
            for (String sessionId : subscribers) {
                sendMessage(sessionId, message);
            }
            log.debug("Broadcasted message to {} subscribers for {}:{}",
                subscribers.size(), channel, reportId);
        }
    }

    /**
     * 发送报告生成进度更新
     */
    @Async
    public void sendProgressUpdate(String reportId, int progress, String message) {
        Map<String, Object> progressData = new java.util.HashMap<>();
        progressData.put("reportId", reportId);
        progressData.put("progress", progress);
        progressData.put("message", message);
        progressData.put("timestamp", System.currentTimeMillis());

        Object progressMessage = createMessage("PROGRESS_UPDATE", "生成进度更新", progressData);
        broadcast("REPORT_GENERATION", reportId, progressMessage);
    }

    /**
     * 发送报告生成完成通知
     */
    @Async
    public void sendReportCompleted(String reportId, String reportUrl, boolean success) {
        Map<String, Object> completedData = new java.util.HashMap<>();
        completedData.put("reportId", reportId);
        completedData.put("reportUrl", reportUrl);
        completedData.put("success", success);
        completedData.put("timestamp", System.currentTimeMillis());

        Object completedMessage = createMessage("REPORT_COMPLETED",
            success ? "报告生成完成" : "报告生成失败", completedData);
        broadcast("REPORT_GENERATION", reportId, completedMessage);
    }

    /**
     * 发送执行进度更新
     */
    @Async
    public void sendExecutionProgress(String executionCode, int completedCases, int totalCases, String currentStatus) {
        Map<String, Object> progressData = new java.util.HashMap<>();
        progressData.put("executionCode", executionCode);
        progressData.put("completedCases", completedCases);
        progressData.put("totalCases", totalCases);
        progressData.put("progress", totalCases > 0 ? (int) ((double) completedCases / totalCases * 100) : 0);
        progressData.put("currentStatus", currentStatus);
        progressData.put("timestamp", System.currentTimeMillis());

        Object progressMessage = createMessage("EXECUTION_PROGRESS", "执行进度更新", progressData);
        broadcast("EXECUTION_MONITOR", executionCode, progressMessage);
    }

    /**
     * 发送执行状态变更通知
     */
    @Async
    public void sendExecutionStatusChange(String executionCode, String oldStatus, String newStatus) {
        Map<String, Object> statusData = new java.util.HashMap<>();
        statusData.put("executionCode", executionCode);
        statusData.put("oldStatus", oldStatus);
        statusData.put("newStatus", newStatus);
        statusData.put("timestamp", System.currentTimeMillis());

        Object statusMessage = createMessage("EXECUTION_STATUS_CHANGE", "执行状态变更", statusData);
        broadcast("EXECUTION_MONITOR", executionCode, statusMessage);
    }

    /**
     * 获取在线会话数量
     */
    public int getOnlineSessionCount() {
        return (int) sessions.values().stream()
            .filter(WebSocketSession::isOpen)
            .count();
    }

    /**
     * 获取订阅统计
     */
    public Map<String, Integer> getSubscriptionStats() {
        Map<String, Integer> stats = new java.util.HashMap<>();
        for (Map.Entry<String, CopyOnWriteArraySet<String>> entry : subscriptions.entrySet()) {
            stats.put(entry.getKey(), entry.getValue().size());
        }
        return stats;
    }

    /**
     * 清理会话
     */
    private void cleanupSession(String sessionId) {
        sessions.remove(sessionId);

        // 清理订阅关系
        for (Map.Entry<String, CopyOnWriteArraySet<String>> entry : subscriptions.entrySet()) {
            entry.getValue().remove(sessionId);
            if (entry.getValue().isEmpty()) {
                subscriptions.remove(entry.getKey());
            }
        }
    }

    /**
     * 创建消息对象
     */
    private Object createMessage(String type, String message, Object data) {
        Map<String, Object> msg = new java.util.HashMap<>();
        msg.put("type", type);
        msg.put("message", message);
        msg.put("timestamp", System.currentTimeMillis());
        if (data != null) {
            msg.put("data", data);
        }
        return msg;
    }
}