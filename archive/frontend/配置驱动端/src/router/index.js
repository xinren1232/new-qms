import { createRouter, createWebHistory } from 'vue-router'
import Layout from '@/layout/index.vue'
import { useAuthStore } from '@/stores/auth'

const routes = [
  {
    path: '/',
    redirect: '/dashboard'
  },
  {
    path: '/login',
    name: 'Login',
    component: () => import('@/views/auth/Login.vue'),
    meta: { title: '登录', hidden: true }
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
      icon: 'Robot'
    },
    children: [
      {
        path: 'chat',
        name: 'AIChat',
        component: () => import('@/views/ai-management/chat/advanced.vue'),
        meta: {
          title: 'AI智能问答',
          icon: 'ChatDotRound'
        }
      },
      {
        path: 'conversation',
        name: 'AIConversation',
        component: () => import('@/views/ai-management/conversation/index.vue'),
        meta: {
          title: 'AI对话记录',
          icon: 'Document',
          configKey: 'aiConversation'
        }
      },
      {
        path: 'data-source',
        name: 'DataSource',
        component: () => import('@/views/ai-management/data-source/index.vue'),
        meta: {
          title: '数据源配置',
          icon: 'Database',
          configKey: 'dataSource'
        }
      },
      {
        path: 'ai-rule',
        name: 'AIRule',
        component: () => import('@/views/ai-management/ai-rule/index.vue'),
        meta: {
          title: 'AI规则配置',
          icon: 'Setting',
          configKey: 'aiRule'
        }
      },
      {
        path: 'user-management',
        name: 'UserManagement',
        component: () => import('@/views/ai-management/user-management/index.vue'),
        meta: {
          title: '用户管理',
          icon: 'User',
          configKey: 'userManagement',
          permission: 'user:view',
          roles: ['ADMIN']
        }
      },
      {
        path: 'chat-config',
        name: 'AIChatConfig',
        component: () => import('@/views/ai-management/chat-config/index.vue'),
        meta: {
          title: '聊天配置',
          icon: 'Setting',
          configKey: 'chatConfig',
          permission: 'ai:config:edit',
          roles: ['ADMIN']
        }
      }
    ]
  },
  {
    path: '/system',
    component: Layout,
    meta: {
      title: '系统管理',
      icon: 'Tools',
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
    component: () => import('@/views/login/index.vue'),
    meta: {
      title: '用户登录',
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
import { ElMessage } from 'element-plus'

// 路由守卫
router.beforeEach(async (to, from, next) => {
  // 设置页面标题
  if (to.meta.title) {
    document.title = `${to.meta.title} - QMS配置驱动智能管理系统`
  }

  console.log('🚀 路由跳转:', to.path, to.meta)
  console.log('🔍 路由详情:', {
    path: to.path,
    name: to.name,
    matched: to.matched.length,
    meta: to.meta
  })

  // 白名单路由（不需要登录）
  const whiteList = ['/login', '/register', '/404', '/403']

  if (whiteList.includes(to.path)) {
    // 白名单页面直接通过
    if (to.path === '/login' && isLoggedIn()) {
      // 已登录用户访问登录页，重定向到首页
      next('/')
    } else {
      next()
    }
    return
  }

  // 检查登录状态
  if (!isLoggedIn()) {
    console.log('🔐 未登录，重定向到登录页')
    ElMessage.warning('请先登录')
    next(`/login?redirect=${encodeURIComponent(to.fullPath)}`)
    return
  }

  // 暂时跳过Token验证和权限检查，直接允许访问
  console.log('✅ 允许访问:', to.path)
  next()
})

export default router
