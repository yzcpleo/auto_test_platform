package com.autotest.platform.execution.log;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.ReentrantLock;

/**
 * 测试执行日志记录器
 *
 * @author autotest
 * @date 2024-01-01
 */
@Slf4j
@Component
public class ExecutionLogger {

    @Autowired
    private ObjectMapper objectMapper;

    private SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS");
    private SimpleDateFormat fileNameFormat = new SimpleDateFormat("yyyyMMdd");
    private Map<String, LogSession> logSessions = new ConcurrentHashMap<>();
    private String logBaseDir = System.getProperty("java.io.tmpdir") + "/autotest-logs";
    private ReentrantLock fileLock = new ReentrantLock();

    /**
     * 初始化日志记录器
     */
    public void initialize() {
        try {
            // 创建日志目录
            Path logDir = Paths.get(logBaseDir);
            if (!Files.exists(logDir)) {
                Files.createDirectories(logDir);
            }
            log.info("ExecutionLogger initialized, log directory: {}", logBaseDir);
        } catch (Exception e) {
            log.error("Failed to initialize ExecutionLogger", e);
        }
    }

    /**
     * 开始日志会话
     */
    public LogSession startSession(String executionCode, Long executionId) {
        LogSession session = new LogSession();
        session.setExecutionCode(executionCode);
        session.setExecutionId(executionId);
        session.setStartTime(new Date());
        session.setLogFilePath(generateLogFilePath(executionCode));

        logSessions.put(executionCode, session);

        // 记录会话开始
        logInfo(executionCode, "Execution started", null);

        log.info("Started log session for execution: {}", executionCode);
        return session;
    }

    /**
     * 结束日志会话
     */
    public void endSession(String executionCode, String status) {
        LogSession session = logSessions.get(executionCode);
        if (session != null) {
            session.setEndTime(new Date());
            session.setStatus(status);

            // 记录会话结束
            logInfo(executionCode, "Execution completed with status: " + status, null);

            // 关闭文件写入器
            closeFileWriter(executionCode);

            log.info("Ended log session for execution: {} with status: {}", executionCode, status);
        }
    }

    /**
     * 记录信息日志
     */
    public void logInfo(String executionCode, String message, Object data) {
        log(executionCode, LogLevel.INFO, message, data);
    }

    /**
     * 记录警告日志
     */
    public void logWarning(String executionCode, String message, Object data) {
        log(executionCode, LogLevel.WARNING, message, data);
    }

    /**
     * 记录错误日志
     */
    public void logError(String executionCode, String message, Throwable throwable) {
        log(executionCode, LogLevel.ERROR, message, throwable);
    }

    /**
     * 记录调试日志
     */
    public void logDebug(String executionCode, String message, Object data) {
        log(executionCode, LogLevel.DEBUG, message, data);
    }

    /**
     * 记录步骤开始
     */
    public void logStepStart(String executionCode, int stepNumber, String stepDescription) {
        String message = String.format("Step %d started: %s", stepNumber, stepDescription);
        logInfo(executionCode, message, createStepData(stepNumber, "STARTED", stepDescription));
    }

    /**
     * 记录步骤完成
     */
    public void logStepEnd(String executionCode, int stepNumber, String stepDescription, boolean success, long duration) {
        String status = success ? "SUCCESS" : "FAILED";
        String message = String.format("Step %d %s: %s (duration: %dms)", stepNumber, status.toLowerCase(), stepDescription, duration);

        Object stepData = createStepData(stepNumber, status, stepDescription);
        if (!success) {
            logError(executionCode, message, null);
        } else {
            logInfo(executionCode, message, stepData);
        }
    }

    /**
     * 记录断言结果
     */
    public void logAssertion(String executionCode, int stepNumber, String description, boolean success, Object expected, Object actual) {
        String status = success ? "PASSED" : "FAILED";
        String message = String.format("Assertion %s: %s", status.toLowerCase(), description);

        Object assertionData = createAssertionData(stepNumber, description, success, expected, actual);
        if (!success) {
            logError(executionCode, message, null);
        } else {
            logDebug(executionCode, message, assertionData);
        }
    }

    /**
     * 记录性能指标
     */
    public void logPerformance(String executionCode, String metricName, Number value, String unit) {
        String message = String.format("Performance metric - %s: %s %s", metricName, value, unit);

        Object perfData = createPerformanceData(metricName, value, unit);
        logInfo(executionCode, message, perfData);
    }

