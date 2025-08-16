<template>
  <el-dialog
    :title="isAdd?'新增':'编辑'"
    :visible.sync="visible"
    width="600px"
    :close-on-click-modal="false"
    class="add-attr-dialog"
    @closed="closeDialog"
  >
    <tr-form ref="TrFormRef" v-model="form" :items="formItems" :rules="rules" :show-buttons="false">
      <el-form-item slot="after" label="图片" prop="icon" style="width: 100%">
        <el-upload
          v-if="!form.icon"
          drag
          action="https://pfgatewayuat.transsion.com:9199/service-ipm-project/ipm/doc/uploadFile"
          :show-file-list="false"
          style="width: 100%"
          :headers="headers"
          name="files"
          :on-success="onSuccess"
        >
          <i class="el-icon-upload" />
          <div class="el-upload__text">将文件拖到此处，或<em>点击上传</em></div>
        </el-upload>
        <div v-else class="image-box">
          <el-image :src="form.icon" fit="cover" />
          <div class="handler-box">
            <i class="el-icon-delete" />
          </div>
          <div class="bg" />
        </div>

      </el-form-item>
    </tr-form>
    <div class="footer flex justify-end">
      <el-button @click="onCancel">取消</el-button>
      <el-button type="primary" @click="onConfirm">确定</el-button>
    </div>
  </el-dialog>
</template>

<script setup>
import { ref, nextTick, computed } from 'vue'
import { Message, Loading } from 'element-ui'
import CONSTRAINT from '@/config/constraint.config.js'
import TrForm from '@@/feature/TrForm'
import { createOrUpdate } from '../api/index.js'
import { getRToken, getToken } from '@/app/cookie'

// 判断是新增还是编辑
const isAdd = ref(true)
const TrFormRef = ref(null)
const visible = ref(false)
const form = ref({
  icon: ''
})
const headers = {
  'P-Auth': getToken(),
  'P-Token': getToken(),
  'P-Rtoken': getRToken()
}

const emit = defineEmits(['add-success'])

const rules = {
  name: [
    { required: true, message: '请输入名称', trigger: 'blur' },
    { max: CONSTRAINT.TEXT, message: `长度不能超过${CONSTRAINT.TEXT}` },
    {
      pattern: /^(\w|[\u4e00-\u9fa5])+$/g,
      message: '只能输入汉字,字母,数字,下划线(_)',
      trigger: ['blur', 'change']
    }
  ],
  description: [
    { max: CONSTRAINT.TEXTAREA, message: `长度不能超过${CONSTRAINT.TEXTAREA}` }
  ]
}

const formItems = computed(() => {
  return [
    {
      label: '名称',
      prop: 'name',
      span: 24,
      attrs: { maxlength: CONSTRAINT.TEXT, placeholder: `最大长度${CONSTRAINT.TEXT}` }
    },
    {
      label: '描述',
      prop: 'description',
      span: 24,
      attrs: { type: 'textarea', maxlength: CONSTRAINT.TEXTAREA, placeholder: `最大长度${CONSTRAINT.TEXTAREA}` }
    }
  ]
})

const show = (row) => {

  if (row) {
    isAdd.value = false
    form.value = { ...row }
  } else {
    isAdd.value = true
    form.value = { icon: '' }
  }
  visible.value = true
  // 清除表单校验
  nextTick(() => TrFormRef.value.clearValidate())
}
const onSuccess = (response) => {
  form.value.icon = response.data.url
  console.log(form.value)
}
const onConfirm = () => {
  const loadingInstance = Loading.service({ lock: true, target: '.add-attr-dialog .el-dialog', text: '正在保存...' })

  validate().then(async () => {
    const params = {
      ...form.value,
      items: [{ languageCode: 'en', value: form.value.en }, { languageCode: 'zh', 'value': form.value.zh }]
    }

    delete params.en
    delete params.zh
    const data = await createOrUpdate(params)

    if (data?.success) {
      Message.success(data.message)
      nextTick(() => loadingInstance.close())
      closeDialog()
      emit('add-success')
    } else {
      Message.error(data?.message)
      nextTick(() => loadingInstance.close())
    }
  }).catch(() => {
    nextTick(() => loadingInstance.close())
  })
}
// 表单校验
const validate = () => {
  return new Promise((resolve, reject) => {
    TrFormRef.value.validate((val) => {
      if (val) {
        resolve()
      } else {
        reject()
      }
    })
  })
}
const onCancel = () => {
  closeDialog()
}
const closeDialog = () => {
  TrFormRef.value.resetFields()
  visible.value = false
}

defineExpose({
  show
})
</script>

<style lang="scss" scoped>
.footer {
  margin-top: 25px;
  text-align: right;
}

::v-deep .el-upload {
  width: 100%;

  .el-upload-dragger {
    width: 100%;
  }
}

.image-box {
  width: 110px;
  height: 110px;
  border-radius: 12px;
  position: relative;
  overflow: hidden;

  .handler-box {
    position: absolute;
    width: 100%;
    height: 100%;
    z-index: 11;
    top: 0;
    left: 0;

    i {
      color: var(--danger-color);
      font-size: 26px;
      cursor: pointer;
    }
  }

  .bg {
    top: 0;
    left: 0;
    position: absolute;
    width: 100%;
    height: 100%;
    z-index: 10;
    background-color: rgba(0, 0, 0, 0.4);
  }
}
</style>
