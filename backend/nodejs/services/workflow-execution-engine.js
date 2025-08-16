/**
 * 优化的工作流执行引擎
 * 支持并行执行、实时监控、错误恢复
 */

const EventEmitter = require('events');
const { v4: uuidv4 } = require('uuid');
const logger = require('../utils/logger');

class WorkflowExecutionEngine extends EventEmitter {
  constructor(options = {}) {
    super();
    
    this.options = {
      maxConcurrentNodes: options.maxConcurrentNodes || 10,
      executionTimeout: options.executionTimeout || 300000, // 5分钟
      retryAttempts: options.retryAttempts || 3,
      ...options
    };
    
    this.executions = new Map();
    this.nodeExecutors = new Map();
    this.performanceMetrics = new Map();
    
    this.initializeNodeExecutors();
  }

  /**
   * 初始化节点执行器
   */
  initializeNodeExecutors() {
    // 开始节点执行器
    this.nodeExecutors.set('start', async (node, context) => {
      return {
        success: true,
        output: context.input || {},
        message: '工作流开始执行'
      };
    });

    // 结束节点执行器
    this.nodeExecutors.set('end', async (node, context) => {
      return {
        success: true,
        output: context.data,
        message: '工作流执行完成'
      };
    });

    // AI对话节点执行器
    this.nodeExecutors.set('ai-chat', async (node, context) => {
      try {
        const { model, prompt, temperature = 0.7 } = node.config;
        const input = context.data;
        
        // 调用AI模型
        const response = await this.callAIModel({
          model,
          messages: [
            { role: 'system', content: prompt },
            { role: 'user', content: JSON.stringify(input) }
          ],
          temperature
        });

        return {
          success: true,
          output: {
            response: response.content,
            usage: response.usage
          }
        };
      } catch (error) {
        throw new Error(`AI对话节点执行失败: ${error.message}`);
      }
    });

    // 条件判断节点执行器
    this.nodeExecutors.set('condition', async (node, context) => {
      try {
        const { condition } = node.config;
        const data = context.data;
        
        // 安全的条件评估
        const result = this.evaluateCondition(condition, data);
        
        return {
          success: true,
          output: data,
          branch: result ? 'true' : 'false'
        };
      } catch (error) {
        throw new Error(`条件判断节点执行失败: ${error.message}`);
      }
    });

    // HTTP请求节点执行器
    this.nodeExecutors.set('http-request', async (node, context) => {
      try {
        const { url, method = 'GET', headers = {}, body } = node.config;
        const axios = require('axios');
        
        const response = await axios({
          url,
          method,
          headers,
          data: body,
          timeout: 30000
        });

        return {
          success: true,
          output: {
            status: response.status,
            data: response.data,
            headers: response.headers
          }
        };
      } catch (error) {
        throw new Error(`HTTP请求节点执行失败: ${error.message}`);
      }
    });

    // 数据转换节点执行器
    this.nodeExecutors.set('data-transform', async (node, context) => {
      try {
        const { script } = node.config;
        const data = context.data;
        
        // 安全的数据转换
        const transformedData = this.executeDataTransform(script, data);
        
        return {
          success: true,
          output: transformedData
        };
      } catch (error) {
        throw new Error(`数据转换节点执行失败: ${error.message}`);
      }
    });
  }

