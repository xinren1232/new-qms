<template>
  <vue-view class="vue-viewer" :code="code" :view-id="viewId" />
</template>

<script>
import VueView from '@@/plm/viewer-card/vue-viewer'
import axios from 'axios'

export default {
  name: 'TlcCard',
  components: { VueView },
  props: {
    cardCode: {
      type: String,
      default: '',
      Comment: '卡片的code，用以查询卡片信息'
    },
    viewType: {
      type: Number,
      default: 1,
      Comment: '视图类型：1-web端，2-移动端'
    },
    version: {
      type: String,
      default: '',
      Comment: '版本号'
    },
    requestParams: {
      type: Object,
      default: () => {
        return {
          baseURL: '',
          headers: null // '请求头'
        }
      },
      Comment: '请求参数'
    }

  },
  data() {
    return {
      code: '',
      viewId: 'card_' + Date.now()
    }
  },
  watch: {
    cardCode(val) {
      this.query()
    }
  },
  created() {
    this.query()
  },
  methods: {
    async query() {
      if (!this.cardCode) return
      const { baseURL, headers } = this.requestParams
      const params = {
        cardCode: this.cardCode,
        cardViewType: this.viewType,
        cardVersion: this.version
      }

      try {
        const { data } = await axios({
          baseURL,
          url: '/twb/cards/query/cardViewByCodeVersion',
          method: 'get',
          params,
          headers
        })

        this.code = Math.random()
      } catch (e) {
        this.$message.error('卡片渲染错误：' + e)
      }
    }
  }
}
</script>

<style lang="scss" scoped>
.vue-viewer{
  height: 100%;
  width: 100%;
}
</style>
