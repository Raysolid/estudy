# Backend Module

## 模块结构

```
estudy-backend/
├── estudy-common/    # 业务逻辑（实体、Service、Mapper、工具类）
├── estudy-web/       # 用户端 API（端口 8090）
└── estudy-admin/     # 管理端 API（端口 8091）
```

**核心原则**：`estudy-common` 包含所有业务逻辑，`estudy-web`/`estudy-admin` 只写 Controller。

## 构建与运行

```bash
# 编译（先 common，再 web/admin）
mvn clean install -DskipTests

# 启动用户端
cd estudy-web && mvn spring-boot:run

# 启动管理端
cd estudy-admin && mvn spring-boot:run
```

## 包结构（com.estudy）

| 包 | 职责 |
|---|---|
| `annotation/` | 自定义注解（如 `@GlobalInterceptor`） |
| `aspect/` | AOP 切面（如 `OperationAspect`） |
| `component/` | 组件（`RedisComponent`, `FileComponent`） |
| `config/` | 配置类（`AppConfig`, `RedisConfig`, `AsyncConfig`） |
| `entity/constants/` | 常量 |
| `entity/dto/` | 数据传输对象 |
| `entity/enums/` | 枚举（`ResultCode`, `TypeEnum`, `DifficultyEnum`） |
| `entity/po/` | 持久化对象（对应数据库表） |
| `entity/query/` | 查询参数（`QueryParams`, `SimplePage`） |
| `entity/vo/` | 视图对象（`Result`, `PaginationResult`） |
| `exception/` | 异常（`BusinessException`, `GlobalExceptionHandler`） |
| `mappers/` | MyBatis Mapper 接口 |
| `service/` + `impl/` | Service 接口与实现 |
| `utils/` | 工具类（`StrUtils`, `RedisUtils`, `MailUtils`） |

## Do's and Don'ts

### Do

- Controller 用 `@GlobalInterceptor` 标注权限（`checkLogin`/`checkAdmin`）
- ServiceImpl 方法加 `@Transactional(rollbackFor = Exception.class)`
- 异步任务用 `@Async("cozeTaskExecutor")`
- 日期用 `java.util.Date` + `@JsonFormat(pattern="yyyy-MM-dd HH:mm:ss", timezone="GMT+8")`

### Don't

- 不要在 Controller 里写业务逻辑
- 不要使用 `@Autowired`
- 不要直接返回错误码，用 `BusinessException`
- 不要在代码中硬编码密码/密钥

## 编码规范

详见 @docs/coding-style.md
