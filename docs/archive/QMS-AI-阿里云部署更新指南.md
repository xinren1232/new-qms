# QMS-AI 阿里云部署更新指南

## 📋 更新概述

本次更新包含以下主要改进：
- ✅ 全面服务管理器优化
- ✅ AI智能功能增强
- ✅ 前端界面改进
- ✅ 系统性能优化
- ✅ AutoGPT执行器
- ✅ CrewAI协调器
- ✅ LangChain内存管理
- ✅ 智能模型选择器
- ✅ 插件生态系统

## 🚀 快速部署步骤

### 步骤1: 上传更新包到服务器

```bash
# 上传更新包到阿里云服务器
scp qms-update-package.zip root@YOUR_SERVER_IP:/opt/

# 或者使用已有的阿里云部署包
scp qms-aliyun-deploy.zip root@YOUR_SERVER_IP:/opt/
```

### 步骤2: 登录服务器并解压

```bash
# 登录阿里云服务器
ssh root@YOUR_SERVER_IP

# 进入部署目录
cd /opt

# 解压更新包
unzip -o qms-update-package.zip
cd qms-update-package

# 或者解压阿里云部署包
# unzip -o qms-aliyun-deploy.zip
# cd qms-aliyun-deploy
```

### 步骤3: 配置环境变量

```bash
# 复制环境变量配置
cp deployment/.env.aliyun .env

# 编辑环境变量（根据您的实际配置修改）
vim .env

# 主要配置项：
# - 数据库配置（如使用阿里云RDS）
# - Redis配置（如使用阿里云Redis）
# - AI API密钥
# - 域名配置
```

### 步骤4: 执行快速更新

```bash
# 给脚本执行权限
chmod +x deployment/quick-update.sh

# 执行快速更新
./deployment/quick-update.sh
```

## 🔧 详细配置说明

### 环境变量配置

主要需要配置的环境变量：

```bash
# 数据库配置
ALIYUN_RDS_HOST=your-rds-host.mysql.rds.aliyuncs.com
ALIYUN_RDS_PASSWORD=your_rds_password
ALIYUN_RDS_DATABASE=qms_ai

# Redis配置
ALIYUN_REDIS_HOST=your-redis-host.redis.rds.aliyuncs.com
ALIYUN_REDIS_PASSWORD=your_redis_password

# AI服务配置
TRANSSION_API_KEY=sk_a264854a38dc034077c23f5951285907e6c40d1b4843f46f95e5f31
DEEPSEEK_API_KEY=sk-cab797574abf4288bcfaca253191565d

# 安全配置
JWT_SECRET=your_secure_jwt_secret
ENCRYPTION_KEY=your_encryption_key

# 域名配置
DOMAIN=your-domain.com
```

### 服务端口配置

更新后的服务端口：
- **配置中心**: 3003 (http://服务器:3003/health)
- **聊天服务**: 3004 (http://服务器:3004/health)
- **API网关**: 8085 (http://服务器:8085/health)
- **前端应用**: 8081 (http://服务器:8081)
- **配置管理**: 8072 (http://服务器:8072)

## 🐳 Docker部署方式

如果使用Docker部署：

```bash
# 使用优化的Docker Compose配置
docker-compose -f deployment/aliyun-deploy-optimized.yml up -d

# 查看服务状态
docker-compose -f deployment/aliyun-deploy-optimized.yml ps

# 查看日志
docker-compose -f deployment/aliyun-deploy-optimized.yml logs -f
```

## 📊 PM2部署方式（推荐）

使用PM2进行进程管理：

```bash
# 安装PM2
npm install -g pm2

# 启动服务
cd backend/nodejs
pm2 start ecosystem.config.js

# 查看状态
pm2 status

# 查看日志
pm2 logs

# 保存配置
pm2 save

# 设置开机自启
pm2 startup
```

## 🔍 部署验证

### 健康检查

```bash
# 检查各服务健康状态
curl http://localhost:3003/health  # 配置中心
curl http://localhost:3004/health  # 聊天服务
curl http://localhost:8085/health  # API网关

# 检查前端访问
curl http://localhost:8081  # 前端应用
```

### 功能测试

1. **访问主应用**: http://YOUR_SERVER_IP:8081
2. **访问配置管理**: http://YOUR_SERVER_IP:8072
3. **测试AI聊天功能**
4. **验证模型切换功能**
5. **检查系统监控**

## 🛠️ 故障排除

### 常见问题

1. **服务启动失败**
   ```bash
   # 检查日志
   pm2 logs qms-chat-service
   
   # 重启服务
   pm2 restart qms-chat-service
   ```

2. **数据库连接失败**
   ```bash
   # 检查数据库配置
   cat .env | grep DB_
   
   # 测试数据库连接
   mysql -h $ALIYUN_RDS_HOST -u $ALIYUN_RDS_USER -p
   ```

3. **Redis连接失败**
   ```bash
   # 检查Redis配置
   cat .env | grep REDIS_
   
   # 测试Redis连接
   redis-cli -h $ALIYUN_REDIS_HOST -p 6379 -a $ALIYUN_REDIS_PASSWORD ping
   ```

### 回滚操作

如果更新出现问题，可以回滚到备份版本：

```bash
# 查看备份目录
ls -la /opt/qms-backup-*

# 停止当前服务
pm2 stop all

# 恢复备份
cp -r /opt/qms-backup-YYYYMMDD-HHMMSS/* /opt/qms/

# 重启服务
pm2 start all
```

## 📞 技术支持

如遇到部署问题，请检查：
1. 服务器资源是否充足（内存、磁盘空间）
2. 网络连接是否正常
3. 环境变量配置是否正确
4. 端口是否被占用或被防火墙阻止

## 🎉 部署完成

部署成功后，您的QMS-AI系统将具备：
- ✅ 最新的AI功能和界面优化
- ✅ 增强的系统性能和稳定性
- ✅ 完整的监控和日志系统
- ✅ 自动化运维管理
- ✅ 生产级安全配置

---

**更新时间**: 2025-08-14
**版本**: v2.0 增量更新
**包含内容**: 全面AI功能优化和系统性能提升
