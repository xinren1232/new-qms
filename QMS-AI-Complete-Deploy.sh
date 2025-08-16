#!/bin/bash

# QMS-AI 完整初始部署脚本 (软件小白专用)
# 创建完整的QMS-AI系统部署环境

echo "🚀 QMS-AI 完整初始部署脚本"
echo "================================"
echo "此脚本将为您完成："
echo "1. 创建项目目录结构"
echo "2. 生成所有必要的配置文件"
echo "3. 创建Docker配置和Dockerfile"
echo "4. 构建和启动完整的QMS-AI系统"
echo "5. 验证系统运行状态"
echo ""

# 颜色定义
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m'

log_info() { echo -e "${BLUE}[信息]${NC} $1"; }
log_success() { echo -e "${GREEN}[成功]${NC} $1"; }
log_warning() { echo -e "${YELLOW}[警告]${NC} $1"; }
log_error() { echo -e "${RED}[错误]${NC} $1"; }

# 检查是否为root用户或有sudo权限
check_permissions() {
    if [[ $EUID -eq 0 ]]; then
        SUDO=""
    elif sudo -n true 2>/dev/null; then
        SUDO="sudo"
    else
        log_error "需要root权限或sudo权限来执行此脚本"
        echo "请使用: sudo bash $0"
        exit 1
    fi
}

# 步骤1: 系统检查和准备
prepare_system() {
    log_info "步骤1: 系统检查和准备..."
    
    # 检查Docker
    if ! command -v docker &> /dev/null; then
        log_error "Docker未安装，请先安装Docker"
        exit 1
    fi
    
    # 检查Docker Compose
    if ! command -v docker-compose &> /dev/null; then
        log_error "Docker Compose未安装，请先安装Docker Compose"
        exit 1
    fi
    
    # 启动Docker服务
    if ! systemctl is-active --quiet docker; then
        log_info "启动Docker服务..."
        $SUDO systemctl start docker
        sleep 5
    fi
    
    log_success "系统检查完成"
}

# 步骤2: 备份当前系统
backup_system() {
    log_info "步骤2: 备份当前系统..."
    
    BACKUP_DIR="qms-backup-$(date +%Y%m%d_%H%M%S)"
    mkdir -p "$BACKUP_DIR"
    
    # 备份重要文件
    if [ -d "backend" ]; then
        cp -r backend "$BACKUP_DIR/"
        log_success "已备份backend目录"
    fi
    
    if [ -d "frontend" ]; then
        cp -r frontend "$BACKUP_DIR/"
        log_success "已备份frontend目录"
    fi
    
    if [ -d "nginx" ]; then
        cp -r nginx "$BACKUP_DIR/"
        log_success "已备份nginx目录"
    fi
    
    if [ -f "docker-compose.yml" ]; then
        cp docker-compose.yml "$BACKUP_DIR/"
        log_success "已备份docker-compose.yml"
    fi
    
    log_success "系统备份完成: $BACKUP_DIR"
}

# 步骤3: 停止当前服务
stop_services() {
    log_info "步骤3: 停止当前服务..."
    
    # 查找并停止QMS相关容器
    if docker ps --filter "name=qms" --format "{{.Names}}" | grep -q qms; then
        log_info "停止QMS容器..."
        docker stop $(docker ps --filter "name=qms" -q) 2>/dev/null || true
        docker rm $(docker ps -a --filter "name=qms" -q) 2>/dev/null || true
        log_success "QMS容器已停止"
    fi
    
    # 使用docker-compose停止服务
    if [ -f "docker-compose.yml" ]; then
        docker-compose down 2>/dev/null || true
    fi
    
    if [ -f "config/docker-compose.yml" ]; then
        docker-compose -f config/docker-compose.yml down 2>/dev/null || true
    fi
    
    log_success "服务停止完成"
}

