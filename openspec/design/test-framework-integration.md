# 测试框架集成方案设计

## 概述
设计可插拔的测试框架集成架构，支持Selenium、JUnit、RestAssured、TestNG等主流测试框架，提供统一的执行接口和结果收集机制。

## 架构设计

### 1. 插件化架构
```
TestExecutionEngine
├── FrameworkAdapter (适配器接口)
├── SeleniumAdapter (Web UI测试)
├── RestAssuredAdapter (API测试)
├── JUnitAdapter (单元测试)
├── TestNGAdapter (测试框架)
├── PerformanceTestAdapter (性能测试)
└── CustomAdapter (自定义扩展)
```

### 2. 核心接口设计

#### FrameworkAdapter接口
```java
public interface FrameworkAdapter {
    /**
     * 框架类型标识
     */
    String getFrameworkType();

    /**
     * 支持的测试类型
     */
    List<String> getSupportedTestTypes();

    /**
     * 初始化适配器
     */
    void initialize(AdapterConfig config);

    /**
     * 执行测试用例
     */
    TestResult executeTest(TestCase testCase, ExecutionContext context);

    /**
     * 批量执行测试
     */
    BatchTestResult executeBatchTests(List<TestCase> testCases, ExecutionContext context);

    /**
     * 停止测试执行
     */
    void stopExecution(String executionId);

    /**
     * 清理资源
     */
    void cleanup();
}
```

#### TestCase数据结构
```java
public class TestCase {
    private Long caseId;
    private String caseType;
    private List<TestStep> testSteps;
    private Map<String, Object> testData;
    private Map<String, String> parameters;
    private Integer timeout;
    private Integer retryCount;
}

public class TestStep {
    private Integer stepNumber;
    private String action;
    private String elementType;
    private String locator;
    private String operation;
    private Object value;
    private List<String> assertions;
}
```

#### ExecutionContext执行上下文
```java
public class ExecutionContext {
    private String executionId;
    private Long projectId;
    private Long envId;
    private TestEnvironment environment;
    private Map<String, Object> globalVariables;
    private WebDriver webDriver;
    private TestReportManager reportManager;
    private ExecutorService executorService;
}
```

## 具体框架适配器实现

### 1. Selenium Web UI测试适配器

#### 适配器实现
```java
@Component
public class SeleniumAdapter implements FrameworkAdapter {

    @Autowired
    private WebDriverManager webDriverManager;

    @Autowired
    private ScreenshotService screenshotService;

    @Override
    public String getFrameworkType() {
        return "SELENIUM";
    }

    @Override
    public List<String> getSupportedTestTypes() {
        return Arrays.asList("WEB_UI", "MOBILE_WEB");
    }

    @Override
    public void initialize(AdapterConfig config) {
        // 初始化WebDriver配置
        webDriverManager.initialize(config);
    }

    @Override
    public TestResult executeTest(TestCase testCase, ExecutionContext context) {
        TestResult result = new TestResult();
        result.setCaseId(testCase.getCaseId());
        result.setStartTime(System.currentTimeMillis());

        try {
            // 获取或创建WebDriver
            WebDriver driver = getWebDriver(context, testCase);
            context.setWebDriver(driver);

            // 执行测试步骤
            for (TestStep step : testCase.getTestSteps()) {
                executeStep(step, driver, context);
            }

            result.setStatus("PASSED");

        } catch (Exception e) {
            result.setStatus("FAILED");
            result.setErrorMessage(e.getMessage());
            result.setStackTrace(getStackTrace(e));

            // 截图
            if (context.getWebDriver() != null) {
                String screenshotPath = screenshotService.captureScreenshot(
                    context.getWebDriver(),
                    context.getExecutionId(),
                    testCase.getCaseId()
                );
                result.setScreenshotPath(screenshotPath);
            }
        } finally {
            result.setEndTime(System.currentTimeMillis());
            result.setDuration(result.getEndTime() - result.getStartTime());
        }

        return result;
    }

    private void executeStep(TestStep step, WebDriver driver, ExecutionContext context) {
        switch (step.getOperation().toLowerCase()) {
            case "navigate":
                driver.get(step.getValue().toString());
                break;
            case "click":
                findElement(driver, step).click();
                break;
            case "sendkeys":
                findElement(driver, step).sendKeys(step.getValue().toString());
                break;
            case "clear":
                findElement(driver, step).clear();
                break;
            case "select":
                handleSelectOperation(driver, step);
                break;
            case "wait":
                handleWaitOperation(driver, step);
                break;
            case "assert":
                handleAssertion(driver, step, context);
                break;
            case "execute_script":
                ((JavascriptExecutor)driver).executeScript(step.getValue().toString());
                break;
            default:
                throw new UnsupportedOperationException("不支持的操作: " + step.getOperation());
        }
    }

    private WebElement findElement(WebDriver driver, TestStep step) {
        By locator = getLocator(step.getElementType(), step.getLocator());
        WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(10));
        return wait.until(ExpectedConditions.presenceOfElementLocated(locator));
    }

    private By getLocator(String elementType, String locator) {
        switch (elementType.toUpperCase()) {
            case "ID": return By.id(locator);
            case "NAME": return By.name(locator);
            case "CLASS": return By.className(locator);
            case "TAG": return By.tagName(locator);
            case "LINK": return By.linkText(locator);
            case "PARTIAL_LINK": return By.partialLinkText(locator);
            case "CSS": return By.cssSelector(locator);
            case "XPATH": return By.xpath(locator);
            default: throw new IllegalArgumentException("不支持的定位器类型: " + elementType);
        }
    }
}
```

