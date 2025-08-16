/**
 * CrewAI多智能体协作系统
 * 实现多个Agent之间的协作和任务分工
 */

const { v4: uuidv4 } = require('uuid');

class CrewAICoordinator {
  constructor(aiModelCaller, dbAdapter, smartModelSelector) {
    this.aiModelCaller = aiModelCaller;
    this.dbAdapter = dbAdapter;
    this.smartModelSelector = smartModelSelector;
    this.activeCrew = new Map(); // 活跃的团队实例
    
    // 预定义的Agent角色模板
    this.agentRoles = {
      // 质量管理专业角色
      'quality_analyst': {
        name: '质量分析师',
        description: '专门负责质量数据分析和趋势识别',
        capabilities: ['data_analysis', 'trend_analysis', 'quality_metrics'],
        preferred_models: ['analysis'],
        expertise: ['统计分析', '质量控制', '数据可视化']
      },
      'defect_inspector': {
        name: '缺陷检测专家',
        description: '专门负责产品缺陷识别和分类',
        capabilities: ['image_analysis', 'defect_detection', 'classification'],
        preferred_models: ['analysis', 'vision'],
        expertise: ['图像识别', '缺陷分类', '质量标准']
      },
      'report_writer': {
        name: '报告撰写专家',
        description: '专门负责生成专业的质量报告',
        capabilities: ['report_generation', 'documentation', 'communication'],
        preferred_models: ['generation'],
        expertise: ['技术写作', '报告格式', '数据展示']
      },
      'process_optimizer': {
        name: '流程优化师',
        description: '专门负责质量流程分析和优化建议',
        capabilities: ['process_analysis', 'optimization', 'recommendation'],
        preferred_models: ['reasoning'],
        expertise: ['流程分析', '效率优化', '最佳实践']
      },
      'compliance_checker': {
        name: '合规检查员',
        description: '专门负责标准符合性检查和风险评估',
        capabilities: ['compliance_check', 'risk_assessment', 'standards'],
        preferred_models: ['reasoning'],
        expertise: ['法规标准', '合规检查', '风险管理']
      },
      'data_collector': {
        name: '数据收集员',
        description: '专门负责收集和整理相关数据信息',
        capabilities: ['data_collection', 'research', 'information_gathering'],
        preferred_models: ['research'],
        expertise: ['数据收集', '信息检索', '资料整理']
      }
    };
    
    // 协作模式定义
    this.collaborationModes = {
      'sequential': '顺序执行 - Agent按顺序完成任务',
      'parallel': '并行执行 - Agent同时工作',
      'hierarchical': '层级协作 - 有主管Agent协调其他Agent',
      'peer_review': '同行评议 - Agent互相检查工作结果',
      'consensus': '共识决策 - 多个Agent共同决策'
    };
  }

  /**
   * 创建Agent团队
   */
  async createCrew(crewConfig) {
    const crewId = uuidv4();
    console.log(`🤖 创建Agent团队: ${crewId}`);
    
    try {
      // 验证团队配置
      this.validateCrewConfig(crewConfig);
      
      // 为每个Agent分配最适合的模型
      const agentsWithModels = await this.assignModelsToAgents(crewConfig.agents);
      
      // 创建团队实例
      const crew = {
        id: crewId,
        name: crewConfig.name,
        description: crewConfig.description,
        agents: agentsWithModels,
        workflow: crewConfig.workflow || 'sequential',
        collaboration_mode: crewConfig.collaboration_mode || 'sequential',
        created_at: new Date().toISOString(),
        status: 'active'
      };
      
      // 保存到数据库
      await this.saveCrewToDatabase(crew);
      
      // 缓存活跃团队
      this.activeCrew.set(crewId, crew);
      
      console.log(`✅ Agent团队创建成功: ${crew.name}`);
      return crew;
      
    } catch (error) {
      console.error(`❌ 创建Agent团队失败:`, error);
      throw error;
    }
  }

