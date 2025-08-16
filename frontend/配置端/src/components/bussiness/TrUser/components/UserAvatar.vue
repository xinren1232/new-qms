<template>
  <span
    class="user-avatar"
    @click="avatarClick"
  >
    <tr-el-avatar
      v-if="user.employeeNo"
      :key="imgUrl(imgSize)"
      :size="size"
      class="cursor-pointer"
      :src="imgUrl(imgSize)"
    >
      <slot name="default">{{ head }}</slot>
    </tr-el-avatar>
    <!--    <el-popover-->
    <!--      ref="popover"-->
    <!--      popper-class="user-avatar-popover"-->
    <!--      :open-delay="600"-->
    <!--      placement="right"-->
    <!--      width="300"-->
    <!--      trigger="hover"-->
    <!--    >-->
    <!--      <el-row>-->
    <!--        <el-col :span="8">-->
    <!--          <tr-el-avatar :size="60" class="user-avatar-popover" :src="imgUrl(imgSize)" />-->
    <!--        </el-col>-->
    <!--        <el-col :span="16">-->
    <!--          <div class="overflow-ellispe">-->
    <!--            {{ $t('component.trUser.name') }}：{{ user.name || user.employeeName }}-->
    <!--          </div>-->
    <!--          <div class="overflow-ellispe">{{ $t('component.trUser.jobNumber') }}：{{ user.employeeNo }}</div>-->
    <!--          <div class="overflow-ellispe">{{ $t('component.trUser.dept') }}：{{ user.deptName }}</div>-->
    <!--          <div class="overflow-ellispe">{{ $t('component.trUser.email') }}：{{ user.email }}</div>-->
    <!--        </el-col>-->
    <!--      </el-row>-->
    <!--    </el-popover>-->
  </span>
</template>

<script>
import TrElAvatar from '@@/feature/TrElAvatar'

export default {
  name: 'UserAvatar',
  components: { TrElAvatar },
  props: {
    user: {
      type: [Object, String, Array],
      default: () => ({})
    },
    size: {
      type: [String, Number],
      default: 'small'
    },
    imgSize: {
      type: String,
      default: 'S'
    }
  },
  data() {
    return { isMounted: false }
  },
  computed: {
    head() {
      const name = this.user.name || this.user.employeeName

      return name ? name.toString().substring(0, 1) : ''
    },
    // 只有在显示的时候，加载大头像，减少默认请求
    popoverImgUrl() {
      if (this.isMounted) {
        return this.$refs.popover && (this.$refs.popover.showPopper ? this.imgUrl('L') : '')
      } else {
        return undefined
      }
    }
  },
  mounted() {
    this.isMounted = true
  },
  methods: {
    avatarClick(event) {
      window.showUserProfileByEmployeeNo(this.user.employeeNo, event.target)
    },
    imgUrl() {
      const jobNumber = this.user.employeeNo

      if (!jobNumber) return ''
      const a1 = jobNumber.slice(0, 4)
      const a2 = jobNumber.slice(4, 8)

      return `https://pfresource.transsion.com:19997/${a1}/${a2}/H_${jobNumber}_D.png`

    }
  }
}
</script>

<style scoped lang="scss">
::v-deep tt-profile {
  position: fixed;
  z-index: 1;
}

.user-avatar ::v-deep .el-avatar {
  background-color: #44a1ff;
}

.user-avatar {
  display: flex;
  align-self: center;
  justify-content: center;
}

.user-avatar-popover span {
  background-color: #44a1ff;
  font-size: 60px;
}

.user-avatar-popover .overflow-ellispe {
  line-height: 26px;
}
</style>
