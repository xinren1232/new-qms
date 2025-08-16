<template>
  <div class="l-main">
    <div class="l-left">
      <!-- <el-input v-model="filterText" class="search" clearable placeholder="输入关键字进行过滤" /> -->
      <el-card ref="left" class="h-full">
        <div class="buttons">
          <el-button type="primary" icon="el-icon-plus" @click="showAddDialog('add')">新增</el-button>
          <el-button type="primary" icon="el-icon-download" @click="onImport">导入</el-button>
          <el-button type="primary" icon="el-icon-upload2" @click="onExport">导出</el-button>
        </div>
        <tr-edit-tree
          v-if="treeData && treeData.length > 0"
          ref="tree"
          :props="{ children: 'childRoles' }"
          node-key="bid"
          current-node-key="团队角色"
          :data="treeData"
          :show-buttons="true"
          layout="collapse,expand"
          :data-template="dataTemplate"
          :allow-drag="allowDrag"
          :allow-drop="allowDrop"
          @remove="removeNode"
          @rename="renameNode"
          @create="createNode"
          @select="onSelected"
          @node-drop="nodeDrop"
          @node-drag-start="nodeDragStart"
        >
          <template
            v-if="data.createdTime"
            slot="buttons-extends"
            slot-scope="{ data }"
          >
            <el-tooltip class="item" effect="dark" content="禁用" placement="top">
              <el-button
                v-if="data.stateCode === 'enable'"
                type="danger"
                icon="el-icon-warning"
                style="padding: 2px; font-size: 12px"
                @click="_setStatus(data)"
              />
            </el-tooltip>
            <el-tooltip v-if="!data.isRoot" class="item" effect="dark" content="启用" placement="top">
              <el-button
                v-if="data.stateCode === 'off' || data.stateCode === 'disable' || !data.stateCode"
                style="padding: 2px; font-size: 12px"
                icon="el-icon-switch-button"
                type="success"
                size="mini"
                @click="_setStatus(data, 'turn')"
              />
            </el-tooltip>
          </template>

          <template slot="buttons" slot-scope="scope">
            <el-button type="success" icon="el-icon-plus" size="mini" class="operate-btn" @click="createBtn(scope.node)" />
            <el-button v-if="scope.data.showDeleteBtn" type="danger" icon="el-icon-delete" size="mini" class="operate-btn" @click="removeBtn(scope.node)" />
          </template>

          <template slot="node-icon" slot-scope="{ data }">
            <!-- <i class="el-icon-s-custom" :style="{color: data.color}" /> -->
            <tr-svg-icon icon-class="role-icon" :style="{ color: data.color }" style="transform: translate(0, 1px)" />
          </template>
        </tr-edit-tree>

        <tr-default-page v-else title="角色节点为空" />
      </el-card>
    </div>

    <div class="l-right">
      <edit-role-form ref="editForm" :node="node" @initData="initData" />
    </div>

    <import-dialog ref="import" @confirm="onConfirm" />
  </div>
</template>
<script>
// import TrNoData from '@@/feature/TrNoData'
import { createBatch, deleteRole, dragRole, editRole, exportResourceRole, queryRole, setStatus } from '@/api/resRole'
import TrEditTree from '@/components/feature/TrEditTree'
import TrDefaultPage from '@@/feature/TrDefaultPage'

// import ImportDialog from '../import-dialog'
import EditRoleForm from './edit-role-form'

