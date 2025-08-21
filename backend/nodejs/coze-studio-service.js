/**
 * Coze Studio 后端服务
 * 提供Agent、Workflow、Knowledge、Plugin等核心功能的API
 */

require('dotenv').config();
const express = require('express');
const cors = require('cors');
const { v4: uuidv4 } = require('uuid');
const DatabaseAdapter = require('./database/database-adapter');
const RedisCacheService = require('./services/redis-cache-service');
const AutoGPTPlanner = require('./services/autogpt-planner');
const AutoGPTExecutor = require('./services/autogpt-executor');
const CrewAICoordinator = require('./services/crewai-coordinator');
const LangChainMemoryManager = require('./services/langchain-memory');
const PluginEcosystemManager = require('./services/plugin-ecosystem');
const DocumentFormatDetector = require('./services/document-format-detector');
const axios = require('axios');

// const fs = require('fs'); // 暂未使用
// const path = require('path'); // 暂未使用
// let XLSX = null; // 延迟加载，避免启动时开销 // 暂未使用

const app = express();
const PORT = process.env.COZE_STUDIO_PORT || 3005;

// 简化的编码检测和解码函数
function detectAndDecodeText(buffer) {
  if (!buffer || buffer.length === 0) return '';

  console.log('🔤 开始编码检测，buffer长度:', buffer.length);

  // 检测BOM标记
  if (buffer.length >= 3 && buffer[0] === 0xEF && buffer[1] === 0xBB && buffer[2] === 0xBF) {
    console.log('🔤 检测到UTF-8 BOM');
    return buffer.slice(3).toString('utf-8');
  }
  if (buffer.length >= 2 && buffer[0] === 0xFF && buffer[1] === 0xFE) {
    console.log('🔤 检测到UTF-16LE BOM');
    return buffer.slice(2).toString('utf-16le');
  }
  if (buffer.length >= 2 && buffer[0] === 0xFE && buffer[1] === 0xFF) {
    console.log('🔤 检测到UTF-16BE BOM');
    return buffer.slice(2).toString('utf-16be');
  }

  try {
    // 1. 首先尝试UTF-8解码
    const utf8Text = buffer.toString('utf-8');
    // 检查是否包含UTF-8替换字符（乱码标志）
    if (!utf8Text.includes('\uFFFD') && utf8Text.length > 0) {
      console.log('🔤 使用UTF-8编码');
      return utf8Text;
    }
  } catch (e) {
    console.warn('UTF-8解码失败:', e.message);
  }

  try {
    // 2. 尝试GBK/GB2312解码（中文常用）
    const iconv = require('iconv-lite');
    if (iconv.encodingExists('gbk')) {
      const gbkText = iconv.decode(buffer, 'gbk');
      if (gbkText && gbkText.length > 0 && !gbkText.includes('\uFFFD')) {
        console.log('🔤 检测到GBK编码');
        return gbkText;
      }
    }
  } catch (e) {
    console.warn('GBK解码失败，可能未安装iconv-lite:', e.message);
  }

  try {
    // 3. 尝试Latin-1解码
    const latin1Text = buffer.toString('latin1');
    if (latin1Text && latin1Text.length > 0) {
      console.log('🔤 使用Latin-1编码');
      return latin1Text;
    }
  } catch (e) {
    console.warn('Latin-1解码失败:', e.message);
  }

  // 4. 最后回退到二进制字符串
  console.warn('🔤 所有编码检测失败，使用二进制回退');
  return buffer.toString('binary');
}

// 中间件配置
app.use(cors());
app.use(express.json({ limit: '50mb' }));
app.use(express.urlencoded({ extended: true, limit: '50mb' }));

// 初始化数据库和缓存
const dbAdapter = new DatabaseAdapter();
const cacheService = new RedisCacheService();

// 初始化AutoGPT组件
const autoGPTPlanner = new AutoGPTPlanner(callAIModel);
const autoGPTExecutor = new AutoGPTExecutor(callAIModel, dbAdapter);

// 初始化智能模型选择器
const SmartModelSelector = require('./services/smart-model-selector');
const smartModelSelector = new SmartModelSelector();

// 初始化CrewAI协调器
const crewAICoordinator = new CrewAICoordinator(callAIModel, dbAdapter, smartModelSelector);
// 简易内存执行存储（演示用途；生产应使用DB）
const executionStore = new Map();


// 初始化LangChain Memory管理器
const langChainMemory = new LangChainMemoryManager(callAIModel, dbAdapter, smartModelSelector);

// 初始化插件生态系统管理器
const pluginEcosystem = new PluginEcosystemManager(callAIModel, dbAdapter, smartModelSelector);

// 初始化文档格式检测器
const formatDetector = new DocumentFormatDetector();

// 缓存辅助函数
const cacheHelper = {
  // 获取用户Agent列表缓存
  async getUserAgents(userId) {
    try {
      return await cacheService.get('user_session', `agents:${userId}`);
    } catch (error) {
      console.warn('获取用户Agent缓存失败:', error.message);
      return null;
    }
  },

  // 设置用户Agent列表缓存
  async setUserAgents(userId, agents) {
    try {
      return await cacheService.set('user_session', `agents:${userId}`, agents);
    } catch (error) {
      console.warn('设置用户Agent缓存失败:', error.message);
      return false;
    }
  },

  // 获取单个Agent缓存
  async getAgent(agentId) {
    try {
      return await cacheService.get('user_session', `agent:${agentId}`);
    } catch (error) {
      console.warn('获取Agent缓存失败:', error.message);
      return null;
    }
  },

  // 设置单个Agent缓存
  async setAgent(agentId, agent) {
    try {
      return await cacheService.set('user_session', `agent:${agentId}`, agent);
    } catch (error) {
      console.warn('设置Agent缓存失败:', error.message);
      return false;
    }
  },

  // 删除用户Agent列表缓存
  async deleteUserAgents(userId) {
    try {
      return await cacheService.del('user_session', `agents:${userId}`);
    } catch (error) {
      console.warn('删除用户Agent缓存失败:', error.message);
      return false;
    }
  },

  // 删除单个Agent缓存
  async deleteAgent(agentId) {
    try {
      return await cacheService.del('user_session', `agent:${agentId}`);
    } catch (error) {
      console.warn('删除Agent缓存失败:', error.message);
      return false;
    }
  }
};

// 简单的认证中间件
const authenticateUser = (req, res, next) => {
  const token = req.headers.authorization?.replace('Bearer ', '');
  if (!token) {
    return res.status(401).json({ success: false, message: '未提供认证令牌' });
  }
  // 简化认证，实际应该验证JWT
  // 生成更真实的用户ID，避免undefined问题
  const userId = token.includes('test') ? 'test_user' : 'user_1';
  req.user = { id: userId, username: 'admin' };
  next();
};

// 支持匿名访问的认证中间件
const authenticateUserOrAnonymous = (req, res, next) => {
  const token = req.headers.authorization?.replace('Bearer ', '');
  if (token) {
    // 有token，正常认证
    const userId = token.includes('test') ? 'test_user' : 'user_1';
    req.user = { id: userId, username: 'admin' };
  } else {
    // 没有token，使用匿名用户
    req.user = { id: 'anonymous', username: 'anonymous' };
  }
  next();
};

// ================================
// Agent 管理 API
// ================================

/**
 * 获取Agent列表
 */
app.get('/api/agents', authenticateUser, async (req, res) => {
  try {
    const { page = 1, limit = 20, search = '', category = '' } = req.query;
    const pageNum = parseInt(page) || 1;
    const limitNum = parseInt(limit) || 20;
    const offset = (pageNum - 1) * limitNum;

    let query = `
      SELECT * FROM agents
      WHERE user_id = ? AND deleted_at IS NULL
    `;
    const params = [req.user.id];

    if (search) {
      query += ` AND (name LIKE ? OR description LIKE ?)`;
      params.push(`%${search}%`, `%${search}%`);
    }

    if (category) {
      query += ` AND category = ?`;
      params.push(category);
    }

    query += ` ORDER BY updated_at DESC LIMIT ? OFFSET ?`;
    params.push(limitNum, offset);

    const agents = await dbAdapter.query(query, params);

    // 获取总数
    const countQuery = `
      SELECT COUNT(*) as total FROM agents
      WHERE user_id = ? AND deleted_at IS NULL
    `;
    const countParams = [req.user.id];
    const [{ total }] = await dbAdapter.query(countQuery, countParams);

    res.json({
      success: true,
      data: {
        agents: agents.map(agent => ({
          ...agent,
          config: JSON.parse(agent.config || '{}'),
          metadata: JSON.parse(agent.metadata || '{}')
        })),
        pagination: {
          page: pageNum,
          limit: limitNum,
          total,
          pages: Math.ceil(total / limitNum)
        }
      }
    });
  } catch (error) {
    console.error('获取Agent列表失败:', error);
    res.status(500).json({
      success: false,
      message: '获取Agent列表失败',
      error: error.message
    });
  }
});

/**
 * 创建新Agent
 */
app.post('/api/agents', authenticateUser, async (req, res) => {
  try {
    const {
      name,
      description,
      avatar,
      category = 'general',
      config = {},
      metadata = {}
    } = req.body;

    if (!name || !description) {
      return res.status(400).json({
        success: false,
        message: '名称和描述不能为空'
      });
    }

    const agentId = uuidv4();
    const now = new Date().toISOString();

    const defaultConfig = {
      model: 'gpt-4o',
      temperature: 0.7,
      maxTokens: 2048,
      systemPrompt: '你是一个专业的AI助手，能够帮助用户解决各种问题。',
      userPrompt: '',
      features: {
        memory: true,
        webSearch: false,
        imageGeneration: false,
        codeExecution: false
      },
      ...config
    };

    const query = `
      INSERT INTO agents (
        id, user_id, name, description, avatar, category,
        config, metadata, status, created_at, updated_at
      ) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
    `;

    await dbAdapter.query(query, [
      agentId,
      req.user.id,
      name,
      description,
      avatar || '',
      category,
      JSON.stringify(defaultConfig),
      JSON.stringify(metadata),
      'draft',
      now,
      now
    ]);

    // 清除缓存
    await cacheHelper.deleteUserAgents(req.user.id);

    res.status(201).json({
      success: true,
      data: {
        id: agentId,
        name,
        description,
        avatar,
        category,
        config: defaultConfig,
        metadata,
        status: 'draft',
        created_at: now,
        updated_at: now
      },
      message: 'Agent创建成功'
    });
  } catch (error) {
    console.error('创建Agent失败:', error);
    res.status(500).json({
      success: false,
      message: '创建Agent失败',
      error: error.message
    });
  }
});

/**
 * 获取单个Agent详情
 */
app.get('/api/agents/:id', authenticateUser, async (req, res) => {
  try {
    const { id } = req.params;

    const query = `
      SELECT * FROM agents
      WHERE id = ? AND user_id = ? AND deleted_at IS NULL
    `;

    const [agent] = await dbAdapter.query(query, [id, req.user.id]);

    if (!agent) {
      return res.status(404).json({
        success: false,
        message: 'Agent不存在'
      });
    }

    res.json({
      success: true,
      data: {
        ...agent,
        config: JSON.parse(agent.config || '{}'),
        metadata: JSON.parse(agent.metadata || '{}')
      }
    });
  } catch (error) {
    console.error('获取Agent详情失败:', error);
    res.status(500).json({
      success: false,
      message: '获取Agent详情失败',
      error: error.message
    });
  }
});

/**
 * 更新Agent
 */
app.put('/api/agents/:id', authenticateUser, async (req, res) => {
  try {
    const { id } = req.params;
    const {
      name,
      description,
      avatar,
      category,
      config,
      metadata,
      status
    } = req.body;

    // 检查Agent是否存在
    const checkQuery = `
      SELECT id FROM agents
      WHERE id = ? AND user_id = ? AND deleted_at IS NULL
    `;
    const [existingAgent] = await dbAdapter.query(checkQuery, [id, req.user.id]);

    if (!existingAgent) {
      return res.status(404).json({
        success: false,
        message: 'Agent不存在'
      });
    }

    const updateFields = [];
    const updateParams = [];

    if (name !== undefined) {
      updateFields.push('name = ?');
      updateParams.push(name);
    }
    if (description !== undefined) {
      updateFields.push('description = ?');
      updateParams.push(description);
    }
    if (avatar !== undefined) {
      updateFields.push('avatar = ?');
      updateParams.push(avatar);
    }
    if (category !== undefined) {
      updateFields.push('category = ?');
      updateParams.push(category);
    }
    if (config !== undefined) {
      updateFields.push('config = ?');
      updateParams.push(JSON.stringify(config));
    }
    if (metadata !== undefined) {
      updateFields.push('metadata = ?');
      updateParams.push(JSON.stringify(metadata));
    }
    if (status !== undefined) {
      updateFields.push('status = ?');
      updateParams.push(status);
    }

    updateFields.push('updated_at = ?');
    updateParams.push(new Date().toISOString());
    updateParams.push(id, req.user.id);

    const updateQuery = `
      UPDATE agents
      SET ${updateFields.join(', ')}
      WHERE id = ? AND user_id = ?
    `;

    await dbAdapter.query(updateQuery, updateParams);

    // 清除缓存
    await cacheHelper.deleteAgent(id);
    await cacheHelper.deleteUserAgents(req.user.id);

    res.json({
      success: true,
      message: 'Agent更新成功'
    });
  } catch (error) {
    console.error('更新Agent失败:', error);
    res.status(500).json({
      success: false,
      message: '更新Agent失败',
      error: error.message
    });
  }
});

/**
 * 删除Agent
 */
app.delete('/api/agents/:id', authenticateUser, async (req, res) => {
  try {
    const { id } = req.params;

    const query = `
      UPDATE agents
      SET deleted_at = ?, updated_at = ?
      WHERE id = ? AND user_id = ? AND deleted_at IS NULL
    `;

    const now = new Date().toISOString();
    const result = await dbAdapter.query(query, [now, now, id, req.user.id]);

    if (result.changes === 0) {
      return res.status(404).json({
        success: false,
        message: 'Agent不存在'
      });
    }

    // 清除缓存
    await cacheHelper.deleteAgent(id);
    await cacheHelper.deleteUserAgents(req.user.id);

    res.json({
      success: true,
      message: 'Agent删除成功'
    });
  } catch (error) {
    console.error('删除Agent失败:', error);
    res.status(500).json({
      success: false,
      message: '删除Agent失败',
      error: error.message
    });
  }
});

/**
 * 复制Agent
 */
app.post('/api/agents/:id/clone', authenticateUser, async (req, res) => {
  try {
    const { id } = req.params;
    const { name } = req.body;

    // 获取原Agent
    const [originalAgent] = await dbAdapter.query(
      'SELECT * FROM agents WHERE id = ? AND user_id = ? AND deleted_at IS NULL',
      [id, req.user.id]
    );

    if (!originalAgent) {
      return res.status(404).json({
        success: false,
        message: 'Agent不存在'
      });
    }

    const newAgentId = uuidv4();
    const now = new Date().toISOString();
    const cloneName = name || `${originalAgent.name} (副本)`;

    await dbAdapter.query(`
      INSERT INTO agents (
        id, user_id, name, description, avatar, category,
        config, metadata, status, created_at, updated_at
      ) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
    `, [
      newAgentId,
      req.user.id,
      cloneName,
      originalAgent.description,
      originalAgent.avatar,
      originalAgent.category,
      originalAgent.config,
      originalAgent.metadata,
      'draft',
      now,
      now
    ]);

    res.status(201).json({
      success: true,
      data: {
        id: newAgentId,
        name: cloneName,
        status: 'draft'
      },
      message: 'Agent复制成功'
    });
  } catch (error) {
    console.error('复制Agent失败:', error);
    res.status(500).json({
      success: false,
      message: '复制Agent失败',
      error: error.message
    });
  }
});

/**
 * 发布Agent
 */
app.post('/api/agents/:id/publish', authenticateUser, async (req, res) => {
  try {
    const { id } = req.params;

    // 检查Agent是否存在
    const [agent] = await dbAdapter.query(
      'SELECT * FROM agents WHERE id = ? AND user_id = ? AND deleted_at IS NULL',
      [id, req.user.id]
    );

    if (!agent) {
      return res.status(404).json({
        success: false,
        message: 'Agent不存在'
      });
    }

    const config = JSON.parse(agent.config || '{}');

    // 验证Agent配置完整性
    if (!config.systemPrompt || !agent.name || !agent.description) {
      return res.status(400).json({
        success: false,
        message: 'Agent配置不完整，无法发布'
      });
    }

    const now = new Date().toISOString();
    await dbAdapter.query(
      'UPDATE agents SET status = ?, version = version + 1, updated_at = ? WHERE id = ?',
      ['published', now, id]
    );

    res.json({
      success: true,
      message: 'Agent发布成功'
    });
  } catch (error) {
    console.error('发布Agent失败:', error);
    res.status(500).json({
      success: false,
      message: '发布Agent失败',
      error: error.message
    });
  }
});

/**
 * 测试Agent
 */
