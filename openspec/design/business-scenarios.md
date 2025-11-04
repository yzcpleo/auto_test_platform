# 多业务场景设计文档

## 概述
基于若依框架的自动化测试平台在多种业务场景下的详细设计方案，重点解决多团队协作、大规模测试执行、CI/CD集成和测试报告分析等核心需求。

## 1. 多团队协作场景

### 1.1 场景描述
多个测试团队同时使用同一个测试平台，每个团队负责不同的项目或模块，需要保证数据隔离、权限管理和资源共享。

### 1.2 架构设计

#### 数据隔离策略
```java
@Component
public class MultiTenantDataFilter {

    /**
     * 基于项目ID的数据权限过滤
     */
    @Aspect
    @Component
    public class ProjectDataPermissionAspect {

        @Before("@annotation(projectDataPermission)")
        public void checkProjectPermission(JoinPoint joinPoint, ProjectDataPermission projectDataPermission) {
            // 获取当前用户
            Long userId = SecurityUtils.getUserId();

            // 获取用户可访问的项目列表
            List<Long> accessibleProjectIds = getUserAccessibleProjects(userId);

            // 设置数据权限上下文
            DataPermissionContext.setCurrentProjectIds(accessibleProjectIds);
        }
    }

    /**
     * MyBatis拦截器实现数据过滤
     */
    @Intercepts({
        @Signature(type = Executor.class, method = "query",
                   args = {MappedStatement.class, Object.class, RowBounds.class, ResultHandler.class}),
        @Signature(type = Executor.class, method = "update",
                   args = {MappedStatement.class, Object.class})
    })
    public class MultiTenantInterceptor implements Interceptor {

        @Override
        public Object intercept(Invocation invocation) throws Throwable {
            MappedStatement mappedStatement = invocation.getArgs()[0];
            Object parameter = invocation.getArgs()[1];

            // 检查是否需要进行数据权限过滤
            if (requiresDataFilter(mappedStatement)) {
                // 添加项目ID过滤条件
                parameter = addProjectIdFilter(parameter, mappedStatement);
            }

            return invocation.proceed();
        }
    }
}
```

#### 权限管理设计
```java
@Entity
@Table(name = "sys_role_permission")
public class RolePermission {
    @Id
    private Long permissionId;

    private Long roleId;
    private Long projectId; // 项目级权限
    private String permissionKey; // CASE_READ, CASE_WRITE, CASE_EXECUTE
    private String dataScope;    // ALL, PROJECT, DEPARTMENT, PERSONAL
}

@Service
public class PermissionService {

    /**
     * 检查用户是否有项目级权限
     */
    public boolean hasProjectPermission(Long userId, Long projectId, String permissionKey) {
        // 1. 检查项目成员权限
        List<ProjectMember> memberships = projectMemberMapper.selectByUserId(userId);
        boolean hasPermission = memberships.stream()
            .filter(member -> member.getProjectId().equals(projectId))
            .anyMatch(member -> member.getPermissions().contains(permissionKey));

        // 2. 检查全局角色权限
        if (!hasPermission) {
            hasPermission = checkGlobalRolePermission(userId, permissionKey);
        }

        return hasPermission;
    }

    /**
     * 获取用户可访问的项目列表
     */
    public List<Long> getUserAccessibleProjects(Long userId) {
        // 获取用户参与的项目
        List<Long> projectIds = projectMemberMapper.selectProjectIdsByUserId(userId);

        // 获取用户有全局权限的项目
        if (hasGlobalPermission(userId, "PROJECT_ACCESS_ALL")) {
            projectIds.addAll(projectMapper.selectAllProjectIds());
        }

        return projectIds.stream().distinct().collect(Collectors.toList());
    }
}
```

#### 资源共享机制
```java
@Service
public class ResourceSharingService {

    /**
     * 测试用例模板共享
     */
    @Transactional
    public void shareCaseTemplate(Long templateId, List<Long> targetProjectIds) {
        TestCaseTemplate template = caseTemplateMapper.selectById(templateId);

        for (Long projectId : targetProjectIds) {
            // 复制模板到目标项目
            TestCaseTemplate copiedTemplate = copyTemplate(template, projectId);
            caseTemplateMapper.insert(copiedTemplate);

            // 记录共享日志
            ResourceShareLog log = new ResourceShareLog();
            log.setResourceType("CASE_TEMPLATE");
            log.setResourceId(templateId);
            log.setSourceProjectId(template.getProjectId());
            log.setTargetProjectId(projectId);
            log.setShareTime(new Date());
            resourceShareLogMapper.insert(log);
        }
    }

    /**
     * 测试环境配置共享
     */
    public void shareEnvironmentConfig(Long envId, List<Long> targetProjectIds) {
        TestEnvironment sourceEnv = environmentMapper.selectById(envId);

        for (Long projectId : targetProjectIds) {
            // 创建环境配置副本，隐藏敏感信息
            TestEnvironment sharedEnv = createSharedEnvironment(sourceEnv, projectId);
            environmentMapper.insert(sharedEnv);
        }
    }
}
```

