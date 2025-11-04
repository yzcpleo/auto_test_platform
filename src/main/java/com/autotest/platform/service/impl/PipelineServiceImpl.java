package com.autotest.platform.service.impl;

import com.autotest.platform.cicd.webhook.GitWebhookHandler;
import com.autotest.platform.cicd.jenkins.JenkinsIntegrationService;
import com.autotest.platform.common.core.domain.AjaxResult;
import com.autotest.platform.common.core.page.TableDataInfo;
import com.autotest.platform.common.exception.ServiceException;
import com.autotest.platform.domain.cicd.Pipeline;
import com.autotest.platform.domain.cicd.PipelineExecution;
import com.autotest.platform.domain.cicd.WebhookEvent;
import com.autotest.platform.mapper.PipelineExecutionMapper;
import com.autotest.platform.mapper.PipelineMapper;
import com.autotest.platform.mapper.WebhookEventMapper;
import com.autotest.platform.service.IPipelineService;
import com.autotest.platform.service.ITestReportService;
import com.autotest.platform.service.ITestExecutionService;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

/**
 * 流水线Service业务层处理
 *
 * @author autotest
 * @date 2024-01-01
 */
@Slf4j
@Service
public class PipelineServiceImpl extends ServiceImpl<PipelineMapper, Pipeline> implements IPipelineService {

    @Autowired
    private PipelineMapper pipelineMapper;

    @Autowired
    private PipelineExecutionMapper pipelineExecutionMapper;

    @Autowired
    private WebhookEventMapper webhookEventMapper;

    @Autowired
    private GitWebhookHandler gitWebhookHandler;

    @Autowired
    private JenkinsIntegrationService jenkinsIntegrationService;

    @Autowired
    private ITestReportService testReportService;

    @Autowired
    private ITestExecutionService testExecutionService;

    @Autowired
    private ObjectMapper objectMapper;

    // 存储正在执行的流水线
    private final Map<String, PipelineExecutionContext> runningExecutions = new ConcurrentHashMap<>();

    // 执行ID生成器
    private final AtomicLong executionIdGenerator = new AtomicLong(1);

    /**
     * 查询流水线
     *
     * @param pipelineId 流水线主键
     * @return 流水线
     */
    @Override
    public Pipeline selectPipelineByPipelineId(Long pipelineId) {
        return pipelineMapper.selectById(pipelineId);
    }

    /**
     * 查询流水线列表
     *
     * @param pipeline 流水线
     * @return 流水线
     */
    @Override
    public List<Pipeline> selectPipelineList(Pipeline pipeline) {
        LambdaQueryWrapper<Pipeline> queryWrapper = new LambdaQueryWrapper<>();

        if (pipeline.getProjectId() != null) {
            queryWrapper.eq(Pipeline::getProjectId, pipeline.getProjectId());
        }
        if (StringUtils.hasText(pipeline.getPipelineName())) {
            queryWrapper.like(Pipeline::getPipelineName, pipeline.getPipelineName());
        }
        if (StringUtils.hasText(pipeline.getPipelineType())) {
            queryWrapper.eq(Pipeline::getPipelineType, pipeline.getPipelineType());
        }
        if (StringUtils.hasText(pipeline.getStatus())) {
            queryWrapper.eq(Pipeline::getStatus, pipeline.getStatus());
        }
        if (StringUtils.hasText(pipeline.getTriggerType())) {
            queryWrapper.eq(Pipeline::getTriggerType, pipeline.getTriggerType());
        }

        queryWrapper.orderByDesc(Pipeline::getCreateTime);

        return pipelineMapper.selectList(queryWrapper);
    }

    /**
     * 新增流水线
     *
     * @param pipeline 流水线
     * @return 结果
     */
    @Override
    @Transactional
    public int insertPipeline(Pipeline pipeline) {
        // 设置默认值
        if (pipeline.getStatus() == null) {
            pipeline.setStatus("ACTIVE");
        }

        // 验证流水线配置
        validatePipelineConfig(pipeline);

        return pipelineMapper.insert(pipeline);
    }

    /**
     * 修改流水线
     *
     * @param pipeline 流水线
     * @return 结果
     */
    @Override
    @Transactional
    public int updatePipeline(Pipeline pipeline) {
        // 验证流水线配置
        validatePipelineConfig(pipeline);

        return pipelineMapper.updateById(pipeline);
    }

    /**
     * 批量删除流水线
     *
     * @param pipelineIds 需要删除的流水线主键
     * @return 结果
     */
    @Override
    @Transactional
    public int deletePipelineByPipelineIds(Long[] pipelineIds) {
        // 检查是否有正在执行的流水线
        for (Long pipelineId : pipelineIds) {
            LambdaQueryWrapper<PipelineExecution> queryWrapper = new LambdaQueryWrapper<>();
            queryWrapper.eq(PipelineExecution::getPipelineId, pipelineId)
                       .in(PipelineExecution::getStatus, "RUNNING", "PENDING");

            long runningCount = pipelineExecutionMapper.selectCount(queryWrapper);
            if (runningCount > 0) {
                throw new ServiceException("流水线正在执行中，无法删除");
            }
        }

        return pipelineMapper.deleteBatchIds(Arrays.asList(pipelineIds));
    }

