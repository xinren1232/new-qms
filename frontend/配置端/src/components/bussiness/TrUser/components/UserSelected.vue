<template>
  <div
    v-if="multiple"
    v-infinite-scroll="load"
    class="user-selected"
  >
    <el-row
      v-for="(user, index) in userList"
      :key="index"
      class="user-selected-row"
      type="flex"
      :gutter="10"
      justify="start"
    >
      <el-col
        :span="4"
        class="user-selected-avatar"
      >
        <user-avatar
          :key="user.employeeNo"
          :user="user"
          size="small"
          img-size="D"
        />
      </el-col>
      <el-col
        :span="14"
        class="overflow-ellispe"
      >
        {{ user.name || user.employeeName }}
      </el-col>
      <el-col
        :span="6"
        class="user-seleced-remove"
      >
        <el-button
          v-if="!unRecoverList.includes(user.employeeNo)"
          type="danger"
          icon="el-icon-delete"
          size="small"
          @click="$emit('removeSelectedUser', user)"
        />
      </el-col>
    </el-row>
  </div>
  <div
    v-else
    class="user-selected-simple"
  >
    <div>
      <user-avatar
        :key="simpleUser.employeeNo"
        class="user-selected-simple-avatar"
        :user="simpleUser"
        :size="90"
        img-size="L"
      />
    </div>
    <div>{{ simpleUser.name || simpleUser.employeeName }}</div>
    <div>{{ simpleUser.employeeNo }}</div>
    <div>{{ simpleUser.deptName }}</div>
    <div
      class="overflow-ellispe"
      :title="simpleUser.email"
    >
      {{ simpleUser.email }}
    </div>
  </div>
</template>

<script>
import UserAvatar from './UserAvatar'

export default {
  name: 'UserSelected',
  components: { UserAvatar },
  inject: ['multiple'],
  props: {
    userList: {
      type: Array,
      default: () => []
    },
    unRecoverList: {
      type: Array,
      default: () => []
    }
  },
  data() {
    return {}
  },
  computed: {
    // 单选用户
    simpleUser() {
      return this.userList[0] || {}
    }
  },
  methods: {
    load() {
      this.$emit('loadSelectedUser')
    }
  }
}
</script>

<style scoped>
.user-selected-avatar {
  display: flex;
  align-self: center;
  justify-content: center;
}

.user-selected-row {
  line-height: 40px;
}

.user-selected-row:hover {
  background-color: #ecf8fe;
}

.user-selected-row:hover .user-seleced-remove {
  display: inline-block;
}

.user-seleced-remove {
  display: none;
  text-align: right;
}

.user-selected-simple-avatar ::v-deep .el-avatar {
  font-size: 60px;
}

.user-selected-simple div {
  line-height: 32px;
  text-align: center;
}
</style>
