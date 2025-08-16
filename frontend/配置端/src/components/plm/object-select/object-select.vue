<template>
  <div class="role-tree">
    <el-input :disabled="disabled" :placeholder="placeholder" :value="showValue" readonly @click.native="onInputClick" />
    <el-dialog
      custom-class="el-picker-panel"
      class="dialog-wrap"
      width="70%"
      :visible.sync="showDialog"
      :close-on-click-modal="false"
      modal-append-to-body
      append-to-body
      :title="title"
      @open="setCheckedKeys"
      @close="keyword = ''"
    >
      <div class="role-select">
        <div class="left-panel">
          <el-input v-model="keyword" placeholder="请输入对象名" clearable />
          <div class="dialog-content">
            <el-tree
              ref="tree"
              :data="data"
              :props="props"
              show-checkbox
              check-strictly
              highlight-current
              :default-expanded-keys="[0]"
              check-on-click-node
              :node-key="nodeKey"
              class="content-inner"
              :expand-on-click-node="false"
              :filter-node-method="_filterNode"
              v-bind="$attrs"
              @check-change="onCheckChange"
              @check="onNodeChecked"
            />
          </div>
        </div>
        <div class="right-panel">
          <div class="title">
            <span>已选择（{{ selectedData.length }}）</span>
            <el-button type="text" @click="onClearSelected">清空</el-button>
          </div>
          <div class="selected-box">
            <tr-table :columns="columns" :data="selectedData" :max-height="500" />
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

export default {
  components: { TrTable },
  inheritAttrs: false,
  props: {
    value: {
      type: String,
      default: '',
      comment: '选择的权限对象'
    },
    title: {
      type: String,
      default: '选择权限对象',
      comment: '弹窗标题'
    },
    nodeKey: {
      type: String,
      default: 'modelCode',
      comment: '节点的唯一key'
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
    multiple: {
      type: Boolean,
      default: true,
      comment: '是否多选'
    },
    placeholder: {
      type: String,
      default: '请选择对象',
      comment: '输入框占位符'
    },
    disabled: {
      type: Boolean,
      default: false,
      comment: '是否禁用'
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
        this.$emit('input', value?.map(it => it[this.nodeKey]).toString())
      }
    },
    defaultCheckedKeys({ value }) {
      return value ? value.split(',').filter(Boolean) : []
    },
    showValue({ dataMap, defaultCheckedKeys }) {
      return defaultCheckedKeys.reduce((acc, cur) => {
        dataMap[cur] && acc.push(dataMap[cur])

        return acc
      }, []).toString()
    },
    columns() {
      return [
        {
          label: '对象名称',
          prop: 'name',
          minWidth: 150
        },
        { label: '对象编码', prop: 'modelCode', minWidth: 150 },
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
    this.props = { label: 'name', children: 'children' }
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

      return data[this.props.label].indexOf(value) !== -1
    },
    onInputClick() {
      if (this.disabled) return
      this.showDialog = true
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
  height: 500px;
  top: 50% !important;
  transform: translateY(-50%);

  &__body{
    .dialog-content{
      position: relative;
      padding-top: 5px;
      height: calc( 100% - 26px);
      overflow: auto;
      .el-input{
        position: absolute;
        left: 0;
        top: 0;
      }

    }
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
.el-dialog__body{
  padding: 0px;
  height: calc(100% - 110px);
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
  height: 500px;
}
.left-panel{
  flex: 0.4;
  padding: 15px;
  padding-right: 5px;
  border: 1px solid #e4e7ed;
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
