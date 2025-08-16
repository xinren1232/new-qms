import i18n from '@/i18n'
import { parseString, typeOf } from '@/utils'
import { transDisplayName } from '@/utils/lang'
import TrUser from '@@/bussiness/TrUser'
import TrUserSelect from '@@/feature/TrAvatarSelect'
import TrRichText from '@@/feature/TrRichText'
import TrDate from '@@/special/TrPainter/components/TrDate'
import TrEditTable from '@@/special/TrPainter/components/TrEditTable'
import TrImage from '@@/special/TrPainter/components/TrImage'
import TrInput from '@@/special/TrPainter/components/TrInput'
import TrSelect from '@@/special/TrPainter/components/TrSelect'
import TrSwitch from '@@/special/TrPainter/components/TrSwitch'
import TrUploadFile from '@@/special/TrPainter/components/TrUploadFile'
import TrUploadFiles from '@@/special/TrPainter/components/TrUploadFiles'

const TYPE_MAP = {
  'yyyy-MM-dd': 'date',
  'yyyy-MM': 'month',
  yyyy第W周: 'week',
  yyyy: 'year'
}

// 生成校验规则
export const generateRule = ({ attrType, constraint: _const, dataType, displayName, innerName, objBid, required }) => {
  const rules = []

  if (required) {
    rules.push({
      required: true,
      message: transDisplayName(objBid, innerName, displayName) + ' ' + i18n.t('common.requireMessage'),
      trigger: ['change', 'input', 'blur']
    })
  }

  switch (attrType) {
    case 'text':
      // 1字符串 2浮点数 3整数
      if (dataType === '1') {
        rules.push({
          type: 'string',
          min: 0,
          max: ~~_const.length,
          message: `${i18n.t('common.maxlength')} ${_const.length}!`
        })
      } else if (dataType === '2') {
        rules.push({
          type: 'string',
          pattern: new RegExp(`^\\d+(\.\\d{0,${_const.scale}})?$`),
          message: `${i18n.t('common.maxDigits')}${_const.scale}`,
          trigger: 'change'
        })
      } else if (dataType === '3') { rules.push({ type: 'string', pattern: /^\d+$/, message: i18n.t('common.onlyInt'), trigger: 'change' }) }
      break
    default:
      break
  }

  return rules
}

// 渲染匹配的组件
export const getComponent = ({ attrType }) => {
  if (typeof attrType !== 'string') return attrType
  switch (attrType) {
    case 'file':
      return TrUploadFile
    case 'switch':
      return TrSwitch
    case 'date':
      return TrDate
    case 'image':
      return TrImage
    case 'multiple-file':
      return TrUploadFiles
    case 'select':
      return TrSelect
    case 'table':
      return TrEditTable
    case 'rich-text':
      return TrRichText
    case 'team-select':
      return TrUserSelect
    case 'user':
      return TrUser
    default:
      // return 'ElInput'
      return TrInput
  }
}

// 设置表单的占比
export const getStyle = (field) => {
  const { offsetLeft, offsetRight, span } = field.constraint

  return `width: ${100 / (24 / (span ?? 8))}%;
  margin-left:${100 / (24 / ~~offsetLeft)}%;
  margin-right:${100 / (24 / ~~offsetRight)}%
  `
}

// 设置组件props
export const getProps = (field) => {
  let constraint = field.constraint

  switch (typeOf(field.constraint)) {
    case 'string':
      constraint = JSON.parse(field.constraint || '{}')
      break
    case 'array':
      constraint = {}
      break
  }

  switch (field.attrType) {
    case 'date':
      return {
        ...field.attrs,
        ...constraint,
        disabled: field?.readonly ?? false,
        type: TYPE_MAP[field.dataType] || 'date',
        format: field.dataType
      }
    case 'select':
    case 'team-select':
    case 'multiple-file':
    case 'lifecycle-select':
      // 国际化映射表需要传入对象信息
      constraint = {
        ...field.attrs,
        ...constraint,
        disabled: field?.readonly ?? false,
        multiple: !!field.multiple,
        hiddenSelectAll: true
      }

      if (constraint.data && typeof constraint.data === 'string') {
        constraint.data = parseString(constraint.data)
        constraint.data.forEach((item) => {
          item.objBid = field.objBid
          item.innerName = field.innerName
        })
      }

      return constraint
    case 'encode':
      return { disabled: true }
    case 'text':
      return {
        ...field.attrs,
        ...constraint,
        disabled: field?.readonly ?? false,
        dataType: field.dataType,
        maxlength: constraint?.length,
        showWordLimit: true
      }
    case 'file':
      return { ...field.attrs, ...constraint, disabled: field?.readonly ?? false, hideTemplate: true }
    case 'image':
      return {
        ...field.attrs,
        ...constraint,
        disabled: field?.readonly ?? false,
        limit: 1,
        onExceed: () => alert('最多上传一张照片!')
      }
    case 'table':
      constraint.columns.forEach((item) => {
        item.label = i18n.t(`${field.objBid}_${field.innerName}_${item.prop}`)
        if (item.type === 'select' && item.attrs && item.attrs.data) {
          item.attrs.data.forEach((list) => {
            list.lang = i18n.t(`${field.objBid}_${field.innerName}_${item.prop}_${list.label}_table_select`)
          })
        }
      })

      return { ...field.attrs, ...constraint, disabled: field?.readonly ?? false }
    case 'switch':
      field.defaultValue = JSON.parse(field.defaultValue)

      return {
        ...field.attrs,
        ...constraint,
        disabled: field?.readonly ?? false,
        activeColor: '#13ce66',
        inactiveColor: '#ff4949'
      }
    case 'textarea':
      return {
        autosize: { maxRows: 5, minRows: 2 },
        ...field.attrs,
        ...constraint,
        disabled: field?.readonly ?? false,
        type: 'textarea',
        resize: 'none',
        showWordLimit: true
      }
    case 'rich-text':
      return {
        ...field.attrs,
        ...constraint,
        disabled: field?.readonly ?? false,
        maxlength: constraint?.length,
        showWordLimit: true
      }
    case 'user':
      return { ...field.attrs, ...constraint, disabled: field?.readonly ?? false, multiple: false }
    default:
      return null
  }
}

// 需要占满 24 格的字段属性
export const blockFieldType = ['table', 'multiple-file', 'image']
