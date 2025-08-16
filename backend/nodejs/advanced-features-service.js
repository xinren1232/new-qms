/**
 * 高级功能集成测试服务
 * 集成：分析、推荐、协作、集成四大高级功能
 */

const express = require('express');
const cors = require('cors');
const ChatHistoryDB = require('./database/chat-history-db');
const AnalyticsService = require('./services/analytics-service');
const RecommendationService = require('./services/recommendation-service');
const CollaborationService = require('./services/collaboration-service');
const IntegrationService = require('./services/integration-service');

const app = express();

// 中间件
app.use(cors());
app.use(express.json());

console.log('🚀 启动高级功能集成服务...');

// 初始化服务
const chatHistoryDB = new ChatHistoryDB();
const analyticsService = new AnalyticsService();
const recommendationService = new RecommendationService();
const collaborationService = new CollaborationService();
const integrationService = new IntegrationService();

// 用户认证中间件
const authenticateUser = (req, res, next) => {
  const userId = req.headers['user-id'] || 'demo_user';

  // 解码可能包含中文的请求头
  let userName = req.headers['user-name'] || '演示用户';
  let userDepartment = req.headers['user-department'] || '质量管理部';

  try {
    // 尝试解码，如果失败则使用原值
    userName = decodeURIComponent(userName);
    userDepartment = decodeURIComponent(userDepartment);
  } catch (e) {
    // 如果解码失败，使用原值
    console.warn('解码用户信息失败，使用原值:', e.message);
  }

  req.user = {
    id: userId,
    username: userName,
    email: req.headers['user-email'] || `${userId}@qms.com`,
    department: userDepartment,
    role: req.headers['user-role'] || 'user',
    team_id: req.headers['team-id'] || 'team_001'
  };

  next();
};

// 健康检查
app.get('/health', (req, res) => {
  res.json({
    status: 'ok',
    timestamp: new Date().toISOString(),
    message: '高级功能集成服务运行正常',
    features: ['analytics', 'recommendation', 'collaboration', 'integration']
  });
});

// ==================== 分析功能 API ====================

// 获取对话主题分析
app.get('/api/analytics/topics', authenticateUser, async (req, res) => {
  try {
    const userId = req.user.id;
    const conversations = await chatHistoryDB.getUserConversations(userId);
    
    // 获取完整对话数据
    const fullConversations = [];
    for (const conv of conversations) {
      const fullConv = await chatHistoryDB.getConversationWithMessages(conv.id, userId);
      if (fullConv) fullConversations.push(fullConv);
    }
    
    const analysis = await analyticsService.analyzeTopics(fullConversations);
    
    res.json({
      success: true,
      data: analysis
    });
  } catch (error) {
    console.error('主题分析失败:', error);
    res.status(500).json({
      success: false,
      message: '主题分析失败',
      error: error.message
    });
  }
});

// 获取用户行为分析
app.get('/api/analytics/behavior', authenticateUser, async (req, res) => {
  try {
    const userId = req.user.id;
    const conversations = await chatHistoryDB.getUserConversations(userId);
    
    const fullConversations = [];
    for (const conv of conversations) {
      const fullConv = await chatHistoryDB.getConversationWithMessages(conv.id, userId);
      if (fullConv) fullConversations.push(fullConv);
    }
    
    const behavior = await analyticsService.analyzeUserBehavior(userId, fullConversations);
    
    res.json({
      success: true,
      data: behavior
    });
  } catch (error) {
    console.error('行为分析失败:', error);
    res.status(500).json({
      success: false,
      message: '行为分析失败',
      error: error.message
    });
  }
});

// 获取情感分析
app.get('/api/analytics/sentiment', authenticateUser, async (req, res) => {
  try {
    const userId = req.user.id;
    const conversations = await chatHistoryDB.getUserConversations(userId);
    
    const fullConversations = [];
    for (const conv of conversations) {
      const fullConv = await chatHistoryDB.getConversationWithMessages(conv.id, userId);
      if (fullConv) fullConversations.push(fullConv);
    }
    
    const sentiment = await analyticsService.analyzeSentiment(fullConversations);
    
    res.json({
      success: true,
      data: sentiment
    });
  } catch (error) {
    console.error('情感分析失败:', error);
    res.status(500).json({
      success: false,
      message: '情感分析失败',
      error: error.message
    });
  }
});

// ==================== 推荐功能 API ====================

// 获取个性化推荐
app.get('/api/recommendations/personalized', authenticateUser, async (req, res) => {
  try {
    const userId = req.user.id;
    const limit = parseInt(req.query.limit) || 5;
    
    const conversations = await chatHistoryDB.getUserConversations(userId);
    const fullConversations = [];
    for (const conv of conversations) {
      const fullConv = await chatHistoryDB.getConversationWithMessages(conv.id, userId);
      if (fullConv) fullConversations.push(fullConv);
    }
    
    const recommendations = await recommendationService.getPersonalizedRecommendations(
      userId, fullConversations, limit
    );
    
    res.json({
      success: true,
      data: recommendations
    });
  } catch (error) {
    console.error('个性化推荐失败:', error);
    res.status(500).json({
      success: false,
      message: '个性化推荐失败',
      error: error.message
    });
  }
});

