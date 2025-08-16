import { customRef } from 'vue'

export const useStorageRef = ({
  storage = localStorage,
  defaultValue = '',
  delay = 300,
  cacheKey
} = {}) => {
  let value = storage.getItem(cacheKey) || defaultValue

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

        trigger()

        storage.setItem(cacheKey, value)

        timeoutId = null
      }, delay)
    }
  }))
}