#### WebDriver配置管理
```java
@Component
public class WebDriverManager {

    private Map<String, WebDriver> driverPool = new ConcurrentHashMap<>();

    public WebDriver getWebDriver(String executionId, BrowserConfig config) {
        return driverPool.computeIfAbsent(executionId, id -> createWebDriver(config));
    }

    private WebDriver createWebDriver(BrowserConfig config) {
        switch (config.getBrowserType().toUpperCase()) {
            case "CHROME":
                return createChromeDriver(config);
            case "FIREFOX":
                return createFirefoxDriver(config);
            case "EDGE":
                return createEdgeDriver(config);
            case "SAFARI":
                return createSafariDriver(config);
            default:
                throw new IllegalArgumentException("不支持的浏览器类型: " + config.getBrowserType());
        }
    }

    private ChromeDriver createChromeDriver(BrowserConfig config) {
        ChromeOptions options = new ChromeOptions();

        // 基础配置
        if (config.isHeadless()) {
            options.addArguments("--headless");
        }
        options.addArguments("--no-sandbox");
        options.addArguments("--disable-dev-shm-usage");
        options.addArguments("--disable-gpu");

        // 窗口大小
        if (config.getWindowWidth() > 0 && config.getWindowHeight() > 0) {
            options.addArguments("--window-size=" + config.getWindowWidth() + "," + config.getWindowHeight());
        }

        // 代理配置
        if (config.getProxyHost() != null) {
            options.addArguments("--proxy-server=" + config.getProxyHost() + ":" + config.getProxyPort());
        }

        // 用户代理
        if (config.getUserAgent() != null) {
            options.addArguments("--user-agent=" + config.getUserAgent());
        }

        // 性能优化
        if (config.isDisableImages()) {
            Map<String, Object> prefs = new HashMap<>();
            prefs.put("profile.managed_default_content_settings.images", 2);
            options.setExperimentalOption("prefs", prefs);
        }

        return new ChromeDriver(options);
    }

    public void closeWebDriver(String executionId) {
        WebDriver driver = driverPool.remove(executionId);
        if (driver != null) {
            driver.quit();
        }
    }

    @PreDestroy
    public void cleanup() {
        driverPool.values().forEach(WebDriver::quit);
        driverPool.clear();
    }
}
```

### 2. RestAssured API测试适配器

