<template>
  <el-form
    ref="form"
    :model="form"
    class="tr-form"
    :label-width="labelWidth"
    v-bind="$attrs"
    :disabled="disabled"
    :rules="disabled ? null : rules"
    :label-position="isMobile ? 'top' : labelPosition"
    :class="{ 'form-disabled': disabled }"
    @submit.native.prevent="onClickQuery"
  >
    <!-- 用于 hack 按下回车后提交表单 -->
    <input
      v-if="enterSubmit"
      type="submit"
      hidden="hidden"
      style="display: none; width: 0; height: 0"
    >
    <el-row class="tr-form-row">
      <slot name="before" />

      <el-col
        v-for="(item, index) of formatData"
        v-show="isExpand || index < fixedItemsCount"
        :key="item.prop + index"
        :span="item.span"
        :class="{ 'form-disabled': item.attrs.disabled }"
        v-bind="_calcColPosition(item)"
      >
        <el-form-item
          v-if="item.show"
          :prop="item.prop"
          :label-width="item.labelWidth"
          :rules="disabled ? null : item.rules"
        >
          <template #label>
            <slot :name="`label-${item.prop}`">
              <span v-html="item.label" />
            </slot>
          </template>

          <slot
            :name="item.prop"
            :item="item"
          >
            <component
              :is="_getComponent(item)"
              :ref="item.ref"
              :value="getValue(item.prop)"
              v-bind="{
                ..._getComponentProp(item.type),
                ...item.attrs,
                title: _getComponentTitle(form[item.prop])
              }"
              :rules="disabled || item.attrs.disabled ? null : item.rules"
              @input="setValue(item.prop, $event)"
              v-on="item.listeners"
            />
          </slot>
        </el-form-item>
      </el-col>

      <slot name="after" />

      <el-col
        v-if="showButtons"
        :span="states.buttonSpan"
        v-bind="_buttonPosition"
        class="tr-form-item__operate text-right"
      >
        <el-button
          type="primary"
          native-type="submit"
        >
          {{ $t('common.search') }}
        </el-button>
        <el-button
          v-debounce-click="onClickReset"
          plain
          type="primary"
        >
          {{ $t('common.reset') }}
        </el-button>
        <el-button
          v-if="collapsible"
          type="text"
          class="expand-button"
          @click="handleUpdateValue"
        >
          <span class="status-text">{{ statusText }}</span>
          <i :class="isExpand ? 'el-icon-arrow-up' : 'el-icon-arrow-down'" />
        </el-button>
        <slot name="extra" />
      </el-col>
    </el-row>
  </el-form>
</template>

<script>
import { debounce, get } from 'lodash'

import { isMobile } from '@/utils'

import { getComponent, getComponentProp } from './config/factory'