app.post('/api/agents/:id/test', authenticateUser, async (req, res) => {
  try {
    const { id } = req.params;
    const { message, useMemory = true, conversationId } = req.body;

    if (!message) {
      return res.status(400).json({
        success: false,
        message: '测试消息不能为空'
      });
    }

    // 获取Agent配置
    const [agent] = await dbAdapter.query(
      'SELECT * FROM agents WHERE id = ? AND user_id = ? AND deleted_at IS NULL',
      [id, req.user.id]
    );

    if (!agent) {
      return res.status(404).json({
        success: false,
        message: 'Agent不存在'
      });
    }

    const agentConfig = JSON.parse(agent.config || '{}');
    let memoryContext = null;

    // 如果启用记忆系统，检索相关记忆
    if (useMemory) {
      try {
        // 确保Agent有记忆系统
        const memoryStats = await langChainMemory.getMemoryStats(id);
        if (memoryStats.buffer_memories === 0 && memoryStats.entities === 0) {
          // 创建记忆系统
          await langChainMemory.createMemoryForAgent(id, {
            types: ['buffer', 'summary', 'entity'],
            maxBufferSize: 10,
            summaryThreshold: 20,
            entityExtractionEnabled: true
          });
        }

        // 检索相关记忆
        memoryContext = await langChainMemory.retrieveRelevantMemory(id, message, {
          includeBuffer: true,
          includeSummary: true,
          includeEntities: true,
          maxResults: 5
        });

        // 构建增强的消息（包含记忆上下文）
        if (memoryContext.buffer.length > 0 || memoryContext.entities.length > 0) {
          let contextInfo = '\n\n=== 相关记忆上下文 ===\n';

          if (memoryContext.summary) {
            contextInfo += `对话摘要: ${memoryContext.summary.content}\n\n`;
          }

          if (memoryContext.buffer.length > 0) {
            contextInfo += '最近对话:\n';
            memoryContext.buffer.forEach((mem, index) => {
              contextInfo += `${index + 1}. 用户: ${mem.user_message}\n   回复: ${mem.agent_response}\n`;
            });
            contextInfo += '\n';
          }

          if (memoryContext.entities.length > 0) {
            contextInfo += '相关实体:\n';
            memoryContext.entities.forEach(entity => {
              contextInfo += `- ${entity.name} (${entity.type}): ${entity.description}\n`;
            });
          }

          contextInfo += '=== 记忆上下文结束 ===\n\n';

          // 更新系统提示词以包含记忆上下文
          const originalSystemPrompt = agentConfig.systemPrompt || '';
          agentConfig.systemPrompt = originalSystemPrompt + '\n\n' + contextInfo + '请基于以上记忆上下文回答用户的问题。';
        }
      } catch (memoryError) {
        console.warn('记忆系统处理失败:', memoryError.message);
        // 记忆失败不影响正常对话
      }
    }

    // 调用AI模型进行测试
    const response = await callAIModel(agentConfig, [], message);

    // 如果启用记忆系统，保存对话记忆
    if (useMemory) {
      try {
        await langChainMemory.addConversationMemory(
          id,
          conversationId || `test_${Date.now()}`,
          message,
          response.content
        );
      } catch (memoryError) {
        console.warn('保存对话记忆失败:', memoryError.message);
      }
    }

    res.json({
      success: true,
      data: {
        input: message,
        output: response.content,
        model: agentConfig.model,
        usage: response.usage,
        memory_used: useMemory,
        memory_context: memoryContext ? {
          buffer_count: memoryContext.buffer.length,
          entities_count: memoryContext.entities.length,
          has_summary: !!memoryContext.summary
        } : null
      }
    });
  } catch (error) {
    console.error('测试Agent失败:', error);
    res.status(500).json({
      success: false,
      message: '测试Agent失败',
      error: error.message
    });
  }
});

// ================================
// Agent 对话 API
// ================================

/**
 * 创建Agent对话会话
 */
app.post('/api/agents/:id/conversations', authenticateUser, async (req, res) => {
  try {
    const { id: agentId } = req.params;
    const { title = '新对话' } = req.body;

    // 检查Agent是否存在
    const [agent] = await dbAdapter.query(
      'SELECT id, config FROM agents WHERE id = ? AND user_id = ? AND deleted_at IS NULL',
      [agentId, req.user.id]
    );

    if (!agent) {
      return res.status(404).json({
        success: false,
        message: 'Agent不存在'
      });
    }

    const conversationId = uuidv4();
    const now = new Date().toISOString();

    await dbAdapter.query(`
      INSERT INTO agent_conversations (
        id, agent_id, user_id, title, context, status, created_at, updated_at
      ) VALUES (?, ?, ?, ?, ?, ?, ?, ?)
    `, [
      conversationId,
      agentId,
      req.user.id,
      title,
      JSON.stringify({ messages: [] }),
      'active',
      now,
      now
    ]);

    res.status(201).json({
      success: true,
      data: {
        id: conversationId,
        agent_id: agentId,
        title,
        status: 'active',
        created_at: now
      },
      message: '对话会话创建成功'
    });
  } catch (error) {
    console.error('创建Agent对话会话失败:', error);
    res.status(500).json({
      success: false,
      message: '创建对话会话失败',
      error: error.message
    });
  }
});

/**
 * Agent对话处理
 */
app.post('/api/agents/:id/chat', authenticateUser, async (req, res) => {
  try {
    const { id: agentId } = req.params;
    const { message, conversation_id } = req.body;

    if (!message || !conversation_id) {
      return res.status(400).json({
        success: false,
        message: '消息内容和对话ID不能为空'
      });
    }

    // 获取Agent配置
    const [agent] = await dbAdapter.query(
      'SELECT * FROM agents WHERE id = ? AND user_id = ? AND deleted_at IS NULL',
      [agentId, req.user.id]
    );

    if (!agent) {
      return res.status(404).json({
        success: false,
        message: 'Agent不存在'
      });
    }

    // 获取对话上下文
    const [conversation] = await dbAdapter.query(
      'SELECT * FROM agent_conversations WHERE id = ? AND agent_id = ? AND user_id = ?',
      [conversation_id, agentId, req.user.id]
    );

    if (!conversation) {
      return res.status(404).json({
        success: false,
        message: '对话会话不存在'
      });
    }

    const agentConfig = JSON.parse(agent.config || '{}');
    const context = JSON.parse(conversation.context || '{"messages":[]}');

    // 保存用户消息
    const userMessageId = uuidv4();
    const now = new Date().toISOString();

    await dbAdapter.query(`
      INSERT INTO agent_messages (
        id, conversation_id, role, content, metadata, created_at
      ) VALUES (?, ?, ?, ?, ?, ?)
    `, [
      userMessageId,
      conversation_id,
      'user',
      message,
      JSON.stringify({ timestamp: now }),
      now
    ]);

    // RAG（轻量）
    const { use_rag = true, kb_scope = 'user', top_k = 3 } = req.body || {};
    let ragContext = '';
    let ragSources = [];
    if (use_rag) {
      const scopeKey = kb_scope === 'session' && conversation_id ? `${req.user.id}:${conversation_id}` : `${req.user.id}`;
      const chunks = await kbGetChunks(cacheService, scopeKey);
      if (chunks && chunks.length) {
        const scored = chunks.map(ch => ({ ch, score: scoreByKeywordOverlap(message, ch.text) }))
          .sort((a,b)=>b.score-a.score).slice(0, Math.max(1, Math.min(10, top_k)));
        ragSources = scored.map(s => ({ title: s.ch.title, idx: s.ch.idx, docId: s.ch.docId, score: s.score }));
        ragContext = scored.map(s => s.ch.text).join('\n---\n');
      }
    }

    // 构建AI请求（将RAG上下文作为system或前置消息）
    const messagesForAI = [...context.messages];
    if (ragContext) {
      messagesForAI.unshift({ role: 'system', content: `你是质量管理助手。以下是与问题相关的资料片段（可能包含来自用户知识库的内容），请优先基于这些资料作答，并在无法确定时说明不确定性：\n${ragContext}` });
    }
    const aiResponse = await callAIModel(agentConfig, messagesForAI, message);

    // 保存AI回复
    const assistantMessageId = uuidv4();
    await dbAdapter.query(`
      INSERT INTO agent_messages (
        id, conversation_id, role, content, metadata, created_at
      ) VALUES (?, ?, ?, ?, ?, ?)
    `, [
      assistantMessageId,
      conversation_id,
      'assistant',
      aiResponse.content,
      JSON.stringify({
        model: agentConfig.model,
        usage: aiResponse.usage,
        rag: { enabled: !!use_rag, sources: ragSources },
        timestamp: now
      }),
      now
    ]);

    // 更新对话上下文
    context.messages.push(
      { role: 'user', content: message },
      { role: 'assistant', content: aiResponse.content }
    );

    // 保持上下文长度在合理范围内
    if (context.messages.length > 20) {
      context.messages = context.messages.slice(-20);
    }

    await dbAdapter.query(
      'UPDATE agent_conversations SET context = ?, updated_at = ? WHERE id = ?',
      [JSON.stringify(context), now, conversation_id]
    );

    res.json({
      success: true,
      data: {
        message_id: assistantMessageId,
        content: aiResponse.content,
        model: agentConfig.model,
        usage: aiResponse.usage
      }
    });
  } catch (error) {
    console.error('Agent对话处理失败:', error);
    res.status(500).json({
      success: false,
      message: 'Agent对话处理失败',
      error: error.message
    });
  }
});

/**
 * 获取Agent对话历史
 */
app.get('/api/agents/:id/conversations/:conversationId/messages', authenticateUser, async (req, res) => {
  try {
    const { conversationId } = req.params;
    const { page = 1, limit = 50 } = req.query;
    const pageNum = parseInt(page) || 1;
    const limitNum = parseInt(limit) || 50;
    const offset = (pageNum - 1) * limitNum;

    const messages = await dbAdapter.query(`
      SELECT * FROM agent_messages
      WHERE conversation_id = ?
      ORDER BY created_at ASC
      LIMIT ? OFFSET ?
    `, [conversationId, limitNum, offset]);

    res.json({
      success: true,
      data: {
        messages: messages.map(msg => ({
          ...msg,
          metadata: JSON.parse(msg.metadata || '{}')
        })),
        pagination: {
          page: parseInt(page),
          limit: parseInt(limit)
        }
      }
    });
  } catch (error) {
    console.error('获取对话历史失败:', error);
    res.status(500).json({
      success: false,
      message: '获取对话历史失败',
      error: error.message
    });
  }
});

/**
 * 获取Agent的所有对话会话
 */
app.get('/api/agents/:id/conversations', authenticateUser, async (req, res) => {
  try {
    const { id: agentId } = req.params;
    const { page = 1, limit = 20 } = req.query;
    const pageNum = parseInt(page) || 1;
    const limitNum = parseInt(limit) || 20;
    const offset = (pageNum - 1) * limitNum;

    const conversations = await dbAdapter.query(`
      SELECT * FROM agent_conversations
      WHERE agent_id = ? AND user_id = ?
      ORDER BY updated_at DESC
      LIMIT ? OFFSET ?
    `, [agentId, req.user.id, limitNum, offset]);

    const total = await dbAdapter.query(`
      SELECT COUNT(*) as count FROM agent_conversations
      WHERE agent_id = ? AND user_id = ?
    `, [agentId, req.user.id]);

    res.json({
      success: true,
      data: {
        conversations: conversations.map(conv => ({
          ...conv,
          context: JSON.parse(conv.context || '{}')
        })),
        pagination: {
          page: parseInt(page),
          limit: parseInt(limit),
          total: total[0].count,
          pages: Math.ceil(total[0].count / limit)
        }
      }
    });
  } catch (error) {
    console.error('获取对话会话列表失败:', error);
    res.status(500).json({
      success: false,
      message: '获取对话会话列表失败',
      error: error.message
    });
  }
});

/**
 * 删除对话会话
 */
app.delete('/api/agents/:id/conversations/:conversationId', authenticateUser, async (req, res) => {
  try {
    const { conversationId } = req.params;

    // 删除对话会话
    await dbAdapter.query(
      'DELETE FROM agent_conversations WHERE id = ? AND user_id = ?',
      [conversationId, req.user.id]
    );

    // 删除相关消息
    await dbAdapter.query(
      'DELETE FROM agent_messages WHERE conversation_id = ?',
      [conversationId]
    );

    res.json({
      success: true,
      message: '对话会话删除成功'
    });
  } catch (error) {
    console.error('删除对话会话失败:', error);
    res.status(500).json({
      success: false,
      message: '删除对话会话失败',
      error: error.message
    });
  }
});

/**
 * 清空对话历史
 */
app.delete('/api/agents/:id/conversations/:conversationId/messages', authenticateUser, async (req, res) => {
  try {
    const { conversationId } = req.params;

    // 清空消息
    await dbAdapter.query(
      'DELETE FROM agent_messages WHERE conversation_id = ?',
      [conversationId]
    );

    // 重置对话上下文
    await dbAdapter.query(
      'UPDATE agent_conversations SET context = ?, updated_at = ? WHERE id = ? AND user_id = ?',
      [JSON.stringify({ messages: [] }), new Date().toISOString(), conversationId, req.user.id]
    );

    res.json({
      success: true,
      message: '对话历史清空成功'
    });
  } catch (error) {
    console.error('清空对话历史失败:', error);
    res.status(500).json({
      success: false,
      message: '清空对话历史失败',
      error: error.message
    });
  }
});

// ================================
// Model Integration API
// ================================

/**
 * 获取可用的AI模型列表
 */
app.get('/api/models', authenticateUser, async (req, res) => {
  try {
    const models = [
      {
        id: 'gpt-4o',
        name: 'GPT-4o',
        provider: 'openai',
        type: 'chat',
        description: '最新的GPT-4优化模型，具有更好的性能和更低的成本',
        maxTokens: 4096,
        supportedFeatures: ['chat', 'function_calling', 'vision'],
        pricing: { input: 0.005, output: 0.015 }
      },
      {
        id: 'gpt-4o-mini',
        name: 'GPT-4o Mini',
        provider: 'openai',
        type: 'chat',
        description: '轻量级GPT-4模型，适合简单任务',
        maxTokens: 2048,
        supportedFeatures: ['chat', 'function_calling'],
        pricing: { input: 0.0015, output: 0.006 }
      },
      {
        id: 'deepseek-chat',
        name: 'DeepSeek Chat',
        provider: 'deepseek',
        type: 'chat',
        description: 'DeepSeek聊天模型，性价比高',
        maxTokens: 4096,
        supportedFeatures: ['chat'],
        pricing: { input: 0.0014, output: 0.0028 }
      },
      {
        id: 'deepseek-coder',
        name: 'DeepSeek Coder',
        provider: 'deepseek',
        type: 'code',
        description: 'DeepSeek代码生成模型，专门用于编程任务',
        maxTokens: 4096,
        supportedFeatures: ['chat', 'code_generation'],
        pricing: { input: 0.0014, output: 0.0028 }
      }
    ];

    res.json({
      success: true,
      data: { models }
    });
  } catch (error) {
    console.error('获取模型列表失败:', error);
    res.status(500).json({
      success: false,
      message: '获取模型列表失败',
      error: error.message
    });
  }
});

/**
 * 测试模型连接
 */
app.post('/api/models/:modelId/test', authenticateUser, async (req, res) => {
  try {
    const { modelId } = req.params;
    const { message = '你好' } = req.body;

    const testConfig = {
      model: modelId,
      temperature: 0.7,
      maxTokens: 100,
      systemPrompt: '你是一个AI助手，请简短回复。'
    };

    const response = await callAIModel(testConfig, [], message);

    res.json({
      success: true,
      data: {
        model: modelId,
        input: message,
        output: response.content,
        usage: response.usage,
        latency: response.latency || 0
      }
    });
  } catch (error) {
    console.error('模型测试失败:', error);
    res.status(500).json({
      success: false,
      message: '模型测试失败',
      error: error.message
    });
  }
});

// AI模型调用函数
async function callAIModel(agentConfig, contextMessages, userMessage) {
  const startTime = Date.now();

  try {
    const model = agentConfig.model || 'gpt-4o';
    const temperature = agentConfig.temperature || 0.7;
    const maxTokens = agentConfig.maxTokens || 2048;
    const systemPrompt = agentConfig.systemPrompt || '你是一个专业的AI助手。';

    // 构建消息历史
    const messages = [
      { role: 'system', content: systemPrompt }
    ];

    // 添加上下文消息（最近的几条）
    const recentMessages = contextMessages.slice(-10);
    messages.push(...recentMessages);

    // 添加当前用户消息
    messages.push({ role: 'user', content: userMessage });

    // 调用聊天服务API
    const chatServiceUrl = process.env.CHAT_SERVICE_URL || 'http://localhost:3004';
    const response = await axios.post(`${chatServiceUrl}/api/chat/send`, {
      message: userMessage,
      model: model,
      temperature: temperature,
      max_tokens: maxTokens,
      messages: messages
    }, {
      headers: {
        'Content-Type': 'application/json',
        'Authorization': `Bearer ${process.env.INTERNAL_API_TOKEN || 'internal-token'}`
      },
      timeout: 30000
    });

    const latency = Date.now() - startTime;

    if (response.data.success) {
      return {
        content: response.data.data.content || response.data.data.message || '收到回复',
        usage: response.data.data.usage || {},
        latency
      };
    } else {
      throw new Error(response.data.message || 'AI模型调用失败');
    }
  } catch (error) {
    console.error('AI模型调用失败:', error);
    const latency = Date.now() - startTime;

    // 返回默认回复
    return {
      content: '抱歉，我现在无法处理您的请求。请稍后再试。',
      usage: {},
      latency,
      error: error.message
    };
  }
}

