# Phase 1 开发任务详细清单

## 阶段目标
完成基础框架搭建，包括若依环境配置、数据库初始化、项目管理和基础权限功能。

**时间**: Week 1-2 (10个工作日)
**团队**: 全员参与

---

## Week 1 任务清单

### Day 1: 若依框架部署和环境配置
**负责人**: 后端开发工程师 (主) + 前端开发工程师 (辅)

#### 开发任务
- [ ] **1.1 下载若依框架源码**
  - 获取RuoYi-Vue-Plus最新版本
  - 解压并导入到IDE (IDEA/VSCode)
  - 配置Maven/Node.js环境

- [ ] **1.2 数据库环境搭建**
  - 安装MySQL 8.0+
  - 创建数据库 `autotest_platform`
  - 执行若依初始化SQL脚本
  - 配置数据库连接参数

- [ ] **1.3 Redis环境配置**
  - 安装Redis 6.0+
  - 配置Redis连接参数
  - 测试Redis连接正常

- [ ] **1.4 项目配置修改**
  - 修改application.yml配置文件
  - 更新数据库连接信息
  - 配置Redis连接参数
  - 设置文件上传路径

#### 验收标准
- [ ] 若依项目成功启动
- [ ] 前端页面正常访问
- [ ] 管理员账号登录成功
- [ ] 基础功能菜单显示正常

#### 交付物
- [ ] 可运行的若依基础环境
- [ ] 环境配置文档

---

### Day 2: 数据库设计和初始化
**负责人**: 后端开发工程师

#### 开发任务
- [ ] **2.1 创建核心业务表**
  ```sql
  -- 执行以下SQL脚本
  -- test_project (测试项目表)
  -- test_project_member (项目成员表)
  -- test_environment (测试环境表)
  -- test_case_category (用例分类表)
  -- test_case (测试用例表)
  -- test_case_version (用例版本表)
  -- test_data_source (测试数据源表)
  -- test_execution (测试执行表)
  -- test_execution_case (执行用例详情表)
  -- test_schedule (测试调度表)
  -- test_report (测试报告表)
  ```

- [ ] **2.2 创建索引和外键约束**
  ```sql
  -- 主要索引
  CREATE INDEX idx_project_member_user ON test_project_member(user_id);
  CREATE INDEX idx_case_project ON test_case(project_id, status);
  CREATE INDEX idx_execution_project ON test_execution(project_id, create_time);
  -- 外键约束
  ALTER TABLE test_project_member ADD CONSTRAINT fk_member_project
    FOREIGN KEY (project_id) REFERENCES test_project(project_id);
  ```

- [ ] **2.3 初始化基础数据**
  ```sql
  -- 插入测试类型枚举
  INSERT INTO sys_dict_type VALUES (100, '测试用例类型', 'test_case_type', '0', 'admin', NOW(), '', NULL, '测试用例类型列表');
  INSERT INTO sys_dict_data VALUES (1000, 1, 'Web UI测试', 'WEB_UI', 'test_case_type', '', 'primary', 'N', '0', 'admin', NOW(), '', NULL, 'Web UI自动化测试');
  INSERT INTO sys_dict_data VALUES (1001, 2, 'API测试', 'API', 'test_case_type', '', 'success', 'N', '0', 'admin', NOW(), '', NULL, 'API接口测试');
  ```

- [ ] **2.4 若依数据字典扩展**
  - 添加测试相关字典类型
  - 配置测试状态枚举
  - 添加优先级设置

#### 验收标准
- [ ] 所有表创建成功，无语法错误
- [ ] 外键约束正确建立
- [ ] 索引创建成功
- [ ] 基础数据插入正确

#### 交付物
- [ ] 完整的数据库创建脚本
- [ ] 基础数据初始化脚本
- [ ] 数据库设计文档

---

### Day 3: 基础权限体系调整
**负责人**: 后端开发工程师

#### 开发任务
- [ ] **3.1 扩展用户权限模型**
  ```java
  // 创建TestProject实体类
  @Data
  @TableName("test_project")
  public class TestProject {
      @TableId(type = IdType.AUTO)
      private Long projectId;
      private String projectCode;
      private String projectName;
      // ... 其他字段
  }

  // 创建ProjectMember实体类
  @Data
  @TableName("test_project_member")
  public class ProjectMember {
      @TableId(type = IdType.AUTO)
      private Long memberId;
      private Long projectId;
      private Long userId;
      private String roleType;
      // ... 其他字段
  }
  ```

