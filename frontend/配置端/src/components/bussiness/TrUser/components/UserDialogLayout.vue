<template>
  <el-row>
    <el-col
      :span="16"
      class="dialog-left"
    >
      <div class="user-find">
        <slot name="userFind" />
      </div>
      <div class="user-list">
        <slot name="userList" />
      </div>
    </el-col>
    <el-col
      :span="8"
      class="dialog-right"
    >
      <div class="user-count">
        <slot name="userCount" />
      </div>
      <div class="user-selected">
        <slot name="userSelected" />
      </div>
    </el-col>
  </el-row>
</template>

<script>
export default {
  name: 'UserDialogLayout',
  data() {
    return {}
  }
}
</script>

<style scoped>
.user-find,
.user-count {
  padding: 0 10px;
  height: 66px;
  line-height: 66px;
  border-bottom: 1px solid #d9d9dd;
}

.user-count {
  display: flex;
  justify-content: space-between;
  position: relative;
}

.user-count ::v-deep .selected-dept {
  position: absolute;
  top: -10px;
}

.user-count ::v-deep .little-count {
  position: absolute;
  top: 10px;
  font-size: 12px;
}

.dialog-left {
  min-width: 500px;
  height: 50vh;
  border-right: 1px solid #d9d9dd;
}

.dialog-right {
  min-width: 180px;
  height: 50vh;
}

.user-list,
.user-selected {
  padding-top: 10px;
  height: calc(100% - 68px);
  overflow: hidden;
  overflow-y: auto;
}

.user-selected {
  padding: 10px;
}
</style>
