<template>
  <el-select
    v-model="modelValue"
    filterable
    remote
    placeholder="请输入姓名或工号"
    :remote-method="remoteMethod"
    :loading="loading"
    popper-class="user-select"
  >
    <el-option
      v-for="item in options"
      :key="item.value"
      :label="item.label"
      :value="item.value"
    >
      <div style="display: flex;align-items: center">
        <div class="left" style="margin-right: 12px;display: flex;align-items: center">
          <el-avatar :size="28" :src="renderUserAvatar(item.value)" @error="errorHandler">
            <img :src="defaultIcon" alt=""></el-avatar>
        </div>
        <div class="right">
          <div>{{ item.label }}</div>
          <div style="font-size: 12px;font-weight: 400;margin-top: 6px">{{ item.dep }}</div>
        </div>
      </div>
    </el-option>
  </el-select>
</template>
<script>
import defaultIcon from '@/assets/images/default-user.png'

export default {
  name: 'UserSelect'
}
</script>
<script setup>
import { computed, onMounted, ref } from 'vue'
import { queryUserLitByDeptId } from '../../api/user'
import request from '../../request'
import store from '@/store'

defineProps({
  value: {
    type: [Array, String],
    default: ''
  }
})

const emits = defineEmits(['input'])

// 计算属性 modelValue
const modelValue = computed({
  get({ value }) {
    return value
  },
  set(value) {
    emits('input', value)
  }
})
const loading = ref(false)
const options = ref([])

const userInfoMap = computed(() => store.getters.userInfoMap)

const remoteMethod = (query) => {
  if (userInfoMap.value[query]) return
  loading.value = true
  request({
    url: process.env.VUE_APP_GATEWAY_URL + '/uac-user-service/v2/api/uac-user/user/webPages',
    method: 'post',
    data: {
      current: 1,
      param: { keyword: query },
      size: 20
    }
  }).then(({ data }) => {
    loading.value = false
    const map = {}

    const remoteData = data.dataList.map(item => {
      map[item.employeeNo] = { realName: item.employeeName, ...item }

      return {
        label: item.employeeName || item.realName,
        value: item.employeeNo,
        dep: item.deptName,
        avatar: renderUserAvatar(item.employeeNo)
      }
    })

    store.dispatch('user/setUserInfoMap', { ...map, ...store.getters.userInfoMap })

    options.value = remoteData
  })
}

if (modelValue.value)remoteMethod(modelValue.value)

const renderUserAvatar = jobNumber => {
  if (!jobNumber) return ''
  const a1 = jobNumber.slice(0, 4)
  const a2 = jobNumber.slice(4, 8)

  return `https://pfresource.transsion.com:19997/${a1}/${a2}/H_${jobNumber}_D.png`
}

// 获取当前登录人部门成员列表
// eslint-disable-next-line no-unused-vars
const getCurrentDepUserList = async () => {
  const userInfo = store.getters.user

  const params = {
    searchTotal: false,
    param: { deptId: userInfo.deptId },
    size: 500
  }
  const { data } = await queryUserLitByDeptId(params)

  const depUserList = data.dataList.map(item => {
    return {
      label: item.employeeName || item.realName,
      value: item.employeeNo,
      dep: item.deptName,
      avatar: renderUserAvatar(item.employeeNo)
    }
  })

  // 去重合并
  options.value = [...new Set([...options.value, ...depUserList])]
}

const errorHandler = () => {
  return 'https://cube.elemecdn.com/3/7c/3ea6beec64369c2642b92c6726f1epng.png'
}

onMounted(() => {
  const storeUser = []
  const userMap = store.getters.userInfoMap

  for (const key in userMap) {
    storeUser.push({
      label: userMap[key].name || userMap[key].realName,
      value: key,
      dep: userMap[key].deptName,
      avatar: renderUserAvatar(key)
    })
  }

  if (storeUser.length) {
    // 当前用户modelValue放在第一位
    const index = storeUser.findIndex(item => item.value === modelValue.value)

    if (index > -1) {
      options.value = storeUser.splice(index, 1)
    }
  }
  // getCurrentDepUserList()
})

</script>

<style lang="scss" scoped>
.user-select{
  .el-select-dropdown__item{
    line-height: 100% !important;
    height: 50px !important;
    padding: 10px 20px;
  }
}

</style>
