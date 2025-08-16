# 🔧 Docker容器化故障排除指南

## 🚨 常见问题和解决方案

### 1. Docker未安装或未启动

**症状**: 运行 `docker --version` 提示命令不存在

**解决方案**:
1. 下载Docker Desktop: https://www.docker.com/products/docker-desktop
2. 安装并重启计算机
3. 启动Docker Desktop，等待引擎启动完成

### 2. Docker服务未运行

**症状**: `docker info` 提示连接失败

**解决方案**:
1. 确保Docker Desktop已启动
2. 检查系统托盘中Docker图标是否为绿色
3. 如果是红色，点击图标查看错误信息

### 3. 镜像构建失败

**症状**: `docker-compose build` 失败

**可能原因和解决方案**:

#### 3.1 网络问题
```bash
# 症状: 下载Node.js镜像超时
# 解决: 配置Docker镜像源
```

#### 3.2 磁盘空间不足
```bash
# 检查磁盘空间
docker system df

# 清理无用镜像和容器
docker system prune -f
```

#### 3.3 端口被占用
```bash
# 检查端口占用
netstat -ano | findstr :3004
netstat -ano | findstr :8080
netstat -ano | findstr :8072

# 停止占用端口的进程
taskkill /PID <进程ID> /F
```

### 4. 容器启动失败

**症状**: 容器状态显示 "Exited"

**排查步骤**:
```bash
# 查看容器日志
docker-compose -f docker-compose.local.yml logs qms-chat-service

# 查看所有服务日志
docker-compose -f docker-compose.local.yml logs
```

### 5. 服务无法访问

**症状**: 浏览器无法打开 http://localhost:8080

**排查步骤**:
1. 检查容器状态: `docker-compose ps`
2. 检查端口映射: `docker port <容器名>`
3. 检查防火墙设置
4. 等待服务完全启动 (可能需要1-2分钟)

### 6. 前端构建错误

**症状**: 前端容器启动失败，提示npm错误

**解决方案**:
```bash
# 清理node_modules
docker-compose down
docker-compose build --no-cache

# 或者手动清理
rmdir /s frontend\应用端\node_modules
rmdir /s frontend\配置端\node_modules
```

## 🔍 调试命令

### 查看服务状态
```bash
# 查看所有容器状态
docker-compose -f docker-compose.local.yml ps

# 查看Docker系统信息
docker info

# 查看镜像列表
docker images
```

### 查看日志
```bash
# 查看特定服务日志
docker-compose -f docker-compose.local.yml logs qms-chat-service

# 实时查看日志
docker-compose -f docker-compose.local.yml logs -f

# 查看最近50行日志
docker-compose -f docker-compose.local.yml logs --tail=50
```

### 进入容器调试
```bash
# 进入聊天服务容器
docker-compose -f docker-compose.local.yml exec qms-chat-service sh

# 进入应用端容器
docker-compose -f docker-compose.local.yml exec qms-frontend sh
```

### 重启服务
```bash
# 重启特定服务
docker-compose -f docker-compose.local.yml restart qms-chat-service

# 重启所有服务
docker-compose -f docker-compose.local.yml restart

# 强制重新创建容器
docker-compose -f docker-compose.local.yml up -d --force-recreate
```

## 🆘 紧急恢复

### 完全重置
```bash
# 停止所有服务
docker-compose -f docker-compose.local.yml down

# 删除所有相关镜像
docker rmi $(docker images -q --filter reference="qms01*")

# 清理系统
docker system prune -f

# 重新构建和启动
docker-compose -f docker-compose.local.yml up -d --build
```

### 回退到非Docker模式
如果Docker出现严重问题，可以临时回退到原来的启动方式：
```bash
# 停止Docker服务
docker-compose -f docker-compose.local.yml down

# 使用原来的启动脚本
start-qms.bat
```

## 📞 获取帮助

### 查看Docker版本信息
```bash
docker --version
docker-compose --version
docker info
```

### 导出日志用于分析
```bash
# 导出所有日志到文件
docker-compose -f docker-compose.local.yml logs > docker-logs.txt
```

### 检查系统资源
```bash
# 查看Docker资源使用
docker stats

# 查看磁盘使用
docker system df
```

## 💡 性能优化建议

### 1. 分配更多资源给Docker
在Docker Desktop设置中：
- 增加CPU核心数 (推荐4核)
- 增加内存 (推荐8GB)
- 增加磁盘空间 (推荐50GB)

### 2. 启用文件共享优化
在Docker Desktop设置中启用：
- Use the WSL 2 based engine
- Enable VirtioFS accelerated directory sharing

### 3. 定期清理
```bash
# 每周运行一次清理
docker system prune -f
docker volume prune -f
```

---

**如果遇到无法解决的问题，请保存错误日志并寻求技术支持！** 🆘
