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
 * 测试执行用例详情对象 test_execution_case
 *
 * @author autotest
 * @date 2024-01-01
 */
@Data
@TableName("test_execution_case")
public class TestExecutionCase extends BaseEntity {
    private static final long serialVersionUID = 1L;

    @TableId(type = IdType.AUTO)
    private Long executionCaseId;

    /** 执行ID */
    private Long executionId;

    /** 用例ID */
    private Long caseId;

    /** 用例编码 */
    @TableField(exist = false)
    private String caseCode;

    /** 用例标题 */
    @TableField(exist = false)
    private String caseTitle;

    /** 用例类型 */
    @TableField(exist = false)
    private String caseType;

    /** 执行状态 (PENDING/RUNNING/SUCCESS/FAILED/SKIPPED/TIMEOUT) */
    private String status;

    /** 优先级 */
    @TableField(exist = false)
    private String priority;

    /** 开始执行时间 */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date startTime;

    /** 结束执行时间 */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date endTime;

    /** 执行时长(毫秒) */
    private Long duration;

    /** 执行结果 */
    private String result;

    /** 错误信息 */
    private String errorMessage;

    /** 执行步骤结果 */
    private String stepResults;

    /** 执行步骤结果列表 */
    @TableField(exist = false)
    private List<StepResult> stepResultList;

    /** 断言结果 */
    private String assertionResults;

    /** 截图路径 */
    private String screenshotPath;

    /** 日志路径 */
    private String logPath;

    /** 重试次数 */
    private Integer retryCount = 0;

    /** 执行机/节点 */
    private String executorNode;

    /** 执行线程ID */
    private String threadId;

    @Data
    public static class StepResult {
        /** 步骤编号 */
        private Integer stepNumber;

        /** 步骤描述 */
        private String description;

        /** 执行状态 */
        private String status;

        /** 执行结果 */
        private String result;

        /** 错误信息 */
        private String errorMessage;

        /** 开始时间 */
        private Long startTime;

        /** 结束时间 */
        private Long endTime;

        /** 执行时长 */
        private Long duration;

        /** 截图路径 */
        private String screenshotPath;

        /** 断言结果 */
        private List<AssertionResult> assertions;
    }

    @Data
    public static class AssertionResult {
        /** 断言描述 */
        private String description;

        /** 期望值 */
        private Object expectedValue;

        /** 实际值 */
        private Object actualValue;

        /** 断言状态 */
        private String status; // PASSED/FAILED

        /** 错误信息 */
        private String errorMessage;
    }
}