// ================================
// Workflow 工作流 API
// ================================

/**
 * 获取工作流列表
 */
app.get('/api/workflows', authenticateUser, async (req, res) => {
  try {
    const { page = 1, limit = 20, search = '', category = '' } = req.query;

    // 模拟工作流数据
    const workflows = [
      {
        id: 'workflow_1',
        user_id: req.user.id,
        name: 'QMS质量检查流程',
        description: '自动化质量检查和报告生成流程',
        category: 'quality_management',
        status: 'published',
        version: 1,
        nodes: [
          {
            id: 'start',
            type: 'start',
            name: '开始',
            position: { x: 100, y: 100 }
          },
          {
            id: 'agent_1',
            type: 'agent',
            name: '质量检查Agent',
            agentId: '5d6c7d7a-c4e5-436d-ade2-c3c7412d25b4',
            position: { x: 300, y: 100 }
          },
          {
            id: 'condition_1',
            type: 'condition',
            name: '检查结果判断',
            condition: 'result.score > 80',
            position: { x: 500, y: 100 }
          },
          {
            id: 'end',
            type: 'end',
            name: '结束',
            position: { x: 700, y: 100 }
          }
        ],
        edges: [
          { from: 'start', to: 'agent_1' },
          { from: 'agent_1', to: 'condition_1' },
          { from: 'condition_1', to: 'end' }
        ],
        created_at: '2025-08-06T05:50:54.112Z',
        updated_at: new Date().toISOString()
      },
      {
        id: 'workflow_2',
        user_id: req.user.id,
        name: '文档处理流程',
        description: '自动化文档分析和摘要生成',
        category: 'document_processing',
        status: 'draft',
        version: 1,
        nodes: [
          {
            id: 'start',
            type: 'start',
            name: '开始',
            position: { x: 100, y: 100 }
          },
          {
            id: 'upload',
            type: 'upload',
            name: '文档上传',
            position: { x: 300, y: 100 }
          },
          {
            id: 'agent_2',
            type: 'agent',
            name: '文档分析Agent',
            agentId: 'f45583fa-c35c-4d08-b72a-3adda53b827c',
            position: { x: 500, y: 100 }
          },
          {
            id: 'end',
            type: 'end',
            name: '结束',
            position: { x: 700, y: 100 }
          }
        ],
        edges: [
          { from: 'start', to: 'upload' },
          { from: 'upload', to: 'agent_2' },
          { from: 'agent_2', to: 'end' }
        ],
        created_at: '2025-08-06T05:50:54.112Z',
        updated_at: new Date().toISOString()
      }
    ];

    // 简单的搜索过滤
    let filteredWorkflows = workflows;
    if (search) {
      filteredWorkflows = workflows.filter(w =>
        w.name.toLowerCase().includes(search.toLowerCase()) ||
        w.description.toLowerCase().includes(search.toLowerCase())
      );
    }
    if (category) {
      filteredWorkflows = filteredWorkflows.filter(w => w.category === category);
    }

    res.json({
      success: true,
      data: {
        workflows: filteredWorkflows,
        pagination: {
          page: parseInt(page),
          limit: parseInt(limit),
          total: filteredWorkflows.length,
          pages: Math.ceil(filteredWorkflows.length / limit)
        }
      }
    });
  } catch (error) {
    console.error('获取工作流列表失败:', error);
    res.status(500).json({
      success: false,
      error: '获取工作流列表失败',
      message: error.message
    });
  }
});

/**
 * 创建工作流
 */
app.post('/api/workflows', authenticateUser, async (req, res) => {
  try {
    const { name, description, category = 'general', nodes = [], edges = [] } = req.body;

    if (!name) {
      return res.status(400).json({
        success: false,
        error: '工作流名称不能为空'
      });
    }

    const workflow = {
      id: `workflow_${Date.now()}`,
      user_id: req.user.id,
      name,
      description: description || '',
      category,
      status: 'draft',
      version: 1,
      nodes,
      edges,
      created_at: new Date().toISOString(),
      updated_at: new Date().toISOString()
    };

    res.status(201).json({
      success: true,
      data: { workflow },
      message: '工作流创建成功'
    });
  } catch (error) {
    console.error('创建工作流失败:', error);
    res.status(500).json({
      success: false,
      error: '创建工作流失败',
      message: error.message
    });
  }
});

/**
 * 获取工作流详情
 */
app.get('/api/workflows/:id', authenticateUser, async (req, res) => {
  try {
    const { id } = req.params;

    // 模拟获取工作流详情
    const workflow = {
      id,
      user_id: req.user.id,
      name: 'QMS质量检查流程',
      description: '自动化质量检查和报告生成流程',
      category: 'quality_management',
      status: 'published',
      version: 1,
      nodes: [
        {
          id: 'start',
          type: 'start',
          name: '开始',
          position: { x: 100, y: 100 },
          config: {}
        },
        {
          id: 'agent_1',
          type: 'agent',
          name: '质量检查Agent',
          agentId: '5d6c7d7a-c4e5-436d-ade2-c3c7412d25b4',
          position: { x: 300, y: 100 },
          config: {
            prompt: '请对输入的内容进行质量检查',
            timeout: 30000
          }
        },
        {
          id: 'condition_1',
          type: 'condition',
          name: '检查结果判断',
          condition: 'result.score > 80',
          position: { x: 500, y: 100 },
          config: {
            trueOutput: 'pass',
            falseOutput: 'fail'
          }
        },
        {
          id: 'end',
          type: 'end',
          name: '结束',
          position: { x: 700, y: 100 },
          config: {}
        }
      ],
      edges: [
        { from: 'start', to: 'agent_1', label: '' },
        { from: 'agent_1', to: 'condition_1', label: '' },
        { from: 'condition_1', to: 'end', label: 'pass' }
      ],
      created_at: '2025-08-06T05:50:54.112Z',
      updated_at: new Date().toISOString()
    };

    res.json({
      success: true,
      data: { workflow }
    });
  } catch (error) {
    console.error('获取工作流详情失败:', error);
    res.status(500).json({
      success: false,
      error: '获取工作流详情失败',
      message: error.message
    });
  }
});

/**
 * 文档解析场景工作流执行 (必须在通用工作流路由之前定义)
 * 路径：/api/workflows/document-parsing/execute
 */
app.post('/api/workflows/document-parsing/execute', authenticateUserOrAnonymous, async (req, res) => {
    try {
      const startAll = Date.now();
      const { input = {}, options = {} } = req.body || {};
      const { file = {}, query = '' } = input;
      const { name = '', type = '', base64 = '', text = '' } = file || {};
      const { summarize = true, stats = true, ingest_kb = false, kb_id = null } = options || {};

      // 生成执行ID
      const executionId = `exec_${Date.now()}`;

      // 立即返回执行ID，表示开始处理
      res.json({
        success: true,
        data: {
          execution_id: executionId,
          status: 'running'
        },
        message: '文档解析开始执行'
      });

      // 立即在executionStore中创建初始记录
      const steps = [];
      executionStore.set(executionId, {
        id: executionId,
        steps: [],
        status: 'running',
        duration: 0,
        current_step: 0,
        total_steps: 5
      });
      const recordStep = (title, status, data = {}, started_at, completed_at) => {
        const step = { title, status, data, started_at, completed_at, duration_ms: (completed_at - started_at) };
        steps.push(step);

        // 实时更新执行状态到store，供前端轮询查询
        executionStore.set(executionId, {
          id: executionId,
          steps: [...steps], // 创建副本避免引用问题
          status: status === 'failed' ? 'failed' : (steps.length >= 5 ? 'completed' : 'running'),
          duration: Date.now() - startAll,
          current_step: steps.length,
          total_steps: 5
        });
      };

      // 0) 确保核心插件可用
      try { await ensureCorePluginsInstalled(); } catch(e) { /* 忽略 */ }

      // 文档插件选择函数（本地定义）
      const pickDocPluginByType = (fileType = 'text') => {
        const t = (fileType || '').toLowerCase();
        if (t.includes('pdf')) return 'pdf_parser';
        if (t.includes('csv')) return 'csv_parser';
        if (t.includes('xlsx') || t.includes('excel') || t === 'xls') return 'xlsx_parser';
        if (t.includes('docx') || t.includes('doc')) return 'docx_parser';
        if (t.includes('json')) return 'json_parser';
        if (t.includes('xml')) return 'xml_parser';
        if (t.includes('png') || t.includes('jpg') || t.includes('jpeg') || t.includes('bmp') || t.includes('gif') || t.includes('webp')) return 'image_parser';
        if (t.includes('txt') || t === 'text') return 'text_parser';
        return null;
      };

      // 1) 类型识别与插件选择
      let t0 = Date.now();
      const inferredType = (type || (name.split('.').pop() || '')).toLowerCase();
      const pluginId = pickDocPluginByType(inferredType) || (inferredType.match(/png|jpg|jpeg|bmp|gif|webp/) ? 'ocr_reader' : null);
      recordStep('detect_type', 'completed', { inferredType, pluginId }, t0, Date.now());

      // 2) 文档解析
      t0 = Date.now();
      let parseResult = null;
      if (pluginId) {
        const pluginInput = {};
        if (base64) pluginInput.base64 = base64;
        if (text) pluginInput.text = text;
        const exec = await pluginEcosystem.executePlugin(pluginId, pluginInput, {});

        // 从插件响应中提取实际结果
        if (exec && exec.result) {
          parseResult = exec.result;
        } else if (exec) {
          parseResult = exec;
        } else {
          parseResult = null;
        }
      } else {
        // 纯文本 - 增强编码检测
        let textContent = text;
        if (!textContent && base64) {
          textContent = detectAndDecodeText(Buffer.from(base64, 'base64'));
        }
        parseResult = { success: true, type: 'text', text: textContent || '' };
      }
      recordStep('parse_document', 'completed', { type: parseResult?.type, size: (parseResult?.text||'').length }, t0, Date.now());

      // 3) 归一化结构（表格/文本）
      t0 = Date.now();
      const normalized = { pluginId: pluginId || 'raw_text', res: {}, basicSummary: null };
      const pr = parseResult || {};
      if (pr.text && typeof pr.text === 'string') {
        normalized.res = { type: pr.type || 'text', text: pr.text, html: pr.html || null, metadata: pr.metadata || {} };
      } else if (pr.preview || pr.rows || pr.columns) {
        const rows = Array.isArray(pr.preview) ? pr.preview : (Array.isArray(pr.rows) ? pr.rows : []);
        normalized.res = { type: pr.type || 'table', preview: rows.slice(0, 100), rows: rows, columns: pr.columns || pr.data || {} };
      } else if (pr.data && (Array.isArray(pr.data.preview) || pr.data.columns)) {
        normalized.res = { type: pr.type || 'table', preview: pr.data.preview || [], rows: pr.data.rows || [], columns: pr.data.columns || {} };
      } else {
        normalized.res = { type: 'unknown', raw: pr };
      }
      recordStep('normalize', 'completed', { res_type: normalized.res.type }, t0, Date.now());

      // 4) 分支分析：文本摘要或统计分析
      t0 = Date.now();

      let analysis = null;
      if (normalized.res.type === 'text' && summarize) {
        const exec = await pluginEcosystem.executePlugin('text_summarizer', { text: normalized.res.text }, { max_sentences: 5 });
        analysis = exec || null;
        normalized.basicSummary = { kind: 'text', summary: (analysis?.summary || ''), sentences: analysis?.sentences || [] };
      } else if (normalized.res.type !== 'text' && stats) {
        const ds = Array.isArray(normalized.res.preview) ? normalized.res.preview : [];
        const exec = await pluginEcosystem.executePlugin('statistical_analyzer', { dataset: ds }, {});
        analysis = exec || null;
        normalized.basicSummary = { kind: 'table', stats: analysis?.stats || {}, notes: analysis?.recommendations || [] };
      }
      recordStep('analyze', 'completed', { kind: normalized.basicSummary?.kind || 'none' }, t0, Date.now());

      // 5) 可选：入库KB
      t0 = Date.now();
      let kbResult = null;
      if (ingest_kb) {
        try {
          const content = normalized.res.type === 'text' ? (normalized.res.text || '') : JSON.stringify((normalized.res.rows || normalized.res.preview || [])).slice(0, 50000);
          kbResult = { success: true, mocked: true, kb_id: kb_id || 'kb_demo', length: content.length };
          recordStep('ingest_kb', 'completed', { kb_id: kbResult.kb_id }, t0, Date.now());
        } catch (e) {
          recordStep('ingest_kb', 'failed', { error: e.message }, t0, Date.now());
        }
      }

      const duration = Date.now() - startAll;

      // 存储执行信息（供前端追踪可视化）
      executionStore.set(executionId, {
        id: executionId,
        steps,
        parsed: normalized,
        status: 'completed',
        duration
      });

      // 异步处理完成，不需要再次响应（已在开始时响应）
      console.log(`✅ 文档解析工作流执行完成: ${executionId}, 耗时: ${duration}ms`);
    } catch (error) {
      console.error('文档解析工作流执行失败:', error);

      // 更新执行状态为失败
      executionStore.set(executionId, {
        id: executionId,
        status: 'failed',
        error: error.message,
        steps,
        duration: Date.now() - startAll
      });
    }
  });

/**
 * 执行工作流
 */
app.post('/api/workflows/:id/execute', authenticateUser, async (req, res) => {
  try {
    const { id } = req.params;
    const { input = {}, context = {} } = req.body;

    const executionId = `exec_${Date.now()}`;

    // 模拟工作流执行
    const execution = {
      id: executionId,
      workflow_id: id,
      user_id: req.user.id,
      status: 'running',
      input,
      context,
      steps: [],
      result: null,
      started_at: new Date().toISOString(),
      completed_at: null
    };

    // 异步执行工作流
    executeWorkflowAsync(execution);

    res.json({
      success: true,
      data: {
        execution_id: executionId,
        status: 'running'
      },
      message: '工作流开始执行'
    });
  } catch (error) {
    console.error('执行工作流失败:', error);
    res.status(500).json({
      success: false,
      error: '执行工作流失败',
      message: error.message
    });
  }
});

/**
 * 获取工作流执行状态
 */
app.get('/api/workflows/executions/:executionId', authenticateUser, async (req, res) => {
  try {
    const { executionId } = req.params;

    // 模拟获取执行状态
    const execution = {
      id: executionId,
      workflow_id: 'workflow_1',
      user_id: req.user.id,
      status: 'completed',
      input: { text: '测试输入' },
      context: {},
      steps: [
        {
          node_id: 'start',
          status: 'completed',
          started_at: '2025-08-06T05:50:54.112Z',
          completed_at: '2025-08-06T05:50:54.200Z',
          output: { text: '测试输入' }
        },
        {
          node_id: 'agent_1',
          status: 'completed',
          started_at: '2025-08-06T05:50:54.200Z',
          completed_at: '2025-08-06T05:50:56.500Z',
          output: {
            result: '质量检查通过',
            score: 85,
            details: '内容符合质量标准'
          }
        },
        {
          node_id: 'condition_1',
          status: 'completed',
          started_at: '2025-08-06T05:50:56.500Z',
          completed_at: '2025-08-06T05:50:56.600Z',
          output: { result: 'pass' }
        },
        {
          node_id: 'end',
          status: 'completed',
          started_at: '2025-08-06T05:50:56.600Z',
          completed_at: '2025-08-06T05:50:56.700Z',
          output: { final_result: 'pass' }
        }
      ],
      result: {
        status: 'pass',
        score: 85,
        message: '质量检查通过'
      },
      started_at: '2025-08-06T05:50:54.112Z',
      completed_at: '2025-08-06T05:50:56.700Z'
    };

    res.json({
      success: true,
      data: { execution }
    });
  } catch (error) {
    console.error('获取执行状态失败:', error);
    res.status(500).json({
      success: false,
      error: '获取执行状态失败',
      message: error.message
    });
  }
});

// 异步执行工作流的函数
async function executeWorkflowAsync(execution) {
  console.log(`🚀 开始执行工作流: ${execution.id}`);

  try {
    // 获取工作流定义
    const [workflow] = await dbAdapter.query(
      'SELECT * FROM workflows WHERE id = ? AND deleted_at IS NULL',
      [execution.workflow_id]
    );

    if (!workflow) {
      throw new Error('工作流不存在');
    }

    const workflowDefinition = JSON.parse(workflow.definition || '{}');
    const nodes = workflowDefinition.nodes || [];
    const edges = workflowDefinition.edges || [];

    // 更新执行状态为运行中
    await dbAdapter.query(
      'UPDATE workflow_executions SET status = ?, started_at = ? WHERE id = ?',
      ['running', new Date().toISOString(), execution.id]
    );

    // 执行工作流节点
    const executionResult = await executeWorkflowNodes(execution.id, nodes, edges, execution.input_data);

    // 更新执行状态为完成
    await dbAdapter.query(
      'UPDATE workflow_executions SET status = ?, output_data = ?, completed_at = ? WHERE id = ?',
      ['completed', JSON.stringify(executionResult), new Date().toISOString(), execution.id]
    );

    console.log(`✅ 工作流执行完成: ${execution.id}`);

  } catch (error) {
    console.error(`❌ 工作流执行失败: ${execution.id}`, error);

    // 更新执行状态为失败
    await dbAdapter.query(
      'UPDATE workflow_executions SET status = ?, error_message = ?, completed_at = ? WHERE id = ?',
      ['failed', error.message, new Date().toISOString(), execution.id]
    );
  }
}

