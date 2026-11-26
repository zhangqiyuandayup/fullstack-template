# 后端工程

校园经费管理系统的后端服务，基于 Spring Boot，提供成员、项目与经费申请相关的 REST 接口。
项目流程与 Git 规范见[根 README](../README.md)。

## 实际使用的技术与版本

| 组件 | 版本 |
|---|---|
| 语言 | Java 17（OpenJDK 17.0.20.1，Eclipse Temurin） |
| 框架 | Spring Boot 4.1.2 |
| 构建工具 | Apache Maven 3.9.16 |
| 数据库 | MySQL 8.0.46 |
| 数据访问 | MyBatis Spring Boot Starter 4.0.0 |
| 密码加密 | spring-security-crypto（BCrypt） |
| 内嵌容器 | Apache Tomcat 11.0.27（Spring Boot 4 自带） |

> **注意版本差异**：Spring Boot 4.x 与 3.x 不同——Web starter 名为 `spring-boot-starter-webmvc`（不是 `-web`），
> 包名统一使用 `jakarta.*`（不是 `javax.*`），内嵌 Tomcat 为 11。按 3.x 教程排查可能对不上。

## 目录结构

```
backend/
├── pom.xml
├── src/main/java/com/campus/erp/
│   ├── CampusErpApplication.java      启动类
│   ├── auth/                          登录与会话
│   │   ├── AuthController.java        登录 / 登出 / 当前用户
│   │   ├── AuthService.java           校验用户名密码
│   │   ├── AuthMapper.java            按用户名查、初始化密码
│   │   ├── CurrentUser.java           会话中的用户（不含密码）
│   │   ├── LoginMember.java           认证专用（含密码哈希）
│   │   ├── LoginRequest.java          登录请求体
│   │   └── DataInitializer.java       启动时把占位密码换成 BCrypt 哈希
│   ├── common/                        公共设施
│   │   ├── Result.java                统一响应 {code,message,data}
│   │   ├── BizException.java          业务异常（带错误码）
│   │   ├── GlobalExceptionHandler.java 异常 → HTTP 状态码映射
│   │   ├── SessionUtils.java          从会话取用户
│   │   ├── AuthInterceptor.java       未登录拦截
│   │   └── AppConfig.java             BCrypt Bean + 拦截器注册
│   ├── project/                       项目
│   │   ├── Project.java
│   │   ├── ProjectController.java
│   │   └── ProjectMapper.java         含冻结/释放/结算的单条 SQL
│   ├── member/                        成员
│   │   ├── Member.java
│   │   ├── MemberController.java
│   │   └── MemberMapper.java
│   └── application/                   申请单（核心）
│       ├── ApplicationStatus.java     六状态枚举
│       ├── FundApplication.java       申请单实体
│       ├── CreateApplicationRequest.java
│       ├── ApplicationMapper.java
│       ├── OperationLogMapper.java
│       ├── ApplicationService.java    五个业务操作 + 权限校验
│       └── ApplicationController.java
└── src/main/resources/
    ├── application.properties         已提交（密码用占位符）
    └── application-local.properties   本地真实密码，不提交
```

## 启动前准备

### 1. 准备数据库

数据库脚本在仓库根的 `practice/sql/` 下。依次执行：

```powershell
cd <仓库根目录>

mysql -u root -p --default-character-set=utf8mb4 -e "source practice/sql/01_schema.sql"
mysql -u root -p --default-character-set=utf8mb4 -e "source practice/sql/02_seed.sql"
```

> **`--default-character-set=utf8mb4` 必须加**。Windows 中文环境下 MySQL 客户端默认按 GBK 读取脚本，
> 会报 `ERROR 1366 Incorrect string value`。

执行后得到数据库 `campus_erp`，含 5 张表：

| 表 | 说明 |
|---|---|
| `project` | 项目与三个金额（有守恒 CHECK 约束） |
| `member` | 成员与角色 |
| `project_member` | 项目成员关联（多对多） |
| `fund_application` | 申请单（三个金额分列，批准额与结算额可空） |
| `operation_log` | 操作日志 |

### 2. 配置数据库密码

`src/main/resources/application.properties` 已提交，密码位置是占位符：

```properties
spring.datasource.password=${DB_PASSWORD:}
spring.config.import=optional:application-local.properties
```

**本地运行时，在同一目录新建 `application-local.properties`**（该文件已被 `.gitignore` 忽略）：

```properties
spring.datasource.password=你的MySQL密码
```

真实密码只放本地，不入库。

### 3. 演示账号

首次启动时 `DataInitializer` 会把种子数据里的占位密码替换成 BCrypt 哈希，并在日志中打印一条 WARN。
四个账号密码均为 `123456`：

| 用户名 | 姓名 | 角色 |
|---|---|---|
| xiaolin | 小林 | MEMBER |
| wang | 王老师 | TEACHER |
| licaiwu | 李财务 | FINANCE |
| admin | 管理员 | ADMIN |

## 构建、启动与检查

命令均在 **backend 目录** 下执行。

```powershell
cd backend

# 构建（产出 target/campus-erp-0.0.1-SNAPSHOT.jar）
mvn clean package

# 启动
mvn spring-boot:run
```

> 停止服务请按 `Ctrl + C`。直接关闭终端窗口可能导致 Java 进程残留并占用 8080，
> 下次启动会报 `Port 8080 was already in use`。

**启动成功的判据（三行都要出现）：**

