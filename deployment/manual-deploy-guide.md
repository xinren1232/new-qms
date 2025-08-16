# 🚀 QMS-AI 手动部署指南

## 📋 部署准备

由于SSH工具可能未在Windows环境中配置，我们提供手动部署方案。

### 服务器信息
- **服务器IP**: 47.108.152.16
- **用户名**: root
- **项目目录**: /opt/qms

## 🔧 部署步骤

### 第一步：连接服务器

使用以下任一方式连接到阿里云服务器：

#### 方式1：使用PuTTY (Windows推荐)
1. 下载并安装PuTTY
2. 输入服务器IP: 47.108.152.16
3. 端口: 22
4. 连接类型: SSH
5. 输入用户名和密码

#### 方式2：使用Windows Terminal (如果已安装)
```bash
ssh root@47.108.152.16
```

#### 方式3：使用阿里云控制台
1. 登录阿里云控制台
2. 进入ECS实例管理
3. 点击"远程连接"

### 第二步：备份现有系统

```bash
# 创建备份目录
BACKUP_DIR="/opt/qms-backup-$(date +%Y%m%d-%H%M%S)"
mkdir -p $BACKUP_DIR

# 进入项目目录
cd /opt/qms

# 停止现有服务
docker-compose -f deployment/aliyun-deploy-optimized.yml down

# 备份关键文件
cp -r backend $BACKUP_DIR/
cp -r frontend $BACKUP_DIR/
cp -r deployment $BACKUP_DIR/
cp .env $BACKUP_DIR/ 2>/dev/null || true

echo "✅ 备份完成: $BACKUP_DIR"
```

### 第三步：拉取最新代码

```bash
# 保存本地更改
git add .
git stash

# 拉取最新代码
git fetch origin clean-main
git checkout clean-main
git pull origin clean-main

echo "✅ 代码更新完成"
```

### 第四步：恢复配置文件

```bash
# 恢复环境变量文件
if [ -f "$BACKUP_DIR/.env" ]; then
    cp $BACKUP_DIR/.env ./
    echo "✅ 环境变量恢复完成"
fi

# 恢复自定义配置
if [ -d "$BACKUP_DIR/config" ]; then
    cp -r $BACKUP_DIR/config/* ./config/ 2>/dev/null || true
    echo "✅ 自定义配置恢复完成"
fi
```

### 第五步：更新依赖

```bash
# 更新后端依赖
if [ -f "backend/nodejs/package.json" ]; then
    echo "📦 更新后端依赖..."
    cd backend/nodejs
    npm install --production
    cd ../..
    echo "✅ 后端依赖更新完成"
fi
```

### 第六步：重新构建和启动服务

```bash
# 清理旧镜像
echo "🧹 清理旧Docker镜像..."
docker system prune -f

# 重新构建镜像
echo "🔨 重新构建Docker镜像..."
docker-compose -f deployment/aliyun-deploy-optimized.yml build --no-cache

# 启动所有服务
echo "🚀 启动所有服务..."
docker-compose -f deployment/aliyun-deploy-optimized.yml up -d

# 等待服务启动
echo "⏳ 等待服务启动..."
sleep 30

echo "✅ 服务启动完成"
```

### 第七步：验证部署

```bash
# 检查服务状态
echo "🔍 检查服务状态..."
docker-compose -f deployment/aliyun-deploy-optimized.yml ps

# 检查健康状态
echo "🏥 检查服务健康状态..."
curl -f http://localhost:3003/health && echo "✅ 配置中心正常"
curl -f http://localhost:3004/health && echo "✅ 聊天服务正常"
curl -f http://localhost:8084/health && echo "✅ 认证服务正常"
curl -f http://localhost:8085/health && echo "✅ API网关正常"
```

## 🎯 一键部署脚本

如果您希望使用一键脚本，可以将以下内容保存为 `update.sh` 并执行：

