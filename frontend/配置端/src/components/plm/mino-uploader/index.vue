<template>
  <div>
    <el-upload
      ref="uploaderRef"
      :file-list="fileList"
      action="/"
      :class="['tr-upload', { 'is-disabled': compositionDisabled, fill: fileList && !fileList.length }]"
      :disabled="compositionDisabled"
      :before-upload="onBeforeUpload"
      :on-success="onSuccess"
      :on-error="onError"
      :show-file-list="false"
      :http-request="httpRequest"
      multiple
      v-bind="$attrs"
      v-on="$listeners"
    >
      <el-button slot="trigger" icon="el-icon-circle-plus-outline" size="mini" type="primary">选取文件</el-button>
      <vxe-table
        border
        style="margin-top: 10px"
        :column-config="{ resizable: true }"
        sync-resize
        max-height="300"
        :data="fileList"
        show-overflow
      >
        <vxe-table-column type="seq" title="序号" width="60" align="center" />
        <vxe-table-column field="name" title="文件名" sortable min-width="150">
          <template #default="{ row }">
            <span class="file-name">{{ row.name }}</span>
          </template>
        </vxe-table-column>
        <!--        <vxe-table-column field="type" title="类型" width="100" sortable>-->
        <!--          <template #default="{ row }">-->
        <!--            <div class="flex items-center">-->
        <!--              <DocSuffixIcon class="ml-4px flex items-center" :url="row.url" :size="18" />-->
        <!--            </div>-->
        <!--          </template>-->
        <!--        </vxe-table-column>-->
        <vxe-table-column field="size" title="大小" width="150">
          <template #default="{ row }">
            <span v-if="row.size">{{ (Number(row.size) / 1024 / 1024).toFixed(2) }}MB</span>
          </template>
        </vxe-table-column>
        <vxe-table-column field="url" title="状态" align="center" min-width="200">
          <template #default="{ row }">
            <el-tag v-if="uploadStatus[row.id] === 'waiting'" disable-transitions type="error">
              <i class="el-icon-loading" />
              等待上传
            </el-tag>
            <template v-else-if="uploadStatus[row.id] === 'uploading'">
              <el-progress text-color="#333" :stroke-width="8" :percentage="progress[row.id]" />
              <span style="margin-right: 15px; font-size: 12px">传输速度：{{ speed[row.id].toFixed(2) }} M/s</span>
              <span style="font-size: 12px">剩余时间: {{ restTime[row.id].toFixed(1) }} 秒</span>
            </template>
            <el-tag v-else disable-transitions type="success"> 上传完成 </el-tag>
            <el-tag v-if="downloadStatus[row.id]" disable-transitions type="">{{ downloadStatus[row.id] }}</el-tag>
          </template>
        </vxe-table-column>
        <vxe-table-column field="action" title="操作" width="200" align="center">
          <template #default="{ row }">
            <el-button-group>
              <el-tooltip v-if="isZip(row.name)" content="打开在线设计工具">
                <el-button
                  v-if="openDesignToolsAuth && (uploadStatus[row.id] === 'success' || !uploadStatus[row.id])"
                  :disabled="buttonDisabledMap[row.id]"
                  type="primary"
                  size="mini"
                  icon="el-icon-monitor"
                  @click="handleScan(row)"
                />
              </el-tooltip>
              <el-tooltip content="下载">
                <el-button
                  v-if="uploadStatus[row.id] === 'success' || !uploadStatus[row.id]"
                  type="primary"
                  size="mini"
                  :disabled="downloadAuth"
                  icon="el-icon-download"
                  @click="handleDownload(row)"
                />
              </el-tooltip>
              <el-tooltip content="删除">
                <el-button type="danger" size="mini" icon="el-icon-delete" @click="handleRemove(row)" />
              </el-tooltip>
            </el-button-group>
          </template>
        </vxe-table-column>
      </vxe-table>
    </el-upload>
    <div v-if="allocateLoading" class="rocket">
      <img ref="rocket" src="./img/Transsioner20231102-112802.gif" alt="" class="img">
    </div>
  </div>
</template>

<script>
import { CancelToken as _CancelToken } from 'axios'

import {
  addInstance,
  allocate,
  getDownloadUrl,
  getUploadUrl,
  uploadFileFinished
} from './api/index.js'
import request from '@/app/request'
import { putFile } from './minio.js'
import { getFileTypeList } from '@/api/file-manage'