#### 适配器实现
```java
@Component
public class RestAssuredAdapter implements FrameworkAdapter {

    @Override
    public String getFrameworkType() {
        return "REST_ASSURED";
    }

    @Override
    public List<String> getSupportedTestTypes() {
        return Arrays.asList("API", "REST_API", "HTTP_API");
    }

    @Override
    public void initialize(AdapterConfig config) {
        // 配置RestAssured
        RestAssured.baseURI = config.getBaseUrl();
        RestAssured.port = config.getPort();

        // 配置SSL
        if (config.isTrustAllSSL()) {
            RestAssured.useRelaxedHTTPSValidation();
        }

        // 配置代理
        if (config.getProxyHost() != null) {
            RestAssured.proxy(config.getProxyHost(), config.getProxyPort());
        }
    }

    @Override
    public TestResult executeTest(TestCase testCase, ExecutionContext context) {
        TestResult result = new TestResult();
        result.setCaseId(testCase.getCaseId());
        result.setStartTime(System.currentTimeMillis());

        try {
            APIRequest apiRequest = buildAPIRequest(testCase, context);
            APIResponse apiResponse = executeAPIRequest(apiRequest);

            // 验证响应
            validateResponse(apiResponse, testCase, result);

            result.setStatus("PASSED");
            result.setResponseData(apiResponse);

        } catch (Exception e) {
            result.setStatus("FAILED");
            result.setErrorMessage(e.getMessage());
            result.setStackTrace(getStackTrace(e));
        } finally {
            result.setEndTime(System.currentTimeMillis());
            result.setDuration(result.getEndTime() - result.getStartTime());
        }

        return result;
    }

    private APIRequest buildAPIRequest(TestCase testCase, ExecutionContext context) {
        APIRequest request = new APIRequest();

        // 从测试步骤中提取API请求信息
        for (TestStep step : testCase.getTestSteps()) {
            switch (step.getAction().toLowerCase()) {
                case "set_method":
                    request.setMethod(step.getValue().toString());
                    break;
                case "set_endpoint":
                    request.setEndpoint(step.getValue().toString());
                    break;
                case "set_header":
                    request.addHeader(step.getLocator(), step.getValue().toString());
                    break;
                case "set_body":
                    request.setBody(step.getValue());
                    break;
                case "set_query_param":
                    request.addQueryParam(step.getLocator(), step.getValue().toString());
                    break;
                case "set_form_param":
                    request.addFormParam(step.getLocator(), step.getValue().toString());
                    break;
                case "set_auth":
                    handleAuthConfiguration(request, step);
                    break;
            }
        }

        // 处理数据驱动
        if (testCase.getTestData() != null) {
            request = substituteVariables(request, testCase.getTestData());
        }

        return request;
    }

    private APIResponse executeAPIRequest(APIRequest request) {
        RequestSpecification reqSpec = RestAssured.given();

        // 设置请求头
        request.getHeaders().forEach(reqSpec::header);

        // 设置查询参数
        request.getQueryParams().forEach(reqSpec::queryParam);

        // 设置表单参数
        if (!request.getFormParams().isEmpty()) {
            reqSpec.formParams(request.getFormParams());
        }

        // 设置请求体
        if (request.getBody() != null) {
            reqSpec.body(request.getBody());
        }

        // 认证配置
        if (request.getAuth() != null) {
            configureAuth(reqSpec, request.getAuth());
        }

        // 执行请求
        Response response = reqSpec.request(request.getMethod(), request.getEndpoint());

        // 构建响应对象
        APIResponse apiResponse = new APIResponse();
        apiResponse.setStatusCode(response.getStatusCode());
        apiResponse.setStatusLine(response.getStatusLine());
        apiResponse.setHeaders(response.getHeaders());
        apiResponse.setBody(response.getBody().asString());
        apiResponse.setResponseTime(response.getTime());

        return apiResponse;
    }

    private void validateResponse(APIResponse response, TestCase testCase, TestResult result) {
        for (TestStep step : testCase.getTestSteps()) {
            if ("assert".equals(step.getAction().toLowerCase())) {
                Assertion assertion = parseAssertion(step);
                boolean passed = evaluateAssertion(response, assertion);

                if (!passed) {
                    result.setStatus("FAILED");
                    result.setErrorMessage("断言失败: " + assertion.getDescription());
                    return;
                }
            }
        }
    }
}
```

### 3. JUnit单元测试适配器

