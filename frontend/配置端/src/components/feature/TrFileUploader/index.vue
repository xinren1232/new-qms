<!--
 * @Author: KunQu
 * @Date: 2021-06-02 11:24:16
 * @LastEditTime: 2021-07-22 16:27:36
 * @Description: file content
-->
<template>
  <div class="tr-file-uploader">
    <input
      ref="fileInput"
      type="file"
      style="display: none"
      @change="OnFileChange"
    >
    <el-button
      v-if="showFileName"
      type="primary"
      :disabled="disabled"
      @click="onUploadClick"
    >
      {{ $t('common.choseFile') }}
    </el-button>
    <el-button
      v-else
      type="primary"
      :disabled="disabled"
      @click="onUploadClick"
    >
      + 导入自文件
    </el-button>

    <span
      v-if="showFileName && file"
      class="m-file-container inline-block"
      :title="file && file.name"
    >
      {{ file && file.name }}
    </span>

    <el-link
      v-if="!hideTemplate"
      :href="templateUrl"
      target="_blank"
      class="margin-left"
      type="primary"
    >
      {{ $t('common.downTemplate') }}
    </el-link>
  </div>
</template>

<script>
import { Link } from 'element-ui'

export default {
  name: 'TrFileUploader',
  components: { ElLink: Link },
  props: {
    value: {
      type: [File, String],
      default: null
    },
    disabled: {
      type: Boolean,
      default: false,
      comment: '是否禁用上传'
    },
    hideTemplate: {
      type: Boolean,
      default: false,
      comment: '隐藏下载模板'
    },
    templateUrl: {
      type: String,
      default: '',
      comment: '用于下载模板的url'
    },
    showFileName: {
      type: Boolean,
      default: true,
      comment: '用于显示导入文件的名字'
    }
  },
  data() {
    return { file: null }
  },
  computed: {
    fileInput() {
      return this.$refs.fileInput
    }
  },
  methods: {
    onUploadClick() {
      this.fileInput.value = ''
      this.fileInput.click()
    },
    OnFileChange() {
      this.file = this.fileInput.files[0]
      this.$emit('input', this.file)
    },
    clearFile() {
      this.file = null
      this.fileInput.value = ''
    }
  }
}
</script>

<style lang="scss" scoped>
.tr-file-uploader {
  display: inline-flex;
  margin-bottom: 15px;

  .m-file-container {
    margin: 0 5px;
    overflow: hidden;
    white-space: nowrap;
    text-overflow: ellipsis;
    padding-top: 4px;
  }
}
</style>
