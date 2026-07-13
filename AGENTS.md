# AGENTS.md - Estudy 项目开发规范

> 本文件是项目开发的核心规范文档，所有 AI Agent 和开发者在修改代码前必须阅读。

---

## 项目概述

Estudy 是一个在线学习考试系统，支持多端访问（移动端 uni-app、Web 管理后台），提供在线考试、智能批改、错题管理、学习记录等功能。

## 架构

```
estudy/
├── estudy-backend/              # 后端 Maven 多模块
│   ├── estudy-common/           # 公共模块 (实体、服务、Mapper、工具类)
│   ├── estudy-web/              # 用户端 API (端口 8090, 上下文 /api)
│   └── estudy-admin/            # 管理端 API (端口 8091, 上下文 /api)
├── estudy-front/                # 移动端 uni-app (H5/小程序/APP)
├── estudy-admin/                # 管理后台 Vue 3 + Element Plus (端口 4060)
├── docs/                        # 设计文档
└── AGENTS.md                    # 本文件
```

**依赖关系**: `estudy-web` 和 `estudy-admin` 都依赖 `estudy-common`。业务逻辑、实体、Mapper、Service 全部写在 `estudy-common` 中，`estudy-web`/`estudy-admin` 只写 Controller。

## 技术栈

| 模块 | 技术 | 版本 |
|---|---|---|
| 后端框架 | Spring Boot | 3.5.4 |
| Java | JDK | 17 |
| ORM | MyBatis (XML Mapper) | 3.0.5 |
| 数据库 | MySQL | 8.3 |
| 缓存 | Redis (Session/Token/业务缓存) | - |
| 移动端 | uni-app + Vue 3 + Pinia | - |
| 管理后台 | Vue 3 + Vite 7 + Element Plus + ECharts | - |
| HTTP 客户端 | OkHttp 4.12 (后端), Axios (前端) | - |

## 开发环境启动

```bash
# 后端编译 (先编译 common)
cd estudy-backend
mvn clean install -DskipTests

# 启动用户端 API (端口 8090)
cd estudy-backend/estudy-web
mvn spring-boot:run

# 启动管理端 API (端口 8091)
cd estudy-backend/estudy-admin
mvn spring-boot:run

# 移动端开发 (端口见 vite.config.js)
cd estudy-front
npm install
npm run dev:h5           # H5
npm run dev:mp-weixin    # 微信小程序

# 管理后台开发 (端口 4060)
cd estudy-admin
npm install
npm run dev
```

---

## Git 工作流

### 分支策略

- `main` 分支：生产就绪代码，仅通过明确指令推送
- 功能开发在本地完成，**只有用户明确说"推送"/"push"时才执行 `git push`**

### 提交规范

提交信息格式：

```
<type>: <description>
```

type 类型：
- `feat`: 新功能
- `fix`: 修复
- `docs`: 文档
- `refactor`: 重构
- `chore`: 构建/工具变更

示例：
```
feat: add AI grading endpoint
fix: resolve login token expiry issue
docs: update API integration guide
```

### ⚠️ 推送规则（重要）

**禁止自动推送代码到远程仓库。** 只有在用户明确要求时（如"推送"、"push"、"提交到远程"），才执行 `git push`。其余情况下只做本地 `git commit`。

---

## 后端开发规范 (Java / Spring Boot)

### 包结构

```
com.estudy
├── annotation/       # 自定义注解 (如 @GlobalInterceptor)
├── aspect/           # AOP 切面 (如 OperationAspect)
├── component/        # 组件 (RedisComponent, FileComponent)
├── config/           # 配置类 (AppConfig, RedisConfig, AsyncConfig)
├── entity/
│   ├── constants/    # 常量 (Constants.java)
│   ├── dto/          # 数据传输对象 (UserDto, SettingDto)
│   ├── enums/        # 枚举 (ResultCode, TypeEnum, DifficultyEnum)
│   ├── po/           # 持久化对象/实体 (User, Paper, Question)
│   ├── query/        # 查询参数 (QueryParams, SimplePage, BaseParam)
│   └── vo/           # 视图对象 (Result, PaginationResult, ExamReport)
├── exception/        # 异常 (BusinessException, GlobalExceptionHandler)
├── mappers/          # MyBatis Mapper 接口 (继承 BaseMapper<T,P>)
├── service/          # Service 接口
│   └── impl/         # Service 实现类
└── utils/            # 工具类 (StrUtils, RedisUtils, MailUtils)
```

### 代码风格

