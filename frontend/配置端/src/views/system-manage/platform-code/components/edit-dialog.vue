<template>
  <el-dialog
    v-loading="loading"
    class="import-template"
    title="编辑平台编码"
    :visible.sync="visible"
    width="500px"
    :destroy-on-close="true"
    :close-on-click-modal="false"
    @close="onCancel"
  >
    <tr-form
      ref="form"
      v-model="form"
      :rules="rules"
      :show-buttons="false"
      :items="items"
    />

    <div class="footer">
      <el-button
        type="primary"
        @click="save"
      >
        保存
      </el-button>
      <el-button @click="onCancel">取消</el-button>
    </div>
  </el-dialog>
</template>

<script>
import { updateStateByPlatformCode } from '@/api/platform-code.js'
import TrForm from '@@/feature/TrForm'

export default {
  components: { TrForm },
  data() {
    return {
      loading: false,
      visible: false,
      bid: '',
      form: {},
      item: {}
    }
  },
  computed: {
    rules() {
      return { platformCode: { required: true, message: '平台编码不能为空', trigger: 'change' } }
    },
    items() {
      return [
        { label: '平台编码', prop: 'platformCode', span: 24, pull: 1, attrs: { maxlength: 50 } },
        { label: '序号', prop: 'serialNo', span: 24, pull: 1, attrs: { maxlength: 50 } }
      ]
    }
  },

  methods: {
    show(row) {
      this.form = row
      this.visible = true
    },

    validate() {
      const promiseArr = [this.$refs.form.validate()]

      return Promise.all(promiseArr)
    },
    async onClickSave() {
      updateStateByPlatformCode(this.form)
        .then(() => {
          this.$message.success('编辑成功')
          this.visible = false
        })
        .catch(() => {
          this.$emit('editPlatform')
        })
    },
    async save() {
      await this.validate()
      await this.onClickSave()
    },
    onCancel() {
      this.visible = false
      this.form = {}
      this.$emit('editPlatform')
    }
  }
}
</script>
<style lang="scss" scoped>
.footer {
  margin-top: 20px;
  text-align: right;
}
</style>
