#!/bin/bash

echo "🚀 QMS智能管理系统 - 一键部署脚本"
echo "=================================="

# 颜色定义
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m' # No Color

# 日志函数
log_info() {
    echo -e "${BLUE}[INFO]${NC} $1"
}

log_success() {
    echo -e "${GREEN}[SUCCESS]${NC} $1"
}

log_warning() {
    echo -e "${YELLOW}[WARNING]${NC} $1"
}

log_error() {
    echo -e "${RED}[ERROR]${NC} $1"
}

# 检查是否为root用户
check_root() {
    if [ "$EUID" -ne 0 ]; then
        log_error "请使用root用户运行此脚本"
        echo "使用方法: sudo ./one-click-deploy.sh"
        exit 1
    fi
}

# 获取用户输入
get_user_input() {
    log_info "请输入部署配置信息："
    
    # 域名配置
    read -p "请输入您的域名 (例: qms.yourcompany.com): " DOMAIN
    if [ -z "$DOMAIN" ]; then
        log_error "域名不能为空"
        exit 1
    fi
    
    # 邮箱配置（用于SSL证书）
    read -p "请输入您的邮箱 (用于SSL证书申请): " EMAIL
    if [ -z "$EMAIL" ]; then
        log_error "邮箱不能为空"
        exit 1
    fi
    
    # 数据库密码
    read -s -p "请设置数据库密码: " DB_PASSWORD
    echo
    if [ -z "$DB_PASSWORD" ]; then
        log_error "数据库密码不能为空"
        exit 1
    fi
    
    # 飞书配置
    read -p "请输入飞书App ID (可选，回车跳过): " FEISHU_APP_ID
    if [ ! -z "$FEISHU_APP_ID" ]; then
        read -s -p "请输入飞书App Secret: " FEISHU_APP_SECRET
        echo
    fi
    
    log_success "配置信息收集完成"
}

# 系统环境检查和安装
setup_environment() {
    log_info "开始环境配置..."
    
    # 运行环境安装脚本
    if [ -f "./aliyun-setup.sh" ]; then
        chmod +x ./aliyun-setup.sh
        ./aliyun-setup.sh
    else
        log_error "未找到aliyun-setup.sh脚本"
        exit 1
    fi
    
    log_success "环境配置完成"
}

# 申请SSL证书
setup_ssl() {
    log_info "申请SSL证书..."
    
    # 安装certbot
    if ! command -v certbot &> /dev/null; then
        if command -v yum &> /dev/null; then
            yum install -y epel-release
            yum install -y certbot python3-certbot-nginx
        elif command -v apt &> /dev/null; then
            apt update
            apt install -y certbot python3-certbot-nginx
        fi
    fi
    
    # 临时停止nginx
    systemctl stop nginx
    
    # 申请证书
    certbot certonly --standalone -d $DOMAIN --email $EMAIL --agree-tos --non-interactive
    
    if [ $? -eq 0 ]; then
        log_success "SSL证书申请成功"
    else
        log_warning "SSL证书申请失败，将使用自签名证书"
        # 创建自签名证书
        mkdir -p /etc/letsencrypt/live/$DOMAIN
        openssl req -x509 -nodes -days 365 -newkey rsa:2048 \
            -keyout /etc/letsencrypt/live/$DOMAIN/privkey.pem \
            -out /etc/letsencrypt/live/$DOMAIN/fullchain.pem \
            -subj "/C=CN/ST=State/L=City/O=Organization/CN=$DOMAIN"
    fi
    
    # 重启nginx
    systemctl start nginx
}

