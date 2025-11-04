package com.autotest.platform.execution.framework;

import com.autotest.platform.domain.testcase.TestCase;
import com.autotest.platform.domain.testcase.TestExecutionCase;
import lombok.extern.slf4j.Slf4j;
import org.openqa.selenium.*;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Selenium Web UI测试框架适配器
 *
 * @author autotest
 * @date 2024-01-01
 */
@Slf4j
@Component
public class SeleniumWebAdapter implements TestFrameworkAdapter {

    private static final String FRAMEWORK_TYPE = "SELENIUM_WEB";
    private static final String FRAMEWORK_NAME = "Selenium Web UI";
    private static final String[] SUPPORTED_CASE_TYPES = {"WEB_UI"};

    private FrameworkStatus status = FrameworkStatus.INITIALIZED;
    private Map<String, WebDriver> driverPool = new ConcurrentHashMap<>();
    private Map<String, Object> config;

    @Override
    public String getFrameworkType() {
        return FRAMEWORK_TYPE;
    }

    @Override
    public String getFrameworkName() {
        return FRAMEWORK_NAME;
    }

    @Override
    public String[] getSupportedCaseTypes() {
        return SUPPORTED_CASE_TYPES;
    }

    @Override
    public boolean initialize(String config) {
        try {
            this.config = parseConfig(config);

            // 设置ChromeDriver路径（如果配置了的话）
            if (this.config.containsKey("chromeDriverPath")) {
                System.setProperty("webdriver.chrome.driver", (String) this.config.get("chromeDriverPath"));
            }

            status = FrameworkStatus.READY;
            log.info("Selenium Web UI adapter initialized successfully");
            return true;
        } catch (Exception e) {
            log.error("Failed to initialize Selenium Web UI adapter", e);
            status = FrameworkStatus.ERROR;
            return false;
        }
    }

    @Override
    public TestExecutionResult executeTestCase(TestCase testCase, TestExecutionCase executionCase, ExecutionContext context) {
        TestExecutionResult result = new TestExecutionResult();
        long startTime = System.currentTimeMillis();

        try {
            status = FrameworkStatus.BUSY;
            WebDriver driver = getDriver(context.getExecutionId());

            List<StepResult> stepResults = new ArrayList<>();
            List<TestCase.TestStep> testSteps = testCase.getTestStepList();

            if (testSteps == null || testSteps.isEmpty()) {
                result.setSuccess(false);
                result.setErrorMessage("No test steps defined");
                return result;
            }

            boolean overallSuccess = true;

            for (TestCase.TestStep step : testSteps) {
                StepResult stepResult = executeStep(driver, step, context);
                stepResults.add(stepResult);

                if (!stepResult.isSuccess()) {
                    overallSuccess = false;
                    // 如果配置了失败时停止，则中断执行
                    Boolean continueOnFailure = (Boolean) context.getConfig().get("continueOnFailure");
                    if (continueOnFailure == null || !continueOnFailure) {
                        break;
                    }
                }
            }

            result.setSuccess(overallSuccess);
            result.setStepResults(stepResults);
            result.setMessage(overallSuccess ? "All steps executed successfully" : "Some steps failed");

        } catch (Exception e) {
            log.error("Failed to execute test case: " + testCase.getCaseTitle(), e);
            result.setSuccess(false);
            result.setErrorMessage(e.getMessage());
        } finally {
            result.setDuration(System.currentTimeMillis() - startTime);
            status = FrameworkStatus.READY;
        }

        return result;
    }

    @Override
    public boolean stopExecution(String executionId) {
        try {
            WebDriver driver = driverPool.remove(executionId);
            if (driver != null) {
                driver.quit();
                log.info("Stopped execution: " + executionId);
                return true;
            }
        } catch (Exception e) {
            log.error("Failed to stop execution: " + executionId, e);
        }
        return false;
    }

    @Override
    public void cleanup() {
        for (Map.Entry<String, WebDriver> entry : driverPool.entrySet()) {
            try {
                entry.getValue().quit();
            } catch (Exception e) {
                log.warn("Failed to cleanup driver for execution: " + entry.getKey(), e);
            }
        }
        driverPool.clear();
        status = FrameworkStatus.STOPPED;
    }

    @Override
    public ValidationResult validateTestCase(TestCase testCase) {
        ValidationResult result = new ValidationResult();
        List<String> errors = new ArrayList<>();

        try {
            // 验证测试步骤
            List<TestCase.TestStep> steps = testCase.getTestStepList();
            if (steps == null || steps.isEmpty()) {
                errors.add("Test case must have at least one step");
            } else {
                for (int i = 0; i < steps.size(); i++) {
                    TestCase.TestStep step = steps.get(i);
                    if (step.getStepNumber() == null) {
                        errors.add("Step " + (i + 1) + " missing step number");
                    }
                    if (step.getAction() == null || step.getAction().trim().isEmpty()) {
                        errors.add("Step " + (i + 1) + " missing action");
                    }
                    if (step.getOperation() == null || step.getOperation().trim().isEmpty()) {
                        errors.add("Step " + (i + 1) + " missing operation");
                    }
                }
            }

            result.setValid(errors.isEmpty());
            result.setErrors(errors);
            result.setMessage(errors.isEmpty() ? "Test case validation passed" : "Test case validation failed");

        } catch (Exception e) {
            result.setValid(false);
            result.setMessage("Validation error: " + e.getMessage());
        }

        return result;
    }

