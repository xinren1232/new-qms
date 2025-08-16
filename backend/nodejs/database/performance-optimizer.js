/**
 * QMS-AI 数据库性能优化器
 * 提供索引管理、查询优化、性能监控等功能
 */

const fs = require('fs');
const path = require('path');

class DatabasePerformanceOptimizer {
  constructor(dbAdapter) {
    this.dbAdapter = dbAdapter;
    this.performanceMetrics = {
      queryCount: 0,
      totalQueryTime: 0,
      slowQueries: [],
      indexUsage: {},
      cacheHitRate: 0
    };
    
    this.slowQueryThreshold = 1000; // 1秒
    this.maxSlowQueries = 100; // 保留最近100个慢查询
  }

  /**
   * 创建性能优化索引
   */
  async createOptimizedIndexes() {
    console.log('🔧 开始创建数据库性能索引...');
    
    const indexes = [
      // 对话表索引
      {
        table: 'conversations',
        name: 'idx_conversations_user_created',
        columns: ['user_id', 'created_at'],
        description: '用户对话按时间查询优化'
      },
      {
        table: 'conversations',
        name: 'idx_conversations_updated',
        columns: ['updated_at'],
        description: '对话更新时间索引'
      },
      {
        table: 'conversations',
        name: 'idx_conversations_status',
        columns: ['status'],
        description: '对话状态索引'
      },
      
      // 消息表索引
      {
        table: 'messages',
        name: 'idx_messages_conversation_created',
        columns: ['conversation_id', 'created_at'],
        description: '对话消息按时间查询优化'
      },
      {
        table: 'messages',
        name: 'idx_messages_user_created',
        columns: ['user_id', 'created_at'],
        description: '用户消息按时间查询优化'
      },
      {
        table: 'messages',
        name: 'idx_messages_role',
        columns: ['role'],
        description: '消息角色索引'
      },
      {
        table: 'messages',
        name: 'idx_messages_model',
        columns: ['model_name'],
        description: '模型名称索引'
      },
      
      // 用户表索引
      {
        table: 'users',
        name: 'idx_users_username',
        columns: ['username'],
        description: '用户名唯一索引'
      },
      {
        table: 'users',
        name: 'idx_users_email',
        columns: ['email'],
        description: '邮箱唯一索引'
      },
      {
        table: 'users',
        name: 'idx_users_created',
        columns: ['created_at'],
        description: '用户创建时间索引'
      },
      
      // 系统日志表索引
      {
        table: 'system_logs',
        name: 'idx_logs_level_created',
        columns: ['level', 'created_at'],
        description: '日志级别和时间复合索引'
      },
      {
        table: 'system_logs',
        name: 'idx_logs_source',
        columns: ['source'],
        description: '日志来源索引'
      }
    ];

    let createdCount = 0;
    let skippedCount = 0;

    for (const index of indexes) {
      try {
        // 检查索引是否已存在
        const exists = await this.checkIndexExists(index.table, index.name);
        
        if (exists) {
          console.log(`⏭️ 索引已存在: ${index.name}`);
          skippedCount++;
          continue;
        }

        // 创建索引
        const sql = `CREATE INDEX IF NOT EXISTS ${index.name} ON ${index.table} (${index.columns.join(', ')})`;
        await this.dbAdapter.run(sql);
        
        console.log(`✅ 创建索引: ${index.name} - ${index.description}`);
        createdCount++;
        
        // 记录索引使用情况
        this.performanceMetrics.indexUsage[index.name] = {
          table: index.table,
          columns: index.columns,
          created: new Date().toISOString(),
          usage: 0
        };
        
      } catch (error) {
        console.error(`❌ 创建索引失败 ${index.name}:`, error.message);
      }
    }

    console.log(`📊 索引创建完成: 新建${createdCount}个, 跳过${skippedCount}个`);
    return { created: createdCount, skipped: skippedCount };
  }

  /**
   * 检查索引是否存在
   */
  async checkIndexExists(tableName, indexName) {
    try {
      const sql = `
        SELECT name FROM sqlite_master 
        WHERE type='index' AND name=? AND tbl_name=?
      `;
      const result = await this.dbAdapter.get(sql, [indexName, tableName]);
      return !!result;
    } catch (error) {
      console.error('检查索引存在性失败:', error.message);
      return false;
    }
  }

