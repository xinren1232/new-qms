<template>
  <div class="relative select-none">
    <div v-if="showAllNode" id="stencil" class="stencil-container w-220px min-w-220px rounded-lg absolute w-full h-full flex flex-col bg-white rounded b-(1 solid $info-light)">
      <div class="title">选择节点</div>
      <div class="p-1.5">
        <el-input v-model="searchValue" clearable class="margin-bottom" placeholder="请输入节点名" @input="onChange" />
      </div>

      <div class="flex-1 overflow-scroll">
        <div class="node-box pt-1.5 px-1.5">
          <div
            v-for="item in cashStateList"
            :key="item.bid"
            tabindex="-1"
            class="node-item flex items-center justify-center text-xs truncate w-full text-center rounded-full py-1.5 px-2.5 active:cursor:grabbing cursor-pointer font-medium"
            :class="[{ 'point-node': [0, 2].includes(item.nodeType)}, `${item.type}-node`]"
            :title="item.nodeName"
            @mousedown="createGraphNode(item, $event)"
          >
            {{ item.name }}
          </div>
        </div>
      </div>
    </div>

    <div v-if="showAllNode" class="open-close-btn open" @click="onBtnClick">
      <i class="el-icon-arrow-left" />
    </div>
    <div v-else class="open-close-btn close" @click="onBtnClick">
      <i class="el-icon-arrow-right" />
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted, watch } from 'vue'
import { createGraphNode } from '../core/connecting'
import { deepClone } from '@/utils'

const showAllNode = ref(true)

const props = defineProps({
  stateList: {
    type: Array,
    default: () => ([])
  }
})

watch(() => props.stateList, (val) => {
  cashStateList.value = deepClone(val)
})
const cashStateList = ref([])
const searchValue = ref('')
const onBtnClick = () => {
  showAllNode.value = !showAllNode.value
}

const onChange = (value) => {
  if (!value) {
    cashStateList.value = props.stateList

    return
  }
  cashStateList.value = props.stateList.filter((item) => item.name.toLowerCase().includes(value.toLowerCase()))
}

onMounted(() => {
  cashStateList.value = deepClone(props.stateList)
})
</script>

<style scoped lang="scss">
.stencil-container{
  z-index: 1;
  top: 0;
  left: 0;
  box-shadow: 0 0 10px 0 rgba(0,0,0,.1) ;

  .title{
    height :40px;
    display: flex;
    align-items: center;
    justify-content: center;
    font-size: 16px;
    font-weight: 500;
  }
  .node-box{
    display: grid;
    grid-template-columns: repeat(2, 1fr); /* 设置3列 */
    grid-template-rows: repeat(2, 1fr); /* 设置3行 */
    grid-gap: 10px;

    .node-item {
      min-width: 0;
      border: 1px solid #cdcdcd;
      background-color: #fff;
      max-width: 120px;

      &.point-node {
        background-color: var(--primary-9);
        border-color: var(--primary);
      }
    }

    .judge-node{
      transform: rotate(45deg);
      position :relative;
      width :38px;
      height :38px;
    }
    .common-node{
      color: var(--primary);
      font-weight:500;
      background: transparent !important;
      font-size :11px;
      border: 1px dashed var(--primary);
      cursor: pointer;
    }
  }

}
.open-close-btn{
  position: absolute;
  z-index: 100;
  top: 47%;
  cursor: pointer;
  transition:none !important;
  background-color: #cccbcb;
  width: 10px;
  height: 70px;
  left: 216px;
  border-radius: 12px;
  display: flex;
  justify-content: center;
  align-items: center;
  color: #FFFFFF;
  &:hover{
    background-color: var(--primary);
  }
  &.close{
    left: 2px;
  }
}
</style>
