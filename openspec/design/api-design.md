# API接口设计规范

## 概述
基于若依框架的RESTful API设计，提供统一的接口规范，支持多团队协作、大规模测试执行和完整的测试生命周期管理。

## 设计原则
- 遵循RESTful API设计原则
- 统一的响应格式和错误处理
- 支持API版本控制
- 完整的接口文档和测试
- 安全认证和权限控制

## 通用规范

### 1. URL设计规范
```
基础URL: http://localhost:8080/api/v1
模块路径: /module/resource
资源ID: /{id}
```

### 2. HTTP方法规范
```
GET    - 查询资源
POST   - 创建资源
PUT    - 更新资源(完整更新)
PATCH  - 更新资源(部分更新)
DELETE - 删除资源
```

### 3. 响应格式规范
```json
{
  "code": 200,
  "msg": "操作成功",
  "data": {},
  "timestamp": "2024-01-01T12:00:00Z"
}
```

### 4. 分页响应格式
```json
{
  "code": 200,
  "msg": "查询成功",
  "data": {
    "total": 100,
    "rows": [],
    "pageNum": 1,
    "pageSize": 10,
    "totalPages": 10
  },
  "timestamp": "2024-01-01T12:00:00Z"
}
```

## 1. 测试项目管理API

### 1.1 项目管理

#### 创建项目
```http
POST /api/v1/projects
Content-Type: application/json

{
  "projectName": "示例项目",
  "description": "项目描述",
  "gitRepoUrl": "https://github.com/example/repo.git",
  "gitBranch": "main"
}

Response:
{
  "code": 200,
  "msg": "创建成功",
  "data": {
    "projectId": 1,
    "projectCode": "PRJ202401001",
    "projectName": "示例项目",
    "description": "项目描述",
    "status": "ACTIVE",
    "createTime": "2024-01-01T12:00:00Z"
  }
}
```

#### 查询项目列表
```http
GET /api/v1/projects?pageNum=1&pageSize=10&projectName=示例

Response:
{
  "code": 200,
  "msg": "查询成功",
  "data": {
    "total": 1,
    "rows": [
      {
        "projectId": 1,
        "projectCode": "PRJ202401001",
        "projectName": "示例项目",
        "description": "项目描述",
        "status": "ACTIVE",
        "ownerId": 1,
        "ownerName": "管理员",
        "memberCount": 5,
        "caseCount": 100,
        "createTime": "2024-01-01T12:00:00Z"
      }
    ],
    "pageNum": 1,
    "pageSize": 10,
    "totalPages": 1
  }
}
```

#### 更新项目信息
```http
PUT /api/v1/projects/{projectId}
Content-Type: application/json

{
  "projectName": "更新后的项目名",
  "description": "更新后的描述",
  "gitRepoUrl": "https://github.com/example/updated-repo.git"
}
```

#### 删除项目
```http
DELETE /api/v1/projects/{projectId}
```

### 1.2 项目成员管理

#### 添加项目成员
```http
POST /api/v1/projects/{projectId}/members
Content-Type: application/json

{
  "userIds": [2, 3, 4],
  "roleType": "MEMBER",
  "permissions": ["CASE_READ", "CASE_EXECUTE"]
}
```

#### 查询项目成员
```http
GET /api/v1/projects/{projectId}/members

Response:
{
  "code": 200,
  "msg": "查询成功",
  "data": [
    {
      "memberId": 1,
      "userId": 2,
      "userName": "张三",
      "userEmail": "zhangsan@example.com",
      "roleType": "ADMIN",
      "permissions": ["CASE_READ", "CASE_WRITE", "CASE_EXECUTE"],
      "joinTime": "2024-01-01T12:00:00Z"
    }
  ]
}
```

#### 更新成员权限
```http
PUT /api/v1/projects/{projectId}/members/{memberId}
Content-Type: application/json

{
  "roleType": "ADMIN",
  "permissions": ["CASE_READ", "CASE_WRITE", "CASE_EXECUTE", "MEMBER_MANAGE"]
}
```

#### 移除项目成员
```http
DELETE /api/v1/projects/{projectId}/members/{memberId}
```

