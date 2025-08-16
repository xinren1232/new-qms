import SortableJS from 'sortablejs/modular/sortable.core.esm.js'
import { onBeforeUnmount, onMounted } from 'vue'

export const SORT_CLASS_NAME = 'allow-sort'

export const useSortable = ({ props, root, selector }) => {
  let sortable, container, timeoutId

  onMounted(() => {
    // 可能传递进来的是一个组件实例, 那就需要获取 $el 属性
    if (root.value.$el instanceof HTMLElement) {
      container = root.value.$el
      // 或者会传入一个 DOM ref
    } else if (root.value instanceof HTMLElement) {
      container = root.value
    } else {
      return
    }

    selector && (container = container.querySelector(selector))

    sortable = SortableJS.create(container, {
      animation: 100,
      handle: `.${SORT_CLASS_NAME}`,
      onEnd: async ({ newIndex, oldIndex }) => {
        const [currentRow] = props.data.splice(oldIndex, 1)

        props.data.splice(newIndex, 0, currentRow)

        timeoutId && clearTimeout(timeoutId)
        timeoutId = setTimeout(() => {
          props.data.forEach((item, i) => {
            item.sort = i
          })
        }, 1000)
      }
    })
  })

  onBeforeUnmount(() => {
    if (container && sortable) {
      sortable.destroy()
    }
  })
}