    /**
     * 删除流水线信息
     *
     * @param pipelineId 流水线主键
     * @return 结果
     */
    @Override
    @Transactional
    public int deletePipelineByPipelineId(Long pipelineId) {
        return deletePipelineByPipelineIds(new Long[]{pipelineId});
    }

    /**
     * 执行流水线
     *
     * @param pipelineId 流水线ID
     * @param params 执行参数
     * @param triggerUserId 触发用户ID
     * @return 执行记录
     */
    @Override
    @Async
    @Transactional
    public PipelineExecution executePipeline(Long pipelineId, Map<String, Object> params, Long triggerUserId) {
        Pipeline pipeline = selectPipelineByPipelineId(pipelineId);
        if (pipeline == null) {
            throw new ServiceException("流水线不存在");
        }

        if (!"ACTIVE".equals(pipeline.getStatus())) {
            throw new ServiceException("流水线未启用");
        }

        // 创建执行记录
        PipelineExecution execution = createPipelineExecution(pipeline, params, triggerUserId);

        try {
            // 异步执行流水线
            CompletableFuture.runAsync(() -> executePipelineSteps(execution, pipeline));

            return execution;
        } catch (Exception e) {
            execution.setStatus("FAILED");
            execution.setErrorMessage("启动执行失败: " + e.getMessage());
            execution.setEndTime(LocalDateTime.now());
            pipelineExecutionMapper.updateById(execution);
            throw e;
        }
    }

    /**
     * 停止流水线执行
     *
     * @param executionId 执行ID
     * @return 是否成功
     */
    @Override
    public boolean stopPipelineExecution(Long executionId) {
        PipelineExecution execution = pipelineExecutionMapper.selectById(executionId);
        if (execution == null) {
            return false;
        }

        if (!"RUNNING".equals(execution.getStatus())) {
            return false;
        }

        // 更新执行状态
        execution.setStatus("STOPPED");
        execution.setEndTime(LocalDateTime.now());
        pipelineExecutionMapper.updateById(execution);

        // 从运行中的执行列表移除
        String executionKey = execution.getExecutionCode();
        runningExecutions.remove(executionKey);

        log.info("流水线执行已停止: {}", executionId);
        return true;
    }

    /**
     * 重新执行流水线
     *
     * @param executionId 执行ID
     * @return 新执行记录
     */
    @Override
    @Async
    @Transactional
    public PipelineExecution retryExecution(Long executionId) {
        PipelineExecution originalExecution = pipelineExecutionMapper.selectById(executionId);
        if (originalExecution == null) {
            throw new ServiceException("原始执行记录不存在");
        }

        Pipeline pipeline = selectPipelineByPipelineId(originalExecution.getPipelineId());
        if (pipeline == null) {
            throw new ServiceException("流水线不存在");
        }

        // 复制执行参数
        Map<String, Object> params = new HashMap<>();
        if (StringUtils.hasText(originalExecution.getExecutionParams())) {
            try {
                params = objectMapper.readValue(originalExecution.getExecutionParams(),
                    new TypeReference<Map<String, Object>>() {});
            } catch (Exception e) {
                log.warn("解析执行参数失败: {}", e.getMessage());
            }
        }

        // 创建新的执行记录
        PipelineExecution newExecution = createPipelineExecution(pipeline, params, originalExecution.getTriggerUserId());
        newExecution.setRetriedFrom(executionId);

        try {
            // 异步执行流水线
            CompletableFuture.runAsync(() -> executePipelineSteps(newExecution, pipeline));

            return newExecution;
        } catch (Exception e) {
            newExecution.setStatus("FAILED");
            newExecution.setErrorMessage("重新执行启动失败: " + e.getMessage());
            newExecution.setEndTime(LocalDateTime.now());
            pipelineExecutionMapper.updateById(newExecution);
            throw e;
        }
    }

    /**
     * 获取流水线执行记录
     *
     * @param pipelineId 流水线ID
     * @return 执行记录列表
     */
    @Override
    public List<PipelineExecution> getPipelineExecutions(Long pipelineId) {
        LambdaQueryWrapper<PipelineExecution> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(PipelineExecution::getPipelineId, pipelineId)
                   .orderByDesc(PipelineExecution::getCreateTime);

        return pipelineExecutionMapper.selectList(queryWrapper);
    }

    /**
     * 获取执行详情
     *
     * @param executionId 执行ID
     * @return 执行详情
     */
    @Override
    public PipelineExecution getExecutionDetail(Long executionId) {
        PipelineExecution execution = pipelineExecutionMapper.selectById(executionId);
        if (execution != null && StringUtils.hasText(execution.getStepResults())) {
            // 解析步骤执行结果
            try {
                List<PipelineExecution.StepExecution> stepExecutions = objectMapper.readValue(
                    execution.getStepResults(),
                    new TypeReference<List<PipelineExecution.StepExecution>>() {}
                );
                execution.setStepExecutions(stepExecutions);
            } catch (Exception e) {
                log.warn("解析步骤执行结果失败: {}", e.getMessage());
            }
        }
        return execution;
    }

