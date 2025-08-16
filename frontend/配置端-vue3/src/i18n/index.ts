import { createI18n } from 'vue-i18n'
import Cookies from 'js-cookie'

// Element Plus 语言包
import elementEnLocale from 'element-plus/es/locale/lang/en'
import elementZhLocale from 'element-plus/es/locale/lang/zh-cn'

// VXE Table 语言包
import vxeEnUS from 'vxe-table/lib/locale/lang/en-US'
import vxeZhCN from 'vxe-table/lib/locale/lang/zh-CN'

// 自定义语言包
import enLocale from './locales/en-US.json'
import zhLocale from './locales/zh-CN.json'

// 合并语言包
const messages = {
  'en-US': {
    ...vxeEnUS,
    ...enLocale,
    ...elementEnLocale
  },
  'zh-CN': {
    ...vxeZhCN,
    ...zhLocale,
    ...elementZhLocale
  }
}

// 获取语言设置
export function getLanguage(): string {
  const chooseLanguage = Cookies.get('language') || 'zh-CN'

  if (chooseLanguage) return chooseLanguage

  // 如果没有选择语言，从浏览器获取
  const language = (navigator.language || (navigator as any).browserLanguage).toLowerCase()
  const locales = Object.keys(messages)

  for (const locale of locales) {
    if (language.indexOf(locale.toLowerCase()) > -1) {
      return locale
    }
  }

  return 'zh-CN'
}

// 设置语言
export function setLanguage(lang: string): void {
  Cookies.set('language', lang)
}

// 创建i18n实例
const i18n = createI18n({
  legacy: false, // 使用Composition API模式
  locale: getLanguage(), // 默认语言
  fallbackLocale: 'zh-CN', // 回退语言
  messages,
  silentTranslationWarn: true
})

export default i18n
