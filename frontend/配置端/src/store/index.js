import Vue from 'vue'
import Vuex, { Store } from 'vuex'

import getters from './getters'

// 检索当前目录下的store
function findStoreModules() {
  const modulesFiles = require.context('../store/modules', true, /\.js$/)

  return modulesFiles.keys().reduce((modules, modulePath) => {
    const moduleName = modulePath.replace(/^\.\/(.*)\.\w+$/, '$1')
    const value = modulesFiles(modulePath)

    modules[moduleName] = value.default

    return modules
  }, {})
}

// 检索组件(@/components)中的store
function findComponentStoreModules() {
  const modulesFiles = require.context('../components', true, /\/store\/.*\.js$/)

  return modulesFiles.keys().reduce((modules, modulePath) => {
    const matchs = modulePath.match(/\/store\/(.*)\.js$/)

    if (matchs.length > 1) {
      const moduleName = matchs[1]
      const value = modulesFiles(modulePath)

      modules[moduleName] = value.default
    }

    return modules
  }, {})
}
const modules = Object.assign({}, findStoreModules(), findComponentStoreModules())

Vue.use(Vuex)

const store = new Store({
  modules,
  getters
})

export default store
