# QMS-AI 完整系统部署脚本
# 将本地完整代码部署到阿里云服务器

param(
    [string]$ServerIP = "47.108.152.16",
    [string]$Username = "root",
    [string]$DeployPath = "/root/QMS-AI-Complete"
)

Write-Host "🚀 开始QMS-AI完整系统部署..." -ForegroundColor Green

# 检查必要工具
if (!(Get-Command scp -ErrorAction SilentlyContinue)) {
    Write-Host "❌ 需要安装OpenSSH客户端" -ForegroundColor Red
    exit 1
}

# 创建临时部署包
$TempDir = "$env:TEMP\qms-ai-deploy-$(Get-Date -Format 'yyyyMMdd-HHmmss')"
New-Item -ItemType Directory -Path $TempDir -Force | Out-Null
Write-Host "📦 创建临时部署目录: $TempDir" -ForegroundColor Yellow

try {
    # 复制核心文件
    Write-Host "📋 复制项目文件..." -ForegroundColor Yellow
    
    # 复制根目录配置文件
    Copy-Item "package.json" "$TempDir\" -Force
    Copy-Item "pnpm-workspace.yaml" "$TempDir\" -Force
    if (Test-Path "pnpm-lock.yaml") {
        Copy-Item "pnpm-lock.yaml" "$TempDir\" -Force
    }
    
    # 复制后端代码
    Write-Host "📂 复制后端代码..." -ForegroundColor Cyan
    $BackendSource = "backend\nodejs"
    $BackendDest = "$TempDir\backend"
    if (Test-Path $BackendSource) {
        robocopy $BackendSource $BackendDest /E /XD node_modules .git /XF "*.log" "*.tmp" | Out-Null
    }
    
    # 复制前端代码
    Write-Host "📂 复制前端代码..." -ForegroundColor Cyan
    $FrontendAppSource = "frontend\应用端"
    $FrontendAppDest = "$TempDir\frontend\app"
    if (Test-Path $FrontendAppSource) {
        robocopy $FrontendAppSource $FrontendAppDest /E /XD node_modules dist .git /XF "*.log" "*.tmp" | Out-Null
    }
    
    $FrontendConfigSource = "frontend\配置端"
    $FrontendConfigDest = "$TempDir\frontend\config"
    if (Test-Path $FrontendConfigSource) {
        robocopy $FrontendConfigSource $FrontendConfigDest /E /XD node_modules dist .git /XF "*.log" "*.tmp" | Out-Null
    }
    
    # 复制配置文件
    Write-Host "📂 复制配置文件..." -ForegroundColor Cyan
    $ConfigSource = "config"
    $ConfigDest = "$TempDir\config"
    if (Test-Path $ConfigSource) {
        robocopy $ConfigSource $ConfigDest /E /XD node_modules .git /XF "*.log" "*.tmp" | Out-Null
    }
    
    # 创建Docker配置
    Write-Host "🐳 创建Docker配置..." -ForegroundColor Cyan
    
    # 创建完整的docker-compose.yml
    $DockerCompose = @"
version: '3.8'

services:
  # Redis服务
  redis:
    image: redis:7-alpine
    container_name: qms-redis
    ports:
      - "6379:6379"
    volumes:
      - redis-data:/data
      - ./config/redis:/usr/local/etc/redis
    command: redis-server /usr/local/etc/redis/redis.conf
    networks:
      - qms-network
    restart: unless-stopped
    healthcheck:
      test: ["CMD", "redis-cli", "ping"]
      interval: 30s
      timeout: 10s
      retries: 3

  # 后端服务 - 聊天服务
  backend-chat:
    build:
      context: ./backend
      dockerfile: Dockerfile.chat
    container_name: qms-backend-chat
    ports:
      - "3003:3003"
    environment:
      - NODE_ENV=production
      - REDIS_URL=redis://redis:6379
      - PORT=3003
    volumes:
      - ./backend/logs:/app/logs
      - ./backend/database:/app/database
    depends_on:
      redis:
        condition: service_healthy
    networks:
      - qms-network
    restart: unless-stopped
    healthcheck:
      test: ["CMD", "curl", "-f", "http://localhost:3003/health"]
      interval: 30s
      timeout: 10s
      retries: 3

  # 后端服务 - 配置中心
  backend-config:
    build:
      context: ./backend
      dockerfile: Dockerfile.config
    container_name: qms-backend-config
    ports:
      - "3004:3004"
    environment:
      - NODE_ENV=production
      - REDIS_URL=redis://redis:6379
      - PORT=3004
    volumes:
      - ./backend/logs:/app/logs
      - ./config:/app/config
    depends_on:
      redis:
        condition: service_healthy
    networks:
      - qms-network
    restart: unless-stopped
    healthcheck:
      test: ["CMD", "curl", "-f", "http://localhost:3004/health"]
      interval: 30s
      timeout: 10s
      retries: 3

  # 后端服务 - 认证服务
  backend-auth:
    build:
      context: ./backend
      dockerfile: Dockerfile.auth
    container_name: qms-backend-auth
    ports:
      - "8084:8084"
    environment:
      - NODE_ENV=production
      - REDIS_URL=redis://redis:6379
      - PORT=8084
    volumes:
      - ./backend/logs:/app/logs
    depends_on:
      redis:
        condition: service_healthy
    networks:
      - qms-network
    restart: unless-stopped
    healthcheck:
      test: ["CMD", "curl", "-f", "http://localhost:8084/health"]
      interval: 30s
      timeout: 10s
      retries: 3

  # 前端服务 - 应用端
  frontend-app:
    build:
      context: ./frontend/app
      dockerfile: Dockerfile
    container_name: qms-frontend-app
    ports:
      - "8081:8081"
    environment:
      - NODE_ENV=production
      - VUE_APP_API_BASE_URL=http://47.108.152.16:3003
      - VUE_APP_CONFIG_API_URL=http://47.108.152.16:3004
      - VUE_APP_AUTH_API_URL=http://47.108.152.16:8084
    networks:
      - qms-network
    restart: unless-stopped
    healthcheck:
      test: ["CMD", "curl", "-f", "http://localhost:8081/health"]
      interval: 30s
      timeout: 10s
      retries: 3

  # 前端服务 - 配置端
  frontend-config:
    build:
      context: ./frontend/config
      dockerfile: Dockerfile
    container_name: qms-frontend-config
    ports:
      - "8072:8072"
    environment:
      - NODE_ENV=production
      - VUE_APP_API_BASE_URL=http://47.108.152.16:3004
    networks:
      - qms-network
    restart: unless-stopped
    healthcheck:
      test: ["CMD", "curl", "-f", "http://localhost:8072/health"]
      interval: 30s
      timeout: 10s
      retries: 3

  # Nginx反向代理
  nginx:
    image: nginx:alpine
    container_name: qms-nginx
    ports:
      - "80:80"
      - "443:443"
    volumes:
      - ./nginx/nginx.conf:/etc/nginx/nginx.conf
      - ./nginx/conf.d:/etc/nginx/conf.d
    depends_on:
      - frontend-app
      - frontend-config
      - backend-chat
      - backend-config
      - backend-auth
    networks:
      - qms-network
    restart: unless-stopped

volumes:
  redis-data:

networks:
  qms-network:
    driver: bridge
"@
    
    $DockerCompose | Out-File -FilePath "$TempDir\docker-compose.yml" -Encoding UTF8
    
    # 创建部署脚本
    $DeployScript = @"
#!/bin/bash
# QMS-AI 完整系统部署脚本

set -e

echo "🚀 开始QMS-AI完整系统部署..."

# 检查Docker和Docker Compose
if ! command -v docker &> /dev/null; then
    echo "❌ Docker未安装"
    exit 1
fi

if ! command -v docker-compose &> /dev/null; then
    echo "❌ Docker Compose未安装"
    exit 1
fi

# 停止现有服务
echo "🛑 停止现有服务..."
docker-compose down --remove-orphans || true

# 清理旧镜像
echo "🧹 清理旧镜像..."
docker system prune -f

# 构建并启动服务
echo "🔨 构建并启动服务..."
docker-compose up -d --build

# 等待服务启动
echo "⏳ 等待服务启动..."
sleep 30

# 检查服务状态
echo "🔍 检查服务状态..."
docker-compose ps

# 健康检查
echo "💚 执行健康检查..."
for service in backend-chat backend-config backend-auth frontend-app frontend-config; do
    container_name="qms-\$service"
    if docker ps --format "table {{.Names}}" | grep -q "\$container_name"; then
        echo "✅ \$service 运行正常"
    else
        echo "❌ \$service 运行异常"
    fi
done

echo "🎉 QMS-AI完整系统部署完成！"
echo "================================"
echo "✅ 应用端: http://47.108.152.16:8081"
echo "✅ 配置端: http://47.108.152.16:8072"
echo "✅ 聊天API: http://47.108.152.16:3003"
echo "✅ 配置API: http://47.108.152.16:3004"
echo "✅ 认证API: http://47.108.152.16:8084"
echo "✅ Nginx: http://47.108.152.16"
echo "================================"
"@
    
    $DeployScript | Out-File -FilePath "$TempDir\deploy.sh" -Encoding UTF8
    
    Write-Host "📤 上传文件到服务器..." -ForegroundColor Yellow
    
    # 压缩部署包
    $ZipPath = "$env:TEMP\qms-ai-complete-$(Get-Date -Format 'yyyyMMdd-HHmmss').zip"
    Compress-Archive -Path "$TempDir\*" -DestinationPath $ZipPath -Force
    
    Write-Host "📦 部署包已创建: $ZipPath" -ForegroundColor Green
    Write-Host "📤 请手动上传到服务器并执行部署" -ForegroundColor Yellow
    Write-Host ""
    Write-Host "上传命令:" -ForegroundColor Cyan
    Write-Host "scp `"$ZipPath`" $Username@${ServerIP}:/tmp/" -ForegroundColor White
    Write-Host ""
    Write-Host "服务器执行命令:" -ForegroundColor Cyan
    Write-Host "cd /tmp && unzip $(Split-Path $ZipPath -Leaf) -d $DeployPath && cd $DeployPath && chmod +x deploy.sh && ./deploy.sh" -ForegroundColor White
    
} catch {
    Write-Host "❌ 部署过程中出现错误: $($_.Exception.Message)" -ForegroundColor Red
    exit 1
} finally {
    # 清理临时目录
    if (Test-Path $TempDir) {
        Remove-Item $TempDir -Recurse -Force
    }
}

Write-Host "✅ 部署脚本执行完成" -ForegroundColor Green
