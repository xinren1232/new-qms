<template>
  <el-dialog
    title="新增字典项"
    :visible.sync="visible"
    width="1000px"
    :close-on-click-modal="false"
    @closed="closeDialog"
  >
    <tr-form ref="TrFormRef" v-model="form" :items="formItems" :show-buttons="false" :rules="formRules" />
    <el-row class="margin-bottom flex items-center">
      <div class="btn-box" @click="insertRow"><tr-svg-icon icon-class="add-list" /><span class="text">添加一条</span></div>
      <el-divider direction="vertical" />
      <div class="btn-box" @click="onSaveBtnClick"><tr-svg-icon icon-class="t-save" /><span class="text">保存</span></div>
    </el-row>
    <vxe-table
      ref="TrTableRef"
      :data="tableData"
      border
      :height="maxHeight"
      :seq-column="false"
      :edit-config="{trigger:'click',mode:'cell',showIcon:false,beforeEditMethod}"
      :edit-rules="validRules"
      keep-source
      @edit-closed="onEditClosed"
    >
      <vxe-table-column title="KEY" field="keyCode" sortable align="center" min-width="200" :edit-render="{}">
        <template #edit="{ row }">
          <el-input v-model="row.keyCode" clearable class="edit-table-input" />
        </template>
      </vxe-table-column>
      <vxe-table-column title="中文" field="zh" sortable align="center" min-width="200" :edit-render="{}">
        <template #edit="{ row }">
          <el-input v-model="row.zh" clearable class="edit-table-input" />
        </template>
        <template #default="{ row }">
          <span v-if="row.zh" class="color-tag" :style="{ backgroundColor: row.custom1, color: replaceOpacity(row.custom1) }">{{ row.zh }}</span>
          <!-- <el-tag v-if="row.zh" :color="row.custom1" size="medium" disable-transitions>{{ row.zh }}</el-tag> -->
        </template>
      </vxe-table-column>
      <vxe-table-column title="英文" field="en" sortable align="center" min-width="200" :edit-render="{}">
        <template #edit="{ row }">
          <el-input v-model="row.en" clearable class="edit-table-input" />
        </template>
        <template #default="{ row }">
          <span v-if="row.en" class="color-tag" :style="{ backgroundColor: row.custom1, color: row.custom2 || replaceOpacity(row.custom1) }">{{ row.en }}</span>
          <!-- <el-tag v-if="row.en" :color="row.custom1" size="medium" disable-transitions>{{ row.en }}</el-tag> -->
        </template>
      </vxe-table-column>
      <vxe-table-column title="排序" field="sort" sortable align="center" min-width="100" :edit-render="{}">
        <template #edit="{ row }">
          <el-input v-model.number="row.sort" clearable class="edit-table-input" />
        </template>
      </vxe-table-column>
      <vxe-table-column title="状态" :field="STATUS_FIELD" sortable align="center" min-width="100">
        <template #default="{ row }">
          <el-switch
            v-model="row[STATUS_FIELD]"
            :params-code="STATUS_FIELD"
            :active-value="1"
            :inactive-value="2"
          />
        </template>
      </vxe-table-column>
      <vxe-table-column title="颜色" field="custom1" sortable align="center" min-width="100">
        <template #default="{ row }">
          <el-color-picker
            :value="row.custom1"
            show-alpha
            color-format="rgb"
            :predefine="universalColors"
            @change="onChangeColor($event, row)"
          />
        </template>
      </vxe-table-column>
    </vxe-table>
  </el-dialog>
</template>

<script setup>
import TrForm from '@@/feature/TrForm'
import { Message, Loading } from 'element-ui'
import Lang from '@/config/lang.config.js'
import { STATUS_FIELD } from '@/config/state.config.js'
import CONSTRAINT from '@/config/constraint.config.js'
import { createOrUpdate, queryDictItemById } from '../api/index.js'
import { universalColors, replaceOpacity } from '@/config/universal-colors.js'

const emit = defineEmits(['add-success'])
const visible = ref(false)
const TrTableRef = ref(null)
const TrFormRef = ref(null)
const isAdd = ref(true)

function onChangeColor(color, row) {
  if (color.endsWith(', 1)')) {
    // custom1 增加透明度, 作为背景, custom2 不透明作为字体颜色
    row.custom1 = color.replace(', 1)', ', 0.11)')
    row.custom2 = color

    return
  }

  row.custom1 = color
  row.custom2 = replaceOpacity(color)
}

const form = ref({})
const tableData = ref([])
const formItems = computed(() => {
  return [
    { label: '名称', prop: 'name', span: 12, attrs: { clearable: true, placeholder: `字典名称，${CONSTRAINT.TEXT}字以内` } },
    { label: '编码', prop: 'code', span: 12, attrs: { disabled: !isAdd.value, clearable: true, placeholder: '大写字母+下划线' } }
  ]
})
const validRules = {
  key: [
    { required: true, message: '字典项KEY不能为空' }
  ]
}
const formRules = {
  name: [
    { required: true, message: '字典名称不能为空' },
    { max: CONSTRAINT.TEXT, message: `字典名称不能超过${CONSTRAINT.TEXT}个字符` }
  ],
  code: [
    { required: true, message: '字典编码不能为空' },
    { pattern: /^[A-Z_]+$/, message: '字典编码只能是大写字母和下划线' }
  ]
}
const maxHeight = computed(() => {
  return window.innerHeight - 300
})

