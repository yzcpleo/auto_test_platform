package com.autotest.platform.execution.monitor;

import com.autotest.platform.domain.testcase.TestExecution;
import com.autotest.platform.execution.parallel.ParallelExecutionController;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * 测试执行监控器
 *
 * @author autotest
 * @date 2024-01-01
 */
@Slf4j
@Component
public class ExecutionMonitor {

    @Autowired
    private ParallelExecutionController parallelExecutionController;

    private Map<String, ExecutionSession> activeSessions = new ConcurrentHashMap<>();
    private ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(2);
    private List<ExecutionListener> listeners = new ArrayList<>();

    /**
     * 初始化监控器
     */
    public void initialize() {
        // 启动定时监控任务
        scheduler.scheduleAtFixedRate(this::monitorActiveExecutions, 5, 5, TimeUnit.SECONDS);
        scheduler.scheduleAtFixedRate(this::cleanupExpiredSessions, 60, 60, TimeUnit.SECONDS);
        log.info("ExecutionMonitor initialized");
    }

    /**
     * 开始监控执行
     */
    public void startMonitoring(TestExecution execution) {
        ExecutionSession session = new ExecutionSession();
        session.setExecutionId(execution.getExecutionId());
        session.setExecutionCode(execution.getExecutionCode());
        session.setStartTime(new Date());
        session.setStatus("RUNNING");
        session.setTotalCases(execution.getTotalCases());
        session.setCompletedCases(0);
        session.setSuccessCases(0);
        session.setFailedCases(0);
        session.setLastUpdateTime(new Date());

        activeSessions.put(execution.getExecutionCode(), session);

        // 通知监听器
        notifyExecutionStarted(execution, session);

        log.info("Started monitoring execution: {}", execution.getExecutionCode());
    }

    /**
     * 更新执行进度
     */
    public void updateProgress(String executionCode, int completedCases, int successCases, int failedCases) {
        ExecutionSession session = activeSessions.get(executionCode);
        if (session != null) {
            session.setCompletedCases(completedCases);
            session.setSuccessCases(successCases);
            session.setFailedCases(failedCases);
            session.setLastUpdateTime(new Date());

            // 计算进度百分比
            int progress = session.getTotalCases() > 0 ?
                (int) ((double) completedCases / session.getTotalCases() * 100) : 0;
            session.setProgress(progress);

            // 通知监听器
            notifyProgressUpdated(executionCode, session);
        }
    }

    /**
     * 完成监控
     */
    public void completeMonitoring(String executionCode, String status, String errorMessage) {
        ExecutionSession session = activeSessions.get(executionCode);
        if (session != null) {
            session.setStatus(status);
            session.setEndTime(new Date());
            session.setLastUpdateTime(new Date());
            session.setErrorMessage(errorMessage);

            // 计算最终进度
            session.setProgress(100);

            // 通知监听器
            notifyExecutionCompleted(executionCode, session);

            log.info("Completed monitoring execution: {} with status: {}", executionCode, status);
        }
    }

    /**
     * 添加监听器
     */
    public void addListener(ExecutionListener listener) {
        listeners.add(listener);
    }

    /**
     * 移除监听器
     */
    public void removeListener(ExecutionListener listener) {
        listeners.remove(listener);
    }

    /**
     * 获取活跃会话
     */
    public List<ExecutionSession> getActiveSessions() {
        return new ArrayList<>(activeSessions.values());
    }

    /**
     * 获取执行会话
     */
    public ExecutionSession getSession(String executionCode) {
        return activeSessions.get(executionCode);
    }

    /**
     * 获取监控统计
     */
    public MonitorStatistics getStatistics() {
        MonitorStatistics stats = new MonitorStatistics();
        stats.setTotalActiveSessions(activeSessions.size());
        stats.setRunningSessions((int) activeSessions.values().stream()
            .filter(s -> "RUNNING".equals(s.getStatus())).count());
        stats.setCompletedSessions((int) activeSessions.values().stream()
            .filter(s -> Arrays.asList("SUCCESS", "FAILED", "CANCELLED").contains(s.getStatus())).count());
        stats.setTotalCases(activeSessions.values().stream()
            .mapToInt(ExecutionSession::getTotalCases).sum());
        stats.setCompletedCases(activeSessions.values().stream()
            .mapToInt(ExecutionSession::getCompletedCases).sum());
        stats.setSuccessCases(activeSessions.values().stream()
            .mapToInt(ExecutionSession::getSuccessCases).sum());
        stats.setFailedCases(activeSessions.values().stream()
            .mapToInt(ExecutionSession::getFailedCases).sum());

        return stats;
    }

