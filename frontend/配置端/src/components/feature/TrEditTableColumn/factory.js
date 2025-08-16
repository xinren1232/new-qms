const componentMap = {
  input: () => import('@@/special/TrPainter/components/TrInput'),
  number: () => import('./components/NumberInputColumn'),
  switch: () => import('./components/SwitchColumn'),
  select: () => import('./components/SelectColumn'),
  textarea: () => import('./components/TextareaColumn'),
  date: () => import('./components/DataPickerColumn'),
  file: () => import('@@/feature/TrFileUploader'),
  files: () => import('@@/feature/TrFilesUploader'),
  // 表格内部的文件上传器
  uploader: () => import('@@/special/TrPainter/components/TrEditTable/Uploader'),
  multiSelect: () => import('@@/special/TrMultiSelect')
}

export function addComponentType(type, component) {
  componentMap[type] = component
}

export function getComponentByType(type) {
  return componentMap[type] || componentMap.input
}