- [ ] **3.2 实现数据权限过滤器**
  ```java
  @Component
  public class DataPermissionAspect {
      @Before("@annotation(projectDataScope)")
      public void dataScopeFilter(JoinPoint point, ProjectDataScope projectDataScope) {
          // 获取当前用户可访问的项目ID列表
          List<Long> projectIds = getAccessibleProjectIds();
          // 设置数据权限范围
          DataScopeHelper.setDataScope(projectIds);
      }
  }
  ```

- [ ] **3.3 创建项目权限Service**
  ```java
  @Service
  public class ProjectPermissionService {
      public boolean hasProjectPermission(Long userId, Long projectId, String permission) {
          // 检查用户是否有项目权限
          return projectMemberMapper.hasPermission(userId, projectId, permission);
      }

      public List<Long> getAccessibleProjectIds(Long userId) {
          // 获取用户可访问的项目列表
          return projectMemberMapper.selectProjectIdsByUserId(userId);
      }
  }
  ```

- [ ] **3.4 权限注解定义**
  ```java
  @Target({ElementType.METHOD, ElementType.TYPE})
  @Retention(RetentionPolicy.RUNTIME)
  @Documented
  public @interface ProjectDataScope {
      String value() default "";
      String alias() default "";
  }
  ```

#### 验收标准
- [ ] 数据权限注解正常工作
- [ ] 用户只能访问有权限的项目数据
- [ ] 权限检查逻辑正确
- [ ] 数据隔离生效

#### 交付物
- [ ] 权限相关实体类
- [ ] 权限Service和Aspect类
- [ ] 权限注解定义
- [ ] 权限测试用例

---

### Day 4: 项目模块结构搭建
**负责人**: 后端开发工程师

#### 开发任务
- [ ] **4.1 创建项目模块基础结构**
  ```
  com.autotest.platform
  ├── controller
  │   ├── project
  │   │   ├── TestProjectController.java
  │   │   ├── ProjectMemberController.java
  │   │   └── TestEnvironmentController.java
  ├── service
  │   ├── ITestProjectService.java
  │   ├── IProjectMemberService.java
  │   └── ITestEnvironmentService.java
  ├── service.impl
  │   ├── TestProjectServiceImpl.java
  │   ├── ProjectMemberServiceImpl.java
  │   └── TestEnvironmentServiceImpl.java
  ├── mapper
  │   ├── TestProjectMapper.java
  │   ├── ProjectMemberMapper.java
  │   └── TestEnvironmentMapper.java
  └── domain
      ├── TestProject.java
      ├── ProjectMember.java
      └── TestEnvironment.java
  ```

- [ ] **4.2 创建基础Service接口**
  ```java
  // ITestProjectService.java
  public interface ITestProjectService extends IService<TestProject> {
      List<TestProject> selectProjectsByUserId(Long userId);
      boolean createProject(TestProject project);
      boolean updateProject(TestProject project);
      boolean deleteProject(Long projectId);
      TestProject selectProjectById(Long projectId);
  }
  ```

- [ ] **4.3 创建Mapper接口和XML**
  ```java
  // TestProjectMapper.java
  public interface TestProjectMapper extends BaseMapper<TestProject> {
      @Select("SELECT * FROM test_project WHERE project_id = #{projectId}")
      TestProject selectProjectById(@Param("projectId") Long projectId);

      @Select("SELECT p.* FROM test_project p " +
              "INNER JOIN test_project_member m ON p.project_id = m.project_id " +
              "WHERE m.user_id = #{userId} AND p.del_flag = '0'")
      List<TestProject> selectProjectsByUserId(@Param("userId") Long userId);
  }
  ```

- [ ] **4.4 配置MyBatis扫描**
  ```java
  @Configuration
  @MapperScan("com.autotest.platform.mapper")
  public class MyBatisConfig {
      // MyBatis配置
  }
  ```

#### 验收标准
- [ ] 项目结构创建完成
- [ ] Service接口定义完整
- [ ] Mapper接口和XML配置正确
- [ ] Spring扫描配置生效

#### 交付物
- [ ] 完整的项目模块结构
- [ ] 基础Service和Mapper代码
- [ ] MyBatis配置文件

---

### Day 5: 前端页面框架初始化
**负责人**: 前端开发工程师

