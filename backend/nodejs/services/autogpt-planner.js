/**
 * AutoGPT自主规划模块
 * 集成AutoGPT的任务分解和自主执行能力
 */

const { v4: uuidv4 } = require('uuid');
const SmartModelSelector = require('./smart-model-selector');

class AutoGPTPlanner {
  constructor(aiModelCaller) {
    this.aiModelCaller = aiModelCaller;
    this.modelSelector = new SmartModelSelector();
    this.maxPlanningDepth = 5; // 最大规划深度
    this.maxExecutionSteps = 20; // 最大执行步骤
  }

  /**
   * 解析JSON的容错方法：优先直接解析，其次截取第一个{到最后一个}的内容再解析
   */
  parseJSONSafe(text) {
    if (!text || typeof text !== 'string') return null;
    try { return JSON.parse(text); } catch (e) { /* fallthrough */ }
    const start = text.indexOf('{');
    const end = text.lastIndexOf('}');
    if (start >= 0 && end > start) {
      const slice = text.slice(start, end + 1);
      try { return JSON.parse(slice); } catch (e) { /* ignore */ }
    }
    // 尝试数组
    const aStart = text.indexOf('[');
    const aEnd = text.lastIndexOf(']');
    if (aStart >= 0 && aEnd > aStart) {
      const slice = text.slice(aStart, aEnd + 1);
      try { return JSON.parse(slice); } catch (e) { /* ignore */ }
    }
    return null;
  }

  /**
   * 选择模型：优先使用智能模型选择器，回退到默认
   */
  async chooseModel(stage = 'general', taskType = 'general', hints = {}) {
    try {
      const sel = await this.modelSelector.selectOptimalModel(taskType, hints);
      return sel?.id || null;
    } catch (e) {
      // ignore, fallback below
      return null;
    }
  }


  /**
   * 自主规划：将复杂目标分解为可执行的子任务
   * @param {string} goal - 用户目标
   * @param {Object} context - 上下文信息
   * @returns {Object} 规划结果
   */
  async planGoal(goal, context = {}) {
    console.log(`🎯 AutoGPT开始规划目标: ${goal}`);

    try {
      // 1. 目标分析
      const goalAnalysis = await this.analyzeGoal(goal, context);

      // 2. 任务分解
      const taskBreakdown = await this.breakdownTasks(goalAnalysis);

      // 3. 执行计划生成
      const executionPlan = await this.generateExecutionPlan(taskBreakdown);

      // 4. 资源评估
      const resourceAssessment = await this.assessResources(executionPlan);

      return {
        id: uuidv4(),
        goal: goal,
        analysis: goalAnalysis,
        tasks: taskBreakdown,
        plan: executionPlan,
        resources: resourceAssessment,
        status: 'planned',
        created_at: new Date().toISOString()
      };

    } catch (error) {
      console.error('AutoGPT规划失败:', error);
      throw new Error(`规划失败: ${error.message}`);
    }
  }

  /**
   * 目标分析：理解用户意图和需求
   */
  async analyzeGoal(goal, context) {
    const analysisPrompt = `
作为一个专业的任务规划助手，请分析以下目标：

目标: ${goal}
上下文: ${JSON.stringify(context, null, 2)}

请从以下维度分析：
1. 目标类型（信息查询、数据分析、文档处理、质量检测等）
2. 复杂程度（简单、中等、复杂）
3. 所需资源（AI模型、插件、外部API等）
4. 预期输出（文本、图表、报告、数据等）
5. 成功标准（如何判断任务完成）

请以JSON格式返回分析结果。
`;

    const response = await this.aiModelCaller({
      model: 'gpt-4o',
      systemPrompt: '你是一个专业的任务分析专家，擅长分解复杂目标。',
      temperature: 0.3
    }, [], analysisPrompt);

    try {
      return JSON.parse(response.content);
    } catch (error) {
      // 如果JSON解析失败，返回结构化的默认分析
      return {
        type: 'general',
        complexity: 'medium',
        resources: ['ai_model'],
        output: 'text',
        success_criteria: '用户满意的回答',
        raw_analysis: response.content
      };
    }
  }

