# QMS-AI 智能质量管理系统

## 🎉 最新更新 (2025-08-22)

### ✅ 核心功能完成
- **🎛️ 配置驱动架构** - 配置端→应用端的完整开发路径
- **🤖 AI模型集成** - 8个主流AI模型统一管理和动态切换
- **🔐 企业级认证** - 完整的用户认证和权限管理体系
- **📊 智能监控** - 实时监控和健康检查体系
- **🔧 微服务架构** - Node.js微服务集群
- **📱 响应式前端** - Vue2/Vue3双技术栈支持
- **🚀 一键启动** - 统一的启动管理器和诊断工具
- **🛠️ 问题诊断** - 完整的故障诊断和修复工具

### 🤖 AI模型生态 (8个模型)
| 模型名称 | 推理能力 | 工具调用 | 图文识别 | 专业领域 | 状态 |
|---------|---------|---------|---------|---------|------|
| **GPT-4o** | ❌ | ✅ | ✅ | 多模态分析 | 🟢 生产就绪 |
| **O3** | ✅ | ✅ | ❌ | 复杂推理 | 🟢 生产就绪 |
| **Gemini 2.5 Pro Thinking** | ✅ | ✅ | ❌ | 思维链推理 | 🟢 生产就绪 |
| **Claude 3.7 Sonnet** | ❌ | ✅ | ❌ | 文档处理 | 🟢 生产就绪 |
| **DeepSeek R1** | ✅ | ❌ | ❌ | 数学推理 | 🟢 生产就绪 |
| **DeepSeek V3** | ❌ | ❌ | ❌ | 基础对话 | 🟢 生产就绪 |
| **DeepSeek Chat (V3-0324)** | ❌ | ✅ | ❌ | 通用对话 | 🟢 生产就绪 |
| **DeepSeek Reasoner (R1-0528)** | ✅ | ✅ | ❌ | 推理专家 | 🟢 生产就绪 |

### 🏗️ 系统架构特色
- **配置驱动**: 配置端统一管控，应用端动态响应
- **微服务化**: 服务独立部署，支持水平扩展
- **AI原生**: 深度集成AI能力，支持质量管理全场景
- **企业级**: 完整的监控、日志、备份、安全机制

## 🎯 项目简介

QMS-AI是一个基于**配置驱动架构**的现代化智能质量管理系统，采用"配置端管控设计，应用端场景实现"的清晰分工模式。系统深度集成6个主流AI模型，专注于质量管理场景的AI应用，提供灵活的配置驱动能力和企业级的智能化业务处理。

**核心理念**: 通过配置驱动实现业务逻辑的可视化配置，让非技术人员也能快速构建复杂的质量管理流程。

## 🏗️ 系统架构

### 🎛️ 双端分离架构
```
QMS-AI 配置驱动智能管理系统
├── 🎛️ 配置端 (8072) - 管控设计和配置管理
│   ├── 🤖 AI管理配置 (8个AI模型统一配置管理)
│   │   ├── 模型参数配置 (温度、令牌数、超时等)
│   │   ├── 模型特性管理 (推理/工具调用/图文识别)
│   │   ├── 模型切换控制 (动态切换默认模型)
│   │   ├── 性能监控面板 (响应时间、成功率统计)
│   │   └── 外网模型支持 (DeepSeek外网API)
│   ├── ⚙️ 系统管理 (属性、对象、视图、方法管理)
│   ├── 📚 字典管理 (数据字典配置)
│   ├── 📁 文件管理 (文件库和规则管理)
│   └── 🚀 应用管理 (应用配置和发布)
│
└── 🚀 应用端 (8080) - 配置驱动的业务应用
    ├── 🤖 AI智能管理 (实际AI业务场景)
    │   ├── 智能问答系统 (8模型动态切换对话)
    │   ├── 质量检测 (基于GPT-4o的图片质量分析)
    │   ├── 缺陷分析 (使用推理模型进行根因分析)
    │   ├── 报告生成 (AI辅助质量报告生成)
    │   └── 模型性能对比 (多模型A/B测试)
    ├── � 配置驱动界面 (基于配置端的动态界面)
    ├── � 数据分析中心 (BI报表和度量)
    ├── � 质量监控 (实时质量指标监控)
    └── � 工作台 (统一的业务工作台)
```