    /**
     * 停止监控
     */
    public void stopMonitoring(String executionCode) {
        ExecutionSession session = activeSessions.remove(executionCode);
        if (session != null) {
            session.setStatus("CANCELLED");
            session.setEndTime(new Date());
            notifyExecutionStopped(executionCode, session);
            log.info("Stopped monitoring execution: {}", executionCode);
        }
    }

    /**
     * 监控活跃执行
     */
    private void monitorActiveExecutions() {
        try {
            for (ExecutionSession session : new ArrayList<>(activeSessions.values())) {
                if ("RUNNING".equals(session.getStatus())) {
                    // 检查是否超时
                    long runningTime = System.currentTimeMillis() - session.getStartTime().getTime();
                    if (runningTime > 2 * 60 * 60 * 1000) { // 2小时超时
                        log.warn("Execution {} appears to be stuck, running for {} minutes",
                                session.getExecutionCode(), runningTime / (60 * 1000));
                        notifyExecutionTimeout(session.getExecutionCode(), session);
                    }

                    // 更新并行执行控制器状态
                    ParallelExecutionController.ExecutorStatus executorStatus =
                        parallelExecutionController.getExecutorStatus();
                    session.setActiveThreads(executorStatus.getActiveCount());
                    session.setQueueSize(executorStatus.getQueueSize());
                }
            }
        } catch (Exception e) {
            log.error("Error monitoring active executions", e);
        }
    }

    /**
     * 清理过期会话
     */
    private void cleanupExpiredSessions() {
        try {
            Iterator<Map.Entry<String, ExecutionSession>> iterator = activeSessions.entrySet().iterator();
            while (iterator.hasNext()) {
                Map.Entry<String, ExecutionSession> entry = iterator.next();
                ExecutionSession session = entry.getValue();

                // 清理已完成超过1小时的会话
                if (session.getEndTime() != null) {
                    long completedTime = System.currentTimeMillis() - session.getEndTime().getTime();
                    if (completedTime > 60 * 60 * 1000) { // 1小时
                        iterator.remove();
                        log.info("Cleaned up expired session: {}", session.getExecutionCode());
                    }
                }
            }
        } catch (Exception e) {
            log.error("Error cleaning up expired sessions", e);
        }
    }

    /**
     * 通知执行开始
     */
    private void notifyExecutionStarted(TestExecution execution, ExecutionSession session) {
        for (ExecutionListener listener : listeners) {
            try {
                listener.onExecutionStarted(execution, session);
            } catch (Exception e) {
                log.error("Error notifying listener", e);
            }
        }
    }

    /**
     * 通知进度更新
     */
    private void notifyProgressUpdated(String executionCode, ExecutionSession session) {
        for (ExecutionListener listener : listeners) {
            try {
                listener.onProgressUpdated(executionCode, session);
            } catch (Exception e) {
                log.error("Error notifying listener", e);
            }
        }
    }

    /**
     * 通知执行完成
     */
    private void notifyExecutionCompleted(String executionCode, ExecutionSession session) {
        for (ExecutionListener listener : listeners) {
            try {
                listener.onExecutionCompleted(executionCode, session);
            } catch (Exception e) {
                log.error("Error notifying listener", e);
            }
        }
    }

    /**
     * 通知执行停止
     */
    private void notifyExecutionStopped(String executionCode, ExecutionSession session) {
        for (ExecutionListener listener : listeners) {
            try {
                listener.onExecutionStopped(executionCode, session);
            } catch (Exception e) {
                log.error("Error notifying listener", e);
            }
        }
    }

