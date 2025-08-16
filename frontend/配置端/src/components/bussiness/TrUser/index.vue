<template>
  <div class="tr-user">
    <el-tooltip
      placement="top"
      effect="light"
      :content="tooltipContent"
      :open-delay="600"
      popper-class="tr-user-tooltip"
      :disabled="inputScrollWidth <= inputClientWidth"
    >
      <el-input
        ref="trUser"
        :value="userShow.substring(0, 200)"
        v-bind="$attrs"
        :clearable="clearable"
        :title="userShow.substring(0, 200)"
        :class="{ block: $attrs.block,'white-back': backClass }"
        :placeholder="$t('component.trUser.placeholder')"
        @clear="handleClear"
        @focus="showUserDialog"
      >
        <i
          v-if="showIcon"
          slot="prefix"
          class="el-icon-user-solid el-input__icon"
        />
      </el-input>
    </el-tooltip>
    <user-dialog
      v-if="visible"
      v-model="visible"
      :default-user="user"
      v-bind="$attrs"
      :un-recover-list="unRecoverList"
      @userSelect="userSelect"
    />
  </div>
</template>

<script>
import { parseString, typeOf } from '@/utils/index.js'
import HttpService from './api'
import UserDialog from './components/UserDialog'

const httpService = new HttpService()

export default {
  name: 'TrUser',
  components: { UserDialog },
  provide() {
    return {
      httpService,
      multiple: this.multiple
    }
  },
  inheritAttrs: false,
  props: {
    value: {
      type: [String, Object, Array, Number],
      default: ''
    },
    multiple: {
      type: Boolean,
      default: false
    },
    clearable: {
      type: Boolean,
      default: true,
      command: '是否显示清除图标'
    },
    showIcon: {
      type: Boolean,
      default: true,
      command: '是否显示人员图标'
    },
    attrs: {
      type: Array,
      default: () => [],
      command: '所有动态字段'
    },
    unRecoverList: {
      type: Array,
      default: () => []
    },
    row: {
      type: Object,
      default: () => ({}),
      command: '当前表格row'
    },
    backClass: {
      type: String,
      default: () => '',
      comment: '选择框的背景颜色'
    }
  },
  data() {
    return {
      httpService: null,
      visible: false,
      inputScrollWidth: 0,
      inputClientWidth: 0,
      maximumUser: 200
    }
  },
  computed: {
    user: {
      get({ value }) {
        let val = value

        if (typeOf(value) === 'string' && Array.isArray(parseString(value))) {
          val = parseString(value)
        }

        return val
      },
      set(value) {
        this.$emit('input', value)
        this.getInputWidth()
      }
    },
    multipleUser() {
      if (this.multiple && Array.isArray(this.user)) {
        return this.user
      }

      return this.user ? [this.user] : []
    },
    userShow() {
      return this.multipleUser
        .slice(0, this.maximumUser)
        .map((s) => s.name || s.employeeName)
        .join(';')
    },
    tooltipContent() {
      const str = this.multipleUser.length > this.maximumUser ? `...等 ${this.multipleUser.length} 位参与者` : ''

      return this.userShow + str
    }
  },
  watch: {
    value() {
      this.initValue()
    }
  },
  beforeUpdate() {
    //  this.setInitStatus()
    this.initValue()
  },
  created() {
    this.httpService = httpService
    this.initValue()
  },
  methods: {
    initValue() {
      const { value } = this

      if (
        !value ||
        typeOf(value) === 'object' ||
        (Array.isArray(value) && value.every((item) => typeof item === 'object'))
      ) {
        return
      }

      // 根据工号查询用户名称
      let params = ''

      if (['string', 'number'].includes(typeof value)) {
        params = value.toString()
        // 当是数组字符类型且都是数组里面都是对象
        if (typeOf(params) === 'string' && (Array.isArray(parseString(params)) && parseString(params).every((item) => typeof item === 'object'))) {
          return
        }
      } else {
        if (this.multiple) {
          params = value.join(',')
        } else {
          params = value.employeeNo
        }
      }

      if (!params.length) return

      this.httpService.queryUserInfoByEmployees(params).then(() => {
        const userMap = this.$store.state.user.userMap

        if (this.multiple) {
          let val = value

          if (typeof value === 'string') {
            val = value.split()
          }
          this.user = val.reduce((acc, item) => {
            acc.push({ employeeNo: item.toString(), employeeName: userMap[item.toString()] })

            return acc
          }, [])

          return
        }

        // value 有可能是数字
        this.user = { employeeNo: value.toString(), employeeName: userMap[value.toString()] }
      })
    },
    showUserDialog() {
      this.visible = true
    },
    userSelect(selectedUser) {
      this.user = this.multiple ? selectedUser : selectedUser[0]
      this.$emit('confirm', selectedUser)
    },
    handleClear() {
      this.user = this.multiple ? [] : {}
      this.$emit('clear', this.user)
    },
    getInputWidth() {
      this.$nextTick(() => {
        const input = this.$refs.trUser?.$el.querySelector('input')

        if (input) {
          this.inputScrollWidth = input.scrollWidth
          this.inputClientWidth = input.clientWidth
        }
      })
    }
  }
}
</script>

<style>
.tr-user-tooltip {
  max-width: 70%;
}

.white-back .el-input__inner {
  background-color: #fff !important;
  cursor: default !important;
}
</style>
