package com.autotest.platform.domain.testcase;

import com.autotest.platform.common.annotation.Excel;
import com.autotest.platform.common.core.domain.BaseEntity;
import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

import java.util.ArrayList;
import java.util.List;

/**
 * 测试用例分类对象 test_case_category
 *
 * @author autotest
 * @date 2024-01-01
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("test_case_category")
@Accessors(chain = true)
public class TestCaseCategory extends BaseEntity {
    private static final long serialVersionUID = 1L;

    /** 分类ID */
    @TableId(value = "category_id", type = IdType.AUTO)
    private Long categoryId;

    /** 项目ID */
    @Excel(name = "项目ID")
    @TableField("project_id")
    private Long projectId;

    /** 分类名称 */
    @Excel(name = "分类名称")
    @TableField("category_name")
    private String categoryName;

    /** 父分类ID */
    @TableField("parent_id")
    private Long parentId;

    /** 祖级列表 */
    @TableField("ancestors")
    private String ancestors;

    /** 显示顺序 */
    @Excel(name = "显示顺序")
    @TableField("order_num")
    private Integer orderNum;

    /** 负责人 */
    @Excel(name = "负责人")
    @TableField("leader")
    private String leader;

    /** 子分类列表 (非数据库字段) */
    @TableField(exist = false)
    private List<TestCaseCategory> children = new ArrayList<>();

    /** 分类下用例数量 (非数据库字段) */
    @TableField(exist = false)
    private Integer caseCount;

    /** 分类层级 (非数据库字段) */
    @TableField(exist = false)
    private Integer level;

    @TableField(fill = FieldFill.INSERT)
    private String createBy;

    @TableField(fill = FieldFill.INSERT)
    private Date createTime;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private String updateBy;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private Date updateTime;
}