  /**
   * 分析查询性能
   */
  async analyzeQueryPerformance(sql, params = []) {
    const startTime = Date.now();
    
    try {
      // 执行EXPLAIN QUERY PLAN
      const explainSql = `EXPLAIN QUERY PLAN ${sql}`;
      const queryPlan = await this.dbAdapter.all(explainSql, params);
      
      // 执行实际查询
      const result = await this.dbAdapter.all(sql, params);
      const executionTime = Date.now() - startTime;
      
      // 更新性能指标
      this.updatePerformanceMetrics(sql, executionTime, queryPlan);
      
      return {
        result,
        executionTime,
        queryPlan,
        isSlowQuery: executionTime > this.slowQueryThreshold
      };
      
    } catch (error) {
      const executionTime = Date.now() - startTime;
      this.updatePerformanceMetrics(sql, executionTime, null, error);
      throw error;
    }
  }

  /**
   * 更新性能指标
   */
  updatePerformanceMetrics(sql, executionTime, queryPlan = null, error = null) {
    this.performanceMetrics.queryCount++;
    this.performanceMetrics.totalQueryTime += executionTime;
    
    // 记录慢查询
    if (executionTime > this.slowQueryThreshold) {
      const slowQuery = {
        sql: sql.substring(0, 200) + (sql.length > 200 ? '...' : ''),
        executionTime,
        timestamp: new Date().toISOString(),
        queryPlan,
        error: error?.message
      };
      
      this.performanceMetrics.slowQueries.unshift(slowQuery);
      
      // 保持慢查询列表大小
      if (this.performanceMetrics.slowQueries.length > this.maxSlowQueries) {
        this.performanceMetrics.slowQueries = 
          this.performanceMetrics.slowQueries.slice(0, this.maxSlowQueries);
      }
      
      console.warn(`🐌 慢查询检测: ${executionTime}ms - ${slowQuery.sql}`);
    }
    
    // 分析索引使用情况
    if (queryPlan) {
      queryPlan.forEach(step => {
        if (step.detail && step.detail.includes('USING INDEX')) {
          const indexMatch = step.detail.match(/USING INDEX (\w+)/);
          if (indexMatch && this.performanceMetrics.indexUsage[indexMatch[1]]) {
            this.performanceMetrics.indexUsage[indexMatch[1]].usage++;
          }
        }
      });
    }
  }

  /**
   * 优化常用查询
   */
  getOptimizedQueries() {
    return {
      // 获取用户最近对话（优化版）
      getUserRecentConversations: `
        SELECT c.*, 
               (SELECT COUNT(*) FROM messages m WHERE m.conversation_id = c.id) as message_count,
               (SELECT m.content FROM messages m 
                WHERE m.conversation_id = c.id 
                ORDER BY m.created_at DESC LIMIT 1) as last_message
        FROM conversations c 
        WHERE c.user_id = ? 
        ORDER BY c.updated_at DESC 
        LIMIT ?
      `,
      
      // 获取对话消息（优化版）
      getConversationMessages: `
        SELECT m.*, u.username 
        FROM messages m
        LEFT JOIN users u ON m.user_id = u.id
        WHERE m.conversation_id = ?
        ORDER BY m.created_at ASC
        LIMIT ? OFFSET ?
      `,
      
      // 用户统计（优化版）
      getUserStats: `
        SELECT 
          u.id,
          u.username,
          COUNT(DISTINCT c.id) as conversation_count,
          COUNT(m.id) as message_count,
          MAX(c.updated_at) as last_activity
        FROM users u
        LEFT JOIN conversations c ON u.id = c.user_id
        LEFT JOIN messages m ON u.id = m.user_id
        WHERE u.id = ?
        GROUP BY u.id, u.username
      `,
      
      // 模型使用统计（优化版）
      getModelUsageStats: `
        SELECT 
          model_name,
          COUNT(*) as usage_count,
          AVG(response_time) as avg_response_time,
          MAX(created_at) as last_used
        FROM messages 
        WHERE role = 'assistant' 
          AND model_name IS NOT NULL
          AND created_at >= datetime('now', '-30 days')
        GROUP BY model_name
        ORDER BY usage_count DESC
      `
    };
  }

