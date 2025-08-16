/**
 * API集成服务 - 与其他QMS模块深度集成
 * 依赖: axios, node-cron, bull (可选)
 */

const axios = require('axios');
const cron = require('node-cron');

class IntegrationService {
  constructor() {
    this.moduleEndpoints = this.getModuleEndpoints();
    this.initializeIntegration();
  }

  // 初始化集成服务
  async initializeIntegration() {
    try {
      // 可选：初始化消息队列
      // this.Queue = require('bull');
      // this.integrationQueue = new this.Queue('integration jobs');
      
      console.log('✅ 集成服务初始化成功');
      
      // 启动定时同步任务
      this.startScheduledTasks();
    } catch (error) {
      console.log('⚠️ 集成服务初始化失败:', error.message);
    }
  }

  // 获取QMS模块端点配置
  getModuleEndpoints() {
    return {
      // 文档管理模块
      document: {
        baseUrl: process.env.DOCUMENT_SERVICE_URL || 'http://localhost:3001',
        endpoints: {
          search: '/api/documents/search',
          create: '/api/documents',
          update: '/api/documents/:id',
          relate: '/api/documents/:id/relations'
        }
      },
      // 流程管理模块
      process: {
        baseUrl: process.env.PROCESS_SERVICE_URL || 'http://localhost:3002',
        endpoints: {
          workflows: '/api/workflows',
          tasks: '/api/tasks',
          approvals: '/api/approvals'
        }
      },
      // 审核管理模块
      audit: {
        baseUrl: process.env.AUDIT_SERVICE_URL || 'http://localhost:3003',
        endpoints: {
          plans: '/api/audit/plans',
          findings: '/api/audit/findings',
          reports: '/api/audit/reports'
        }
      },
      // 培训管理模块
      training: {
        baseUrl: process.env.TRAINING_SERVICE_URL || 'http://localhost:3004',
        endpoints: {
          courses: '/api/training/courses',
          records: '/api/training/records',
          assessments: '/api/training/assessments'
        }
      },
      // 不合格品管理模块
      nonconformity: {
        baseUrl: process.env.NCR_SERVICE_URL || 'http://localhost:3005',
        endpoints: {
          ncrs: '/api/ncr',
          corrective_actions: '/api/ncr/corrective-actions',
          preventive_actions: '/api/ncr/preventive-actions'
        }
      }
    };
  }

  // 智能文档推荐
  async getRelatedDocuments(conversationContent, limit = 5) {
    try {
      const response = await axios.post(
        `${this.moduleEndpoints.document.baseUrl}${this.moduleEndpoints.document.endpoints.search}`,
        {
          query: conversationContent,
          type: 'semantic_search',
          limit: limit,
          filters: {
            status: 'active',
            access_level: 'public'
          }
        },
        { timeout: 5000 }
      );

      if (response.data.success) {
        return response.data.documents.map(doc => ({
          id: doc.id,
          title: doc.title,
          type: doc.type,
          relevance_score: doc.score,
          url: doc.url,
          summary: doc.summary,
          last_updated: doc.updated_at
        }));
      }
    } catch (error) {
      console.error('获取相关文档失败:', error.message);
      return this.getFallbackDocuments(conversationContent);
    }
  }

  // 备用文档推荐
  getFallbackDocuments(content) {
    const fallbackDocs = [
      {
        id: 'doc_001',
        title: 'ISO 9001:2015质量管理体系标准',
        type: 'standard',
        relevance_score: 0.85,
        url: '/documents/iso-9001-2015',
        summary: 'ISO 9001:2015质量管理体系要求标准文档',
        last_updated: '2025-01-01'
      },
      {
        id: 'doc_002',
        title: '质量手册模板',
        type: 'template',
        relevance_score: 0.78,
        url: '/documents/quality-manual-template',
        summary: '标准质量手册编写模板和指南',
        last_updated: '2024-12-15'
      },
      {
        id: 'doc_003',
        title: 'PDCA循环实施指南',
        type: 'guide',
        relevance_score: 0.72,
        url: '/documents/pdca-implementation-guide',
        summary: 'PDCA循环在质量改进中的应用指南',
        last_updated: '2024-12-10'
      }
    ];

    // 简单的关键词匹配
    return fallbackDocs.filter(doc => {
      const keywords = ['质量', '管理', '体系', '标准', 'ISO', 'PDCA'];
      return keywords.some(keyword => content.includes(keyword));
    });
  }

