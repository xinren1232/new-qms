const express = require('express');
const cors = require('cors');

const app = express();
const PORT = 3004;

// 中间件
app.use(cors());
app.use(express.json());

// 模拟监控数据
const generateMockData = () => {
  const now = Date.now();
  const oneHourAgo = now - 60 * 60 * 1000;
  
  // 生成时间序列数据
  const timePoints = [];
  for (let i = 0; i < 60; i++) {
    timePoints.push(oneHourAgo + i * 60 * 1000);
  }
  
  return {
    // 总体统计
    overallStats: {
      totalRequests: Math.floor(Math.random() * 10000) + 5000,
      successRate: (Math.random() * 10 + 90).toFixed(2),
      avgResponseTime: Math.floor(Math.random() * 200) + 100,
      activeUsers: Math.floor(Math.random() * 500) + 100,
      errorCount: Math.floor(Math.random() * 50) + 10,
      systemLoad: (Math.random() * 0.5 + 0.3).toFixed(2)
    },
    
    // 请求趋势
    requestTrend: timePoints.map(time => ({
      timestamp: time,
      requests: Math.floor(Math.random() * 100) + 50,
      errors: Math.floor(Math.random() * 10)
    })),
    
    // 响应时间趋势
    responseTimeTrend: timePoints.map(time => ({
      timestamp: time,
      avgTime: Math.floor(Math.random() * 100) + 100,
      p95Time: Math.floor(Math.random() * 200) + 200,
      p99Time: Math.floor(Math.random() * 300) + 300
    })),
    
    // 系统资源
    systemResources: {
      cpu: (Math.random() * 50 + 30).toFixed(1),
      memory: (Math.random() * 40 + 40).toFixed(1),
      disk: (Math.random() * 30 + 20).toFixed(1),
      network: (Math.random() * 100 + 50).toFixed(1)
    },
    
    // 错误分析
    errorAnalysis: [
      { type: '网络超时', count: Math.floor(Math.random() * 20) + 5, percentage: '35%' },
      { type: '参数错误', count: Math.floor(Math.random() * 15) + 3, percentage: '25%' },
      { type: '权限不足', count: Math.floor(Math.random() * 10) + 2, percentage: '20%' },
      { type: '服务异常', count: Math.floor(Math.random() * 8) + 1, percentage: '15%' },
      { type: '其他', count: Math.floor(Math.random() * 5) + 1, percentage: '5%' }
    ],
    
    // API性能
    apiPerformance: [
      { 
        endpoint: '/api/auth/login', 
        avgTime: Math.floor(Math.random() * 50) + 80,
        requests: Math.floor(Math.random() * 1000) + 500,
        errors: Math.floor(Math.random() * 10) + 2
      },
      { 
        endpoint: '/api/user/profile', 
        avgTime: Math.floor(Math.random() * 30) + 60,
        requests: Math.floor(Math.random() * 800) + 300,
        errors: Math.floor(Math.random() * 5) + 1
      },
      { 
        endpoint: '/api/data/query', 
        avgTime: Math.floor(Math.random() * 100) + 150,
        requests: Math.floor(Math.random() * 600) + 200,
        errors: Math.floor(Math.random() * 15) + 5
      },
      { 
        endpoint: '/api/file/upload', 
        avgTime: Math.floor(Math.random() * 200) + 300,
        requests: Math.floor(Math.random() * 200) + 50,
        errors: Math.floor(Math.random() * 8) + 2
      }
    ]
  };
};

// API路由
app.get('/health', (req, res) => {
  console.log('🔍 监控API健康检查');
  res.json({ status: 'ok', timestamp: new Date().toISOString() });
});

app.get('/api/monitoring/overall-statistics', (req, res) => {
  console.log('📊 获取总体统计数据');
  const data = generateMockData();
  res.json({
    success: true,
    data: data.overallStats
  });
});

app.get('/api/monitoring/request-trend', (req, res) => {
  console.log('📈 获取请求趋势数据');
  const data = generateMockData();
  res.json({
    success: true,
    data: data.requestTrend
  });
});

app.get('/api/monitoring/response-time-trend', (req, res) => {
  console.log('⏱️ 获取响应时间趋势数据');
  const data = generateMockData();
  res.json({
    success: true,
    data: data.responseTimeTrend
  });
});

app.get('/api/monitoring/system-resources', (req, res) => {
  console.log('💻 获取系统资源数据');
  const data = generateMockData();
  res.json({
    success: true,
    data: data.systemResources
  });
});

app.get('/api/monitoring/error-analysis', (req, res) => {
  console.log('🚨 获取错误分析数据');
  const data = generateMockData();
  res.json({
    success: true,
    data: data.errorAnalysis
  });
});

app.get('/api/monitoring/api-performance', (req, res) => {
  console.log('⚡ 获取API性能数据');
  const data = generateMockData();
  res.json({
    success: true,
    data: data.apiPerformance
  });
});

// 启动服务
app.listen(PORT, () => {
  console.log('🚀 QMS监控API服务启动成功！');
  console.log(`📡 服务地址: http://localhost:${PORT}`);
  console.log(`🔍 健康检查: http://localhost:${PORT}/health`);
  console.log('📊 监控API端点:');
  console.log('  - 总体统计: /api/monitoring/overall-statistics');
  console.log('  - 请求趋势: /api/monitoring/request-trend');
  console.log('  - 响应时间: /api/monitoring/response-time-trend');
  console.log('  - 系统资源: /api/monitoring/system-resources');
  console.log('  - 错误分析: /api/monitoring/error-analysis');
  console.log('  - API性能: /api/monitoring/api-performance');
});
