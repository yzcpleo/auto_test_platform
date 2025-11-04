-- 基础数据初始化脚本

SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

-- ----------------------------
-- 用户表数据
-- ----------------------------
INSERT INTO `sys_user` VALUES (1, 'admin', '超级管理员', '00', 'admin@ruoyi.vip', '15888888888', '1', '', '$2a$10$7JB720yubVSZvUI0rEqK/.VqGOZTH.ulu33dHOiBE8ByOhJIrdAu2', '0', '0', '127.0.0.1', '2024-01-01 12:00:00', 'admin', '2024-01-01 12:00:00', '', NULL, '管理员');
INSERT INTO `sys_user` VALUES (2, 'ry', '测试员', '00', 'ry@qq.com', '15666666666', '1', '', '$2a$10$7JB720yubVSZvUI0rEqK/.VqGOZTH.ulu33dHOiBE8ByOhJIrdAu2', '0', '0', '127.0.0.1', '2024-01-01 12:00:00', 'admin', '2024-01-01 12:00:00', '', NULL, '测试员');

-- ----------------------------
-- 岗位信息表数据
-- ----------------------------
INSERT INTO `sys_post` VALUES (1, 'ceo', '董事长', 1, '0', 'admin', '2024-01-01 12:00:00', '', NULL, '');
INSERT INTO `sys_post` VALUES (2, 'se', '项目经理', 2, '0', 'admin', '2024-01-01 12:00:00', '', NULL, '');
INSERT INTO `sys_post` VALUES (3, 'hr', '人力资源', 3, '0', 'admin', '2024-01-01 12:00:00', '', NULL, '');
INSERT INTO `sys_post` VALUES (4, 'user', '普通员工', 4, '0', 'admin', '2024-01-01 12:00:00', '', NULL, '');

-- ----------------------------
-- 角色信息表数据
-- ----------------------------
INSERT INTO `sys_role` VALUES (1, '超级管理员', 'admin', 1, '1', 1, 1, '0', '0', 'admin', '2024-01-01 12:00:00', '', NULL, '超级管理员');
INSERT INTO `sys_role` VALUES (2, '普通角色', 'common', 2, '2', 1, 1, '0', '0', 'admin', '2024-01-01 12:00:00', '', NULL, '普通角色');

-- ----------------------------
-- 用户和角色关联表数据
-- ----------------------------
INSERT INTO `sys_user_role` VALUES (1, 1);
INSERT INTO `sys_user_role` VALUES (2, 2);

-- ----------------------------
-- 部门表数据
-- ----------------------------
INSERT INTO `sys_dept` VALUES (1, 0, '0', '若依科技', 0, '若依', '15888888888', 'ry@qq.com', '0', '0', 'admin', '2024-01-01 12:00:00', '', NULL);
INSERT INTO `sys_dept` VALUES (2, 1, '0,1', '深圳总公司', 1, '若依', '15888888888', 'ry@qq.com', '0', '0', 'admin', '2024-01-01 12:00:00', '', NULL);
INSERT INTO `sys_dept` VALUES (3, 2, '0,1,2', '研发部门', 1, '若依', '15888888888', 'ry@qq.com', '0', '0', 'admin', '2024-01-01 12:00:00', '', NULL);
INSERT INTO `sys_dept` VALUES (4, 2, '0,1,2', '市场部门', 2, '若依', '15888888888', 'ry@qq.com', '0', '0', 'admin', '2024-01-01 12:00:00', '', NULL);
INSERT INTO `sys_dept` VALUES (5, 2, '0,1,2', '测试部门', 3, '若依', '15888888888', 'ry@qq.com', '0', '0', 'admin', '2024-01-01 12:00:00', '', NULL);

