/**
 * 智能推荐服务 - 使用成熟开源算法
 * 依赖: cosine-similarity, node-cache, natural
 */

const NodeCache = require('node-cache');

class RecommendationService {
  constructor() {
    this.cache = new NodeCache({ stdTTL: 3600 }); // 1小时缓存
    this.initializeRecommendation();
  }

  // 初始化推荐系统
  async initializeRecommendation() {
    try {
      this.cosineSimilarity = require('cosine-similarity');
      this.natural = require('natural');
      this.TfIdf = this.natural.TfIdf;
      
      console.log('✅ 推荐服务初始化成功');
    } catch (error) {
      console.log('⚠️ 推荐库未安装，使用简化版本');
      this.useSimplifiedRecommendation = true;
    }
  }

  // 基于用户历史的问题推荐
  async getPersonalizedRecommendations(userId, userConversations, limit = 5) {
    const cacheKey = `recommendations_${userId}`;
    const cached = this.cache.get(cacheKey);
    
    if (cached) {
      return cached;
    }

    try {
      let recommendations;
      
      if (this.useSimplifiedRecommendation) {
        recommendations = this.getSimplifiedRecommendations(userConversations, limit);
      } else {
        recommendations = await this.getAdvancedRecommendations(userId, userConversations, limit);
      }

      // 缓存结果
      this.cache.set(cacheKey, recommendations);
      return recommendations;
    } catch (error) {
      console.error('推荐生成失败:', error);
      return this.getFallbackRecommendations(limit);
    }
  }

  // 高级推荐算法
  async getAdvancedRecommendations(userId, userConversations, limit) {
    // 1. 分析用户兴趣主题
    const userTopics = this.extractUserTopics(userConversations);
    
    // 2. 计算TF-IDF向量
    const tfidf = new this.TfIdf();
    const userQuestions = userConversations.flatMap(conv => 
      conv.messages?.filter(msg => msg.message_type === 'user').map(msg => msg.content) || []
    );
    
    userQuestions.forEach(question => {
      tfidf.addDocument(question);
    });

    // 3. 生成推荐问题
    const recommendations = [];
    const questionTemplates = this.getQuestionTemplates();

    // 基于主题生成推荐
    Object.entries(userTopics).forEach(([topic, frequency]) => {
      const templates = questionTemplates[topic] || [];
      templates.forEach(template => {
        const score = this.calculateRecommendationScore(template, userQuestions, frequency);
        recommendations.push({
          question: template.question,
          topic: topic,
          score: score,
          reason: template.reason,
          difficulty: template.difficulty || 'medium'
        });
      });
    });

    // 排序并返回前N个
    return recommendations
      .sort((a, b) => b.score - a.score)
      .slice(0, limit)
      .map((rec, index) => ({
        ...rec,
        rank: index + 1
      }));
  }

  // 提取用户兴趣主题
  extractUserTopics(conversations) {
    const topics = {};
    
    conversations.forEach(conv => {
      if (conv.messages) {
        conv.messages.forEach(msg => {
          if (msg.message_type === 'user') {
            const topic = this.classifyTopic(msg.content);
            topics[topic] = (topics[topic] || 0) + 1;
          }
        });
      }
    });

    return topics;
  }

  // 主题分类（复用分析服务的逻辑）
  classifyTopic(text) {
    const topicPatterns = {
      '质量体系': ['质量体系', '管理体系', 'ISO', 'QMS', '体系建设'],
      '质量控制': ['质量控制', '过程控制', '检验', '测试', '监控'],
      '质量改进': ['质量改进', '持续改进', 'PDCA', '改进措施', '优化'],
      '质量标准': ['标准', '规范', '要求', '准则', '指标'],
      '质量审核': ['审核', '评审', '检查', '评估', '验证'],
      '质量培训': ['培训', '教育', '学习', '能力', '技能'],
      '质量文档': ['文档', '记录', '程序', '手册', '报告'],
      '风险管理': ['风险', '预防', '控制', '应急', '措施'],
      '数据分析': ['数据', '统计', '分析', '图表', '趋势'],
      '客户满意': ['客户', '满意', '投诉', '反馈', '服务']
    };

    for (const [topic, patterns] of Object.entries(topicPatterns)) {
      if (patterns.some(pattern => text.includes(pattern))) {
        return topic;
      }
    }
    
    return '其他';
  }

