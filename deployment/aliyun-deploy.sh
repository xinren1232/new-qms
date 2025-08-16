#!/bin/bash

# QMS-AI阿里云优化部署脚本
# 解决内存使用率过高问题，优化云端部署

set -e

# 颜色定义
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
PURPLE='\033[0;35m'
CYAN='\033[0;36m'
NC='\033[0m' # No Color

# 项目配置
PROJECT_NAME="qms-ai"
PROJECT_DIR="/opt/qms"
BACKUP_DIR="/opt/qms-backup"
LOG_FILE="/var/log/qms-deploy.log"

# 日志函数
log() {
    echo -e "${CYAN}[$(date +'%Y-%m-%d %H:%M:%S')]${NC} $1" | tee -a $LOG_FILE
}

error() {
    echo -e "${RED}[ERROR]${NC} $1" | tee -a $LOG_FILE
    exit 1
}

success() {
    echo -e "${GREEN}[SUCCESS]${NC} $1" | tee -a $LOG_FILE
}

warning() {
    echo -e "${YELLOW}[WARNING]${NC} $1" | tee -a $LOG_FILE
}

# 显示横幅
show_banner() {
    echo -e "${PURPLE}"
    echo "╔══════════════════════════════════════════════════════════════╗"
    echo "║                    QMS-AI阿里云部署工具                      ║"
    echo "║                   内存优化 + 云端适配版本                     ║"
    echo "╚══════════════════════════════════════════════════════════════╝"
    echo -e "${NC}"
}

# 检查系统要求
check_requirements() {
    log "🔍 检查系统要求..."
    
    # 检查是否为root用户
    if [ "$EUID" -ne 0 ]; then
        error "请使用root用户运行此脚本: sudo $0"
    fi
    
    # 检查内存
    TOTAL_MEM=$(free -m | awk 'NR==2{printf "%.0f", $2}')
    if [ $TOTAL_MEM -lt 1024 ]; then
        warning "系统内存少于1GB，建议升级到至少2GB"
    fi
    
    # 检查磁盘空间
    DISK_SPACE=$(df / | awk 'NR==2{print $4}')
    if [ $DISK_SPACE -lt 5242880 ]; then  # 5GB in KB
        warning "磁盘空间不足5GB，建议至少10GB可用空间"
    fi
    
    success "系统要求检查完成"
}