-- ----------------------------
-- 字典类型表数据
-- ----------------------------
INSERT INTO `sys_dict_type` VALUES (1, '用户性别', 'sys_user_sex', '0', 'admin', '2024-01-01 12:00:00', '', '用户性别列表');
INSERT INTO `sys_dict_type` VALUES (2, '菜单状态', 'sys_show_hide', '0', 'admin', '2024-01-01 12:00:00', '', '菜单状态列表');
INSERT INTO `sys_dict_type` VALUES (3, '系统开关', 'sys_normal_disable', '0', 'admin', '2024-01-01 12:00:00', '', '系统开关列表');
INSERT INTO `sys_dict_type` VALUES (4, '任务状态', 'sys_job_status', '0', 'admin', '2024-01-01 12:00:00', '', '任务状态列表');
INSERT INTO `sys_dict_type` VALUES (5, '任务分组', 'sys_job_group', '0', 'admin', '2024-01-01 12:00:00', '', '任务分组列表');
INSERT INTO `sys_dict_type` VALUES (6, '系统是否', 'sys_yes_no', '0', 'admin', '2024-01-01 12:00:00', '', '系统是否列表');
INSERT INTO `sys_dict_type` VALUES (7, '通知类型', 'sys_notice_type', '0', 'admin', '2024-01-01 12:00:00', '', '通知类型列表');
INSERT INTO `sys_dict_type` VALUES (8, '通知状态', 'sys_notice_status', '0', 'admin', '2024-01-01 12:00:00', '', '通知状态列表');
INSERT INTO `sys_dict_type` VALUES (9, '操作类型', 'sys_oper_type', '0', 'admin', '2024-01-01 12:00:00', '', '操作类型列表');
INSERT INTO `sys_dict_type` VALUES (10, '系统状态', 'sys_common_status', '0', 'admin', '2024-01-01 12:00:00', '', '登录状态列表');
INSERT INTO `sys_dict_type` VALUES (100, '测试用例类型', 'test_case_type', '0', 'admin', '2024-01-01 12:00:00', '', '测试用例类型列表');
INSERT INTO `sys_dict_type` VALUES (101, '测试用例优先级', 'test_case_priority', '0', 'admin', '2024-01-01 12:00:00', '', '测试用例优先级列表');
INSERT INTO `sys_dict_type` VALUES (102, '测试用例状态', 'test_case_status', '0', 'admin', '2024-01-01 12:00:00', '', '测试用例状态列表');
INSERT INTO `sys_dict_type` VALUES (103, '执行状态', 'execution_status', '0', 'admin', '2024-01-01 12:00:00', '', '执行状态列表');
INSERT INTO `sys_dict_type` VALUES (104, '项目角色类型', 'project_role_type', '0', 'admin', '2024-01-01 12:00:00', '', '项目角色类型列表');
INSERT INTO `sys_dict_type` VALUES (105, '环境类型', 'environment_type', '0', 'admin', '2024-01-01 12:00:00', '', '环境类型列表');

