import { computed } from 'vue'

export const useModelValue = ({ emit, event = 'input', get, prop = 'value', props, set } = {}) => {
  return computed({
    get() {
      return get?.(props[prop]) ?? props[prop]
    },
    set(value) {
      emit(event, set?.(value) ?? value)
    }
  })
}
