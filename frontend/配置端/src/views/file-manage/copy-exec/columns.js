
const stateCodeMapping = {
  // 暂时没有超时这个状态
  // 5: { text: '超时', color: '#ad0598' },
  4: { text: '数据错误', color: '#f00' },
  3: { text: '执行失败', color: '#f00' },
  2: { text: '执行成功', color: '#5400c8' },
  1: { text: '执行中', color: '#0f9fd6' },
  0: { text: '未开始', color: '' }
}

const stateCodeFormatter = ({ row: { executeState } }) => {
  if (executeState == null) return

  return `<span style="color:${stateCodeMapping[executeState]?.color}">${stateCodeMapping[executeState].text}</span>`
}

// 格式化文件大小(byte => MB)
const fileSizeFormatter = ({ row: { fileSize } }) => fileSize && (fileSize / 1024 / 1024).toFixed(2)

const failedTableColumns = [
  { field: 'fileName', title: '文件名', minWidth: 180, fixed: 'left', showOverflow: true },
  { field: 'sourceFileLibrary', title: '源库', width: 120, showOverflow: true },
  { field: 'targetFileLibrary', title: '目标库', width: 120, showOverflow: true },
  { title: '执行状态', type: 'html', width: 100, formatter: stateCodeFormatter },
  { field: 'copyRuleName', title: '复制规则名称', minWidth: 150 },
  { field: 'executionTimes', title: '尝试次数', width: 80 },
  { field: 'fileSize', title: '文件大小(M)', width: 100, formatter: fileSizeFormatter },
  { field: 'nextExecuteTime', title: '执行开始时间', width: 150 },
  { field: 'updatedTime', title: '执行结束时间', width: 150 }
]

const successTableColumns = [
  { field: 'fileName', title: '文件名', minWidth: 180, fixed: 'left', showOverflow: true },
  { field: 'sourceFileLibrary', title: '源库', width: 120, showOverflow: true },
  { field: 'targetFileLibrary', title: '目标库', width: 120, showOverflow: true },
  { title: '执行状态', type: 'html', width: 100, formatter: stateCodeFormatter },
  { field: 'copyRuleName', title: '复制规则名称', minWidth: 150 },
  { field: 'executionTimes', title: '尝试次数', width: 80 },
  { field: 'fileSize', title: '文件大小(M)', width: 100, formatter: fileSizeFormatter },
  { field: 'nextExecuteTime', title: '执行开始时间', width: 150 },
  { field: 'updatedTime', title: '执行结束时间', width: 150 }
]

const processingTableColumns = [
  { field: 'fileName', title: '文件名', minWidth: 180, fixed: 'left', showOverflow: true },
  { field: 'sourceFileLibrary', title: '源库', width: 120, showOverflow: true },
  { field: 'targetFileLibrary', title: '目标库', width: 120, showOverflow: true },
  { title: '执行状态', type: 'html', width: 100, formatter: stateCodeFormatter },
  { field: 'copyRuleName', title: '复制规则名称', minWidth: 150 },
  { field: 'executionTimes', title: '尝试次数', width: 80 },
  { field: 'fileSize', title: '文件大小(M)', width: 100, formatter: fileSizeFormatter },
  { field: 'nextExecuteTime', title: '执行开始时间', width: 150 }
]

const waitingTableColumns = [
  { field: 'fileName', title: '文件名', minWidth: 180, fixed: 'left', showOverflow: true },
  { field: 'sourceFileLibrary', title: '源库', width: 120, showOverflow: true },
  { field: 'targetFileLibrary', title: '目标库', width: 120, showOverflow: true },
  { title: '执行状态', type: 'html', width: 100, formatter: stateCodeFormatter },
  { field: 'copyRuleName', title: '复制规则名称', minWidth: 150 },
  { field: 'fileSize', title: '文件大小(M)', width: 100, formatter: fileSizeFormatter }
]

// 0:未处理 1:执行中 2:执行成功 3:执行失败 4:数据错误
export const tableColumnsByCode = {
  3: failedTableColumns,
  2: successTableColumns,
  1: processingTableColumns,
  0: waitingTableColumns
}
