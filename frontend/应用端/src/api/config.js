import axios from 'axios'

// 创建axios实例 - 统一使用/api前缀，通过代理转发到配置端Mock服务
const configApi = axios.create({
  baseURL: '/api',
  timeout: 0 // 解除超时限制
})

const appApi = axios.create({
  baseURL: '/api',
  timeout: 0 // 解除超时限制
})

// 请求拦截器
configApi.interceptors.request.use(
  config => {
    // 添加认证token等
    return config
  },
  error => {
    return Promise.reject(error)
  }
)

// 响应拦截器
configApi.interceptors.response.use(
  response => {
    return response.data
  },
  error => {
    console.error('配置API请求失败:', error)
    return Promise.reject(error)
  }
)

appApi.interceptors.response.use(
  response => {
    return response.data
  },
  error => {
    console.error('应用API请求失败:', error)
    return Promise.reject(error)
  }
)

// 从配置端获取配置
export const getConfigFromBackend = async () => {
  try {
    // 优先从配置端获取配置 - 直接使用/health路径
    const response = await axios.get('/health')
    console.log('配置端连接状态:', response)

    // 模拟配置数据，实际应该从配置端获取
    const mockConfig = {
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

    return mockConfig
  } catch (error) {
    console.error('获取配置失败:', error)
    // 返回默认配置
    return getDefaultConfig()
  }
}

// 订阅配置更新
export const subscribeConfigUpdates = (callback) => {
  // 这里可以实现WebSocket或轮询机制
  // 暂时使用轮询
  const pollInterval = 30000 // 30秒轮询一次
  
  const poll = async () => {
    try {
      const newConfig = await getConfigFromBackend()
      callback(newConfig)
    } catch (error) {
      console.error('轮询配置更新失败:', error)
    }
  }
  
  // 立即执行一次
  setTimeout(poll, 1000)
  
  // 设置定时轮询
  setInterval(poll, pollInterval)
}

// 获取默认配置
const getDefaultConfig = () => {
  return {
    aiConversation: {
      fields: [
        {
          prop: 'conversationId',
          label: '对话ID',
          type: 'text',
          width: 150,
          visible: true,
          sortable: true
        },
        {
          prop: 'userId',
          label: '用户ID',
          type: 'text',
          width: 120,
          visible: true,
          sortable: true
        },
        {
          prop: 'userQuestion',
          label: '用户问题',
          type: 'text',
          minWidth: 300,
          visible: true,
          showTooltip: true
        },
        {
          prop: 'conversationStatus',
          label: '对话状态',
          type: 'tag',
          width: 100,
          visible: true,
          sortable: true
        },
        {
          prop: 'satisfactionScore',
          label: '满意度',
          type: 'rate',
          width: 100,
          visible: true
        },
        {
          prop: 'conversationTime',
          label: '对话时间',
          type: 'datetime',
          width: 160,
          visible: true,
          sortable: true
        }
      ],
      permissions: {
        view: ['admin', 'user'],
        export: ['admin'],
        analyze: ['admin', 'analyst']
      },
      functions: {
        showSelection: true,
        showIndex: true,
        pageSize: 20,
        tableHeight: null,
        rowClickAction: 'detail'
      },
      search: [
        {
          key: 'userId',
          label: '用户ID',
          type: 'input',
          placeholder: '请输入用户ID',
          span: 6
        },
        {
          key: 'status',
          label: '对话状态',
          type: 'select',
          placeholder: '请选择状态',
          span: 6,
          options: [
            { label: '全部', value: '' },
            { label: '进行中', value: '进行中' },
            { label: '已完成', value: '已完成' },
            { label: '已中断', value: '已中断' }
          ]
        },
        {
          key: 'dateRange',
          label: '时间范围',
          type: 'daterange',
          span: 6
        }
      ]
    },
    dataSource: {
      fields: [
        {
          prop: 'dataSourceName',
          label: '数据源名称',
          type: 'text',
          width: 200,
          visible: true
        },
        {
          prop: 'dataSourceType',
          label: '数据源类型',
          type: 'tag',
          width: 120,
          visible: true
        },
        {
          prop: 'connectionUrl',
          label: '连接地址',
          type: 'text',
          minWidth: 250,
          visible: true,
          showTooltip: true
        },
        {
          prop: 'connectionStatus',
          label: '连接状态',
          type: 'status',
          width: 100,
          visible: true
        },
        {
          prop: 'updateFrequency',
          label: '更新频率',
          type: 'text',
          width: 120,
          visible: true
        }
      ],
      permissions: {
        view: ['admin', 'user'],
        add: ['admin'],
        edit: ['admin'],
        delete: ['admin'],
        test: ['admin', 'user']
      },
      functions: {
        showSelection: true,
        showIndex: true,
        pageSize: 20
      },
      search: [
        {
          key: 'dataSourceName',
          label: '数据源名称',
          type: 'input',
          placeholder: '请输入数据源名称',
          span: 6
        },
        {
          key: 'dataSourceType',
          label: '数据源类型',
          type: 'select',
          placeholder: '请选择类型',
          span: 6,
          options: [
            { label: '全部', value: '' },
            { label: 'MySQL', value: 'MySQL' },
            { label: 'PostgreSQL', value: 'PostgreSQL' },
            { label: 'Oracle', value: 'Oracle' }
          ]
        }
      ]
    },
    aiRule: {
      fields: [
        {
          prop: 'ruleName',
          label: '规则名称',
          type: 'text',
          width: 200,
          visible: true
        },
        {
          prop: 'ruleType',
          label: '规则类型',
          type: 'tag',
          width: 120,
          visible: true
        },
        {
          prop: 'priority',
          label: '优先级',
          type: 'number',
          width: 100,
          visible: true,
          sortable: true
        },
        {
          prop: 'ruleStatus',
          label: '规则状态',
          type: 'switch',
          width: 100,
          visible: true
        },
        {
          prop: 'triggerCondition',
          label: '触发条件',
          type: 'text',
          minWidth: 200,
          visible: true,
          showTooltip: true
        },
        {
          prop: 'executeAction',
          label: '执行动作',
          type: 'text',
          minWidth: 200,
          visible: true,
          showTooltip: true
        }
      ],
      permissions: {
        view: ['admin', 'user'],
        add: ['admin'],
        edit: ['admin'],
        delete: ['admin'],
        toggle: ['admin']
      },
      functions: {
        showSelection: true,
        showIndex: true,
        pageSize: 20
      }
    },
    userManagement: {
      fields: [
        {
          prop: 'username',
          label: '用户名',
          type: 'text',
          width: 150,
          visible: true
        },
        {
          prop: 'realName',
          label: '真实姓名',
          type: 'text',
          width: 120,
          visible: true
        },
        {
          prop: 'userRole',
          label: '用户角色',
          type: 'tag',
          width: 120,
          visible: true
        },
        {
          prop: 'email',
          label: '邮箱',
          type: 'text',
          width: 200,
          visible: true
        },
        {
          prop: 'phone',
          label: '手机号',
          type: 'text',
          width: 150,
          visible: true
        },
        {
          prop: 'accountStatus',
          label: '账户状态',
          type: 'switch',
          width: 100,
          visible: true
        }
      ],
      permissions: {
        view: ['admin'],
        add: ['admin'],
        edit: ['admin'],
        resetPassword: ['admin'],
        toggle: ['admin']
      },
      functions: {
        showSelection: true,
        showIndex: true,
        pageSize: 20
      }
    }
  }
}

// 获取业务数据
export const getBusinessData = async (moduleKey, params = {}) => {
  try {
    const response = await appApi.post(`/object-model/${moduleKey}/page`, params)
    return response
  } catch (error) {
    console.error(`获取${moduleKey}数据失败:`, error)
    throw error
  }
}