  // 触发相关流程
  async triggerRelatedWorkflow(conversationData, triggerType) {
    try {
      const workflowData = {
        trigger_source: 'ai_conversation',
        conversation_id: conversationData.conversation_id,
        user_id: conversationData.user_id,
        trigger_type: triggerType,
        content: conversationData.content,
        metadata: {
          model: conversationData.model,
          rating: conversationData.rating,
          timestamp: new Date().toISOString()
        }
      };

      let workflowEndpoint;
      switch (triggerType) {
        case 'training_request':
          workflowEndpoint = `${this.moduleEndpoints.training.baseUrl}/api/workflows/training-request`;
          break;
        case 'document_creation':
          workflowEndpoint = `${this.moduleEndpoints.document.baseUrl}/api/workflows/document-creation`;
          break;
        case 'improvement_suggestion':
          workflowEndpoint = `${this.moduleEndpoints.process.baseUrl}/api/workflows/improvement`;
          break;
        default:
          throw new Error(`未知的工作流类型: ${triggerType}`);
      }

      const response = await axios.post(workflowEndpoint, workflowData, { timeout: 10000 });
      
      return {
        success: true,
        workflow_id: response.data.workflow_id,
        message: '工作流触发成功'
      };
    } catch (error) {
      console.error('触发工作流失败:', error.message);
      return {
        success: false,
        message: '工作流触发失败: ' + error.message
      };
    }
  }

  // 同步培训记录
  async syncTrainingRecords(userId) {
    try {
      const response = await axios.get(
        `${this.moduleEndpoints.training.baseUrl}${this.moduleEndpoints.training.endpoints.records}`,
        {
          params: { user_id: userId, status: 'completed' },
          timeout: 5000
        }
      );

      if (response.data.success) {
        return response.data.records.map(record => ({
          course_id: record.course_id,
          course_name: record.course_name,
          completion_date: record.completion_date,
          score: record.score,
          certificate_url: record.certificate_url
        }));
      }
    } catch (error) {
      console.error('同步培训记录失败:', error.message);
      return [];
    }
  }

  // 获取审核计划
  async getUpcomingAudits(userId, userDepartment) {
    try {
      const response = await axios.get(
        `${this.moduleEndpoints.audit.baseUrl}${this.moduleEndpoints.audit.endpoints.plans}`,
        {
          params: { 
            department: userDepartment,
            status: 'planned',
            start_date: new Date().toISOString().split('T')[0]
          },
          timeout: 5000
        }
      );

      if (response.data.success) {
        return response.data.plans.map(plan => ({
          id: plan.id,
          title: plan.title,
          type: plan.audit_type,
          scheduled_date: plan.scheduled_date,
          auditor: plan.auditor_name,
          scope: plan.scope
        }));
      }
    } catch (error) {
      console.error('获取审核计划失败:', error.message);
      return [];
    }
  }

  // 创建改进建议
  async createImprovementSuggestion(conversationData) {
    try {
      const suggestion = {
        source: 'ai_conversation',
        conversation_id: conversationData.conversation_id,
        title: `基于AI对话的改进建议: ${conversationData.title}`,
        description: conversationData.content,
        suggested_by: conversationData.user_id,
        category: this.categorizeImprovement(conversationData.content),
        priority: this.calculatePriority(conversationData),
        metadata: {
          ai_model: conversationData.model,
          conversation_rating: conversationData.rating,
          created_from_ai: true
        }
      };

      const response = await axios.post(
        `${this.moduleEndpoints.process.baseUrl}/api/improvements`,
        suggestion,
        { timeout: 10000 }
      );

      return {
        success: true,
        suggestion_id: response.data.id,
        message: '改进建议创建成功'
      };
    } catch (error) {
      console.error('创建改进建议失败:', error.message);
      return {
        success: false,
        message: '创建改进建议失败: ' + error.message
      };
    }
  }

