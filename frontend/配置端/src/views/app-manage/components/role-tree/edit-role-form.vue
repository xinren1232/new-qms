<template>
  <el-card class="m-node-content-container">
    <el-form
      ref="form"
      :model="node"
      :disabled="!edit"
      :rules="rules"
      class="m-edit-form"
      label-width="80px"
      style="width: 99%"
    >
      <el-row>
        <el-col :span="12">
          <el-form-item
            label="角色名称"
            prop="name"
          >
            <el-input
              v-model.trim="node.name"
              maxlength="50"
              :disabled="node.isRoot"
            />
          </el-form-item>
        </el-col>
        <el-col :span="12">
          <el-form-item
            label="角色编码"
            prop="bid"
          >
            <el-input
              v-model.trim="node.bid"
              maxlength="50"
              :disabled="!!node.isRoot || !!node.createdTime || disabled"
            />
          </el-form-item>
        </el-col>
        <el-col :span="12">
          <el-form-item
            label="上级角色"
            prop="parentCode"
          >
            <el-input
              v-model.trim="node.parentName"
              disabled
            />
          </el-form-item>
        </el-col>
        <!-- :offset="7" :pull="7" -->
        <el-col
          :span="12"
          :offset="7"
          :pull="7"
        >
          <el-form-item
            v-if="!node.isRoot"
            label="状态"
            prop="stateCode"
          >
            <tr-switch v-model="node.stateCode" />
          </el-form-item>
        </el-col>
        <el-col :span="24">
          <el-form-item
            label="说明"
            prop="description"
          >
            <tr-text-area
              v-model.trim="node.description"
              :attr="{ maxlength: '1000', disabled: node.isRoot }"
            />
          </el-form-item>
        </el-col>
      </el-row>
      <el-col :push="10">
        <el-button
          v-if="!node.isRoot"
          type="primary"
          class="saveBtn"
          @click="_debounceClick(node)"
        >
          保 存
        </el-button>
      </el-col>
    </el-form>
  </el-card>
</template>

<script>
import { createRole, editRole } from '@/api/resRole'
import TrSwitch from '@/components/feature/TrSwitch'
import TrTextArea from '@@/feature/TrTextarea'

export default {
  components: { TrSwitch, TrTextArea },
  props: {
    edit: {
      type: Boolean,
      default: true
    },
    node: {
      type: Object,
      default: () => {
        return {}
      }
    }
  },
  data() {
    return {
      disabled: false,
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
          { max: 50, message: '不能超过50个字符', trigger: 'blur' }
        ],
        description: [{ max: 1000, message: '不能超过1000个字符' }]
      }
    }
  },
  methods: {
    _debounceClick() {
      this.timer && clearTimeout(this.timer)
      this.timer = setTimeout(this.saveBtn, 300)
    },
    controlBtnClick() {
      if (this.$parent.controlNode && this.$parent.controlNode.name !== this.node.name) {
        return this.$message.warning(`${this.$parent.controlNode.name}未保存，请保存后再进行其他操作`)
      } else return false
    },
    saveBtn() {
      if (this.controlBtnClick()) return
      if (this.$parent.$refs.tree.canOperate()) {
        // 判断tree是否有操作未完成
        this.$refs.form.validate((valid) => {
          if (valid) {
            if (this.node.createdTime) {
              // 拿是否有createdTime属性来判断当前节点是新增还是保存
              editRole(this.node).then(() => {
                this.$message.success('保存成功')
                this.$parent.initData()
              })
            } else {
              createRole(this.node).then(({ data }) => {
                this.$parent.node.bid = data ? data.bid : ''
                this.$parent.controlNode = ''
                this.$parent.initData()
                this.$message.success('新增成功')
              })
            }
          }
        })
      } else {
        this.$message.warning('请先保存节点再进行操作！')
      }
    }
  }
}
</script>

<style lang="scss" scoped></style>
