# QMS-AI 智能质量管理系统

## 🎯 系统概述

QMS-AI是一个基于人工智能的质量管理系统，集成了多模型AI对话、文档解析、工作流引擎、插件生态等功能，为质量管理提供智能化解决方案。

## 🏗️ 系统架构

### 核心组件

```
                    前端应用端 (Vue3 + Element Plus)
                           ↓ HTTP/WebSocket
                    API Gateway (统一网关)
                    ↓ 路由分发 ↓
    ┌─────────────────────────────────────────────────────┐
    ↓                ↓                ↓                   ↓
认证服务 (8084) ← 配置中心 (3003) ← 聊天服务 (3004) ← Coze Studio (3005)
    ↓                ↓                ↓                   ↓
用户管理         AI模型配置        多模型对话          插件生态系统
    ↓                ↓                ↓                   ↓
SQLite数据库    Redis缓存 (6379)  SQLite数据库      SQLite数据库
                     ↑
                 会话存储
                     ↑
    ┌─────────────────┼─────────────────┐
    ↓                ↓                 ↓
导出服务 (3008)  高级功能服务 (3009)  系统监控 (3010)
    ↓                ↓                 ↓
PDF/Word/Excel   分析/推荐/协作      健康检查/指标
导出功能         集成功能            性能监控
```

### 服务端口分配

| 服务名称 | 端口 | 功能描述 | 状态 |
|---------|------|----------|------|
| Redis缓存服务 | 6379 | 缓存和会话存储 | ✅ 运行中 |
| 配置中心服务 | 3003 | AI模型配置管理 | ✅ 运行中 |
| 聊天服务 | 3004 | 多模型聊天引擎 | ✅ 运行中 |
| Coze Studio服务 | 3005 | 插件生态+工作流引擎 | ✅ 运行中 |
| 评估服务 | 3006 | AI效果评测 | 🔄 待启动 |
| 导出服务 | 3008 | 对话记录导出 | ✅ 运行中 |
| 高级功能服务 | 3009 | 分析/推荐/协作/集成 | ✅ 运行中 |
| 系统监控服务 | 3010 | 健康检查/性能监控 | ✅ 运行中 |
| 前端应用端 | 8081 | Vue3智能问答界面 | ✅ 运行中 |
| 认证服务 | 8084 | 用户认证和权限管理 | ✅ 运行中 |
| API网关 | 8085 | 统一API网关 | ✅ 运行中 |

## 🚀 快速启动

### 环境要求

- Node.js 16+ 
- Redis 6+
- Python 3.8+ (可选，用于AI模型)

### 一键启动

```bash
# 启动所有后端服务
cd backend/nodejs
node start-all-services.js

# 启动前端服务
cd frontend/应用端
npm run dev
```

### 分步启动

```bash
# 1. 启动Redis
redis-server

# 2. 启动核心服务
cd backend/nodejs
node lightweight-config-service.js    # 配置中心
node chat-service.js                  # 聊天服务
node coze-studio-service.js           # Coze Studio
node auth-service.js                  # 认证服务

# 3. 启动扩展服务
node export-service-standalone.js     # 导出服务
node advanced-features-service.js     # 高级功能
node api-gateway.js                   # API网关
node system-monitor.js                # 系统监控

# 4. 启动前端
cd frontend/应用端
npm run dev
```

## 🔧 配置说明

### 环境变量

```bash
# API配置
VUE_APP_USE_GATEWAY=true              # 是否使用网关模式
VUE_APP_GATEWAY_URL=http://localhost:8085  # 网关地址

# 服务端点（直连模式）
VUE_APP_API_BASE_URL=http://localhost:3004
VUE_APP_CONFIG_API_URL=http://localhost:3003
VUE_APP_AUTH_API_URL=http://localhost:8084

# Redis配置
REDIS_HOST=localhost
REDIS_PORT=6379
REDIS_PASSWORD=

# 数据库配置
DB_PATH=./data/qms.db
```

### AI模型配置

系统支持8个AI模型：

**传音代理模型 (6个)**
- GPT-4o
- GPT-4o-mini  
- Claude-3.5-Sonnet
- Claude-3.5-Haiku
- Gemini-1.5-Pro
- Gemini-1.5-Flash

**DeepSeek直连模型 (2个)**
- DeepSeek-Chat
- DeepSeek-Coder

## 📊 功能特性

### 核心功能

