<template>
  <el-cascader
    v-model="selectData"
    :options="data"
    :disabled="disabled"
    :placeholder="placeholderText"
    :clearable="clearable"
    :show-all-levels="false"
    filterable
    :collapse-tags="collapseTags"
    :props="{
      multiple: multiple,
      value: valueField,
      label: labelField,
      children: childrenField,
      expandTrigger: 'hover',
      checkStrictly: true,
      emitPath: false
    }"
    v-bind="$attrs"
    @change="onInput"
    v-on="$listeners"
  />
</template>

<script>
import request from '@/app/request'

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
    multiple: {
      type: Boolean,
      default: false,
      comment: '是否开启多选'
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
      default: 'name',
      comment: '从对象中以哪个键名作为数据展示'
    },
    valueField: {
      type: String,
      default: 'bid',
      comment: '从对象中以哪个键名作为数据发送'
    },
    childrenField: {
      type: String,
      default: 'childRoles',
      comment: '从对象中以哪个键名作为子级'
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
    },
    domainRole: {
      type: Boolean,
      default: true,
      commend: '是否从域中选择角色，默认true，会在接口中添加域ID，false默认是系统全量角色'
    },
    disabled: {
      type: Boolean,
      default: false,
      command: '是否禁用'
    },
    levelNum: {
      type: Number,
      default: Infinity,
      command: '数据显示层级数'
    }
  },
  data() {
    return {
      selectData: null,
      data: [],
      level: this.levelNum
    }
  },
  computed: {
    placeholderText({ $attrs, elForm }) {
      return $attrs.disabled || elForm?.disabled ? '' : this.$t('common.pleaseSelect')
    },
    collapseTags({ selectData }) {
      return selectData?.length > 3
    }
  },

  watch: {
    value: {
      immediate: true,
      handler(value = '') {
        this.selectData = value
      }
    }
  },
  mounted() {
    this.getRoleData()
  },
  methods: {
    onInput(value) {
      this.$emit('input', value)
    },
    onClear() {
      this.$emit('input', this.multiple ? [] : '')
    },
    getRoleData() {
      const domainBid = this.domainRole ? this.$attrs?.['domain-bid'] ?? '' : ''

      request.post(process.env.VUE_APP_BASE_API_2 + 'ipm/role/findRoleTree', { objInsBid: domainBid }).then((data) => {
        this.data = data?.data ?? []
        this.rewriteChildren(this.data, this.level)
      })
    },
    // 处理el-cascader children为空数组显示暂无数据问题
    rewriteChildren(data, level) {
      data?.forEach((item) => {
        if (level - 1 <= 0) {
          item.childRoles = undefined
        } else {
          item.childRoles = item?.childRoles.length !== 0 ? item?.childRoles : undefined
          if (item?.childRoles) {
            this.rewriteChildren(item.childRoles, level - 1)
          }
        }
      })
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
