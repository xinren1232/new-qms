/**
 * QMS-AI 统一服务启动脚本
 * 按正确顺序启动所有后端服务，并提供状态监控
 */

const { spawn } = require('child_process');
const axios = require('axios');
const path = require('path');

// 服务配置（按启动顺序）
const SERVICES = [
  {
    name: 'Redis缓存服务',
    command: 'redis-server',
    args: [],
    cwd: path.join(__dirname, '../../'),
    port: 6379,
    type: 'tcp',
    critical: true,
    startupTime: 3000
  },
  {
    name: '配置中心服务',
    command: 'node',
    args: ['lightweight-config-service.js'],
    cwd: __dirname,
    port: 3003,
    healthUrl: 'http://localhost:3003/health',
    critical: true,
    startupTime: 5000
  },
  {
    name: '聊天服务',
    command: 'node',
    args: ['chat-service.js'],
    cwd: __dirname,
    port: 3004,
    healthUrl: 'http://localhost:3004/health',
    critical: true,
    startupTime: 5000
  },
  {
    name: 'Coze Studio服务',
    command: 'node',
    args: ['coze-studio-service.js'],
    cwd: __dirname,
    port: 3005,
    healthUrl: 'http://localhost:3005/health',
    critical: true,
    startupTime: 8000
  },
  {
    name: '认证服务',
    command: 'node',
    args: ['auth-service.js'],
    cwd: __dirname,
    port: 8084,
    healthUrl: 'http://localhost:8084/health',
    critical: true,
    startupTime: 3000
  },
  {
    name: '评估服务',
    command: 'node',
    args: ['evaluation-service.js'],
    cwd: __dirname,
    port: 3006,
    healthUrl: 'http://localhost:3006/health',
    critical: false,
    startupTime: 3000,
    optional: true
  },
  {
    name: '导出服务',
    command: 'node',
    args: ['export-service-standalone.js'],
    cwd: __dirname,
    port: 3008,
    healthUrl: 'http://localhost:3008/health',
    critical: false,
    startupTime: 5000
  },
  {
    name: '高级功能服务',
    command: 'node',
    args: ['advanced-features-service.js'],
    cwd: __dirname,
    port: 3009,
    healthUrl: 'http://localhost:3009/health',
    critical: false,
    startupTime: 8000
  },
  {
    name: 'API网关',
    command: 'node',
    args: ['api-gateway.js'],
    cwd: __dirname,
    port: 8085,
    healthUrl: 'http://localhost:8085/health',
    critical: false,
    startupTime: 3000
  },
  {
    name: '系统监控服务',
    command: 'node',
    args: ['system-monitor.js'],
    cwd: __dirname,
    port: 3010,
    healthUrl: 'http://localhost:3010/health',
    critical: false,
    startupTime: 3000
  }
];

// 运行中的进程
const runningProcesses = new Map();
const serviceStatus = new Map();

// 颜色输出
const colors = {
  reset: '\x1b[0m',
  red: '\x1b[31m',
  green: '\x1b[32m',
  yellow: '\x1b[33m',
  blue: '\x1b[34m',
  magenta: '\x1b[35m',
  cyan: '\x1b[36m'
};

function log(message, color = 'reset') {
  console.log(`${colors[color]}${message}${colors.reset}`);
}

// 检查端口是否开放
function checkPort(port) {
  return new Promise((resolve) => {
    const net = require('net');
    const socket = new net.Socket();
    
    const timeout = setTimeout(() => {
      socket.destroy();
      resolve(false);
    }, 2000);
    
    socket.connect(port, 'localhost', () => {
      clearTimeout(timeout);
      socket.destroy();
      resolve(true);
    });
    
    socket.on('error', () => {
      clearTimeout(timeout);
      resolve(false);
    });
  });
}

// 检查HTTP健康状态
async function checkHealth(url) {
  try {
    const response = await axios.get(url, { timeout: 3000 });
    return response.status === 200;
  } catch (error) {
    return false;
  }
}

// 等待服务启动
async function waitForService(service) {
  const maxAttempts = 20;
  let attempts = 0;
  
  log(`⏳ 等待 ${service.name} 启动...`, 'yellow');
  
  while (attempts < maxAttempts) {
    attempts++;
    
    if (service.type === 'tcp') {
      const isOpen = await checkPort(service.port);
      if (isOpen) {
        log(`✅ ${service.name} 启动成功 (端口 ${service.port})`, 'green');
        return true;
      }
    } else if (service.healthUrl) {
      const isHealthy = await checkHealth(service.healthUrl);
      if (isHealthy) {
        log(`✅ ${service.name} 启动成功 (端口 ${service.port})`, 'green');
        return true;
      }
    }
    
    await new Promise(resolve => setTimeout(resolve, 1000));
  }
  
  log(`❌ ${service.name} 启动超时`, 'red');
  return false;
}

