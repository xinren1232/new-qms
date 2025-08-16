const EXCLUDES_COLUMN_PROP = ['operate', 'action', 'sortable', 'seq']

// 用于 hack 表格发出的事件，可以在冒泡到外部之前做一些拦截或者通用的判断处理
const eventHandlers = {
  'cell-click': (emits, ev) => {
    // 过滤掉组件内置的拖拽、序号、编辑列，减少每次在使用组件时都需要去排除的麻烦
    if (EXCLUDES_COLUMN_PROP.includes(ev.column.property)) return

    emits('cell-click', ev)
  }
}

export const useListeners = (emits, listeners) => {
  const events = {}

  for (const event in listeners) {
    events[event] = eventHandlers[event]
      ? (...args) => {
          eventHandlers[event].call(null, emits, ...args)
        }
      : (events[event] = listeners[event])
  }

  return events
}
