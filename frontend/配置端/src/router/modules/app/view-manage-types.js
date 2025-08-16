const routes = {
  title: '视图管理',
  path: '/view-manage/action',
  hidden: true,
  component: () => import('@/layout/blank/index.vue'),
  children: [
    {
      path: 'designer/:id',
      name: 'view-designer',
      component: () => import('@/views/system-manage/view-manage/components/view-designer.vue'),
      hidden: true,
      meta: { name: '视图设计', icon: 'menu-bom-template', autoTitle: true }
    },
    {
      path: 'preview/:id',
      name: 'view-preview',
      component: () => import('@/views/system-manage/view-manage/components/preview-view.vue'),
      hidden: true,
      meta: { name: '视图查看', icon: 'menu-bom-template', autoTitle: true }
    }
  ]
}

export default routes