  /**
   * 任务分解：将目标分解为具体的子任务
   */
  async breakdownTasks(goalAnalysis) {
    const breakdownPrompt = `
基于以下目标分析，请将目标分解为具体的子任务：

目标分析: ${JSON.stringify(goalAnalysis, null, 2)}

请按照以下格式分解任务：
1. 每个子任务应该是原子性的（不可再分）
2. 子任务之间应该有明确的依赖关系
3. 每个子任务应该指定所需的工具或方法

请以JSON数组格式返回，每个任务包含：
- id: 任务ID
- name: 任务名称
- description: 详细描述
- type: 任务类型（research, analysis, generation, validation等）
- dependencies: 依赖的任务ID列表
- tools: 所需工具列表
- estimated_time: 预估时间（分钟）
- priority: 优先级（1-5）
`;

    const response = await this.aiModelCaller({
      model: 'deepseek-r1',
      systemPrompt: '你是一个专业的任务分解专家，擅长将复杂目标分解为可执行的子任务。',
      temperature: 0.2
    }, [], breakdownPrompt);

    try {
      const tasks = JSON.parse(response.content);
      // 为每个任务添加唯一ID
      return tasks.map((task, index) => ({
        ...task,
        id: task.id || `task_${index + 1}`,
        status: 'pending'
      }));
    } catch (error) {
      // 默认任务分解
      return [
        {
          id: 'task_1',
          name: '信息收集',
          description: '收集完成目标所需的信息',
          type: 'research',
          dependencies: [],
          tools: ['ai_model'],
          estimated_time: 5,
          priority: 1,
          status: 'pending'
        },
        {
          id: 'task_2',
          name: '信息处理',
          description: '处理和分析收集到的信息',
          type: 'analysis',
          dependencies: ['task_1'],
          tools: ['ai_model'],
          estimated_time: 10,
          priority: 2,
          status: 'pending'
        },
        {
          id: 'task_3',
          name: '结果生成',
          description: '生成最终结果',
          type: 'generation',
          dependencies: ['task_2'],
          tools: ['ai_model'],
          estimated_time: 5,
          priority: 3,
          status: 'pending'
        }
      ];
    }
  }

  /**
   * 生成执行计划：确定任务执行顺序和策略
   */
  async generateExecutionPlan(tasks) {
    // 1. 拓扑排序确定执行顺序
    const executionOrder = this.topologicalSort(tasks);

    // 2. 并行任务识别
    const parallelGroups = this.identifyParallelTasks(tasks, executionOrder);

    // 3. 资源分配
    const resourceAllocation = this.allocateResources(tasks);

    return {
      execution_order: executionOrder,
      parallel_groups: parallelGroups,
      resource_allocation: resourceAllocation,
      estimated_total_time: this.calculateTotalTime(tasks, parallelGroups),
      strategy: 'adaptive' // 自适应执行策略
    };
  }

  /**
   * 拓扑排序：根据依赖关系确定执行顺序
   */
  topologicalSort(tasks) {
    const graph = new Map();
    const inDegree = new Map();

    // 构建图和入度表
    tasks.forEach(task => {
      graph.set(task.id, []);
      inDegree.set(task.id, 0);
    });

    tasks.forEach(task => {
      task.dependencies.forEach(dep => {
        if (graph.has(dep)) {
          graph.get(dep).push(task.id);
          inDegree.set(task.id, inDegree.get(task.id) + 1);
        }
      });
    });

    // Kahn算法
    const queue = [];
    const result = [];

    inDegree.forEach((degree, taskId) => {
      if (degree === 0) {
        queue.push(taskId);
      }
    });

    while (queue.length > 0) {
      const current = queue.shift();
      result.push(current);

      graph.get(current).forEach(neighbor => {
        inDegree.set(neighbor, inDegree.get(neighbor) - 1);
        if (inDegree.get(neighbor) === 0) {
          queue.push(neighbor);
        }
      });
    }

    return result;
  }

