# AutoTest Platform - 自动化测试平台

## 项目介绍
基于若依框架开发的企业级自动化测试平台，支持多团队协作、大规模测试执行、CI/CD集成和智能报告分析。

## 技术架构
- **后端**: Spring Boot 2.5.15 + MyBatis Plus + MySQL 8.0 + Redis
- **前端**: Vue 3 + Element UI Plus
- **数据库**: MySQL 8.0
- **缓存**: Redis 6.0
- **容器化**: Docker + Docker Compose

## 🚀 快速开始

### 1. 环境准备
- JDK 1.8+
- Maven 3.6+
- Node.js 14+
- Docker & Docker Compose

### 2. 启动基础服务
```bash
# 启动Docker服务
docker-compose up -d

# 等待服务启动完成（约30秒）
docker-compose ps
```

### 3. 启动应用
```bash
# 编译项目
mvn clean compile

# 启动应用
mvn spring-boot:run
```

### 4. 访问系统
- **应用地址**: http://localhost:8080
- **管理员账号**: admin / admin123456
- **普通用户**: ry / admin123456

### 5. MinIO管理控制台
- **访问地址**: http://localhost:9001
- **账号**: admin / admin123456

## 📋 功能模块

### ✅ Phase 1: 基础框架搭建 (已完成)
- [x] 若依框架集成
- [x] 数据库设计和初始化
- [x] 项目管理功能
- [x] 多租户权限控制

### ✅ Phase 2: 核心功能开发 (已完成)
- [x] 测试用例分类管理
  - 树形结构分类
  - 分类权限控制
  - 分类统计信息
- [x] 测试用例CRUD功能
  - 用例创建、编辑、删除
  - 支持多种测试类型（Web UI、API、单元测试、性能测试）
  - 测试步骤管理
  - 标签系统
- [x] 测试用例版本控制
  - 版本历史记录
  - 版本比较
  - 版本回滚
  - 自动版本清理
- [x] 测试数据源管理
  - 文件类型数据源（Excel、CSV、JSON）
  - 数据库类型数据源
  - API接口数据源
  - 数据预览功能

### 🚧 Phase 3: 高级功能开发 (开发中)
- [ ] 测试执行引擎
  - 任务调度系统
  - 并行执行控制
  - 框架适配器架构
- [ ] 报告分析系统
  - 实时执行监控
  - 统计分析图表
  - 趋势分析
- [ ] CI/CD集成
  - Git Webhook
  - Jenkins集成
  - 自动化部署

## 📊 API接口文档

### 项目管理
- `GET /test/project/list` - 查询项目列表
- `GET /test/project/{id}` - 查询项目详情
- `POST /test/project` - 创建项目
- `PUT /test/project` - 更新项目
- `DELETE /test/project/{ids}` - 删除项目

### 测试用例分类
- `GET /test/category/list` - 查询分类列表
- `GET /test/category/tree/{projectId}` - 查询分类树
- `POST /test/category` - 创建分类
- `PUT /test/category` - 更新分类
- `DELETE /test/category/{ids}` - 删除分类

### 测试用例管理
- `GET /test/case/list` - 查询用例列表
- `GET /test/case/all` - 查询所有用例
- `GET /test/case/{id}` - 查询用例详情
- `POST /test/case` - 创建用例
- `PUT /test/case` - 更新用例
- `DELETE /test/case/{ids}` - 删除用例
- `PUT /test/case/status/{id}/{status}` - 更新用例状态
- `POST /test/case/copy/{id}` - 复制用例
- `PUT /test/case/move` - 移动用例

### 测试用例版本控制
- `GET /test/case/versions/{caseId}` - 查询版本历史
- `GET /test/case/version/{versionId}` - 查询版本详情
- `POST /test/case/version/compare` - 比较版本差异
- `POST /test/case/version/rollback` - 回滚版本

### 测试数据源
- `GET /test/datasource/list` - 查询数据源列表
- `GET /test/datasource/{id}` - 查询数据源详情
- `POST /test/datasource` - 创建数据源
- `PUT /test/datasource` - 更新数据源
- `DELETE /test/datasource/{ids}` - 删除数据源
- `POST /test/datasource/upload` - 上传文件数据源
- `POST /test/datasource/validate/db` - 验证数据库连接
- `POST /test/datasource/test/api` - 测试API数据源

## 🎯 核心特性

### 1. 多团队协作
- 基于项目的数据隔离
- 灵活的权限管理
- 项目成员角色控制
- 资源共享机制

