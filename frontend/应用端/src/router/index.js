import { createRouter, createWebHistory } from 'vue-router'
import { ElMessage } from 'element-plus'
import Layout from '@/layout/index.vue'
import { useAuthStore } from '@/stores/auth'

const routes = [
  {
    path: '/',
    redirect: '/dashboard'
  },

  {
    path: '/',
    component: Layout,
    children: [
      {
        path: 'dashboard',
        name: 'Dashboard',
        component: () => import('@/views/dashboard/index.vue'),
        meta: {
          title: '工作台',
          icon: 'House',
          affix: true
        }
      }
    ]
  },
  {
    path: '/ai-management',
    component: Layout,
    meta: {
      title: 'AI智能管理',
      icon: 'Cpu'
    },
    children: [
      // === 核心功能区 ===
      {
        path: 'chat',
        name: 'AIChat',
        component: () => import('@/views/ai-management/chat/optimized.vue'),
        meta: {
          title: 'AI智能问答',
          icon: 'ChatDotRound',
          description: '与AI助手进行智能对话，获取质量管理专业建议'
        }
      },
      {
        path: 'models',
        name: 'AIModels',
        component: () => import('@/views/ai-management/models/index.vue'),
        meta: {
          title: 'AI模型管理',
          icon: 'Cpu',
          description: '管理和配置QMS系统中的8个AI模型'
        }
      },
      {
        path: 'evaluation',
        name: 'AIEvaluation',
        component: () => import('@/views/ai-management/evaluation/index.vue'),
        meta: {
          title: '效果评测',
          icon: 'DataAnalysis',
          description: 'AI模型效果评测和性能分析'
        }
      },
      {
        path: 'conversations',
        name: 'ConversationHistory',
        component: () => import('@/views/ai-management/management/conversations.vue'),
        meta: {
          title: '对话历史',
          icon: 'Document',
          description: '查看和管理所有AI对话历史记录'
        }
      },



      // === 功能测试 ===
      {
        path: 'test',
        name: 'FunctionalTest',
        component: () => import('@/views/ai-management/test/functional-test.vue'),
        meta: {
          title: '功能验证测试',
          icon: 'Monitor',
          description: '验证系统各项功能是否正常工作',
          roles: ['ADMIN']
        }
      },

      // === 管理中心 ===
      {
        path: 'management',
        name: 'AIManagement',
        component: () => import('@/views/ai-management/management/index.vue'),
        meta: {
          title: 'AI管理中心',
          icon: 'Operation'
        },
        children: [
          {
            path: 'conversations',
            name: 'ConversationManagement',
            component: () => import('@/views/ai-management/management/conversations.vue'),
            meta: {
              title: '对话记录管理',
              icon: 'Document',
              description: '查看、搜索、导出所有AI对话记录'
            }
          },
          {
            path: 'analytics',
            name: 'AIAnalytics',
            component: () => import('@/views/ai-management/rating-stats/index.vue'),
            meta: {
              title: '使用分析统计',
              icon: 'DataAnalysis',
              description: '分析AI使用情况、评分统计、用户行为'
            }
          },
          {
            path: 'monitoring',
            name: 'AIMonitoring',
            component: () => import('@/views/ai-management/monitoring/index.vue'),
            meta: {
              title: '性能监控',
              icon: 'Monitor',
              description: '监控AI服务性能、响应时间、错误率',
              permission: 'ai:monitoring:view',
              roles: ['ADMIN']
            }
          }
        ]
      },

      // === 系统配置 ===
      {
        path: 'config',
        name: 'AIConfig',
        component: () => import('@/views/ai-management/config/index.vue'),
        meta: {
          title: '系统配置',
          icon: 'Setting',
          roles: ['ADMIN']
        },
        children: [
          {
            path: 'models',
            name: 'ModelConfig',
            component: () => import('@/views/ai-management/chat-config/index.vue'),
            meta: {
              title: '模型配置',
              icon: 'Cpu',
              description: '配置AI模型参数、API密钥、服务地址'
            }
          },
          {
            path: 'rules',
            name: 'RuleConfig',
            component: () => import('@/views/ai-management/ai-rule/index.vue'),
            meta: {
              title: '规则配置',
              icon: 'DocumentChecked',
              description: '设置AI回答规则、内容过滤、安全策略'
            }
          },
          {
            path: 'datasource',
            name: 'DataSourceConfig',
            component: () => import('@/views/ai-management/data-source/index.vue'),
            meta: {
              title: '数据源配置',
              icon: 'DataBoard',
              description: '配置知识库、文档库、数据接口'
            }
          },
          {
            path: 'users',
            name: 'UserManagement',
            component: () => import('@/views/ai-management/user-management/index.vue'),
            meta: {
              title: '用户权限',
              icon: 'User',
              description: '管理用户权限、角色分配、访问控制'
            }
          }
        ]
      },

      // === 高级功能 ===
      {
        path: 'advanced',
        name: 'AIAdvanced',
        meta: {
          title: '高级功能',
          icon: 'MagicStick',
          hidden: true // 暂时隐藏，未来开发
        },
        children: [
          {
            path: 'recommendations',
            name: 'AIRecommendations',
            component: () => import('@/views/recommendations/RecommendationPanel.vue'),
            meta: {
              title: '智能推荐',
              icon: 'MagicStick'
            }
          },
          {
            path: 'collaboration',
            name: 'AICollaboration',
            component: () => import('@/views/collaboration/CollaborationCenter.vue'),
            meta: {
              title: '团队协作',
              icon: 'UserFilled'
            }
          },
          {
            path: 'integration',
            name: 'AIIntegration',
            component: () => import('@/views/integration/IntegrationStatus.vue'),
            meta: {
              title: '系统集成',
              icon: 'Connection'
            }
          }
        ]
      }
    ]
  },
  {
    path: '/system',
    component: Layout,
    meta: {
      title: '系统管理',
      icon: 'Setting',
      roles: ['ADMIN']
    },
    children: [
      {
        path: 'config',
        name: 'SystemConfig',
        component: () => import('@/views/system/config/index.vue'),
        meta: {
          title: '系统配置',
          icon: 'Setting'
        }
      },
      {
        path: 'logs',
        name: 'SystemLogs',
        component: () => import('@/views/system/logs/index.vue'),
        meta: {
          title: '系统日志',
          icon: 'Document'
        }
      }
    ]
  },

  // 登录页面
  {
    path: '/login',
    name: 'Login',
    component: () => import('@/views/auth/Login.vue'),
    meta: {
      title: '用户登录',
      hidden: true
    }
  },

  // 登录帮助页面
  {
    path: '/help/login',
    name: 'LoginGuide',
    component: () => import('@/views/help/LoginGuide.vue'),
    meta: {
      title: '登录帮助',
      hidden: true
    }
  },

  // 错误页面
  {
    path: '/403',
    name: 'Forbidden',
    component: () => import('@/views/error/403.vue'),
    meta: {
      title: '权限不足',
      hidden: true
    }
  },
  {
    path: '/404',
    name: 'NotFound',
    component: () => import('@/views/error/404.vue'),
    meta: {
      title: '页面不存在',
      hidden: true
    }
  },

  // 个人中心
  {
    path: '/profile',
    name: 'Profile',
    component: () => import('@/views/profile/index.vue'),
    meta: {
      title: '个人中心',
      hidden: true
    }
  },

  // 集成管理
  {
    path: '/integrations',
    component: Layout,
    meta: {
      title: '集成管理',
      icon: 'Connection'
    },
    children: [
      {
        path: 'feishu',
        name: 'FeishuIntegration',
        component: () => import('@/views/integrations/feishu/index.vue'),
        meta: {
          title: '飞书集成',
          icon: 'ChatDotRound',
          description: '集成飞书开放平台API，实现消息推送、文档操作、日程管理等功能'
        }
      },
      {
        path: 'mcp',
        name: 'MCPIntegration',
        component: () => import('@/views/integrations/mcp/index.vue'),
        meta: {
          title: 'MCP服务连接器',
          icon: 'Connection',
          description: 'Model Context Protocol - 连接各种AI工具和服务的标准协议'
        }
      }
    ]
  },

  // Studio 入口合并到 Coze Studio：保留路由但重定向，避免双入口
  {
    path: '/studio',
    redirect: '/coze-studio',
    meta: {
      title: 'QMS Studio（已合并到 Coze Studio）',
      icon: 'Tools',
      requiresAuth: true,
      hidden: true
    }
  },

  // 个人空间
  {
    path: '/personal-space',
    name: 'PersonalSpace',
    component: () => import('@/views/studio/PersonalSpace.vue'),
    meta: {
      title: '个人空间',
      icon: 'User',
      description: '个人资源管理和项目开发空间',
      requiresAuth: true
    }
  },

  {
    path: '/app',
    name: 'App',
    component: () => import('@/views/app/index.vue'),
    meta: {
      title: 'QMS AI - 智能助手平台',
      icon: 'ChatDotRound',
      description: '使用AI助手、工作流和知识库',
      requiresAuth: true
    }
  },

  // Coze Studio AI开发平台
  {
    path: '/coze-studio',
    component: Layout,
    meta: {
      title: 'Coze Studio',
      icon: 'Grid',
      requiresAuth: true
    },
    children: [
      // 平台首页
      {
        path: '',
        name: 'CozeStudioHome',
        component: () => import('@/views/coze-studio/index.vue'),
        meta: {
          title: 'Coze Studio',
          icon: 'Grid',
          description: 'AI开发平台总览'
        }
      },


	      // 插件建设（分组）
	      {
	        path: 'plugins-hub',
	        name: 'CozePluginsHub',
	        component: () => import('@/views/common/RouterView.vue'),
	        meta: {
	          title: '插件建设',
	          icon: 'Tools',
	          description: '插件安装、功能验证与执行监控'
	        },
	        children: [
	          {
	            path: 'plugins',
	            name: 'CozePluginsGrouped',
	            component: () => import('@/views/coze-studio/components/PluginEcosystemManager.vue'),
	            meta: { title: '插件中心', icon: 'Tools' }
	          },
	          {
	            path: 'validation',
	            name: 'CozeValidationGrouped',
	            component: () => import('@/views/coze-studio/validation/index.vue'),
	            meta: { title: '功能验证/巡检', icon: 'Check' }
	          },
	          {
	            path: 'monitoring',
	            name: 'CozeMonitoringGrouped',
	            component: () => import('@/views/coze-studio/monitoring/index.vue'),
	            meta: { title: '执行监控', icon: 'Monitor' }
	          }
	        ]
	      },

	      // 业务编排（分组）
	      {
	        path: 'orchestration-hub',
	        name: 'CozeOrchestrationHub',
	        component: () => import('@/views/common/RouterView.vue'),
	        meta: { title: '业务编排', icon: 'OfficeBuilding', description: '项目、Agent 与工作流编排' },
	        children: [
	          { path: 'projects', name: 'CozeProjectsGrouped', component: () => import('@/views/coze-studio/dashboard/index.vue'), meta: { title: '项目管理', icon: 'OfficeBuilding' } },
	          { path: 'agents', name: 'CozeAgentsGrouped', component: () => import('@/views/coze-studio/agents/agents.vue'), meta: { title: 'Agent开发', icon: 'User' } },
	          { path: 'workflows', name: 'CozeWorkflowsGrouped', component: () => import('@/views/coze-studio/workflows/index.vue'), meta: { title: '工作流设计', icon: 'Connection' } }
	        ]
	      },

	      // AI能力（分组）
	      {
	        path: 'ai-hub',
	        name: 'CozeAIHub',
	        component: () => import('@/views/common/RouterView.vue'),
	        meta: { title: 'AI能力', icon: 'Cpu', description: '模型、知识库与记忆' },
	        children: [
	          { path: 'models', name: 'CozeModelsGrouped', component: () => import('@/views/coze-studio/models/index.vue'), meta: { title: 'AI模型', icon: 'Cpu' } },
	          { path: 'knowledge', name: 'CozeKnowledgeGrouped', component: () => import('@/views/coze-studio/knowledge/index.vue'), meta: { title: '知识库', icon: 'Document' } },
	          { path: 'memory', name: 'CozeMemoryGrouped', component: () => import('@/views/coze-studio/components/MemoryManager.vue'), meta: { title: 'Memory记忆', icon: 'DataBoard' } }
	        ]
	      },

	      // 智能协作（分组）
	      {
	        path: 'collab-hub',
	        name: 'CozeCollabHub',
	        component: () => import('@/views/common/RouterView.vue'),
	        meta: { title: '智能协作', icon: 'MagicStick', description: 'AutoGPT 与 CrewAI' },
	        children: [
	          { path: 'autogpt', name: 'CozeAutoGPTGrouped', component: () => import('@/views/coze-studio/components/AutoGPTManager.vue'), meta: { title: 'AutoGPT规划', icon: 'MagicStick' } },
	          { path: 'crewai', name: 'CozeCrewAIGrouped', component: () => import('@/views/coze-studio/components/CrewAIManager.vue'), meta: { title: 'CrewAI协作', icon: 'UserFilled' } }
	        ]
	      },
      // 项目管理
      {
        path: 'projects',
        name: 'CozeProjects',
        component: () => import('@/views/coze-studio/dashboard/index.vue'),
        meta: {
          title: '项目管理',
          icon: 'OfficeBuilding',
          description: '项目创建、管理和协作',
          hidden: true
        }
      },

      // Agent开发
      {
        path: 'agents',
        name: 'CozeAgents',
        component: () => import('@/views/coze-studio/agents/agents.vue'),
        meta: {
          title: 'Agent开发',
          icon: 'User',
          description: 'AI Agent创建、测试和发布',
          hidden: true
        }
      },

      // 工作流设计
      {
        path: 'workflows',
        name: 'CozeWorkflows',
        component: () => import('@/views/coze-studio/workflows/index.vue'),
        meta: {
          title: '工作流设计',
          icon: 'Connection',
          description: '可视化工作流设计和执行',
          hidden: true
        }
      },
      {
        path: 'workflows/:id',
        name: 'CozeWorkflowDetail',
        component: () => import('@/views/coze-studio/workflows/detail.vue'),
        meta: {
          title: '工作流详情',
          icon: 'Connection',
          description: '工作流详情查看',
          hidden: true
        }
      },
      {
        path: 'workflows/:id/edit',
        name: 'CozeWorkflowEdit',
        component: () => import('@/views/coze-studio/workflows/edit.vue'),
        meta: {
          title: '工作流编辑',
          icon: 'Connection',
          description: '可视化工作流编辑器',
          hidden: true
        }
      },

      // 插件中心
      {
        path: 'plugins',
        name: 'CozePlugins',
        component: () => import('@/views/coze-studio/components/PluginEcosystemManager.vue'),
        meta: {
          title: '插件中心',
          icon: 'Tools',
          description: '插件安装、管理和执行',
          hidden: true
        }
      },

      // 知识库
      {
        path: 'knowledge',
        name: 'CozeKnowledge',
        component: () => import('@/views/coze-studio/knowledge/index.vue'),
        meta: {
          title: '知识库',
          icon: 'Document',
          description: '知识库创建和文档管理',
          hidden: true
        }
      },

      // AI模型
      {
        path: 'models',
        name: 'CozeModels',
        component: () => import('@/views/coze-studio/models/index.vue'),
        meta: {
          title: 'AI模型',
          icon: 'Cpu',
          description: 'AI模型管理和配置',
          hidden: true
        }
      },

      // AutoGPT规划
      {
        path: 'autogpt',
        name: 'CozeAutoGPT',
        component: () => import('@/views/coze-studio/components/AutoGPTManager.vue'),
        meta: {
          title: 'AutoGPT规划',
          icon: 'MagicStick',
          description: 'AI自主任务规划和执行',
          hidden: true
        }
      },

      // CrewAI协作
      {
        path: 'crewai',
        name: 'CozeCrewAI',
        component: () => import('@/views/coze-studio/components/CrewAIManager.vue'),
        meta: {
          title: 'CrewAI协作',
          icon: 'UserFilled',
          description: '多智能体团队协作',
          hidden: true
        }
      },

      // Memory记忆
      {
        path: 'memory',
        name: 'CozeMemory',
        component: () => import('@/views/coze-studio/components/MemoryManager.vue'),
        meta: {
          title: 'Memory记忆',
          icon: 'DataBoard',
          description: 'Agent记忆系统管理',
          hidden: true
        }
      },

      // 执行监控
      {
        path: 'monitoring',
        name: 'CozeMonitoring',
        component: () => import('@/views/coze-studio/monitoring/index.vue'),
        meta: {
          title: '执行监控',
          icon: 'Monitor',
          description: '任务执行状态和性能监控',
          hidden: true
        }
      },

      {
        path: 'validation',
        name: 'CozeValidation',
        component: () => import('@/views/coze-studio/validation/index.vue'),
        meta: {
          title: '功能验证/巡检',
          icon: 'Check',
          description: '选择插件与场景，逐步执行并查看过程与结果',
          hidden: true
        }
      },


      // 开发工具（隐藏）
      {
        path: 'api-test',
        name: 'CozeStudioAPITest',
        component: () => import('@/views/coze-studio/test.vue'),
        meta: {
          title: 'API测试',
          icon: 'Setting',
          description: '开发者API接口测试工具',
          hidden: true
        }
      }
    ]
  },

  // 调试页面
  {
    path: '/debug',
    name: 'MenuDebug',
    component: () => import('@/views/debug/menu-debug.vue'),
    meta: {
      title: '菜单调试',
      hidden: true
    }
  },

  // 404重定向
  {
    path: '/:pathMatch(.*)*',
    redirect: '/404'
  }
]

