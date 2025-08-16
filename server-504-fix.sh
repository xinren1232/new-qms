#!/bin/bash

# QMS-AI 服务器504错误修复脚本
echo "🔧 QMS-AI 服务器504错误修复脚本"
echo "================================"

# 1. 检查Docker服务
echo "检查Docker服务状态..."
if ! systemctl is-active --quiet docker; then
    echo "启动Docker服务..."
    sudo systemctl start docker
    sleep 5
fi

# 2. 检查QMS容器状态
echo "检查QMS容器状态..."
docker ps --filter "name=qms" --format "table {{.Names}}\t{{.Status}}\t{{.Ports}}"

# 3. 检查关键端口
echo "检查关键端口..."
for port in 80 443 3003 3004 8081 8072 8085; do
    if netstat -tlnp | grep ":$port " > /dev/null; then
        echo "✅ 端口 $port: 监听中"
    else
        echo "❌ 端口 $port: 未监听"
    fi
done

# 4. 检查后端服务健康状态
echo "检查后端服务..."
services=("3003:配置中心" "3004:聊天服务" "8081:应用端" "8072:配置端" "8085:API网关")

for service in "${services[@]}"; do
    port=$(echo $service | cut -d: -f1)
    name=$(echo $service | cut -d: -f2)
    
    if curl -s -f "http://localhost:$port/health" > /dev/null 2>&1; then
        echo "✅ $name ($port): 健康"
    else
        echo "❌ $name ($port): 异常"
    fi
done

# 5. 重启服务
echo "重启QMS服务..."
if [ -f "docker-compose.yml" ]; then
    docker-compose restart
elif [ -f "config/docker-compose.yml" ]; then
    docker-compose -f config/docker-compose.yml restart
elif [ -f "deployment/docker-compose.yml" ]; then
    docker-compose -f deployment/docker-compose.yml restart
else
    echo "未找到docker-compose文件，手动重启容器..."
    docker restart $(docker ps --filter "name=qms" -q)
fi

echo "等待服务启动..."
sleep 30

# 6. 检查防火墙
echo "检查防火墙设置..."
if command -v ufw &> /dev/null && ufw status | grep -q "Status: active"; then
    echo "开放必要端口..."
    sudo ufw allow 80
    sudo ufw allow 443
    sudo ufw allow 8081
    sudo ufw allow 8072
elif command -v firewall-cmd &> /dev/null && systemctl is-active --quiet firewalld; then
    echo "开放必要端口..."
    sudo firewall-cmd --permanent --add-port=80/tcp
    sudo firewall-cmd --permanent --add-port=443/tcp
    sudo firewall-cmd --permanent --add-port=8081/tcp
    sudo firewall-cmd --permanent --add-port=8072/tcp
    sudo firewall-cmd --reload
fi

# 7. 最终验证
echo "最终验证..."
external_ip=$(curl -s ifconfig.me || hostname -I | awk '{print $1}')
echo "服务器IP: $external_ip"

test_urls=("http://localhost/" "http://localhost/config/" "http://localhost/health")
for url in "${test_urls[@]}"; do
    if curl -s -f "$url" > /dev/null 2>&1; then
        echo "✅ $url: 可访问"
    else
        echo "❌ $url: 不可访问"
    fi
done

echo ""
echo "🎯 访问地址:"
echo "  • 主应用: http://$external_ip/"
echo "  • 配置端: http://$external_ip/config/"
echo ""
echo "如果问题仍然存在，请检查云服务器安全组配置！"
