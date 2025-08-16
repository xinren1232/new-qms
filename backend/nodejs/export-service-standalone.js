const express = require('express');
const cors = require('cors');
const ExportService = require('./services/export-service');
const ChatHistoryDB = require('./database/chat-history-db');

const app = express();
const PORT = process.env.EXPORT_SERVICE_PORT || 3008;

// 中间件
app.use(cors());
app.use(express.json({ limit: '50mb' }));
app.use(express.urlencoded({ extended: true, limit: '50mb' }));

// 初始化服务
const exportService = new ExportService();
const chatHistoryDB = new ChatHistoryDB();

// 健康检查
app.get('/health', (req, res) => {
  res.json({
    status: 'healthy',
    service: 'QMS导出服务',
    timestamp: new Date().toISOString(),
    port: PORT
  });
});

// 导出对话记录
app.post('/api/export', async (req, res) => {
  try {
    const { format, conversation_ids, user_id, options = {} } = req.body;

    // 验证导出格式
    const supportedFormats = ['pdf', 'word', 'excel', 'markdown', 'json'];
    if (!supportedFormats.includes(format)) {
      return res.status(400).json({
        success: false,
        message: `不支持的导出格式: ${format}。支持的格式: ${supportedFormats.join(', ')}`
      });
    }

    // 获取对话记录
    let conversations = [];
    if (conversation_ids && conversation_ids.length > 0) {
      // 获取指定对话
      for (const convId of conversation_ids) {
        const conv = await chatHistoryDB.getConversation(convId, user_id);
        if (conv) {
          conversations.push(conv);
        }
      }
    } else if (user_id) {
      // 获取用户所有对话
      conversations = await chatHistoryDB.getUserConversations(user_id);
    } else {
      return res.status(400).json({
        success: false,
        message: '请提供conversation_ids或user_id'
      });
    }

    if (conversations.length === 0) {
      return res.status(404).json({
        success: false,
        message: '未找到要导出的对话记录'
      });
    }

    // 执行导出
    let result;
    const exportOptions = {
      ...options,
      user_info: { id: user_id, username: options.username || 'Unknown' }
    };

    switch (format) {
      case 'pdf':
        result = await exportService.exportToPDF(conversations, exportOptions);
        break;
      case 'word':
        result = await exportService.exportToWord(conversations, exportOptions);
        break;
      case 'excel':
        result = await exportService.exportToExcel(conversations, exportOptions);
        break;
      case 'markdown':
        result = await exportService.exportToMarkdown(conversations, exportOptions);
        break;
      case 'json':
        result = await exportService.exportToJSON(conversations, exportOptions);
        break;
    }

    res.json({
      success: true,
      message: '导出成功',
      data: result
    });

  } catch (error) {
    console.error('导出失败:', error);
    res.status(500).json({
      success: false,
      message: '导出失败',
      error: error.message
    });
  }
});

// 下载导出文件
app.get('/api/download/:filename', (req, res) => {
  try {
    const { filename } = req.params;
    const filepath = require('path').join(__dirname, 'exports', filename);

    // 验证文件存在
    if (!require('fs').existsSync(filepath)) {
      return res.status(404).json({
        success: false,
        message: '文件不存在'
      });
    }

    // 设置下载头
    res.download(filepath, filename, (err) => {
      if (err) {
        console.error('文件下载失败:', err);
        res.status(500).json({
          success: false,
          message: '文件下载失败'
        });
      }
    });

  } catch (error) {
    console.error('下载文件错误:', error);
    res.status(500).json({
      success: false,
      message: '下载文件失败',
      error: error.message
    });
  }
});

// 清理过期文件
app.post('/api/cleanup', (req, res) => {
  try {
    const { maxAgeHours = 24 } = req.body;
    exportService.cleanupOldExports(maxAgeHours);
    
    res.json({
      success: true,
      message: '清理完成'
    });
  } catch (error) {
    console.error('清理失败:', error);
    res.status(500).json({
      success: false,
      message: '清理失败',
      error: error.message
    });
  }
});

// 启动服务
app.listen(PORT, () => {
  console.log(`🚀 QMS导出服务启动成功！`);
  console.log(`📍 服务地址: http://localhost:${PORT}`);
  console.log(`🔧 健康检查: http://localhost:${PORT}/health`);
  console.log(`📤 导出API: http://localhost:${PORT}/api/export`);
  console.log(`📥 下载API: http://localhost:${PORT}/api/download/:filename`);
  console.log(`🧹 清理API: http://localhost:${PORT}/api/cleanup`);
});

module.exports = app;
