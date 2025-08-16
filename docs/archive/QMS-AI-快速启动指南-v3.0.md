# 🚀 QMS-AI系统快速启动指南 v3.0

> **更新时间**: 2025-08-06  
> **版本**: v3.0 (当前运行版本)  
> **状态**: ✅ 已验证可用

## 📋 系统架构概览

### 🎯 当前运行的服务
| 服务名称 | 端口 | 状态 | 功能描述 |
|---------|------|------|----------|
| **Redis缓存** | 6380 | ✅ 运行中 | 数据缓存和会话存储 |
| **配置中心** | 8082 | ✅ 运行中 | 轻量级配置管理服务 |
| **认证服务** | 8084 | ✅ 运行中 | 用户认证和权限管理 |
| **AI聊天服务** | 3004 | ✅ 运行中 | 8个AI模型聊天功能 |
| **应用端前端** | 8081 | ✅ 运行中 | 主要业务操作界面 |

---

## 🚀 一键启动 (推荐)

### 方式1: 使用pnpm启动脚本
```bash
# 在项目根目录执行
start-with-pnpm.bat
```

### 方式2: 使用npx命令
```bash
# 启动Redis
npx pnpm run redis:start

# 启动后端服务
npx pnpm run dev:backend

# 启动应用端前端
npx pnpm run dev:app
```

---

## 🔧 手动启动 (开发调试)

### 1. 启动Redis缓存 (必需)
```bash
npx pnpm run redis:start
```
**验证**: Redis运行在端口6380

### 2. 启动后端服务集群
```bash
npx pnpm run dev:backend
```
**包含服务**:
- 配置中心 (8082)
- 认证服务 (8084) 
- AI聊天服务 (3004)

### 3. 启动前端应用
```bash
npx pnpm run dev:app
```
**访问地址**: http://localhost:8081

---

## 🏥 健康检查

### 快速检查所有服务
```bash
# 检查Redis
npx pnpm run redis:status

# 检查后端服务
curl http://localhost:8082/health  # 配置中心
curl http://localhost:8084/health  # 认证服务
curl http://localhost:3004/health  # AI聊天服务

# 检查前端
curl http://localhost:8081/        # 应用端
```

### 预期响应
- **Redis**: 显示进程信息
- **后端服务**: `{"status":"healthy","service":"..."}`
- **前端**: HTML页面内容

---

## 🌐 访问地址

### 主要入口
- **🎨 应用端**: http://localhost:8081/
  - 工作流搭建页面
  - AI聊天界面
  - 系统管理

### API端点
- **💬 AI聊天**: http://localhost:3004/api/chat/
- **🔐 认证**: http://localhost:8084/api/auth/
- **⚙️ 配置**: http://localhost:8082/api/

---

## 🛠️ 故障排除

### 常见问题

#### 1. Redis连接失败
```bash
# 检查Redis状态
npx pnpm run redis:status

# 重启Redis
npx pnpm run redis:stop
npx pnpm run redis:start
```

#### 2. 端口被占用
```bash
# 检查端口占用 (Windows)
netstat -ano | findstr ":8081"
netstat -ano | findstr ":8082"
netstat -ano | findstr ":8084"
netstat -ano | findstr ":3004"
netstat -ano | findstr ":6380"

# 杀死占用进程
taskkill /PID <进程ID> /F
```

#### 3. 前端404错误
- 确保后端服务已启动
- 检查代理配置 (vite.config.js)
- 清除浏览器缓存

#### 4. AI模型配置加载失败
- 检查配置中心是否运行 (8082)
- 验证配置文件: `backend/nodejs/config/data/ai_models.json`

---

## 📁 重要文件位置

### 配置文件
```
backend/nodejs/
├── .env                           # 环境变量配置
├── config/data/
│   ├── ai_models.json            # AI模型配置
│   ├── services.json             # 服务配置
│   └── system.json               # 系统配置
└── config/redis/
    └── redis-persistent.conf     # Redis配置
```

### 启动脚本
```
项目根目录/
├── start-with-pnpm.bat          # 一键启动脚本
├── package.json                 # pnpm工作空间配置
└── pnpm-workspace.yaml         # 工作空间定义
```

---

## 🔄 版本更新记录

### v3.0 (2025-08-06) - 当前版本
- ✅ Redis端口: 6379 → 6380
- ✅ 应用端: 8080 → 8081
- ✅ 配置中心: 3003 → 8082
- ✅ 统一使用轻量级配置服务
- ✅ 简化服务架构，移除冗余服务

### v2.0 (历史版本)
- 配置端: 8072 → 8073
- 应用端: 8080 → 8082
- 配置中心: 3003 → 3005

---

## 💡 开发提示

### 推荐开发流程
1. 启动基础设施 (Redis)
2. 启动后端服务集群
3. 启动前端应用
4. 使用健康检查验证

### 性能优化
- Redis缓存已启用
- 数据库连接池已配置
- 前端代理已优化

### 调试技巧
- 查看服务日志: 各服务终端窗口
- 使用浏览器开发者工具
- 检查网络请求和响应

---

## 📞 技术支持

如遇问题，请检查：
1. 所有服务是否正常启动
2. 端口是否被占用
3. 配置文件是否正确
4. 网络连接是否正常

**文档更新**: 本指南基于实际运行状态编写，确保准确性。
