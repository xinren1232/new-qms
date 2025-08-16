#!/bin/bash

echo "🚀 QMS智能管理系统 - 阿里云部署脚本"
echo "======================================="

# 检查是否为root用户
if [ "$EUID" -ne 0 ]; then
    echo "❌ 请使用root用户运行此脚本"
    echo "💡 使用: sudo ./aliyun-setup.sh"
    exit 1
fi

# 系统信息
echo "📋 系统信息检查..."
echo "操作系统: $(cat /etc/os-release | grep PRETTY_NAME | cut -d'"' -f2)"
echo "内核版本: $(uname -r)"
echo "CPU核心: $(nproc)"
echo "内存大小: $(free -h | grep Mem | awk '{print $2}')"
echo "磁盘空间: $(df -h / | tail -1 | awk '{print $4}')"

# 更新系统
echo "🔄 更新系统包..."
if command -v yum &> /dev/null; then
    # CentOS/RHEL
    yum update -y
    yum install -y wget curl git vim htop
elif command -v apt &> /dev/null; then
    # Ubuntu/Debian
    apt update && apt upgrade -y
    apt install -y wget curl git vim htop
fi

# 安装Docker
echo "🐳 安装Docker..."
if ! command -v docker &> /dev/null; then
    # 使用阿里云Docker镜像源
    curl -fsSL https://mirrors.aliyun.com/docker-ce/linux/centos/docker-ce.repo -o /etc/yum.repos.d/docker-ce.repo
    yum install -y docker-ce docker-ce-cli containerd.io
    
    # 启动Docker服务
    systemctl start docker
    systemctl enable docker
    
    # 配置Docker镜像加速器（阿里云）
    mkdir -p /etc/docker
    cat > /etc/docker/daemon.json << 'EOF'
{
  "registry-mirrors": [
    "https://mirror.ccs.tencentyun.com",
    "https://docker.mirrors.ustc.edu.cn",
    "https://registry.docker-cn.com"
  ],
  "log-driver": "json-file",
  "log-opts": {
    "max-size": "100m",
    "max-file": "3"
  },
  "storage-driver": "overlay2"
}
EOF
    
    systemctl restart docker
    echo "✅ Docker安装完成"
else
    echo "✅ Docker已安装"
fi

