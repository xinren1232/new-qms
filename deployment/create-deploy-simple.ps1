# QMS-AI 阿里云部署包创建脚本 (简化版)

Write-Host "🚀 开始创建 QMS-AI 阿里云部署包..." -ForegroundColor Green

# 设置变量
$DEPLOY_DIR = "qms-aliyun-deploy"
$ARCHIVE_NAME = "qms-aliyun-deploy.zip"

# 清理旧的部署目录
if (Test-Path $DEPLOY_DIR) {
    Write-Host "🧹 清理旧的部署目录..." -ForegroundColor Yellow
    Remove-Item -Recurse -Force $DEPLOY_DIR
}

# 创建部署目录结构
Write-Host "📁 创建部署目录结构..." -ForegroundColor Cyan
New-Item -ItemType Directory -Force -Path "$DEPLOY_DIR\backend\nodejs" | Out-Null
New-Item -ItemType Directory -Force -Path "$DEPLOY_DIR\frontend" | Out-Null
New-Item -ItemType Directory -Force -Path "$DEPLOY_DIR\config" | Out-Null
New-Item -ItemType Directory -Force -Path "$DEPLOY_DIR\deployment" | Out-Null
New-Item -ItemType Directory -Force -Path "$DEPLOY_DIR\nginx" | Out-Null
New-Item -ItemType Directory -Force -Path "$DEPLOY_DIR\logs" | Out-Null
New-Item -ItemType Directory -Force -Path "$DEPLOY_DIR\data" | Out-Null
New-Item -ItemType Directory -Force -Path "$DEPLOY_DIR\ssl" | Out-Null
New-Item -ItemType Directory -Force -Path "$DEPLOY_DIR\mysql" | Out-Null

# 复制后端文件
Write-Host "📦 复制后端文件..." -ForegroundColor Cyan
if (Test-Path "backend\nodejs") {
    Copy-Item -Recurse -Force "backend\nodejs\*" "$DEPLOY_DIR\backend\nodejs\"
}

# 创建完整的 Docker Compose 文件
Write-Host "🐳 创建 Docker Compose 配置..." -ForegroundColor Cyan
@"
version: '3.8'

services:
  mysql:
    image: mysql:8.0
    container_name: qms-mysql
    restart: unless-stopped
    environment:
      MYSQL_ROOT_PASSWORD: qms123456
      MYSQL_DATABASE: qms_ai
      MYSQL_USER: qms_user
      MYSQL_PASSWORD: qms123456
    ports:
      - "3306:3306"
    volumes:
      - ./mysql:/var/lib/mysql
      - ./logs:/var/log/mysql
      - ./config/init-db.sql:/docker-entrypoint-initdb.d/init.sql
    command: --default-authentication-plugin=mysql_native_password

  redis:
    image: redis:7-alpine
    container_name: qms-redis
    restart: unless-stopped
    ports:
      - "6379:6379"
    volumes:
      - ./data/redis:/data
    command: redis-server --requirepass qms123456 --appendonly yes

  auth-service:
    image: node:18-alpine
    container_name: qms-auth-service
    restart: unless-stopped
    working_dir: /app
    ports:
      - "8084:8084"
    volumes:
      - ./backend/nodejs:/app
    environment:
      - NODE_ENV=production
      - PORT=8084
      - MYSQL_HOST=mysql
      - MYSQL_PORT=3306
      - MYSQL_USER=qms_user
      - MYSQL_PASSWORD=qms123456
      - MYSQL_DATABASE=qms_ai
      - REDIS_HOST=redis
      - REDIS_PORT=6379
      - REDIS_PASSWORD=qms123456
    command: sh -c "npm install --production && node auth-service.js"
    depends_on:
      - mysql
      - redis

  config-center:
    image: node:18-alpine
    container_name: qms-config-center
    restart: unless-stopped
    working_dir: /app
    ports:
      - "8083:8083"
    volumes:
      - ./backend/nodejs:/app
    environment:
      - NODE_ENV=production
      - PORT=8083
      - REDIS_HOST=redis
      - REDIS_PORT=6379
      - REDIS_PASSWORD=qms123456
    command: sh -c "npm install --production && node lightweight-config-service.js"
    depends_on:
      - redis

  chat-service:
    image: node:18-alpine
    container_name: qms-chat-service
    restart: unless-stopped
    working_dir: /app
    ports:
      - "3004:3004"
    volumes:
      - ./backend/nodejs:/app
    environment:
      - NODE_ENV=production
      - PORT=3004
      - MYSQL_HOST=mysql
      - MYSQL_PORT=3306
      - MYSQL_USER=qms_user
      - MYSQL_PASSWORD=qms123456
      - MYSQL_DATABASE=qms_ai
      - REDIS_HOST=redis
      - REDIS_PORT=6379
      - REDIS_PASSWORD=qms123456
    command: sh -c "npm install --production && node chat-service.js"
    depends_on:
      - mysql
      - redis
      - config-center

  nginx:
    image: nginx:alpine
    container_name: qms-nginx
    restart: unless-stopped
    ports:
      - "8081:80"
    volumes:
      - ./nginx/nginx.conf:/etc/nginx/nginx.conf
      - ./frontend:/usr/share/nginx/html
      - ./logs:/var/log/nginx
    depends_on:
      - auth-service
      - config-center
      - chat-service
