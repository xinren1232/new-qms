import { shallowRef } from 'vue'

/**
 * 通过切换状态提升运行时渲染性能
 * @param {boolean} initialValue 默认展示
 */
export function useLazyShow({ initialValue = true, hideDelay = 1500 } = {}) {
  const render = shallowRef(initialValue)
  const visible = shallowRef(initialValue)

  let renderTimeoutId = null

  function open() {
    render.value = true

    clearTimeout(renderTimeoutId)

    setTimeout(() => {
      visible.value = true
    }, 0)
  }

  function close() {
    visible.value = false

    renderTimeoutId = setTimeout(() => {
      render.value = false
    }, hideDelay)
  }

  // toggle 不是必要的, 如果有需要可以在对应的代码里自行实现
  // function toggle() {
  //   visible.value ? close() : open()
  // }

  return {
    render,
    visible,

    open,
    close
    // toggle
  }
}
