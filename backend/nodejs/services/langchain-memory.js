/**
 * LangChain Memory集成系统
 * 为Agent提供多种类型的记忆能力
 */

const { v4: uuidv4 } = require('uuid');

class LangChainMemoryManager {
  constructor(aiModelCaller, dbAdapter, smartModelSelector) {
    this.aiModelCaller = aiModelCaller;
    this.dbAdapter = dbAdapter;
    this.smartModelSelector = smartModelSelector;
    
    // 记忆类型配置
    this.memoryTypes = {
      'buffer': {
        name: '缓冲记忆',
        description: '保存最近的对话历史',
        maxTokens: 2000,
        retention: '会话期间'
      },
      'summary': {
        name: '摘要记忆',
        description: '保存对话的智能摘要',
        maxTokens: 1000,
        retention: '长期'
      },
      'vector': {
        name: '向量记忆',
        description: '基于语义相似性的记忆检索',
        maxTokens: 500,
        retention: '永久'
      },
      'entity': {
        name: '实体记忆',
        description: '记住重要的人物、组织、概念等实体',
        maxTokens: 300,
        retention: '永久'
      }
    };
    
    // 记忆缓存
    this.memoryCache = new Map();
    this.entityCache = new Map();
  }

  /**
   * 为Agent创建记忆系统
   */
  async createMemoryForAgent(agentId, memoryConfig = {}) {
    console.log(`🧠 为Agent ${agentId} 创建记忆系统`);
    
    try {
      const memoryId = uuidv4();
      const defaultConfig = {
        types: ['buffer', 'summary', 'entity'], // 默认记忆类型
        maxBufferSize: 10, // 最大缓冲消息数
        summaryThreshold: 20, // 触发摘要的消息数
        entityExtractionEnabled: true,
        vectorSearchEnabled: false // 向量搜索需要额外配置
      };
      
      const finalConfig = { ...defaultConfig, ...memoryConfig };
      
      // 创建记忆实例
      const memory = {
        id: memoryId,
        agent_id: agentId,
        config: finalConfig,
        buffer: [], // 缓冲记忆
        summary: null, // 摘要记忆
        entities: new Map(), // 实体记忆
        conversation_count: 0,
        created_at: new Date().toISOString(),
        updated_at: new Date().toISOString()
      };
      
      // 缓存记忆实例
      this.memoryCache.set(agentId, memory);
      
      // 保存到数据库
      await this.saveMemoryToDatabase(memory);
      
      console.log(`✅ Agent ${agentId} 记忆系统创建成功`);
      return memory;
      
    } catch (error) {
      console.error(`❌ 创建记忆系统失败:`, error);
      throw error;
    }
  }

  /**
   * 添加对话记忆
   */
  async addConversationMemory(agentId, conversationId, message, response) {
    console.log(`💭 添加对话记忆: Agent ${agentId}`);
    
    try {
      // 获取或创建记忆
      let memory = await this.getMemoryForAgent(agentId);
      if (!memory) {
        memory = await this.createMemoryForAgent(agentId);
      }
      
      // 创建记忆条目
      const memoryEntry = {
        id: uuidv4(),
        conversation_id: conversationId,
        user_message: message,
        agent_response: response,
        timestamp: new Date().toISOString(),
        importance: this.calculateImportance(message, response)
      };
      
      // 添加到缓冲记忆
      memory.buffer.push(memoryEntry);
      memory.conversation_count++;
      memory.updated_at = new Date().toISOString();
      
      // 管理缓冲大小
      if (memory.buffer.length > memory.config.maxBufferSize) {
        memory.buffer.shift(); // 移除最旧的记忆
      }
      
      // 检查是否需要生成摘要
      if (memory.conversation_count % memory.config.summaryThreshold === 0) {
        await this.generateSummary(memory);
      }
      
      // 提取实体（如果启用）
      if (memory.config.entityExtractionEnabled) {
        await this.extractAndUpdateEntities(memory, memoryEntry);
      }
      
      // 保存记忆到数据库
      await this.saveConversationMemory(memoryEntry, agentId);
      
      // 更新缓存
      this.memoryCache.set(agentId, memory);
      
      console.log(`✅ 对话记忆添加成功`);
      return memoryEntry;
      
    } catch (error) {
      console.error(`❌ 添加对话记忆失败:`, error);
      throw error;
    }
  }