### 1.3 用户界面设计

#### 项目切换界面
```vue
<template>
  <div class="project-switcher">
    <el-dropdown @command="switchProject">
      <span class="current-project">
        {{ currentProject.projectName }}
        <i class="el-icon-arrow-down"></i>
      </span>
      <el-dropdown-menu slot="dropdown">
        <el-dropdown-item
          v-for="project in accessibleProjects"
          :key="project.projectId"
          :command="project.projectId">
          {{ project.projectName }}
          <el-tag v-if="project.projectId === currentProject.projectId"
                   type="success" size="mini">当前</el-tag>
        </el-dropdown-item>
        <el-dropdown-item divided command="create-project">
          <i class="el-icon-plus"></i> 创建新项目
        </el-dropdown-item>
      </el-dropdown-menu>
    </el-dropdown>
  </div>
</template>

<script>
export default {
  data() {
    return {
      currentProject: {},
      accessibleProjects: []
    }
  },
  methods: {
    async switchProject(projectId) {
      if (projectId === 'create-project') {
        this.$router.push('/project/create');
        return;
      }

      try {
        await this.$store.dispatch('project/switchProject', projectId);
        this.$message.success('项目切换成功');
        this.$router.go(0); // 刷新页面以更新权限
      } catch (error) {
        this.$message.error('项目切换失败');
      }
    }
  }
}
</script>
```

## 2. 大规模测试执行场景

### 2.1 场景描述
单次执行涉及数千个测试用例，需要高效的任务调度、并行执行、资源管理和结果收集。

### 2.2 分布式执行架构

#### 任务调度器设计
```java
@Component
public class DistributedTestScheduler {

    @Autowired
    private RedisTemplate<String, Object> redisTemplate;

    @Autowired
    private TaskExecutor taskExecutor;

    private final String TASK_QUEUE_KEY = "test:execution:queue";
    private final String EXECUTION_LOCK_KEY = "test:execution:lock:";

    /**
     * 提交批量测试任务
     */
    public CompletableFuture<BatchTestResult> submitBatchTest(
            Long executionId, List<TestCase> testCases, ExecutionContext context) {

        return CompletableFuture.supplyAsync(() -> {
            try {
                // 1. 任务分组和优先级排序
                List<TestTaskGroup> taskGroups = groupTestTasks(testCases, context);

                // 2. 分配执行节点
                Map<String, List<TestTaskGroup>> nodeAssignments =
                    assignTasksToNodes(taskGroups);

                // 3. 并行执行任务
                List<CompletableFuture<TestResult>> futures = new ArrayList<>();

                for (Map.Entry<String, List<TestTaskGroup>> entry : nodeAssignments.entrySet()) {
                    String nodeId = entry.getKey();
                    List<TestTaskGroup> groups = entry.getValue();

                    CompletableFuture<TestResult> future =
                        executeTasksOnNode(nodeId, groups, context);
                    futures.add(future);
                }

                // 4. 收集执行结果
                List<TestResult> results = futures.stream()
                    .map(CompletableFuture::join)
                    .collect(Collectors.toList());

                return new BatchTestResult(results);

            } catch (Exception e) {
                throw new RuntimeException("批量测试执行失败", e);
            }
        }, taskExecutor);
    }

    /**
     * 任务分组策略
     */
    private List<TestTaskGroup> groupTestTasks(List<TestCase> testCases, ExecutionContext context) {
        // 按测试类型分组
        Map<String, List<TestCase>> typeGroups = testCases.stream()
            .collect(Collectors.groupingBy(TestCase::getCaseType));

        List<TestTaskGroup> groups = new ArrayList<>();

        for (Map.Entry<String, List<TestCase>> entry : typeGroups.entrySet()) {
            String caseType = entry.getKey();
            List<TestCase> cases = entry.getValue();

            // 根据类型特性进一步分组
            switch (caseType) {
                case "WEB_UI":
                    groups.addAll(groupWebUITests(cases, context));
                    break;
                case "API":
                    groups.addAll(groupAPITests(cases, context));
                    break;
                case "UNIT":
                    groups.addAll(groupUnitTests(cases, context));
                    break;
            }
        }

        return groups;
    }

    /**
     * Web UI测试分组（考虑浏览器资源限制）
     */
    private List<TestTaskGroup> groupWebUITests(List<TestCase> cases, ExecutionContext context) {
        int maxConcurrentBrowsers = getMaxConcurrentBrowsers(context);
        int groupSize = Math.max(1, cases.size() / maxConcurrentBrowsers);

        return IntStream.range(0, (cases.size() + groupSize - 1) / groupSize)
            .mapToObj(i -> new TestTaskGroup(
                "WEB_UI_GROUP_" + i,
                cases.subList(i * groupSize, Math.min((i + 1) * groupSize, cases.size())),
                "WEB_UI"
            ))
            .collect(Collectors.toList());
    }
}
```

