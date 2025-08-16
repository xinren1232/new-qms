/**
 * MCP (Model Context Protocol) 连接器插件
 * 支持连接和管理多个MCP服务，扩展AI能力
 */

const { spawn } = require('child_process');
const { EventEmitter } = require('events');
const { logger } = require('../middleware/error-handler');

class MCPConnector extends EventEmitter {
  constructor(config = {}) {
    super();
    
    this.config = {
      maxConnections: config.maxConnections || 10,
      connectionTimeout: config.connectionTimeout || 30000,
      retryAttempts: config.retryAttempts || 3,
      retryDelay: config.retryDelay || 2000,
      ...config
    };
    
    this.connections = new Map();
    this.services = new Map();
    this.enabled = false;
    
    // 插件元数据
    this.metadata = {
      id: 'mcp-connector',
      name: 'MCP服务连接器',
      version: '1.0.0',
      description: '连接Model Context Protocol服务，扩展AI能力',
      category: 'ai',
      features: ['protocol', 'context', 'tools', 'resources'],
      author: 'QMS AI Team',
      dependencies: ['child_process', 'events']
    };

    // 预定义的MCP服务
    this.predefinedServices = [
      {
        id: 'filesystem',
        name: '文件系统工具',
        description: '提供文件和目录操作功能',
        command: 'npx',
        args: ['-y', '@modelcontextprotocol/server-filesystem', '/path/to/allowed/files'],
        capabilities: ['tools', 'resources'],
        tools: ['read_file', 'write_file', 'create_directory', 'list_directory', 'move_file', 'search_files']
      },
      {
        id: 'git',
        name: 'Git工具',
        description: '提供Git版本控制操作',
        command: 'npx',
        args: ['-y', '@modelcontextprotocol/server-git', '--repository', '.'],
        capabilities: ['tools'],
        tools: ['git_status', 'git_diff', 'git_log', 'git_show', 'git_commit', 'git_push']
      },
      {
        id: 'postgres',
        name: 'PostgreSQL数据库',
        description: '连接和查询PostgreSQL数据库',
        command: 'npx',
        args: ['-y', '@modelcontextprotocol/server-postgres'],
        capabilities: ['tools', 'resources'],
        tools: ['query', 'describe_table', 'list_tables'],
        env: {
          POSTGRES_CONNECTION_STRING: process.env.POSTGRES_CONNECTION_STRING
        }
      },
      {
        id: 'web-search',
        name: '网络搜索',
        description: '提供网络搜索和网页抓取功能',
        command: 'npx',
        args: ['-y', '@modelcontextprotocol/server-brave-search'],
        capabilities: ['tools'],
        tools: ['brave_web_search'],
        env: {
          BRAVE_API_KEY: process.env.BRAVE_API_KEY
        }
      },
      {
        id: 'memory',
        name: '记忆存储',
        description: '提供持久化记忆存储功能',
        command: 'npx',
        args: ['-y', '@modelcontextprotocol/server-memory'],
        capabilities: ['tools', 'resources'],
        tools: ['create_memory', 'search_memories', 'update_memory', 'delete_memory']
      }
    ];
  }

  /**
   * 初始化插件
   */
  async initialize() {
    try {
      logger.info('🔌 初始化MCP连接器插件...');
      
      // 检查Node.js版本
      const nodeVersion = process.version;
      const majorVersion = parseInt(nodeVersion.slice(1).split('.')[0]);
      if (majorVersion < 18) {
        throw new Error('MCP连接器需要Node.js 18或更高版本');
      }

      this.enabled = true;
      logger.info('✅ MCP连接器插件初始化成功');
      
      return {
        success: true,
        message: 'MCP连接器初始化成功',
        metadata: this.metadata,
        availableServices: this.predefinedServices.map(s => ({
          id: s.id,
          name: s.name,
          description: s.description,
          capabilities: s.capabilities,
          tools: s.tools
        }))
      };
    } catch (error) {
      logger.error('❌ MCP连接器初始化失败:', error);
      return {
        success: false,
        message: error.message,
        metadata: this.metadata
      };
    }
  }

