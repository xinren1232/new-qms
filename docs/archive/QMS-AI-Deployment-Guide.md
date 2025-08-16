# 🚀 QMS-AI渐进式部署指南

## 📊 部署策略概览

**目标**: 从本地开发平滑过渡到云端生产环境  
**原则**: 渐进式、低风险、易维护  
**适用**: 软件开发新手和快速迭代项目

---

## 🎯 三阶段部署策略

### 阶段一：本地容器化 (推荐当前执行)
**时间**: 1-2天  
**风险**: 极低  
**目标**: 统一开发环境，为云端部署做准备

### 阶段二：单机云端部署 (功能稳定后)
**时间**: 1-3天  
**风险**: 低  
**目标**: 体验云端环境，学习运维知识

### 阶段三：生产级云端部署 (正式上线前)
**时间**: 3-7天  
**风险**: 中等  
**目标**: 高可用、可扩展的生产环境

---

## 🐳 阶段一：本地容器化详细指南

### 1. 安装Docker Desktop
**Windows用户**:
1. 下载Docker Desktop: https://www.docker.com/products/docker-desktop
2. 安装并启动Docker Desktop
3. 验证安装: `docker --version`

### 2. 创建简化的Docker配置

#### 2.1 创建主服务Dockerfile
```dockerfile
# backend/nodejs/Dockerfile
FROM node:18-alpine

WORKDIR /app

# 复制package文件
COPY package*.json ./

# 安装依赖
RUN npm install --production

# 复制源代码
COPY . .

# 暴露端口
EXPOSE 3004

# 启动命令
CMD ["node", "chat-service.js"]
```

#### 2.2 创建简化的docker-compose.yml
```yaml
# docker-compose.local.yml
version: '3.8'

services:
  # QMS聊天服务
  qms-chat:
    build: ./backend/nodejs
    ports:
      - "3004:3004"
    environment:
      - NODE_ENV=development
      - DB_TYPE=sqlite
      - CACHE_ENABLED=false
    volumes:
      - ./backend/nodejs:/app
      - /app/node_modules
    restart: unless-stopped

  # QMS前端应用
  qms-frontend:
    build: ./frontend/应用端
    ports:
      - "8080:8080"
    volumes:
      - ./frontend/应用端:/app
      - /app/node_modules
    restart: unless-stopped

  # QMS配置端
  qms-config:
    build: ./frontend/配置端
    ports:
      - "8072:8072"
    volumes:
      - ./frontend/配置端:/app
      - /app/node_modules
    restart: unless-stopped

volumes:
  qms_data:
```

### 3. 本地容器化的优势
- ✅ **环境隔离**: 每个服务独立运行
- ✅ **一键启动**: `docker-compose up -d`
- ✅ **便于调试**: 代码热重载
- ✅ **资源控制**: 可以限制CPU和内存使用
- ✅ **为云端做准备**: 配置可直接用于服务器

---

## ☁️ 阶段二：单机云端部署指南

### 1. 阿里云ECS选择建议
**推荐配置** (开发测试用):
- **实例规格**: ecs.t6-c1m2.large (2核4GB)
- **操作系统**: Ubuntu 20.04 LTS
- **存储**: 40GB SSD云盘
- **网络**: 按使用流量计费
- **预估费用**: 约200-300元/月

### 2. 服务器环境准备
```bash
# 1. 更新系统
sudo apt update && sudo apt upgrade -y

# 2. 安装Docker
curl -fsSL https://get.docker.com -o get-docker.sh
sudo sh get-docker.sh
sudo usermod -aG docker $USER

# 3. 安装Docker Compose
sudo curl -L "https://github.com/docker/compose/releases/download/v2.20.0/docker-compose-$(uname -s)-$(uname -m)" -o /usr/local/bin/docker-compose
sudo chmod +x /usr/local/bin/docker-compose

# 4. 安装Git
sudo apt install git -y
```

### 3. 代码部署流程
```bash
# 1. 克隆代码
git clone <your-repo-url> qms-ai
cd qms-ai

# 2. 启动服务
docker-compose -f docker-compose.local.yml up -d

# 3. 查看服务状态
docker-compose ps

# 4. 查看日志
docker-compose logs -f qms-chat
```

