/*
 * @Author: BanLi
 * @Date: 2021-04-16 18:28:25
 * @LastEditors: BanLi
 * @LastEditTime: 2021-04-28 11:08:23
 * @Description: 顶部操作栏
 */

import { Message } from 'element-ui'

import { traverseNode } from '../util'
import { defineProps } from './attrs'

export default {
  props: defineProps('layout,showButtons,extendsMenus'),

  computed: {
    navbars({ layout }) {
      return [...layout.split(',').map((name) => name.trim())]
    }
  },

  created() {
    // 如果没有传入拓展菜单菜单列表 则清除事件
    if (!this.extendsMenus?.length) {
      this.onClickExtend = () => {}
    }
  },

  methods: {
    /**
     * 点击右上角扩展菜单
     * @param {object} menu 点击的那一项菜单
     * @param {MouseEvent} event 原生事件对象
     */
    _onClickExtendMenu(menu, event) {
      menu?.handler &&
        menu.handler({
          event,
          node: this.currentNode,
          data: this.currentNode.data
        })
    },

    /**
     * TOOL: 收起目标节点或者所有节点
     * @param {Node|null} targetNode 目标节点
     */
    collapseNode(targetNode) {
      if (targetNode?.data) return targetNode.collapse()

      const root = this.tree?.root

      traverseNode(root, (node) => node.expanded && node.collapse()) // 只有展开的才折叠
    },

    /**
     * TOOL: 展开目标节点或者所有节点
     * @param {Node|null} targetNode 目标节点
     */
    expandNode(targetNode) {
      if (targetNode?.data) return targetNode.expand()

      const root = this.tree?.root

      traverseNode(root, (node) => !node.expanded && node.childNodes?.length && node.expand()) // 只有折叠的才展开
    },

    /**
     * TOOL: 上下移排序节点
     * @param {number} step 节点上移还是下移的数据偏移量
     * @returns {object} indexs 对象排序时的数据索引
     * @returns {number} indexs.oldIndex 数据原来位置
     * @returns {number} indexs.newIndex 数据新的位置
     */
    sortedNode(step) {
      const { currentNode: node } = this

      // 移动的位置不正确、当前是根节点、当前节点被锁定
      if (!step) {
        return console.warn('step of error')
      } else if (node?.data.isRoot) {
        return Message.warning('不能移动根节点')
      } else if (node.data.lockInfo) {
        return Message.error('当前对象链已被检出, 不可进行移动操作!')
      }

      const { nodeKey, tree } = this
      const array = node.parent.data[this.childrenField]
      const index = array.findIndex((i) => i[nodeKey] === node.data[nodeKey])

      const _target = array[index] // 当前操作的数据
      let current
      let lastThrough

      if (!_target) {
        return Message.error('请先选中目标节点！')
      }

      // 当前节点是最顶部或者最底部
      if ((step < 0 && index + step < 0) || (step > 0 && index + step >= array.length)) {
        return { oldIndex: index, newIndex: index }
      }

      // 上移
      if (step < 0 && index + step >= 0) {
        tree.remove(_target)
        lastThrough = array[index + step]
        tree.insertBefore(_target, array[index + step])
        // 下移
      } else if (step > 0 && index + step < array.length) {
        tree.remove(_target)
        lastThrough = array[index]
        tree.insertAfter(_target, array[index])
      }

      // 移动完成以后获取新的节点
      this.$nextTick(() => {
        current = this.getNode(_target)

        this._setNode(current)

        this.$emit('node-drop', current, this.getNode(lastThrough))
      })

      return { oldIndex: index, newIndex: index + step }
    }
  }
}
