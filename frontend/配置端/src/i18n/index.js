import Vue from 'vue'
import elementEnLocale from 'element-ui/lib/locale/lang/en' // element-ui lang
import elementKoLocale from 'element-ui/lib/locale/lang/ko' // element-ui lang
import elementZhLocale from 'element-ui/lib/locale/lang/zh-CN' // element-ui lang
import Cookies from 'js-cookie'
import VueI18n from 'vue-i18n'
import enUS from 'vxe-table/lib/locale/lang/en-US'
import zhCN from 'vxe-table/lib/locale/lang/zh-CN'

import enLocale from './lang/en-US'
import zhLocale from './lang/zh-CN'
import koLocale from './lang/ko-KR'

// tr-comments lang

Vue.use(VueI18n)

const messages = {
  'en-US': {
    ...enUS,
    ...enLocale,
    ...elementEnLocale
  },
  'zh-CN': {
    ...zhCN,
    ...zhLocale,
    ...elementZhLocale
  },
  'ko-KR': {
    ...koLocale,
    ...elementKoLocale
  }
}

// 合并组件(@/components)中的I18n
;(function combineI18n(defaultTarget) {
  const deepAssign = (target, source) => {
    if (!target || !source) return
    const keys = Object.keys(target).concat(Object.keys(source))
    const uniqueKeys = Array.from(new Set(keys))

    uniqueKeys.forEach((key) => {
      if (typeof target[key] === 'object' && typeof source[key] === 'object') {
        deepAssign(target[key], source[key])
      } else if (source[key] !== undefined) {
        target[key] = source[key]
      }
    })
  }

  const modulesFiles = require.context('../components', true, /\/lang\/.*\.js$/)

  modulesFiles.keys().reduce((modules, modulePath) => {
    const matchs = modulePath.match(/\/lang\/(.*)\.js$/)

    if (matchs.length > 1) {
      const moduleName = matchs[1]
      const value = modulesFiles(modulePath)

      deepAssign(modules[moduleName], value.default)
    }

    return modules
  }, defaultTarget)
})(messages)

export function getLanguage() {
  const chooseLanguage = Cookies.get('language') || 'zh-CN'

  if (chooseLanguage) return chooseLanguage

  // if has not choose language
  const language = (navigator.language || navigator.browserLanguage).toLowerCase()
  const locales = Object.keys(messages)

  for (const locale of locales) {
    if (language.indexOf(locale) > -1) {
      return locale
    }
  }

  return 'zh-CN'
}



const i18n = new VueI18n({
  locale: getLanguage(),
  fallbackLocale: 'zh-CN',
  messages,
  silentTranslationWarn: true
})

Vue.prototype.i18n = i18n

export default i18n