    /**
     * 通知执行超时
     */
    private void notifyExecutionTimeout(String executionCode, ExecutionSession session) {
        for (ExecutionListener listener : listeners) {
            try {
                listener.onExecutionTimeout(executionCode, session);
            } catch (Exception e) {
                log.error("Error notifying listener", e);
            }
        }
    }

    /**
     * 执行会话
     */
    public static class ExecutionSession {
        private Long executionId;
        private String executionCode;
        private String status;
        private Date startTime;
        private Date endTime;
        private Date lastUpdateTime;
        private int totalCases;
        private int completedCases;
        private int successCases;
        private int failedCases;
        private int progress;
        private String errorMessage;
        private int activeThreads;
        private int queueSize;

        // Getters and setters
        public Long getExecutionId() { return executionId; }
        public void setExecutionId(Long executionId) { this.executionId = executionId; }
        public String getExecutionCode() { return executionCode; }
        public void setExecutionCode(String executionCode) { this.executionCode = executionCode; }
        public String getStatus() { return status; }
        public void setStatus(String status) { this.status = status; }
        public Date getStartTime() { return startTime; }
        public void setStartTime(Date startTime) { this.startTime = startTime; }
        public Date getEndTime() { return endTime; }
        public void setEndTime(Date endTime) { this.endTime = endTime; }
        public Date getLastUpdateTime() { return lastUpdateTime; }
        public void setLastUpdateTime(Date lastUpdateTime) { this.lastUpdateTime = lastUpdateTime; }
        public int getTotalCases() { return totalCases; }
        public void setTotalCases(int totalCases) { this.totalCases = totalCases; }
        public int getCompletedCases() { return completedCases; }
        public void setCompletedCases(int completedCases) { this.completedCases = completedCases; }
        public int getSuccessCases() { return successCases; }
        public void setSuccessCases(int successCases) { this.successCases = successCases; }
        public int getFailedCases() { return failedCases; }
        public void setFailedCases(int failedCases) { this.failedCases = failedCases; }
        public int getProgress() { return progress; }
        public void setProgress(int progress) { this.progress = progress; }
        public String getErrorMessage() { return errorMessage; }
        public void setErrorMessage(String errorMessage) { this.errorMessage = errorMessage; }
        public int getActiveThreads() { return activeThreads; }
        public void setActiveThreads(int activeThreads) { this.activeThreads = activeThreads; }
        public int getQueueSize() { return queueSize; }
        public void setQueueSize(int queueSize) { this.queueSize = queueSize; }
    }

    /**
     * 监控统计
     */
    public static class MonitorStatistics {
        private int totalActiveSessions;
        private int runningSessions;
        private int completedSessions;
        private int totalCases;
        private int completedCases;
        private int successCases;
        private int failedCases;

        // Getters and setters
        public int getTotalActiveSessions() { return totalActiveSessions; }
        public void setTotalActiveSessions(int totalActiveSessions) { this.totalActiveSessions = totalActiveSessions; }
        public int getRunningSessions() { return runningSessions; }
        public void setRunningSessions(int runningSessions) { this.runningSessions = runningSessions; }
        public int getCompletedSessions() { return completedSessions; }
        public void setCompletedSessions(int completedSessions) { this.completedSessions = completedSessions; }
        public int getTotalCases() { return totalCases; }
        public void setTotalCases(int totalCases) { this.totalCases = totalCases; }
        public int getCompletedCases() { return completedCases; }
        public void setCompletedCases(int completedCases) { this.completedCases = completedCases; }
        public int getSuccessCases() { return successCases; }
        public void setSuccessCases(int successCases) { this.successCases = successCases; }
        public int getFailedCases() { return failedCases; }
        public void setFailedCases(int failedCases) { this.failedCases = failedCases; }
    }

    /**
     * 执行监听器接口
     */
    public interface ExecutionListener {
        void onExecutionStarted(TestExecution execution, ExecutionSession session);
        void onProgressUpdated(String executionCode, ExecutionSession session);
        void onExecutionCompleted(String executionCode, ExecutionSession session);
        void onExecutionStopped(String executionCode, ExecutionSession session);
        void onExecutionTimeout(String executionCode, ExecutionSession session);
    }
}