### 1.3 测试环境管理

#### 创建测试环境
```http
POST /api/v1/projects/{projectId}/environments
Content-Type: application/json

{
  "envName": "测试环境",
  "envType": "TEST",
  "baseUrl": "https://test.example.com",
  "dbConfig": {
    "host": "localhost",
    "port": 3306,
    "database": "test_db",
    "username": "test_user",
    "password": "encrypted_password"
  },
  "apiConfig": {
    "timeout": 30000,
    "retryCount": 3
  }
}
```

#### 查询环境列表
```http
GET /api/v1/projects/{projectId}/environments

Response:
{
  "code": 200,
  "msg": "查询成功",
  "data": [
    {
      "envId": 1,
      "envName": "测试环境",
      "envType": "TEST",
      "baseUrl": "https://test.example.com",
      "isDefault": true,
      "sortOrder": 1,
      "createTime": "2024-01-01T12:00:00Z"
    }
  ]
}
```

## 2. 测试用例管理API

### 2.1 用例分类管理

#### 创建分类
```http
POST /api/v1/projects/{projectId}/categories
Content-Type: application/json

{
  "categoryName": "登录模块",
  "parentId": 0,
  "orderNum": 1,
  "leader": "张三"
}
```

#### 查询分类树
```http
GET /api/v1/projects/{projectId}/categories

Response:
{
  "code": 200,
  "msg": "查询成功",
  "data": [
    {
      "categoryId": 1,
      "categoryName": "登录模块",
      "parentId": 0,
      "children": [
        {
          "categoryId": 2,
          "categoryName": "用户登录",
          "parentId": 1,
          "children": []
        }
      ]
    }
  ]
}
```

### 2.2 测试用例管理

#### 创建测试用例
```http
POST /api/v1/projects/{projectId}/cases
Content-Type: application/json

{
  "categoryId": 1,
  "caseTitle": "用户登录成功测试",
  "caseType": "WEB_UI",
  "priority": "HIGH",
  "preconditions": "用户已注册",
  "testSteps": [
    {
      "stepNumber": 1,
      "action": "打开登录页面",
      "elementType": "url",
      "locator": "/login",
      "operation": "navigate"
    },
    {
      "stepNumber": 2,
      "action": "输入用户名",
      "elementType": "input",
      "locator": "#username",
      "operation": "sendKeys",
      "value": "${username}"
    }
  ],
  "expectedResult": "登录成功，跳转到首页",
  "tags": ["登录", "冒烟测试"]
}
```

#### 查询用例列表
```http
GET /api/v1/projects/{projectId}/cases?pageNum=1&pageSize=10&categoryId=1&caseType=WEB_UI&status=ACTIVE

Response:
{
  "code": 200,
  "msg": "查询成功",
  "data": {
    "total": 50,
    "rows": [
      {
        "caseId": 1,
        "caseCode": "CASE202401001",
        "caseTitle": "用户登录成功测试",
        "caseType": "WEB_UI",
        "priority": "HIGH",
        "status": "ACTIVE",
        "version": 1,
        "authorName": "张三",
        "categoryName": "登录模块",
        "tags": ["登录", "冒烟测试"],
        "createTime": "2024-01-01T12:00:00Z",
        "updateTime": "2024-01-01T12:00:00Z"
      }
    ]
  }
}
```

#### 获取用例详情
```http
GET /api/v1/cases/{caseId}

Response:
{
  "code": 200,
  "msg": "查询成功",
  "data": {
    "caseId": 1,
    "caseCode": "CASE202401001",
    "caseTitle": "用户登录成功测试",
    "caseType": "WEB_UI",
    "priority": "HIGH",
    "preconditions": "用户已注册",
    "testSteps": [...],
    "expectedResult": "登录成功，跳转到首页",
    "testDataSource": "用户数据.xlsx",
    "tags": ["登录", "冒烟测试"],
    "version": 1,
    "authorName": "张三",
    "createTime": "2024-01-01T12:00:00Z"
  }
}
```

