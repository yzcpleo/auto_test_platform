package com.autotest.platform.domain.project;

import com.autotest.platform.common.annotation.Excel;
import com.autotest.platform.common.core.domain.BaseEntity;
import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

import java.util.Date;

/**
 * 测试项目对象 test_project
 *
 * @author autotest
 * @date 2024-01-01
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("test_project")
@Accessors(chain = true)
public class TestProject extends BaseEntity {
    private static final long serialVersionUID = 1L;

    /** 项目ID */
    @TableId(value = "project_id", type = IdType.AUTO)
    private Long projectId;

    /** 项目编码 */
    @Excel(name = "项目编码")
    @TableField("project_code")
    private String projectCode;

    /** 项目名称 */
    @Excel(name = "项目名称")
    @TableField("project_name")
    private String projectName;

    /** 项目描述 */
    @Excel(name = "项目描述")
    @TableField("description")
    private String description;

    /** 项目状态(0正常 1停用) */
    @Excel(name = "项目状态", readConverterExp = "0=正常,1=停用")
    @TableField("status")
    private String status;

    /** Git仓库地址 */
    @Excel(name = "Git仓库地址")
    @TableField("git_repo_url")
    private String gitRepoUrl;

    /** Git分支 */
    @Excel(name = "Git分支")
    @TableField("git_branch")
    private String gitBranch;

    /** Git访问Token(加密) */
    @TableField("git_access_token")
    private String gitAccessToken;

    /** 项目负责人ID */
    @Excel(name = "项目负责人ID")
    @TableField("owner_id")
    private Long ownerId;

    /** 项目负责人名称 (非数据库字段，用于展示) */
    @TableField(exist = false)
    private String ownerName;

    /** 项目成员数量 (非数据库字段，用于展示) */
    @TableField(exist = false)
    private Integer memberCount;

    /** 测试用例数量 (非数据库字段，用于展示) */
    @TableField(exist = false)
    private Integer caseCount;

    /** 最近执行时间 (非数据库字段，用于展示) */
    @TableField(exist = false)
    private Date lastExecutionTime;

    /** 最近执行状态 (非数据库字段，用于展示) */
    @TableField(exist = false)
    private String lastExecutionStatus;

    /** 删除标志(0代表存在 2代表删除) */
    @TableField("del_flag")
    private String delFlag;

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
}