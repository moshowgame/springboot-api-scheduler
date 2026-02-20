# API监控系统前端 - Vue 3 + View UI Plus

这是一个基于 Vue 3 + View UI Plus 的企业级API监控系统前端应用，提供了完整的API监控、调度、断言验证和警报功能。

## 🚀 技术栈

- **框架**: Vue 3.4+ (Composition API)
- **UI组件库**: View UI Plus 1.3+
- **路由**: Vue Router 4
- **状态管理**: Pinia 2
- **构建工具**: Vite 5
- **HTTP客户端**: Axios
- **时间处理**: Day.js
- **图表**: ECharts
- **样式**: SCSS

## 📁 项目结构

```
src/
├── api/                    # API接口定义
│   ├── auth.js            # 认证相关接口
│   └── tasks.js           # 任务管理接口
├── assets/                # 静态资源
├── components/            # 公共组件
├── router/                # 路由配置
│   └── index.js
├── stores/                # 状态管理
│   └── auth.js           # 认证状态
├── styles/                # 样式文件
│   ├── index.scss        # 全局样式
│   └── variables.scss    # 变量定义
├── utils/                 # 工具类
│   ├── request.js        # HTTP请求配置
│   └── storage.js        # 本地存储工具
├── views/                 # 页面组件
│   ├── Layout/           # 主布局
│   ├── Login/            # 登录页面
│   ├── Dashboard/        # 仪表板
│   ├── TaskManagement/   # 任务管理
│   ├── ResponseMonitoring/ # 响应监控
│   ├── AlertManagement/  # 警报管理
│   ├── AssertionConfig/  # 断言配置
│   ├── ApiTest/          # API测试
│   ├── Profile/          # 个人中心
│   └── NotFound/         # 404页面
├── App.vue               # 根组件
└── main.js              # 应用入口
```

## 🎨 设计风格

### 企业级沉稳风格
- **主色调**: 蓝色系 (#2563eb) - 专业、稳重
- **辅助色**: 灰色系 - 中性、平和
- **状态色**: 成功绿、警告橙、错误红
- **布局**: 左侧导航 + 顶部栏的经典后台布局
- **字体**: 系统字体栈，确保各平台一致性
- **阴影**: 轻微阴影增加层次感
- **圆角**: 适度圆角提升现代感

### 响应式设计
- 适配桌面端 (1200px+)
- 适配平板端 (768px-1199px)
- 适配移动端 (<768px)

## 🔧 开发环境

### 环境要求
- Node.js 16+
- npm 或 yarn

### 安装依赖
```bash
cd src/main/resources/static
npm install
```

### 开发模式
```bash
npm run dev
```

### 构建生产版本
```bash
npm run build
```

### 代码检查
```bash
npm run lint
```

## 🌐 功能特性

### 🔐 用户认证
- 用户登录/退出
- 基于Token的认证
- 权限验证和路由守卫
- 记住登录状态

### 📊 仪表板
- 系统概览统计
- 任务状态分布图表
- 执行成功率趋势
- 最近执行记录
- 系统警报提示

### 📋 任务管理
- 创建/编辑/删除任务
- 任务启动/暂停/立即执行
- 批量操作支持
- 任务复制功能
- Cron表达式配置助手
- 任务状态监控

### 📈 响应监控
- 执行记录查看
- 多维度筛选 (任务、状态、时间)
- 响应详情展示
- 断言结果查看
- 数据导出功能

### 🚨 警报管理
- 警报规则配置
- 失败率阈值设置
- Webhook通知配置
- 警报记录查看

### 🔍 断言配置
- HTTP状态码断言
- JSON内容断言
- JSON路径断言
- 批量断言管理

### 🧪 API测试
- 在线API测试工具
- 请求构造器
- 响应分析器

### 👤 个人中心
- 个人信息管理
- 密码修改
- 系统设置
- 通知偏好配置

## 🔌 API集成

### 认证接口
```javascript
// 登录
POST /api/auth/login
{ username, password }

// 检查认证状态
GET /api/auth/check

// 退出登录
POST /api/auth/logout
```

### 任务管理接口
```javascript
// 获取任务列表
GET /api/tasks

// 创建任务
POST /api/tasks

// 更新任务
PUT /api/tasks/{id}

// 删除任务
DELETE /api/tasks/{id}

// 启动/暂停任务
POST /api/tasks/{id}/start
POST /api/tasks/{id}/pause
```

### 响应监控接口
```javascript
// 获取响应记录
GET /api/responses?page=1&size=20&taskId=1&status=SUCCESS
```

## 📱 响应式特性

### 移动端适配
- 侧边栏自动折叠
- 表格横向滚动
- 按钮组垂直排列
- 卡片式布局优化

### 交互优化
- 触摸友好的按钮尺寸
- 手势支持
- 键盘导航支持
- 无障碍访问优化

## 🎯 性能优化

### 代码分割
- 路由级别的懒加载
- 组件按需加载
- 第三方库分离

### 资源优化
- 图片懒加载
- 静态资源CDN
- Gzip压缩
- 缓存策略

### 用户体验
- 加载状态指示
- 错误边界处理
- 离线降级策略

## 🔒 安全特性

### 前端安全
- XSS防护
- CSRF保护
- 敏感信息加密
- 安全的本地存储

### 权限控制
- 路由级权限验证
- 组件级权限控制
- API访问权限管理

## 📝 开发规范

### 代码风格
- 使用 Composition API
- TypeScript类型支持 (可选)
- ESLint代码检查
- Prettier代码格式化

### 组件规范
- 单一职责原则
- 可复用性设计
- 统一的命名规范
- 完善的注释文档

### 状态管理
- 模块化状态设计
- 清晰的数据流
- 持久化策略
- 错误处理机制

## 🚀 部署说明

### 构建配置
构建后的文件会输出到 `../templates` 目录，与Spring Boot模板系统集成。

### 环境变量
```bash
# API地址
VITE_API_BASE_URL=http://localhost:8080/api

# 其他配置...
```

### Nginx配置
```nginx
server {
    listen 80;
    server_name your-domain.com;
    
    location / {
        root /path/to/templates;
        try_files $uri $uri/ /index.html;
    }
    
    location /api {
        proxy_pass http://localhost:8080;
        proxy_set_header Host $host;
        proxy_set_header X-Real-IP $remote_addr;
    }
}
```

## 🤝 贡献指南

1. Fork 项目
2. 创建特性分支
3. 提交变更
4. 推送到分支
5. 创建 Pull Request

## 📄 许可证

MIT License

## 📞 技术支持

如有问题，请通过以下方式联系：

- 提交 Issue
- 发送邮件
- 技术文档

---

**注意**: 这是一个完整的企业级前端应用，建议在生产环境中使用时进行充分的测试和安全评估。