    /**
     * 手动触发流水线
     *
     * @param pipelineId 流水线ID
     * @param triggerParams 触发参数
     * @param userId 用户ID
     * @return 触发结果
     */
    @Override
    public Map<String, Object> manualTrigger(Long pipelineId, Map<String, Object> triggerParams, Long userId) {
        PipelineExecution execution = executePipeline(pipelineId, triggerParams, userId);

        return Map.of(
            "executionId", execution.getExecutionId(),
            "executionCode", execution.getExecutionCode(),
            "status", execution.getStatus(),
            "message", "手动触发成功"
        );
    }

    /**
     * 验证流水线配置
     *
     * @param pipeline 流水线
     * @return 验证结果
     */
    @Override
    public Map<String, Object> validatePipelineConfig(Pipeline pipeline) {
        Map<String, Object> result = new HashMap<>();
        List<String> errors = new ArrayList<>();
        List<String> warnings = new ArrayList<>();

        try {
            // 验证基本配置
            if (!StringUtils.hasText(pipeline.getPipelineName())) {
                errors.add("流水线名称不能为空");
            }

            if (!StringUtils.hasText(pipeline.getPipelineType())) {
                errors.add("流水线类型不能为空");
            }

            if (!StringUtils.hasText(pipeline.getTriggerType())) {
                errors.add("触发类型不能为空");
            }

            // 验证流水线配置
            if (StringUtils.hasText(pipeline.getPipelineConfig())) {
                Pipeline.PipelineConfig config = objectMapper.readValue(
                    pipeline.getPipelineConfig(),
                    Pipeline.PipelineConfig.class
                );

                if (config.getSteps() == null || config.getSteps().isEmpty()) {
                    errors.add("流水线步骤不能为空");
                } else {
                    // 验证每个步骤
                    for (int i = 0; i < config.getSteps().size(); i++) {
                        Pipeline.PipelineStep step = config.getSteps().get(i);
                        if (!StringUtils.hasText(step.getStepName())) {
                            errors.add("步骤 " + (i + 1) + " 名称不能为空");
                        }
                        if (!StringUtils.hasText(step.getStepType())) {
                            errors.add("步骤 " + (i + 1) + " 类型不能为空");
                        }
                    }
                }

                // 验证触发配置
                Pipeline.TriggerConfig triggerConfig = config.getTriggerConfig();
                if (triggerConfig != null && "WEBHOOK".equals(pipeline.getTriggerType())) {
                    if (!StringUtils.hasText(triggerConfig.getWebhookUrl())) {
                        warnings.add("Webhook URL未配置，将使用默认URL");
                    }
                }
            } else {
                errors.add("流水线配置不能为空");
            }

            result.put("valid", errors.isEmpty());
            result.put("errors", errors);
            result.put("warnings", warnings);

        } catch (Exception e) {
            result.put("valid", false);
            errors.add("配置解析失败: " + e.getMessage());
            result.put("errors", errors);
        }

        return result;
    }

    /**
     * 复制流水线
     *
     * @param pipelineId 流水线ID
     * @param newName 新名称
     * @param creatorId 创建人ID
     * @return 新流水线
     */
    @Override
    @Transactional
    public Pipeline copyPipeline(Long pipelineId, String newName, Long creatorId) {
        Pipeline originalPipeline = selectPipelineByPipelineId(pipelineId);
        if (originalPipeline == null) {
            throw new ServiceException("原流水线不存在");
        }

        Pipeline newPipeline = new Pipeline();
        newPipeline.setPipelineName(newName);
        newPipeline.setPipelineType(originalPipeline.getPipelineType());
        newPipeline.setTriggerType(originalPipeline.getTriggerType());
        newPipeline.setPipelineConfig(originalPipeline.getPipelineConfig());
        newPipeline.setProjectId(originalPipeline.getProjectId());
        newPipeline.setStatus("ACTIVE");
        newPipeline.setCreateBy(String.valueOf(creatorId));

        pipelineMapper.insert(newPipeline);
        return newPipeline;
    }

    /**
     * 启用流水线
     *
     * @param pipelineIds 流水线ID列表
     * @return 启用数量
     */
    @Override
    @Transactional
    public int enablePipelines(Long[] pipelineIds) {
        int count = 0;
        for (Long pipelineId : pipelineIds) {
            Pipeline pipeline = new Pipeline();
            pipeline.setPipelineId(pipelineId);
            pipeline.setStatus("ACTIVE");
            count += pipelineMapper.updateById(pipeline);
        }
        return count;
    }

    /**
     * 禁用流水线
     *
     * @param pipelineIds 流水线ID列表
     * @return 禁用数量
     */
    @Override
    @Transactional
    public int disablePipelines(Long[] pipelineIds) {
        int count = 0;
        for (Long pipelineId : pipelineIds) {
            Pipeline pipeline = new Pipeline();
            pipeline.setPipelineId(pipelineId);
            pipeline.setStatus("INACTIVE");
            count += pipelineMapper.updateById(pipeline);
        }
        return count;
    }

