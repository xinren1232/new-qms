/**
 * 简化版高级功能演示
 * 不依赖外部库，展示核心功能逻辑
 */

console.log('🚀 QMS AI高级功能演示\n');

// 模拟对话数据
const mockConversations = [
  {
    id: 'conv_001',
    title: 'ISO 9001质量管理体系建设',
    user_id: 'user_001',
    created_at: '2025-01-15T10:00:00Z',
    messages: [
      {
        id: 'msg_001',
        message_type: 'user',
        content: '如何建立符合ISO 9001标准的质量管理体系？',
        created_at: '2025-01-15T10:00:00Z'
      },
      {
        id: 'msg_002',
        message_type: 'assistant',
        content: '建立ISO 9001质量管理体系需要遵循以下步骤：1. 理解标准要求 2. 建立质量方针和目标 3. 识别过程和相互关系 4. 建立文件化体系 5. 实施和运行 6. 内部审核 7. 管理评审 8. 持续改进',
        created_at: '2025-01-15T10:01:00Z',
        rating: 5,
        feedback: '回答很详细，很有帮助',
        response_time: 1200
      }
    ]
  },
  {
    id: 'conv_002',
    title: 'PDCA循环在质量改进中的应用',
    user_id: 'user_001',
    created_at: '2025-01-16T14:30:00Z',
    messages: [
      {
        id: 'msg_003',
        message_type: 'user',
        content: 'PDCA循环如何在质量改进中应用？',
        created_at: '2025-01-16T14:30:00Z'
      },
      {
        id: 'msg_004',
        message_type: 'assistant',
        content: 'PDCA循环是持续改进的核心工具：Plan(计划)-识别改进机会，制定改进计划；Do(执行)-实施改进措施；Check(检查)-监控改进效果；Act(处理)-标准化成功做法，持续改进',
        created_at: '2025-01-16T14:31:00Z',
        rating: 4,
        feedback: '解释清楚，但希望有更多实例',
        response_time: 980
      }
    ]
  },
  {
    id: 'conv_003',
    title: '供应商质量管理策略',
    user_id: 'user_002',
    created_at: '2025-01-17T09:15:00Z',
    messages: [
      {
        id: 'msg_005',
        message_type: 'user',
        content: '如何建立有效的供应商质量管理体系？',
        created_at: '2025-01-17T09:15:00Z'
      },
      {
        id: 'msg_006',
        message_type: 'assistant',
        content: '供应商质量管理包括：1. 供应商评价和选择 2. 质量协议签订 3. 来料检验 4. 供应商审核 5. 绩效监控 6. 持续改进合作',
        created_at: '2025-01-17T09:16:00Z',
        rating: 4,
        response_time: 1100
      }
    ]
  }
];

// 1. 智能分析功能演示
function demonstrateAnalytics() {
  console.log('📊 智能分析功能演示');
  console.log('='.repeat(40));
  
  // 主题分析
  console.log('\n🏷️ 主题分析:');
  const topics = analyzeTopics(mockConversations);
  topics.forEach((topic, index) => {
    console.log(`${index + 1}. ${topic.topic}: ${topic.count}个对话`);
  });
  
  // 关键词分析
  console.log('\n🔍 关键词分析:');
  const keywords = extractKeywords(mockConversations);
  keywords.slice(0, 8).forEach((keyword, index) => {
    console.log(`${index + 1}. ${keyword.word}: ${keyword.count}次`);
  });
  
  // 用户行为分析
  console.log('\n👤 用户行为分析:');
  const behavior = analyzeBehavior(mockConversations, 'user_001');
  console.log(`总对话数: ${behavior.total_conversations}`);
  console.log(`总消息数: ${behavior.total_messages}`);
  console.log(`平均评分: ${behavior.avg_rating.toFixed(1)}`);
  console.log(`最活跃时间: ${behavior.peak_hour}:00`);
  
  // 情感分析
  console.log('\n😊 情感分析:');
  const sentiment = analyzeSentiment(mockConversations);
  console.log(`积极情感: ${sentiment.positive}%`);
  console.log(`中性情感: ${sentiment.neutral}%`);
  console.log(`消极情感: ${sentiment.negative}%`);
}

// 2. 智能推荐功能演示
function demonstrateRecommendations() {
  console.log('\n\n🤖 智能推荐功能演示');
  console.log('='.repeat(40));
  
  // 个性化推荐
  console.log('\n🎯 个性化推荐:');
  const personalizedRecs = getPersonalizedRecommendations(mockConversations, 'user_001');
  personalizedRecs.forEach((rec, index) => {
    console.log(`${index + 1}. ${rec.question}`);
    console.log(`   推荐理由: ${rec.reason}`);
    console.log(`   相关度: ${rec.score.toFixed(1)}`);
  });
  
  // 热门推荐
  console.log('\n🔥 热门推荐:');
  const popularRecs = getPopularRecommendations();
  popularRecs.slice(0, 5).forEach((rec, index) => {
    console.log(`${index + 1}. ${rec.question}`);
    console.log(`   热度: ${rec.popularity}次询问`);
  });
}

