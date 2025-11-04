-- ----------------------------
-- 报告分析系统相关表 - Phase 4
-- ----------------------------

-- ----------------------------
-- 测试报告表
-- ----------------------------
DROP TABLE IF EXISTS `test_report`;
CREATE TABLE `test_report` (
  `report_id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '报告ID',
  `report_code` varchar(50) NOT NULL COMMENT '报告编号',
  `report_name` varchar(200) NOT NULL COMMENT '报告名称',
  `report_type` varchar(20) NOT NULL COMMENT '报告类型(EXECUTION/TREND/ANALYSIS/SUMMARY)',
  `execution_id` bigint(20) DEFAULT NULL COMMENT '关联执行ID',
  `project_id` bigint(20) DEFAULT NULL COMMENT '关联项目ID',
  `status` varchar(20) NOT NULL DEFAULT 'GENERATING' COMMENT '报告状态(GENERATING/COMPLETED/FAILED)',
  `format` varchar(10) NOT NULL DEFAULT 'HTML' COMMENT '报告格式(HTML/PDF/EXCEL/JSON)',
  `template` varchar(100) DEFAULT NULL COMMENT '报告模板',
  `summary` text COMMENT '报告内容摘要',
  `report_data` longtext COMMENT '报告数据(JSON格式)',
  `file_path` varchar(500) DEFAULT NULL COMMENT '报告文件路径',
  `report_url` varchar(500) DEFAULT NULL COMMENT '报告URL',
  `generate_start_time` datetime DEFAULT NULL COMMENT '生成开始时间',
  `generate_end_time` datetime DEFAULT NULL COMMENT '生成完成时间',
  `generate_duration` bigint(20) DEFAULT NULL COMMENT '生成耗时(毫秒)',
  `generator_id` bigint(20) DEFAULT NULL COMMENT '生成人ID',
  `create_by` varchar(64) DEFAULT '' COMMENT '创建者',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_by` varchar(64) DEFAULT '' COMMENT '更新者',
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `remark` varchar(500) DEFAULT NULL COMMENT '备注',
  PRIMARY KEY (`report_id`),
  UNIQUE KEY `uk_report_code` (`report_code`),
  KEY `idx_project_id` (`project_id`),
  KEY `idx_execution_id` (`execution_id`),
  KEY `idx_report_type` (`report_type`),
  KEY `idx_status` (`status`),
  KEY `idx_generator_id` (`generator_id`),
  KEY `idx_create_time` (`create_time`),
  CONSTRAINT `fk_report_execution` FOREIGN KEY (`execution_id`) REFERENCES `test_execution` (`execution_id`) ON DELETE SET NULL,
  CONSTRAINT `fk_report_project` FOREIGN KEY (`project_id`) REFERENCES `test_project` (`project_id`) ON DELETE CASCADE
) ENGINE=InnoDB AUTO_INCREMENT=1 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='测试报告表';

-- ----------------------------
-- 报告模板表
-- ----------------------------
DROP TABLE IF EXISTS `report_template`;
CREATE TABLE `report_template` (
  `template_id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '模板ID',
  `template_name` varchar(200) NOT NULL COMMENT '模板名称',
  `template_code` varchar(50) NOT NULL COMMENT '模板编码',
  `template_type` varchar(20) NOT NULL COMMENT '模板类型(EXECUTION/TREND/ANALYSIS/SUMMARY/CUSTOM)',
  `description` varchar(500) DEFAULT NULL COMMENT '模板描述',
  `html_template` longtext COMMENT 'HTML模板内容',
  `css_styles` text COMMENT 'CSS样式',
  `js_script` text COMMENT 'JavaScript脚本',
  `template_variables` text COMMENT '模板变量定义(JSON格式)',
  `default_config` text COMMENT '默认配置(JSON格式)',
  `is_system` tinyint(1) DEFAULT 0 COMMENT '是否为系统模板',
  `enabled` tinyint(1) DEFAULT 1 COMMENT '是否启用',
  `usage_count` int(11) DEFAULT 0 COMMENT '使用次数',
  `last_used_time` datetime DEFAULT NULL COMMENT '最后使用时间',
  `preview_image` varchar(500) DEFAULT NULL COMMENT '模板预览图',
  `version` varchar(20) DEFAULT '1.0' COMMENT '模板版本',
  `supported_formats` varchar(100) DEFAULT 'HTML,PDF' COMMENT '兼容的报告格式',
  `create_by` varchar(64) DEFAULT '' COMMENT '创建者',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_by` varchar(64) DEFAULT '' COMMENT '更新者',
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `remark` varchar(500) DEFAULT NULL COMMENT '备注',
  PRIMARY KEY (`template_id`),
  UNIQUE KEY `uk_template_code` (`template_code`),
  KEY `idx_template_type` (`template_type`),
  KEY `idx_is_system` (`is_system`),
  KEY `idx_enabled` (`enabled`)
) ENGINE=InnoDB AUTO_INCREMENT=1 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='报告模板表';

