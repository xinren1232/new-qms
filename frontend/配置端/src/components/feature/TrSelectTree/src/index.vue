<template>
  <div
    v-outside-click="_onCancelActive"
    class="tr-select-tree"
  >
    <div
      class="tr-select-tree__select"
      :class="{ 'is-focus': isFocus, 'is-disabled': disabled }"
      @click="_onWakeUpTree"
    >
      <!-- CHOOSED TAGS -->
      <div
        v-if="multiple"
        class="tr-select-tree__tags"
      >
        <el-tag
          v-for="tag of modelValue"
          :key="tag[nodeKey]"
          type="info"
          size="mini"
          closable
          disable-transitions
          @close="_onClearTreeAndTag(tag)"
          @dblclick.native.prevent="_onClearTreeAndTag(tag)"
        >
          {{ tag[treePropConfig.label] }}
        </el-tag>
      </div>

      <!-- MULTIPLE SELECTION IS TURNED OFF -->
      <div
        v-else
        class="tr-select-tree__radio-text"
      >
        <span v-if="showKeyword">{{ modelValue[treePropConfig.label] }}</span>
      </div>

      <!-- FILTER DATA INPUT -->
      <div
        v-if="filterable"
        class="tr-select-tree__input"
      >
        <input
          ref="filterInput"
          v-model="filterKeyword"
          :placeholder="placeholder"
          @keydown="_onInputKeydown"
        >
      </div>
      <i
        v-if="clearable"
        class="el-icon-close"
        @click.stop="clearData"
      />
    </div>

    <!-- TRANSITION TREE LIST -->
    <transition name="el-zoom-in-top">
      <el-tree
        v-show="isFocus"
        ref="tree"
        class="tr-select-tree__tree"
        :data="data"
        :props="treePropConfig"
        :expand-on-click-node="false"
        :filter-node-method="_filterNodeMethod"
        v-bind="$attrs"
        v-on="$listeners"
        @node-click="selectData"
      >
        <template slot-scope="scope">
          <slot name="tree-node">
            <div
              :class="['custom-tree-node', { 'is-active': _isActiveNode(scope), 'is-disabled': scope.data.disabled }]"
            >
              {{ scope.data[treePropConfig.label] }}
            </div>
          </slot>
        </template>
      </el-tree>
    </transition>
  </div>
</template>

<script>
import OutsideClick from 'element-ui/src/utils/clickoutside'

import methods from './methods'

export default {
  name: 'TrSelectTree',
  directives: { OutsideClick },
  inheritAttrs: false,
  props: {
    value: {
      type: [String, Object, Array],
      default: '',
      comment: '数据源'
    },
    data: {
      type: Array,
      default: () => [],
      comment: '数据列表'
    },
    nodeKey: {
      type: String,
      default: 'bid',
      comment: '数据的唯一标识，必须要保证唯一'
    },
    multiple: {
      type: Boolean,
      default: false,
      comment: '是否可以多选'
    },
    disabled: {
      type: Boolean,
      default: false,
      comment: '是否禁用'
    },
    clearable: {
      type: Boolean,
      default: true,
      comment: '是否显示一键清除'
    },
    filterable: {
      type: Boolean,
      default: true,
      comment: '是否可筛选输入'
    },
    // valueType: {
    //   type: String,
    //   default: 'object',
    //   comment: '数据列表中的数据类型'
    // },
    placeholder: {
      type: String,
      default: '',
      comment: '提示文本'
    },
    labelField: {
      type: String,
      default: 'name',
      comment: '作为展示的数据字段'
    },
    childrenField: {
      type: String,
      default: 'childList',
      comment: '子级节点集合的字段名'
    },
    onSelect: {
      type: [Promise, Function, Boolean],
      default: null,
      comment: '选中节点前的事件钩子'
    },
    filterData: {
      type: Function,
      default: (v) => v,
      comment: '选中数据的时候，对数据进行过滤或者操作的事件钩子，必须有返回值'
    }
  },

  data() {
    return {
      isFocus: false,
      filterKeyword: '',
      showKeyword: true
    }
  },

  computed: {
    modelValue: {
      get: ({ value }) => value,
      set(value) {
        this.$emit('input', value)
      }
    },
    treePropConfig({ childrenField, labelField, nodeKey }) {
      return { key: nodeKey, label: labelField, children: childrenField }
    }
  },

  watch: { filterKeyword: '_debounceFilters' },

  created() {
    this._initNonreactive()
    this._initStaticMethods()

    this.$once('hook:mounted', () => {
      this._initOwnTreeMethods((this.tree = this.$refs.tree))
    })
  },

  methods: {
    _initNonreactive() {
      this.tree = null
      this.timer = null
    },
    _initStaticMethods() {
      this._filterNodeMethod = (value, data) => {
        if (!value) return true

        return String(data[this.labelField]).toLowerCase().includes(value.toLowerCase())
      }
    },

    // 把 Tree 组件向外提供的所有事件挂在到当前组件上 可直接调用
    _initOwnTreeMethods(tree) {
      for (const method of methods) {
        this[method] = (...args) => tree[method](...args)
      }
    },
    // 当前节点是否被选中
    _isActiveNode({ data }) {
      const key = this.nodeKey

      return this.multiple ? this._hasCurrentData(data) : this.modelValue && this.modelValue[key] === data[key]
    },
    // 节点过滤防抖
    _debounceFilters(keyword) {
      this.timer && clearTimeout(this.timer)
      this.timer = setTimeout(this.filter, 300, keyword)
    },
    // 清除数据的选中
    _onClearTreeAndTag(tag) {
      const key = this.nodeKey

      this.modelValue = this.multiple ? this.modelValue.filter((item) => item[key] !== tag[key]) : ''
    },
    // 判断数据是否已经勾选
    _hasCurrentData(data) {
      const { modelValue, nodeKey } = this

      if (!modelValue) return false

      return this.multiple
        ? !!modelValue.find((it) => it[nodeKey] === data[nodeKey])
        : modelValue[nodeKey] === data[nodeKey]
    },
    // 根据 valueType 来转换数据
    // _transferData(data) {
    //   if (this.valueType === 'string') {
    //     this._transferData = (data) => data[this.nodeKey]
    //   } else if (this.valueType === 'Object') {
    //     this._transferData = (data) => data
    //   }
    // },
    // 唤醒下拉框
    _onWakeUpTree() {
      this.isFocus = true
      this.filterable && this.$refs.filterInput.focus()
    },
    // 取消下拉框
    _onCancelActive() {
      this.isFocus = false
      this.showKeyword = true
      this.filterKeyword = ''
    },
    _onInputKeydown() {
      this.showKeyword = false
    },

    // 点击节点
    async selectData(data) {
      // 如果组件或者节点被禁用 或者选择节点的事件钩子返回了一个 falsy 的值 那么则不进行选中
      if (this.disabled || data.disabled || (this.onSelect && !(await this.onSelect(data)))) return

      data &&= this.filterData(data) ?? data

      if (this.multiple) {
        this.filterKeyword = ''
        if (this._hasCurrentData(data)) {
          this._onClearTreeAndTag(data)

          return
        }

        this.modelValue.push(data)

        return
      }

      this.modelValue = this.modelValue[this.nodeKey] === data[this.nodeKey] ? '' : data
      this._onCancelActive()
    },
    // 清除数据
    clearData() {
      this.modelValue = this.multiple ? [] : ''
      this._onCancelActive()
    }
  }
}
</script>

<style lang="scss" scoped>
@import './index.scss';
</style>
