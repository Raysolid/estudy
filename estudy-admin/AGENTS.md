# Admin Panel Module

## 技术栈

Vue 3 + Element Plus + Vite 7 + Vue Router 4 + Axios + ECharts

## 开发

```bash
npm install
npm run dev    # 端口 4060，代理 /api → localhost:8091
```

## 目录结构

```
src/
├── views/         # 页面（按功能模块分文件夹）
├── components/    # 公共组件
├── router/        # Vue Router 配置
├── utils/         # 工具函数（Request.js, Api.js, Message.js）
├── assets/        # 静态资源
└── main.js        # 入口
```

## Do's and Don'ts

### Do

- 请求统一走 `src/utils/Request.js`，通过 `src/utils/Api.js` 配置基础 URL
- 页面用 PascalCase 命名（`UserList.vue`、`Paper.vue`）
- 表单弹窗用 Element Plus 的 `el-dialog`
- 富文本编辑用 SunEditor（`@sun-editor/vue-edit`）

### Don't

- **禁止**直接调用 `axios`
- **禁止**在页面中硬编码 API 地址
- **禁止**在 `src/` 外创建业务代码

## 页面路由

| 路径 | 页面 | 说明 |
|---|---|---|
| `/login` | `Login.vue` | 登录 |
| `/home` | `Home.vue` | 首页（默认重定向） |
| `/category` | `Category.vue` | 分类管理 |
| `/exam/paper` | `Paper.vue` | 试卷管理 |
| `/exam/question` | `Question.vue` | 题目管理 |
| `/exam/compose` | `ComposePaper.vue` | 组卷系统 |
| `/user/list` | `UserList.vue` | 用户管理 |
| `/user/examRecord` | `ExamRecord.vue` | 考试记录 |
| `/system` | `System.vue` | 系统设置 |