### 🔧 微服务后端架构
```
后端服务生态系统
├── 🤖 AI服务层
│   ├── chat-service.js (3004) - AI聊天服务
│   │   ├── 8个AI模型统一接口
│   │   ├── 流式响应支持 (Server-Sent Events)
│   │   ├── 图片解析功能 (Vision API)
│   │   ├── 工具调用功能 (Function Calling)
│   │   └── 外网模型支持 (DeepSeek外网API)
│   └── ai-proxy-service.js - AI代理服务
│
├── 🎛️ 配置服务层
│   ├── config-center-mock.js (8081) - 配置中心服务
│   ├── enhanced-mock.js - 增强Mock服务
│   └── advanced-features.js (3009) - 高级功能服务
│
├── 🏢 业务服务层 (Spring Boot)
│   ├── 配置中心服务 (8888) - 配置管理后端
│   ├── 数据驱动服务 (8889) - 业务数据处理
│   └── 用户认证服务 - JWT认证和权限管理
│
└── 🗄️ 数据存储层
    ├── SQLite - AI对话历史存储
    ├── MySQL - 业务数据存储
    ├── Redis - 缓存和会话存储
    └── MinIO - 文件对象存储
```

### 🐳 容器化部署架构
```
Docker容器编排
├── 🔍 服务发现层
│   └── Consul (8500) - 服务注册与发现
├── 📊 监控观测层
│   ├── Prometheus (9090) - 指标收集
│   ├── Grafana (3000) - 可视化监控
│   └── Alertmanager - 告警管理
├── 🔐 认证授权层
│   └── Casdoor (8000) - 开源身份认证
└── 🌐 网关代理层
    └── Nginx - 反向代理和负载均衡
```

## 🚀 快速开始

### 🔧 环境要求
- **Node.js** >= 18.0.0 (推荐 20.x LTS)
- **npm** >= 8.0.0
- **操作系统**: Windows 10/11
- **内存**: 4GB+ (推荐8GB)
- **磁盘空间**: 2GB+

### ⚡ 一键启动 (推荐)

```bash
# 🚀 统一启动管理器 (推荐，包含完整菜单)
QMS-START.bat

# 🔍 快速检查和启动 (自动检查环境)
QMS-Quick-Check-And-Start.bat

# 🎛️ 服务管理器 (单独管理各服务)
QMS-Service-Manager.bat

# 🏥 系统健康检查
QMS-Health-Check.bat

# 🛠️ 问题诊断工具
diagnose-proxy-issue.bat

# 🔧 前端代理问题修复
fix-frontend-proxy-issue.bat

# 🛑 停止所有服务
QMS-Stop-All.bat
```

### 🐳 Docker容器化部署

```bash
# Redis集成部署 (推荐)
docker-compose -f config/docker-compose.redis.yml up -d

# 生产环境部署
docker-compose -f config/docker-compose.prod.yml up -d

# 小型环境部署 (资源受限)
docker-compose -f config/docker-compose.small.yml up -d

# 开发环境部署
docker-compose -f config/docker-compose.local.yml up -d

# 一键部署脚本 (Linux)
chmod +x deployment/one-click-deploy.sh
./deployment/one-click-deploy.sh
```

### 📋 分步启动 (开发模式)

**1. 启动核心后端服务**
```bash
# AI聊天服务 (核心)
cd backend/nodejs
node chat-service.js

# 配置中心服务
node config-center-mock.js

# 高级功能服务
node advanced-features.js
```

**2. 启动前端应用**
```bash
# 配置端 (管控中心)
cd frontend/配置端
pnpm install && pnpm run serve

# 应用端 (业务场景)
cd frontend/应用端
npm install && npm run uat

# 配置驱动端 (可视化配置)
cd frontend/配置驱动端
npm install && npm run serve
```

**3. 启动Spring Boot服务 (可选)**
```bash
# 配置中心后端
cd backend/springboot/配置中心
mvn spring-boot:run

# 数据驱动后端
cd backend/springboot/数据驱动
mvn spring-boot:run
```

