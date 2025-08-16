#!/bin/bash

# QMS-AI WebSocket连接修复脚本
echo "🔧 QMS-AI WebSocket连接修复脚本"
echo "================================"

# 颜色定义
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m'

log_info() { echo -e "${BLUE}[INFO]${NC} $1"; }
log_success() { echo -e "${GREEN}[SUCCESS]${NC} $1"; }
log_warning() { echo -e "${YELLOW}[WARNING]${NC} $1"; }
log_error() { echo -e "${RED}[ERROR]${NC} $1"; }

# 1. 检查Nginx容器
log_info "检查Nginx容器状态..."
nginx_container=$(docker ps --filter "name=nginx" --format "{{.Names}}" | head -1)

if [ -z "$nginx_container" ]; then
    log_error "未找到Nginx容器"
    exit 1
fi

log_success "找到Nginx容器: $nginx_container"

# 2. 备份当前Nginx配置
log_info "备份当前Nginx配置..."
docker exec "$nginx_container" cp /etc/nginx/conf.d/qms.conf /etc/nginx/conf.d/qms.conf.backup
log_success "配置已备份"

# 3. 创建WebSocket优化配置
log_info "创建WebSocket优化配置..."
cat > /tmp/websocket_fix.conf << 'EOF'
# WebSocket连接优化配置
map $http_upgrade $connection_upgrade {
    default upgrade;
    '' close;
}

# 上游服务器定义
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
    
    # 全局WebSocket支持
    proxy_http_version 1.1;
    proxy_set_header Upgrade $http_upgrade;
    proxy_set_header Connection $connection_upgrade;
    proxy_set_header Host $host;
    proxy_set_header X-Real-IP $remote_addr;
    proxy_set_header X-Forwarded-For $proxy_add_x_forwarded_for;
    proxy_set_header X-Forwarded-Proto $scheme;
    proxy_cache_bypass $http_upgrade;
    
    # 超时设置
    proxy_connect_timeout 60s;
    proxy_send_timeout 300s;
    proxy_read_timeout 300s;
    proxy_buffering off;
    
    # 主应用
    location / {
        proxy_pass http://qms_frontend_app;
    }
    
    # 配置端
    location /config {
        proxy_pass http://qms_frontend_config/;
    }
    
    # API代理
    location /api/ {
        proxy_pass http://qms_api_gateway/api/;
        proxy_send_timeout 120s;
        proxy_read_timeout 120s;
    }
    
    # WebSocket专用路径
    location /config-sync {
        proxy_pass http://qms_api_gateway/config-sync;
    }
    
    location /ws/ {
        proxy_pass http://qms_api_gateway/ws/;
    }
    
    location /socket.io/ {
        proxy_pass http://qms_api_gateway/socket.io/;
    }
    
    # 健康检查
    location /health {
        proxy_pass http://qms_api_gateway/health;
        proxy_connect_timeout 5s;
        proxy_send_timeout 5s;
        proxy_read_timeout 5s;
    }
}
EOF

# 4. 应用新配置
log_info "应用WebSocket优化配置..."
docker cp /tmp/websocket_fix.conf "$nginx_container":/etc/nginx/conf.d/qms.conf

# 5. 测试配置
log_info "测试Nginx配置..."
if docker exec "$nginx_container" nginx -t; then
    log_success "Nginx配置测试通过"
else
    log_error "Nginx配置测试失败，恢复备份..."
    docker exec "$nginx_container" cp /etc/nginx/conf.d/qms.conf.backup /etc/nginx/conf.d/qms.conf
    exit 1
fi

# 6. 重新加载Nginx
log_info "重新加载Nginx配置..."
docker exec "$nginx_container" nginx -s reload
sleep 5
log_success "Nginx配置已重新加载"

# 7. 检查后端WebSocket支持
log_info "检查后端WebSocket支持..."

# 检查API网关是否支持WebSocket
api_gateway_container=$(docker ps --filter "name=api-gateway" --format "{{.Names}}" | head -1)
if [ -n "$api_gateway_container" ]; then
    log_success "API网关容器运行中: $api_gateway_container"
    
    # 检查API网关日志中的WebSocket相关信息
    log_info "检查API网关WebSocket配置..."
    if docker logs "$api_gateway_container" --tail 50 | grep -i websocket > /dev/null; then
        log_success "API网关支持WebSocket"
    else
        log_warning "API网关可能不支持WebSocket，需要检查代码配置"
    fi
else
    log_warning "未找到API网关容器"
fi

# 8. 测试WebSocket连接
log_info "测试WebSocket连接..."

# 使用curl测试WebSocket握手
if curl -s -I -H "Connection: Upgrade" -H "Upgrade: websocket" -H "Sec-WebSocket-Key: test" -H "Sec-WebSocket-Version: 13" http://localhost/config-sync | grep -i "upgrade.*websocket" > /dev/null; then
    log_success "WebSocket握手测试通过"
else
    log_warning "WebSocket握手测试失败，可能需要后端支持"
fi

# 9. 清理临时文件
rm -f /tmp/websocket_fix.conf

# 10. 显示结果
echo ""
log_info "WebSocket修复完成！"
echo ""
echo "📋 修复内容:"
echo "  ✅ 优化了Nginx WebSocket代理配置"
echo "  ✅ 添加了WebSocket专用路径支持"
echo "  ✅ 增加了连接超时设置"
echo "  ✅ 启用了连接保持功能"
echo ""
echo "🔍 如果WebSocket仍然无法连接，请检查:"
echo "  1. 后端服务是否支持WebSocket"
echo "  2. 防火墙是否允许WebSocket连接"
echo "  3. 云服务器安全组是否正确配置"
echo "  4. 前端WebSocket连接地址是否正确"
echo ""
echo "📝 测试WebSocket连接:"
echo "  • 在浏览器控制台运行:"
echo "    const ws = new WebSocket('ws://your-domain/config-sync');"
echo "    ws.onopen = () => console.log('WebSocket连接成功');"
echo "    ws.onerror = (e) => console.log('WebSocket连接失败:', e);"
echo ""

log_success "修复脚本执行完成！"