-- ----------------------------
-- 字典数据表数据
-- ----------------------------
INSERT INTO `sys_dict_data` VALUES (1, 1, '男', '0', 'sys_user_sex', '', 'primary', 'Y', '0', 'admin', '2024-01-01 12:00:00', '', NULL, '性别男');
INSERT INTO `sys_dict_data` VALUES (2, 2, '女', '1', 'sys_user_sex', '', 'danger', 'N', '0', 'admin', '2024-01-01 12:00:00', '', NULL, '性别女');
INSERT INTO `sys_dict_data` VALUES (3, 3, '未知', '2', 'sys_user_sex', '', 'info', 'N', '0', 'admin', '2024-01-01 12:00:00', '', NULL, '性别未知');
INSERT INTO `sys_dict_data` VALUES (4, 1, '显示', '0', 'sys_show_hide', '', 'primary', 'Y', '0', 'admin', '2024-01-01 12:00:00', '', NULL, '显示菜单');
INSERT INTO `sys_dict_data` VALUES (5, 2, '隐藏', '1', 'sys_show_hide', '', 'danger', 'N', '0', 'admin', '2024-01-01 12:00:00', '', NULL, '隐藏菜单');
INSERT INTO `sys_dict_data` VALUES (6, 1, '正常', '0', 'sys_normal_disable', '', 'primary', 'Y', '0', 'admin', '2024-01-01 12:00:00', '', NULL, '正常状态');
INSERT INTO `sys_dict_data` VALUES (7, 2, '停用', '1', 'sys_normal_disable', '', 'danger', 'N', '0', 'admin', '2024-01-01 12:00:00', '', NULL, '停用状态');
INSERT INTO `sys_dict_data` VALUES (8, 1, '正常', '0', 'sys_job_status', '', 'primary', 'Y', '0', 'admin', '2024-01-01 12:00:00', '', NULL, '正常状态');
INSERT INTO `sys_dict_data` VALUES (9, 2, '暂停', '1', 'sys_job_status', '', 'danger', 'N', '0', 'admin', '2024-01-01 12:00:00', '', NULL, '停用状态');
INSERT INTO `sys_dict_data` VALUES (10, 1, '默认', 'DEFAULT', 'sys_job_group', '', '', 'Y', '0', 'admin', '2024-01-01 12:00:00', '', NULL, '默认分组');
INSERT INTO `sys_dict_data` VALUES (11, 2, '系统', 'SYSTEM', 'sys_job_group', '', '', 'N', '0', 'admin', '2024-01-01 12:00:00', '', NULL, '系统分组');
INSERT INTO `sys_dict_data` VALUES (12, 1, '是', 'Y', 'sys_yes_no', '', 'primary', 'Y', '0', 'admin', '2024-01-01 12:00:00', '', NULL, '系统默认是');
INSERT INTO `sys_dict_data` VALUES (13, 2, '否', 'N', 'sys_yes_no', '', 'danger', 'N', '0', 'admin', '2024-01-01 12:00:00', '', NULL, '系统默认否');
INSERT INTO `sys_dict_data` VALUES (14, 1, '通知', '1', 'sys_notice_type', '', 'warning', 'Y', '0', 'admin', '2024-01-01 12:00:00', '', NULL, '通知');
INSERT INTO `sys_dict_data` VALUES (15, 2, '公告', '2', 'sys_notice_type', '', 'success', 'N', '0', 'admin', '2024-01-01 12:00:00', '', NULL, '公告');
INSERT INTO `sys_dict_data` VALUES (16, 1, '正常', '0', 'sys_notice_status', '', 'primary', 'Y', '0', 'admin', '2024-01-01 12:00:00', '', NULL, '正常状态');
INSERT INTO `sys_dict_data` VALUES (17, 2, '关闭', '1', 'sys_notice_status', '', 'danger', 'N', '0', 'admin', '2024-01-01 12:00:00', '', NULL, '关闭状态');
INSERT INTO `sys_dict_data` VALUES (18, 1, '新增', '1', 'sys_oper_type', '', 'info', 'N', '0', 'admin', '2024-01-01 12:00:00', '', NULL, '新增操作');
INSERT INTO `sys_dict_data` VALUES (19, 2, '修改', '2', 'sys_oper_type', '', 'info', 'N', '0', 'admin', '2024-01-01 12:00:00', '', NULL, '修改操作');
INSERT INTO `sys_dict_data` VALUES (20, 3, '删除', '3', 'sys_oper_type', '', 'danger', 'N', '0', 'admin', '2024-01-01 12:00:00', '', NULL, '删除操作');
INSERT INTO `sys_dict_data` VALUES (21, 4, '授权', '4', 'sys_oper_type', '', 'primary', 'N', '0', 'admin', '2024-01-01 12:00:00', '', NULL, '授权操作');
INSERT INTO `sys_dict_data` VALUES (22, 5, '导出', '5', 'sys_oper_type', '', 'warning', 'N', '0', 'admin', '2024-01-01 12:00:00', '', NULL, '导出操作');
INSERT INTO `sys_dict_data` VALUES (23, 6, '导入', '6', 'sys_oper_type', '', 'warning', 'N', '0', 'admin', '2024-01-01 12:00:00', '', NULL, '导入操作');
INSERT INTO `sys_dict_data` VALUES (24, 7, '强退', '7', 'sys_oper_type', '', 'danger', 'N', '0', 'admin', '2024-01-01 12:00:00', '', NULL, '强退操作');
INSERT INTO `sys_dict_data` VALUES (25, 8, '生成代码', '8', 'sys_oper_type', '', 'warning', 'N', '0', 'admin', '2024-01-01 12:00:00', '', NULL, '生成操作');
INSERT INTO `sys_dict_data` VALUES (26, 9, '清空数据', '9', 'sys_oper_type', '', 'danger', 'N', '0', 'admin', '2024-01-01 12:00:00', '', NULL, '清空操作');
INSERT INTO `sys_dict_data` VALUES (27, 1, '成功', '0', 'sys_common_status', '', 'primary', 'N', '0', 'admin', '2024-01-01 12:00:00', '', NULL, '正常状态');
INSERT INTO `sys_dict_data` VALUES (28, 2, '失败', '1', 'sys_common_status', '', 'danger', 'N', '0', 'admin', '2024-01-01 12:00:00', '', NULL, '停用状态');