  /**
   * 连接MCP服务
   * @param {string} serviceId - 服务ID
   * @param {Object} customConfig - 自定义配置
   */
  async connectService(serviceId, customConfig = {}) {
    try {
      if (this.connections.has(serviceId)) {
        throw new Error(`服务 ${serviceId} 已经连接`);
      }

      if (this.connections.size >= this.config.maxConnections) {
        throw new Error(`已达到最大连接数限制: ${this.config.maxConnections}`);
      }

      const serviceConfig = this.predefinedServices.find(s => s.id === serviceId);
      if (!serviceConfig) {
        throw new Error(`未找到服务配置: ${serviceId}`);
      }

      logger.info(`🔗 连接MCP服务: ${serviceConfig.name}`);

      // 合并配置
      const finalConfig = { ...serviceConfig, ...customConfig };

      // 启动MCP服务进程
      const mcpProcess = spawn(finalConfig.command, finalConfig.args, {
        stdio: ['pipe', 'pipe', 'pipe'],
        env: { ...process.env, ...finalConfig.env }
      });

      // 创建连接对象
      const connection = {
        id: serviceId,
        name: serviceConfig.name,
        process: mcpProcess,
        status: 'connecting',
        capabilities: serviceConfig.capabilities,
        tools: serviceConfig.tools,
        createdAt: new Date(),
        lastActivity: new Date()
      };

      this.connections.set(serviceId, connection);

      // 设置进程事件监听
      mcpProcess.on('error', (error) => {
        logger.error(`❌ MCP服务 ${serviceId} 进程错误:`, error);
        connection.status = 'error';
        this.emit('service-error', { serviceId, error });
      });

      mcpProcess.on('exit', (code, signal) => {
        logger.warn(`⚠️ MCP服务 ${serviceId} 进程退出: code=${code}, signal=${signal}`);
        connection.status = 'disconnected';
        this.connections.delete(serviceId);
        this.emit('service-disconnected', { serviceId, code, signal });
      });

      // 等待连接建立
      await new Promise((resolve, reject) => {
        const timeout = setTimeout(() => {
          reject(new Error(`连接超时: ${serviceId}`));
        }, this.config.connectionTimeout);

        mcpProcess.stdout.once('data', (data) => {
          clearTimeout(timeout);
          connection.status = 'connected';
          logger.info(`✅ MCP服务 ${serviceId} 连接成功`);
          this.emit('service-connected', { serviceId, connection });
          resolve();
        });
      });

      return {
        success: true,
        serviceId,
        connection: {
          id: connection.id,
          name: connection.name,
          status: connection.status,
          capabilities: connection.capabilities,
          tools: connection.tools,
          createdAt: connection.createdAt
        }
      };
    } catch (error) {
      logger.error(`❌ 连接MCP服务失败 ${serviceId}:`, error);
      
      // 清理失败的连接
      if (this.connections.has(serviceId)) {
        const connection = this.connections.get(serviceId);
        if (connection.process) {
          connection.process.kill();
        }
        this.connections.delete(serviceId);
      }
      
      throw error;
    }
  }

  /**
   * 断开MCP服务
   * @param {string} serviceId - 服务ID
   */
  async disconnectService(serviceId) {
    try {
      const connection = this.connections.get(serviceId);
      if (!connection) {
        throw new Error(`服务 ${serviceId} 未连接`);
      }

      logger.info(`🔌 断开MCP服务: ${serviceId}`);

      // 终止进程
      if (connection.process) {
        connection.process.kill('SIGTERM');
        
        // 等待进程退出，如果超时则强制杀死
        setTimeout(() => {
          if (!connection.process.killed) {
            connection.process.kill('SIGKILL');
          }
        }, 5000);
      }

      this.connections.delete(serviceId);
      
      return {
        success: true,
        serviceId,
        message: '服务已断开'
      };
    } catch (error) {
      logger.error(`❌ 断开MCP服务失败 ${serviceId}:`, error);
      throw error;
    }
  }

