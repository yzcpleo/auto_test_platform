-- ----------------------------
-- CI/CD Jenkins集成相关表结构
-- ----------------------------

-- Jenkins服务器配置表
DROP TABLE IF EXISTS `jenkins_server`;
CREATE TABLE `jenkins_server` (
  `server_id` bigint NOT NULL AUTO_INCREMENT COMMENT '服务器主键',
  `server_name` varchar(100) NOT NULL COMMENT '服务器名称',
  `server_url` varchar(500) NOT NULL COMMENT 'Jenkins服务器URL',
  `username` varchar(100) COMMENT '用户名',
  `password` varchar(500) COMMENT '密码(加密存储)',
  `api_token` varchar(500) COMMENT 'API Token(加密存储)',
  `is_default` tinyint(1) NOT NULL DEFAULT 0 COMMENT '是否默认服务器：0-否，1-是',
  `connection_timeout` int NOT NULL DEFAULT 30 COMMENT '连接超时时间(秒)',
  `read_timeout` int NOT NULL DEFAULT 60 COMMENT '读取超时时间(秒)',
  `status` varchar(20) NOT NULL DEFAULT 'ACTIVE' COMMENT '状态：ACTIVE-激活，INACTIVE-停用',
  `last_check_time` datetime COMMENT '最后检查时间',
  `check_result` varchar(20) COMMENT '检查结果：SUCCESS-成功，FAILED-失败，UNKNOWN-未知',
  `project_id` bigint COMMENT '项目ID',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `create_by` varchar(64) DEFAULT '' COMMENT '创建者',
  `update_by` varchar(64) DEFAULT '' COMMENT '更新者',
  `remark` varchar(500) DEFAULT NULL COMMENT '备注',
  PRIMARY KEY (`server_id`),
  UNIQUE KEY `uk_server_name` (`server_name`),
  KEY `idx_server_project_id` (`project_id`),
  KEY `idx_server_status` (`status`),
  KEY `idx_server_is_default` (`is_default`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='Jenkins服务器配置表';

-- Jenkins作业配置表
DROP TABLE IF EXISTS `jenkins_job`;
CREATE TABLE `jenkins_job` (
  `job_id` bigint NOT NULL AUTO_INCREMENT COMMENT '作业主键',
  `job_name` varchar(200) NOT NULL COMMENT '作业名称',
  `job_display_name` varchar(200) COMMENT '作业显示名称',
  `job_url` varchar(500) COMMENT '作业URL',
  `server_id` bigint NOT NULL COMMENT '服务器ID',
  `job_type` varchar(50) NOT NULL COMMENT '作业类型：FREESTYLE-自由风格，PIPELINE-流水线，MULTIBRANCH-多分支流水线',
  `job_config` longtext COMMENT '作业配置(XML格式)',
  `parameters` text COMMENT '作业参数(JSON格式)',
  `scm_url` varchar(500) COMMENT '源码仓库URL',
  `scm_branch` varchar(200) COMMENT '源码分支',
  `build_tool` varchar(50) COMMENT '构建工具：MAVEN- Maven，GRADLE- Gradle，ANT- Ant，NONE-无',
  `build_command` text COMMENT '构建命令',
  `test_command` text COMMENT '测试命令',
  `artifacts` text COMMENT '构建产物(JSON格式)',
  `is_enabled` tinyint(1) NOT NULL DEFAULT 1 COMMENT '是否启用：0-禁用，1-启用',
  `auto_trigger` tinyint(1) NOT NULL DEFAULT 0 COMMENT '是否自动触发：0-否，1-是',
  `last_build_number` int COMMENT '最后构建号',
  `last_build_status` varchar(20) COMMENT '最后构建状态',
  `last_build_time` datetime COMMENT '最后构建时间',
  `project_id` bigint COMMENT '项目ID',
  `pipeline_id` bigint COMMENT '关联的流水线ID',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `create_by` varchar(64) DEFAULT '' COMMENT '创建者',
  `update_by` varchar(64) DEFAULT '' COMMENT '更新者',
  `remark` varchar(500) DEFAULT NULL COMMENT '备注',
  PRIMARY KEY (`job_id`),
  UNIQUE KEY `uk_job_server_name` (`server_id`, `job_name`),
  KEY `idx_job_server_id` (`server_id`),
  KEY `idx_job_project_id` (`project_id`),
  KEY `idx_job_pipeline_id` (`pipeline_id`),
  KEY `idx_job_enabled` (`is_enabled`),
  KEY `idx_job_type` (`job_type`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='Jenkins作业配置表';

-- Jenkins构建记录表
DROP TABLE IF EXISTS `jenkins_build`;
CREATE TABLE `jenkins_build` (
  `build_id` bigint NOT NULL AUTO_INCREMENT COMMENT '构建主键',
  `job_id` bigint NOT NULL COMMENT '作业ID',
  `build_number` int NOT NULL COMMENT '构建号',
  `build_url` varchar(500) COMMENT '构建URL',
  `build_status` varchar(20) NOT NULL COMMENT '构建状态：SUCCESS-成功，FAILURE-失败，ABORTED-中止，UNSTABLE-不稳定，NOT_BUILT-未构建，RUNNING-运行中，PENDING-等待中',
  `build_duration` int COMMENT '构建时长(毫秒)',
  `build_start_time` datetime COMMENT '构建开始时间',
  `build_end_time` datetime COMMENT '构建结束时间',
  `build_cause` text COMMENT '构建原因(JSON格式)',
  `build_parameters` text COMMENT '构建参数(JSON格式)',
  `build_log` longtext COMMENT '构建日志',
  `test_results` text COMMENT '测试结果(JSON格式)',
  `artifacts` text COMMENT '构建产物(JSON格式)',
  `console_output` longtext COMMENT '控制台输出',
  `error_message` text COMMENT '错误信息',
  `triggered_by` varchar(100) COMMENT '触发者',
  `trigger_source` varchar(50) COMMENT '触发来源：MANUAL-手动，WEBHOOK-Webhook，SCHEDULE-定时，UPSTREAM-上游',
  `pipeline_execution_id` bigint COMMENT '关联的流水线执行ID',
  `webhook_event_id` bigint COMMENT '关联的Webhook事件ID',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`build_id`),
  UNIQUE KEY `uk_build_job_number` (`job_id`, `build_number`),
  KEY `idx_build_job_id` (`job_id`),
  KEY `idx_build_status` (`build_status`),
  KEY `idx_build_start_time` (`build_start_time`),
  KEY `idx_build_pipeline_execution` (`pipeline_execution_id`),
  KEY `idx_build_webhook_event` (`webhook_event_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='Jenkins构建记录表';

-- Jenkins构建测试结果表
DROP TABLE IF EXISTS `jenkins_build_test`;
CREATE TABLE `jenkins_build_test` (
  `test_id` bigint NOT NULL AUTO_INCREMENT COMMENT '测试主键',
  `build_id` bigint NOT NULL COMMENT '构建ID',
  `test_suite` varchar(200) NOT NULL COMMENT '测试套件',
  `test_class` varchar(200) NOT NULL COMMENT '测试类',
  `test_name` varchar(200) NOT NULL COMMENT '测试名称',
  `test_status` varchar(20) NOT NULL COMMENT '测试状态：PASSED-通过，FAILED-失败，SKIPPED-跳过，ERROR-错误',
  `test_duration` decimal(10,3) COMMENT '测试耗时(秒)',
  `error_message` text COMMENT '错误信息',
  `stack_trace` text COMMENT '错误堆栈',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (`test_id`),
  KEY `idx_test_build_id` (`build_id`),
  KEY `idx_test_suite` (`test_suite`),
  KEY `idx_test_status` (`test_status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='Jenkins构建测试结果表';

-- Jenkins构建产物表
DROP TABLE IF EXISTS `jenkins_build_artifact`;
CREATE TABLE `jenkins_build_artifact` (
  `artifact_id` bigint NOT NULL AUTO_INCREMENT COMMENT '产物主键',
  `build_id` bigint NOT NULL COMMENT '构建ID',
  `artifact_name` varchar(200) NOT NULL COMMENT '产物名称',
  `artifact_path` varchar(500) NOT NULL COMMENT '产物路径',
  `artifact_url` varchar(500) COMMENT '产物URL',
  `artifact_type` varchar(50) NOT NULL COMMENT '产物类型：FILE-文件，ARCHIVE-归档，IMAGE-镜像，REPORT-报告',
  `artifact_size` bigint COMMENT '产物大小(字节)',
  `md5_checksum` varchar(32) COMMENT 'MD5校验和',
  `download_count` int NOT NULL DEFAULT 0 COMMENT '下载次数',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (`artifact_id`),
  KEY `idx_artifact_build_id` (`build_id`),
  KEY `idx_artifact_type` (`artifact_type`),
  KEY `idx_artifact_name` (`artifact_name`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='Jenkins构建产物表';

-- Jenkins服务器与项目关联表
DROP TABLE IF EXISTS `jenkins_server_project`;
CREATE TABLE `jenkins_server_project` (
  `relation_id` bigint NOT NULL AUTO_INCREMENT COMMENT '关联主键',
  `server_id` bigint NOT NULL COMMENT '服务器ID',
  `project_id` bigint NOT NULL COMMENT '项目ID',
  `is_enabled` tinyint(1) NOT NULL DEFAULT 1 COMMENT '是否启用：0-禁用，1-启用',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (`relation_id`),
  UNIQUE KEY `uk_server_project` (`server_id`, `project_id`),
  KEY `idx_relation_server_id` (`server_id`),
  KEY `idx_relation_project_id` (`project_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='Jenkins服务器与项目关联表';

-- ----------------------------
-- 初始化Jenkins配置数据
-- ----------------------------

INSERT INTO `jenkins_server` (`server_name`, `server_url`, `username`, `api_token`, `is_default`, `connection_timeout`, `read_timeout`, `status`, `remark`) VALUES
('默认Jenkins服务器', 'http://localhost:8080', 'admin', '', 1, 30, 60, 'ACTIVE', '默认的Jenkins服务器配置'),
('测试环境Jenkins', 'http://jenkins-test.company.com', 'jenkins', '', 0, 30, 60, 'ACTIVE', '测试环境专用Jenkins服务器'),
('生产环境Jenkins', 'http://jenkins-prod.company.com', 'jenkins-prod', '', 0, 60, 120, 'INACTIVE', '生产环境专用Jenkins服务器');