// 执行工作流节点的函数
async function executeWorkflowNodes(executionId, nodes, edges, inputData) {
  console.log(`📋 开始执行工作流节点，共 ${nodes.length} 个节点`);

  // 构建节点执行图
  const nodeMap = new Map();
  const nodeInputs = new Map();
  const nodeOutputs = new Map();

  // 初始化节点
  nodes.forEach(node => {
    nodeMap.set(node.id, node);
    nodeInputs.set(node.id, []);
    nodeOutputs.set(node.id, []);
  });

  // 构建连接关系
  edges.forEach(edge => {
    nodeOutputs.get(edge.source).push(edge.target);
    nodeInputs.get(edge.target).push(edge.source);
  });

  // 找到起始节点（没有输入的节点）
  const startNodes = nodes.filter(node => nodeInputs.get(node.id).length === 0);

  if (startNodes.length === 0) {
    throw new Error('工作流中没有找到起始节点');
  }

  // 执行结果存储
  const nodeResults = new Map();
  const executedNodes = new Set();

  // 递归执行节点
  async function executeNode(nodeId, context = {}) {
    if (executedNodes.has(nodeId)) {
      return nodeResults.get(nodeId);
    }

    const node = nodeMap.get(nodeId);
    if (!node) {
      throw new Error(`节点 ${nodeId} 不存在`);
    }

    console.log(`🔄 执行节点: ${node.name} (${node.type})`);

    // 记录节点执行开始
    const nodeLogId = uuidv4();
    await dbAdapter.query(`
      INSERT INTO workflow_node_logs (
        id, execution_id, node_id, node_type, input_data, status, started_at
      ) VALUES (?, ?, ?, ?, ?, ?, ?)
    `, [
      nodeLogId, executionId, nodeId, node.type,
      JSON.stringify(context), 'running', new Date().toISOString()
    ]);

    try {
      let result = {};

      // 根据节点类型执行不同逻辑
      switch (node.type) {
        case 'input':
          result = { data: inputData, ...context };
          break;

        case 'agent':
          result = await executeAgentNode(node, context);
          break;

        case 'plugin':
          result = await executePluginNode(node, context);
          break;

        case 'condition':
          result = await executeConditionNode(node, context);
          break;

        case 'transform':
          result = await executeTransformNode(node, context);
          break;

        case 'document_input':
          result = await executeDocumentInputNode(node, context);
          break;

        case 'format_detection':
          result = await executeFormatDetectionNode(node, context);
          break;

        case 'parsing_router':
          result = await executeParsingRouterNode(node, context);
          break;

        case 'document_parser':
          result = await executeDocumentParserNode(node, context);
          break;

        case 'ai_analysis':
          result = await executeAiAnalysisNode(node, context);
          break;

        case 'result_formatter':
          result = await executeResultFormatterNode(node, context);
          break;

        case 'output':
          result = { output: context, final: true };
          break;

        default:
          throw new Error(`不支持的节点类型: ${node.type}`);
      }

      // 记录执行成功
      await dbAdapter.query(`
        UPDATE workflow_node_logs
        SET status = ?, output_data = ?, completed_at = ?
        WHERE id = ?
      `, ['completed', JSON.stringify(result), new Date().toISOString(), nodeLogId]);

      nodeResults.set(nodeId, result);
      executedNodes.add(nodeId);

      console.log(`✅ 节点执行完成: ${node.name}`);

      // 执行后续节点
      const nextNodes = nodeOutputs.get(nodeId) || [];
      for (const nextNodeId of nextNodes) {
        await executeNode(nextNodeId, { ...context, ...result });
      }

      return result;

    } catch (error) {
      console.error(`❌ 节点执行失败: ${node.name}`, error);

      // 记录执行失败
      await dbAdapter.query(`
        UPDATE workflow_node_logs
        SET status = ?, error_message = ?, completed_at = ?
        WHERE id = ?
      `, ['failed', error.message, new Date().toISOString(), nodeLogId]);

      throw error;
    }
  }

  // 从起始节点开始执行
  let finalResult = {};
  for (const startNode of startNodes) {
    const result = await executeNode(startNode.id, { input: inputData });
    if (result.final) {
      finalResult = result;
    }
  }

  return finalResult;
}

// 执行Agent节点
async function executeAgentNode(node, context) {
  console.log(`🤖 执行Agent节点: ${node.name}`);

  const agentConfig = node.data || {};
  const message = context.message || context.input || '请处理这个任务';

  // 调用Agent
  const response = await callAIModel(agentConfig, [], message);

  return {
    type: 'agent',
    agent_id: agentConfig.agent_id,
    message: response.content,
    usage: response.usage,
    model: agentConfig.model
  };
}

// 执行插件节点
async function executePluginNode(node, context) {
  console.log(`🔌 执行插件节点: ${node.name}`);

  const pluginConfig = node.data || {};
  const pluginId = pluginConfig.plugin_id;
  const endpoint = pluginConfig.endpoint || 'execute';
  const parameters = { ...pluginConfig.parameters, ...context };

  // 这里可以调用真实的插件执行逻辑
  // 目前返回模拟结果
  return {
    type: 'plugin',
    plugin_id: pluginId,
    endpoint: endpoint,
    result: `插件 ${pluginId} 执行结果`,
    parameters: parameters
  };
}

// 执行条件节点
async function executeConditionNode(node, context) {
  console.log(`🔀 执行条件节点: ${node.name}`);

  const conditionConfig = node.data || {};
  const condition = conditionConfig.condition || 'true';
  const leftValue = context[conditionConfig.leftField] || conditionConfig.leftValue;
  const rightValue = conditionConfig.rightValue;
  const operator = conditionConfig.operator || '==';

  let result = false;

  switch (operator) {
    case '==':
      result = leftValue == rightValue;
      break;
    case '!=':
      result = leftValue != rightValue;
      break;
    case '>':
      result = Number(leftValue) > Number(rightValue);
      break;
    case '<':
      result = Number(leftValue) < Number(rightValue);
      break;
    case 'contains':
      result = String(leftValue).includes(String(rightValue));
      break;
    default:
      result = Boolean(leftValue);
  }

  return {
    type: 'condition',
    condition: condition,
    result: result,
    leftValue: leftValue,
    rightValue: rightValue,
    operator: operator
  };
}

// 执行数据转换节点
async function executeTransformNode(node, context) {
  console.log(`🔄 执行转换节点: ${node.name}`);

  const transformConfig = node.data || {};
  const transformType = transformConfig.type || 'passthrough';

  let result = { ...context };

  switch (transformType) {
    case 'extract':
      // 提取特定字段
      const fields = transformConfig.fields || [];
      result = {};
      fields.forEach(field => {
        if (context[field] !== undefined) {
          result[field] = context[field];
        }
      });
      break;

    case 'rename':
      // 重命名字段
      const mapping = transformConfig.mapping || {};
      Object.keys(mapping).forEach(oldKey => {
        if (context[oldKey] !== undefined) {
          result[mapping[oldKey]] = context[oldKey];
          delete result[oldKey];
        }
      });
      break;

    case 'format':
      // 格式化数据
      const template = transformConfig.template || '${input}';
      result.formatted = template.replace(/\$\{(\w+)\}/g, (match, key) => {
        return context[key] || match;
      });
      break;

    default:
      // 直接传递
      result = { ...context };
  }

  return {
    type: 'transform',
    transform_type: transformType,
    ...result
  };
}

// ================================
// Knowledge 知识库 API
// ================================

/**
 * 获取知识库列表
 */
app.get('/api/knowledge', authenticateUser, async (req, res) => {
  try {
    const { page = 1, limit = 20, search = '', category = '' } = req.query;

    // 模拟知识库数据
    const knowledgeBases = [
      {
        id: 'kb_1',
        user_id: req.user.id,
        name: 'ISO质量标准知识库',
        description: '包含ISO 9001、ISO 14001等质量管理标准文档',
        category: 'quality_standards',
        status: 'active',
        document_count: 156,
        vector_count: 12450,
        embedding_model: 'text-embedding-ada-002',
        created_at: '2025-08-06T05:50:54.112Z',
        updated_at: new Date().toISOString()
      },
      {
        id: 'kb_2',
        user_id: req.user.id,
        name: '企业内部文档库',
        description: '公司内部流程、制度、规范文档',
        category: 'internal_docs',
        status: 'active',
        document_count: 89,
        vector_count: 7823,
        embedding_model: 'text-embedding-ada-002',
        created_at: '2025-08-06T05:50:54.112Z',
        updated_at: new Date().toISOString()
      },
      {
        id: 'kb_3',
        user_id: req.user.id,
        name: '技术文档库',
        description: '技术规范、API文档、开发指南',
        category: 'technical_docs',
        status: 'processing',
        document_count: 45,
        vector_count: 3421,
        embedding_model: 'text-embedding-ada-002',
        created_at: '2025-08-06T05:50:54.112Z',
        updated_at: new Date().toISOString()
      }
    ];

    // 简单的搜索过滤
    let filteredKnowledgeBases = knowledgeBases;
    if (search) {
      filteredKnowledgeBases = knowledgeBases.filter(kb =>
        kb.name.toLowerCase().includes(search.toLowerCase()) ||
        kb.description.toLowerCase().includes(search.toLowerCase())
      );
    }
    if (category) {
      filteredKnowledgeBases = filteredKnowledgeBases.filter(kb => kb.category === category);
    }

    res.json({
      success: true,
      data: {
        knowledge_bases: filteredKnowledgeBases,
        pagination: {
          page: parseInt(page),
          limit: parseInt(limit),
          total: filteredKnowledgeBases.length,
          pages: Math.ceil(filteredKnowledgeBases.length / limit)
        }
      }
    });
  } catch (error) {
    console.error('获取知识库列表失败:', error);
    res.status(500).json({
      success: false,
      error: '获取知识库列表失败',
      message: error.message
    });
  }
});

/**
 * 创建知识库
 */
app.post('/api/knowledge', authenticateUser, async (req, res) => {
  try {
    const { name, description, category = 'general', embedding_model = 'text-embedding-ada-002' } = req.body;

    if (!name) {
      return res.status(400).json({
        success: false,
        error: '知识库名称不能为空'
      });
    }

    const knowledgeBase = {
      id: `kb_${Date.now()}`,
      user_id: req.user.id,
      name,
      description: description || '',
      category,
      status: 'empty',
      document_count: 0,
      vector_count: 0,
      embedding_model,
      created_at: new Date().toISOString(),
      updated_at: new Date().toISOString()
    };

    res.status(201).json({
      success: true,
      data: { knowledge_base: knowledgeBase },
      message: '知识库创建成功'
    });
  } catch (error) {
    console.error('创建知识库失败:', error);
    res.status(500).json({
      success: false,
      error: '创建知识库失败',
      message: error.message
    });
  }
});

/**
 * 上传文档到知识库
 */
app.post('/api/knowledge/:id/documents', authenticateUser, async (req, res) => {
  try {
    const { id } = req.params;
    const { title, content, file_type = 'text', metadata = {} } = req.body;

    if (!title || !content) {
      return res.status(400).json({
        success: false,
        error: '文档标题和内容不能为空'
      });
    }

    const document = {
      id: `doc_${Date.now()}`,
      knowledge_base_id: id,
      title,
      content,
      file_type,
      metadata,
      status: 'processing',
      vector_count: 0,
      created_at: new Date().toISOString(),
      updated_at: new Date().toISOString()
    };

    // 模拟文档处理
    setTimeout(() => {
      console.log(`文档 ${document.id} 处理完成`);
    }, 2000);

    res.status(201).json({
      success: true,
      data: { document },
      message: '文档上传成功，正在处理中'
    });
  } catch (error) {
    console.error('上传文档失败:', error);
    res.status(500).json({
      success: false,
      error: '上传文档失败',
      message: error.message
    });
  }
});

/**
 * 知识库检索
 */
app.post('/api/knowledge/:id/search', authenticateUser, async (req, res) => {
  try {
    const { id } = req.params;
    const { query, top_k = 5, threshold = 0.7 } = req.body;

    if (!query) {
      return res.status(400).json({
        success: false,
        error: '检索查询不能为空'
      });

/**
 * 轻量知识入库（M1）：解析→简易切块→存入Redis（按user/session命名空间）
 * 参考开源实践：先用keyword overlap作为最小可用检索；后续可替换为向量索引
 */
function simpleChunkText(text, maxLen = 1000) {
  const chunks = [];
  if (!text || typeof text !== 'string') return chunks;
  let start = 0;
  while (start < text.length) {
    let end = Math.min(start + maxLen, text.length);
    // 优先在句子边界裁剪
    const slice = text.slice(start, end);
    const lastPunct = Math.max(slice.lastIndexOf('。'), slice.lastIndexOf('.'));
    if (lastPunct > 200 && start + lastPunct + 1 < text.length) {
      end = start + lastPunct + 1;
    }
    chunks.push(text.slice(start, end));
    start = end;
  }
  return chunks;
}

function tokenizeZhEn(str) {
  if (!str) return [];
  // 简化分词：去除标点，按空白和中英文混合切分
  const cleaned = String(str).toLowerCase().replace(/[^a-z0-9 ---\u4e00-\u9fa5\s]/g, ' ');
  return cleaned.split(/\s+/).filter(Boolean);
}

function scoreByKeywordOverlap(query, chunk) {
  const qTokens = tokenizeZhEn(query);
  const cTokens = tokenizeZhEn(chunk);
  if (!qTokens.length || !cTokens.length) return 0;
  const setC = new Map();
  for (const t of cTokens) setC.set(t, (setC.get(t) || 0) + 1);
  let score = 0;
  for (const t of qTokens) {
    if (setC.has(t)) score += 1 + Math.log(1 + setC.get(t));
  }
  // 归一化到(0,1)
  return Math.min(1, score / Math.max(5, qTokens.length));
}

async function kbGetChunks(cacheService, scopeKey) {
  try {
    return (await cacheService.get('kb', `chunks:${scopeKey}`)) || [];
  } catch (e) {
    console.warn('读取KB失败:', e.message);
    return [];
  }
}

async function kbSetChunks(cacheService, scopeKey, chunks) {
  try {
    await cacheService.set('kb', `chunks:${scopeKey}`, chunks);
    return true;
  } catch (e) {
    console.warn('写入KB失败:', e.message);
    return false;
  }
}

// 动态选择文档解析插件
function pickDocPluginByType(fileType = 'text') {
  const t = (fileType || '').toLowerCase();
  if (t.includes('pdf')) return 'pdf_parser';
  if (t.includes('csv')) return 'csv_parser';
  // 优先检查更具体的格式
  if (t.includes('xlsx') || t.includes('excel') || t === 'xls' || t.includes('spreadsheetml')) return 'xlsx_parser';
  if (t.includes('docx') || t.includes('doc') || t.includes('wordprocessingml')) return 'docx_parser';
  if (t.includes('json')) return 'json_parser';
  if (t.includes('xml')) return 'xml_parser';
  if (t.includes('png') || t.includes('jpg') || t.includes('jpeg') || t.includes('bmp') || t.includes('gif') || t.includes('webp')) return 'image_parser';
  if (t.includes('txt') || t === 'text') return 'text_parser';
  return null; // 未知格式
}

/**
 * 执行文档输入节点
 */
async function executeDocumentInputNode(node, context) {
  const { config } = node;
  const inputData = context.inputData || {};

  let documentData = {
    type: 'unknown',
    content: null,
    metadata: {}
  };

  // 处理文件输入
  if (inputData.file) {
    const file = inputData.file;
    documentData = {
      type: 'file',
      content: {
        name: file.name,
        size: file.size,
        type: file.type,
        base64: file.base64,
        lastModified: file.lastModified
      },
      metadata: {
        source: 'file_upload',
        originalName: file.name,
        fileSize: file.size,
        mimeType: file.type
      }
    };
  }
  // 处理URL输入
  else if (inputData.url) {
    documentData = {
      type: 'url',
      content: {
        url: inputData.url
      },
      metadata: {
        source: 'url_input',
        url: inputData.url
      }
    };
  }
  // 处理文本输入
  else if (inputData.text) {
    documentData = {
      type: 'text',
      content: {
        text: inputData.text
      },
      metadata: {
        source: 'text_input',
        length: inputData.text.length
      }
    };
  }

  return {
    success: true,
    documentData,
    nextNode: 'format_detection'
  };
}

/**
 * 执行格式检测节点 - 使用增强格式检测器
 */
async function executeFormatDetectionNode(node, context) {
  const { documentData } = context;
  const { config } = node;

  try {
    let detectionInput = {};

    if (documentData.type === 'file') {
      const file = documentData.content;
      detectionInput = {
        fileName: file.name || '',
        mimeType: file.type || '',
        base64: file.base64 || ''
      };
    } else if (documentData.type === 'url') {
      detectionInput = {
        fileName: documentData.content.url,
        mimeType: '',
        base64: ''
      };
    } else if (documentData.type === 'text') {
      // 纯文本输入
      return {
        success: true,
        detectedFormat: 'text',
        confidence: 0.95,
        parserPlugin: 'text_parser',
        formatMetadata: {
          detectionMethod: 'text_input',
          confidence: 0.95,
          supportedParsers: config.supportedFormats || {}
        },
        nextNode: 'parsing_router'
      };
    }

    // 使用增强格式检测器
    const detectionResult = await formatDetector.detectFormat(detectionInput);

    console.log('🔍 格式检测结果:', {
      format: detectionResult.format,
      confidence: detectionResult.confidence,
      plugin: detectionResult.plugin,
      method: detectionResult.detectionMethod
    });

    return {
      success: true,
      detectedFormat: detectionResult.format,
      confidence: detectionResult.confidence,
      parserPlugin: detectionResult.plugin,
      formatMetadata: {
        detectionMethod: detectionResult.detectionMethod,
        confidence: detectionResult.confidence,
        supportedParsers: config.supportedFormats || {},
        metadata: detectionResult.metadata
      },
      nextNode: 'parsing_router'
    };

  } catch (error) {
    console.error('格式检测失败:', error);

    // 降级处理
    const fallbackFormat = 'text';
    const fallbackPlugin = 'text_parser';

    return {
      success: true,
      detectedFormat: fallbackFormat,
      confidence: 0.3,
      parserPlugin: fallbackPlugin,
      formatMetadata: {
        detectionMethod: 'error_fallback',
        confidence: 0.3,
        error: error.message,
        supportedParsers: config.supportedFormats || {}
      },
      nextNode: 'parsing_router'
    };
  }
}

/**
 * 执行解析路由节点
 */
async function executeParsingRouterNode(node, context) {
  const { detectedFormat, parserPlugin } = context;
  const { config } = node;

  let targetNode = config.fallbackRoute || 'text_parsing';

  // 根据检测到的格式选择目标节点
  if (config.routingRules && config.routingRules[detectedFormat]) {
    targetNode = config.routingRules[detectedFormat];
  }

  return {
    success: true,
    routedTo: targetNode,
    routingReason: `Format detected as ${detectedFormat}, using parser ${parserPlugin}`,
    nextNode: targetNode
  };
}

    }

    // 模拟向量检索结果
    const searchResults = [
      {
        document_id: 'doc_1',
        title: 'ISO 9001质量管理体系要求',
        content: 'ISO 9001是国际标准化组织制定的质量管理体系标准，旨在帮助组织建立有效的质量管理体系...',
        score: 0.92,
        metadata: {
          section: '4.1 理解组织及其环境',
          page: 15
        }
      },
      {
        document_id: 'doc_2',
        title: '质量管理原则',
        content: '质量管理的八项原则包括：以顾客为关注焦点、领导作用、全员参与、过程方法...',
        score: 0.87,
        metadata: {
          section: '2.2 质量管理原则',
          page: 8
        }
      },
      {
        document_id: 'doc_3',
        title: '持续改进要求',
        content: '组织应持续改进质量管理体系的有效性，通过质量方针、质量目标、审核结果...',
        score: 0.81,
        metadata: {
          section: '10.3 持续改进',
          page: 45
        }
      }
    ];

    res.json({
      success: true,
      data: {
        query,
        results: searchResults.filter(r => r.score >= threshold).slice(0, top_k),
        total_found: searchResults.length
      }
    });
  } catch (error) {
    console.error('知识库检索失败:', error);
    res.status(500).json({
      success: false,
      error: '知识库检索失败',
      message: error.message
    });
  }
});


