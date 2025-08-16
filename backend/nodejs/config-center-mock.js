const express = require('express');
const cors = require('cors');
const helmet = require('helmet');
const rateLimit = require('express-rate-limit');
const { JWTManager, PasswordManager, InputValidator, SecurityMiddleware } = require('./utils/security');
const app = express();

// 安全中间件配置
app.use(helmet({
  contentSecurityPolicy: {
    directives: {
      defaultSrc: ["'self'"],
      styleSrc: ["'self'", "'unsafe-inline'"],
      scriptSrc: ["'self'"],
      imgSrc: ["'self'", "data:", "https:"]
    }
  }
}));

// API限流
const apiLimiter = rateLimit({
  windowMs: 15 * 60 * 1000, // 15分钟
  max: 100, // 限制每个IP 15分钟内最多100个请求
  message: {
    success: false,
    message: '请求过于频繁，请稍后再试'
  },
  standardHeaders: true,
  legacyHeaders: false
});

app.use('/api/', apiLimiter);

// 基础中间件配置
app.use(cors());
app.use(express.json({ limit: '10mb' }));
app.use(express.urlencoded({ extended: true, limit: '10mb' }));

// 请求日志中间件
app.use((req, res, next) => {
  console.log(`📥 ${new Date().toLocaleTimeString()} ${req.method} ${req.path}`);
  next();
});

// Mock数据存储 - AI辅助系统属性
let attributes = [
  // AI对话记录属性组
  {
    bid: 'attr_001',
    name: '对话ID',
    code: 'conversationId',
    dataType: 'TEXT',
    required: true,
    maxLength: 50,
    description: '唯一标识对话记录',
    groupName: 'AI对话',
    createdTime: '2025-01-30 10:00:00'
  },
  {
    bid: 'attr_002',
    name: '用户问题',
    code: 'userQuestion',
    dataType: 'LONG_TEXT',
    required: true,
    maxLength: 2000,
    description: '用户提出的问题内容',
    groupName: 'AI对话',
    createdTime: '2025-01-30 10:01:00'
  },
  {
    bid: 'attr_003',
    name: 'AI回答',
    code: 'aiResponse',
    dataType: 'LONG_TEXT',
    required: true,
    maxLength: 5000,
    description: 'AI生成的回答内容',
    groupName: 'AI对话',
    createdTime: '2025-01-30 10:02:00'
  },
  {
    bid: 'attr_004',
    name: '对话时间',
    code: 'conversationTime',
    dataType: 'DATETIME',
    required: true,
    defaultValue: 'CURRENT_TIME',
    description: '对话发生的时间',
    groupName: 'AI对话',
    createdTime: '2025-01-30 10:03:00'
  },
  {
    bid: 'attr_005',
    name: '用户ID',
    code: 'userId',
    dataType: 'TEXT',
    required: true,
    maxLength: 50,
    description: '提问用户的唯一标识',
    groupName: 'AI对话',
    createdTime: '2025-01-30 10:04:00'
  },
  {
    bid: 'attr_006',
    name: '对话状态',
    code: 'conversationStatus',
    dataType: 'SELECT',
    required: true,
    options: ['进行中', '已完成', '已中断'],
    defaultValue: '进行中',
    description: '对话的当前状态',
    groupName: 'AI对话',
    createdTime: '2025-01-30 10:05:00'
  },
  {
    bid: 'attr_007',
    name: '满意度评分',
    code: 'satisfactionScore',
    dataType: 'NUMBER',
    required: false,
    minValue: 1,
    maxValue: 5,
    description: '用户对AI回答的满意度评分',
    groupName: 'AI对话',
    createdTime: '2025-01-30 10:06:00'
  },

  // 数据源管理属性组
  {
    bid: 'attr_008',
    name: '数据源名称',
    code: 'dataSourceName',
    dataType: 'TEXT',
    required: true,
    maxLength: 100,
    description: '数据源的显示名称',
    groupName: '数据源管理',
    createdTime: '2025-01-30 10:07:00'
  },
  {
    bid: 'attr_009',
    name: '数据源类型',
    code: 'dataSourceType',
    dataType: 'SELECT',
    required: true,
    options: ['数据库', 'API接口', '文件系统', '云存储', '实时流'],
    defaultValue: '数据库',
    description: '数据源的类型分类',
    groupName: '数据源管理',
    createdTime: '2025-01-30 10:08:00'
  },
  {
    bid: 'attr_010',
    name: '连接地址',
    code: 'connectionUrl',
    dataType: 'TEXT',
    required: true,
    maxLength: 500,
    description: '数据源的连接地址或URL',
    groupName: '数据源管理',
    createdTime: '2025-01-30 10:09:00'
  },
  {
    bid: 'attr_011',
    name: '接入状态',
    code: 'connectionStatus',
    dataType: 'SELECT',
    required: true,
    options: ['已连接', '连接中', '连接失败', '已断开'],
    defaultValue: '连接中',
    description: '数据源的连接状态',
    groupName: '数据源管理',
    createdTime: '2025-01-30 10:10:00'
  },
  {
    bid: 'attr_012',
    name: '数据更新频率',
    code: 'updateFrequency',
    dataType: 'SELECT',
    required: true,
    options: ['实时', '每小时', '每天', '每周', '手动'],
    defaultValue: '每天',
    description: '数据同步的频率设置',
    groupName: '数据源管理',
    createdTime: '2025-01-30 10:11:00'
  },

  // AI规则配置属性组
  {
    bid: 'attr_013',
    name: '规则名称',
    code: 'ruleName',
    dataType: 'TEXT',
    required: true,
    maxLength: 100,
    description: 'AI逻辑规则的名称',
    groupName: 'AI规则配置',
    createdTime: '2025-01-30 10:12:00'
  },
  {
    bid: 'attr_014',
    name: '规则类型',
    code: 'ruleType',
    dataType: 'SELECT',
    required: true,
    options: ['检索规则', '过滤规则', '排序规则', '推荐规则', '安全规则'],
    defaultValue: '检索规则',
    description: 'AI规则的类型分类',
    groupName: 'AI规则配置',
    createdTime: '2025-01-30 10:13:00'
  },
  {
    bid: 'attr_015',
    name: '触发条件',
    code: 'triggerCondition',
    dataType: 'LONG_TEXT',
    required: true,
    maxLength: 2000,
    description: '规则触发的条件表达式',
    groupName: 'AI规则配置',
    createdTime: '2025-01-30 10:14:00'
  },
  {
    bid: 'attr_016',
    name: '执行动作',
    code: 'executeAction',
    dataType: 'LONG_TEXT',
    required: true,
    maxLength: 2000,
    description: '规则触发后执行的动作',
    groupName: 'AI规则配置',
    createdTime: '2025-01-30 10:15:00'
  },
  {
    bid: 'attr_017',
    name: '优先级',
    code: 'priority',
    dataType: 'SELECT',
    required: true,
    options: ['高', '中', '低'],
    defaultValue: '中',
    description: '规则执行的优先级',
    groupName: 'AI规则配置',
    createdTime: '2025-01-30 10:16:00'
  },
  {
    bid: 'attr_018',
    name: '规则状态',
    code: 'ruleStatus',
    dataType: 'SELECT',
    required: true,
    options: ['启用', '禁用', '测试中'],
    defaultValue: '测试中',
    description: '规则的当前状态',
    groupName: 'AI规则配置',
    createdTime: '2025-01-30 10:17:00'
  },

  // 用户管理属性组
  {
    bid: 'attr_019',
    name: '用户名',
    code: 'username',
    dataType: 'TEXT',
    required: true,
    maxLength: 50,
    description: '用户登录名',
    groupName: '用户管理',
    createdTime: '2025-01-30 10:18:00'
  },
  {
    bid: 'attr_020',
    name: '真实姓名',
    code: 'realName',
    dataType: 'TEXT',
    required: true,
    maxLength: 50,
    description: '用户的真实姓名',
    groupName: '用户管理',
    createdTime: '2025-01-30 10:19:00'
  },
  {
    bid: 'attr_021',
    name: '用户角色',
    code: 'userRole',
    dataType: 'SELECT',
    required: true,
    options: ['管理员', '使用者', '访客'],
    defaultValue: '使用者',
    description: '用户在系统中的角色',
    groupName: '用户管理',
    createdTime: '2025-01-30 10:20:00'
  },
  {
    bid: 'attr_022',
    name: '账户状态',
    code: 'accountStatus',
    dataType: 'SELECT',
    required: true,
    options: ['正常', '锁定', '禁用', '待激活'],
    defaultValue: '待激活',
    description: '用户账户的状态',
    groupName: '用户管理',
    createdTime: '2025-01-30 10:21:00'
  }
];

