# QMS项目结构说明

## 📁 项目目录结构

```
QMS01/
├── 📁 frontend/                    # 前端项目
│   ├── 📁 配置端/                  # 配置端 (端口8072)
│   │   ├── 📁 src/
│   │   │   ├── 📁 views/ai-management-config/  # AI管理配置
│   │   │   ├── 📁 views/system-manage/         # 系统管理
│   │   │   ├── 📁 views/dictionary-manage/     # 字典管理
│   │   │   ├── 📁 views/file-manage/           # 文件管理
│   │   │   └── 📁 views/app-manage/            # 应用管理
│   │   ├── package.json
│   │   └── vue.config.js
│   │
│   ├── 📁 应用端/                  # 应用端 (端口8080)
│   │   ├── 📁 src/
│   │   │   ├── 📁 views/ai-management/         # AI智能管理
│   │   │   ├── 📁 views/dashboard/             # 工作台
│   │   │   ├── 📁 views/system/                # 系统管理
│   │   │   └── 📁 views/profile/               # 个人中心
│   │   ├── package.json
│   │   └── vite.config.js
│   │
│   └── 前端项目总体介绍.md
│
├── 📁 backend/                     # 后端服务
│   ├── 📁 nodejs/                  # Node.js服务
│   │   ├── chat-service.js         # AI聊天服务 (端口3002)
│   │   ├── config-center-mock.js   # 配置中心服务 (端口8081)
│   │   ├── package.json
│   │   ├── package-lock.json
│   │   ├── node_modules/
│   │   └── test-chat.html
│   │
│   └── 📁 springboot/              # Spring Boot服务
│       ├── 📁 数据驱动/            # 数据驱动服务
│       ├── 📁 配置中心/            # 配置中心服务
│       ├── create-qms-user.sql     # 数据库用户创建脚本
│       └── setup-database.sql      # 数据库初始化脚本
│
├── 📁 docs/                        # 项目文档
│   ├── Architecture-Analysis.md    # 架构分析文档
│   ├── AI-API-Integration-Guide.md # AI API集成指南
│   ├── AI-Integration-Summary.md   # AI集成总结
│   ├── QMS-AI-集成开发设计方案.md  # 集成开发设计方案
│   └── Project-Structure.md        # 项目结构说明 (本文档)
│
├── 📁 config/                      # 配置文件
│   ├── enterprise-integration.yml  # 企业集成配置
│   └── postgresql.conf             # 数据库配置
│
├── 📁 deploy/                      # 部署脚本
│   ├── aliyun-setup.sh            # 阿里云部署脚本
│   ├── one-click-deploy.sh        # 一键部署脚本
│   └── upload-to-server.sh        # 服务器上传脚本
│
├── 📁 nginx/                       # Nginx配置
│   └── nginx.conf                  # Nginx配置文件
│
├── start-qms.bat                   # 项目启动脚本
├── package.json                    # 根项目依赖
├── docker-compose.yml              # Docker编排文件
├── README.md                       # 项目说明文档
└── CHANGELOG.md                    # 变更日志
```

## 🎯 核心组件说明

### 前端架构

#### 配置端 (端口8072)
- **技术栈**: Vue 2 + Element UI + Webpack
- **功能定位**: 管控设计和配置管理
- **核心模块**:
  - AI管理配置: 6个AI模型的配置管理
  - 系统管理: 属性、对象、视图、方法管理
  - 字典管理: 数据字典配置
  - 文件管理: 文件库和规则管理
  - 应用管理: 应用配置和发布

#### 应用端 (端口8080)
- **技术栈**: Vue 3 + Element Plus + Vite
- **功能定位**: 场景实现和业务应用
- **核心模块**:
  - AI智能管理: 实际AI业务场景
  - 质量检测: 基于GPT-4o的图片质量分析
  - 缺陷分析: 使用推理模型进行根因分析
  - 报告生成: AI辅助质量报告生成
  - 工作台: 统一的业务工作台

### 后端架构

#### AI聊天服务 (端口3002)
- **技术栈**: Node.js + Express
- **功能**: 6个AI模型统一服务接口
- **支持模型**: GPT-4o, O3, Gemini 2.5 Pro, Claude 3.7, DeepSeek R1, DeepSeek V3

#### 配置中心服务 (端口8081)
- **技术栈**: Node.js + Express (Mock服务)
- **功能**: 配置数据管理和API服务
- **预置用户**: admin/admin123, developer/dev123, tester/test123

#### Spring Boot服务
- **数据驱动服务**: 业务数据处理
- **配置中心服务**: 配置管理后端

## 🚀 启动方式

### 一键启动
```bash
# Windows环境
start-qms.bat
```

### 手动启动
```bash
# 1. 启动后端服务
cd backend
node chat-service.js        # AI聊天服务
node config-center-mock.js  # 配置中心服务

# 2. 启动配置端
cd frontend/配置端
npx vue-cli-service serve --mode uat --port 8072

# 3. 启动应用端
cd frontend/应用端
npx vite --port 8080
```

## 📋 访问地址

| 服务名称 | 端口 | 访问地址 | 功能 |
|---------|------|----------|------|
| 配置端 | 8072 | http://localhost:8072/alm-transcend-configcenter-web/ | 管控设计 |
| 应用端 | 8080 | http://localhost:8080/ | 场景实现 |
| AI聊天服务 | 3002 | http://localhost:3002/health | AI模型服务 |
| 配置中心服务 | 8081 | http://localhost:8081/health | 配置管理 |

## 🔄 配置驱动联动机制

1. **配置端**: 管理AI模型配置、系统配置、业务规则
2. **应用端**: 根据配置端的设置动态调整功能和界面
3. **配置同步**: 通过API接口实现配置的实时同步
4. **模型管理**: 配置端统一管理6个AI模型，应用端动态调用

## 📝 开发指南

### 配置端开发
- 主要用于系统配置和管理功能开发
- 遵循Vue 2 + Element UI的开发规范
- 重点关注配置管理和系统管理功能

### 应用端开发
- 主要用于业务场景和AI功能开发
- 遵循Vue 3 + Element Plus的开发规范
- 重点关注质量管理场景的AI应用

### 后端开发
- AI聊天服务: 负责AI模型的统一接口
- 配置中心服务: 负责配置数据的管理
- Spring Boot服务: 负责业务数据处理

这个项目结构实现了清晰的职责分工，为QMS系统提供了强大的AI能力和灵活的配置管理能力。
