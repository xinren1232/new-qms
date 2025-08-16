<template>
  <div
    class="color-switch-wrap"
    @click.stop.prevent="onUpdateModel"
  >
    <span
      v-if="value === invalidValue"
      class="status-text invalid-text"
      :class="_toggleActiveClass(invalidValue)"
    >
      {{ invalidText }}
    </span>
    <span
      v-else
      class="status-text inactive-text"
      :class="_toggleActiveClass(inactiveValue)"
    >
      {{ inactiveText }}
    </span>

    <!-- 主体切换 -->
    <span
      :class="[
        'color-switch select-none cursor-pointer overflow-hidden',
        { 'is-disabled': innerLoading || compositionDisabled },
        _toggleActiveClass(activeValue)
      ]"
      :style="{ fontSize: size, backgroundColor }"
    >
      <span class="color-switch__inner">
        <i
          v-show="innerLoading"
          class="el-icon-loading"
        />
      </span>
    </span>

    <span
      class="status-text active-text"
      :class="_toggleActiveClass(activeValue)"
    >
      {{ activeText }}
    </span>
  </div>
</template>

<script>
import i18n from '@/i18n'

export default {
  name: 'TrSwitch',
  inject: { elForm: { default: {} } },
  model: { prop: 'value' },
  props: {
    value: {
      type: [Boolean, String, Number],
      default: 'off',
      comment: '数据值'
    },
    size: {
      type: String,
      default: '',
      comment: '按钮大小尺寸'
    },
    loading: {
      type: Boolean,
      default: null,
      comment: '是否展示加载中'
    },
    disabled: {
      type: Boolean,
      default: false,
      comment: '是否禁用'
    },
    params: {
      type: Object,
      default: () => ({}),
      comment: '自定义改变状态函数的参数'
    },
    onChange: {
      type: [Promise, Function],
      default: null,
      comment: '当组件在切换时触发的钩子'
    },
    httpRequest: {
      type: [Promise, Function],
      default: null,
      comment: '自定义改变状态的函数'
    },
    activeColor: {
      type: String,
      default: 'var(--primary)',
      comment: '启用时的颜色'
    },
    invalidColor: {
      type: String,
      default: '#dcdfe6',
      comment: '未启用时的背景色'
    },
    inactiveColor: {
      type: String,
      default: '#ff4949',
      comment: '禁用时的背景色'
    },
    activeValue: {
      type: [Boolean, String, Number],
      default: 'enable',
      comment: '激活时的值'
    },
    invalidValue: {
      type: [Boolean, String, Number],
      default: 'off',
      comment: '未启用的值'
    },
    inactiveValue: {
      type: [Boolean, String, Number],
      default: 'disable',
      comment: '禁用时的值'
    },
    paramsCode: {
      type: String,
      default: 'stateCode',
      comment: '接口默认的传参值'
    },
    // statecode 存放层次
    paramsSet: {
      type: Array,
      default: () => []
    }
  },

  data() {
    return { innerLoading: this.loading }
  },

  computed: {
    // 获取当前组件或者是form表单的禁用状态
    compositionDisabled({ disabled, elForm }) {
      return disabled || !!elForm?.disabled
    },
    backgroundColor({ activeColor, activeValue, inactiveColor, inactiveValue, invalidColor, invalidValue, value }) {
      switch (value) {
        case activeValue:
          return activeColor
        case invalidValue:
          return invalidColor
        case inactiveValue:
          return inactiveColor
        default:
          return invalidColor
      }
    }
  },

  created() {
    this.invalidText = i18n.t('common.unenable')
    this.inactiveText = i18n.t('common.disabled')
    this.activeText = i18n.t('common.enable')
    this.stateMap = {
      [this.activeValue]: i18n.t('common.enable'),
      [this.inactiveValue]: i18n.t('common.disabled'),
      [this.invalidValue]: i18n.t('common.unenable')
    }

    // 如果传入了 loading prop 属性
    if (this.loading !== null) {
      this.$watch(
        'loading',
        (value) => {
          this.innerLoading = value
        },
        { immediate: true }
      )
    }
  },

  methods: {
    _toggleActiveClass(value) {
      return { 'is-active': this.value === value }
    },

    async onUpdateModel() {
      const { activeValue, compositionDisabled, innerLoading, onChange, value } = this

      if (compositionDisabled || innerLoading || (onChange !== null && !(await onChange()))) return

      if (value === this.invalidValue) {
        this.$confirm(i18n.t('switch.sub_title'), i18n.t('switch.title'), {
          confirmButtonText: i18n.t('common.confirm'),
          cancelButtonText: i18n.t('common.cancel'),
          type: 'warning'
        })
          .then(() => {
            this.updateModelData(activeValue)
          })
          .catch((e) => e)
      } else {
        this.updateModelData(value === activeValue ? this.inactiveValue : activeValue)
      }
    },

    updateModelData(stateCode) {
      const { activeValue, inactiveValue, value } = this

      const params = { ...this.params }
      let changeParams = params

      this.paramsSet.forEach((item) => {
        changeParams = changeParams[item]
      })
      changeParams[this.paramsCode] = stateCode
      if (this.httpRequest) {
        this.innerLoading = true
        this.httpRequest(params)
          .then(({ data }) => {
            if (data) {
              this.$message.success(`状态成功修改为${this.stateMap[stateCode]}`)
              this.$emit('input', value === activeValue ? inactiveValue : activeValue)
            }
          })
          .finally(() => {
            this.innerLoading = false
          })
      } else {
        this.$emit('input', stateCode)
      }
    }
  }
}
</script>

<style lang="scss" scoped>
.active {
  color: var(--primary);
}

.status-text {
  display: inline-block;
  width: 56px;
  font-size: 13px;
  user-select: none;
  text-align: center;

  &.is-active {
    color: var(--primary);
  }
}

.color-switch {
  display: inline-block;
  vertical-align: middle;
  width: 2em;
  height: 1em;
  margin: 0 10px;
  font-size: 22px;
  border-radius: 2em;
  transition: background-color 0.2s linear;

  // 禁用
  &.is-disabled {
    opacity: 0.5;
    cursor: not-allowed;
  }

  // 激活
  &.is-active .color-switch__inner {
    transform: translateX(1em);
  }

  // 内部默认
  &__inner {
    display: flex;
    align-items: center;
    justify-content: center;
    width: 1em;
    height: 1em;
    border-radius: 50%;
    background: #fff;
    background-clip: content-box;
    transition: transform 0.2s linear;
    border: 0.1em solid transparent;

    .el-icon-loading {
      font-size: 0.5em;
    }
  }
}
</style>
