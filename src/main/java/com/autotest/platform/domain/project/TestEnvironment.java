package com.autotest.platform.domain.project;

import com.autotest.platform.common.annotation.Excel;
import com.autotest.platform.common.core.domain.BaseEntity;
import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

import java.util.Date;
import java.util.Map;

/**
 * 测试环境对象 test_environment
 *
 * @author autotest
 * @date 2024-01-01
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("test_environment")
@Accessors(chain = true)
public class TestEnvironment extends BaseEntity {
    private static final long serialVersionUID = 1L;

    /** 环境ID */
    @TableId(value = "env_id", type = IdType.AUTO)
    private Long envId;

    /** 项目ID */
    @Excel(name = "项目ID")
    @TableField("project_id")
    private Long projectId;

    /** 环境名称 */
    @Excel(name = "环境名称")
    @TableField("env_name")
    private String envName;

    /** 环境类型(DEV,TEST,STAGING,PROD) */
    @Excel(name = "环境类型", readConverterExp = "DEV=开发环境,TEST=测试环境,STAGING=预发布环境,PROD=生产环境")
    @TableField("env_type")
    private String envType;

    /** 基础URL */
    @Excel(name = "基础URL")
    @TableField("base_url")
    private String baseUrl;

    /** 数据库配置(JSON加密) */
    @TableField("db_config")
    private String dbConfig;

    /** 数据库配置对象 (非数据库字段，用于操作) */
    @TableField(exist = false)
    private Map<String, Object> dbConfigMap;

    /** API配置(JSON) */
    @TableField("api_config")
    private String apiConfig;

    /** API配置对象 (非数据库字段，用于操作) */
    @TableField(exist = false)
    private Map<String, Object> apiConfigMap;

    /** 自定义配置(JSON) */
    @TableField("custom_config")
    private String customConfig;

    /** 自定义配置对象 (非数据库字段，用于操作) */
    @TableField(exist = false)
    private Map<String, Object> customConfigMap;

    /** 是否默认环境 */
    @Excel(name = "是否默认环境", readConverterExp = "0=否,1=是")
    @TableField("is_default")
    private String isDefault;

    /** 排序 */
    @Excel(name = "排序")
    @TableField("sort_order")
    private Integer sortOrder;

    @TableField(fill = FieldFill.INSERT)
    private String createBy;

    @TableField(fill = FieldFill.INSERT)
    private Date createTime;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private String updateBy;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private Date updateTime;
}