#### 适配器实现
```java
@Component
public class JUnitAdapter implements FrameworkAdapter {

    @Autowired
    private ClasspathTestFinder testFinder;

    @Override
    public String getFrameworkType() {
        return "JUNIT";
    }

    @Override
    public List<String> getSupportedTestTypes() {
        return Arrays.asList("UNIT", "INTEGRATION");
    }

    @Override
    public TestResult executeTest(TestCase testCase, ExecutionContext context) {
        TestResult result = new TestResult();
        result.setCaseId(testCase.getCaseId());
        result.setStartTime(System.currentTimeMillis());

        try {
            // 查找对应的JUnit测试类
            Class<?> testClass = findTestClass(testCase);
            Method testMethod = findTestMethod(testClass, testCase);

            // 创建测试实例
            Object testInstance = createTestInstance(testClass, context);

            // 执行测试前处理
            runBefores(testInstance, testMethod);

            // 执行测试方法
            Object testResult = testMethod.invoke(testInstance);

            // 执行测试后处理
            runAfters(testInstance, testMethod);

            result.setStatus("PASSED");
            result.setTestOutput(testResult != null ? testResult.toString() : "");

        } catch (Exception e) {
            result.setStatus("FAILED");
            result.setErrorMessage(e.getMessage());
            result.setStackTrace(getStackTrace(e));
        } finally {
            result.setEndTime(System.currentTimeMillis());
            result.setDuration(result.getEndTime() - result.getStartTime());
        }

        return result;
    }

    @Override
    public BatchTestResult executeBatchTests(List<TestCase> testCases, ExecutionContext context) {
        BatchTestResult batchResult = new BatchTestResult();

        // 使用JUnit Runner执行批量测试
        JUnitCore junit = new JUnitCore();

        // 添加自定义监听器
        junit.addListener(new TestExecutionListener(batchResult));

        // 查找所有测试类
        Set<Class<?>> testClasses = findTestClasses(testCases);

        // 执行测试
        Result result = junit.run(testClasses.toArray(new Class<?>[0]));

        // 统计结果
        batchResult.setTotalRunCount(result.getRunCount());
        batchResult.setFailureCount(result.getFailureCount());
        batchResult.setIgnoreCount(result.getIgnoreCount());
        batchResult.setRunTime(result.getRunTime());
        batchResult.setWasSuccessful(result.wasSuccessful());

        return batchResult;
    }
}
```

### 4. 性能测试适配器

#### 适配器实现
```java
@Component
public class PerformanceTestAdapter implements FrameworkAdapter {

    @Autowired
    private LoadTestExecutor loadTestExecutor;

    @Override
    public String getFrameworkType() {
        return "PERFORMANCE";
    }

    @Override
    public List<String> getSupportedTestTypes() {
        return Arrays.asList("PERFORMANCE", "LOAD", "STRESS");
    }

    @Override
    public TestResult executeTest(TestCase testCase, ExecutionContext context) {
        TestResult result = new TestResult();
        result.setCaseId(testCase.getCaseId());
        result.setStartTime(System.currentTimeMillis());

        try {
            // 解析性能测试配置
            PerformanceTestConfig config = parsePerformanceConfig(testCase);

            // 执行性能测试
            PerformanceTestMetrics metrics = loadTestExecutor.executeLoadTest(config);

            // 评估性能结果
            evaluatePerformanceMetrics(metrics, testCase, result);

            result.setStatus("PASSED");
            result.setPerformanceMetrics(metrics);

        } catch (Exception e) {
            result.setStatus("FAILED");
            result.setErrorMessage(e.getMessage());
            result.setStackTrace(getStackTrace(e));
        } finally {
            result.setEndTime(System.currentTimeMillis());
            result.setDuration(result.getEndTime() - result.getStartTime());
        }

        return result;
    }

    private PerformanceTestConfig parsePerformanceConfig(TestCase testCase) {
        PerformanceTestConfig config = new PerformanceTestConfig();

        for (TestStep step : testCase.getTestSteps()) {
            switch (step.getAction().toLowerCase()) {
                case "set_concurrent_users":
                    config.setConcurrentUsers(Integer.parseInt(step.getValue().toString()));
                    break;
                case "set_ramp_up_time":
                    config.setRampUpTime(Integer.parseInt(step.getValue().toString()));
                    break;
                case "set_test_duration":
                    config.setTestDuration(Integer.parseInt(step.getValue().toString()));
                    break;
                case "set_throughput_threshold":
                    config.setThroughputThreshold(Double.parseDouble(step.getValue().toString()));
                    break;
                case "set_response_time_threshold":
                    config.setResponseTimeThreshold(Integer.parseInt(step.getValue().toString()));
                    break;
            }
        }

        return config;
    }
}
```

## 配置管理