-- 测试相关字典数据
INSERT INTO `sys_dict_data` VALUES (100, 1, 'Web UI测试', 'WEB_UI', 'test_case_type', '', 'primary', 'N', '0', 'admin', '2024-01-01 12:00:00', '', NULL, 'Web UI自动化测试');
INSERT INTO `sys_dict_data` VALUES (101, 2, 'API接口测试', 'API', 'test_case_type', '', 'success', 'N', '0', 'admin', '2024-01-01 12:00:00', '', NULL, 'API接口测试');
INSERT INTO `sys_dict_data` VALUES (102, 3, '单元测试', 'UNIT', 'test_case_type', '', 'info', 'N', '0', 'admin', '2024-01-01 12:00:00', '', NULL, '单元测试');
INSERT INTO `sys_dict_data` VALUES (103, 4, '性能测试', 'PERFORMANCE', 'test_case_type', '', 'warning', 'N', '0', 'admin', '2024-01-01 12:00:00', '', NULL, '性能测试');
INSERT INTO `sys_dict_data` VALUES (104, 1, '高', 'HIGH', 'test_case_priority', '', 'danger', 'N', '0', 'admin', '2024-01-01 12:00:00', '', NULL, '高优先级');
INSERT INTO `sys_dict_data` VALUES (105, 2, '中', 'MEDIUM', 'test_case_priority', '', 'warning', 'N', '0', 'admin', '2024-01-01 12:00:00', '', NULL, '中优先级');
INSERT INTO `sys_dict_data` VALUES (106, 3, '低', 'LOW', 'test_case_priority', '', 'info', 'N', '0', 'admin', '2024-01-01 12:00:00', '', NULL, '低优先级');
INSERT INTO `sys_dict_data` VALUES (107, 1, '草稿', 'DRAFT', 'test_case_status', '', 'info', 'N', '0', 'admin', '2024-01-01 12:00:00', '', NULL, '草稿状态');
INSERT INTO `sys_dict_data` VALUES (108, 2, '活跃', 'ACTIVE', 'test_case_status', '', 'success', 'N', '0', 'admin', '2024-01-01 12:00:00', '', NULL, '活跃状态');
INSERT INTO `sys_dict_data` VALUES (109, 3, '已废弃', 'DEPRECATED', 'test_case_status', '', 'danger', 'N', '0', 'admin', '2024-01-01 12:00:00', '', NULL, '已废弃状态');
INSERT INTO `sys_dict_data` VALUES (110, 1, '待执行', 'PENDING', 'execution_status', '', 'info', 'N', '0', 'admin', '2024-01-01 12:00:00', '', NULL, '待执行状态');
INSERT INTO `sys_dict_data` VALUES (111, 2, '执行中', 'RUNNING', 'execution_status', '', 'primary', 'N', '0', 'admin', '2024-01-01 12:00:00', '', NULL, '执行中状态');
INSERT INTO `sys_dict_data` VALUES (112, 3, '已完成', 'COMPLETED', 'execution_status', '', 'success', 'N', '0', 'admin', '2024-01-01 12:00:00', '', NULL, '已完成状态');
INSERT INTO `sys_dict_data` VALUES (113, 4, '失败', 'FAILED', 'execution_status', '', 'danger', 'N', '0', 'admin', '2024-01-01 12:00:00', '', NULL, '失败状态');
INSERT INTO `sys_dict_data` VALUES (114, 5, '已取消', 'CANCELLED', 'execution_status', '', 'warning', 'N', '0', 'admin', '2024-01-01 12:00:00', '', NULL, '已取消状态');
INSERT INTO `sys_dict_data` VALUES (115, 1, '项目负责人', 'OWNER', 'project_role_type', '', 'danger', 'N', '0', 'admin', '2024-01-01 12:00:00', '', NULL, '项目负责人');
INSERT INTO `sys_dict_data` VALUES (116, 2, '管理员', 'ADMIN', 'project_role_type', '', 'warning', 'N', '0', 'admin', '2024-01-01 12:00:00', '', NULL, '管理员');
INSERT INTO `sys_dict_data` VALUES (117, 3, '成员', 'MEMBER', 'project_role_type', '', 'primary', 'N', '0', 'admin', '2024-01-01 12:00:00', '', NULL, '成员');
INSERT INTO `sys_dict_data` VALUES (118, 4, '查看者', 'VIEWER', 'project_role_type', '', 'info', 'N', '0', 'admin', '2024-01-01 12:00:00', '', NULL, '查看者');
INSERT INTO `sys_dict_data` VALUES (119, 1, '开发环境', 'DEV', 'environment_type', '', 'primary', 'N', '0', 'admin', '2024-01-01 12:00:00', '', NULL, '开发环境');
INSERT INTO `sys_dict_data` VALUES (120, 2, '测试环境', 'TEST', 'environment_type', '', 'warning', 'N', '0', 'admin', '2024-01-01 12:00:00', '', NULL, '测试环境');
INSERT INTO `sys_dict_data` VALUES (121, 3, '预发布环境', 'STAGING', 'environment_type', '', 'info', 'N', '0', 'admin', '2024-01-01 12:00:00', '', NULL, '预发布环境');
INSERT INTO `sys_dict_data` VALUES (122, 4, '生产环境', 'PROD', 'environment_type', '', 'danger', 'N', '0', 'admin', '2024-01-01 12:00:00', '', NULL, '生产环境');