#### 负载均衡器设计
```java
@Component
public class ExecutionNodeLoadBalancer {

    @Autowired
    private NodeMonitorService nodeMonitorService;

    /**
     * 选择最优执行节点
     */
    public String selectOptimalNode(TestTaskGroup taskGroup, ExecutionContext context) {
        List<ExecutionNode> availableNodes = getAvailableNodes(taskGroup.getTestType());

        if (availableNodes.isEmpty()) {
            throw new RuntimeException("没有可用的执行节点");
        }

        // 计算节点负载权重
        Map<String, Double> nodeWeights = calculateNodeWeights(availableNodes, taskGroup);

        // 选择权重最低的节点（负载最轻）
        return nodeWeights.entrySet().stream()
            .min(Map.Entry.comparingByValue())
            .map(Map.Entry::getKey)
            .orElse(availableNodes.get(0).getNodeId());
    }

    /**
     * 计算节点负载权重
     */
    private Map<String, Double> calculateNodeWeights(
            List<ExecutionNode> nodes, TestTaskGroup taskGroup) {

        Map<String, Double> weights = new HashMap<>();

        for (ExecutionNode node : nodes) {
            double weight = 0.0;

            // CPU使用率权重 (30%)
            weight += node.getCpuUsage() * 0.3;

            // 内存使用率权重 (25%)
            weight += node.getMemoryUsage() * 0.25;

            // 当前任务数权重 (25%)
            weight += (double) node.getCurrentTaskCount() / node.getMaxTaskCount() * 0.25;

            // 网络延迟权重 (10%)
            weight += node.getNetworkLatency() / 1000.0 * 0.1;

            // 任务类型适配性权重 (10%)
            weight += getTaskTypeCompatibilityScore(node, taskGroup) * 0.1;

            weights.put(node.getNodeId(), weight);
        }

        return weights;
    }

    /**
     * 获取任务类型兼容性评分
     */
    private double getTaskTypeCompatibilityScore(ExecutionNode node, TestTaskGroup taskGroup) {
        Map<String, Boolean> supportedTypes = node.getSupportedTestTypes();

        if (supportedTypes.containsKey(taskGroup.getTestType())) {
            return supportedTypes.get(taskGroup.getTestType()) ? 0.0 : 1.0;
        }

        return 0.5; // 未知类型，给予中等评分
    }
}
```

#### 结果收集器设计
```java
@Component
public class TestResultCollector {

    @Autowired
    private RedisTemplate<String, Object> redisTemplate;

    private final String RESULT_KEY_PREFIX = "test:result:";

    /**
     * 实时收集测试结果
     */
    public void collectResult(Long executionId, TestResult result) {
        try {
            // 1. 存储到Redis（实时访问）
            String resultKey = RESULT_KEY_PREFIX + executionId;
            redisTemplate.opsForList().rightPush(resultKey, result);

            // 2. 更新执行统计
            updateExecutionStatistics(executionId, result);

            // 3. 发送实时通知
            sendRealTimeNotification(executionId, result);

            // 4. 异步持久化到数据库
            CompletableFuture.runAsync(() -> persistResult(executionId, result));

        } catch (Exception e) {
            logger.error("收集测试结果失败", e);
        }
    }

    /**
     * 批量收集测试结果
     */
    public void collectBatchResults(Long executionId, List<TestResult> results) {
        // 使用管道批量存储到Redis
        redisTemplate.executePipelined((RedisCallback<Object>) connection -> {
            String resultKey = RESULT_KEY_PREFIX + executionId;

            for (TestResult result : results) {
                connection.listRightPush(
                    resultKey.getBytes(),
                    SerializationUtils.serialize(result)
                );
            }

            return null;
        });

        // 批量更新统计
        BatchStatistics statistics = calculateBatchStatistics(results);
        updateBatchStatistics(executionId, statistics);
    }

    /**
     * 获取实时执行进度
     */
    public ExecutionProgress getExecutionProgress(Long executionId) {
        String resultKey = RESULT_KEY_PREFIX + executionId;

        Long totalResults = redisTemplate.opsForList().size(resultKey);
        List<Object> results = redisTemplate.opsForList().range(resultKey, 0, -1);

        ExecutionProgress progress = new ExecutionProgress();
        progress.setExecutionId(executionId);
        progress.setTotalResults(totalResults != null ? totalResults.intValue() : 0);

        if (results != null && !results.isEmpty()) {
            Map<String, Long> statusCounts = results.stream()
                .map(obj -> (TestResult) obj)
                .collect(Collectors.groupingBy(
                    TestResult::getStatus,
                    Collectors.counting()
                ));

            progress.setPassedCount(statusCounts.getOrDefault("PASSED", 0L).intValue());
            progress.setFailedCount(statusCounts.getOrDefault("FAILED", 0L).intValue());
            progress.setRunningCount(statusCounts.getOrDefault("RUNNING", 0L).intValue());
            progress.setPendingCount(statusCounts.getOrDefault("PENDING", 0L).intValue());
        }

        return progress;
    }
}
```

