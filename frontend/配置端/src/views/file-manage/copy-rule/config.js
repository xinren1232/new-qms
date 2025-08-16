export const copyTypeList = [
  { label: '复制', value: 'copy' },
  { label: '移动', value: 'move' }
]

export const copyEventList = [
  { label: '请求时', value: 'request' },
  { label: '变化时', value: 'change' },
  { label: '事件', value: 'event' }
]

export const copyModeList = [
  { label: '立即', value: '1' },
  { label: '延迟', value: '2' },
  { label: '计划', value: '3' }
  // { label: '手动', value: '4' }
]

export const copyTypeMap = copyTypeList.reduce((acc, cur) => {
  acc[cur.value] = cur.label

  return acc
}, {})

export const copyEventMap = copyEventList.reduce((acc, cur) => {
  acc[cur.value] = cur.label

  return acc
}, {})

export const copyModeMap = copyModeList.reduce((acc, cur) => {
  acc[cur.value] = cur.label

  return acc
}, {})