  /**
   * 检索相关记忆
   */
  async retrieveRelevantMemory(agentId, query, options = {}) {
    console.log(`🔍 检索相关记忆: Agent ${agentId}`);
    
    try {
      const memory = await this.getMemoryForAgent(agentId);
      if (!memory) {
        return { buffer: [], summary: null, entities: [], vector: [] };
      }
      
      const defaultOptions = {
        includeBuffer: true,
        includeSummary: true,
        includeEntities: true,
        includeVector: false,
        maxResults: 5
      };
      
      const finalOptions = { ...defaultOptions, ...options };
      const relevantMemory = {
        buffer: [],
        summary: null,
        entities: [],
        vector: []
      };
      
      // 获取缓冲记忆
      if (finalOptions.includeBuffer) {
        relevantMemory.buffer = memory.buffer
          .slice(-finalOptions.maxResults)
          .map(entry => ({
            user_message: entry.user_message,
            agent_response: entry.agent_response,
            timestamp: entry.timestamp,
            importance: entry.importance
          }));
      }
      
      // 获取摘要记忆
      if (finalOptions.includeSummary && memory.summary) {
        relevantMemory.summary = memory.summary;
      }
      
      // 获取相关实体
      if (finalOptions.includeEntities) {
        relevantMemory.entities = await this.findRelevantEntities(agentId, query);
      }
      
      // 向量搜索（如果启用）
      if (finalOptions.includeVector && memory.config.vectorSearchEnabled) {
        relevantMemory.vector = await this.vectorSearch(agentId, query, finalOptions.maxResults);
      }
      
      console.log(`✅ 检索到相关记忆: ${relevantMemory.buffer.length}条缓冲, ${relevantMemory.entities.length}个实体`);
      return relevantMemory;
      
    } catch (error) {
      console.error(`❌ 检索记忆失败:`, error);
      return { buffer: [], summary: null, entities: [], vector: [] };
    }
  }

  /**
   * 生成对话摘要
   */
  async generateSummary(memory) {
    console.log(`📝 生成对话摘要: Agent ${memory.agent_id}`);
    
    try {
      // 准备摘要内容
      const conversationHistory = memory.buffer.map(entry => 
        `用户: ${entry.user_message}\nAgent: ${entry.agent_response}`
      ).join('\n\n');
      
      const summaryPrompt = `
请为以下对话历史生成一个简洁的摘要，重点关注：
1. 主要讨论的话题和问题
2. 重要的决定和结论
3. 关键的信息和数据
4. 用户的偏好和需求

对话历史：
${conversationHistory}

请生成一个结构化的摘要，包含：
- 主要话题
- 关键信息
- 重要结论
- 后续行动
`;

      // 选择适合的模型生成摘要
      const selectedModel = await this.smartModelSelector.selectOptimalModel('summarization', {
        lowCost: true // 摘要任务使用低成本模型
      });
      
      const response = await this.aiModelCaller({
        model: selectedModel.id,
        systemPrompt: '你是一个专业的对话摘要助手，擅长提取对话中的关键信息。',
        temperature: 0.3
      }, [], summaryPrompt);
      
      // 更新摘要
      const newSummary = {
        content: response.content,
        generated_at: new Date().toISOString(),
        conversation_count: memory.conversation_count,
        model_used: selectedModel.id
      };
      
      // 如果已有摘要，进行合并
      if (memory.summary) {
        const mergedSummary = await this.mergeSummaries(memory.summary, newSummary);
        memory.summary = mergedSummary;
      } else {
        memory.summary = newSummary;
      }
      
      // 保存摘要到数据库
      await this.saveSummaryToDatabase(memory.agent_id, memory.summary);
      
      console.log(`✅ 对话摘要生成成功`);
      return memory.summary;
      
    } catch (error) {
      console.error(`❌ 生成摘要失败:`, error);
      return null;
    }
  }

