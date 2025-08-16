# QMS-AI 启动脚本使用指南

## 🚀 推荐启动方式

### 1. 主启动器 (推荐)
```bash
QMS-START.bat
```
- 提供完整的菜单选项
- 支持多种启动模式
- 包含健康检查和重启功能

### 2. 快速启动
```bash
QMS-Quick-Check-And-Start.bat
```
- 一键启动所有服务
- 自动环境检查和端口检查
- 适合日常开发使用

### 3. 服务管理器
```bash
QMS-Service-Manager.bat
```
- 单独管理各个服务
- 支持启动/停止/重启
- 详细的状态监控

## 🛠️ 问题诊断工具

### 代理问题诊断
```bash
diagnose-proxy-issue.bat
```
- 检查网络连接
- 诊断端口监听状态
- 检查配置文件

### 前端代理修复
```bash
fix-frontend-proxy-issue.bat
```
- 清理前端缓存
- 重新安装依赖
- 重启服务

## 📊 状态检查
```bash
QMS-Health-Check.bat
check-services-status.bat
```

## 🛑 停止服务
```bash
QMS-Stop-All.bat
```

## ⚠️ 已清理的过时脚本
- start-qms.bat (功能重复)
- quick-start.bat (端口过时)
- *.vbs (过时的VBScript启动器)
- backend/nodejs/start-all.bat (端口配置错误)
- 大量test-*.js测试文件
- 重复的部署包目录

## 📁 当前推荐的启动流程
1. **首次使用**: `QMS-START.bat` → 选择 "1 全面启动"
2. **日常开发**: `QMS-Quick-Check-And-Start.bat`
3. **问题排查**: `QMS-Service-Manager.bat`
4. **状态检查**: `QMS-Final-Status-Check.ps1`

## 🔧 服务端口配置 (已统一)
- **配置中心**: 3003 (轻量级配置服务)
- **聊天服务**: 3004 (AI模型接口)
- **Coze Studio**: 3005 (Coze Studio集成)
- **导出服务**: 3008 (文档导出)
- **高级功能**: 3009 (高级功能支持)
- **认证服务**: 8084 (用户认证)
- **API网关**: 8085 (统一网关)
- **应用端**: 8081 (前端应用)
- **配置端**: 8072 (管理界面)

## 🛠️ 常用命令
```bash
# 检查服务状态
netstat -ano | findstr ":3004\|:8081"

# 手动启动聊天服务
cd backend/nodejs && node chat-service.js

# 手动启动应用端
cd frontend/应用端 && npm run dev

# 清理npm缓存
npm cache clean --force

# 重新安装依赖
npm install
```

## 🗂️ 文档整理
- 过时报告已移动到 `docs/archive/`
- 保留核心文档: README.md, QUICK-START-GUIDE.md
- 新增本指南: START-GUIDE.md

## 🎯 清理成果
✅ 删除重复启动脚本: 15+ 个
✅ 删除过时VBS文件: 11 个  
✅ 删除重复部署包: 3 个目录
✅ 删除临时测试文件: 15+ 个
✅ 移动过时文档到archive: 40+ 个
✅ 统一端口配置
✅ 修复启动脚本冲突

现在你的项目更加整洁，启动方式更加统一！