| 规则 | 说明 |
|---|---|
| **DI 注解** | 使用 `@Resource`（Jakarta），**不使用** `@Autowired` |
| **Lombok** | 实体类用 `@Data`，Service 用 `@Slf4j`，枚举用 `@Getter` |
| **异常处理** | 使用 `BusinessException` + `ResultCode` 枚举抛出业务异常 |
| **统一响应** | 所有 API 返回 `Result<T>`（code/info/data/status） |
| **分页** | 使用 `PaginationResult<T>`，分页参数用 `SimplePage` |
| **事务** | 在 ServiceImpl 方法上加 `@Transactional(rollbackFor = Exception.class)` |
| **异步** | 使用 `@Async("cozeTaskExecutor")` 或自定义线程池 |
| **日期** | 使用 `java.util.Date` + `@JsonFormat(pattern="yyyy-MM-dd HH:mm:ss", timezone="GMT+8")` |
| **DTO 转换** | 使用 `CopyUtils.copy()` 进行 PO/VO 之间的属性拷贝 |
| **密码** | 使用 `StrUtils.md5()` 进行 MD5 哈希 |
| **错误信息** | 使用中文，如 `"账号或密码错误"`、`"积分不足"` |
| **数据库列名** | snake_case，MyBatis 开启 `map-underscore-to-camel-case` |

### Controller 规范

```java
@RestController
@RequestMapping("/模块名")
public class XxxController {

    @Resource
    private XxxService xxxService;

    @GlobalInterceptor(checkLogin = true)  // 需要登录
    @GetMapping("/action")
    public Result<?> action(@RequestParam String param) {
        // 调用 Service，返回 Result
        return Result.success(xxxService.doSomething(param));
    }
}
```

- 用户端 Controller 在 `com.estudy.web.controller`
- 管理端 Controller 在 `com.estudy.admin.controller`
- `@GlobalInterceptor(checkLogin = true)` 用于需要登录的接口
- `@GlobalInterceptor(checkAdmin = true)` 用于需要管理员权限的接口
- 无拦截注解 = 公开接口

### MyBatis Mapper 规范

- Mapper 接口在 `com.estudy.mappers`（所有模块共用）
- XML 文件在 `estudy-common/src/main/resources/com/estudy/mappers/`
- 继承 `BaseMapper<T,P>` 获得通用 CRUD
- 自定义查询在 XML 中编写，使用 `resultMap` 显式映射
- 动态条件使用 `QueryParams` 的 `params` Map

### 统一响应结构

```json
{
  "code": 200,
  "info": "操作成功",
  "status": "OK",
  "data": { ... }
}
```

- 成功：`code=200`, `status="OK"`
- 失败：`code` 为错误码（如 401, 404, 999），`info` 为错误描述

---

## 前端开发规范 (Vue 3)

### estudy-front（移动端 uni-app）

- **状态管理**：Pinia，Store 文件在 `stores/` 目录
- **请求封装**：`utils/request.js`，API 基础配置在 `utils/api.js`
- **路由拦截**：`utils/permission.js`，自动检查登录状态
- **页面目录**：`pages/` 下按功能模块分文件夹
- **样式**：使用 `uni.scss` 全局变量，组件内用 scoped 样式
- **组件**：优先使用 `uni_modules/` 下的内置组件

### estudy-admin（管理后台）

- **UI 框架**：Element Plus，组件在 `src/components/`
- **路由**：Vue Router，配置在 `src/router/index.js`
- **请求封装**：`src/utils/Request.js`，API 配置在 `src/utils/Api.js`
- **页面**：`src/views/` 下按功能模块分文件夹
- **富文本**：使用 SunEditor（`@sun-editor/vue-edit`）

### 前端通用规范

- 文件命名：PascalCase（`UserList.vue`、`QuestionStore.js`）
- 组件 props：camelCase
- CSS class：kebab-case
- API 调用统一走 `Request` 封装，**禁止**直接调用 `uni.request` 或 `axios`

---

## 环境变量

所有敏感配置通过 `.env` 文件管理，**禁止**提交到 Git。

### 后端 (.env)

```env
DB_USERNAME=root
DB_PASSWORD=xxx
MAIL_USERNAME=xxx@qq.com
MAIL_PASSWORD=xxx
PROJECT_FOLDER=/path/to/estudy/
COZE_UPLOAD_URL=https://api.coze.cn/v1/files/upload
COZE_WORKFLOW_URL=https://api.coze.cn/v1/workflow/run
COZE_BEARER_TOKEN=xxx
COZE_WORKFLOW_ID=xxx
ADMIN_ACCOUNT=admin
ADMIN_PASSWORD=xxx
```

### 前端 (.env.development)

```env
VITE_API_BASE_URL=http://localhost:9090
```

---

## 测试

- 后端：目前无自动化测试，修改代码后手动验证功能
- 前端：目前无单元测试，修改后通过浏览器/模拟器验证
- 提交前：确认无编译错误（`mvn clean install -DskipTests`、`npm run build`）

## 安全要求

- **永不**将 `.env` 文件提交到 Git
- **永不**在代码中硬编码密码、密钥、Token
- 密码传输使用 MD5 哈希
- Token 认证通过 Redis 存储（7天有效期）
- API 权限通过 AOP 拦截器（`@GlobalInterceptor`）统一验证
- 管理员密码通过环境变量注入，不写死在配置文件中