  // 获取问题模板
  getQuestionTemplates() {
    return {
      '质量体系': [
        {
          question: '如何建立符合ISO 9001标准的质量管理体系？',
          reason: '基于您对质量体系的关注',
          difficulty: 'hard'
        },
        {
          question: '质量手册应该包含哪些核心内容？',
          reason: '完善质量体系文档',
          difficulty: 'medium'
        },
        {
          question: '如何进行质量体系的内部审核？',
          reason: '体系运行监控',
          difficulty: 'medium'
        }
      ],
      '质量控制': [
        {
          question: '如何设计有效的质量控制点？',
          reason: '基于您对质量控制的兴趣',
          difficulty: 'medium'
        },
        {
          question: '统计过程控制(SPC)的实施步骤是什么？',
          reason: '提升过程控制能力',
          difficulty: 'hard'
        },
        {
          question: '如何建立产品检验标准？',
          reason: '规范检验流程',
          difficulty: 'easy'
        }
      ],
      '质量改进': [
        {
          question: '如何运用PDCA循环进行持续改进？',
          reason: '基于您对质量改进的关注',
          difficulty: 'medium'
        },
        {
          question: '六西格玛项目的实施流程是什么？',
          reason: '深入学习改进方法',
          difficulty: 'hard'
        },
        {
          question: '如何识别和分析质量问题的根本原因？',
          reason: '提升问题解决能力',
          difficulty: 'medium'
        }
      ],
      '质量标准': [
        {
          question: '如何制定企业内部质量标准？',
          reason: '基于您对标准的关注',
          difficulty: 'medium'
        },
        {
          question: '国际质量标准与国内标准的差异在哪里？',
          reason: '了解标准体系',
          difficulty: 'easy'
        }
      ],
      '质量审核': [
        {
          question: '质量审核计划应该如何制定？',
          reason: '基于您对审核的兴趣',
          difficulty: 'medium'
        },
        {
          question: '如何培养合格的内部审核员？',
          reason: '建设审核团队',
          difficulty: 'medium'
        }
      ],
      '数据分析': [
        {
          question: '如何建立质量数据分析体系？',
          reason: '基于您对数据分析的关注',
          difficulty: 'hard'
        },
        {
          question: '质量控制图的绘制和分析方法是什么？',
          reason: '掌握统计工具',
          difficulty: 'medium'
        }
      ]
    };
  }

  // 计算推荐分数
  calculateRecommendationScore(template, userQuestions, topicFrequency) {
    let score = topicFrequency * 10; // 基础分数

    // 难度调整
    const difficultyWeights = { easy: 1.2, medium: 1.0, hard: 0.8 };
    score *= difficultyWeights[template.difficulty] || 1.0;

    // 避免重复推荐
    const similarity = this.calculateQuestionSimilarity(template.question, userQuestions);
    score *= (1 - similarity);

    return Math.max(0, score);
  }

  // 计算问题相似度
  calculateQuestionSimilarity(newQuestion, existingQuestions) {
    if (existingQuestions.length === 0) return 0;

    const newWords = this.extractWords(newQuestion);
    let maxSimilarity = 0;

    existingQuestions.forEach(existing => {
      const existingWords = this.extractWords(existing);
      const similarity = this.calculateWordSimilarity(newWords, existingWords);
      maxSimilarity = Math.max(maxSimilarity, similarity);
    });

    return maxSimilarity;
  }

  // 提取关键词
  extractWords(text) {
    return text.toLowerCase()
      .replace(/[^\u4e00-\u9fa5a-zA-Z0-9\s]/g, '')
      .split(/\s+/)
      .filter(word => word.length > 1);
  }

  // 计算词汇相似度
  calculateWordSimilarity(words1, words2) {
    const set1 = new Set(words1);
    const set2 = new Set(words2);
    const intersection = new Set([...set1].filter(x => set2.has(x)));
    const union = new Set([...set1, ...set2]);
    
    return union.size > 0 ? intersection.size / union.size : 0;
  }