### 2.3 性能优化策略

#### 连接池管理
```java
@Configuration
public class ConnectionPoolConfig {

    /**
     * 数据库连接池配置
     */
    @Bean
    @Primary
    public DataSource masterDataSource() {
        HikariConfig config = new HikariConfig();
        config.setJdbcUrl("jdbc:mysql://localhost:3306/autotest_platform");
        config.setUsername("root");
        config.setPassword("password");

        // 优化连接池参数
        config.setMaximumPoolSize(50);
        config.setMinimumIdle(10);
        config.setConnectionTimeout(30000);
        config.setIdleTimeout(600000);
        config.setMaxLifetime(1800000);
        config.setLeakDetectionThreshold(60000);

        return new HikariDataSource(config);
    }

    /**
     * Redis连接池配置
     */
    @Bean
    public LettuceConnectionFactory redisConnectionFactory() {
        LettuceClientConfiguration clientConfig = LettuceClientConfiguration.builder()
            .commandTimeout(Duration.ofSeconds(2))
            .shutdownTimeout(Duration.ZERO)
            .build();

        RedisStandaloneConfiguration serverConfig = new RedisStandaloneConfiguration();
        serverConfig.setHostName("localhost");
        serverConfig.setPort(6379);
        serverConfig.setDatabase(0);

        return new LettuceConnectionFactory(serverConfig, clientConfig);
    }
}
```

#### 缓存策略
```java
@Service
public class TestResultCacheService {

    @Autowired
    private CacheManager cacheManager;

    private static final String CACHE_NAME = "testResults";

    /**
     * 缓存测试结果（短期缓存）
     */
    @Cacheable(value = CACHE_NAME, key = "#executionId + ':' + #caseId", unless = "#result.status == 'RUNNING'")
    public TestResult getCachedResult(Long executionId, Long caseId) {
        return null; // 由缓存注解处理
    }

    /**
     * 预热缓存
     */
    public void warmupCache(Long executionId) {
        // 预加载最近执行的测试结果
        List<TestResult> recentResults = testResultMapper.selectRecentResults(executionId, 100);

        Cache cache = cacheManager.getCache(CACHE_NAME);
        recentResults.forEach(result -> {
            String key = executionId + ":" + result.getCaseId();
            cache.put(key, result);
        });
    }

    /**
     * 清理过期缓存
     */
    @Scheduled(fixedRate = 300000) // 5分钟执行一次
    public void cleanupExpiredCache() {
        Cache cache = cacheManager.getCache(CACHE_NAME);
        if (cache instanceof RedisCache) {
            // Redis会自动过期，无需手动清理
            return;
        }

        // 对于本地缓存，手动清理过期数据
        if (cache instanceof CaffeineCache) {
            CaffeineCache caffeineCache = (CaffeineCache) cache;
            // Caffeine会自动清理过期条目
        }
    }
}
```

## 3. CI/CD集成场景

### 3.1 场景描述
与Jenkins、GitLab CI、GitHub Actions等CI/CD系统集成，实现代码提交后自动触发测试执行，并将结果反馈给CI/CD系统。

### 3.2 Webhook设计

#### Git Webhook处理器
```java
@RestController
@RequestMapping("/api/v1/webhooks")
public class WebhookController {

    @Autowired
    private WebhookService webhookService;

    @PostMapping("/git-push")
    public ResponseEntity<WebhookResponse> handleGitPush(
            @RequestBody GitPushPayload payload,
            @RequestHeader("X-Webhook-Signature") String signature) {

        try {
            // 1. 验证Webhook签名
            if (!validateWebhookSignature(payload, signature)) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
            }

            // 2. 解析项目信息
            String projectCode = extractProjectCode(payload);
            TestProject project = projectService.selectByProjectCode(projectCode);

            if (project == null) {
                return ResponseEntity.notFound().build();
            }

            // 3. 创建执行任务
            TestExecution execution = webhookService.createExecutionFromWebhook(payload, project);

            // 4. 异步执行测试
            CompletableFuture.runAsync(() -> {
                testExecutionService.executeTestAsync(execution.getExecutionId());
            });

            WebhookResponse response = new WebhookResponse();
            response.setExecutionId(execution.getExecutionId());
            response.setStatus("PENDING");
            response.setMessage("测试任务已创建");

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            logger.error("处理Git Webhook失败", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @PostMapping("/jenkins-notification")
    public ResponseEntity<Void> handleJenkinsNotification(
            @RequestBody JenkinsNotificationPayload payload) {

        // 处理Jenkins构建通知
        webhookService.processJenkinsNotification(payload);

        return ResponseEntity.ok().build();
    }
}
```

