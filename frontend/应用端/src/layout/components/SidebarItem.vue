<template>
  <div v-if="!item.meta?.hidden">
    <!-- 有子菜单的情况 -->
    <el-sub-menu
      v-if="hasChildren"
      :index="resolveRoutePath(item.path)"
      popper-append-to-body
    >
      <template #title>
        <el-icon v-if="item.meta?.icon">
          <component :is="item.meta.icon" />
        </el-icon>
        <span>{{ item.meta?.title }}</span>
      </template>

      <sidebar-item
        v-for="child in visibleChildren"
        :key="child.path"
        :item="child"
        :base-path="resolvePath(props.basePath, item.path)"
      />
    </el-sub-menu>

    <!-- 单个菜单项 -->
    <el-menu-item
      v-else
      :index="resolveRoutePath(item.path)"
    >
      <el-icon v-if="item.meta?.icon">
        <component :is="item.meta.icon" />
      </el-icon>
      <template #title>
        <span>{{ item.meta?.title }}</span>
      </template>
    </el-menu-item>
  </div>
</template>

<script setup>
import { computed } from 'vue'
import { isExternal } from '@/utils/validate'
import { getUser } from '@/utils/auth'

// 简单的路径解析函数
const resolvePath = (basePath, routePath) => {
  if (isExternal(routePath)) {
    return routePath
  }
  if (isExternal(basePath)) {
    return basePath
  }
  // 简单的路径拼接
  if (routePath.startsWith('/')) {
    return routePath
  }

  // 确保basePath不为空
  if (!basePath) {
    return '/' + routePath
  }

  // 拼接路径
  const cleanBasePath = basePath.endsWith('/') ? basePath.slice(0, -1) : basePath
  return cleanBasePath + '/' + routePath
}

const props = defineProps({
  item: {
    type: Object,
    required: true
  },
  basePath: {
    type: String,
    default: ''
  }
})

// 检查菜单项权限 - 暂时简化
const hasMenuPermission = (menuItem) => {
  const user = getUser()
  if (!user) return false

  // 只对特定的管理员功能进行权限检查
  if (menuItem.meta?.roles && menuItem.meta.roles.includes('ADMIN')) {
    return user.roles && user.roles.includes('ADMIN')
  }

  return true
}

// 可见的子菜单
const visibleChildren = computed(() => {
  return props.item.children?.filter(child =>
    !child.meta?.hidden && hasMenuPermission(child)
  ) || []
})

// 是否有子菜单
const hasChildren = computed(() => {
  return visibleChildren.value.length > 0
})

// 解析路径 - 使用上面定义的函数
const resolveRoutePath = (routePath) => {
  const resolved = resolvePath(props.basePath, routePath)
  console.log('路径解析:', {
    basePath: props.basePath,
    routePath: routePath,
    resolved: resolved,
    itemTitle: props.item.meta?.title
  })
  return resolved
}
</script>