#### 开发任务
- [ ] **5.1 创建前端项目结构**
  ```
  src/
  ├── views/
  │   ├── project/
  │   │   ├── index.vue          # 项目列表
  │   │   ├── add.vue           # 新增项目
  │   │   ├── edit.vue          # 编辑项目
  │   │   ├── members.vue       # 成员管理
  │   │   └── environments.vue  # 环境管理
  ├── api/
  │   └── project/
  │       ├── index.js          # 项目API
  │       ├── member.js         # 成员API
  │       └── environment.js    # 环境API
  └── components/
      └── project/
          ├── ProjectCard.vue   # 项目卡片
          └── MemberForm.vue    # 成员表单
  ```

- [ ] **5.2 配置路由**
  ```javascript
  // router/index.js
  {
    path: '/project',
    component: Layout,
    redirect: '/project/list',
    children: [
      {
        path: 'list',
        component: () => import('@/views/project/index'),
        name: 'ProjectList',
        meta: { title: '项目管理', icon: 'example' }
      },
      {
        path: 'add',
        component: () => import('@/views/project/add'),
        name: 'ProjectAdd',
        meta: { title: '新增项目', activeMenu: '/project/list' }
      }
    ]
  }
  ```

- [ ] **5.3 创建API接口**
  ```javascript
  // api/project/index.js
  import request from '@/utils/request'

  export function listProjects(query) {
    return request({
      url: '/test/project/list',
      method: 'get',
      params: query
    })
  }

  export function getProject(projectId) {
    return request({
      url: '/test/project/' + projectId,
      method: 'get'
    })
  }

  export function addProject(data) {
    return request({
      url: '/test/project',
      method: 'post',
      data: data
    })
  }
  ```

- [ ] **5.4 创建基础页面组件**
  ```vue
  <!-- views/project/index.vue -->
  <template>
    <div class="app-container">
      <el-card class="box-card">
        <div slot="header" class="clearfix">
          <span>项目管理</span>
          <el-button style="float: right; padding: 3px 0" type="text" @click="handleAdd">新增项目</el-button>
        </div>
        <!-- 项目列表内容 -->
      </el-card>
    </div>
  </template>
  ```

#### 验收标准
- [ ] 前端路由配置正确
- [ ] API接口调用正常
- [ ] 页面组件渲染成功
- [ ] 菜单导航显示正确

#### 交付物
- [ ] 前端页面结构
- [ ] API接口配置
- [ ] 基础页面组件

---

## Week 2 任务清单

### Day 6-7: 项目创建和配置管理
**负责人**: 后端开发工程师 (主) + 前端开发工程师 (辅)

#### 后端任务 (Day 6)
- [ ] **6.1 实现项目Controller**
  ```java
  @RestController
  @RequestMapping("/test/project")
  public class TestProjectController extends BaseController {
      @Autowired
      private ITestProjectService projectService;

      @GetMapping("/list")
      public TableDataInfo list(TestProject project) {
          startPage();
          List<TestProject> list = projectService.selectProjectsByUserId(getUserId());
          return getDataTable(list);
      }

      @PostMapping
      public AjaxResult add(@Validated @RequestBody TestProject project) {
          return toAjax(projectService.createProject(project));
      }
  }
  ```

- [ ] **6.2 实现项目Service逻辑**
  ```java
  @Service
  public class TestProjectServiceImpl extends ServiceImpl<TestProjectMapper, TestProject> implements ITestProjectService {
      @Override
      public boolean createProject(TestProject project) {
          // 生成项目编码
          project.setProjectCode(generateProjectCode());
          // 设置创建人
          project.setOwnerId(getUserId());
          return save(project);
      }

      private String generateProjectCode() {
          return "PRJ" + DateUtils.dateTimeNow("yyyyMMdd") +
                 String.format("%03d", getNextSequence());
      }
  }
  ```

- [ ] **6.3 项目数据验证**
  ```java
  @Component
  public class ProjectValidator {
      public void validateProject(TestProject project) {
          if (StringUtils.isEmpty(project.getProjectName())) {
              throw new ServiceException("项目名称不能为空");
          }
          if (checkProjectNameExists(project)) {
              throw new ServiceException("项目名称已存在");
          }
      }
  }
  ```