```bash
#!/bin/bash

echo "🚀 QMS-AI 增量更新开始..."

# 配置
PROJECT_DIR="/opt/qms"
BACKUP_DIR="/opt/qms-backup-$(date +%Y%m%d-%H%M%S)"

# 检查项目目录
if [ ! -d "$PROJECT_DIR" ]; then
    echo "❌ 项目目录不存在: $PROJECT_DIR"
    exit 1
fi

cd $PROJECT_DIR

# 备份
echo "💾 创建备份..."
mkdir -p $BACKUP_DIR
docker-compose -f deployment/aliyun-deploy-optimized.yml down
cp -r backend frontend deployment $BACKUP_DIR/
cp .env $BACKUP_DIR/ 2>/dev/null || true

# 更新代码
echo "📥 更新代码..."
git add .
git stash
git fetch origin clean-main
git checkout clean-main
git pull origin clean-main

# 恢复配置
echo "⚙️ 恢复配置..."
if [ -f "$BACKUP_DIR/.env" ]; then
    cp $BACKUP_DIR/.env ./
fi

# 更新依赖
echo "📦 更新依赖..."
if [ -f "backend/nodejs/package.json" ]; then
    cd backend/nodejs
    npm install --production
    cd ../..
fi

# 重新部署
echo "🚀 重新部署..."
docker system prune -f
docker-compose -f deployment/aliyun-deploy-optimized.yml build --no-cache
docker-compose -f deployment/aliyun-deploy-optimized.yml up -d

# 等待启动
echo "⏳ 等待服务启动..."
sleep 30

# 验证
echo "🔍 验证部署..."
docker-compose -f deployment/aliyun-deploy-optimized.yml ps

echo "✅ 更新完成！"
echo "🌐 访问地址:"
echo "  主应用: http://47.108.152.16:8081"
echo "  配置管理: http://47.108.152.16:8072"
echo "  API网关: http://47.108.152.16:8085"
echo "💾 备份位置: $BACKUP_DIR"
```

## 📊 部署后验证

### 检查服务状态
```bash
# 查看所有容器状态
docker-compose -f deployment/aliyun-deploy-optimized.yml ps

# 查看服务日志
docker-compose -f deployment/aliyun-deploy-optimized.yml logs -f
```

### 测试Web访问
在浏览器中访问以下地址：

- **主应用**: http://47.108.152.16:8081
- **配置管理**: http://47.108.152.16:8072
- **API网关**: http://47.108.152.16:8085

### 测试API接口
```bash
# 测试配置中心
curl http://localhost:3003/health

# 测试聊天服务
curl http://localhost:3004/health

# 测试认证服务
curl http://localhost:8084/health

# 测试API网关
curl http://localhost:8085/health
```

## 🛠️ 故障排除

### 如果服务启动失败
```bash
# 查看详细日志
docker-compose -f deployment/aliyun-deploy-optimized.yml logs service-name

# 检查端口占用
netstat -tlnp | grep :3003

# 重启单个服务
docker-compose -f deployment/aliyun-deploy-optimized.yml restart service-name
```

### 如果需要回滚
```bash
# 停止当前服务
docker-compose -f deployment/aliyun-deploy-optimized.yml down

# 恢复备份
cp -r $BACKUP_DIR/* ./

# 重新启动
docker-compose -f deployment/aliyun-deploy-optimized.yml up -d
```

## 🎉 部署完成

部署完成后，您的QMS-AI系统将包含以下新功能：

### ✨ 新增功能
- 🛠️ 全面服务管理器
- 🤖 AutoGPT执行器
- 🤝 CrewAI协调器
- 🧠 LangChain内存管理
- 🎯 智能模型选择器
- 🔧 插件生态系统

### 🎨 界面优化
- 智能问答界面重大优化
- 右侧执行流程看板可视化
- 拖动交互功能增强
- 模型切换功能完全可用

### ⚙️ 系统优化
- 统一配置中心驱动
- 8个AI模型正常工作
- 动态配置更新机制
- 服务健康检查完善

---

**部署完成后，请访问主应用地址验证所有功能是否正常工作！** 🚀
