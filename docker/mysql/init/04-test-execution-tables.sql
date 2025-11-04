-- ----------------------------
-- 测试执行引擎相关表 - Phase 3
-- ----------------------------

-- ----------------------------
-- 测试执行表
-- ----------------------------
DROP TABLE IF EXISTS `test_execution`;
CREATE TABLE `test_execution` (
  `execution_id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '执行ID',
  `execution_code` varchar(50) NOT NULL COMMENT '执行编号',
  `project_id` bigint(20) NOT NULL COMMENT '项目ID',
  `execution_name` varchar(200) NOT NULL COMMENT '执行名称',
  `execution_type` varchar(20) NOT NULL COMMENT '执行类型(SCHEDULE/MANUAL/BATCH/RETRY)',
  `status` varchar(20) NOT NULL DEFAULT 'PENDING' COMMENT '执行状态(PENDING/RUNNING/SUCCESS/FAILED/CANCELLED/TIMEOUT)',
  `priority` varchar(20) DEFAULT 'MEDIUM' COMMENT '优先级(LOW/MEDIUM/HIGH/URGENT)',
  `planned_start_time` datetime DEFAULT NULL COMMENT '计划开始时间',
  `actual_start_time` datetime DEFAULT NULL COMMENT '实际开始时间',
  `planned_end_time` datetime DEFAULT NULL COMMENT '计划结束时间',
  `actual_end_time` datetime DEFAULT NULL COMMENT '实际结束时间',
  `environment_id` bigint(20) DEFAULT NULL COMMENT '执行环境ID',
  `execution_config` text COMMENT '执行配置(JSON格式)',
  `total_cases` int(11) DEFAULT 0 COMMENT '总用例数',
  `success_cases` int(11) DEFAULT 0 COMMENT '成功用例数',
  `failed_cases` int(11) DEFAULT 0 COMMENT '失败用例数',
  `skipped_cases` int(11) DEFAULT 0 COMMENT '跳过用例数',
  `progress` int(3) DEFAULT 0 COMMENT '执行进度(0-100)',
  `error_message` text COMMENT '错误信息',
  `log_path` varchar(500) DEFAULT NULL COMMENT '执行日志路径',
  `report_path` varchar(500) DEFAULT NULL COMMENT '报告文件路径',
  `executor_id` bigint(20) DEFAULT NULL COMMENT '执行人ID',
  `create_by` varchar(64) DEFAULT '' COMMENT '创建者',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_by` varchar(64) DEFAULT '' COMMENT '更新者',
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `remark` varchar(500) DEFAULT NULL COMMENT '备注',
  PRIMARY KEY (`execution_id`),
  UNIQUE KEY `uk_execution_code` (`execution_code`),
  KEY `idx_project_id` (`project_id`),
  KEY `idx_status` (`status`),
  KEY `idx_executor_id` (`executor_id`),
  KEY `idx_create_time` (`create_time`),
  KEY `idx_execution_type` (`execution_type`)
) ENGINE=InnoDB AUTO_INCREMENT=1 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='测试执行表';

-- ----------------------------
-- 测试执行用例详情表
-- ----------------------------
DROP TABLE IF EXISTS `test_execution_case`;
CREATE TABLE `test_execution_case` (
  `execution_case_id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '执行用例ID',
  `execution_id` bigint(20) NOT NULL COMMENT '执行ID',
  `case_id` bigint(20) NOT NULL COMMENT '用例ID',
  `status` varchar(20) NOT NULL DEFAULT 'PENDING' COMMENT '执行状态(PENDING/RUNNING/SUCCESS/FAILED/SKIPPED/TIMEOUT)',
  `start_time` datetime DEFAULT NULL COMMENT '开始执行时间',
  `end_time` datetime DEFAULT NULL COMMENT '结束执行时间',
  `duration` bigint(20) DEFAULT NULL COMMENT '执行时长(毫秒)',
  `result` text COMMENT '执行结果',
  `error_message` text COMMENT '错误信息',
  `step_results` longtext COMMENT '执行步骤结果(JSON格式)',
  `assertion_results` text COMMENT '断言结果(JSON格式)',
  `screenshot_path` varchar(500) DEFAULT NULL COMMENT '截图路径',
  `log_path` varchar(500) DEFAULT NULL COMMENT '日志路径',
  `retry_count` int(3) DEFAULT 0 COMMENT '重试次数',
  `executor_node` varchar(100) DEFAULT NULL COMMENT '执行机/节点',
  `thread_id` varchar(50) DEFAULT NULL COMMENT '执行线程ID',
  `create_by` varchar(64) DEFAULT '' COMMENT '创建者',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_by` varchar(64) DEFAULT '' COMMENT '更新者',
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`execution_case_id`),
  KEY `idx_execution_id` (`execution_id`),
  KEY `idx_case_id` (`case_id`),
  KEY `idx_status` (`status`),
  KEY `idx_start_time` (`start_time`),
  CONSTRAINT `fk_execution_case_execution` FOREIGN KEY (`execution_id`) REFERENCES `test_execution` (`execution_id`) ON DELETE CASCADE,
  CONSTRAINT `fk_execution_case_case` FOREIGN KEY (`case_id`) REFERENCES `test_case` (`case_id`) ON DELETE CASCADE
) ENGINE=InnoDB AUTO_INCREMENT=1 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='测试执行用例详情表';