-- ----------------------------
-- 菜单信息表数据
-- ----------------------------
INSERT INTO `sys_menu` VALUES (1, '系统管理', 0, 1, 'system',           NULL,        '', 1, 0, 'M', '0', '0', '', 'system',           'admin', '2024-01-01 12:00:00', '', NULL, '系统管理目录');
INSERT INTO `sys_menu` VALUES (2, '系统监控', 0, 2, 'monitor',          NULL,        '', 1, 0, 'M', '0', '0', '', 'monitor',          'admin', '2024-01-01 12:00:00', '', NULL, '系统监控目录');
INSERT INTO `sys_menu` VALUES (3, '系统工具', 0, 3, 'tool',             NULL,        '', 1, 0, 'M', '0', '0', '', 'tool',             'admin', '2024-01-01 12:00:00', '', NULL, '系统工具目录');
INSERT INTO `sys_menu` VALUES (4, '测试平台', 0, 4, 'test',             NULL,        '', 1, 0, 'M', '0', '0', '', 'bug',              'admin', '2024-01-01 12:00:00', '', NULL, '测试平台目录');

-- 系统管理模块
INSERT INTO `sys_menu` VALUES (100,  '用户管理', 1,   1, 'user',       'system/user/index',        '', 1, 0, 'C', '0', '0', 'system:user:list',        'user',          'admin', '2024-01-01 12:00:00', '', NULL, '用户管理菜单');
INSERT INTO `sys_menu` VALUES (101,  '角色管理', 1,   2, 'role',       'system/role/index',        '', 1, 0, 'C', '0', '0', 'system:role:list',        'peoples',       'admin', '2024-01-01 12:00:00', '', NULL, '角色管理菜单');
INSERT INTO `sys_menu` VALUES (102,  '菜单管理', 1,   3, 'menu',       'system/menu/index',        '', 1, 0, 'C', '0', '0', 'system:menu:list',        'tree-table',    'admin', '2024-01-01 12:00:00', '', NULL, '菜单管理菜单');
INSERT INTO `sys_menu` VALUES (103,  '部门管理', 1,   4, 'dept',       'system/dept/index',        '', 1, 0, 'C', '0', '0', 'system:dept:list',        'tree',          'admin', '2024-01-01 12:00:00', '', NULL, '部门管理菜单');
INSERT INTO `sys_menu` VALUES (104,  '岗位管理', 1,   5, 'post',       'system/post/index',        '', 1, 0, 'C', '0', '0', 'system:post:list',        'post',          'admin', '2024-01-01 12:00:00', '', NULL, '岗位管理菜单');
INSERT INTO `sys_menu` VALUES (105,  '字典管理', 1,   6, 'dict',       'system/dict/index',        '', 1, 0, 'C', '0', '0', 'system:dict:list',        'dict',          'admin', '2024-01-01 12:00:00', '', NULL, '字典管理菜单');
INSERT INTO `sys_menu` VALUES (106,  '参数设置', 1,   7, 'config',     'system/config/index',       '', 1, 0, 'C', '0', '0', 'system:config:list',      'edit',          'admin', '2024-01-01 12:00:00', '', NULL, '参数设置菜单');
INSERT INTO `sys_menu` VALUES (107,  '通知公告', 1,   8, 'notice',     'system/notice/index',       '', 1, 0, 'C', '0', '0', 'system:notice:list',      'message',       'admin', '2024-01-01 12:00:00', '', NULL, '通知公告菜单');
INSERT INTO `sys_menu` VALUES (108,  '日志管理', 1,   9, 'log',        '',                         '', 1, 0, 'M', '0', '0', '',                        'log',           'admin', '2024-01-01 12:00:00', '', NULL, '日志管理菜单');
INSERT INTO `sys_menu` VALUES (109,  '在线用户', 2,   1, 'online',     'monitor/online/index',      '', 1, 0, 'C', '0', '0', 'monitor:online:list',      'online',        'admin', '2024-01-01 12:00:00', '', NULL, '在线用户菜单');
INSERT INTO `sys_menu` VALUES (110,  '定时任务', 2,   2, 'job',        'monitor/job/index',         '', 1, 0, 'C', '0', '0', 'monitor:job:list',         'job',           'admin', '2024-01-01 12:00:00', '', NULL, '定时任务菜单');
INSERT INTO `sys_menu` VALUES (111,  '数据监控', 2,   3, 'druid',      'monitor/druid/index',       '', 1, 0, 'C', '0', '0', 'monitor:druid:list',       'druid',         'admin', '2024-01-01 12:00:00', '', NULL, '数据监控菜单');
INSERT INTO `sys_menu` VALUES (112,  '服务监控', 2,   4, 'server',     'monitor/server/index',      '', 1, 0, 'C', '0', '0', 'monitor:server:list',      'server',        'admin', '2024-01-01 12:00:00', '', NULL, '服务监控菜单');
INSERT INTO `sys_menu` VALUES (113,  '缓存监控', 2,   5, 'cache',      'monitor/cache/index',       '', 1, 0, 'C', '0', '0', 'monitor:cache:list',       'redis',         'admin', '2024-01-01 12:00:00', '', NULL, '缓存监控菜单');
INSERT INTO `sys_menu` VALUES (114,  '表单构建', 3,   1, 'build',      'tool/build/index',          '', 1, 0, 'C', '0', '0', 'tool:build:list',          'build',         'admin', '2024-01-01 12:00:00', '', NULL, '表单构建菜单');
INSERT INTO `sys_menu` VALUES (115,  '代码生成', 3,   2, 'gen',        'tool/gen/index',            '', 1, 0, 'C', '0', '0', 'tool:gen:list',            'code',          'admin', '2024-01-01 12:00:00', '', NULL, '代码生成菜单');
INSERT INTO `sys_menu` VALUES (116,  '系统接口', 3,   3, 'swagger',    'tool/swagger/index',        '', 1, 0, 'C', '0', '0', 'tool:swagger:list',        'swagger',       'admin', '2024-01-01 12:00:00', '', NULL, '系统接口菜单');

