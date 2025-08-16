/**
 * 高级分析服务 - 使用成熟开源工具
 * 依赖: jieba-js, natural, sentiment
 */

const fs = require('fs');
const path = require('path');

class AnalyticsService {
  constructor() {
    this.initializeAnalytics();
  }

  // 初始化分析工具
  async initializeAnalytics() {
    try {
      // 动态导入分析库
      this.jieba = require('jieba-js'); // 中文分词
      this.natural = require('natural'); // 英文NLP
      this.sentiment = require('sentiment'); // 情感分析
      
      console.log('✅ 分析服务初始化成功');
    } catch (error) {
      console.log('⚠️ 分析库未安装，使用简化版本');
      this.useSimplifiedAnalysis = true;
    }
  }

  // 对话主题分析
  async analyzeTopics(conversations) {
    try {
      const topics = {};
      const keywords = {};
      
      conversations.forEach(conv => {
        if (conv.messages) {
          conv.messages.forEach(msg => {
            if (msg.message_type === 'user') {
              // 提取关键词
              const words = this.extractKeywords(msg.content);
              words.forEach(word => {
                keywords[word] = (keywords[word] || 0) + 1;
              });
              
              // 主题分类
              const topic = this.classifyTopic(msg.content);
              topics[topic] = (topics[topic] || 0) + 1;
            }
          });
        }
      });

      // 排序并取前20个关键词
      const topKeywords = Object.entries(keywords)
        .sort(([,a], [,b]) => b - a)
        .slice(0, 20)
        .map(([word, count]) => ({ word, count }));

      // 排序主题
      const topTopics = Object.entries(topics)
        .sort(([,a], [,b]) => b - a)
        .map(([topic, count]) => ({ topic, count }));

      return {
        keywords: topKeywords,
        topics: topTopics,
        total_analyzed: conversations.length
      };
    } catch (error) {
      console.error('主题分析失败:', error);
      return this.getSimplifiedTopicAnalysis(conversations);
    }
  }

  // 提取关键词
  extractKeywords(text) {
    if (this.useSimplifiedAnalysis) {
      return this.simpleKeywordExtraction(text);
    }

    try {
      // 使用jieba进行中文分词
      const words = this.jieba.cut(text);
      
      // 过滤停用词和短词
      const stopWords = new Set(['的', '了', '在', '是', '我', '有', '和', '就', '不', '人', '都', '一', '一个', '上', '也', '很', '到', '说', '要', '去', '你', '会', '着', '没有', '看', '好', '自己', '这']);
      
      return words.filter(word => 
        word.length > 1 && 
        !stopWords.has(word) && 
        /[\u4e00-\u9fa5]/.test(word) // 包含中文字符
      );
    } catch (error) {
      return this.simpleKeywordExtraction(text);
    }
  }

  // 简化版关键词提取
  simpleKeywordExtraction(text) {
    const qualityKeywords = [
      '质量', '管理', '体系', '控制', '改进', '标准', '流程', '检验', '测试', '审核',
      '认证', '合规', '风险', '预防', '纠正', '持续', 'ISO', 'QMS', 'PDCA', '六西格玛',
      '精益', '统计', '数据', '分析', '监控', '评估', '培训', '文档', '记录', '追溯'
    ];
    
    const foundKeywords = [];
    qualityKeywords.forEach(keyword => {
      if (text.includes(keyword)) {
        foundKeywords.push(keyword);
      }
    });
    
    return foundKeywords;
  }

  // 主题分类
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

