import { shallowRef } from 'vue'

// 记录树节点的展开/折叠状态，用于在数据刷新后还能保持之前的展开状态
export function useTreeCollapse({ fieldKey = 'bid', defaultKeys = ['0'] } = {}) {
  const defaultExpandedKeys = shallowRef(defaultKeys)

  /**
   * 当折叠/展开树组件时
   * @param {Record<string, string|number} data 数据
   * @param {Record<string, string|number>} _ Node ElTree 的 Node 节点
   * @param {import('vue').VNode} param2 ElTree 的 Expand 组件实例
   */
  function onNodeExpandAndCollapse(data, _, { expanded }) {
    if (!data[fieldKey]) return

    // 在事件触发时，expanded 状态是当前状态而不是变化后的状态
    if (expanded) {
      /**
       * 有时候内部操作(新增节点后自动展开)后会出现多个 bid 重复的问题，所以需要去除所有重复的 bid
       * 这里不能重新赋值，这样会导致 ElTree 组件内部 watch defaultExpandedKeys 的方法触发
       * 从而又将节点的展开状态设置为 true，所以只能保证原始的引用不变，然后修改引用的值，这样就不会触发 watch 了
       * ElTree 的这段代码在 /node_modules/element-ui/lib/element-ui.common.js#26257 行代码调用了 setDefaultExpandedKeys 方法
       * @example
       * defaultExpandedKeys: function defaultExpandedKeys(newVal) {
       *   this.store.defaultExpandedKeys = newVal;
       *   this.store.setDefaultExpandedKeys(newVal);
       * }
       */
      const keys = defaultExpandedKeys.value.filter(key => key !== data[fieldKey])

      defaultExpandedKeys.value.length = 0
      defaultExpandedKeys.value.push(...keys)
    } else {
      defaultExpandedKeys.value.push(data[fieldKey])
    }
  }

  return {
    defaultExpandedKeys,
    onNodeExpandAndCollapse
  }
}
