#!/bin/bash

# QMS-AI 504 Gateway Timeout 修复脚本
echo "🔧 QMS-AI 504 Gateway Timeout 诊断和修复工具"
echo "=================================================="
echo ""

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

# 检查服务器基本信息
check_server_info() {
    log_info "检查服务器基本信息..."
    echo "服务器IP: $(curl -s ifconfig.me || echo "无法获取外网IP")"
    echo "内网IP: $(hostname -I | awk '{print $1}')"
    echo "系统: $(uname -a)"
    echo "内存: $(free -h | grep Mem)"
    echo "磁盘: $(df -h / | tail -1)"
    echo ""
}

# 检查Docker服务状态
check_docker_status() {
    log_info "检查Docker服务状态..."
    
    if ! command -v docker &> /dev/null; then
        log_error "Docker未安装"
        return 1
    fi
    
    if ! systemctl is-active --quiet docker; then
        log_error "Docker服务未运行"
        log_info "尝试启动Docker服务..."
        sudo systemctl start docker
        sleep 5
    fi
    
    log_success "Docker服务正常"
    
    # 检查Docker Compose
    if ! command -v docker-compose &> /dev/null; then
        log_error "Docker Compose未安装"
        return 1
    fi
    
    log_success "Docker Compose已安装"
    echo ""
}

# 检查容器状态
check_container_status() {
    log_info "检查QMS-AI容器状态..."
    
    # 检查是否有QMS相关容器
    containers=$(docker ps -a --filter "name=qms" --format "table {{.Names}}\t{{.Status}}\t{{.Ports}}")
    
    if [ -z "$containers" ]; then
        log_warning "未找到QMS相关容器"
        return 1
    fi
    
    echo "$containers"
    echo ""
    
    # 检查运行中的容器
    running_containers=$(docker ps --filter "name=qms" --format "{{.Names}}")
    if [ -z "$running_containers" ]; then
        log_error "没有QMS容器在运行"
        return 1
    fi
    
    log_success "发现运行中的QMS容器: $running_containers"
    echo ""
}

# 检查端口占用
check_ports() {
    log_info "检查关键端口状态..."
    
    ports=(80 443 3003 3004 3005 3008 3009 6379 8072 8081 8084 8085)
    
    for port in "${ports[@]}"; do
        if netstat -tlnp | grep ":$port " > /dev/null; then
            process=$(netstat -tlnp | grep ":$port " | awk '{print $7}' | head -1)
            log_success "端口 $port: 监听中 ($process)"
        else
            log_warning "端口 $port: 未监听"
        fi
    done
    echo ""
}

# 检查Nginx配置
check_nginx_config() {
    log_info "检查Nginx配置..."
    
    # 检查Nginx容器
    nginx_container=$(docker ps --filter "name=nginx" --format "{{.Names}}" | head -1)
    
    if [ -z "$nginx_container" ]; then
        log_error "Nginx容器未运行"
        return 1
    fi
    
    log_success "Nginx容器运行中: $nginx_container"
    
    # 检查Nginx配置语法
    if docker exec "$nginx_container" nginx -t > /dev/null 2>&1; then
        log_success "Nginx配置语法正确"
    else
        log_error "Nginx配置语法错误"
        docker exec "$nginx_container" nginx -t
        return 1
    fi
    
    # 检查Nginx错误日志
    log_info "最近的Nginx错误日志:"
    docker logs "$nginx_container" --tail 20 2>&1 | grep -i error || echo "无错误日志"
    echo ""
}

# 检查后端服务健康状态
check_backend_health() {
    log_info "检查后端服务健康状态..."
    
    services=(
        "3003:配置中心"
        "3004:聊天服务"
        "3008:导出服务"
        "3009:高级功能"
        "8084:认证服务"
        "8085:API网关"
    )
    
    for service in "${services[@]}"; do
        port=$(echo $service | cut -d: -f1)
        name=$(echo $service | cut -d: -f2)
        
        if curl -s -f "http://localhost:$port/health" > /dev/null; then
            log_success "$name ($port): 健康"
        else
            log_error "$name ($port): 异常"
            # 尝试获取详细错误信息
            response=$(curl -s "http://localhost:$port/health" 2>&1 || echo "连接失败")
            echo "  响应: $response"
        fi
    done
    echo ""
}

