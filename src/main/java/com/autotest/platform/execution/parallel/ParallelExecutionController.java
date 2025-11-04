package com.autotest.platform.execution.parallel;

import com.autotest.platform.domain.testcase.TestCase;
import com.autotest.platform.domain.testcase.TestExecution;
import com.autotest.platform.domain.testcase.TestExecutionCase;
import com.autotest.platform.execution.framework.FrameworkAdapterManager;
import com.autotest.platform.execution.framework.TestFrameworkAdapter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.stereotype.Component;

import javax.annotation.PreDestroy;
import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * 并行执行控制器
 *
 * @author autotest
 * @date 2024-01-01
 */
@Slf4j
@Component
public class ParallelExecutionController {

    @Autowired
    private FrameworkAdapterManager frameworkAdapterManager;

    private ThreadPoolTaskExecutor taskExecutor;
    private Map<String, ExecutionTask> runningTasks = new ConcurrentHashMap<>();
    private AtomicInteger taskCounter = new AtomicInteger(0);

    /**
     * 初始化执行器
     */
    public void initialize(int corePoolSize, int maxPoolSize, int queueCapacity) {
        taskExecutor = new ThreadPoolTaskExecutor();
        taskExecutor.setCorePoolSize(corePoolSize);
        taskExecutor.setMaxPoolSize(maxPoolSize);
        taskExecutor.setQueueCapacity(queueCapacity);
        taskExecutor.setThreadNamePrefix("TestExec-");
        taskExecutor.setRejectedExecutionHandler(new ThreadPoolExecutor.CallerRunsPolicy());
        taskExecutor.initialize();
        log.info("ParallelExecutionController initialized with core={}, max={}, queue={}",
                corePoolSize, maxPoolSize, queueCapacity);
    }

    /**
     * 执行测试用例集合
     */
    public ExecutionResult executeTestCases(TestExecution execution, List<TestCase> testCases,
                                           int parallelCount, Map<String, Object> config) {
        String executionId = "EXEC-" + taskCounter.incrementAndGet();
        log.info("Starting parallel execution: {} with {} cases, parallel count: {}",
                executionId, testCases.size(), parallelCount);

        ExecutionResult result = new ExecutionResult();
        result.setExecutionId(executionId);
        result.setStartTime(System.currentTimeMillis());
        result.setTotalCases(testCases.size());

        List<ExecutionTask> tasks = new ArrayList<>();
        List<Future<TestCaseResult>> futures = new ArrayList<>();

        // 创建执行任务
        for (int i = 0; i < testCases.size(); i++) {
            TestCase testCase = testCases.get(i);
            TestExecutionCase executionCase = new TestExecutionCase();
            executionCase.setExecutionId(execution.getExecutionId());
            executionCase.setCaseId(testCase.getCaseId());
            executionCase.setStatus("PENDING");

            ExecutionTask task = new ExecutionTask(executionId, testCase, executionCase, config);
            tasks.add(task);
            runningTasks.put(executionId + "-" + i, task);
        }

        // 分批并行执行
        try {
            int batchSize = Math.min(parallelCount, testCases.size());
            for (int i = 0; i < tasks.size(); i += batchSize) {
                int endIndex = Math.min(i + batchSize, tasks.size());
                List<ExecutionTask> batch = tasks.subList(i, endIndex);

                // 提交批次任务
                for (ExecutionTask task : batch) {
                    Future<TestCaseResult> future = taskExecutor.submit(task);
                    futures.add(future);
                }

                // 等待批次完成
                for (Future<TestCaseResult> future : futures) {
                    try {
                        TestCaseResult caseResult = future.get();
                        result.addCaseResult(caseResult);
                    } catch (Exception e) {
                        log.error("Error getting test case result", e);
                        TestCaseResult errorResult = new TestCaseResult();
                        errorResult.setSuccess(false);
                        errorResult.setErrorMessage("Execution error: " + e.getMessage());
                        result.addCaseResult(errorResult);
                    }
                }

                futures.clear();
            }

        } catch (Exception e) {
            log.error("Parallel execution failed", e);
            result.setSuccess(false);
            result.setErrorMessage("Parallel execution failed: " + e.getMessage());
        } finally {
            // 清理任务
            for (ExecutionTask task : tasks) {
                runningTasks.remove(task.getTaskId());
            }
        }

        result.setEndTime(System.currentTimeMillis());
        result.setDuration(result.getEndTime() - result.getStartTime());
        result.setSuccess(result.getFailedCases() == 0);

        log.info("Parallel execution completed: {} in {}ms, success: {}, failed: {}, skipped: {}",
                executionId, result.getDuration(), result.getSuccessCases(),
                result.getFailedCases(), result.getSkippedCases());

        return result;
    }

