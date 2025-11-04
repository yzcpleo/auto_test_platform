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
 * 测试调度对象 test_schedule
 *
 * @author autotest
 * @date 2024-01-01
 */
@Data
@TableName("test_schedule")
public class TestSchedule extends BaseEntity {
    private static final long serialVersionUID = 1L;

    @TableId(type = IdType.AUTO)
    private Long scheduleId;

    /** 调度名称 */
    private String scheduleName;

    /** 项目ID */
    private Long projectId;

    /** 调度类型 (CRON/FIXED_RATE/FIXED_DELAY) */
    private String scheduleType;

    /** 调度表达式 */
    private String cronExpression;

    /** 固定间隔(毫秒) */
    private Long fixedRate;

    /** 固定延迟(毫秒) */
    private Long fixedDelay;

    /** 调度状态 (ENABLED/DISabled/PAUSED) */
    private String status;

    /** 描述 */
    private String description;

    /** 上次执行时间 */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date lastExecuteTime;

    /** 下次执行时间 */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date nextExecuteTime;

    /** 执行次数 */
    private Integer executeCount = 0;

    /** 成功次数 */
    private Integer successCount = 0;

    /** 失败次数 */
    private Integer failureCount = 0;

    /** 最大执行次数 */
    private Integer maxExecuteCount;

    /** 开始时间 */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date startTime;

    /** 结束时间 */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date endTime;

    /** 执行配置 */
    private String executionConfig;

    /** 用例ID列表 */
    private String caseIds;

    /** 分类ID列表 */
    private String categoryIds;

    /** 环境ID */
    private Long environmentId;

    /** 执行人ID */
    private Long executorId;

    /** 项目名称 */
    @TableField(exist = false)
    private String projectName;

    /** 执行人姓名 */
    @TableField(exist = false)
    private String executorName;

    /** 环境名称 */
    @TableField(exist = false)
    private String environmentName;

    /** 关联的测试用例列表 */
    @TableField(exist = false)
    private List<TestCase> testCases;

    /** 调度配置对象 */
    @TableField(exist = false)
    private ScheduleConfig configObject;

    @Data
    public static class ScheduleConfig {
        /** 并行执行数量 */
        private Integer parallelCount = 1;

        /** 超时时间(分钟) */
        private Integer timeoutMinutes = 60;

        /** 失败后是否继续 */
        private Boolean continueOnFailure = true;

        /** 是否发送通知 */
        private Boolean sendNotification = true;

        /** 通知类型 (EMAIL/WEBHOOK/SMS) */
        private List<String> notificationTypes;

        /** 通知接收人 */
        private List<String> notificationRecipients;

        /** 是否跳过已通过的用例 */
        private Boolean skipPassedCases = false;

        /** 测试数据覆盖 */
        private Boolean useTestData = false;

        /** 重试策略 */
        private RetryStrategy retryStrategy;

        @Data
        public static class RetryStrategy {
            /** 最大重试次数 */
            private Integer maxRetries = 0;

            /** 重试间隔(秒) */
            private Integer retryInterval = 30;

            /** 重试条件 (ALL/FAILED_ONLY/TIMEOUT_ONLY) */
            private String retryCondition = "FAILED_ONLY";
        }
    }
}