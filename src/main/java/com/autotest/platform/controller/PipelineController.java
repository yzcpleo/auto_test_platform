package com.autotest.platform.controller;

import com.autotest.platform.common.core.controller.BaseController;
import com.autotest.platform.common.core.domain.AjaxResult;
import com.autotest.platform.common.core.page.TableDataInfo;
import com.autotest.platform.domain.cicd.Pipeline;
import com.autotest.platform.domain.cicd.PipelineExecution;
import com.autotest.platform.service.IPipelineService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * 流水线Controller
 *
 * @author autotest
 * @date 2024-01-01
 */
@RestController
@RequestMapping("/cicd/pipeline")
public class PipelineController extends BaseController {

    @Autowired
    private IPipelineService pipelineService;

    /**
     * 查询流水线列表
     */
    @PreAuthorize("@ss.hasPermi('cicd:pipeline:list')")
    @GetMapping("/list")
    public TableDataInfo list(Pipeline pipeline) {
        startPage();
        List<Pipeline> list = pipelineService.selectPipelineList(pipeline);
        return getDataTable(list);
    }

    /**
     * 获取流水线详细信息
     */
    @PreAuthorize("@ss.hasPermi('cicd:pipeline:query')")
    @GetMapping(value = "/{pipelineId}")
    public AjaxResult getInfo(@PathVariable("pipelineId") Long pipelineId) {
        return success(pipelineService.selectPipelineByPipelineId(pipelineId));
    }

    /**
     * 新增流水线
     */
    @PreAuthorize("@ss.hasPermi('cicd:pipeline:add')")
    @PostMapping
    public AjaxResult add(@RequestBody Pipeline pipeline) {
        pipeline.setCreateBy(getUsername());
        return toAjax(pipelineService.insertPipeline(pipeline));
    }

    /**
     * 修改流水线
     */
    @PreAuthorize("@ss.hasPermi('cicd:pipeline:edit')")
    @PutMapping
    public AjaxResult edit(@RequestBody Pipeline pipeline) {
        pipeline.setUpdateBy(getUsername());
        return toAjax(pipelineService.updatePipeline(pipeline));
    }

    /**
     * 删除流水线
     */
    @PreAuthorize("@ss.hasPermi('cicd:pipeline:remove')")
    @DeleteMapping("/{pipelineIds}")
    public AjaxResult remove(@PathVariable Long[] pipelineIds) {
        return toAjax(pipelineService.deletePipelineByPipelineIds(pipelineIds));
    }

    /**
     * 执行流水线
     */
    @PreAuthorize("@ss.hasPermi('cicd:pipeline:execute')")
    @PostMapping("/{pipelineId}/execute")
    public AjaxResult executePipeline(@PathVariable Long pipelineId, @RequestBody Map<String, Object> params) {
        try {
            Map<String, Object> executeParams = params != null ? params : new java.util.HashMap<>();
            Long triggerUserId = getUserId();

            PipelineExecution execution = pipelineService.executePipeline(pipelineId, executeParams, triggerUserId);
            return success("流水线执行已启动", execution);
        } catch (Exception e) {
            return error("执行流水线失败: " + e.getMessage());
        }
    }

    /**
     * 停止流水线执行
     */
    @PreAuthorize("@ss.hasPermi('cicd:pipeline:stop')")
    @PostMapping("/execution/{executionId}/stop")
    public AjaxResult stopExecution(@PathVariable Long executionId) {
        try {
            boolean success = pipelineService.stopPipelineExecution(executionId);
            if (success) {
                return success("流水线执行已停止");
            } else {
                return error("停止流水线执行失败");
            }
        } catch (Exception e) {
            return error("停止流水线执行失败: " + e.getMessage());
        }
    }

    /**
     * 重新执行流水线
     */
    @PreAuthorize("@ss.hasPermi('cicd:pipeline:retry')")
    @PostMapping("/execution/{executionId}/retry")
    public AjaxResult retryExecution(@PathVariable Long executionId) {
        try {
            PipelineExecution newExecution = pipelineService.retryExecution(executionId);
            return success("流水线重新执行已启动", newExecution);
        } catch (Exception e) {
            return error("重新执行流水线失败: " + e.getMessage());
        }
    }

    /**
     * 获取流水线执行记录
     */
    @PreAuthorize("@ss.hasPermi('cicd:pipeline:history')")
    @GetMapping("/{pipelineId}/executions")
    public AjaxResult getExecutions(@PathVariable Long pipelineId) {
        List<PipelineExecution> executions = pipelineService.getPipelineExecutions(pipelineId);
        return success(executions);
    }

    /**
     * 获取执行详情
     */
    @PreAuthorize("@ss.hasPermi('cicd:pipeline:detail')")
    @GetMapping("/execution/{executionId}")
    public AjaxResult getExecutionDetail(@PathVariable Long executionId) {
        PipelineExecution execution = pipelineService.getExecutionDetail(executionId);
        return success(execution);
    }

    /**
     * 手动触发流水线
     */
    @PreAuthorize("@ss.hasPermi('cicd:pipeline:trigger')")
    @PostMapping("/{pipelineId}/trigger")
    public AjaxResult manualTrigger(@PathVariable Long pipelineId, @RequestBody Map<String, Object> triggerParams) {
        try {
            Map<String, Object> result = pipelineService.manualTrigger(pipelineId, triggerParams, getUserId());
            return success("手动触发成功", result);
        } catch (Exception e) {
            return error("手动触发失败: " + e.getMessage());
        }
    }

    /**
     * 验证流水线配置
     */
    @PreAuthorize("@ss.hasPermi('cicd:pipeline:validate')")
    @PostMapping("/validate")
    public AjaxResult validatePipeline(@RequestBody Pipeline pipeline) {
        Map<String, Object> result = pipelineService.validatePipelineConfig(pipeline);
        return success(result);
    }