let objects = [
  {
    bid: 'obj_001',
    name: 'AI对话记录',
    code: 'aiConversation',
    description: 'AI问答对话记录管理',
    type: 'BUSINESS',
    attributes: ['attr_001', 'attr_002', 'attr_003', 'attr_004', 'attr_005', 'attr_006', 'attr_007'],
    createdTime: '2025-01-30 10:30:00'
  },
  {
    bid: 'obj_002',
    name: '数据源配置',
    code: 'dataSource',
    description: '数据接入源配置管理',
    type: 'BUSINESS',
    attributes: ['attr_008', 'attr_009', 'attr_010', 'attr_011', 'attr_012'],
    createdTime: '2025-01-30 10:31:00'
  },
  {
    bid: 'obj_003',
    name: 'AI规则配置',
    code: 'aiRule',
    description: 'AI逻辑和检索规则配置',
    type: 'BUSINESS',
    attributes: ['attr_013', 'attr_014', 'attr_015', 'attr_016', 'attr_017', 'attr_018'],
    createdTime: '2025-01-30 10:32:00'
  },
  {
    bid: 'obj_004',
    name: '用户管理',
    code: 'userManagement',
    description: '系统用户和权限管理',
    type: 'BUSINESS',
    attributes: ['attr_019', 'attr_020', 'attr_021', 'attr_022'],
    createdTime: '2025-01-30 10:33:00'
  }
];

let views = [
  // AI对话记录视图
  {
    bid: 'view_001',
    name: 'AI对话列表视图',
    code: 'aiConversation_list',
    objectCode: 'aiConversation',
    type: 'LIST',
    fields: [
      { code: 'conversationId', name: '对话ID', width: '120px', sortable: true },
      { code: 'userId', name: '用户ID', width: '100px', sortable: true },
      { code: 'userQuestion', name: '用户问题', width: '300px', sortable: false },
      { code: 'conversationStatus', name: '对话状态', width: '100px', sortable: true },
      { code: 'satisfactionScore', name: '满意度', width: '80px', sortable: true },
      { code: 'conversationTime', name: '对话时间', width: '150px', sortable: true }
    ],
    createdTime: '2025-01-30 10:40:00'
  },
  {
    bid: 'view_002',
    name: 'AI对话表单视图',
    code: 'aiConversation_form',
    objectCode: 'aiConversation',
    type: 'FORM',
    layout: {
      columns: 2,
      fields: [
        { code: 'conversationId', row: 1, col: 1, span: 1, required: true },
        { code: 'userId', row: 1, col: 2, span: 1, required: true },
        { code: 'conversationStatus', row: 2, col: 1, span: 1, required: true },
        { code: 'satisfactionScore', row: 2, col: 2, span: 1, required: false },
        { code: 'conversationTime', row: 3, col: 1, span: 2, readonly: true },
        { code: 'userQuestion', row: 4, col: 1, span: 2, type: 'textarea' },
        { code: 'aiResponse', row: 5, col: 1, span: 2, type: 'textarea' }
      ]
    },
    createdTime: '2025-01-30 10:41:00'
  },

  // 数据源配置视图
  {
    bid: 'view_003',
    name: '数据源列表视图',
    code: 'dataSource_list',
    objectCode: 'dataSource',
    type: 'LIST',
    fields: [
      { code: 'dataSourceName', name: '数据源名称', width: '200px', sortable: true },
      { code: 'dataSourceType', name: '数据源类型', width: '120px', sortable: true },
      { code: 'connectionStatus', name: '连接状态', width: '100px', sortable: true },
      { code: 'updateFrequency', name: '更新频率', width: '100px', sortable: true },
      { code: 'connectionUrl', name: '连接地址', width: '300px', sortable: false }
    ],
    createdTime: '2025-01-30 10:42:00'
  },
  {
    bid: 'view_004',
    name: '数据源表单视图',
    code: 'dataSource_form',
    objectCode: 'dataSource',
    type: 'FORM',
    layout: {
      columns: 2,
      fields: [
        { code: 'dataSourceName', row: 1, col: 1, span: 1, required: true },
        { code: 'dataSourceType', row: 1, col: 2, span: 1, required: true },
        { code: 'connectionStatus', row: 2, col: 1, span: 1, required: true },
        { code: 'updateFrequency', row: 2, col: 2, span: 1, required: true },
        { code: 'connectionUrl', row: 3, col: 1, span: 2, required: true }
      ]
    },
    createdTime: '2025-01-30 10:43:00'
  },

  // AI规则配置视图
  {
    bid: 'view_005',
    name: 'AI规则列表视图',
    code: 'aiRule_list',
    objectCode: 'aiRule',
    type: 'LIST',
    fields: [
      { code: 'ruleName', name: '规则名称', width: '200px', sortable: true },
      { code: 'ruleType', name: '规则类型', width: '120px', sortable: true },
      { code: 'priority', name: '优先级', width: '80px', sortable: true },
      { code: 'ruleStatus', name: '规则状态', width: '100px', sortable: true },
      { code: 'triggerCondition', name: '触发条件', width: '250px', sortable: false }
    ],
    createdTime: '2025-01-30 10:44:00'
  },
  {
    bid: 'view_006',
    name: 'AI规则表单视图',
    code: 'aiRule_form',
    objectCode: 'aiRule',
    type: 'FORM',
    layout: {
      columns: 2,
      fields: [
        { code: 'ruleName', row: 1, col: 1, span: 1, required: true },
        { code: 'ruleType', row: 1, col: 2, span: 1, required: true },
        { code: 'priority', row: 2, col: 1, span: 1, required: true },
        { code: 'ruleStatus', row: 2, col: 2, span: 1, required: true },
        { code: 'triggerCondition', row: 3, col: 1, span: 2, type: 'textarea' },
        { code: 'executeAction', row: 4, col: 1, span: 2, type: 'textarea' }
      ]
    },
    createdTime: '2025-01-30 10:45:00'
  },

  // 用户管理视图
  {
    bid: 'view_007',
    name: '用户列表视图',
    code: 'userManagement_list',
    objectCode: 'userManagement',
    type: 'LIST',
    fields: [
      { code: 'username', name: '用户名', width: '120px', sortable: true },
      { code: 'realName', name: '真实姓名', width: '120px', sortable: true },
      { code: 'userRole', name: '用户角色', width: '100px', sortable: true },
      { code: 'accountStatus', name: '账户状态', width: '100px', sortable: true }
    ],
    createdTime: '2025-01-30 10:46:00'
  },
  {
    bid: 'view_008',
    name: '用户表单视图',
    code: 'userManagement_form',
    objectCode: 'userManagement',
    type: 'FORM',
    layout: {
      columns: 2,
      fields: [
        { code: 'username', row: 1, col: 1, span: 1, required: true },
        { code: 'realName', row: 1, col: 2, span: 1, required: true },
        { code: 'userRole', row: 2, col: 1, span: 1, required: true },
        { code: 'accountStatus', row: 2, col: 2, span: 1, required: true }
      ]
    },
    createdTime: '2025-01-30 10:47:00'
  }
];

