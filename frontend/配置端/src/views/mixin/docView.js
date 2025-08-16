/**
@name: docView
@description: 文档在线查看 添加标注
@author: jiaqiang.zhang
@time: 2021-11-04 13:02:54
**/
import TrDocView from '@@/special/TrDocView'

export default {
  components: { TrDocView },
  data() {
    return {
      docView: {
        info: null, // 文档信息
        docViewVisible: false // 在线预览文件窗口显示隐藏
      }
    }
  },
  methods: {
    onViewDoc(doc) {
      this.docView.info = doc
      this.docView.docViewVisible = true
      this.docView.xfdfUrl = doc.url
    }
  }
}
