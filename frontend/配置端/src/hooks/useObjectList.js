import { flatTreeData } from '@/utils/index.js'
import { queryObjectTree } from '@/views/system-manage/object-manage/api/index.js'
import { Message } from 'element-ui'
import store from '@/store/index.js'

export const useObjectTreeData = async () => {

  const storeObjectData = store.getters.globalData?.objectData

  if (storeObjectData?.tree?.length > 0) return storeObjectData
  const objectTreeData = {
    list: [],
    map: {},
    tree: []
  }
  const { data, success, message } = await queryObjectTree()

  if (!success) return Message.error(message)
  objectTreeData.tree = data
  objectTreeData.list = flatTreeData(data, 'children').map(item => {
    if (item.modelCode.length > 3) {
      item.parentBid = item.modelCode.slice(0, -3)
    } else {
      item.parentBid = '0'
    }
    delete item.children
    objectTreeData.map[item.modelCode] = item

    return item
  })
  await store.dispatch('globalData/setObjectData', objectTreeData)

  return objectTreeData
}
