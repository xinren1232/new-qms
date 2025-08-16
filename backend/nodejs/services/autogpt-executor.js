/**
 * AutoGPT执行器
 * 负责执行AutoGPT规划的任务
 */

const { v4: uuidv4 } = require('uuid');
const SmartModelSelector = require('./smart-model-selector');

class AutoGPTExecutor {
  constructor(aiModelCaller, dbAdapter) {
    this.aiModelCaller = aiModelCaller;
    this.dbAdapter = dbAdapter;
    this.modelSelector = new SmartModelSelector();
    this.activeExecutions = new Map(); // 活跃的执行实例
    this.maxConcurrentTasks = 3; // 最大并发任务数
  }

  /**
   * 执行AutoGPT规划
   * @param {Object} plan - AutoGPT规划结果
   * @param {Object} context - 执行上下文
   * @returns {Object} 执行结果
   */
  async executePlan(plan, context = {}) {
    const executionId = uuidv4();
    console.log(`🚀 AutoGPT开始执行规划: ${executionId}`);
    
    try {
      // 创建执行实例
      const execution = {
        id: executionId,
        plan_id: plan.id,
        status: 'running',
        current_task: null,
        completed_tasks: [],
        failed_tasks: [],
        results: {},
        context: context,
        started_at: new Date().toISOString(),
        updated_at: new Date().toISOString()
      };
      
      this.activeExecutions.set(executionId, execution);
      
      // 保存执行记录到数据库
      await this.saveExecutionRecord(execution, plan);
      
      // 按照执行计划顺序执行任务
      const results = await this.executeTaskGroups(plan, execution);
      
      // 更新执行状态
      execution.status = 'completed';
      execution.completed_at = new Date().toISOString();
      execution.final_result = results;
      
      await this.updateExecutionRecord(execution);
      
      console.log(`✅ AutoGPT执行完成: ${executionId}`);
      return {
        execution_id: executionId,
        status: 'completed',
        results: results,
        summary: await this.generateExecutionSummary(execution, plan)
      };
      
    } catch (error) {
      console.error(`❌ AutoGPT执行失败: ${executionId}`, error);
      
      const execution = this.activeExecutions.get(executionId);
      if (execution) {
        execution.status = 'failed';
        execution.error = error.message;
        execution.completed_at = new Date().toISOString();
        await this.updateExecutionRecord(execution);
      }
      
      throw error;
    } finally {
      this.activeExecutions.delete(executionId);
    }
  }

  /**
   * 按组执行任务（支持并行执行）
   */
  async executeTaskGroups(plan, execution) {
    const results = {};
    const parallelGroups = plan.plan.parallel_groups;
    
    for (let groupIndex = 0; groupIndex < parallelGroups.length; groupIndex++) {
      const group = parallelGroups[groupIndex];
      console.log(`📋 执行任务组 ${groupIndex + 1}/${parallelGroups.length}: [${group.join(', ')}]`);
      
      // 并行执行组内任务
      const groupPromises = group.map(taskId => 
        this.executeTask(taskId, plan, execution, results)
      );
      
      try {
        const groupResults = await Promise.allSettled(groupPromises);
        
        // 处理组内任务结果
        groupResults.forEach((result, index) => {
          const taskId = group[index];
          if (result.status === 'fulfilled') {
            results[taskId] = result.value;
            execution.completed_tasks.push(taskId);
            console.log(`✅ 任务完成: ${taskId}`);
          } else {
            execution.failed_tasks.push({
              task_id: taskId,
              error: result.reason.message,
              timestamp: new Date().toISOString()
            });
            console.error(`❌ 任务失败: ${taskId}`, result.reason);
          }
        });
        
        // 更新执行记录
        execution.results = results;
        execution.updated_at = new Date().toISOString();
        await this.updateExecutionRecord(execution);
        
      } catch (error) {
        console.error(`任务组执行失败:`, error);
        throw error;
      }
    }
    
    return results;
  }

  /**
   * 执行单个任务
   */
  async executeTask(taskId, plan, execution, previousResults) {
    const task = plan.tasks.find(t => t.id === taskId);
    if (!task) {
      throw new Error(`任务不存在: ${taskId}`);
    }
    
    console.log(`🔄 执行任务: ${task.name} (${task.type})`);
    execution.current_task = taskId;
    
    // 准备任务上下文
    const taskContext = this.prepareTaskContext(task, execution.context, previousResults);
    
    // 根据任务类型选择执行策略
    let result;
    switch (task.type) {
      case 'research':
        result = await this.executeResearchTask(task, taskContext);
        break;
      case 'analysis':
        result = await this.executeAnalysisTask(task, taskContext);
        break;
      case 'generation':
        result = await this.executeGenerationTask(task, taskContext);
        break;
      case 'validation':
        result = await this.executeValidationTask(task, taskContext);
        break;
      case 'coding':
        result = await this.executeCodingTask(task, taskContext);
        break;
      default:
        result = await this.executeGenericTask(task, taskContext);
    }
    
    // 记录任务执行日志
    await this.logTaskExecution(execution.id, task, result);
    
    return result;
  }