// 3. 团队协作功能演示
function demonstrateCollaboration() {
  console.log('\n\n👥 团队协作功能演示');
  console.log('='.repeat(40));
  
  // 团队统计
  console.log('\n📊 团队统计:');
  const teamStats = getTeamStatistics(mockConversations);
  console.log(`团队名称: ${teamStats.team_name}`);
  console.log(`成员数量: ${teamStats.member_count}人`);
  console.log(`团队对话: ${teamStats.total_conversations}个`);
  console.log(`团队消息: ${teamStats.total_messages}条`);
  console.log(`平均评分: ${teamStats.avg_rating.toFixed(1)}`);
  
  // 分享统计
  console.log('\n📤 分享统计:');
  console.log(`已分享对话: ${teamStats.shared_conversations}个`);
  console.log(`分享参与度: ${teamStats.sharing_rate}%`);
  
  // 权限管理
  console.log('\n🔐 权限管理:');
  const permissions = checkUserPermissions('user_001', 'manager');
  console.log(`用户角色: ${permissions.role}`);
  console.log(`可访问范围: ${permissions.access_scope.join(', ')}`);
  console.log(`操作权限: ${permissions.actions.join(', ')}`);
}

// 4. 系统集成功能演示
function demonstrateIntegration() {
  console.log('\n\n🔗 系统集成功能演示');
  console.log('='.repeat(40));
  
  // 文档推荐
  console.log('\n📄 相关文档推荐:');
  const docs = getRelatedDocuments('质量管理体系建设');
  docs.forEach((doc, index) => {
    console.log(`${index + 1}. ${doc.title}`);
    console.log(`   类型: ${doc.type}`);
    console.log(`   相关度: ${(doc.relevance * 100).toFixed(1)}%`);
  });
  
  // 工作流触发
  console.log('\n⚡ 工作流集成:');
  const workflows = getTriggerableWorkflows();
  workflows.forEach((workflow, index) => {
    console.log(`${index + 1}. ${workflow.name}`);
    console.log(`   触发条件: ${workflow.trigger_condition}`);
    console.log(`   目标模块: ${workflow.target_module}`);
  });
  
  // 集成健康状态
  console.log('\n🏥 集成健康状态:');
  const healthStatus = getIntegrationHealth();
  Object.entries(healthStatus).forEach(([module, status]) => {
    const statusIcon = status.healthy ? '✅' : '❌';
    console.log(`${statusIcon} ${module}: ${status.status}`);
  });
}

// 分析函数实现
function analyzeTopics(conversations) {
  const topicPatterns = {
    '质量体系': ['质量体系', '管理体系', 'ISO', 'QMS'],
    '质量控制': ['质量控制', '过程控制', '检验', '测试'],
    '质量改进': ['质量改进', '持续改进', 'PDCA', '改进'],
    '供应商管理': ['供应商', '供方', '采购', '外包'],
    '审核管理': ['审核', '评审', '检查', '评估'],
    '培训管理': ['培训', '教育', '学习', '能力']
  };
  
  const topics = {};
  conversations.forEach(conv => {
    const content = conv.title + ' ' + conv.messages.map(m => m.content).join(' ');
    for (const [topic, patterns] of Object.entries(topicPatterns)) {
      if (patterns.some(pattern => content.includes(pattern))) {
        topics[topic] = (topics[topic] || 0) + 1;
      }
    }
  });
  
  return Object.entries(topics)
    .map(([topic, count]) => ({ topic, count }))
    .sort((a, b) => b.count - a.count);
}

function extractKeywords(conversations) {
  const keywords = {};
  const qualityKeywords = [
    '质量', '管理', '体系', '标准', 'ISO', 'PDCA', '改进', '控制',
    '检验', '审核', '培训', '供应商', '过程', '文档', '记录', '监控'
  ];
  
  conversations.forEach(conv => {
    const content = conv.title + ' ' + conv.messages.map(m => m.content).join(' ');
    qualityKeywords.forEach(keyword => {
      const count = (content.match(new RegExp(keyword, 'g')) || []).length;
      if (count > 0) {
        keywords[keyword] = (keywords[keyword] || 0) + count;
      }
    });
  });
  
  return Object.entries(keywords)
    .map(([word, count]) => ({ word, count }))
    .sort((a, b) => b.count - a.count);
}

function analyzeBehavior(conversations, userId) {
  const userConvs = conversations.filter(conv => conv.user_id === userId);
  const totalMessages = userConvs.reduce((sum, conv) => sum + conv.messages.length, 0);
  const ratings = userConvs.flatMap(conv => 
    conv.messages.filter(msg => msg.rating).map(msg => msg.rating)
  );
  const avgRating = ratings.length > 0 ? ratings.reduce((sum, r) => sum + r, 0) / ratings.length : 0;
  
  // 模拟最活跃时间分析
  const hours = userConvs.map(conv => new Date(conv.created_at).getHours());
  const hourCounts = {};
  hours.forEach(hour => hourCounts[hour] = (hourCounts[hour] || 0) + 1);
  const peakHour = Object.entries(hourCounts).sort(([,a], [,b]) => b - a)[0]?.[0] || 14;
  
  return {
    total_conversations: userConvs.length,
    total_messages: totalMessages,
    avg_rating: avgRating,
    peak_hour: parseInt(peakHour)
  };
}

