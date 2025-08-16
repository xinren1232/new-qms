<template>
  <div>
    <div class="search-box">
      <el-input v-model="searchText" placeholder="请输入对象名称" clearable @keyup.enter.native="handleSearch" />
      <el-tooltip content="收起">
        <tr-svg-icon icon-class="collapse-left" @click="handleCollapse" />
      </el-tooltip>
    </div>
    <div v-loading="loading" class="tree-box">
      <el-tree
        ref="treeRef"
        :data="treeData"
        :props="defaultProps"
        node-key="bid"
        highlight-current
        :filter-node-method="filterNode"
        :expand-on-click-node="false"
        @node-click="handleNodeClick"
      >
        <template #default="{node,data}">
          <span class="span-ellipsis">
            <span v-if="data.children && data.children.length" style="display: flex;align-items: center">
              <tr-svg-icon v-if="node.expanded" icon-class="t-folder-minus" />
              <tr-svg-icon v-else icon-class="t-folder-plus" />
            </span>
            <tr-svg-icon v-else icon-class="folder-fill" />
            <span :title="data.name" class="name">{{ data.name }}</span>
          </span>
        </template>

      </el-tree>
    </div>
  </div>
</template>

<script setup>
import { ref, watch, nextTick } from 'vue'
import { Message } from 'element-ui'
import { queryObjectTree } from '@/views/system-manage/object-manage/api'
import { handleCollapse } from './collapse-controller'

const defaultProps = {
  children: 'children',
  label: 'name'
}
const treeRef = ref(null)
const searchText = ref('')

const emits = defineEmits(['node-click'])

watch(() => searchText.value, (val) => {
  treeRef.value.filter(val)
  if (!val) {
    // 全部收起
    setAllFold()
  }
})
const loading = ref(false)
const treeData = ref([])


const handleNodeClick = (data) => {
  emits('node-click', data)
}
const getObjectTree = async () => {
  loading.value = true
  const { data,success,message } = await queryObjectTree().finally(() => {
    loading.value = false
  })

  if (!success) {
    Message.error(message)

    return
  }
  treeData.value = data
  nextTick(() => {
    treeRef.value.setCurrentKey('material')
    emits('node-click', treeRef.value.getCurrentNode())
  })

}

getObjectTree()

const handleSearch = () => {
  console.log(searchText.value)
}
const filterNode = (value, data) => {
  if (!value) return true

  return data.name.indexOf(value) !== -1
}

// 全部收起
const setAllFold = () => {
  for (let i = 0; i < treeRef.value.store._getAllNodes().length; i++) {
    treeRef.value.store._getAllNodes()[i].expanded = false
  }
}

</script>

<style scoped lang="scss">
.span-ellipsis{
  display: flex;
  align-items: center;
  width: 100%;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
  .name{
    font-weight: bold;
    font-size: 12px;
  }
  .tr-svg-icon{
    margin-right: 3px;
    color: var(--primary);
    font-size: 14px;
  }
}
.search-box{
  margin-bottom: 10px;
  padding: 0 6px;
  display: flex;
  align-items: center;
  .tr-svg-icon{
    margin-left: 5px;
    color: var(--primary);
    font-size: 30px !important;
    cursor: pointer;
    padding: 0 2px;
    &:hover {
      background-color: #EAEDEC;
      border-radius: 4px;
    }
    &:active {
      background-color: #dee1e0;
    }
  }
}
.tree-box{
  height: calc(100vh - 50px);
  overflow: auto;
}

//is-focusable
::v-deep.el-tree--highlight-current .el-tree-node.is-current>.el-tree-node__content {
  background-color: var(--primary-8);
  color: var(--primary-3);
  font-weight: bold;
  border-radius: 2px;
  border: 1px dashed var(--primary-6)
}
::v-deep.el-tree--highlight-current .el-tree-node{
  border: 1px dashed transparent;
}

</style>