# 安装Docker Compose
echo "🔧 安装Docker Compose..."
if ! command -v docker-compose &> /dev/null; then
    # 下载最新版本的Docker Compose
    DOCKER_COMPOSE_VERSION=$(curl -s https://api.github.com/repos/docker/compose/releases/latest | grep 'tag_name' | cut -d'"' -f4)
    curl -L "https://github.com/docker/compose/releases/download/${DOCKER_COMPOSE_VERSION}/docker-compose-$(uname -s)-$(uname -m)" -o /usr/local/bin/docker-compose
    chmod +x /usr/local/bin/docker-compose
    ln -sf /usr/local/bin/docker-compose /usr/bin/docker-compose
    echo "✅ Docker Compose安装完成"
else
    echo "✅ Docker Compose已安装"
fi

# 安装Node.js
echo "📦 安装Node.js..."
if ! command -v node &> /dev/null; then
    # 使用NodeSource仓库安装Node.js 18
    curl -fsSL https://rpm.nodesource.com/setup_18.x | bash -
    yum install -y nodejs
    echo "✅ Node.js安装完成"
else
    echo "✅ Node.js已安装"
fi

# 安装Nginx
echo "🌐 安装Nginx..."
if ! command -v nginx &> /dev/null; then
    yum install -y nginx
    systemctl start nginx
    systemctl enable nginx
    echo "✅ Nginx安装完成"
else
    echo "✅ Nginx已安装"
fi

# 配置防火墙
echo "🔥 配置防火墙..."
if command -v firewall-cmd &> /dev/null; then
    # CentOS/RHEL防火墙
    systemctl start firewalld
    systemctl enable firewalld
    
    # 开放必要端口
    firewall-cmd --permanent --add-port=80/tcp      # HTTP
    firewall-cmd --permanent --add-port=443/tcp     # HTTPS
    firewall-cmd --permanent --add-port=3000/tcp    # Grafana
    firewall-cmd --permanent --add-port=3004/tcp    # 聊天服务
    firewall-cmd --permanent --add-port=8081/tcp    # 前端应用
    firewall-cmd --permanent --add-port=8083/tcp    # 配置中心
    firewall-cmd --permanent --add-port=8084/tcp    # 认证服务
    firewall-cmd --permanent --add-port=22/tcp      # SSH
    
    firewall-cmd --reload
    echo "✅ 防火墙配置完成"
elif command -v ufw &> /dev/null; then
    # Ubuntu防火墙
    ufw --force enable
    ufw allow 80/tcp
    ufw allow 443/tcp
    ufw allow 8000/tcp
    ufw allow 8500/tcp
    ufw allow 9000/tcp
    ufw allow 3000/tcp
    ufw allow 22/tcp
    echo "✅ 防火墙配置完成"
fi

# 创建项目目录
echo "📁 创建项目目录..."
PROJECT_DIR="/opt/qms"
mkdir -p $PROJECT_DIR
cd $PROJECT_DIR

# 创建必要的子目录
mkdir -p {logs,data,config,ssl,backup}
chmod 755 logs data config ssl backup

# 设置时区
echo "🕐 设置时区..."
timedatectl set-timezone Asia/Shanghai

# 创建部署用户
echo "👤 创建部署用户..."
if ! id "qms" &>/dev/null; then
    useradd -m -s /bin/bash qms
    usermod -aG docker qms
    echo "✅ 用户qms创建完成"
fi

# 生成SSL证书目录（后续配置Let's Encrypt）
echo "🔒 准备SSL证书目录..."
mkdir -p /etc/letsencrypt/live
mkdir -p $PROJECT_DIR/ssl

# 创建环境变量模板
echo "📝 创建环境变量模板..."
cat > $PROJECT_DIR/.env.template << 'EOF'
# QMS智能管理系统 - 生产环境配置

# 基础配置
NODE_ENV=production
PROJECT_NAME=qms
DOMAIN=your-domain.com

# 数据库配置
DB_HOST=postgres
DB_PORT=5432
DB_NAME=qms
DB_USER=qms
DB_PASSWORD=your_secure_password_here

# Redis配置
REDIS_HOST=redis
REDIS_PORT=6379
REDIS_PASSWORD=your_redis_password_here

# 飞书配置
FEISHU_APP_ID=cli_your_app_id
FEISHU_APP_SECRET=your_app_secret
FEISHU_REDIRECT_URI=https://your-domain.com/auth/feishu/callback

# GitHub配置（可选）
GITHUB_CLIENT_ID=your_github_client_id
GITHUB_CLIENT_SECRET=your_github_client_secret

# JWT配置
JWT_SECRET=your_jwt_secret_here
ENCRYPTION_KEY=your_encryption_key_here

# MinIO配置
MINIO_ROOT_USER=minioadmin
MINIO_ROOT_PASSWORD=your_minio_password_here

# Grafana配置
GRAFANA_USER=admin
GRAFANA_PASSWORD=your_grafana_password_here

# 邮件配置（可选）
SMTP_HOST=smtp.aliyun.com
SMTP_PORT=465
SMTP_USER=your_email@aliyun.com
SMTP_PASSWORD=your_email_password

# 阿里云配置（可选）
ALIYUN_ACCESS_KEY_ID=your_access_key_id
ALIYUN_ACCESS_KEY_SECRET=your_access_key_secret
ALIYUN_REGION=cn-hangzhou
EOF

# 创建Nginx配置
echo "🌐 创建Nginx配置..."
cat > /etc/nginx/conf.d/qms.conf << 'EOF'
# QMS智能管理系统 - Nginx配置

upstream qms_backend {
    server 127.0.0.1:8080;
    keepalive 32;
}

upstream qms_auth {
    server 127.0.0.1:8000;
    keepalive 16;
}

# HTTP重定向到HTTPS
server {
    listen 80;
    server_name your-domain.com;
    return 301 https://$server_name$request_uri;
}

# HTTPS主站
server {
    listen 443 ssl http2;
    server_name your-domain.com;
    
    # SSL证书配置
    ssl_certificate /etc/letsencrypt/live/your-domain.com/fullchain.pem;
    ssl_certificate_key /etc/letsencrypt/live/your-domain.com/privkey.pem;
    
    # SSL安全配置
    ssl_protocols TLSv1.2 TLSv1.3;
    ssl_ciphers ECDHE-RSA-AES128-GCM-SHA256:ECDHE-RSA-AES256-GCM-SHA384;
    ssl_prefer_server_ciphers off;
    ssl_session_cache shared:SSL:10m;
    ssl_session_timeout 10m;
    
    # 安全头
    add_header X-Frame-Options DENY;
    add_header X-Content-Type-Options nosniff;
    add_header X-XSS-Protection "1; mode=block";
    add_header Strict-Transport-Security "max-age=31536000; includeSubDomains" always;
    
    # 日志配置
    access_log /var/log/nginx/qms_access.log;
    error_log /var/log/nginx/qms_error.log;
    
    # 主应用
    location / {
        proxy_pass http://qms_backend;
        proxy_http_version 1.1;
        proxy_set_header Upgrade $http_upgrade;
        proxy_set_header Connection 'upgrade';
        proxy_set_header Host $host;
        proxy_set_header X-Real-IP $remote_addr;
        proxy_set_header X-Forwarded-For $proxy_add_x_forwarded_for;
        proxy_set_header X-Forwarded-Proto $scheme;
        proxy_cache_bypass $http_upgrade;
        proxy_read_timeout 300s;
        proxy_connect_timeout 75s;
    }
    
    # 认证服务
    location /auth/ {
        proxy_pass http://qms_auth/;
        proxy_http_version 1.1;
        proxy_set_header Host $host;
        proxy_set_header X-Real-IP $remote_addr;
        proxy_set_header X-Forwarded-For $proxy_add_x_forwarded_for;
        proxy_set_header X-Forwarded-Proto $scheme;
    }
    
    # 静态文件缓存
    location ~* \.(js|css|png|jpg|jpeg|gif|ico|svg|woff|woff2|ttf|eot)$ {
        expires 1y;
        add_header Cache-Control "public, immutable";
        proxy_pass http://qms_backend;
    }
    
    # API接口
    location /api/ {
        proxy_pass http://qms_backend;
        proxy_http_version 1.1;
        proxy_set_header Host $host;
        proxy_set_header X-Real-IP $remote_addr;
        proxy_set_header X-Forwarded-For $proxy_add_x_forwarded_for;
        proxy_set_header X-Forwarded-Proto $scheme;
        proxy_read_timeout 300s;
    }
}
EOF

# 创建部署脚本
echo "📜 创建部署脚本..."
cat > $PROJECT_DIR/deploy.sh << 'EOF'
#!/bin/bash

echo "🚀 QMS系统部署脚本"
echo "=================="

# 检查环境变量文件
if [ ! -f .env ]; then
    echo "❌ 未找到.env文件，请先配置环境变量"
    echo "💡 复制.env.template为.env并填写配置"
    exit 1
fi

# 拉取最新代码
echo "📥 拉取最新代码..."
git pull origin main

# 构建和启动服务
echo "🔨 构建和启动服务..."
docker-compose -f docker-compose.prod.yml down
docker-compose -f docker-compose.prod.yml build --no-cache
docker-compose -f docker-compose.prod.yml up -d

# 等待服务启动
echo "⏳ 等待服务启动..."
sleep 30

# 检查服务状态
echo "🔍 检查服务状态..."
docker-compose -f docker-compose.prod.yml ps

# 重启Nginx
echo "🔄 重启Nginx..."
systemctl reload nginx

echo "✅ 部署完成！"
echo "🌐 访问地址: https://$(hostname -I | awk '{print $1}')"
EOF

chmod +x $PROJECT_DIR/deploy.sh

# 创建监控脚本
echo "📊 创建监控脚本..."
cat > $PROJECT_DIR/monitor.sh << 'EOF'
#!/bin/bash

echo "📊 QMS系统监控"
echo "=============="

echo "🐳 Docker容器状态:"
docker ps --format "table {{.Names}}\t{{.Status}}\t{{.Ports}}"

echo ""
echo "💾 系统资源使用:"
echo "CPU: $(top -bn1 | grep "Cpu(s)" | awk '{print $2}' | cut -d'%' -f1)%"
echo "内存: $(free | grep Mem | awk '{printf("%.1f%%", $3/$2 * 100.0)}')"
echo "磁盘: $(df -h / | tail -1 | awk '{print $5}')"

echo ""
echo "🌐 网络连接:"
netstat -tuln | grep -E ':(80|443|8000|8500|3000|5432|6379)'

echo ""
echo "📝 最近日志:"
tail -n 5 /var/log/nginx/qms_access.log
EOF

chmod +x $PROJECT_DIR/monitor.sh

echo ""
echo "🎉 阿里云环境配置完成！"
echo "======================================="
echo "📁 项目目录: $PROJECT_DIR"
echo "👤 部署用户: qms"
echo "🌐 Nginx配置: /etc/nginx/conf.d/qms.conf"
echo ""
echo "📋 下一步操作："
echo "1. 配置域名解析到服务器IP"
echo "2. 申请SSL证书: certbot --nginx -d your-domain.com"
echo "3. 复制项目代码到 $PROJECT_DIR"
echo "4. 配置环境变量: cp .env.template .env && vim .env"
echo "5. 运行部署脚本: ./deploy.sh"
echo ""
echo "🔧 常用命令："
echo "  查看服务状态: ./monitor.sh"
echo "  查看日志: docker-compose logs -f"
echo "  重启服务: docker-compose restart"
echo ""
echo "⚠️  安全提醒："
echo "  - 修改默认密码"
echo "  - 配置防火墙规则"
echo "  - 定期备份数据"
echo "  - 监控系统资源"