  /**
   * 准备任务执行上下文
   */
  prepareTaskContext(task, globalContext, previousResults) {
    const context = {
      ...globalContext,
      task_id: task.id,
      task_name: task.name,
      task_description: task.description,
      previous_results: {}
    };
    
    // 添加依赖任务的结果
    task.dependencies.forEach(depId => {
      if (previousResults[depId]) {
        context.previous_results[depId] = previousResults[depId];
      }
    });
    
    return context;
  }

  /**
   * 执行研究类任务
   */
  async executeResearchTask(task, context) {
    const researchPrompt = `
作为一个专业的研究助手，请完成以下研究任务：

任务: ${task.name}
描述: ${task.description}
上下文: ${JSON.stringify(context, null, 2)}

请进行深入研究并提供：
1. 关键信息和发现
2. 数据来源和可靠性
3. 相关的背景知识
4. 潜在的问题和限制

请以结构化的方式组织你的研究结果。
`;

    // 使用智能模型选择器选择最适合的模型
    const selectedModel = await this.modelSelector.selectOptimalModel('research', {
      highPerformance: true
    });

    const response = await this.aiModelCaller({
      model: selectedModel.id,
      systemPrompt: '你是一个专业的研究助手，擅长信息收集和分析。',
      temperature: 0.3
    }, [], researchPrompt);

    return {
      type: 'research',
      content: response.content,
      sources: this.extractSources(response.content),
      confidence: this.assessConfidence(response.content),
      timestamp: new Date().toISOString()
    };
  }

  /**
   * 执行分析类任务
   */
  async executeAnalysisTask(task, context) {
    const analysisPrompt = `
作为一个专业的数据分析师，请完成以下分析任务：

任务: ${task.name}
描述: ${task.description}
输入数据: ${JSON.stringify(context.previous_results, null, 2)}

请进行深入分析并提供：
1. 数据模式和趋势
2. 关键洞察和发现
3. 统计分析结果
4. 结论和建议

请使用适当的分析方法和工具。
`;

    // 选择最适合分析任务的模型
    const selectedModel = await this.modelSelector.selectOptimalModel('analysis', {
      highPerformance: true
    });

    const response = await this.aiModelCaller({
      model: selectedModel.id,
      systemPrompt: '你是一个专业的数据分析师，擅长数据分析和洞察发现。',
      temperature: 0.2
    }, [], analysisPrompt);

    return {
      type: 'analysis',
      content: response.content,
      insights: this.extractInsights(response.content),
      metrics: this.extractMetrics(response.content),
      timestamp: new Date().toISOString()
    };
  }

  /**
   * 执行生成类任务
   */
  async executeGenerationTask(task, context) {
    const generationPrompt = `
作为一个专业的内容生成专家，请完成以下生成任务：

任务: ${task.name}
描述: ${task.description}
基础材料: ${JSON.stringify(context.previous_results, null, 2)}

请生成高质量的内容，包括：
1. 结构清晰的主要内容
2. 支撑细节和例子
3. 适当的格式和样式
4. 质量检查和优化建议

请确保内容的准确性和实用性。
`;

    // 选择最适合生成任务的模型
    const selectedModel = await this.modelSelector.selectOptimalModel('generation', {
      highPerformance: true
    });

    const response = await this.aiModelCaller({
      model: selectedModel.id,
      systemPrompt: '你是一个专业的内容生成专家，擅长创作高质量的内容。',
      temperature: 0.4
    }, [], generationPrompt);

    return {
      type: 'generation',
      content: response.content,
      quality_score: this.assessContentQuality(response.content),
      word_count: response.content.split(' ').length,
      timestamp: new Date().toISOString()
    };
  }