/**
 * 新增：知识入库（轻量）
 * 入参：{ fileName, fileType, base64?, text?, saveScope: 'user'|'session', conversation_id? }
 */
app.post('/api/knowledge/ingest', authenticateUser, async (req, res) => {
  try {
    const { fileName = '未命名', fileType = 'text', base64, text, saveScope = 'user', conversation_id } = req.body || {};

    if (!text && !base64) {
      return res.status(400).json({ success: false, message: '请提供文本(text)或文件(base64)' });
    }

    // 调用文档解析插件（如可用），否则使用纯文本
    let plainText = '';
    const pluginId = pickDocPluginByType(fileType);
    if (pluginId) {
      try {
        const input = {};
        if (base64) input.base64 = base64;
        if (text) input.text = text;
        const exec = await pluginEcosystem.executePlugin(pluginId, input, {});
        if (exec && exec.success) {
          if (exec.text) plainText = exec.text;
          else if (exec.preview) plainText = JSON.stringify(exec.preview).slice(0, 20000);
          else if (exec.data) plainText = (typeof exec.data === 'string') ? exec.data : JSON.stringify(exec.data).slice(0, 20000);
        }
      } catch (e) {
        console.warn('文档解析失败，回退纯文本:', e.message);
      }
    }
    if (!plainText) plainText = text || '';

    const chunks = simpleChunkText(plainText, 1200).map((c, idx) => ({ docId: `doc_${Date.now()}`, idx, title: fileName, text: c }));

    // 写入Redis（按user或session命名空间）
    const scopeKey = saveScope === 'session' && conversation_id ? `${req.user.id}:${conversation_id}` : `${req.user.id}`;
    const existed = await kbGetChunks(cacheService, scopeKey);
    const next = [...(existed || []), ...chunks];
    await kbSetChunks(cacheService, scopeKey, next);

    res.json({ success: true, data: { fileName, fileType, scopeKey, added: chunks.length, total: next.length, sample: chunks.slice(0, 2) } });
  } catch (error) {
    console.error('知识入库失败:', error);
    res.status(500).json({ success: false, message: '知识入库失败', error: error.message });
  }
});

/**
 * 新增：知识检索（轻量关键词重叠）
 * 入参：{ query, top_k=3, kb_scope='user'|'session', conversation_id? }
 */
app.post('/api/knowledge/search', authenticateUser, async (req, res) => {
  try {
    const { query, top_k = 3, kb_scope = 'user', conversation_id } = req.body || {};
    if (!query) return res.status(400).json({ success: false, message: 'query 不能为空' });

    // TODO: 实现知识检索逻辑
    res.json({ success: true, data: { items: [] } });
  } catch (error) {
    console.error('知识检索失败:', error);
    res.status(500).json({ success: false, message: '知识检索失败', error: error.message });
  }
});

/** Traces: 执行过程记录（右侧过程栏数据源） **/
async function appendTrace(cacheService, userId, conversationId, trace) {
  try {
    const key = `${userId}:${conversationId}`;
    const list = (await cacheService.get('traces', key)) || [];
    list.push(trace);
    // 仅保留最近 100 条
    await cacheService.set('traces', key, list.slice(-100));
  } catch (e) {
    console.warn('写入Trace失败:', e.message);
  }
}

app.post('/api/traces/append', authenticateUserOrAnonymous, async (req, res) => {
  console.log('🔍 收到 Traces 追加请求:', {
    method: req.method,
    url: req.url,
    userId: req.user?.id,
    body: req.body
  });
  try {
    const { conversation_id, trace } = req.body || {};
    if (!conversation_id || !trace) return res.status(400).json({ success: false, message: 'conversation_id 与 trace 必填' });
    await appendTrace(cacheService, req.user.id, conversation_id, { ts: Date.now(), ...trace });
    console.log('✅ Traces 追加成功');
    res.json({ success: true });
  } catch (error) {
    console.error('❌ Trace追加失败:', error);
    res.status(500).json({ success: false, message: 'Trace追加失败', error: error.message });
  }
});

app.get('/api/traces', authenticateUserOrAnonymous, async (req, res) => {
  try {
    const { conversation_id, limit = 50 } = req.query;
    if (!conversation_id) return res.status(400).json({ success: false, message: 'conversation_id 必填' });
    const key = `${req.user.id}:${conversation_id}`;
    const list = (await cacheService.get('traces', key)) || [];
    res.json({ success: true, data: { items: list.slice(-Number(limit)) } });
  } catch (error) {
    console.error('Trace获取失败:', error);
    res.status(500).json({ success: false, message: 'Trace获取失败', error: error.message });
  }
});



/**
 * 工作流模板：案例应用指导
 */
app.get('/api/workflows/templates/case-guidance', authenticateUser, async (req, res) => {
  res.json({
    success: true,
    data: {
      id: 'case-guidance',
      name: '案例应用指导（解析→入库→检索→指导）',
      steps: [
        { id: 'parse', title: '解析文件/文本', kind: 'parse' },
        { id: 'ingest_kb', title: '写入知识库', kind: 'kb' },
        { id: 'search_kb', title: '检索知识片段', kind: 'kb' },
        { id: 'case_guidance', title: '生成经验指导', kind: 'guidance' },
        { id: 'respond', title: '组装结果返回', kind: 'respond' }
      ]
    }
  });
});

/**
 * 工作流模板：文档解析专用工作流
 */
app.get('/api/workflows/templates/document-parsing', authenticateUser, async (req, res) => {
  res.json({
    success: true,
    data: {
      id: 'document-parsing',
      name: '智能文档解析工作流',
      description: '支持多种格式文档的智能解析、内容分析和结构化输出',
      category: 'document-processing',
      version: '1.0.0',
      nodes: [
        {
          id: 'start',
          type: 'start',
          name: '开始',
          description: '工作流开始节点',
          position: { x: 100, y: 200 },
          config: {
            inputs: ['file', 'url', 'text'],
            supportedFormats: ['pdf', 'docx', 'xlsx', 'csv', 'txt', 'json', 'xml', 'png', 'jpg']
          }
        },
        {
          id: 'document_input',
          type: 'document_input',
          name: '文档输入',
          description: '接收文档文件、URL或文本内容',
          position: { x: 300, y: 200 },
          config: {
            acceptTypes: ['.pdf', '.docx', '.xlsx', '.csv', '.txt', '.json', '.xml', '.png', '.jpg', '.jpeg'],
            maxFileSize: '50MB',
            allowUrl: true,
            allowText: true
          }
        },
        {
          id: 'format_detection',
          type: 'format_detection',
          name: '格式检测',
          description: '自动检测文档格式和类型',
          position: { x: 500, y: 200 },
          config: {
            autoDetect: true,
            fallbackToText: true,
            supportedFormats: {
              'pdf': { parser: 'pdf_parser', priority: 1 },
              'docx': { parser: 'docx_parser', priority: 1 },
              'xlsx': { parser: 'xlsx_parser', priority: 1 },
              'csv': { parser: 'csv_parser', priority: 1 },
              'json': { parser: 'json_parser', priority: 1 },
              'xml': { parser: 'xml_parser', priority: 1 },
              'image': { parser: 'ocr_reader', priority: 2 },
              'text': { parser: 'text_processor', priority: 3 }
            }
          }
        },
        {
          id: 'parsing_router',
          type: 'parsing_router',
          name: '解析路由',
          description: '根据文档格式选择对应的解析策略',
          position: { x: 700, y: 200 },
          config: {
            routingRules: {
              'pdf': 'pdf_parsing',
              'docx': 'docx_parsing',
              'xlsx': 'xlsx_parsing',
              'csv': 'csv_parsing',
              'json': 'json_parsing',
              'xml': 'xml_parsing',
              'image': 'ocr_parsing',
              'text': 'text_parsing'
            },
            fallbackRoute: 'text_parsing'
          }
        },
        {
          id: 'pdf_parsing',
          type: 'document_parser',
          name: 'PDF解析',
          description: '解析PDF文档内容',
          position: { x: 900, y: 100 },
          config: {
            parser: 'pdf_parser',
            extractText: true,
            extractImages: false,
            extractTables: true,
            preserveLayout: true
          }
        },
        {
          id: 'docx_parsing',
          type: 'document_parser',
          name: 'Word解析',
          description: '解析Word文档内容',
          position: { x: 900, y: 200 },
          config: {
            parser: 'docx_parser',
            extractText: true,
            extractImages: false,
            preserveFormatting: true
          }
        },
        {
          id: 'xlsx_parsing',
          type: 'document_parser',
          name: 'Excel解析',
          description: '解析Excel表格数据',
          position: { x: 900, y: 300 },
          config: {
            parser: 'xlsx_parser',
            extractAllSheets: true,
            includeHeaders: true,
            convertToJson: true
          }
        },
        {
          id: 'content_analysis',
          type: 'ai_analysis',
          name: '内容分析',
          description: 'AI分析文档内容结构和关键信息',
          position: { x: 1100, y: 200 },
          config: {
            analysisTypes: ['structure', 'keywords', 'summary', 'entities'],
            aiModel: 'deepseek-chat',
            maxTokens: 4000,
            temperature: 0.3
          }
        },
        {
          id: 'result_formatter',
          type: 'result_formatter',
          name: '结果格式化',
          description: '格式化输出解析结果',
          position: { x: 1300, y: 200 },
          config: {
            outputFormat: 'structured',
            includeMetadata: true,
            includeAnalysis: true,
            exportFormats: ['json', 'markdown', 'html']
          }
        },
        {
          id: 'end',
          type: 'end',
          name: '结束',
          description: '工作流结束节点',
          position: { x: 1500, y: 200 },
          config: {
            outputTypes: ['parsed_content', 'analysis_result', 'metadata']
          }
        }
      ],
      edges: [
        { from: 'start', to: 'document_input' },
        { from: 'document_input', to: 'format_detection' },
        { from: 'format_detection', to: 'parsing_router' },
        { from: 'parsing_router', to: 'pdf_parsing', condition: 'format === "pdf"' },
        { from: 'parsing_router', to: 'docx_parsing', condition: 'format === "docx"' },
        { from: 'parsing_router', to: 'xlsx_parsing', condition: 'format === "xlsx"' },
        { from: 'pdf_parsing', to: 'content_analysis' },
        { from: 'docx_parsing', to: 'content_analysis' },
        { from: 'xlsx_parsing', to: 'content_analysis' },
        { from: 'content_analysis', to: 'result_formatter' },
        { from: 'result_formatter', to: 'end' }
      ],
      metadata: {
        created_at: new Date().toISOString(),
        updated_at: new Date().toISOString(),
        version: '1.0.0',
        tags: ['document', 'parsing', 'ai', 'analysis'],
        estimatedDuration: '30-120s',
        complexity: 'medium'
      }
    }
  });
});

/**
 * 执行：案例应用指导工作流
 * 入参：{ fileName, fileType, base64?, text?, query?, saveScope='user'|'session', conversation_id, category? }
 */
app.post('/api/workflows/case-guidance/execute', authenticateUser, async (req, res) => {
  const userId = req.user.id;
  const { fileName = '未命名', fileType = 'text', base64, text, query, saveScope = 'user', conversation_id, category } = req.body || {};
  try {
    if (!conversation_id) return res.status(400).json({ success: false, message: 'conversation_id 必填' });

    // Step: parse
    let plainText = '';
    let parseMeta = {};
    try {
      const pluginId = pickDocPluginByType(fileType);
      if (pluginId) {
        const input = {};
        if (base64) input.base64 = base64;
        if (text) input.text = text;
        const exec = await pluginEcosystem.executePlugin(pluginId, input, {});
        if (exec && exec.success) {
          if (exec.text) plainText = exec.text;
          else if (exec.preview) plainText = JSON.stringify(exec.preview).slice(0, 20000);
          else if (exec.data) plainText = (typeof exec.data === 'string') ? exec.data : JSON.stringify(exec.data).slice(0, 20000);
          parseMeta = { pluginId, length: plainText.length };
        }
      }
    } catch (e) {
      console.warn('解析失败回退文本', e.message);
    }
    if (!plainText) plainText = text || '';
    await appendTrace(cacheService, userId, conversation_id, { ts: Date.now(), category: 'parse', name: fileName, status: 'success', output: parseMeta });

    // Step: ingest_kb
    const chunks = simpleChunkText(plainText, 1200).map((c, idx) => ({ docId: `doc_${Date.now()}`, idx, title: fileName, text: c }));
    const scopeKey = saveScope === 'session' && conversation_id ? `${userId}:${conversation_id}` : `${userId}`;
    const existed = await kbGetChunks(cacheService, scopeKey);
    const next = [...(existed || []), ...chunks];
    await kbSetChunks(cacheService, scopeKey, next);
    await appendTrace(cacheService, userId, conversation_id, { ts: Date.now(), category: 'ingest_kb', name: 'chunks', status: 'success', output: { added: chunks.length, total: next.length } });

    // Step: search_kb（若有query）
    let kbHits = [];
    if (query) {
      const scored = (next || []).map(ch => ({
        docId: ch.docId,
        title: ch.title,
        idx: ch.idx,
        score: scoreByKeywordOverlap(query, ch.text),
        content: ch.text
      })).sort((a,b)=>b.score-a.score).slice(0, 5);
      kbHits = scored.map(s => ({ docId: s.docId, title: s.title, idx: s.idx, score: Number(s.score.toFixed(3)) }));
      await appendTrace(cacheService, userId, conversation_id, { ts: Date.now(), category: 'search_kb', name: 'keyword-overlap', status: 'success', input: { query }, output: { hits: kbHits } });
    }

    // Step: case_guidance（结合历史案例库）
    let guidance = null;
    try {
      const resp = await axios.post('http://localhost:3004/api/chat/case-guidance', { query: query || fileName, category, limit: 8 });
      if (resp.data?.success) guidance = resp.data.data?.guidance;
    } catch (e) {
      console.warn('调用case-guidance失败', e.message);
    }
    await appendTrace(cacheService, userId, conversation_id, { ts: Date.now(), category: 'case_guidance', name: 'generate', status: guidance ? 'success' : 'error' });

    // Step: respond
    await appendTrace(cacheService, userId, conversation_id, { ts: Date.now(), category: 'respond', name: 'return', status: 'success' });

    return res.json({ success: true, data: { guidance, kb_hits: kbHits } });
  } catch (error) {
    console.error('案例应用指导工作流失败:', error);
    res.status(500).json({ success: false, message: '案例应用指导工作流失败', error: error.message });
  }
});