### 4. 域名和SSL配置 (可选)
```bash
# 1. 安装Nginx
sudo apt install nginx -y

# 2. 配置反向代理
sudo nano /etc/nginx/sites-available/qms-ai

# 3. 申请SSL证书 (Let's Encrypt)
sudo apt install certbot python3-certbot-nginx -y
sudo certbot --nginx -d your-domain.com
```

---

## 🏭 阶段三：生产级云端部署指南

### 1. 阿里云产品选择
**推荐架构**:
- **ECS**: 2台实例做负载均衡
- **RDS**: PostgreSQL数据库
- **Redis**: 缓存服务
- **SLB**: 负载均衡器
- **OSS**: 对象存储
- **CDN**: 内容分发网络

### 2. 高可用部署架构
```
Internet
    ↓
[SLB负载均衡]
    ↓
[ECS-1] [ECS-2]
    ↓
[RDS PostgreSQL] [Redis]
    ↓
[OSS对象存储]
```

---

## 🛠️ 维护和更新策略

### 本地开发时的更新流程
```bash
# 1. 修改代码后重启服务
docker-compose restart qms-chat

# 2. 查看实时日志
docker-compose logs -f qms-chat

# 3. 进入容器调试
docker-compose exec qms-chat sh
```

### 云端更新流程
```bash
# 1. 拉取最新代码
git pull origin main

# 2. 重新构建镜像
docker-compose build

# 3. 滚动更新服务
docker-compose up -d --no-deps qms-chat

# 4. 验证服务状态
curl http://localhost:3004/health
```

---

## 📊 成本预估

### 本地开发成本
- **硬件要求**: 8GB内存，4核CPU
- **软件成本**: 免费 (Docker Desktop个人版)
- **时间成本**: 1-2天学习和配置

### 云端部署成本 (月费用)
| 阶段 | 配置 | 预估费用 | 适用场景 |
|------|------|----------|----------|
| 阶段二 | 单机ECS | 200-300元 | 开发测试 |
| 阶段三 | 高可用架构 | 800-1500元 | 生产环境 |

---

## 🎯 推荐的实施计划

### 第1周：本地容器化
- [ ] 安装Docker Desktop
- [ ] 创建Dockerfile和docker-compose.yml
- [ ] 测试本地容器化部署
- [ ] 熟悉Docker基本命令

### 第2-3周：功能完善
- [ ] 继续本地开发和测试
- [ ] 完善功能和修复bug
- [ ] 准备云端部署配置

### 第4周：云端部署 (功能相对稳定后)
- [ ] 购买阿里云ECS
- [ ] 部署到云端测试环境
- [ ] 配置域名和SSL
- [ ] 监控和日志配置

---

## 🚨 风险控制建议

### 1. 数据备份策略
```bash
# 本地数据备份
docker-compose exec qms-chat cp /app/chat_history.db /backup/

# 云端数据备份
# 使用阿里云RDS自动备份功能
```

### 2. 回滚策略
```bash
# 快速回滚到上一个版本
git checkout HEAD~1
docker-compose up -d --build
```

### 3. 监控告警
- 使用阿里云监控服务
- 设置CPU、内存、磁盘使用率告警
- 配置服务可用性监控

---

## 💡 新手友好的学习路径

### 必学技能 (按优先级)
1. **Docker基础** (1-2天)
   - 容器概念
   - 基本命令
   - docker-compose使用

2. **Linux基础** (2-3天)
   - 文件操作
   - 进程管理
   - 网络配置

3. **Nginx配置** (1天)
   - 反向代理
   - SSL配置
   - 负载均衡

4. **云服务使用** (2-3天)
   - ECS管理
   - 安全组配置
   - 域名解析

### 学习资源推荐
- **Docker官方教程**: https://docs.docker.com/get-started/
- **阿里云大学**: https://edu.aliyun.com/
- **菜鸟教程Linux**: https://www.runoob.com/linux/linux-tutorial.html

---

## 🎊 总结建议

### 当前最佳选择：阶段一本地容器化
**理由**:
1. **风险最低** - 不影响现有开发环境
2. **学习成本低** - 逐步掌握Docker技能
3. **便于调试** - 本地环境便于问题排查
4. **为云端做准备** - 配置可直接迁移到服务器

### 何时考虑云端部署
- ✅ 功能相对稳定 (变动频率 < 1次/周)
- ✅ 掌握Docker基础操作
- ✅ 需要外部访问或演示
- ✅ 团队协作需要共享环境

**建议：先花1-2天时间完成本地容器化，熟悉Docker后再考虑云端部署！** 🚀