  /**
   * 执行验证类任务
   */
  async executeValidationTask(task, context) {
    const validationPrompt = `
作为一个专业的质量验证专家，请验证以下内容：

任务: ${task.name}
描述: ${task.description}
待验证内容: ${JSON.stringify(context.previous_results, null, 2)}

请进行全面验证并提供：
1. 准确性检查结果
2. 完整性评估
3. 一致性验证
4. 改进建议

请给出明确的验证结论。
`;

    const response = await this.aiModelCaller({
      model: 'gpt-4o',
      systemPrompt: '你是一个专业的质量验证专家，擅长内容验证和质量控制。',
      temperature: 0.1
    }, [], validationPrompt);

    return {
      type: 'validation',
      content: response.content,
      validation_result: this.parseValidationResult(response.content),
      issues_found: this.extractIssues(response.content),
      timestamp: new Date().toISOString()
    };
  }

  /**
   * 执行编程类任务
   */
  async executeCodingTask(task, context) {
    const codingPrompt = `
作为一个专业的软件开发工程师，请完成以下编程任务：

任务: ${task.name}
描述: ${task.description}
需求: ${JSON.stringify(context.previous_results, null, 2)}

请提供：
1. 完整的代码实现
2. 代码注释和文档
3. 测试用例
4. 使用说明

请确保代码的质量和可维护性。
`;

    // 选择最适合编程任务的模型
    const selectedModel = await this.modelSelector.selectOptimalModel('coding', {
      highPerformance: true
    });

    const response = await this.aiModelCaller({
      model: selectedModel.id,
      systemPrompt: '你是一个专业的软件开发工程师，擅长编写高质量的代码。',
      temperature: 0.2
    }, [], codingPrompt);

    return {
      type: 'coding',
      content: response.content,
      code_blocks: this.extractCodeBlocks(response.content),
      language: this.detectLanguage(response.content),
      timestamp: new Date().toISOString()
    };
  }

  /**
   * 执行通用任务
   */
  async executeGenericTask(task, context) {
    const genericPrompt = `
请完成以下任务：

任务: ${task.name}
描述: ${task.description}
上下文: ${JSON.stringify(context, null, 2)}

请提供详细的执行结果和说明。
`;

    const response = await this.aiModelCaller({
      model: 'gpt-4o',
      systemPrompt: '你是一个专业的任务执行助手。',
      temperature: 0.3
    }, [], genericPrompt);

    return {
      type: 'generic',
      content: response.content,
      timestamp: new Date().toISOString()
    };
  }

  /**
   * 保存执行记录到数据库
   */
  async saveExecutionRecord(execution, plan) {
    try {
      await this.dbAdapter.query(`
        INSERT INTO autogpt_executions (
          id, plan_id, status, context, started_at, updated_at
        ) VALUES (?, ?, ?, ?, ?, ?)
      `, [
        execution.id,
        plan.id,
        execution.status,
        JSON.stringify(execution.context),
        execution.started_at,
        execution.updated_at
      ]);
    } catch (error) {
      console.warn('保存执行记录失败:', error.message);
    }
  }

  /**
   * 更新执行记录
   */
  async updateExecutionRecord(execution) {
    try {
      await this.dbAdapter.query(`
        UPDATE autogpt_executions 
        SET status = ?, results = ?, completed_tasks = ?, failed_tasks = ?, 
            updated_at = ?, completed_at = ?
        WHERE id = ?
      `, [
        execution.status,
        JSON.stringify(execution.results || {}),
        JSON.stringify(execution.completed_tasks || []),
        JSON.stringify(execution.failed_tasks || []),
        execution.updated_at,
        execution.completed_at || null,
        execution.id
      ]);
    } catch (error) {
      console.warn('更新执行记录失败:', error.message);
    }
  }

  /**
   * 记录任务执行日志
   */
  async logTaskExecution(executionId, task, result) {
    try {
      await this.dbAdapter.query(`
        INSERT INTO autogpt_task_logs (
          id, execution_id, task_id, task_name, task_type, 
          result, created_at
        ) VALUES (?, ?, ?, ?, ?, ?, ?)
      `, [
        uuidv4(),
        executionId,
        task.id,
        task.name,
        task.type,
        JSON.stringify(result),
        new Date().toISOString()
      ]);
    } catch (error) {
      console.warn('记录任务日志失败:', error.message);
    }
  }

  /**
   * 生成执行总结
   */
  async generateExecutionSummary(execution, plan) {
    const summary = {
      total_tasks: plan.tasks.length,
      completed_tasks: execution.completed_tasks.length,
      failed_tasks: execution.failed_tasks.length,
      success_rate: (execution.completed_tasks.length / plan.tasks.length * 100).toFixed(1) + '%',
      execution_time: this.calculateExecutionTime(execution),
      key_results: this.extractKeyResults(execution.results)
    };

    return summary;
  }