#### CI/CD集成服务
```java
@Service
public class CIDIntegrationService {

    @Autowired
    private RestTemplate restTemplate;

    @Value("${ci.jenkins.url}")
    private String jenkinsUrl;

    @Value("${ci.gitlab.url}")
    private String gitlabUrl;

    /**
     * 创建Jenkins任务
     */
    public JenkinsJob createJenkinsJob(TestProject project, TestExecution execution) {
        try {
            JenkinsJobConfig config = new JenkinsJobConfig();
            config.setJobName("autotest_" + project.getProjectCode() + "_" + execution.getExecutionId());
            config.setProjectType("pipeline");

            // 生成Jenkinsfile
            String jenkinsfile = generateJenkinsfile(project, execution);
            config.setJenkinsfile(jenkinsfile);

            // 调用Jenkins API创建任务
            String url = jenkinsUrl + "/createItem?name=" + config.getJobName();

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_XML);
            headers.setBasicAuth("admin", "apiToken");

            HttpEntity<String> request = new HttpEntity<>(config.toXml(), headers);
            ResponseEntity<String> response = restTemplate.postForEntity(url, request, String.class);

            if (response.getStatusCode().is2xxSuccessful()) {
                return new JenkinsJob(config.getJobName(), "CREATED");
            } else {
                throw new RuntimeException("创建Jenkins任务失败: " + response.getBody());
            }

        } catch (Exception e) {
            logger.error("创建Jenkins任务失败", e);
            throw new RuntimeException("创建Jenkins任务失败", e);
        }
    }

    /**
     * 生成Jenkins Pipeline脚本
     */
    private String generateJenkinsfile(TestProject project, TestExecution execution) {
        return String.format("""
            pipeline {
                agent any

                environment {
                    TEST_PLATFORM_URL = '%s'
                    EXECUTION_ID = '%d'
                    PROJECT_CODE = '%s'
                }

                stages {
                    stage('Checkout') {
                        steps {
                            git branch: '%s', url: '%s'
                        }
                    }

                    stage('Run Tests') {
                        steps {
                            script {
                                // 触发测试平台执行
                                def response = sh(
                                    script: '''curl -X POST \\
                                        "${TEST_PLATFORM_URL}/api/v1/executions/${EXECUTION_ID}/start" \\
                                        -H "Content-Type: application/json" \\
                                        -H "Authorization: Bearer ${TEST_PLATFORM_TOKEN}"''',
                                    returnStdout: true
                                ).trim()

                                echo "测试执行响应: ${response}"

                                // 等待测试完成
                                waitForTestCompletion()
                            }
                        }
                    }

                    stage('Report Results') {
                        steps {
                            script {
                                // 获取测试报告
                                def reportUrl = "${TEST_PLATFORM_URL}/api/v1/executions/${EXECUTION_ID}/reports/export"

                                sh """
                                    curl -o test-report.pdf "${reportUrl}?format=pdf"
                                    archiveArtifacts artifacts: 'test-report.pdf', fingerprint: true
                                """

                                // 根据测试结果设置构建状态
                                def testResult = getTestResult()
                                if (testResult.status == 'SUCCESS') {
                                    currentBuild.result = 'SUCCESS'
                                } else {
                                    currentBuild.result = 'UNSTABLE'
                                    echo "测试失败原因: ${testResult.errorMessage}"
                                }
                            }
                        }
                    }
                }

                post {
                    always {
                        // 发送通知
                        script {
                            sendNotification(currentBuild.result)
                        }
                    }
                }
            }

            def waitForTestCompletion() {
                def maxWaitTime = 60 // 最大等待60分钟
                def waitInterval = 30 // 每30秒检查一次
                def elapsedTime = 0

                while (elapsedTime < maxWaitTime * 60) {
                    def status = sh(
                        script: """curl -s "${TEST_PLATFORM_URL}/api/v1/executions/${EXECUTION_ID}/status" \\
                            -H "Authorization: Bearer ${TEST_PLATFORM_TOKEN}" """,
                        returnStdout: true
                    ).trim()

                    def statusData = readJSON text: status

                    if (statusData.executionStatus == 'COMPLETED') {
                        echo "测试执行完成"
                        break
                    }

                    echo "测试执行中... 状态: ${statusData.executionStatus}"
                    sleep time: waitInterval, unit: 'SECONDS'
                    elapsedTime += waitInterval
                }

                if (elapsedTime >= maxWaitTime * 60) {
                    error "测试执行超时"
                }
            }

            def sendNotification(buildResult) {
                // 发送钉钉通知
                def dingtalkUrl = 'https://oapi.dingtalk.com/robot/send?access_token=YOUR_TOKEN'
                def message = """
                {
                    "msgtype": "markdown",
                    "markdown": {
                        "title": "自动化测试通知",
                        "text": "## 自动化测试通知\\n\\n**项目**: ${PROJECT_CODE}\\n**执行ID**: ${EXECUTION_ID}\\n**构建结果**: ${buildResult}\\n**查看详情**: [测试报告](${TEST_PLATFORM_URL}/reports/${EXECUTION_ID})"
                    }
                }
                """

                sh """curl -X POST "${dingtalkUrl}" -H 'Content-Type: application/json' -d '${message}'"""
            }
            """,
            jenkinsUrl,
            execution.getExecutionId(),
            project.getProjectCode(),
            project.getGitBranch(),
            project.getGitRepoUrl()
        );
    }
}
```

