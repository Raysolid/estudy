# 编码规范

## 后端（Java / Spring Boot）

### 依赖注入

- 使用 `@Resource`（Jakarta），**禁止** `@Autowired`

### 异常处理

- 业务异常统一抛 `BusinessException(ResultCode.XXX)`
- **禁止**在 Controller 中直接返回错误码

### 统一响应

- 所有 API 返回 `Result<T>`，结构：`{ code, info, status, data }`
- 分页用 `PaginationResult<T>` + `SimplePage`

### 事务管理

- ServiceImpl 方法加 `@Transactional(rollbackFor = Exception.class)`

### DTO 转换

- 使用 `CopyUtils.copy(source, TargetClass.class)` 进行 PO/VO 互转

### 错误信息

- 使用中文，如 `"账号或密码错误"`、`"积分不足"`

### 密码处理

- `StrUtils.md5()`，不加盐

### MyBatis

- Mapper 接口在 `com.estudy.mappers`（所有模块共用）
- XML 文件在 `estudy-common/src/main/resources/com/estudy/mappers/`
- 继承 `BaseMapper<T,P>` 获得通用 CRUD
- 动态条件使用 `QueryParams` 的 `params` Map

### 命名约定

| 类型 | 规则 | 示例 |
|---|---|---|
| 包名 | `com.estudy.*` | `com.estudy.service.impl` |
| 类名 | PascalCase | `UserServiceImpl` |
| 方法 | camelCase | `getUserInfo` |
| 常量 | UPPER_SNAKE_CASE | `KEY_TOKEN` |
| 数据库列 | snake_case → camelCase | `create_time` → `createTime` |
| PO 类 | 实体名 | `User`, `Paper` |
| VO 类 | 后缀 `VO`/`Data`/`Result` | `ExamReport`, `StatsData` |
| DTO 类 | 后缀 `Dto` | `UserDto`, `SettingDto` |

---

## 前端（Vue 3）

### 请求封装

- 移动端：`utils/request.js`
- 管理后台：`src/utils/Request.js`
- **禁止**直接调用 `uni.request` 或 `axios`

### 状态管理

- 使用 Pinia，Store 文件在 `stores/` 目录

### 文件命名

- 组件：PascalCase（`UserList.vue`）
- 工具函数：camelCase（`formatDate.js`）

### 样式

- 使用 `uni.scss` 全局变量（移动端）
- 组件内用 scoped 样式
- CSS class：kebab-case

---

## Git 提交

```
feat: 新功能
fix: 修复
docs: 文档
refactor: 重构
chore: 构建/工具
```
