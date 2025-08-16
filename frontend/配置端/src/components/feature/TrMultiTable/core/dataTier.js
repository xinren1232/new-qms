// 角色tree 数据层
import { ref } from 'vue'
import { flatTreeData,setUserStore } from './../utils'
import { getRoleTree,queryObjectTree } from '../api/extendData'
import { mergeApiNeedUserInfo } from '@/utils'


const roleDataLoaded = ref(false)
const objectDataLoaded = ref(false)
const roleTreeData = ref({
  list: [],
  map: {},
  tree: {}
})

const queryRoleTreeData = async () => {
  const { data } = await getRoleTree()
  const _data = data.filter(item => item.enableFlag === 1)

  roleTreeData.value.tree = _data
  roleTreeData.value.list = flatTreeData(_data,'children')
  const jobNumbers = [...new Set([...roleTreeData.value.list.map(item => item.createdBy)])]

  setUserStore(jobNumbers)
  roleTreeData.value.map = roleTreeData.value.list.reduce((prev, item) => {
    prev[item.code] = item

    return prev
  }, {})
  roleDataLoaded.value = true
}

// queryRoleTreeData()

const objectTreeData = ref({
  list: [],
  map: {},
  tree: {}
})

const queryObjectTreeData = async () => {
  const { data } = await mergeApiNeedUserInfo(queryObjectTree(), ['createdBy'])

  objectTreeData.value.tree = data
  objectTreeData.value.list = flatTreeData(data,'children').map(item => {
    if (item.modelCode.length > 3)item.parentBid = item.modelCode.slice(0,-3)
    else item.parentBid = '0'
    delete item.children
    objectTreeData.value.map[item.modelCode] = item

    return item
  })
  objectDataLoaded.value = true
}

queryObjectTreeData()

export { roleTreeData,objectTreeData,queryRoleTreeData,roleDataLoaded,objectDataLoaded }