### 3.3 结果反馈机制

#### 状态回调服务
```java
@Service
public class CallbackService {

    @Autowired
    private RestTemplate restTemplate;

    /**
     * 执行完成后回调CI/CD系统
     */
    public void notifyCICompletion(TestExecution execution) {
        // 查找回调配置
        List<CICallbackConfig> callbacks = callbackMapper.selectByExecutionId(execution.getExecutionId());

        for (CICallbackConfig callback : callbacks) {
            try {
                executeCallback(callback, execution);
            } catch (Exception e) {
                logger.error("执行CI回调失败", e);
            }
        }
    }

    private void executeCallback(CICallbackConfig callback, TestExecution execution) {
        CallbackPayload payload = new CallbackPayload();
        payload.setExecutionId(execution.getExecutionId());
        payload.setStatus(execution.getExecutionStatus());
        payload.setResult(execution.getSuccessRate() >= 80.0 ? "SUCCESS" : "FAILURE");
        payload.setReportUrl(generateReportUrl(execution));
        payload.setTimestamp(System.currentTimeMillis());

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        if (callback.getAuthToken() != null) {
            headers.setBearerAuth(callback.getAuthToken());
        }

        HttpEntity<CallbackPayload> request = new HttpEntity<>(payload, headers);

        ResponseEntity<String> response = restTemplate.postForEntity(
            callback.getCallbackUrl(),
            request,
            String.class
        );

        if (!response.getStatusCode().is2xxSuccessful()) {
            throw new RuntimeException("CI回调失败: " + response.getStatusCode());
        }

        // 记录回调日志
        CallbackLog log = new CallbackLog();
        log.setExecutionId(execution.getExecutionId());
        log.setCallbackUrl(callback.getCallbackUrl());
        log.setResponseStatus(response.getStatusCode().value());
        log.setResponseTime(System.currentTimeMillis());
        callbackLogMapper.insert(log);
    }
}
```

## 4. 测试报告分析场景

### 4.1 场景描述
提供多维度的测试报告分析，包括趋势分析、根因分析、性能分析和智能预测等功能。

### 4.2 报告生成引擎

#### 报告模板引擎
```java
@Service
public class ReportTemplateEngine {

    @Autowired
    private ThymeleafTemplateEngine templateEngine;

    /**
     * 生成HTML测试报告
     */
    public String generateHtmlReport(TestExecution execution, ReportTemplate template) {
        Context context = new Context();

        // 准备报告数据
        context.setVariable("execution", execution);
        context.setVariable("summary", calculateSummary(execution));
        context.setVariable("charts", generateChartData(execution));
        context.setVariable("failures", getFailureAnalysis(execution));
        context.setVariable("trends", getTrendAnalysis(execution));
        context.setVariable("recommendations", generateRecommendations(execution));

        // 渲染模板
        String htmlContent = templateEngine.process(template.getTemplatePath(), context);

        return htmlContent;
    }

    /**
     * 生成PDF报告
     */
    public byte[] generatePdfReport(TestExecution execution, ReportTemplate template) {
        try {
            String htmlContent = generateHtmlReport(execution, template);

            // 使用Flying Saucer转换为PDF
            ITextRenderer renderer = new ITextRenderer();
            renderer.setDocumentFromString(htmlContent);
            renderer.layout();

            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            renderer.createPDF(outputStream);

            return outputStream.toByteArray();

        } catch (Exception e) {
            logger.error("生成PDF报告失败", e);
            throw new RuntimeException("生成PDF报告失败", e);
        }
    }
}
```