  /**
   * 提取和更新实体
   */
  async extractAndUpdateEntities(memory, memoryEntry) {
    console.log(`🏷️ 提取实体: Agent ${memory.agent_id}`);
    
    try {
      const text = `${memoryEntry.user_message} ${memoryEntry.agent_response}`;
      
      const entityPrompt = `
请从以下文本中提取重要的实体信息，包括：
1. 人物姓名
2. 组织机构
3. 产品名称
4. 技术概念
5. 时间日期
6. 地点位置
7. 数值数据

文本内容：
${text}

请以JSON格式返回提取的实体，格式如下：
{
  "persons": ["姓名1", "姓名2"],
  "organizations": ["组织1", "组织2"],
  "products": ["产品1", "产品2"],
  "concepts": ["概念1", "概念2"],
  "dates": ["日期1", "日期2"],
  "locations": ["地点1", "地点2"],
  "numbers": ["数值1", "数值2"]
}
`;

      // 选择适合的模型进行实体提取
      const selectedModel = await this.smartModelSelector.selectOptimalModel('analysis', {
        highPerformance: false
      });
      
      const response = await this.aiModelCaller({
        model: selectedModel.id,
        systemPrompt: '你是一个专业的实体提取助手，擅长从文本中识别和提取关键实体。',
        temperature: 0.1
      }, [], entityPrompt);
      
      // 解析提取的实体
      let extractedEntities;
      try {
        extractedEntities = JSON.parse(response.content);
      } catch (parseError) {
        console.warn('实体提取结果解析失败，使用简化提取');
        extractedEntities = this.simpleEntityExtraction(text);
      }
      
      // 更新实体记忆
      await this.updateEntityMemory(memory.agent_id, extractedEntities, memoryEntry.timestamp);
      
      console.log(`✅ 实体提取完成: ${Object.keys(extractedEntities).length}类实体`);
      return extractedEntities;
      
    } catch (error) {
      console.error(`❌ 实体提取失败:`, error);
      return {};
    }
  }

  /**
   * 更新实体记忆
   */
  async updateEntityMemory(agentId, extractedEntities, timestamp) {
    try {
      for (const [entityType, entities] of Object.entries(extractedEntities)) {
        for (const entityName of entities) {
          if (!entityName || entityName.trim() === '') continue;
          
          const entityId = uuidv4();
          const entityData = {
            id: entityId,
            agent_id: agentId,
            entity_name: entityName.trim(),
            entity_type: entityType,
            description: `${entityType}实体: ${entityName}`,
            attributes: {},
            last_mentioned: timestamp,
            mention_count: 1,
            created_at: timestamp,
            updated_at: timestamp
          };
          
          // 检查实体是否已存在
          const existingEntity = await this.findExistingEntity(agentId, entityName, entityType);
          if (existingEntity) {
            // 更新现有实体
            existingEntity.mention_count++;
            existingEntity.last_mentioned = timestamp;
            existingEntity.updated_at = timestamp;
            await this.updateEntityInDatabase(existingEntity);
          } else {
            // 创建新实体
            await this.saveEntityToDatabase(entityData);
          }
        }
      }
    } catch (error) {
      console.error('更新实体记忆失败:', error);
    }
  }

  /**
   * 查找相关实体
   */
  async findRelevantEntities(agentId, query, maxResults = 5) {
    try {
      // 从数据库查询相关实体
      const entities = await this.dbAdapter.query(`
        SELECT * FROM langchain_entities 
        WHERE agent_id = ? AND (
          entity_name LIKE ? OR 
          description LIKE ? OR
          entity_type IN (
            SELECT entity_type FROM langchain_entities 
            WHERE agent_id = ? AND entity_name LIKE ?
          )
        )
        ORDER BY mention_count DESC, last_mentioned DESC
        LIMIT ?
      `, [
        agentId, 
        `%${query}%`, 
        `%${query}%`, 
        agentId, 
        `%${query}%`, 
        maxResults
      ]);
      
      return entities.map(entity => ({
        name: entity.entity_name,
        type: entity.entity_type,
        description: entity.description,
        mention_count: entity.mention_count,
        last_mentioned: entity.last_mentioned
      }));
      
    } catch (error) {
      console.warn('查找相关实体失败:', error.message);
      return [];
    }
  }

