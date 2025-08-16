export function putFile(fileId, filePath = 'test', file, getUploadUrl, success, progress) {
  const reader = new FileReader()

  reader.readAsDataURL(file)
  reader.onloadend = function () {
    getUploadUrl(fileId, file, filePath, progress, success)
  }
}

// 获取上传进度
export function createUploadHttp(fileId, file, filePutUrl, filePath, progress, success) {
  const http = new XMLHttpRequest()

  const startTime = new Date().getTime()

  http.upload.addEventListener('progress', e => {
    const num = (e.loaded / e.total).toFixed(2)
    const str = (Number(num) * 100).toFixed(2) + '%'
    // 计算网速和预估剩余时间
    const speed = e.loaded / (new Date().getTime() - startTime) / 1024
    const restTime = (e.total - e.loaded) / 1024 / speed / 1024

    // console.log('上传进度：' + str + '网速：' + speed.toFixed(2) + 'M/s' + '预估剩余时间：' + restTime.toFixed(2) + 's')
    progress(str, speed, restTime)
    if (str === '100.00%') {
      success({ id: fileId, path: filePath, url: filePutUrl })
    }
  })
  http.open('PUT', filePutUrl, true)
  http.send(file)
}
