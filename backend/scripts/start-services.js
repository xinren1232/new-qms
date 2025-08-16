#!/usr/bin/env node

const { spawn } = require('child_process');
const path = require('path');
const http = require('http');

console.log('🚀 启动QMS-AI服务管理器');
console.log('Node.js版本:', process.version);
console.log('当前目录:', process.cwd());

// 服务配置
const services = [
  {
    name: '聊天服务',
    cwd: path.join(__dirname, 'backend/nodejs'),
    command: 'node',
    args: ['chat-service.js'],
    port: 3004,
    env: {
      NODE_ENV: 'development',
      PORT: '3004',
      DB_TYPE: 'sqlite',
      CACHE_ENABLED: 'false'
    }
  }
];

// 启动服务函数
function startService(service) {
  return new Promise((resolve, reject) => {
    console.log(`\n🚀 启动${service.name}...`);
    console.log(`📁 工作目录: ${service.cwd}`);
    console.log(`🔧 命令: ${service.command} ${service.args.join(' ')}`);
    
    // 设置环境变量
    const env = { ...process.env, ...service.env };
    
    // 启动进程
    const child = spawn(service.command, service.args, {
      cwd: service.cwd,
      env: env,
      stdio: ['pipe', 'pipe', 'pipe'],
      shell: false
    });

    let output = '';
    let hasStarted = false;

    // 处理输出
    child.stdout.on('data', (data) => {
      const text = data.toString();
      output += text;
      console.log(`[${service.name}] ${text.trim()}`);
      
      // 检查是否启动成功
      if (text.includes('Server running') || text.includes('listening') || text.includes('started')) {
        hasStarted = true;
      }
    });

    child.stderr.on('data', (data) => {
      const text = data.toString();
      console.error(`[${service.name}] ❌ ${text.trim()}`);
    });

    // 处理进程退出
    child.on('close', (code) => {
      if (code === 0 || hasStarted) {
        console.log(`✅ ${service.name} 启动成功`);
        resolve(child);
      } else {
        console.error(`❌ ${service.name} 启动失败，退出代码: ${code}`);
        reject(new Error(`${service.name} 启动失败`));
      }
    });

    child.on('error', (error) => {
      console.error(`❌ ${service.name} 启动错误:`, error.message);
      reject(error);
    });

    // 等待5秒后检查端口
    setTimeout(() => {
      if (service.port) {
        checkPort(service.port, (isOpen) => {
          if (isOpen) {
            console.log(`✅ ${service.name} 端口${service.port}已开放`);
            hasStarted = true;
            resolve(child);
          } else if (!hasStarted) {
            console.log(`⏳ ${service.name} 仍在启动中...`);
          }
        });
      } else {
        resolve(child);
      }
    }, 5000);
  });
}

// 检查端口是否开放
function checkPort(port, callback) {
  const req = http.get(`http://localhost:${port}`, (res) => {
    callback(true);
  });

  req.on('error', () => {
    callback(false);
  });

  req.setTimeout(1000, () => {
    req.destroy();
    callback(false);
  });
}

// 主函数
async function main() {
  try {
    console.log('\n📋 准备启动以下服务:');
    services.forEach(service => {
      console.log(`  - ${service.name} (端口: ${service.port || 'N/A'})`);
    });

    console.log('\n🚀 开始启动服务...');
    
    for (const service of services) {
      try {
        await startService(service);
        console.log(`✅ ${service.name} 启动完成`);
        
        // 等待2秒再启动下一个服务
        await new Promise(resolve => setTimeout(resolve, 2000));
      } catch (error) {
        console.error(`❌ ${service.name} 启动失败:`, error.message);
        // 继续启动其他服务
      }
    }

    console.log('\n🎉 服务启动流程完成！');
    console.log('\n📍 访问地址:');
    console.log('  🤖 聊天服务: http://localhost:3004');
    console.log('  🏥 健康检查: http://localhost:3004/health');
    
    console.log('\n💡 提示: 按Ctrl+C停止所有服务');
    
    // 保持进程运行
    process.on('SIGINT', () => {
      console.log('\n🔄 正在关闭所有服务...');
      process.exit(0);
    });

  } catch (error) {
    console.error('❌ 启动失败:', error.message);
    process.exit(1);
  }
}

// 运行主函数
main();