  /**
   * 执行团队任务
   */
  async executeCrewTask(crewId, task, context = {}) {
    const executionId = uuidv4();
    console.log(`🚀 团队 ${crewId} 开始执行任务: ${executionId}`);
    
    try {
      // 获取团队信息
      const crew = await this.getCrew(crewId);
      if (!crew) {
        throw new Error('团队不存在');
      }
      
      // 创建执行记录
      const execution = {
        id: executionId,
        crew_id: crewId,
        task_description: task.description,
        context: context,
        status: 'running',
        started_at: new Date().toISOString(),
        agent_results: {},
        collaboration_log: []
      };
      
      // 保存执行记录
      await this.saveExecutionRecord(execution);
      
      // 根据协作模式执行任务
      let results;
      switch (crew.collaboration_mode) {
        case 'sequential':
          results = await this.executeSequential(crew, task, execution);
          break;
        case 'parallel':
          results = await this.executeParallel(crew, task, execution);
          break;
        case 'hierarchical':
          results = await this.executeHierarchical(crew, task, execution);
          break;
        case 'peer_review':
          results = await this.executePeerReview(crew, task, execution);
          break;
        case 'consensus':
          results = await this.executeConsensus(crew, task, execution);
          break;
        default:
          results = await this.executeSequential(crew, task, execution);
      }
      
      // 更新执行状态
      execution.status = 'completed';
      execution.completed_at = new Date().toISOString();
      execution.final_result = results;
      await this.updateExecutionRecord(execution);
      
      console.log(`✅ 团队任务执行完成: ${executionId}`);
      return {
        execution_id: executionId,
        crew_id: crewId,
        results: results,
        collaboration_summary: this.generateCollaborationSummary(execution)
      };
      
    } catch (error) {
      console.error(`❌ 团队任务执行失败:`, error);
      throw error;
    }
  }

  /**
   * 顺序执行模式
   */
  async executeSequential(crew, task, execution) {
    console.log(`📋 顺序执行模式: ${crew.agents.length} 个Agent`);
    
    let currentContext = { ...execution.context, task: task };
    const results = [];
    
    for (let i = 0; i < crew.agents.length; i++) {
      const agent = crew.agents[i];
      console.log(`🔄 Agent ${i + 1}/${crew.agents.length}: ${agent.role} 开始工作`);
      
      try {
        // 执行Agent任务
        const agentResult = await this.executeAgentTask(agent, task, currentContext);
        
        // 记录结果
        results.push({
          agent_id: agent.id,
          agent_role: agent.role,
          result: agentResult,
          timestamp: new Date().toISOString()
        });
        
        // 更新上下文，传递给下一个Agent
        currentContext = {
          ...currentContext,
          previous_results: results,
          latest_result: agentResult
        };
        
        // 记录协作日志
        execution.collaboration_log.push({
          action: 'agent_completed',
          agent_role: agent.role,
          timestamp: new Date().toISOString(),
          summary: agentResult.summary || '任务完成'
        });
        
        console.log(`✅ Agent ${agent.role} 完成任务`);
        
      } catch (error) {
        console.error(`❌ Agent ${agent.role} 执行失败:`, error);
        
        // 记录错误但继续执行
        results.push({
          agent_id: agent.id,
          agent_role: agent.role,
          error: error.message,
          timestamp: new Date().toISOString()
        });
        
        execution.collaboration_log.push({
          action: 'agent_failed',
          agent_role: agent.role,
          error: error.message,
          timestamp: new Date().toISOString()
        });
      }
    }
    
    return {
      mode: 'sequential',
      agent_results: results,
      final_output: this.synthesizeResults(results),
      collaboration_effectiveness: this.calculateEffectiveness(results)
    };
  }

  /**
   * 并行执行模式
   */
  async executeParallel(crew, task, execution) {
    console.log(`⚡ 并行执行模式: ${crew.agents.length} 个Agent同时工作`);
    
    const context = { ...execution.context, task: task };
    
    // 并行执行所有Agent
    const agentPromises = crew.agents.map(async (agent, index) => {
      try {
        console.log(`🔄 Agent ${index + 1}: ${agent.role} 开始并行工作`);
        
        const agentResult = await this.executeAgentTask(agent, task, context);
        
        console.log(`✅ Agent ${agent.role} 并行任务完成`);
        
        return {
          agent_id: agent.id,
          agent_role: agent.role,
          result: agentResult,
          timestamp: new Date().toISOString(),
          success: true
        };
        
      } catch (error) {
        console.error(`❌ Agent ${agent.role} 并行执行失败:`, error);
        
        return {
          agent_id: agent.id,
          agent_role: agent.role,
          error: error.message,
          timestamp: new Date().toISOString(),
          success: false
        };
      }
    });
    
    // 等待所有Agent完成
    const results = await Promise.allSettled(agentPromises);
    const processedResults = results.map(result => 
      result.status === 'fulfilled' ? result.value : {
        error: result.reason.message,
        success: false,
        timestamp: new Date().toISOString()
      }
    );
    
    // 记录协作日志
    execution.collaboration_log.push({
      action: 'parallel_execution_completed',
      total_agents: crew.agents.length,
      successful_agents: processedResults.filter(r => r.success).length,
      timestamp: new Date().toISOString()
    });
    
    return {
      mode: 'parallel',
      agent_results: processedResults,
      final_output: this.synthesizeResults(processedResults),
      collaboration_effectiveness: this.calculateEffectiveness(processedResults)
    };
  }