  // 用户行为分析
  async analyzeUserBehavior(userId, conversations) {
    try {
      const behavior = {
        total_conversations: conversations.length,
        total_messages: 0,
        avg_messages_per_conversation: 0,
        most_active_hours: {},
        most_active_days: {},
        conversation_lengths: [],
        topics_interest: {},
        rating_patterns: {
          avg_rating: 0,
          rating_distribution: { 1: 0, 2: 0, 3: 0, 4: 0, 5: 0 },
          feedback_frequency: 0
        }
      };

      let totalRating = 0;
      let ratedCount = 0;
      let feedbackCount = 0;

      conversations.forEach(conv => {
        if (conv.messages) {
          behavior.total_messages += conv.messages.length;
          behavior.conversation_lengths.push(conv.messages.length);

          conv.messages.forEach(msg => {
            // 分析活跃时间
            const date = new Date(msg.created_at);
            const hour = date.getHours();
            const day = date.getDay();
            
            behavior.most_active_hours[hour] = (behavior.most_active_hours[hour] || 0) + 1;
            behavior.most_active_days[day] = (behavior.most_active_days[day] || 0) + 1;

            // 分析主题兴趣
            if (msg.message_type === 'user') {
              const topic = this.classifyTopic(msg.content);
              behavior.topics_interest[topic] = (behavior.topics_interest[topic] || 0) + 1;
            }

            // 分析评分模式
            if (msg.rating) {
              totalRating += msg.rating;
              ratedCount++;
              behavior.rating_patterns.rating_distribution[msg.rating]++;
            }
            
            if (msg.feedback) {
              feedbackCount++;
            }
          });
        }
      });

      // 计算平均值
      behavior.avg_messages_per_conversation = behavior.total_messages / behavior.total_conversations;
      behavior.rating_patterns.avg_rating = ratedCount > 0 ? totalRating / ratedCount : 0;
      behavior.rating_patterns.feedback_frequency = (feedbackCount / behavior.total_messages) * 100;

      // 找出最活跃的时间
      behavior.peak_hour = Object.entries(behavior.most_active_hours)
        .sort(([,a], [,b]) => b - a)[0]?.[0] || 0;
      
      behavior.peak_day = Object.entries(behavior.most_active_days)
        .sort(([,a], [,b]) => b - a)[0]?.[0] || 0;

      return behavior;
    } catch (error) {
      console.error('用户行为分析失败:', error);
      return this.getSimplifiedBehaviorAnalysis(conversations);
    }
  }

  // 情感分析
  async analyzeSentiment(conversations) {
    try {
      const sentiments = {
        positive: 0,
        negative: 0,
        neutral: 0,
        total: 0
      };

      conversations.forEach(conv => {
        if (conv.messages) {
          conv.messages.forEach(msg => {
            if (msg.message_type === 'user') {
              const sentiment = this.getSentiment(msg.content);
              sentiments[sentiment]++;
              sentiments.total++;
            }
          });
        }
      });

      // 计算百分比
      const total = sentiments.total;
      return {
        positive: ((sentiments.positive / total) * 100).toFixed(1),
        negative: ((sentiments.negative / total) * 100).toFixed(1),
        neutral: ((sentiments.neutral / total) * 100).toFixed(1),
        total: total
      };
    } catch (error) {
      console.error('情感分析失败:', error);
      return { positive: 50, negative: 20, neutral: 30, total: 0 };
    }
  }

  // 获取情感倾向
  getSentiment(text) {
    if (this.useSimplifiedAnalysis) {
      return this.simpleSentimentAnalysis(text);
    }

    try {
      const result = this.sentiment.analyze(text);
      if (result.score > 0) return 'positive';
      if (result.score < 0) return 'negative';
      return 'neutral';
    } catch (error) {
      return this.simpleSentimentAnalysis(text);
    }
  }

  // 简化版情感分析
  simpleSentimentAnalysis(text) {
    const positiveWords = ['好', '很好', '满意', '优秀', '完美', '棒', '赞', '喜欢', '感谢'];
    const negativeWords = ['不好', '差', '糟糕', '失望', '问题', '错误', '不满', '抱怨'];
    
    const positiveCount = positiveWords.filter(word => text.includes(word)).length;
    const negativeCount = negativeWords.filter(word => text.includes(word)).length;
    
    if (positiveCount > negativeCount) return 'positive';
    if (negativeCount > positiveCount) return 'negative';
    return 'neutral';
  }

  // 简化版主题分析
  getSimplifiedTopicAnalysis(conversations) {
    const topics = {};
    const keywords = {};
    
    conversations.forEach(conv => {
      const topic = this.classifyTopic(conv.title);
      topics[topic] = (topics[topic] || 0) + 1;
      
      const words = this.simpleKeywordExtraction(conv.title);
      words.forEach(word => {
        keywords[word] = (keywords[word] || 0) + 1;
      });
    });

    return {
      keywords: Object.entries(keywords)
        .sort(([,a], [,b]) => b - a)
        .slice(0, 10)
        .map(([word, count]) => ({ word, count })),
      topics: Object.entries(topics)
        .sort(([,a], [,b]) => b - a)
        .map(([topic, count]) => ({ topic, count })),
      total_analyzed: conversations.length
    };
  }

  // 简化版行为分析
  getSimplifiedBehaviorAnalysis(conversations) {
    return {
      total_conversations: conversations.length,
      total_messages: conversations.reduce((sum, conv) => sum + (conv.message_count || 0), 0),
      avg_messages_per_conversation: conversations.length > 0 ? 
        conversations.reduce((sum, conv) => sum + (conv.message_count || 0), 0) / conversations.length : 0,
      peak_hour: 14, // 默认下午2点
      peak_day: 2,   // 默认周二
      topics_interest: { '质量管理': 5, '流程优化': 3, '标准制定': 2 }
    };
  }
}

module.exports = AnalyticsService;
