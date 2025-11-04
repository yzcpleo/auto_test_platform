package com.autotest.platform.domain.report;

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
 * 测试报告对象 test_report
 *
 * @author autotest
 * @date 2024-01-01
 */
@Data
@TableName("test_report")
public class TestReport extends BaseEntity {
    private static final long serialVersionUID = 1L;

    @TableId(type = IdType.AUTO)
    private Long reportId;

    /** 报告编号 */
    private String reportCode;

    /** 报告名称 */
    private String reportName;

    /** 报告类型 (EXECUTION/TREND/ANALYSIS/SUMMARY) */
    private String reportType;

    /** 关联执行ID */
    private Long executionId;

    /** 关联项目ID */
    private Long projectId;

    /** 报告状态 (GENERATING/COMPLETED/FAILED) */
    private String status;

    /** 报告格式 (HTML/PDF/EXCEL/JSON) */
    private String format;

    /** 报告模板 */
    private String template;

    /** 报告内容摘要 */
    private String summary;

    /** 报告数据 */
    private String reportData;

    /** 报告文件路径 */
    private String filePath;

    /** 报告URL */
    private String reportUrl;

    /** 生成开始时间 */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date generateStartTime;

    /** 生成完成时间 */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date generateEndTime;

    /** 生成耗时(毫秒) */
    private Long generateDuration;

    /** 生成人ID */
    private Long generatorId;

    /** 生成人姓名 */
    @TableField(exist = false)
    private String generatorName;

    /** 项目名称 */
    @TableField(exist = false)
    private String projectName;

    /** 执行编号 */
    @TableField(exist = false)
    private String executionCode;

    /** 执行名称 */
    @TableField(exist = false)
    private String executionName;

    /** 报告配置 */
    @TableField(exist = false)
    private ReportConfig reportConfig;

    /** 报告统计信息 */
    @TableField(exist = false)
    private ReportStatistics statistics;

    /** 报告图表数据 */
    @TableField(exist = false)
    private List<ChartData> chartData;

    /**
     * 报告配置
     */
    @Data
    public static class ReportConfig {
        /** 是否包含图表 */
        private Boolean includeCharts = true;

        /** 是否包含详细日志 */
        private Boolean includeDetailedLogs = false;

        /** 是否包含截图 */
        private Boolean includeScreenshots = true;

        /** 是否包含性能指标 */
        private Boolean includePerformanceMetrics = true;

        /** 报告主题 (light/dark) */
        private String theme = "light";

        /** 自定义样式 */
        private String customStyles;

        /** 报告语言 */
        private String language = "zh-CN";

        /** 时间范围 */
        private String timeRange;

        /** 过滤条件 */
        private Map<String, Object> filters;
    }

    /**
     * 报告统计信息
     */
    @Data
    public static class ReportStatistics {
        /** 总执行数 */
        private Integer totalExecutions;

        /** 成功执行数 */
        private Integer successExecutions;

        /** 失败执行数 */
        private Integer failedExecutions;

        /** 跳过执行数 */
        private Integer skippedExecutions;

        /** 成功率 */
        private Double successRate;

        /** 平均执行时间 */
        private Double avgExecutionTime;

        /** 最长执行时间 */
        private Long maxExecutionTime;

        /** 最短执行时间 */
        private Long minExecutionTime;

        /** 总用例数 */
        private Integer totalCases;

        /** 成功用例数 */
        private Integer successCases;

        /** 失败用例数 */
        private Integer failedCases;

        /** 跳过用例数 */
        private Integer skippedCases;

        /** 用例成功率 */
        private Double caseSuccessRate;

        /** 错误分类统计 */
        private Map<String, Integer> errorCategories;

        /** 趋势数据 */
        private List<TrendData> trendData;

        /** 热门失败用例 */
        private List<FailedCaseInfo> topFailedCases;
    }

    /**
     * 趋势数据
     */
    @Data
    public static class TrendData {
        private String date;
        private Integer total;
        private Integer success;
        private Integer failed;
        private Double successRate;
    }

    /**
     * 失败用例信息
     */
    @Data
    public static class FailedCaseInfo {
        private Long caseId;
        private String caseTitle;
        private String caseType;
        private Integer failureCount;
        private String lastFailureTime;
        private String commonError;
        private Double failureRate;
    }

    /**
     * 图表数据
     */
    @Data
    public static class ChartData {
        private String chartId;
        private String chartType; // pie/line/bar/area
        private String title;
        private List<String> categories;
        private List<Number> values;
        private Map<String, Object> options;
    }
}