<template>
  <div class="theme-switcher">
    <el-dropdown @command="handleThemeChange" trigger="click">
      <el-button circle :icon="currentThemeIcon" size="small" />
      <template #dropdown>
        <el-dropdown-menu>
          <el-dropdown-item 
            v-for="theme in themes" 
            :key="theme.value"
            :command="theme.value"
            :class="{ 'is-active': currentTheme === theme.value }"
          >
            <el-icon><component :is="theme.icon" /></el-icon>
            <span>{{ theme.label }}</span>
          </el-dropdown-item>
        </el-dropdown-menu>
      </template>
    </el-dropdown>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { Sunny, Moon, Monitor } from '@element-plus/icons-vue'
import { ElMessage } from 'element-plus'

// 主题配置
const themes = [
  { value: 'light', label: '亮色主题', icon: Sunny },
  { value: 'dark', label: '暗色主题', icon: Moon },
  { value: 'auto', label: '跟随系统', icon: Monitor }
]

// 当前主题
const currentTheme = ref('light')

// 当前主题图标
const currentThemeIcon = computed(() => {
  const theme = themes.find(t => t.value === currentTheme.value)
  return theme ? theme.icon : Sunny
})

// 应用主题
const applyTheme = (theme) => {
  const root = document.documentElement
  
  if (theme === 'auto') {
    // 跟随系统主题
    const prefersDark = window.matchMedia('(prefers-color-scheme: dark)').matches
    root.setAttribute('data-theme', prefersDark ? 'dark' : 'light')
    
    // 监听系统主题变化
    window.matchMedia('(prefers-color-scheme: dark)').addEventListener('change', (e) => {
      if (currentTheme.value === 'auto') {
        root.setAttribute('data-theme', e.matches ? 'dark' : 'light')
      }
    })
  } else {
    root.setAttribute('data-theme', theme)
  }
  
  // 保存到本地存储
  localStorage.setItem('qms-theme', theme)
  currentTheme.value = theme
}

// 处理主题切换
const handleThemeChange = (theme) => {
  applyTheme(theme)
  
  const themeLabel = themes.find(t => t.value === theme)?.label
  ElMessage.success(`已切换到${themeLabel}`)
}

// 初始化主题
onMounted(() => {
  const savedTheme = localStorage.getItem('qms-theme') || 'light'
  applyTheme(savedTheme)
})
</script>

<style lang="scss" scoped>
.theme-switcher {
  .el-dropdown-menu__item {
    display: flex;
    align-items: center;
    gap: 8px;
    
    &.is-active {
      color: var(--el-color-primary);
      background-color: var(--el-color-primary-light-9);
    }
  }
}
</style>
