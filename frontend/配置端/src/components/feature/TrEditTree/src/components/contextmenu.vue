<!--
 * @Author: BanLi
 * @Date: 2021-01-16 20:15:58
 * @LastEditTime: 2021-08-03 16:05:05
 * @Description: 属性右键菜单
-->

<template>
  <!-- 如果没有传入菜单 那么不创建模板节点 -->
  <!-- 只有传入了菜单列表 显示隐藏菜单才有意义 -->
  <!-- 传入class为cut-off的li为分割线 -->
  <ul
    v-if="menus.length"
    v-show="value"
    class="contextmenu"
    :style="menusStyle || menuPosition"
    @contextmenu.stop.prevent
  >
    <li
      v-for="menu of menus"
      :key="menu.label"
      class="parent-li relative"
      :class="menu.class"
      @click.stop.prevent="onClickMenu(menu, $event)"
      @mouseenter="current = menu.label"
      @mouseleave="current = ''"
    >
      <tr-svg-icon
        v-if="menu.icon"
        :icon-class="menu.icon || ''"
        class="peoples"
      />
      {{ menu.label }}
      <i
        v-if="menu.child"
        class="el-icon-arrow-right"
      />
      <ul
        v-if="menu.child && menu.child.length"
        class="childrenList"
        :class="{ showChild: current === menu.label }"
      >
        <li
          v-for="(titem, tindex) in menu.child"
          :key="tindex"
          class="select-none"
          @click.stop.prevent="onClickMenu(titem, $event)"
        >
          {{ titem.label }}
        </li>
      </ul>
    </li>
    <slot name="after" />
  </ul>
</template>

<script>
export default {
  model: { event: 'show-contextmenu' },
  props: {
    value: {
      type: Boolean,
      default: false,
      comment: '是否展示或者隐藏'
    },
    menus: {
      type: Array,
      default: () => [],
      comment: '右键时展示的菜单列表'
    },
    targetNode: {
      type: Object,
      default: null,
      comment: '当前右击的节点'
    },
    nodeEvent: {
      type: [MouseEvent, Object],
      default: null,
      comment: '右击节点时的鼠标事件对象'
    },
    menusStyle: {
      type: Object,
      default: null
    }
  },
  data() {
    return { current: '' }
  },

  computed: {
    CONTEXT_MENU_OFFSET_HEIGHT({ menus }) {
      // 菜单数量 * 单个菜单高度 + padding: 5px 0; + 5偏移量
      return menus.length * 30 + 10 + 5
    },
    menuPosition({ CONTEXT_MENU_OFFSET_HEIGHT: HT, nodeEvent }) {
      const clientX = nodeEvent?.clientX ?? 0
      const clientY = nodeEvent?.clientY ?? 0
      // const HT = this.CONTEXT_MENU_OFFSET_HEIGHT

      // 边界处理 如果在右击时 位置不够展示菜单 那么将菜单向上展示
      const Y = clientY + HT >= window.innerHeight ? clientY - HT + 25 : clientY + 5

      return { left: clientX + 5 + 'px', top: Y + 'px' }
    }
  },

  methods: {
    onClickMenu(menu, event) {
      if (!menu.handler) {
        this.$emit('show-contextmenu', true)

        return
      }
      this.$emit('show-contextmenu', false)
      menu?.handler &&
        menu.handler({
          node: this.targetNode,
          data: this.targetNode?.data,
          menuEvent: event,
          nodeEvent: this.nodeEvent
        })
    }
  }
}
</script>

<style lang="scss" scoped>
.contextmenu {
  position: fixed;
  margin: 5px 0;
  padding: 5px 0;
  border-radius: 5px;
  box-sizing: border-box;
  border: 1px solid #e4e7ed;
  background-color: #fff;
  box-shadow: 0 2px 12px 0 rgba(0, 0, 0, 0.1);
  font-size: 12px;
  z-index: 100;

  li,
  .childrenList > li {
    padding: 0 20px;
    height: 30px;
    line-height: 30px;
    cursor: pointer;
    text-overflow: ellipsis;
    white-space: nowrap;
    color: #606266;

    &:hover {
      background-color: #f5f7fa;
    }
  }

  .cut-off {
    height: 1px;
    background: #e4e7ed;
  }

  .childrenList {
    display: none;
    padding: 5px 0;
    border-radius: 5px;
    box-sizing: border-box;
    border: 1px solid #e4e7ed;
    background-color: #fff;
    box-shadow: 0 2px 12px 0 rgba(0, 0, 0, 0.1);
    font-size: 12px;
    z-index: 100;
    margin: 0;
    position: absolute;
    top: -6px;
    right: -1px;
    transform: translateX(100%);
    &.showChild {
      display: block;
    }
  }

  .el-icon-arrow-right {
    font-size: 16px;
    position: absolute;
    top: 8px;
    right: 6px;
  }
}
</style>