export default {
  name: 'TrForm',
  model: { prop: 'form' },
  props: {
    form: {
      type: Object,
      default: () => ({}),
      comment: '数据源'
    },
    items: {
      type: Array,
      default: () => []
      /**
       * 表单数据项, 数据项如下:
        {
          prop: '', // 绑定的字段
          label: '', // 标签文本
          span: 6, // 占据栅格的位置, 默认为 6
          pull: 0, // 栅格向左移动格数
          push: 0, // 栅格向右移动格数
          offset: 0, // 栅格左侧的间隔格数
          margin: 0, // 栅格右侧的间隔格数 (自定义扩展属性)
          labelWidth: 100px, // form-item的label宽度, 默认继承于form组件的属性
          type: 'input', // 展示的组件, 默认input, 其他预设值: [input, date, time, checkbox, dateYMD, datetime], 除此之外, 还可以传入ElRate, ElSwitch,ElSlider等任何挂在到全局的组件
          component: null, // 自定义传入的组件 当都满足不了需求 那么可以放入自定义组件来渲染自定义组件
          listeners: {}, // 监听的事件名, 比如 input focus blur, 或者是组件的自定义事件
          attrs: {}, // 对显示的组件需要设置的属性, 比如 placeholder type disabled 属性, 支持原生element表单属性
        }
      */
    },
    rules: {
      type: Object,
      default: null,
      comment: '校验规则'
    },
    disabled: {
      type: Boolean,
      default: false,
      comment: '是否禁用整个表单'
    },
    labelPosition: {
      type: String,
      default: 'right',
      comment: '表单域标签的位置，如果值为 left 或者 right 时，则需要设置 label-width'
    },
    showButtons: {
      type: Boolean,
      default: true,
      comment: '是否显示查询/重置/[展开|收起]按钮'
    },
    buttonPosition: {
      type: Object,
      default: null,
      comment: '按钮位置'
    },
    collapsible: {
      type: Boolean,
      default: true,
      comment: '是否可折叠，显示[展开|收起]按钮'
    },
    defaultExpand: {
      type: Boolean,
      default: true,
      comment: '默认是否展开'
    },
    fixedItemsCount: {
      type: Number,
      default: 2,
      comment: '固定展示(收起面板以后)展示多少个表单项'
    },
    enterSubmit: {
      type: Boolean,
      default: false,
      comment: '按下回车以后视为提交表单'
    }
  },
  data() {
    return {
      isExpand: this.defaultExpand, // 组件的展开/关闭状态
      states: {
        buttonSpan: 6, // 按钮模块占用的位置
        buttonPull: 0, // 按钮模块收起以后向左移动的格数
        buttonPush: 0 // 按钮模块收起以后向右移动的格数
      }
    }
  },

  computed: {
    isMobile() {
      return isMobile()
    },
    // v-for数据源
    formatData() {
      const DEFAULT_CONFIG = {
        span: 6,
        show: true,
        type: 'input',
        position: {},
        listeners: {},
        attrs: {}
      }
      // 补充缺失的字段属性 并过滤不显示的数据
      const formatData = this.items.reduce((acc, item) => {
        if (!item.hide) {
          acc.push(Object.assign({}, DEFAULT_CONFIG, item))
        }

        return acc
      }, [])

      return formatData
    },
    labelWidth() {
      return this.$attrs['label-width'] || '100px'
    },
    statusText() {
      return this.isExpand ? this.$t('common.folding') : this.$t('common.stretch')
    },
    // 计算偏移位置
    _buttonPosition() {
      const total = this.formatData.reduce((prev, item) => {
        prev += item.span + ~~item.push + ~~item.offset - ~~item.pull - ~~item.margin

        return prev
      }, 0)

      // 如果是展开的情况下 那么置于最右
      if (this.isExpand) {
        // 总空间数量减去自身占位再减去剩余空间
        return { push: 24 - this.states.buttonSpan - (total % 24) }
      } else {
        // 如果是收起状态 设置为偏移的位置
        const nums = this.states.buttonPull - this.states.buttonPush

        if (nums === 0) {
          return { push: 24 - this.states.buttonSpan - (total % 24) }
        } else if (nums > 0) {
          return { pull: nums }
        } else {
          return { push: -nums }
        }
      }
    }
  },

  created() {
    this.buttonPosition && (this.states = this.buttonPosition)
  },

  mounted() {
    this.initMethods()
  },

  methods: {
    getValue(prop) {
      return get(this.form, prop)
    },
    setValue(prop, value) {
      const _props = prop.split('.')

      // 如果只有一个属性则直接赋值
      if (_props.length <= 1) {
        this.$set(this.form, prop, value)

        return
      }

      // 如果有多个属性则进行递归赋值
      let obj = this.form
      // 取出最后一项属性名
      const key = _props.pop()

      _props.forEach((key) => {
        // 如果没有这个属性 那么就设置这个属性
        if (!(key in obj)) {
          this.$set(obj, key, {})
        }

        obj = obj[key]
      })

      this.$set(obj, key, value)
      console.log(obj, value)
    },
    // 集成el-form的方法
    initMethods() {
      const methods = ['validate', 'validateField', 'resetFields', 'clearValidate']
      const form = this.$refs.form

      methods.forEach((method) => {
        this[method] = (...args) => form[method].apply(form, args)
      })
    },
    // 切换组件表单的显示隐藏状态
    handleUpdateValue() {
      this.isExpand = !this.isExpand
      this.$emit('toggle', this.isExpand ? 'close' : 'open')
    },
    onClickQuery: debounce(async function () {
      this.$emit('query', this.form)
    }, 300),
    onClickReset() {
      this.formatData.forEach((item) => {
        if (item.prop) {
          this.$set(this.form, item.prop, '')
        }
      })
      this.resetFields()
      this.$emit('reset')
    },

    _getComponent(item) {
      return getComponent(item)
    },
    _getComponentProp(type) {
      return getComponentProp(type)
    },
    // 获取表单项在鼠标滑过时的提示文本
    _getComponentTitle(value = '') {
      value = value ? value.toString() : ''
      if (value.startsWith('[') && value.endsWith(']')) return ''

      return value
    },
    // 设置col组件的pull push offset margin
    _calcColPosition(item) {
      return {
        push: item.push,
        pull: item.pull,
        offset: item.offset,
        margin: item.offset,
        style: { marginRight: `${(100 / 24) * item.margin}%` }
      }
    }
  }
}
</script>

<style lang="scss" scoped>
.tr-form {
  margin-bottom: 15px;

  &-item__operate {
    position: relative;
  }

  &-row {
    display: flex;
    flex-wrap: wrap;
  }
  .el-form-item{
    margin-bottom: 16px;
  }
}

.expand-button i {
  width: 12px;
}
</style>
