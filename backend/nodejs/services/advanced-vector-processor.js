/**
 * 高性能向量化处理器
 * 支持自适应分块、批量向量化、智能检索
 */

const EventEmitter = require('events');
const logger = require('../utils/logger');

class AdvancedVectorProcessor extends EventEmitter {
  constructor(options = {}) {
    super();
    
    this.options = {
      maxChunkSize: options.maxChunkSize || 1000,
      chunkOverlap: options.chunkOverlap || 200,
      batchSize: options.batchSize || 50,
      maxConcurrentBatches: options.maxConcurrentBatches || 3,
      embeddingModel: options.embeddingModel || 'text-embedding-ada-002',
      ...options
    };
    
    this.chunkingStrategy = new AdaptiveChunking(this.options);
    this.embeddingService = new OptimizedEmbedding(this.options);
    this.vectorStore = new HighPerformanceVectorStore(this.options);
    this.processingQueue = [];
    this.isProcessing = false;
    
    this.initializeProcessor();
  }

  /**
   * 初始化处理器
   */
  async initializeProcessor() {
    try {
      await this.vectorStore.initialize();
      logger.info('🚀 高性能向量处理器初始化完成');
    } catch (error) {
      logger.error('向量处理器初始化失败:', error);
    }
  }

  /**
   * 处理文档
   */
  async processDocument(document, options = {}) {
    const startTime = Date.now();
    const documentId = document.id || `doc_${Date.now()}`;
    
    try {
      logger.info(`📄 开始处理文档: ${document.name || documentId}`);
      
      // 1. 检测文档类型
      const documentType = this.detectDocumentType(document);
      
      // 2. 自适应分块
      const chunks = await this.adaptiveChunking(document, documentType);
      
      // 3. 批量向量化
      const vectors = await this.batchVectorize(chunks);
      
      // 4. 存储向量
      await this.storeVectors(documentId, chunks, vectors);
      
      const processingTime = Date.now() - startTime;
      
      this.emit('document-processed', {
        documentId,
        chunksCount: chunks.length,
        processingTime,
        documentType
      });
      
      logger.info(`✅ 文档处理完成: ${documentId} (${chunks.length}块, ${processingTime}ms)`);
      
      return {
        success: true,
        documentId,
        chunksCount: chunks.length,
        processingTime,
        documentType
      };
    } catch (error) {
      logger.error(`文档处理失败: ${documentId} - ${error.message}`);
      throw error;
    }
  }

