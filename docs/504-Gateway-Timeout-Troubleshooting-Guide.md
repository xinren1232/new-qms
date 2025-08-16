# QMS-AI 504 Gateway Timeout 故障排查指南

## 🚨 问题描述
当访问QMS-AI前端时出现"504 Gateway Time-out"错误，这通常表示Nginx代理服务器在等待后端服务响应时超时。

## 🔍 故障排查步骤

### 1. 快速诊断
运行自动诊断脚本：
```bash
# Linux/Mac
bash tools/deployment/fix-504-gateway-timeout.sh

# Windows
tools\deployment\fix-504-gateway-timeout.bat
```

### 2. 手动检查步骤

#### 步骤1: 检查Docker服务状态
```bash
# 检查Docker是否运行
docker --version
systemctl status docker

# 检查QMS容器状态
docker ps --filter "name=qms"
```

#### 步骤2: 检查关键端口
```bash
# 检查端口监听状态
netstat -tlnp | grep -E ":(80|443|3003|3004|8081|8072|8085)"

# 或使用ss命令
ss -tlnp | grep -E ":(80|443|3003|3004|8081|8072|8085)"
```

#### 步骤3: 检查后端服务健康状态
```bash
# 配置中心
curl -f http://localhost:3003/health

# 聊天服务
curl -f http://localhost:3004/health

# 应用端
curl -f http://localhost:8081/health

# 配置端
curl -f http://localhost:8072/health

# API网关
curl -f http://localhost:8085/health
```

#### 步骤4: 检查Nginx配置
```bash
# 找到Nginx容器
nginx_container=$(docker ps --filter "name=nginx" --format "{{.Names}}" | head -1)

# 检查配置语法
docker exec $nginx_container nginx -t

# 查看Nginx错误日志
docker logs $nginx_container --tail 50
```

#### 步骤5: 检查防火墙设置
```bash
# Ubuntu/Debian (UFW)
sudo ufw status

# CentOS/RHEL (firewalld)
sudo firewall-cmd --list-all

# 检查iptables
sudo iptables -L -n
```

## 🛠️ 常见问题和解决方案

### 问题1: 后端服务未启动
**症状**: 后端健康检查失败
**解决方案**:
```bash
# 重启所有服务
docker-compose restart

# 或重启特定服务
docker-compose restart qms-chat-service
docker-compose restart qms-config-service
```

### 问题2: Nginx代理超时
**症状**: Nginx日志显示upstream timeout
**解决方案**: 已在 `nginx/conf.d/qms.conf` 中优化超时配置
- `proxy_connect_timeout 60s`
- `proxy_send_timeout 60s` 
- `proxy_read_timeout 60s`
- API请求超时延长到120s

### 问题3: 防火墙阻止访问
**症状**: 本地可访问，外网无法访问
**解决方案**:
```bash
# Ubuntu/Debian
sudo ufw allow 80
sudo ufw allow 443
sudo ufw allow 8081
sudo ufw allow 8072

# CentOS/RHEL
sudo firewall-cmd --permanent --add-port=80/tcp
sudo firewall-cmd --permanent --add-port=443/tcp
sudo firewall-cmd --permanent --add-port=8081/tcp
sudo firewall-cmd --permanent --add-port=8072/tcp
sudo firewall-cmd --reload
```

### 问题4: 云服务器安全组未配置
**症状**: 防火墙已开放但仍无法访问
**解决方案**: 在云控制台配置安全组
- 阿里云: ECS控制台 → 安全组 → 添加规则
- 腾讯云: CVM控制台 → 安全组 → 入站规则
- AWS: EC2控制台 → Security Groups → Inbound Rules

需要开放的端口:
- 80 (HTTP)
- 443 (HTTPS)
- 8081 (应用端)
- 8072 (配置端)

### 问题5: 内存不足导致服务异常
**症状**: 容器频繁重启或响应缓慢
**解决方案**:
```bash
# 检查内存使用
free -h
docker stats

# 清理无用镜像和容器
docker system prune -f

# 如果内存不足，考虑升级服务器配置
```

### 问题6: 磁盘空间不足
**症状**: 容器无法启动或日志写入失败
**解决方案**:
```bash
# 检查磁盘使用
df -h

# 清理Docker数据
docker system prune -a -f

# 清理日志文件
sudo journalctl --vacuum-time=7d
```

## 🔧 高级排查

### 查看详细日志
```bash
# 查看所有容器日志
docker-compose logs

# 查看特定服务日志
docker logs qms-nginx --tail 100
docker logs qms-chat-service --tail 100
docker logs qms-config-service --tail 100

# 实时查看日志
docker logs -f qms-nginx
```

### 网络连接测试
```bash
# 测试容器间网络连接
docker exec qms-nginx ping qms-chat-service
docker exec qms-nginx ping qms-config-service

# 测试端口连通性
telnet localhost 3003
telnet localhost 3004
telnet localhost 8081
telnet localhost 8072
```

### 性能分析
```bash
# 检查系统负载
top
htop

# 检查网络连接
netstat -an | grep ESTABLISHED | wc -l

# 检查进程状态
ps aux | grep -E "(nginx|node|docker)"
```

## 📋 预防措施

### 1. 监控配置
设置服务监控和告警:
```bash
# 使用脚本定期检查服务状态
*/5 * * * * /path/to/tools/deployment/quick-fix-504.sh
```

### 2. 日志轮转
配置日志轮转防止磁盘满:
```bash
# 在docker-compose.yml中添加日志配置
logging:
  driver: "json-file"
  options:
    max-size: "10m"
    max-file: "3"
```

### 3. 健康检查
确保所有服务都有健康检查端点:
```yaml
healthcheck:
  test: ["CMD", "curl", "-f", "http://localhost:3004/health"]
  interval: 30s
  timeout: 10s
  retries: 3
```

### 4. 资源限制
为容器设置合理的资源限制:
```yaml
deploy:
  resources:
    limits:
      memory: 512M
      cpus: '0.5'
```

## 🆘 紧急恢复

如果所有方法都无效，可以尝试完全重新部署:

```bash
# 1. 停止所有服务
docker-compose down

# 2. 清理所有容器和网络
docker system prune -f

# 3. 重新构建和启动
docker-compose up --build -d

# 4. 等待服务启动
sleep 60

# 5. 验证服务状态
bash tools/deployment/quick-fix-504.sh
```

## 📞 获取帮助

如果问题仍然存在，请收集以下信息:
1. 服务器配置 (CPU、内存、磁盘)
2. 操作系统版本
3. Docker版本
4. 错误日志 (Nginx、应用日志)
5. 网络配置 (防火墙、安全组)

然后联系技术支持或在项目Issues中提交问题。