  /**
   * 获取Agent的记忆
   */
  async getMemoryForAgent(agentId) {
    // 先从缓存获取
    if (this.memoryCache.has(agentId)) {
      return this.memoryCache.get(agentId);
    }
    
    // 从数据库加载
    try {
      const memories = await this.dbAdapter.query(
        'SELECT * FROM langchain_memories WHERE agent_id = ? ORDER BY created_at DESC LIMIT 10',
        [agentId]
      );
      
      if (memories.length > 0) {
        // 重构记忆对象
        const memory = {
          id: uuidv4(),
          agent_id: agentId,
          config: { types: ['buffer', 'summary', 'entity'], maxBufferSize: 10, summaryThreshold: 20 },
          buffer: memories.map(m => ({
            id: m.id,
            user_message: JSON.parse(m.content || '{}').user_message || '',
            agent_response: JSON.parse(m.content || '{}').agent_response || '',
            timestamp: m.created_at,
            importance: 0.5
          })),
          summary: null,
          entities: new Map(),
          conversation_count: memories.length,
          created_at: memories[memories.length - 1].created_at,
          updated_at: memories[0].created_at
        };
        
        this.memoryCache.set(agentId, memory);
        return memory;
      }
    } catch (error) {
      console.warn('从数据库加载记忆失败:', error.message);
    }
    
    return null;
  }

  /**
   * 计算记忆重要性
   */
  calculateImportance(message, response) {
    let importance = 0.5; // 基础重要性
    
    // 关键词权重
    const importantKeywords = ['重要', '关键', '问题', '错误', '成功', '失败', '决定', '计划'];
    const messageText = `${message} ${response}`.toLowerCase();
    
    importantKeywords.forEach(keyword => {
      if (messageText.includes(keyword)) {
        importance += 0.1;
      }
    });
    
    // 长度权重
    if (message.length > 100 || response.length > 200) {
      importance += 0.1;
    }
    
    // 问号权重（问题通常更重要）
    if (message.includes('?') || message.includes('？')) {
      importance += 0.1;
    }
    
    return Math.min(1.0, importance);
  }

  /**
   * 简化的实体提取
   */
  simpleEntityExtraction(text) {
    const entities = {
      persons: [],
      organizations: [],
      products: [],
      concepts: [],
      dates: [],
      locations: [],
      numbers: []
    };
    
    // 简单的正则表达式提取
    const numberRegex = /\d+(?:\.\d+)?/g;
    const numbers = text.match(numberRegex) || [];
    entities.numbers = numbers.slice(0, 5); // 最多5个数字
    
    // 提取可能的产品名称（大写字母开头的词）
    const productRegex = /[A-Z][a-zA-Z0-9]+/g;
    const products = text.match(productRegex) || [];
    entities.products = products.slice(0, 3); // 最多3个产品
    
    return entities;
  }

  /**
   * 合并摘要
   */
  async mergeSummaries(oldSummary, newSummary) {
    const mergePrompt = `
请将以下两个对话摘要合并为一个更完整的摘要：

旧摘要：
${oldSummary.content}

新摘要：
${newSummary.content}

请生成一个合并后的摘要，保留所有重要信息，去除重复内容。
`;

    try {
      const selectedModel = await this.smartModelSelector.selectOptimalModel('summarization');
      const response = await this.aiModelCaller({
        model: selectedModel.id,
        systemPrompt: '你是一个专业的摘要合并助手。',
        temperature: 0.3
      }, [], mergePrompt);
      
      return {
        content: response.content,
        generated_at: new Date().toISOString(),
        conversation_count: newSummary.conversation_count,
        model_used: selectedModel.id,
        merged_from: [oldSummary.generated_at, newSummary.generated_at]
      };
    } catch (error) {
      console.error('合并摘要失败:', error);
      return newSummary; // 返回新摘要作为备选
    }
  }

  // 数据库操作方法
  async saveMemoryToDatabase(memory) {
    try {
      await this.dbAdapter.query(`
        INSERT INTO langchain_memories (
          id, agent_id, conversation_id, memory_type, content, metadata, created_at, updated_at
        ) VALUES (?, ?, ?, ?, ?, ?, ?, ?)
      `, [
        memory.id,
        memory.agent_id,
        'system',
        'config',
        JSON.stringify(memory.config),
        JSON.stringify({ conversation_count: memory.conversation_count }),
        memory.created_at,
        memory.updated_at
      ]);
    } catch (error) {
      console.warn('保存记忆配置到数据库失败:', error.message);
    }
  }

  async saveConversationMemory(memoryEntry, agentId) {
    try {
      await this.dbAdapter.query(`
        INSERT INTO langchain_memories (
          id, agent_id, conversation_id, memory_type, content, metadata, created_at, updated_at
        ) VALUES (?, ?, ?, ?, ?, ?, ?, ?)
      `, [
        memoryEntry.id,
        agentId,
        memoryEntry.conversation_id,
        'buffer',
        JSON.stringify({
          user_message: memoryEntry.user_message,
          agent_response: memoryEntry.agent_response
        }),
        JSON.stringify({ importance: memoryEntry.importance }),
        memoryEntry.timestamp,
        memoryEntry.timestamp
      ]);
    } catch (error) {
      console.warn('保存对话记忆到数据库失败:', error.message);
    }
  }