  /**
   * 层级协作模式
   */
  async executeHierarchical(crew, task, execution) {
    console.log(`🏢 层级协作模式: 主管Agent协调执行`);
    
    // 找到主管Agent（通常是第一个或指定的）
    const supervisorAgent = crew.agents.find(a => a.is_supervisor) || crew.agents[0];
    const workerAgents = crew.agents.filter(a => a.id !== supervisorAgent.id);
    
    console.log(`👨‍💼 主管Agent: ${supervisorAgent.role}`);
    console.log(`👥 工作Agent: ${workerAgents.map(a => a.role).join(', ')}`);
    
    // 主管Agent制定执行计划
    const planningContext = { ...execution.context, task: task, worker_agents: workerAgents };
    const executionPlan = await this.executeAgentTask(supervisorAgent, {
      ...task,
      description: `作为主管，请为以下任务制定执行计划：${task.description}。可用的工作Agent：${workerAgents.map(a => a.role).join(', ')}`
    }, planningContext);
    
    execution.collaboration_log.push({
      action: 'supervisor_planning',
      agent_role: supervisorAgent.role,
      plan: executionPlan.summary,
      timestamp: new Date().toISOString()
    });
    
    // 工作Agent执行任务
    const workerResults = [];
    for (const workerAgent of workerAgents) {
      try {
        const workerContext = {
          ...execution.context,
          task: task,
          supervisor_plan: executionPlan,
          previous_worker_results: workerResults
        };
        
        const workerResult = await this.executeAgentTask(workerAgent, task, workerContext);
        workerResults.push({
          agent_id: workerAgent.id,
          agent_role: workerAgent.role,
          result: workerResult,
          timestamp: new Date().toISOString()
        });
        
        console.log(`✅ 工作Agent ${workerAgent.role} 完成任务`);
        
      } catch (error) {
        console.error(`❌ 工作Agent ${workerAgent.role} 执行失败:`, error);
        workerResults.push({
          agent_id: workerAgent.id,
          agent_role: workerAgent.role,
          error: error.message,
          timestamp: new Date().toISOString()
        });
      }
    }
    
    // 主管Agent总结和评估
    const reviewContext = {
      ...execution.context,
      task: task,
      execution_plan: executionPlan,
      worker_results: workerResults
    };
    
    const finalReview = await this.executeAgentTask(supervisorAgent, {
      ...task,
      description: `作为主管，请总结和评估工作Agent的执行结果，并提供最终结论`
    }, reviewContext);
    
    execution.collaboration_log.push({
      action: 'supervisor_review',
      agent_role: supervisorAgent.role,
      review: finalReview.summary,
      timestamp: new Date().toISOString()
    });
    
    return {
      mode: 'hierarchical',
      supervisor_plan: executionPlan,
      worker_results: workerResults,
      supervisor_review: finalReview,
      final_output: finalReview,
      collaboration_effectiveness: this.calculateEffectiveness([...workerResults, {
        agent_role: supervisorAgent.role,
        result: finalReview,
        success: true
      }])
    };
  }

