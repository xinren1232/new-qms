import TrMultiTable from './index.vue'

import '@/utils'
import '@/icons/multi-table'
import '@/iconfont/iconfont.css'


TrMultiTable.install = function (Vue) {
  Vue.component(TrMultiTable.name, TrMultiTable)
}


const components = [
  TrMultiTable
]

const install = (Vue) => {
  components.forEach(component => {
    Vue.component(component.name, component)
  })
}

if (typeof window !== 'undefined' && window.Vue) { /* script方式引入时主动调用install方法！！ */
  install(window.Vue)
}

export default {
  install
}