#### 前端任务 (Day 7)
- [ ] **7.1 项目列表页面**
  ```vue
  <template>
    <div class="project-list">
      <!-- 搜索表单 -->
      <el-form :model="queryParams" ref="queryForm" :inline="true">
        <el-form-item label="项目名称" prop="projectName">
          <el-input v-model="queryParams.projectName" placeholder="请输入项目名称" clearable />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" icon="el-icon-search" @click="handleQuery">搜索</el-button>
        </el-form-item>
      </el-form>

      <!-- 项目列表 -->
      <el-table v-loading="loading" :data="projectList">
        <el-table-column label="项目编码" prop="projectCode" />
        <el-table-column label="项目名称" prop="projectName" />
        <el-table-column label="状态" prop="status">
          <template slot-scope="scope">
            <el-tag :type="scope.row.status === '0' ? 'success' : 'danger'">
              {{ scope.row.status === '0' ? '正常' : '停用' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="创建时间" prop="createTime" />
        <el-table-column label="操作" width="180">
          <template slot-scope="scope">
            <el-button size="mini" @click="handleUpdate(scope.row)">编辑</el-button>
            <el-button size="mini" type="danger" @click="handleDelete(scope.row)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>
    </div>
  </template>
  ```

- [ ] **7.2 项目新增/编辑表单**
  ```vue
  <template>
    <el-dialog :title="title" :visible.sync="open" width="500px">
      <el-form ref="form" :model="form" :rules="rules" label-width="80px">
        <el-form-item label="项目名称" prop="projectName">
          <el-input v-model="form.projectName" placeholder="请输入项目名称" />
        </el-form-item>
        <el-form-item label="项目描述" prop="description">
          <el-input v-model="form.description" type="textarea" placeholder="请输入项目描述" />
        </el-form-item>
        <el-form-item label="Git仓库" prop="gitRepoUrl">
          <el-input v-model="form.gitRepoUrl" placeholder="请输入Git仓库地址" />
        </el-form-item>
        <el-form-item label="Git分支" prop="gitBranch">
          <el-input v-model="form.gitBranch" placeholder="请输入Git分支" />
        </el-form-item>
      </el-form>
      <div slot="footer" class="dialog-footer">
        <el-button @click="cancel">取 消</el-button>
        <el-button type="primary" @click="submitForm">确 定</el-button>
      </div>
    </el-dialog>
  </template>
  ```

#### 验收标准
- [ ] 项目创建功能正常
- [ ] 项目编辑功能正常
- [ ] 项目列表显示正确
- [ ] 表单验证生效

#### 交付物
- [ ] 项目管理Controller和Service
- [ ] 项目管理前端页面
- [ ] 表单验证规则

---

### Day 8-9: 项目成员和权限管理
**负责人**: 后端开发工程师

#### 开发任务
- [ ] **8.1 成员管理Controller**
  ```java
  @RestController
  @RequestMapping("/test/project/member")
  public class ProjectMemberController {
      @PostMapping("/add")
      public AjaxResult addMember(@RequestBody AddMemberDTO dto) {
          return toAjax(memberService.addMember(dto));
      }

      @GetMapping("/list/{projectId}")
      public AjaxResult listMembers(@PathVariable Long projectId) {
          List<ProjectMemberVO> members = memberService.selectMembersByProjectId(projectId);
          return AjaxResult.success(members);
      }

      @DeleteMapping("/{memberId}")
      public AjaxResult removeMember(@PathVariable Long memberId) {
          return toAjax(memberService.removeMember(memberId));
      }
  }
  ```

- [ ] **8.2 权限检查逻辑**
  ```java
  @Service
  public class ProjectMemberServiceImpl implements IProjectMemberService {
      @Override
      public boolean addMember(AddMemberDTO dto) {
          // 检查当前用户是否有添加成员权限
          if (!permissionService.hasPermission(getUserId(), dto.getProjectId(), "MEMBER_MANAGE")) {
              throw new ServiceException("无权限添加成员");
          }

          // 检查用户是否已是项目成员
          if (checkMemberExists(dto.getProjectId(), dto.getUserId())) {
              throw new ServiceException("用户已是项目成员");
          }

          ProjectMember member = new ProjectMember();
          member.setProjectId(dto.getProjectId());
          member.setUserId(dto.getUserId());
          member.setRoleType(dto.getRoleType());
          member.setPermissions(String.join(",", dto.getPermissions()));

          return save(member);
      }
  }
  ```

- [ ] **8.3 角色权限定义**
  ```java
  public enum ProjectRoleType {
      OWNER("项目负责人", Arrays.asList("ALL")),
      ADMIN("管理员", Arrays.asList("PROJECT_MANAGE", "MEMBER_MANAGE", "CASE_WRITE", "CASE_EXECUTE")),
      MEMBER("成员", Arrays.asList("CASE_READ", "CASE_EXECUTE")),
      VIEWER("查看者", Arrays.asList("CASE_READ"));

      private String roleName;
      private List<String> defaultPermissions;
      // 构造方法和getter
  }
  ```