  /**
   * 计算执行时间
   */
  calculateExecutionTime(execution) {
    if (!execution.started_at || !execution.completed_at) {
      return null;
    }
    
    const start = new Date(execution.started_at);
    const end = new Date(execution.completed_at);
    const diffMs = end - start;
    
    return {
      milliseconds: diffMs,
      seconds: Math.round(diffMs / 1000),
      minutes: Math.round(diffMs / 60000),
      human_readable: this.formatDuration(diffMs)
    };
  }

  /**
   * 格式化持续时间
   */
  formatDuration(ms) {
    const seconds = Math.floor(ms / 1000);
    const minutes = Math.floor(seconds / 60);
    const hours = Math.floor(minutes / 60);
    
    if (hours > 0) {
      return `${hours}小时${minutes % 60}分钟`;
    } else if (minutes > 0) {
      return `${minutes}分钟${seconds % 60}秒`;
    } else {
      return `${seconds}秒`;
    }
  }

  // 辅助方法
  extractSources(content) {
    // 简化的来源提取
    const sources = [];
    const urlRegex = /https?:\/\/[^\s]+/g;
    const matches = content.match(urlRegex);
    if (matches) {
      sources.push(...matches);
    }
    return sources;
  }

  assessConfidence(content) {
    // 简化的置信度评估
    const confidenceKeywords = ['确定', '明确', '证实', '验证'];
    const uncertaintyKeywords = ['可能', '也许', '大概', '估计'];
    
    let score = 0.5;
    confidenceKeywords.forEach(keyword => {
      if (content.includes(keyword)) score += 0.1;
    });
    uncertaintyKeywords.forEach(keyword => {
      if (content.includes(keyword)) score -= 0.1;
    });
    
    return Math.max(0, Math.min(1, score));
  }

  extractInsights(content) {
    // 简化的洞察提取
    const insights = [];
    const lines = content.split('\n');
    lines.forEach(line => {
      if (line.includes('发现') || line.includes('洞察') || line.includes('结论')) {
        insights.push(line.trim());
      }
    });
    return insights;
  }

  extractMetrics(content) {
    // 简化的指标提取
    const metrics = {};
    const numberRegex = /(\d+(?:\.\d+)?)\s*([%％]|percent|百分比)/g;
    let match;
    while ((match = numberRegex.exec(content)) !== null) {
      metrics[`percentage_${Object.keys(metrics).length + 1}`] = parseFloat(match[1]);
    }
    return metrics;
  }

  assessContentQuality(content) {
    // 简化的内容质量评估
    let score = 0.5;
    
    // 长度评估
    if (content.length > 500) score += 0.1;
    if (content.length > 1000) score += 0.1;
    
    // 结构评估
    if (content.includes('\n\n')) score += 0.1; // 有段落分隔
    if (content.match(/\d+\./)) score += 0.1; // 有编号列表
    
    // 专业性评估
    const professionalWords = ['分析', '评估', '建议', '总结', '结论'];
    professionalWords.forEach(word => {
      if (content.includes(word)) score += 0.02;
    });
    
    return Math.max(0, Math.min(1, score));
  }

  parseValidationResult(content) {
    // 简化的验证结果解析
    if (content.includes('通过') || content.includes('正确') || content.includes('有效')) {
      return 'passed';
    } else if (content.includes('失败') || content.includes('错误') || content.includes('无效')) {
      return 'failed';
    } else {
      return 'partial';
    }
  }

  extractIssues(content) {
    // 简化的问题提取
    const issues = [];
    const lines = content.split('\n');
    lines.forEach(line => {
      if (line.includes('问题') || line.includes('错误') || line.includes('缺陷')) {
        issues.push(line.trim());
      }
    });
    return issues;
  }

  extractCodeBlocks(content) {
    // 提取代码块
    const codeBlocks = [];
    const codeRegex = /```(\w+)?\n([\s\S]*?)```/g;
    let match;
    while ((match = codeRegex.exec(content)) !== null) {
      codeBlocks.push({
        language: match[1] || 'text',
        code: match[2].trim()
      });
    }
    return codeBlocks;
  }

  detectLanguage(content) {
    // 简化的语言检测
    if (content.includes('def ') || content.includes('import ')) return 'python';
    if (content.includes('function ') || content.includes('const ')) return 'javascript';
    if (content.includes('public class') || content.includes('import java')) return 'java';
    return 'text';
  }

  extractKeyResults(results) {
    // 提取关键结果
    const keyResults = [];
    Object.entries(results).forEach(([taskId, result]) => {
      if (result.content && result.content.length > 100) {
        keyResults.push({
          task_id: taskId,
          type: result.type,
          summary: result.content.substring(0, 200) + '...'
        });
      }
    });
    return keyResults;
  }
}

module.exports = AutoGPTExecutor;