#### 数据分析服务
```java
@Service
public class TestDataAnalyticsService {

    /**
     * 趋势分析
     */
    public TrendAnalysis calculateTrendAnalysis(Long projectId, LocalDate startDate, LocalDate endDate) {
        List<TestExecution> executions = executionMapper.selectByDateRange(projectId, startDate, endDate);

        TrendAnalysis analysis = new TrendAnalysis();

        // 计算通过率趋势
        List<TimeSeriesData> passRateTrend = executions.stream()
            .map(execution -> new TimeSeriesData(
                execution.getCreateTime(),
                execution.getSuccessRate()
            ))
            .sorted(Comparator.comparing(TimeSeriesData::getTimestamp))
            .collect(Collectors.toList());

        analysis.setPassRateTrend(passRateTrend);

        // 计算执行时长趋势
        List<TimeSeriesData> durationTrend = executions.stream()
            .map(execution -> new TimeSeriesData(
                execution.getCreateTime(),
                execution.getDuration() / 1000.0 // 转换为秒
            ))
            .sorted(Comparator.comparing(TimeSeriesData::getTimestamp))
            .collect(Collectors.toList());

        analysis.setDurationTrend(durationTrend);

        // 计算趋势方向和变化率
        analysis.setPassRateTrendDirection(calculateTrendDirection(passRateTrend));
        analysis.setPassRateChangeRate(calculateChangeRate(passRateTrend));

        return analysis;
    }

    /**
     * 失败模式分析
     */
    public FailurePatternAnalysis analyzeFailurePatterns(Long projectId, LocalDate startDate, LocalDate endDate) {
        List<TestExecutionCase> failedCases = executionCaseMapper.selectFailedCasesByDateRange(
            projectId, startDate, endDate);

        FailurePatternAnalysis analysis = new FailurePatternAnalysis();

        // 按错误类型分组
        Map<String, Long> errorTypeGroups = failedCases.stream()
            .collect(Collectors.groupingBy(
                case_ -> categorizeError(case_.getErrorMessage()),
                Collectors.counting()
            ));

        analysis.setErrorTypeDistribution(errorTypeGroups);

        // 按用例分组（找出最不稳定的用例）
        Map<Long, Long> caseFailureCounts = failedCases.stream()
            .collect(Collectors.groupingBy(
                TestExecutionCase::getCaseId,
                Collectors.counting()
            ));

        List<UnstableTestCase> unstableCases = caseFailureCounts.entrySet().stream()
            .map(entry -> new UnstableTestCase(
                entry.getKey(),
                getCaseTitle(entry.getKey()),
                entry.getValue()
            ))
            .sorted(Comparator.comparing(UnstableTestCase::getFailureCount).reversed())
            .limit(10)
            .collect(Collectors.toList());

        analysis.setMostUnstableCases(unstableCases);

        // 按时间模式分析
        Map<String, Long> timePatternGroups = failedCases.stream()
            .collect(Collectors.groupingBy(
                case_ -> categorizeTimePattern(case_.getStartTime()),
                Collectors.counting()
            ));

        analysis.setTimePatternDistribution(timePatternGroups);

        return analysis;
    }

    /**
     * 性能基线分析
     */
    public PerformanceBaselineAnalysis analyzePerformanceBaseline(Long projectId, Long caseId) {
        List<TestExecutionCase> recentExecutions = executionCaseMapper.selectRecentExecutions(
            projectId, caseId, 30); // 最近30次执行

        PerformanceBaselineAnalysis analysis = new PerformanceBaselineAnalysis();

        if (recentExecutions.isEmpty()) {
            return analysis;
        }

        // 计算执行时间统计
        List<Double> executionTimes = recentExecutions.stream()
            .filter(case_ -> case_.getDuration() != null && case_.getDuration() > 0)
            .map(case_ -> case_.getDuration() / 1000.0) // 转换为秒
            .collect(Collectors.toList());

        if (!executionTimes.isEmpty()) {
            analysis.setAverageExecutionTime(executionTimes.stream()
                .mapToDouble(Double::doubleValue).average().orElse(0.0));
            analysis.setMinExecutionTime(executionTimes.stream()
                .mapToDouble(Double::doubleValue).min().orElse(0.0));
            analysis.setMaxExecutionTime(executionTimes.stream()
                .mapToDouble(Double::doubleValue).max().orElse(0.0));
            analysis.setPercentile95(executionTimes.stream()
                .sorted().skip((long) (executionTimes.size() * 0.95)).findFirst().orElse(0.0));
        }

        // 检测性能异常
        List<PerformanceAnomaly> anomalies = detectPerformanceAnomalies(recentExecutions);
        analysis.setPerformanceAnomalies(anomalies);

        return analysis;
    }

    /**
     * 智能推荐生成
     */
    public List<Recommendation> generateRecommendations(TestExecution execution) {
        List<Recommendation> recommendations = new ArrayList<>();

        // 基于失败率的推荐
        if (execution.getSuccessRate() < 80.0) {
            recommendations.add(new Recommendation(
                "PRIORITY",
                "测试通过率偏低",
                "当前通过率为" + execution.getSuccessRate() + "%，建议优先修复失败的测试用例",
                "REVIEW_FAILURES"
            ));
        }

        // 基于执行时间的推荐
        if (execution.getDuration() > 30 * 60 * 1000) { // 超过30分钟
            recommendations.add(new Recommendation(
                "PERFORMANCE",
                "执行时间过长",
                "测试执行时间为" + (execution.getDuration() / 60000) + "分钟，建议优化测试用例或增加并行度",
                "OPTIMIZE_EXECUTION"
            ));
        }

        // 基于历史趋势的推荐
        TrendAnalysis trend = calculateTrendAnalysis(
            execution.getProjectId(),
            LocalDate.now().minusDays(7),
            LocalDate.now()
        );

        if ("DECLINING".equals(trend.getPassRateTrendDirection())) {
            recommendations.add(new Recommendation(
                "QUALITY",
                "质量趋势下降",
                "最近一周测试通过率呈下降趋势，建议加强代码审查和测试覆盖",
                "IMPROVE_QUALITY"
            ));
        }

        return recommendations;
    }
}
```

