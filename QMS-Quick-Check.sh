#!/bin/bash

# QMS-AI 快速状态检查脚本
echo "🔍 QMS-AI 系统状态快速检查"
echo "=========================="

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

# 获取服务器IP
get_server_ip() {
    external_ip=$(curl -s ifconfig.me 2>/dev/null || hostname -I | awk '{print $1}')
    echo "$external_ip"
}

# 检查Docker状态
check_docker() {
    log_info "检查Docker服务..."
    if ! command -v docker &> /dev/null; then
        log_error "Docker未安装"
        return 1
    fi
    
    if ! systemctl is-active --quiet docker; then
        log_error "Docker服务未运行"
        return 1
    fi
    
    log_success "Docker服务正常"
    return 0
}

# 检查容器状态
check_containers() {
    log_info "检查QMS容器状态..."
    
    containers=$(docker ps --filter "name=qms" --format "{{.Names}}" | wc -l)
    if [ "$containers" -eq 0 ]; then
        log_error "未找到QMS容器"
        return 1
    fi
    
    log_success "发现 $containers 个QMS容器"
    docker ps --filter "name=qms" --format "table {{.Names}}\t{{.Status}}\t{{.Ports}}"
    return 0
}

# 检查端口监听
check_ports() {
    log_info "检查端口监听状态..."
    
    ports=(80 3003 3004 8081 8072 8085)
    failed_ports=()
    
    for port in "${ports[@]}"; do
        if netstat -tlnp | grep ":$port " > /dev/null 2>&1; then
            log_success "端口 $port: 监听中"
        else
            log_warning "端口 $port: 未监听"
            failed_ports+=($port)
        fi
    done
    
    if [ ${#failed_ports[@]} -gt 0 ]; then
        return 1
    fi
    return 0
}

# 检查服务健康状态
check_services() {
    log_info "检查服务健康状态..."
    
    services=(
        "3003:配置中心"
        "3004:聊天服务"
        "8081:应用端"
        "8072:配置端"
        "8085:API网关"
    )
    
    failed_services=()
    
    for service in "${services[@]}"; do
        port=$(echo $service | cut -d: -f1)
        name=$(echo $service | cut -d: -f2)
        
        if curl -s -f "http://localhost:$port/health" > /dev/null 2>&1; then
            log_success "$name ($port): 健康"
        else
            log_error "$name ($port): 异常"
            failed_services+=("$service")
        fi
    done
    
    if [ ${#failed_services[@]} -gt 0 ]; then
        return 1
    fi
    return 0
}

# 测试WebSocket连接
test_websocket() {
    log_info "测试WebSocket连接..."
    
    if ! command -v node &> /dev/null; then
        log_warning "Node.js未安装，跳过WebSocket测试"
        return 0
    fi
    
    # 创建WebSocket测试脚本
    cat > /tmp/test_websocket.js << 'EOF'
const WebSocket = require('ws');

const ws = new WebSocket('ws://localhost:8085/config-sync');

ws.on('open', function open() {
    console.log('✅ WebSocket连接成功');
    ws.close();
    process.exit(0);
});

ws.on('error', function error(err) {
    console.log('❌ WebSocket连接失败:', err.message);
    process.exit(1);
});

// 5秒超时
setTimeout(() => {
    console.log('❌ WebSocket连接超时');
    process.exit(1);
}, 5000);
EOF
    
    if cd backend/nodejs && node /tmp/test_websocket.js 2>/dev/null; then
        cd ../..
        rm -f /tmp/test_websocket.js
        log_success "WebSocket连接正常"
        return 0
    else
        cd ../.. 2>/dev/null
        rm -f /tmp/test_websocket.js
        log_error "WebSocket连接失败"
        return 1
    fi
}

# 测试网页访问
test_web_access() {
    log_info "测试网页访问..."
    
    urls=(
        "http://localhost/:主应用"
        "http://localhost/config/:配置端"
        "http://localhost/health:健康检查"
    )
    
    failed_urls=()
    
    for url_info in "${urls[@]}"; do
        url=$(echo $url_info | cut -d: -f1)
        name=$(echo $url_info | cut -d: -f2)
        
        if curl -s -f "$url" > /dev/null 2>&1; then
            log_success "$name: 可访问"
        else
            log_error "$name: 不可访问"
            failed_urls+=("$url_info")
        fi
    done
    
    if [ ${#failed_urls[@]} -gt 0 ]; then
        return 1
    fi
    return 0
}

# 显示访问信息
show_access_info() {
    server_ip=$(get_server_ip)
    
    echo ""
    echo "📋 访问地址:"
    echo "  🏠 主应用: http://$server_ip/"
    echo "  ⚙️ 配置端: http://$server_ip/config/"
    echo "  🔧 API网关: http://$server_ip:8085/"
    echo "  🔌 WebSocket: ws://$server_ip:8085/config-sync"
    echo ""
}

# 显示故障排除建议
show_troubleshooting() {
    echo ""
    echo "🔧 故障排除建议:"
    echo ""
    echo "1. 如果容器未运行:"
    echo "   docker-compose up -d"
    echo ""
    echo "2. 如果端口未监听:"
    echo "   docker-compose restart"
    echo ""
    echo "3. 如果服务异常:"
    echo "   docker logs container-name"
    echo ""
    echo "4. 如果WebSocket失败:"
    echo "   bash QMS-AI-Complete-Deploy.sh"
    echo ""
    echo "5. 如果网页无法访问:"
    echo "   检查阿里云安全组是否开放80、443、8085端口"
    echo ""
    echo "6. 重新部署:"
    echo "   bash QMS-AI-Complete-Deploy.sh"
    echo ""
}

# 主函数
main() {
    echo "开始检查QMS-AI系统状态..."
    echo ""
    
    # 执行所有检查
    docker_ok=0
    containers_ok=0
    ports_ok=0
    services_ok=0
    websocket_ok=0
    web_ok=0
    
    check_docker && docker_ok=1
    check_containers && containers_ok=1
    check_ports && ports_ok=1
    check_services && services_ok=1
    test_websocket && websocket_ok=1
    test_web_access && web_ok=1
    
    # 计算总分
    total_score=$((docker_ok + containers_ok + ports_ok + services_ok + websocket_ok + web_ok))
    max_score=6
    
    echo ""
    echo "📊 系统状态总结:"
    echo "=================="
    
    [ $docker_ok -eq 1 ] && echo "✅ Docker服务: 正常" || echo "❌ Docker服务: 异常"
    [ $containers_ok -eq 1 ] && echo "✅ 容器状态: 正常" || echo "❌ 容器状态: 异常"
    [ $ports_ok -eq 1 ] && echo "✅ 端口监听: 正常" || echo "❌ 端口监听: 异常"
    [ $services_ok -eq 1 ] && echo "✅ 服务健康: 正常" || echo "❌ 服务健康: 异常"
    [ $websocket_ok -eq 1 ] && echo "✅ WebSocket: 正常" || echo "❌ WebSocket: 异常"
    [ $web_ok -eq 1 ] && echo "✅ 网页访问: 正常" || echo "❌ 网页访问: 异常"
    
    echo ""
    echo "🎯 系统健康度: $total_score/$max_score"
    
    if [ $total_score -eq $max_score ]; then
        echo "🎉 系统运行完美！所有功能正常"
        show_access_info
    elif [ $total_score -ge 4 ]; then
        echo "⚠️ 系统基本正常，但有部分问题需要关注"
        show_access_info
        show_troubleshooting
    else
        echo "❌ 系统存在严重问题，需要立即修复"
        show_troubleshooting
    fi
    
    echo ""
    log_info "检查完成！"
}

# 执行主函数
main "$@"