    /**
     * 获取日志内容
     */
    public String getLogContent(String executionCode, int maxLines) {
        try {
            String logFilePath = getLogFilePath(executionCode);
            if (logFilePath == null || !Files.exists(Paths.get(logFilePath))) {
                return "Log file not found";
            }

            return readFileTail(logFilePath, maxLines);
        } catch (Exception e) {
            log.error("Failed to get log content for execution: " + executionCode, e);
            return "Error reading log file: " + e.getMessage();
        }
    }

    /**
     * 获取日志文件路径
     */
    public String getLogFilePath(String executionCode) {
        LogSession session = logSessions.get(executionCode);
        if (session != null) {
            return session.getLogFilePath();
        }
        return generateLogFilePath(executionCode);
    }

    /**
     * 清理过期日志
     */
    public void cleanupOldLogs(int retentionDays) {
        try {
            long cutoffTime = System.currentTimeMillis() - (retentionDays * 24 * 60 * 60 * 1000L);
            Path logDir = Paths.get(logBaseDir);

            if (Files.exists(logDir)) {
                Files.list(logDir)
                    .filter(path -> Files.isRegularFile(path))
                    .filter(path -> {
                        try {
                            return Files.getLastModifiedTime(path).toMillis() < cutoffTime;
                        } catch (Exception e) {
                            return false;
                        }
                    })
                    .forEach(path -> {
                        try {
                            Files.delete(path);
                            log.info("Deleted old log file: {}", path.getFileName());
                        } catch (Exception e) {
                            log.warn("Failed to delete old log file: " + path.getFileName(), e);
                        }
                    });
            }
        } catch (Exception e) {
            log.error("Failed to cleanup old logs", e);
        }
    }

    /**
     * 核心日志记录方法
     */
    private void log(String executionCode, LogLevel level, String message, Object data) {
        try {
            LogEntry entry = new LogEntry();
            entry.setExecutionCode(executionCode);
            entry.setTimestamp(new Date());
            entry.setLevel(level);
            entry.setMessage(message);
            entry.setData(data);

            // 写入文件
            writeToFile(executionCode, entry);

            // 同时写入应用日志
            String logMessage = String.format("[%s] [%s] %s", executionCode, level, message);
            switch (level) {
                case ERROR:
                    log.error(logMessage);
                    break;
                case WARNING:
                    log.warn(logMessage);
                    break;
                case DEBUG:
                    log.debug(logMessage);
                    break;
                default:
                    log.info(logMessage);
            }

        } catch (Exception e) {
            log.error("Failed to write log entry", e);
        }
    }

    /**
     * 写入文件
     */
    private void writeToFile(String executionCode, LogEntry entry) {
        try {
            LogSession session = logSessions.get(executionCode);
            if (session == null) {
                return;
            }

            if (session.getFileWriter() == null) {
                // 创建新的文件写入器
                FileWriter fileWriter = new FileWriter(session.getLogFilePath(), true);
                BufferedWriter bufferedWriter = new BufferedWriter(fileWriter);
                session.setFileWriter(bufferedWriter);
            }

            fileLock.lock();
            try {
                // 写入日志条目
                String logLine = formatLogEntry(entry);
                session.getFileWriter().write(logLine);
                session.getFileWriter().write(System.lineSeparator());
                session.getFileWriter().flush();
            } finally {
                fileLock.unlock();
            }

        } catch (Exception e) {
            log.error("Failed to write to log file for execution: " + executionCode, e);
        }
    }

    /**
     * 格式化日志条目
     */
    private String formatLogEntry(LogEntry entry) {
        try {
            StringBuilder sb = new StringBuilder();
            sb.append(dateFormat.format(entry.getTimestamp()));
            sb.append(" [").append(entry.getLevel()).append("] ");
            sb.append(entry.getMessage());

            if (entry.getData() != null) {
                String dataJson = objectMapper.writeValueAsString(entry.getData());
                sb.append(" | ").append(dataJson);
            }

            return sb.toString();
        } catch (Exception e) {
            return dateFormat.format(entry.getTimestamp()) + " [" + entry.getLevel() + "] " + entry.getMessage() + " | [Data serialization error]";
        }
    }