#### 验收标准
- [ ] 成员添加功能正常
- [ ] 权限检查生效
- [ ] 成员列表显示正确
- [ ] 角色权限定义完整

#### 交付物
- [ ] 成员管理相关代码
- [ ] 权限检查逻辑
- [ ] 角色权限枚举

---

### Day 10: 测试环境配置管理
**负责人**: 后端开发工程师

#### 开发任务
- [ ] **10.1 环境管理功能**
  ```java
  @RestController
  @RequestMapping("/test/environment")
  public class TestEnvironmentController {
      @PostMapping
      public AjaxResult add(@RequestBody TestEnvironment environment) {
          return toAjax(environmentService.createEnvironment(environment));
      }

      @GetMapping("/list/{projectId}")
      public AjaxResult list(@PathVariable Long projectId) {
          List<TestEnvironment> list = environmentService.selectByProjectId(projectId);
          return AjaxResult.success(list);
      }
  }
  ```

- [ ] **10.2 环境配置加密**
  ```java
  @Component
  public class ConfigEncryption {
      @Value("${encryption.key}")
      private String encryptionKey;

      public String encrypt(String plainText) {
          // AES加密实现
          return AESUtils.encrypt(plainText, encryptionKey);
      }

      public String decrypt(String encryptedText) {
          // AES解密实现
          return AESUtils.decrypt(encryptedText, encryptionKey);
      }
  }
  ```

- [ ] **10.3 数据权限集成测试**
  ```java
  @Test
  public void testDataPermission() {
      // 测试数据权限过滤
      Long userId = 1L;
      List<TestProject> projects = projectService.selectProjectsByUserId(userId);
      assertThat(projects).isNotEmpty();

      // 测试跨项目访问限制
      Long unauthorizedProjectId = 999L;
      assertThatThrownBy(() ->
          projectService.selectProjectById(unauthorizedProjectId)
      ).isInstanceOf(ServiceException.class);
  }
  ```

#### 验收标准
- [ ] 环境配置功能正常
- [ ] 敏感信息加密存储
- [ ] 数据权限测试通过
- [ ] 所有基础功能集成完成

#### 交付物
- [ ] 环境管理功能
- [ ] 配置加密组件
- [ ] 集成测试用例

---

## 📋 Phase 1 检查清单

### 开发环境
- [ ] 开发工具安装配置完成 (IDEA/VSCode, MySQL, Redis)
- [ ] 若依框架成功部署和启动
- [ ] 数据库连接和基础数据正常

### 后端开发
- [ ] 数据库表结构创建完成
- [ ] 项目管理模块功能完整
- [ ] 权限体系和数据隔离正常
- [ ] API接口开发和测试完成

### 前端开发
- [ ] 前端项目结构搭建完成
- [ ] 项目管理页面开发完成
- [ ] 前后端接口联调正常
- [ ] 用户界面友好易用

### 测试验证
- [ ] 单元测试覆盖核心功能
- [ ] 集成测试验证模块交互
- [ ] 功能测试验证业务流程
- [ ] 性能测试验证基础性能

### 文档交付
- [ ] 环境配置文档
- [ ] 数据库设计文档
- [ ] API接口文档
- [ ] 用户操作手册

---

## 🎯 Phase 1 成功标准

### 功能完整性 ✅
- [ ] 项目创建、编辑、删除功能正常
- [ ] 项目成员管理功能完整
- [ ] 测试环境配置管理正常
- [ ] 多租户数据隔离生效

### 技术指标 ✅
- [ ] 代码质量符合规范
- [ ] 单元测试覆盖率>70%
- [ ] 接口响应时间<500ms
- [ ] 前端页面加载时间<3秒

### 用户体验 ✅
- [ ] 界面美观，操作流畅
- [ ] 功能完整，符合需求
- [ ] 错误提示清晰友好
- [ ] 响应式设计适配移动端

### 项目管理 ✅
- [ ] 开发进度符合计划
- [ ] 代码质量检查通过
- [ ] 文档编写完整规范
- [ ] 团队协作顺畅高效

完成Phase 1后，团队将具备完整的若依框架扩展能力，为后续核心功能开发奠定坚实基础。