### 🌐 访问地址

#### 🎯 前端应用入口
| 应用名称 | 端口 | 访问地址 | 功能描述 | 技术栈 |
|---------|------|----------|----------|--------|
| **🎛️ 配置端** | 8072 | http://localhost:8072 | 配置管理中心 | Vue2 + Element UI |
| **🚀 应用端** | 8081 | http://localhost:8081 | AI智能问答和业务应用 | Vue3 + Element Plus |

#### 🔧 后端服务接口
| 服务名称 | 端口 | 健康检查 | 功能描述 | 状态 |
|---------|------|----------|----------|------|
| **🤖 AI聊天服务** | 3004 | http://localhost:3004/health | 8个AI模型统一接口 | 🟢 运行中 |
| **⚙️ 配置中心服务** | 3003 | http://localhost:3003/health | 轻量级配置管理 | 🟢 运行中 |
| **🔐 认证服务** | 8084 | http://localhost:8084/health | 用户认证和权限管理 | 🟢 运行中 |
| **🚀 Coze Studio服务** | 3005 | http://localhost:3005/health | Coze Studio集成 | 🟢 运行中 |
| **📤 导出服务** | 3008 | http://localhost:3008/health | 文档导出功能 | 🟢 运行中 |
| **🔧 高级功能服务** | 3009 | http://localhost:3009/health | 高级功能支持 | 🟢 运行中 |
| **🌐 API网关** | 8085 | http://localhost:8085/health | 统一API网关 | 🟢 运行中 |

#### 📊 监控和管理
| 工具名称 | 端口 | 访问地址 | 功能描述 | 状态 |
|---------|------|----------|----------|------|
| **Redis** | 6379 | redis://localhost:6379 | 高性能缓存数据库 | 🟡 可选 |
| **系统监控** | - | 内置健康检查 | 服务状态监控 | 🟢 运行中 |

#### 🎯 推荐使用路径
1. **🎛️ 配置管理**: 访问配置端进行系统配置和8个AI模型管理
2. **🚀 AI智能问答**: 访问应用端进行AI对话和质量管理
3. **🔧 服务管理**: 使用启动脚本进行服务管理
4. **🛠️ 问题诊断**: 使用诊断工具排查问题

## 🔐 登录凭据

### 🎛️ 前端应用登录
| 用户名 | 密码 | 角色 | 权限范围 | 适用系统 |
|--------|------|------|----------|----------|
| **admin** | admin123 | 系统管理员 | 全部权限 | 配置端 + 应用端 |
| **developer** | dev123 | 开发工程师 | 开发相关权限 | 配置端 + 应用端 |
| **tester** | test123 | 测试工程师 | 测试相关权限 | 应用端 |
| **quality** | quality123 | 质量工程师 | 质量管理权限 | 应用端 |

### � 监控系统登录
| 系统名称 | 用户名 | 密码 | 说明 |
|---------|--------|------|------|
| **Grafana** | admin | admin123 | 监控面板管理员 |
| **Casdoor** | admin | 123 | 身份认证管理员 |

## �📁 项目结构

