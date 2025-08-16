import { VXETable } from 'vxe-table'
import { parseString } from '@/utils'
import { getLanguage } from '@/i18n'

const LANGUAGE = getLanguage().split('-')[0]

let optionsMap = null

VXETable.renderer.add('selectEditor', {
  renderEdit(h, renderOpts, { row, column }) {
    const dictType = column.params.remoteDictType

    return (
      <el-select v-model={row[column.field]} multiple={column.params.multiple} filterabl={true} clearable={true}>
        {optionsMap[dictType]?.map(option => (
          <el-option label={option[LANGUAGE]} value={option.keyCode} key={option.keyCode} />
        ))}
      </el-select>
    )
  },
  renderCell(h, renderOpts, { row, column }) {
    if (!optionsMap) optionsMap = column.params.dictMap
    const dictType = column.params.remoteDictType

    if (!row[column.field]) return
    if (column.params.multiple) {
      return [
        calcMultiSelectData(row[column.field]).map(list => (
          <el-tag
            key={list}
            color={calcSelectColor(row[column.field], dictType)}
            size="medium"
            disable-transitions={true}
          >
            {getSelectLabel(list, dictType)}
          </el-tag>
        ))
      ]
    }

    return [
      <el-tag
        color={calcSelectColor(row[column.field], column.params.remoteDictType)}
        size="medium"
        disable-transitions={true}
      >
        {getSelectLabel(row[column.field], column.params.remoteDictType)}
      </el-tag>
    ]
  }
})

// 计算选项颜色
const calcSelectColor = (value = '', type, valueProp = 'keyCode', colorField = 'custom1') => {
  if (!type) return value
  const item = optionsMap?.[type]?.find(item => item[valueProp] === value)

  return item ? item[colorField] : '#B2B5B8FF'
}
const getSelectLabel = (value = '', type, valueProp = 'keyCode', labelField = LANGUAGE) => {
  if (!type) return value
  const item = optionsMap?.[type]?.find(item => item[valueProp] === value)

  return item ? item[labelField] : value
}
const calcMultiSelectData = value => {
  return parseString(value, {})
}