    /**
     * 生成日志文件路径
     */
    private String generateLogFilePath(String executionCode) {
        String dateStr = fileNameFormat.format(new Date());
        return logBaseDir + "/" + dateStr + "_" + executionCode + ".log";
    }

    /**
     * 获取日志文件路径
     */
    private String getLogFilePath(String executionCode) {
        LogSession session = logSessions.get(executionCode);
        return session != null ? session.getLogFilePath() : generateLogFilePath(executionCode);
    }

    /**
     * 关闭文件写入器
     */
    private void closeFileWriter(String executionCode) {
        LogSession session = logSessions.get(executionCode);
        if (session != null && session.getFileWriter() != null) {
            try {
                session.getFileWriter().close();
                session.setFileWriter(null);
            } catch (Exception e) {
                log.warn("Failed to close file writer for execution: " + executionCode, e);
            }
        }
    }

    /**
     * 读取文件尾部
     */
    private String readFileTail(String filePath, int maxLines) throws IOException {
        java.util.List<String> lines = Files.readAllLines(Paths.get(filePath));
        if (lines.size() <= maxLines) {
            return String.join(System.lineSeparator(), lines);
        } else {
            return String.join(System.lineSeparator(),
                lines.subList(lines.size() - maxLines, lines.size()));
        }
    }

    /**
     * 创建步骤数据
     */
    private Object createStepData(int stepNumber, String status, String description) {
        Map<String, Object> data = new HashMap<>();
        data.put("type", "step");
        data.put("stepNumber", stepNumber);
        data.put("status", status);
        data.put("description", description);
        data.put("timestamp", new Date());
        return data;
    }

    /**
     * 创建断言数据
     */
    private Object createAssertionData(int stepNumber, String description, boolean success, Object expected, Object actual) {
        Map<String, Object> data = new HashMap<>();
        data.put("type", "assertion");
        data.put("stepNumber", stepNumber);
        data.put("description", description);
        data.put("status", success ? "PASSED" : "FAILED");
        data.put("expected", expected);
        data.put("actual", actual);
        data.put("timestamp", new Date());
        return data;
    }

    /**
     * 创建性能数据
     */
    private Object createPerformanceData(String metricName, Number value, String unit) {
        Map<String, Object> data = new HashMap<>();
        data.put("type", "performance");
        data.put("metric", metricName);
        data.put("value", value);
        data.put("unit", unit);
        data.put("timestamp", new Date());
        return data;
    }

    /**
     * 日志级别枚举
     */
    public enum LogLevel {
        DEBUG, INFO, WARNING, ERROR
    }

    /**
     * 日志会话
     */
    public static class LogSession {
        private String executionCode;
        private Long executionId;
        private Date startTime;
        private Date endTime;
        private String status;
        private String logFilePath;
        private BufferedWriter fileWriter;

        // Getters and setters
        public String getExecutionCode() { return executionCode; }
        public void setExecutionCode(String executionCode) { this.executionCode = executionCode; }
        public Long getExecutionId() { return executionId; }
        public void setExecutionId(Long executionId) { this.executionId = executionId; }
        public Date getStartTime() { return startTime; }
        public void setStartTime(Date startTime) { this.startTime = startTime; }
        public Date getEndTime() { return endTime; }
        public void setEndTime(Date endTime) { this.endTime = endTime; }
        public String getStatus() { return status; }
        public void setStatus(String status) { this.status = status; }
        public String getLogFilePath() { return logFilePath; }
        public void setLogFilePath(String logFilePath) { this.logFilePath = logFilePath; }
        public BufferedWriter getFileWriter() { return fileWriter; }
        public void setFileWriter(BufferedWriter fileWriter) { this.fileWriter = fileWriter; }
    }

    /**
     * 日志条目
     */
    public static class LogEntry {
        private String executionCode;
        private Date timestamp;
        private LogLevel level;
        private String message;
        private Object data;

        // Getters and setters
        public String getExecutionCode() { return executionCode; }
        public void setExecutionCode(String executionCode) { this.executionCode = executionCode; }
        public Date getTimestamp() { return timestamp; }
        public void setTimestamp(Date timestamp) { this.timestamp = timestamp; }
        public LogLevel getLevel() { return level; }
        public void setLevel(LogLevel level) { this.level = level; }
        public String getMessage() { return message; }
        public void setMessage(String message) { this.message = message; }
        public Object getData() { return data; }
        public void setData(Object data) { this.data = data; }
    }
}