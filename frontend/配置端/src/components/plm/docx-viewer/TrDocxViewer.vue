<template>
  <div ref="container" class="tr-docx-viewer" />
</template>

<script>
import { loadDocxPreview } from './utils'

export default {
  name: 'TrDocxViewer',
  props: {
    url: {
      type: String,
      default: '',
      required: true
    }
  },
  data() {
    return {
      docxPreview: null
    }
  },
  watch: {
    url(val) {
      this.render(val)
    }
  },
  async mounted() {
    this.docxPreview = await loadDocxPreview()

    this.$nextTick(() => {
      this.render(this.url)
    })
  },
  methods: {
    render(url) {
      if (!url) return

      /** @type {import("docx-preview")} */
      const docxPreview = this.docxPreview

      const xhr = new XMLHttpRequest()

      xhr.open('GET', url)
      xhr.responseType = 'arraybuffer'
      xhr.addEventListener('load', async () => {
        const arrayBuffer = xhr.response

        if (arrayBuffer && this.$refs.container) {
          try {
            await docxPreview.renderAsync(arrayBuffer, this.$refs.container, null, {
              className: 'tr-docx-viewer__content'
            })
            this.$emit('success')
          } catch (e) {
            this.$emit('error', e)
          }
        }
      })

      xhr.send()
    }
  }
}
</script>

<style lang="scss">
.tr-docx-viewer {
  & &__content-wrapper {
    background: #fff;
  }
}
</style>
