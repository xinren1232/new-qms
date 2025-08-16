<template>
  <div class="role-tree">
    <el-row v-if="multiple" class="role-select-box">
      <el-tag v-for="item in showValue" :key="item">{{ item }}</el-tag>
      <el-button icon="el-icon-plus" type="text" @click.native="showDialog = true" />
    </el-row>
    <!--    <el-row class="role-select-box" v-else>
      <el-tag @click.native="showDialog = true">{{showValue}}</el-tag>
    </el-row>-->
    <el-input v-else :value="showValue" readonly @click.native="showDialog = true" />

    <el-dialog
      width="70%"
      custom-class="el-picker-panel"
      class="dialog-wrap"
      :visible.sync="showDialog"
      :close-on-click-modal="false"
      modal-append-to-body
      append-to-body
      title="选择角色"
      @open="setCheckedKeys"
      @close="keyword = ''"
    >
      <div class="role-select">
        <div class="left-panel">
          <el-input v-model="keyword" style="margin-bottom: 15px" placeholder="请输入角色名" clearable />
          <el-tree
            ref="tree"
            style="height: 500px;overflow: auto"
            :data="data"
            show-checkbox
            check-strictly
            highlight-current
            default-expand-all
            check-on-click-node
            :node-key="nodeKey"
            class="content-inner"
            :expand-on-click-node="false"
            :filter-node-method="_filterNode"
            v-bind="$attrs"
            :props="propsConfig"
            @check-change="onCheckChange"
            @check="onNodeChecked"
          />
        </div>
        <div class="right-panel">
          <div class="title">
            <span>已选择（{{ selectedData.length }}）</span>
            <el-button type="text" @click="onClearSelected">清空</el-button>
          </div>
          <div class="selected-box">
            <tr-table :columns="columns" :data="selectedData" :height="520" />
          </div>
        </div>
      </div>

      <div slot="footer" class="dialog-footer">
        <el-button @click="onCancel">取消</el-button>
        <el-button type="primary" @click="onConfirm">确定</el-button>
      </div>
    </el-dialog>
  </div>
</template>

<script>
// import { transObjectName } from '@/utils/lang'
import TrTable from '@@/feature/TrTableX'
import { getRoleTree } from '@/views/system-manage/role-manage/api'

