/**
 * 第一阶段部署脚本：智能分析 + 推荐功能
 */

const express = require('express');
const cors = require('cors');
const ChatHistoryDB = require('../../backend/nodejs/database/chat-history-db');
const AnalyticsService = require('../../backend/nodejs/services/analytics-service');
const RecommendationService = require('../../backend/nodejs/services/recommendation-service');

const app = express();

// 中间件
app.use(cors());
app.use(express.json());

console.log('🚀 第一阶段部署：智能分析 + 推荐功能');

// 初始化服务
const chatHistoryDB = new ChatHistoryDB();
const analyticsService = new AnalyticsService();
const recommendationService = new RecommendationService();

// 用户认证中间件
const authenticateUser = (req, res, next) => {
  const userId = req.headers['user-id'] || 'anonymous';
  let userName = req.headers['user-name'] || '匿名用户';
  let userDepartment = req.headers['user-department'] || '质量管理部';

  // 解码可能包含中文的请求头
  try {
    if (userName) userName = decodeURIComponent(userName);
    if (userDepartment) userDepartment = decodeURIComponent(userDepartment);
  } catch (e) {
    console.warn('解码用户信息失败，使用原值:', e.message);
  }

  req.user = {
    id: userId,
    username: userName,
    email: req.headers['user-email'] || `${userId}@qms.com`,
    department: userDepartment,
    role: req.headers['user-role'] || 'user'
  };

  next();
};

// 健康检查
app.get('/health', (req, res) => {
  res.json({
    status: 'ok',
    phase: 'Phase 1',
    timestamp: new Date().toISOString(),
    message: '第一阶段服务运行正常',
    features: ['analytics', 'recommendation']
  });
});

// ==================== 智能分析 API ====================

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
      data: analysis,
      phase: 'Phase 1'
    });
  } catch (error) {
    console.error('主题分析失败:', error);
    res.status(500).json({
      success: false,
      message: '主题分析失败',
      error: error.message,
      phase: 'Phase 1'
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
      data: behavior,
      phase: 'Phase 1'
    });
  } catch (error) {
    console.error('行为分析失败:', error);
    res.status(500).json({
      success: false,
      message: '行为分析失败',
      error: error.message,
      phase: 'Phase 1'
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
      data: sentiment,
      phase: 'Phase 1'
    });
  } catch (error) {
    console.error('情感分析失败:', error);
    res.status(500).json({
      success: false,
      message: '情感分析失败',
      error: error.message,
      phase: 'Phase 1'
    });
  }
});

// ==================== 智能推荐 API ====================

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
      data: recommendations,
      phase: 'Phase 1'
    });
  } catch (error) {
    console.error('个性化推荐失败:', error);
    res.status(500).json({
      success: false,
      message: '个性化推荐失败',
      error: error.message,
      phase: 'Phase 1'
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
      data: recommendations,
      phase: 'Phase 1'
    });
  } catch (error) {
    console.error('热门推荐失败:', error);
    res.status(500).json({
      success: false,
      message: '热门推荐失败',
      error: error.message,
      phase: 'Phase 1'
    });
  }
});

// 清除推荐缓存（管理接口）
app.post('/api/recommendations/clear-cache', authenticateUser, async (req, res) => {
  try {
    if (req.user.role !== 'admin') {
      return res.status(403).json({
        success: false,
        message: '权限不足'
      });
    }
    
    recommendationService.clearCache();
    
    res.json({
      success: true,
      message: '推荐缓存已清除',
      phase: 'Phase 1'
    });
  } catch (error) {
    console.error('清除缓存失败:', error);
    res.status(500).json({
      success: false,
      message: '清除缓存失败',
      error: error.message
    });
  }
});

// ==================== 第一阶段统计 API ====================

// 获取第一阶段功能统计
app.get('/api/phase1/stats', authenticateUser, async (req, res) => {
  try {
    const userId = req.user.id;
    const conversations = await chatHistoryDB.getUserConversations(userId);
    
    // 基础统计
    const totalConversations = conversations.length;
    const totalMessages = conversations.reduce((sum, conv) => sum + (conv.message_count || 0), 0);
    
    // 分析统计
    const fullConversations = [];
    for (const conv of conversations.slice(0, 10)) { // 限制数量避免性能问题
      const fullConv = await chatHistoryDB.getConversationWithMessages(conv.id, userId);
      if (fullConv) fullConversations.push(fullConv);
    }
    
    const [topics, behavior, sentiment] = await Promise.all([
      analyticsService.analyzeTopics(fullConversations),
      analyticsService.analyzeUserBehavior(userId, fullConversations),
      analyticsService.analyzeSentiment(fullConversations)
    ]);
    
    // 推荐统计
    const personalizedRecs = await recommendationService.getPersonalizedRecommendations(userId, fullConversations, 3);
    const popularRecs = await recommendationService.getPopularRecommendations(5);
    
    res.json({
      success: true,
      data: {
        phase: 'Phase 1',
        deployment_date: new Date().toISOString(),
        user_stats: {
          total_conversations: totalConversations,
          total_messages: totalMessages,
          analyzed_conversations: fullConversations.length
        },
        analytics_stats: {
          topics_count: topics.topics.length,
          keywords_count: topics.keywords.length,
          sentiment_distribution: sentiment,
          peak_activity_hour: behavior.peak_hour
        },
        recommendation_stats: {
          personalized_count: personalizedRecs.length,
          popular_count: popularRecs.length,
          cache_hit_rate: '85%' // 模拟数据
        }
      }
    });
  } catch (error) {
    console.error('获取第一阶段统计失败:', error);
    res.status(500).json({
      success: false,
      message: '获取统计失败',
      error: error.message
    });
  }
});

// 错误处理中间件
app.use((error, req, res, next) => {
  console.error('第一阶段服务错误:', error);
  res.status(500).json({
    success: false,
    message: '服务内部错误',
    phase: 'Phase 1',
    timestamp: new Date().toISOString()
  });
});

const PORT = process.env.PORT || 3010;
app.listen(PORT, () => {
  console.log(`✅ 第一阶段服务启动成功！`);
  console.log(`📍 服务地址: http://localhost:${PORT}`);
  console.log(`🔧 健康检查: http://localhost:${PORT}/health`);
  console.log(`📊 分析功能: http://localhost:${PORT}/api/analytics/*`);
  console.log(`🤖 推荐功能: http://localhost:${PORT}/api/recommendations/*`);
  console.log(`📈 阶段统计: http://localhost:${PORT}/api/phase1/stats`);
  console.log('\n🎯 第一阶段功能:');
  console.log('• 智能分析: 主题分析、行为分析、情感分析');
  console.log('• 智能推荐: 个性化推荐、热门推荐');
  console.log('\n⏭️ 下一阶段: 团队协作功能 (2周后)');
});
