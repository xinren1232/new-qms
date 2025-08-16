import { flatTreeData } from '@/utils/index.js'
import { getRoleTree } from '@/views/system-manage/role-manage/api/index.js'
import { Message } from 'element-ui'
import store from '@/store/index.js'

export const useRoleTreeData = async () => {

  const storeRoleData = store.getters.globalData?.roleData

  if (storeRoleData?.list?.length > 0) return storeRoleData
  const roleTreeData = {
    list: [],
    map: {},
    tree: []
  }
  const { data, success, message } = await getRoleTree()

  if (!success) return Message.error(message)
  roleTreeData.tree = data
  roleTreeData.list = flatTreeData(data, 'children')
  roleTreeData.map = roleTreeData.list.reduce((prev, item) => {
    prev[item.code] = item

    return prev
  }, {})

  await store.dispatch('globalData/setRoleData', roleTreeData)

  return roleTreeData
}