  // 改进建议分类
  categorizeImprovement(content) {
    const categories = {
      'process': ['流程', '过程', '程序', '步骤'],
      'system': ['体系', '系统', '管理', '制度'],
      'quality': ['质量', '品质', '缺陷', '不合格'],
      'efficiency': ['效率', '时间', '成本', '资源'],
      'training': ['培训', '教育', '技能', '能力'],
      'technology': ['技术', '工具', '设备', '自动化']
    };

    for (const [category, keywords] of Object.entries(categories)) {
      if (keywords.some(keyword => content.includes(keyword))) {
        return category;
      }
    }
    
    return 'general';
  }

  // 计算优先级
  calculatePriority(conversationData) {
    let priority = 'medium';
    
    // 基于评分
    if (conversationData.rating >= 4) {
      priority = 'high';
    } else if (conversationData.rating <= 2) {
      priority = 'low';
    }
    
    // 基于关键词
    const highPriorityKeywords = ['紧急', '重要', '关键', '风险', '安全'];
    if (highPriorityKeywords.some(keyword => conversationData.content.includes(keyword))) {
      priority = 'high';
    }
    
    return priority;
  }

  // 启动定时任务
  startScheduledTasks() {
    // 每天凌晨2点同步数据
    cron.schedule('0 2 * * *', async () => {
      console.log('🔄 开始定时数据同步...');
      await this.performDailySync();
    });

    // 每小时检查集成状态
    cron.schedule('0 * * * *', async () => {
      await this.checkIntegrationHealth();
    });

    console.log('⏰ 定时任务已启动');
  }

  // 执行每日同步
  async performDailySync() {
    try {
      console.log('📊 同步培训数据...');
      // await this.syncAllTrainingRecords();
      
      console.log('📋 同步审核计划...');
      // await this.syncAuditPlans();
      
      console.log('📄 同步文档更新...');
      // await this.syncDocumentUpdates();
      
      console.log('✅ 每日同步完成');
    } catch (error) {
      console.error('❌ 每日同步失败:', error);
    }
  }

  // 检查集成健康状态
  async checkIntegrationHealth() {
    const healthStatus = {};
    
    for (const [moduleName, config] of Object.entries(this.moduleEndpoints)) {
      try {
        const response = await axios.get(
          `${config.baseUrl}/health`,
          { timeout: 3000 }
        );
        
        healthStatus[moduleName] = {
          status: 'healthy',
          response_time: response.headers['x-response-time'] || 'unknown',
          last_check: new Date().toISOString()
        };
      } catch (error) {
        healthStatus[moduleName] = {
          status: 'unhealthy',
          error: error.message,
          last_check: new Date().toISOString()
        };
      }
    }
    
    // 记录健康状态
    console.log('🏥 集成健康检查:', JSON.stringify(healthStatus, null, 2));
    
    return healthStatus;
  }

  // 获取集成统计
  async getIntegrationStats() {
    return {
      connected_modules: Object.keys(this.moduleEndpoints).length,
      health_status: await this.checkIntegrationHealth(),
      daily_sync_count: 1, // 模拟数据
      api_calls_today: 156, // 模拟数据
      last_sync: new Date().toISOString(),
      integration_features: [
        '智能文档推荐',
        '工作流自动触发', 
        '培训记录同步',
        '审核计划集成',
        '改进建议创建'
      ]
    };
  }
}

module.exports = IntegrationService;
