# 🚀 QMS-AI系统GitHub推送指南

## 📋 系统概述

QMS-AI系统是一个完整的质量管理AI集成平台，包含：

### ✅ 核心功能
- **18个插件验证系统** - 完整的插件生态
- **8个AI模型集成** - 6个内部模型 + 2个外部模型
- **Vue3前端应用** - 现代化用户界面
- **Node.js微服务** - 高性能后端架构
- **配置驱动系统** - 动态功能管理

### 📊 插件分类
1. **文档解析类** (7个): PDF、CSV、JSON、XML、XLSX、DOCX、Excel分析器
2. **数据分析类** (5个): 统计分析器、SPC控制器、数据清洗器、异常检测器、文本摘要器
3. **质量工具类** (3个): FMEA分析器、MSA计算器、缺陷检测器
4. **AI处理类** (1个): OCR识别器
5. **外部连接类** (2个): API连接器、数据库查询器

## 🔧 手动推送到GitHub步骤

### 1. 初始化Git仓库
```bash
# 进入项目目录
cd d:\QMS01

# 初始化Git仓库
git init

# 配置用户信息
git config user.name "你的用户名"
git config user.email "你的邮箱"
```

### 2. 添加远程仓库
```bash
# 添加GitHub远程仓库
git remote add origin https://github.com/你的用户名/qms-ai.git
```

### 3. 添加文件并提交
```bash
# 添加所有文件
git add .

# 提交更改
git commit -m "🎯 QMS-AI插件验证系统完整实现

✅ 完成所有18个插件的测试文件导入功能
- 文档解析类: 7个插件
- 数据分析类: 5个插件  
- 质量工具类: 3个插件
- AI处理类: 1个插件
- 外部连接类: 2个插件

🔧 技术实现:
- 统一配置所有插件支持预制测试文件选择
- 16个专用测试文件覆盖所有插件类型
- 完整的验证界面和用户体验优化

📊 验证成果:
- 100%插件覆盖率
- 统一的用户界面
- 完整的测试文档
- 系统性的验证流程"
```

### 4. 推送到GitHub
```bash
# 推送到main分支
git push -u origin main

# 如果失败，尝试master分支
git push -u origin master
```

## 📁 项目结构

```
QMS01/
├── frontend/                    # 前端应用
│   ├── 应用端/                  # Vue3主应用
│   ├── 配置端/                  # Vue2配置界面
│   └── 配置端-vue3/             # Vue3配置界面
├── backend/                     # 后端服务
│   └── nodejs/                  # Node.js微服务
├── config/                      # 配置文件
├── docs/                        # 文档
├── scripts/                     # 脚本文件
├── deployment/                  # 部署配置
└── tools/                       # 工具脚本
```

## 🎯 关键文件

### 插件验证相关
- `frontend/应用端/src/views/coze-studio/validation/` - 验证界面
- `frontend/应用端/public/plugin-test-files/` - 测试文件
- `QMS-AI-Plugin-Validation-Complete-Report.md` - 验证报告

### 系统配置
- `package.json` - 项目依赖
- `pnpm-workspace.yaml` - 工作空间配置
- `.gitignore` - Git忽略文件

### 启动脚本
- `QMS-START.bat` - 系统启动脚本
- `QMS-Clean-Start.bat` - 清洁启动脚本
- `start-qms-services.bat` - 服务启动脚本

## 🚀 部署说明

### 本地开发
```bash
# 安装依赖
pnpm install

# 启动所有服务
./QMS-START.bat
```

### 生产部署
```bash
# 使用Docker部署
docker-compose up -d

# 或使用部署脚本
./deployment/deploy-to-server.sh
```

## 📊 系统特性

### AI模型集成
- **GPT-4o** - 推理、工具、视觉
- **O3** - 推理、工具
- **Gemini 2.5 Pro** - 推理、工具
- **Claude 3.7 Sonnet** - 工具支持
- **DeepSeek系列** - 4个模型

### 技术栈
- **前端**: Vue3 + Element Plus + Vite
- **后端**: Node.js + Express + Redis
- **数据库**: MySQL + Redis
- **部署**: Docker + Nginx

### 端口配置
- **3003** - 配置中心
- **3004** - 聊天服务
- **3005** - Coze Studio
- **8081** - 应用端
- **8072** - 配置端

## 📝 提交信息模板

```
🎯 [功能类型] 简短描述

✅ 主要更改:
- 具体更改1
- 具体更改2

🔧 技术实现:
- 技术细节1
- 技术细节2

📊 影响范围:
- 影响的模块或功能
```

## 🔗 相关链接

- **项目文档**: `docs/`目录
- **API文档**: `docs/QMS-API文档-v2.0.md`
- **部署指南**: `deployment/`目录
- **用户指南**: `docs/Coze-Studio-User-Guide.md`

## ⚠️ 注意事项

1. **敏感信息**: 确保不要提交API密钥和密码
2. **大文件**: node_modules等大文件已在.gitignore中排除
3. **分支策略**: 建议使用main分支作为主分支
4. **提交频率**: 建议按功能模块分别提交

## 🎉 完成确认

推送完成后，您可以在GitHub上看到：
- ✅ 完整的QMS-AI系统代码
- ✅ 18个插件验证功能
- ✅ 详细的文档和说明
- ✅ 部署和启动脚本
- ✅ 系统配置文件

这样就完成了QMS-AI系统到GitHub的完整保存！
