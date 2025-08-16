# QMS-AI 最新版本部署包生成器 v2.0
# 包含所有最新功能和优化

Write-Host "🚀 创建QMS-AI最新版本部署包..." -ForegroundColor Green
Write-Host "版本: v2.0 - 包含全面服务管理和AI优化" -ForegroundColor Cyan

$DEPLOY_DIR = "qms-aliyun-deploy-v2"
$ARCHIVE_NAME = "qms-aliyun-deploy-v2.zip"
$TIMESTAMP = Get-Date -Format "yyyy-MM-dd-HH-mm"

# 清理旧目录
if (Test-Path $DEPLOY_DIR) {
    Write-Host "清理旧部署目录..." -ForegroundColor Yellow
    Remove-Item -Recurse -Force $DEPLOY_DIR
}

# 创建目录结构
Write-Host "创建目录结构..." -ForegroundColor Cyan
$directories = @(
    "$DEPLOY_DIR\backend\nodejs",
    "$DEPLOY_DIR\frontend\app",
    "$DEPLOY_DIR\frontend\config",
    "$DEPLOY_DIR\config",
    "$DEPLOY_DIR\deployment",
    "$DEPLOY_DIR\nginx",
    "$DEPLOY_DIR\logs",
    "$DEPLOY_DIR\data",
    "$DEPLOY_DIR\ssl",
    "$DEPLOY_DIR\mysql",
    "$DEPLOY_DIR\scripts",
    "$DEPLOY_DIR\tools",
    "$DEPLOY_DIR\docs"
)

foreach ($dir in $directories) {
    New-Item -ItemType Directory -Force -Path $dir | Out-Null
}

# 复制后端文件
Write-Host "复制后端文件..." -ForegroundColor Cyan
if (Test-Path "backend\nodejs") {
    Copy-Item -Recurse -Force "backend\nodejs\*" "$DEPLOY_DIR\backend\nodejs\"
    Write-Host "✅ 后端文件复制完成" -ForegroundColor Green
} else {
    Write-Host "❌ 后端目录不存在" -ForegroundColor Red
}

# 复制前端文件
Write-Host "复制前端文件..." -ForegroundColor Cyan
if (Test-Path "frontend\应用端") {
    Copy-Item -Recurse -Force "frontend\应用端\*" "$DEPLOY_DIR\frontend\app\"
    Write-Host "✅ 应用端文件复制完成" -ForegroundColor Green
} else {
    Write-Host "❌ 应用端目录不存在" -ForegroundColor Red
}

if (Test-Path "frontend\配置端") {
    Copy-Item -Recurse -Force "frontend\配置端\*" "$DEPLOY_DIR\frontend\config\"
    Write-Host "✅ 配置端文件复制完成" -ForegroundColor Green
} else {
    Write-Host "❌ 配置端目录不存在" -ForegroundColor Red
}
    Copy-Item -Recurse -Force "frontend\应用端\*" "$DEPLOY_DIR\frontend\应用端\"
    Write-Host "✅ 应用端文件复制完成" -ForegroundColor Green
}
if (Test-Path "frontend\配置端") {
    Copy-Item -Recurse -Force "frontend\配置端\*" "$DEPLOY_DIR\frontend\配置端\"
    Write-Host "✅ 配置端文件复制完成" -ForegroundColor Green
}

# 复制部署配置文件
Write-Host "复制部署配置..." -ForegroundColor Cyan
if (Test-Path "deployment") {
    Copy-Item -Recurse -Force "deployment\*" "$DEPLOY_DIR\deployment\"
    Write-Host "✅ 部署配置复制完成" -ForegroundColor Green
}

# 复制服务管理脚本
Write-Host "复制服务管理脚本..." -ForegroundColor Cyan
$managementFiles = @(
    "QMS-Service-Manager.bat",
    "QMS-Quick-Check-And-Start.bat",
    "QMS-Status-Check.bat",
    "QMS-Final-Status-Report.md"
)

foreach ($file in $managementFiles) {
    if (Test-Path $file) {
        Copy-Item -Force $file "$DEPLOY_DIR\tools\"
        Write-Host "✅ 复制 $file" -ForegroundColor Green
    }
}

# Create docker-compose.yml
Write-Host "Creating docker-compose.yml..." -ForegroundColor Cyan
$dockerCompose = @"
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
"@

$dockerCompose | Out-File -FilePath "$DEPLOY_DIR\docker-compose.yml" -Encoding UTF8

# Create nginx.conf
Write-Host "Creating nginx.conf..." -ForegroundColor Cyan
$nginxConf = @"
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
"@

$nginxConf | Out-File -FilePath "$DEPLOY_DIR\nginx\nginx.conf" -Encoding UTF8

# Create init-db.sql
Write-Host "Creating init-db.sql..." -ForegroundColor Cyan
$initDb = @"
-- QMS-AI Database Initialization

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
"@

$initDb | Out-File -FilePath "$DEPLOY_DIR\config\init-db.sql" -Encoding UTF8