-- ----------------------------
-- 测试调度表
-- ----------------------------
DROP TABLE IF EXISTS `test_schedule`;
CREATE TABLE `test_schedule` (
  `schedule_id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '调度ID',
  `schedule_name` varchar(200) NOT NULL COMMENT '调度名称',
  `project_id` bigint(20) NOT NULL COMMENT '项目ID',
  `schedule_type` varchar(20) NOT NULL COMMENT '调度类型(CRON/FIXED_RATE/FIXED_DELAY)',
  `cron_expression` varchar(100) DEFAULT NULL COMMENT '调度表达式',
  `fixed_rate` bigint(20) DEFAULT NULL COMMENT '固定间隔(毫秒)',
  `fixed_delay` bigint(20) DEFAULT NULL COMMENT '固定延迟(毫秒)',
  `status` varchar(20) NOT NULL DEFAULT 'ENABLED' COMMENT '调度状态(ENABLED/DISABLED/PAUSED)',
  `description` varchar(500) DEFAULT NULL COMMENT '描述',
  `last_execute_time` datetime DEFAULT NULL COMMENT '上次执行时间',
  `next_execute_time` datetime DEFAULT NULL COMMENT '下次执行时间',
  `execute_count` int(11) DEFAULT 0 COMMENT '执行次数',
  `success_count` int(11) DEFAULT 0 COMMENT '成功次数',
  `failure_count` int(11) DEFAULT 0 COMMENT '失败次数',
  `max_execute_count` int(11) DEFAULT NULL COMMENT '最大执行次数',
  `start_time` datetime DEFAULT NULL COMMENT '开始时间',
  `end_time` datetime DEFAULT NULL COMMENT '结束时间',
  `execution_config` text COMMENT '执行配置(JSON格式)',
  `case_ids` text COMMENT '用例ID列表(逗号分隔)',
  `category_ids` text COMMENT '分类ID列表(逗号分隔)',
  `environment_id` bigint(20) DEFAULT NULL COMMENT '环境ID',
  `executor_id` bigint(20) DEFAULT NULL COMMENT '执行人ID',
  `create_by` varchar(64) DEFAULT '' COMMENT '创建者',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_by` varchar(64) DEFAULT '' COMMENT '更新者',
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `remark` varchar(500) DEFAULT NULL COMMENT '备注',
  PRIMARY KEY (`schedule_id`),
  KEY `idx_project_id` (`project_id`),
  KEY `idx_status` (`status`),
  KEY `idx_next_execute_time` (`next_execute_time`),
  KEY `idx_schedule_type` (`schedule_type`),
  KEY `idx_executor_id` (`executor_id`)
) ENGINE=InnoDB AUTO_INCREMENT=1 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='测试调度表';

-- ----------------------------
-- 插入测试环境数据
-- ----------------------------
INSERT INTO `test_environment` (`environment_name`, `environment_code`, `environment_type`, `base_url`, `description`, `status`, `create_by`, `create_time`) VALUES
('开发环境', 'DEV', 'WEB', 'http://localhost:8080', '开发测试环境', '1', 'admin', NOW()),
('测试环境', 'TEST', 'WEB', 'http://test.example.com', '功能测试环境', '1', 'admin', NOW()),
('预生产环境', 'PRE', 'WEB', 'http://pre.example.com', '预生产验证环境', '1', 'admin', NOW()),
('生产环境', 'PROD', 'WEB', 'https://www.example.com', '生产环境(仅监控)', '1', 'admin', NOW());

-- ----------------------------
-- 创建索引优化查询性能
-- ----------------------------
-- 执行表索引
CREATE INDEX `idx_execution_status_type` ON `test_execution` (`status`, `execution_type`);
CREATE INDEX `idx_execution_project_time` ON `test_execution` (`project_id`, `create_time`);
CREATE INDEX `idx_execution_executor_status` ON `test_execution` (`executor_id`, `status`);

-- 执行用例表索引
CREATE INDEX `idx_execution_case_status_time` ON `test_execution_case` (`execution_id`, `status`, `start_time`);
CREATE INDEX `idx_execution_case_case_status` ON `test_execution_case` (`case_id`, `status`);

-- 调度表索引
CREATE INDEX `idx_schedule_status_next_time` ON `test_schedule` (`status`, `next_execute_time`);
CREATE INDEX `idx_schedule_project_status` ON `test_schedule` (`project_id`, `status`);

-- ----------------------------
-- 插入示例调度任务
-- ----------------------------
INSERT INTO `test_schedule` (`schedule_name`, `project_id`, `schedule_type`, `cron_expression`, `status`, `description`, `execution_config`, `case_ids`, `environment_id`, `executor_id`, `create_by`, `create_time`) VALUES
('每日冒烟测试', 1, 'CRON', '0 0 9 * * ?', 'ENABLED', '每天上午9点执行冒烟测试', '{"parallelCount": 3, "timeoutMinutes": 30, "continueOnFailure": true}', '1,2,3', 1, 1, 'admin', NOW()),
('每周回归测试', 1, 'CRON', '0 0 2 ? * SUN', 'ENABLED', '每周日凌晨2点执行回归测试', '{"parallelCount": 5, "timeoutMinutes": 120, "continueOnFailure": false}', '4,5,6,7,8', 1, 1, 'admin', NOW());