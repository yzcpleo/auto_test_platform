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
 * CI/CD流水线对象 pipeline
 *
 * @author autotest
 * @date 2024-01-01
 */
@Data
@TableName("pipeline")
public class Pipeline extends BaseEntity {
    private static final long serialVersionUID = 1L;

    @TableId(type = IdType.AUTO)
    private Long pipelineId;

    /** 流水线名称 */
    private String pipelineName;

    /** 流水线编码 */
    private String pipelineCode;

    /** 项目ID */
    private Long projectId;

    /** 流水线类型 (BUILD/DEPLOY/TEST/CUSTOM) */
    private String pipelineType;

    /** 流水线状态 (ACTIVE/INACTIVE/DRAFT) */
    private String status;

    /** 触发方式 (MANUAL/AUTO/SCHEDULE) */
    private String triggerType;

    /** 触发配置 */
    private String triggerConfig;

    /** 流水线配置(JSON格式) */
    private String pipelineConfig;

    /** 流水线步骤(JSON格式) */
    private String pipelineSteps;

    /** 环境变量(JSON格式) */
    private String environmentVariables;

    /** 通知配置(JSON格式) */
    private String notificationConfig;

    /** 最后执行时间 */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date lastExecuteTime;

    /** 最后执行状态 */
    private String lastExecuteStatus;

    /** 执行次数 */
    private Integer executeCount;

    /** 成功次数 */
    private Integer successCount;

    /** 失败次数 */
    private Integer failureCount;

    /** 平均执行时间(秒) */
    private Double avgExecuteTime;

    /** 是否启用 */
    private Boolean enabled;

    /** 创建人ID */
    private Long creatorId;

    /** 项目名称 */
    @TableField(exist = false)
    private String projectName;

    /** 创建人姓名 */
    @TableField(exist = false)
    private String creatorName;

    /** 流水线步骤列表 */
    @TableField(exist = false)
    private List<PipelineStep> stepList;

    /** 流水线配置对象 */
    @TableField(exist = false)
    private PipelineConfig configObject;

    /**
     * 流水线配置
     */
    @Data
    public static class PipelineConfig {
        /** 并发执行 */
        private Boolean parallel = false;

        /** 失败时是否停止 */
        private Boolean stopOnFailure = true;

        /** 超时时间(分钟) */
        private Integer timeoutMinutes = 60;

        /** 重试次数 */
        private Integer retryCount = 0;

        /** 工作目录 */
        private String workingDirectory;

        /** 构建工具 (MAVEN/GRADLE/NPM/CUSTOM) */
        private String buildTool;

        /** 构建命令 */
        private String buildCommand;

        /** 部署环境 */
        private String deployEnvironment;

        /** 部署目标 */
        private String deployTarget;

        /** 测试类型 */
        private List<String> testTypes;

        /** 清理策略 */
        private String cleanupPolicy;

        /** 保留天数 */
        private Integer retentionDays = 30;
    }

    /**
     * 流水线步骤
     */
    @Data
    public static class PipelineStep {
        /** 步骤ID */
        private Long stepId;

        /** 步骤名称 */
        private String stepName;

        /** 步骤类型 (BUILD/TEST/DEPLOY/CUSTOM) */
        private String stepType;

        /** 步骤顺序 */
        private Integer stepOrder;

        /** 步骤命令 */
        private String command;

        /** 步骤参数 */
        private Map<String, Object> parameters;

        /** 超时时间(秒) */
        private Integer timeoutSeconds;

        /** 重试次数 */
        private Integer retryCount;

        /** 失败时是否继续 */
        private Boolean continueOnFailure;

        /** 步骤条件 */
        private String condition;

        /** 依赖步骤 */
        private List<Long> dependencies;

        /** 步骤状态 */
        private String status;

        /** 开始时间 */
        private Date startTime;

        /** 结束时间 */
        private Date endTime;

        /** 执行时长(毫秒) */
        private Long duration;

        /** 执行日志 */
        private String logOutput;

        /** 错误信息 */
        private String errorMessage;
    }

    /**
     * 触发配置
     */
    @Data
    public static class TriggerConfig {
        /** Git Webhook配置 */
        private GitWebhookConfig gitWebhook;

        /** 定时触发配置 */
        private ScheduleConfig schedule;

        /** 手动触发配置 */
        private ManualConfig manual;

        /** API触发配置 */
        private ApiConfig api;

        @Data
        public static class GitWebhookConfig {
            private String repositoryUrl;
            private String branch;
            private List<String> events;
            private String secretToken;
            private Boolean triggerOnPush = true;
            private Boolean triggerOnMerge = false;
        }

        @Data
        public static class ScheduleConfig {
            private String cronExpression;
            private String timezone = "UTC";
            private Boolean enabled = true;
        }

        @Data
        public static class ManualConfig {
            private Boolean requireApproval = false;
            private List<Long> approvers;
        }

        @Data
        public static class ApiConfig {
            private String endpoint;
            private String method = "POST";
            private Map<String, String> headers;
            private Boolean authentication = true;
        }
    }
}