-- ----------------------------
-- CI/CD 流水线相关表结构
-- ----------------------------

-- 流水线表
DROP TABLE IF EXISTS `pipeline`;
CREATE TABLE `pipeline` (
  `pipeline_id` bigint NOT NULL AUTO_INCREMENT COMMENT '流水线主键',
  `pipeline_name` varchar(200) NOT NULL COMMENT '流水线名称',
  `pipeline_type` varchar(50) NOT NULL COMMENT '流水线类型：BUILD-构建，DEPLOY-部署，TEST-测试，CUSTOM-自定义',
  `trigger_type` varchar(50) NOT NULL COMMENT '触发类型：MANUAL-手动，WEBHOOK-Webhook，SCHEDULE-定时',
  `pipeline_config` longtext COMMENT '流水线配置(JSON格式)',
  `project_id` bigint NOT NULL COMMENT '项目ID',
  `status` varchar(20) NOT NULL DEFAULT 'ACTIVE' COMMENT '状态：ACTIVE-激活，INACTIVE-停用',
  `last_execution_time` datetime COMMENT '最后执行时间',
  `execution_count` int NOT NULL DEFAULT 0 COMMENT '执行次数',
  `success_count` int NOT NULL DEFAULT 0 COMMENT '成功次数',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `create_by` varchar(64) DEFAULT '' COMMENT '创建者',
  `update_by` varchar(64) DEFAULT '' COMMENT '更新者',
  `remark` varchar(500) DEFAULT NULL COMMENT '备注',
  PRIMARY KEY (`pipeline_id`),
  KEY `idx_pipeline_project_id` (`project_id`),
  KEY `idx_pipeline_status` (`status`),
  KEY `idx_pipeline_type` (`pipeline_type`),
  KEY `idx_pipeline_trigger_type` (`trigger_type`),
  KEY `idx_pipeline_last_execution` (`last_execution_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='流水线表';

-- 流水线执行表
DROP TABLE IF EXISTS `pipeline_execution`;
CREATE TABLE `pipeline_execution` (
  `execution_id` bigint NOT NULL AUTO_INCREMENT COMMENT '执行主键',
  `execution_code` varchar(100) NOT NULL COMMENT '执行编码',
  `pipeline_id` bigint NOT NULL COMMENT '流水线ID',
  `project_id` bigint NOT NULL COMMENT '项目ID',
  `status` varchar(20) NOT NULL DEFAULT 'PENDING' COMMENT '状态：PENDING-待执行，RUNNING-运行中，SUCCESS-成功，FAILED-失败，STOPPED-停止',
  `trigger_type` varchar(50) NOT NULL COMMENT '触发类型',
  `trigger_user_id` bigint COMMENT '触发用户ID',
  `execution_params` longtext COMMENT '执行参数(JSON格式)',
  `step_results` longtext COMMENT '步骤执行结果(JSON格式)',
  `start_time` datetime NOT NULL COMMENT '开始时间',
  `end_time` datetime COMMENT '结束时间',
  `duration` int COMMENT '执行时长(秒)',
  `error_message` text COMMENT '错误信息',
  `retried_from` bigint COMMENT '重试来源执行ID',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `create_by` varchar(64) DEFAULT '' COMMENT '创建者',
  `update_by` varchar(64) DEFAULT '' COMMENT '更新者',
  `remark` varchar(500) DEFAULT NULL COMMENT '备注',
  PRIMARY KEY (`execution_id`),
  UNIQUE KEY `uk_execution_code` (`execution_code`),
  KEY `idx_execution_pipeline_id` (`pipeline_id`),
  KEY `idx_execution_project_id` (`project_id`),
  KEY `idx_execution_status` (`status`),
  KEY `idx_execution_trigger_type` (`trigger_type`),
  KEY `idx_execution_start_time` (`start_time`),
  KEY `idx_execution_retried_from` (`retried_from`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='流水线执行表';

-- 流水线执行步骤表
DROP TABLE IF EXISTS `pipeline_execution_step`;
CREATE TABLE `pipeline_execution_step` (
  `step_id` bigint NOT NULL AUTO_INCREMENT COMMENT '步骤主键',
  `execution_id` bigint NOT NULL COMMENT '执行ID',
  `step_name` varchar(100) NOT NULL COMMENT '步骤名称',
  `step_type` varchar(50) NOT NULL COMMENT '步骤类型',
  `status` varchar(20) NOT NULL DEFAULT 'PENDING' COMMENT '状态：PENDING-待执行，RUNNING-运行中，SUCCESS-成功，FAILED-失败，SKIPPED-跳过',
  `start_time` datetime COMMENT '开始时间',
  `end_time` datetime COMMENT '结束时间',
  `output` text COMMENT '输出内容',
  `error_message` text COMMENT '错误信息',
  `step_index` int NOT NULL DEFAULT 0 COMMENT '步骤序号',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`step_id`),
  KEY `idx_step_execution_id` (`execution_id`),
  KEY `idx_step_name` (`step_name`),
  KEY `idx_step_status` (`status`),
  KEY `idx_step_index` (`step_index`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='流水线执行步骤表';

-- 流水线执行日志表
DROP TABLE IF EXISTS `pipeline_execution_log`;
CREATE TABLE `pipeline_execution_log` (
  `log_id` bigint NOT NULL AUTO_INCREMENT COMMENT '日志主键',
  `execution_id` bigint NOT NULL COMMENT '执行ID',
  `step_name` varchar(100) COMMENT '步骤名称',
  `log_content` longtext NOT NULL COMMENT '日志内容',
  `log_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '日志时间',
  PRIMARY KEY (`log_id`),
  KEY `idx_log_execution_id` (`execution_id`),
  KEY `idx_log_step_name` (`step_name`),
  KEY `idx_log_time` (`log_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='流水线执行日志表';

-- 流水线执行资源表
DROP TABLE IF EXISTS `pipeline_execution_resource`;
CREATE TABLE `pipeline_execution_resource` (
  `resource_id` bigint NOT NULL AUTO_INCREMENT COMMENT '资源主键',
  `execution_id` bigint NOT NULL COMMENT '执行ID',
  `cpu_usage` decimal(5,2) COMMENT 'CPU使用率(%)',
  `memory_usage` decimal(5,2) COMMENT '内存使用率(%)',
  `disk_usage` decimal(5,2) COMMENT '磁盘使用率(%)',
  `network_usage` decimal(10,2) COMMENT '网络使用量(MB)',
  `record_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '记录时间',
  PRIMARY KEY (`resource_id`),
  KEY `idx_resource_execution_id` (`execution_id`),
  KEY `idx_resource_time` (`record_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='流水线执行资源表';

-- 流水线执行产物表
DROP TABLE IF EXISTS `pipeline_execution_artifact`;
CREATE TABLE `pipeline_execution_artifact` (
  `artifact_id` bigint NOT NULL AUTO_INCREMENT COMMENT '产物主键',
  `execution_id` bigint NOT NULL COMMENT '执行ID',
  `artifact_name` varchar(200) NOT NULL COMMENT '产物名称',
  `artifact_type` varchar(50) NOT NULL COMMENT '产物类型：FILE-文件，IMAGE-镜像，REPORT-报告',
  `artifact_path` varchar(500) COMMENT '产物路径',
  `artifact_size` bigint COMMENT '产物大小(字节)',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (`artifact_id`),
  KEY `idx_artifact_execution_id` (`execution_id`),
  KEY `idx_artifact_type` (`artifact_type`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='流水线执行产物表';

-- 流水线执行环境变量表
DROP TABLE IF EXISTS `pipeline_execution_env`;
CREATE TABLE `pipeline_execution_env` (
  `env_id` bigint NOT NULL AUTO_INCREMENT COMMENT '环境变量主键',
  `execution_id` bigint NOT NULL COMMENT '执行ID',
  `env_key` varchar(100) NOT NULL COMMENT '环境变量键',
  `env_value` text COMMENT '环境变量值',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`env_id`),
  UNIQUE KEY `uk_env_execution_key` (`execution_id`, `env_key`),
  KEY `idx_env_execution_id` (`execution_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='流水线执行环境变量表';

-- 流水线模板表
DROP TABLE IF EXISTS `pipeline_template`;
CREATE TABLE `pipeline_template` (
  `template_id` bigint NOT NULL AUTO_INCREMENT COMMENT '模板主键',
  `template_type` varchar(100) NOT NULL COMMENT '模板类型',
  `template_name` varchar(200) NOT NULL COMMENT '模板名称',
  `template_config` longtext NOT NULL COMMENT '模板配置(JSON格式)',
  `description` varchar(500) COMMENT '模板描述',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`template_id`),
  UNIQUE KEY `uk_template_type` (`template_type`),
  KEY `idx_template_name` (`template_name`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='流水线模板表';

-- 流水线依赖关系表
DROP TABLE IF EXISTS `pipeline_dependency`;
CREATE TABLE `pipeline_dependency` (
  `dependency_id` bigint NOT NULL AUTO_INCREMENT COMMENT '依赖主键',
  `pipeline_id` bigint NOT NULL COMMENT '流水线ID',
  `dependency_pipeline_id` bigint NOT NULL COMMENT '依赖的流水线ID',
  `dependency_type` varchar(50) NOT NULL DEFAULT 'SUCCESS' COMMENT '依赖类型：SUCCESS-成功后执行，FAILED-失败后执行，ALWAYS-总是执行',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (`dependency_id`),
  UNIQUE KEY `uk_pipeline_dependency` (`pipeline_id`, `dependency_pipeline_id`),
  KEY `idx_dependency_pipeline_id` (`pipeline_id`),
  KEY `idx_dependency_dependency_id` (`dependency_pipeline_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='流水线依赖关系表';

-- 流水线执行重试关系表
DROP TABLE IF EXISTS `pipeline_execution_retry`;
CREATE TABLE `pipeline_execution_retry` (
  `retry_id` bigint NOT NULL AUTO_INCREMENT COMMENT '重试主键',
  `original_execution_id` bigint NOT NULL COMMENT '原始执行ID',
  `retry_execution_id` bigint NOT NULL COMMENT '重试执行ID',
  `retry_reason` varchar(500) COMMENT '重试原因',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (`retry_id`),
  UNIQUE KEY `uk_execution_retry` (`original_execution_id`, `retry_execution_id`),
  KEY `idx_retry_original_id` (`original_execution_id`),
  KEY `idx_retry_retry_id` (`retry_execution_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='流水线执行重试关系表';

-- ----------------------------
-- 初始化流水线模板数据
-- ----------------------------

INSERT INTO `pipeline_template` (`template_type`, `template_name`, `template_config`, `description`) VALUES
('单元测试模板', '单元测试流水线', '{
  "description": "运行单元测试并生成测试报告",
  "steps": [
    {
      "stepName": "构建项目",
      "stepType": "BUILD",
      "stepConfig": {
        "buildType": "MAVEN",
        "goals": "clean compile"
      }
    },
    {
      "stepName": "运行单元测试",
      "stepType": "TEST",
      "stepConfig": {
        "testType": "UNIT",
        "testFramework": "JUNIT",
        "includes": "**/*Test.java",
        "generateReport": true
      }
    }
  ]
}', '适用于Java项目的单元测试流水线模板'),
('集成测试模板', '集成测试流水线', '{
  "description": "运行集成测试并生成测试报告",
  "steps": [
    {
      "stepName": "构建项目",
      "stepType": "BUILD",
      "stepConfig": {
        "buildType": "MAVEN",
        "goals": "clean package"
      }
    },
    {
      "stepName": "部署测试环境",
      "stepType": "DEPLOY",
      "stepConfig": {
        "deployType": "TEST",
        "envName": "test"
      }
    },
    {
      "stepName": "运行集成测试",
      "stepType": "TEST",
      "stepConfig": {
        "testType": "INTEGRATION",
        "testFramework": "TESTNG",
        "includes": "**/*IT.java",
        "generateReport": true
      }
    }
  ]
}', '适用于Java项目的集成测试流水线模板'),
('API测试模板', 'API测试流水线', '{
  "description": "运行API测试并生成测试报告",
  "steps": [
    {
      "stepName": "运行API测试",
      "stepType": "TEST",
      "stepConfig": {
        "testType": "API",
        "testFramework": "REST_ASSURED",
        "testSuite": "api-test-suite.json",
        "generateReport": true
      }
    }
  ]
}', '适用于API接口测试的流水线模板'),
('Maven构建模板', 'Maven构建流水线', '{
  "description": "使用Maven构建项目",
  "steps": [
    {
      "stepName": "Maven构建",
      "stepType": "BUILD",
      "stepConfig": {
        "buildType": "MAVEN",
        "goals": "clean package",
        "skipTests": false
      }
    }
  ]
}', '适用于Maven项目的构建流水线模板'),
('Gradle构建模板', 'Gradle构建流水线', '{
  "description": "使用Gradle构建项目",
  "steps": [
    {
      "stepName": "Gradle构建",
      "stepType": "BUILD",
      "stepConfig": {
        "buildType": "GRADLE",
        "tasks": "clean build",
        "skipTests": false
      }
    }
  ]
}', '适用于Gradle项目的构建流水线模板');