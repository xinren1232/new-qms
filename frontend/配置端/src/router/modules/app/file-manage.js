const routes = {
  title: '文件管理',
  path: '/file-manage',
  component: () => import('@/layout/default/index.vue'),
  meta: { icon: 'files' },
  redirect: '/file-manage/file-library',
  children: [
    {
      path: 'file-library',
      name: 'file-manage-file-library',
      component: () => import('@/views/file-manage/file-library/index.vue'),
      title: '文件库',
      meta: { title: '文件库',icon: 'file-storage' }
    },
    {
      path: 'file-library/:bid',
      hidden: true,
      name: 'file-manage-file-library-edit',
      component: () => import('@/views/file-manage/file-library/add-or-edit.vue'),
      meta: { title: '文件库' }
    },

    {
      path: 'file-type',
      name: 'file-manage-file-type',
      component: () => import('@/views/file-manage/file-type/index.vue'),
      title: '文件类型',
      meta: { title: '文件类型',icon: 'file-type' }
    },
    {
      path: 'file-type/:bid',
      hidden: true,
      name: 'file-manage-file-type-edit',
      component: () => import('@/views/file-manage/file-type/add-or-edit.vue'),
      meta: { title: '文件类型' }
    },

    {
      path: 'copy-rule',
      name: 'file-manage-copy-rule',
      component: () => import('@/views/file-manage/copy-rule/index.vue'),
      title: '复制规则',
      meta: { title: '复制规则',icon: 'copy-rule' }
    },
    {
      path: 'copy-rule/:bid',
      hidden: true,
      name: 'file-manage-copy-rule-edit',
      component: () => import('@/views/file-manage/copy-rule/add-or-edit.vue'),
      meta: { title: '复制规则' }
    },

    {
      path: 'copy-exec',
      name: 'file-manage-copy-exec',
      component: () => import('@/views/file-manage/copy-exec/index.vue'),
      title: '复制执行',
      meta: { title: '复制执行', icon: 'copy-exec' }
    },

    {
      path: 'viewer',
      name: 'file-manage-viewer',
      component: () => import('@/views/file-manage/viewer/index.vue'),
      title: '查看器',
      meta: { title: '查看器',icon: 'viewer' }
    },
    {
      path: 'viewer/:bid',
      hidden: true,
      name: 'file-manage-viewer-edit',
      component: () => import('@/views/file-manage/viewer/add-or-edit.vue'),
      meta: { title: '查看器' }
    },
    {
      path: 'file-list',
      name: 'file-manage-file-list',
      component: () => import('@/views/file-manage/file-list/index.vue'),
      title: '文件列表',
      meta: { title: '文件列表',icon: 'file-list' }
    },
    {
      path: 'file-list/add',
      hidden: true,
      name: 'file-manage-file-list-add',
      component: () => import('@/views/file-manage/file-list/add-or-edit.vue'),
      meta: { title: '文件新增' }
    },
    {
      path: 'file-list/:id',
      hidden: true,
      name: 'file-manage-file-list-edit',
      component: () => import('@/views/file-manage/file-list/add-or-edit.vue'),
      meta: { title: '文件编辑' }
    }
  ]
}

export default routes
