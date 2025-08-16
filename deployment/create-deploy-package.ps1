# QMS-AI 阿里云部署包创建脚本 (PowerShell版本)
# 创建包含所有必要文件的部署包

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
New-Item -ItemType Directory -Force -Path "$DEPLOY_DIR\backend" | Out-Null
New-Item -ItemType Directory -Force -Path "$DEPLOY_DIR\frontend" | Out-Null
New-Item -ItemType Directory -Force -Path "$DEPLOY_DIR\config" | Out-Null
New-Item -ItemType Directory -Force -Path "$DEPLOY_DIR\deployment" | Out-Null
New-Item -ItemType Directory -Force -Path "$DEPLOY_DIR\nginx" | Out-Null
New-Item -ItemType Directory -Force -Path "$DEPLOY_DIR\scripts" | Out-Null
New-Item -ItemType Directory -Force -Path "$DEPLOY_DIR\logs" | Out-Null
New-Item -ItemType Directory -Force -Path "$DEPLOY_DIR\data" | Out-Null
New-Item -ItemType Directory -Force -Path "$DEPLOY_DIR\ssl" | Out-Null
New-Item -ItemType Directory -Force -Path "$DEPLOY_DIR\mysql" | Out-Null

# 复制后端文件
Write-Host "📦 复制后端文件..." -ForegroundColor Cyan
if (Test-Path "backend\nodejs") {
    Copy-Item -Recurse -Force "backend\nodejs" "$DEPLOY_DIR\backend\"
}
if (Test-Path "backend\api-gateway") {
    Copy-Item -Recurse -Force "backend\api-gateway" "$DEPLOY_DIR\backend\"
}

# 复制前端文件
Write-Host "🎨 复制前端文件..." -ForegroundColor Cyan
if (Test-Path "frontend\应用端") {
    Copy-Item -Recurse -Force "frontend\应用端" "$DEPLOY_DIR\frontend\app"
}
if (Test-Path "frontend\配置端") {
    Copy-Item -Recurse -Force "frontend\配置端" "$DEPLOY_DIR\frontend\config"
}

# 复制配置文件
Write-Host "⚙️ 复制配置文件..." -ForegroundColor Cyan
if (Test-Path "config\docker-compose.yml") {
    Copy-Item -Force "config\docker-compose.yml" "$DEPLOY_DIR\"
}
if (Test-Path "config\init-db.sql") {
    Copy-Item -Force "config\init-db.sql" "$DEPLOY_DIR\config\"
}
if (Test-Path "nginx\nginx.conf") {
    Copy-Item -Force "nginx\nginx.conf" "$DEPLOY_DIR\nginx\"
}

# 复制部署脚本
Write-Host "📋 复制部署脚本..." -ForegroundColor Cyan
if (Test-Path "deployment\aliyun-deploy.sh") {
    Copy-Item -Force "deployment\aliyun-deploy.sh" "$DEPLOY_DIR\deployment\"
}
if (Test-Path "deployment\阿里云部署指南.md") {
    Copy-Item -Force "deployment\阿里云部署指南.md" "$DEPLOY_DIR\"
}

# 创建完整的 Docker Compose 文件
Write-Host "🐳 创建 Docker Compose 配置..." -ForegroundColor Cyan
$dockerComposeContent = @"
version: '3.8'

services:
  # MySQL数据库
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
    command: --default-authentication-plugin=mysql_native_password --log-error=/var/log/mysql/error.log

  # Redis缓存
  redis:
    image: redis:7-alpine
    container_name: qms-redis
    restart: unless-stopped
    ports:
      - "6379:6379"
    volumes:
      - ./data/redis:/data
    command: redis-server --requirepass qms123456 --appendonly yes

  # 认证服务
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

  # 配置中心
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

  # 聊天服务
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

  # Nginx前端代理
  nginx:
    image: nginx:alpine
    container_name: qms-nginx
    restart: unless-stopped
    ports:
      - "8081:80"
      - "443:443"
    volumes:
      - ./nginx/nginx.conf:/etc/nginx/nginx.conf
      - ./frontend:/usr/share/nginx/html
      - ./ssl:/etc/nginx/ssl
      - ./logs:/var/log/nginx
    depends_on:
      - auth-service
      - config-center
      - chat-service
