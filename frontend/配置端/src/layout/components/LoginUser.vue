<script setup>
import { useLazyShow } from '@/composables/lazy-show.js'
import store from '@/store/index.js'
import CustomTheme from './CustomTheme.vue'
import vClickOutside from 'element-ui/src/utils/clickoutside.js'

import i18n from '@/i18n/index.js'

const lang = computed(() => i18n.locale)

const langList = [
  { code: 'zh-CN', name: '简体中文' },
  { code: 'en-US', name: 'English' }
]

function onLanguageChange (lang) {
  i18n.locale = lang
  store.dispatch('app/setLanguage', lang)
}

function logout () {
  store.dispatch('user/logout')
}

const {
  open,
  close,
  render,
  visible
} = useLazyShow({ initialValue: false })

const currentUser = computed(() => store.getters.user)
</script>

<template>
  <div v-click-outside="close" class="relative pl-6 py-3 b-(t-1 t-solid $border-color)">
    <div class="flex items-center" @click="open">
      <img
        :src="currentUser.avatar"
        :data-show="visible"
        class="relative z-2 rounded-full pointer-events-none cursor-pointer mr-2 outline-(2 solid $primary)"
        width="36"
        height="36"
        loading="lazy"
        fetchpriority="low"
        alt=""
      >
      <div class="h-10 pt-.5 mb-1.5 pointer-events-none">
        <p class="font-medium m-0">{{ currentUser.name }}<!--({{ currentUser.enName }})--></p>
        <span class="text-xs mt--1 op-60">{{ currentUser.deptName }}</span>
      </div>
    </div>

    <transition name="el-fade-in-linear">
      <ul
        v-if="render"
        v-show="visible"
        class="el-dropdown-menu el-popper w-56 el-dropdown-menu--mini !z-1 !p-1.5 !pt-2 !pr-2 !top-unset !left-full bottom-3"
        style="--dropdown-item-height:30px;--dropdown-item-font-size:13px;--dropdown-item-padding:10px"
      >
        <li class="flex items-center text-13px pl-2.5 mb-1.5">
          <span class="min-w-55px">语言:</span>

          <el-select v-model="lang" placeholder="请选择界面语言" :popper-append-to-body="false" @change="onLanguageChange">
            <el-option v-for="item in langList" :key="item.code" :label="item.name" :value="item.code" />
          </el-select>
        </li>

        <li class="flex items-center text-13px pl-2.5 mb-1.5">
          <span class="min-w-55px">主题:</span>

          <custom-theme />
        </li>
        <li class="el-dropdown-menu__item not-last-mb-2px" @click="logout">退出登录</li>
      </ul>
    </transition>
  </div>
</template>
