<!--
 * @Author: BanLi
 * @Date: 2021-03-11 12:10:51
 * @LastEditTime: 2021-08-10 13:45:06
 * @Description: 动态字段
-->

<template>
  <el-form
    ref="form"
    :model="form"
    :label-width="labelWidth"
    :disabled="disabled"
    v-bind="$attrs"
  >
    <el-row
      class="dynamic-field margin-bottom"
      :class="{ 'form-disabled': disabled }"
    >
      <el-col
        v-for="item of computedAttrs"
        :key="_setFieldsKey(item)"
        :style="getStyle(item)"
      >
        <el-form-item
          v-if="item.visible"
          :prop="item[valueField]"
          :rules="isRules ? generateRule(item) : []"
          :class="[item.highlight ? 'highlight' : '']"
        >
          <template #label>
            <span>{{ labelRender(item) }}</span>
            <el-tooltip
              v-if="item.description"
              :content="descriptionRender(item)"
              placement="top-start"
            >
              <i class="active-color el-icon-info" />
            </el-tooltip>
          </template>
          <component
            :is="getComponent(item)"
            v-model.trim="form[item[valueField]]"
            v-bind="getProps(item)"
            :source="item"
            :attrs="attrs"
            :row="form"
            @clearValidate="clearValidate"
          />
        </el-form-item>
      </el-col>
    </el-row>
  </el-form>
</template>

<script>
import { parseString } from '@/utils'
import { transDescription, transDisplayName } from '@/utils/lang'

import { generateRule, getComponent, getProps, getStyle } from './config'

export default {
  provide() {
    return { trFieldConfig: this }
  },
  model: { prop: 'form' },
  props: {
    form: {
      type: Object,
      default: () => ({}),
      comment: '动态字段列表'
    },
    attrs: {
      type: Array,
      default: () => [],
      comment: '对象的所有属性'
    },
    labelWidth: {
      type: String,
      default: '100px',
      comment: 'form label标签宽度'
    },
    labelField: {
      type: String,
      default: 'displayName',
      comment: 'label 字段'
    },
    valueField: {
      type: String,
      default: 'innerName',
      comment: 'prop 字段'
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
    isRules: {
      type: Boolean,
      default: true,
      comment: '是否启用校验'
    }
  },
  data() {
    return {
      currency: 1 // 货币的汇率
    }
  },
  computed: {
    computedAttrs({ attrs }) {
      return attrs.map((field) => {
        field.constraint = parseString(field.constraint)

        return field
      })
    }
  },
  mounted() {
    this.initFormEvent()
  },

  methods: {
    generateRule,
    getComponent,
    getStyle,
    getProps,
    initFormEvent() {
      const methods = ['validate', 'validateField', 'resetFields', 'clearValidate']

      this.$nextTick(() => {
        const form = this.$refs.form

        methods.forEach((method) => {
          this[method] = (...args) => form[method].apply(form, args)
        })
      })
    },
    clearValidate() {
      this.$refs.form.clearValidate()
    },
    _setFieldsKey(field) {
      return field[this.valueField] + field[this.labelField]
    },
    labelRender(item) {
      const { displayName, innerName, objBid } = item

      return transDisplayName(objBid, innerName, displayName)
    },
    descriptionRender(item) {
      const { description, innerName, objBid } = item

      return transDescription(objBid, innerName, description)
    }
  }
}
</script>

<style lang="scss" scoped>
.dynamic-field {
  display: flex;
  flex-wrap: wrap;
  ::v-deep .highlight{
    .el-input.is-disabled .el-input__inner{
      background: var(--primary-8) !important;
    }
  }

}
</style>