  /**
   * 调用MCP工具
   * @param {string} serviceId - 服务ID
   * @param {string} toolName - 工具名称
   * @param {Object} args - 工具参数
   */
  async callTool(serviceId, toolName, args = {}) {
    try {
      const connection = this.connections.get(serviceId);
      if (!connection || connection.status !== 'connected') {
        throw new Error(`服务 ${serviceId} 未连接或不可用`);
      }

      if (!connection.tools.includes(toolName)) {
        throw new Error(`服务 ${serviceId} 不支持工具: ${toolName}`);
      }

      logger.info(`🔧 调用MCP工具: ${serviceId}.${toolName}`);

      // 构建MCP请求
      const request = {
        jsonrpc: '2.0',
        id: Date.now(),
        method: 'tools/call',
        params: {
          name: toolName,
          arguments: args
        }
      };

      // 发送请求到MCP服务
      const requestData = JSON.stringify(request) + '\n';
      connection.process.stdin.write(requestData);

      // 等待响应
      const response = await new Promise((resolve, reject) => {
        const timeout = setTimeout(() => {
          reject(new Error(`工具调用超时: ${toolName}`));
        }, 30000);

        const onData = (data) => {
          clearTimeout(timeout);
          connection.process.stdout.off('data', onData);
          
          try {
            const response = JSON.parse(data.toString());
            resolve(response);
          } catch (error) {
            reject(new Error(`解析响应失败: ${error.message}`));
          }
        };

        connection.process.stdout.on('data', onData);
      });

      connection.lastActivity = new Date();

      if (response.error) {
        throw new Error(`工具调用失败: ${response.error.message}`);
      }

      return {
        success: true,
        result: response.result,
        toolName,
        serviceId
      };
    } catch (error) {
      logger.error(`❌ MCP工具调用失败 ${serviceId}.${toolName}:`, error);
      throw error;
    }
  }

  /**
   * 获取连接状态
   */
  getConnections() {
    const connections = [];
    
    for (const [serviceId, connection] of this.connections) {
      connections.push({
        id: connection.id,
        name: connection.name,
        status: connection.status,
        capabilities: connection.capabilities,
        tools: connection.tools,
        createdAt: connection.createdAt,
        lastActivity: connection.lastActivity,
        uptime: Date.now() - connection.createdAt.getTime()
      });
    }
    
    return connections;
  }

  /**
   * 获取可用服务列表
   */
  getAvailableServices() {
    return this.predefinedServices.map(service => ({
      id: service.id,
      name: service.name,
      description: service.description,
      capabilities: service.capabilities,
      tools: service.tools,
      connected: this.connections.has(service.id)
    }));
  }

  /**
   * 插件健康检查
   */
  async healthCheck() {
    try {
      if (!this.enabled) {
        return {
          status: 'disabled',
          message: '插件未启用'
        };
      }

      const connections = this.getConnections();
      const healthyConnections = connections.filter(c => c.status === 'connected');
      
      return {
        status: 'healthy',
        message: '插件运行正常',
        totalConnections: connections.length,
        healthyConnections: healthyConnections.length,
        connections: connections
      };
    } catch (error) {
      return {
        status: 'error',
        message: error.message
      };
    }
  }

  /**
   * 获取插件配置
   */
  getConfig() {
    return {
      ...this.metadata,
      enabled: this.enabled,
      config: this.config,
      connections: this.getConnections(),
      availableServices: this.getAvailableServices()
    };
  }

  /**
   * 清理资源
   */
  async cleanup() {
    logger.info('🧹 清理MCP连接器资源...');
    
    const disconnectPromises = [];
    for (const serviceId of this.connections.keys()) {
      disconnectPromises.push(this.disconnectService(serviceId).catch(error => {
        logger.error(`清理服务失败 ${serviceId}:`, error);
      }));
    }
    
    await Promise.all(disconnectPromises);
    this.enabled = false;
    
    logger.info('✅ MCP连接器资源清理完成');
  }
}

module.exports = MCPConnector;
