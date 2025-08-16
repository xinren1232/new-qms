<template>
  <el-dialog
    v-loading="loading"
    class="import-template"
    title="新增平台编码"
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
        @click="save('save')"
      >
        保存
      </el-button>
      <el-button
        type="primary"
        @click="save('continue')"
      >
        保存并继续
      </el-button>
      <el-button @click="onCancel">取消</el-button>
    </div>
  </el-dialog>
</template>
<script>
import { addPlatformCode } from '@/api/platform-code.js'
import TrSelect from '@/components/feature/TrSelect'
import TrForm from '@@/feature/TrForm'

export default {
  components: { TrForm },
  data() {
    return {
      loading: false,
      visible: false,
      form: {}
    }
  },
  computed: {
    rules() {
      return { platformCode: [{ required: true, message: '平台编码不能为空', trigger: 'change' }] }
    },
    items() {
      return [
        { label: '平台编码', prop: 'platformCode', span: 24, pull: 1, attrs: { maxlength: 50 } },
        { label: '序号', prop: 'serialNo', span: 24, pull: 1, attrs: { maxlength: 50 } },
        {
          label: '是否启用',
          prop: 'stateCode',
          span: 24,
          pull: 1,
          component: TrSelect,
          attrs: {
            data: [
              { label: '启用', value: 'enable' },
              { label: '未启用', value: 'off' }
            ]
          }
        }
      ]
    }
  },

  methods: {
    show() {
      this.visible = true
    },

    validate() {
      const promiseArr = [this.$refs.form.validate()]

      // this.objectAttrs.length && promiseArr.push(this.$refs.extendsForm.$refs.form.$refs.form.validate())
      return Promise.all(promiseArr)
    },
    async onClickSave(type) {
      await addPlatformCode(this.form)
      this.$message.success('新增成功')
      this.$emit('addPlatform')
      if (type === 'save') this.visible = false
      this.form = {}
      this.$refs.form.resetFields()
    },
    async save(type) {
      await this.validate()
      this.onClickSave(type)
    },
    onCancel() {
      this.visible = false
      this.form = {}
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
