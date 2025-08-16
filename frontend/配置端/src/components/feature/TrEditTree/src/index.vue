<template>
  <div v-outside-click="_hideContextMenu" class="tr-edit-tree">
    <!-- 按钮菜单组 -->
    <div v-if="showButtons" class="operator-bar clearfix" @click.stop>
      <slot name="operate">
        <component :is="nav" v-for="nav of navbars" :key="nav" />
        <slot name="operate-after" />
        <el-dropdown
          v-if="extendsMenus.length"
          trigger="click"
        >
          <el-button class="el-icon-more more-menu-list" />
          <el-dropdown-menu slot="dropdown">
            <el-dropdown-item
              v-for="menu of extendsMenus"
              :key="menu.label"
              @click.native="_onClickExtendMenu(menu, $event)"
            >
              {{ menu.label }}
            </el-dropdown-item>
          </el-dropdown-menu>
        </el-dropdown>
      </slot>

      <el-collapse-transition>
        <div v-if="showFilter" class="filter-bar">
          <el-input ref="filterInput" v-model.trim="keyword" :placeholder="$t('component.trEditTree.inputPlace')" clearable />
        </div>
      </el-collapse-transition>
    </div>

    <el-tree
      ref="tree"
      :data="data"
      class="tree-node"
      highlight-current
      :node-key="nodeKey"
      :draggable="draggable"
      :expand-on-click-node="false"
      :default-expand-all="isExpandNode"
      :filter-node-method="_filterNodeMethods"
      v-bind="$attrs"
      v-on="$listeners"
      @node-contextmenu="_showContextMenu"
      @click.native="_hideContextMenu"
      @node-click="selectNode"
    >
      <div
        slot-scope="scope"
        class="tree-node-container"
        :class="{ editing: scope.data.editing, beDrop: scope.node && scope.node.beDrop }"
        @dragenter="dragenter(scope)"
        @drop="drop($event, scope)"
        @dragleave="dragleave(scope)"
      >
        <div
          class="tree-node-label"
          @dblclick.stop="_dblClickNode(scope, $event)"
        >
          <el-input
            v-if="scope.data.editing"
            v-model="scope.data[labelField]"
            class="rename-input"
            draggable="none"
            :maxlength="50"
            @blur="renameNode(scope)"
            @dblclick.stop.native
            @keydown.native.enter="renameNode(scope)"
          />
          <template v-else>
            <!-- 节点被锁定 -->
            <template v-if="scope.data.lockInfo">
              <slot
                name="lock-icon"
                :node="scope.node"
                :data="scope.data"
              />
            </template>
            <template v-if="scope.data.star && scope.data.star === true">
              <slot
                name="star-icon"
                :node="scope.node"
                :data="scope.data"
              />
            </template>
            <template v-else-if="scope.data.star && scope.data.star === false">
              <slot name="icon-placeholder" />
            </template>
            <!-- 自定义图标 -->
            <slot
              name="node-icon"
              :scope="scope"
              :data="scope.data"
              :node="scope.node"
              :color="scope.data.color"
            >
              <i v-if="scope.data.icon" :class="scope.data.icon" />
            </slot>
            <slot name="content" :scope="scope" :data="scope.data" :node="scope.node">
              <span>{{ scope.data[labelField] }}</span>
            </slot>
          </template>
        </div>

        <!-- 当右击新增和删除按钮的时候不出现菜单 以及快速点击新增/删除按钮的时候不出现名称编辑 -->
        <span
          class="tree-node-operate-container"
          @dblclick.stop
          @contextmenu.stop
        >
          <template v-if="scope.data.editing">
            <!-- 确定新增节点 -->
            <el-button type="success" icon="el-icon-check" :title="t('confirm')" size="mini" class="operate-btn" @click.stop="renameNode(scope)" />
            <!-- 取消新增节点 -->
            <el-button type="danger" icon="el-icon-close" :title="t('cancel')" size="mini" class="operate-btn" @click.stop="cancelNode(scope)" />
          </template>

          <template v-else>
            <slot name="buttons" :node="scope.node" :data="scope.data">
              <!-- 新增节点 -->
              <el-button
                v-if="_showCreateButton(scope)"
                type="success"
                icon="el-icon-plus"
                :title="t('create')"
                size="mini"
                class="operate-btn"
                @click.stop="createNode(scope.node)"
              />
              <!-- 删除节点 -->
              <el-button
                v-if="_showDeleteButton(scope.node)"
                type="danger"
                icon="el-icon-delete"
                size="mini"
                :title="t('remove')"
                class="operate-btn"
                @click.stop="removeNode(scope.node)"
              />
            </slot>
          </template>
          <slot
            name="buttons-extends"
            :node="scope.node"
            :data="scope.data"
          />
        </span>
      </div>
    </el-tree>

    <popconfirm
      v-model="showPopconfirm"
      @confirm="onRemoveConfirm"
    />
    <contextmenu
      ref="contextmenu"
      v-model="showContextMenu"
      :style="menuPositions"
      :target-node="currentNode"
      :node-event="contextEvent"
      :menus-style="menusStyle"
      :menus="contextMenus"
    />
  </div>
</template>

<script>
import components from './components'
import {
  common, // 其他模块中会公用的属性
  contextmenu, // 右键相关的操作
  filter, // navbar中过滤的方法
  // lock, // 节点状态锁定相关
  navbar, // 顶部navbar菜单列表
  operate// 对节点进行的主体操作
} from './modules/index.js'

export default {
  name: 'TrEditTree',
  components,
  mixins: [common, filter, navbar, operate, contextmenu],
  inheritAttrs: false,

  methods: {
    // 移除节点确认弹窗的回调事件
    async onRemoveConfirm(node = this.currentNode) {
      const precondition = this.$attrs['on-remove']

      // 如果传递了删除的请求 并且最终结的请求失败了 则不移除节点
      if (precondition !== undefined && !(await precondition(node))) {
        return this.$emit('cancel-remove', node)
      }

      this._removeConfirm()
    },

    // 添加节点
    async createNode(node = this.currentNode) {
      const precondition = this.$attrs['on-create']

      // 如果传递了新增的请求 并且最终的请求失败了 则不新增节点
      if (precondition !== undefined && !(await precondition(node))) {
        return this.$emit('cancel-create', node)
      }

      this._debounceEvent(this, '_createNode', node)
    },
    dragenter({ node }) {
      node.expanded = true
      this.$set(node, 'beDrop', true)
      // node.beDrop = true
    },
    dragleave({ node }) {
      node.beDrop = false
    },
    drop(e, { node }) {
      this.$emit('dropInNode', node, e)
      node.beDrop = false
    }
  }
}
</script>

<style lang="scss" scoped>
@import './index.scss';
</style>
