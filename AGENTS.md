# AGENTS.md

## 项目概览

Estudy 是一个在线学习考试系统，支持移动端（uni-app）和 Web 管理后台。后端是 Spring Boot 3.5 + MyBatis + MySQL + Redis，Java 17。前端是 Vue 3 + Pinia（移动端）和 Vue 3 + Element Plus（管理后台）。

**架构关键点**：`estudy-backend` 是 Maven 多模块，`estudy-common` 放所有业务逻辑（实体、Service、Mapper），`estudy-web`（用户端，8090）和 `estudy-admin`（管理端，8091）只写 Controller。

## 构建与测试

```bash
# 后端编译（先 common，再 web/admin）
cd estudy-backend && mvn clean install -DskipTests

# 启动用户端 API
cd estudy-backend/estudy-web && mvn spring-boot:run

# 启动管理端 API
cd estudy-backend/estudy-admin && mvn spring-boot:run

# 移动端开发
cd estudy-front && npm install && npm run dev:h5

# 管理后台开发
cd estudy-admin && npm install && npm run dev
```

**目前无自动化测试。** 修改后手动验证功能。提交前确认编译通过。

## 代码风格

### 后端（Java）

- **DI 用 `@Resource`**，不要用 `@Autowired`
- **异常**：抛 `BusinessException(ResultCode.XXX)`，不要直接返回错误码
- **统一响应**：所有 API 返回 `Result<T>`
- **分页**：用 `PaginationResult<T>` + `SimplePage`
- **事务**：ServiceImpl 方法加 `@Transactional(rollbackFor = Exception.class)`
- **DTO 转换**：用 `CopyUtils.copy()`
- **错误信息**：用中文，如 `"账号或密码错误"`
- **密码**：`StrUtils.md5()`，不加盐

### 前端（Vue 3）

- **请求**：统一走 `utils/request.js`（移动端）或 `src/utils/Request.js`（管理后台），禁止直接调用 `uni.request` 或 `axios`
- **状态管理**：Pinia（`stores/` 目录）
- **文件命名**：PascalCase 组件（`UserList.vue`），camelCase 工具函数

## 提交规范

```
feat: 新功能
fix: 修复
docs: 文档
refactor: 重构
chore: 构建/工具
```

## 安全红线

- **永远不要**把 `.env` 文件提交到 Git
- **永远不要**在代码中硬编码密码、密钥、Token
- **永远不要**自动 push 到远程仓库——只在用户明确说"推送"时才 `git push`
- 管理员密码通过环境变量注入，不写死在配置文件中
