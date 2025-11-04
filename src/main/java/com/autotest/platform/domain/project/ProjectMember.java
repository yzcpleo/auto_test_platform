package com.autotest.platform.domain.project;

import com.autotest.platform.common.annotation.Excel;
import com.autotest.platform.common.core.domain.BaseEntity;
import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

import java.util.Date;
import java.util.List;

/**
 * 项目成员对象 test_project_member
 *
 * @author autotest
 * @date 2024-01-01
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("test_project_member")
@Accessors(chain = true)
public class ProjectMember extends BaseEntity {
    private static final long serialVersionUID = 1L;

    /** 成员ID */
    @TableId(value = "member_id", type = IdType.AUTO)
    private Long memberId;

    /** 项目ID */
    @Excel(name = "项目ID")
    @TableField("project_id")
    private Long projectId;

    /** 用户ID */
    @Excel(name = "用户ID")
    @TableField("user_id")
    private Long userId;

    /** 用户名称 (非数据库字段，用于展示) */
    @TableField(exist = false)
    private String userName;

    /** 用户昵称 (非数据库字段，用于展示) */
    @TableField(exist = false)
    private String nickName;

    /** 用户邮箱 (非数据库字段，用于展示) */
    @TableField(exist = false)
    private String email;

    /** 角色类型(OWNER,ADMIN,MEMBER,VIEWER) */
    @Excel(name = "角色类型", readConverterExp = "OWNER=项目负责人,ADMIN=管理员,MEMBER=成员,VIEWER=查看者")
    @TableField("role_type")
    private String roleType;

    /** 权限列表(JSON格式) */
    @TableField("permissions")
    private String permissions;

    /** 权限列表 (非数据库字段，用于展示) */
    @TableField(exist = false)
    private List<String> permissionList;

    /** 加入时间 */
    @Excel(name = "加入时间", width = 30, dateFormat = "yyyy-MM-dd HH:mm:ss")
    @TableField("join_time")
    private Date joinTime;

    @TableField(fill = FieldFill.INSERT)
    private String createBy;

    @TableField(fill = FieldFill.INSERT)
    private Date createTime;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private String updateBy;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private Date updateTime;
}