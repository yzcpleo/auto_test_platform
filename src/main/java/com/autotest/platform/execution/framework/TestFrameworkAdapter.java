package com.autotest.platform.execution.framework;

import com.autotest.platform.domain.testcase.TestCase;
import com.autotest.platform.domain.testcase.TestExecutionCase;

/**
 * 测试框架适配器接口
 *
 * @author autotest
 * @date 2024-01-01
 */
public interface TestFrameworkAdapter {

    /**
     * 获取框架类型
     *
     * @return 框架类型
     */
    String getFrameworkType();

    /**
     * 获取框架名称
     *
     * @return 框架名称
     */
    String getFrameworkName();

    /**
     * 获取支持的用例类型
     *
     * @return 支持的用例类型列表
     */
    String[] getSupportedCaseTypes();

    /**
     * 初始化框架
     *
     * @param config 配置信息
     * @return 初始化结果
     */
    boolean initialize(String config);

    /**
     * 执行测试用例
     *
     * @param testCase 测试用例
     * @param executionCase 执行记录
     * @param context 执行上下文
     * @return 执行结果
     */
    TestExecutionResult executeTestCase(TestCase testCase, TestExecutionCase executionCase, ExecutionContext context);

    /**
     * 停止测试用例执行
     *
     * @param executionId 执行ID
     * @return 停止结果
     */
    boolean stopExecution(String executionId);

    /**
     * 清理资源
     */
    void cleanup();

    /**
     * 验证用例配置
     *
     * @param testCase 测试用例
     * @return 验证结果
     */
    ValidationResult validateTestCase(TestCase testCase);

    /**
     * 获取框架配置模板
     *
     * @return 配置模板
     */
    String getConfigTemplate();

    /**
     * 是否支持并行执行
     *
     * @return 是否支持
     */
    boolean supportParallelExecution();

    /**
     * 获取框架状态
     *
     * @return 框架状态
     */
    FrameworkStatus getFrameworkStatus();

    /**
     * 执行结果
     */
    class TestExecutionResult {
        private boolean success;
        private String message;
        private String errorMessage;
        private long duration;
        private String output;
        private String screenshotPath;
        private String logPath;
        private java.util.List<StepResult> stepResults;

        // Getters and setters
        public boolean isSuccess() { return success; }
        public void setSuccess(boolean success) { this.success = success; }
        public String getMessage() { return message; }
        public void setMessage(String message) { this.message = message; }
        public String getErrorMessage() { return errorMessage; }
        public void setErrorMessage(String errorMessage) { this.errorMessage = errorMessage; }
        public long getDuration() { return duration; }
        public void setDuration(long duration) { this.duration = duration; }
        public String getOutput() { return output; }
        public void setOutput(String output) { this.output = output; }
        public String getScreenshotPath() { return screenshotPath; }
        public void setScreenshotPath(String screenshotPath) { this.screenshotPath = screenshotPath; }
        public String getLogPath() { return logPath; }
        public void setLogPath(String logPath) { this.logPath = logPath; }
        public java.util.List<StepResult> getStepResults() { return stepResults; }
        public void setStepResults(java.util.List<StepResult> stepResults) { this.stepResults = stepResults; }
    }

    /**
     * 执行上下文
     */
    class ExecutionContext {
        private String executionId;
        private String environmentId;
        private String workingDir;
        private java.util.Map<String, Object> variables;
        private java.util.Map<String, String> config;

        // Getters and setters
        public String getExecutionId() { return executionId; }
        public void setExecutionId(String executionId) { this.executionId = executionId; }
        public String getEnvironmentId() { return environmentId; }
        public void setEnvironmentId(String environmentId) { this.environmentId = environmentId; }
        public String getWorkingDir() { return workingDir; }
        public void setWorkingDir(String workingDir) { this.workingDir = workingDir; }
        public java.util.Map<String, Object> getVariables() { return variables; }
        public void setVariables(java.util.Map<String, Object> variables) { this.variables = variables; }
        public java.util.Map<String, String> getConfig() { return config; }
        public void setConfig(java.util.Map<String, String> config) { this.config = config; }
    }

    /**
     * 验证结果
     */
    class ValidationResult {
        private boolean valid;
        private String message;
        private java.util.List<String> errors;

        public boolean isValid() { return valid; }
        public void setValid(boolean valid) { this.valid = valid; }
        public String getMessage() { return message; }
        public void setMessage(String message) { this.message = message; }
        public java.util.List<String> getErrors() { return errors; }
        public void setErrors(java.util.List<String> errors) { this.errors = errors; }
    }

    /**
     * 步骤结果
     */
    class StepResult {
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
     * 框架状态
     */
    enum FrameworkStatus {
        INITIALIZED,
        READY,
        BUSY,
        ERROR,
        STOPPED
    }
}