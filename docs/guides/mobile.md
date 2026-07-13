# 移动端模块规范（uni-app）

## 技术栈

Vue 3 + Pinia + uni-app 3.0

## 开发

```bash
npm install
npm run dev:h5           # H5
npm run dev:mp-weixin    # 微信小程序
```

## 目录结构

```
pages/         # 页面（按功能模块分文件夹）
stores/        # Pinia Store（userStore, questionStore, taskStore）
utils/         # 工具函数（request.js, api.js, permission.js）
static/        # 静态资源
uni_modules/   # 内置组件
```

## 必须做

- 请求统一走 `utils/request.js`，通过 `utils/api.js` 配置基础 URL
- 路由拦截在 `utils/permission.js`，自动检查登录状态
- 使用 `uni_modules/` 下的内置组件（uni-forms, uni-popup 等）
- 页面样式用 scoped，全局变量用 `uni.scss`

## 禁止做

- **禁止**直接调用 `uni.request`
- **禁止**在页面中硬编码 API 地址
- **禁止**修改 `utils/request.js` 的拦截器逻辑（除非明确要求）

## 页面路由

| 页面 | 路径 |
|---|---|
| 首页 | `pages/index/index` |
| 登录 | `pages/login/login` |
| 分类 | `pages/category/category` |
| 搜索 | `pages/category/search` |
| 试卷列表 | `pages/category/paperlist` |
| 试卷详情 | `pages/exam/paper-detail` |
| 答题 | `pages/exam/question` |
| 错题本 | `pages/wrong/wrong` |
| 错题详情 | `pages/wrong/wrong-detail` |
| 我的 | `pages/my/my` |
| 编辑资料 | `pages/my/edit` |
| 修改密码 | `pages/my/changePassword` |
| 学习记录 | `pages/report/report` |
| 考试结果 | `pages/report/report-detail` |
| AI 批改 | `pages/agent/agent` |
| 批改结果 | `pages/agent/result` |
| 题目详情 | `pages/question/detail` |
