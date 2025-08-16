# 创建GitHub配置文件脚本

# 创建CI/CD工作流
@"
name: 🚀 QMS-AI CI/CD Pipeline

on:
  push:
    branches: [ main, develop ]
  pull_request:
    branches: [ main ]
  workflow_dispatch:

env:
  NODE_VERSION: '18'
  PNPM_VERSION: '8'

jobs:
  # 🔍 代码质量检查
  lint-and-test:
    name: 🔍 代码质量检查
    runs-on: ubuntu-latest
    
    steps:
    - name: 📥 检出代码
      uses: actions/checkout@v4
      
    - name: 📦 设置 Node.js
      uses: actions/setup-node@v4
      with:
        node-version: `${{ env.NODE_VERSION }}
        
    - name: 📦 设置 pnpm
      uses: pnpm/action-setup@v2
      with:
        version: `${{ env.PNPM_VERSION }}
        
    - name: 📦 安装依赖
      run: pnpm install --frozen-lockfile
      
    - name: 🔍 ESLint 检查
      run: pnpm run lint
      
    - name: 🧪 运行测试
      run: pnpm run test

  # 🏗️ 构建检查
  build:
    name: 🏗️ 构建检查
    runs-on: ubuntu-latest
    needs: lint-and-test
    
    steps:
    - name: 📥 检出代码
      uses: actions/checkout@v4
      
    - name: 📦 设置 Node.js
      uses: actions/setup-node@v4
      with:
        node-version: `${{ env.NODE_VERSION }}
        
    - name: 📦 设置 pnpm
      uses: pnpm/action-setup@v2
      with:
        version: `${{ env.PNPM_VERSION }}
        
    - name: 📦 安装依赖
      run: pnpm install --frozen-lockfile
      
    - name: 🏗️ 构建项目
      run: pnpm run build
"@ | Out-File -FilePath ".github\workflows\ci.yml" -Encoding UTF8

# 创建Bug报告模板
@"
---
name: 🐛 Bug报告
about: 创建一个Bug报告来帮助我们改进
title: '[BUG] '
labels: ['bug', 'needs-triage']
assignees: ''
---

## 🐛 Bug描述
简洁明了地描述这个Bug。

## 🔄 复现步骤
复现该行为的步骤：
1. 进入 '...'
2. 点击 '....'
3. 滚动到 '....'
4. 看到错误

## ✅ 期望行为
简洁明了地描述您期望发生的事情。

## 📸 截图
如果适用，请添加截图来帮助解释您的问题。

## 🖥️ 环境信息
**桌面环境:**
- 操作系统: [例如 Windows 11]
- 浏览器: [例如 Chrome, Safari]
- 版本: [例如 22]

## 🔧 QMS-AI版本
- QMS-AI版本: [例如 v2.1.0]
- Node.js版本: [例如 18.17.0]
- 使用的AI模型: [例如 GPT-4o]

## ✅ 检查清单
- [ ] 我已经搜索了现有的Issues，确认这不是重复问题
- [ ] 我已经阅读了文档和FAQ
- [ ] 我已经提供了足够的信息来复现这个问题
"@ | Out-File -FilePath ".github\ISSUE_TEMPLATE\bug_report.md" -Encoding UTF8

# 创建功能请求模板
@"
---
name: ✨ 功能请求
about: 为这个项目建议一个想法
title: '[FEATURE] '
labels: ['enhancement', 'needs-discussion']
assignees: ''
---

## 🚀 功能描述
简洁明了地描述您想要的功能。

## 💡 动机
这个功能请求是否与问题相关？请描述。

## 📋 详细描述
清楚详细地描述您希望发生的事情。

## 🔄 替代方案
简洁明了地描述您考虑过的任何替代解决方案或功能。

## ✅ 检查清单
- [ ] 我已经搜索了现有的Issues，确认这不是重复请求
- [ ] 我已经考虑了这个功能对现有用户的影响
- [ ] 我已经提供了足够的细节来理解这个请求
"@ | Out-File -FilePath ".github\ISSUE_TEMPLATE\feature_request.md" -Encoding UTF8

# 创建PR模板
@"
## 📋 Pull Request 描述

### 🎯 变更类型
- [ ] 🐛 Bug修复
- [ ] ✨ 新功能
- [ ] 💄 UI/样式更新
- [ ] ♻️ 代码重构
- [ ] 📝 文档更新

### 📝 变更描述
简洁明了地描述这个PR的变更内容。

### 🔗 相关Issue
关闭 #(issue编号)

### 🧪 测试
描述您如何测试了您的变更。

### ✅ 检查清单
- [ ] 代码遵循项目的编码规范
- [ ] 已经进行了自我代码审查
- [ ] 新功能有对应的测试
- [ ] 所有测试都通过
"@ | Out-File -FilePath ".github\pull_request_template.md" -Encoding UTF8

# 创建CODEOWNERS
@"
# QMS-AI 代码所有者配置
* @xinren1232

# 前端代码
frontend/ @xinren1232
*.vue @xinren1232
*.js @xinren1232

# 后端代码
backend/ @xinren1232

# 配置文件
config/ @xinren1232
.github/ @xinren1232

# 文档
docs/ @xinren1232
*.md @xinren1232
"@ | Out-File -FilePath ".github\CODEOWNERS" -Encoding UTF8

Write-Host "GitHub配置文件创建完成！"
