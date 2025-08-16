import { customRef } from 'vue'

// 防抖输入
export const useDebounceRef = (value = '', delay = 300) => {
  let timeoutId

  return customRef((track, trigger) => ({
    get() {
      track()

      return value
    },
    set(newValue) {
      timeoutId && clearTimeout(timeoutId)

      timeoutId = setTimeout(() => {
        value = newValue
        timeoutId = null
        trigger()
      }, delay)
    }
  }))
}
