export const spacePublicMenus = () => [
  {
    bid: 'config',
    name: '空间配置',
    type: 'config',
    icon: 'box-3d',
    modelCode: 'config',
    children: [
      { bid: 'base-info', name: '基本信息', type: 'config', icon: 'info', modelCode: 'base-info' },
      { bid: 'application-manage', name: '应用管理', type: 'config', icon: 'archives', modelCode: 'application-manage' },
      { bid: 'role-auth', name: '角色&权限', type: 'config', icon: 'lock', modelCode: 'role-auth' }
    ]
  }

  // {
  //   bid: 'space-manage',
  //   name: '空间管理',
  //   type: 'config',
  //   icon: 'settings',
  //   modelCode: 'space-manage',
  //   nodeKey: '/space-manage'
  //   // children: []
  // }
]