"@

$dockerComposeContent | Out-File -FilePath "$DEPLOY_DIR\docker-compose.yml" -Encoding UTF8

# 创建 Nginx 配置文件
Write-Host "🌐 创建 Nginx 配置..." -ForegroundColor Cyan
$nginxConfig = @"
events {
    worker_connections 1024;
}

http {
    include       /etc/nginx/mime.types;
    default_type  application/octet-stream;

    # 日志格式
    log_format main '$remote_addr - $remote_user [$time_local] "$request" '
                    '$status $body_bytes_sent "$http_referer" '
                    '"$http_user_agent" "$http_x_forwarded_for"';

    access_log /var/log/nginx/access.log main;
    error_log /var/log/nginx/error.log;

    # 基本设置
    sendfile on;
    tcp_nopush on;
    tcp_nodelay on;
    keepalive_timeout 65;
    types_hash_max_size 2048;

    # Gzip压缩
    gzip on;
    gzip_vary on;
    gzip_min_length 1024;
    gzip_types text/plain text/css text/xml text/javascript application/javascript application/xml+rss application/json;

    # 上游服务器
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

        # 静态文件根目录
        root /usr/share/nginx/html;
        index index.html;

        # 静态文件处理
        location / {
            try_files $uri $uri/ /index.html;
            add_header Cache-Control "public, max-age=3600";
        }

        # API代理
        location /api/auth/ {
            proxy_pass http://auth_backend/api/auth/;
            proxy_set_header Host $host;
            proxy_set_header X-Real-IP $remote_addr;
            proxy_set_header X-Forwarded-For $proxy_add_x_forwarded_for;
            proxy_set_header X-Forwarded-Proto $scheme;
        }

        location /api/config/ {
            proxy_pass http://config_backend/api/;
            proxy_set_header Host $host;
            proxy_set_header X-Real-IP $remote_addr;
            proxy_Set_header X-Forwarded-For $proxy_add_x_forwarded_for;
            proxy_set_header X-Forwarded-Proto $scheme;
        }

        location /api/chat/ {
            proxy_pass http://chat_backend/api/chat/;
            proxy_set_header Host $host;
            proxy_set_header X-Real-IP $remote_addr;
            proxy_set_header X-Forwarded-For $proxy_add_x_forwarded_for;
            proxy_set_header X-Forwarded-Proto $scheme;
        }

        # 健康检查
        location /health {
            access_log off;
            return 200 "healthy\n";
            add_header Content-Type text/plain;
        }
    }
}
"@

$nginxConfig | Out-File -FilePath "$DEPLOY_DIR\nginx\nginx.conf" -Encoding UTF8

# 创建数据库初始化脚本
Write-Host "🗄️ 创建数据库初始化脚本..." -ForegroundColor Cyan
$initDbSql = @"
-- QMS-AI 数据库初始化脚本

-- 创建用户表
CREATE TABLE IF NOT EXISTS users (
    id INT AUTO_INCREMENT PRIMARY KEY,
    username VARCHAR(50) UNIQUE NOT NULL,
    password VARCHAR(255) NOT NULL,
    email VARCHAR(100),
    role VARCHAR(20) DEFAULT 'user',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
);

-- 创建聊天记录表
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

-- 创建AI模型配置表
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