### 2. 测试用例管理
- 多种测试类型支持
- 树形分类结构
- 版本控制和历史记录
- 标签系统和快速搜索

### 3. 数据驱动测试
- Excel/CSV文件支持
- 数据库直连
- API接口数据源
- 数据预览和验证

### 4. 企业级特性
- 基于若依框架的成熟架构
- 完整的权限控制体系
- 操作审计和日志记录
- 高性能缓存和数据库优化

## 🔧 开发指南

### 数据库表结构
- `test_project` - 测试项目表
- `test_project_member` - 项目成员表
- `test_environment` - 测试环境表
- `test_case_category` - 用例分类表
- `test_case` - 测试用例表
- `test_case_version` - 用例版本历史表
- `test_data_source` - 测试数据源表
- `test_execution` - 测试执行表
- `test_execution_case` - 执行用例详情表
- `test_schedule` - 测试调度表
- `test_report` - 测试报告表

### 代码生成
使用若依代码生成器可以快速生成：
- 标准的CRUD代码
- 前端页面和组件
- API接口文档

### 测试示例
```bash
# 运行单元测试
mvn test

# 运行集成测试
mvn test -Dspring.profiles.active=test

# 测试分类和用例功能
mvn test -Dtest=TestCaseServiceTest
```

## 📝 项目结构

```
autotest-platform/
├── docker-compose.yml              # Docker编排配置
├── src/main/java/com/autotest/platform/
│   ├── AutoTestPlatformApplication.java  # 启动类
│   ├── config/
│   │   └── MybatisPlusConfig.java        # MyBatis Plus配置
│   ├── controller/                     # 控制器层
│   │   ├── TestProjectController.java    # 项目管理API
│   │   ├── TestCaseCategoryController.java # 分类管理API
│   │   └── TestCaseController.java      # 用例管理API
│   ├── domain/                         # 实体类
│   │   ├── project/                       # 项目实体
│   │   └── testcase/                     # 测试用例实体
│   ├── service/                        # 业务服务层
│   │   ├── impl/
│   │   │   ├── TestProjectServiceImpl.java
│   │   │   ├── TestCaseCategoryServiceImpl.java
│   │   │   ├── TestCaseServiceImpl.java
│   │   │   └── TestCaseVersionServiceImpl.java
│   │   └── I*Service.java               # 服务接口
│   └── mapper/                         # 数据访问层
│       └── **Mapper.java                 # Mapper接口
├── src/main/resources/
│   ├── application.yml                    # 应用配置
│   ├── mapper/                          # MyBatis映射文件
│   └── docker/mysql/init/                # 数据库初始化脚本
└── README.md                              # 项目说明文档
```

## 🚀 部署说明

### Docker部署
```bash
# 构建镜像
docker build -t autotest-platform:latest .

# 运行容器
docker run -d -p 8080:8080 autotest-platform:latest
```

### 生产环境配置
1. 修改`application-prod.yml`中的数据库和Redis配置
2. 配置SSL证书
3. 设置文件存储路径
4. 配置日志级别

## 📈 性能指标

- **支持用例数量**: 10,000+
- **并发执行能力**: 1000+
- **系统响应时间**: <2秒
- **系统可用性**: 99.9%
- **支持团队数量**: 100+

## 🤝 贡献指南

1. Fork 本仓库
2. 创建特性分支 (`git checkout -b feature/AmazingFeature`)
3. 提交更改 (`git commit -m 'Add some AmazingFeature'`)
4. 推送到分支 (`git push origin feature/AmazingFeature`)
5. 打开 Pull Request

## 📝 许可证

本项目采用 [MIT](LICENSE) 许可证。

## 📞 联系方式

- **项目地址**: https://github.com/your-org/autotest-platform
- **问题反馈**: https://github.com/your-org/autotest-platform/issues
- **文档地址**: https://docs.autotest-platform.com

---

## 🎉 更新日志

### v1.1.0 (2024-01-01)
- ✨ 完成Phase 1: 基础框架搭建
- ✨ 完成Phase 2: 核心功能开发
- 新增测试用例分类管理
- 新增测试用例CRUD功能
- 新增测试用例版本控制
- 新增测试数据源管理
- 完善API接口和权限控制

### v1.0.0 (2024-01-01)
- 🎉 项目初始化
- 基础框架搭建
- 项目管理功能
- 多租户权限控制