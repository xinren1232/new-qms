const routes = {
  name: 'system-manage',
  title: '系统管理',
  class: 'bussiness',
  icon: 'systemManage',
  meta: { title: '系统管理' },
  children: [
    {
      path: 'properties-manage',
      name: 'properties-manage',
      component: () => import('@/views/system-manage/properties-manage'),
      meta: { title: '属性维护', icon: 'menu-att-maintenance' }
    }
  ]
}

export default routes
