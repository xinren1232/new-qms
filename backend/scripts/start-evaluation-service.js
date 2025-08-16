#!/usr/bin/env node

/**
 * QMS AI效果评测服务启动脚本
 */

const { spawn } = require('child_process');
const path = require('path');
const fs = require('fs');

console.log('🚀 启动QMS AI效果评测服务...\n');

// 检查Node.js版本
const nodeVersion = process.version;
const majorVersion = parseInt(nodeVersion.slice(1).split('.')[0]);

if (majorVersion < 14) {
  console.error('❌ 需要Node.js 14或更高版本');
  console.error(`当前版本: ${nodeVersion}`);
  process.exit(1);
}

// 服务配置
const services = [
  {
    name: '评测服务',
    script: path.join(__dirname, '../backend/nodejs/evaluation-service.js'),
    port: 3006,
    env: {
      NODE_ENV: 'development',
      PORT: 3006
    }
  }
];

// 检查端口是否被占用
function checkPort(port) {
  return new Promise((resolve) => {
    const net = require('net');
    const server = net.createServer();
    
    server.listen(port, () => {
      server.once('close', () => {
        resolve(true); // 端口可用
      });
      server.close();
    });
    
    server.on('error', () => {
      resolve(false); // 端口被占用
    });
  });
}

// 启动服务
async function startService(service) {
  console.log(`📍 检查端口 ${service.port}...`);
  
  const isPortAvailable = await checkPort(service.port);
  if (!isPortAvailable) {
    console.log(`⚠️  端口 ${service.port} 已被占用，${service.name}可能已在运行`);
    return null;
  }

  console.log(`🔧 启动${service.name}...`);
  
  const child = spawn('node', [service.script], {
    env: { ...process.env, ...service.env },
    stdio: ['inherit', 'pipe', 'pipe']
  });

  // 处理输出
  child.stdout.on('data', (data) => {
    const output = data.toString().trim();
    if (output) {
      console.log(`[${service.name}] ${output}`);
    }
  });

  child.stderr.on('data', (data) => {
    const output = data.toString().trim();
    if (output) {
      console.error(`[${service.name}] ❌ ${output}`);
    }
  });

  child.on('close', (code) => {
    if (code !== 0) {
      console.error(`[${service.name}] ❌ 进程退出，代码: ${code}`);
    } else {
      console.log(`[${service.name}] ✅ 进程正常退出`);
    }
  });

  child.on('error', (error) => {
    console.error(`[${service.name}] ❌ 启动失败:`, error.message);
  });

  return child;
}

// 主启动函数
async function main() {
  const processes = [];

  try {
    // 检查必要文件
    for (const service of services) {
      if (!fs.existsSync(service.script)) {
        console.error(`❌ 服务脚本不存在: ${service.script}`);
        process.exit(1);
      }
    }

    // 启动所有服务
    for (const service of services) {
      const child = await startService(service);
      if (child) {
        processes.push({ name: service.name, process: child });
        
        // 等待一下再启动下一个服务
        await new Promise(resolve => setTimeout(resolve, 1000));
      }
    }

    if (processes.length === 0) {
      console.log('⚠️  没有新服务需要启动');
      return;
    }

    console.log('\n🎉 所有服务启动完成！');
    console.log('\n📋 服务列表:');
    services.forEach(service => {
      console.log(`  • ${service.name}: http://localhost:${service.port}`);
    });

    console.log('\n🔍 健康检查:');
    services.forEach(service => {
      console.log(`  • ${service.name}: http://localhost:${service.port}/health`);
    });

    console.log('\n💡 使用说明:');
    console.log('  • 按 Ctrl+C 停止所有服务');
    console.log('  • 查看日志输出了解服务状态');
    console.log('  • 访问健康检查端点验证服务运行状态');

    // 优雅关闭处理
    process.on('SIGINT', () => {
      console.log('\n🛑 收到停止信号，正在关闭服务...');
      
      processes.forEach(({ name, process }) => {
        console.log(`📴 停止${name}...`);
        process.kill('SIGTERM');
      });

      setTimeout(() => {
        console.log('🔚 强制退出');
        process.exit(0);
      }, 5000);
    });

    process.on('SIGTERM', () => {
      console.log('\n🛑 收到终止信号，正在关闭服务...');
      processes.forEach(({ process }) => {
        process.kill('SIGTERM');
      });
    });

  } catch (error) {
    console.error('❌ 启动失败:', error.message);
    process.exit(1);
  }
}

// 运行主函数
main().catch(error => {
  console.error('❌ 未处理的错误:', error);
  process.exit(1);
});
