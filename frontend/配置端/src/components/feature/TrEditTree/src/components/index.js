/*
 * @Author: BanLi
 * @Date: 2021-04-28 11:11:33
 * @LastEditors: Please set LastEditors
 * @LastEditTime: 2021-07-02 18:15:16
 * @Description: 组件内部所使用的所有子组件
 */

export default {
  Contextmenu: () => import('./contextmenu'),
  Popconfirm: () => import('./popconfirm'),
  // 顶部新增
  Create: {
    render(h) {
      return h('tr-svg-icon', {
        class: 'icon-button bussiness',
        attrs: { iconClass: 'add', title: this.$t('component.trEditTree.create') },
        on: { click: () => this.$parent.createNode() }
      })
    }
  },
  // 顶部删除
  Remove: {
    render(h) {
      return h('tr-svg-icon', {
        class: 'icon-button bussiness',
        attrs: { iconClass: 'delete', title: this.$t('component.trEditTree.remove') },
        on: { click: () => this.$parent.removeNode() }
      })
    }
  },
  // 顶部搜索
  Search: {
    render(h) {
      return h('tr-svg-icon', {
        class: 'icon-button bussiness',
        attrs: { iconClass: 'search', title: this.$t('component.trEditTree.search') },
        on: { click: () => this.$parent.searchNode() }
      })
    }
  },
  // 顶部上移
  MoveUp: {
    render(h) {
      return h('tr-svg-icon', {
        class: 'icon-button bussiness',
        attrs: { iconClass: 'moveup', title: this.$t('component.trEditTree.moveUp') },
        on: { click: () => this.$parent.sortedNode(-1) }
      })
    }
  },
  // 顶部下移
  MoveDown: {
    render(h) {
      return h('tr-svg-icon', {
        class: 'icon-button bussiness',
        attrs: { iconClass: 'movedown', title: this.$t('component.trEditTree.moveDown') },
        on: { click: () => this.$parent.sortedNode(1) }
      })
    }
  },
  // 顶部展开
  Expand: {
    render(h) {
      return h('tr-svg-icon', {
        class: 'icon-button bussiness',
        attrs: { iconClass: 'expand-down', title: this.$t('component.trEditTree.stretch') },
        on: { click: this.$parent.expandNode }
      })
    }
  },
  // 顶部收缩
  Collapse: {
    render(h) {
      return h('tr-svg-icon', {
        class: 'icon-button bussiness',
        attrs: { iconClass: 'stow-up', title: this.$t('component.trEditTree.folding') },
        on: { click: this.$parent.collapseNode }
      })
    }
  },
  // 自行定义功能
  Replace: {
    render(h) {
      return h('tr-svg-icon', {
        class: 'icon-button bussiness',
        attrs: { iconClass: 'replace', title: this.$t('component.trEditTree.replace') },
        on: { click: () => this.$parent.$emit('replacement') }
      })
    }
  },
  // 催办
  urge: {
    render(h) {
      return h('tr-svg-icon', {
        class: 'icon-button bussiness',
        attrs: { iconClass: 'phone', title: this.$t('component.trEditTree.urge') },
        on: { click: () => this.$parent.$emit('urgeClick') }
      })
    }
  }
}
