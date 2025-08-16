#!/bin/bash

# QMS-AI 504错误快速修复脚本
echo "🚀 QMS-AI 504错误快速修复脚本"
echo "================================"
echo ""

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

# 1. 快速检查服务状态
log_info "检查关键服务状态..."
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
        log_success "$name ($port): 正常"
    else
        log_error "$name ($port): 异常"
        failed_services+=("$service")
    fi
done

# 2. 如果有服务异常，尝试重启
if [ ${#failed_services[@]} -gt 0 ]; then
    log_warning "发现 ${#failed_services[@]} 个异常服务，尝试重启..."
    
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
    
    if [ -n "$compose_file" ]; then
        log_info "使用配置文件: $compose_file"
        log_info "重启服务..."
        
        docker-compose -f "$compose_file" restart
        sleep 20
        
        log_info "验证重启结果..."
        for service in "${failed_services[@]}"; do
            port=$(echo $service | cut -d: -f1)
            name=$(echo $service | cut -d: -f2)
            
            if curl -s -f "http://localhost:$port/health" > /dev/null 2>&1; then
                log_success "$name ($port): 重启成功"
            else
                log_error "$name ($port): 重启失败"
            fi
        done
    else
        log_error "未找到docker-compose配置文件"
    fi
fi

# 3. 检查Nginx配置并重启
log_info "检查Nginx配置..."
nginx_container=$(docker ps --filter "name=nginx" --format "{{.Names}}" | head -1)

if [ -n "$nginx_container" ]; then
    if docker exec "$nginx_container" nginx -t > /dev/null 2>&1; then
        log_success "Nginx配置正确"
        log_info "重启Nginx..."
        docker restart "$nginx_container"
        sleep 10
        log_success "Nginx重启完成"
    else
        log_error "Nginx配置错误"
        docker exec "$nginx_container" nginx -t
    fi
else
    log_warning "未找到Nginx容器"
fi

# 4. 检查防火墙和端口
log_info "检查防火墙状态..."
if command -v ufw &> /dev/null; then
    if ufw status | grep -q "Status: active"; then
        log_warning "UFW防火墙已启用，检查端口开放状态..."
        for port in 80 443 8081 8072; do
            if ufw status | grep -q "$port"; then
                log_success "端口 $port: 已开放"
            else
                log_warning "端口 $port: 未开放"
                log_info "开放端口 $port..."
                sudo ufw allow $port
            fi
        done
    else
        log_success "UFW防火墙未启用"
    fi
elif command -v firewall-cmd &> /dev/null; then
    if systemctl is-active --quiet firewalld; then
        log_warning "firewalld防火墙已启用，检查端口开放状态..."
        for port in 80 443 8081 8072; do
            if firewall-cmd --list-ports | grep -q "$port"; then
                log_success "端口 $port: 已开放"
            else
                log_warning "端口 $port: 未开放"
                log_info "开放端口 $port..."
                sudo firewall-cmd --permanent --add-port=$port/tcp
                sudo firewall-cmd --reload
            fi
        done
    else
        log_success "firewalld防火墙未启用"
    fi
else
    log_info "未检测到常见防火墙工具"
fi

# 5. 最终验证
echo ""
log_info "最终验证..."
echo ""

# 检查外网访问
external_ip=$(curl -s ifconfig.me || echo "unknown")
log_info "服务器外网IP: $external_ip"

# 测试主要访问点
test_urls=(
    "http://localhost/"
    "http://localhost/config/"
    "http://localhost/health"
    "http://localhost:8085/health"
)

for url in "${test_urls[@]}"; do
    if curl -s -f "$url" > /dev/null 2>&1; then
        log_success "$url: 可访问"
    else
        log_error "$url: 不可访问"
    fi
done

echo ""
echo "🎯 访问地址:"
echo "  • 主应用: http://$external_ip/"
echo "  • 配置端: http://$external_ip/config/"
echo "  • API网关: http://$external_ip:8085/"
echo ""

echo "📋 如果问题仍然存在，请检查:"
echo "1. 云服务器安全组是否开放80, 443, 8081, 8072端口"
echo "2. 域名DNS是否正确解析到服务器IP"
echo "3. 是否有CDN或负载均衡器配置问题"
echo "4. 查看详细日志: docker logs container-name"
echo ""

log_success "快速修复完成！"