```
QMS01/ (智能质量管理系统根目录)
├── 📁 backend/                    # 后端服务生态
│   ├── nodejs/                   # Node.js微服务集群
│   │   ├── chat-service.js       # AI聊天服务 (3004) 🤖
│   │   ├── config-center-mock.js # 配置中心服务 (8081) ⚙️
│   │   ├── advanced-features.js  # 高级功能服务 (3009) 🚀
│   │   └── enhanced-mock.js      # 增强Mock服务 📡
│   └── springboot/               # Spring Boot企业服务
│       ├── 配置中心/              # 配置管理后端 (8888) 🎛️
│       └── 数据驱动/              # 数据驱动后端 (8889) 📊
├── 📁 frontend/                   # 前端应用矩阵
│   ├── 应用端/                    # Vue3配置驱动业务应用 (8080) 🚀
│   │   ├── AI智能管理/            # 8模型AI功能模块
│   │   ├── 质量检测/              # 质量检测模块
│   │   ├── 数据分析/              # 数据分析模块
│   │   └── 配置驱动界面/          # 基于配置的动态界面
│   └── 配置端/                    # Vue2配置管理 (8072) 🎛️
│       ├── AI管理配置/            # 8个AI模型配置
│       ├── 系统管理/              # 系统配置管理
│       └── 权限管理/              # 权限配置管理
├── 📁 config/                     # 配置文件中心
│   ├── docker-compose.prod.yml   # 生产环境配置 🐳
│   ├── docker-compose.small.yml  # 小型环境配置 📦
│   ├── docker-compose.local.yml  # 开发环境配置 🔧
│   ├── enterprise-integration.yml # 企业集成配置 🏢
│   └── database/                 # 数据库配置文件
├── 📁 docs/                       # 技术文档库
│   ├── QMS-AI-集成开发设计方案.md  # 核心设计方案 📋
│   ├── AI-Integration-Summary.md  # AI集成总结 🤖
│   ├── Project-Structure.md       # 项目结构说明 🏗️
│   └── archive/                  # 历史文档归档 (21个文档) 📚
├── 📁 deployment/                 # 部署自动化
│   ├── one-click-deploy.sh       # 一键部署脚本 🚀
│   ├── aliyun-setup.sh          # 阿里云部署脚本 ☁️
│   └── phase1/                  # 分阶段部署配置 📈
├── 📁 monitoring/                 # 监控观测体系
│   ├── prometheus/               # Prometheus配置 📊
│   ├── grafana/                  # Grafana仪表板 📈
│   └── alertmanager/             # 告警管理配置 🚨
├── 📁 scripts/                    # 自动化脚本
│   ├── qms-service-manager.js   # 服务管理器 🎛️
│   ├── start-services.js        # 服务启动脚本 🚀
│   └── ecosystem.config.js      # PM2进程管理配置 ⚙️
├── 📁 nginx/                      # 反向代理配置
│   └── nginx.conf               # Nginx配置文件 🌐
├── 🚀 QMS-Quick-Start.bat        # 主启动脚本 (推荐)
├── 🛑 QMS-Stop-All.bat           # 停止所有服务
├── 🏥 QMS-Health-Check.bat       # 系统健康检查
└── 🎛️ qms-manager.bat            # 图形化管理工具
```

## 🔧 核心功能特性

### 🤖 AI智能化功能
- ✅ **多模型支持**: 8个主流AI模型统一管理 (含外网模型)
- ✅ **智能问答**: 支持文本、图片、工具调用等多种交互方式
- ✅ **模型切换**: 动态切换AI模型，支持A/B测试
- ✅ **推理能力**: 支持DeepSeek R1等推理专用模型
- ✅ **质量分析**: AI驱动的质量检测和缺陷分析
- ✅ **报告生成**: AI辅助的质量报告自动生成
- ✅ **流式响应**: 支持Server-Sent Events实时响应
- ✅ **外网支持**: 支持DeepSeek外网API调用

### 🗄️ 数据存储架构
- ✅ **SQLite主库**: 轻量级数据库，适合中小型应用，零维护成本
- ✅ **Redis持久化缓存**: 高性能缓存系统，支持RDB+AOF双重持久化
  - 🔧 **WSL2集成**: 连接WSL2中的Redis服务 (172.17.202.149:6380)
  - 💾 **双重持久化**: RDB快照 + AOF日志，确保数据安全
  - 🚀 **高性能配置**: 512MB内存限制，多线程I/O，LRU淘汰策略
  - 📊 **监控支持**: 慢查询日志，延迟监控，性能统计
- ✅ **文件存储**: 配置文件基于JSON，支持版本控制
- 🔄 **MySQL支持**: 已配置支持，可按需切换 (数据量>100GB时)
- 🔄 **PostgreSQL支持**: Docker已配置，支持复杂查询场景

### 🎛️ 配置驱动架构
- ✅ **可视化配置**: 拖拽式界面配置，无需编程
- ✅ **实时同步**: 配置变更实时生效，支持热更新
- ✅ **表单机器**: 动态表单生成和渲染
- ✅ **流程设计**: 可视化业务流程配置
- ✅ **权限配置**: 细粒度的权限控制配置
- ✅ **数据字典**: 统一的数据字典管理