-- ----------------------------
-- 报告分享表
-- ----------------------------
DROP TABLE IF EXISTS `report_share`;
CREATE TABLE `report_share` (
  `share_id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '分享ID',
  `report_id` bigint(20) NOT NULL COMMENT '报告ID',
  `share_token` varchar(100) NOT NULL COMMENT '分享令牌',
  `share_url` varchar(500) NOT NULL COMMENT '分享链接',
  `share_type` varchar(20) DEFAULT 'LINK' COMMENT '分享类型(LINK/EMAIL)',
  `share_config` text COMMENT '分享配置(JSON格式)',
  `access_count` int(11) DEFAULT 0 COMMENT '访问次数',
  `max_access_count` int(11) DEFAULT NULL COMMENT '最大访问次数',
  `expire_time` datetime DEFAULT NULL COMMENT '过期时间',
  `password` varchar(100) DEFAULT NULL COMMENT '访问密码',
  `creator_id` bigint(20) DEFAULT NULL COMMENT '创建人ID',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `last_access_time` datetime DEFAULT NULL COMMENT '最后访问时间',
  `status` varchar(20) DEFAULT 'ACTIVE' COMMENT '状态(ACTIVE/EXPIRED/DISABLED)',
  PRIMARY KEY (`share_id`),
  UNIQUE KEY `uk_share_token` (`share_token`),
  KEY `idx_report_id` (`report_id`),
  KEY `idx_creator_id` (`creator_id`),
  KEY `idx_status` (`status`),
  KEY `idx_expire_time` (`expire_time`),
  CONSTRAINT `fk_share_report` FOREIGN KEY (`report_id`) REFERENCES `test_report` (`report_id`) ON DELETE CASCADE
) ENGINE=InnoDB AUTO_INCREMENT=1 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='报告分享表';

-- ----------------------------
-- 插入默认报告模板
-- ----------------------------
INSERT INTO `report_template` (`template_name`, `template_code`, `template_type`, `description`, `html_template`, `css_styles`, `js_script`, `template_variables`, `default_config`, `is_system`, `enabled`, `version`, `supported_formats`, `create_by`, `create_time`) VALUES
('默认执行报告模板', 'DEFAULT_EXECUTION', 'EXECUTION', '系统默认的测试执行报告模板', '<!DOCTYPE html>
<html>
<head>
    <title>{{reportName}}</title>
    <style>{{cssStyles}}</style>
</head>
<body>
    <div class="header">
        <h1>{{reportName}}</h1>
        <p>生成时间: {{generateTime}}</p>
    </div>
    <div class="content">
        {{reportContent}}
    </div>
</body>
</html>', NULL, NULL, NULL, '{"includeCharts": true, "includeDetailedLogs": false, "theme": "light"}', 1, 1, '1.0', 'HTML,PDF', 'admin', NOW()),

('默认趋势报告模板', 'DEFAULT_TREND', 'TREND', '系统默认的趋势分析报告模板', '<!DOCTYPE html>
<html>
<head>
    <title>{{reportName}}</title>
    <style>{{cssStyles}}</style>
</head>
<body>
    <div class="header">
        <h1>{{reportName}}</h1>
        <p>时间范围: {{timeRange}}</p>
    </div>
    <div class="content">
        {{reportContent}}
    </div>
</body>
</html>', NULL, NULL, NULL, '{"includeCharts": true, "timeRange": "7d", "theme": "light"}', 1, 1, '1.0', 'HTML,PDF', 'admin', NOW()),

('默认摘要报告模板', 'DEFAULT_SUMMARY', 'SUMMARY', '系统默认的项目摘要报告模板', '<!DOCTYPE html>
<html>
<head>
    <title>{{reportName}}</title>
    <style>{{cssStyles}}</style>
</head>
<body>
    <div class="header">
        <h1>{{reportName}}</h1>
        <p>项目: {{projectName}}</p>
    </div>
    <div class="content">
        {{reportContent}}
    </div>
</body>
</html>', NULL, NULL, NULL, '{"includeCharts": true, "includePerformanceMetrics": true, "theme": "light"}', 1, 1, '1.0', 'HTML,PDF', 'admin', NOW());

-- ----------------------------
-- 创建索引优化查询性能
-- ----------------------------
-- 报告表索引
CREATE INDEX `idx_report_project_status` ON `test_report` (`project_id`, `status`);
CREATE INDEX `idx_report_type_time` ON `test_report` (`report_type`, `create_time`);
CREATE INDEX `idx_report_generator_status` ON `test_report` (`generator_id`, `status`);

-- 报告模板表索引
CREATE INDEX `idx_template_type_enabled` ON `report_template` (`template_type`, `enabled`);
CREATE INDEX `idx_template_usage` ON `report_template` (`usage_count`, `last_used_time`);

-- 报告分享表索引
CREATE INDEX `idx_share_report_status` ON `report_share` (`report_id`, `status`);
CREATE INDEX `idx_share_token_expire` ON `report_share` (`share_token`, `expire_time`);

-- ----------------------------
-- 插入示例报告数据
-- ----------------------------
INSERT INTO `test_report` (`report_code`, `report_name`, `report_type`, `execution_id`, `project_id`, `status`, `format`, `summary`, `file_path`, `generate_start_time`, `generate_end_time`, `generator_id`, `create_by`, `create_time`) VALUES
('RPT-EXEC-20240101-001', '测试执行报告 - 示例', 'EXECUTION', 1, 1, 'COMPLETED', 'HTML', '示例执行报告，包含详细的测试结果和分析', '/reports/RPT-EXEC-20240101-001.html', '2024-01-01 10:00:00', '2024-01-01 10:01:30', 1, 'admin', NOW()),
('RPT-TREND-20240101-001', '趋势分析报告 - 示例', 'TREND', NULL, 1, 'COMPLETED', 'HTML', '示例趋势报告，展示7天的测试执行趋势', '/reports/RPT-TREND-20240101-001.html', '2024-01-01 11:00:00', '2024-01-01 11:02:00', 1, 'admin', NOW()),
('RPT-SUMMARY-20240101-001', '项目摘要报告 - 示例', 'SUMMARY', NULL, 1, 'COMPLETED', 'HTML', '示例项目摘要报告，包含项目的整体测试概况', '/reports/RPT-SUMMARY-20240101-001.html', '2024-01-01 12:00:00', '2024-01-01 12:01:00', 1, 'admin', NOW());