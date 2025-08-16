// noinspection JSConstantReassignment

import '@/assets/css' // 全局样式
import '@/directives' // 自定义指令
import '@/router/guard' // 路由权鉴

import Element from 'element-ui'
import { debounce } from 'lodash'
// 微前端接入
import WujieVue from 'wujie-vue2'
import EventBus from '@/utils/event'
import TrCard from '@@/feature/TrCard/index.js'
import DictLabel from '@@/plm/dict-label/index.js'
// VXE Table
import { installTable } from '@@/feature/TrTableX'
import SVGIcon from '@@/plm/TrSvgIcon'
import i18n from '@/i18n' // SVG 图标

// 修改element-ui提示框始终可以手动关闭
const rawMessage = Element.Message
// 添加防抖
const Message = debounce((options) => rawMessage({ showClose: true, ...options }), 300)

;['success', 'warning', 'info', 'error'].forEach(function (type) {
  Message[type] = debounce(function (options) {
    if (typeof options === 'string') {
      options = {
        type,
        showClose: true,
        message: options
      }
    }
    rawMessage(options)
  }, 300)
})

Element.Message = Message

function initApp(Vue) {
  Vue.use(TrCard)
  Vue.use(DictLabel)
  Vue.use(SVGIcon)
  Vue.use(installTable)
  Vue.use(WujieVue)
  // // 挂载Element,配置默认UI尺寸，国际化
  Vue.use(Element, {
    size: 'mini',
    zIndex: 100,
    i18n: (key, value) => i18n.t(key, value)
  })
  Vue.config.productionTip = false // 关闭开发环境控制台调试
  // 覆盖$message方法，消息框添加关闭按钮
  Vue.prototype.$message = Message
  Vue.prototype.$EventBus = EventBus // 注册单独数据流，用来处理一些特殊事务
  Vue.prototype.log = console.log // 方便调试信息 在template中也可以打印信息
}

export default initApp