"@ | Out-File -FilePath "$DEPLOY_DIR\docker-compose.yml" -Encoding UTF8

# 创建 Nginx 配置文件
Write-Host "🌐 创建 Nginx 配置..." -ForegroundColor Cyan
@"
events {
    worker_connections 1024;
}

http {
    include       /etc/nginx/mime.types;
    default_type  application/octet-stream;

    access_log /var/log/nginx/access.log;
    error_log /var/log/nginx/error.log;

    sendfile on;
    keepalive_timeout 65;

    gzip on;
    gzip_types text/plain text/css application/json application/javascript text/xml application/xml application/xml+rss text/javascript;

    upstream auth_backend {
        server auth-service:8084;
    }

    upstream config_backend {
        server config-center:8083;
    }

    upstream chat_backend {
        server chat-service:3004;
    }

    server {
        listen 80;
        server_name _;

        root /usr/share/nginx/html;
        index index.html;

        location / {
            try_files `$uri `$uri/ /index.html;
        }

        location /api/auth/ {
            proxy_pass http://auth_backend/api/auth/;
            proxy_set_header Host `$host;
            proxy_set_header X-Real-IP `$remote_addr;
            proxy_set_header X-Forwarded-For `$proxy_add_x_forwarded_for;
        }

        location /api/config/ {
            proxy_pass http://config_backend/api/;
            proxy_set_header Host `$host;
            proxy_set_header X-Real-IP `$remote_addr;
            proxy_set_header X-Forwarded-For `$proxy_add_x_forwarded_for;
        }

        location /api/chat/ {
            proxy_pass http://chat_backend/api/chat/;
            proxy_set_header Host `$host;
            proxy_set_header X-Real-IP `$remote_addr;
            proxy_set_header X-Forwarded-For `$proxy_add_x_forwarded_for;
        }

        location /health {
            return 200 "healthy\n";
            add_header Content-Type text/plain;
        }
    }
}
"@ | Out-File -FilePath "$DEPLOY_DIR\nginx\nginx.conf" -Encoding UTF8

# 创建数据库初始化脚本
Write-Host "🗄️ 创建数据库初始化脚本..." -ForegroundColor Cyan
@"
-- QMS-AI 数据库初始化脚本

CREATE TABLE IF NOT EXISTS users (
    id INT AUTO_INCREMENT PRIMARY KEY,
    username VARCHAR(50) UNIQUE NOT NULL,
    password VARCHAR(255) NOT NULL,
    email VARCHAR(100),
    role VARCHAR(20) DEFAULT 'user',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS chat_records (
    id INT AUTO_INCREMENT PRIMARY KEY,
    user_id INT,
    session_id VARCHAR(100),
    message TEXT,
    response TEXT,
    model_id VARCHAR(50),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES users(id)
);

CREATE TABLE IF NOT EXISTS ai_models (
    id VARCHAR(50) PRIMARY KEY,
    name VARCHAR(100) NOT NULL,
    provider VARCHAR(50) NOT NULL,
    api_endpoint VARCHAR(255),
    api_key VARCHAR(255),
    max_tokens INT DEFAULT 4000,
    temperature DECIMAL(3,2) DEFAULT 0.7,
    status ENUM('active', 'inactive') DEFAULT 'active',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
);

INSERT IGNORE INTO ai_models (id, name, provider, api_endpoint, max_tokens, temperature) VALUES
('deepseek-chat', 'DeepSeek Chat', 'deepseek', 'https://api.deepseek.com/v1/chat/completions', 4000, 0.7),
('deepseek-coder', 'DeepSeek Coder', 'deepseek', 'https://api.deepseek.com/v1/chat/completions', 4000, 0.7);

INSERT IGNORE INTO users (username, password, email, role) VALUES
('admin', '`$2b`$10`$rOzJqQjQjQjQjQjQjQjQjOzJqQjQjQjQjQjQjQjQjQjQjQjQjQjQjQ', 'admin@qms.com', 'admin');
"@ | Out-File -FilePath "$DEPLOY_DIR\config\init-db.sql" -Encoding UTF8

# 创建部署脚本
Write-Host "🚀 创建部署脚本..." -ForegroundColor Cyan
@"
#!/bin/bash

echo "🚀 开始部署 QMS-AI 到阿里云..."

# 检查 Docker
if ! command -v docker &> /dev/null; then
    echo "❌ Docker 未安装，正在安装..."
    curl -fsSL https://get.docker.com | sh
    systemctl start docker
    systemctl enable docker
fi

if ! command -v docker-compose &> /dev/null; then
    echo "❌ Docker Compose 未安装，正在安装..."
    curl -L "https://github.com/docker/compose/releases/download/v2.20.0/docker-compose-`$(uname -s)-`$(uname -m)" -o /usr/local/bin/docker-compose
    chmod +x /usr/local/bin/docker-compose
fi

# 停止现有服务
echo "🛑 停止现有服务..."
docker-compose down 2>/dev/null || true

# 创建必要目录
echo "📁 创建必要目录..."
mkdir -p {logs,data/redis,ssl,mysql}
chmod -R 755 logs data ssl mysql

# 启动服务
echo "🚀 启动服务..."
docker-compose up -d

# 等待服务启动
echo "⏳ 等待服务启动..."
sleep 30

# 检查服务状态
echo "📊 检查服务状态..."
docker-compose ps

echo ""
echo "🎉 QMS-AI 部署完成！"
echo "📋 访问地址: http://`$(curl -s ifconfig.me):8081"
echo "🔧 管理命令:"
echo "- 查看状态: docker-compose ps"
echo "- 查看日志: docker-compose logs -f"
echo "- 重启服务: docker-compose restart"
echo "- 停止服务: docker-compose down"
"@ | Out-File -FilePath "$DEPLOY_DIR\deployment\aliyun-deploy.sh" -Encoding UTF8

# 创建简单的前端页面
Write-Host "🎨 创建前端页面..." -ForegroundColor Cyan
@"
<!DOCTYPE html>
<html lang="zh-CN">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>QMS-AI 管理系统</title>
    <style>
        body { font-family: Arial, sans-serif; margin: 0; padding: 20px; background: #f5f5f5; }
        .container { max-width: 800px; margin: 0 auto; background: white; padding: 30px; border-radius: 8px; box-shadow: 0 2px 10px rgba(0,0,0,0.1); }
        h1 { color: #333; text-align: center; }
        .service { margin: 10px 0; padding: 10px; background: #f8f9fa; border-radius: 4px; }
        .btn { display: inline-block; padding: 10px 20px; margin: 5px; background: #007bff; color: white; text-decoration: none; border-radius: 4px; }
    </style>
</head>
<body>
    <div class="container">
        <h1>🚀 QMS-AI 管理系统</h1>
        <p>欢迎使用 QMS-AI 智能质量管理系统！</p>

        <div>
            <h3>📊 服务状态</h3>
            <div class="service">认证服务: <a href="/api/auth/health" class="btn">检查</a></div>
            <div class="service">配置中心: <a href="/api/config/health" class="btn">检查</a></div>
            <div class="service">聊天服务: <a href="/api/chat/health" class="btn">检查</a></div>
        </div>

        <div>
            <h3>🔗 快速链接</h3>
            <a href="/api/chat/models" class="btn">AI模型列表</a>
            <a href="/api/config/configs" class="btn">配置管理</a>
            <a href="/health" class="btn">系统健康</a>
        </div>
    </div>
</body>
</html>
"@ | Out-File -FilePath "$DEPLOY_DIR\frontend\index.html" -Encoding UTF8

# 创建 README
Write-Host "📖 创建 README..." -ForegroundColor Cyan
@"
# QMS-AI 阿里云部署包

## 🚀 快速部署

1. 上传到服务器: scp qms-aliyun-deploy.zip root@47.108.152.16:/opt/
2. 登录服务器: ssh root@47.108.152.16
3. 解压: cd /opt && unzip qms-aliyun-deploy.zip && cd qms-aliyun-deploy
4. 部署: bash deployment/aliyun-deploy.sh

## 🌐 访问地址

- Web界面: http://47.108.152.16:8081
- 认证服务: http://47.108.152.16:8084
- 配置中心: http://47.108.152.16:8083
- 聊天服务: http://47.108.152.16:3004

## 📊 默认配置

- MySQL: qms_ai/qms_user/qms123456
- Redis: qms123456
- 管理员: admin/admin123
"@ | Out-File -FilePath "$DEPLOY_DIR\README.md" -Encoding UTF8

# 创建压缩包
Write-Host "🗜️ 创建压缩包..." -ForegroundColor Yellow
if (Test-Path $ARCHIVE_NAME) {
    Remove-Item $ARCHIVE_NAME -Force
}

Compress-Archive -Path $DEPLOY_DIR -DestinationPath $ARCHIVE_NAME -Force

# 显示结果
Write-Host ""
Write-Host "✅ 部署包创建完成!" -ForegroundColor Green
Write-Host "📦 文件名: $ARCHIVE_NAME" -ForegroundColor Cyan
$fileSize = (Get-Item $ARCHIVE_NAME).Length / 1MB
Write-Host "📊 包大小: $($fileSize.ToString('F2')) MB" -ForegroundColor Cyan
Write-Host ""
Write-Host "🚀 下一步操作:" -ForegroundColor Green
Write-Host "1. 上传到服务器: scp $ARCHIVE_NAME root@47.108.152.16:/opt/" -ForegroundColor White
Write-Host "2. 登录服务器: ssh root@47.108.152.16" -ForegroundColor White
Write-Host "3. 解压并部署: cd /opt && unzip $ARCHIVE_NAME && cd qms-aliyun-deploy && bash deployment/aliyun-deploy.sh" -ForegroundColor White
Write-Host ""
Write-Host "🎉 准备就绪，可以开始部署了！" -ForegroundColor Green