    /**
     * 停止执行
     */
    public boolean stopExecution(String executionId) {
        log.info("Stopping execution: {}", executionId);

        boolean stopped = true;
        Iterator<Map.Entry<String, ExecutionTask>> iterator = runningTasks.entrySet().iterator();

        while (iterator.hasNext()) {
            Map.Entry<String, ExecutionTask> entry = iterator.next();
            String taskId = entry.getKey();
            ExecutionTask task = entry.getValue();

            if (taskId.startsWith(executionId + "-")) {
                try {
                    task.cancel();
                    iterator.remove();
                } catch (Exception e) {
                    log.error("Failed to stop task: " + taskId, e);
                    stopped = false;
                }
            }
        }

        return stopped;
    }

    /**
     * 获取运行中的任务数
     */
    public int getRunningTaskCount() {
        return runningTasks.size();
    }

    /**
     * 获取执行器状态
     */
    public ExecutorStatus getExecutorStatus() {
        ExecutorStatus status = new ExecutorStatus();
        status.setActiveCount(taskExecutor.getActiveCount());
        status.setPoolSize(taskExecutor.getPoolSize());
        status.setCorePoolSize(taskExecutor.getCorePoolSize());
        status.setMaxPoolSize(taskExecutor.getMaxPoolSize());
        status.setQueueSize(taskExecutor.getThreadPoolExecutor().getQueue().size());
        status.setRunningTaskCount(runningTasks.size());
        return status;
    }

    @PreDestroy
    public void shutdown() {
        log.info("Shutting down ParallelExecutionController");

        // 停止所有运行中的任务
        for (ExecutionTask task : new ArrayList<>(runningTasks.values())) {
            try {
                task.cancel();
            } catch (Exception e) {
                log.warn("Failed to cancel task during shutdown", e);
            }
        }

        // 关闭线程池
        if (taskExecutor != null) {
            taskExecutor.shutdown();
            try {
                if (!taskExecutor.getThreadPoolExecutor().awaitTermination(30, TimeUnit.SECONDS)) {
                    taskExecutor.getThreadPoolExecutor().shutdownNow();
                }
            } catch (InterruptedException e) {
                taskExecutor.getThreadPoolExecutor().shutdownNow();
                Thread.currentThread().interrupt();
            }
        }

        runningTasks.clear();
        log.info("ParallelExecutionController shutdown completed");
    }

    /**
     * 执行任务
     */
    private class ExecutionTask implements Callable<TestCaseResult> {
        private String taskId;
        private TestCase testCase;
        private TestExecutionCase executionCase;
        private Map<String, Object> config;
        private volatile boolean cancelled = false;

        public ExecutionTask(String executionId, TestCase testCase, TestExecutionCase executionCase,
                           Map<String, Object> config) {
            this.taskId = executionId + "-" + testCase.getCaseId();
            this.testCase = testCase;
            this.executionCase = executionCase;
            this.config = config;
        }