export default {
  name: 'PainterMultipleFile',
  components: {},
  inject: {
    elForm: {
      default: ''
    }
  },
  inheritAttrs: false,
  props: {
    value: {
      type: [Array, String],
      default: () => [],
      comment: '文件上传列表'
    },
    size: {
      type: [String, Number],
      default: 0,
      comment: '上传的文件大小的上限'
    },
    suffix: {
      type: String,
      default: '',
      comment: '接收的文件类型'
    },
    disabled: {
      type: Boolean,
      default: false,
      comment: '是否禁用上传组件'
    },
    notify: {
      type: Boolean,
      default: false,
      comment: '上传成功后是否提示'
    }
  },

  data() {
    return {
      allocateLoading: false,
      fileList: [],
      isShowPercentage: false,
      fileUid: '',
      fileId: '',
      filePath: '',
      progress: {},
      speed: {},
      restTime: {},
      uploadStatus: {},
      downloadStatus: {},
      buttonDisabledMap: {},
      cancelToken: {},
      fileTypeData: []
    }
  },

  computed: {
    compositionDisabled({ disabled, elForm }) {
      return elForm.disabled || disabled
    },
    // 是否有打开在线设计工具
    openDesignToolsAuth(/* { elForm } */) {
      return true // elForm.model?.DATA_AUTH?.OPEN_DESIGN_TOOLS
    },
    // 是否有下载权限
    downloadAuth(/* { elForm } */) {
      return false
    }
  },

  watch: {
    value: {
      deep: true,
      immediate: true,
      handler(value) {
        if (!value || String(value) === 'null') value = []
        this.fileList = Array.isArray(value) ? value : JSON.parse(value || '[]')
      }
    }
  },
  created() {
    this.getAllFileTypeData()
  },
  methods: {
    isZip(filename) {
      const compressedExtensions = ['.zip', '.rar', '.7z'] // 压缩文件的扩展名列表
      const fileExtension = filename.split('.').pop().toLowerCase() // 获取文件的扩展名并转换为小写

      return compressedExtensions.includes('.' + fileExtension) // 检查文件的扩展名是否在压缩文件的扩展名列表中
    },
    onBeforeUpload() {
      return Promise.resolve(true)
    },
    async getFileIdAndFilePath(file,fileTypeCode,storageRule) {
      // 匹配文件类型规则
      const { name, size } = file
      const data = await getUploadUrl({
        module: 'default',
        name,
        fileTypeCode,
        storageRule
      })

      if (data.code !== '200') return this.$message.error('获取文件ID和文件路径失败', data.message)
      this.fileList.unshift({
        name,
        url: data.data.url,
        id: data.data.id,
        size
      })
      this.$set(this.uploadStatus, data.data.id, 'waiting')
      this.$set(this.buttonDisabledMap, data.data.id, false)

      return {
        ...data.data,
        path: data.data.url
      }
    },
    // 设置进度条
    setProgress(fileId, progress, speed, restTime) {
      this.$set(this.progress, fileId, parseInt(progress))
      this.$set(this.speed, fileId, speed)
      this.$set(this.restTime, fileId, restTime)
      this.$set(this.uploadStatus, fileId, 'uploading')
    },
    httpRequest({ file }) {
      const fileType = this.matchFileType(file)
      const storageRule = fileType?.storageRule
      const fileTypeCode = fileType?.code

      this.getFileIdAndFilePath(file,fileTypeCode,storageRule).then(fileObj => {
        putFile(
          fileObj.id,
          fileObj.path,
          file,
          this.httpRequestFun,
          () => {
            this.$set(this.uploadStatus, fileObj.id, 'success')
            uploadFileFinished([fileObj.id])
            addInstance('1230215578301853696','1230216360040427520',{
              name: file.name,
              fileUrl: fileObj.url,
              fileId: fileObj.id,
              fileSize: file.size
            })
            this.$emit('input', [...this.fileList])
            this.$emit('clearValidate')
            this.notify && this.$message.success(file.name + '上传成功')
          },
          this.setProgress
        )
      })
    },
    httpRequestFun(fileId, file, filePath, progress, success) {
      // file 转 blob，再上传 Blob
      const blob = new Blob([file], { type: file.type })

      const filePutUrl = filePath
      const startTime = new Date().getTime()

      request({
        url: filePutUrl,
        method: 'put',
        data: blob,
        timeout: 0, // 设置为0 代表无超时
        headers: {
          'Content-Type': file.type
        },
        cancelToken: new _CancelToken(c => {
          this.cancelToken[fileId] = c
        }),
        onUploadProgress(e) {
          const num = (e.loaded / e.total).toFixed(2)
          const str = (Number(num) * 100).toFixed(2) + '%'
          // 计算网速和预估剩余时间
          const speed = e.loaded / (new Date().getTime() - startTime) / 1024
          const restTime = (e.total - e.loaded) / 1024 / speed / 1024

          // console.log('上传进度：' + str + '网速：' + speed.toFixed(2) + 'M/s' + '预估剩余时间：' + restTime.toFixed(2) + 's')
          progress(fileId, str, speed, restTime)
          if (str === '100.00%') {
            success({ id: fileId, path: filePath, url: filePutUrl })
          }
        }
      })
    },
    handleRemove(file) {
      this.$confirm('确定要移除该文件, 是否继续?', '提示', {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      })
        .then(() => {
          this.fileList = this.fileList.filter(v => file.id !== v.id)
          this.cancelToken[file.id] && this.cancelToken[file.id]('取消上传')
          this.$emit('input', [...this.fileList])
          this.$emit('file', 'delete', file)
          this.$emit('clearValidate')
        })
        .catch(() => {})
    },
    handleDownload(file) {
      if (!file.id) {
        return this.$message.error('文件ID不存在')
      }
      // 获取文件下载地址
      getDownloadUrl(file.id).then(res => {
        if (res.data) {
          // window.open(res.data.url)
          this.downFile(res.data)
        }
      })
    },
    downFile({ id, name, url }) {
      const startTime = new Date().getTime()
      const x = new XMLHttpRequest()

      x.open('GET', url, true)
      x.responseType = 'blob'
      x.onprogress = e => {
        const filesObj = this.fileList.find(v => v.id === id)
        const num = (e.loaded / filesObj.size).toFixed(2)
        const str = (Number(num) * 100).toFixed(2) + '%'
        // 计算网速和预估剩余时间
        const speed = e.loaded / (new Date().getTime() - startTime) / 1024
        const restTime = (filesObj.size - e.loaded) / 1024 / speed / 1024

        this.setProgress(id, str, speed, restTime)
        if (e.loaded === filesObj.size) {
          this.$set(this.uploadStatus, id, 'success')
          this.$set(this.downloadStatus, id, '下载完成')
        }
      }

      x.onload = function () {
        const url = window.URL.createObjectURL(x.response)
        const a = document.createElement('a')

        a.href = url
        a.download = name
        a.click()
      }

      x.send()
    },
    handleScan(file) {
      this.allocateClick(file.id)
    },
    async allocateClick(id) {
      this.allocateLoading = true
      this.buttonDisabledMap[id] = true
      const { data } = await allocate(id).catch(() => {
        this.allocateLoading = false
        this.buttonDisabledMap[id] = false
      })

      // 等待10秒 发起远程桌面
      const params = data?.loginConfig

      setTimeout(() => {
        this.buttonDisabledMap[id] = false
        this.$forceUpdate()
      }, 1000 * 60)
      if (params) {
        this.$notify({
          title: '服务IP',
          message: '准备就绪！',
          type: 'success',
          duration: 3000
        })

        setTimeout(() => {
          const downloadElement = document.createElement('a')

          downloadElement.href = `Transcend://${params}`
          document.body.appendChild(downloadElement)
          downloadElement.click() // 点击
          document.body.removeChild(downloadElement) // 完成移除元素
          this.allocateLoading = false
        }, 2500)
      } else {
        this.allocateLoading = false
      }
    },
    onError() {
      this.isShowPercentage = false
    },
    onSuccess(response, file) {
      this.percentage = 100
      setTimeout(() => {
        this.isShowPercentage = false
      }, 1000)

      if (response?.code * 1 !== 200) {
        this.$message.error(response?.data?.message)

        return
      }
      const data = response.data

      data.size = file.size

      this.$nextTick(() => {
        this.$emit('file', 'add', data)
      })
      this.fileList.push(data)
      this.$emit('input', [...this.fileList])
      this.$emit('clearValidate')
      this.notify && this.$message.success('上传成功')
    },
    async getAllFileTypeData() {
      const { data,success,message } = await getFileTypeList({
        count: true,
        param: {},
        current: 1,
        size: 9999
      })

      if (!success) return this.$message.error(message)

      this.fileTypeData = data?.data ?? []
    },
    // 匹配文件类型
    matchFileType(file) {
      // 1.首选匹配后缀名，从文件类型数据中找到对应的文件类型，如果只有一个则直接返回，多个则继续匹配
      // 2.读取匹配规则，通过匹配规则匹配文件类型
      // 3.按照优先级排序，取优先级最高的文件类型
      const suffix = file.name.split('.').pop()
      const filterData = this.fileTypeData.filter(v => v.suffixName === suffix)

      if (filterData.length === 1) return filterData[0]
      if (filterData.length > 1) {
        const matchData = []

        filterData.forEach(item => {
          if (item.readRule && item.matching) {
            // 截取文件的前readRule个字节，和match进行匹配，如果匹配到则放到matchData中
            this.getFileType(file,item.readRule).then(res => {
              if (item.matching.toLocaleUpperCase().trim() === res.toLocaleUpperCase().trim()) {
                matchData.push(item)
              }
            })
          }
        })

        // 如果matchData为空，则返回filterData中优先级最高的
        // 如果matchData不为空，则返回matchData中优先级最高的
        return matchData.length ?
          matchData.sort((a, b) => b.priority - a.priority)[0] :
          filterData.sort((a, b) => b.priority - a.priority)[0]
      }
    },
    getFileType(file,length = 14) {
      // 创建一个 FileReader 对象
      const reader = new FileReader()


      return new Promise(resolve => {
        reader.onloadend = () => {
          // 将读取的文件内容转换为16进制字符串
          const buffer = reader.result
            .split('').map(v => v.charCodeAt(0))
            .map(v => v.toString(16).padStart(2, '0'))
            .join('')

          resolve(buffer.toLocaleUpperCase())
        }
        // 以数组缓冲区的形式读取文件的前4个字节
        reader.readAsBinaryString(file.slice(0, length))
      })
    }
  }
}
</script>

