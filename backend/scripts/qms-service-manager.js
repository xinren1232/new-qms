#!/usr/bin/env node

const { spawn, exec } = require('child_process');
const path = require('path');
const fs = require('fs');
const http = require('http');

class QMSServiceManager {
  constructor() {
    this.services = new Map();
    this.config = {
      // 后端服务配置
      'chat-service': {
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
      },
      'config-service': {
        name: '配置中心服务',
        cwd: path.join(__dirname, 'backend/nodejs'),
        command: 'node',
        args: ['config-center-mock.js'],
        port: 8081,
        env: {
          NODE_ENV: 'development',
          PORT: '8081'
        }
      },
      'advanced-service': {
        name: '高级功能服务',
        cwd: path.join(__dirname, 'backend/nodejs'),
        command: 'node',
        args: ['advanced-features-service.js'],
        port: 3009,
        env: {
          NODE_ENV: 'development',
          PORT: '3009'
        }
      },
      // 前端服务配置
      'frontend-app': {
        name: '应用端',
        cwd: path.join(__dirname, 'frontend/应用端'),
        command: 'npm',
        args: ['run', 'dev'],
        port: 8080,
        env: {
          NODE_ENV: 'development'
        }
      },
      'frontend-config': {
        name: '配置端',
        cwd: path.join(__dirname, 'frontend/配置端'),
        command: 'npm',
        args: ['run', 'serve'],
        port: 8072,
        env: {
          NODE_ENV: 'development'
        }
      }
    };
  }

  // 启动单个服务
  async startService(serviceId) {
    const config = this.config[serviceId];
    if (!config) {
      throw new Error(`服务 ${serviceId} 不存在`);
    }

    if (this.services.has(serviceId)) {
      console.log(`⚠️ 服务 ${config.name} 已在运行`);
      return;
    }

    console.log(`🚀 启动服务: ${config.name}`);

    // 检查工作目录是否存在
    if (!fs.existsSync(config.cwd)) {
      throw new Error(`工作目录不存在: ${config.cwd}`);
    }

    // 设置环境变量
    const env = { ...process.env, ...config.env };

    // 启动进程
    const child = spawn(config.command, config.args, {
      cwd: config.cwd,
      env: env,
      stdio: ['pipe', 'pipe', 'pipe'],
      shell: true
    });

    // 存储进程信息
    this.services.set(serviceId, {
      process: child,
      config: config,
      startTime: new Date(),
      status: 'starting'
    });

    // 处理输出
    child.stdout.on('data', (data) => {
      const output = data.toString().trim();
      if (output) {
        console.log(`[${config.name}] ${output}`);
      }
    });

    child.stderr.on('data', (data) => {
      const output = data.toString().trim();
      if (output) {
        console.error(`[${config.name}] ❌ ${output}`);
      }
    });

    // 处理进程退出
    child.on('close', (code) => {
      console.log(`[${config.name}] 进程退出，代码: ${code}`);
      this.services.delete(serviceId);
    });

    child.on('error', (error) => {
      console.error(`[${config.name}] 启动失败: ${error.message}`);
      this.services.delete(serviceId);
    });

    // 等待服务启动
    await this.waitForService(serviceId);
  }

  // 等待服务启动完成
  async waitForService(serviceId, timeout = 30000) {
    const config = this.config[serviceId];
    const startTime = Date.now();

    return new Promise((resolve, reject) => {
      const checkService = () => {
        if (Date.now() - startTime > timeout) {
          reject(new Error(`服务 ${config.name} 启动超时`));
          return;
        }

        // 检查HTTP服务是否可用
        if (config.port) {
          const req = http.get(`http://localhost:${config.port}`, (res) => {
            console.log(`✅ 服务 ${config.name} 启动成功 (端口: ${config.port})`);
            const service = this.services.get(serviceId);
            if (service) {
              service.status = 'running';
            }
            resolve();
          });

          req.on('error', () => {
            // 服务还未启动，继续等待
            setTimeout(checkService, 1000);
          });

          req.setTimeout(1000, () => {
            req.destroy();
            setTimeout(checkService, 1000);
          });
        } else {
          // 非HTTP服务，直接标记为运行中
          setTimeout(() => {
            console.log(`✅ 服务 ${config.name} 启动成功`);
            const service = this.services.get(serviceId);
            if (service) {
              service.status = 'running';
            }
            resolve();
          }, 2000);
        }
      };

      checkService();
    });
  }

  // 停止单个服务
  async stopService(serviceId) {
    const service = this.services.get(serviceId);
    if (!service) {
      console.log(`⚠️ 服务 ${serviceId} 未运行`);
      return;
    }

    console.log(`🛑 停止服务: ${service.config.name}`);
    
    // 优雅关闭
    service.process.kill('SIGTERM');
    
    // 等待进程结束
    await new Promise((resolve) => {
      service.process.on('close', resolve);
      // 强制结束超时
      setTimeout(() => {
        service.process.kill('SIGKILL');
        resolve();
      }, 5000);
    });

    this.services.delete(serviceId);
    console.log(`✅ 服务 ${service.config.name} 已停止`);
  }