// 获取热门推荐
app.get('/api/recommendations/popular', authenticateUser, async (req, res) => {
  try {
    const limit = parseInt(req.query.limit) || 10;
    const recommendations = await recommendationService.getPopularRecommendations(limit);
    
    res.json({
      success: true,
      data: recommendations
    });
  } catch (error) {
    console.error('热门推荐失败:', error);
    res.status(500).json({
      success: false,
      message: '热门推荐失败',
      error: error.message
    });
  }
});

// ==================== 协作功能 API ====================

// 分享对话
app.post('/api/collaboration/share', authenticateUser, async (req, res) => {
  try {
    const { conversation_id, share_options } = req.body;
    const userId = req.user.id;
    
    const result = await collaborationService.shareConversation(
      conversation_id, userId, share_options
    );
    
    res.json(result);
  } catch (error) {
    console.error('分享对话失败:', error);
    res.status(500).json({
      success: false,
      message: '分享对话失败',
      error: error.message
    });
  }
});

// 获取团队统计
app.get('/api/collaboration/team/stats', authenticateUser, async (req, res) => {
  try {
    const teamId = req.user.team_id;
    const timeRange = req.query.time_range || '30d';
    
    const stats = await collaborationService.getTeamStatistics(teamId, timeRange);
    
    res.json({
      success: true,
      data: stats
    });
  } catch (error) {
    console.error('获取团队统计失败:', error);
    res.status(500).json({
      success: false,
      message: '获取团队统计失败',
      error: error.message
    });
  }
});

// 获取可访问的对话
app.get('/api/collaboration/conversations', authenticateUser, async (req, res) => {
  try {
    const userId = req.user.id;
    const userRole = req.user.role;
    const teamId = req.user.team_id;
    
    const conversations = await collaborationService.getAccessibleConversations(
      userId, userRole, teamId
    );
    
    res.json({
      success: true,
      data: conversations
    });
  } catch (error) {
    console.error('获取可访问对话失败:', error);
    res.status(500).json({
      success: false,
      message: '获取可访问对话失败',
      error: error.message
    });
  }
});

// ==================== 集成功能 API ====================

// 获取相关文档
app.post('/api/integration/documents', authenticateUser, async (req, res) => {
  try {
    const { content, limit = 5 } = req.body;
    
    const documents = await integrationService.getRelatedDocuments(content, limit);
    
    res.json({
      success: true,
      data: documents
    });
  } catch (error) {
    console.error('获取相关文档失败:', error);
    res.status(500).json({
      success: false,
      message: '获取相关文档失败',
      error: error.message
    });
  }
});

// 触发工作流
app.post('/api/integration/workflow', authenticateUser, async (req, res) => {
  try {
    const { conversation_data, trigger_type } = req.body;
    
    const result = await integrationService.triggerRelatedWorkflow(
      conversation_data, trigger_type
    );
    
    res.json(result);
  } catch (error) {
    console.error('触发工作流失败:', error);
    res.status(500).json({
      success: false,
      message: '触发工作流失败',
      error: error.message
    });
  }
});

// 获取集成统计
app.get('/api/integration/stats', authenticateUser, async (req, res) => {
  try {
    const stats = await integrationService.getIntegrationStats();
    
    res.json({
      success: true,
      data: stats
    });
  } catch (error) {
    console.error('获取集成统计失败:', error);
    res.status(500).json({
      success: false,
      message: '获取集成统计失败',
      error: error.message
    });
  }
});

// ==================== 综合功能 API ====================

// 获取用户仪表板数据
app.get('/api/dashboard', authenticateUser, async (req, res) => {
  try {
    const userId = req.user.id;
    const userRole = req.user.role;
    const teamId = req.user.team_id;
    
    // 并行获取各种数据
    const [
      personalRecommendations,
      popularRecommendations,
      teamStats,
      integrationStats
    ] = await Promise.all([
      recommendationService.getPersonalizedRecommendations(userId, [], 3),
      recommendationService.getPopularRecommendations(5),
      collaborationService.getTeamStatistics(teamId, '7d'),
      integrationService.getIntegrationStats()
    ]);
    
    res.json({
      success: true,
      data: {
        user_info: req.user,
        recommendations: {
          personalized: personalRecommendations,
          popular: popularRecommendations
        },
        team_stats: teamStats,
        integration_stats: integrationStats,
        last_updated: new Date().toISOString()
      }
    });
  } catch (error) {
    console.error('获取仪表板数据失败:', error);
    res.status(500).json({
      success: false,
      message: '获取仪表板数据失败',
      error: error.message
    });
  }
});

const PORT = 3009;
app.listen(PORT, () => {
  console.log(`✅ 高级功能集成服务启动成功！`);
  console.log(`📍 服务地址: http://localhost:${PORT}`);
  console.log(`🔧 健康检查: http://localhost:${PORT}/health`);
  console.log(`📊 分析功能: http://localhost:${PORT}/api/analytics/*`);
  console.log(`🤖 推荐功能: http://localhost:${PORT}/api/recommendations/*`);
  console.log(`👥 协作功能: http://localhost:${PORT}/api/collaboration/*`);
  console.log(`🔗 集成功能: http://localhost:${PORT}/api/integration/*`);
  console.log(`📋 综合仪表板: http://localhost:${PORT}/api/dashboard`);
});