# 配置项目
setup_project() {
    log_info "配置项目..."
    
    PROJECT_DIR="/opt/qms"
    cd $PROJECT_DIR
    
    # 克隆或更新代码
    if [ ! -d ".git" ]; then
        log_info "克隆项目代码..."
        # 这里需要替换为实际的Git仓库地址
        # git clone https://github.com/your-repo/qms.git .
        
        # 临时方案：复制本地代码
        if [ -d "/tmp/qms-source" ]; then
            cp -r /tmp/qms-source/* .
        else
            log_warning "请手动将项目代码复制到 $PROJECT_DIR"
        fi
    else
        log_info "更新项目代码..."
        git pull origin main
    fi
    
    # 创建环境变量文件
    log_info "创建环境变量文件..."
    cat > .env << EOF
# QMS智能管理系统 - 生产环境配置
NODE_ENV=production
DOMAIN=$DOMAIN

# 数据库配置
DB_HOST=postgres
DB_PORT=5432
DB_NAME=qms
DB_USER=qms
DB_PASSWORD=$DB_PASSWORD

# Redis配置
REDIS_HOST=redis
REDIS_PORT=6379
REDIS_PASSWORD=$(openssl rand -base64 32)

# 飞书配置
FEISHU_APP_ID=$FEISHU_APP_ID
FEISHU_APP_SECRET=$FEISHU_APP_SECRET
FEISHU_REDIRECT_URI=https://$DOMAIN/auth/feishu/callback

# JWT配置
JWT_SECRET=$(openssl rand -base64 64)
ENCRYPTION_KEY=$(openssl rand -base64 32)

# MinIO配置
MINIO_ROOT_USER=minioadmin
MINIO_ROOT_PASSWORD=$(openssl rand -base64 32)

# Grafana配置
GRAFANA_USER=admin
GRAFANA_PASSWORD=$(openssl rand -base64 16)

# 邮件配置
ADMIN_EMAIL=$EMAIL
EOF
    
    # 更新Nginx配置中的域名
    sed -i "s/your-domain.com/$DOMAIN/g" /etc/nginx/conf.d/qms.conf
    
    log_success "项目配置完成"
}

# 构建和启动服务
deploy_services() {
    log_info "构建和启动服务..."
    
    PROJECT_DIR="/opt/qms"
    cd $PROJECT_DIR
    
    # 停止现有服务
    docker-compose -f docker-compose.prod.yml down 2>/dev/null || true
    
    # 构建镜像
    log_info "构建Docker镜像..."
    docker-compose -f docker-compose.prod.yml build --no-cache
    
    # 启动服务
    log_info "启动服务..."
    docker-compose -f docker-compose.prod.yml up -d
    
    # 等待服务启动
    log_info "等待服务启动..."
    sleep 60
    
    # 检查服务状态
    log_info "检查服务状态..."
    docker-compose -f docker-compose.prod.yml ps
    
    log_success "服务部署完成"
}

# 配置自动备份
setup_backup() {
    log_info "配置自动备份..."
    
    # 创建备份脚本
    cat > /opt/qms/backup.sh << 'EOF'
#!/bin/bash

BACKUP_DIR="/opt/qms/backup"
DATE=$(date +%Y%m%d_%H%M%S)

# 备份数据库
docker exec qms-postgres pg_dump -U qms qms > $BACKUP_DIR/db_backup_$DATE.sql

# 备份配置文件
tar -czf $BACKUP_DIR/config_backup_$DATE.tar.gz /opt/qms/.env /etc/nginx/conf.d/qms.conf

# 清理7天前的备份
find $BACKUP_DIR -name "*.sql" -mtime +7 -delete
find $BACKUP_DIR -name "*.tar.gz" -mtime +7 -delete

echo "备份完成: $DATE"
EOF
    
    chmod +x /opt/qms/backup.sh
    
    # 添加到crontab（每天凌晨2点备份）
    (crontab -l 2>/dev/null; echo "0 2 * * * /opt/qms/backup.sh >> /opt/qms/logs/backup.log 2>&1") | crontab -
    
    log_success "自动备份配置完成"
}

# 配置监控
setup_monitoring() {
    log_info "配置系统监控..."
    
    # 创建监控脚本
    cat > /opt/qms/health-check.sh << 'EOF'
#!/bin/bash

# 检查关键服务
services=("qms-postgres" "qms-redis" "qms-consul" "qms-casdoor")
failed_services=()

for service in "${services[@]}"; do
    if ! docker ps | grep -q $service; then
        failed_services+=($service)
    fi
done

# 检查磁盘空间
disk_usage=$(df / | tail -1 | awk '{print $5}' | sed 's/%//')
if [ $disk_usage -gt 80 ]; then
    echo "警告: 磁盘使用率超过80%"
fi

# 检查内存使用
memory_usage=$(free | grep Mem | awk '{printf("%.1f", $3/$2 * 100.0)}')
if (( $(echo "$memory_usage > 90" | bc -l) )); then
    echo "警告: 内存使用率超过90%"
fi

# 如果有服务失败，发送通知
if [ ${#failed_services[@]} -gt 0 ]; then
    echo "错误: 以下服务未运行: ${failed_services[*]}"
    # 这里可以添加邮件或短信通知
fi
EOF
    
    chmod +x /opt/qms/health-check.sh
    
    # 添加到crontab（每5分钟检查一次）
    (crontab -l 2>/dev/null; echo "*/5 * * * * /opt/qms/health-check.sh >> /opt/qms/logs/health.log 2>&1") | crontab -
    
    log_success "监控配置完成"
}

# 显示部署结果
show_result() {
    log_success "🎉 QMS智能管理系统部署完成！"
    echo "=================================="
    echo "🌐 访问地址: https://$DOMAIN"
    echo "🔐 Casdoor管理: https://$DOMAIN:8000"
    echo "📊 Consul控制台: https://$DOMAIN:8500"
    echo "📈 Grafana监控: https://$DOMAIN:3000"
    echo "💾 MinIO存储: https://$DOMAIN:9001"
    echo ""
    echo "📋 管理员账号信息："
    echo "  Casdoor: admin / 123456"
    echo "  Grafana: $(grep GRAFANA_USER /opt/qms/.env | cut -d'=' -f2) / $(grep GRAFANA_PASSWORD /opt/qms/.env | cut -d'=' -f2)"
    echo "  MinIO: $(grep MINIO_ROOT_USER /opt/qms/.env | cut -d'=' -f2) / $(grep MINIO_ROOT_PASSWORD /opt/qms/.env | cut -d'=' -f2)"
    echo ""
    echo "🔧 常用命令："
    echo "  查看服务状态: cd /opt/qms && docker-compose ps"
    echo "  查看日志: cd /opt/qms && docker-compose logs -f"
    echo "  重启服务: cd /opt/qms && docker-compose restart"
    echo "  系统监控: /opt/qms/monitor.sh"
    echo "  手动备份: /opt/qms/backup.sh"
    echo ""
    echo "📞 技术支持："
    echo "  配置文件: /opt/qms/.env"
    echo "  日志目录: /opt/qms/logs"
    echo "  备份目录: /opt/qms/backup"
    echo ""
    log_warning "请及时修改默认密码并配置防火墙规则！"
}

# 主函数
main() {
    log_info "开始QMS智能管理系统一键部署..."
    
    check_root
    get_user_input
    setup_environment
    setup_ssl
    setup_project
    deploy_services
    setup_backup
    setup_monitoring
    show_result
    
    log_success "部署完成！"
}

# 执行主函数
main "$@"