/**
 * 新增：从对话消息入库（将选中消息内容保存到KB）
 * 入参：{ conversation_id, messages: [{id?, role, content}], saveScope, conversation_scope_id? }
 */
app.post('/api/knowledge/ingest-from-messages', authenticateUser, async (req, res) => {
  try {
    const { conversation_id, messages = [], saveScope = 'user', conversation_scope_id } = req.body || {};
    if (!conversation_id || !Array.isArray(messages) || !messages.length) {
      return res.status(400).json({ success: false, message: 'conversation_id 与 messages 不能为空' });
    }

    const plainText = messages.map(m => `【${m.role}】${m.content}`).join('\n');
    const chunks = simpleChunkText(plainText, 1200).map((c, idx) => ({ docId: `conv_${conversation_id}`, idx, title: `对话:${conversation_id}`, text: c }));

    const scopeKey = saveScope === 'session' && conversation_scope_id ? `${req.user.id}:${conversation_scope_id}` : `${req.user.id}`;
    const existed = await kbGetChunks(cacheService, scopeKey);
    const next = [...(existed || []), ...chunks];
    await kbSetChunks(cacheService, scopeKey, next);

    // 记录Trace
    const trace = { ts: Date.now(), category: 'ingest', name: 'messages->kb', status: 'success', input: { count: messages.length }, output: { added: chunks.length, total: next.length } };
    await appendTrace(cacheService, req.user.id, conversation_id, trace);

    res.json({ success: true, data: { added: chunks.length, total: next.length } });
  } catch (error) {
    console.error('对话消息入库失败:', error);
    res.status(500).json({ success: false, message: '对话消息入库失败', error: error.message });
  }
});

// ================================
// Plugin 插件 API
// ================================

/**
 * 获取插件列表
 */
app.get('/api/plugins-demo', authenticateUser, async (req, res) => {
  try {
    const { page = 1, limit = 20, search = '', category = '', status = '' } = req.query;

    // 模拟插件数据
    const plugins = [
      {
        id: 'plugin_1',
        name: 'Web搜索插件',
        description: '提供实时网络搜索功能，支持多种搜索引擎',
        category: 'search',
        version: '1.2.0',
        author: 'QMS Team',
        status: 'installed',
        enabled: true,
        capabilities: ['网络搜索', '多引擎支持', '结果过滤'],
        type: 'custom_plugin',
        api_endpoints: [
          {
            name: 'search',
            method: 'POST',
            path: '/search',
            description: '执行网络搜索'
          }
        ],
        config_schema: {
          search_engine: {
            type: 'string',
            enum: ['google', 'bing', 'baidu'],
            default: 'google'
          },
          max_results: {
            type: 'number',
            default: 10,
            min: 1,
            max: 50
          }
        },
        created_at: '2025-08-06T05:50:54.112Z',
        updated_at: new Date().toISOString()
      },
      {
        id: 'plugin_2',
        name: '文档生成插件',
        description: '自动生成各种格式的文档（PDF、Word、Excel）',
        category: 'document',
        version: '2.1.0',
        author: 'QMS Team',
        status: 'installed',
        enabled: true,
        capabilities: ['PDF生成', 'Word生成', '模板支持', '批量处理'],
        type: 'custom_plugin',
        api_endpoints: [
          {
            name: 'generate_pdf',
            method: 'POST',
            path: '/generate/pdf',
            description: '生成PDF文档'
          },
          {
            name: 'generate_word',
            method: 'POST',
            path: '/generate/word',
            description: '生成Word文档'
          }
        ],
        config_schema: {
          template_style: {
            type: 'string',
            enum: ['modern', 'classic', 'minimal'],
            default: 'modern'
          },
          include_watermark: {
            type: 'boolean',
            default: false
          }
        },
        created_at: '2025-08-06T05:50:54.112Z',
        updated_at: new Date().toISOString()
      },
      {
        id: 'plugin_3',
        name: '邮件发送插件',
        description: '支持发送邮件通知和报告',
        category: 'communication',
        version: '1.0.5',
        author: 'QMS Team',
        status: 'available',
        enabled: false,
        capabilities: ['邮件发送', 'SMTP支持', '模板邮件', '附件支持'],
        type: 'langchain_tool',
        api_endpoints: [
          {
            name: 'send_email',
            method: 'POST',
            path: '/send',
            description: '发送邮件'
          }
        ],
        config_schema: {
          smtp_server: {
            type: 'string',
            required: true
          },
          smtp_port: {
            type: 'number',
            default: 587
          },
          use_ssl: {
            type: 'boolean',
            default: true
          }
        },
        created_at: '2025-08-06T05:50:54.112Z',
        updated_at: new Date().toISOString()
      }
    ];

    // 过滤
    let filteredPlugins = plugins;
    if (search) {
      filteredPlugins = filteredPlugins.filter(p =>
        p.name.toLowerCase().includes(search.toLowerCase()) ||
        p.description.toLowerCase().includes(search.toLowerCase())
      );
    }
    if (category) {
      filteredPlugins = filteredPlugins.filter(p => p.category === category);
    }
    if (status) {
      filteredPlugins = filteredPlugins.filter(p => p.status === status);
    }

    res.json({
      success: true,
      data: {
        plugins: filteredPlugins,
        pagination: {
          page: parseInt(page),
          limit: parseInt(limit),
          total: filteredPlugins.length,
          pages: Math.ceil(filteredPlugins.length / limit)
        }
      }
    });
  } catch (error) {
    console.error('获取插件列表失败:', error);
    res.status(500).json({
      success: false,
      error: '获取插件列表失败',
      message: error.message
    });
  }
});

/**
 * 安装插件（统一走真实安装逻辑）
 */
app.post('/api/plugins-demo/:id/install', authenticateUser, async (req, res) => {
  try {
    const { id } = req.params;
    const { config = {} } = req.body;

    console.log(`📦 用户 ${req.user.id} 安装插件(兼容路由): ${id}`);
    const installResult = await pluginEcosystem.installPlugin(id, config);

    res.json({
      success: true,
      data: installResult,
      message: '插件安装成功'
    });
  } catch (error) {
    console.error('安装插件失败:', error);
    res.status(500).json({
      success: false,
      error: '安装插件失败',
      message: error.message
    });
  }
});

/**
 * 执行插件功能
 */
app.post('/api/plugins-demo/:id/execute', authenticateUser, async (req, res) => {
  try {
    const { id } = req.params;
    const { endpoint, parameters = {} } = req.body;

    if (!endpoint) {
      return res.status(400).json({
        success: false,
        error: '插件端点不能为空'
      });
    }

    // 模拟插件执行
    let result = {};

    if (id === 'plugin_1' && endpoint === 'search') {
      // Web搜索插件
      result = {
        query: parameters.query || '',
        results: [
          {
            title: 'ISO 9001质量管理体系标准',
            url: 'https://www.iso.org/iso-9001-quality-management.html',
            snippet: 'ISO 9001是世界上最广泛使用的质量管理体系标准...'
          },
          {
            title: '质量管理体系实施指南',
            url: 'https://example.com/qms-guide',
            snippet: '本指南详细介绍了如何实施和维护质量管理体系...'
          }
        ],
        total_results: 2
      };
    } else if (id === 'plugin_2' && endpoint === 'generate_pdf') {
      // 文档生成插件
      result = {
        document_id: `doc_${Date.now()}`,
        file_url: '/downloads/generated_document.pdf',
        file_size: 1024576,
        pages: 15
      };
    }

    res.json({
      success: true,
      data: {
        plugin_id: id,
        endpoint,
        result
      }
    });
  } catch (error) {
    console.error('执行插件失败:', error);
    res.status(500).json({
      success: false,
      error: '执行插件失败',
      message: error.message
    });
  }
});

// ================================
// 项目管理 API
// ================================

// 获取项目列表
app.get('/api/projects', authenticateUser, async (req, res) => {
  try {
    const { page = 1, limit = 20, search = '' } = req.query;

    // 模拟项目数据
    const projects = [
      {
        id: 'project_1',
        user_id: req.user.id,
        name: 'QMS质量管理项目',
        description: '基于ISO标准的质量管理系统项目',
        type: 'quality_management',
        status: 'active',
        agents_count: 2,
        workflows_count: 1,
        knowledge_count: 3,
        created_at: '2025-08-06T05:50:54.112Z',
        updated_at: new Date().toISOString()
      },
      {
        id: 'project_2',
        user_id: req.user.id,
        name: '测试项目',
        description: '用于测试Coze Studio功能的项目',
        type: 'test',
        status: 'draft',
        agents_count: 1,
        workflows_count: 0,
        knowledge_count: 1,
        created_at: '2025-08-06T05:50:54.112Z',
        updated_at: new Date().toISOString()
      }
    ];

    // 简单的搜索过滤
    let filteredProjects = projects;
    if (search) {
      filteredProjects = projects.filter(p =>
        p.name.toLowerCase().includes(search.toLowerCase()) ||
        p.description.toLowerCase().includes(search.toLowerCase())
      );
    }

    res.json({
      success: true,
      data: {
        projects: filteredProjects,
        pagination: {
          page: parseInt(page),
          limit: parseInt(limit),
          total: filteredProjects.length,
          pages: Math.ceil(filteredProjects.length / limit)
        }
      }
    });
  } catch (error) {
    console.error('获取项目列表失败:', error);
    res.status(500).json({
      success: false,
      error: '获取项目列表失败',
      message: error.message
    });
  }
});

// 创建项目
app.post('/api/projects', authenticateUser, async (req, res) => {
  try {
    const { name, description, type = 'general' } = req.body;

    if (!name) {
      return res.status(400).json({
        success: false,
        error: '项目名称不能为空'
      });
    }

    const project = {
      id: `project_${Date.now()}`,
      user_id: req.user.id,
      name,
      description: description || '',
      type,
      status: 'draft',
      agents_count: 0,
      workflows_count: 0,
      knowledge_count: 0,
      created_at: new Date().toISOString(),
      updated_at: new Date().toISOString()
    };

    res.status(201).json({
      success: true,
      data: { project },
      message: '项目创建成功'
    });
  } catch (error) {
    console.error('创建项目失败:', error);
    res.status(500).json({
      success: false,
      error: '创建项目失败',
      message: error.message
    });
  }
});

// ================================
// AutoGPT 自主规划 API
// ================================

/**
 * 创建AutoGPT规划
 */
app.post('/api/autogpt/plans', authenticateUser, async (req, res) => {
  try {
    const { goal, context = {} } = req.body;

    if (!goal) {
      return res.status(400).json({
        success: false,
        message: '目标不能为空'
      });
    }

    console.log(`🎯 用户 ${req.user.id} 请求AutoGPT规划: ${goal}`);

    // 创建规划
    const plan = await autoGPTPlanner.planGoal(goal, {
      ...context,
      user_id: req.user.id
    });

    // 保存规划到数据库
    try {
      await dbAdapter.query(`
        INSERT INTO autogpt_plans (
          id, user_id, goal, analysis, tasks, execution_plan,
          resource_assessment, status, created_at, updated_at
        ) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
      `, [
        plan.id,
        req.user.id,
        goal,
        JSON.stringify(plan.analysis),
        JSON.stringify(plan.tasks),
        JSON.stringify(plan.plan),
        JSON.stringify(plan.resources),
        plan.status,
        plan.created_at,
        plan.created_at
      ]);
    } catch (dbError) {
      console.warn('保存AutoGPT规划到数据库失败:', dbError.message);
      // 继续返回规划结果，即使数据库保存失败
    }

    res.json({
      success: true,
      data: plan,
      message: 'AutoGPT规划创建成功'
    });

  } catch (error) {
    console.error('创建AutoGPT规划失败:', error);
    res.status(500).json({
      success: false,
      message: '创建规划失败',
      error: error.message
    });
  }
});

/**
 * 执行AutoGPT规划
 */
app.post('/api/autogpt/plans/:planId/execute', authenticateUser, async (req, res) => {
  try {
    const { planId } = req.params;
    const { context = {} } = req.body;

    // 获取规划（先尝试从数据库，如果失败则使用内存中的规划）
    let plan = null;
    try {
      const [dbPlan] = await dbAdapter.query(
        'SELECT * FROM autogpt_plans WHERE id = ? AND user_id = ?',
        [planId, req.user.id]
      );

      if (dbPlan) {
        plan = {
          id: dbPlan.id,
          goal: dbPlan.goal,
          analysis: JSON.parse(dbPlan.analysis || '{}'),
          tasks: JSON.parse(dbPlan.tasks || '[]'),
          plan: JSON.parse(dbPlan.execution_plan || '{}'),
          resources: JSON.parse(dbPlan.resource_assessment || '{}')
        };
      }
    } catch (dbError) {
      console.warn('从数据库获取规划失败:', dbError.message);
    }

    if (!plan) {
      return res.status(404).json({
        success: false,
        message: '规划不存在'
      });
    }

    console.log(`🚀 用户 ${req.user.id} 开始执行AutoGPT规划: ${planId}`);

    // 异步执行规划
    autoGPTExecutor.executePlan(plan, {
      ...context,
      user_id: req.user.id
    }).then(result => {
      console.log(`✅ AutoGPT规划执行完成: ${planId}`, result.execution_id);
    }).catch(error => {
      console.error(`❌ AutoGPT规划执行失败: ${planId}`, error);
    });

    res.json({
      success: true,
      message: 'AutoGPT规划开始执行',
      data: {
        plan_id: planId,
        status: 'executing'
      }
    });

  } catch (error) {
    console.error('执行AutoGPT规划失败:', error);
    res.status(500).json({
      success: false,
      message: '执行规划失败',
      error: error.message
    });
  }
});

/**
 * 获取单个AutoGPT规划详情
 */
app.get('/api/autogpt/plans/:planId', authenticateUser, async (req, res) => {
  try {
    const { planId } = req.params;

    // 尝试从数据库获取规划
    try {
      const [dbPlan] = await dbAdapter.query(
        'SELECT * FROM autogpt_plans WHERE id = ? AND user_id = ?',
        [planId, req.user.id]
      );

      if (dbPlan) {
        const plan = {
          id: dbPlan.id,
          goal: dbPlan.goal,
          status: dbPlan.status,
          analysis: JSON.parse(dbPlan.analysis || '{}'),
          tasks: JSON.parse(dbPlan.tasks || '[]'),
          plan: JSON.parse(dbPlan.execution_plan || '{}'),
          resource_assessment: JSON.parse(dbPlan.resource_assessment || '{}'),
          created_at: dbPlan.created_at,
          updated_at: dbPlan.updated_at,
          steps: JSON.parse(dbPlan.tasks || '[]').map((task, index) => ({
            action: task.action || task.name || `步骤 ${index + 1}`,
            description: task.description || task.details || '',
            status: task.status || 'pending',
            result: task.result || null
          })),
          progress: dbPlan.status === 'completed' ? 100 :
                   dbPlan.status === 'running' ? 50 : 0,
          steps_count: JSON.parse(dbPlan.tasks || '[]').length
        };

        return res.json({
          success: true,
          data: plan,
          message: '规划详情获取成功'
        });
      }
    } catch (dbError) {
      console.warn('从数据库获取规划失败:', dbError.message);
    }

    // 如果数据库中没有找到，返回模拟数据
    const mockPlan = {
      id: planId,
      goal: '分析手机质量数据并生成报告',
      status: 'pending',
      created_at: new Date().toISOString(),
      updated_at: new Date().toISOString(),
      steps: [
        {
          action: '数据收集',
          description: '收集最新的手机质量检测数据',
          status: 'pending',
          result: null
        },
        {
          action: '数据分析',
          description: '分析质量数据趋势和异常',
          status: 'pending',
          result: null
        },
        {
          action: '报告生成',
          description: '生成质量分析报告',
          status: 'pending',
          result: null
        }
      ],
      progress: 0,
      steps_count: 3
    };

    res.json({
      success: true,
      data: mockPlan,
      message: '规划详情获取成功'
    });

  } catch (error) {
    console.error('获取AutoGPT规划详情失败:', error);
    res.status(500).json({
      success: false,
      message: '获取规划详情失败',
      error: error.message
    });
  }
});

/**
 * 获取AutoGPT规划列表
 */
app.get('/api/autogpt/plans', authenticateUser, async (req, res) => {
  try {
    const { page = 1, limit = 20, status = '' } = req.query;

    // 模拟规划列表（如果数据库不可用）
    const mockPlans = [
      {
        id: 'plan_demo_1',
        user_id: req.user.id,
        goal: '分析手机质量数据并生成报告',
        status: 'pending',
        created_at: new Date().toISOString(),
        updated_at: new Date().toISOString(),
        progress: 0,
        steps_count: 5
      },
      {
        id: 'plan_demo_2',
        user_id: req.user.id,
        goal: '优化生产流程效率',
        status: 'running',
        created_at: new Date(Date.now() - 86400000).toISOString(),
        updated_at: new Date().toISOString(),
        progress: 60,
        steps_count: 8
      },
      {
        id: 'plan_demo_3',
        user_id: req.user.id,
        goal: '自动化测试流程设计',
        status: 'completed',
        created_at: new Date(Date.now() - 172800000).toISOString(),
        updated_at: new Date(Date.now() - 3600000).toISOString(),
        progress: 100,
        steps_count: 6
      }
    ];

    res.json({
      success: true,
      data: {
        plans: mockPlans,
        pagination: {
          page: parseInt(page) || 1,
          limit: parseInt(limit) || 20,
          total: mockPlans.length,
          pages: Math.ceil(mockPlans.length / (parseInt(limit) || 20))
        }
      }
    });

  } catch (error) {
    console.error('获取AutoGPT规划列表失败:', error);
    res.status(500).json({
      success: false,
      message: '获取规划列表失败',
      error: error.message
    });
  }
});