  /**
   * 检测文档类型
   */
  detectDocumentType(document) {
    const content = document.content || '';
    const name = document.name || '';
    
    // 根据文件扩展名
    if (name.endsWith('.pdf')) return 'pdf';
    if (name.endsWith('.docx') || name.endsWith('.doc')) return 'word';
    if (name.endsWith('.md')) return 'markdown';
    if (name.endsWith('.html')) return 'html';
    if (name.endsWith('.json')) return 'json';
    if (name.endsWith('.csv')) return 'csv';
    
    // 根据内容特征
    if (content.includes('```') && content.includes('#')) return 'markdown';
    if (content.includes('<html>') || content.includes('<!DOCTYPE')) return 'html';
    if (content.includes('{') && content.includes('}')) return 'json';
    if (content.includes(',") && content.split('\n').length > 1) return 'csv';
    
    return 'text';
  }

  /**
   * 自适应分块
   */
  async adaptiveChunking(document, documentType) {
    const strategy = this.chunkingStrategy.getStrategy(documentType);
    
    const chunks = await strategy.chunk(document.content, {
      maxChunkSize: this.options.maxChunkSize,
      overlap: this.options.chunkOverlap,
      preserveStructure: true,
      documentType
    });
    
    // 为每个块添加元数据
    return chunks.map((chunk, index) => ({
      id: `${document.id || 'doc'}_chunk_${index}`,
      content: chunk.content,
      metadata: {
        documentId: document.id,
        documentName: document.name,
        documentType,
        chunkIndex: index,
        startPosition: chunk.startPosition,
        endPosition: chunk.endPosition,
        ...chunk.metadata
      }
    }));
  }

  /**
   * 批量向量化
   */
  async batchVectorize(chunks) {
    const batches = this.createBatches(chunks, this.options.batchSize);
    const results = [];
    
    logger.info(`🔄 开始批量向量化: ${chunks.length}块 → ${batches.length}批次`);
    
    // 并行处理批次
    const batchPromises = batches.map(async (batch, batchIndex) => {
      try {
        const batchTexts = batch.map(chunk => chunk.content);
        const batchVectors = await this.embeddingService.embed(batchTexts);
        
        return batch.map((chunk, index) => ({
          chunkId: chunk.id,
          vector: batchVectors[index],
          metadata: chunk.metadata
        }));
      } catch (error) {
        logger.error(`批次 ${batchIndex} 向量化失败:`, error);
        throw error;
      }
    });
    
    // 限制并发数
    const concurrentBatches = [];
    for (let i = 0; i < batchPromises.length; i += this.options.maxConcurrentBatches) {
      const batch = batchPromises.slice(i, i + this.options.maxConcurrentBatches);
      const batchResults = await Promise.all(batch);
      concurrentBatches.push(...batchResults);
    }
    
    return concurrentBatches.flat();
  }

  /**
   * 创建批次
   */
  createBatches(items, batchSize) {
    const batches = [];
    for (let i = 0; i < items.length; i += batchSize) {
      batches.push(items.slice(i, i + batchSize));
    }
    return batches;
  }

  /**
   * 存储向量
   */
  async storeVectors(documentId, chunks, vectors) {
    await this.vectorStore.upsert(documentId, vectors);
  }

  /**
   * 智能检索
   */
  async intelligentSearch(query, options = {}) {
    const startTime = Date.now();
    
    try {
      // 1. 查询向量化
      const queryVector = await this.embeddingService.embed([query]);
      
      // 2. 混合检索
      const hybridResults = await this.hybridSearch(queryVector[0], query, options);
      
      // 3. 结果排序
      const rankedResults = this.rankResults(hybridResults, query);
      
      const searchTime = Date.now() - startTime;
      
      logger.info(`🔍 智能检索完成: ${rankedResults.length}个结果 (${searchTime}ms)`);
      
      return {
        results: rankedResults,
        searchTime,
        query
      };
    } catch (error) {
      logger.error('智能检索失败:', error);
      throw error;
    }
  }

  /**
   * 混合检索（向量检索 + 关键词检索）
   */
  async hybridSearch(queryVector, queryText, options = {}) {
    const {
      topK = 20,
      threshold = 0.7,
      hybridWeight = 0.7 // 向量检索权重
    } = options;
    
    // 向量检索
    const vectorResults = await this.vectorStore.search(queryVector, {
      topK: topK * 2, // 获取更多候选
      threshold: threshold * 0.8
    });
    
    // 关键词检索
    const keywordResults = await this.keywordSearch(queryText, {
      topK: topK * 2
    });
    
    // 合并和重新评分
    const combinedResults = this.combineSearchResults(
      vectorResults,
      keywordResults,
      hybridWeight
    );
    
    return combinedResults.slice(0, topK);
  }

  /**
   * 关键词检索
   */
  async keywordSearch(query, options = {}) {
    // 简单的关键词匹配实现
    // 在实际应用中可以使用更复杂的全文检索引擎
    const keywords = query.toLowerCase().split(/\s+/);
    const results = await this.vectorStore.keywordSearch(keywords, options);
    
    return results;
  }

  /**
   * 合并搜索结果
   */
  combineSearchResults(vectorResults, keywordResults, vectorWeight) {
    const resultMap = new Map();
    
    // 处理向量检索结果
    vectorResults.forEach((result, index) => {
      const score = result.score * vectorWeight;
      resultMap.set(result.id, {
        ...result,
        vectorScore: result.score,
        keywordScore: 0,
        combinedScore: score,
        vectorRank: index + 1
      });
    });
    
    // 处理关键词检索结果
    keywordResults.forEach((result, index) => {
      const keywordScore = result.score * (1 - vectorWeight);
      
      if (resultMap.has(result.id)) {
        const existing = resultMap.get(result.id);
        existing.keywordScore = result.score;
        existing.combinedScore += keywordScore;
        existing.keywordRank = index + 1;
      } else {
        resultMap.set(result.id, {
          ...result,
          vectorScore: 0,
          keywordScore: result.score,
          combinedScore: keywordScore,
          keywordRank: index + 1
        });
      }
    });
    
    // 按组合分数排序
    return Array.from(resultMap.values())
      .sort((a, b) => b.combinedScore - a.combinedScore);
  }

  /**
   * 结果排序
   */
  rankResults(results, query) {
    // 可以添加更复杂的排序逻辑
    // 例如：考虑文档新鲜度、权威性、用户偏好等
    
    return results.map((result, index) => ({
      ...result,
      rank: index + 1,
      relevanceScore: result.combinedScore
    }));
  }

  /**
   * 获取处理统计
   */
  getProcessingStats() {
    return {
      totalDocuments: this.vectorStore.getDocumentCount(),
      totalChunks: this.vectorStore.getChunkCount(),
      averageProcessingTime: this.vectorStore.getAverageProcessingTime(),
      storageSize: this.vectorStore.getStorageSize()
    };
  }
}

/**
 * 自适应分块策略
 */
class AdaptiveChunking {
  constructor(options = {}) {
    this.options = options;
  }

  getStrategy(documentType) {
    switch (documentType) {
      case 'markdown':
        return new MarkdownChunking(this.options);
      case 'html':
        return new HTMLChunking(this.options);
      case 'json':
        return new JSONChunking(this.options);
      case 'csv':
        return new CSVChunking(this.options);
      default:
        return new TextChunking(this.options);
    }
  }
}

/**
 * 文本分块策略
 */
class TextChunking {
  constructor(options = {}) {
    this.options = options;
  }

  async chunk(content, options = {}) {
    const { maxChunkSize, overlap } = options;
    const chunks = [];
    
    // 按段落分割
    const paragraphs = content.split(/\n\s*\n/);
    let currentChunk = '';
    let startPosition = 0;
    
    for (const paragraph of paragraphs) {
      if (currentChunk.length + paragraph.length <= maxChunkSize) {
        currentChunk += (currentChunk ? '\n\n' : '') + paragraph;
      } else {
        if (currentChunk) {
          chunks.push({
            content: currentChunk,
            startPosition,
            endPosition: startPosition + currentChunk.length,
            metadata: { type: 'paragraph' }
          });
          
          // 处理重叠
          if (overlap > 0) {
            const overlapText = currentChunk.slice(-overlap);
            currentChunk = overlapText + '\n\n' + paragraph;
            startPosition += currentChunk.length - overlap;
          } else {
            currentChunk = paragraph;
            startPosition += currentChunk.length;
          }
        } else {
          currentChunk = paragraph;
        }
      }
    }
    
    if (currentChunk) {
      chunks.push({
        content: currentChunk,
        startPosition,
        endPosition: startPosition + currentChunk.length,
        metadata: { type: 'paragraph' }
      });
    }
    
    return chunks;
  }
}

/**
 * Markdown分块策略
 */
class MarkdownChunking extends TextChunking {
  async chunk(content, options = {}) {
    // 按标题层级分块
    const sections = this.splitByHeaders(content);
    const chunks = [];
    
    for (const section of sections) {
      if (section.content.length <= options.maxChunkSize) {
        chunks.push({
          content: section.content,
          startPosition: section.startPosition,
          endPosition: section.endPosition,
          metadata: {
            type: 'section',
            level: section.level,
            title: section.title
          }
        });
      } else {
        // 大段落继续分割
        const subChunks = await super.chunk(section.content, options);
        chunks.push(...subChunks.map(chunk => ({
          ...chunk,
          metadata: {
            ...chunk.metadata,
            parentTitle: section.title,
            level: section.level
          }
        })));
      }
    }
    
    return chunks;
  }

  splitByHeaders(content) {
    const lines = content.split('\n');
    const sections = [];
    let currentSection = null;
    let position = 0;
    
    for (const line of lines) {
      const headerMatch = line.match(/^(#{1,6})\s+(.+)$/);
      
      if (headerMatch) {
        if (currentSection) {
          currentSection.endPosition = position;
          sections.push(currentSection);
        }
        
        currentSection = {
          level: headerMatch[1].length,
          title: headerMatch[2],
          content: line,
          startPosition: position
        };
      } else if (currentSection) {
        currentSection.content += '\n' + line;
      }
      
      position += line.length + 1;
    }
    
    if (currentSection) {
      currentSection.endPosition = position;
      sections.push(currentSection);
    }
    
    return sections;
  }
}

/**
 * 优化的嵌入服务
 */
class OptimizedEmbedding {
  constructor(options = {}) {
    this.options = options;
    this.cache = new Map();
  }

  async embed(texts) {
    // 检查缓存
    const cacheKey = this.getCacheKey(texts);
    if (this.cache.has(cacheKey)) {
      return this.cache.get(cacheKey);
    }
    
    // 调用嵌入API（这里需要集成实际的嵌入服务）
    const vectors = await this.callEmbeddingAPI(texts);
    
    // 缓存结果
    this.cache.set(cacheKey, vectors);
    
    return vectors;
  }

  getCacheKey(texts) {
    return texts.join('|').substring(0, 100);
  }

  async callEmbeddingAPI(texts) {
    // 占位符实现，需要集成实际的嵌入服务
    return texts.map(() => Array(1536).fill(0).map(() => Math.random()));
  }
}

/**
 * 高性能向量存储
 */
class HighPerformanceVectorStore {
  constructor(options = {}) {
    this.options = options;
    this.vectors = new Map();
    this.metadata = new Map();
    this.index = null;
  }

  async initialize() {
    // 初始化向量索引
    logger.info('初始化向量存储');
  }

  async upsert(documentId, vectors) {
    vectors.forEach(vector => {
      this.vectors.set(vector.chunkId, vector.vector);
      this.metadata.set(vector.chunkId, vector.metadata);
    });
  }

  async search(queryVector, options = {}) {
    // 简单的余弦相似度搜索
    const results = [];
    
    for (const [chunkId, vector] of this.vectors) {
      const similarity = this.cosineSimilarity(queryVector, vector);
      if (similarity >= options.threshold) {
        results.push({
          id: chunkId,
          score: similarity,
          metadata: this.metadata.get(chunkId)
        });
      }
    }
    
    return results
      .sort((a, b) => b.score - a.score)
      .slice(0, options.topK);
  }

  async keywordSearch(keywords, options = {}) {
    // 简单的关键词匹配
    const results = [];
    
    for (const [chunkId, metadata] of this.metadata) {
      let score = 0;
      const content = metadata.content || '';
      
      keywords.forEach(keyword => {
        const matches = (content.toLowerCase().match(new RegExp(keyword, 'g')) || []).length;
        score += matches;
      });
      
      if (score > 0) {
        results.push({
          id: chunkId,
          score: score / keywords.length,
          metadata
        });
      }
    }
    
    return results
      .sort((a, b) => b.score - a.score)
      .slice(0, options.topK);
  }

  cosineSimilarity(a, b) {
    const dotProduct = a.reduce((sum, val, i) => sum + val * b[i], 0);
    const magnitudeA = Math.sqrt(a.reduce((sum, val) => sum + val * val, 0));
    const magnitudeB = Math.sqrt(b.reduce((sum, val) => sum + val * val, 0));
    return dotProduct / (magnitudeA * magnitudeB);
  }

  getDocumentCount() {
    return new Set(Array.from(this.metadata.values()).map(m => m.documentId)).size;
  }

  getChunkCount() {
    return this.vectors.size;
  }

  getAverageProcessingTime() {
    return 1000; // 占位符
  }

  getStorageSize() {
    return this.vectors.size * 1536 * 4; // 假设每个向量1536维，每个float 4字节
  }
}

// 其他分块策略类的占位符实现
class HTMLChunking extends TextChunking {}
class JSONChunking extends TextChunking {}
class CSVChunking extends TextChunking {}

module.exports = AdvancedVectorProcessor;