    /**
     * 复制流水线
     */
    @PreAuthorize("@ss.hasPermi('cicd:pipeline:copy')")
    @PostMapping("/{pipelineId}/copy")
    public AjaxResult copyPipeline(@PathVariable Long pipelineId, @RequestBody Map<String, Object> params) {
        try {
            String newName = (String) params.get("newName");
            Long creatorId = getUserId();

            Pipeline newPipeline = pipelineService.copyPipeline(pipelineId, newName, creatorId);
            return success("流水线复制成功", newPipeline);
        } catch (Exception e) {
            return error("复制流水线失败: " + e.getMessage());
        }
    }

    /**
     * 启用流水线
     */
    @PreAuthorize("@ss.hasPermi('cicd:pipeline:enable')")
    @PostMapping("/enable")
    public AjaxResult enablePipelines(@RequestBody Long[] pipelineIds) {
        return toAjax(pipelineService.enablePipelines(pipelineIds));
    }

    /**
     * 禁用流水线
     */
    @PreAuthorize("@ss.hasPermi('cicd:pipeline:disable')")
    @PostMapping("/disable")
    public AjaxResult disablePipelines(@RequestBody Long[] pipelineIds) {
        return toAjax(pipelineService.disablePipelines(pipelineIds));
    }

    /**
     * 获取流水线统计
     */
    @PreAuthorize("@ss.hasPermi('cicd:pipeline:statistics')")
    @GetMapping("/statistics/{projectId}")
    public AjaxResult getStatistics(@PathVariable Long projectId,
                                   @RequestParam(required = false) String timeRange) {
        Map<String, Object> statistics = pipelineService.getPipelineStatistics(projectId, timeRange);
        return success(statistics);
    }

    /**
     * 创建流水线模板
     */
    @PreAuthorize("@ss.hasPermi('cicd:pipeline:template')")
    @PostMapping("/create-from-template")
    public AjaxResult createFromTemplate(@RequestBody Map<String, Object> templateConfig) {
        try {
            Long creatorId = getUserId();
            Pipeline pipeline = pipelineService.createFromTemplate(templateConfig, creatorId);
            return success("从模板创建流水线成功", pipeline);
        } catch (Exception e) {
            return error("从模板创建流水线失败: " + e.getMessage());
        }
    }

    /**
     * 导入流水线配置
     */
    @PreAuthorize("@ss.hasPermi('cicd:pipeline:import')")
    @PostMapping("/import")
    public AjaxResult importPipelineConfig(@RequestBody Map<String, Object> importConfig) {
        try {
            Long projectId = Long.valueOf(importConfig.get("projectId").toString());
            Long creatorId = getUserId();

            Map<String, Object> result = pipelineService.importPipelineConfig(importConfig, projectId, creatorId);
            return success("导入流水线配置成功", result);
        } catch (Exception e) {
            return error("导入流水线配置失败: " + e.getMessage());
        }
    }

    /**
     * 导出流水线配置
     */
    @PreAuthorize("@ss.hasPermi('cicd:pipeline:export')")
    @GetMapping("/{pipelineId}/export")
    public AjaxResult exportPipelineConfig(@PathVariable Long pipelineId) {
        try {
            Map<String, Object> exportConfig = pipelineService.exportPipelineConfig(pipelineId);
            return success("导出流水线配置成功", exportConfig);
        } catch (Exception e) {
            return error("导出流水线配置失败: " + e.getMessage());
        }
    }

    /**
     * 获取流水线模板列表
     */
    @PreAuthorize("@ss.hasPermi('cicd:pipeline:template')")
    @GetMapping("/templates")
    public AjaxResult getPipelineTemplates(@RequestParam(required = false) String pipelineType) {
        List<Map<String, Object>> templates = pipelineService.getPipelineTemplates(pipelineType);
        return success(templates);
    }

    /**
     * 清理过期执行记录
     */
    @PreAuthorize("@ss.hasPermi('cicd:pipeline:cleanup')")
    @PostMapping("/cleanup/{projectId}")
    public AjaxResult cleanExpiredExecutions(@PathVariable Long projectId,
                                              @RequestParam(defaultValue = "30") Integer days) {
        int cleanedCount = pipelineService.cleanExpiredExecutions(projectId, days);
        return success("清理完成", Map.of("cleanedCount", cleanedCount));
    }

    /**
     * 批量执行流水线
     */
    @PreAuthorize("@ss.hasPermi('cicd:pipeline:batch')")
    @PostMapping("/batch-execute")
    public AjaxResult batchExecute(@RequestBody Map<String, Object> params) {
        try {
            @SuppressWarnings("unchecked")
            List<Long> pipelineIds = (List<Long>) params.get("pipelineIds");
            @SuppressWarnings("unchecked")
            Map<String, Object> executeParams = (Map<String, Object>) params.getOrDefault("params", new java.util.HashMap<>());
            Long triggerUserId = getUserId();

            List<Map<String, Object>> results = new java.util.ArrayList<>();

            for (Long pipelineId : pipelineIds) {
                try {
                    PipelineExecution execution = pipelineService.executePipeline(pipelineId, executeParams, triggerUserId);
                    results.add(Map.of(
                        "pipelineId", pipelineId,
                        "executionId", execution.getExecutionId(),
                        "status", "SUCCESS",
                        "message", "执行成功"
                    ));
                } catch (Exception e) {
                    results.add(Map.of(
                        "pipelineId", pipelineId,
                        "status", "FAILED",
                        "message", e.getMessage()
                    ));
                }
            }

            return success("批量执行完成", Map.of("results", results));

        } catch (Exception e) {
            return error("批量执行失败: " + e.getMessage());
        }
    }
}