/**
 * 获取环境状态和模型可用性报告
 */
app.get('/api/autogpt/environment', authenticateUser, async (req, res) => {
  try {
    const environmentReport = await smartModelSelector.getEnvironmentReport();

    res.json({
      success: true,
      data: environmentReport,
      message: '环境状态获取成功'
    });

  } catch (error) {
    console.error('获取环境状态失败:', error);
    res.status(500).json({
      success: false,
      message: '获取环境状态失败',
      error: error.message
    });
  }
});

// ================================
// CrewAI 多智能体协作 API
// ================================

/**
 * 创建Agent团队
 */
app.post('/api/crewai/crews', authenticateUser, async (req, res) => {
  try {
    const { name, description, agents, collaboration_mode = 'sequential' } = req.body;

    if (!name || !agents || agents.length === 0) {
      return res.status(400).json({
        success: false,
        message: '团队名称和Agent列表不能为空'
      });
    }

    console.log(`🤖 用户 ${req.user.id} 创建Agent团队: ${name}`);

    // 创建团队
    const crew = await crewAICoordinator.createCrew({
      name,
      description,
      agents,
      collaboration_mode,
      user_id: req.user.id
    });

    res.json({
      success: true,
      data: crew,
      message: 'Agent团队创建成功'
    });

  } catch (error) {
    console.error('创建Agent团队失败:', error);
    res.status(500).json({
      success: false,
      message: '创建团队失败',
      error: error.message
    });
  }
});

/**
 * 执行团队任务
 */
app.post('/api/crewai/crews/:crewId/execute', authenticateUser, async (req, res) => {
  try {
    const { crewId } = req.params;
    const { task, context = {} } = req.body;

    if (!task || !task.description) {
      return res.status(400).json({
        success: false,
        message: '任务描述不能为空'
      });
    }

    console.log(`🚀 用户 ${req.user.id} 执行团队任务: ${crewId}`);

    // 异步执行团队任务
    crewAICoordinator.executeCrewTask(crewId, task, {
      ...context,
      user_id: req.user.id
    }).then(result => {
      console.log(`✅ 团队任务执行完成: ${crewId}`, result.execution_id);
    }).catch(error => {
      console.error(`❌ 团队任务执行失败: ${crewId}`, error);
    });

    res.json({
      success: true,
      message: '团队任务开始执行',
      data: {
        crew_id: crewId,
        status: 'executing'
      }
    });

  } catch (error) {
    console.error('执行团队任务失败:', error);
    res.status(500).json({
      success: false,
      message: '执行任务失败',
      error: error.message
    });
  }
});

/**
 * 获取团队列表
 */
app.get('/api/crewai/crews', authenticateUser, async (req, res) => {
  try {
    const { page = 1, limit = 20 } = req.query;

    // 模拟团队列表（如果数据库不可用）
    const mockCrews = [
      {
        id: 'crew_demo_1',
        name: '手机质量检测团队',
        description: '专门负责手机质量检测和分析的Agent团队',
        agents: [
          { role: 'data_collector', name: '数据收集员' },
          { role: 'quality_analyst', name: '质量分析师' },
          { role: 'defect_inspector', name: '缺陷检测专家' },
          { role: 'report_writer', name: '报告撰写专家' }
        ],
        collaboration_mode: 'sequential',
        status: 'active',
        created_at: new Date().toISOString()
      },
      {
        id: 'crew_demo_2',
        name: '流程优化团队',
        description: '专门负责质量流程分析和优化的Agent团队',
        agents: [
          { role: 'process_optimizer', name: '流程优化师' },
          { role: 'compliance_checker', name: '合规检查员' },
          { role: 'quality_analyst', name: '质量分析师' }
        ],
        collaboration_mode: 'hierarchical',
        status: 'active',
        created_at: new Date().toISOString()
      }
    ];

    res.json({
      success: true,
      data: {
        crews: mockCrews,
        pagination: {
          page: parseInt(page),
          limit: parseInt(limit),
          total: mockCrews.length,
          pages: 1
        }
      }
    });

  } catch (error) {
    console.error('获取团队列表失败:', error);
    res.status(500).json({
      success: false,
      message: '获取团队列表失败',
      error: error.message
    });
  }
});

/**
 * 获取Agent角色模板
 */
app.get('/api/crewai/agent-roles', authenticateUser, async (req, res) => {
  try {
    const agentRoles = crewAICoordinator.agentRoles;

    res.json({
      success: true,
      data: {
        roles: Object.entries(agentRoles).map(([key, role]) => ({
          id: key,
          ...role
        })),
        collaboration_modes: crewAICoordinator.collaborationModes
      }
    });

  } catch (error) {
    console.error('获取Agent角色失败:', error);
    res.status(500).json({
      success: false,
      message: '获取角色列表失败',
      error: error.message
    });
  }
});

// ================================
// LangChain Memory 记忆系统 API
// ================================

/**
 * 为Agent创建记忆系统
 */
app.post('/api/memory/agents/:agentId/create', authenticateUser, async (req, res) => {
  try {
    const { agentId } = req.params;
    const { memoryConfig = {} } = req.body;

    console.log(`🧠 用户 ${req.user.id} 为Agent ${agentId} 创建记忆系统`);

    // 创建记忆系统
    const memory = await langChainMemory.createMemoryForAgent(agentId, memoryConfig);

    res.json({
      success: true,
      data: {
        memory_id: memory.id,
        agent_id: agentId,
        config: memory.config,
        created_at: memory.created_at
      },
      message: 'Agent记忆系统创建成功'
    });

  } catch (error) {
    console.error('创建记忆系统失败:', error);
    res.status(500).json({
      success: false,
      message: '创建记忆系统失败',
      error: error.message
    });
  }
});

/**
 * 添加对话记忆
 */
app.post('/api/memory/agents/:agentId/conversations', authenticateUser, async (req, res) => {
  try {
    const { agentId } = req.params;
    const { conversationId, message, response } = req.body;

    if (!message || !response) {
      return res.status(400).json({
        success: false,
        message: '消息和回复不能为空'
      });
    }

    console.log(`💭 用户 ${req.user.id} 添加对话记忆: Agent ${agentId}`);

    // 添加对话记忆
    const memoryEntry = await langChainMemory.addConversationMemory(
      agentId,
      conversationId || uuidv4(),
      message,
      response
    );

    res.json({
      success: true,
      data: {
        memory_entry_id: memoryEntry.id,
        importance: memoryEntry.importance,
        timestamp: memoryEntry.timestamp
      },
      message: '对话记忆添加成功'
    });

  } catch (error) {
    console.error('添加对话记忆失败:', error);
    res.status(500).json({
      success: false,
      message: '添加对话记忆失败',
      error: error.message
    });
  }
});

/**
 * 检索相关记忆
 */
app.post('/api/memory/agents/:agentId/retrieve', authenticateUser, async (req, res) => {
  try {
    const { agentId } = req.params;
    const { query, options = {} } = req.body;

    if (!query) {
      return res.status(400).json({
        success: false,
        message: '查询内容不能为空'
      });
    }

    console.log(`🔍 用户 ${req.user.id} 检索Agent ${agentId} 的相关记忆`);

    // 检索相关记忆
    const relevantMemory = await langChainMemory.retrieveRelevantMemory(agentId, query, options);

    res.json({
      success: true,
      data: relevantMemory,
      message: '记忆检索成功'
    });

  } catch (error) {
    console.error('检索记忆失败:', error);
    res.status(500).json({
      success: false,
      message: '检索记忆失败',
      error: error.message
    });
  }
});

/**
 * 获取Agent记忆统计
 */
app.get('/api/memory/agents/:agentId/stats', authenticateUser, async (req, res) => {
  try {
    const { agentId } = req.params;

    console.log(`📊 用户 ${req.user.id} 获取Agent ${agentId} 记忆统计`);

    // 获取记忆统计
    const stats = await langChainMemory.getMemoryStats(agentId);

    res.json({
      success: true,
      data: {
        agent_id: agentId,
        ...stats
      },
      message: '记忆统计获取成功'
    });

  } catch (error) {
    console.error('获取记忆统计失败:', error);
    res.status(500).json({
      success: false,
      message: '获取记忆统计失败',
      error: error.message
    });
  }
});

/**
 * 清理Agent过期记忆
 */
app.delete('/api/memory/agents/:agentId/cleanup', authenticateUser, async (req, res) => {
  try {
    const { agentId } = req.params;
    const { retentionDays = 30 } = req.query;

    console.log(`🧹 用户 ${req.user.id} 清理Agent ${agentId} 过期记忆`);

    // 清理过期记忆
    await langChainMemory.cleanupExpiredMemory(agentId, parseInt(retentionDays));

    res.json({
      success: true,
      message: `Agent过期记忆清理成功 (保留${retentionDays}天)`
    });

  } catch (error) {
    console.error('清理过期记忆失败:', error);
    res.status(500).json({
      success: false,
      message: '清理过期记忆失败',
      error: error.message
    });
  }
});

// ================================
// 插件生态系统 API
// ================================

/**
 * 获取插件列表
 */
app.get('/api/plugins', authenticateUserOrAnonymous, async (req, res) => {
  try {
    const { type, category, status } = req.query;

    console.log(`🔌 用户 ${req.user.id} 获取插件列表`);

    // 获取插件列表
    const plugins = pluginEcosystem.getPluginList({
      type,
      category,
      status
    });

    res.json({
      success: true,
      data: {
        plugins: plugins,
        total: plugins.length,
        plugin_types: Object.keys(pluginEcosystem.pluginTypes),
        categories: [...new Set(plugins.map(p => p.category))]
      },
      message: '插件列表获取成功'
    });

  } catch (error) {
    console.error('获取插件列表失败:', error);
    res.status(500).json({
      success: false,
      message: '获取插件列表失败',
      error: error.message
    });
  }
});

/**
 * 获取插件统计信息
 */
app.get('/api/plugins/stats', authenticateUserOrAnonymous, async (req, res) => {
  try {
    console.log(`📊 用户 ${req.user.id} 获取插件统计`);

    // 获取插件统计
    const stats = pluginEcosystem.getPluginStats();

    res.json({
      success: true,
      data: stats,
      message: '插件统计获取成功'
    });

  } catch (error) {
    console.error('获取插件统计失败:', error);
    res.status(500).json({
      success: false,
      message: '获取插件统计失败',
      error: error.message
    });
  }
});

/**
 * 获取插件详情
 */
app.get('/api/plugins/:pluginId', authenticateUserOrAnonymous, async (req, res) => {
  try {
    const { pluginId } = req.params;

    console.log(`🔍 用户 ${req.user.id} 获取插件详情: ${pluginId}`);

    // 获取插件详情
    const plugin = pluginEcosystem.getPluginDetails(pluginId);

    if (!plugin) {
      return res.status(404).json({
        success: false,
        message: '插件不存在'
      });
    }

    res.json({
      success: true,
      data: plugin,
      message: '插件详情获取成功'
    });

  } catch (error) {
    console.error('获取插件详情失败:', error);
    res.status(500).json({
      success: false,
      message: '获取插件详情失败',
      error: error.message
    });
  }
});

/**
 * 安装插件
 */
app.post('/api/plugins/:pluginId/install', authenticateUser, async (req, res) => {
  try {
    const { pluginId } = req.params;
    const { config = {} } = req.body;

    console.log(`📦 用户 ${req.user.id} 安装插件: ${pluginId}`);

    // 安装插件
    const installResult = await pluginEcosystem.installPlugin(pluginId, config);

    res.json({
      success: true,
      data: installResult,
      message: '插件安装成功'
    });

  } catch (error) {
    console.error('安装插件失败:', error);
    res.status(500).json({
      success: false,
      message: '安装插件失败',
      error: error.message
    });
  }
});

/**
 * 执行插件
 */
app.post('/api/plugins/:pluginId/execute', authenticateUserOrAnonymous, async (req, res) => {
  try {
    const { pluginId } = req.params;
    const { input, options = {} } = req.body;

    if (!input) {
      return res.status(400).json({
        success: false,
        message: '插件输入不能为空'
      });
    }

    console.log(`🔄 用户 ${req.user.id} 执行插件: ${pluginId}`);
    console.log('📥 接收到的输入数据:', JSON.stringify(input, null, 2));
    console.log('⚙️ 执行选项:', JSON.stringify(options, null, 2));

    // 执行插件
    const executionResult = await pluginEcosystem.executePlugin(pluginId, input, options);

    res.json({
      success: executionResult.success,
      data: executionResult,
      message: executionResult.success ? '插件执行成功' : '插件执行失败'
    });

  } catch (error) {
    console.error('执行插件失败:', error);
    res.status(500).json({
      success: false,
      message: '执行插件失败',
      error: error.message
    });
  }
});

/**
 * 执行插件 (Coze Studio路径)
 */
app.post('/api/coze-studio/plugins/:pluginId/execute', authenticateUserOrAnonymous, async (req, res) => {
  try {
    const { pluginId } = req.params;
    const { input, options = {} } = req.body;

    if (!input) {
      return res.status(400).json({
        success: false,
        message: '插件输入不能为空'
      });
    }

    console.log(`🔄 用户 ${req.user.id} 通过Coze Studio执行插件: ${pluginId}`);

    // 执行插件
    const executionResult = await pluginEcosystem.executePlugin(pluginId, input, options);

    res.json({
      success: executionResult.success,
      data: executionResult,
      message: executionResult.success ? '插件执行成功' : '插件执行失败'
    });

  } catch (error) {
    console.error('执行插件失败:', error);
    res.status(500).json({
      success: false,
      message: '执行插件失败',
      error: error.message
    });
  }
});

/**
 * 执行文档解析工作流
 */
app.post('/api/workflows/execute/document-parsing', authenticateUserOrAnonymous, async (req, res) => {
  try {
    const { inputData, detectedFormat } = req.body;

    if (!inputData) {
      return res.status(400).json({
        success: false,
        message: '输入数据不能为空'
      });
    }

    console.log(`🔄 用户 ${req.user.id} 执行文档解析工作流`);

    // 本地定义文档插件选择函数
    const pickDocPluginByType = (fileType = 'text') => {
      const t = (fileType || '').toLowerCase();
      if (t.includes('pdf')) return 'pdf_parser';
      if (t.includes('csv')) return 'csv_parser';
      if (t.includes('xlsx') || t.includes('excel') || t === 'xls') return 'xlsx_parser';
      if (t.includes('docx') || t.includes('doc')) return 'docx_parser';
      if (t.includes('json')) return 'json_parser';
      if (t.includes('xml')) return 'xml_parser';
      if (t.includes('png') || t.includes('jpg') || t.includes('jpeg') || t.includes('bmp') || t.includes('gif') || t.includes('webp')) return 'image_parser';
      if (t.includes('txt') || t === 'text') return 'text_parser';
      return null;
    };

    let result = {
      content: '',
      metadata: {},
      parser: 'unknown'
    };

    // 根据检测到的格式选择对应的解析器
    const pluginId = pickDocPluginByType(detectedFormat);

    if (pluginId && inputData.base64) {
      try {
        const pluginInput = {
          base64: inputData.base64
        };

        const executionResult = await pluginEcosystem.executePlugin(pluginId, pluginInput, {});

        if (executionResult && executionResult.success) {
          result = {
            content: executionResult.text || executionResult.data || '',
            metadata: {
              fileName: inputData.name,
              format: detectedFormat,
              fileSize: inputData.size,
              parser: pluginId
            },
            parser: pluginId
          };
        }
      } catch (error) {
        console.warn('插件解析失败，使用文本回退:', error.message);
      }
    }

    // 如果插件解析失败或没有对应插件，使用简单文本处理
    if (!result.content) {
      if (inputData.type === 'text') {
        result.content = inputData.text || inputData.content || '';
      } else if (inputData.content) {
        // 直接使用content字段
        result.content = inputData.content;
      } else if (inputData.base64) {
        try {
          const buffer = Buffer.from(inputData.base64, 'base64');
          result.content = detectAndDecodeText(buffer);
        } catch {
          result.content = '无法解析文件内容';
        }
      }

      // 如果有内容，尝试使用增强文本解析器
      if (result.content && pluginId === 'text_parser') {
        try {
          const pluginInput = {
            content: result.content,
            name: inputData.name || 'text-document.txt'
          };

          const executionResult = await pluginEcosystem.executePlugin('text_parser', pluginInput, {});

          if (executionResult && executionResult.success) {
            result = {
              content: executionResult.text || result.content,
              rawContent: result.content,
              structure: executionResult.structure,
              metadata: {
                ...executionResult.metadata,
                fileName: inputData.name || 'text-document.txt',
                format: detectedFormat,
                fileSize: inputData.size || result.content.length,
                parser: 'enhanced_text_parser'
              },
              summary: executionResult.summary,
              keywords: executionResult.keywords,
              parser: 'enhanced_text_parser'
            };
          }
        } catch (enhancedError) {
          console.warn('增强文本解析失败，使用基础处理:', enhancedError.message);
        }
      }

      // 如果还没有设置metadata，使用默认值
      if (!result.metadata) {
        result.metadata = {
          fileName: inputData.name || 'unknown',
          format: detectedFormat,
          fileSize: inputData.size || (result.content ? result.content.length : 0),
          parser: 'fallback_text'
        };
        result.parser = 'fallback_text';
      }
    }

    res.json({
      success: true,
      data: result,
      message: '文档解析成功'
    });

  } catch (error) {
    console.error('文档解析工作流执行失败:', error);
    res.status(500).json({
      success: false,
      message: '文档解析失败',
      error: error.message
    });
  }
});

