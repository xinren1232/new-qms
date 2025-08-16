/*
 * @Author: BanLi
 * @Date: 2021-04-16 18:58:35
 * @LastEditTime: 2021-07-08 10:58:43
 * @Description: 对节点的操作: 增删改
 */

import { Message } from 'element-ui'

import { findLastParent } from '../util'
import { defineProps } from './attrs'

export default {
  props: defineProps('draggable,dataTemplate,createCustomEditing'),

  data() {
    return {
      // rootParent: null, // 最后一个不为root节点的父节点
      currentNode: null, // 当前操作的节点数据 包含 node 及 data
      showPopconfirm: false // 删除的确认弹窗
    }
  },

  created() {
    this.oldName = ''
    this._canOperate = true
  },

  methods: {
    /**
     * 是否显示新增节点按钮
     * @param {Node} node Tree组件node对象
     * @returns {boolean}
     */
    _showCreateButton({ data }) {
      return !data.isRoot && (this.createField === undefined || data[this.createField])
    },

    /**
     * 是否显示删除按钮
     * @param {Node} node Tree组件node对象
     * @returns {boolean}
     */
    _showDeleteButton({ data, parent }) {
      return parent.parent && (this.removeField === undefined || data[this.removeField])
    },

    _highlightNode() {
      this.tree.setCurrentKey(this.currentNode?.data) // 设置节点高亮
    },

    /**
     * 更新当前节点以及最后一个父级节点的信息 同时选中当前
     * @param {Node|object} node 节点信息
     */
    _updateNodeData(node) {
      this.currentNode = this.getNode(node)
      this._highlightNode()
      // this.rootParent = findLastParent(this.currentNode)
    },

    /**
     * TOOL: 获取节点
     * @param {string|object} keyOrData 根据唯一标识或者数据对象获取Node节点
     * @returns {Node} node 返回符合条件的节点
     */
    getNode(keyOrData) {
      return this.tree.getNode(keyOrData)
    },

    /**
     * 更新当前节点对象数据、设置高亮节点、更新最后一个父节点
     * @param {Node} node 更新的节点对象
     */
    _setNode(node) {
      if (this.currentNode === node) return

      this._updateNodeData(node)
      this.canOperate() && (this.oldName = node.data[this.labelField])

      return node
    },

    /**
     * 生成新节点的基础数据
     * @param {Node} node 生成节点新数据时使用的基础数据
     * @returns {object} 生成的新数据
     */
    _generateNodeData(node) {
      const { data } = node
      const KEY = this.nodeKey

      // 判断是以根节点的数组长度做判断还是选中节点的数组
      const index = (node.childNodes ?? this.data).length + 1

      return {
        ...this.dataTemplate, // 外部传入锁包含必须字段的模板
        version: data.version || 'V1.0', // 当前节点的版本
        parentVersion: data.version || 'V1.0', // 父节点版本
        // 边界处理 可能存在没有父节点的情况
        // 如果有并且不是root节点 那就是当前节点
        // 如果是root就传null
        parentBid: data[KEY],
        parent_bid: data[KEY],
        rootBid: findLastParent(node)?.data?.[KEY],

        sort: index, // 排序
        editing: true, // 新建节点以后让节点处于编辑状态
        _newNode: true, // 标识是否是新创建的节点 在编辑和取消编辑时用
        [KEY]: `${Date.now()}`, // 随机创建一个id作为key保证循环时不重复
        [this.childrenField]: [],
        [this.labelField]: `${data[this.labelField] ?? '新节点'}_${index}`
      }
    },

    /**
     * 添加节点的主程序方法 只供组件内部使用 外部使用时应调用 createNode
     * @param {Node} node 操作的节点 如果没有传入就是用当前节点
     */
    async _createNode(node = this.currentNode) {
      // 如果当前节点也不存在 那就把tree root作为节点
      node ??= this.tree.root.childNodes[0] ?? this.tree.root

      if (!this.canOperate()) {
        return Message.warning('请先保存节点再进行新建操作！')
      }

      // 如果这个没有子节点的容器那就创建一个
      if (!node?.data[this.childrenField]) {
        this.$set(node?.data, this.childrenField, [])
      }

      const nodeData = this._generateNodeData(node)

      // 把节点添加到子级列表当中
      this.tree.append(nodeData, node)
      this._canOperate = false

      await this.$nextTick()

      let lastParent = node

      // 依次向上展开所有的父级节点
      while (lastParent && lastParent.expand) {
        lastParent.expand()

        // 因为是模拟的组件事件，所以这里拿不到第三个参数的节点组件，只能手动模拟一下需要的参数
        this.$emit('custom-node-expand', lastParent.data, lastParent, { expanded: false })

        lastParent = lastParent.parent
      }

      // 节点更新以后把编辑状态下的输入框内文本选中 方便用户可以直接修改
      setTimeout(() => {
        this.$el.querySelector('.tree-node-container.editing input')?.select()
      }, 300)

      return this.tree.getNode(nodeData)
    },

    /**
     * 删除节点的主程序方法, 删除节点以后会选中父级节点 同时重置可编辑状态
     */
    async _removeConfirm() {
      this._hideContextMenu()
      if (!this.currentNode) return

      const { childrenField, currentNode: node, nodeKey, tree } = this

      const data = node?.data
      const parent = node?.parent
      const children = parent.data[childrenField]
      const findNode = children.find((d) => d[nodeKey] === data[nodeKey])

      tree.remove(findNode)

      await this.$nextTick()

      this._canOperate = true
      this.$emit('remove', { node, data })
      this.$emit('select', { node: parent, data: parent.data })
      // 如果删除的节点父级是根，那么取消节点
      if (parent.data.isRoot) {
        this.unhighlight()
      } else {
        this._setNode(parent)
      }
    },

    /**
     * 双击节点时, 进入编辑节点状态
     * @param {Node} {node,data}
     * @param {MouseEvent} event
     */
    _dblClickNode({ data, node }, event) {
      if (data?.noDocEdit) return
      this._hideContextMenu()

      this.setEditing({ node, data, event })
    },

    /**
     * TOOL: 设置节点编辑状态及选中文本框内容文本
     * @param {Node} node 操作的对象节点 其中包含 MouseEvent 对象
     */
    async setEditing({ data, event: { target } }) {
      if (data.isRoot) return

      if (!this.canOperate()) {
        return Message.warning('请先保存节点再进行编辑操作！')
      }

      this.$set(data, 'editing', true)
      this._canOperate = false

      // 双击出现输入框以后选中输入框内的文字
      await this.$nextTick()
      target.querySelector('input')?.select()
    },

    /**
     * TOOL: 当前组件是否能进行新增/编辑/删除操作
     * @returns {boolean} 返回当前树组件是否能进行操作
     */
    canOperate() {
      return this._canOperate
    },

    /**
     * TOOL: 获取当前高亮/选中的节点数据
     * @returns {object} 返回当前操作的数据
     */
    getCurrentData() {
      return this.currentNode?.data
    },

    /**
     * TOOL: 获取当前节点链上最后一个不为root节点的父级节点
     *       如果传入了数据则查找传入的数据, 否则直接返回当前父级
     * @paran {node} dataOrNode 需要获取的数据
     * @returns {Node} 返回Node节点
     */
    // getRootParent(node) {
    //   if (node) return findLastParent(this.getNode(node))

    //   return this.rootParent
    // },

    // TOOL: 取消节点的高亮结果 但不会取消上次选中的结果
    unhighlight() {
      this.tree.setCurrentKey(null)
      this.currentNode = null
    },

    /**
     * TOOL: 根据查找目标节点并且选中, 同时触发选中事件
     * @param {any} data 选中节点时的凭证 可以是id 可以是数据对象
     * @param {boolean} log 是否打印选中的节点
     */
    async selectNode(data, node) {
      this._hideContextMenu()

      await this.$nextTick()

      const findNode = node || this.getNode(data)

      // 解决单双击事件冲突以及重复触发事件
      if (findNode === this.currentNode || data?.editing) return
      if (data.isRoot) {
        this.unhighlight()
      } else {
        this._setNode(findNode)
        this.$emit('select', { node: findNode, data: findNode.data })
      }
    },

    /**
     * TOOL: 删除节点的入口方法 在这之前会做一些校验 外部使用时应调用 removeNode
     * @param {Node} node 操作的节点 如果没有传入就是用当前节点
     */
    removeNode(node = this.currentNode) {
      if (!this.canOperate()) {
        return Message.warning('请先保存节点再进行操作！')
      } else if (node.data.isRoot) {
        return Message.warning('不能删除根节点')
      }

      this._setNode(node)
      this.showPopconfirm = true
    },

    /**
     * TOOL: 点击节点旁边的确认编辑按钮
     * @param {Node} {node,data} 操作的节点
     * @description 按钮有两个作用, 分别是确定新增和确定编辑名称
     */
    renameNode({ data, node }) {
      clearTimeout(this.saveTimeoutId)

      this.saveTimeoutId = setTimeout(() => {
        this._hideContextMenu()

        if (!data[this.labelField].trim()) {
          return Message.error('请输入节点名!')
        }
        // else if (!new RegExp(this.nameValidate[0], 'g').test(data[this.labelField])) {
        //   return Message.error(this.nameValidate[1])
        // }

        if (data._newNode) {
          data._newNode = false
          this.$emit('create', { node, data })
        } else if (data.name !== this.oldName) {
          this.$emit('rename', { node, data, oldName: this.oldName })
          this.oldName = data.name
        }

        this._canOperate = true
        this._setNode(node)
        if (this.createCustomEditing) {
          this.$set(data, 'editing', false)
        }
      }, 800)
    },

    /**
     * TOOL: 取消节点的编辑状态, 或者是移除节点
     * @param {Node} {node,data} 操作的节点对象
     */
    cancelNode({ data, node }, remove) {
      // 取消因为失焦而导致的自动保存延时
      clearTimeout(this.saveTimeoutId)

      if (remove) {
        this.tree.remove(node ?? data)
      } else if (data._newNode) {
        this.tree.remove(node)
      } else {
        data[this.labelField] = this.oldName
        this.$set(data, 'editing', false)
      }

      this._canOperate = true
    }
  }
}
