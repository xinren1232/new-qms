import OutsideClick from 'element-ui/src/utils/clickoutside'
import { debounce } from 'lodash'

const TIME_DELAY = 350
const OPTION_CONFIG = { leading: true, trailing: false }
const _debounceFactory = (fn) => debounce(fn, TIME_DELAY, OPTION_CONFIG)

/**
 * v-debounce-click 防抖点击
 * 直接使用:
 *    v-debounce-click="handleClick"
 * 如果需要在执行时携带参数, 则需要使用函数返回一个函数:
 *    v-debounce-click="() => handleClick(params)"
 */
export const debounceClick = {
  name: 'debounce-click',
  bind(el, binding) {
    el.addEventListener('click', _debounceFactory(binding.value), false)
  },
  unbind(el, binding) {
    el.removeEventListener('click', _debounceFactory(binding.value), false)
  }
}

// 点击当前元素区域外时触发
OutsideClick.name = 'outside-click'

export const outsideClick = OutsideClick