### 1. 适配器配置
```java
@ConfigurationProperties(prefix = "test.framework")
@Data
public class FrameworkConfig {

    private SeleniumConfig selenium = new SeleniumConfig();
    private RestAssuredConfig restAssured = new RestAssuredConfig();
    private JUnitConfig junit = new JUnitConfig();
    private PerformanceConfig performance = new PerformanceConfig();

    @Data
    public static class SeleniumConfig {
        private String defaultBrowser = "CHROME";
        private boolean headless = false;
        private int pageLoadTimeout = 30;
        private int elementWaitTimeout = 10;
        private String screenshotPath = "/screenshots";
        private boolean autoScreenshot = true;
    }

    @Data
    public static class RestAssuredConfig {
        private String baseUrl = "http://localhost";
        private int port = 8080;
        private boolean trustAllSSL = false;
        private int connectionTimeout = 10000;
        private int readTimeout = 30000;
        private String proxyHost;
        private Integer proxyPort;
    }
}
```

### 2. 测试环境配置
```java
@Data
public class TestEnvironment {
    private Long envId;
    private String envName;
    private String envType;
    private String baseUrl;
    private Map<String, Object> dbConfig;
    private Map<String, Object> apiConfig;
    private Map<String, Object> customConfig;

    public SeleniumConfig getSeleniumConfig() {
        SeleniumConfig config = new SeleniumConfig();
        if (customConfig != null) {
            // 从自定义配置中提取Selenium配置
            config.setBrowserType((String) customConfig.get("browserType"));
            config.setHeadless(Boolean.parseBoolean(customConfig.getOrDefault("headless", "false").toString()));
            config.setWindowWidth(Integer.parseInt(customConfig.getOrDefault("windowWidth", "1920").toString()));
            config.setWindowHeight(Integer.parseInt(customConfig.getOrDefault("windowHeight", "1080").toString()));
        }
        return config;
    }

    public RestAssuredConfig getRestAssuredConfig() {
        RestAssuredConfig config = new RestAssuredConfig();
        config.setBaseUrl(baseUrl);
        if (apiConfig != null) {
            // 从API配置中提取设置
            config.setConnectionTimeout(Integer.parseInt(apiConfig.getOrDefault("timeout", "10000").toString()));
            config.setTrustAllSSL(Boolean.parseBoolean(apiConfig.getOrDefault("trustAllSSL", "false").toString()));
        }
        return config;
    }
}
```

## 扩展机制

### 1. 自定义适配器接口
```java
public interface CustomFrameworkAdapter extends FrameworkAdapter {
    /**
     * 自定义配置解析
     */
    void parseCustomConfig(String configJson);

    /**
     * 自定义结果处理
     */
    void processCustomResult(TestResult result);
}
```

### 2. 插件注册机制
```java
@Component
public class FrameworkAdapterManager {

    private Map<String, FrameworkAdapter> adapters = new ConcurrentHashMap<>();

    @PostConstruct
    public void initializeAdapters() {
        // 注册内置适配器
        registerAdapter(new SeleniumAdapter());
        registerAdapter(new RestAssuredAdapter());
        registerAdapter(new JUnitAdapter());
        registerAdapter(new PerformanceTestAdapter());

        // 扫描并注册自定义适配器
        scanAndRegisterCustomAdapters();
    }

    public void registerAdapter(FrameworkAdapter adapter) {
        adapters.put(adapter.getFrameworkType(), adapter);
    }

    public FrameworkAdapter getAdapter(String frameworkType) {
        return adapters.get(frameworkType);
    }

    public List<String> getSupportedTypes() {
        return adapters.values().stream()
                .flatMap(adapter -> adapter.getSupportedTestTypes().stream())
                .distinct()
                .collect(Collectors.toList());
    }
}
```

这个测试框架集成方案提供了：

1. **统一的接口规范** - 所有测试框架都通过相同的接口执行
2. **可插拔架构** - 支持动态加载和卸载测试框架适配器
3. **丰富的配置选项** - 支持各种测试框架的详细配置
4. **完整的错误处理** - 统一的异常处理和错误信息收集
5. **性能优化** - 连接池、缓存、并行执行等优化机制
6. **扩展性强** - 支持自定义测试框架的集成

通过这个架构，平台可以轻松集成新的测试框架，同时保持统一的执行体验和结果格式。