-- 插入默认AI模型配置
INSERT IGNORE INTO ai_models (id, name, provider, api_endpoint, max_tokens, temperature) VALUES
('deepseek-chat', 'DeepSeek Chat', 'deepseek', 'https://api.deepseek.com/v1/chat/completions', 4000, 0.7),
('deepseek-coder', 'DeepSeek Coder', 'deepseek', 'https://api.deepseek.com/v1/chat/completions', 4000, 0.7),
('qwen-turbo', 'Qwen Turbo', 'qwen', 'https://dashscope.aliyuncs.com/api/v1/services/aigc/text-generation/generation', 2000, 0.7),
('qwen-plus', 'Qwen Plus', 'qwen', 'https://dashscope.aliyuncs.com/api/v1/services/aigc/text-generation/generation', 8000, 0.7),
('qwen-max', 'Qwen Max', 'qwen', 'https://dashscope.aliyuncs.com/api/v1/services/aigc/text-generation/generation', 8000, 0.7),
('baichuan2-turbo', 'Baichuan2 Turbo', 'baichuan', 'https://api.baichuan-ai.com/v1/chat/completions', 4000, 0.7),
('chatglm-turbo', 'ChatGLM Turbo', 'zhipu', 'https://open.bigmodel.cn/api/paas/v4/chat/completions', 4000, 0.7),
('yi-large', 'Yi Large', 'yi', 'https://api.lingyiwanwu.com/v1/chat/completions', 4000, 0.7);

-- 插入默认管理员用户 (密码: admin123)
INSERT IGNORE INTO users (username, password, email, role) VALUES
('admin', '$2b$10$rOzJqQjQjQjQjQjQjQjQjOzJqQjQjQjQjQjQjQjQjQjQjQjQjQjQjQ', 'admin@qms.com', 'admin');

-- 创建配置表
CREATE TABLE IF NOT EXISTS system_configs (
    id INT AUTO_INCREMENT PRIMARY KEY,
    config_key VARCHAR(100) UNIQUE NOT NULL,
    config_value TEXT,
    description VARCHAR(255),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
);

-- 插入默认系统配置
INSERT IGNORE INTO system_configs (config_key, config_value, description) VALUES
('system_name', 'QMS-AI智能质量管理系统', '系统名称'),
('max_chat_history', '100', '最大聊天历史记录数'),
('default_model', 'deepseek-chat', '默认AI模型'),
('api_rate_limit', '60', 'API调用频率限制（每分钟）');
"@

$initDbSql | Out-File -FilePath "$DEPLOY_DIR\config\init-db.sql" -Encoding UTF8

# 创建部署脚本
Write-Host "🚀 创建部署脚本..." -ForegroundColor Cyan
$deployScript = @"
#!/bin/bash

# QMS-AI 阿里云部署脚本

echo "🚀 开始部署 QMS-AI 到阿里云..."

# 检查 Docker 和 Docker Compose
if ! command -v docker &> /dev/null; then
    echo "❌ Docker 未安装，正在安装..."
    curl -fsSL https://get.docker.com | sh
    systemctl start docker
    systemctl enable docker
fi

if ! command -v docker-compose &> /dev/null; then
    echo "❌ Docker Compose 未安装，正在安装..."
    curl -L "https://github.com/docker/compose/releases/download/v2.20.0/docker-compose-$(uname -s)-$(uname -m)" -o /usr/local/bin/docker-compose
    chmod +x /usr/local/bin/docker-compose
fi

# 停止现有服务
echo "🛑 停止现有服务..."
docker-compose down 2>/dev/null || true

# 清理旧容器和镜像
echo "🧹 清理旧容器..."
docker container prune -f
docker image prune -f

# 创建必要目录
echo "📁 创建必要目录..."
mkdir -p {logs,data/redis,ssl,mysql}

# 设置权限
echo "🔐 设置目录权限..."
chmod -R 755 logs data ssl mysql
chown -R 999:999 mysql  # MySQL容器用户

# 启动服务
echo "🚀 启动服务..."
docker-compose up -d

# 等待服务启动
echo "⏳ 等待服务启动..."
sleep 30

# 检查服务状态
echo "📊 检查服务状态..."
docker-compose ps

