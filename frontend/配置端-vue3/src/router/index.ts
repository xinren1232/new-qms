import { createRouter, createWebHistory } from 'vue-router'
import type { RouteRecordRaw } from 'vue-router'

// 布局组件
const Layout = () => import('@/layout/index.vue')
const BlankLayout = () => import('@/layout/blank.vue')

// 路由配置
const routes: RouteRecordRaw[] = [
  // 重定向到默认页面
  {
    path: '/',
    redirect: '/system-manage/properties-manage'
  },

  // 登录页面
  {
    path: '/login',
    name: 'Login',
    component: () => import('@/views/login/index.vue'),
    meta: { title: '登录', hidden: true }
  },

  // 系统管理模块
  {
    path: '/system-manage/properties-manage',
    component: Layout,
    meta: { title: '属性维护', icon: 'Tools' },
    children: [
      {
        path: '',
        name: 'properties-manage-page',
        component: () => import('@/views/system-manage/properties-manage/index.vue'),
        meta: { title: '属性维护', icon: 'Tools' }
      }
    ]
  },
  {
    path: '/system-manage/role-manage',
    component: Layout,
    meta: { title: '角色管理', icon: 'UserFilled' },
    children: [
      {
        path: '',
        name: 'role-manage-page',
        component: () => import('@/views/system-manage/role-manage/index.vue'),
        meta: { title: '角色管理', icon: 'UserFilled' }
      }
    ]
  },
  {
    path: '/system-manage/object-manage',
    component: Layout,
    meta: { title: '对象管理', icon: 'Box' },
    children: [
      {
        path: '',
        name: 'object-manage-page',
        component: () => import('@/views/system-manage/object-manage/index.vue'),
        meta: { title: '对象管理', icon: 'Box' }
      }
    ]
  },
  {
    path: '/system-manage/view-manage',
    component: Layout,
    meta: { title: '视图管理', icon: 'View' },
    children: [
      {
        path: '',
        name: 'view-manage-page',
        component: () => import('@/views/system-manage/view-manage/index.vue'),
        meta: { title: '视图管理', icon: 'View' }
      }
    ]
  },
  {
    path: '/system-manage/method-manage',
    component: Layout,
    meta: { title: '方法管理', icon: 'Operation' },
    children: [
      {
        path: '',
        name: 'method-manage-page',
        component: () => import('@/views/system-manage/method-manage/index.vue'),
        meta: { title: '方法管理', icon: 'Operation' }
      }
    ]
  },
  {
    path: '/system-manage/lifecycle-state-manage',
    component: Layout,
    meta: { title: '生命周期状态', icon: 'CircleCheck' },
    children: [
      {
        path: '',
        name: 'lifecycle-state-manage-page',
        component: () => import('@/views/system-manage/lifecycle-state/index.vue'),
        meta: { title: '生命周期状态', icon: 'CircleCheck' }
      }
    ]
  },
  {
    path: '/system-manage/lifecycle-template-manage',
    component: Layout,
    meta: { title: '生命周期模板', icon: 'Document' },
    children: [
      {
        path: '',
        name: 'lifecycle-template-manage-page',
        component: () => import('@/views/system-manage/lifecycle-template/index.vue'),
        meta: { title: '生命周期模板', icon: 'Document' }
      }
    ]
  },
  {
    path: '/system-manage/relation-manage',
    component: Layout,
    meta: { title: '关系管理', icon: 'Connection' },
    children: [
      {
        path: '',
        name: 'relation-manage-page',
        component: () => import('@/views/system-manage/relation-manage/index.vue'),
        meta: { title: '关系管理', icon: 'Connection' }
      }
    ]
  },
  {
    path: '/system-manage/encode-rule-manage',
    component: Layout,
    meta: { title: '编码规则', icon: 'Key' },
    children: [
      {
        path: '',
        name: 'encode-rule-manage-page',
        component: () => import('@/views/system-manage/encode-rule-manage/index.vue'),
        meta: { title: '编码规则', icon: 'Key' }
      }
    ]
  },
  {
    path: '/system-manage/language-manage',
    component: Layout,
    meta: { title: '语言管理', icon: 'ChatDotRound' },
    children: [
      {
        path: '',
        name: 'language-manage-page',
        component: () => import('@/views/system-manage/language-manage/index.vue'),
        meta: { title: '语言管理', icon: 'ChatDotRound' }
      }
    ]
  },

  // 应用中心模块
  {
    path: '/app-center/dictionary-manage',
    component: Layout,
    meta: { title: '数据字典', icon: 'Collection' },
    children: [
      {
        path: '',
        name: 'dictionary-manage-page',
        component: () => import('@/views/dictionary-manage/index.vue'),
        meta: { title: '数据字典', icon: 'Collection' }
      }
    ]
  },

  // AI管理配置模块
  {
    path: '/ai-management',
    component: Layout,
    meta: { title: 'AI管理配置', icon: 'Cpu' },
    children: [
      {
        path: 'model-config',
        name: 'ai-model-config',
        component: () => import('@/views/ai-management-config/model-config.vue'),
        meta: { title: 'AI模型配置', icon: 'Setting' }
      },
      {
        path: 'conversation-config',
        name: 'ai-conversation-config',
        component: () => import('@/views/ai-management-config/conversation-config.vue'),
        meta: { title: '对话配置', icon: 'ChatDotRound' }
      },
      {
        path: 'data-source-config',
        name: 'ai-data-source-config',
        component: () => import('@/views/ai-management-config/data-source-config.vue'),
        meta: { title: '数据源配置', icon: 'DataBoard' }
      },
      {
        path: 'ai-rule-config',
        name: 'ai-rule-config',
        component: () => import('@/views/ai-management-config/ai-rule-config.vue'),
        meta: { title: 'AI规则配置', icon: 'Guide' }
      },
      {
        path: 'app-control',
        name: 'app-control',
        component: () => import('@/views/ai-management-config/app-control.vue'),
        meta: { title: '应用端管控', icon: 'Monitor' }
      },
      {
        path: 'user-control',
        name: 'user-control',
        component: () => import('@/views/ai-management-config/user-control.vue'),
        meta: { title: '用户权限管控', icon: 'Lock' }
      },
      {
        path: 'monitoring',
        name: 'ai-monitoring',
        component: () => import('@/views/ai-management-config/monitoring.vue'),
        meta: { title: 'AI使用监控', icon: 'TrendCharts' }
      },
      {
        path: 'chat-test',
        name: 'chat-test',
        component: () => import('@/views/ai-management-config/chat-test.vue'),
        meta: { title: '聊天测试', icon: 'ChatLineRound' }
      }
    ]
  },

  // 文件管理模块
  {
    path: '/file-manage',
    component: Layout,
    meta: { title: '文件管理', icon: 'Folder' },
    children: [
      {
        path: 'file-library',
        name: 'file-library',
        component: () => import('@/views/file-manage/file-library/index.vue'),
        meta: { title: '文件库', icon: 'FolderOpened' }
      },
      {
        path: 'file-type',
        name: 'file-type',
        component: () => import('@/views/file-manage/file-type/index.vue'),
        meta: { title: '文件类型', icon: 'Document' }
      },
      {
        path: 'copy-exec',
        name: 'copy-exec',
        component: () => import('@/views/file-manage/copy-exec/index.vue'),
        meta: { title: '复制执行规则', icon: 'CopyDocument' }
      },
      {
        path: 'copy-rule',
        name: 'copy-rule',
        component: () => import('@/views/file-manage/copy-rule/index.vue'),
        meta: { title: '复制规则', icon: 'DocumentCopy' }
      },
      {
        path: 'viewer',
        name: 'file-viewer',
        component: () => import('@/views/file-manage/viewer/index.vue'),
        meta: { title: '查看器配置', icon: 'View' }
      },
      {
        path: 'file-list',
        name: 'file-list',
        component: () => import('@/views/file-manage/file-list/index.vue'),
        meta: { title: '文件列表', icon: 'List' }
      }
    ]
  }]

const router = createRouter({
  history: createWebHistory(import.meta.env.BASE_URL),
  routes,
  scrollBehavior: () => ({ top: 0 })
})

// 路由守卫
router.beforeEach(async (to, from, next) => {
  // 设置页面标题
  if (to.meta?.title) {
    document.title = `${to.meta.title} - QMS-AI配置中心`
  } else {
    document.title = 'QMS-AI配置中心'
  }

  // 白名单路由（不需要登录）
  const whiteList = ['/login']

  if (whiteList.includes(to.path)) {
    next()
    return
  }

  // 检查登录状态
  const token = localStorage.getItem('token')
  if (!token) {
    // 未登录，重定向到登录页
    next(`/login?redirect=${encodeURIComponent(to.fullPath)}`)
    return
  }

  // 已登录，允许访问
  next()
})

export default router