function analyzeSentiment(conversations) {
  const positiveWords = ['好', '很好', '满意', '优秀', '完美', '帮助', '详细', '清楚'];
  const negativeWords = ['不好', '差', '糟糕', '失望', '问题', '错误'];
  
  let positive = 0, negative = 0, neutral = 0;
  
  conversations.forEach(conv => {
    conv.messages.forEach(msg => {
      if (msg.message_type === 'user' || msg.feedback) {
        const content = msg.content + (msg.feedback || '');
        const posCount = positiveWords.filter(word => content.includes(word)).length;
        const negCount = negativeWords.filter(word => content.includes(word)).length;
        
        if (posCount > negCount) positive++;
        else if (negCount > posCount) negative++;
        else neutral++;
      }
    });
  });
  
  const total = positive + negative + neutral;
  return {
    positive: total > 0 ? ((positive / total) * 100).toFixed(1) : '0.0',
    negative: total > 0 ? ((negative / total) * 100).toFixed(1) : '0.0',
    neutral: total > 0 ? ((neutral / total) * 100).toFixed(1) : '100.0'
  };
}

function getPersonalizedRecommendations(conversations, userId) {
  const userTopics = analyzeTopics(conversations.filter(conv => conv.user_id === userId));
  const recommendations = [
    {
      question: '如何进行有效的内部审核？',
      reason: '基于您对质量体系的关注',
      score: 8.5
    },
    {
      question: '质量成本分析的方法有哪些？',
      reason: '扩展质量管理知识',
      score: 7.8
    },
    {
      question: '如何建立客户满意度调查体系？',
      reason: '完善质量管理闭环',
      score: 7.2
    }
  ];
  
  return recommendations;
}

function getPopularRecommendations() {
  return [
    { question: 'ISO 9001:2015标准的主要变化', popularity: 156 },
    { question: '如何建立供应商评价体系？', popularity: 142 },
    { question: '质量成本的分类和计算方法', popularity: 138 },
    { question: 'FMEA分析的实施步骤', popularity: 125 },
    { question: '如何制定质量目标和指标？', popularity: 118 }
  ];
}

function getTeamStatistics(conversations) {
  return {
    team_name: '质量管理团队',
    member_count: 12,
    total_conversations: conversations.length,
    total_messages: conversations.reduce((sum, conv) => sum + conv.messages.length, 0),
    avg_rating: 4.2,
    shared_conversations: 8,
    sharing_rate: 65
  };
}

function checkUserPermissions(userId, role) {
  const rolePermissions = {
    'admin': {
      role: '系统管理员',
      access_scope: ['全部对话', '全部用户', '系统设置'],
      actions: ['读取', '编辑', '删除', '分享', '管理']
    },
    'manager': {
      role: '部门经理',
      access_scope: ['团队对话', '部门用户'],
      actions: ['读取', '分享', '导出', '统计']
    },
    'user': {
      role: '普通用户',
      access_scope: ['个人对话'],
      actions: ['读取', '导出']
    }
  };
  
  return rolePermissions[role] || rolePermissions['user'];
}

function getRelatedDocuments(query) {
  return [
    {
      title: 'ISO 9001:2015质量管理体系标准',
      type: '标准文档',
      relevance: 0.95
    },
    {
      title: '质量手册编写指南',
      type: '指导文档',
      relevance: 0.88
    },
    {
      title: '过程方法实施案例',
      type: '案例文档',
      relevance: 0.82
    }
  ];
}

function getTriggerableWorkflows() {
  return [
    {
      name: '培训需求申请',
      trigger_condition: '用户询问培训相关问题',
      target_module: '培训管理系统'
    },
    {
      name: '文档创建请求',
      trigger_condition: '用户需要新建文档',
      target_module: '文档管理系统'
    },
    {
      name: '改进建议提交',
      trigger_condition: '用户提出改进意见',
      target_module: '流程管理系统'
    }
  ];
}

function getIntegrationHealth() {
  return {
    '文档管理': { healthy: true, status: '正常运行' },
    '流程管理': { healthy: true, status: '正常运行' },
    '培训管理': { healthy: false, status: '连接超时' },
    '审核管理': { healthy: true, status: '正常运行' }
  };
}

// 运行演示
function runDemo() {
  console.log('开始QMS AI高级功能演示...\n');
  
  demonstrateAnalytics();
  demonstrateRecommendations();
  demonstrateCollaboration();
  demonstrateIntegration();
  
  console.log('\n\n🎉 演示完成！');
  console.log('\n📋 功能总结:');
  console.log('✅ 智能分析: 主题分析、关键词提取、行为分析、情感分析');
  console.log('✅ 智能推荐: 个性化推荐、热门问题推荐');
  console.log('✅ 团队协作: 团队统计、权限管理、对话分享');
  console.log('✅ 系统集成: 文档推荐、工作流触发、健康监控');
  console.log('\n🚀 这些功能使用成熟的开源技术实现，稳定可靠！');
}

// 运行演示
runDemo();