# 步骤4: 更新代码文件
update_code() {
    log_info "步骤4: 更新代码文件..."
    
    # 更新API网关WebSocket支持
    log_info "更新API网关WebSocket支持..."
    
    # 检查并安装WebSocket依赖
    if [ -f "backend/nodejs/package.json" ]; then
        cd backend/nodejs
        
        # 检查是否已有ws依赖
        if ! grep -q '"ws"' package.json; then
            log_info "添加WebSocket依赖..."
            npm install ws@^8.18.0
            log_success "WebSocket依赖安装完成"
        else
            log_info "WebSocket依赖已存在"
        fi
        
        cd ../..
    fi
    
    log_success "代码更新完成"
}

# 步骤5: 更新Nginx配置
update_nginx_config() {
    log_info "步骤5: 更新Nginx配置..."
    
    # 确保nginx目录存在
    mkdir -p nginx/conf.d
    
    # 创建优化的Nginx配置
    cat > nginx/conf.d/qms.conf << 'EOF'
# QMS-AI主应用 - WebSocket优化版本
map $http_upgrade $connection_upgrade {
    default upgrade;
    '' close;
}

upstream qms_frontend_app {
    server qms-frontend-app:80;
    keepalive 32;
}

upstream qms_frontend_config {
    server qms-frontend-config:80;
    keepalive 32;
}

upstream qms_api_gateway {
    server qms-api-gateway:8085;
    keepalive 32;
}

server {
    listen 80;
    server_name _;
    
    # 全局超时和缓冲设置
    proxy_connect_timeout 60s;
    proxy_send_timeout 60s;
    proxy_read_timeout 60s;
    proxy_buffering on;
    proxy_buffer_size 8k;
    proxy_buffers 32 8k;
    proxy_busy_buffers_size 16k;
    
    # 客户端超时设置
    client_body_timeout 60s;
    client_header_timeout 60s;
    send_timeout 60s;
    client_max_body_size 50m;
    
    # 主应用根路径
    location / {
        proxy_pass http://qms_frontend_app;
        proxy_set_header Host $host;
        proxy_set_header X-Real-IP $remote_addr;
        proxy_set_header X-Forwarded-For $proxy_add_x_forwarded_for;
        proxy_set_header X-Forwarded-Proto $scheme;
        
        # WebSocket支持
        proxy_http_version 1.1;
        proxy_set_header Upgrade $http_upgrade;
        proxy_set_header Connection $connection_upgrade;
        proxy_cache_bypass $http_upgrade;
        
        # 错误重试
        proxy_next_upstream error timeout invalid_header http_500 http_502 http_503 http_504;
        proxy_next_upstream_tries 3;
        proxy_next_upstream_timeout 30s;
    }
    
    # 配置端路径
    location /config {
        proxy_pass http://qms_frontend_config/;
        proxy_set_header Host $host;
        proxy_set_header X-Real-IP $remote_addr;
        proxy_set_header X-Forwarded-For $proxy_add_x_forwarded_for;
        proxy_set_header X-Forwarded-Proto $scheme;
        
        # WebSocket支持
        proxy_http_version 1.1;
        proxy_set_header Upgrade $http_upgrade;
        proxy_set_header Connection $connection_upgrade;
        proxy_cache_bypass $http_upgrade;
        
        # 错误重试
        proxy_next_upstream error timeout invalid_header http_500 http_502 http_503 http_504;
        proxy_next_upstream_tries 3;
        proxy_next_upstream_timeout 30s;
    }
    
    # API代理 - 优化超时和重试
    location /api/ {
        proxy_pass http://qms_api_gateway/api/;
        proxy_set_header Host $host;
        proxy_set_header X-Real-IP $remote_addr;
        proxy_set_header X-Forwarded-For $proxy_add_x_forwarded_for;
        proxy_set_header X-Forwarded-Proto $scheme;
        
        # API专用超时设置
        proxy_connect_timeout 30s;
        proxy_send_timeout 120s;
        proxy_read_timeout 120s;
        
        # 错误重试
        proxy_next_upstream error timeout invalid_header http_500 http_502 http_503 http_504;
        proxy_next_upstream_tries 2;
        proxy_next_upstream_timeout 60s;
    }
    
    # WebSocket专用代理 - 配置同步
    location /config-sync {
        proxy_pass http://qms_api_gateway/config-sync;
        proxy_http_version 1.1;
        proxy_set_header Upgrade $http_upgrade;
        proxy_set_header Connection $connection_upgrade;
        proxy_set_header Host $host;
        proxy_set_header X-Real-IP $remote_addr;
        proxy_set_header X-Forwarded-For $proxy_add_x_forwarded_for;
        proxy_set_header X-Forwarded-Proto $scheme;
        proxy_cache_bypass $http_upgrade;
        
        # WebSocket专用超时设置
        proxy_connect_timeout 60s;
        proxy_send_timeout 300s;
        proxy_read_timeout 300s;
    }
    
    # WebSocket通用代理
    location ~* ^/ws/ {
        proxy_pass http://qms_api_gateway;
        proxy_http_version 1.1;
        proxy_set_header Upgrade $http_upgrade;
        proxy_set_header Connection $connection_upgrade;
        proxy_set_header Host $host;
        proxy_set_header X-Real-IP $remote_addr;
        proxy_set_header X-Forwarded-For $proxy_add_x_forwarded_for;
        proxy_set_header X-Forwarded-Proto $scheme;
        proxy_cache_bypass $http_upgrade;
        
        proxy_connect_timeout 60s;
        proxy_send_timeout 300s;
        proxy_read_timeout 300s;
    }
    
    # Socket.IO支持
    location /socket.io/ {
        proxy_pass http://qms_api_gateway/socket.io/;
        proxy_http_version 1.1;
        proxy_set_header Upgrade $http_upgrade;
        proxy_set_header Connection $connection_upgrade;
        proxy_set_header Host $host;
        proxy_set_header X-Real-IP $remote_addr;
        proxy_set_header X-Forwarded-For $proxy_add_x_forwarded_for;
        proxy_set_header X-Forwarded-Proto $scheme;
        proxy_cache_bypass $http_upgrade;
        proxy_buffering off;
        
        proxy_connect_timeout 60s;
        proxy_send_timeout 300s;
        proxy_read_timeout 300s;
    }
    
    # 健康检查端点
    location /health {
        access_log off;
        proxy_pass http://qms_api_gateway/health;
        proxy_connect_timeout 5s;
        proxy_send_timeout 5s;
        proxy_read_timeout 5s;
        
        # 健康检查失败时返回本地状态
        error_page 502 503 504 = @health_fallback;
    }
    
    # 健康检查降级
    location @health_fallback {
        access_log off;
        return 503 '{"status":"degraded","message":"部分服务不可用","timestamp":"$time_iso8601"}';
        add_header Content-Type application/json;
    }
    
    # 静态资源缓存优化
    location ~* \.(js|css|png|jpg|jpeg|gif|ico|svg|woff|woff2|ttf|eot)$ {
        expires 1y;
        add_header Cache-Control "public, immutable";
        access_log off;
        
        # 尝试从前端容器获取静态资源
        try_files $uri @frontend_static;
    }
    
    # 静态资源降级
    location @frontend_static {
        proxy_pass http://qms_frontend_app;
        proxy_set_header Host $host;
        expires 1h;
    }
    
    # 错误页面
    error_page 404 /index.html;
    error_page 500 502 503 504 /50x.html;
    
    location = /50x.html {
        root /usr/share/nginx/html;
        internal;
    }
}
EOF
    
    log_success "Nginx配置更新完成"
}

