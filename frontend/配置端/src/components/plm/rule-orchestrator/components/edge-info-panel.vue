<template>
  <div class="drawer-box">
    <tr-form v-model="form" :items="formItems" :show-buttons="false" />
  </div>
</template>

<script setup>
import { ref,onMounted,watch, computed } from 'vue'
import { activeCellState } from '../core/state'
import TrForm from '@@/feature/TrForm'
import TrSelect from '@@/feature/TrSelect'
// import RoleSelect from '@@/plm/role-select/role-select.vue'
import { pageQuery } from '@/views/system-manage/method-manage/api'
import { getRoleTree } from '@/views/system-manage/role-manage/api'
import { flatTreeData , deepClone } from '@/utils'
import TrTreeSelect from '@@/plm/tree-selector/index.vue'
import { roleTreeData } from '@@/feature/TrMultiTable/core/dataTier'

// 角色树数据
// const roleTreeData = ref([])
// 角色map数据
const roleMapData = ref({})
const form = ref({
  beforeMethod: [],
  afterMethod: [],
  roleBid: [],
  description: ''
})
const methodsList = ref([])


watch(() => form.value, () => {
  const { cell } = activeCellState.value

  if (cell?.data) cell.data = form.value
}, { deep: true,immediate: true })


watch(() => activeCellState.value, () => {
  if (!activeCellState.value?.type) {
    return
  }
  const { cell } = activeCellState.value

  form.value = deepClone(cell?.data || {})

}, { immediate: true })
// 线条的form表单
const formItems = computed(() => {
  return [
    { label: '角色', prop: 'roleBid', span: 24,
      component: TrTreeSelect,
      attrs: { sourceData: roleTreeData.value, multiple: true, title: '角色选择',type: 'formItem',
        columns: [{ field: 'code','title': '角色编码' },{ field: 'enableFlag','title': '状态' }],valueField: 'code',keyField: 'bid' ,effect: 'edit' } },
    { label: '前置方法', prop: 'beforeMethod', span: 24,
      component: TrSelect,attrs: { data: methodsList.value,multiple: true } },
    { label: '后置方法', prop: 'afterMethod', span: 24,
      component: TrSelect,attrs: { data: methodsList.value,multiple: true } },
    { label: '说明', prop: 'description', span: 24, attrs: { type: 'textarea' } }
  ]
})

// 查询方法列表
const queryMethodList = async () => {
  const { data, success } = await pageQuery({ page: 1,pageSize: 1000 })

  if (success) {
    methodsList.value = data?.data || []
    methodsList.value.forEach(item => {
      item.label = item.name
      item.value = item.bid
    })
  }
}
// eslint-disable-next-line no-unused-vars
const getRoleTreeData = async () => {
  const { data } = await getRoleTree()

  roleTreeData.value = data
  // bid 作为key,name 作为value
  roleMapData.value = flatTreeData(data,'children').reduce((acc, cur) => {
    acc[cur.bid] = cur.name

    return acc
  }, {})
}

// getRoleTreeData()

onMounted(() => {
  queryMethodList()
  // form.value = activeCellState.value?.cell ?? {label:''}
})

</script>
