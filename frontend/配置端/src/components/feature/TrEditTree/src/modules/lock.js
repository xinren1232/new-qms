/*
 * @Author: BanLi
 * @Date: 2021-04-28 09:30:49
 * @LastEditors: BanLi
 * @LastEditTime: 2021-04-28 10:49:05
 * @Description: 节点锁定相关
 */

export default {
  methods: {
    // TOOL: 对当前节点的最后一个父节点添加检出的状态: (检出人/检出节点名/检出节点id)
    lockedNode() {
      if (this.rootParent?.data?.checkoutBy) return

      this.$emit('refresh', this.currentNode.data)
    },

    // TOOL: 对当前节点的最后一个父节点移除检出的状态: (检出人/检出节点名/检出节点id)
    unlockNode() {
      if (!this.rootParent?.data?.checkoutBy) return

      this.$emit('refresh', this.currentNode.data)
    }
  }
}