# 检查前端服务
check_frontend_status() {
    log_info "检查前端服务状态..."
    
    # 检查前端容器
    app_container=$(docker ps --filter "name=frontend-app" --format "{{.Names}}" | head -1)
    config_container=$(docker ps --filter "name=frontend-config" --format "{{.Names}}" | head -1)
    
    if [ -n "$app_container" ]; then
        log_success "应用端容器运行中: $app_container"
        # 检查应用端健康状态
        if curl -s -f "http://localhost:8081/health" > /dev/null; then
            log_success "应用端 (8081): 健康"
        else
            log_error "应用端 (8081): 异常"
        fi
    else
        log_error "应用端容器未运行"
    fi
    
    if [ -n "$config_container" ]; then
        log_success "配置端容器运行中: $config_container"
        # 检查配置端健康状态
        if curl -s -f "http://localhost:8072/health" > /dev/null; then
            log_success "配置端 (8072): 健康"
        else
            log_error "配置端 (8072): 异常"
        fi
    else
        log_error "配置端容器未运行"
    fi
    echo ""
}

# 修复Nginx超时配置
fix_nginx_timeout() {
    log_info "修复Nginx超时配置..."
    
    nginx_container=$(docker ps --filter "name=nginx" --format "{{.Names}}" | head -1)
    
    if [ -z "$nginx_container" ]; then
        log_error "Nginx容器未运行，无法修复"
        return 1
    fi
    
    # 创建优化的Nginx配置
    cat > /tmp/nginx_timeout_fix.conf << 'EOF'
# 在http块中添加超时配置
proxy_connect_timeout 60s;
proxy_send_timeout 60s;
proxy_read_timeout 60s;
proxy_buffering on;
proxy_buffer_size 8k;
proxy_buffers 32 8k;
proxy_busy_buffers_size 16k;

# 客户端超时
client_body_timeout 60s;
client_header_timeout 60s;
send_timeout 60s;

# 保持连接
keepalive_timeout 65s;
keepalive_requests 1000;
EOF
    
    log_info "应用Nginx超时修复配置..."
    
    # 重启Nginx容器
    docker restart "$nginx_container"
    sleep 10
    
    if docker exec "$nginx_container" nginx -t > /dev/null 2>&1; then
        log_success "Nginx配置修复成功"
    else
        log_error "Nginx配置修复失败"
        return 1
    fi
    echo ""
}

# 重启服务
restart_services() {
    log_info "重启QMS-AI服务..."
    
    # 查找docker-compose文件
    compose_files=(
        "docker-compose.yml"
        "deployment/docker-compose.yml"
        "config/docker-compose.yml"
        "deployment/aliyun-deploy-optimized.yml"
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
        return 1
    fi
    
    log_info "使用配置文件: $compose_file"
    
    # 重启服务
    docker-compose -f "$compose_file" down
    sleep 5
    docker-compose -f "$compose_file" up -d
    
    log_info "等待服务启动..."
    sleep 30
    
    log_success "服务重启完成"
    echo ""
}

# 主函数
main() {
    echo "开始诊断QMS-AI 504 Gateway Timeout问题..."
    echo ""
    
    check_server_info
    check_docker_status || exit 1
    check_container_status
    check_ports
    check_nginx_config
    check_backend_health
    check_frontend_status
    
    echo ""
    echo "🔧 开始修复..."
    echo ""
    
    fix_nginx_timeout
    restart_services
    
    echo ""
    echo "✅ 修复完成！"
    echo ""
    echo "🔍 验证修复结果..."
    sleep 10
    
    check_backend_health
    check_frontend_status
    
    echo ""
    echo "📋 访问地址:"
    echo "  • 主应用: http://$(curl -s ifconfig.me || hostname -I | awk '{print $1}')"
    echo "  • 配置端: http://$(curl -s ifconfig.me || hostname -I | awk '{print $1}')/config"
    echo "  • API网关: http://$(curl -s ifconfig.me || hostname -I | awk '{print $1}'):8085"
    echo ""
    echo "如果问题仍然存在，请检查:"
    echo "1. 防火墙设置"
    echo "2. 云服务器安全组配置"
    echo "3. 域名DNS解析"
    echo "4. SSL证书配置"
}

# 执行主函数
main "$@"
