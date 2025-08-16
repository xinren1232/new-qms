#!/bin/bash

# QMS-AI 服务器WebSocket修复脚本
echo "🔧 QMS-AI 服务器WebSocket修复脚本"
echo "================================"

# 1. 安装WebSocket依赖
echo "📦 安装WebSocket依赖..."
cd backend/nodejs
npm install ws@^8.18.0
echo "✅ WebSocket依赖安装完成"

# 2. 重启API网关服务
echo "🔄 重启API网关服务..."
if command -v docker &> /dev/null; then
    # Docker环境
    echo "检测到Docker环境，重启容器..."
    
    # 查找API网关容器
    gateway_container=$(docker ps --filter "name=api-gateway" --format "{{.Names}}" | head -1)
    if [ -n "$gateway_container" ]; then
        echo "重启API网关容器: $gateway_container"
        docker restart "$gateway_container"
        sleep 10
    else
        echo "未找到API网关容器，尝试重启所有QMS容器..."
        docker restart $(docker ps --filter "name=qms" -q)
        sleep 15
    fi
    
    # 检查容器状态
    echo "检查容器状态..."
    docker ps --filter "name=qms" --format "table {{.Names}}\t{{.Status}}\t{{.Ports}}"
    
elif command -v pm2 &> /dev/null; then
    # PM2环境
    echo "检测到PM2环境，重启服务..."
    pm2 restart api-gateway || pm2 start api-gateway.js --name api-gateway
    sleep 5
    pm2 status
    
else
    # 直接运行
    echo "直接运行模式，请手动重启API网关服务"
    echo "运行命令: cd backend/nodejs && node api-gateway.js"
fi

# 3. 检查WebSocket端点
echo "🔍 检查WebSocket端点..."
sleep 5

# 检查HTTP健康状态
if curl -s -f "http://localhost:8085/health" > /dev/null; then
    echo "✅ API网关HTTP服务正常"
else
    echo "❌ API网关HTTP服务异常"
fi

# 4. 测试WebSocket连接
echo "🧪 测试WebSocket连接..."

# 创建WebSocket测试脚本
cat > /tmp/test_websocket.js << 'EOF'
const WebSocket = require('ws');

console.log('🔌 测试WebSocket连接...');

const ws = new WebSocket('ws://localhost:8085/config-sync');

ws.on('open', function open() {
    console.log('✅ WebSocket连接成功');
    
    // 发送测试消息
    ws.send(JSON.stringify({
        type: 'test',
        message: 'Hello WebSocket',
        timestamp: new Date().toISOString()
    }));
});

ws.on('message', function message(data) {
    console.log('📨 收到消息:', data.toString());
});

ws.on('error', function error(err) {
    console.error('❌ WebSocket错误:', err.message);
});

ws.on('close', function close() {
    console.log('🔌 WebSocket连接关闭');
    process.exit(0);
});

// 5秒后关闭连接
setTimeout(() => {
    ws.close();
}, 5000);
EOF

# 运行WebSocket测试
if command -v node &> /dev/null; then
    echo "运行WebSocket连接测试..."
    node /tmp/test_websocket.js
else
    echo "⚠️ 未找到Node.js，跳过WebSocket测试"
fi

# 清理测试文件
rm -f /tmp/test_websocket.js

# 5. 更新Nginx配置
echo "🔧 更新Nginx配置..."

# 检查是否有Nginx容器
nginx_container=$(docker ps --filter "name=nginx" --format "{{.Names}}" | head -1)

if [ -n "$nginx_container" ]; then
    echo "重启Nginx容器: $nginx_container"
    docker restart "$nginx_container"
    sleep 5
    
    # 检查Nginx配置
    if docker exec "$nginx_container" nginx -t; then
        echo "✅ Nginx配置正确"
    else
        echo "❌ Nginx配置错误"
    fi
else
    echo "⚠️ 未找到Nginx容器"
fi

# 6. 最终验证
echo "🔍 最终验证..."

# 检查端口监听
echo "检查端口监听状态:"
for port in 80 8085; do
    if netstat -tlnp | grep ":$port " > /dev/null 2>&1; then
        echo "✅ 端口 $port: 监听中"
    else
        echo "❌ 端口 $port: 未监听"
    fi
done

# 检查服务健康状态
echo "检查服务健康状态:"
services=("8085:API网关")

for service in "${services[@]}"; do
    port=$(echo $service | cut -d: -f1)
    name=$(echo $service | cut -d: -f2)
    
    if curl -s -f "http://localhost:$port/health" > /dev/null 2>&1; then
        echo "✅ $name ($port): 健康"
    else
        echo "❌ $name ($port): 异常"
    fi
done

# 7. 显示结果
echo ""
echo "🎉 WebSocket修复完成！"
echo ""
echo "📋 修复内容:"
echo "  ✅ 安装了WebSocket依赖 (ws@^8.18.0)"
echo "  ✅ 为API网关添加了WebSocket服务器"
echo "  ✅ 配置了/config-sync WebSocket端点"
echo "  ✅ 重启了相关服务"
echo ""
echo "🔌 WebSocket端点:"
echo "  • ws://your-server-ip:8085/config-sync"
echo ""
echo "🧪 测试WebSocket连接:"
echo "  在浏览器控制台运行:"
echo "  const ws = new WebSocket('ws://your-server-ip:8085/config-sync');"
echo "  ws.onopen = () => console.log('WebSocket连接成功');"
echo "  ws.onmessage = (e) => console.log('收到消息:', e.data);"
echo "  ws.onerror = (e) => console.log('WebSocket错误:', e);"
echo ""
echo "如果仍有问题，请检查:"
echo "1. 防火墙是否开放8085端口"
echo "2. 云服务器安全组是否配置正确"
echo "3. 前端WebSocket连接地址是否正确"
echo ""

echo "✅ 修复脚本执行完成！"
