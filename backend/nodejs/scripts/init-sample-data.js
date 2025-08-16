/**
 * 初始化示例数据脚本
 * 为空的数据表创建真实的示例数据，展示系统的完整功能
 */

const sqlite3 = require('sqlite3').verbose();
const path = require('path');

// 数据库路径
const dbPath = path.join(__dirname, '../database/chat_history.db');

// 示例数据
const sampleData = {
  // 工作流示例数据
  workflows: [
    {
      id: 'wf_001',
      user_id: '1',
      name: '客户服务自动化流程',
      description: '自动处理客户咨询、分类问题并分配给相应的客服人员',
      category: 'customer-service',
      definition: JSON.stringify({
        nodes: [
          { id: 'start', type: 'start', label: '开始' },
          { id: 'classify', type: 'ai', label: '问题分类', model: 'gpt-4o' },
          { id: 'route', type: 'condition', label: '路由分配' },
          { id: 'response', type: 'ai', label: '自动回复', model: 'deepseek-chat' },
          { id: 'end', type: 'end', label: '结束' }
        ],
        edges: [
          { source: 'start', target: 'classify' },
          { source: 'classify', target: 'route' },
          { source: 'route', target: 'response' },
          { source: 'response', target: 'end' }
        ]
      }),
      config: JSON.stringify({
        timeout: 300,
        retry_count: 3,
        auto_execute: true
      }),
      status: 'published',
      version: 1,
      created_at: new Date('2024-01-15').toISOString(),
      updated_at: new Date('2024-01-20').toISOString()
    },
    {
      id: 'wf_002',
      user_id: '1',
      name: '质量检测报告生成',
      description: '基于检测数据自动生成质量分析报告',
      category: 'quality-control',
      definition: JSON.stringify({
        nodes: [
          { id: 'start', type: 'start', label: '开始' },
          { id: 'collect', type: 'data', label: '数据收集' },
          { id: 'analyze', type: 'ai', label: '数据分析', model: 'claude-3.7-sonnet' },
          { id: 'generate', type: 'ai', label: '报告生成', model: 'gpt-4o' },
          { id: 'review', type: 'human', label: '人工审核' },
          { id: 'end', type: 'end', label: '结束' }
        ],
        edges: [
          { source: 'start', target: 'collect' },
          { source: 'collect', target: 'analyze' },
          { source: 'analyze', target: 'generate' },
          { source: 'generate', target: 'review' },
          { source: 'review', target: 'end' }
        ]
      }),
      config: JSON.stringify({
        timeout: 600,
        retry_count: 2,
        auto_execute: false
      }),
      status: 'published',
      version: 1,
      created_at: new Date('2024-01-18').toISOString(),
      updated_at: new Date('2024-01-22').toISOString()
    },
    {
      id: 'wf_003',
      user_id: '1',
      name: '多Agent协作任务',
      description: '多个AI Agent协作完成复杂任务',
      category: 'collaboration',
      definition: JSON.stringify({
        nodes: [
          { id: 'start', type: 'start', label: '开始' },
          { id: 'planner', type: 'agent', label: '任务规划Agent', agent_id: 1 },
          { id: 'executor1', type: 'agent', label: '执行Agent1', agent_id: 2 },
          { id: 'executor2', type: 'agent', label: '执行Agent2', agent_id: 3 },
          { id: 'reviewer', type: 'agent', label: '审核Agent', agent_id: 4 },
          { id: 'end', type: 'end', label: '结束' }
        ],
        edges: [
          { source: 'start', target: 'planner' },
          { source: 'planner', target: 'executor1' },
          { source: 'planner', target: 'executor2' },
          { source: 'executor1', target: 'reviewer' },
          { source: 'executor2', target: 'reviewer' },
          { source: 'reviewer', target: 'end' }
        ]
      }),
      config: JSON.stringify({
        timeout: 1200,
        retry_count: 1,
        auto_execute: false,
        parallel_execution: true
      }),
      status: 'draft',
      version: 1,
      created_at: new Date('2024-01-20').toISOString(),
      updated_at: new Date('2024-01-21').toISOString()
    }
  ],

  // 插件示例数据
  plugins: [
    {
      id: 'plugin_001',
      user_id: '1',
      name: '数据分析插件',
      description: '提供数据统计、图表生成和趋势分析功能',
      category: 'data-analysis',
      type: 'code',
      config: JSON.stringify({
        input_schema: {
          type: 'object',
          properties: {
            data: { type: 'array', description: '待分析的数据' },
            chart_type: { type: 'string', enum: ['line', 'bar', 'pie'], description: '图表类型' }
          }
        },
        output_schema: {
          type: 'object',
          properties: {
            chart_url: { type: 'string', description: '生成的图表URL' },
            summary: { type: 'string', description: '数据分析摘要' }
          }
        },
        author: 'QMS Team',
        downloads: 156,
        rating: 4.8
      }),
      code: `
function analyze(input) {
  const { data, chart_type } = input;
  // 数据分析逻辑
  const summary = generateSummary(data);
  const chart_url = generateChart(data, chart_type);
  return { chart_url, summary };
}
      `,
      status: 'active',
      version: '1.0.0',
      created_at: new Date('2024-01-10').toISOString(),
      updated_at: new Date('2024-01-15').toISOString()
    },
    {
      id: 'plugin_002',
      user_id: '1',
      name: '文档生成器',
      description: '自动生成各种格式的文档和报告',
      category: 'document',
      type: 'code',
      config: JSON.stringify({
        input_schema: {
          type: 'object',
          properties: {
            template: { type: 'string', description: '文档模板' },
            data: { type: 'object', description: '填充数据' },
            format: { type: 'string', enum: ['pdf', 'docx', 'html'], description: '输出格式' }
          }
        },
        output_schema: {
          type: 'object',
          properties: {
            document_url: { type: 'string', description: '生成的文档URL' },
            file_size: { type: 'number', description: '文件大小(字节)' }
          }
        },
        author: 'QMS Team',
        downloads: 89,
        rating: 4.6
      }),
      code: `
function generateDocument(input) {
  const { template, data, format } = input;
  // 文档生成逻辑
  const document_url = createDocument(template, data, format);
  const file_size = getFileSize(document_url);
  return { document_url, file_size };
}
      `,
      status: 'active',
      version: '2.1.0',
      created_at: new Date('2024-01-12').toISOString(),
      updated_at: new Date('2024-01-18').toISOString()
    },
    {
      id: 'plugin_003',
      user_id: '1',
      name: '质量检测工具',
      description: '手机质量检测和缺陷识别工具',
      category: 'quality-control',
      type: 'code',
      config: JSON.stringify({
        input_schema: {
          type: 'object',
          properties: {
            image_url: { type: 'string', description: '产品图片URL' },
            detection_type: { type: 'string', enum: ['surface', 'function', 'assembly'], description: '检测类型' }
          }
        },
        output_schema: {
          type: 'object',
          properties: {
            defects: { type: 'array', description: '检测到的缺陷列表' },
            quality_score: { type: 'number', description: '质量评分(0-100)' },
            recommendations: { type: 'array', description: '改进建议' }
          }
        },
        author: 'QMS Team',
        downloads: 234,
        rating: 4.9
      }),
      code: `
function detectQuality(input) {
  const { image_url, detection_type } = input;
  // 质量检测逻辑
  const defects = analyzeImage(image_url, detection_type);
  const quality_score = calculateScore(defects);
  const recommendations = generateRecommendations(defects);
  return { defects, quality_score, recommendations };
}
      `,
      status: 'active',
      version: '1.2.0',
      created_at: new Date('2024-01-08').toISOString(),
      updated_at: new Date('2024-01-20').toISOString()
    }
  ],

  // 项目示例数据
  projects: [
    {
      id: 'proj_001',
      user_id: '1',
      name: '智能客服系统',
      description: '基于AI的智能客服解决方案，包含多个Agent和工作流',
      type: 'agent',
      resource_id: '1',
      status: 'active',
      config: JSON.stringify({
        models: ['gpt-4o', 'deepseek-chat'],
        max_tokens: 4000,
        temperature: 0.7,
        agents: ['1', '2'],
        workflows: ['wf_001']
      }),
      created_at: new Date('2024-01-15').toISOString(),
      updated_at: new Date('2024-01-20').toISOString()
    },
    {
      id: 'proj_002',
      user_id: '1',
      name: '质量管理工作流',
      description: '手机生产质量管理的完整工作流程',
      type: 'workflow',
      resource_id: 'wf_002',
      status: 'active',
      config: JSON.stringify({
        auto_execute: true,
        notification: true,
        retry_count: 3,
        workflows: ['wf_002'],
        plugins: ['plugin_003']
      }),
      created_at: new Date('2024-01-18').toISOString(),
      updated_at: new Date('2024-01-22').toISOString()
    },
    {
      id: 'proj_003',
      user_id: '1',
      name: '数据分析平台',
      description: '集成多个数据分析插件的综合平台',
      type: 'plugin',
      resource_id: 'plugin_001',
      status: 'draft',
      config: JSON.stringify({
        plugins: ['plugin_001', 'plugin_002', 'plugin_003'],
        auto_update: true,
        cache_enabled: true,
        dashboard_enabled: true
      }),
      created_at: new Date('2024-01-20').toISOString(),
      updated_at: new Date('2024-01-21').toISOString()
    }
  ]
};

