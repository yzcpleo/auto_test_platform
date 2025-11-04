package com.autotest.platform.domain.testcase;

import com.autotest.platform.domain.BaseEntity;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.util.Date;
import java.util.List;

/**
 * 测试执行对象 test_execution
 *
 * @author autotest
 * @date 2024-01-01
 */
@Data
@TableName("test_execution")
public class TestExecution extends BaseEntity {
    private static final long serialVersionUID = 1L;

    @TableId(type = IdType.AUTO)
    private Long executionId;

    /** 执行编号 */
    private String executionCode;

    /** 项目ID */
    private Long projectId;

    /** 执行名称 */
    private String executionName;

    /** 执行类型 (SCHEDULE/ MANUAL/BATCH) */
    private String executionType;

    /** 执行状态 (PENDING/RUNNING/SUCCESS/FAILED/CANCELLED/TIMEOUT) */
    private String status;

    /** 优先级 */
    private String priority;

    /** 计划开始时间 */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date plannedStartTime;

    /** 实际开始时间 */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date actualStartTime;

    /** 计划结束时间 */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date plannedEndTime;

    /** 实际结束时间 */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date actualEndTime;

    /** 执行环境ID */
    private Long environmentId;

    /** 执行配置 */
    private String executionConfig;

    /** 总用例数 */
    private Integer totalCases;

    /** 成功用例数 */
    private Integer successCases;

    /** 失败用例数 */
    private Integer failedCases;

    /** 跳过用例数 */
    private Integer skippedCases;

    /** 执行进度 (0-100) */
    private Integer progress;

    /** 错误信息 */
    private String errorMessage;

    /** 执行日志路径 */
    private String logPath;

    /** 报告文件路径 */
    private String reportPath;

    /** 执行人ID */
    private Long executorId;

    /** 执行人姓名 */
    @TableField(exist = false)
    private String executorName;

    /** 项目名称 */
    @TableField(exist = false)
    private String projectName;

    /** 环境名称 */
    @TableField(exist = false)
    private String environmentName;

    /** 执行用例列表 */
    @TableField(exist = false)
    private List<TestExecutionCase> executionCases;

    /** 执行配置对象 */
    @TableField(exist = false)
    private ExecutionConfig configObject;

    @Data
    public static class ExecutionConfig {
        /** 并行执行数量 */
        private Integer parallelCount = 1;

        /** 超时时间(分钟) */
        private Integer timeoutMinutes = 60;

        /** 失败后是否继续 */
        private Boolean continueOnFailure = true;

        /** 是否生成详细报告 */
        private Boolean generateDetailedReport = true;

        /** 重试次数 */
        private Integer retryCount = 0;

        /** 测试数据覆盖 */
        private Boolean useTestData = false;

        /** 邮件通知配置 */
        private EmailNotification emailNotification;

        @Data
        public static class EmailNotification {
            private Boolean enabled = false;
            private List<String> recipients;
            private String template;
        }
    }
}