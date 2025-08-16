<template>
  <el-dialog
    v-dialogDrag
    :title="$t('component.trUser.title')"
    :visible.sync="visible"
    width="60%"
    top="10vh"
    class="user-dialog vxe-table--ignore-clear"
    append-to-body
    :close-on-click-modal="false"
    @closed="handleClose"
  >
    <el-menu
      :default-active="activeIndex"
      class="el-menu-demo"
      mode="horizontal"
      @select="handleIndexSelect"
    >
      <el-menu-item index="recent">{{ $t('component.trUser.recent') }}</el-menu-item>
      <el-menu-item index="organization">{{ $t('component.trUser.org') }}</el-menu-item>
      <el-menu-item index="member">{{ $t('component.trUser.person') }}</el-menu-item>
    </el-menu>
    <user-dialog-layout
      v-if="visible"
      v-loading="loading"
    >
      <template #userFind>
        <user-find @handleUserFind="handleUserFind" />
      </template>
      <template #userCount>
        <div v-if="selectedDeptName">
          <span class="selected-dept">{{ selectedDeptName }}</span>
          <span class="little-count">({{ selectedUser.length }} )</span>
        </div>
        <div v-else>{{ $t('component.trUser.selected', { count: realSelectedUserCount }) }}</div>
        <div>
          <el-button
            size="small"
            @click="clearSelectedUser"
          >
            {{ $t('component.trUser.clear') }}
          </el-button>
        </div>
      </template>
      <template #userList>
        <user-member
          v-if="activeIndex === 'recent'"
          :user-list="userRecent"
          @handleUserSelect="handleUserSelect"
          @dblclick="handlerUserDbclick"
        />
        <user-organization
          v-else-if="activeIndex === 'organization'"
          @handleUserSelect="handleUserSelect"
          @handleOrganizationSelect="handleOrganizationSelect"
          @dblclick="handlerUserDbclick"
        />
        <user-member
          v-else
          :user-list="userMembers"
          @handleUserSelect="handleUserSelect"
          @handleOrganizationSelect="handleOrganizationSelect"
          @dblclick="handlerUserDbclick"
        />
      </template>
      <template #userSelected>
        <user-selected
          :user-list="selectedUser.slice(0, selectedUserCount)"
          :un-recover-list="unRecoverList"
          @removeSelectedUser="removeSelectedUser"
          @loadSelectedUser="loadSelectedUser"
        />
      </template>
    </user-dialog-layout>
    <div
      slot="footer"
      class="text-center"
    >
      <el-button @click="visible = false">{{ $t('common.cancel') }}</el-button>
      <el-button
        type="primary"
        @click="handleConfirm"
      >
        {{ $t('common.confirm') }}
      </el-button>
    </div>
  </el-dialog>
</template>

<script>
import { orderBy } from 'lodash'

import UserDialogLayout from './UserDialogLayout'
import UserFind from './UserFind'
import UserMember from './UserMember'
import UserOrganization from './UserOrganization'
import UserSelected from './UserSelected'

export default {
  name: 'UserDialog',
  components: {
    UserDialogLayout,
    UserFind,
    UserSelected,
    UserOrganization,
    UserMember
  },
  inject: ['httpService', 'multiple'],
  props: {
    value: {
      type: Boolean,
      default: false
    },
    defaultUser: {
      type: [String, Array, Object],
      default: null
    },
    unRecoverList: {
      type: Array,
      default: () => []
    }
  },
  data() {
    return {
      loading: false,
      activeIndex: 'recent',
      userMembers: [],
      userRecent: [],
      selectedUser: [],
      selectedUserCount: 10,
      selectedDeptName: ''
    }
  },
  computed: {
    visible: {
      get() {
        return this.value
      },
      set(value) {
        this.$emit('input', value)
      }
    },
    // 修复selectedUser设置defaultUser的Bug： 初始化未选中人员，打开弹出框时，会显示“已选中1个”，
    // 清除掉不合法值 undefined、null、""、0、false、NaN
    realSelectedUserCount() {
      return this.selectedUser.filter((user) => user && user.employeeNo).length
    }
  },
  watch: {
    defaultUser: {
      immediate: true,
      handler(value) {
        this.selectedUser = [...value]
      }
    }
  },
  created() {
    this.handleOpen()
  },

  methods: {
    handleOpen() {
      this.selectedDeptName = ''
      this.activeIndex = 'recent'
      this.userRecent = this.httpService.getUserRecent()

      const { defaultUser } = this

      if (defaultUser) {
        this.selectedUser = Array.isArray(defaultUser) ? [...defaultUser] : [defaultUser]
      } else {
        this.selectedUser = []
      }
    },
    handleClose() {
      this.userMembers = []
      this.userRecent = []
    },
    handleIndexSelect(index) {
      this.activeIndex = index
    },
    removeSelectedUser(user) {
      const index = this.selectedUser.findIndex((s) => s.employeeNo === user.employeeNo)

      this.selectedUser.splice(index, 1)
    },
    clearSelectedUser() {
      this.selectedDeptName = ''
      this.selectedUserCount = 20
      this.selectedUser = []
    },
    loadSelectedUser() {
      this.selectedUserCount += 10
    },
    async handleUserFind(query) {
      if (query) {
        this.activeIndex = 'member'
        this.userMembers = []

        this.loading = true
        const data = await this.httpService.queryDeptAndEmployee(query).catch(() => {
          this.loading = false
        })

        this.userMembers = orderBy(data, (s) => s.type || '1')
        if (this.$attrs.containDimissionUser) {
          // queryLeaveEmployee
          const res = await this.httpService.queryLeaveEmployee(query).finally(() => {
            this.loading = false
          })

          this.userMembers = this.userMembers.concat(orderBy(res, (s) => s.type || '1'))
        } else {
          this.loading = false
        }
      } else {
        this.userMembers = []
      }
    },
    handleConfirm() {
      this.$emit('userSelect', this.selectedUser)
      this.visible = false
      this.httpService.setUserRecent(this.selectedUser)
    },
    handleUserSelect(user) {
      if (this.multiple) {
        const existUser = this.selectedUser.find((s) => s.employeeNo === user.employeeNo)

        if (!existUser) {
          this.selectedUser.unshift(user)
          this.selectedDeptName = ''
        }
      } else {
        this.selectedUser = [user]
      }
    },
    handlerUserDbclick(user) {
      this.handleUserSelect(user)
      if (!this.multiple) {
        this.handleConfirm()
      }
    },
    handleOrganizationSelect(dept) {
      this.selectedUser = []
      this.selectedUserCount = 20
      this.loading = true
      this.httpService
        .listEmployeeByParam(dept.deptId)
        .then((data) => {
          if (data?.code === '500') {
            this.$message.error(data.message)
          } else {
            this.selectedDeptName = dept.deptName
            this.selectedUser = data.dataList
          }
        })
        .finally(() => {
          this.loading = false
        })
    }
  }
}
</script>

<style>
.user-dialog .el-dialog {
  min-width: 800px;
}

.user-dialog .el-dialog__body {
  padding: 10px 20px;
}

.user-dialog .overflow-ellispe,
.user-avatar-popover .overflow-ellispe {
  text-overflow: ellipsis;
  overflow: hidden;
  white-space: nowrap;
}
</style>
