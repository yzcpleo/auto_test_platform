package com.autotest.platform.domain.report;

import com.autotest.platform.domain.BaseEntity;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.util.Map;

/**
 * 报告模板对象 report_template
 *
 * @author autotest
 * @date 2024-01-01
 */
@Data
@TableName("report_template")
public class ReportTemplate extends BaseEntity {
    private static final long serialVersionUID = 1L;

    @TableId(type = IdType.AUTO)
    private Long templateId;

    /** 模板名称 */
    private String templateName;

    /** 模板编码 */
    private String templateCode;

    /** 模板类型 (EXECUTION/TREND/ANALYSIS/SUMMARY/CUSTOM) */
    private String templateType;

    /** 模板描述 */
    private String description;

    /** HTML模板内容 */
    private String htmlTemplate;

    /** CSS样式 */
    private String cssStyles;

    /** JavaScript脚本 */
    private String jsScript;

    /** 模板变量定义 */
    private String templateVariables;

    /** 默认配置 */
    private String defaultConfig;

    /** 是否为系统模板 */
    private Boolean isSystem;

    /** 是否启用 */
    private Boolean enabled;

    /** 使用次数 */
    private Integer usageCount;

    /** 最后使用时间 */
    private java.util.Date lastUsedTime;

    /** 模板预览图 */
    private String previewImage;

    /** 模板版本 */
    private String version;

    /** 兼容的报告格式 */
    private String supportedFormats;

    /** 模板配置 */
    @SuppressWarnings("unused")
    private Map<String, Object> templateConfig;

    /**
     * 获取模板变量映射
     */
    public Map<String, Object> getVariableMapping() {
        // 解析templateVariables JSON字符串为Map
        return new java.util.HashMap<>();
    }

    /**
     * 获取默认配置映射
     */
    public Map<String, Object> getDefaultConfigMapping() {
        // 解析defaultConfig JSON字符串为Map
        return new java.util.HashMap<>();
    }
}