# 🛡️ GitHub分支保护规则设置指南

## 📋 设置步骤

### 1. 进入仓库设置
1. 打开 https://github.com/xinren1232/new-qms
2. 点击 **Settings** 标签
3. 在左侧菜单中选择 **Branches**

### 2. 添加分支保护规则
点击 **Add rule** 按钮，配置以下设置：

#### 🎯 基本设置
- **Branch name pattern**: `main`
- **Restrict pushes that create matching branches**: ✅

#### 🔒 保护设置
- **Require a pull request before merging**: ✅
  - **Require approvals**: ✅ (设置为 2)
  - **Dismiss stale PR approvals when new commits are pushed**: ✅
  - **Require review from code owners**: ✅
  - **Require approval of the most recent reviewable push**: ✅

#### 🧪 状态检查
- **Require status checks to pass before merging**: ✅
- **Require branches to be up to date before merging**: ✅
- **Status checks that are required**:
  - `🔍 代码质量检查`
  - `🏗️ 构建检查`
  - `🔒 安全扫描`

#### 🚫 限制设置
- **Restrict who can push to matching branches**: ✅
  - 添加团队: `qms-ai-maintainers`
  - 添加用户: `xinren1232`

#### 📝 其他规则
- **Require linear history**: ✅
- **Allow force pushes**: ❌
- **Allow deletions**: ❌
- **Require conversation resolution before merging**: ✅

### 3. 保存设置
点击 **Create** 按钮保存分支保护规则。

---

## 🔧 GitHub Actions Secrets 配置

### 必需的Secrets
在 **Settings** → **Secrets and variables** → **Actions** 中添加：

#### 🐳 Docker相关
- `DOCKER_USERNAME`: Docker Hub用户名
- `DOCKER_PASSWORD`: Docker Hub密码

#### 📊 代码质量
- `SONAR_TOKEN`: SonarCloud访问令牌
- `CODECOV_TOKEN`: Codecov访问令牌

#### 🔒 安全扫描
- `SNYK_TOKEN`: Snyk安全扫描令牌

#### 📢 通知
- `SLACK_WEBHOOK`: Slack通知Webhook URL

---

## 🏷️ GitHub Labels 配置

### 建议的标签
在 **Issues** → **Labels** 中创建以下标签：

#### 🐛 Bug相关
- `bug` (红色) - 程序错误
- `critical` (深红色) - 严重问题
- `needs-triage` (橙色) - 需要分类

#### ✨ 功能相关
- `enhancement` (绿色) - 功能增强
- `feature` (蓝色) - 新功能
- `needs-discussion` (紫色) - 需要讨论

#### 🔧 技术相关
- `refactor` (黄色) - 代码重构
- `performance` (青色) - 性能优化
- `security` (深蓝色) - 安全相关

#### 📝 文档相关
- `documentation` (浅蓝色) - 文档更新
- `help-wanted` (绿色) - 寻求帮助
- `good-first-issue` (浅绿色) - 适合新手

---

## 🎯 GitHub Environments 配置

### 创建环境
在 **Settings** → **Environments** 中创建：

#### 🧪 Staging环境
- **Environment name**: `staging`
- **Required reviewers**: `xinren1232`
- **Wait timer**: 0 minutes
- **Environment secrets**: 测试环境相关配置

#### 🚀 Production环境
- **Environment name**: `production`
- **Required reviewers**: `xinren1232`
- **Wait timer**: 5 minutes
- **Environment secrets**: 生产环境相关配置

---

## 📊 GitHub Projects 配置

### 创建项目看板
1. 进入 **Projects** 标签
2. 点击 **New project**
3. 选择 **Board** 模板
4. 创建以下列：
   - 📋 **Backlog** - 待办事项
   - 🔄 **In Progress** - 进行中
   - 👀 **In Review** - 代码审查
   - ✅ **Done** - 已完成

---

## 🔍 代码扫描配置

### CodeQL设置
1. 进入 **Security** → **Code scanning**
2. 点击 **Set up CodeQL**
3. 选择 **Advanced** 配置
4. 使用项目中的 `.github/workflows/security.yml`

### Dependabot设置
1. 进入 **Security** → **Dependabot**
2. 启用 **Dependabot alerts**
3. 启用 **Dependabot security updates**
4. 配置 **Dependabot version updates**

---

## 📈 Insights配置

### 启用功能
- **Pulse**: 项目活动概览
- **Contributors**: 贡献者统计
- **Traffic**: 访问统计
- **Commits**: 提交统计
- **Code frequency**: 代码频率
- **Dependency graph**: 依赖关系图

---

## ✅ 配置检查清单

- [ ] 分支保护规则已设置
- [ ] GitHub Actions Secrets已配置
- [ ] Labels已创建
- [ ] Environments已设置
- [ ] Projects看板已创建
- [ ] 代码扫描已启用
- [ ] Dependabot已配置
- [ ] Insights功能已启用

---

## 🎉 完成

配置完成后，您的QMS-AI项目将具备：
- 🛡️ 完整的分支保护
- 🔄 自动化CI/CD流水线
- 📊 代码质量监控
- 🔒 安全漏洞扫描
- 📈 项目管理工具
- 👥 团队协作功能
