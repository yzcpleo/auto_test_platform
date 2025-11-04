-- ----------------------------
-- CI/CD Webhook事件相关表结构
-- ----------------------------

-- Webhook事件表
DROP TABLE IF EXISTS `webhook_event`;
CREATE TABLE `webhook_event` (
  `event_id` bigint NOT NULL AUTO_INCREMENT COMMENT '事件主键',
  `event_type` varchar(100) NOT NULL COMMENT '事件类型：push-推送，merge_request-合并请求，release-发布等',
  `event_source` varchar(50) NOT NULL COMMENT '事件来源：GITHUB- GitHub，GITLAB- GitLab，GITEE- Gitee',
  `repository_url` varchar(500) COMMENT '仓库URL',
  `branch` varchar(200) COMMENT '分支名称',
  `commit_sha` varchar(100) COMMENT '提交SHA',
  `commit_message` text COMMENT '提交信息',
  `author` varchar(100) COMMENT '提交作者',
  `tag` varchar(100) COMMENT '标签名称',
  `payload` longtext COMMENT '事件负载(JSON格式)',
  `headers` text COMMENT '请求头信息(JSON格式)',
  `signature` varchar(500) COMMENT '事件签名',
  `status` varchar(20) NOT NULL DEFAULT 'PENDING' COMMENT '处理状态：PENDING-待处理，PROCESSING-处理中，COMPLETED-已完成，FAILED-失败，SKIPPED-跳过，ARCHIVED-已归档',
  `processing_time` int COMMENT '处理耗时(毫秒)',
  `error_message` text COMMENT '错误信息',
  `triggered_execution_id` bigint COMMENT '触发的执行ID',
  `project_id` bigint COMMENT '项目ID',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `create_by` varchar(64) DEFAULT '' COMMENT '创建者',
  `update_by` varchar(64) DEFAULT '' COMMENT '更新者',
  `remark` varchar(500) DEFAULT NULL COMMENT '备注',
  PRIMARY KEY (`event_id`),
  KEY `idx_event_type` (`event_type`),
  KEY `idx_event_source` (`event_source`),
  KEY `idx_event_repository_url` (`repository_url`),
  KEY `idx_event_branch` (`branch`),
  KEY `idx_event_commit_sha` (`commit_sha`),
  KEY `idx_event_status` (`status`),
  KEY `idx_event_project_id` (`project_id`),
  KEY `idx_event_create_time` (`create_time`),
  KEY `idx_event_triggered_execution` (`triggered_execution_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='Webhook事件表';

-- Webhook事件处理历史表
DROP TABLE IF EXISTS `webhook_event_history`;
CREATE TABLE `webhook_event_history` (
  `history_id` bigint NOT NULL AUTO_INCREMENT COMMENT '历史主键',
  `event_id` bigint NOT NULL COMMENT '事件ID',
  `step` varchar(100) NOT NULL COMMENT '处理步骤',
  `status` varchar(20) NOT NULL COMMENT '步骤状态：STARTED-已开始，SUCCESS-成功，FAILED-失败',
  `message` text COMMENT '处理消息',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (`history_id`),
  KEY `idx_history_event_id` (`event_id`),
  KEY `idx_history_step` (`step`),
  KEY `idx_history_status` (`status`),
  KEY `idx_history_create_time` (`create_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='Webhook事件处理历史表';

-- Webhook事件重试关系表
DROP TABLE IF EXISTS `webhook_event_retry`;
CREATE TABLE `webhook_event_retry` (
  `retry_id` bigint NOT NULL AUTO_INCREMENT COMMENT '重试主键',
  `original_event_id` bigint NOT NULL COMMENT '原始事件ID',
  `retry_event_id` bigint NOT NULL COMMENT '重试事件ID',
  `retry_reason` varchar(500) COMMENT '重试原因',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (`retry_id`),
  UNIQUE KEY `uk_webhook_retry` (`original_event_id`, `retry_event_id`),
  KEY `idx_webhook_retry_original_id` (`original_event_id`),
  KEY `idx_webhook_retry_retry_id` (`retry_event_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='Webhook事件重试关系表';

-- 触发器配置表
DROP TABLE IF EXISTS `trigger_config`;
CREATE TABLE `trigger_config` (
  `config_id` bigint NOT NULL AUTO_INCREMENT COMMENT '配置主键',
  `project_id` bigint NOT NULL COMMENT '项目ID',
  `trigger_type` varchar(50) NOT NULL COMMENT '触发器类型',
  `config_name` varchar(100) NOT NULL COMMENT '配置名称',
  `config_json` longtext NOT NULL COMMENT '配置内容(JSON格式)',
  `is_enabled` tinyint(1) NOT NULL DEFAULT 1 COMMENT '是否启用：0-禁用，1-启用',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`config_id`),
  UNIQUE KEY `uk_trigger_config` (`project_id`, `trigger_type`, `config_name`),
  KEY `idx_trigger_project_id` (`project_id`),
  KEY `idx_trigger_type` (`trigger_type`),
  KEY `idx_trigger_enabled` (`is_enabled`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='触发器配置表';

-- ----------------------------
-- 初始化触发器配置数据
-- ----------------------------

INSERT INTO `trigger_config` (`project_id`, `trigger_type`, `config_name`, `config_json`, `is_enabled`) VALUES
(0, 'WEBHOOK', 'GitHub Push事件', '{
  "eventType": "push",
  "eventSource": "GITHUB",
  "branchFilter": ["main", "master", "develop"],
  "fileFilter": {
    "includes": ["src/**/*.java", "pom.xml", "build.gradle"],
    "excludes": ["src/test/**", "*.md"]
  },
  "autoTrigger": true,
  "signatureValidation": true,
  "secretToken": ""
}', 1),
(0, 'WEBHOOK', 'GitLab Merge Request事件', '{
  "eventType": "merge_request",
  "eventSource": "GITLAB",
  "actionFilter": ["open", "update", "merge"],
  "targetBranchFilter": ["main", "master", "develop"],
  "autoTrigger": true,
  "requireApproval": false
}', 1),
(0, 'WEBHOOK', 'Gitee Push事件', '{
  "eventType": "push",
  "eventSource": "GITEE",
  "branchFilter": ["main", "master", "develop"],
  "autoTrigger": true,
  "signatureValidation": true
}', 1),
(0, 'SCHEDULE', '每日构建', '{
  "scheduleType": "DAILY",
  "scheduleTime": "02:00",
  "timezone": "Asia/Shanghai",
  "enabledDays": ["MONDAY", "TUESDAY", "WEDNESDAY", "THURSDAY", "FRIDAY"],
  "autoTrigger": true
}', 1),
(0, 'SCHEDULE', '每周构建', '{
  "scheduleType": "WEEKLY",
  "scheduleDay": "SUNDAY",
  "scheduleTime": "03:00",
  "timezone": "Asia/Shanghai",
  "autoTrigger": true
}', 0);