const router = createRouter({
  history: createWebHistory(),
  routes,
  scrollBehavior: () => ({ top: 0 })
})

// 导入认证工具
import { isLoggedIn, validateToken, hasPermission, getUser } from '@/utils/auth'
// ElMessage已在文件顶部导入，无需重复导入

// 路由守卫
router.beforeEach(async (to, from, next) => {
  // 设置页面标题
  if (to.meta.title) {
    document.title = `${to.meta.title} - QMS配置驱动智能管理系统`
  }

  // 白名单路由（不需要登录的页面）
  const whiteList = ['/login', '/register', '/404', '/403', '/about', '/help/login']
  if (whiteList.includes(to.path)) {
    if ((to.path === '/login' || to.path === '/register') && isLoggedIn()) {
      return next('/')
    }
    return next()
  }

  // 认证状态：轻量化、只初始化一次（幂等）
  const authStore = useAuthStore()
  const isValidAuth = await authStore.ensureInitialized()
  if (!isValidAuth) {
    if (to.path !== '/login') {
      ElMessage.warning('请先登录才能访问系统')
    }
    return next(`/login?redirect=${encodeURIComponent(to.fullPath)}`)
  }

  // 检查页面权限（存在权限声明时才检查）
  if (to.meta?.permissions && Array.isArray(to.meta.permissions)) {
    const ok = to.meta.permissions.some(p => authStore.hasPermission(p))
    if (!ok) return next('/403')
  }

  return next()
})

export default router