  // 启动所有服务
  async startAll() {
    console.log('🚀 启动所有QMS-AI服务...\n');

    const serviceOrder = [
      'chat-service',      // 先启动核心服务
      'config-service',    // 配置中心
      'advanced-service',  // 高级功能
      'frontend-app',      // 应用端
      'frontend-config'    // 配置端
    ];

    for (const serviceId of serviceOrder) {
      try {
        await this.startService(serviceId);
        await new Promise(resolve => setTimeout(resolve, 2000)); // 间隔2秒启动
      } catch (error) {
        console.error(`❌ 启动服务 ${serviceId} 失败: ${error.message}`);
        // 继续启动其他服务
      }
    }

    console.log('\n🎉 所有服务启动完成！');
    this.showStatus();
  }

  // 停止所有服务
  async stopAll() {
    console.log('🛑 停止所有服务...\n');

    const promises = Array.from(this.services.keys()).map(serviceId => 
      this.stopService(serviceId)
    );

    await Promise.all(promises);
    console.log('✅ 所有服务已停止');
  }

  // 重启服务
  async restartService(serviceId) {
    await this.stopService(serviceId);
    await new Promise(resolve => setTimeout(resolve, 1000));
    await this.startService(serviceId);
  }

  // 显示服务状态
  showStatus() {
    console.log('\n📊 服务状态:');
    console.log('='.repeat(60));

    if (this.services.size === 0) {
      console.log('没有运行中的服务');
      return;
    }

    for (const [serviceId, service] of this.services) {
      const uptime = Math.floor((Date.now() - service.startTime) / 1000);
      const status = service.status === 'running' ? '✅ 运行中' : '🔄 启动中';
      const port = service.config.port ? `:${service.config.port}` : '';
      
      console.log(`${status} ${service.config.name}${port} (运行时间: ${uptime}s)`);
    }

    console.log('\n📍 访问地址:');
    console.log('  🌐 应用端: http://localhost:8080');
    console.log('  ⚙️ 配置端: http://localhost:8072');
    console.log('  🤖 聊天服务: http://localhost:3004');
    console.log('  🔧 配置中心: http://localhost:8081');
    console.log('  🚀 高级功能: http://localhost:3009');
  }

  // 显示日志
  showLogs(serviceId) {
    const service = this.services.get(serviceId);
    if (!service) {
      console.log(`服务 ${serviceId} 未运行`);
      return;
    }

    console.log(`📋 ${service.config.name} 实时日志 (按Ctrl+C退出):`);
    // 日志已经在启动时输出到控制台
  }
}

// 命令行接口
async function main() {
  const manager = new QMSServiceManager();
  const args = process.argv.slice(2);
  const command = args[0];

  // 优雅退出处理
  process.on('SIGINT', async () => {
    console.log('\n🔄 正在关闭所有服务...');
    await manager.stopAll();
    process.exit(0);
  });

  try {
    switch (command) {
      case 'start':
        if (args[1]) {
          await manager.startService(args[1]);
        } else {
          await manager.startAll();
        }
        break;

      case 'stop':
        if (args[1]) {
          await manager.stopService(args[1]);
        } else {
          await manager.stopAll();
        }
        break;

      case 'restart':
        if (args[1]) {
          await manager.restartService(args[1]);
        } else {
          await manager.stopAll();
          await new Promise(resolve => setTimeout(resolve, 2000));
          await manager.startAll();
        }
        break;

      case 'status':
        manager.showStatus();
        break;

      case 'logs':
        if (args[1]) {
          manager.showLogs(args[1]);
        } else {
          console.log('请指定服务ID');
        }
        break;

      default:
        console.log('QMS-AI服务管理器');
        console.log('用法:');
        console.log('  node qms-service-manager.js start [service-id]  # 启动服务');
        console.log('  node qms-service-manager.js stop [service-id]   # 停止服务');
        console.log('  node qms-service-manager.js restart [service-id] # 重启服务');
        console.log('  node qms-service-manager.js status             # 查看状态');
        console.log('  node qms-service-manager.js logs <service-id>  # 查看日志');
        console.log('');
        console.log('可用的服务ID:');
        console.log('  chat-service, config-service, advanced-service');
        console.log('  frontend-app, frontend-config');
        break;
    }
  } catch (error) {
    console.error('❌ 操作失败:', error.message);
    process.exit(1);
  }
}

if (require.main === module) {
  main();
}

module.exports = QMSServiceManager;