        @Override
        public TestCaseResult call() {
            TestCaseResult result = new TestCaseResult();
            result.setCaseId(testCase.getCaseId());
            result.setCaseTitle(testCase.getCaseTitle());
            result.setStartTime(System.currentTimeMillis());

            try {
                if (cancelled) {
                    result.setSuccess(false);
                    result.setErrorMessage("Task cancelled");
                    return result;
                }

                // 获取框架适配器
                TestFrameworkAdapter adapter = frameworkAdapterManager.getAdapterByCaseType(testCase.getCaseType());
                if (adapter == null) {
                    throw new RuntimeException("No adapter found for case type: " + testCase.getCaseType());
                }

                // 创建执行上下文
                TestFrameworkAdapter.ExecutionContext context = new TestFrameworkAdapter.ExecutionContext();
                context.setExecutionId(taskId);
                context.setWorkingDir(System.getProperty("java.io.tmpdir"));
                context.setVariables(new HashMap<>());
                context.setConfig(new HashMap<>());

                // 执行测试用例
                TestFrameworkAdapter.TestExecutionResult adapterResult =
                    adapter.executeTestCase(testCase, executionCase, context);

                result.setSuccess(adapterResult.isSuccess());
                result.setErrorMessage(adapterResult.getErrorMessage());
                result.setOutput(adapterResult.getOutput());
                result.setScreenshotPath(adapterResult.getScreenshotPath());
                result.setLogPath(adapterResult.getLogPath());

                // 转换步骤结果
                if (adapterResult.getStepResults() != null) {
                    List<StepResult> stepResults = new ArrayList<>();
                    for (TestFrameworkAdapter.StepResult adapterStep : adapterResult.getStepResults()) {
                        StepResult stepResult = new StepResult();
                        stepResult.setStepNumber(adapterStep.getStepNumber());
                        stepResult.setDescription(adapterStep.getDescription());
                        stepResult.setSuccess(adapterStep.isSuccess());
                        stepResult.setErrorMessage(adapterStep.getErrorMessage());
                        stepResult.setDuration(adapterStep.getDuration());
                        stepResult.setScreenshotPath(adapterStep.getScreenshotPath());
                        stepResults.add(stepResult);
                    }
                    result.setStepResults(stepResults);
                }

            } catch (Exception e) {
                log.error("Failed to execute test case: " + testCase.getCaseTitle(), e);
                result.setSuccess(false);
                result.setErrorMessage(e.getMessage());
            } finally {
                result.setEndTime(System.currentTimeMillis());
                result.setDuration(result.getEndTime() - result.getStartTime());
            }

            return result;
        }

        public void cancel() {
            this.cancelled = true;
        }

        public String getTaskId() {
            return taskId;
        }
    }

    /**
     * 执行结果
     */
    public static class ExecutionResult {
        private String executionId;
        private long startTime;
        private long endTime;
        private long duration;
        private int totalCases;
        private int successCases;
        private int failedCases;
        private int skippedCases;
        private boolean success;
        private String errorMessage;
        private List<TestCaseResult> caseResults = new ArrayList<>();

        public void addCaseResult(TestCaseResult result) {
            caseResults.add(result);
            if (result.isSuccess()) {
                successCases++;
            } else {
                failedCases++;
            }
        }

        // Getters and setters
        public String getExecutionId() { return executionId; }
        public void setExecutionId(String executionId) { this.executionId = executionId; }
        public long getStartTime() { return startTime; }
        public void setStartTime(long startTime) { this.startTime = startTime; }
        public long getEndTime() { return endTime; }
        public void setEndTime(long endTime) { this.endTime = endTime; }
        public long getDuration() { return duration; }
        public void setDuration(long duration) { this.duration = duration; }
        public int getTotalCases() { return totalCases; }
        public void setTotalCases(int totalCases) { this.totalCases = totalCases; }
        public int getSuccessCases() { return successCases; }
        public void setSuccessCases(int successCases) { this.successCases = successCases; }
        public int getFailedCases() { return failedCases; }
        public void setFailedCases(int failedCases) { this.failedCases = failedCases; }
        public int getSkippedCases() { return skippedCases; }
        public void setSkippedCases(int skippedCases) { this.skippedCases = skippedCases; }
        public boolean isSuccess() { return success; }
        public void setSuccess(boolean success) { this.success = success; }
        public String getErrorMessage() { return errorMessage; }
        public void setErrorMessage(String errorMessage) { this.errorMessage = errorMessage; }
        public List<TestCaseResult> getCaseResults() { return caseResults; }
        public void setCaseResults(List<TestCaseResult> caseResults) { this.caseResults = caseResults; }
    }