#### 更新测试用例
```http
PUT /api/v1/cases/{caseId}
Content-Type: application/json

{
  "caseTitle": "更新后的用例标题",
  "priority": "MEDIUM",
  "testSteps": [...],
  "expectedResult": "更新后的期望结果"
}
```

#### 删除测试用例
```http
DELETE /api/v1/cases/{caseId}
```

### 2.3 测试用例执行

#### 立即执行用例
```http
POST /api/v1/executions
Content-Type: application/json

{
  "executionName": "冒烟测试执行",
  "projectId": 1,
  "envId": 1,
  "caseIds": [1, 2, 3],
  "executionType": "MANUAL"
}

Response:
{
  "code": 200,
  "msg": "执行任务已创建",
  "data": {
    "executionId": 1,
    "executionName": "冒烟测试执行",
    "executionStatus": "PENDING",
    "totalCases": 3,
    "createTime": "2024-01-01T12:00:00Z"
  }
}
```

#### 查询执行状态
```http
GET /api/v1/executions/{executionId}/status

Response:
{
  "code": 200,
  "msg": "查询成功",
  "data": {
    "executionId": 1,
    "executionStatus": "RUNNING",
    "totalCases": 3,
    "passedCases": 1,
    "failedCases": 0,
    "runningCases": 1,
    "pendingCases": 1,
    "startTime": "2024-01-01T12:00:00Z",
    "duration": 30
  }
}
```

#### 获取执行结果
```http
GET /api/v1/executions/{executionId}/results?pageNum=1&pageSize=10&status=FAILED

Response:
{
  "code": 200,
  "msg": "查询成功",
  "data": {
    "executionId": 1,
    "totalCases": 3,
    "passedCases": 2,
    "failedCases": 1,
    "rows": [
      {
        "executionCaseId": 1,
        "caseId": 1,
        "caseTitle": "用户登录成功测试",
        "executionStatus": "FAILED",
        "duration": 5000,
        "errorMessage": "元素未找到: #submit-button",
        "screenshotPath": "/screenshots/execution_1_case_1.png",
        "stepResults": [...],
        "startTime": "2024-01-01T12:00:00Z",
        "endTime": "2024-01-01T12:00:05Z"
      }
    ]
  }
}
```

### 2.4 测试调度管理

#### 创建定时任务
```http
POST /api/v1/projects/{projectId}/schedules
Content-Type: application/json

{
  "scheduleName": "每日冒烟测试",
  "cronExpression": "0 0 8 * * ?",
  "caseIds": [1, 2, 3, 4, 5],
  "envId": 1,
  "notificationConfig": {
    "emails": ["test@example.com"],
    "webhook": "https://hook.example.com",
    "notifyOnFailure": true,
    "notifyOnSuccess": false
  }
}
```

#### 查询调度列表
```http
GET /api/v1/projects/{projectId}/schedules

Response:
{
  "code": 200,
  "msg": "查询成功",
  "data": [
    {
      "scheduleId": 1,
      "scheduleName": "每日冒烟测试",
      "cronExpression": "0 0 8 * * ?",
      "scheduleStatus": "ACTIVE",
      "lastExecutionTime": "2024-01-01T08:00:00Z",
      "nextExecutionTime": "2024-01-02T08:00:00Z",
      "executionCount": 10,
      "successCount": 8,
      "failureCount": 2
    }
  ]
}
```

## 3. 测试报告API

### 3.1 报告查询

#### 获取执行汇总报告
```http
GET /api/v1/executions/{executionId}/reports/summary

Response:
{
  "code": 200,
  "msg": "查询成功",
  "data": {
    "executionId": 1,
    "executionName": "冒烟测试执行",
    "summary": {
      "totalCases": 100,
      "passedCases": 95,
      "failedCases": 3,
      "skippedCases": 2,
      "passRate": 95.0,
      "totalDuration": 1800,
      "averageDuration": 18.0
    },
    "statusDistribution": {
      "PASSED": 95,
      "FAILED": 3,
      "SKIPPED": 2
    },
    "priorityDistribution": {
      "HIGH": 20,
      "MEDIUM": 60,
      "LOW": 20
    },
    "typeDistribution": {
      "WEB_UI": 50,
      "API": 30,
      "UNIT": 20
    }
  }
}
```

