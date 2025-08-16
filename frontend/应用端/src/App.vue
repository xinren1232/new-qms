<template>
  <div id="app" class="qms-app">
    <router-view />
  </div>
</template>

<script setup>
import { onMounted } from 'vue'
import { useConfigStore } from '@/stores/config'
import { useAuthStore } from '@/stores/auth'

const configStore = useConfigStore()
const authStore = useAuthStore()

onMounted(async () => {
  // 初始化认证（创建匿名用户）
  await authStore.initializeAuth()

  // 初始化配置
  configStore.initializeConfig()
})
</script>

<style lang="scss">
#app {
  font-family: 'Helvetica Neue', Helvetica, 'PingFang SC', 'Hiragino Sans GB', 'Microsoft YaHei', '微软雅黑', Arial, sans-serif;
  -webkit-font-smoothing: antialiased;
  -moz-osx-font-smoothing: grayscale;
  height: 100vh;
  margin: 0;
  padding: 0;
}

* {
  box-sizing: border-box;
}

body {
  margin: 0;
  padding: 0;
  background-color: #f5f7fa;
}

.qms-app {
  height: 100vh;
  overflow: hidden;
}
</style>