    /**
     * 用例执行结果
     */
    public static class TestCaseResult {
        private Long caseId;
        private String caseTitle;
        private long startTime;
        private long endTime;
        private long duration;
        private boolean success;
        private String errorMessage;
        private String output;
        private String screenshotPath;
        private String logPath;
        private List<StepResult> stepResults;

        // Getters and setters
        public Long getCaseId() { return caseId; }
        public void setCaseId(Long caseId) { this.caseId = caseId; }
        public String getCaseTitle() { return caseTitle; }
        public void setCaseTitle(String caseTitle) { this.caseTitle = caseTitle; }
        public long getStartTime() { return startTime; }
        public void setStartTime(long startTime) { this.startTime = startTime; }
        public long getEndTime() { return endTime; }
        public void setEndTime(long endTime) { this.endTime = endTime; }
        public long getDuration() { return duration; }
        public void setDuration(long duration) { this.duration = duration; }
        public boolean isSuccess() { return success; }
        public void setSuccess(boolean success) { this.success = success; }
        public String getErrorMessage() { return errorMessage; }
        public void setErrorMessage(String errorMessage) { this.errorMessage = errorMessage; }
        public String getOutput() { return output; }
        public void setOutput(String output) { this.output = output; }
        public String getScreenshotPath() { return screenshotPath; }
        public void setScreenshotPath(String screenshotPath) { this.screenshotPath = screenshotPath; }
        public String getLogPath() { return logPath; }
        public void setLogPath(String logPath) { this.logPath = logPath; }
        public List<StepResult> getStepResults() { return stepResults; }
        public void setStepResults(List<StepResult> stepResults) { this.stepResults = stepResults; }
    }

    /**
     * 步骤结果
     */
    public static class StepResult {
        private int stepNumber;
        private String description;
        private boolean success;
        private String errorMessage;
        private long duration;
        private String screenshotPath;

        // Getters and setters
        public int getStepNumber() { return stepNumber; }
        public void setStepNumber(int stepNumber) { this.stepNumber = stepNumber; }
        public String getDescription() { return description; }
        public void setDescription(String description) { this.description = description; }
        public boolean isSuccess() { return success; }
        public void setSuccess(boolean success) { this.success = success; }
        public String getErrorMessage() { return errorMessage; }
        public void setErrorMessage(String errorMessage) { this.errorMessage = errorMessage; }
        public long getDuration() { return duration; }
        public void setDuration(long duration) { this.duration = duration; }
        public String getScreenshotPath() { return screenshotPath; }
        public void setScreenshotPath(String screenshotPath) { this.screenshotPath = screenshotPath; }
    }

    /**
     * 执行器状态
     */
    public static class ExecutorStatus {
        private int activeCount;
        private int poolSize;
        private int corePoolSize;
        private int maxPoolSize;
        private int queueSize;
        private int runningTaskCount;

        // Getters and setters
        public int getActiveCount() { return activeCount; }
        public void setActiveCount(int activeCount) { this.activeCount = activeCount; }
        public int getPoolSize() { return poolSize; }
        public void setPoolSize(int poolSize) { this.poolSize = poolSize; }
        public int getCorePoolSize() { return corePoolSize; }
        public void setCorePoolSize(int corePoolSize) { this.corePoolSize = corePoolSize; }
        public int getMaxPoolSize() { return maxPoolSize; }
        public void setMaxPoolSize(int maxPoolSize) { this.maxPoolSize = maxPoolSize; }
        public int getQueueSize() { return queueSize; }
        public void setQueueSize(int queueSize) { this.queueSize = queueSize; }
        public int getRunningTaskCount() { return runningTaskCount; }
        public void setRunningTaskCount(int runningTaskCount) { this.runningTaskCount = runningTaskCount; }
    }
}