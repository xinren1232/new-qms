<template>
  <el-dialog
    title="新增角色"
    :visible.sync="showForm"
    :close-on-click-modal="false"
    @close="handleClose"
  >
    <el-form
      ref="form"
      :model="form"
      :rules="rules"
      class="m-edit-form"
      label-width="80px"
      style="width: 99%"
    >
      <el-row>
        <el-col :span="12">
          <el-form-item
            label="角色编码"
            prop="bid"
          >
            <el-input v-model="form.bid" />
          </el-form-item>
        </el-col>
        <el-col :span="12">
          <el-form-item
            label="角色名称"
            prop="name"
          >
            <el-input v-model="form.name" />
          </el-form-item>
        </el-col>
        <el-col :span="12">
          <el-form-item label="上级角色">
            <tr-remote-search
              v-model="form.parentBid"
              :remote-method="remoteMethod"
              :value="form.parentBid"
              label-field="name"
              value-field="bid"
              @select="onSelected"
            />
          </el-form-item>
        </el-col>
        <el-col :span="12">
          <el-form-item
            label="是否启用"
            prop="stateCode"
          >
            <tr-dict-select
              v-model="form.stateCode"
              type="ROLE_STATUS_CODE"
            />
          </el-form-item>
        </el-col>
        <el-col :span="24">
          <el-form-item
            label="说明"
            prop="description"
          >
            <!-- <el-input v-model="form.description" type="textarea" /> -->
            <tr-text-area
              v-model="form.description"
              :attr="{ maxlength: '1000' }"
            />
          </el-form-item>
        </el-col>

        <slot name="after" />

        <el-col class="m-operate-item text-right">
          <el-button
            type="primary"
            @click="submitForm('form')"
          >
            保存
          </el-button>
          <el-button
            type="primary"
            @click="saveContinue"
          >
            保存并继续
          </el-button>
          <el-button @click="handleClose">取消</el-button>
        </el-col>
      </el-row>
    </el-form>
  </el-dialog>
</template>

<script>
import { createRole, queryRoleByName } from '@/api/resRole'
import TrDictSelect from '@/components/bussiness/TrDict'
import TrRemoteSearch from '@/components/feature/TrRemoteSearch'
import TrTextArea from '@@/feature/TrTextarea'

export default {
  components: { TrDictSelect, TrRemoteSearch, TrTextArea },
  props: {
    showForm: {
      type: Boolean,
      default: false
    },
    parent: {
      type: String,
      default: ''
    },
    selectedNode: {
      type: Object,
      default: () => ({})
    },
    dataSort: {
      type: null,
      default: 0
    }
  },
  data() {
    return {
      options: [],
      sort: 0,
      rootSort: 0,
      bid: '',
      form: {
        name: '',
        bid: '',
        parentBid: '',
        type: 'bizRole',
        stateCode: '',
        sort: 1,
        description: '',
        objInsBid: ''
      }, // 'bizRole'必填，为默认角色类型，具体找后端
      rules: {
        name: [
          { required: true, message: '请输入角色名称', trigger: 'blur' },
          {
            pattern: /^(\w|[\u4e00-\u9fa5])+$/g,
            message: '只能输入汉字,字母,数字,下划线(_)',
            trigger: ['blur', 'change']
          },
          { max: 50, message: '不能超过50个字符' }
        ],
        bid: [
          { required: true, message: '请输入角色编码', trigger: 'blur' },
          {
            pattern: /^(\w|\$|\-)+$/g,
            message: '只能输入字母,数字,下划线(_),连字符(-),美元符号($)',
            trigger: ['blur', 'change']
          },
          { max: 50, message: '不能超过50个字符' }
        ],
        type: [{ required: true, message: '请选择角色类型', trigger: 'blur' }],
        state: [{ required: true, message: '请选择是否启用', trigger: 'blur' }],
        description: [{ max: 1000, message: '不能超过1000个字符' }]
      }
    }
  },
  watch: {
    dataSort(val) {
      this.rootSort = val
      this.sort = val
    }
  },
  methods: {
    onSelected({ childRoles }) {
      childRoles ? (this.sort = childRoles.length) : (this.sort = this.rootSort) // 根据上级角色获取当前新增节点的sort排序
    },
    remoteMethod(val) {
      return queryRoleByName(val)
    },
    handleClose() {
      this.$emit('showAddDialog', 'close')
      this.clear()
    },
    clear() {
      this.$refs.form.resetFields()
      this.form = {
        name: '',
        bid: '',
        parentBid: '',
        type: 'bizRole',
        stateCode: '',
        sort: 0,
        description: '',
        objInsBid: ''
      }
    },
    submitForm(formName) {
      this.$refs[formName].validate((msg) => {
        if (msg) {
          this.form.sort = this.sort
          createRole(this.form).then(() => {
            this.clear()
            this.$emit('showAddDialog', 'close')
            this.$parent.initData()
          })
        }
      })
    },
    saveContinue() {
      this.$refs.form.validate((msg) => {
        if (msg) {
          const formData = { ...this.form }

          formData.sort = this.sort
          createRole(formData).then(() => {
            this.clear()
            this.$message.success('新增成功')
            this.$parent.initData()
          })
        }
      })
    }
  }
}
</script>

<style></style>
