# Project Context

## Purpose
AutoTest Platform - An automated testing platform designed to streamline and manage various types of software testing processes. The platform aims to provide comprehensive test automation capabilities including unit testing, integration testing, and end-to-end testing workflows.

## Tech Stack
**RuoYi Framework Based**
- **Backend**: Java 8+, Spring Boot 2.x, Spring Framework 5.x
- **Frontend**: Vue.js 3.x, Element UI Plus, Axios
- **Database**: MySQL 8.0+ (primary), Redis (cache/sessions)
- **ORM**: MyBatis-Plus for enhanced database operations
- **Security**: Spring Security + JWT (JSON Web Token)
- **Build Tools**: Maven (backend), Node.js (frontend)
- **Testing Frameworks**: Selenium (Web UI), RestAssured (API), JUnit 5
- **CI/CD Integration**: Jenkins, GitLab CI, GitHub Actions
- **Deployment**: Local server deployment (initial), Docker support planned

## Project Conventions

### Code Style
**RuoYi Framework Standards:**
- **Java**:阿里巴巴Java开发手册规范，使用Google Code Format
- **JavaScript/Vue**: ESLint + Prettier，标准Vue 3 Composition API风格
- **Naming conventions**:
  - Java: camelCase for variables, PascalCase for classes, UPPER_CASE for constants
  - Vue: kebab-case for components, camelCase for methods
- **Maximum line length**: 120 characters
- **Documentation**: 标准JavaDoc和JSDoc注释
- **Code Generation**: 使用若依代码生成器保证一致性

### Architecture Patterns
**RuoYi Framework + Testing Platform Extensions:**
- **Layered Architecture**: Controller → Service → Mapper → Entity (若依标准)
- **Modular Design**: 按测试功能模块划分（项目管理、用例管理、执行引擎等）
- **RESTful APIs**: 标准化API设计，支持版本控制
- **Event-Driven**: 基于Spring Events的测试执行事件处理
- **Plugin Architecture**: 可插拔的测试框架集成（Selenium、RestAssured等）
- **Multi-tenancy**: 基于若依数据权限的多团队支持

### Testing Strategy
**平台自身测试 + 用户测试管理:**
- **Platform Testing**:
  - Unit tests: JUnit 5 + Mockito (80%)
  - Integration tests: @SpringBootTest (15%)
  - E2E tests: Selenium WebDriver (5%)
- **User Test Management**:
  - 支持多种测试框架：Selenium、RestAssured、JUnit、TestNG
  - 并行测试执行支持
  - 测试结果收集和报告生成
  - 测试数据和环境管理

### Git Workflow
**RuoYi标准 + 测试平台扩展:**
- `master` branch: 生产环境代码 (若依标准)
- `develop` branch: 开发环境集成分支
- `feature/*` branches: 功能模块开发分支
- `hotfix/*` branches: 紧急修复分支
- `release/*` branches: 发布准备分支
- **Commit format**: 若依标准 (`feat: 新功能`, `fix: 修复`, `docs: 文档`)
- **Code Review**: 必须经过PR审查，重点检查测试相关逻辑
- **Version Tag**: `v1.0.0` 语义化版本控制

## Domain Context

### Testing Platform Concepts (基于若依框架扩展)
- **Test Projects**: 测试项目管理，对应若依的业务模块
- **Test Suites**: 测试套件，包含相关测试用例的集合
- **Test Cases**: 测试用例，支持多种类型（UI、API、性能等）
- **Test Execution**: 测试执行引擎，支持并行和定时执行
- **Test Reports**: 测试报告，基于若依监控模块扩展
- **Test Environments**: 测试环境管理，开发/测试/生产环境隔离
- **Data-Driven Testing**: 数据驱动测试，支持Excel、JSON等数据源
- **CI/CD Integration**: 持续集成集成，基于若依定时任务扩展

### Key Stakeholders
- **测试管理员**: 系统管理、用户权限、项目管理
- **QA工程师**: 测试用例设计、执行、结果分析
- **开发人员**: CI/CD集成、测试框架对接
- **项目经理**: 测试进度跟踪、质量报告分析
- **运维工程师**: 环境维护、性能监控、故障排查

## Important Constraints

### Technical Constraints
- **Scalability**: 支持大规模测试套件 (10,000+ 测试用例)
- **Performance**: 测试执行性能优化，支持并行执行
- **Reliability**: 99.9% 系统可用性，基于若依监控模块
- **Security**: 基于若依安全模块，测试数据和凭据安全管理
- **Multi-tenancy**: 支持多团队使用，数据隔离
- **Framework Compatibility**: 兼容主流测试框架 (Selenium, JUnit, TestNG等)

### Business Constraints
- **Budget**: 基于开源技术栈，降低成本
- **Timeline**: 6个月内交付MVP版本
- **Compliance**: 企业级数据安全和合规要求
- **Integration**: 必须与现有开发工具链集成
- **User Experience**: 基于若依UI，保持一致的用户体验

## External Dependencies

**核心集成 (基于若依生态):**
- **Version Control**: Git (GitHub, GitLab, Gitee)
- **CI/CD Platforms**: Jenkins, GitLab CI, GitHub Actions
- **Testing Frameworks**: Selenium WebDriver, RestAssured, JUnit 5, TestNG
- **Database**: MySQL 8.0+, Redis 6.0+
- **Notification**: 钉钉、企业微信、邮件通知

**扩展集成 (后续版本):**
- **Monitoring**: Prometheus + Grafana (基于若依监控扩展)
- **Container**: Docker, Kubernetes (容器化部署)
- **File Storage**: MinIO, 阿里云OSS (测试报告存储)
- **Message Queue**: RabbitMQ (异步任务处理)