#### 获取详细执行报告
```http
GET /api/v1/executions/{executionId}/reports/detailed

Response:
{
  "code": 200,
  "msg": "查询成功",
  "data": {
    "executionId": 1,
    "executionName": "冒烟测试执行",
    "summary": {...},
    "caseResults": [
      {
        "caseId": 1,
        "caseTitle": "用户登录成功测试",
        "executionStatus": "PASSED",
        "duration": 5000,
        "stepResults": [...],
        "screenshotPath": "/screenshots/execution_1_case_1.png"
      }
    ],
    "charts": {
      "passTrend": [...],
      "executionTimeline": [...],
      "categoryPerformance": [...]
    }
  }
}
```

#### 导出报告
```http
GET /api/v1/executions/{executionId}/reports/export?format=pdf&template=default

Response: Binary file download
```

## 4. CI/CD集成API

### 4.1 Webhook集成

#### Git Webhook触发
```http
POST /api/v1/webhooks/git-push
Content-Type: application/json

{
  "repository": {
    "url": "https://github.com/example/repo.git",
    "branch": "main"
  },
  "commit": {
    "id": "abc123",
    "message": "fix: 修复登录问题",
    "author": "张三"
  },
  "projectCode": "PRJ202401001"
}

Response:
{
  "code": 200,
  "msg": "触发成功",
  "data": {
    "executionId": 1,
    "executionStatus": "PENDING"
  }
}
```

### 4.2 执行状态查询

#### 查询执行状态(供CI/CD系统使用)
```http
GET /api/v1/executions/{executionId}/status?format=json

Response:
{
  "executionId": 1,
  "executionStatus": "COMPLETED",
  "result": "SUCCESS",
  "passRate": 95.0,
  "reportUrl": "https://test-platform.com/reports/1",
  "startTime": "2024-01-01T12:00:00Z",
  "endTime": "2024-01-01T12:30:00Z"
}
```

## 5. 实时监控API

### 5.1 WebSocket连接

#### 执行状态推送
```javascript
// WebSocket连接
const ws = new WebSocket('ws://localhost:8080/ws/execution/{executionId}');

// 接收消息格式
{
  "type": "STATUS_UPDATE",
  "data": {
    "executionId": 1,
    "executionStatus": "RUNNING",
    "totalCases": 100,
    "completedCases": 45,
    "passedCases": 40,
    "failedCases": 5,
    "currentCase": {
      "caseId": 45,
      "caseTitle": "当前执行的用例",
      "stepNumber": 3,
      "totalSteps": 5
    }
  }
}
```

## 6. 错误码规范

### 6.1 业务错误码
```
1001 - 项目不存在
1002 - 项目权限不足
1003 - 项目状态异常

2001 - 用例不存在
2002 - 用例权限不足
2003 - 用例状态不允许执行

3001 - 执行任务不存在
3002 - 执行状态异常
3003 - 环境配置错误

4001 - 调度任务不存在
4002 - Cron表达式无效
4003 - 调度状态异常

5001 - 文件上传失败
5002 - 文件格式不支持
5003 - 文件大小超限
```

### 6.2 系统错误码
```
9001 - 系统内部错误
9002 - 数据库连接异常
9003 - 缓存服务异常
9004 - 消息队列异常
9005 - 文件系统异常
```

## 7. 安全认证

### 7.1 JWT Token认证
```http
Authorization: Bearer {jwt_token}
```

### 7.2 API Key认证
```http
X-API-Key: {api_key}
```

## 8. 接口限流

### 8.1 限流策略
- 普通用户：100次/分钟
- 高级用户：500次/分钟
- 系统调用：1000次/分钟

### 8.2 限流响应
```json
{
  "code": 429,
  "msg": "请求过于频繁，请稍后重试",
  "data": {
    "retryAfter": 60,
    "limit": 100,
    "remaining": 0
  }
}
```

这个API设计支持：
- 完整的测试生命周期管理
- 多团队协作和权限控制
- CI/CD系统集成
- 实时监控和状态推送
- 灵活的报告生成和导出
- 标准化的错误处理和响应格式