// API路由

// 属性管理API
app.get('/manager/cfg/attribute/page', (req, res) => {
  res.json({
    code: 200,
    message: 'success',
    data: {
      records: attributes,
      total: attributes.length,
      current: 1,
      size: 20
    }
  });
});

app.post('/manager/cfg/attribute', (req, res) => {
  const newAttr = {
    bid: 'attr_' + Date.now(),
    ...req.body,
    createdTime: new Date().toISOString()
  };
  attributes.push(newAttr);
  res.json({
    code: 200,
    message: '属性创建成功',
    data: newAttr
  });
});

// 对象管理API
app.get('/manager/cfg/object/page', (req, res) => {
  res.json({
    code: 200,
    message: 'success',
    data: {
      records: objects,
      total: objects.length,
      current: 1,
      size: 20
    }
  });
});

app.post('/manager/cfg/object', (req, res) => {
  const newObj = {
    bid: 'obj_' + Date.now(),
    ...req.body,
    createdTime: new Date().toISOString()
  };
  objects.push(newObj);
  res.json({
    code: 200,
    message: '对象创建成功',
    data: newObj
  });
});

// 视图管理API
app.get('/manager/cfg/view/page', (req, res) => {
  res.json({
    code: 200,
    message: 'success',
    data: {
      records: views,
      total: views.length,
      current: 1,
      size: 20
    }
  });
});

app.post('/manager/cfg/view', (req, res) => {
  const newView = {
    bid: 'view_' + Date.now(),
    ...req.body,
    createdTime: new Date().toISOString()
  };
  views.push(newView);
  res.json({
    code: 200,
    message: '视图创建成功',
    data: newView
  });
});

// 获取配置信息API (供应用端使用)
app.post('/data-driven/api/config-center/getOptimalMatchView', (req, res) => {
  const { modelCode } = req.body;
  const view = views.find(v => v.objectCode === modelCode);
  res.json({
    code: 200,
    message: 'success',
    data: view || null
  });
});

// 应用端需要的API接口

// 获取菜单应用树
app.get('/menu-app/tree', (req, res) => {
  res.json({
    code: 200,
    message: 'success',
    data: [
      {
        bid: 'ai_conversation',
        name: 'AI对话记录',
        icon: 'chat',
        modelCode: 'aiConversation',
        type: 'application',
        children: []
      },
      {
        bid: 'data_source',
        name: '数据源配置',
        icon: 'database',
        modelCode: 'dataSource',
        type: 'application',
        children: []
      },
      {
        bid: 'ai_rule',
        name: 'AI规则配置',
        icon: 'setting',
        modelCode: 'aiRule',
        type: 'application',
        children: []
      },
      {
        bid: 'user_management',
        name: '用户管理',
        icon: 'user',
        modelCode: 'userManagement',
        type: 'application',
        children: []
      }
    ]
  });
});

// 获取视图配置
app.post('/config-center/getOptimalMatchView', (req, res) => {
  const { modelCode } = req.body;
  const viewsForModel = views.filter(v => v.objectCode === modelCode);

  res.json({
    code: 200,
    message: 'success',
    data: viewsForModel
  });
});

// 获取对象实例列表
app.post('/object-model/:modelCode/page', (req, res) => {
  const { modelCode } = req.params;
  const { pageNum = 1, pageSize = 10 } = req.body;

  // 模拟数据
  const mockData = {
    aiConversation: [
      {
        bid: 'conv_001',
        conversationId: 'CONV_20250130_001',
        userId: 'user_001',
        userQuestion: '如何配置AI规则？',
        aiResponse: '您可以通过AI规则配置模块来设置逻辑规则和检索规则...',
        conversationStatus: '已完成',
        satisfactionScore: 5,
        conversationTime: '2025-01-30 14:30:00'
      }
    ],
    dataSource: [
      {
        bid: 'ds_001',
        dataSourceName: 'MySQL主数据库',
        dataSourceType: '数据库',
        connectionUrl: 'jdbc:mysql://localhost:3306/ai_system',
        connectionStatus: '已连接',
        updateFrequency: '每天'
      }
    ],
    aiRule: [
      {
        bid: 'rule_001',
        ruleName: '智能问答检索规则',
        ruleType: '检索规则',
        priority: '高',
        ruleStatus: '启用',
        triggerCondition: '用户提问包含关键词'
      }
    ],
    userManagement: [
      {
        bid: 'user_001',
        username: 'admin',
        realName: '系统管理员',
        userRole: '管理员',
        accountStatus: '正常'
      }
    ]
  };

  const data = mockData[modelCode] || [];

  res.json({
    code: 200,
    message: 'success',
    data: {
      records: data,
      total: data.length,
      size: pageSize,
      current: pageNum,
      pages: Math.ceil(data.length / pageSize)
    }
  });
});

