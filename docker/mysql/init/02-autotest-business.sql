-- 自动化测试平台业务表初始化脚本

SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

-- ----------------------------
-- 测试项目表
-- ----------------------------
DROP TABLE IF EXISTS `test_project`;
CREATE TABLE `test_project`  (
  `project_id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '项目ID',
  `project_code` varchar(50) NOT NULL COMMENT '项目编码',
  `project_name` varchar(200) NOT NULL COMMENT '项目名称',
  `description` varchar(500) DEFAULT NULL COMMENT '项目描述',
  `status` char(1) NOT NULL DEFAULT '0' COMMENT '项目状态(0正常 1停用)',
  `git_repo_url` varchar(500) DEFAULT NULL COMMENT 'Git仓库地址',
  `git_branch` varchar(100) DEFAULT 'main' COMMENT 'Git分支',
  `git_access_token` varchar(500) DEFAULT NULL COMMENT 'Git访问Token(加密)',
  `owner_id` bigint(20) NOT NULL COMMENT '项目负责人ID',
  `create_by` varchar(64) DEFAULT '' COMMENT '创建者',
  `create_time` datetime DEFAULT NULL COMMENT '创建时间',
  `update_by` varchar(64) DEFAULT '' COMMENT '更新者',
  `update_time` datetime DEFAULT NULL COMMENT '更新时间',
  `remark` varchar(500) DEFAULT NULL COMMENT '备注',
  `del_flag` char(1) DEFAULT '0' COMMENT '删除标志(0代表存在 2代表删除)',
  PRIMARY KEY (`project_id`) USING BTREE,
  UNIQUE INDEX `uk_project_code`(`project_code`) USING BTREE,
  INDEX `idx_owner_id`(`owner_id`) USING BTREE,
  INDEX `idx_create_time`(`create_time`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 1 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci COMMENT = '测试项目表' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- 项目成员表
-- ----------------------------
DROP TABLE IF EXISTS `test_project_member`;
CREATE TABLE `test_project_member`  (
  `member_id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '成员ID',
  `project_id` bigint(20) NOT NULL COMMENT '项目ID',
  `user_id` bigint(20) NOT NULL COMMENT '用户ID',
  `role_type` varchar(20) NOT NULL COMMENT '角色类型(OWNER,ADMIN,MEMBER,VIEWER)',
  `permissions` varchar(500) DEFAULT NULL COMMENT '权限列表(JSON格式)',
  `join_time` datetime DEFAULT NULL COMMENT '加入时间',
  `create_by` varchar(64) DEFAULT '' COMMENT '创建者',
  `create_time` datetime DEFAULT NULL COMMENT '创建时间',
  `update_by` varchar(64) DEFAULT '' COMMENT '更新者',
  `update_time` datetime DEFAULT NULL COMMENT '更新时间',
  PRIMARY KEY (`member_id`) USING BTREE,
  UNIQUE INDEX `uk_project_user`(`project_id`, `user_id`) USING BTREE,
  INDEX `idx_user_id`(`user_id`) USING BTREE,
  CONSTRAINT `fk_member_project` FOREIGN KEY (`project_id`) REFERENCES `test_project` (`project_id`) ON DELETE CASCADE,
  CONSTRAINT `fk_member_user` FOREIGN KEY (`user_id`) REFERENCES `sys_user` (`user_id`) ON DELETE CASCADE
) ENGINE = InnoDB AUTO_INCREMENT = 1 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci COMMENT = '项目成员表' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- 测试环境表
-- ----------------------------
DROP TABLE IF EXISTS `test_environment`;
CREATE TABLE `test_environment`  (
  `env_id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '环境ID',
  `project_id` bigint(20) NOT NULL COMMENT '项目ID',
  `env_name` varchar(100) NOT NULL COMMENT '环境名称',
  `env_type` varchar(20) NOT NULL COMMENT '环境类型(DEV,TEST,STAGING,PROD)',
  `base_url` varchar(500) DEFAULT NULL COMMENT '基础URL',
  `db_config` text COMMENT '数据库配置(JSON加密)',
  `api_config` text COMMENT 'API配置(JSON)',
  `custom_config` text COMMENT '自定义配置(JSON)',
  `is_default` char(1) DEFAULT '0' COMMENT '是否默认环境',
  `sort_order` int(11) DEFAULT 0 COMMENT '排序',
  `create_by` varchar(64) DEFAULT '' COMMENT '创建者',
  `create_time` datetime DEFAULT NULL COMMENT '创建时间',
  `update_by` varchar(64) DEFAULT '' COMMENT '更新者',
  `update_time` datetime DEFAULT NULL COMMENT '更新时间',
  PRIMARY KEY (`env_id`) USING BTREE,
  INDEX `idx_project_id`(`project_id`) USING BTREE,
  CONSTRAINT `fk_env_project` FOREIGN KEY (`project_id`) REFERENCES `test_project` (`project_id`) ON DELETE CASCADE
) ENGINE = InnoDB AUTO_INCREMENT = 1 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci COMMENT = '测试环境表' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- 测试用例分类表
-- ----------------------------
DROP TABLE IF EXISTS `test_case_category`;
CREATE TABLE `test_case_category`  (
  `category_id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '分类ID',
  `project_id` bigint(20) NOT NULL COMMENT '项目ID',
  `category_name` varchar(100) NOT NULL COMMENT '分类名称',
  `parent_id` bigint(20) DEFAULT 0 COMMENT '父分类ID',
  `ancestors` varchar(500) DEFAULT '' COMMENT '祖级列表',
  `order_num` int(4) DEFAULT 0 COMMENT '显示顺序',
  `leader` varchar(20) DEFAULT NULL COMMENT '负责人',
  `create_by` varchar(64) DEFAULT '' COMMENT '创建者',
  `create_time` datetime DEFAULT NULL COMMENT '创建时间',
  `update_by` varchar(64) DEFAULT '' COMMENT '更新者',
  `update_time` datetime DEFAULT NULL COMMENT '更新时间',
  PRIMARY KEY (`category_id`) USING BTREE,
  INDEX `idx_project_id`(`project_id`) USING BTREE,
  INDEX `idx_parent_id`(`parent_id`) USING BTREE,
  CONSTRAINT `fk_category_project` FOREIGN KEY (`project_id`) REFERENCES `test_project` (`project_id`) ON DELETE CASCADE
) ENGINE = InnoDB AUTO_INCREMENT = 1 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci COMMENT = '测试用例分类表' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- 测试用例表
-- ----------------------------
DROP TABLE IF EXISTS `test_case`;
CREATE TABLE `test_case`  (
  `case_id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '用例ID',
  `project_id` bigint(20) NOT NULL COMMENT '项目ID',
  `category_id` bigint(20) NOT NULL COMMENT '分类ID',
  `case_title` varchar(200) NOT NULL COMMENT '用例标题',
  `case_code` varchar(50) NOT NULL COMMENT '用例编码',
  `case_type` varchar(20) NOT NULL COMMENT '用例类型(WEB_UI,API,UNIT,PERFORMANCE)',
  `priority` varchar(10) NOT NULL DEFAULT 'MEDIUM' COMMENT '优先级(HIGH,MEDIUM,LOW)',
  `preconditions` text COMMENT '前置条件',
  `test_steps` longtext COMMENT '测试步骤(JSON)',
  `expected_result` text COMMENT '期望结果',
  `test_data_source` varchar(200) DEFAULT NULL COMMENT '测试数据源',
  `tags` varchar(500) DEFAULT NULL COMMENT '标签(逗号分隔)',
  `status` varchar(20) NOT NULL DEFAULT 'DRAFT' COMMENT '状态(DRAFT,ACTIVE,DEPRECATED)',
  `version` int(11) DEFAULT 1 COMMENT '版本号',
  `author_id` bigint(20) NOT NULL COMMENT '作者ID',
  `reviewer_id` bigint(20) DEFAULT NULL COMMENT '审核人ID',
  `review_time` datetime DEFAULT NULL COMMENT '审核时间',
  `create_by` varchar(64) DEFAULT '' COMMENT '创建者',
  `create_time` datetime DEFAULT NULL COMMENT '创建时间',
  `update_by` varchar(64) DEFAULT '' COMMENT '更新者',
  `update_time` datetime DEFAULT NULL COMMENT '更新时间',
  `del_flag` char(1) DEFAULT '0' COMMENT '删除标志',
  PRIMARY KEY (`case_id`) USING BTREE,
  UNIQUE INDEX `uk_case_code`(`project_id`, `case_code`) USING BTREE,
  INDEX `idx_project_id`(`project_id`) USING BTREE,
  INDEX `idx_category_id`(`category_id`) USING BTREE,
  INDEX `idx_author_id`(`author_id`) USING BTREE,
  INDEX `idx_status`(`status`) USING BTREE,
  CONSTRAINT `fk_case_project` FOREIGN KEY (`project_id`) REFERENCES `test_project` (`project_id`) ON DELETE CASCADE,
  CONSTRAINT `fk_case_category` FOREIGN KEY (`category_id`) REFERENCES `test_case_category` (`category_id`) ON DELETE CASCADE
) ENGINE = InnoDB AUTO_INCREMENT = 1 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci COMMENT = '测试用例表' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- 测试用例版本历史表
-- ----------------------------
DROP TABLE IF EXISTS `test_case_version`;
CREATE TABLE `test_case_version`  (
  `version_id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '版本ID',
  `case_id` bigint(20) NOT NULL COMMENT '用例ID',
  `version_number` int(11) NOT NULL COMMENT '版本号',
  `case_title` varchar(200) NOT NULL COMMENT '用例标题',
  `test_steps` longtext COMMENT '测试步骤(JSON)',
  `expected_result` text COMMENT '期望结果',
  `change_log` varchar(500) DEFAULT NULL COMMENT '变更日志',
  `create_by` varchar(64) DEFAULT '' COMMENT '创建者',
  `create_time` datetime DEFAULT NULL COMMENT '创建时间',
  PRIMARY KEY (`version_id`) USING BTREE,
  UNIQUE INDEX `uk_case_version`(`case_id`, `version_number`) USING BTREE,
  INDEX `idx_case_id`(`case_id`) USING BTREE,
  CONSTRAINT `fk_version_case` FOREIGN KEY (`case_id`) REFERENCES `test_case` (`case_id`) ON DELETE CASCADE
) ENGINE = InnoDB AUTO_INCREMENT = 1 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci COMMENT = '测试用例版本历史表' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- 测试数据源表
-- ----------------------------
DROP TABLE IF EXISTS `test_data_source`;
CREATE TABLE `test_data_source`  (
  `data_id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '数据ID',
  `project_id` bigint(20) NOT NULL COMMENT '项目ID',
  `data_name` varchar(100) NOT NULL COMMENT '数据源名称',
  `data_type` varchar(20) NOT NULL COMMENT '数据类型(FILE,DATABASE,API)',
  `file_path` varchar(500) DEFAULT NULL COMMENT '文件路径',
  `file_name` varchar(200) DEFAULT NULL COMMENT '文件名',
  `db_config` text COMMENT '数据库配置(JSON)',
  `api_config` text COMMENT 'API配置(JSON)',
  `data_schema` text COMMENT '数据结构(JSON)',
  `record_count` int(11) DEFAULT 0 COMMENT '记录数',
  `create_by` varchar(64) DEFAULT '' COMMENT '创建者',
  `create_time` datetime DEFAULT NULL COMMENT '创建时间',
  `update_by` varchar(64) DEFAULT '' COMMENT '更新者',
  `update_time` datetime DEFAULT NULL COMMENT '更新时间',
  PRIMARY KEY (`data_id`) USING BTREE,
  INDEX `idx_project_id`(`project_id`) USING BTREE,
  CONSTRAINT `fk_data_project` FOREIGN KEY (`project_id`) REFERENCES `test_project` (`project_id`) ON DELETE CASCADE
) ENGINE = InnoDB AUTO_INCREMENT = 1 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci COMMENT = '测试数据源表' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- 测试执行表
-- ----------------------------
DROP TABLE IF EXISTS `test_execution`;
CREATE TABLE `test_execution`  (
  `execution_id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '执行ID',
  `execution_name` varchar(200) NOT NULL COMMENT '执行名称',
  `project_id` bigint(20) NOT NULL COMMENT '项目ID',
  `env_id` bigint(20) NOT NULL COMMENT '环境ID',
  `execution_type` varchar(20) NOT NULL COMMENT '执行类型(MANUAL,SCHEDULED,CICD)',
  `trigger_source` varchar(50) DEFAULT NULL COMMENT '触发源',
  `total_cases` int(11) NOT NULL DEFAULT 0 COMMENT '总用例数',
  `passed_cases` int(11) NOT NULL DEFAULT 0 COMMENT '通过用例数',
  `failed_cases` int(11) NOT NULL DEFAULT 0 COMMENT '失败用例数',
  `skipped_cases` int(11) NOT NULL DEFAULT 0 COMMENT '跳过用例数',
  `execution_status` varchar(20) NOT NULL DEFAULT 'PENDING' COMMENT '执行状态(PENDING,RUNNING,COMPLETED,FAILED,CANCELLED)',
  `start_time` datetime DEFAULT NULL COMMENT '开始时间',
  `end_time` datetime DEFAULT NULL COMMENT '结束时间',
  `duration` bigint(20) DEFAULT NULL COMMENT '执行时长(秒)',
  `trigger_by` bigint(20) NOT NULL COMMENT '触发人ID',
  `create_by` varchar(64) DEFAULT '' COMMENT '创建者',
  `create_time` datetime DEFAULT NULL COMMENT '创建时间',
  `update_by` varchar(64) DEFAULT '' COMMENT '更新者',
  `update_time` datetime DEFAULT NULL COMMENT '更新时间',
  PRIMARY KEY (`execution_id`) USING BTREE,
  INDEX `idx_project_id`(`project_id`) USING BTREE,
  INDEX `idx_env_id`(`env_id`) USING BTREE,
  INDEX `idx_trigger_by`(`trigger_by`) USING BTREE,
  INDEX `idx_create_time`(`create_time`) USING BTREE,
  CONSTRAINT `fk_execution_project` FOREIGN KEY (`project_id`) REFERENCES `test_project` (`project_id`) ON DELETE CASCADE,
  CONSTRAINT `fk_execution_env` FOREIGN KEY (`env_id`) REFERENCES `test_environment` (`env_id`) ON DELETE CASCADE
) ENGINE = InnoDB AUTO_INCREMENT = 1 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci COMMENT = '测试执行表' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- 执行用例详情表
-- ----------------------------
DROP TABLE IF EXISTS `test_execution_case`;
CREATE TABLE `test_execution_case`  (
  `execution_case_id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '执行用例ID',
  `execution_id` bigint(20) NOT NULL COMMENT '执行ID',
  `case_id` bigint(20) NOT NULL COMMENT '用例ID',
  `case_version` int(11) DEFAULT 1 COMMENT '用例版本',
  `execution_status` varchar(20) NOT NULL DEFAULT 'PENDING' COMMENT '执行状态(PENDING,RUNNING,PASSED,FAILED,SKIPPED,ERROR)',
  `start_time` datetime DEFAULT NULL COMMENT '开始时间',
  `end_time` datetime DEFAULT NULL COMMENT '结束时间',
  `duration` bigint(20) DEFAULT NULL COMMENT '执行时长(毫秒)',
  `error_message` text COMMENT '错误信息',
  `stack_trace` text COMMENT '错误堆栈',
  `execution_log` longtext COMMENT '执行日志',
  `screenshot_path` varchar(500) DEFAULT NULL COMMENT '截图路径',
  `step_results` longtext COMMENT '步骤执行结果(JSON)',
  `retry_count` int(11) DEFAULT 0 COMMENT '重试次数',
  `executor_node` varchar(100) DEFAULT NULL COMMENT '执行节点',
  `create_time` datetime DEFAULT NULL COMMENT '创建时间',
  PRIMARY KEY (`execution_case_id`) USING BTREE,
  INDEX `idx_execution_id`(`execution_id`) USING BTREE,
  INDEX `idx_case_id`(`case_id`) USING BTREE,
  INDEX `idx_status`(`execution_status`) USING BTREE,
  CONSTRAINT `fk_exec_case_execution` FOREIGN KEY (`execution_id`) REFERENCES `test_execution` (`execution_id`) ON DELETE CASCADE,
  CONSTRAINT `fk_exec_case_case` FOREIGN KEY (`case_id`) REFERENCES `test_case` (`case_id`) ON DELETE CASCADE
) ENGINE = InnoDB AUTO_INCREMENT = 1 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci COMMENT = '执行用例详情表' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- 测试调度表
-- ----------------------------
DROP TABLE IF EXISTS `test_schedule`;
CREATE TABLE `test_schedule`  (
  `schedule_id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '调度ID',
  `schedule_name` varchar(200) NOT NULL COMMENT '调度名称',
  `project_id` bigint(20) NOT NULL COMMENT '项目ID',
  `cron_expression` varchar(100) NOT NULL COMMENT 'Cron表达式',
  `case_ids` text COMMENT '用例ID列表(JSON)',
  `env_id` bigint(20) NOT NULL COMMENT '执行环境ID',
  `schedule_status` varchar(20) NOT NULL DEFAULT 'ACTIVE' COMMENT '调度状态(ACTIVE,INACTIVE,PAUSED)',
  `last_execution_time` datetime DEFAULT NULL COMMENT '最后执行时间',
  `next_execution_time` datetime DEFAULT NULL COMMENT '下次执行时间',
  `execution_count` int(11) DEFAULT 0 COMMENT '执行次数',
  `success_count` int(11) DEFAULT 0 COMMENT '成功次数',
  `failure_count` int(11) DEFAULT 0 COMMENT '失败次数',
  `notification_config` text COMMENT '通知配置(JSON)',
  `create_by` varchar(64) DEFAULT '' COMMENT '创建者',
  `create_time` datetime DEFAULT NULL COMMENT '创建时间',
  `update_by` varchar(64) DEFAULT '' COMMENT '更新者',
  `update_time` datetime DEFAULT NULL COMMENT '更新时间',
  PRIMARY KEY (`schedule_id`) USING BTREE,
  INDEX `idx_project_id`(`project_id`) USING BTREE,
  INDEX `idx_next_time`(`next_execution_time`) USING BTREE,
  CONSTRAINT `fk_schedule_project` FOREIGN KEY (`project_id`) REFERENCES `test_project` (`project_id`) ON DELETE CASCADE
) ENGINE = InnoDB AUTO_INCREMENT = 1 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci COMMENT = '测试调度表' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- 测试报告表
-- ----------------------------
DROP TABLE IF EXISTS `test_report`;
CREATE TABLE `test_report`  (
  `report_id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '报告ID',
  `execution_id` bigint(20) NOT NULL COMMENT '执行ID',
  `report_name` varchar(200) NOT NULL COMMENT '报告名称',
  `report_type` varchar(20) NOT NULL DEFAULT 'SUMMARY' COMMENT '报告类型(SUMMARY,DETAILED,TREND)',
  `summary_data` longtext COMMENT '汇总数据(JSON)',
  `chart_data` longtext COMMENT '图表数据(JSON)',
  `file_path` varchar(500) DEFAULT NULL COMMENT '报告文件路径',
  `generate_time` datetime DEFAULT NULL COMMENT '生成时间',
  `template_id` bigint(20) DEFAULT NULL COMMENT '模板ID',
  `create_by` varchar(64) DEFAULT '' COMMENT '创建者',
  `create_time` datetime DEFAULT NULL COMMENT '创建时间',
  PRIMARY KEY (`report_id`) USING BTREE,
  UNIQUE INDEX `uk_execution_report`(`execution_id`, `report_type`) USING BTREE,
  INDEX `idx_generate_time`(`generate_time`) USING BTREE,
  CONSTRAINT `fk_report_execution` FOREIGN KEY (`execution_id`) REFERENCES `test_execution` (`execution_id`) ON DELETE CASCADE
) ENGINE = InnoDB AUTO_INCREMENT = 1 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci COMMENT = '测试报告表' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- 创建索引优化
-- ----------------------------
-- 项目用例查询优化
CREATE INDEX `idx_project_category_status` ON `test_case` (`project_id`, `category_id`, `status`);

-- 执行记录查询优化
CREATE INDEX `idx_project_status_time` ON `test_execution` (`project_id`, `execution_status`, `create_time`);

-- 用例执行详情查询优化
CREATE INDEX `idx_execution_status_time` ON `test_execution_case` (`execution_id`, `execution_status`, `start_time`);

SET FOREIGN_KEY_CHECKS = 1;