    /**
     * 获取流水线统计
     *
     * @param projectId 项目ID
     * @param timeRange 时间范围
     * @return 统计数据
     */
    @Override
    public Map<String, Object> getPipelineStatistics(Long projectId, String timeRange) {
        Map<String, Object> statistics = new HashMap<>();

        // 计算时间范围
        LocalDateTime endTime = LocalDateTime.now();
        LocalDateTime startTime = calculateStartTime(endTime, timeRange);

        // 查询流水线统计
        Map<String, Object> pipelineStats = pipelineMapper.selectPipelineStatistics(projectId, startTime);
        statistics.putAll(pipelineStats);

        // 查询执行统计
        Map<String, Object> executionStats = pipelineExecutionMapper.selectExecutionStatistics(projectId, startTime);
        statistics.putAll(executionStats);

        // 查询成功率趋势
        List<Map<String, Object>> successTrend = pipelineExecutionMapper.selectSuccessTrend(projectId, startTime, endTime);
        statistics.put("successTrend", successTrend);

        return statistics;
    }

    /**
     * 从模板创建流水线
     *
     * @param templateConfig 模板配置
     * @param creatorId 创建人ID
     * @return 新流水线
     */
    @Override
    @Transactional
    public Pipeline createFromTemplate(Map<String, Object> templateConfig, Long creatorId) {
        String templateType = (String) templateConfig.get("templateType");
        String pipelineName = (String) templateConfig.get("pipelineName");
        Long projectId = Long.valueOf(templateConfig.get("projectId").toString());

        // 获取模板配置
        Pipeline.PipelineConfig config = getPipelineTemplate(templateType);
        if (config == null) {
            throw new ServiceException("流水线模板不存在");
        }

        Pipeline pipeline = new Pipeline();
        pipeline.setPipelineName(pipelineName);
        pipeline.setPipelineType(templateType);
        pipeline.setTriggerType("MANUAL");
        pipeline.setPipelineConfig(objectMapper.valueToTree(config).toString());
        pipeline.setProjectId(projectId);
        pipeline.setStatus("ACTIVE");
        pipeline.setCreateBy(String.valueOf(creatorId));

        pipelineMapper.insert(pipeline);
        return pipeline;
    }

    /**
     * 导入流水线配置
     *
     * @param importConfig 导入配置
     * @param projectId 项目ID
     * @param creatorId 创建人ID
     * @return 导入结果
     */
    @Override
    @Transactional
    public Map<String, Object> importPipelineConfig(Map<String, Object> importConfig, Long projectId, Long creatorId) {
        Map<String, Object> result = new HashMap<>();
        List<String> successList = new ArrayList<>();
        List<String> errorList = new ArrayList<>();

        @SuppressWarnings("unchecked")
        List<Map<String, Object>> pipelines = (List<Map<String, Object>>) importConfig.get("pipelines");

        for (Map<String, Object> pipelineData : pipelines) {
            try {
                Pipeline pipeline = new Pipeline();
                pipeline.setPipelineName((String) pipelineData.get("pipelineName"));
                pipeline.setPipelineType((String) pipelineData.get("pipelineType"));
                pipeline.setTriggerType((String) pipelineData.get("triggerType"));
                pipeline.setPipelineConfig(objectMapper.valueToTree(pipelineData.get("pipelineConfig")).toString());
                pipeline.setProjectId(projectId);
                pipeline.setStatus("ACTIVE");
                pipeline.setCreateBy(String.valueOf(creatorId));

                pipelineMapper.insert(pipeline);
                successList.add(pipeline.getPipelineName());

            } catch (Exception e) {
                errorList.add(pipelineData.get("pipelineName") + ": " + e.getMessage());
            }
        }

        result.put("successCount", successList.size());
        result.put("errorCount", errorList.size());
        result.put("successList", successList);
        result.put("errorList", errorList);

        return result;
    }

    /**
     * 导出流水线配置
     *
     * @param pipelineId 流水线ID
     * @return 导出配置
     */
    @Override
    public Map<String, Object> exportPipelineConfig(Long pipelineId) {
        Pipeline pipeline = selectPipelineByPipelineId(pipelineId);
        if (pipeline == null) {
            throw new ServiceException("流水线不存在");
        }

        Map<String, Object> exportConfig = new HashMap<>();
        exportConfig.put("pipelineName", pipeline.getPipelineName());
        exportConfig.put("pipelineType", pipeline.getPipelineType());
        exportConfig.put("triggerType", pipeline.getTriggerType());
        exportConfig.put("pipelineConfig", pipeline.getPipelineConfig());
        exportConfig.put("exportTime", LocalDateTime.now());
        exportConfig.put("version", "1.0");

        return exportConfig;
    }

    /**
     * 获取流水线模板列表
     *
     * @param pipelineType 流水线类型
     * @return 模板列表
     */
    @Override
    public List<Map<String, Object>> getPipelineTemplates(String pipelineType) {
        List<Map<String, Object>> templates = new ArrayList<>();

        // 构建测试类型模板
        if (pipelineType == null || "TEST".equals(pipelineType)) {
            templates.add(createTemplate("单元测试模板", "TEST",
                "运行单元测试并生成测试报告", createUnitTestTemplate()));
            templates.add(createTemplate("集成测试模板", "TEST",
                "运行集成测试并生成测试报告", createIntegrationTestTemplate()));
            templates.add(createTemplate("API测试模板", "TEST",
                "运行API测试并生成测试报告", createApiTestTemplate()));
        }

        // 构建构建类型模板
        if (pipelineType == null || "BUILD".equals(pipelineType)) {
            templates.add(createTemplate("Maven构建模板", "BUILD",
                "使用Maven构建项目", createMavenBuildTemplate()));
            templates.add(createTemplate("Gradle构建模板", "BUILD",
                "使用Gradle构建项目", createGradleBuildTemplate()));
        }

        return templates;
    }

