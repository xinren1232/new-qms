<!--suppress ALL -->
<template>
  <tr-card v-loading="loading" class="form-designer-container___box">
    <tr-form-designer
      ref="TrFormDesignerRef"
      :designer-config="designerConfig"
      :custom-fields-list="customFieldsList"
      :client-type="clientType"
      :global-dsv="globalDsv"
    >
      <template #customToolButtons>
        <el-button type="text" icon="el-icon-check" @click="printFormJson">保存数据</el-button>
        <el-button type="text" icon="el-icon-view" @click="onPreviewBtnClick">新开预览</el-button>
      </template>
    </tr-form-designer>
  </tr-card>
</template>

<script setup>
import { ref, nextTick } from 'vue'
import route from '@/router'
import { Message } from 'element-ui'
import { getViewDetail,saveOrUpdate } from '../api/index.js'
import { getObjectAttrByModelCode } from '@/views/system-manage/object-manage/api/index.js'
import { pageQuery } from '@/views/system-manage/properties-manage/api/index.js'
import { getLanguage } from '@/i18n'
// 表单设计器
import '@root/libs/form-designer/TrFormDesigner.css'
import { TrFormDesigner } from '@root/libs/form-designer/TrFormDesigner.umd.min.js'
import { getToken, getRToken } from '@/app/cookie'
// console.log(window.FormDesigner)

const id = route.currentRoute?.params?.id
const loading = ref(false)
const TrFormDesignerRef = ref(null)

const globalDsv = {
  requestHeader: {
    'token': getToken(),
    'rtoken': getRToken(),
    'appId': process.env.VUE_APP_CENTER_BASE_APP_ID
  },
  fileServiceUrl: process.env.VUE_APP_FILE_URL,
  gateway: process.env.VUE_APP_GATEWAY_URL,
  microServices: process.env.VUE_APP_PUBLIC_PATH === '/transcend-pi-configcenter/' ? '/transcend-plm-configcenter-pi/' : '/transcend-plm-configcenter/'
}
const viewData = ref({})
const designerConfig = ref({
  importJsonButton: false,
  exportJsonButton: false,
  exportCodeButton: false,
  generateSFCButton: false,
  domain: ['TR', 'IPM']
})
const clientType = ref(null)

const customFieldsList = ref([])


const printFormJson = () => {
  updateViewData()
  console.info(TrFormDesignerRef.value.getFormJson())
}

// 获取视图详情
const getDetail = async () => {
  loading.value = true
  const { success,data,message } = await getViewDetail(id)

  if (success) {
    viewData.value = data
    nextTick(() => {
      // 清空画布
      clientType.value = data.clientType
      TrFormDesignerRef.value.clearDesigner()
      TrFormDesignerRef.value.setFormJson(data.content)
      console.log(getLanguage())
      TrFormDesignerRef.value.changeLanguage(getLanguage() || 'zh-CN')
    })
    TrFormDesignerRef.value.setClientType(data.clientType)
    // 如果是对象视图，获取对象属性列表
    if (viewData.value.type === 'OBJECT') {
      await getObjectAttrList()
    } else if (viewData.value.type === 'PROPERTY') {
      await getAllPropertiesList()
    }
  } else {
    Message.error(message)
  }
  loading.value = false
}

getDetail()

// 更新视图数据
const updateViewData = async () => {
  loading.value = true
  const propertiesList = TrFormDesignerRef.value.getFieldWidgets()
  const params = {
    ...viewData.value,
    content: {
      propertiesList,
      ...TrFormDesignerRef.value.getFormJson()
    }
  }
  const { success,message } = await saveOrUpdate(params)

  if (success) {
    Message.success('保存成功')
  } else {
    Message.error(message)
  }
  loading.value = false
}

// 获取对象属性列表
const getObjectAttrList = async () => {
  const { data,success,message } = await getObjectAttrByModelCode(viewData.value.modelCode)

  if (success) {
    customFieldsList.value = data.attrList.map(item => {
      return {
        label: item.name,
        name: item.code
      }
    })
  } else {
    Message.error(message)
  }
}

// 获取属性列表
const getAllPropertiesList = async () => {
  const { data,success,message } = await pageQuery({
    current: 1,
    size: 1000,
    count: true,
    param: {}
  })

  if (success) {
    customFieldsList.value = data?.data?.map(item => {
      return {
        label: item.name,
        name: item.code
      }
    })
  } else {
    Message.error(message)
  }
}

const onPreviewBtnClick = () => {
  let routeData = route.resolve('/view-manage/action/preview/' + id)

  window.open(routeData.href, '_blank')
}

function matchComponent(name) {
  const components = [
    { type: 'select', weight: 5, keywords: ['选择', '选项', '下拉','状态','品牌','类型','方式','网络制式'] },
    { type: 'radio', weight: 3, keywords: ['单选', '选择', '圆选', '圆形按钮'] },
    { type: 'checkbox', weight: 3, keywords: ['多选', '选中', '方框选中', '复选框'] },
    { type: 'switch', weight: 2, keywords: ['是否', '开关', '开/关', '切换', '开启', '关闭'] },
    { type: 'button', weight: 2, keywords: ['提交', '保存', '取消', '重置', '按钮'] },
    { type: 'number', weight: 1, keywords: ['数量', '数字', '计数', '数值'] },
    { type: 'date', weight: 1, keywords: ['日期', '时间', '日程安排'] },
    { type: 'slider', weight: 1, keywords: ['进度', '滑块', '拖动', '滑动条'] },
    { type: 'color', weight: 1, keywords: ['颜色', '色彩', '图案', '调色板'] },
    { type: 'textarea', weight: 1, keywords: ['描述', '输入框', '文本框', '多行输入框'] },
    { type: 'input', weight: 1, keywords: ['输入框', '文本框', '单行输入框', '编辑框'] },
    { type: 'time', weight: 1, keywords: ['时间', '时刻', '时间选择器', '时钟'] },
    { type: 'date-range', weight: 1, keywords: ['时间范围', '日期范围', '区间选择器'] },
    { type: 'richtext', weight: 1, keywords: ['描述', '编辑器'] },
    { type: 'user', weight: 1, keywords: ['人', '者'] }
    // 其他组件类型及关键词
  ]

  let maxScore = 0
  let maxComponent = null

  for (let component of components) {
    let score = 0

    for (let keyword of component.keywords) {
      if (name.indexOf(keyword) > -1) {
        score += 1
      }
    }

    if (score > 0 && score + component.weight > maxScore) {
      maxScore = score + component.weight
      maxComponent = component.type
    }
  }

  return maxComponent || 'input'
}

console.log(matchComponent('状态'))
</script>

<style lang="scss" scoped>
.footer {
  margin-top: 25px;
  text-align: right;
}
.graph-box{
  // 阴影
  box-shadow: 0 0 10px 0 rgba(0, 0, 0, 0.16);
  border-radius: 4px;
  overflow: hidden;
}
</style>

<style lang="scss" scoped>
.form-designer-container___box{
  height: 100%;
  //overflow-y: hidden !important;
  border-radius: 0 !important;
  padding: 0 !important;
}
::v-deep *::-webkit-scrollbar{
  width: 0 !important;
}

</style>