export default {
  components: {
    TrEditTree,
    EditRoleForm,
    // ImportDialog,
    //  TrNoData
    TrDefaultPage
  },
  props: {
    edit: {
      type: Boolean,
      default: true
    }
  },
  data() {
    return {
      canShowDelBtn: false,
      length: 0,
      controlNode: '', // 保存新建节点名称，用于提示语与判断条件
      dataTemplate: {
        color: 'DarkSeaGreen',
        stateCode: 'off',
        objInsBid: '',
        type: 'bizRole',
        showDeleteBtn: true
      },
      dragList: [],
      node: {}
    }
  },
  mounted() {
    this.initData()
    this.node = { ...this.treeData[0] } // 前端设置根节点
    this.$refs.tree.$refs.tree.setCurrentKey(this.node)
  },
  methods: {
    // 限制操作提示
    controlBtnClick(type, data) {
      if (type === 'remove') {
        return this.controlNode && this.controlNode.name !== data.data.name
          ? this.$message.warning(`${this.controlNode.name}未保存，请保存后再进行其他操作`)
          : false
      } else {
        return this.controlNode
          ? this.$message.warning(`${this.controlNode.name}未保存，请保存后再进行其他操作`)
          : false
      }
    },
    allowDrop(dragNode, { data }) {
      return !data.isRoot
    },
    allowDrag({ data }) {
      return !(this.controlNode || !data.createdTime || !this.$refs.tree.canOperate())
    },
    onConfirm(data) {
      // 导入
      createBatch(data).then(() => {
        this.$message.success('导入成功')
        this.initData()
      })
    },
    removeBtn(scope) {
      if (this.controlBtnClick('remove', scope)) return // 判断操作是否可以进行
      this.$refs.tree.removeNode(scope)
    },
    createBtn(scope) {
      if (this.controlBtnClick('others')) return // 判断操作是否可以进行
      this.$refs.tree.createNode(scope)
    },
    renameNode({ data, oldName }) {
      if (data.createdTime) {
        // 已入库的节点才进行请求，否则会报错
        editRole(data)
          .then(() => {
            this.$message.success('名称修改成功')
          })
          .catch(() => {
            data.name = oldName
          })
      }
    },
    createNode({ data, node }) {
      this.controlNode = data // 保存未入库的节点用于判断是否可进行其他操作
      const sort = node.parent.data.childRoles.length // 获取新增节点的sort

      data.sort = sort > 0 ? sort - 1 : 0
      data.parentBid = node.parent.data.bid
      data.parentName = node.parent.data.name
      // data.bid = ''
      this.$nextTick(() => {
        this.node = data
        this.$parent.selectedNode = data
        this.$refs.editForm.disabled = false
      })
    },
    removeNode(node) {
      this.controlNode = ''
      if (node.data.createdTime) {
        deleteRole(node.data).then(() => {
          this.$message.success('删除成功')
          this.initData('delete')
          this.node = this.treeData[0]
        })
      }
    },
    showAddDialog(state) {
      this.$emit('showAddDialog', state)
    },
    onExport() {
      exportResourceRole()
    },
    onImport() {
      this.$refs.import.show()
    },
    initData() {
      queryRole().then(({ data }) => {
        this.traverseRole(data)
        this.treeData[0].childRoles = data
        this.$refs.editForm.disabled = true
        this.$parent.dataSort = data.length
        this.$nextTick(() => {
          this.$refs.tree.$refs.tree.setCurrentKey(this.node.bid)
          this.node = this.$refs.tree.$refs.tree.getCurrentNode() // 覆盖当前选中的节点
          this.parentNode = this.$refs.tree.$refs.tree.getNode(this.node).parent.data
          this.node.parentName = this.parentNode.name
        })
      })
    },
    async _setStatus(data) {
      if (!this.$refs.tree.canOperate()) return this.$message.warning('请先保存节点再进行操作！')
      if (this.controlBtnClick('others')) return
      // ① 是否弹出确认框, 并设置stateCode
      let stateCode = data.stateCode

      if (stateCode === 'off') {
        await this.$confirm('该操作将不可逆,启用后将无法恢复，是否继续?', '提示', {
          confirmButtonText: '确定',
          cancelButtonText: '取消',
          type: 'warning'
        })
        stateCode = 'enable'
        this.$set(data, 'stateCode', 'enable') // 避免页面不刷新
      } else {
        stateCode =
          stateCode === 'enable' ? this.$set(data, 'stateCode', 'disable') : this.$set(data, 'stateCode', 'enable')
      }
      // ② 发送setStatus请求
      setStatus({ ...data, stateCode }).then(() => {
        this.$message.success('设置状态成功')
        this.controlNode = ''
        this.initData()
      })
    },
    nodeDragStart(node) {
      const parent = { ...node.parent.data }

      if (parent.childRoles.length > 0) {
        this.dragList = parent.childRoles.map((item, index) => {
          item.sort = index

          return item
        })
      }
    },
    // TODO：待测试
    nodeDrop(before, after, inner) {
      const node = before.data
      let parent

      inner === 'inner' ? (parent = { ...after.data }) : (parent = { ...after.parent.data })
      const childRoles = [...parent.childRoles]

      node.parentBid = parent.bid
      childRoles.forEach((item, index) => {
        item.sort = index
      })
      this.dragList.push(...childRoles)
      this.$nextTick(() => {
        const params = [...Array.from(new Set(this.dragList))]

        dragRole(params)
          .then(() => {
            this.$message.success('拖拽成功')
            this.dragList = []
          })
          .catch(this.initData)
      })
    },
    onSelected({ data, node }) {
      if (data) {
        data.parentCode = node.parent.data.code
        data.parentName = node.parent.data.name
        this.node = data
        this.$parent.selectedNode = data
      }
    },
    traverseRole(arr) {
      if (!arr) return
      for (let i = 0; i < arr.length; i++) {
        const node = arr[i]

        // 当前节点状态未启用时再判断子节点是否启用过
        if (node.stateCode === 'off') {
          const flag = this.traverseChilds(node.childRoles)

          node.showDeleteBtn = !flag
          this.canShowDelBtn = false
        }

        if (node.stateCode === 'off' || !node.stateCode) {
          node.color = 'DarkSeaGreen'
        }

        if (node.stateCode === 'disable') {
          node.color = 'darkgray'
        }
        this.traverseRole(node.childRoles)
      }

      return arr
    },
    traverseChilds(arr) {
      if (!arr?.length) return
      for (let i = 0; i < arr.length; i++) {
        const node = arr[i]

        if (node.stateCode !== 'off') {
          this.canShowDelBtn = true
        } else {
          this.traverseChilds(node.childRoles)
        }
      }

      return this.canShowDelBtn
    }
  }
}
</script>

