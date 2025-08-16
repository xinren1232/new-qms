<template>
  <div class="layout-container w-full h-full overflow-hidden" :data-show="visible">
    <main class="h-full transition-padding p-$container-padding">
      <transition name="el-fade-in" mode="out-in">
        <router-view class="h-full overflow-hidden" />
      </transition>
    </main>

    <div class="fixed left-0 top-0 bottom-0 z-2">
      <transition v-if="render" name="el-zoom-side-left" mode="out-in">
        <aside
          v-show="visible"
          class="h-full flex flex-col b-(r-1 r-solid $border-color) w-$sidebar-width bg-$white"
        >
          <router-link class="pt-3 pb-1.5 text-center" to="/">
            <img class="app-logo" src="@/assets/images/app/logo.png" width="140" alt="软件全生命周期管理平台">
          </router-link>

          <app-tree />
          <login-user />
        </aside>
      </transition>

      <i class="collapse-icon cursor-pointer el-icon-arrow-right absolute top-50%" @click="toggleSidebar" />
    </div>
  </div>
</template>

<script setup>
import { useDebounceFn } from '@vueuse/core'
import { useSystemTheme } from '../composables/system-theme.js'

import { useLazyShow } from '@/composables/lazy-show.js'
import EventBus from '@/utils/event.js'

const LoginUser = defineAsyncComponent(() => import('../components/LoginUser.vue'))
const AppTree = defineAsyncComponent(() => import('../components/AppTree.vue'))

const {
  open,
  close,
  render,
  visible
} = useLazyShow({
  initialValue: !localStorage.getItem('sidebar-visible') || localStorage.getItem('sidebar-visible') === 'true'
})

// 切换侧边栏的时候通知页面, 用于重置表格宽度
const debounceToggleEmitter = useDebounceFn((isOpen) => {
  EventBus.$emit('sidebar-resize', isOpen)
}, 500)

const toggleSidebar = () => {
  localStorage.setItem('sidebar-visible', !visible.value)

  visible.value ? close() : open()
  debounceToggleEmitter()
}

// 所以不会执行主题初始化的操作, 所以必须放在这里让确保执行
// 主题放在这里的原因是因为在初始情况下个人信息是隐藏的不会渲染
provide('system-theme', useSystemTheme())
</script>

<style lang="scss" scoped>
.layout-container {
  --sidebar-width: 220px;
  --border-color: #e8eaec;
  --container-padding: 12px 12px 12px 20px;
}

.layout-container[data-show="true"] {
  --container-padding: 12px 12px 12px calc(var(--sidebar-width) + 22px);
}

.app-logo {
  transform: translateX(-var(--sidebar-width));
  filter: drop-shadow(-var(--sidebar-width) 0 0 var(--primary));
}

.collapse-icon {
  left: 0;
  padding: 32px 0;
  width: 12px;
  font-weight: bold;
  border-radius: 0 6px 6px 0;
  transform: translate(0, -65%);
  border-style: solid;
  border-width: 1px 1px 1px 0;
  border-color: var(--info-7);
  background-color: var(--info-9);
  transition: transform 200ms;
  color: var(--primary);
  z-index: 2;

  &:hover {
    background-color: var(--info-8)
  }

  &:active {
    background-color: var(--info-7)
  }

  &::before{
    display: block;
    margin-left: -1px
  }
}

[data-show="true"] .collapse-icon {
  left: -1px;
  width: 14px;
  transform: translate(var(--sidebar-width), -65%);

  &::before {
    transform: rotateY(180deg);
  }
}
</style>
