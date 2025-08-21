/**
 * QMS-AI 系统监控服务
 * 统一监控所有后端服务的健康状态、性能指标和资源使用情况
 */

const express = require('express');
const cors = require('cors');
const axios = require('axios');
const os = require('os');
const fs = require('fs');
const path = require('path');

const app = express();
const PORT = process.env.MONITOR_PORT || 3010;

// 中间件
app.use(cors());
app.use(express.json());

// 服务配置
const SERVICES = {
  redis: { 
    name: 'Redis缓存服务', 
    port: 6379, 
    type: 'tcp',
    critical: true 
  },
  config: { 
    name: '配置中心服务', 
    port: 3003, 
    url: 'http://localhost:3003/health',
    critical: true 
  },
  chat: { 
    name: '聊天服务', 
    port: 3004, 
    url: 'http://localhost:3004/health',
    critical: true 
  },
  coze: { 
    name: 'Coze Studio服务', 
    port: 3005, 
    url: 'http://localhost:3005/health',
    critical: true 
  },
  evaluation: { 
    name: '评估服务', 
    port: 3006, 
    url: 'http://localhost:3006/health',
    critical: false 
  },
  export: { 
    name: '导出服务', 
    port: 3008, 
    url: 'http://localhost:3008/health',
    critical: false 
  },
  advanced: { 
    name: '高级功能服务', 
    port: 3009, 
    url: 'http://localhost:3009/health',
    critical: false 
  },
  frontend: { 
    name: '前端应用端', 
    port: 8081, 
    type: 'tcp',
    critical: true 
  },
  auth: { 
    name: '认证服务', 
    port: 8084, 
    url: 'http://localhost:8084/health',
    critical: true 
  },
  gateway: { 
    name: 'API网关', 
    port: 8085, 
    url: 'http://localhost:8085/health',
    critical: false 
  }
};

// 系统指标
let systemMetrics = {
  uptime: Date.now(),
  checks: 0,
  errors: 0,
  services: {},
  system: {}
};

