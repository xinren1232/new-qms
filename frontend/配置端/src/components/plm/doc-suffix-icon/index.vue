<template>
  <div class="DocSuffixIcon">
    <tr-svg-icon
      :icon-class="iconClass"
      :style="{ color: color, fontSize: size + 'px' }"
    />
  </div>
</template>

<script>
export default {
  name: 'DocSuffixIcon',
  props: {
    color: {
      type: String,
      default: '',
      command: '预设颜色'
    },
    url: {
      type: String,
      default: '',
      command: '文档地址'
    },
    size: {
      type: Number,
      default: 20,
      command: '大小'
    }
  },
  computed: {
    iconClass({ url }) {
      if (!url) return 'default'
      const _suffix = url.split('.')[url.split('.').length - 1].toLocaleLowerCase()

      if (['xlsx', 'xls'].includes(_suffix)) {
        return 'excel'
      } else if (['dwg', 'dxf'].includes(_suffix)) {
        return 'cad'
      } else if (['doc', 'docx'].includes(_suffix)) {
        return 'doc'
      } else if (['pdf'].includes(_suffix)) {
        return 'pdf'
      } else if (['pptx', 'ppt', 'pptm', 'ppsx', 'ppsm'].includes(_suffix)) {
        return 'ppt'
      } else if (['txt'].includes(_suffix)) {
        return 'text'
      } else if (['html', 'js', 'css', 'java', '.class'].includes(_suffix)) {
        return 'html'
      } else if (['exe'].includes(_suffix)) {
        return 'exe'
      } else if (['jpg', 'png', 'svg', 'jpeg', 'gif'].includes(_suffix)) {
        return 'image'
      } else if (['md'].includes(_suffix)) {
        return 'md'
      } else if (['mp3', 'wav', 'wma', 'cda', 'aif', 'ape', 'aiff'].includes(_suffix)) {
        return 'mp3'
      } else if (['rar'].includes(_suffix)) {
        return 'rar'
      } else if (['zip'].includes(_suffix)) {
        return 'zip'
      } else if (['xmind'].includes(_suffix)) {
        return 'xmind'
      } else if (['avi', 'rm', 'rmvb', 'mpeg', 'mpg', 'dat', 'mov', 'qt', 'asf', 'wmv'].includes(_suffix)) {
        return 'video'
      } else if (['psd', 'psb'].includes(_suffix)) {
        return 'ps'
      } else if (['rp'].includes(_suffix)) {
        return 'rp'
      } else if (['crx'].includes(_suffix)) {
        return 'crx'
      } else {
        return 'default'
      }
    }
  }
}
</script>

<style scoped></style>
