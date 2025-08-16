<!--
 * @Author: BanLi
 * @Date: 2021-03-11 12:10:51
 * @LastEditTime: 2021-08-11 14:36:52
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
    <template v-for="group of computedAttrs">
      <component
        :is="group.name ? 'tr-card' : 'div'"
        v-if="!group.hide"
        :key="group.name"
        :expand="group.expand"
        :header="groupRender(group)"
      >
        <el-row
          class="dynamic-field margin-bottom"
          :class="{ 'form-disabled': disabled, anonymous: !group.name }"
        >
          <el-col
            v-for="item of group.children"
            :key="_setFieldsKey(item)"
            :style="getStyle(item)"
          >
            <el-form-item
              v-if="item.visible"
              :prop="item[valueField]"
              :rules="generateRule(item)"
            >
              <template #label>
                {{ labelRender(item) }}
                <el-tooltip
                  v-if="item.description"
                  placement="top-start"
                  :content="descriptionRender(item)"
                >
                  <!--                  <span slot="content" v-html="autoNewline(item.description)" />-->
                  <i class="active-color el-icon-info" />
                </el-tooltip>
              </template>
              <component
                :is="getComponent(item)"
                :ref="item[valueField]"
                v-model.trim="form[item[valueField]]"
                v-bind="getProps(item)"
                :source="item"
                :attrs="attrs"
                :row="form"
                v-on="item.listeners"
                @clearValidate="clearValidate"
              />
            </el-form-item>
          </el-col>
        </el-row>
      </component>
    </template>
  </el-form>
</template>

<script>
import { getConversion } from '@/api/object-manage'
import { parseString } from '@/utils'
import { transDescription, transDisplayName, transGroup } from '@/utils/lang'

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
    currentNode: {
      type: String,
      default: '',
      command: '当前节点'
    }
  },
  data() {
    return {
      currency: 1 // 货币的汇率
    }
  },

  computed: {
    computedAttrs({ attrs }) {
      const groups = []
      let group, target, objBid, innerName, hide

      attrs.forEach((field, i) => {
        field.constraint = parseString(field.constraint)
        group = field.constraint?.group ?? ''
        hide = field.constraint?.hide
        objBid = field.objBid
        innerName = field.innerName
        if ((target = groups.find((it) => it.name === group))) {
          target.children.push(field)
        } else {
          groups.push({ name: group, objBid, innerName, expand: !(i ^= 0), hide, children: [field] })
        }
        // this.onChangeHandler(this.form[field.innerName], field.innerName)
      })

      return groups
    }
  },

  watch: {
    // 如果修改了货币的选项则发起汇率转换的请求
    'form.Currency': {
      immediate: true,
      async handler(value) {
        // 如果没有设置则默认以人民币为单位
        this.currency = (await getConversion(value || 'CNY')) || 1
      }
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
    autoNewline(content) {
      const BASE = 15

      if (content.length <= BASE) return content

      const result = []
      const lines = Math.ceil(content.length / BASE)

      for (let i = 0; i < lines; i++) {
        result.push(content.substr(i, BASE))
      }

      return result.join('<br/>')
    },
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
    groupRender(group) {
      const { name, objBid } = group

      return transGroup(objBid, name)
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
::v-deep .el-card .el-card__body {
  padding-left: 0;
}
.dynamic-field {
  display: flex;
  flex-wrap: wrap;

  &.anonymous {
    padding-top: 10px;
    border-top: 1px solid #ebeef5;
  }
}
</style>
