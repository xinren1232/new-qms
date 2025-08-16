<!--
 * @Author: BanLi
 * @Date: 2021-01-22 17:52:07
 * @LastEditTime: 2021-03-18 14:21:59
 * @Description: 删除节点时出现的小弹窗
-->

<template>
  <transition name="fade">
    <div
      v-show="value"
      class="remove-popconfirm"
      @click="updateModelValue"
    >
      <div class="popconfirm-dialog">
        <p class="popconfirm-content">
          <i :class="`el-icon-${type}`" />
          <span>&nbsp; {{ context }}</span>
        </p>

        <div class="text-right">
          <el-button
            type="text"
            @click.stop="onCancel"
          >
            {{ cancelText }}
          </el-button>
          <el-button
            type="primary"
            @click.stop="onConfirm"
          >
            {{ confirmText }}
          </el-button>
        </div>
      </div>
    </div>
  </transition>
</template>

<script>
export default {
  props: {
    value: {
      type: Boolean,
      default: false,
      comment: '是否展示菜单'
    },
    type: {
      type: String,
      default: 'error',
      comment: '内容前面的小图标类型'
    },
    context: {
      type: String,
      default: '是否删除该节点?',
      comment: '弹窗提示文本内容'
    },
    cancelText: {
      type: String,
      default: '取消',
      comment: '取消按钮文本'
    },
    confirmText: {
      type: String,
      default: '确定',
      comment: '确定按钮文本'
    },
    popPosition: {
      type: Number,
      default: 0,
      comment: '确认弹窗的位置'
    }
  },

  methods: {
    onCancel() {
      this.updateModelValue()
      this.$emit('cancel')
    },
    onConfirm() {
      this.updateModelValue()
      this.$emit('confirm')
    },

    updateModelValue() {
      this.$emit('input', false)
    }
  }
}
</script>

<style lang="scss" scoped>
.remove-popconfirm {
  position: absolute;
  left: 0;
  top: 0;
  right: 0;
  bottom: 0;
  z-index: 10;

  .popconfirm-dialog {
    position: absolute;
    left: 50%;
    top: 50%;
    padding: 10px;
    width: 220px;
    background: #fff;
    border-radius: 4px;
    transform: translate(-50%, -50%);
    box-shadow: 0 2px 12px 0 rgba(0, 0, 0, 0.1);
    z-index: 12;

    .popconfirm-content {
      margin: 10px 0 20px;
    }
  }

  .el-icon-error {
    color: #f56c6c;
  }
  .el-icon-success {
    color: var(--primary);
  }
  .el-icon-warning {
    color: #e6a23c;
  }
  .el-icon-error {
    color: #f56c6c;
  }
  .el-icon-info {
    color: #909399;
  }
}
</style>