  async saveSummaryToDatabase(agentId, summary) {
    try {
      await this.dbAdapter.query(`
        INSERT OR REPLACE INTO langchain_memories (
          id, agent_id, conversation_id, memory_type, content, metadata, created_at, updated_at
        ) VALUES (?, ?, ?, ?, ?, ?, ?, ?)
      `, [
        uuidv4(),
        agentId,
        'system',
        'summary',
        summary.content,
        JSON.stringify({
          conversation_count: summary.conversation_count,
          model_used: summary.model_used
        }),
        summary.generated_at,
        summary.generated_at
      ]);
    } catch (error) {
      console.warn('保存摘要到数据库失败:', error.message);
    }
  }

  async saveEntityToDatabase(entityData) {
    try {
      await this.dbAdapter.query(`
        INSERT INTO langchain_entities (
          id, agent_id, entity_name, entity_type, description, attributes,
          last_mentioned, mention_count, created_at, updated_at
        ) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
      `, [
        entityData.id,
        entityData.agent_id,
        entityData.entity_name,
        entityData.entity_type,
        entityData.description,
        JSON.stringify(entityData.attributes),
        entityData.last_mentioned,
        entityData.mention_count,
        entityData.created_at,
        entityData.updated_at
      ]);
    } catch (error) {
      console.warn('保存实体到数据库失败:', error.message);
    }
  }

  async findExistingEntity(agentId, entityName, entityType) {
    try {
      const [entity] = await this.dbAdapter.query(
        'SELECT * FROM langchain_entities WHERE agent_id = ? AND entity_name = ? AND entity_type = ?',
        [agentId, entityName, entityType]
      );
      return entity || null;
    } catch (error) {
      console.warn('查找现有实体失败:', error.message);
      return null;
    }
  }

  async updateEntityInDatabase(entity) {
    try {
      await this.dbAdapter.query(`
        UPDATE langchain_entities 
        SET mention_count = ?, last_mentioned = ?, updated_at = ?
        WHERE id = ?
      `, [
        entity.mention_count,
        entity.last_mentioned,
        entity.updated_at,
        entity.id
      ]);
    } catch (error) {
      console.warn('更新实体失败:', error.message);
    }
  }

  /**
   * 清理过期记忆
   */
  async cleanupExpiredMemory(agentId, retentionDays = 30) {
    try {
      const cutoffDate = new Date();
      cutoffDate.setDate(cutoffDate.getDate() - retentionDays);
      
      await this.dbAdapter.query(`
        DELETE FROM langchain_memories 
        WHERE agent_id = ? AND memory_type = 'buffer' AND created_at < ?
      `, [agentId, cutoffDate.toISOString()]);
      
      console.log(`✅ Agent ${agentId} 过期记忆清理完成`);
    } catch (error) {
      console.warn('清理过期记忆失败:', error.message);
    }
  }

  /**
   * 获取记忆统计
   */
  async getMemoryStats(agentId) {
    try {
      const [bufferCount] = await this.dbAdapter.query(
        'SELECT COUNT(*) as count FROM langchain_memories WHERE agent_id = ? AND memory_type = "buffer"',
        [agentId]
      );
      
      const [entityCount] = await this.dbAdapter.query(
        'SELECT COUNT(*) as count FROM langchain_entities WHERE agent_id = ?',
        [agentId]
      );
      
      const [summaryCount] = await this.dbAdapter.query(
        'SELECT COUNT(*) as count FROM langchain_memories WHERE agent_id = ? AND memory_type = "summary"',
        [agentId]
      );
      
      return {
        buffer_memories: bufferCount?.count || 0,
        entities: entityCount?.count || 0,
        summaries: summaryCount?.count || 0,
        cache_status: this.memoryCache.has(agentId) ? 'cached' : 'not_cached'
      };
    } catch (error) {
      console.warn('获取记忆统计失败:', error.message);
      return { buffer_memories: 0, entities: 0, summaries: 0, cache_status: 'error' };
    }
  }
}

module.exports = LangChainMemoryManager;