-- 测试平台模块
INSERT INTO `sys_menu` VALUES (200,  '项目管理', 4,   1, 'project',    'test/project/index',         '', 1, 0, 'C', '0', '0', 'test:project:list',         'example',       'admin', '2024-01-01 12:00:00', '', NULL, '项目管理菜单');
INSERT INTO `sys_menu` VALUES (201,  '测试用例', 4,   2, 'testcase',   'test/testcase/index',        '', 1, 0, 'C', '0', '0', 'test:testcase:list',        'bug',           'admin', '2024-01-01 12:00:00', '', NULL, '测试用例菜单');
INSERT INTO `sys_menu` VALUES (202,  '测试执行', 4,   3, 'execution',  'test/execution/index',       '', 1, 0, 'C', '0', '0', 'test:execution:list',       'validCode',     'admin', '2024-01-01 12:00:00', '', NULL, '测试执行菜单');
INSERT INTO `sys_menu` VALUES (203,  '测试报告', 4,   4, 'report',     'test/report/index',          '', 1, 0, 'C', '0', '0', 'test:report:list',          'documentation', 'admin', '2024-01-01 12:00:00', '', NULL, '测试报告菜单');

-- 用户管理按钮
INSERT INTO `sys_menu` VALUES (1001, '用户查询', 100, 1,  '', '', '', 1, 0, 'F', '0', '0', 'system:user:query',          '#', 'admin', '2024-01-01 12:00:00', '', NULL, '');
INSERT INTO `sys_menu` VALUES (1002, '用户新增', 100, 2,  '', '', '', 1, 0, 'F', '0', '0', 'system:user:add',            '#', 'admin', '2024-01-01 12:00:00', '', NULL, '');
INSERT INTO `sys_menu` VALUES (1003, '用户修改', 100, 3,  '', '', '', 1, 0, 'F', '0', '0', 'system:user:edit',           '#', 'admin', '2024-01-01 12:00:00', '', NULL, '');
INSERT INTO `sys_menu` VALUES (1004, '用户删除', 100, 4,  '', '', '', 1, 0, 'F', '0', '0', 'system:user:remove',         '#', 'admin', '2024-01-01 12:00:00', '', NULL, '');
INSERT INTO `sys_menu` VALUES (1005, '用户导出', 100, 5,  '', '', '', 1, 0, 'F', '0', '0', 'system:user:export',         '#', 'admin', '2024-01-01 12:00:00', '', NULL, '');
INSERT INTO `sys_menu` VALUES (1006, '用户导入', 100, 6,  '', '', '', 1, 0, 'F', '0', '0', 'system:user:import',         '#', 'admin', '2024-01-01 12:00:00', '', NULL, '');
INSERT INTO `sys_menu` VALUES (1007, '重置密码', 100, 7,  '', '', '', 1, 0, 'F', '0', '0', 'system:user:resetPwd',       '#', 'admin', '2024-01-01 12:00:00', '', NULL, '');