// 启动单个服务
async function startService(service) {
  log(`🚀 启动 ${service.name}...`, 'cyan');
  
  try {
    const process = spawn(service.command, service.args, {
      cwd: service.cwd,
      stdio: ['ignore', 'pipe', 'pipe'],
      shell: true
    });
    
    runningProcesses.set(service.name, process);
    
    // 处理输出
    process.stdout.on('data', (data) => {
      const output = data.toString().trim();
      if (output) {
        log(`[${service.name}] ${output}`, 'blue');
      }
    });
    
    process.stderr.on('data', (data) => {
      const output = data.toString().trim();
      if (output && !output.includes('Warning') && !output.includes('Deprecation')) {
        log(`[${service.name}] ERROR: ${output}`, 'red');
      }
    });
    
    process.on('exit', (code) => {
      if (code !== 0) {
        log(`❌ ${service.name} 异常退出 (代码: ${code})`, 'red');
        serviceStatus.set(service.name, 'failed');
      }
      runningProcesses.delete(service.name);
    });
    
    // 等待启动
    await new Promise(resolve => setTimeout(resolve, service.startupTime));
    
    // 验证启动状态
    const isRunning = await waitForService(service);
    serviceStatus.set(service.name, isRunning ? 'running' : 'failed');
    
    return isRunning;
    
  } catch (error) {
    log(`❌ 启动 ${service.name} 失败: ${error.message}`, 'red');
    serviceStatus.set(service.name, 'failed');
    return false;
  }
}

// 启动所有服务
async function startAllServices() {
  log('🎯 QMS-AI 系统启动中...', 'magenta');
  log('=' * 50, 'cyan');
  
  let successCount = 0;
  let criticalFailures = 0;
  
  for (const service of SERVICES) {
    // 跳过可选服务如果启动失败
    if (service.optional) {
      try {
        const success = await startService(service);
        if (success) successCount++;
      } catch (error) {
        log(`⚠️ 可选服务 ${service.name} 跳过: ${error.message}`, 'yellow');
      }
    } else {
      const success = await startService(service);
      if (success) {
        successCount++;
      } else if (service.critical) {
        criticalFailures++;
      }
    }
    
    // 服务间启动间隔
    await new Promise(resolve => setTimeout(resolve, 2000));
  }
  
  // 启动总结
  log('=' * 50, 'cyan');
  log(`📊 启动总结:`, 'magenta');
  log(`✅ 成功启动: ${successCount}/${SERVICES.length}`, 'green');
  log(`❌ 关键服务失败: ${criticalFailures}`, criticalFailures > 0 ? 'red' : 'green');
  
  if (criticalFailures === 0) {
    log('🎉 QMS-AI 系统启动完成！', 'green');
    log('🌐 访问地址:', 'cyan');
    log('  - 前端应用: http://localhost:8081', 'blue');
    log('  - API网关: http://localhost:8085', 'blue');
    log('  - 系统监控: http://localhost:3010', 'blue');
  } else {
    log('⚠️ 系统启动不完整，请检查失败的关键服务', 'yellow');
  }
  
  return criticalFailures === 0;
}

// 优雅关闭
function gracefulShutdown() {
  log('🛑 正在关闭所有服务...', 'yellow');
  
  for (const [name, process] of runningProcesses) {
    log(`🔄 关闭 ${name}...`, 'yellow');
    process.kill('SIGTERM');
  }
  
  setTimeout(() => {
    for (const [name, process] of runningProcesses) {
      log(`💀 强制关闭 ${name}...`, 'red');
      process.kill('SIGKILL');
    }
    process.exit(0);
  }, 10000);
}

// 信号处理
process.on('SIGINT', gracefulShutdown);
process.on('SIGTERM', gracefulShutdown);

// 主函数
async function main() {
  try {
    const success = await startAllServices();
    
    if (success) {
      log('🔄 系统运行中... (按 Ctrl+C 退出)', 'green');
      
      // 保持进程运行
      setInterval(() => {
        const runningCount = runningProcesses.size;
        if (runningCount === 0) {
          log('⚠️ 所有服务已停止', 'yellow');
          process.exit(1);
        }
      }, 30000);
    } else {
      log('❌ 系统启动失败', 'red');
      process.exit(1);
    }
  } catch (error) {
    log(`💥 启动过程中发生错误: ${error.message}`, 'red');
    process.exit(1);
  }
}

// 如果直接运行此脚本
if (require.main === module) {
  main();
}

module.exports = {
  startAllServices,
  SERVICES,
  runningProcesses,
  serviceStatus
};