  /**
   * 执行单个Agent任务
   */
  async executeAgentTask(agent, task, context) {
    console.log(`🤖 执行Agent任务: ${agent.role}`);
    
    // 构建Agent专用的提示词
    const agentPrompt = this.buildAgentPrompt(agent, task, context);
    
    // 选择最适合的模型
    const selectedModel = await this.smartModelSelector.selectOptimalModel(
      agent.preferred_task_type || 'analysis',
      {
        highPerformance: agent.requires_high_performance || false,
        lowCost: agent.cost_sensitive || false
      }
    );
    
    // 调用AI模型
    const response = await this.aiModelCaller({
      model: selectedModel.id,
      systemPrompt: this.buildSystemPrompt(agent),
      temperature: agent.temperature || 0.3
    }, [], agentPrompt);
    
    // 解析和结构化结果
    return this.parseAgentResult(agent, response, context);
  }

  /**
   * 构建Agent专用提示词
   */
  buildAgentPrompt(agent, task, context) {
    const roleInfo = this.agentRoles[agent.role] || {};
    
    return `
作为 ${roleInfo.name || agent.role}，你的专业领域是：${roleInfo.expertise?.join('、') || '通用任务'}

任务描述：${task.description}

你的职责：
${roleInfo.description || '完成分配的任务'}

可用能力：
${roleInfo.capabilities?.join('、') || '通用分析'}

上下文信息：
${JSON.stringify(context, null, 2)}

请根据你的专业角色完成任务，并提供：
1. 专业分析和见解
2. 具体的执行结果
3. 对其他团队成员的建议
4. 工作总结

请以专业、准确、有用的方式回应。
`;
  }

  /**
   * 构建系统提示词
   */
  buildSystemPrompt(agent) {
    const roleInfo = this.agentRoles[agent.role] || {};
    
    return `你是一个专业的${roleInfo.name || agent.role}，具有以下专业能力：${roleInfo.expertise?.join('、') || '通用分析'}。你在团队中与其他专业Agent协作，需要提供高质量的专业服务。`;
  }

  /**
   * 解析Agent结果
   */
  parseAgentResult(agent, response, context) {
    return {
      agent_role: agent.role,
      content: response.content,
      summary: this.extractSummary(response.content),
      recommendations: this.extractRecommendations(response.content),
      confidence: this.assessConfidence(response.content),
      next_steps: this.extractNextSteps(response.content),
      timestamp: new Date().toISOString(),
      model_used: response.model || 'unknown',
      token_usage: response.usage || {}
    };
  }

  // 辅助方法
  validateCrewConfig(config) {
    if (!config.name) throw new Error('团队名称不能为空');
    if (!config.agents || config.agents.length === 0) throw new Error('团队至少需要一个Agent');
    if (config.agents.length > 10) throw new Error('团队Agent数量不能超过10个');
  }

  async assignModelsToAgents(agents) {
    const agentsWithModels = [];
    
    for (const agent of agents) {
      const roleInfo = this.agentRoles[agent.role] || {};
      const preferredTaskType = roleInfo.preferred_models?.[0] || 'analysis';
      
      const selectedModel = await this.smartModelSelector.selectOptimalModel(preferredTaskType);
      
      agentsWithModels.push({
        ...agent,
        id: agent.id || uuidv4(),
        assigned_model: selectedModel.id,
        preferred_task_type: preferredTaskType,
        capabilities: roleInfo.capabilities || [],
        expertise: roleInfo.expertise || []
      });
    }
    
    return agentsWithModels;
  }

  async saveCrewToDatabase(crew) {
    try {
      await this.dbAdapter.query(`
        INSERT INTO agent_crews (
          id, user_id, name, description, agents, workflow, status, created_at, updated_at
        ) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)
      `, [
        crew.id,
        'system', // 系统创建
        crew.name,
        crew.description,
        JSON.stringify(crew.agents),
        JSON.stringify(crew.workflow),
        crew.status,
        crew.created_at,
        crew.created_at
      ]);
    } catch (error) {
      console.warn('保存团队到数据库失败:', error.message);
    }
  }

  async getCrew(crewId) {
    // 先从缓存获取
    if (this.activeCrew.has(crewId)) {
      return this.activeCrew.get(crewId);
    }
    
    // 从数据库获取
    try {
      const [crew] = await this.dbAdapter.query(
        'SELECT * FROM agent_crews WHERE id = ?',
        [crewId]
      );
      
      if (crew) {
        const parsedCrew = {
          ...crew,
          agents: JSON.parse(crew.agents || '[]'),
          workflow: JSON.parse(crew.workflow || '{}')
        };
        
        this.activeCrew.set(crewId, parsedCrew);
        return parsedCrew;
      }
    } catch (error) {
      console.warn('从数据库获取团队失败:', error.message);
    }
    
    return null;
  }

