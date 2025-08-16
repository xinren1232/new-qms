/*
 * @Author: BanLi
 * @Date: 2021-04-16 18:24:07
 * @LastEditors: BanLi
 * @LastEditTime: 2021-04-28 16:57:55
 * @Description: 组件操作的公共数据或者公共方法
 */

let debounce
const DELAY_TIMEOUT = 400

import { defineProps } from './attrs'

export default {
  props: defineProps('data,nodeKey'),

  data() {
    return { showPopconfirm: false }
  },

  computed: {
    labelField({ $attrs }) {
      return $attrs.props?.label ?? 'name'
    },
    childrenField({ $attrs }) {
      return $attrs.props?.children ?? 'children'
    },
    createField({ $attrs }) {
      return $attrs.props?.createField
    },
    showBtn({ $attrs }) {
      return $attrs.props?.showBtn ?? true
    },
    removeField({ $attrs }) {
      return $attrs.props?.removeField
    },
    isExpandNode({ $attrs }) {
      return !$attrs['default-expanded-keys']?.length
    }
  },

  created() {
    this.t = (text) => this.$t('component.trEditTree.' + text)
  },

  mounted() {
    this.$nextTick(() => {
      this.tree = this.$refs.tree
    })
  },

  methods: {
    /**
     * 防抖函数 在一定时间内重复触发的事件只执行最后一次
     * @param {object} handler 包含name方法的对象
     * @param {string} name 需要执行的函数名字
     * @param {...any} args 函数执行时传入的参数
     */
    _debounceEvent(handler, name, ...args) {
      debounce && clearTimeout(debounce)
      debounce = setTimeout(() => {
        handler[name](...args)
        debounce = null
      }, DELAY_TIMEOUT)
    }
  }
}