# 安装依赖
install_dependencies() {
    log "📦 安装系统依赖..."
    
    # 更新系统
    if command -v yum &> /dev/null; then
        yum update -y
        yum install -y wget curl git vim htop unzip
    elif command -v apt &> /dev/null; then
        apt update && apt upgrade -y
        apt install -y wget curl git vim htop unzip
    fi
    
    # 安装Docker
    if ! command -v docker &> /dev/null; then
        log "🐳 安装Docker..."
        curl -fsSL https://get.docker.com -o get-docker.sh
        sh get-docker.sh
        systemctl start docker
        systemctl enable docker
        
        # 配置Docker镜像加速器
        mkdir -p /etc/docker
        cat > /etc/docker/daemon.json << 'EOF'
{
  "registry-mirrors": [
    "https://mirror.ccs.tencentyun.com",
    "https://docker.mirrors.ustc.edu.cn"
  ],
  "log-driver": "json-file",
  "log-opts": {
    "max-size": "50m",
    "max-file": "3"
  },
  "storage-driver": "overlay2"
}
EOF
        systemctl restart docker
        success "Docker安装完成"
    else
        success "Docker已安装"
    fi
    
    # 安装Docker Compose
    if ! command -v docker-compose &> /dev/null; then
        log "🔧 安装Docker Compose..."
        DOCKER_COMPOSE_VERSION=$(curl -s https://api.github.com/repos/docker/compose/releases/latest | grep 'tag_name' | cut -d'"' -f4)
        curl -L "https://github.com/docker/compose/releases/download/${DOCKER_COMPOSE_VERSION}/docker-compose-$(uname -s)-$(uname -m)" -o /usr/local/bin/docker-compose
        chmod +x /usr/local/bin/docker-compose
        ln -sf /usr/local/bin/docker-compose /usr/bin/docker-compose
        success "Docker Compose安装完成"
    else
        success "Docker Compose已安装"
    fi
}

# 创建项目目录
setup_directories() {
    log "📁 创建项目目录..."
    
    # 创建主目录
    mkdir -p $PROJECT_DIR
    mkdir -p $BACKUP_DIR
    
    # 创建子目录
    cd $PROJECT_DIR
    mkdir -p {logs,data,config,ssl,backup,nginx/conf.d,prometheus,grafana/provisioning}
    
    # 设置权限
    chmod 755 logs data config ssl backup
    chown -R 1000:1000 logs data
    
    success "项目目录创建完成"
}

# 配置环境变量
setup_environment() {
    log "⚙️ 配置环境变量..."
    
    if [ ! -f "$PROJECT_DIR/.env" ]; then
        if [ -f "$PROJECT_DIR/deployment/.env.aliyun" ]; then
            cp "$PROJECT_DIR/deployment/.env.aliyun" "$PROJECT_DIR/.env"
            warning "请编辑 $PROJECT_DIR/.env 文件，配置您的阿里云参数"
            warning "特别注意配置: RDS数据库、Redis缓存、域名、API密钥等"
        else
            error "未找到环境变量模板文件"
        fi
    else
        success "环境变量文件已存在"
    fi
}

# 配置Nginx
setup_nginx() {
    log "🌐 配置Nginx..."
    
    cat > $PROJECT_DIR/nginx/conf.d/qms.conf << 'EOF'
# QMS-AI生产环境Nginx配置
upstream qms_auth {
    server qms-auth-service:8084;
    keepalive 16;
}

upstream qms_config {
    server qms-config-service:8083;
    keepalive 16;
}

upstream qms_chat {
    server qms-chat-service:3004;
    keepalive 32;
}

upstream qms_frontend {
    server qms-frontend:80;
    keepalive 8;
}

# HTTP重定向到HTTPS
server {
    listen 80;
    server_name _;
    return 301 https://$server_name$request_uri;
}

# HTTPS主站
server {
    listen 443 ssl http2;
    server_name _;
    
    # SSL配置
    ssl_certificate /etc/nginx/ssl/fullchain.pem;
    ssl_certificate_key /etc/nginx/ssl/privkey.pem;
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
    
    # 日志
    access_log /var/log/nginx/qms_access.log;
    error_log /var/log/nginx/qms_error.log;
    
    # 前端应用
    location / {
        proxy_pass http://qms_frontend;
        proxy_http_version 1.1;
        proxy_set_header Upgrade $http_upgrade;
        proxy_set_header Connection 'upgrade';
        proxy_set_header Host $host;
        proxy_set_header X-Real-IP $remote_addr;
        proxy_set_header X-Forwarded-For $proxy_add_x_forwarded_for;
        proxy_set_header X-Forwarded-Proto $scheme;
        proxy_cache_bypass $http_upgrade;
    }
    
    # API路由
    location /api/auth/ {
        proxy_pass http://qms_auth/;
        proxy_http_version 1.1;
        proxy_set_header Host $host;
        proxy_set_header X-Real-IP $remote_addr;
        proxy_set_header X-Forwarded-For $proxy_add_x_forwarded_for;
        proxy_set_header X-Forwarded-Proto $scheme;
    }
    
    location /api/config/ {
        proxy_pass http://qms_config/;
        proxy_http_version 1.1;
        proxy_set_header Host $host;
        proxy_set_header X-Real-IP $remote_addr;
        proxy_set_header X-Forwarded-For $proxy_add_x_forwarded_for;
        proxy_set_header X-Forwarded-Proto $scheme;
    }
    
    location /api/chat/ {
        proxy_pass http://qms_chat/;
        proxy_http_version 1.1;
        proxy_set_header Host $host;
        proxy_set_header X-Real-IP $remote_addr;
        proxy_set_header X-Forwarded-For $proxy_add_x_forwarded_for;
        proxy_set_header X-Forwarded-Proto $scheme;
        proxy_read_timeout 300s;
    }
    
    # 监控面板
    location /grafana/ {
        proxy_pass http://qms-grafana:3000/;
        proxy_http_version 1.1;
        proxy_set_header Host $host;
        proxy_set_header X-Real-IP $remote_addr;
        proxy_set_header X-Forwarded-For $proxy_add_x_forwarded_for;
        proxy_set_header X-Forwarded-Proto $scheme;
    }
}
EOF
    
    success "Nginx配置完成"
}

# 部署应用
deploy_application() {
    log "🚀 部署QMS-AI应用..."
    
    cd $PROJECT_DIR
    
    # 停止现有服务
    if [ -f "docker-compose.yml" ] || [ -f "deployment/aliyun-deploy-optimized.yml" ]; then
        log "停止现有服务..."
        docker-compose -f deployment/aliyun-deploy-optimized.yml down 2>/dev/null || true
    fi
    
    # 清理旧镜像
    log "清理旧镜像..."
    docker system prune -f
    
    # 构建和启动服务
    log "构建和启动服务..."
    docker-compose -f deployment/aliyun-deploy-optimized.yml build --no-cache
    docker-compose -f deployment/aliyun-deploy-optimized.yml up -d
    
    # 等待服务启动
    log "等待服务启动..."
    sleep 60
    
    success "应用部署完成"
}

# 检查服务状态
check_services() {
    log "🔍 检查服务状态..."
    
    cd $PROJECT_DIR
    docker-compose -f deployment/aliyun-deploy-optimized.yml ps
    
    # 检查健康状态
    log "检查服务健康状态..."
    
    services=("qms-auth-service:8084" "qms-config-service:8083" "qms-chat-service:3004")
    
    for service in "${services[@]}"; do
        name=$(echo $service | cut -d: -f1)
        port=$(echo $service | cut -d: -f2)
        
        if curl -f -s "http://localhost:$port/health" > /dev/null; then
            success "$name 服务正常"
        else
            warning "$name 服务可能未就绪，请稍后检查"
        fi
    done
}

# 显示部署结果
show_results() {
    echo ""
    echo -e "${GREEN}🎉 QMS-AI阿里云部署完成！${NC}"
    echo "========================================"
    echo ""
    echo -e "${BLUE}📋 服务信息:${NC}"
    echo "  🔐 认证服务: http://your-domain.com/api/auth/"
    echo "  ⚙️ 配置中心: http://your-domain.com/api/config/"
    echo "  💬 聊天服务: http://your-domain.com/api/chat/"
    echo "  🌐 前端应用: https://your-domain.com"
    echo "  📊 监控面板: https://your-domain.com/grafana/"
    echo ""
    echo -e "${YELLOW}⚠️ 重要提醒:${NC}"
    echo "  1. 请配置域名解析指向服务器IP"
    echo "  2. 申请SSL证书: certbot --nginx -d your-domain.com"
    echo "  3. 修改 $PROJECT_DIR/.env 中的配置参数"
    echo "  4. 配置阿里云RDS和Redis连接信息"
    echo ""
    echo -e "${CYAN}🔧 常用命令:${NC}"
    echo "  查看服务状态: cd $PROJECT_DIR && docker-compose -f deployment/aliyun-deploy-optimized.yml ps"
    echo "  查看日志: cd $PROJECT_DIR && docker-compose -f deployment/aliyun-deploy-optimized.yml logs -f"
    echo "  重启服务: cd $PROJECT_DIR && docker-compose -f deployment/aliyun-deploy-optimized.yml restart"
    echo "  停止服务: cd $PROJECT_DIR && docker-compose -f deployment/aliyun-deploy-optimized.yml down"
    echo ""
    echo -e "${PURPLE}📊 资源使用优化:${NC}"
    echo "  - 总内存限制: ~1GB (相比之前减少60%)"
    echo "  - CPU限制: 合理分配，避免资源争抢"
    echo "  - 日志轮转: 自动清理，防止磁盘满"
    echo "  - 缓存优化: Redis内存限制128MB"
}

# 主函数
main() {
    show_banner
    
    log "开始QMS-AI阿里云优化部署..."
    
    check_requirements
    install_dependencies
    setup_directories
    setup_environment
    setup_nginx
    deploy_application
    check_services
    show_results
    
    success "部署流程完成！"
}

# 执行主函数
main "$@"