<script setup>
import { ref } from 'vue'
import { getRoleTree } from '@/views/system-manage/role-manage/api/index.js'

const treeData = ref([{
  bid: 0,
  childRoles: [],
  name: '业务角色',
  isRoot: true,
  note: '根节点',
  icon: 'el-icon-s-custom',
  code: 'root'
}
])
// 查询角色树
const getRoleTreeData = async () => {
  const { data } = await getRoleTree()

  treeData.value = data
}

getRoleTreeData()
</script>

<style lang="scss" scoped>
.nodeOff {
  color: greenyellow;
}
.nodeDisable {
  color: darkgray;
}
.tr-edit-tree {
  width: 100%;
  height: calc(100% - 30px);
}
.search {
  margin-top: 10px;
  margin-bottom: 10px;
}
.saveBtn {
  margin: 0 auto;
}
.l-main {
  display: flex;
  height: 100%;
  min-height: 400px;
}
.l-left {
  height: 100%;
  width: 450px;
  .tree-operate-container {
    margin-left: 15px;
  }
}
.l-right {
  width: 100%;
  height: 100%;
  padding-left: 10px;
  .m-node-content-container {
    height: 100%;
    min-width: 500px;
    border-right: none;
    border-bottom: none;
    padding: 10px;
  }
}
.line {
  width: 0.5px;
  margin-left: 30px;
  background-color: rgb(201, 200, 200);
}
</style>
