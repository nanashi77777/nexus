# Vue 3 前端从零学习路线

这份文档专为**没有前端基础**但希望使用 Vue 3 开发 NEXUS 项目前端的开发者设计。

## 第一阶段：前端三剑客 (基础必修，预计 1-2 周)

在学习 Vue 之前，必须掌握 Web 开发的基础。

### 1. HTML5 (结构)
- 语义化标签 (`<header>`, `<nav>`, `<section>`, `<article>`)。
- 表单元素 (`<input>`, `<select>`, `<button>`)。

### 2. CSS3 (样式)
- **盒模型**: margin, border, padding, content。
- **布局**: **Flexbox** (弹性盒子，重点掌握) 和 Grid (网格布局)。
- **选择器**: 类选择器, ID 选择器, 伪类 (`:hover`).

### 3. JavaScript (ES6+ 核心，重中之重)
Vue 3 深度依赖现代 JavaScript 特性。
- **变量声明**: `let` vs `const` (不再使用 `var`)。
- **箭头函数**: `() => {}`。
- **解构赋值**: `const { data } = response`。
- **模块化**: `import` / `export`。
- **异步编程**: `Promise`, `async` / `await` (处理后端 API 请求必备)。
- **数组方法**: `map`, `filter`, `reduce`, `forEach`。

**推荐资源**:
- [MDN Web Docs](https://developer.mozilla.org/zh-CN/) (权威文档)
- [阮一峰 ES6 入门](https://es6.ruanyifeng.com/)

---

## 第二阶段：Vue 3 核心概念 (预计 2 周)

建议直接学习 **Composition API (组合式 API)** 和 `<script setup>` 语法糖，这是 Vue 3 的主流写法。

### 1. 基础语法
- **创建应用**: `createApp`。
- **模板语法**: 插值 `{{ }}`, 指令 (`v-if`, `v-for`, `v-bind`, `v-on`, `v-model`)。

### 2. 响应式系统 (Reactivity)
- **核心 API**: `ref` (基本类型), `reactive` (对象类型)。
- **计算属性**: `computed`。
- **监听器**: `watch`, `watchEffect`。

### 3. 组件化开发
- **组件通信**: `props` (父传子), `emit` (子传父)。
- **生命周期**: `onMounted`, `onUnmounted`。
- **插槽**: `slot`。

**推荐资源**:
- [Vue.js 官方文档](https://cn.vuejs.org/) (文档非常优秀，建议通读)

---

## 第三阶段：Vue 生态系统 (预计 2 周)

开发真实项目需要配合周边工具。

### 1. 构建工具：Vite
- 极速的开发服务器，Vue 官方推荐。
- 学习如何创建项目: `npm create vue@latest`。

### 2. 路由：Vue Router 4
- 实现单页应用 (SPA) 的页面跳转。
- 路由配置, 嵌套路由, 路由守卫 (用于登录拦截)。

### 3. 状态管理：Pinia
- Vuex 的替代者，更简单、更符合组合式 API 风格。
- Store 的定义 (State, Getters, Actions)。

### 4. HTTP 请求：Axios
- 发送 GET/POST 请求对接后端接口。
- **拦截器**: 统一处理请求头 (Token) 和响应错误。

---

## 第四阶段：UI 组件库与样式 (预计 1 周)

不要重复造轮子，使用成熟的组件库。

### 1. Element Plus (推荐)
- 一套基于 Vue 3 的桌面端组件库，非常适合后台管理系统。
- 常用组件: Table, Form, Dialog, Pagination, Menu。

### 2. CSS 框架 (可选)
- **Tailwind CSS**: 原子化 CSS，提高开发效率。
- **Sass/Scss**: CSS 预处理器。

---

## 第五阶段：对接 NEXUS 后端实战

将前端与 NEXUS 的 Spring Boot 后端连接起来。

### 1. 跨域处理 (CORS)
- 理解浏览器的同源策略。
- 在 Spring Boot 后端配置 `@CrossOrigin` 或全局 CORS 配置，或者在 Vite 中配置 Proxy 代理。

### 2. 认证与授权
- **登录流程**: 前端发送账号密码 -> 后端验证并返回 Token (Sa-Token) -> 前端保存 Token (localStorage) -> 后续请求在 Header 中携带 Token。
- **路由守卫**: 未登录用户访问受保护页面时跳转至登录页。

### 3. 实战任务
1. **搭建项目**: 使用 Vite + Vue 3 + TypeScript (可选) + Pinia + Vue Router 初始化项目。
2. **登录页**: 使用 Element Plus 的 Form 组件制作登录表单，调用后端 `/login` 接口。
3. **资源列表**: 调用后端资源列表接口，使用 Element Plus 的 Table 组件展示数据。

## 学习建议

1. **多写代码**: 只有亲手敲过代码才能真正理解。
2. **看官方文档**: Vue 的文档是所有框架中写得最好的之一。
3. **拥抱 TypeScript**: 虽然学习曲线稍陡，但它能极大减少代码错误，是大型项目的标配 (NEXUS 后端是强类型的 Java，前端配合 TS 食用更佳)。