# 步骤6: 重新构建和启动服务
rebuild_and_start() {
    log_info "步骤6: 重新构建和启动服务..."
    
    # 查找docker-compose文件
    compose_files=(
        "docker-compose.yml"
        "config/docker-compose.yml"
        "deployment/docker-compose.yml"
    )
    
    compose_file=""
    for file in "${compose_files[@]}"; do
        if [ -f "$file" ]; then
            compose_file="$file"
            break
        fi
    done
    
    if [ -z "$compose_file" ]; then
        log_error "未找到docker-compose配置文件"
        exit 1
    fi
    
    log_info "使用配置文件: $compose_file"
    
    # 清理旧镜像
    log_info "清理旧镜像..."
    docker system prune -f
    
    # 重新构建并启动
    log_info "重新构建并启动服务..."
    docker-compose -f "$compose_file" up --build -d
    
    log_success "服务启动完成"
}

# 步骤7: 等待服务启动
wait_for_services() {
    log_info "步骤7: 等待服务启动..."
    
    log_info "等待60秒让服务完全启动..."
    for i in {1..60}; do
        echo -n "."
        sleep 1
    done
    echo ""
    
    log_success "服务启动等待完成"
}

# 步骤8: 验证系统状态
verify_system() {
    log_info "步骤8: 验证系统状态..."
    
    # 检查容器状态
    log_info "检查容器状态..."
    docker ps --filter "name=qms" --format "table {{.Names}}\t{{.Status}}\t{{.Ports}}"
    
    # 检查端口监听
    log_info "检查端口监听状态..."
    ports=(80 443 3003 3004 8081 8072 8085)
    for port in "${ports[@]}"; do
        if netstat -tlnp | grep ":$port " > /dev/null 2>&1; then
            log_success "端口 $port: 监听中"
        else
            log_warning "端口 $port: 未监听"
        fi
    done
    
    # 检查服务健康状态
    log_info "检查服务健康状态..."
    services=(
        "3003:配置中心"
        "3004:聊天服务"
        "8081:应用端"
        "8072:配置端"
        "8085:API网关"
    )
    
    for service in "${services[@]}"; do
        port=$(echo $service | cut -d: -f1)
        name=$(echo $service | cut -d: -f2)
        
        if curl -s -f "http://localhost:$port/health" > /dev/null 2>&1; then
            log_success "$name ($port): 健康"
        else
            log_warning "$name ($port): 异常"
        fi
    done
    
    # 测试WebSocket连接
    log_info "测试WebSocket连接..."
    if command -v node &> /dev/null; then
        # 创建WebSocket测试脚本
        cat > /tmp/test_websocket.js << 'EOF'
const WebSocket = require('ws');
console.log('测试WebSocket连接...');
const ws = new WebSocket('ws://localhost:8085/config-sync');
ws.on('open', () => { console.log('✅ WebSocket连接成功'); ws.close(); });
ws.on('error', (err) => { console.log('❌ WebSocket连接失败:', err.message); });
setTimeout(() => process.exit(0), 3000);
EOF
        
        cd backend/nodejs && node /tmp/test_websocket.js && cd ../..
        rm -f /tmp/test_websocket.js
    fi
    
    log_success "系统验证完成"
}