    /**
     * 清理过期执行记录
     *
     * @param projectId 项目ID
     * @param days 保留天数
     * @return 清理数量
     */
    @Override
    @Transactional
    public int cleanExpiredExecutions(Long projectId, Integer days) {
        LocalDateTime cutoffTime = LocalDateTime.now().minusDays(days);

        LambdaQueryWrapper<PipelineExecution> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(PipelineExecution::getProjectId, projectId)
                   .lt(PipelineExecution::getCreateTime, cutoffTime)
                   .in(PipelineExecution::getStatus, "SUCCESS", "FAILED", "STOPPED");

        List<PipelineExecution> expiredExecutions = pipelineExecutionMapper.selectList(queryWrapper);

        if (!expiredExecutions.isEmpty()) {
            List<Long> executionIds = expiredExecutions.stream()
                .map(PipelineExecution::getExecutionId)
                .collect(Collectors.toList());

            return pipelineExecutionMapper.deleteBatchIds(executionIds);
        }

        return 0;
    }

    /**
     * 异步生成报告
     *
     * @param reportRequest 报告请求
     * @return 报告ID
     */
    @Override
    @Async
    public Long generateReportAsync(Map<String, Object> reportRequest) {
        String reportType = (String) reportRequest.get("reportType");
        Long projectId = Long.valueOf(reportRequest.get("projectId").toString());
        Long generatorId = Long.valueOf(reportRequest.get("generatorId").toString());

        Map<String, Object> reportConfig = (Map<String, Object>) reportRequest.getOrDefault("config", new HashMap<>());

        TestReport report = null;

        switch (reportType) {
            case "EXECUTION":
                Long executionId = Long.valueOf(reportRequest.get("executionId").toString());
                report = testReportService.generateExecutionReport(executionId, reportConfig, generatorId);
                break;
            case "TREND":
                String timeRange = (String) reportRequest.get("timeRange");
                report = testReportService.generateTrendReport(projectId, timeRange, reportConfig, generatorId);
                break;
            case "SUMMARY":
                report = testReportService.generateSummaryReport(projectId, reportConfig, generatorId);
                break;
            default:
                throw new ServiceException("不支持的报告类型: " + reportType);
        }

        return report.getReportId();
    }

    /**
     * 获取报告生成状态
     *
     * @param reportId 报告ID
     * @return 生成状态
     */
    @Override
    public String getReportGenerationStatus(Long reportId) {
        TestReport report = testReportService.selectTestReportByReportId(reportId);
        if (report == null) {
            return "NOT_FOUND";
        }

        return report.getStatus();
    }

    /**
     * 获取报告统计数据
     *
     * @param projectId 项目ID
     * @param timeRange 时间范围
     * @return 统计数据
     */
    @Override
    public Map<String, Object> getReportStatistics(Long projectId, String timeRange) {
        return testReportService.getReportStatistics(projectId, timeRange);
    }

    /**
     * 获取趋势数据
     *
     * @param projectId 项目ID
     * @param days 天数
     * @return 趋势数据
     */
    @Override
    public List<Map<String, Object>> getTrendData(Long projectId, Integer days) {
        return testReportService.getTrendData(projectId, days);
    }

    /**
     * 获取热门失败用例
     *
     * @param projectId 项目ID
     * @param timeRange 时间范围
     * @param limit 限制数量
     * @return 失败用例列表
     */
    @Override
    public List<Map<String, Object>> getTopFailedCases(Long projectId, String timeRange, Integer limit) {
        return testReportService.getTopFailedCases(projectId, timeRange, limit);
    }

    /**
     * 获取性能指标
     *
     * @param projectId 项目ID
     * @param timeRange 时间范围
     * @return 性能指标
     */
    @Override
    public Map<String, Object> getPerformanceMetrics(Long projectId, String timeRange) {
        return testReportService.getPerformanceMetrics(projectId, timeRange);
    }

    /**
     * 导出报告
     *
     * @param reportId 报告ID
     * @param format 导出格式
     * @return 导出文件路径
     */
    @Override
    public String exportReport(Long reportId, String format) {
        return testReportService.exportReport(reportId, format);
    }

    /**
     * 分享报告
     *
     * @param reportId 报告ID
     * @param shareConfig 分享配置
     * @return 分享链接
     */
    @Override
    public String shareReport(Long reportId, Map<String, Object> shareConfig) {
        return testReportService.shareReport(reportId, shareConfig);
    }

    /**
     * 清理过期报告
     *
     * @param projectId 项目ID
     * @param days 保留天数
     * @return 清理数量
     */
    @Override
    @Transactional
    public int cleanExpiredReports(Long projectId, Integer days) {
        return testReportService.cleanExpiredReports(projectId, days);
    }

    /**
     * 获取报告模板列表
     *
     * @param reportType 报告类型
     * @return 模板列表
     */
    @Override
    public List<Map<String, Object>> getReportTemplates(String reportType) {
        return testReportService.getReportTemplates(reportType);
    }