# Create deploy script
Write-Host "Creating deploy script..." -ForegroundColor Cyan
$deployScript = @"
#!/bin/bash

echo "Starting QMS-AI deployment..."

# Install Docker if not exists
if ! command -v docker &> /dev/null; then
    echo "Installing Docker..."
    curl -fsSL https://get.docker.com | sh
    systemctl start docker
    systemctl enable docker
fi

if ! command -v docker-compose &> /dev/null; then
    echo "Installing Docker Compose..."
    curl -L "https://github.com/docker/compose/releases/download/v2.20.0/docker-compose-`$(uname -s)-`$(uname -m)" -o /usr/local/bin/docker-compose
    chmod +x /usr/local/bin/docker-compose
fi

# Stop existing services
echo "Stopping existing services..."
docker-compose down 2>/dev/null || true

# Create directories
echo "Creating directories..."
mkdir -p {logs,data/redis,ssl,mysql}
chmod -R 755 logs data ssl mysql

# Start services
echo "Starting services..."
docker-compose up -d

# Wait for services
echo "Waiting for services to start..."
sleep 30

# Check status
echo "Checking service status..."
docker-compose ps

echo ""
echo "QMS-AI deployment completed!"
echo "Access URL: http://`$(curl -s ifconfig.me):8081"
echo ""
echo "Management commands:"
echo "- Check status: docker-compose ps"
echo "- View logs: docker-compose logs -f"
echo "- Restart: docker-compose restart"
echo "- Stop: docker-compose down"
"@

$deployScript | Out-File -FilePath "$DEPLOY_DIR\deployment\aliyun-deploy.sh" -Encoding UTF8

# Create index.html
Write-Host "Creating index.html..." -ForegroundColor Cyan
$indexHtml = @"
<!DOCTYPE html>
<html lang="zh-CN">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>QMS-AI Management System</title>
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
        <h1>QMS-AI Management System</h1>
        <p>Welcome to QMS-AI Intelligent Quality Management System!</p>

        <div>
            <h3>Service Status</h3>
            <div class="service">Auth Service: <a href="/api/auth/health" class="btn">Check</a></div>
            <div class="service">Config Center: <a href="/api/config/health" class="btn">Check</a></div>
            <div class="service">Chat Service: <a href="/api/chat/health" class="btn">Check</a></div>
        </div>

        <div>
            <h3>Quick Links</h3>
            <a href="/api/chat/models" class="btn">AI Models</a>
            <a href="/api/config/configs" class="btn">Configuration</a>
            <a href="/health" class="btn">System Health</a>
        </div>
    </div>
</body>
</html>
"@

$indexHtml | Out-File -FilePath "$DEPLOY_DIR\frontend\index.html" -Encoding UTF8

# Create README
Write-Host "Creating README..." -ForegroundColor Cyan
$readme = @"
# QMS-AI Aliyun Deployment Package

## Quick Deployment

1. Upload to server: scp qms-aliyun-deploy.zip root@47.108.152.16:/opt/
2. Login to server: ssh root@47.108.152.16
3. Extract: cd /opt && unzip qms-aliyun-deploy.zip && cd qms-aliyun-deploy
4. Deploy: bash deployment/aliyun-deploy.sh

## Access URLs

- Web Interface: http://47.108.152.16:8081
- Auth Service: http://47.108.152.16:8084
- Config Center: http://47.108.152.16:8083
- Chat Service: http://47.108.152.16:3004

## Default Configuration

- MySQL: qms_ai/qms_user/qms123456
- Redis: qms123456
- Admin: admin/admin123
"@

$readme | Out-File -FilePath "$DEPLOY_DIR\README.md" -Encoding UTF8

# Create archive
Write-Host "Creating archive..." -ForegroundColor Yellow
if (Test-Path $ARCHIVE_NAME) {
    Remove-Item $ARCHIVE_NAME -Force
}

Compress-Archive -Path $DEPLOY_DIR -DestinationPath $ARCHIVE_NAME -Force

# Show results
Write-Host ""
Write-Host "Deployment package created successfully!" -ForegroundColor Green
Write-Host "File: $ARCHIVE_NAME" -ForegroundColor Cyan
$fileSize = (Get-Item $ARCHIVE_NAME).Length / 1MB
Write-Host "Size: $($fileSize.ToString('F2')) MB" -ForegroundColor Cyan
Write-Host ""
Write-Host "Next steps:" -ForegroundColor Green
Write-Host "1. Upload: scp $ARCHIVE_NAME root@47.108.152.16:/opt/" -ForegroundColor White
Write-Host "2. Login: ssh root@47.108.152.16" -ForegroundColor White
Write-Host "3. Deploy: cd /opt && unzip $ARCHIVE_NAME && cd qms-aliyun-deploy && bash deployment/aliyun-deploy.sh" -ForegroundColor White
Write-Host ""
Write-Host "Ready to deploy!" -ForegroundColor Green