  /**
   * 数据库清理和维护
   */
  async performMaintenance() {
    console.log('🧹 开始数据库维护...');
    
    const maintenanceTasks = [
      {
        name: '清理过期会话',
        sql: `DELETE FROM conversations WHERE updated_at < datetime('now', '-90 days')`,
        description: '删除90天前的对话'
      },
      {
        name: '清理孤立消息',
        sql: `DELETE FROM messages WHERE conversation_id NOT IN (SELECT id FROM conversations)`,
        description: '删除没有对应对话的消息'
      },
      {
        name: '清理系统日志',
        sql: `DELETE FROM system_logs WHERE created_at < datetime('now', '-30 days')`,
        description: '删除30天前的系统日志'
      },
      {
        name: '更新统计信息',
        sql: `ANALYZE`,
        description: '更新SQLite统计信息'
      },
      {
        name: '整理数据库',
        sql: `VACUUM`,
        description: '整理数据库文件，回收空间'
      }
    ];

    const results = [];
    
    for (const task of maintenanceTasks) {
      try {
        const startTime = Date.now();
        const result = await this.dbAdapter.run(task.sql);
        const duration = Date.now() - startTime;
        
        results.push({
          name: task.name,
          success: true,
          duration,
          changes: result?.changes || 0,
          description: task.description
        });
        
        console.log(`✅ ${task.name}: ${duration}ms, 影响${result?.changes || 0}行`);
        
      } catch (error) {
        results.push({
          name: task.name,
          success: false,
          error: error.message,
          description: task.description
        });
        
        console.error(`❌ ${task.name}失败:`, error.message);
      }
    }

    console.log('🧹 数据库维护完成');
    return results;
  }

  /**
   * 获取数据库统计信息
   */
  async getDatabaseStats() {
    try {
      const stats = {};
      
      // 表大小统计
      const tables = ['conversations', 'messages', 'users', 'system_logs'];
      for (const table of tables) {
        const countResult = await this.dbAdapter.get(`SELECT COUNT(*) as count FROM ${table}`);
        stats[table] = countResult.count;
      }
      
      // 数据库文件大小
      const dbPath = this.dbAdapter.dbPath;
      if (fs.existsSync(dbPath)) {
        const fileStats = fs.statSync(dbPath);
        stats.fileSize = fileStats.size;
        stats.fileSizeMB = Math.round(fileStats.size / 1024 / 1024 * 100) / 100;
      }
      
      // 性能指标
      stats.performance = {
        ...this.performanceMetrics,
        averageQueryTime: this.performanceMetrics.queryCount > 0 ? 
          Math.round(this.performanceMetrics.totalQueryTime / this.performanceMetrics.queryCount) : 0
      };
      
      return stats;
      
    } catch (error) {
      console.error('获取数据库统计失败:', error.message);
      return { error: error.message };
    }
  }

  /**
   * 重置性能指标
   */
  resetPerformanceMetrics() {
    this.performanceMetrics = {
      queryCount: 0,
      totalQueryTime: 0,
      slowQueries: [],
      indexUsage: { ...this.performanceMetrics.indexUsage }, // 保留索引信息，重置使用计数
      cacheHitRate: 0
    };
    
    // 重置索引使用计数
    Object.keys(this.performanceMetrics.indexUsage).forEach(key => {
      this.performanceMetrics.indexUsage[key].usage = 0;
    });
    
    console.log('📊 性能指标已重置');
  }

  /**
   * 导出性能报告
   */
  async exportPerformanceReport() {
    const report = {
      timestamp: new Date().toISOString(),
      databaseStats: await this.getDatabaseStats(),
      performanceMetrics: this.performanceMetrics,
      optimizedQueries: Object.keys(this.getOptimizedQueries()),
      recommendations: this.generateRecommendations()
    };
    
    const reportPath = path.join(process.cwd(), 'logs', `db-performance-${Date.now()}.json`);
    fs.writeFileSync(reportPath, JSON.stringify(report, null, 2));
    
    console.log(`📄 性能报告已导出: ${reportPath}`);
    return reportPath;
  }

  /**
   * 生成优化建议
   */
  generateRecommendations() {
    const recommendations = [];
    
    // 慢查询建议
    if (this.performanceMetrics.slowQueries.length > 10) {
      recommendations.push({
        type: 'slow_queries',
        priority: 'high',
        message: `检测到${this.performanceMetrics.slowQueries.length}个慢查询，建议优化SQL或添加索引`
      });
    }
    
    // 索引使用建议
    const unusedIndexes = Object.entries(this.performanceMetrics.indexUsage)
      .filter(([_, info]) => info.usage === 0)
      .map(([name]) => name);
    
    if (unusedIndexes.length > 0) {
      recommendations.push({
        type: 'unused_indexes',
        priority: 'medium',
        message: `发现${unusedIndexes.length}个未使用的索引，考虑删除: ${unusedIndexes.join(', ')}`
      });
    }
    
    // 平均查询时间建议
    const avgQueryTime = this.performanceMetrics.queryCount > 0 ? 
      this.performanceMetrics.totalQueryTime / this.performanceMetrics.queryCount : 0;
    
    if (avgQueryTime > 100) {
      recommendations.push({
        type: 'query_performance',
        priority: 'medium',
        message: `平均查询时间${Math.round(avgQueryTime)}ms较高，建议优化查询或增加缓存`
      });
    }
    
    return recommendations;
  }
}

module.exports = DatabasePerformanceOptimizer;