// 检查用户是否是全局管理员
app.get('/apm/role/isGlobalAdmin', (req, res) => {
  res.json({
    code: 200,
    message: 'success',
    data: true
  });
});

// 获取字典数据
app.post('/dictionary/listDictionaryAndItemByCodesAndEnableFlags', (req, res) => {
  res.json({
    code: 200,
    message: 'success',
    data: {}
  });
});

// 健康检查
app.get('/health', (req, res) => {
  res.json({
    success: true,
    status: 'UP',
    service: 'AI Assistant System Mock API',
    timestamp: new Date().toISOString(),
    endpoints: [
      'GET /health',
      'GET /attributes',
      'GET /objects',
      'GET /views',
      'GET /menu-app/tree',
      'POST /config-center/getOptimalMatchView',
      'POST /object-model/:modelCode/page'
    ]
  });
});

// ==================== 用户认证相关API ====================

// 模拟用户数据库
let users = [
  {
    id: 1,
    username: 'admin',
    password: 'admin123', // 实际应用中应该加密
    email: 'admin@qms-ai.com',
    realName: '系统管理员',
    phone: '13800138000',
    department: '技术部',
    position: '系统管理员',
    roles: ['admin'],
    permissions: ['*'], // 所有权限
    status: 'active',
    createdAt: '2024-01-01T00:00:00Z',
    lastLoginAt: null
  },
  {
    id: 2,
    username: 'developer',
    password: 'dev123',
    email: 'developer@qms-ai.com',
    realName: '开发工程师',
    phone: '13800138001',
    department: '技术部',
    position: '高级开发工程师',
    roles: ['manager'],
    permissions: [
      'ai:conversation:view', 'ai:conversation:analyze', 'ai:conversation:export',
      'ai:datasource:view', 'ai:datasource:add', 'ai:datasource:edit', 'ai:datasource:test',
      'ai:rule:view', 'ai:rule:add', 'ai:rule:edit',
      'user:view'
    ],
    status: 'active',
    createdAt: '2024-01-01T00:00:00Z',
    lastLoginAt: null
  },
  {
    id: 3,
    username: 'tester',
    password: 'test123',
    email: 'tester@qms-ai.com',
    realName: '测试工程师',
    phone: '13800138002',
    department: '质量部',
    position: '测试工程师',
    roles: ['user'],
    permissions: [
      'ai:conversation:view',
      'ai:datasource:view',
      'ai:rule:view'
    ],
    status: 'active',
    createdAt: '2024-01-01T00:00:00Z',
    lastLoginAt: null
  }
];

// 用户会话存储
let userSessions = new Map();

// JWT Token管理 - 使用安全的标准实现
// 注意：generateToken和verifyToken现在由SecurityMiddleware提供

// 认证中间件 - 使用新的安全实现
function authMiddleware(req, res, next) {
  const token = req.headers.authorization;
  if (!token) {
    return res.status(401).json({
      success: false,
      message: '未提供认证令牌'
    });
  }

  const payload = JWTManager.verifyToken(token);
  if (!payload) {
    return res.status(401).json({
      success: false,
      message: '认证令牌无效或已过期'
    });
  }

  const user = users.find(u => u.id === payload.userId);
  if (!user || user.status !== 'active') {
    return res.status(401).json({
      success: false,
      message: '用户不存在或已被禁用'
    });
  }

  req.user = user;
  next();
}

// 用户登录 - 使用安全的认证机制
app.post('/auth/login', async (req, res) => {
  try {
    // 输入验证
    const validation = InputValidator.validateLoginData(req.body);
    if (!validation.isValid) {
      return res.status(400).json({
        success: false,
        message: validation.error
      });
    }

    const { username, password, rememberMe } = validation.sanitized;
    console.log(`🔐 用户登录请求: ${username}`);

    // 查找用户
    const user = users.find(u =>
      u.username === username || u.email === username
    );

    if (!user) {
      return res.status(401).json({
        success: false,
        message: '用户名或密码错误'
      });
    }

    // 验证密码（这里暂时使用明文比较，实际应该使用加密密码）
    // TODO: 将用户密码迁移到加密存储
    const isPasswordValid = user.password === password;

    if (!isPasswordValid) {
      return res.status(401).json({
        success: false,
        message: '用户名或密码错误'
      });
    }

    if (user.status !== 'active') {
      return res.status(401).json({
        success: false,
        message: '账户已被禁用，请联系管理员'
      });
    }

    // 生成安全的JWT Token
    const token = JWTManager.generateToken(user);

    // 更新最后登录时间
    user.lastLoginAt = new Date().toISOString();

    // 存储会话
    userSessions.set(token, {
      userId: user.id,
      loginTime: Date.now(),
      rememberMe
    });

    // 返回用户信息（不包含密码）
    const { password: _, ...userInfo } = user;

    res.json({
      success: true,
      code: 200,
      message: '登录成功',
      data: {
        token,
        user: userInfo,
        permissions: user.permissions,
        roles: user.roles
      }
    });

    console.log(`✅ 用户 ${username} 登录成功`);
  } catch (error) {
    console.error('❌ 登录处理失败:', error.message);
    res.status(500).json({
      success: false,
      message: '登录处理失败，请稍后重试'
    });
  }
});

// 用户注册
app.post('/auth/register', (req, res) => {
  const { username, password, email, realName, phone, department, position } = req.body;

  console.log(`📝 用户注册请求: ${username}`);

  // 检查用户名是否已存在
  if (users.find(u => u.username === username)) {
    return res.status(400).json({
      success: false,
      message: '用户名已存在'
    });
  }

  // 检查邮箱是否已存在
  if (users.find(u => u.email === email)) {
    return res.status(400).json({
      success: false,
      message: '邮箱已被注册'
    });
  }

  // 创建新用户
  const newUser = {
    id: users.length + 1,
    username,
    password, // 实际应用中应该加密
    email,
    realName,
    phone,
    department,
    position,
    roles: ['user'], // 默认为普通用户
    permissions: [
      'ai:conversation:view',
      'ai:datasource:view',
      'ai:rule:view'
    ],
    status: 'active',
    createdAt: new Date().toISOString(),
    lastLoginAt: null
  };

  users.push(newUser);

  // 返回用户信息（不包含密码）
  const { password: _, ...userInfo } = newUser;

  res.json({
    success: true,
    message: '注册成功',
    data: userInfo
  });

  console.log(`✅ 用户 ${username} 注册成功`);
});