### 🔐 企业级安全
- ✅ **多重认证**: JWT + Session双重认证机制
- ✅ **角色权限**: 基于RBAC的权限控制体系
- ✅ **数据隔离**: 用户数据完全隔离
- ✅ **审计日志**: 完整的操作审计追踪
- ✅ **安全防护**: XSS、CSRF、SQL注入防护
- ✅ **身份集成**: 支持Casdoor身份认证集成

### 📊 监控观测体系
- ✅ **实时监控**: Prometheus + Grafana监控体系
- ✅ **健康检查**: 多层次的服务健康检查
- ✅ **性能指标**: 响应时间、成功率、资源使用率监控
- ✅ **告警管理**: Alertmanager智能告警
- ✅ **日志聚合**: 统一的日志收集和分析
- ✅ **链路追踪**: 分布式请求链路追踪

### 🐳 云原生部署
- ✅ **容器化**: Docker容器化部署
- ✅ **编排管理**: Docker Compose服务编排
- ✅ **服务发现**: Consul服务注册与发现
- ✅ **负载均衡**: Nginx反向代理和负载均衡
- ✅ **自动扩缩**: 支持水平扩展
- ✅ **一键部署**: 自动化部署脚本

### 🔧 中间件集成策略
- ✅ **Redis集成**: 推荐立即集成，解决配置缓存和会话管理
- 🔄 **MySQL备选**: 已配置支持，数据量增长时可切换
- 🔄 **RabbitMQ备选**: 已配置支持，分布式需求时可启用
- ✅ **轻量优先**: 当前SQLite+EventEmitter已满足需求
- 📊 **渐进升级**: 根据实际需求渐进式引入中间件

## 🛠️ 开发指南

### 🔌 API接口规范

#### 认证相关API
```bash
# 用户登录
POST /uac-auth-service/v2/api/uac-auth/login
# 获取用户信息
POST /uac-auth-service/v2/api/uac-auth/utoken/getUserInfo
# 刷新Token
POST /uac-auth-service/v2/api/uac-auth/refresh
```

#### AI服务API
```bash
# 发送聊天消息
POST /api/chat
# 图片解析聊天
POST /api/chat/vision
# 工具调用聊天
POST /api/chat/tools
# 获取模型列表
GET /api/models
# 切换模型
POST /api/models/switch
```

#### 健康检查API
```bash
# 服务健康检查
GET /health
GET /api/health
GET /actuator/health
```

### 🎨 前端开发规范

#### 配置端 (Vue2)
- **技术栈**: Vue 2.7 + Element UI + Vuex
- **构建工具**: Vue CLI + Webpack
- **包管理**: pnpm
- **代码规范**: ESLint + Prettier

#### 应用端 (Vue3)
- **技术栈**: Vue 3 + Element Plus + Pinia
- **构建工具**: Vite
- **包管理**: npm
- **状态管理**: Pinia
- **特色**: 配置驱动的业务应用，集成8个AI模型

### 🔧 后端开发规范

#### Node.js服务
- **框架**: Express.js
- **数据库**: SQLite (开发) / PostgreSQL (生产)
- **缓存**: Redis
- **API设计**: RESTful风格
- **错误处理**: 统一错误处理中间件

#### Spring Boot服务
- **框架**: Spring Boot 2.7+
- **数据库**: MySQL 8.0+
- **ORM**: MyBatis Plus
- **缓存**: Redis
- **监控**: Spring Boot Actuator

## 📝 版本更新日志

### 🎉 v2.0.0 (2025-08-01) - AI集成版本
- ✅ **AI模型集成**: 完成8个主流AI模型的统一集成 (含外网模型)
- ✅ **配置驱动架构**: 实现完整的配置驱动开发模式
- ✅ **微服务架构**: 完成Node.js + Spring Boot微服务架构
- ✅ **监控体系**: 集成Prometheus + Grafana监控体系
- ✅ **容器化部署**: 完成Docker容器化和一键部署
- ✅ **企业级安全**: 实现完整的认证授权和数据隔离
- ✅ **双端分离**: 配置端、应用端完整实现 (应用端集成配置驱动功能)