  synthesizeResults(results) {
    const successfulResults = results.filter(r => r.success !== false && !r.error);
    
    if (successfulResults.length === 0) {
      return {
        summary: '所有Agent执行失败',
        confidence: 0,
        recommendations: ['检查Agent配置', '重新执行任务']
      };
    }
    
    return {
      summary: `${successfulResults.length}个Agent成功完成任务`,
      key_findings: successfulResults.map(r => r.result?.summary).filter(Boolean),
      combined_recommendations: successfulResults.flatMap(r => r.result?.recommendations || []),
      overall_confidence: successfulResults.reduce((sum, r) => sum + (r.result?.confidence || 0.5), 0) / successfulResults.length
    };
  }

  calculateEffectiveness(results) {
    const total = results.length;
    const successful = results.filter(r => r.success !== false && !r.error).length;
    
    return {
      success_rate: (successful / total * 100).toFixed(1) + '%',
      total_agents: total,
      successful_agents: successful,
      failed_agents: total - successful
    };
  }

  // 简化的文本解析方法
  extractSummary(content) {
    const lines = content.split('\n');
    const summaryLine = lines.find(line => 
      line.includes('总结') || line.includes('摘要') || line.includes('结论')
    );
    return summaryLine ? summaryLine.trim() : content.substring(0, 200) + '...';
  }

  extractRecommendations(content) {
    const recommendations = [];
    const lines = content.split('\n');
    
    lines.forEach(line => {
      if (line.includes('建议') || line.includes('推荐') || line.includes('应该')) {
        recommendations.push(line.trim());
      }
    });
    
    return recommendations.slice(0, 5); // 最多5个建议
  }

  extractNextSteps(content) {
    const steps = [];
    const lines = content.split('\n');
    
    lines.forEach(line => {
      if (line.includes('下一步') || line.includes('接下来') || line.includes('后续')) {
        steps.push(line.trim());
      }
    });
    
    return steps.slice(0, 3); // 最多3个步骤
  }

  assessConfidence(content) {
    // 简化的置信度评估
    const confidenceKeywords = ['确定', '明确', '肯定', '确认'];
    const uncertaintyKeywords = ['可能', '也许', '大概', '估计', '不确定'];
    
    let score = 0.5;
    confidenceKeywords.forEach(keyword => {
      if (content.includes(keyword)) score += 0.1;
    });
    uncertaintyKeywords.forEach(keyword => {
      if (content.includes(keyword)) score -= 0.1;
    });
    
    return Math.max(0, Math.min(1, score));
  }

  async saveExecutionRecord(execution) {
    try {
      await this.dbAdapter.query(`
        INSERT INTO crew_executions (
          id, crew_id, task_description, agent_assignments, execution_log, 
          results, status, started_at
        ) VALUES (?, ?, ?, ?, ?, ?, ?, ?)
      `, [
        execution.id,
        execution.crew_id,
        execution.task_description,
        JSON.stringify(execution.context),
        JSON.stringify(execution.collaboration_log),
        JSON.stringify(execution.agent_results || {}),
        execution.status,
        execution.started_at
      ]);
    } catch (error) {
      console.warn('保存执行记录失败:', error.message);
    }
  }

  async updateExecutionRecord(execution) {
    try {
      await this.dbAdapter.query(`
        UPDATE crew_executions 
        SET status = ?, results = ?, execution_log = ?, completed_at = ?
        WHERE id = ?
      `, [
        execution.status,
        JSON.stringify(execution.final_result || {}),
        JSON.stringify(execution.collaboration_log),
        execution.completed_at,
        execution.id
      ]);
    } catch (error) {
      console.warn('更新执行记录失败:', error.message);
    }
  }

  generateCollaborationSummary(execution) {
    return {
      total_agents: execution.collaboration_log.filter(log => log.action === 'agent_completed').length,
      collaboration_mode: execution.collaboration_mode || 'sequential',
      execution_time: execution.completed_at ? 
        new Date(execution.completed_at) - new Date(execution.started_at) : null,
      key_events: execution.collaboration_log.slice(-5) // 最近5个事件
    };
  }
}

module.exports = CrewAICoordinator;