    @Override
    public String getConfigTemplate() {
        return "{\n" +
               "  \"chromeDriverPath\": \"/path/to/chromedriver\",\n" +
               "  \"browser\": \"chrome\",\n" +
               "  \"headless\": false,\n" +
               "  \"windowSize\": \"1920x1080\",\n" +
               "  \"implicitWait\": 10,\n" +
               "  \"pageLoadTimeout\": 30,\n" +
               "  \"screenshotOnFailure\": true\n" +
               "}";
    }

    @Override
    public boolean supportParallelExecution() {
        return true;
    }

    @Override
    public FrameworkStatus getFrameworkStatus() {
        return status;
    }

    /**
     * 获取WebDriver实例
     */
    private WebDriver getDriver(String executionId) {
        return driverPool.computeIfAbsent(executionId, id -> createDriver());
    }

    /**
     * 创建WebDriver实例
     */
    private WebDriver createDriver() {
        ChromeOptions options = new ChromeOptions();

        if (config != null) {
            // 无头模式
            if (Boolean.TRUE.equals(config.get("headless"))) {
                options.addArguments("--headless");
            }

            // 窗口大小
            String windowSize = (String) config.get("windowSize");
            if (windowSize != null) {
                options.addArguments("--window-size=" + windowSize);
            }

            // 其他选项
            options.addArguments("--no-sandbox");
            options.addArguments("--disable-dev-shm-usage");
            options.addArguments("--disable-gpu");
        }

        WebDriver driver = new ChromeDriver(options);

        // 设置隐式等待
        if (config != null && config.get("implicitWait") != null) {
            int implicitWait = (Integer) config.get("implicitWait");
            driver.manage().timeouts().implicitlyWait(Duration.ofSeconds(implicitWait));
        }

        return driver;
    }

    /**
     * 执行单个步骤
     */
    private StepResult executeStep(WebDriver driver, TestCase.TestStep step, ExecutionContext context) {
        StepResult result = new StepResult();
        result.setStepNumber(step.getStepNumber());
        result.setDescription(step.getAction());

        long startTime = System.currentTimeMillis();

        try {
            switch (step.getOperation().toLowerCase()) {
                case "navigate":
                    executeNavigate(driver, step);
                    break;
                case "click":
                    executeClick(driver, step);
                    break;
                case "sendkeys":
                case "input":
                    executeSendKeys(driver, step);
                    break;
                case "gettext":
                    executeGetText(driver, step);
                    break;
                case "wait":
                    executeWait(driver, step);
                    break;
                case "screenshot":
                    executeScreenshot(driver, step, result);
                    break;
                default:
                    throw new UnsupportedOperationException("Unsupported operation: " + step.getOperation());
            }

            result.setSuccess(true);

        } catch (Exception e) {
            log.error("Failed to execute step: " + step.getAction(), e);
            result.setSuccess(false);
            result.setErrorMessage(e.getMessage());

            // 失败时截图
            if (config != null && Boolean.TRUE.equals(config.get("screenshotOnFailure"))) {
                try {
                    String screenshotPath = takeScreenshot(driver, context.getExecutionId() + "_step" + step.getStepNumber());
                    result.setScreenshotPath(screenshotPath);
                } catch (Exception screenshotException) {
                    log.warn("Failed to take screenshot on failure", screenshotException);
                }
            }
        } finally {
            result.setDuration(System.currentTimeMillis() - startTime);
        }

        return result;
    }

    private void executeNavigate(WebDriver driver, TestCase.TestStep step) {
        String url = (String) step.getValue();
        driver.get(url);
    }

    private void executeClick(WebDriver driver, TestCase.TestStep step) {
        WebElement element = driver.findElement(By.cssSelector(step.getLocator()));
        element.click();
    }

    private void executeSendKeys(WebDriver driver, TestCase.TestStep step) {
        WebElement element = driver.findElement(By.cssSelector(step.getLocator()));
        element.clear();
        element.sendKeys((String) step.getValue());
    }

    private void executeGetText(WebDriver driver, TestCase.TestStep step) {
        WebElement element = driver.findElement(By.cssSelector(step.getLocator()));
        String text = element.getText();
        // 将文本结果存储到上下文中
        // context.getVariables().put(step.getAction(), text);
    }

    private void executeWait(WebDriver driver, TestCase.TestStep step) {
        long waitTime = Long.parseLong(step.getValue().toString());
        Thread.sleep(waitTime);
    }

    private void executeScreenshot(WebDriver driver, TestCase.TestStep step, StepResult result) {
        String screenshotPath = takeScreenshot(driver, "step_" + step.getStepNumber());
        result.setScreenshotPath(screenshotPath);
    }

    private String takeScreenshot(WebDriver driver, String fileName) {
        try {
            TakesScreenshot screenshotDriver = (TakesScreenshot) driver;
            byte[] screenshotBytes = screenshotDriver.getScreenshotAs(OutputType.BYTES);
            String filePath = "/tmp/screenshots/" + fileName + "_" + System.currentTimeMillis() + ".png";
            // 这里应该实现文件保存逻辑
            // Files.write(Paths.get(filePath), screenshotBytes);
            return filePath;
        } catch (Exception e) {
            log.error("Failed to take screenshot", e);
            return null;
        }
    }

    private Map<String, Object> parseConfig(String configJson) {
        // 这里应该实现JSON解析逻辑
        Map<String, Object> config = new HashMap<>();
        config.put("headless", false);
        config.put("windowSize", "1920x1080");
        config.put("implicitWait", 10);
        config.put("pageLoadTimeout", 30);
        config.put("screenshotOnFailure", true);
        return config;
    }
}