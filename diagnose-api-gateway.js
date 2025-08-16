/**
 * API网关启动诊断脚本
 */

console.log('🔍 开始诊断API网关启动问题...');

// 检查Node.js版本
console.log('Node.js版本:', process.version);

// 检查依赖
const dependencies = [
  'express',
  'cors', 
  'compression',
  'helmet',
  'ws'
];

console.log('\n📦 检查依赖包:');
dependencies.forEach(dep => {
  try {
    require(dep);
    console.log(`✅ ${dep}: 已安装`);
  } catch (error) {
    console.log(`❌ ${dep}: 未安装 - ${error.message}`);
  }
});

// 检查端口
const net = require('net');
const PORT = 8085;

console.log(`\n🔌 检查端口 ${PORT}:`);
const server = net.createServer();

server.listen(PORT, () => {
  console.log(`✅ 端口 ${PORT} 可用`);
  server.close();
  
  // 尝试启动简单的Express服务器
  console.log('\n🚀 尝试启动简单Express服务器:');
  try {
    const express = require('express');
    const app = express();
    
    app.get('/test', (req, res) => {
      res.json({ status: 'ok', message: 'Test server running' });
    });
    
    const testServer = app.listen(PORT, () => {
      console.log(`✅ 测试服务器在端口 ${PORT} 启动成功`);
      testServer.close();
      
      // 现在尝试启动实际的API网关
      console.log('\n🌐 尝试启动实际API网关:');
      try {
        require('./api-gateway.js');
      } catch (error) {
        console.error('❌ API网关启动失败:', error.message);
        console.error('详细错误:', error.stack);
      }
    });
    
    testServer.on('error', (error) => {
      console.error(`❌ 测试服务器启动失败: ${error.message}`);
    });
    
  } catch (error) {
    console.error('❌ Express初始化失败:', error.message);
  }
});

server.on('error', (error) => {
  if (error.code === 'EADDRINUSE') {
    console.log(`❌ 端口 ${PORT} 已被占用`);
  } else {
    console.error(`❌ 端口检查失败: ${error.message}`);
  }
});