// 初始化数据库数据
async function initSampleData() {
  const db = new sqlite3.Database(dbPath);
  
  console.log('开始初始化示例数据...');
  
  try {
    // 插入工作流数据
    console.log('插入工作流数据...');
    for (const workflow of sampleData.workflows) {
      await new Promise((resolve, reject) => {
        db.run(`
          INSERT OR REPLACE INTO workflows
          (id, user_id, name, description, category, definition, config, status, version, created_at, updated_at)
          VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
        `, [
          workflow.id, workflow.user_id, workflow.name, workflow.description,
          workflow.category, workflow.definition, workflow.config, workflow.status,
          workflow.version, workflow.created_at, workflow.updated_at
        ], function(err) {
          if (err) reject(err);
          else resolve();
        });
      });
    }
    
    // 插入插件数据
    console.log('插入插件数据...');
    for (const plugin of sampleData.plugins) {
      await new Promise((resolve, reject) => {
        db.run(`
          INSERT OR REPLACE INTO plugins
          (id, user_id, name, description, category, type, config, code, status, version, created_at, updated_at)
          VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
        `, [
          plugin.id, plugin.user_id, plugin.name, plugin.description,
          plugin.category, plugin.type, plugin.config, plugin.code,
          plugin.status, plugin.version, plugin.created_at, plugin.updated_at
        ], function(err) {
          if (err) reject(err);
          else resolve();
        });
      });
    }
    
    // 插入项目数据
    console.log('插入项目数据...');
    for (const project of sampleData.projects) {
      await new Promise((resolve, reject) => {
        db.run(`
          INSERT OR REPLACE INTO projects
          (id, user_id, name, description, type, resource_id, status, config, created_at, updated_at)
          VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
        `, [
          project.id, project.user_id, project.name, project.description,
          project.type, project.resource_id, project.status,
          project.config, project.created_at, project.updated_at
        ], function(err) {
          if (err) reject(err);
          else resolve();
        });
      });
    }
    
    console.log('示例数据初始化完成！');
    
    // 验证数据
    console.log('\n验证数据...');
    const tables = ['workflows', 'plugins', 'projects'];
    for (const table of tables) {
      await new Promise((resolve) => {
        db.get(`SELECT COUNT(*) as count FROM ${table}`, (err, row) => {
          if (err) {
            console.log(`${table}: 错误 - ${err.message}`);
          } else {
            console.log(`${table}: ${row.count} 条记录`);
          }
          resolve();
        });
      });
    }
    
  } catch (error) {
    console.error('初始化数据失败:', error);
  } finally {
    db.close();
  }
}

// 如果直接运行此脚本
if (require.main === module) {
  initSampleData();
}

module.exports = { initSampleData, sampleData };
