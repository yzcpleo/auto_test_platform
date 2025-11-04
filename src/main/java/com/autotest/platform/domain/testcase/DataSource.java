package com.autotest.platform.domain.testcase;

import com.autotest.platform.common.annotation.Excel;
import com.autotest.platform.common.core.domain.BaseEntity;
import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

import java.util.Date;
import java.util.Map;

/**
 * 测试数据源对象 test_data_source
 *
 * @author autotest
 * @date 2024-01-01
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("test_data_source")
@Accessors(chain = true)
public class DataSource extends BaseEntity {
    private static final long serialVersionUID = 1L;

    /** 数据ID */
    @TableId(value = "data_id", type = IdType.AUTO)
    private Long dataId;

    /** 项目ID */
    @Excel(name = "项目ID")
    @TableField("project_id")
    private Long projectId;

    /** 数据源名称 */
    @Excel(name = "数据源名称")
    @TableField("data_name")
    private String dataName;

    /** 数据类型 */
    @Excel(name = "数据类型", readConverterExp = "FILE=文件,DATABASE=数据库,API=接口")
    @TableField("data_type")
    private String dataType;

    /** 文件路径 */
    @TableField("file_path")
    private String filePath;

    /** 文件名 */
    @Excel(name = "文件名")
    @TableField("file_name")
    private String fileName;

    /** 数据库配置 */
    @TableField("db_config")
    private String dbConfig;

    /** 数据库配置对象 (非数据库字段) */
    @TableField(exist = false)
    private Map<String, Object> dbConfigMap;

    /** API配置 */
    @TableField("api_config")
    private String apiConfig;

    /** API配置对象 (非数据库字段) */
    @TableField(exist = false)
    private Map<String, Object> apiConfigMap;

    /** 数据结构 */
    @TableField("data_schema")
    private String dataSchema;

    /** 数据结构对象 (非数据库字段) */
    @TableField(exist = false)
    private Map<String, Object> dataSchemaMap;

    /** 记录数 */
    @Excel(name = "记录数")
    @TableField("record_count")
    private Integer recordCount;

    @TableField(fill = FieldFill.INSERT)
    private String createBy;

    @TableField(fill = FieldFill.INSERT)
    private Date createTime;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private String updateBy;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private Date updateTime;
}