package com.autotest.platform.service;

import com.autotest.platform.domain.cicd.Pipeline;
import com.autotest.platform.domain.cicd.PipelineExecution;
import com.baomidou.mybatisplus.extension.service.IService;

import java.util.List;
import java.util.Map;

/**
 * 流水线Service接口
 *
 * @author autotest
 * @date 2024-01-01
 */
public interface IPipelineService extends IService<Pipeline> {

    /**
     * 查询流水线
     *
     * @param pipelineId 流水线主键
     * @return 流水线
     */
    Pipeline selectPipelineByPipelineId(Long pipelineId);

    /**
     * 查询流水线列表
     *
     * @param pipeline 流水线
     * @return 流水线集合
     */
    List<Pipeline> selectPipelineList(Pipeline pipeline);

    /**
     * 新增流水线
     *
     * @param pipeline 流水线
     * @return 结果
     */
    int insertPipeline(Pipeline pipeline);

    /**
     * 修改流水线
     *
     * @param pipeline 流水线
     * @return 结果
     */
    int updatePipeline(Pipeline pipeline);

    /**
     * 批量删除流水线
     *
     * @param pipelineIds 需要删除的流水线主键集合
     * @return 结果
     */
    int deletePipelineByPipelineIds(Long[] pipelineIds);

    /**
     * 删除流水线信息
     *
     * @param pipelineId 流水线主键
     * @return 结果
     */
    int deletePipelineByPipelineId(Long pipelineId);

    /**
     * 执行流水线
     *
     * @param pipelineId 流水线ID
     * @param params 执行参数
     * @param triggerUserId 触发用户ID
     * @return 执行记录
     */
    PipelineExecution executePipeline(Long pipelineId, Map<String, Object> params, Long triggerUserId);

    /**
     * 停止流水线执行
     *
     * @param executionId 执行ID
     * @return 结果
     */
    boolean stopPipelineExecution(Long executionId);

    /**
     * 重新执行流水线
     *
     * @param executionId 原执行ID
     * @return 新执行记录
     */
    PipelineExecution retryExecution(Long executionId);

    /**
     * 获取流水线执行记录
     *
     * @param pipelineId 流水线ID
     * @return 执行记录列表
     */
    List<PipelineExecution> getPipelineExecutions(Long pipelineId);

    /**
     * 获取流水线执行详情
     *
     * @param executionId 执行ID
     * @return 执行详情
     */
    PipelineExecution getExecutionDetail(Long executionId);

    /**
     * 验证流水线配置
     *
     * @param pipeline 流水线信息
     * @return 验证结果
     */
    Map<String, Object> validatePipelineConfig(Pipeline pipeline);

    /**
     * 复制流水线
     *
     * @param pipelineId 原流水线ID
     * @param newName 新流水线名称
     * @param creatorId 创建人ID
     * @return 新流水线
     */
    Pipeline copyPipeline(Long pipelineId, String newName, Long creatorId);

    /**
     * 启用流水线
     *
     * @param pipelineIds 流水线ID列表
     * @return 结果
     */
    int enablePipelines(Long[] pipelineIds);

    /**
     * 禁用流水线
     *
     * @param pipelineIds 流水线ID列表
     * @return 结果
     */
    int disablePipelines(Long[] pipelineIds);

    /**
     * 获取流水线统计
     *
     * @param projectId 项目ID
     * @param timeRange 时间范围
     * @return 统计信息
     */
    Map<String, Object> getPipelineStatistics(Long projectId, String timeRange);

    /**
     * 手动触发流水线
     *
     * @param pipelineId 流水线ID
     * @param triggerParams 触发参数
     * @param triggerUserId 触发用户ID
     * @return 执行结果
     */
    Map<String, Object> manualTrigger(Long pipelineId, Map<String, Object> triggerParams, Long triggerUserId);

    /**
     * 创建流水线模板
     *
     * @param templateConfig 模板配置
     * @param creatorId 创建人ID
     * @return 流水线
     */
    Pipeline createFromTemplate(Map<String, Object> templateConfig, Long creatorId);

    /**
     * 导入流水线配置
     *
     * @param importConfig 导入配置
     * @param projectId 项目ID
     * @param creatorId 创建人ID
     * @return 导入结果
     */
    Map<String, Object> importPipelineConfig(Map<String, Object> importConfig, Long projectId, Long creatorId);

    /**
     * 导出流水线配置
     *
     * @param pipelineId 流水线ID
     * @return 导出配置
     */
    Map<String, Object> exportPipelineConfig(Long pipelineId);

    /**
     * 获取流水线模板列表
     *
     * @param pipelineType 流水线类型
     * @return 模板列表
     */
    List<Map<String, Object>> getPipelineTemplates(String pipelineType);

    /**
     * 清理过期执行记录
     *
     * @param projectId 项目ID
     * @param days 保留天数
     * @return 清理数量
     */
    int cleanExpiredExecutions(Long projectId, Integer days);
}