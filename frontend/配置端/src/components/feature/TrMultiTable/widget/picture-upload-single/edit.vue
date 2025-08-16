<template>
  <div class="picture-edit-box">
    <el-button class="add-btn" icon="el-icon-plus" plain @click="showUploadPopover = !showUploadPopover" />
    <div v-if="modelValue" class="image-box">
      <el-image :src="modelValue" fit="fill" />
    </div>
    <div v-if="showUploadPopover" class="picture-list">
      <div v-if="modelValue" class="list-box">
        <div class="image-box-container">
          <el-image :src="modelValue" fit="fill" lazy />
          <span class="el-upload-list__item-actions">
            <span class="el-upload-list__item-preview t-btn" @click.stop="handlePictureCardPreview">
              <i class="el-icon-zoom-in" />
            </span>
            <span class="el-upload-list__item-preview t-btn" @click.stop="handleDownload">
              <i class="el-icon-download" />
            </span>
            <span v-if="!disabled" class="el-upload-list__item-preview t-btn" @click.stop="handleRemove">
              <i class="el-icon-delete danger-color" />
            </span>
          </span>
        </div>
      </div>
      <div v-else class="no-data" @click="onUploadHandler">暂无图片</div>
      <el-upload
        v-if="!disabled"
        ref="uploadRef"
        class="upload-demo"
        :action="action"
        :before-upload="onBeforeUpload"
        :on-remove="handleRemove"
        :multiple="multiple"
        :limit="limit"
        :on-exceed="handleExceed"
        :headers="headers"
        name="files"
        list-type="text"
        :on-success="onSuccess"
        :show-file-list="false"
      >
        <el-button type="text" icon="el-icon-plus">添加本地文件</el-button>
      </el-upload>
    </div>
    <el-image-viewer
      v-if="dialogVisible"
      :url-list="previewUrlList"
      class="el-picker-panel"
      :on-close="onPreviewClose"
    />
  </div>
</template>

<script setup>
import { ref, computed } from 'vue'
import ElImageViewer from 'element-ui/packages/image/src/image-viewer'
import { Message } from 'element-ui'

const emits = defineEmits(['change'])
const dialogVisible = ref(false)
const dialogImageUrl = ref('')
const props = defineProps({
  value: {
    type: String,
    default: ''
  },
  row: {
    type: Object,
    default: () => ({})
  },
  column: {
    type: Object,
    default: () => ({})
  },
  fileUploadConfig: {
    type: Object,
    default: () => ({})
  }
})
const modelValue = computed({
  get() {
    return props.value
  },
  set(val) {
    emits('change', val)
  }
})
const showUploadPopover = ref(false)
const uploadRef = ref(null)
const headers = ref(props.fileUploadConfig.headers || {
  'P-Auth': props.column.params.token,
  'P-Rtoken': props.column.params.rtoken,
  'P-Empno': props.column.params.empno
})

const action = computed(() => {
  return props.fileUploadConfig.uploadURL || props.column.params.uploadURL
})
// 权限控制
const disabled = computed(() => {
  return props.column.params.disabled
})

// 允许的文件类型["jpg","jpeg",  "png"]
const fileType = computed(() => {
  return props.column.params.fileTypes
})
// 文件大小
const fileSize = computed(() => {
  return props.column.params.fileMaxSize
})
// 数量限制
const limit = computed(() => {
  return props.column.params.limit
})
// 是否多选
const multiple = computed(() => {
  return props.column.params.multipleSelect
})