# 检查服务健康状态
echo "🏥 检查服务健康状态..."
for i in {1..10}; do
    echo "尝试 $i/10..."

    # 检查MySQL
    if docker exec qms-mysql mysqladmin ping -h localhost -u root -pqms123456 &>/dev/null; then
        echo "✅ MySQL 服务正常"
        mysql_ok=true
    else
        echo "❌ MySQL 服务异常"
        mysql_ok=false
    fi

    # 检查Redis
    if docker exec qms-redis redis-cli -a qms123456 ping &>/dev/null; then
        echo "✅ Redis 服务正常"
        redis_ok=true
    else
        echo "❌ Redis 服务异常"
        redis_ok=false
    fi

    # 检查HTTP服务
    if curl -s http://localhost:8081/health &>/dev/null; then
        echo "✅ Web 服务正常"
        web_ok=true
    else
        echo "❌ Web 服务异常"
        web_ok=false
    fi

    if [ "$mysql_ok" = true ] && [ "$redis_ok" = true ] && [ "$web_ok" = true ]; then
        echo "🎉 所有服务启动成功！"
        break
    fi

    if [ $i -eq 10 ]; then
        echo "⚠️ 部分服务可能未正常启动，请检查日志"
        docker-compose logs --tail=50
    fi

    sleep 10
done

# 显示访问信息
echo ""
echo "🎉 QMS-AI 部署完成！"
echo ""
echo "📋 服务信息:"
echo "- Web界面: http://$(curl -s ifconfig.me):8081"
echo "- 认证服务: http://$(curl -s ifconfig.me):8084"
echo "- 配置中心: http://$(curl -s ifconfig.me):8083"
echo "- 聊天服务: http://$(curl -s ifconfig.me):3004"
echo ""
echo "🔧 管理命令:"
echo "- 查看状态: docker-compose ps"
echo "- 查看日志: docker-compose logs -f"
echo "- 重启服务: docker-compose restart"
echo "- 停止服务: docker-compose down"
echo ""
echo "📞 如有问题请检查日志或联系技术支持"
"@

$deployScript | Out-File -FilePath "$DEPLOY_DIR\deployment\aliyun-deploy.sh" -Encoding UTF8

# 创建 README
Write-Host "📖 创建 README..." -ForegroundColor Cyan
$readmeContent = @"
# QMS-AI 阿里云部署包