// 获取用户信息
app.get('/auth/userinfo', authMiddleware, (req, res) => {
  const { password: _, ...userInfo } = req.user;

  res.json({
    success: true,
    data: userInfo,
    permissions: req.user.permissions,
    roles: req.user.roles
  });
});

// 检查用户名是否可用
app.get('/auth/check-username', (req, res) => {
  const { username } = req.query;
  const exists = users.some(u => u.username === username);

  res.json({
    success: true,
    data: {
      available: !exists,
      message: exists ? '用户名已存在' : '用户名可用'
    }
  });
});

// 检查邮箱是否可用
app.get('/auth/check-email', (req, res) => {
  const { email } = req.query;
  const exists = users.some(u => u.email === email);

  res.json({
    success: true,
    data: {
      available: !exists,
      message: exists ? '邮箱已被注册' : '邮箱可用'
    }
  });
});

// 用户登出
app.post('/auth/logout', authMiddleware, (req, res) => {
  const token = req.headers.authorization?.replace('Bearer ', '');
  if (token) {
    userSessions.delete(token);
  }

  res.json({
    success: true,
    message: '登出成功'
  });

  console.log(`👋 用户 ${req.user.username} 已登出`);
});

// 配置同步API - AI管理配置
app.post('/config-sync/ai-management', (req, res) => {
  console.log('🔄 配置同步请求 - AI管理系统');

  res.json({
    code: 200,
    message: 'success',
    data: {
      fields: [
        {
          name: 'conversationId',
          label: '对话ID',
          type: 'input',
          required: true,
          placeholder: '请输入对话ID'
        },
        {
          name: 'userQuestion',
          label: '用户问题',
          type: 'textarea',
          required: true,
          placeholder: '请输入用户问题'
        },
        {
          name: 'aiResponse',
          label: 'AI回答',
          type: 'textarea',
          required: true,
          placeholder: 'AI生成的回答'
        }
      ],
      permissions: {
        create: true,
        read: true,
        update: true,
        delete: true
      },
      syncTime: new Date().toISOString()
    }
  });
});

// AI对话记录分页查询
app.post('/object-model/aiConversation/page', (req, res) => {
  console.log('📋 AI对话记录分页查询:', req.body);

  const mockData = [
    {
      bid: 'conv_001',
      conversationId: 'CONV_001',
      userQuestion: '如何优化质量管理流程？',
      aiResponse: '质量管理流程优化建议...',
      conversationTime: '2025-01-30 14:30:00'
    }
  ];

  res.json({
    code: 200,
    message: 'success',
    data: {
      records: mockData,
      total: 1,
      size: 10,
      current: 1,
      pages: 1
    }
  });
});

// 添加 /api/health 路由，与 /health 路由功能相同
app.get('/api/health', (req, res) => {
  res.json({
    success: true,
    status: 'ok',
    message: 'QMS配置端Mock服务运行正常',
    timestamp: new Date().toISOString(),
    service: 'config-center-mock',
    version: '1.0.0'
  });
});

// ==================== AI监控相关API ====================

// 模拟监控数据
let monitoringData = {
  chatRecords: [
    {
      id: 1,
      bid: 'chat_001',
      conversationId: 'conv_20250131_001',
      messageId: 'msg_20250131_001',
      userId: 'user001',
      userName: '张三',
      userMessage: '如何提升产品质量？',
      aiResponse: '提升产品质量可以从以下几个方面入手：1. 建立完善的质量管理体系...',
      modelProvider: 'transsion',
      modelName: 'gemini-2.5-pro',
      responseTime: 1250,
      chatStatus: 'SUCCESS',
      createdTime: '2025-01-31 14:30:00'
    },
    {
      id: 2,
      bid: 'chat_002',
      conversationId: 'conv_20250131_002',
      messageId: 'msg_20250131_002',
      userId: 'user002',
      userName: '李四',
      userMessage: '质量管理体系如何建立？',
      aiResponse: '建立质量管理体系需要遵循以下步骤：1. 确定组织的质量方针和目标...',
      modelProvider: 'openai',
      modelName: 'gpt-4o',
      responseTime: 980,
      chatStatus: 'SUCCESS',
      createdTime: '2025-01-31 15:15:00'
    }
  ],
  feedbackRecords: [
    {
      id: 1,
      bid: 'feedback_001',
      messageId: 'msg_20250131_001',
      userId: 'user001',
      feedbackType: 'LIKE',
      feedbackScore: 5,
      feedbackReason: 'HELPFUL',
      feedbackComment: '回答很详细，很有帮助',
      createdTime: '2025-01-31 14:32:00'
    }
  ]
};

// 权限检查中间件（简化版）
function checkAdminPermission(req, res, next) {
  const token = req.headers.authorization?.replace('Bearer ', '');
  if (!token) {
    return res.json({
      success: false,
      message: '权限不足，仅管理员可访问'
    });
  }

  const payload = verifyToken(token);
  if (!payload) {
    return res.json({
      success: false,
      message: '权限不足，仅管理员可访问'
    });
  }

  const user = users.find(u => u.id === payload.userId);
  if (!user || !user.roles.includes('admin')) {
    return res.json({
      success: false,
      message: '权限不足，仅管理员可访问'
    });
  }

  req.user = user;
  next();
}

