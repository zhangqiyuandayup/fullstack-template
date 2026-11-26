# 前端工程

校园经费管理系统的页面，基于 Vue 3 + Vite，调用 `backend` 提供的接口。
项目流程与 Git 规范见[根 README](../README.md)。

## 实际使用的技术与版本

| 组件 | 版本 |
|---|---|
| 运行时 | Node.js v24.22.0 |
| 包管理器 | npm 11.19.1 |
| 构建工具 | Vite 8.4.0 |
| 框架 | Vue 3.5.46 |
| 路由 | vue-router 4.6.5 |
| 请求库 | axios 1.21.1 |
| UI 组件库 | Element Plus 2.14.8 |
| 语言 | JavaScript（未使用 TypeScript，因此没有类型检查步骤） |

## 目录结构

```
frontend/
├── index.html
├── package.json
├── package-lock.json            锁文件，需提交
├── vite.config.js               含开发代理配置
└── src/
    ├── main.js                  挂载路由与 Element Plus
    ├── App.vue                  路由出口
    ├── style.css                全局基础样式
    ├── router/index.js          两个路由 + 登录状态守卫
    ├── api/
    │   ├── request.js           axios 封装：统一拆包、401 跳登录
    │   ├── auth.js              登录 / 登出 / 当前用户
    │   ├── project.js           项目查询
    │   └── application.js       申请单查询与五个操作
    └── views/
        ├── Login.vue            登录页
        └── Workspace.vue        工作台（经费看板 + 申请列表 + 操作）
```

## 运行前准备

1. **Node.js 20.19+ 或 22.12+**（Vite 8 的要求，本机实测 v24.22.0 可用）。
2. **后端已启动**（见 [backend/README.md](../backend/README.md)）。前端本身不连数据库，所有数据都来自后端接口。

## 安装、启动与构建

命令均在 **frontend 目录** 下执行。

```powershell
cd frontend

# 安装依赖（首次或依赖变化后）
npm install

# 开发启动（默认 http://localhost:5173）
npm run dev

# 生产构建（产物在 dist/，该目录不提交）
npm run build

# 预览构建产物
npm run preview
```

> 停止开发服务器请按 `Ctrl + C`。**修改 `vite.config.js` 或安装/升级依赖后必须重启**，配置不参与热更新。
> 若页面出现 `Failed to parse source ... Install @vitejs/plugin-vue` 这类报错，先尝试删除
> `node_modules/.vite` 缓存并重启开发服务器。

## 后端地址如何配置

没有硬编码地址，靠 **Vite 开发代理**转发：

```js
// vite.config.js
server: {
  port: 5173,
  proxy: {
    '/api': {
      target: 'http://localhost:8080',
      changeOrigin: true
    }
  }
}
```

- 前端代码里所有请求都以 `/api` 开头（见 `src/api/request.js` 的 `baseURL`）
- 浏览器看到的是 `http://localhost:5173/api/...`，属同源请求，**不产生跨域问题**
- 开发服务器把它转发到 `http://localhost:8080/api/...`
- **会话 Cookie 因此能正常携带**，后端无需额外配置 CORS

若要指向其他后端地址，只改 `target` 即可，前端代码不用动。

> 生产部署时没有 Vite 代理，需要由 Nginx 之类的服务器把 `/api` 反向代理到后端，
> 或改用完整地址并让后端开放跨域。

## 如何验证页面确实发出了请求

**不要只看页面有没有变化**，按以下步骤核对：

1. 浏览器打开 `http://localhost:5173`，用 `xiaolin / 123456` 登录。
2. 按 `F12` 打开开发者工具，切到 **Network** 面板，筛选 `Fetch/XHR`。
3. 找到 `login` 请求，确认：
   - **Request URL 是 `http://localhost:5173/api/auth/login`**（是 5173，不是 8080，说明走了代理）
   - Status 为 `200`
   - 响应头含 `Set-Cookie: JSESSIONID=...`
   - 响应体为 `{"code":0,"message":"success","data":{"id":1,...}}`
4. 进入工作台后，应再看到 `project`、`applications` 两个请求，状态均为 200。
5. 点任意操作按钮（如「提交」），应看到对应的 `POST .../submit` 请求，且
   **响应 200 后经费卡片数字发生变化**。

## 演示账号

后端初始化了四个账号，密码均为 `123456`：

| 用户名 | 姓名 | 角色 | 登录后可见的操作 |
|---|---|---|---|
| xiaolin | 小林 | MEMBER | 新建、提交、取消自己的申请 |
| wang | 王老师 | TEACHER | 批准、拒绝别人的待审批申请 |
| licaiwu | 李财务 | FINANCE | 结算已批准的申请 |
| admin | 管理员 | ADMIN | 无业务操作按钮 |

**页面上的按钮是否显示，只是操作体验层；真正的权限校验在后端**。
即使绕过界面直接调用接口，越权操作也会返回 `403`。

## 页面功能

| 页面 | 路径 | 内容 |
|---|---|---|
| 登录页 | `/login` | 用户名密码登录，成功后写入会话并跳转工作台 |
| 工作台 | `/workspace` | 四张经费卡片（总预算/可用/冻结/已用）、守恒校验提示条、申请列表、按角色显示的五个操作按钮、新建申请对话框 |

工作台的数据流向：页面挂载时并发请求 `GET /api/projects/1` 与 `GET /api/applications?projectId=1`，
每次操作成功后重新拉取这两个接口，保证界面与数据库一致。

## 金额显示约定

`approvedAmount` 与 `settledAmount` 在后端可能为 `null`：

| 返回值 | 页面显示 | 业务含义 |
|---|---|---|
| `null` | `—` | 该环节尚未发生 |
| `0.00` | `0.00` | 该环节已发生，金额为零（如结算时实际没花钱） |

**两者必须能区分**，所以数据库里这两列允许 `NULL`，前端也分别渲染。

## 已验证的内容

- `npm run build` 构建通过。
- 登录成功后跳转工作台，页面显示当前用户信息。
- 工作台正确渲染四张经费卡片与申请列表。
- 新建草稿后经费三个金额不变；提交、批准、结算后数字随之变化。
- 切换账号后，操作按钮随之变化（成员看不到批准/结算按钮）。
- 浏览器 Network 面板确认请求地址为 `5173`、状态 200、响应头含 `Set-Cookie`。

## 仍未完成的内容

- **无自动化测试**：无单元测试或端到端测试。
- **无前端表单校验库**：目前是手写正则与判断，未使用 `el-form` 的校验规则。
- **单项目固定**：页面固定操作 1 号项目，未做项目切换。
- **未做 TypeScript 类型检查**：工程使用 JavaScript。
- **界面未做移动端适配**：仅在桌面宽度下检查过。
- **未实现进阶功能**：二级审批展示、操作历史展示、多明细录入均未做。

## 官方入口

- [Vue 3 中文文档](https://cn.vuejs.org/guide/quick-start.html)
- [Vite 官方文档](https://vite.dev/guide/)
- [Element Plus 中文文档](https://element-plus.org/zh-CN/)
