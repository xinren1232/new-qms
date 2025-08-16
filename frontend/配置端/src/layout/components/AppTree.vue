<script setup>
import { nextTick } from 'vue'
import store from '@/store/index.js'
import router from '@/router/index.js'

const IGNORE_PATHS = ['*']

const formatterRoutes = computed(() => {
  const permission_routes = store.getters.permission_routes ?? []

  return permission_routes.reduce((routes, route) => {
    if (route.hidden || IGNORE_PATHS.includes(route.path)) {
      return routes
    }

    if (route.children?.length) {
      // 如果是嵌套路由并且只有一个子路由, 则不显示父路由
      if (route.children.length === 1 && !route.children[0].path) {
        route.children = undefined
      } else {
        route.children = route.children.reduce((acc, item) => {
          if (item.path.startsWith('/') || !route.path || item.hidden) {
            return acc
          }

          acc.push({
            ...item,
            path: route.path + '/' + item.path
          })

          return acc
        }, [])
      }
    }

    routes.push(route)

    return routes
  }, [])
})

function labelGetter({ title, meta }) {
  return meta?.title ?? title
}

const treeRef = shallowRef(null)

function onNodeClick({ path, children }, appTreeNode) {
  if (children?.length) {
    path = children[0].path

    // 如果点击的是包含子级的节点， 则展开或者收起当前节点
    if (appTreeNode.expanded) {
      appTreeNode.collapse()
    } else {
      appTreeNode.expand()
    }
  }

  // 如果路径一致则不跳转, 否则会重置url中的视图参数
  if (route.path === path) {
    return
  }

  router.push({
    path: path,
    query: { viewMode: route.query.viewMode }
  })
}

// 被动激活节点， 可能是由其他组件触发的路由跳转， 但是需要激活当前节点
function onToggleCurrentNode(nodeOrKey = route.path) {
  if (!nodeOrKey || !treeRef.value) {
    return
  }

  if (typeof nodeOrKey !== 'object') {
    nodeOrKey = treeRef.value.getNode(nodeOrKey)
  }

  treeRef.value.setCurrentKey(nodeOrKey)

  // 逐级向上展开所有节点
  while (nodeOrKey?.parent) {
    nodeOrKey.parent.expand()
    nodeOrKey = nodeOrKey.parent
  }
}

const route = useRoute()

watch(() => route.path, () => {
  // 数据刚获取以后页面可能还没渲染出来
  // 等组件内部更新完成后再获取
  nextTick(() => {
    onToggleCurrentNode()
  })
}, { immediate: true })
</script>

<template>
  <div class="application-tree overflow-y-scroll flex-1 py-1.5 pl-1.5 pr-.5 b-t-1 b-t-solid b-$border-color">
    <el-tree
      ref="treeRef"
      node-key="path"
      highlight-current
      :data="formatterRoutes"
      :expand-on-click-node="false"
      @node-click="onNodeClick"
    >
      <template #default="{data}">
        <div class="flex-1 flex items-center select-none pr-1.5">
          <tr-svg-icon v-if="data.meta?.icon" :icon-class="data.meta?.icon" class="text-24px py-4px pl-3.5px pr-2.5px mr-1.5 rounded-2 bg-$color-primary-9" />
          <i v-else class="inline-block w-18px h-18px mr-1.5 rounded bg-$primary-9" />

          <span class="flex-1">{{ labelGetter(data) }}</span>
        </div>
      </template>
    </el-tree>
  </div>
</template>

<style lang="scss" scoped>
.application-tree ::v-deep {
  --tree-node-height: 32px;

  .el-tree-node__expand-icon {
    width: 22px;
    margin: 0 0 0 4px;
  }

  .create-btn {
    display: none;
    contain: none;
    content-visibility: hidden;
    contain-intrinsic-height: 0;
  }

  .el-tree-node {
    contain: content;
  }

  .el-tree-node__children > .el-tree-node,
  .el-tree-node + .el-tree-node {
    margin-top: 4px
  }

  .el-tree-node__content:only-child:not(:last-child) {
    margin-bottom: 4px;
  }

  // 禁用树组件展开的动画效果
  // 因为在被动切换节点的时候出现的过渡动画观感很不好
  .el-tree-node__expand-icon,
  .el-tree-node__children {
    transition: none !important
  }

  .el-tree-node__content {
    transition: background-color .2s;
    cursor: pointer;

    &:hover .create-btn {
      display: block;
      contain: content;
      content-visibility: visible;
      contain-intrinsic: 22px 22px;
    }
  }

}
</style>