// 获取AI模型配置
app.get('/api/ai/models', (req, res) => {
  const aiModels = [
    {
      id: 'gpt-4o',
      name: 'GPT-4o',
      provider: 'openai',
      model: 'gpt-4o',
      baseURL: 'https://hk-intra-paas.transsion.com/tranai-proxy/v1',
      apiKey: process.env.INTERNAL_AI_API_KEY,
      contextLength: 128000,
      maxTokens: 4096,
      temperature: 0.7,
      features: {
        streaming: true,
        functionCalling: true,
        vision: true
      },
      tags: ['内部模型', '多模态', '推荐'],
      status: 'active'
    },
    {
      id: 'claude-3-5-sonnet',
      name: 'Claude-3.5-Sonnet',
      provider: 'anthropic',
      model: 'claude-3-5-sonnet-20241022',
      baseURL: 'https://hk-intra-paas.transsion.com/tranai-proxy/v1',
      apiKey: process.env.INTERNAL_AI_API_KEY,
      contextLength: 200000,
      maxTokens: 8192,
      temperature: 0.7,
      features: {
        streaming: true,
        functionCalling: true,
        longContext: true
      },
      tags: ['内部模型', '长上下文', '推荐'],
      status: 'active'
    },
    {
      id: 'deepseek-chat',
      name: 'DeepSeek-Chat',
      provider: 'deepseek',
      model: 'deepseek-chat',
      baseURL: 'https://api.deepseek.com',
      apiKey: process.env.EXTERNAL_AI_API_KEY,
      contextLength: 32768,
      maxTokens: 4096,
      temperature: 0.7,
      features: {
        streaming: true,
        coding: true,
        reasoning: true
      },
      tags: ['外部模型', '编程', '推理'],
      status: 'active'
    },
    {
      id: 'deepseek-coder',
      name: 'DeepSeek-Coder',
      provider: 'deepseek',
      model: 'deepseek-coder',
      baseURL: 'https://api.deepseek.com',
      apiKey: process.env.EXTERNAL_AI_API_KEY,
      contextLength: 16384,
      maxTokens: 4096,
      temperature: 0.1,
      features: {
        streaming: true,
        coding: true,
        codeGeneration: true
      },
      tags: ['外部模型', '编程专用', '代码生成'],
      status: 'active'
    },
    {
      id: 'gemini-1-5-pro',
      name: 'Gemini-1.5-Pro',
      provider: 'google',
      model: 'gemini-1.5-pro',
      baseURL: 'https://hk-intra-paas.transsion.com/tranai-proxy/v1',
      apiKey: process.env.INTERNAL_AI_API_KEY,
      contextLength: 1000000,
      maxTokens: 8192,
      temperature: 0.7,
      features: {
        streaming: true,
        vision: true,
        longContext: true
      },
      tags: ['内部模型', '超长上下文', '多模态'],
      status: 'active'
    },
    {
      id: 'gpt-4-turbo',
      name: 'GPT-4-Turbo',
      provider: 'openai',
      model: 'gpt-4-turbo',
      baseURL: 'https://hk-intra-paas.transsion.com/tranai-proxy/v1',
      apiKey: process.env.INTERNAL_AI_API_KEY,
      contextLength: 128000,
      maxTokens: 4096,
      temperature: 0.7,
      features: {
        streaming: true,
        functionCalling: true,
        vision: true
      },
      tags: ['内部模型', '高性能', '多模态'],
      status: 'active'
    },
    {
      id: 'claude-3-haiku',
      name: 'Claude-3-Haiku',
      provider: 'anthropic',
      model: 'claude-3-haiku-20240307',
      baseURL: 'https://hk-intra-paas.transsion.com/tranai-proxy/v1',
      apiKey: process.env.INTERNAL_AI_API_KEY,
      contextLength: 200000,
      maxTokens: 4096,
      temperature: 0.7,
      features: {
        streaming: true,
        fastResponse: true
      },
      tags: ['内部模型', '快速响应', '轻量级'],
      status: 'active'
    },
    {
      id: 'gpt-3-5-turbo',
      name: 'GPT-3.5-Turbo',
      provider: 'openai',
      model: 'gpt-3.5-turbo',
      baseURL: 'https://hk-intra-paas.transsion.com/tranai-proxy/v1',
      apiKey: process.env.INTERNAL_AI_API_KEY,
      contextLength: 16385,
      maxTokens: 4096,
      temperature: 0.7,
      features: {
        streaming: true,
        functionCalling: true
      },
      tags: ['内部模型', '经济实用', '通用'],
      status: 'active'
    }
  ];

  res.json({
    success: true,
    data: aiModels,
    message: 'AI模型配置获取成功'
  });
});

// 兼容聊天服务的AI模型配置端点
app.get('/api/configs/ai_models', (req, res) => {
  const aiModels = [
    {
      id: 'gpt-4o',
      name: 'GPT-4o',
      provider: 'openai',
      model: 'gpt-4o',
      baseURL: 'https://hk-intra-paas.transsion.com/tranai-proxy/v1',
      apiKey: process.env.INTERNAL_AI_API_KEY,
      contextLength: 128000,
      maxTokens: 4096,
      temperature: 0.7,
      features: {
        streaming: true,
        functionCalling: true,
        vision: true
      },
      tags: ['内部模型', '多模态', '推荐'],
      status: 'active'
    },
    {
      id: 'claude-3-5-sonnet',
      name: 'Claude-3.5-Sonnet',
      provider: 'anthropic',
      model: 'claude-3-5-sonnet-20241022',
      baseURL: 'https://hk-intra-paas.transsion.com/tranai-proxy/v1',
      apiKey: process.env.INTERNAL_AI_API_KEY,
      contextLength: 200000,
      maxTokens: 8192,
      temperature: 0.7,
      features: {
        streaming: true,
        functionCalling: true,
        longContext: true
      },
      tags: ['内部模型', '长上下文', '推荐'],
      status: 'active'
    },
    {
      id: 'deepseek-chat',
      name: 'DeepSeek-Chat',
      provider: 'deepseek',
      model: 'deepseek-chat',
      baseURL: 'https://api.deepseek.com',
      apiKey: process.env.EXTERNAL_AI_API_KEY,
      contextLength: 32768,
      maxTokens: 4096,
      temperature: 0.7,
      features: {
        streaming: true,
        coding: true,
        reasoning: true
      },
      tags: ['外部模型', '编程', '推理'],
      status: 'active'
    },
    {
      id: 'deepseek-coder',
      name: 'DeepSeek-Coder',
      provider: 'deepseek',
      model: 'deepseek-coder',
      baseURL: 'https://api.deepseek.com',
      apiKey: process.env.EXTERNAL_AI_API_KEY,
      contextLength: 16384,
      maxTokens: 4096,
      temperature: 0.1,
      features: {
        streaming: true,
        coding: true,
        codeGeneration: true
      },
      tags: ['外部模型', '编程专用', '代码生成'],
      status: 'active'
    },
    {
      id: 'gemini-1-5-pro',
      name: 'Gemini-1.5-Pro',
      provider: 'google',
      model: 'gemini-1.5-pro',
      baseURL: 'https://hk-intra-paas.transsion.com/tranai-proxy/v1',
      apiKey: process.env.INTERNAL_AI_API_KEY,
      contextLength: 1000000,
      maxTokens: 8192,
      temperature: 0.7,
      features: {
        streaming: true,
        vision: true,
        longContext: true
      },
      tags: ['内部模型', '超长上下文', '多模态'],
      status: 'active'
    },
    {
      id: 'gpt-4-turbo',
      name: 'GPT-4-Turbo',
      provider: 'openai',
      model: 'gpt-4-turbo',
      baseURL: 'https://hk-intra-paas.transsion.com/tranai-proxy/v1',
      apiKey: process.env.INTERNAL_AI_API_KEY,
      contextLength: 128000,
      maxTokens: 4096,
      temperature: 0.7,
      features: {
        streaming: true,
        functionCalling: true,
        vision: true
      },
      tags: ['内部模型', '高性能', '多模态'],
      status: 'active'
    },
    {
      id: 'claude-3-haiku',
      name: 'Claude-3-Haiku',
      provider: 'anthropic',
      model: 'claude-3-haiku-20240307',
      baseURL: 'https://hk-intra-paas.transsion.com/tranai-proxy/v1',
      apiKey: process.env.INTERNAL_AI_API_KEY,
      contextLength: 200000,
      maxTokens: 4096,
      temperature: 0.7,
      features: {
        streaming: true,
        fastResponse: true
      },
      tags: ['内部模型', '快速响应', '轻量级'],
      status: 'active'
    },
    {
      id: 'gpt-3-5-turbo',
      name: 'GPT-3.5-Turbo',
      provider: 'openai',
      model: 'gpt-3.5-turbo',
      baseURL: 'https://hk-intra-paas.transsion.com/tranai-proxy/v1',
      apiKey: process.env.INTERNAL_AI_API_KEY,
      contextLength: 16385,
      maxTokens: 4096,
      temperature: 0.7,
      features: {
        streaming: true,
        functionCalling: true
      },
      tags: ['内部模型', '经济实用', '通用'],
      status: 'active'
    }
  ];

  res.json({
    success: true,
    data: aiModels,
    message: 'AI模型配置获取成功'
  });
});