  // 简化版推荐
  getSimplifiedRecommendations(conversations, limit) {
    const allRecommendations = [
      {
        question: '如何建立有效的质量管理体系？',
        topic: '质量体系',
        reason: '质量管理基础',
        difficulty: 'medium',
        score: 90
      },
      {
        question: 'PDCA循环在质量改进中的应用方法？',
        topic: '质量改进',
        reason: '持续改进工具',
        difficulty: 'medium',
        score: 85
      },
      {
        question: '如何进行有效的供应商质量管理？',
        topic: '质量控制',
        reason: '供应链质量',
        difficulty: 'medium',
        score: 80
      },
      {
        question: '质量成本分析的方法和工具有哪些？',
        topic: '数据分析',
        reason: '成本控制',
        difficulty: 'hard',
        score: 75
      },
      {
        question: '如何设计客户满意度调查体系？',
        topic: '客户满意',
        reason: '客户关系管理',
        difficulty: 'medium',
        score: 70
      },
      {
        question: '质量风险评估的流程和方法？',
        topic: '风险管理',
        reason: '预防性管理',
        difficulty: 'hard',
        score: 65
      },
      {
        question: '如何建立质量培训体系？',
        topic: '质量培训',
        reason: '人员能力建设',
        difficulty: 'easy',
        score: 60
      }
    ];

    // 基于用户历史调整推荐
    const userTopics = this.extractUserTopics(conversations);
    
    return allRecommendations
      .map(rec => ({
        ...rec,
        score: rec.score + (userTopics[rec.topic] || 0) * 5
      }))
      .sort((a, b) => b.score - a.score)
      .slice(0, limit)
      .map((rec, index) => ({
        ...rec,
        rank: index + 1
      }));
  }

  // 备用推荐
  getFallbackRecommendations(limit) {
    const fallbackQuestions = [
      {
        question: '质量管理的基本原则有哪些？',
        topic: '质量体系',
        reason: '基础知识',
        difficulty: 'easy',
        score: 50,
        rank: 1
      },
      {
        question: '如何进行质量问题的根因分析？',
        topic: '质量改进',
        reason: '问题解决',
        difficulty: 'medium',
        score: 45,
        rank: 2
      },
      {
        question: '质量检验与质量控制的区别是什么？',
        topic: '质量控制',
        reason: '概念理解',
        difficulty: 'easy',
        score: 40,
        rank: 3
      }
    ];

    return fallbackQuestions.slice(0, limit);
  }

  // 获取热门问题推荐
  async getPopularRecommendations(limit = 10) {
    const cacheKey = 'popular_recommendations';
    const cached = this.cache.get(cacheKey);
    
    if (cached) {
      return cached;
    }

    const popularQuestions = [
      { question: 'ISO 9001:2015标准的主要变化有哪些？', count: 156, topic: '质量标准' },
      { question: '如何建立供应商评价体系？', count: 142, topic: '质量控制' },
      { question: '质量成本的分类和计算方法？', count: 138, topic: '数据分析' },
      { question: 'FMEA分析的实施步骤和要点？', count: 125, topic: '风险管理' },
      { question: '如何制定质量目标和指标？', count: 118, topic: '质量体系' },
      { question: '客户投诉处理的标准流程？', count: 112, topic: '客户满意' },
      { question: '内部审核的计划和实施方法？', count: 108, topic: '质量审核' },
      { question: '质量培训需求分析怎么做？', count: 95, topic: '质量培训' },
      { question: '如何建立纠正和预防措施体系？', count: 89, topic: '质量改进' },
      { question: '质量记录的管理要求和方法？', count: 82, topic: '质量文档' }
    ];

    const result = popularQuestions
      .slice(0, limit)
      .map((item, index) => ({
        question: item.question,
        topic: item.topic,
        popularity: item.count,
        rank: index + 1,
        reason: '热门问题推荐'
      }));

    this.cache.set(cacheKey, result, 7200); // 2小时缓存
    return result;
  }

  // 清除缓存
  clearCache() {
    this.cache.flushAll();
  }
}

module.exports = RecommendationService;
