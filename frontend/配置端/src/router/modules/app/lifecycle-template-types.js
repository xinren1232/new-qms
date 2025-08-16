const routes = {
  title: '生命周期模板',
  path: '/lifecycle-template-manage/action',
  hidden: true,
  component: () => import('@/layout/default/index.vue'),
  children: [
    {
      path: 'add',
      name: 'lifecycle-template-add',
      component: () => import('@/views/system-manage/lifecycle-template/components/add-edit-panel.vue'),
      hidden: true,
      meta: { name: '新增生命周期模板', icon: 'menu-bom-template', autoTitle: true }
    },
    {
      path: 'detail/:id/:version',
      name: 'lifecycle-template-detail',
      component: () => import('@/views/system-manage/lifecycle-template/components/add-edit-panel.vue'),
      hidden: true,
      meta: { name: '生命周期模板详情', icon: 'menu-bom-template', autoTitle: true }
    }
  ]
}

export default routes
