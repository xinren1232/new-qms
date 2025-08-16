<script setup>
import store from '@/store/index.js'

const props = defineProps({
  jobNumber: {
    type: String,
    default: ''
  }
})

const userInfo = computed(() => {
  const a1 = props.jobNumber.slice(0, 4)
  const a2 = props.jobNumber.slice(4, 8)

  return {
    src: `https://pfresource.transsion.com:19997/${a1}/${a2}/H_${props.jobNumber}_S.png`,
    name: store.getters.userInfoMap[props.jobNumber]?.name
  }
})
</script>

<template>
  <div v-if="jobNumber" class="flex items-center justify-center">
    <img :src="userInfo.src" class="rounded-full mr-1" loading="lazy" fetchpriority="low" width="24" height="24" alt="">
    <span class="pt-1">{{ userInfo.name }}</span>
  </div>
</template>