  /**
   * 识别可并行执行的任务
   */
  identifyParallelTasks(tasks, executionOrder) {
    const groups = [];
    const processed = new Set();

    for (const taskId of executionOrder) {
      if (processed.has(taskId)) continue;

      const task = tasks.find(t => t.id === taskId);
      const parallelTasks = [taskId];

      // 查找可以并行执行的任务
      for (const otherTaskId of executionOrder) {
        if (otherTaskId === taskId || processed.has(otherTaskId)) continue;

        const otherTask = tasks.find(t => t.id === otherTaskId);
        if (this.canExecuteInParallel(task, otherTask, tasks)) {
          parallelTasks.push(otherTaskId);
        }
      }

      groups.push(parallelTasks);
      parallelTasks.forEach(id => processed.add(id));
    }

    return groups;
  }

  /**
   * 判断两个任务是否可以并行执行
   */
  canExecuteInParallel(task1, task2, allTasks) {
    // 检查依赖关系
    const task1Deps = new Set(task1.dependencies);
    const task2Deps = new Set(task2.dependencies);

    // 如果任务之间有直接或间接依赖，不能并行
    if (task1Deps.has(task2.id) || task2Deps.has(task1.id)) {
      return false;
    }

    // 检查资源冲突（简化版本）
    const task1Tools = new Set(task1.tools);
    const task2Tools = new Set(task2.tools);

    // 如果都需要相同的独占资源，不能并行
    const exclusiveTools = ['file_system', 'database_write'];
    for (const tool of exclusiveTools) {
      if (task1Tools.has(tool) && task2Tools.has(tool)) {
        return false;
      }
    }

    return true;
  }

  /**
   * 资源分配 - 使用智能模型选择器
   */
  async allocateResources(tasks) {
    const allocation = {
      ai_models: {},
      plugins: {},
      memory: {},
      compute: {}
    };

    // 使用智能模型选择器分配模型
    const modelAllocation = await this.modelSelector.allocateModelsForTasks(tasks);
    allocation.ai_models = modelAllocation.allocation;
    allocation.environment_info = {
      environment: modelAllocation.environment,
      summary: modelAllocation.summary
    };

    tasks.forEach(task => {
      allocation.plugins[task.id] = task.tools.filter(tool => tool !== 'ai_model');
      allocation.memory[task.id] = this.estimateMemoryUsage(task);
      allocation.compute[task.id] = this.estimateComputeUsage(task);
    });

    return allocation;
  }

  /**
   * 为任务选择最优的AI模型 (已废弃，使用智能模型选择器)
   */
  async selectOptimalModel(task) {
    // 使用智能模型选择器
    const selectedModel = await this.modelSelector.selectOptimalModel(task.type, {
      longContext: task.estimated_time > 15,
      lowCost: task.priority <= 2,
      highPerformance: task.priority >= 4
    });

    return selectedModel.id;
  }

  /**
   * 估算内存使用量
   */
  estimateMemoryUsage(task) {
    const baseMemory = 100; // MB
    const typeMultiplier = {
      'research': 1.2,
      'analysis': 2.0,
      'generation': 1.5,
      'validation': 1.0
    };

    return Math.round(baseMemory * (typeMultiplier[task.type] || 1.0));
  }

  /**
   * 估算计算使用量
   */
  estimateComputeUsage(task) {
    return {
      cpu_cores: task.type === 'analysis' ? 2 : 1,
      gpu_memory: task.tools.includes('image_processing') ? 4096 : 0,
      estimated_tokens: task.estimated_time * 100 // 粗略估算
    };
  }