1. **智能问答**
   - 多模型AI对话
   - 模型动态切换
   - 上下文记忆
   - 流式响应

2. **文档解析**
   - PDF/Excel/CSV/JSON/XML解析
   - 图片OCR识别
   - 批量文档处理
   - 结构化数据提取

3. **工作流引擎**
   - 可视化工作流设计
   - 插件生态系统
   - 异步任务处理
   - 执行状态跟踪

4. **知识库管理**
   - RAG向量检索
   - 文档入库
   - 智能问答
   - 知识图谱

### 扩展功能

1. **数据导出**
   - 对话记录导出
   - 多格式支持 (PDF/Word/Excel)
   - 批量导出
   - 自定义模板

2. **高级分析**
   - 对话质量分析
   - 用户行为分析
   - 性能指标统计
   - 智能推荐

3. **协作功能**
   - 多用户支持
   - 权限管理
   - 团队协作
   - 审批流程

4. **系统集成**
   - API接口
   - Webhook支持
   - 第三方集成
   - 数据同步

## 🔍 监控和运维

### 健康检查

```bash
# 检查所有服务状态
curl http://localhost:3010/api/services

# 检查系统指标
curl http://localhost:3010/api/metrics

# 检查系统概览
curl http://localhost:3010/api/overview
```

### 日志管理

```bash
# 查看服务日志
tail -f logs/chat-service.log
tail -f logs/config-service.log
tail -f logs/coze-studio.log

# 查看错误日志
tail -f logs/error.log
```

### 性能监控

- **内存使用**: 实时监控各服务内存占用
- **响应时间**: API响应时间统计
- **错误率**: 服务错误率监控
- **并发数**: 同时在线用户数

## 🛠️ 开发指南

### 目录结构

```
QMS01/
├── backend/
│   └── nodejs/
│       ├── lightweight-config-service.js    # 配置中心
│       ├── chat-service.js                  # 聊天服务
│       ├── coze-studio-service.js           # Coze Studio
│       ├── auth-service.js                  # 认证服务
│       ├── export-service-standalone.js     # 导出服务
│       ├── advanced-features-service.js     # 高级功能
│       ├── api-gateway.js                   # API网关
│       ├── system-monitor.js                # 系统监控
│       └── start-all-services.js            # 统一启动脚本
├── frontend/
│   └── 应用端/
│       ├── src/
│       │   ├── components/
│       │   │   └── SystemHealthMonitor.vue  # 健康监控组件
│       │   ├── config/
│       │   │   └── api.js                   # API配置
│       │   └── utils/
│       └── package.json
├── data/                                    # 数据目录
├── logs/                                    # 日志目录
└── README-SYSTEM.md                         # 系统文档
```

### API接口

#### 认证接口
- `POST /api/auth/login` - 用户登录
- `POST /api/auth/logout` - 用户登出
- `GET /api/auth/profile` - 获取用户信息

#### 聊天接口
- `POST /api/chat/send` - 发送消息
- `GET /api/chat/conversations` - 获取对话列表
- `GET /api/chat/models` - 获取模型列表

#### 配置接口
- `GET /api/configs/ai_models` - 获取AI模型配置
- `PUT /api/configs/ai_models/:id` - 更新模型配置

#### 工作流接口
- `GET /api/workflows` - 获取工作流列表
- `POST /api/workflows/execute` - 执行工作流

## 🔒 安全说明

### 认证机制
- JWT Token认证
- 会话管理
- 权限控制
- API密钥管理

### 数据安全
- 数据库加密
- 传输加密 (HTTPS)
- 敏感信息脱敏
- 访问日志记录

## 📈 性能优化

### 缓存策略
- Redis缓存
- 内存缓存
- 静态资源缓存
- API响应缓存

### 负载均衡
- 服务集群
- 请求分发
- 故障转移
- 健康检查

## 🐛 故障排除

### 常见问题

1. **服务启动失败**
   - 检查端口占用
   - 检查依赖安装
   - 查看错误日志

2. **数据库连接失败**
   - 检查Redis服务状态
   - 检查数据库文件权限
   - 验证连接配置

3. **AI模型调用失败**
   - 检查API密钥配置
   - 验证网络连接
   - 查看模型状态

### 联系支持

- 技术支持: support@qms-ai.com
- 文档中心: https://docs.qms-ai.com
- 问题反馈: https://github.com/qms-ai/issues

---

**QMS-AI Team** | 版本: v1.0.0 | 更新时间: 2024-12-19
