import { exportExcel } from '@/api/excel'
import { ref } from 'vue'
import { Message } from 'element-ui'

// 定义导出的业务模块
const TYPES = {
  ATTRS: {
    name: '属性导出文件',
    value: 'cfgAttributeApplicationService'
  },
  OBJECT: {
    name: '对象类型导出文件',
    value: 'cfgObjectAppServiceImpl'
  },
  LC_STATE: {
    name: '生命周期状态导出文件',
    value: 'lifeCycleApplicationServiceImpl'
  },
  DICT: {
    name: '字典导出文件',
    value: 'cfgDictionaryServiceApplicationServiceImpl'
  },
  ENCODE: {
    name: '编码导出文件',
    value: 'cfgCodeRuleDomainService'
  },
  ROLE: {
    name: '角色导出文件',
    value: 'cfgRoleDomainService'
  },
  VIEW: {
    name: '视图导出文件',
    value: 'cfgViewDomainService'
  }
}

export default function useExport() {
  const exportLoading = ref(false)

  const exportFile = (type,param) => {
    if (!type) return Message.warning('导出类型不能为空')
    if (!TYPES[type]) return Message.warning('导出类型不存在')
    if (exportLoading.value) return Message.warning('导出中，请稍后再试')
    const { value,name } = TYPES[type]

    exportLoading.value = true

    return exportExcel(name, value,param).finally(() => {
      setTimeout(() => {
        exportLoading.value = false
      },400)
    })
  }

  return {
    TYPES,
    exportFile,
    exportLoading
  }

}