const show = (row) => {
  if (row) {
    isAdd.value = false
    form.value = { ...row }
    queryDictItems(row.bid)
  } else {
    isAdd.value = true
    form.value = {}
    tableData.value = []
  }
  visible.value = true
}
// 新增一行，选中新增的行
const insertRow = async (row) => {
  // 处理多国语言
  const oLang = {}

  Lang.forEach(lang => { oLang[lang.code] = '' })
  const record = {
    keyCode: '',
    ...oLang,
    [STATUS_FIELD]: 1,
    custom1: universalColors[0],
    sort: TrTableRef.value.getTableData().tableData.length + 1
  }
  const { row: newRow } = await TrTableRef.value.insert(record, row)

  await TrTableRef.value.setActiveRow(newRow, 'keyCode')
}
// 如果新插入的行没有填写key，删除该行
const onEditClosed = ({ row }) => {
  if (row.keyCode === '') {
    TrTableRef.value.remove(row)
  }
}
// 编辑前的校验
const beforeEditMethod = ({ row,column }) => {
  // return true

  // 如果有bid 说明是服务器端的数据，不允许编辑KEY
  return !(row.bid && column.property === 'keyCode')

}
const closeDialog = () => {
  // 清除表单校验
  TrFormRef.value.clearValidate()
  form.value = {}
  tableData.value = []
  visible.value = false
}
// 保存新增的字典项
const onSaveBtnClick = () => {
  validateForm()
    .then(() => {
      return validateTable()
    })
    .then(() => {
      addDictPost()
    })
    .catch(() => {
      console.log('校验失败')
    })
}
// 校验表单
const validateForm = () => {
  return new Promise((resolve, reject) => {
    TrFormRef.value.validate((valid) => {
      if (valid) {
        resolve()
      } else {
        reject()
      }
    })
  })
}
// 校验表格, 表格中的每一行都必须有key, 否则校验失败,定位到第一行没有key的行
const validateTable = () => {
  return new Promise((resolve, reject) => {
    const tableData = TrTableRef.value.getTableData().tableData

    if (!tableData.length) {
      Message.warning('请添加字典项')
      reject()
    }
    const firstInvalidRow = tableData.find(row => row.key === '')

    if (firstInvalidRow) {
      TrTableRef.value.setActiveRow(firstInvalidRow)
      reject()
    } else {
      resolve()
    }
  })
}

// 异步请求新增字典
const addDictPost = async () => {
  const loadingInstance = Loading.service({ lock: true, target: '.el-dialog', text: '正在保存...' })
  const tableData = TrTableRef.value.getTableData().tableData

  const params = {
    ...form.value,
    dictionaryItems: tableData.map(({ keyCode, zh, _X_ROW_KEY, ...item }) => {
      keyCode = keyCode.trim()

      // 把中间的空格替换成下划线
      const _keyCode = keyCode.replace(/\s/g, '_')

      return {
        ...item,
        keyCode: _keyCode,
        zh: zh || keyCode
      }
    })
  }

  const { success, message } = await createOrUpdate(params).finally(() => {
    loadingInstance.close()
  })

  if (success) {
    emit('add-success')

    Message.success(message)
    closeDialog()

    return
  }

  Message.error(message)
}

// 查询字典项
const queryDictItems = async bid => {
  const { data, success } = await queryDictItemById(bid)

  if (success)tableData.value = data?.dictionaryItems || []
}

defineExpose({
  show,
  insertRow,
  closeDialog
})
</script>

<style scoped lang="scss">
.edit-table-input{
  ::v-deep .el-input__inner{
    border-radius: 0;
    height: 38px;
    width: 99%;
    border-color: var(--primary);
    border-width: 2px;
    font-size: 14px;
  }
}
::v-deep .vxe-table--render-default .vxe-cell{
  padding-left: 0 !important;
  padding-right:0 !important;
}
::v-deep .cell-disabled{
  cursor: no-drop;
}
.btn-box {
  display: flex;
  align-items: center;
  cursor: pointer;
  color: var(--primary);
  padding: 4px;
  width: fit-content;
  margin-right: 4px;
  .text{
    font-size: 14px;
    margin-right: 4px;
    vertical-align: -0.2em;
  }
  .tr-svg-icon{
    font-size: 16px;
    margin-right: 4px;
    vertical-align: -0.15em;
  }

  &:hover {
    background-color: #EAEDEC;
    border-radius: 4px;
  }
  &:active {
    background-color: #dee1e0;
  }
}
.color-tag{
  display: inline-block;
  padding: 2px 6px;
  border-radius: 5px;
}
</style>
