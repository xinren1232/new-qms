<script setup>

import TrUser from '@@/bussiness/TrUser'
import { ref } from 'vue'
import { Message } from 'element-ui'
import { updateRoleMember, getRoleMemberByCode } from '../api/index.js'

const role = ref(null)
const user = ref([])
const TrUserRef = ref(null)

const confirmUser = (userList) => {
  const { bid, code, tenantId,type } = role.value
  const commonParams = {
    roleBid: bid,
    roleCode: code,
    tenantId,
    type
  }
  const params = userList.map(item => {
    return {
      ...commonParams,
      jobNumber: item?.employeeNo,
      userName: item?.employeeName
    }
  })

  updateRoleMembers(params)
}
// 添加成员
const updateRoleMembers = async (params) => {
  const { success, message } = await updateRoleMember(role.value.code, params)

  if (!success) {
    Message.error(message)

    return
  }
  Message.success(message)
}
const showUserDialog = (_role) => {
  if (!_role) return Message.error('请选择角色')
  role.value = _role
  TrUserRef.value.showUserDialog()
  console.log(role.value.code)
  getRoleMember(role.value.code)
}

// 获取角色成员
const getRoleMember = async (roleCode) => {
  const { data, success, message } = await getRoleMemberByCode(roleCode)

  if (!success) return Message.error(message)
  user.value = data.map(item => item.jobNumber)
}

defineExpose({
  showUserDialog
})
</script>

<template>
  <TrUser
    ref="TrUserRef"
    v-model="user"
    style="display: none"
    multiple
    @confirm="confirmUser"
  />
</template>

<style scoped lang="scss">

</style>