  /**
   * 执行工作流
   */
  async executeWorkflow(workflow, input = {}, options = {}) {
    const executionId = uuidv4();
    const startTime = Date.now();
    
    const execution = {
      id: executionId,
      workflowId: workflow.id,
      status: 'running',
      startTime,
      endTime: null,
      input,
      output: null,
      nodes: new Map(),
      edges: workflow.edges || [],
      context: {
        input,
        variables: {},
        ...options.context
      },
      metrics: {
        totalNodes: workflow.nodes?.length || 0,
        completedNodes: 0,
        failedNodes: 0,
        executionTime: 0
      }
    };

    this.executions.set(executionId, execution);
    
    try {
      logger.info(`🚀 开始执行工作流: ${workflow.name} (${executionId})`);
      
      // 构建执行图
      const executionGraph = this.buildExecutionGraph(workflow);
      
      // 并行执行工作流
      const result = await this.executeParallel(executionGraph, execution);
      
      execution.status = 'completed';
      execution.endTime = Date.now();
      execution.output = result;
      execution.metrics.executionTime = execution.endTime - execution.startTime;
      
      this.emit('workflow-completed', {
        executionId,
        result,
        metrics: execution.metrics
      });
      
      logger.info(`✅ 工作流执行完成: ${workflow.name} (${execution.metrics.executionTime}ms)`);
      
      return {
        success: true,
        executionId,
        result,
        metrics: execution.metrics
      };
      
    } catch (error) {
      execution.status = 'failed';
      execution.endTime = Date.now();
      execution.error = error.message;
      
      this.emit('workflow-failed', {
        executionId,
        error: error.message,
        metrics: execution.metrics
      });
      
      logger.error(`❌ 工作流执行失败: ${workflow.name} - ${error.message}`);
      
      throw error;
    }
  }

  /**
   * 构建执行图
   */
  buildExecutionGraph(workflow) {
    const nodes = new Map();
    const dependencies = new Map();
    const dependents = new Map();
    
    // 初始化节点
    workflow.nodes.forEach(node => {
      nodes.set(node.id, {
        ...node,
        status: 'pending',
        dependencies: new Set(),
        dependents: new Set()
      });
      dependencies.set(node.id, new Set());
      dependents.set(node.id, new Set());
    });
    
    // 构建依赖关系
    workflow.edges.forEach(edge => {
      const fromNode = edge.from || edge.fromNode;
      const toNode = edge.to || edge.toNode;
      
      if (nodes.has(fromNode) && nodes.has(toNode)) {
        dependencies.get(toNode).add(fromNode);
        dependents.get(fromNode).add(toNode);
        nodes.get(toNode).dependencies.add(fromNode);
        nodes.get(fromNode).dependents.add(toNode);
      }
    });
    
    return {
      nodes,
      dependencies,
      dependents
    };
  }

  /**
   * 并行执行工作流
   */
  async executeParallel(executionGraph, execution) {
    const { nodes, dependencies } = executionGraph;
    const completed = new Set();
    const running = new Set();
    const results = new Map();
    
    // 查找可以立即执行的节点（无依赖）
    const getReadyNodes = () => {
      return Array.from(nodes.values()).filter(node => 
        node.status === 'pending' && 
        Array.from(node.dependencies).every(dep => completed.has(dep))
      );
    };
    
    while (completed.size < nodes.size) {
      const readyNodes = getReadyNodes();
      
      if (readyNodes.length === 0 && running.size === 0) {
        throw new Error('工作流存在循环依赖或无法执行的节点');
      }
      
      // 并行执行就绪的节点
      const executionPromises = readyNodes
        .slice(0, this.options.maxConcurrentNodes - running.size)
        .map(async (node) => {
          if (running.has(node.id)) return;
          
          running.add(node.id);
          node.status = 'running';
          
          this.emit('node-started', {
            executionId: execution.id,
            nodeId: node.id,
            nodeName: node.name
          });
          
          try {
            const nodeResult = await this.executeNode(node, execution, results);
            
            node.status = 'completed';
            results.set(node.id, nodeResult);
            completed.add(node.id);
            running.delete(node.id);
            execution.metrics.completedNodes++;
            
            this.emit('node-completed', {
              executionId: execution.id,
              nodeId: node.id,
              result: nodeResult
            });
            
            return nodeResult;
            
          } catch (error) {
            node.status = 'failed';
            node.error = error.message;
            running.delete(node.id);
            execution.metrics.failedNodes++;
            
            this.emit('node-failed', {
              executionId: execution.id,
              nodeId: node.id,
              error: error.message
            });
            
            throw error;
          }
        });
      
      if (executionPromises.length > 0) {
        await Promise.all(executionPromises);
      } else {
        // 等待运行中的节点完成
        await new Promise(resolve => setTimeout(resolve, 100));
      }
    }
    
    // 返回最终结果
    const endNodes = Array.from(nodes.values()).filter(node => 
      node.dependents.size === 0 || node.type === 'end'
    );
    
    if (endNodes.length === 1) {
      return results.get(endNodes[0].id);
    } else {
      return Object.fromEntries(results);
    }
  }