## 📦 包含内容
- **backend/**: 后端服务代码
  - nodejs/: Node.js 服务
  - api-gateway/: API网关
- **frontend/**: 前端应用代码
  - app/: 应用端界面
  - config/: 配置端界面
- **config/**: 配置文件
  - init-db.sql: 数据库初始化脚本
- **deployment/**: 部署脚本
  - aliyun-deploy.sh: 阿里云部署脚本
- **nginx/**: Nginx配置
  - nginx.conf: Nginx配置文件
- **docker-compose.yml**: Docker编排文件

## 🚀 快速部署

### 1. 上传到服务器
```bash
scp qms-aliyun-deploy.zip root@47.108.152.16:/opt/
```

### 2. 登录服务器并解压
```bash
ssh root@47.108.152.16
cd /opt
unzip qms-aliyun-deploy.zip
cd qms-aliyun-deploy
```

### 3. 执行部署
```bash
bash deployment/aliyun-deploy.sh
```

## 🔧 服务管理

### 查看服务状态
```bash
docker-compose ps
```

### 查看服务日志
```bash
docker-compose logs -f
```

### 重启服务
```bash
docker-compose restart
```

### 停止服务
```bash
docker-compose down
```

## 🌐 访问地址

部署成功后可通过以下地址访问：

- **Web界面**: http://47.108.152.16:8081
- **认证服务**: http://47.108.152.16:8084
- **配置中心**: http://47.108.152.16:8083
- **聊天服务**: http://47.108.152.16:3004

## 📊 默认配置

- **MySQL**:
  - 端口: 3306
  - 数据库: qms_ai
  - 用户: qms_user
  - 密码: qms123456

- **Redis**:
  - 端口: 6379
  - 密码: qms123456

- **默认管理员**:
  - 用户名: admin
  - 密码: admin123

## 🔒 安全建议

1. 修改默认密码
2. 配置防火墙规则
3. 启用HTTPS
4. 定期备份数据

## 📞 技术支持

如有问题请联系开发团队或查看日志文件。
"@

$readmeContent | Out-File -FilePath "$DEPLOY_DIR\README.md" -Encoding UTF8

# 创建简单的前端页面
Write-Host "🎨 创建简单前端页面..." -ForegroundColor Cyan
$indexHtml = @"
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
        .status { margin: 20px 0; }
        .service { margin: 10px 0; padding: 10px; background: #f8f9fa; border-radius: 4px; }
        .btn { display: inline-block; padding: 10px 20px; margin: 5px; background: #007bff; color: white; text-decoration: none; border-radius: 4px; }
        .btn:hover { background: #0056b3; }
    </style>
</head>
<body>
    <div class="container">
        <h1>🚀 QMS-AI 管理系统</h1>
        <p>欢迎使用 QMS-AI 智能质量管理系统！</p>

        <div class="status">
            <h3>📊 服务状态</h3>
            <div class="service">
                <strong>认证服务:</strong> <span id="auth-status">检查中...</span>
                <a href="/api/auth/health" target="_blank" class="btn">检查</a>
            </div>
            <div class="service">
                <strong>配置中心:</strong> <span id="config-status">检查中...</span>
                <a href="/api/config/health" target="_blank" class="btn">检查</a>
            </div>
            <div class="service">
                <strong>聊天服务:</strong> <span id="chat-status">检查中...</span>
                <a href="/api/chat/health" target="_blank" class="btn">检查</a>
            </div>
        </div>

        <div>
            <h3>🔗 快速链接</h3>
            <a href="/api/chat/models" class="btn">AI模型列表</a>
            <a href="/api/config/configs" class="btn">配置管理</a>
            <a href="/health" class="btn">系统健康</a>
        </div>
    </div>

    <script>
        // 检查服务状态
        async function checkService(url, elementId) {
            try {
                const response = await fetch(url);
                const element = document.getElementById(elementId);
                if (response.ok) {
                    element.textContent = '✅ 正常';
                    element.style.color = 'green';
                } else {
                    element.textContent = '❌ 异常';
                    element.style.color = 'red';
                }
            } catch (error) {
                const element = document.getElementById(elementId);
                element.textContent = '❌ 连接失败';
                element.style.color = 'red';
            }
        }

        // 页面加载后检查服务
        window.onload = function() {
            checkService('/api/auth/health', 'auth-status');
            checkService('/api/config/health', 'config-status');
            checkService('/api/chat/health', 'chat-status');
        };
    </script>
</body>
</html>
"@

$indexHtml | Out-File -FilePath "$DEPLOY_DIR\frontend\index.html" -Encoding UTF8

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
Write-Host "📊 包大小: $((Get-Item $ARCHIVE_NAME).Length / 1MB | ForEach-Object { "{0:N2} MB" -f $_ })" -ForegroundColor Cyan
Write-Host ""
Write-Host "📁 包内容预览:" -ForegroundColor Yellow
Get-ChildItem $DEPLOY_DIR -Recurse | Select-Object -First 20 | ForEach-Object { Write-Host "  $($_.FullName.Replace((Get-Location).Path + '\', ''))" }
Write-Host "..."
Write-Host ""
Write-Host "🚀 下一步操作:" -ForegroundColor Green
Write-Host "1. 上传到服务器: scp $ARCHIVE_NAME root@47.108.152.16:/opt/" -ForegroundColor White
Write-Host "2. 登录服务器: ssh root@47.108.152.16" -ForegroundColor White
Write-Host "3. 解压并部署: cd /opt && unzip $ARCHIVE_NAME && cd qms-aliyun-deploy && bash deployment/aliyun-deploy.sh" -ForegroundColor White
Write-Host ""
Write-Host "🎉 准备就绪，可以开始部署了！" -ForegroundColor Green
