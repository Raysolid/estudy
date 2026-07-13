# Estudy 在线学习考试系统

一个功能完善的在线学习考试系统，支持多端访问（移动端、Web端），提供在线考试、智能批题、错题管理、学习记录等核心功能。

## 技术栈

| 层级 | 技术 |
|---|---|
| **后端** | Spring Boot 3.5.4、MyBatis、MySQL 8.3、Redis、JDK 17 |
| **移动端** | uni-app 3.0、Vue 3、Pinia |
| **管理后台** | Vue 3、Vite 7、Element Plus、ECharts |
| **AI能力** | 扣子（Coze）API |

## 项目结构

```
estudy/
├── estudy-backend/           # 后端 (Maven 多模块)
│   ├── estudy-common/        # 公共模块 (实体、服务、Mapper、工具类)
│   ├── estudy-web/           # 用户端 API (端口 8090)
│   └── estudy-admin/         # 管理端 API (端口 8091)
├── estudy-front/             # 移动端 (uni-app, 支持 H5/小程序/APP)
├── estudy-admin/             # 管理后台 (Vue 3 + Element Plus, 端口 4060)
└── AGENTS.md                 # 开发规范文档
```

## 环境变量配置

本项目使用环境变量管理敏感配置，请参考 `.env.example` 文件创建 `.env` 文件。

### 后端配置

在 `estudy-backend/estudy-web/.env` 和 `estudy-backend/estudy-admin/.env` 中配置：

```env
# MySQL
DB_USERNAME=root
DB_PASSWORD=your_password

# 邮箱 (SMTP)
MAIL_USERNAME=your_email@qq.com
MAIL_PASSWORD=your_smtp_authorization_code

# 项目目录
PROJECT_FOLDER=/path/to/estudy/

# Coze API (仅 estudy-web 需要)
COZE_UPLOAD_URL=https://api.coze.cn/v1/files/upload
COZE_WORKFLOW_URL=https://api.coze.cn/v1/workflow/run

# 管理员账号 (仅 estudy-admin 需要)
ADMIN_ACCOUNT=admin
ADMIN_PASSWORD=your_admin_password
```

### 前端配置

在 `estudy-front/.env.development` 中配置：

```env
VITE_API_BASE_URL=http://your-server-ip:9090
```

## 快速开始

### 后端

```bash
cd estudy-backend

# 编译
mvn clean install -DskipTests

# 启动用户端 (端口 8090)
cd estudy-web
mvn spring-boot:run

# 启动管理端 (端口 8091)
cd estudy-admin
mvn spring-boot:run
```

### 移动端

```bash
cd estudy-front

# 安装依赖
npm install

# H5 开发
npm run dev:h5

# 微信小程序开发
npm run dev:mp-weixin
```

### 管理后台

```bash
cd estudy-admin

# 安装依赖
npm install

# 启动开发服务器 (端口 4060)
npm run dev
```

## 核心功能

- **用户管理**: 注册登录、权限认证、Token管理
- **试卷管理**: 试卷创建、题目管理、组卷
- **在线考试**: 倒计时、自动评分、错题收集
- **错题本**: 自动收集错题、分类复习
- **学习记录**: 学习时长统计、连续签到、练习记录
- **智能批改**: 集成 Coze API，支持图片识别和AI批改
- **数据统计**: ECharts 可视化分析

## 多端部署

| 平台 | 命令 |
|---|---|
| H5 | `npm run build:h5` |
| 微信小程序 | `npm run build:mp-weixin` |
| Android | `npm run build:app-android` |
| iOS | `npm run build:app-ios` |

## 安全说明

- 所有敏感配置通过环境变量管理，**请勿**将 `.env` 文件提交到版本库
- 密码传输使用 MD5 加密
- Token 认证通过 Redis 存储，支持 7 天有效期
- API 请求通过 AOP 拦截器验证权限

## License

MIT