    /**
     * 预览报告
     *
     * @param reportId 报告ID
     * @return 预览内容
     */
    @Override
    public String previewReport(Long reportId) {
        return testReportService.previewReport(reportId);
    }

    // ==================== 私有方法 ====================

    /**
     * 创建流水线执行记录
     */
    private PipelineExecution createPipelineExecution(Pipeline pipeline, Map<String, Object> params, Long triggerUserId) {
        PipelineExecution execution = new PipelineExecution();
        execution.setExecutionCode(generateExecutionCode());
        execution.setPipelineId(pipeline.getPipelineId());
        execution.setProjectId(pipeline.getProjectId());
        execution.setStatus("PENDING");
        execution.setTriggerType("MANUAL");
        execution.setTriggerUserId(triggerUserId);

        try {
            execution.setExecutionParams(objectMapper.writeValueAsString(params));
        } catch (Exception e) {
            log.warn("序列化执行参数失败: {}", e.getMessage());
        }

        execution.setStartTime(LocalDateTime.now());
        execution.setCreateBy(String.valueOf(triggerUserId));

        pipelineExecutionMapper.insert(execution);
        return execution;
    }

    /**
     * 执行流水线步骤
     */
    private void executePipelineSteps(PipelineExecution execution, Pipeline pipeline) {
        String executionKey = execution.getExecutionCode();
        runningExecutions.put(executionKey, new PipelineExecutionContext(execution, pipeline));

        try {
            execution.setStatus("RUNNING");
            pipelineExecutionMapper.updateById(execution);

            // 解析流水线配置
            Pipeline.PipelineConfig config = objectMapper.readValue(
                pipeline.getPipelineConfig(),
                Pipeline.PipelineConfig.class
            );

            List<PipelineExecution.StepExecution> stepExecutions = new ArrayList<>();

            // 执行每个步骤
            for (Pipeline.PipelineStep step : config.getSteps()) {
                PipelineExecution.StepExecution stepExecution = executeStep(execution, step);
                stepExecutions.add(stepExecution);

                // 如果步骤失败且设置为失败时停止，则停止执行
                if ("FAILED".equals(stepExecution.getStatus()) && step.isStopOnFailure()) {
                    break;
                }
            }

            // 保存步骤执行结果
            execution.setStepResults(objectMapper.writeValueAsString(stepExecutions));

            // 计算最终状态
            boolean allSuccess = stepExecutions.stream()
                .allMatch(step -> "SUCCESS".equals(step.getStatus()));

            execution.setStatus(allSuccess ? "SUCCESS" : "FAILED");
            execution.setEndTime(LocalDateTime.now());

            pipelineExecutionMapper.updateById(execution);

        } catch (Exception e) {
            execution.setStatus("FAILED");
            execution.setErrorMessage(e.getMessage());
            execution.setEndTime(LocalDateTime.now());
            pipelineExecutionMapper.updateById(execution);
        } finally {
            runningExecutions.remove(executionKey);
        }
    }

    /**
     * 执行单个步骤
     */
    private PipelineExecution.StepExecution executeStep(PipelineExecution execution, Pipeline.PipelineStep step) {
        PipelineExecution.StepExecution stepExecution = new PipelineExecution.StepExecution();
        stepExecution.setStepName(step.getStepName());
        stepExecution.setStepType(step.getStepType());
        stepExecution.setStatus("RUNNING");
        stepExecution.setStartTime(LocalDateTime.now());

        try {
            switch (step.getStepType()) {
                case "BUILD":
                    executeBuildStep(stepExecution, step);
                    break;
                case "TEST":
                    executeTestStep(stepExecution, step, execution);
                    break;
                case "DEPLOY":
                    executeDeployStep(stepExecution, step);
                    break;
                case "WEBHOOK":
                    executeWebhookStep(stepExecution, step);
                    break;
                case "SCRIPT":
                    executeScriptStep(stepExecution, step);
                    break;
                default:
                    throw new ServiceException("不支持的步骤类型: " + step.getStepType());
            }

            stepExecution.setStatus("SUCCESS");

        } catch (Exception e) {
            stepExecution.setStatus("FAILED");
            stepExecution.setErrorMessage(e.getMessage());
        }

        stepExecution.setEndTime(LocalDateTime.now());
        return stepExecution;
    }

    /**
     * 执行构建步骤
     */
    private void executeBuildStep(PipelineExecution.StepExecution stepExecution, Pipeline.PipelineStep step) {
        Map<String, Object> stepConfig = step.getStepConfig();
        String buildType = (String) stepConfig.get("buildType");

        switch (buildType) {
            case "MAVEN":
                executeMavenBuild(stepExecution, stepConfig);
                break;
            case "GRADLE":
                executeGradleBuild(stepExecution, stepConfig);
                break;
            case "JENKINS":
                executeJenkinsBuild(stepExecution, stepConfig);
                break;
            default:
                throw new ServiceException("不支持的构建类型: " + buildType);
        }
    }

    /**
     * 执行测试步骤
     */
    private void executeTestStep(PipelineExecution.StepExecution stepExecution, Pipeline.PipelineStep step, PipelineExecution execution) {
        Map<String, Object> stepConfig = step.getStepConfig();
        String testType = (String) stepConfig.get("testType");

        // 触发测试执行
        Map<String, Object> testParams = new HashMap<>();
        testParams.put("pipelineExecutionId", execution.getExecutionId());
        testParams.putAll(stepConfig);

        // 这里可以调用测试执行服务
        // testExecutionService.executeTestAsync(testType, testParams);

        stepExecution.setOutput("测试执行已启动");
    }

