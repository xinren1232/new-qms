<template>
  <div class="user-member">
    <div
      v-for="(user, index) in userList"
      :key="index"
    >
      <el-row
        v-if="user.type === 1 || user.type === void 0"
        class="user-member-row"
        type="flex"
        justify="start"
        :gutter="10"
        @dblclick.native="handleRowDbClick(user)"
        @click.native="handleRowClick(user)"
      >
        <el-col :span="2" class="user-member-avatar">
          <user-avatar :user="user" size="small" img-size="D" />
        </el-col>
        <el-col :span="6" class="overflow-ellispe">{{ user.name || user.employeeName }}</el-col>
        <el-col :span="8" class="overflow-ellispe">{{ user.employeeNo }}</el-col>
        <el-col :span="8" class="overflow-ellispe" :title="user.deptName">{{ user.deptName }}</el-col>
      </el-row>
      <div
        v-else-if="user.type === 0"
        class="user-member-organization"
      >
        <user-organization
          :key="user.deptId"
          active-index="member"
          :root="user"
          @handleUserSelect="handleRowClick"
          @handleOrganizationSelect="handleOrganizationSelect"
        />
      </div>
    </div>
    <div
      v-if="userList.length === 0"
      class="user-empty"
    >
      {{ $t('common.noData') }}
    </div>
  </div>
</template>

<script>
import UserAvatar from './UserAvatar'
import UserOrganization from './UserOrganization'

export default {
  name: 'UserMember',
  components: {
    UserAvatar,
    UserOrganization
  },
  props: {
    userList: {
      type: Array,
      default: () => []
    }
  },
  data() {
    return {}
  },
  methods: {
    handleCheckChange(user) {
      this.$emit('handleUserSelect', user)
    },
    handleRowClick(user) {
      this.$emit('handleUserSelect', user)
    },
    handleOrganizationSelect(dept) {
      this.$emit('handleOrganizationSelect', dept)
    },
    handleRowDbClick(user) {
      this.$emit('dblclick', user)
    }
  }
}
</script>

<style scoped>
.user-member-row {
  line-height: 40px;
}

.user-member-row:hover {
  background-color: #ecf8fe;
}

.user-member-avatar {
  display: flex;
  align-self: center;
  justify-content: center;
}

.user-empty {
  text-align: center;
  margin-top: 20px;
}

.user-member-organization {
  margin: 10px 0;
}
</style>