### 4.3 可视化图表服务

#### 图表数据生成器
```java
@Service
public class ChartDataGenerator {

    /**
     * 生成通过率趋势图数据
     */
    public ChartData generatePassRateTrendChart(List<TestExecution> executions) {
        ChartData chartData = new ChartData();
        chartData.setType("line");
        chartData.setTitle("测试通过率趋势");

        List<String> labels = executions.stream()
            .map(execution -> formatDate(execution.getCreateTime()))
            .collect(Collectors.toList());

        List<Number> data = executions.stream()
            .map(TestExecution::getSuccessRate)
            .collect(Collectors.toList());

        chartData.setLabels(labels);
        chartData.addDataset(new Dataset("通过率(%)", data, "#67C23A"));

        return chartData;
    }

    /**
     * 生成用例状态分布饼图
     */
    public ChartData generateStatusDistributionChart(TestExecution execution) {
        ChartData chartData = new ChartData();
        chartData.setType("pie");
        chartData.setTitle("测试用例状态分布");

        Map<String, Integer> statusCounts = new HashMap<>();
        statusCounts.put("通过", execution.getPassedCases());
        statusCounts.put("失败", execution.getFailedCases());
        statusCounts.put("跳过", execution.getSkippedCases());

        chartData.setLabels(Arrays.asList("通过", "失败", "跳过"));

        List<Dataset> datasets = Arrays.asList(
            new Dataset("用例数量",
                Arrays.asList(
                    statusCounts.get("通过"),
                    statusCounts.get("失败"),
                    statusCounts.get("跳过")
                ),
                Arrays.asList("#67C23A", "#F56C6C", "#E6A23C")
            )
        );

        chartData.setDatasets(datasets);

        return chartData;
    }

    /**
     * 生成模块测试覆盖热力图
     */
    public ChartData generateModuleCoverageChart(Long projectId) {
        List<TestCoverage> coverages = coverageMapper.selectByProjectId(projectId);

        ChartData chartData = new ChartData();
        chartData.setType("heatmap");
        chartData.setTitle("模块测试覆盖率热力图");

        // 按模块分组
        Map<String, List<TestCoverage>> moduleGroups = coverages.stream()
            .collect(Collectors.groupingBy(TestCoverage::getModuleName));

        List<String> modules = new ArrayList<>(moduleGroups.keySet());
        List<String> dates = coverages.stream()
            .map(coverage -> formatDate(coverage.getTestDate()))
            .distinct()
            .sorted()
            .collect(Collectors.toList());

        // 生成热力图数据矩阵
        List<List<Number>> heatData = new ArrayList<>();

        for (String module : modules) {
            List<Number> rowData = new ArrayList<>();
            List<TestCoverage> moduleCoverages = moduleGroups.get(module);

            for (String date : dates) {
                Optional<TestCoverage> coverage = moduleCoverages.stream()
                    .filter(c -> formatDate(c.getTestDate()).equals(date))
                    .findFirst();

                rowData.add(coverage.map(TestCoverage::getCoverage).orElse(0));
            }

            heatData.add(rowData);
        }

        chartData.setLabels(dates);
        chartData.setYLabels(modules);
        chartData.setData(heatData);

        return chartData;
    }
}
```

这个多业务场景设计文档详细描述了：

1. **多团队协作** - 数据隔离、权限管理、资源共享机制
2. **大规模测试执行** - 分布式调度、负载均衡、性能优化
3. **CI/CD集成** - Webhook处理、Jenkins集成、结果反馈
4. **测试报告分析** - 模板引擎、数据分析、可视化展示

这些设计确保了平台能够满足企业级多团队使用的复杂需求。