### 🚀 v1.5.0 (2025-07-30) - 架构优化版本
- ✅ **前端重构**: Vue2/Vue3双技术栈支持
- ✅ **后端优化**: Express.js + Spring Boot混合架构
- ✅ **数据库设计**: MySQL + SQLite + Redis多数据源
- ✅ **API规范**: RESTful API设计规范化
- ✅ **错误处理**: 统一错误处理和日志记录

### 📋 v1.0.0 (2025-07-26) - 基础版本
- ✅ 完成基础架构搭建
- ✅ 实现用户认证功能
- ✅ 完成登录页面开发
- ✅ 集成路由守卫机制
- ✅ 添加模拟API服务

## 🎯 未来规划

### 🔮 v2.1.0 (计划中) - 智能化增强
- 🔄 **AI工作流**: 可视化AI工作流设计器
- 🔄 **智能推荐**: 基于历史数据的智能推荐系统
- 🔄 **自动化测试**: AI驱动的自动化测试生成
- 🔄 **性能优化**: 基于AI的性能自动优化

### 🚀 v3.0.0 (规划中) - 企业级增强
- 🔄 **多租户支持**: SaaS模式的多租户架构
- 🔄 **国际化**: 多语言和多地区支持
- 🔄 **移动端**: React Native移动应用
- 🔄 **大数据集成**: 支持大数据分析和处理

## 🤝 贡献指南

### 🔧 开发环境搭建
1. **克隆仓库**: `git clone https://github.com/xinren1232/qmsai.git`
2. **安装依赖**: 运行 `QMS-Quick-Start.bat` 自动安装
3. **启动服务**: 使用一键启动脚本
4. **开发调试**: 访问对应端口进行开发

### 📋 贡献流程
1. **Fork仓库**: Fork本仓库到你的GitHub账户
2. **创建分支**: `git checkout -b feature/your-feature-name`
3. **开发功能**: 遵循代码规范进行开发
4. **测试验证**: 确保所有测试通过
5. **提交代码**: `git commit -m 'feat: add your feature'`
6. **推送分支**: `git push origin feature/your-feature-name`
7. **创建PR**: 在GitHub上创建Pull Request

### 📏 代码规范
- **提交信息**: 遵循Conventional Commits规范
- **代码风格**: 使用ESLint + Prettier统一代码风格
- **测试覆盖**: 新功能需要包含相应的测试用例
- **文档更新**: 重要功能需要更新相关文档

## 📄 许可证

本项目采用 **MIT许可证** - 查看 [LICENSE](LICENSE) 文件了解详情

## 📞 技术支持

### 🏢 项目团队
- **项目负责人**: QMS-AI Development Team
- **技术架构师**: AI Integration Specialist
- **前端团队**: Vue.js Development Team
- **后端团队**: Node.js & Spring Boot Team

### 📧 联系方式
- **技术支持**: support@qms-ai.com
- **商务合作**: business@qms-ai.com
- **Bug反馈**: issues@qms-ai.com
- **项目地址**: https://github.com/xinren1232/qmsai

### 🌐 在线资源
- **在线文档**: https://docs.qms-ai.com
- **API文档**: https://api.qms-ai.com/docs
- **视频教程**: https://learn.qms-ai.com
- **社区论坛**: https://community.qms-ai.com

---

<div align="center">

**🚀 QMS-AI - 让质量管理更智能，让AI赋能质量管理**

[![GitHub stars](https://img.shields.io/github/stars/xinren1232/qmsai?style=social)](https://github.com/xinren1232/qmsai)
[![GitHub forks](https://img.shields.io/github/forks/xinren1232/qmsai?style=social)](https://github.com/xinren1232/qmsai)
[![GitHub issues](https://img.shields.io/github/issues/xinren1232/qmsai)](https://github.com/xinren1232/qmsai/issues)
[![GitHub license](https://img.shields.io/github/license/xinren1232/qmsai)](https://github.com/xinren1232/qmsai/blob/main/LICENSE)

</div>
