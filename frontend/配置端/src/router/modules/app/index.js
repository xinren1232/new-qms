const Layout = () => import('@/layout/default')
const Blank = () => import('@/layout/blank')

import i18n from '@/i18n'

// import MyApp from '@/views/application-manage/index.vue'
import MyApp from '@/views/system-manage/properties-manage/index.vue'

import lifecycleTemplateTypes from './lifecycle-template-types'
import viewManageTypes from './view-manage-types'
import fileManageRoutes from './file-manage.js'

// function _generateRoutes(route) {
//   return {
//     path: `${route.path ?? '/' + route.name}`,
//     name: route.name,
//     component: window.$wujie ? Blank : Layout,
//     outPath: route?.outPath,
//     meta: {
//       title: route.title,
//       icon: route.icon
//     },
//     children: route.children
//   }
// }

const appRoutes = [
  // 登录页面路由
  {
    path: '/login',
    name: 'Login',
    component: () => import('@/views/login/index.vue'),
    meta: { title: '登录', hidden: true }
  },
  // {
  //   path: '/',
  //   name: 'home',
  //   component: window.$wujie ? Blank : Layout,
  //   redirect: '/system-manage/properties-manage',
  //   meta: { class: 'bussiness', icon: 'application', title: '我的应用' },
  //   children: [
  //     {
  //       path: '',
  //       name: '',
  //       component: MyApp,
  //       meta: { title: '我的应用', class: 'application' }
  //     }
  //   ]
  // },
  {
    path: '/system-manage/properties-manage',
    component: window.$wujie ? Blank : Layout,
    meta: { title: i18n.t('menu.property'), icon: 'property' },
    children: [
      {
        path: '',
        name: 'properties-manage-page',
        component: MyApp,
        meta: { title: i18n.t('menu.property'),icon: 'application' }
      }
    ]
  },
  {
    path: '/system-manage/role-manage',
    component: window.$wujie ? Blank : Layout,
    meta: { title: i18n.t('menu.role'), icon: 'role' },
    children: [
      {
        path: '',
        name: 'role-manage-page',
        component: () => import('@/views/system-manage/role-manage'),
        meta: { title: i18n.t('menu.role'), icon: 'role' }
      }
    ]
  },
  {
    path: '/system-manage/object-manage',
    component: window.$wujie ? Blank : Layout,
    meta: { title: i18n.t('menu.object'), icon: 'object' },
    children: [
      {
        path: '',
        name: 'object-manage-page',
        component: () => import('@/views/system-manage/object-manage'),
        meta: { title: i18n.t('menu.object'), icon: 'object' }
      }
    ]
  },
  {
    path: '/system-manage/view-manage',
    component: window.$wujie ? Blank : Layout,
    meta: { title: i18n.t('menu.view'), icon: 'view' },
    children: [
      {
        path: '',
        name: 'view-manage-page',
        component: () => import('@/views/system-manage/view-manage/index.vue')
      }
    ]
  },
  {
    path: '/system-manage/method-manage',
    meta: { title: i18n.t('menu.method'), icon: 'function' },
    component: window.$wujie ? Blank : Layout,
    children: [
      {
        path: '',
        name: 'method-manage-page',
        component: () => import('@/views/system-manage/method-manage/index.vue')
      }
    ]
  },
  {
    path: '/system-manage/lifecycle-state-manage',
    meta: { title: i18n.t('menu.state'), icon: 'state' },
    component: window.$wujie ? Blank : Layout,
    children: [
      {
        path: '',
        name: 'lifecycle-state-manage-page',
        component: () => import('@/views/system-manage/lifecycle-state/index.vue')
      }
    ]
  },
  {
    path: '/system-manage/lifecycle-template-manage',
    meta: { title: i18n.t('menu.lifecycle'), icon: 'state' },
    component: window.$wujie ? Blank : Layout,
    children: [
      {
        path: '',
        name: 'lifecycle-template-manage-page',
        component: () => import('@/views/system-manage/lifecycle-template/index.vue'),
        meta: { title: i18n.t('menu.lifecycle'), icon: 'lifetep' }
      }
    ]
  },
  {
    path: '/system-manage/relation-manage',
    meta: { title: i18n.t('menu.relation'), icon: 'relation' },
    component: window.$wujie ? Blank : Layout,
    children: [
      {
        path: '',
        name: 'relation-manage-page',
        component: () => import('@/views/system-manage/relation-manage/index.vue'),
        meta: { title: i18n.t('menu.relation'), icon: 'relation' }
      }
    ]
  },
  {
    path: '/system-manage/encode-rule-manage',
    meta: { title: i18n.t('menu.encode'), icon: 'code' },
    component: window.$wujie ? Blank : Layout,
    children: [
      {
        path: '',
        name: 'encode-rule-manage-page',
        component: () => import('@/views/system-manage/encode-rule-manage/index.vue'),
        meta: { title: i18n.t('menu.encode'), icon: 'encode' }
      }
    ]
  },
  {
    path: '/system-manage/language-manage',
    meta: { title: i18n.t('menu.language'), icon: 'lang' },
    component: window.$wujie ? Blank : Layout,
    children: [
      {
        path: '',
        name: 'language-manage-page',
        component: () => import('@/views/system-manage/language-manage/index.vue'),
        meta: { title: i18n.t('menu.language'), icon: 'lang' }
      }
    ]
  },
  // {
  //   path: '/system-manage/app-manage',
  //   meta: { title: i18n.t('menu.application'), icon: 'app' },
  //   component: window.$wujie ? Blank : Layout,
  //   children: [
  //     {
  //       path: '',
  //       name: 'app-manage-page',
  //       component: () => import('@/views/app-manage/index.vue'),
  //       meta: { title: i18n.t('menu.application'), icon: 'app' }
  //     }
  //   ]
  // },
  {
    path: '/app-center/dictionary-manage',
    component: Layout,
    meta: { title: i18n.t('menu.dictionary'), icon: 'dict' },
    children: [
      {
        path: '',
        name: 'dictionary-manage-page',
        component: () => import('@/views/dictionary-manage/index.vue'),
        meta: { title: i18n.t('menu.dictionary'), icon: 'dict' }
      }
    ]
  },
  // {
  //   path: '/app-center/space-manage',
  //   name: 'space-manage',
  //   component: Layout,
  //   meta: { title: i18n.t('menu.space'), icon: 'zone' },
  //   children: [
  //     {
  //       path: '',
  //       name: 'space-manage-page',
  //       component: () => import('@/views/space/index.vue')
  //     }
  //   ]
  // },

  // AI管理配置模块 - 新增应用端管控功能
  {
    path: '/ai-management',
    component: window.$wujie ? Blank : Layout,
    meta: { title: 'AI管理配置', icon: 'ai-management' },
    children: [
      {
        path: 'model-config',
        name: 'ai-model-config',
        component: () => import('@/views/ai-management-config/model-config.vue'),
        meta: { title: 'AI模型配置', icon: 'model' }
      },
      {
        path: 'conversation-config',
        name: 'ai-conversation-config',
        component: () => import('@/views/ai-management-config/conversation-config.vue'),
        meta: { title: '对话配置', icon: 'conversation' }
      },
      {
        path: 'data-source-config',
        name: 'ai-data-source-config',
        component: () => import('@/views/ai-management-config/data-source-config.vue'),
        meta: { title: '数据源配置', icon: 'data-source' }
      },
      {
        path: 'ai-rule-config',
        name: 'ai-rule-config',
        component: () => import('@/views/ai-management-config/ai-rule-config.vue'),
        meta: { title: 'AI规则配置', icon: 'rule' }
      },
      {
        path: 'app-control',
        name: 'app-control',
        component: () => import('@/views/ai-management-config/app-control.vue'),
        meta: { title: '应用端管控', icon: 'control' }
      },
      {
        path: 'user-control',
        name: 'user-control',
        component: () => import('@/views/ai-management-config/user-control.vue'),
        meta: { title: '用户权限管控', icon: 'user-control' }
      },
      {
        path: 'monitoring',
        name: 'ai-monitoring',
        component: () => import('@/views/ai-management-config/monitoring.vue'),
        meta: { title: 'AI使用监控', icon: 'monitor' }
      },
      {
        path: 'chat-test',
        name: 'chat-test',
        component: () => import('@/views/ai-management-config/chat-test.vue'),
        meta: { title: '聊天测试', icon: 'chat-test' }
      }
    ]
  },

  lifecycleTemplateTypes,
  viewManageTypes,
  fileManageRoutes
]

export default appRoutes