    /**
     * 执行部署步骤
     */
    private void executeDeployStep(PipelineExecution.StepExecution stepExecution, Pipeline.PipelineStep step) {
        Map<String, Object> stepConfig = step.getStepConfig();
        String deployType = (String) stepConfig.get("deployType");

        // 实现部署逻辑
        stepExecution.setOutput("部署到 " + deployType + " 完成");
    }

    /**
     * 执行Webhook步骤
     */
    private void executeWebhookStep(PipelineExecution.StepExecution stepExecution, Pipeline.PipelineStep step) {
        Map<String, Object> stepConfig = step.getStepConfig();
        String webhookUrl = (String) stepConfig.get("webhookUrl");

        // 发送HTTP请求
        // webHookService.sendWebhook(webhookUrl, stepConfig);

        stepExecution.setOutput("Webhook发送成功: " + webhookUrl);
    }

    /**
     * 执行脚本步骤
     */
    private void executeScriptStep(PipelineExecution.StepExecution stepExecution, Pipeline.PipelineStep step) {
        Map<String, Object> stepConfig = step.getStepConfig();
        String scriptContent = (String) stepConfig.get("scriptContent");

        // 执行脚本
        // scriptService.executeScript(scriptContent);

        stepExecution.setOutput("脚本执行完成");
    }

    /**
     * 执行Maven构建
     */
    private void executeMavenBuild(PipelineExecution.StepExecution stepExecution, Map<String, Object> config) {
        // 实现Maven构建逻辑
        stepExecution.setOutput("Maven构建完成");
    }

    /**
     * 执行Gradle构建
     */
    private void executeGradleBuild(PipelineExecution.StepExecution stepExecution, Map<String, Object> config) {
        // 实现Gradle构建逻辑
        stepExecution.setOutput("Gradle构建完成");
    }

    /**
     * 执行Jenkins构建
     */
    private void executeJenkinsBuild(PipelineExecution.StepExecution stepExecution, Map<String, Object> config) {
        String jobName = (String) config.get("jobName");
        @SuppressWarnings("unchecked")
        Map<String, Object> parameters = (Map<String, Object>) config.getOrDefault("parameters", new HashMap<>());

        // 调用Jenkins服务
        JenkinsBuildResult result = jenkinsIntegrationService.triggerBuild(jobName, parameters);

        stepExecution.setOutput("Jenkins构建已启动: " + result.getBuildUrl());
    }

    /**
     * 验证流水线配置
     */
    private void validatePipelineConfig(Pipeline pipeline) {
        Map<String, Object> validationResult = validatePipelineConfig(pipeline);
        if (!(Boolean) validationResult.get("valid")) {
            @SuppressWarnings("unchecked")
            List<String> errors = (List<String>) validationResult.get("errors");
            throw new ServiceException("流水线配置验证失败: " + String.join(", ", errors));
        }
    }

    /**
     * 生成执行编码
     */
    private String generateExecutionCode() {
        return "EXEC-" + System.currentTimeMillis() + "-" + executionIdGenerator.getAndIncrement();
    }

    /**
     * 计算开始时间
     */
    private LocalDateTime calculateStartTime(LocalDateTime endTime, String timeRange) {
        switch (timeRange) {
            case "7d":
                return endTime.minusDays(7);
            case "30d":
                return endTime.minusDays(30);
            case "90d":
                return endTime.minusDays(90);
            case "1y":
                return endTime.minusYears(1);
            default:
                return endTime.minusDays(30);
        }
    }

    /**
     * 获取流水线模板
     */
    private Pipeline.PipelineConfig getPipelineTemplate(String templateType) {
        switch (templateType) {
            case "单元测试模板":
                return createUnitTestTemplate();
            case "集成测试模板":
                return createIntegrationTestTemplate();
            case "API测试模板":
                return createApiTestTemplate();
            case "Maven构建模板":
                return createMavenBuildTemplate();
            case "Gradle构建模板":
                return createGradleBuildTemplate();
            default:
                return null;
        }
    }

    /**
     * 创建模板信息
     */
    private Map<String, Object> createTemplate(String name, String type, String description, Pipeline.PipelineConfig config) {
        Map<String, Object> template = new HashMap<>();
        template.put("templateName", name);
        template.put("templateType", type);
        template.put("description", description);
        template.put("config", config);
        return template;
    }

    /**
     * 创建单元测试模板
     */
    private Pipeline.PipelineConfig createUnitTestTemplate() {
        Pipeline.PipelineConfig config = new Pipeline.PipelineConfig();
        config.setDescription("运行单元测试并生成测试报告");

        List<Pipeline.PipelineStep> steps = new ArrayList<>();

        // 构建步骤
        Pipeline.PipelineStep buildStep = new Pipeline.PipelineStep();
        buildStep.setStepName("构建项目");
        buildStep.setStepType("BUILD");
        buildStep.setStepConfig(Map.of("buildType", "MAVEN", "goals", "clean compile"));
        steps.add(buildStep);

        // 测试步骤
        Pipeline.PipelineStep testStep = new Pipeline.PipelineStep();
        testStep.setStepName("运行单元测试");
        testStep.setStepType("TEST");
        testStep.setStepConfig(Map.of(
            "testType", "UNIT",
            "testFramework", "JUNIT",
            "includes", "**/*Test.java",
            "generateReport", true
        ));
        steps.add(testStep);

        config.setSteps(steps);
        return config;
    }

