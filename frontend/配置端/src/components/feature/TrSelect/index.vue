<template>
  <el-select
    ref="select"
    v-model="modelValue"
    :multiple="multiple"
    :clearable="clearable"
    :value-key="valueField"
    :filterable="filterable"
    :collapse-tags="collapseTags"
    :placeholder="placeholderText"
    v-bind="$attrs"
    v-on="$listeners"
    @clear="onClear"
    @keydown.enter.native.stop
  >
    <el-option
      v-if="!$attrs.isKeyValue && showSelectAll"
      class="check-all-wrap"
      value
    >
      <el-checkbox
        v-model="isAllSelected"
        :indeterminate="indeterminateStatus"
        class="check-all-button"
        @input="onAllSelectedChange"
        @click.native.stop
      >
        {{ $t('common.addAll') }}
      </el-checkbox>
    </el-option>
    <el-option
      v-for="(item, i) in options"
      :key="_setOptionsKey(item, i)"
      class="vxe-table--ignore-clear"
      :label="item[labelField]"
      :value="getItemValue(item)"
      :disabled="item.disabled"
    >
      <span
        v-if="$attrs.isKeyValue"
        style="float: left; margin-right: 20px"
      >
        {{ item[labelField] }}
      </span>
      <span
        v-if="$attrs.isKeyValue && item.tips"
        style="float: right; color: #8492a6; font-size: 13px"
      >
        {{ item['tips'] }}
      </span>
      <span
        v-else-if="$attrs.isKeyValue && item.key"
        style="float: right; color: #8492a6; font-size: 13px"
      >
        {{ item['key'] }}
      </span>
    </el-option>
  </el-select>
</template>

<script>
import { typeOf } from '@/utils/index.js'

export default {
  inject: {
    elForm: {
      default: ''
    }
  },
  inheritAttrs: false,
  props: {
    value: {
      type: [String, Number, Array, Object, Boolean],
      default: '',
      comment: 'model数据值'
    },
    // 展示的数据列
    data: {
      type: [Array, Function],
      default: null
    },
    multiple: {
      type: Boolean,
      default: false,
      comment: '是否开启多选'
    },
    collapseTags: {
      type: Boolean,
      default: false,
      comment: '多选内容折叠显示'
    },
    clearable: {
      type: Boolean,
      default: true,
      comment: '是否开启一键清除'
    },
    filterable: {
      type: Boolean,
      default: true,
      comment: '是否开启搜索过滤'
    },
    labelField: {
      type: String,
      default: 'label',
      comment: '从对象中以哪个键名作为数据展示'
    },
    valueField: {
      type: String,
      default: 'value',
      comment: '从对象中以哪个键名作为数据发送'
    },
    valueType: {
      type: String,
      default: 'string',
      comment: '返回值的对象类型：string/object'
    },
    hiddenSelectAll: {
      type: Boolean,
      default: false,
      comment: '是否隐藏选择全部选项'
    },
    attrs: {
      type: Array,
      default: () => [],
      command: '所有动态字段'
    }
  },
  data() {
    return {
      options: [],
      isAllSelected: false
    }
  },

  computed: {
    modelValue: {
      get({ value }) {
        return value
      },
      set(value) {
        this.$emit('input', value)
        this.isAllSelected = value ? value.length && value.length >= this.data.length : false
      }
    },
    // 选中了一部分选项的时候展示位半选状态
    indeterminateStatus() {
      if (!this.value) return false

      return !!(this.value.length && this.value.length < this.data.length)
    },
    placeholderText({ $attrs, elForm }) {
      return $attrs.disabled || elForm?.disabled ? '' : this.$t('common.pleaseSelect')
    },
    showSelectAll() {
      return this.multiple && !this.hiddenSelectAll && this.data.length
    }
  },

  watch: {
    data: {
      immediate: true,
      handler: '_initData'
    }
  },

  methods: {
    async _initData(datalist) {
      datalist = datalist || []

      if (typeOf(datalist) === 'function') {
        this.options = await datalist()
      } else {
        // 删除if(datalist.length) 设置联动时还原options为[]
        this.options = datalist
      }
    },
    _setOptionsKey(option, idx) {
      return option[this.labelField] + option[this.valueField] + idx
    },
    // 选择全部数据
    onAllSelectedChange(selected) {
      let selectedOptions

      if (selected) {
        const { data, valueField, valueType } = this

        selectedOptions = data.reduce((acc, option) => {
          // 确保不会选中已禁用的数据
          if (!option.disabled) {
            acc.push(valueType === 'object' ? option : option[valueField])
          }

          return acc
        }, [])
      } else {
        selectedOptions = []
      }
      this.modelValue = selectedOptions
    },
    getItemValue(item) {
      if (this.valueType === 'object') {
        return item
      }

      return item[this.valueField]
    },
    onClear() {
      this.$emit('input', this.multiple ? [] : '')
    }
  }
}
</script>

<style lang="scss" scoped>
.check-all-wrap {
  padding: 0;

  &::after {
    display: none;
  }

  .check-all-button {
    width: 100%;
    padding-left: 20px;
  }
}
</style>