-- 角色管理按钮
INSERT INTO `sys_menu` VALUES (1008, '角色查询', 101, 1,  '', '', '', 1, 0, 'F', '0', '0', 'system:role:query',          '#', 'admin', '2024-01-01 12:00:00', '', NULL, '');
INSERT INTO `sys_menu` VALUES (1009, '角色新增', 101, 2,  '', '', '', 1, 0, 'F', '0', '0', 'system:role:add',            '#', 'admin', '2024-01-01 12:00:00', '', NULL, '');
INSERT INTO `sys_menu` VALUES (1010, '角色修改', 101, 3,  '', '', '', 1, 0, 'F', '0', '0', 'system:role:edit',           '#', 'admin', '2024-01-01 12:00:00', '', NULL, '');
INSERT INTO `sys_menu` VALUES (1011, '角色删除', 101, 4,  '', '', '', 1, 0, 'F', '0', '0', 'system:role:remove',         '#', 'admin', '2024-01-01 12:00:00', '', NULL, '');
INSERT INTO `sys_menu` VALUES (1012, '角色导出', 101, 5,  '', '', '', 1, 0, 'F', '0', '0', 'system:role:export',         '#', 'admin', '2024-01-01 12:00:00', '', NULL, '');

-- 测试项目管理按钮
INSERT INTO `sys_menu` VALUES (2001, '项目查询', 200, 1,  '', '', '', 1, 0, 'F', '0', '0', 'test:project:query',         '#', 'admin', '2024-01-01 12:00:00', '', NULL, '');
INSERT INTO `sys_menu` VALUES (2002, '项目新增', 200, 2,  '', '', '', 1, 0, 'F', '0', '0', 'test:project:add',           '#', 'admin', '2024-01-01 12:00:00', '', NULL, '');
INSERT INTO `sys_menu` VALUES (2003, '项目修改', 200, 3,  '', '', '', 1, 0, 'F', '0', '0', 'test:project:edit',          '#', 'admin', '2024-01-01 12:00:00', '', NULL, '');
INSERT INTO `sys_menu` VALUES (2004, '项目删除', 200, 4,  '', '', '', 1, 0, 'F', '0', '0', 'test:project:remove',        '#', 'admin', '2024-01-01 12:00:00', '', NULL, '');
INSERT INTO `sys_menu` VALUES (2005, '项目导出', 200, 5,  '', '', '', 1, 0, 'F', '0', '0', 'test:project:export',        '#', 'admin', '2024-01-01 12:00:00', '', NULL, '');

