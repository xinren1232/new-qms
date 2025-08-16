/*
 * @Author: BanLi
 * @Date: 2021-04-27 15:32:15
 * @LastEditors: Please set LastEditors
 * @LastEditTime: 2021-07-22 10:16:22
 * @Description: 节点右键相关的操作
 */
import { defineProps } from './attrs'

export default {
  props: defineProps('contextMenus,menusStyle'),

  data() {
    return {
      contextEvent: null, // 右键菜单节点事件对象
      menuPositions: null, // 菜单的位置
      showContextMenu: false // 右键菜单
    }
  },

  created() {
    // 如果没有右键菜单 则重置事件
    if (!this.contextMenus?.length) {
      this._hideContextMenu = () => {}

      this._showContextMenu = () => {}
    }
  },

  computed: {
    CONTEXT_MENU_OFFSET_HEIGHT({ contextMenus }) {
      // 菜单数量 * 单个菜单高度 + padding: 5px 0; + 5偏移量
      return contextMenus.length * 30 + 10 + 5
    }
  },

  methods: {
    // 隐藏菜单
    _hideContextMenu() {
      this.showContextMenu = false
    },

    /**
     * 右键节点 展示右键菜单
     * @param {MouseEvent} event 原生事件对象
     * @param {object} data 节点的数据对象
     * @param {Node} node ElementUI Tree组件Node对象
     */
    async _showContextMenu(event, data, node) {
      if (data.isRoot) return

      const { clientX, clientY } = event
      const HT = this.CONTEXT_MENU_OFFSET_HEIGHT

      // 边界处理 如果在右击时 位置不够展示菜单 那么将菜单向上展示
      const Y = clientY + HT >= window.innerHeight ? clientY - HT : clientY + 8

      this.contextEvent = event
      this.menuPositions = { left: clientX + 8 + 'px', top: Y + 'px' }

      if (this.currentNode !== node) {
        this._setNode(node)
        this.$emit('select', { node, data })
      }
      if (!this.showBtn) return
      setTimeout(() => {
        this.showContextMenu = true
      }, 300)
    }
  }
}
