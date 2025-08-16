import { computed } from 'vue'

export const useEditMode = (props) => {
  return computed(() => {
    if (!props.trigger) return null

    const [trigger, mode = 'row'] = props.trigger.split(',')

    return { trigger, mode, activeMethod: props.activeMethod || (() => true) }
  })
}
