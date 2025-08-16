<template>
  <el-upload
    v-loading="loading"
    :file-list="fileList"
    drag
    action
    list-type="picture-card"
    :http-request="uploadSectionFile"
    :class="['tr-upload', { disabled: compositionDisabled, fill: !fileList.length }]"
    :disabled="compositionDisabled"
    :on-change="onChange"
    v-bind="$attrs"
    v-on="$listeners"
  >
    <i v-if="!compositionDisabled" class="el-icon-plus" />
    <span v-else-if="compositionDisabled && !fileList.length" class="nomedia">
      {{ $t('common.noData') }}
    </span>

    <template slot="file" slot-scope="{ file }">
      <div class="m-img-container">
        <img class="el-upload-list__item-thumbnail" :src="getImgUrl(file)" alt="">
      </div>
      <div>
        <span class="el-upload-list__item-actions">
          <span class="el-upload-list__item-delete" @click="handleDownload(file)">
            <i class="el-icon-download" />
          </span>
          <span v-if="canDelete" class="el-upload-list__item-delete" @click="handleRemove(file)">
            <i class="el-icon-delete" />
          </span>
        </span>
      </div>

      <span class="m-file-card-label cursor-pointer" :title="file.name">
        {{ file.name }}
      </span>
    </template>
  </el-upload>
</template>

<script>
import docPath from './assets/doc.png'
import filePath from './assets/file.png'
import xlsPath from './assets/xls.png'

export default {
  name: 'TrFilesUploader',
  inject: { elForm: { default: '' } },
  props: {
    value: {
      type: Array,
      default: () => [],
      comment: '文件上传列表'
    },
    disabled: {
      type: Boolean,
      default: false,
      comment: '是否禁用上传组件'
    },
    httpRequest: {
      type: Function,
      default: null,
      comment: '是否启用自定义上传方法'
    },
    canDelete: {
      type: Boolean,
      default: true,
      comment: '是否显示删除按钮'
    }
  },

  data() {
    return {
      loading: false,
      fileList: []
    }
  },

  computed: {
    compositionDisabled({ disabled, elForm }) {
      return disabled || !!elForm?.disabled
    }
  },

  watch: {
    value: {
      immediate: true,
      handler(value) {
        this.fileList = Array.isArray(value) ? value : []
      }
    }
  },

  methods: {
    onChange(file, fileList) {
      this.$emit('input', [...fileList])
    },
    uploadSectionFile(params) {
      if (!(this.httpRequest instanceof Function)) return

      return new Promise((resolve, reject) => {
        this.loading = true
        const file = params.file

        this.httpRequest(file)
          .then(({ data }) => {
            if (data && data.url) {
              resolve(data)
            } else {
              this.$message.error(this.$t('common.uploadFailed'))
              reject(new Error(false))
            }
          })
          .finally(() => {
            this.loading = false
          })
      })
    },
    handleRemove(file) {
      this.fileList = this.fileList.filter((v) => {
        return file.uid !== v.uid
      })
      this.$emit('input', this.fileList.slice())
    },
    handleDownload(file) {
      const url = file.response ? file.response.url : file.url

      if (url) {
        window.open(url)
      } else {
        this.$message.error(this.$t('common.fileIsEmpty'))
      }
    },
    getImgUrl(file) {
      const upperName = file.name.toUpperCase()

      if (/BMP|JPG|JPEG|PNG|GIF$/.test(upperName)) {
        return file.url
      } else if (/DOC|DOCX$/.test(upperName)) {
        return docPath
      } else if (/XLS|XLSX$/.test(upperName)) {
        return xlsPath
      } else {
        return filePath
      }
    }
  }
}
</script>

<style lang="scss" scoped>
.tr-upload {
  ::v-deep .m-img-container,
  ::v-deep .el-upload-list {
    display: inline-flex;
    justify-content: center;
    align-items: center;
  }

  &.disabled {
    ::v-deep .el-upload.el-upload--picture-card {
      border: 0;
      background: 0;
    }

    .el-upload-dragger {
      cursor: default !important;
    }
  }

  &.disabled.fill ::v-deep .el-upload.el-upload--picture-card {
    width: 100%;
    height: 100%;
  }

  ::v-deep .el-upload {
    width: 80px;
    height: 80px;
    line-height: 115px;
    // 拖拽上传框
    .el-upload-dragger {
      width: 100%;
      height: 100%;
      border: 0;
      background: transparent;
      font-size: 12px;
      color: #999;
    }
    .el-icon-plus {
      vertical-align: top;
      line-height: 80px;
    }
  }

  .el-upload-list__item-thumbnail {
    max-width: 100%;
    height: auto;
  }

  ::v-deep .el-upload-list__item {
    width: 80px;
    height: 80px;
    overflow: initial;
    margin-bottom: 30px;
    outline: none;
  }

  .m-img-container,
  .m-card-container {
    width: 100%;
    height: 100%;
    font-size: 0;
    border-radius: 8px;
    overflow: hidden;
  }
}

.m-file-card-label {
  position: absolute;
  bottom: -24px;
  display: inline-block;
  width: 100%;
  padding: 0 5px;
  text-align: center;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
  color: #606266;
  font-size: 12px;
}
</style>
