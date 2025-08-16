/**
@name: method
@description: 动态字段 方法和脚本注入代码
@description: 监听 form 变化，执行对应的脚本和方法
@author: jiaqiang.zhang
@time: 2021-09-01 10:28:38
**/
import fn from './fun'

export default {
  mixins: [fn],
  mounted() {
    // 方法初始化 赋值 只执行一次
    const unwatch = this.$watch(
      'form',
      (value) => {
        const obj2arr = Object.entries(value)

        if (!obj2arr.length) {
          unwatch && unwatch()

          return
        }
        obj2arr.forEach((item) => this.onChangeHandler(item[1], item[0]))
        obj2arr.length && unwatch && unwatch()
      },
      { immediate: true, deep: true }
    )
  },
  methods: {
    onChangeHandler(value = '', key) {
      const target = this.find(key)

      if (!target) return
      // 代码注入是否启用
      const isCodeEnable = target.constraint?.method?.isEnable ?? false
      // 方法信息，包含脚本code和方法list
      const method = target?.constraint?.method

      if (!method) return
      // 如果启用了脚本，执行脚本语言
      if (isCodeEnable) {
        if (method.content) this._runScript(method.content, value)
      } else {
        // 方法列表
        const methodList = method?.list

        if (methodList.length > 0 && !methodList[0]?.method) return
        const map = {}

        // 构建当前值和与之绑定的方法值映射表
        methodList.forEach((item) => {
          this._initState(item.controlComponent)
          if (map[item.currentValue]) {
            map[item.currentValue].push(item)
          } else {
            map[item.currentValue] = []
            map[item.currentValue].push(item)
          }
        })
        // 获取当前值绑定的方法列表
        const currentValueMethodList = map[value]

        if (!currentValueMethodList) return
        currentValueMethodList.forEach((item) => {
          if (item.controlComponent) {
            let param = ''

            if (item.controlValue === '是' || item.controlValue === '开') {
              param = 'true'
            } else if (item.controlValue === '否' || item.controlValue === '关') {
              param = 'false'
            } else {
              param = item.controlValue
            }
            if (item.method) this[item.method](item.controlComponent, param)
          } else {
            if (item.method) this[item.method](item.controlValue)
          }
        })
      }
    },
    // 执行脚本
    _runScript(code, value) {
      if (!code) return
      try {
        // eslint-disable-next-line no-new-func
        const fun = new Function('value', `with(this){${code}}`)

        fun.call(this, value)
      } catch (e) {
        this.$message.error('执行脚本失败：' + e)
      }
    },
    // 获取组件
    find(code) {
      return this.attrs.find((item) => item.innerName === code)
    },
    // 隐藏组件
    hide(code, flag) {
      const target = this.find(code)

      if (target) target.visible = !flag
    },
    readonly(code, flag) {
      const target = this.find(code)

      if (target) target.readonly = flag
    },
    // 显示组件
    show(code) {
      const target = this.find(code)

      if (target) target.visible = true
    },
    require(code, flag) {
      const target = this.find(code)

      target.required = flag
    },
    // 设置组件的值
    set(code, value) {
      this.form[code] = value
    },
    // 获取组件的值
    get(code) {
      return this.form[code]
    },
    _initState(code) {
      const target = this.find(code)

      if (!target) return
      target.visible = true
      target.required = false
      target.readonly = false
    }
  }
}