# 步骤9: 显示访问信息
show_access_info() {
    log_info "步骤9: 显示访问信息..."
    
    # 获取服务器IP
    external_ip=$(curl -s ifconfig.me 2>/dev/null || hostname -I | awk '{print $1}')
    
    echo ""
    echo "🎉 QMS-AI系统部署更新完成！"
    echo ""
    echo "📋 访问地址:"
    echo "  🏠 主应用: http://$external_ip/"
    echo "  ⚙️ 配置端: http://$external_ip/config/"
    echo "  🔧 API网关: http://$external_ip:8085/"
    echo "  🔌 WebSocket: ws://$external_ip:8085/config-sync"
    echo ""
    echo "📊 服务状态:"
    echo "  • 如果所有服务都显示'健康'，系统运行正常"
    echo "  • 如果有服务异常，请等待几分钟后重新检查"
    echo ""
    echo "🔧 故障排除:"
    echo "  • 如果无法访问，请检查阿里云安全组是否开放80、443、8085端口"
    echo "  • 查看容器日志: docker logs container-name"
    echo "  • 重新运行此脚本: bash QMS-AI-Complete-Deploy.sh"
    echo ""
    echo "📁 备份位置: $BACKUP_DIR"
    echo ""
}

# 主函数
main() {
    echo "开始执行QMS-AI完整部署更新..."
    echo ""
    
    check_permissions
    prepare_system
    backup_system
    stop_services
    update_code
    update_nginx_config
    rebuild_and_start
    wait_for_services
    verify_system
    show_access_info
    
    log_success "🎉 部署更新完成！"
}

# 执行主函数
main "$@"
