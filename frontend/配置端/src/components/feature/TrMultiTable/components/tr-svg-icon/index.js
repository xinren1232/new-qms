import Component from './index.vue'

/** 引入所有的svg */
const req = require.context('@/icons', true, /\.svg$/)
const requireAll = (context) => context.keys().map(context)

requireAll(req)
// 注册
Component.install = function (Vue) {
  Vue.component(Component.name, Component)
}

export default Component