/**
 * AI文档分析
 */
app.post('/api/ai/analyze-document', authenticateUserOrAnonymous, async (req, res) => {
  try {
    const { content, analysisTypes = ['structure', 'keywords', 'summary'] } = req.body;

    if (!content || typeof content !== 'string') {
      return res.status(400).json({
        success: false,
        message: '文档内容不能为空'
      });
    }

    console.log(`🤖 用户 ${req.user.id} 请求AI文档分析`);

    const analysis = {};

    // 基础统计分析
    const words = content.split(/\s+/).filter(Boolean);
    const sentences = content.split(/[.!?]+/).filter(Boolean);
    const paragraphs = content.split(/\n\s*\n/).filter(Boolean);

    if (analysisTypes.includes('structure')) {
      analysis.structure = `文档结构分析：
- 总字数：${words.length}
- 句子数：${sentences.length}
- 段落数：${paragraphs.length}
- 平均句长：${Math.round(words.length / sentences.length)} 词/句
- 平均段长：${Math.round(sentences.length / paragraphs.length)} 句/段`;
    }

    if (analysisTypes.includes('keywords')) {
      // 简单的关键词提取
      const wordCount = {};
      words.forEach(word => {
        const cleanWord = word.toLowerCase().replace(/[^\w\u4e00-\u9fff]/g, '');
        if (cleanWord.length > 2) {
          wordCount[cleanWord] = (wordCount[cleanWord] || 0) + 1;
        }
      });

      analysis.keywords = Object.entries(wordCount)
        .sort(([,a], [,b]) => b - a)
        .slice(0, 15)
        .map(([word]) => word);
    }

    if (analysisTypes.includes('summary')) {
      // 简单的摘要生成（取前几句）
      const firstSentences = sentences.slice(0, 3).join('。');
      analysis.summary = firstSentences.length > 200
        ? firstSentences.substring(0, 200) + '...'
        : firstSentences;
    }

    if (analysisTypes.includes('entities')) {
      // 简单的实体识别（基于模式匹配）
      const entities = [];

      // 匹配邮箱
      const emailPattern = /\b[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\.[A-Z|a-z]{2,}\b/g;
      const emails = content.match(emailPattern) || [];
      emails.forEach(email => {
        entities.push({ text: email, type: 'EMAIL' });
      });

      // 匹配电话号码
      const phonePattern = /\b\d{3}-\d{3}-\d{4}\b|\b\d{11}\b/g;
      const phones = content.match(phonePattern) || [];
      phones.forEach(phone => {
        entities.push({ text: phone, type: 'PHONE' });
      });

      // 匹配日期
      const datePattern = /\b\d{4}[-/]\d{1,2}[-/]\d{1,2}\b/g;
      const dates = content.match(datePattern) || [];
      dates.forEach(date => {
        entities.push({ text: date, type: 'DATE' });
      });

      analysis.entities = entities.slice(0, 20); // 限制数量
    }

    res.json({
      success: true,
      data: {
        analysis,
        model: 'basic_nlp',
        timestamp: new Date().toISOString()
      },
      message: 'AI分析完成'
    });

  } catch (error) {
    console.error('AI文档分析失败:', error);
    res.status(500).json({
      success: false,
      message: 'AI分析失败',
      error: error.message
    });
  }
});



/**
 * 查询文档解析工作流的执行结果
 */
app.get('/api/workflows/document-parsing/executions/:id', authenticateUserOrAnonymous, async (req, res) => {
  const exec = executionStore.get(req.params.id)
  if(!exec) return res.status(404).json({ success:false, message:'未找到执行记录' })
  res.json({ success:true, data: exec })
})

/**
 * 列出最近N次执行（演示）
 */
app.get('/api/workflows/document-parsing/executions', authenticateUserOrAnonymous, async (req, res) => {
  const list = Array.from(executionStore.values()).slice(-50).reverse()
  res.json({ success:true, data: list })
})

/**
 * 通用工作流运行（最小实现）
 * 路径：/api/workflows/run
 * 输入：{ workflow: { nodes: [...] }, input: { file: { name,type,base64,text } } }
 * 特性：线性顺序执行（后续可按 edges 拓扑排序）；格式自适应（文本/表格分别分析）
 */
app.post('/api/workflows/run', authenticateUserOrAnonymous, async (req, res) => {
  try {
    const startAll = Date.now();
    const { workflow = {}, input = {}, options = {} } = req.body || {};
    const nodes = Array.isArray(workflow.nodes) ? workflow.nodes : [];

    const steps = [];
    const record = (title, status, data = {}, t0, t1) => steps.push({ title, status, data, started_at: t0, completed_at: t1, duration_ms: (t1 - t0) });

    // 上下文
    const ctx = { vars: {}, artifacts: {}, input };

    // 预安装基础插件（尽力而为）
    try { await ensureCorePluginsInstalled(); } catch(e) {}

    // 执行辅助
    const getFile = () => (input && input.file) ? input.file : {};

    // 文档插件选择函数（本地定义）
    const pickDocPluginByType = (fileType = 'text') => {
      const t = (fileType || '').toLowerCase();
      if (t.includes('pdf')) return 'pdf_parser';
      if (t.includes('csv')) return 'csv_parser';
      if (t.includes('xlsx') || t.includes('excel') || t === 'xls') return 'xlsx_parser';
      if (t.includes('docx') || t.includes('doc')) return 'docx_parser';
      if (t.includes('json')) return 'json_parser';
      if (t.includes('xml')) return 'xml_parser';
      if (t.includes('png') || t.includes('jpg') || t.includes('jpeg') || t.includes('bmp') || t.includes('gif') || t.includes('webp')) return 'image_parser';
      if (t.includes('txt') || t === 'text') return 'text_parser';
      return null;
    };

    // 逐节点线性执行（最小实现）
    for (const node of nodes) {
      const t0 = Date.now();
      try {
        switch (node.type) {
          case 'input_file': {
            const f = getFile();
            record('input_file', 'completed', { name: f.name, type: f.type, hasBase64: !!f.base64, textLen: (f.text||'').length }, t0, Date.now());
            break;
          }
          case 'detect_type': {
            const f = getFile();
            const inferredType = (f.type || (f.name||'').split('.').pop() || '').toLowerCase();
            ctx.vars.detectedType = inferredType;
            record('detect_type', 'completed', { inferredType }, t0, Date.now());
            break;
          }
          case 'parse_auto': {
            const f = getFile();
            const inferredType = ctx.vars.detectedType || (f.type || (f.name||'').split('.').pop() || '').toLowerCase();
            const pluginId = pickDocPluginByType(inferredType) || (inferredType.match(/png|jpg|jpeg|bmp|gif|webp/) ? 'ocr_reader' : null);
            let parseResult = null;
            if (pluginId) {
              const pin = {}; if (f.base64) pin.base64 = f.base64; if (f.text) pin.text = f.text;
              try {
                const pluginResponse = await pluginEcosystem.executePlugin(pluginId, pin, node.params || {});
                if (pluginResponse && pluginResponse.result) {
                  // 从插件响应中提取文本内容
                  parseResult = {
                    success: true,
                    type: pluginResponse.result.type || 'text',
                    text: pluginResponse.result.text || pluginResponse.result.data?.text || pluginResponse.result.data?.content || '',
                    html: pluginResponse.result.html || pluginResponse.result.data?.html || null,
                    preview: pluginResponse.result.data?.preview || [],
                    metadata: pluginResponse.result.metadata || {}
                  };
                }
              } catch(e) {
                console.warn('插件执行失败:', e.message);
                parseResult = null;
              }
            }
            if (!parseResult) {
              // 文本兜底 - 使用编码检测
              let textContent = f.text;
              if (!textContent && f.base64) {
                textContent = detectAndDecodeText(Buffer.from(f.base64, 'base64'));
              }
              parseResult = { success: true, type: 'text', text: textContent || '' };
            }
            ctx.vars.parseResult = parseResult;
            record('parse_document', 'completed', { type: parseResult?.type, textLen: (parseResult?.text||'').length, rows: (parseResult?.preview||[]).length }, t0, Date.now());
            break;
          }
          case 'normalize_auto':
          case 'normalize_text':
          case 'normalize_table': {
            const pr = ctx.vars.parseResult || {};
            const normalized = { res: {}, basicSummary: null };
            if ((node.type === 'normalize_text') || (node.type === 'normalize_auto' && pr.text)) {
              normalized.res = { type: pr.type || 'text', text: pr.text || '', html: pr.html || null, metadata: pr.metadata || {} };
            } else if ((node.type === 'normalize_table') || (node.type === 'normalize_auto' && (pr.preview || pr.rows || (pr.data && (pr.data.preview || pr.data.columns))))) {
              const rows = Array.isArray(pr.preview) ? pr.preview : (Array.isArray(pr.rows) ? pr.rows : (Array.isArray(pr.data?.preview) ? pr.data.preview : []));
              normalized.res = { type: pr.type || 'table', preview: rows.slice(0, 100), rows: rows, columns: pr.columns || pr.data?.columns || {} };
            } else {
              normalized.res = { type: 'unknown', raw: pr };
            }
            ctx.vars.normalized = normalized;
            record('normalize', 'completed', { res_type: normalized.res.type }, t0, Date.now());
            break;
          }
          case 'summarize_text': {
            const n = ctx.vars.normalized || {}; const r = n.res || {};
            if ((r.type || 'text') === 'text' && (r.text || '').length) {
              let exec = null;
              try { exec = await pluginEcosystem.executePlugin('text_summarizer', { text: r.text }, { max_sentences: (node.params?.max_sentences || 5) }); } catch(e) {}
              n.basicSummary = { kind: 'text', summary: exec?.summary || '', sentences: exec?.sentences || [] };
              ctx.vars.normalized = n;
              record('analyze_text', 'completed', { summaryLen: (n.basicSummary.summary||'').length }, t0, Date.now());
            } else {
              record('analyze_text', 'skipped', { reason: 'not_text' }, t0, Date.now());
            }
            break;
          }
          case 'table_stats': {
            const n = ctx.vars.normalized || {}; const r = n.res || {};
            if ((r.type || 'table') !== 'text' && (Array.isArray(r.preview) || Array.isArray(r.rows))) {
              const ds = Array.isArray(r.preview) ? r.preview : (r.rows || []);
              let exec = null;
              try { exec = await pluginEcosystem.executePlugin('statistical_analyzer', { dataset: ds }, node.params || {}); } catch(e) {}
              n.basicSummary = { kind: 'table', stats: exec?.stats || {}, notes: exec?.recommendations || [] };
              ctx.vars.normalized = n;
              record('analyze_table', 'completed', { fields: Object.keys(n.basicSummary.stats||{}).length }, t0, Date.now());
            } else {
              record('analyze_table', 'skipped', { reason: 'not_table' }, t0, Date.now());
            }
            break;
          }
          case 'present_text':
          case 'present_table': {
            // 展示层节点：目前仅记录
            record(node.type, 'completed', {}, t0, Date.now());
            break;
          }
          case 'output_result': {
            // 汇总输出
            record('output_result', 'completed', {}, t0, Date.now());
            break;
          }
          default: {
            record(node.type || 'unknown', 'skipped', { reason: 'unknown_node' }, t0, Date.now());
            break;
          }
        }
      } catch (e) {
        record(node.type || 'unknown', 'failed', { error: e.message }, t0, Date.now());
      }
    }

    const duration = Date.now() - startAll;
    return res.json({ success: true, data: { parsed: ctx.vars.normalized || null, steps, duration, artifacts: ctx.artifacts || {} }, message: '工作流执行完成' });
  } catch (error) {
    console.error('通用工作流执行失败:', error);
    return res.status(500).json({ success: false, message: error.message });
  }
});

/**
 * 卸载插件
 */
app.delete('/api/plugins/:pluginId/uninstall', authenticateUser, async (req, res) => {
  try {
    const { pluginId } = req.params;

    console.log(`🗑️ 用户 ${req.user.id} 卸载插件: ${pluginId}`);

    // 卸载插件
    await pluginEcosystem.uninstallPlugin(pluginId);

    res.json({
      success: true,
      message: '插件卸载成功'
    });

  } catch (error) {
    console.error('卸载插件失败:', error);
    res.status(500).json({
      success: false,
      message: '卸载插件失败',
      error: error.message
    });
  }
});



// 健康检查
app.get('/health', (req, res) => {
  res.json({
    status: 'healthy',
    service: 'coze-studio-service',
    timestamp: new Date().toISOString(),
    port: PORT
  });
});

// 启动服务
async function startServer() {
  try {
    console.log('🚀 开始启动 Coze Studio 服务...');

    // 初始化数据库
    console.log('📊 正在初始化数据库...');
    await dbAdapter.initialize();
    console.log('✅ 数据库连接成功');

    // 初始化缓存服务
    try {
      await cacheService.connect();
      console.log('✅ Redis缓存连接成功');
    } catch (cacheError) {
      console.warn('⚠️ Redis缓存连接失败，将使用内存缓存:', cacheError.message);
      // 可以在这里初始化备用的内存缓存
    }

    app.listen(PORT, async () => {
      console.log(`🚀 Coze Studio 服务启动成功！`);
      console.log(`📍 服务地址: http://localhost:${PORT}`);

      // 启动后预安装核心插件，保证首次可用
      try {
        console.log('⏭️ 跳过核心插件预安装（调试模式）');
        // await ensureCorePluginsInstalled();
        // console.log('✅ 核心插件已预安装/激活');
      } catch (e) {
        console.warn('核心插件预安装失败', e.message);
      }

      console.log(`🏥 健康检查: http://localhost:${PORT}/health`);
      console.log(`🤖 Agent API: http://localhost:${PORT}/api/agents`);
    });
  } catch (error) {
    console.error('❌ 服务启动失败:', error);
    process.exit(1);
  }
}

// 为场景运行自动确保核心插件已安装
async function ensureCorePluginsInstalled() {
  const core = ['excel_analyzer', 'statistical_analyzer', 'spc_controller'];
  for (const pid of core) {
    if (!pluginEcosystem.activePlugins || !pluginEcosystem.activePlugins.get(pid)) {
      try { await pluginEcosystem.installPlugin(pid, {}); } catch (e) { console.warn('插件自动安装失败', pid, e.message); }
    }
  }
}


// 质量问题分析场景：串联 excel_analyzer -> statistical_analyzer -> spc_controller
app.post('/api/scenarios/quality-analysis', authenticateUser, async (req, res) => {
  try {
    const { filePath, csv, dataset, valueField } = req.body || {};

    // 1) Excel/CSV 解析
    let excelInput = {};
    if (csv) excelInput.csv = csv;
    if (Array.isArray(dataset)) excelInput.dataset = dataset;
    if (filePath) {
      // 由 excel_analyzer 内部处理（后续可增强为直接读取本地文件）
      // 这里将文件读取留给插件实现，以保持接口统一
    }
    // 确保核心插件可用
    await ensureCorePluginsInstalled();

    const excelResult = await pluginEcosystem.executePlugin('excel_analyzer', excelInput);

    // 2) 统计分析
    const statInput = { dataset: excelResult.preview || [], columns: excelResult.columns };
    const statResult = await pluginEcosystem.executePlugin('statistical_analyzer', statInput);

    // 3) SPC 控制图（选择一列数值序列）
    let series = [];
    if (valueField && Array.isArray(excelResult.preview) && excelResult.preview.length) {
      series = excelResult.preview.map(r => Number(r[valueField])).filter(v => !isNaN(v));
    } else if (excelResult.columns) {
      const numericCol = Object.entries(excelResult.columns).find(([k, v]) => Array.isArray(v) && typeof v[0] === 'number');
      if (numericCol) series = numericCol[1];
    }
    const spcResult = await pluginEcosystem.executePlugin('spc_controller', { series });

    // 4) 组装报告（纯逻辑合成，后续可接模型总结）
    const report = {
      title: '质量问题分析自动报告',
      generated_at: new Date().toISOString(),
      overview: {
        rows: (excelResult.preview || []).length,
        fields: excelResult.columns ? Object.keys(excelResult.columns).length : 0,
        notes: excelResult.warnings || []
      },
      statistics: statResult.stats || {},
      spc: spcResult.chart || null,
      recommendations: [
        ...(statResult.recommendations || []),
        ...(spcResult.recommendations || [])
      ]
    };

    res.json({ success: true, data: { excelResult, statResult, spcResult, report } });
  } catch (error) {
    console.error('质量问题分析场景执行失败:', error);
    res.status(500).json({ success: false, message: error.message });
  }
});

startServer();