export default {
  components: { TrTable },
  inheritAttrs: false,
  props: {
    value: {
      type: String,
      default: '',
      comment: '选择的权限角色'
    },
    title: {
      type: String,
      default: '选择权限角色',
      comment: '弹窗标题'
    },
    nodeKey: {
      type: String,
      default: 'bid',
      comment: '节点的唯一key'
    },
    multiple: {
      type: Boolean,
      default: true,
      comment: '是否多选'
    },
    data: {
      type: Array,
      default: () => [],
      comment: '树组件数据'
    },
    dataMap: {
      type: Object,
      default: () => ({}),
      comment: '数据索引'
    },
    propsConfig: {
      type: Object,
      default: () => ({
        label: 'name',
        children: 'children'
      }),
      comment: '树组件配置'
    }
  },
  data() {
    return {
      selectedData: [],
      node: null,
      keyword: '',
      showDialog: false
    }
  },

  computed: {
    modelValue: {
      get: ({ value }) => value,
      set(value) {
        this.$emit('input', value?.map(it => it.bid).toString())
      }
    },
    defaultCheckedKeys({ value }) {
      return value ? value.split(',').filter(Boolean) : []
    },
    showValue({ dataMap, defaultCheckedKeys,multiple }) {
      const res = defaultCheckedKeys.reduce((acc, cur) => {
        dataMap[cur] && acc.push(dataMap[cur])

        return acc
      }, [])

      if (!multiple) { return res.toString() }

      return res
    },
    columns() {
      return [
        {
          label: '角色名称',
          prop: 'name',
          minWidth: 150
        },
        { label: '角色ID', prop: 'bid', minWidth: 150 },
        {
          label: '操作',
          prop: 'action',
          width: 70,
          align: 'center',
          slots: {
            default: ([, row]) => {
              return (
                <el-button
                  type="danger"
                  circle
                  icon="el-icon-delete"
                  onClick={() => this.removeSelectedData(row)}
                />
              )
            }
          }
        }
      ]
    }
  },

  watch: {
    keyword: 'onFilter'
  },

  created() {
    this.timer = null
  },

  methods: {
    onClearSelected() {
      this.$refs.tree.setCheckedKeys(this.data, false, false)
    },
    onCheckChange() {
      this.selectedData = this.$refs.tree.getCheckedNodes()
    },
    onNodeChecked(node, isChecked) {
      if (!this.multiple) {
        this.$refs.tree.setCheckedKeys([node[this.nodeKey]], isChecked, false)
        isChecked ? this.selectedData = [node] : this.selectedData = []
      }
    },
    removeSelectedData(row) {
      this.$refs.tree.setChecked(row, false, false)
    },
    async setCheckedKeys() {
      await this.$nextTick()
      await this.$refs.tree.setCheckedKeys(this.defaultCheckedKeys)
    },
    onFilter(v) {
      this.timer && clearTimeout(this.timer)
      this.timer = setTimeout(() => {
        this.$refs.tree.filter(v)
        this.timer = null
      }, 500)
    },
    onCancel() {
      this.showDialog = false
      this.setCheckedKeys()
    },
    onConfirm() {
      const nodes = this.$refs.tree.getCheckedNodes()

      this.modelValue = nodes
      this.$emit('confirm', nodes)
      this.showDialog = false
    },

    _filterNode(value, data) {
      if (!value) return true

      return data[this.propsConfig.label].indexOf(value) !== -1
    },
    async _getRoleTree() {
      const { data } = await getRoleTree()

      this.data = data
      // bid 作为key,name 作为value
      this.dataMap = data.reduce((acc, cur) => {
        acc[cur.bid] = cur.name

        return acc
      }, {})
    }
  }
}
</script>
<style lang="scss" scoped>
.role-select-box{
  display: flex;
  align-items: center;
  flex-wrap: wrap;
  .el-tag{
    margin-right: 5px;
  }
}
.dialog-wrap  .el-dialog{
  margin-top: 0 !important;
  top: 50% !important;
  transform: translateY(-50%);

  &__body{
    .el-tree {
      .el-tree-node{
        // height: 30px;
        line-height: 30px;

        &__content{
          height: 30px;

          .custom-render-node{
            width: 100%;
            height: 100%;

            &.disabled{
              cursor: not-allowed;
              color: #ccc;
            }
          }
        }

        &__expand-icon:not(.is-leaf){
          color: var(--primary);
        }
      }
    }
  }
}
.el-dialog__header{
  padding: 15px 20px;
}
::v-deep .el-dialog__footer{
  padding: 0px 20px 20px !important;
}
.role-select{
  display: flex;
  min-width: 200px;
  border-top: 1px solid #e8ebf0;
  padding: 5px;
  height: 560px;
}
.left-panel{
  flex: 0.4;
  padding: 15px 15px 0;
  padding-right: 5px;
  border: 1px solid #e4e7ed;
  height: 567px;
}
.right-panel {
  flex: 1;
  height: 100%;
  padding: 15px 5px 0px 5px;
  background-color: #fff;
  box-sizing: border-box;
  .title {
    border-bottom: 1px solid #e4e7ed;
    font-size: 14px;
    color: #606266;
    display: flex;
    justify-content: space-between;
    align-items: center;
    span {
      margin-right: 10px;
    }
  }
  .selected-box {
    height: calc(100% - 35px)
  }
  .btn-box {
    width: 100%;
    height: 60px;
    display: flex;
    align-items: center;
  }
}
</style>
