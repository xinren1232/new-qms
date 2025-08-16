const express = require('express');
const cors = require('cors');
const multer = require('multer');
const path = require('path');
const fs = require('fs');

const app = express();
const PORT = process.env.PORT || 3006;

// 中间件配置
app.use(cors());
app.use(express.json());
app.use(express.urlencoded({ extended: true }));

// 文件上传配置
const storage = multer.diskStorage({
  destination: function (req, file, cb) {
    const uploadDir = path.join(__dirname, 'uploads/evaluation');
    if (!fs.existsSync(uploadDir)) {
      fs.mkdirSync(uploadDir, { recursive: true });
    }
    cb(null, uploadDir);
  },
  filename: function (req, file, cb) {
    const uniqueSuffix = Date.now() + '-' + Math.round(Math.random() * 1E9);
    cb(null, file.fieldname + '-' + uniqueSuffix + path.extname(file.originalname));
  }
});

const upload = multer({ 
  storage: storage,
  limits: {
    fileSize: 10 * 1024 * 1024 // 10MB
  },
  fileFilter: function (req, file, cb) {
    const allowedTypes = ['.txt', '.csv', '.json', '.xlsx'];
    const ext = path.extname(file.originalname).toLowerCase();
    if (allowedTypes.includes(ext)) {
      cb(null, true);
    } else {
      cb(new Error('不支持的文件类型'));
    }
  }
});

// 模拟数据存储
let evaluations = [];
let datasets = [
  {
    id: 'dataset1',
    name: '通用对话数据集',
    description: '包含多轮对话的通用数据集',
    size: 10000,
    type: '对话',
    createdAt: '2025-01-01'
  },
  {
    id: 'dataset2',
    name: '专业问答数据集',
    description: '专业领域的问答数据',
    size: 5000,
    type: '问答',
    createdAt: '2025-01-02'
  },
  {
    id: 'dataset3',
    name: '代码生成数据集',
    description: '代码生成和解释数据',
    size: 8000,
    type: '代码',
    createdAt: '2025-01-03'
  }
];

let models = [
  { id: 'gpt-4o', name: 'GPT-4o', provider: 'OpenAI' },
  { id: 'deepseek-chat', name: 'DeepSeek Chat', provider: 'DeepSeek' },
  { id: 'claude-3-5-sonnet', name: 'Claude 3.5 Sonnet', provider: 'Anthropic' },
  { id: 'gpt-4o-mini', name: 'GPT-4o Mini', provider: 'OpenAI' }
];

// API路由

// 健康检查
app.get('/health', (req, res) => {
  res.json({
    success: true,
    message: 'AI效果评测服务运行正常',
    timestamp: new Date().toISOString(),
    service: 'evaluation-service'
  });
});

// 获取可用数据集
app.get('/api/evaluation/datasets', (req, res) => {
  try {
    res.json({
      success: true,
      data: datasets,
      total: datasets.length
    });
  } catch (error) {
    console.error('❌ 获取数据集失败:', error);
    res.status(500).json({
      success: false,
      message: '获取数据集失败',
      error: error.message
    });
  }
});

// 获取可用模型
app.get('/api/evaluation/models', (req, res) => {
  try {
    res.json({
      success: true,
      data: models,
      total: models.length
    });
  } catch (error) {
    console.error('❌ 获取模型列表失败:', error);
    res.status(500).json({
      success: false,
      message: '获取模型列表失败',
      error: error.message
    });
  }
});

// 上传评测数据
app.post('/api/evaluation/upload', upload.single('file'), (req, res) => {
  try {
    if (!req.file) {
      return res.status(400).json({
        success: false,
        message: '请选择要上传的文件'
      });
    }

    const fileInfo = {
      id: Date.now().toString(),
      originalName: req.file.originalname,
      filename: req.file.filename,
      size: req.file.size,
      mimetype: req.file.mimetype,
      uploadedAt: new Date().toISOString()
    };

    console.log('📁 文件上传成功:', fileInfo.originalName);

    res.json({
      success: true,
      message: '文件上传成功',
      data: fileInfo
    });
  } catch (error) {
    console.error('❌ 文件上传失败:', error);
    res.status(500).json({
      success: false,
      message: '文件上传失败',
      error: error.message
    });
  }
});

// 创建评测任务
app.post('/api/evaluation/create', (req, res) => {
  try {
    const { name, datasetId, models, metrics, rounds, concurrency } = req.body;

    if (!name || !datasetId || !models || !metrics) {
      return res.status(400).json({
        success: false,
        message: '请填写完整的评测信息'
      });
    }

    const evaluation = {
      id: Date.now().toString(),
      name,
      datasetId,
      models,
      metrics,
      rounds: rounds || 3,
      concurrency: concurrency || 2,
      status: 'created',
      progress: 0,
      createdAt: new Date().toISOString(),
      updatedAt: new Date().toISOString()
    };

    evaluations.push(evaluation);

    console.log('🚀 创建评测任务:', evaluation.name);

    res.json({
      success: true,
      message: '评测任务创建成功',
      data: evaluation
    });
  } catch (error) {
    console.error('❌ 创建评测任务失败:', error);
    res.status(500).json({
      success: false,
      message: '创建评测任务失败',
      error: error.message
    });
  }
});

