<template>
  <details
    v-if="showHead && (header || $slots.header)"
    ref="detailsRef"
    class="tr-card flex flex-col overflow-hidden"
    :class="{ collapsable: showIcon }"
    :open="expand"
    @click.stop="onPanelClick"
  >
    <summary class="tr-card--header select-none font-medium pl-4 flex items-center b-b-1 b-b-dashed b-transparent">
      <i
        v-if="showIcon"
        class="tr-card--header-icon absolute mr-1.5 el-icon-arrow-right font-bold"
      />
      <div class="flex-1" :class="{'pl-5': showIcon}">
        <slot name="header">
          <span v-html="header" />
        </slot>
      </div>
    </summary>
    <div class="tr-card--container overflow-overlay relative p-4" :class="cardContainerClass">
      <slot />
    </div>
  </details>

  <div v-else class="tr-card--container overflow-overlay relative p-4" :class="cardContainerClass">
    <slot />
  </div>
</template>

<script setup>
import { shallowRef } from 'vue'

import EventBus from '@/utils/event.js'

const emits = defineEmits(['toggle'])
const props = defineProps({
  cardContainerClass: {
    type: String,
    default: ''
  },
  header: {
    type: String,
    default: ''
  },
  expand: {
    type: Boolean,
    default: true
  },
  showIcon: {
    type: Boolean,
    default: true
  },
  showHead: {
    type: Boolean,
    default: true
  },
  margin: {
    type: Boolean,
    default: true
  },
  eventBusName: {
    type: String,
    default: ''
  }
})

const detailsRef = shallowRef(null)
const onPanelClick = (ev) => {
  if (!props.showIcon) {
    ev.preventDefault()

    return
  }
  // 获取元素上的 open 属性
  emits('toggle', !detailsRef.value.open)
  props.eventBusName && EventBus.$emit(props.eventBusName, !detailsRef.value.open)
}
</script>

<script>
export default {
  name: 'TrCard'
}
</script>

<style lang="scss" scoped>
.tr-card {
  border-radius: var(--radius);
  border: 1px solid var(--info-light);
  background: var(--white);

  &--header {
    height: 45px;
    color: #424242;
  }

  // 组件是否可展开/收起
  &.collapsable {
    .tr-card--header {
      cursor: pointer;
    }

    &[open] > .tr-card--header .tr-card--header-icon {
      transform: rotate(90deg);
    }
  }

  // 当开启了折叠/展开功能, 并且处于打开状态时
  &[open] > .tr-card--header {
    border-color: #e0e0e0;
  }

  & &--container {
    flex: 1;
    border: none;
  }
}

.tr-card--container {
  background: var(--white);
  border-radius: var(--radius);
  border: 1px solid var(--info-light);
}

.tr-card:not(:last-child) {
  margin-bottom: 10px;

  .tr-card--container {
    margin-bottom: 0;
  }
}

.tr-card--container:not(:last-of-type) {
  margin-bottom: 10px;
}
</style>