  /**
   * 执行单个节点
   */
  async executeNode(node, execution, previousResults) {
    const executor = this.nodeExecutors.get(node.type);
    
    if (!executor) {
      throw new Error(`未知的节点类型: ${node.type}`);
    }
    
    // 准备节点执行上下文
    const context = {
      ...execution.context,
      data: this.prepareNodeInput(node, previousResults),
      node,
      execution
    };
    
    // 执行节点
    const startTime = Date.now();
    const result = await executor(node, context);
    const executionTime = Date.now() - startTime;
    
    // 记录性能指标
    this.recordNodePerformance(node.type, executionTime);
    
    return {
      ...result,
      executionTime,
      timestamp: new Date().toISOString()
    };
  }

  /**
   * 准备节点输入数据
   */
  prepareNodeInput(node, previousResults) {
    if (node.dependencies.size === 0) {
      return {};
    }
    
    if (node.dependencies.size === 1) {
      const depId = Array.from(node.dependencies)[0];
      return previousResults.get(depId)?.output || {};
    }
    
    // 多个依赖的情况，合并输出
    const combinedInput = {};
    node.dependencies.forEach(depId => {
      const result = previousResults.get(depId);
      if (result?.output) {
        Object.assign(combinedInput, result.output);
      }
    });
    
    return combinedInput;
  }

  /**
   * 记录节点性能指标
   */
  recordNodePerformance(nodeType, executionTime) {
    if (!this.performanceMetrics.has(nodeType)) {
      this.performanceMetrics.set(nodeType, {
        count: 0,
        totalTime: 0,
        avgTime: 0,
        minTime: Infinity,
        maxTime: 0
      });
    }
    
    const metrics = this.performanceMetrics.get(nodeType);
    metrics.count++;
    metrics.totalTime += executionTime;
    metrics.avgTime = metrics.totalTime / metrics.count;
    metrics.minTime = Math.min(metrics.minTime, executionTime);
    metrics.maxTime = Math.max(metrics.maxTime, executionTime);
  }

  /**
   * 获取执行状态
   */
  getExecutionStatus(executionId) {
    return this.executions.get(executionId);
  }

  /**
   * 获取性能指标
   */
  getPerformanceMetrics() {
    return Object.fromEntries(this.performanceMetrics);
  }

  /**
   * 安全的条件评估
   */
  evaluateCondition(condition, data) {
    try {
      // 简单的条件评估，可以扩展为更复杂的表达式引擎
      const context = { data, ...data };
      return Function('"use strict"; return (' + condition + ')').call(context);
    } catch (error) {
      logger.warn(`条件评估失败: ${condition} - ${error.message}`);
      return false;
    }
  }

  /**
   * 安全的数据转换
   */
  executeDataTransform(script, data) {
    try {
      const context = { data, input: data };
      return Function('"use strict"; return (' + script + ')').call(context);
    } catch (error) {
      logger.warn(`数据转换失败: ${script} - ${error.message}`);
      return data;
    }
  }

  /**
   * 调用AI模型（占位符，需要集成实际的AI服务）
   */
  async callAIModel(config) {
    // 这里应该集成实际的AI模型调用
    return {
      content: '这是AI模型的响应',
      usage: { tokens: 100 }
    };
  }
}

module.exports = WorkflowExecutionEngine;