// 开始评测
app.post('/api/evaluation/:id/start', async (req, res) => {
  try {
    const { id } = req.params;
    const evaluation = evaluations.find(e => e.id === id);

    if (!evaluation) {
      return res.status(404).json({
        success: false,
        message: '评测任务不存在'
      });
    }

    evaluation.status = 'running';
    evaluation.startedAt = new Date().toISOString();

    console.log('▶️ 开始评测任务:', evaluation.name);

    // 模拟评测过程
    simulateEvaluation(evaluation);

    res.json({
      success: true,
      message: '评测已开始',
      data: evaluation
    });
  } catch (error) {
    console.error('❌ 开始评测失败:', error);
    res.status(500).json({
      success: false,
      message: '开始评测失败',
      error: error.message
    });
  }
});

// 获取评测列表
app.get('/api/evaluation/list', (req, res) => {
  try {
    const { status, page = 1, limit = 10 } = req.query;
    
    let filteredEvaluations = evaluations;
    if (status) {
      filteredEvaluations = evaluations.filter(e => e.status === status);
    }

    const startIndex = (page - 1) * limit;
    const endIndex = startIndex + parseInt(limit);
    const paginatedEvaluations = filteredEvaluations.slice(startIndex, endIndex);

    res.json({
      success: true,
      data: paginatedEvaluations,
      total: filteredEvaluations.length,
      page: parseInt(page),
      limit: parseInt(limit)
    });
  } catch (error) {
    console.error('❌ 获取评测列表失败:', error);
    res.status(500).json({
      success: false,
      message: '获取评测列表失败',
      error: error.message
    });
  }
});

// 获取评测详情
app.get('/api/evaluation/:id', (req, res) => {
  try {
    const { id } = req.params;
    const evaluation = evaluations.find(e => e.id === id);

    if (!evaluation) {
      return res.status(404).json({
        success: false,
        message: '评测任务不存在'
      });
    }

    res.json({
      success: true,
      data: evaluation
    });
  } catch (error) {
    console.error('❌ 获取评测详情失败:', error);
    res.status(500).json({
      success: false,
      message: '获取评测详情失败',
      error: error.message
    });
  }
});

// 获取评测结果
app.get('/api/evaluation/:id/results', (req, res) => {
  try {
    const { id } = req.params;
    const evaluation = evaluations.find(e => e.id === id);

    if (!evaluation) {
      return res.status(404).json({
        success: false,
        message: '评测任务不存在'
      });
    }

    if (evaluation.status !== 'completed') {
      return res.status(400).json({
        success: false,
        message: '评测尚未完成'
      });
    }

    // 模拟评测结果
    const results = generateMockResults(evaluation);

    res.json({
      success: true,
      data: results
    });
  } catch (error) {
    console.error('❌ 获取评测结果失败:', error);
    res.status(500).json({
      success: false,
      message: '获取评测结果失败',
      error: error.message
    });
  }
});

// 删除评测
app.delete('/api/evaluation/:id', (req, res) => {
  try {
    const { id } = req.params;
    const index = evaluations.findIndex(e => e.id === id);

    if (index === -1) {
      return res.status(404).json({
        success: false,
        message: '评测任务不存在'
      });
    }

    const deletedEvaluation = evaluations.splice(index, 1)[0];
    console.log('🗑️ 删除评测任务:', deletedEvaluation.name);

    res.json({
      success: true,
      message: '评测任务已删除',
      data: deletedEvaluation
    });
  } catch (error) {
    console.error('❌ 删除评测失败:', error);
    res.status(500).json({
      success: false,
      message: '删除评测失败',
      error: error.message
    });
  }
});

// 辅助函数

// 模拟评测过程
function simulateEvaluation(evaluation) {
  const totalSteps = 100;
  let currentStep = 0;

  const interval = setInterval(() => {
    currentStep += Math.floor(Math.random() * 10) + 1;
    evaluation.progress = Math.min(currentStep, totalSteps);
    evaluation.updatedAt = new Date().toISOString();

    if (currentStep >= totalSteps) {
      evaluation.status = 'completed';
      evaluation.completedAt = new Date().toISOString();
      evaluation.results = generateMockResults(evaluation);
      clearInterval(interval);
      console.log('✅ 评测任务完成:', evaluation.name);
    }
  }, 1000);
}

// 生成模拟评测结果
function generateMockResults(evaluation) {
  const results = {
    summary: {
      totalTests: Math.floor(Math.random() * 1000) + 500,
      avgAccuracy: (Math.random() * 20 + 80).toFixed(1) + '%',
      avgResponseTime: (Math.random() * 2 + 0.5).toFixed(1) + 's',
      successRate: (Math.random() * 10 + 90).toFixed(1) + '%'
    },
    modelResults: evaluation.models.map(modelId => {
      const model = models.find(m => m.id === modelId);
      return {
        modelId,
        modelName: model?.name || modelId,
        accuracy: (Math.random() * 20 + 80).toFixed(1) + '%',
        responseTime: (Math.random() * 2 + 0.5).toFixed(1) + 's',
        throughput: Math.floor(Math.random() * 100) + 50,
        errorRate: (Math.random() * 5).toFixed(2) + '%'
      };
    }),
    metricDetails: evaluation.metrics.map(metric => ({
      name: metric,
      value: (Math.random() * 20 + 80).toFixed(1),
      unit: metric === 'response_time' ? 'ms' : '%',
      trend: Math.random() > 0.5 ? 'up' : 'down'
    }))
  };

  return results;
}

// 启动服务
app.listen(PORT, () => {
  console.log(`🚀 AI效果评测服务启动成功`);
  console.log(`📍 服务地址: http://localhost:${PORT}`);
  console.log(`🔍 健康检查: http://localhost:${PORT}/health`);
  console.log(`📊 API文档: http://localhost:${PORT}/api/evaluation`);
  console.log('='.repeat(50));
});

module.exports = app;
