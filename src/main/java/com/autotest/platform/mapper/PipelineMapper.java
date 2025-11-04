package com.autotest.platform.mapper;

import com.autotest.platform.domain.cicd.Pipeline;
import com.autotest.platform.common.core.mapper.BaseMapperPlus;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * 流水线Mapper接口
 *
 * @author autotest
 * @date 2024-01-01
 */
@Mapper
public interface PipelineMapper extends BaseMapperPlus<PipelineMapper, Pipeline> {

    /**
     * 查询流水线统计信息
     *
     * @param projectId 项目ID
     * @param startTime 开始时间
     * @return 统计信息
     */
    Map<String, Object> selectPipelineStatistics(@Param("projectId") Long projectId,
                                               @Param("startTime") LocalDateTime startTime);

    /**
     * 查询项目下流水线列表
     *
     * @param projectId 项目ID
     * @param status 状态
     * @return 流水线列表
     */
    List<Pipeline> selectPipelinesByProject(@Param("projectId") Long projectId,
                                          @Param("status") String status);

    /**
     * 查询指定类型的流水线
     *
     * @param pipelineType 流水线类型
     * @param projectId 项目ID
     * @return 流水线列表
     */
    List<Pipeline> selectPipelinesByType(@Param("pipelineType") String pipelineType,
                                       @Param("projectId") Long projectId);

    /**
     * 查询可触发的流水线
     *
     * @param projectId 项目ID
     * @return 流水线列表
     */
    List<Pipeline> selectTriggerablePipelines(@Param("projectId") Long projectId);

    /**
     * 更新流水线状态
     *
     * @param pipelineId 流水线ID
     * @param status 状态
     * @return 影响行数
     */
    int updatePipelineStatus(@Param("pipelineId") Long pipelineId,
                           @Param("status") String status);

    /**
     * 批量更新流水线状态
     *
     * @param pipelineIds 流水线ID列表
     * @param status 状态
     * @return 影响行数
     */
    int updatePipelineStatusBatch(@Param("pipelineIds") List<Long> pipelineIds,
                                @Param("status") String status);

    /**
     * 查询流水线执行次数
     *
     * @param pipelineId 流水线ID
     * @return 执行次数
     */
    Long selectExecutionCount(@Param("pipelineId") Long pipelineId);

    /**
     * 查询流水线成功率
     *
     * @param pipelineId 流水线ID
     * @param days 统计天数
     * @return 成功率
     */
    Double selectSuccessRate(@Param("pipelineId") Long pipelineId,
                           @Param("days") Integer days);

    /**
     * 查询最近执行的流水线
     *
     * @param projectId 项目ID
     * @param limit 限制数量
     * @return 流水线列表
     */
    List<Pipeline> selectRecentlyExecutedPipelines(@Param("projectId") Long projectId,
                                                 @Param("limit") Integer limit);

    /**
     * 查询流水线模板
     *
     * @param templateType 模板类型
     * @return 模板配置
     */
    String selectPipelineTemplate(@Param("templateType") String templateType);

    /**
     * 插入流水线模板
     *
     * @param templateType 模板类型
     * @param templateConfig 模板配置
     * @return 影响行数
     */
    int insertPipelineTemplate(@Param("templateType") String templateType,
                              @Param("templateConfig") String templateConfig);

    /**
     * 更新流水线模板
     *
     * @param templateType 模板类型
     * @param templateConfig 模板配置
     * @return 影响行数
     */
    int updatePipelineTemplate(@Param("templateType") String templateType,
                              @Param("templateConfig") String templateConfig);

    /**
     * 删除流水线模板
     *
     * @param templateType 模板类型
     * @return 影响行数
     */
    int deletePipelineTemplate(@Param("templateType") String templateType);

    /**
     * 查询所有流水线模板
     *
     * @return 模板列表
     */
    List<Map<String, Object>> selectAllPipelineTemplates();

    /**
     * 检查流水线名称是否存在
     *
     * @param pipelineName 流水线名称
     * @param projectId 项目ID
     * @param excludeId 排除的ID
     * @return 存在数量
     */
    Long checkPipelineNameExists(@Param("pipelineName") String pipelineName,
                               @Param("projectId") Long projectId,
                               @Param("excludeId") Long excludeId);

    /**
     * 查询流水线使用统计
     *
     * @param projectId 项目ID
     * @param timeRange 时间范围
     * @return 使用统计
     */
    List<Map<String, Object>> selectPipelineUsageStats(@Param("projectId") Long projectId,
                                                     @Param("timeRange") String timeRange);

    /**
     * 查询流水线类型分布
     *
     * @param projectId 项目ID
     * @return 类型分布
     */
    List<Map<String, Object>> selectPipelineTypeDistribution(@Param("projectId") Long projectId);

    /**
     * 查询流水线执行趋势
     *
     * @param projectId 项目ID
     * @param days 天数
     * @return 执行趋势
     */
    List<Map<String, Object>> selectPipelineExecutionTrend(@Param("projectId") Long projectId,
                                                          @Param("days") Integer days);

    /**
     * 查询热门流水线
     *
     * @param projectId 项目ID
     * @param limit 限制数量
     * @return 热门流水线
     */
    List<Pipeline> selectPopularPipelines(@Param("projectId") Long projectId,
                                       @Param("limit") Integer limit);

    /**
     * 查询失败率高的流水线
     *
     * @param projectId 项目ID
     * @param days 天数
     * @param limit 限制数量
     * @return 流水线列表
     */
    List<Map<String, Object>> selectHighFailureRatePipelines(@Param("projectId") Long projectId,
                                                           @Param("days") Integer days,
                                                           @Param("limit") Integer limit);

    /**
     * 查询长时间未执行的流水线
     *
     * @param projectId 项目ID
     * @param days 天数
     * @return 流水线列表
     */
    List<Pipeline> selectLongTimeNoExecutedPipelines(@Param("projectId") Long projectId,
                                                   @Param("days") Integer days);

    /**
     * 更新流水线最后执行时间
     *
     * @param pipelineId 流水线ID
     * @param lastExecutionTime 最后执行时间
     * @return 影响行数
     */
    int updateLastExecutionTime(@Param("pipelineId") Long pipelineId,
                               @Param("lastExecutionTime") LocalDateTime lastExecutionTime);

    /**
     * 查询流水线依赖关系
     *
     * @param pipelineId 流水线ID
     * @return 依赖的流水线列表
     */
    List<Pipeline> selectPipelineDependencies(@Param("pipelineId") Long pipelineId);

    /**
     * 查询依赖指定流水线的流水线列表
     *
     * @param pipelineId 流水线ID
     * @return 依赖此流水线的流水线列表
     */
    List<Pipeline> selectDependentPipelines(@Param("pipelineId") Long pipelineId);

    /**
     * 清理无效的流水线依赖
     *
     * @param projectId 项目ID
     * @return 清理数量
     */
    int cleanInvalidDependencies(@Param("projectId") Long projectId);
}