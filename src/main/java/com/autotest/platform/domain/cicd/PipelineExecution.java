package com.autotest.platform.domain.cicd;

import com.autotest.platform.domain.BaseEntity;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.util.Date;
import java.util.List;
import java.util.Map;

/**
 * 流水线执行记录对象 pipeline_execution
 *
 * @author autotest
 * @date 2024-01-01
 */
@Data
@TableName("pipeline_execution")
public class PipelineExecution extends BaseEntity {
    private static final long serialVersionUID = 1L;

    @TableId(type = IdType.AUTO)
    private Long executionId;

    /** 执行编号 */
    private String executionCode;

    /** 流水线ID */
    private Long pipelineId;

    /** 项目ID */
    private Long projectId;

    /** 执行状态 (PENDING/RUNNING/SUCCESS/FAILED/CANCELLED/TIMEOUT) */
    private String status;

    /** 触发方式 */
    private String triggerType;

    /** 触发用户ID */
    private Long triggerUserId;

    /** 触发用户名 */
    @TableField(exist = false)
    private String triggerUserName;

    /** Git提交信息 */
    private String commitInfo;

    /** Git分支 */
    private String branch;

    /** Git标签 */
    private String tag;

    /** 开始执行时间 */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date startTime;

    /** 结束执行时间 */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date endTime;

    /** 执行时长(毫秒) */
    private Long duration;

    /** 执行环境 */
    private String environment;

    /** 执行参数(JSON格式) */
    private String executionParams;

    /** 执行日志路径 */
    private String logPath;

    /** 执行报告路径 */
    private String reportPath;

    /** 错误信息 */
    private String errorMessage;

    /** 执行结果(JSON格式) */
    private String executionResult;

    /** 执行步骤结果(JSON格式) */
    private String stepResults;

    /** 资源使用情况(JSON格式) */
    private String resourceUsage;

    /** 构建号 */
    private String buildNumber;

    /** 版本号 */
    private String version;

    /** 部署目标 */
    private String deployTarget;

    /** 回滚信息 */
    private String rollbackInfo;

    /** 流水线名称 */
    @TableField(exist = false)
    private String pipelineName;

    /** 项目名称 */
    @TableField(exist = false)
    private String projectName;

    /** 执行步骤列表 */
    @TableField(exist = false)
    private List<StepExecution> stepExecutions;

    /**
     * 步骤执行记录
     */
    @Data
    public static class StepExecution {
        /** 步骤执行ID */
        private Long stepExecutionId;

        /** 步骤ID */
        private Long stepId;

        /** 步骤名称 */
        private String stepName;

        /** 步骤类型 */
        private String stepType;

        /** 执行状态 */
        private String status;

        /** 开始时间 */
        private Date startTime;

        /** 结束时间 */
        private Date endTime;

        /** 执行时长(毫秒) */
        private Long duration;

        /** 执行命令 */
        private String command;

        /** 执行参数 */
        private Map<String, Object> parameters;

        /** 执行日志 */
        private String logOutput;

        /** 错误信息 */
        private String errorMessage;

        /** 退出码 */
        private Integer exitCode;

        /** 重试次数 */
        private Integer retryCount;

        /** 资源使用 */
        private Map<String, Object> resourceUsage;

        /** 产物信息 */
        private List<Artifact> artifacts;
    }

    /**
     * 构建产物
     */
    @Data
    public static class Artifact {
        /** 产物名称 */
        private String name;

        /** 产物路径 */
        private String path;

        /** 产物类型 */
        private String type;

        /** 文件大小 */
        private Long size;

        /** 校验和 */
        private String checksum;

        /** 下载URL */
        private String downloadUrl;

        /** 创建时间 */
        private Date createdTime;
    }

    /**
     * 资源使用情况
     */
    @Data
    public static class ResourceUsage {
        /** CPU使用率 */
        private Double cpuUsage;

        /** 内存使用量(MB) */
        private Long memoryUsage;

        /** 磁盘使用量(MB) */
        private Long diskUsage;

        /** 网络流量(KB) */
        private Long networkUsage;

        /** 执行节点 */
        private String executorNode;

        /** 并发数 */
        private Integer concurrency;
    }
}