```
HikariPool-1 - Start completed.                     ← 数据库连通
Tomcat started on port 8080 (http) with context path ''
Started CampusErpApplication in X.XXX seconds
```

> 编译成功、服务启动、接口正常是三种不同的结果，需要分别确认，不能互相替代。

## 接口清单

统一前缀 `/api`。除登录与「当前用户」外，所有接口都要求已登录（会话 Cookie）。

### 认证

| 方法 | 路径 | 说明 |
|---|---|---|
| POST | `/api/auth/login` | 登录，Body：`{"username":"xiaolin","password":"123456"}` |
| POST | `/api/auth/logout` | 登出（销毁会话） |
| GET | `/api/auth/me` | 当前用户；未登录返回 `data: null` |

### 项目

| 方法 | 路径 | 说明 |
|---|---|---|
| GET | `/api/projects` | 项目列表 |
| GET | `/api/projects/{id}` | 单个项目（含总预算/可用/冻结/已用） |

### 成员

| 方法 | 路径 | 说明 |
|---|---|---|
| GET | `/api/members` | 成员列表（不返回密码字段） |

### 申请单

| 方法 | 路径 | 说明 | 谁可以 |
|---|---|---|---|
| GET | `/api/applications?projectId=1` | 某项目下的申请列表 | 登录用户 |
| GET | `/api/applications/{id}` | 申请详情 | 登录用户 |
| POST | `/api/applications` | 新建草稿，Body：`{"projectId":1,"purpose":"买硬盘","requestedAmount":120.00}` | 项目成员 |
| POST | `/api/applications/{id}/submit` | 提交（冻结经费） | 申请人本人 |
| POST | `/api/applications/{id}/approve?approvedAmount=100` | 批准（可减额） | 项目内 TEACHER，且不能审批本人申请 |
| POST | `/api/applications/{id}/reject?reason=不需要` | 拒绝 | 同上 |
| POST | `/api/applications/{id}/cancel?reason=不买了` | 取消 | 申请人本人 |
| POST | `/api/applications/{id}/settle?actualAmount=90` | 结算 | 项目内 FINANCE |

**操作人不从请求参数传入**，一律取自登录会话。前端无法冒充他人身份。

## 请求示例

推荐用 Apifox / Postman 调试（会自动管理 Cookie）。以下为命令行等价写法。

**登录（保存 Cookie）：**

```powershell
curl.exe -i -c cookies.txt -X POST "http://localhost:8080/api/auth/login" `
  -H "Content-Type: application/json" `
  --data-binary "@login.json"
```

其中 `login.json` 内容（**不要用 PowerShell 双引号内嵌 JSON，双引号会被吞掉**）：

```json
{"username":"xiaolin","password":"123456"}
```

期望：`HTTP/1.1 200`，响应头含 `Set-Cookie: JSESSIONID=...`，Body 为 `{"code":0,...,"data":{"id":1,...}}`。

**带会话查询：**

```powershell
curl.exe -i -b cookies.txt "http://localhost:8080/api/projects/1"
```

期望 `data` 中 `totalBudget`、`available`、`frozen`、`used` 四个字段，且**三项之和等于总额**。

**新建申请：**

```powershell
curl.exe -i -b cookies.txt -X POST "http://localhost:8080/api/applications" `
  -H "Content-Type: application/json" `
  --data-binary "@create.json"
```

`create.json`：

```json
{"projectId":1,"purpose":"购买测试硬盘","requestedAmount":120.00}
```

## 错误码约定

| HTTP | code | 含义 |
|---|---|---|
| 200 | 0 | 成功 |
| 400 | 1000 | 业务校验不通过（余额不足、状态不对等） |
| 400 | 4000 | 请求体不是合法 JSON |
| 400 | 4001 | 缺少必填参数 |
| 400 | 4002 | 参数类型不正确 |
| 401 | 401 | 未登录或会话失效 |
| 403 | 403 | 已登录但无权限（角色不符 / 非本人 / 不在该项目） |
| 500 | 9999 | 服务器内部错误（不返回细节） |

## 已验证的内容

- `mvn clean package` 构建通过，产出可执行 jar。
- 启动日志出现 HikariPool、Tomcat、Started 三行。
- 未登录访问 `/api/members` 返回 401；登录后返回 200 与四名成员。
- 新建草稿后经费三个金额不变；提交后可用减少、冻结增加。
- 减额批准会立即释放差额；拒绝与取消释放剩余冻结；结算清零冻结并把实际花费计入已使用。
- 每一步操作后 `available + frozen + used` 都等于 `total_budget`。
- 越权操作返回 403：非本人提交/取消、非老师审批、老师审批本人申请、非财务结算。

## 仍未完成的内容

- **没有自动化测试**：目前靠人工按上述步骤验证，无单元测试或集成测试。
- **参数校验为手写判断**：未使用 `@Valid` + 注解，功能等价但写法不够规范。
- **`MemberController` 未分层**：直接调用 Mapper，没有 Service 层（该接口目前无业务规则）。
- **无管理员建号接口**：新增账号需直接写数据库。
- **未实现进阶需求**：二级审批、幂等与并发控制、多明细分类预算均未做。
- **演示账号共用密码 `123456`**：仅用于本地演示，正式部署必须修改。

## 官方入口

- [Spring Boot 官方文档](https://docs.spring.io/spring-boot/index.html)
- [Spring Initializr 工程生成器](https://start.spring.io/)
- [MyBatis Spring Boot Starter 文档](https://mybatis.org/spring-boot-starter/mybatis-spring-boot-autoconfigure/)