<style lang="scss" scoped>
.tr-upload {
  padding: 10px;
  border-radius: 6px;
  border: 1px solid #ebeef5;
  ::v-deep .m-img-container {
    display: inline-flex;
    justify-content: center;
    align-items: center;
  }

  &.is-disabled {
    ::v-deep .el-upload.el-upload--picture-card {
      border: 0;
      background: 0;
    }

    .el-upload-dragger {
      cursor: default !important;
    }
  }

  &.is-disabled.fill ::v-deep .el-upload.el-upload--picture-card {
    width: 100%;
    height: 100%;
    line-height: 26px;
  }

  ::v-deep .el-upload {
    //width: 80px;
    //height: 80px;

    // 拖拽上传框
    .el-upload-dragger {
      width: 100%;
      height: 100%;
      border: 0;
      text-align: center;
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
    position: relative;
    width: 80px;
    height: 80px;
    margin-bottom: 10px;
    transition: none !important;
    outline: none;
  }

  .file-card-label {
    position: absolute;
    bottom: 0;
    left: 0;
    width: 100%;
    padding: 0 5px;
    background-color: rgba(0, 0, 0, 0.5);
    color: #fff;
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

.tr-upload.disabled ::v-deep .el-upload .el-upload-dragger {
  text-align: left;
}

.file-card-label {
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
.file-name {
  font-weight: 600;
  cursor: pointer;
  &:hover {
    color: var(--primary-color);
  }
}
.loading-model {
  display: flex;
  align-items: center;
  justify-content: center;
  flex-direction: column;
  position: fixed;
  top: 0;
  bottom: 0;
  right: 0;
  left: 0;
  background-color: rgba(255, 255, 255, 0.8);
  z-index: 99;

  .progress1 {
    position: relative;
    width: 200px;
    height: 20px;
    line-height: 20px;
    background: #dfdfdf;
  }

  .progress1:before {
    position: absolute;
    width: 0%;
    height: 100%;
    background: var(--primary-color);
    content: '0%';
    color: #fff;
    font-size: 12px;
    text-align: center;
    animation: aw 5s forwards;
  }
}
@keyframes aw {
  0% {
    width: 0;
    content: '0%';
  }
  25% {
    width: 25%;
    content: '25%';
  }
  50% {
    width: 50%;
    content: '50%';
  }
  75% {
    width: 75%;
    content: '75%';
  }
  90% {
    width: 90%;
    content: '90%';
  }
  100% {
    width: 100%;
    content: '100%';
  }
}

.progress {
  background: rgba(255, 255, 255, 1);
  width: 78px !important;

  ::v-deep .el-progress__text {
    font-size: 14px !important;
    font-weight: bold;
    color: var(--primary-color);
  }
}

.rocket {
  position: fixed;
  top: 0;
  bottom: 0;
  right: 0;
  left: 0;
  z-index: 99;
  pointer-events: none;

  .img {
    width: 120px;
    height: 120px;
    position: absolute;
    left: 22%;
    transform: rotate(90deg);
    offset-path: path('M 10 200 S 150 0 1600 40');
    animation: rocketFlight 3s ease-in forwards;
  }
}

@keyframes rocketFlight {
  100% {
    offset-distance: 100%;
  }
}
</style>
