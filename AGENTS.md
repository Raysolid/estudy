# AGENTS.md

## 项目

Estudy — 在线学习考试系统，支持移动端（uni-app）和 Web 管理后台。

## 技术栈

Spring Boot 3.5 + MyBatis + MySQL + Redis（Java 17）
Vue 3 + Pinia（移动端）
Vue 3 + Element Plus（管理后台）

## 目录结构

```
estudy-backend/     后端 Maven 多模块
├── estudy-common/  业务逻辑（实体、Service、Mapper）
├── estudy-web/     用户端 API（8090）
└── estudy-admin/   管理端 API（8091）
estudy-front/       移动端 uni-app
estudy-admin/       管理后台 Vue 3
docs/               文档
```

## 基本规则

- 不要把 `.env` 提交到 Git
- 不要在代码中硬编码密码/密钥/Token
- 不要自动 push 到远程仓库——只在用户明确说"推送"时才 git push
- 修改后手动验证功能，提交前确认编译通过
- 保持改动最小化，遵循现有模式

## 构建

```bash
# 后端编译
cd estudy-backend && mvn clean install -DskipTests

# 用户端 API
cd estudy-backend/estudy-web && mvn spring-boot:run

# 管理端 API
cd estudy-backend/estudy-admin && mvn spring-boot:run

# 移动端
cd estudy-front && npm install && npm run dev:h5

# 管理后台
cd estudy-admin && npm install && npm run dev
```

## 文档

编码规范: docs/guides/coding-style.md
后端模块: docs/guides/backend.md
移动端: docs/guides/mobile.md
管理后台: docs/guides/admin.md
AI 设计: docs/design/AI-INTEGRATION-DESIGN.md