// 获取系统总体统计
app.get('/api/ai-monitoring/statistics/overall', checkAdminPermission, (req, res) => {
  const stats = {
    total_chats: monitoringData.chatRecords.length,
    unique_users: new Set(monitoringData.chatRecords.map(r => r.userId)).size,
    total_conversations: new Set(monitoringData.chatRecords.map(r => r.conversationId)).size,
    successful_chats: monitoringData.chatRecords.filter(r => r.chatStatus === 'SUCCESS').length,
    failed_chats: monitoringData.chatRecords.filter(r => r.chatStatus === 'FAILED').length,
    avg_response_time: monitoringData.chatRecords.reduce((sum, r) => sum + r.responseTime, 0) / monitoringData.chatRecords.length,
    total_likes: monitoringData.feedbackRecords.filter(f => f.feedbackType === 'LIKE').length,
    total_dislikes: monitoringData.feedbackRecords.filter(f => f.feedbackType === 'DISLIKE').length,
    total_feedback: monitoringData.feedbackRecords.length
  };

  res.json({
    success: true,
    data: stats
  });
});

// 获取用户使用统计
app.get('/api/ai-monitoring/statistics/users', checkAdminPermission, (req, res) => {
  const userStats = [];
  const userGroups = {};

  // 按用户分组统计
  monitoringData.chatRecords.forEach(record => {
    if (!userGroups[record.userId]) {
      userGroups[record.userId] = {
        user_id: record.userId,
        user_name: record.userName,
        count: 0,
        like_count: 0,
        dislike_count: 0,
        total_feedback: 0
      };
    }
    userGroups[record.userId].count++;
  });

  // 添加反馈统计
  monitoringData.feedbackRecords.forEach(feedback => {
    const chatRecord = monitoringData.chatRecords.find(r => r.messageId === feedback.messageId);
    if (chatRecord && userGroups[chatRecord.userId]) {
      if (feedback.feedbackType === 'LIKE') {
        userGroups[chatRecord.userId].like_count++;
      } else {
        userGroups[chatRecord.userId].dislike_count++;
      }
      userGroups[chatRecord.userId].total_feedback++;
    }
  });

  res.json({
    success: true,
    data: Object.values(userGroups)
  });
});

// 获取模型使用统计
app.get('/api/ai-monitoring/statistics/models', checkAdminPermission, (req, res) => {
  const modelStats = [];
  const modelGroups = {};

  // 按模型分组统计
  monitoringData.chatRecords.forEach(record => {
    const key = `${record.modelProvider}_${record.modelName}`;
    if (!modelGroups[key]) {
      modelGroups[key] = {
        model_provider: record.modelProvider,
        model_name: record.modelName,
        count: 0,
        like_count: 0,
        dislike_count: 0,
        total_feedback: 0
      };
    }
    modelGroups[key].count++;
  });

  // 添加反馈统计
  monitoringData.feedbackRecords.forEach(feedback => {
    const chatRecord = monitoringData.chatRecords.find(r => r.messageId === feedback.messageId);
    if (chatRecord) {
      const key = `${chatRecord.modelProvider}_${chatRecord.modelName}`;
      if (modelGroups[key]) {
        if (feedback.feedbackType === 'LIKE') {
          modelGroups[key].like_count++;
        } else {
          modelGroups[key].dislike_count++;
        }
        modelGroups[key].total_feedback++;
      }
    }
  });

  res.json({
    success: true,
    data: Object.values(modelGroups)
  });
});

// 获取反馈统计
app.get('/api/ai-monitoring/statistics/feedback', checkAdminPermission, (req, res) => {
  const feedbackTypeStats = [
    { feedback_type: 'LIKE', count: monitoringData.feedbackRecords.filter(f => f.feedbackType === 'LIKE').length },
    { feedback_type: 'DISLIKE', count: monitoringData.feedbackRecords.filter(f => f.feedbackType === 'DISLIKE').length }
  ];

  const feedbackReasonStats = {};
  monitoringData.feedbackRecords.forEach(f => {
    if (f.feedbackReason) {
      feedbackReasonStats[f.feedbackReason] = (feedbackReasonStats[f.feedbackReason] || 0) + 1;
    }
  });

  const reasonStats = Object.entries(feedbackReasonStats).map(([reason, count]) => ({
    feedback_reason: reason,
    count
  }));

  res.json({
    success: true,
    data: {
      feedback_type_distribution: feedbackTypeStats,
      feedback_reason_distribution: reasonStats,
      satisfaction_trend: [
        { date: '2025-01-31', like_count: 8, dislike_count: 2, total_feedback: 10 }
      ]
    }
  });
});

// 分页查询问答记录
app.get('/api/ai-monitoring/chat-records', checkAdminPermission, (req, res) => {
  const { current = 1, size = 20, userId, modelProvider, chatStatus } = req.query;

  let filteredRecords = [...monitoringData.chatRecords];

  // 应用过滤条件
  if (userId) {
    filteredRecords = filteredRecords.filter(r => r.userId.includes(userId));
  }
  if (modelProvider) {
    filteredRecords = filteredRecords.filter(r => r.modelProvider === modelProvider);
  }
  if (chatStatus) {
    filteredRecords = filteredRecords.filter(r => r.chatStatus === chatStatus);
  }

  // 分页
  const start = (current - 1) * size;
  const end = start + parseInt(size);
  const records = filteredRecords.slice(start, end);

  res.json({
    success: true,
    data: {
      records,
      total: filteredRecords.length,
      current: parseInt(current),
      size: parseInt(size)
    }
  });
});