// 检查TCP端口
async function checkTcpPort(port) {
  return new Promise((resolve) => {
    const net = require('net');
    const socket = new net.Socket();
    
    const timeout = setTimeout(() => {
      socket.destroy();
      resolve(false);
    }, 3000);
    
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

// 检查HTTP服务
async function checkHttpService(url) {
  try {
    const response = await axios.get(url, { 
      timeout: 5000,
      validateStatus: () => true 
    });
    return {
      status: response.status < 500,
      statusCode: response.status,
      responseTime: response.headers['x-response-time'] || 'N/A'
    };
  } catch (error) {
    return {
      status: false,
      error: error.message,
      responseTime: 'N/A'
    };
  }
}

// 获取系统资源信息
function getSystemInfo() {
  const totalMem = os.totalmem();
  const freeMem = os.freemem();
  const usedMem = totalMem - freeMem;
  
  return {
    platform: os.platform(),
    arch: os.arch(),
    nodeVersion: process.version,
    uptime: os.uptime(),
    loadAverage: os.loadavg(),
    memory: {
      total: Math.round(totalMem / 1024 / 1024),
      used: Math.round(usedMem / 1024 / 1024),
      free: Math.round(freeMem / 1024 / 1024),
      usage: Math.round((usedMem / totalMem) * 100)
    },
    cpu: {
      count: os.cpus().length,
      model: os.cpus()[0]?.model || 'Unknown'
    }
  };
}

// 检查磁盘空间
function getDiskInfo() {
  try {
    const stats = fs.statSync(__dirname);
    return {
      available: true,
      path: __dirname
    };
  } catch (error) {
    return {
      available: false,
      error: error.message
    };
  }
}

// 执行健康检查
async function performHealthCheck() {
  systemMetrics.checks++;
  const results = {};
  
  for (const [key, service] of Object.entries(SERVICES)) {
    const startTime = Date.now();
    let result;
    
    try {
      if (service.type === 'tcp') {
        const isOpen = await checkTcpPort(service.port);
        result = {
          status: isOpen ? 'healthy' : 'unhealthy',
          port: service.port,
          responseTime: Date.now() - startTime
        };
      } else {
        const httpResult = await checkHttpService(service.url);
        result = {
          status: httpResult.status ? 'healthy' : 'unhealthy',
          port: service.port,
          statusCode: httpResult.statusCode,
          responseTime: Date.now() - startTime,
          error: httpResult.error
        };
      }
    } catch (error) {
      result = {
        status: 'error',
        port: service.port,
        error: error.message,
        responseTime: Date.now() - startTime
      };
      systemMetrics.errors++;
    }
    
    results[key] = {
      ...result,
      name: service.name,
      critical: service.critical,
      lastCheck: new Date().toISOString()
    };
  }
  
  systemMetrics.services = results;
  systemMetrics.system = getSystemInfo();
  systemMetrics.disk = getDiskInfo();
  systemMetrics.lastCheck = new Date().toISOString();
  
  return results;
}

// API路由

// 健康检查
app.get('/health', (req, res) => {
  res.json({
    status: 'healthy',
    service: 'QMS系统监控',
    timestamp: new Date().toISOString(),
    port: PORT,
    uptime: Date.now() - systemMetrics.uptime
  });
});

// 获取所有服务状态
app.get('/api/services', async (req, res) => {
  try {
    const results = await performHealthCheck();
    res.json({
      success: true,
      data: results,
      summary: {
        total: Object.keys(SERVICES).length,
        healthy: Object.values(results).filter(s => s.status === 'healthy').length,
        unhealthy: Object.values(results).filter(s => s.status !== 'healthy').length,
        critical: Object.values(results).filter(s => s.critical && s.status !== 'healthy').length
      },
      timestamp: new Date().toISOString()
    });
  } catch (error) {
    res.status(500).json({
      success: false,
      error: error.message
    });
  }
});

// 获取系统指标
app.get('/api/metrics', (req, res) => {
  res.json({
    success: true,
    data: {
      ...systemMetrics,
      runtime: Date.now() - systemMetrics.uptime
    }
  });
});

// 获取特定服务状态
app.get('/api/services/:service', async (req, res) => {
  const { service } = req.params;
  
  if (!SERVICES[service]) {
    return res.status(404).json({
      success: false,
      error: `服务 '${service}' 不存在`
    });
  }
  
  try {
    const results = await performHealthCheck();
    res.json({
      success: true,
      data: results[service]
    });
  } catch (error) {
    res.status(500).json({
      success: false,
      error: error.message
    });
  }
});

// 系统概览
app.get('/api/overview', async (req, res) => {
  try {
    const services = await performHealthCheck();
    const system = getSystemInfo();
    
    res.json({
      success: true,
      data: {
        services: {
          total: Object.keys(SERVICES).length,
          healthy: Object.values(services).filter(s => s.status === 'healthy').length,
          critical_down: Object.values(services).filter(s => s.critical && s.status !== 'healthy').length
        },
        system: {
          uptime: system.uptime,
          memory_usage: system.memory.usage,
          cpu_count: system.cpu.count,
          load_average: system.loadAverage[0]
        },
        monitor: {
          uptime: Date.now() - systemMetrics.uptime,
          checks_performed: systemMetrics.checks,
          errors_encountered: systemMetrics.errors
        }
      }
    });
  } catch (error) {
    res.status(500).json({
      success: false,
      error: error.message
    });
  }
});

// 启动定时健康检查
setInterval(async () => {
  try {
    await performHealthCheck();
    console.log(`✅ 健康检查完成 - ${new Date().toLocaleTimeString()}`);
  } catch (error) {
    console.error(`❌ 健康检查失败:`, error.message);
  }
}, 30000); // 每30秒检查一次

// 启动服务
app.listen(PORT, () => {
  console.log(`🚀 QMS系统监控服务启动成功！`);
  console.log(`📍 服务地址: http://localhost:${PORT}`);
  console.log(`🔧 健康检查: http://localhost:${PORT}/health`);
  console.log(`📊 服务状态: http://localhost:${PORT}/api/services`);
  console.log(`📈 系统指标: http://localhost:${PORT}/api/metrics`);
  console.log(`📋 系统概览: http://localhost:${PORT}/api/overview`);
  console.log(`⏰ 自动检查间隔: 30秒`);
  
  // 执行初始检查
  performHealthCheck().then(() => {
    console.log(`✅ 初始健康检查完成`);
  });
});

module.exports = app;