  /**
   * 计算总执行时间
   */
  calculateTotalTime(tasks, parallelGroups) {
    let totalTime = 0;

    parallelGroups.forEach(group => {
      const groupTime = Math.max(...group.map(taskId => {
        const task = tasks.find(t => t.id === taskId);
        return task ? task.estimated_time : 0;
      }));
      totalTime += groupTime;
    });

    return totalTime;
  }

  /**
   * 资源评估：评估执行计划的可行性
   */
  async assessResources(executionPlan) {
    return {
      feasibility: 'high',
      estimated_cost: this.estimateCost(executionPlan),
      resource_requirements: this.summarizeResourceRequirements(executionPlan),
      potential_bottlenecks: this.identifyBottlenecks(executionPlan),
      optimization_suggestions: this.generateOptimizationSuggestions(executionPlan)
    };
  }

  /**
   * 估算执行成本
   */
  estimateCost(executionPlan) {
    const modelCosts = {
      'gpt-4o': 0.03,           // 每1K tokens
      'claude-3.7-sonnet': 0.025,
      'deepseek-r1': 0.01,
      'deepseek-v3': 0.008
    };

    let totalCost = 0;
    const aiModels = executionPlan.resource_allocation?.ai_models || {};
    Object.values(aiModels).forEach(model => {
      totalCost += (modelCosts[model] || 0.02) * 10; // 假设平均10K tokens
    });

    return {
      estimated_usd: totalCost.toFixed(4),
      currency: 'USD',
      breakdown: aiModels
    };
  }

  /**
   * 总结资源需求
   */
  summarizeResourceRequirements(executionPlan) {
    const allocation = executionPlan.resource_allocation || {};

    return {
      total_memory_mb: Object.values(allocation.memory || {}).reduce((sum, mem) => sum + mem, 0),
      total_cpu_cores: Math.max(...Object.values(allocation.compute || {}).map(c => c.cpu_cores || 1)),
      total_gpu_memory_mb: Object.values(allocation.compute || {}).reduce((sum, c) => sum + (c.gpu_memory || 0), 0),
      unique_models: [...new Set(Object.values(allocation.ai_models || {}))],
      unique_plugins: [...new Set(Object.values(allocation.plugins || {}).flat())]
    };
  }

  /**
   * 识别潜在瓶颈
   */
  identifyBottlenecks(executionPlan) {
    const bottlenecks = [];

    // 检查资源竞争
    const modelUsage = {};
    const aiModels = executionPlan.resource_allocation?.ai_models || {};
    Object.values(aiModels).forEach(model => {
      modelUsage[model] = (modelUsage[model] || 0) + 1;
    });

    Object.entries(modelUsage).forEach(([model, count]) => {
      if (count > 3) {
        bottlenecks.push({
          type: 'model_overuse',
          resource: model,
          severity: 'medium',
          description: `模型 ${model} 被过度使用 (${count} 次)`
        });
      }
    });

    // 检查执行时间
    if (executionPlan.estimated_total_time > 30) {
      bottlenecks.push({
        type: 'long_execution',
        severity: 'low',
        description: `预计执行时间较长 (${executionPlan.estimated_total_time} 分钟)`
      });
    }

    return bottlenecks;
  }

  /**
   * 生成优化建议
   */
  generateOptimizationSuggestions(executionPlan) {
    const suggestions = [];

    // 并行化建议
    if (executionPlan.parallel_groups.length > executionPlan.execution_order.length / 2) {
      suggestions.push({
        type: 'parallelization',
        description: '可以进一步优化任务并行执行',
        impact: 'medium'
      });
    }

    // 模型选择建议
    const modelCounts = {};
    const aiModels = executionPlan.resource_allocation?.ai_models || {};
    Object.values(aiModels).forEach(model => {
      modelCounts[model] = (modelCounts[model] || 0) + 1;
    });

    if (modelCounts['gpt-4o'] > 5) {
      suggestions.push({
        type: 'model_optimization',
        description: '考虑使用更经济的模型替代部分GPT-4o调用',
        impact: 'high'
      });
    }

    return suggestions;
  }
}

module.exports = AutoGPTPlanner;