// 分页查询反馈记录
app.get('/api/ai-monitoring/feedback-records', checkAdminPermission, (req, res) => {
  const { current = 1, size = 20, feedbackType, userId } = req.query;

  let filteredRecords = [...monitoringData.feedbackRecords];

  // 应用过滤条件
  if (feedbackType) {
    filteredRecords = filteredRecords.filter(r => r.feedbackType === feedbackType);
  }
  if (userId) {
    filteredRecords = filteredRecords.filter(r => r.userId.includes(userId));
  }

  // 关联问答记录信息
  const recordsWithChatInfo = filteredRecords.map(feedback => {
    const chatRecord = monitoringData.chatRecords.find(r => r.messageId === feedback.messageId);
    return {
      ...feedback,
      user_message: chatRecord?.userMessage,
      ai_response: chatRecord?.aiResponse,
      model_provider: chatRecord?.modelProvider,
      model_name: chatRecord?.modelName
    };
  });

  // 分页
  const start = (current - 1) * size;
  const end = start + parseInt(size);
  const records = recordsWithChatInfo.slice(start, end);

  res.json({
    success: true,
    data: {
      records,
      total: filteredRecords.length,
      current: parseInt(current),
      size: parseInt(size)
    }
  });
});

// 获取监控仪表板数据
app.get('/api/ai-monitoring/dashboard', checkAdminPermission, (req, res) => {
  const overallStats = {
    total_chats: monitoringData.chatRecords.length,
    unique_users: new Set(monitoringData.chatRecords.map(r => r.userId)).size,
    successful_chats: monitoringData.chatRecords.filter(r => r.chatStatus === 'SUCCESS').length,
    total_likes: monitoringData.feedbackRecords.filter(f => f.feedbackType === 'LIKE').length,
    total_dislikes: monitoringData.feedbackRecords.filter(f => f.feedbackType === 'DISLIKE').length
  };

  res.json({
    success: true,
    data: {
      overall_statistics: overallStats,
      usage_trend: [
        { hour: 0, count: 5 },
        { hour: 8, count: 15 },
        { hour: 12, count: 25 },
        { hour: 16, count: 20 },
        { hour: 20, count: 10 }
      ],
      satisfaction_trend: [
        { date: '2025-01-31', like_count: 8, dislike_count: 2 }
      ],
      response_time_distribution: [
        { time_range: '< 1s', count: 5 },
        { time_range: '1-3s', count: 15 },
        { time_range: '3-5s', count: 8 },
        { time_range: '> 5s', count: 2 }
      ],
      popular_questions: [
        { user_message: '如何提升产品质量？', count: 5 },
        { user_message: '质量管理体系如何建立？', count: 3 }
      ],
      active_users: [
        { user_id: 'user001', user_name: '张三', chat_count: 15, last_chat_time: '2025-01-31 15:30:00' },
        { user_id: 'user002', user_name: '李四', chat_count: 12, last_chat_time: '2025-01-31 14:45:00' }
      ]
    }
  });
});

// 获取实时监控指标
app.get('/api/ai-monitoring/realtime-metrics', checkAdminPermission, (req, res) => {
  const now = new Date();
  const oneHourAgo = new Date(now.getTime() - 60 * 60 * 1000);

  // 模拟最近一小时的数据
  const recentChats = monitoringData.chatRecords.filter(r =>
    new Date(r.createdTime) >= oneHourAgo
  );

  res.json({
    success: true,
    data: {
      total_chats: recentChats.length,
      unique_users: new Set(recentChats.map(r => r.userId)).size,
      successful_chats: recentChats.filter(r => r.chatStatus === 'SUCCESS').length,
      avg_response_time: recentChats.length > 0 ?
        recentChats.reduce((sum, r) => sum + r.responseTime, 0) / recentChats.length : 0
    }
  });
});

// 记录AI问答
app.post('/api/ai-monitoring/chat-record', (req, res) => {
  const chatRecord = {
    id: monitoringData.chatRecords.length + 1,
    ...req.body,
    createdTime: new Date().toISOString()
  };

  monitoringData.chatRecords.push(chatRecord);

  console.log('📝 记录AI问答:', chatRecord.messageId);

  res.json({
    success: true,
    message: '记录成功'
  });
});

// 记录用户反馈
app.post('/api/ai-monitoring/feedback', (req, res) => {
  const feedback = {
    id: monitoringData.feedbackRecords.length + 1,
    ...req.body,
    createdTime: new Date().toISOString()
  };

  monitoringData.feedbackRecords.push(feedback);

  console.log('👍 记录用户反馈:', feedback.messageId, feedback.feedbackType);

  res.json({
    success: true,
    message: '反馈记录成功'
  });
});

const PORT = 3003;
app.listen(PORT, () => {
  console.log(`🚀 QMS配置中心服务启动成功！`);
  console.log(`📍 服务地址: http://localhost:${PORT}`);
  console.log(`🔧 健康检查: http://localhost:${PORT}/health`);
  console.log(`📋 已预置配置中心数据`);
  console.log(`🔐 认证功能: 支持用户登录注册`);
  console.log(`👥 预置用户: admin/admin123, developer/dev123, tester/test123`);
});

// 启动认证服务代理 (端口8083)
const authApp = express();
authApp.use(cors());
authApp.use(express.json());

// 代理所有认证请求到配置中心服务
authApp.use('/auth', (req, res) => {
  const axios = require('axios');
  const targetUrl = `http://localhost:3003/auth${req.path}`;

  axios({
    method: req.method,
    url: targetUrl,
    data: req.body,
    headers: {
      ...req.headers,
      host: 'localhost:3003'
    }
  }).then(response => {
    res.status(response.status).json(response.data);
  }).catch(error => {
    console.error('认证代理错误:', error.message);
    if (error.response) {
      res.status(error.response.status).json(error.response.data);
    } else {
      res.status(500).json({
        success: false,
        message: '认证服务连接失败'
      });
    }
  });
});

const AUTH_PORT = 8083;
authApp.listen(AUTH_PORT, () => {
  console.log(`🔐 认证服务代理启动成功！`);
  console.log(`📍 认证服务地址: http://localhost:${AUTH_PORT}`);
  console.log(`🔄 代理目标: http://localhost:3003`);
});