const previewUrlList = computed(() => {
  return [modelValue.value]
})
const onBeforeUpload = (file) => {
  const isLt2M = file.size / 1024 / 1024 < fileSize.value
  const isType = fileType.value.includes(file.name.split('.').pop())

  if (!isLt2M) {
    Message.error(`上传图片大小不能超过 ${fileSize.value}MB!`)
  }

  if (!isType) {
    Message.error(`上传图片格式不正确!`)
  }

  return isLt2M && isType
}
const handleRemove = () => {
  modelValue.value = ''
  props.row[props.column.field] = ''
  // showUploadPopover.value = false
}
const handlePictureCardPreview = () => {
  dialogImageUrl.value = modelValue.value
  dialogVisible.value = true
}
const handleDownload = () => {
  window.open(modelValue.value)
}
const handleExceed = (files, fileList) => {
  Message({
    type: 'warning',
    message: `当前限制选择 ${limit.value} 个文件，本次选择了 ${files.length} 个文件，共选择了 ${files.length + fileList.length} 个文件`
  })
}
const onSuccess = (response) => {
  if (response.code !== '200') {
    Message.error(response.message)

    return
  }
  modelValue.value = response.data?.url
  props.row[props.column.field] = response.data?.url
}
const onPreviewClose = () => {
  dialogVisible.value = false
}
const onUploadHandler = () => {
  uploadRef.value.$el.querySelector('input').click()
}
</script>

<style scoped lang="scss">
.picture-edit-box{
  border: 2px solid var(--primary);
  height: 40px;
  line-height: 40px;
  padding: 0 6px;
  position: relative;
  width: 100%;
  display: flex;
  align-items: center;
  .add-btn{
    margin-right: 10px;
  }
  .image-box{
    width: 100%;
    height: 100%;
    overflow: hidden;
    margin-bottom: 0.4em;
    .el-image{
      cursor: pointer;
      margin-right: 10px;
      width: 32px;
      height: 26px;
      transition: all 0.3s;
      border-radius: 2px;
      overflow: hidden;
      vertical-align: middle;
      img{
        width: 100%;
        height: 100%;
      }
      &:hover{
        box-shadow: 0 0 5px 0 rgba(0,0,0,0.2);
        transform: scale(1.1);
      }
    }
  }

  .picture-list{
    position: absolute;
    left: 0;
    top: 44px;
    width: 360px;
    background-color: #fff;
    box-shadow: 0 0 5px 0 rgba(0,0,0,0.5);
    z-index: 100;
    padding: 15px 0 0 0 ;
    border-radius: 4px;
    .list-box{
      height: 140px;
      overflow: overlay;
      border: 1px dashed #dee0e3;
      width: 96%;
      margin: auto;
      padding: 10px;
      position: relative;
      text-align: left;
      .image-box-container{
        display: inline-block;
        position: relative;
        .el-image{
          cursor: pointer;
          margin-right: 10px;
          width: 96px;
          height: 60px;
          transition: all 0.3s;
          border-radius: 2px;
          overflow: hidden;
          margin-bottom:10px;
          border: 1px solid #ccc;
          img{
            width: 100%;
            height: 100%;
          }
        }
        .el-upload-list__item-actions{
          display: none;
          position: absolute;
          left: 0;
          top: 0;
          width: 96px;
          height: 60px;
          border-radius: 2px;
          overflow: hidden;
          background-color: rgba(0,0,0,0.8);
          //display: flex;
          align-items: center;
          justify-content: center;
          .el-upload-list__item-preview{
            margin:0 2px;
            i{
              color: #c1bebe;
              font-size: 16px;
              cursor: pointer;
            }
          }
        }
        &:hover{
          .el-upload-list__item-actions{
            display: flex;
          }
        }
      }

    }
    .no-data{
      text-align: center;
      color: #ccc;
      font-size: 14px;
      background-color: #f5f6f7;
      border: 1px dashed #dee0e3;
      height: 120px;
      line-height: 120px;
      border-radius: 6px;
      width: 90%;
      margin: auto;
    }
  }
}
.el-picker-panel{
  color: transparent;
 border: none;
 box-shadow: none;
 background: none;
 border-radius: 0;
 line-height: inherit;
  margin:inherit;
}
.upload-demo{
  text-align: center;
}
</style>
