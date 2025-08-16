<template>
  <div class="instance-box">
    <multi-table
      ref="multiTableRef"
      :field-list="fieldList"
      :data="instanceList"
      :loading="loading"
      :height="height"
      :width="width"
      :extend-data="extendData"
      :column-rules="columnRules"
      @row-detail-click="handleRowDetailClick"
      @edit-closed="onEditClosed"
      @delete-rows="onDeleteRows"
    >
      <!--  接受顶部按钮扩展插槽     -->
      <!--      <template #tools>-->
      <!--        <el-button>asas1111</el-button>-->
      <!--      </template>-->
    </multi-table>
    <el-drawer
      v-loading="infoLoading"
      :visible.sync="drawerVisible"
      size="80%"
      :with-header="false"
      destroy-on-close
      append-to-body
      custom-class="info-drawer"
    >
      <v-form-render
        ref="preFormRef"
        :form-json="formJson"
        :preview-state="false"
        :option-data="dictMap"
        :extend-data="{
          currentRow,
          superSelectMap,
          lifecycleData,
          dictMap
        }"
        @formChange="onFormChange"
      />
    </el-drawer>
  </div>
</template>

<script setup>
import { ref } from 'vue'
import MultiTable from '@@/feature/TrMultiTable'
import { getViewDetail } from '@/views/system-manage/view-manage/api'
import { roleTreeData, objectTreeData } from '@@/feature/TrMultiTable/core/dataTier'
import { getRToken, getToken } from '@/app/cookie'

const props = defineProps({
  height: {
    type: Number,
    default: 400
  },
  width: {
    type: Number,
    default: 1000
  },
  fieldList: {
    type: Array,
    default: () => []
  },
  objectInfo: {
    type: Object,
    default: () => ({}),
    comment: '对象信息'
  },
  instanceList: {
    type: Array,
    default: () => [],
    comment: '实例列表'
  },
  lifecycleData: {
    type: Array,
    default: () => [],
    comment: '生命周期数据,用于渲染生命周期下拉框'
  },
  dictMap: {
    type: Object,
    default: () => ({}),
    comment: '字典映射'
  },
  superSelectMap: {
    type: Object,
    default: () => ({}),
    comment: '超级下拉框实例数据映射'
  },
  columnRules: {
    type: Object,
    default: () => ({}),
    comment: '符合表头项的校验规则，例子： {name: [{ required: true, message: "名字不能为空", trigger: "blur"}] }'
  }
})
const loading = ref(false)
const infoLoading = ref(false)
const formJson = ref({})
const drawerVisible = ref(false)
const preFormRef = ref(null)
const multiTableRef = ref(null)
const currentRow = ref({})

// 表头字段值映射，匹配表头字段值
const fieldMap = { label: 'label', value: 'name', isHeader: 'isHeader' }

// 表格需要的额外扩展数据
const extendData = ref({
  objectInfo: props.objectInfo, // 对象信息
  fieldMap: fieldMap, // 表头字段值映射，匹配表头字段值
  superSelectMap: props.superSelectMap, // 超级下拉框实例数据映射
  dictMap: props.dictMap, // 字典映射
  lifecycleData: props.lifecycleData, // 生命周期下拉框数据
  role: {
    // 角色选择组件数据配置
    childrenField: 'children', // 子节点字段
    data: roleTreeData, // 数据,结构是{list:[],map:{}}的形式
    keyField: 'bid', // 唯一标识字段,用于匹配树的rowKey
    valueField: 'code', // 值字段,用于匹配树的value,传递给服务端的值
    labelField: 'name', // 显示字段,用于匹配树的label
    parentField: 'parentBid' // 父节点字段,用于匹配树的parentKey
  },
  object: {
    // 对象选择组件数据配置
    childrenField: 'children', // 子节点字段
    data: objectTreeData, // 数据,结构是{list:[],map:{}}的形式
    keyField: 'modelCode', // 唯一标识字段,用于匹配树的rowKey
    valueField: 'modelCode', // 值字段,用于匹配树的value,传递给服务端的值
    labelField: 'name', // 显示字段,用于匹配树的label
    parentField: 'parentField' // 父节点字段,用于匹配树的parentKey
  },
  // 文件上传配置
  fileUploadConfig: {
    header: {
      // 上传文件的请求头
      'P-Auth': getToken(),
      'P-Rtoken': getRToken(),
      'P-Appid': process.env.VUE_APP_INNER_ID
    },
    uploadURL: process.env.VUE_APP_BASE_API + '/file/upload' // 上传文件的接口地址
  },
  extendComponent: [
    {
      name: 'customSpan', // 组件名称
      icon: () => import('@/icons/sort-icon.svg'), // 组件图标
      component: {
        view: () => import('./extendComponent/customSpan.vue'), // 查看状态组件
        edit: () => import('./extendComponent/customSpan.vue') // 编辑状态
      }
    }
  ]
})

const handleRowDetailClick = row => {
  row.viewBid = '1091061325095899136'
  currentRow.value = row
  drawerVisible.value = true
  getDetail(row.viewBid, row)
}
const getDetail = async (id, row) => {
  infoLoading.value = true
  const { success, data } = await getViewDetail(id).finally(() => {
    infoLoading.value = false
  })

  if (success) {
    formJson.value = data.content
    preFormRef.value.setFormJson(data.content)
    preFormRef.value.setFormData(row)
  }
}
const onFormChange = (fieldName, newValue, oldValue, formModel) => {
  // 更新表格数据
  const index = props.instanceList.findIndex(i => i.bid === formModel.bid)

  props.instanceList[index] = formModel
  multiTableRef.value.setTableData(props.instanceList, formModel)
  console.log('onFormChange', formModel)
}

// 一行数据从编辑状态退出时触发，包含新增或者编辑
const onEditClosed = row => {
  console.log(row)
  // TODO 保存数据
}
const onDeleteRows = rows => {
  console.log(rows)
}
</script>

<style scoped lang="scss">
.instance-box {
  height: 100%;
  overflow: auto;
  border-radius: 6px;
  width: 100%;
}

::v-deep .el-upload-list--picture-card .el-upload-list__item {
  width: 80px;
  height: 80px;
}

::v-deep .el-upload--picture-card {
  width: 80px;
  height: 80px;
  line-height: 86px;

  i {
    font-size: 20px;
  }
}

::v-deep .info-drawer {
  background-color: #f2f3f5;
  padding: 10px 20px;
}

::v-deep .tr-card {
  margin-bottom: 10px;
}
</style>