    /**
     * 创建集成测试模板
     */
    private Pipeline.PipelineConfig createIntegrationTestTemplate() {
        Pipeline.PipelineConfig config = new Pipeline.PipelineConfig();
        config.setDescription("运行集成测试并生成测试报告");

        List<Pipeline.PipelineStep> steps = new ArrayList<>();

        // 构建步骤
        Pipeline.PipelineStep buildStep = new Pipeline.PipelineStep();
        buildStep.setStepName("构建项目");
        buildStep.setStepType("BUILD");
        buildStep.setStepConfig(Map.of("buildType", "MAVEN", "goals", "clean package"));
        steps.add(buildStep);

        // 部署步骤
        Pipeline.PipelineStep deployStep = new Pipeline.PipelineStep();
        deployStep.setStepName("部署测试环境");
        deployStep.setStepType("DEPLOY");
        deployStep.setStepConfig(Map.of("deployType", "TEST", "envName", "test"));
        steps.add(deployStep);

        // 测试步骤
        Pipeline.PipelineStep testStep = new Pipeline.PipelineStep();
        testStep.setStepName("运行集成测试");
        testStep.setStepType("TEST");
        testStep.setStepConfig(Map.of(
            "testType", "INTEGRATION",
            "testFramework", "TESTNG",
            "includes", "**/*IT.java",
            "generateReport", true
        ));
        steps.add(testStep);

        config.setSteps(steps);
        return config;
    }

    /**
     * 创建API测试模板
     */
    private Pipeline.PipelineConfig createApiTestTemplate() {
        Pipeline.PipelineConfig config = new Pipeline.PipelineConfig();
        config.setDescription("运行API测试并生成测试报告");

        List<Pipeline.PipelineStep> steps = new ArrayList<>();

        // 测试步骤
        Pipeline.PipelineStep testStep = new Pipeline.PipelineStep();
        testStep.setStepName("运行API测试");
        testStep.setStepType("TEST");
        testStep.setStepConfig(Map.of(
            "testType", "API",
            "testFramework", "REST_ASSURED",
            "testSuite", "api-test-suite.json",
            "generateReport", true
        ));
        steps.add(testStep);

        config.setSteps(steps);
        return config;
    }

    /**
     * 创建Maven构建模板
     */
    private Pipeline.PipelineConfig createMavenBuildTemplate() {
        Pipeline.PipelineConfig config = new Pipeline.PipelineConfig();
        config.setDescription("使用Maven构建项目");

        List<Pipeline.PipelineStep> steps = new ArrayList<>();

        Pipeline.PipelineStep buildStep = new Pipeline.PipelineStep();
        buildStep.setStepName("Maven构建");
        buildStep.setStepType("BUILD");
        buildStep.setStepConfig(Map.of(
            "buildType", "MAVEN",
            "goals", "clean package",
            "skipTests", false
        ));
        steps.add(buildStep);

        config.setSteps(steps);
        return config;
    }

    /**
     * 创建Gradle构建模板
     */
    private Pipeline.PipelineConfig createGradleBuildTemplate() {
        Pipeline.PipelineConfig config = new Pipeline.PipelineConfig();
        config.setDescription("使用Gradle构建项目");

        List<Pipeline.PipelineStep> steps = new ArrayList<>();

        Pipeline.PipelineStep buildStep = new Pipeline.PipelineStep();
        buildStep.setStepName("Gradle构建");
        buildStep.setStepType("BUILD");
        buildStep.setStepConfig(Map.of(
            "buildType", "GRADLE",
            "tasks", "clean build",
            "skipTests", false
        ));
        steps.add(buildStep);

        config.setSteps(steps);
        return config;
    }

    /**
     * 流水线执行上下文
     */
    private static class PipelineExecutionContext {
        private final PipelineExecution execution;
        private final Pipeline pipeline;
        private final LocalDateTime startTime;

        public PipelineExecutionContext(PipelineExecution execution, Pipeline pipeline) {
            this.execution = execution;
            this.pipeline = pipeline;
            this.startTime = LocalDateTime.now();
        }

        public PipelineExecution getExecution() {
            return execution;
        }

        public Pipeline getPipeline() {
            return pipeline;
        }

        public LocalDateTime getStartTime() {
            return startTime;
        }
    }

    /**
     * Jenkins构建结果
     */
    private static class JenkinsBuildResult {
        private String buildUrl;
        private String buildNumber;
        private String status;

        // getters and setters
        public String getBuildUrl() { return buildUrl; }
        public void setBuildUrl(String buildUrl) { this.buildUrl = buildUrl; }
        public String getBuildNumber() { return buildNumber; }
        public void setBuildNumber(String buildNumber) { this.buildNumber = buildNumber; }
        public String getStatus() { return status; }
        public void setStatus(String status) { this.status = status; }
    }
}