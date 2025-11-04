package com.autotest.platform.domain.testcase;

import com.autotest.platform.common.annotation.Excel;
import com.autotest.platform.common.core.domain.BaseEntity;
import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

import java.util.Date;
import java.util.List;
import java.util.Map;

/**
 * 测试用例对象 test_case
 *
 * @author autotest
 * @date 2024-01-01
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("test_case")
@Accessors(chain = true)
public class TestCase extends BaseEntity {
    private static final long serialVersionUID = 1L;

    /** 用例ID */
    @TableId(value = "case_id", type = IdType.AUTO)
    private Long caseId;

    /** 项目ID */
    @Excel(name = "项目ID")
    @TableField("project_id")
    private Long projectId;

    /** 分类ID */
    @Excel(name = "分类ID")
    @TableField("category_id")
    private Long categoryId;

    /** 用例标题 */
    @Excel(name = "用例标题")
    @TableField("case_title")
    private String caseTitle;

    /** 用例编码 */
    @Excel(name = "用例编码")
    @TableField("case_code")
    private String caseCode;

    /** 用例类型 */
    @Excel(name = "用例类型", readConverterExp = "WEB_UI=Web UI测试,API=API接口测试,UNIT=单元测试,PERFORMANCE=性能测试")
    @TableField("case_type")
    private String caseType;

    /** 优先级 */
    @Excel(name = "优先级", readConverterExp = "HIGH=高,MEDIUM=中,LOW=低")
    @TableField("priority")
    private String priority;

    /** 前置条件 */
    @TableField("preconditions")
    private String preconditions;

    /** 测试步骤 */
    @TableField("test_steps")
    private String testSteps;

    /** 测试步骤对象 (非数据库字段) */
    @TableField(exist = false)
    private List<TestStep> testStepList;

    /** 期望结果 */
    @TableField("expected_result")
    private String expectedResult;

    /** 测试数据源 */
    @TableField("test_data_source")
    private String testDataSource;

    /** 标签 */
    @TableField("tags")
    private String tags;

    /** 标签列表 (非数据库字段) */
    @TableField(exist = false)
    private List<String> tagList;

    /** 状态 */
    @Excel(name = "状态", readConverterExp = "DRAFT=草稿,ACTIVE=活跃,DEPRECATED=已废弃")
    @TableField("status")
    private String status;

    /** 版本号 */
    @Excel(name = "版本号")
    @TableField("version")
    private Integer version;

    /** 作者ID */
    @TableField("author_id")
    private Long authorId;

    /** 审核人ID */
    @TableField("reviewer_id")
    private Long reviewerId;

    /** 审核时间 */
    @TableField("review_time")
    private Date reviewTime;

    /** 删除标志 */
    @TableField("del_flag")
    private String delFlag;

    /** 分类名称 (非数据库字段) */
    @TableField(exist = false)
    private String categoryName;

    /** 作者名称 (非数据库字段) */
    @TableField(exist = false)
    private String authorName;

    /** 审核人名称 (非数据库字段) */
    @TableField(exist = false)
    private String reviewerName;

    /** 最近执行结果 (非数据库字段) */
    @TableField(exist = false)
    private String lastExecutionResult;

    /** 最近执行时间 (非数据库字段) */
    @TableField(exist = false)
    private Date lastExecutionTime;

    @TableField(fill = FieldFill.INSERT)
    private String createBy;

    @TableField(fill = FieldFill.INSERT)
    private Date createTime;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private String updateBy;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private Date updateTime;

    @TableField("remark")
    private String remark;

    /**
     * 测试步骤内部类
     */
    @Data
    public static class TestStep {
        private Integer stepNumber;
        private String action;
        private String elementType;
        private String locator;
        private String operation;
        private Object value;
        private String expectedResult;
        private Integer timeout;
    }
}