-- 角色和菜单关联表数据
INSERT INTO `sys_role_menu` VALUES (1, 1);
INSERT INTO `sys_role_menu` VALUES (1, 2);
INSERT INTO `sys_role_menu` VALUES (1, 3);
INSERT INTO `sys_role_menu` VALUES (1, 4);
INSERT INTO `sys_role_menu` VALUES (1, 100);
INSERT INTO `sys_role_menu` VALUES (1, 101);
INSERT INTO `sys_role_menu` VALUES (1, 102);
INSERT INTO `sys_role_menu` VALUES (1, 103);
INSERT INTO `sys_role_menu` VALUES (1, 104);
INSERT INTO `sys_role_menu` VALUES (1, 105);
INSERT INTO `sys_role_menu` VALUES (1, 106);
INSERT INTO `sys_role_menu` VALUES (1, 107);
INSERT INTO `sys_role_menu` VALUES (1, 108);
INSERT INTO `sys_role_menu` VALUES (1, 109);
INSERT INTO `sys_role_menu` VALUES (1, 110);
INSERT INTO `sys_role_menu` VALUES (1, 111);
INSERT INTO `sys_role_menu` VALUES (1, 112);
INSERT INTO `sys_role_menu` VALUES (1, 113);
INSERT INTO `sys_role_menu` VALUES (1, 114);
INSERT INTO `sys_role_menu` VALUES (1, 115);
INSERT INTO `sys_role_menu` VALUES (1, 116);
INSERT INTO `sys_role_menu` VALUES (1, 200);
INSERT INTO `sys_role_menu` VALUES (1, 201);
INSERT INTO `sys_role_menu` VALUES (1, 202);
INSERT INTO `sys_role_menu` VALUES (1, 203);

-- 普通角色关联菜单
INSERT INTO `sys_role_menu` VALUES (2, 200);
INSERT INTO `sys_role_menu` VALUES (2, 201);
INSERT INTO `sys_role_menu` VALUES (2, 202);
INSERT INTO `sys_role_menu` VALUES (2, 203);

SET FOREIGN_KEY_CHECKS = 1;