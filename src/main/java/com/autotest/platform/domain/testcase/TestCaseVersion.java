package com.autotest.platform.domain.testcase;

import com.autotest.platform.common.annotation.Excel;
import com.autotest.platform.common.core.domain.BaseEntity;
import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

import java.util.Date;
import java.util.List;

/**
 * 测试用例版本历史对象 test_case_version
 *
 * @author autotest
 * @date 2024-01-01
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("test_case_version")
@Accessors(chain = true)
public class TestCaseVersion extends BaseEntity {
    private static final long serialVersionUID = 1L;

    /** 版本ID */
    @TableId(value = "version_id", type = IdType.AUTO)
    private Long versionId;

    /** 用例ID */
    @Excel(name = "用例ID")
    @TableField("case_id")
    private Long caseId;

    /** 版本号 */
    @Excel(name = "版本号")
    @TableField("version_number")
    private Integer versionNumber;

    /** 用例标题 */
    @Excel(name = "用例标题")
    @TableField("case_title")
    private String caseTitle;

    /** 测试步骤 */
    @TableField("test_steps")
    private String testSteps;

    /** 测试步骤对象 (非数据库字段) */
    @TableField(exist = false)
    private List<TestCase.TestStep> testStepList;

    /** 期望结果 */
    @TableField("expected_result")
    private String expectedResult;

    /** 变更日志 */
    @Excel(name = "变更日志")
    @TableField("change_log")
    private String changeLog;

    /** 创建时间 */
    @Excel(name = "创建时间", width = 30, dateFormat = "yyyy-MM-dd HH:mm:ss")
    @TableField("create_time")
    private Date createTime;

    /** 作者名称 (非数据库字段) */
    @TableField(exist = false)
    private String authorName;

    /** 是否为最新版本 (非数据库字段) */
    @TableField(exist = false)
    private Boolean isLatest;

    /** 版本差异信息 (非数据库字段) */
    @TableField(exist = false)
    private VersionDiff versionDiff;

    /**
     * 版本差异信息
     */
    @Data
    public static class VersionDiff {
        private List<String> addedSteps;
        private List<String> modifiedSteps;
        private List<String> removedSteps;
        private String expectedResultChange;
    }
}