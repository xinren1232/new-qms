<!--
 * @Author: BanLi
 * @Date: 2021-07-26 09:57:55
 * @LastEditTime: 2021-07-27 09:56:43
 * @Description: 网络离线组件, 点击可以刷新页面
-->

<template>
  <transition name="el-fade-in-linear">
    <div
      v-if="display"
      class="network-offline"
    >
      <div
        v-outside-click="close"
        class="network-offline__content"
      >
        <p class="title">提示信息</p>
        <i
          class="close-icon el-icon-close"
          @click="close"
        />
        <!--<tr-no-data type="offline" />-->
        <tr-default-page
          type="nonet"
          title="哎呀，网络溜走了~"
        />

        <div class="text-center margin-top">
          <el-button
            class="cancel"
            @click="close"
          >
            关闭
          </el-button>
          <el-button
            class="confirm"
            type="primary"
            @click="refresh"
          >
            刷新
          </el-button>
        </div>
      </div>
    </div>
  </transition>
</template>

<script>
import TrDefaultPage from '@@/feature/TrDefaultPage'

export default {
  name: 'NetworkOffline',
  components: { TrDefaultPage },
  computed: {
    display({ $store }) {
      return !$store.getters.online
    }
  },
  mounted() {
    window.addEventListener('online', this.updateIndicator, false)
    window.addEventListener('offline', this.updateIndicator, false)
  },
  beforeDestroy() {
    window.removeEventListener('online', this.updateIndicator, false)
    window.removeEventListener('offline', this.updateIndicator, false)
  },
  methods: {
    close() {
      this.changeStatus(true)
    },
    refresh() {
      this.close()
      this.$router.go(0)
    },
    changeStatus(status) {
      this.$store.dispatch('app/changeOnlineStatus', status)
    },
    updateIndicator() {
      this.changeStatus(window.navigator.onLine)
    }
  }
}
</script>

<style lang="scss" scoped>
.network-offline {
  position: fixed;
  left: 0;
  top: 0;
  right: 0;
  bottom: 0;
  background: #00000090;
  z-index: 9999;

  &__content {
    position: absolute;
    left: 50%;
    top: 50%;
    padding: 15px 15px 20px;
    transform: translate(-50%, -50%);
    width: 350px;
    border-radius: 8px;
    background: #fff;

    .title {
      margin: 0;
      font-size: 14px;
      font-weight: bold;
    }

    .close-icon {
      position: absolute;
      right: 10px;
      top: 10px;
      padding: 5px;
      text-align: center;
      border-radius: 5px;
      transition: background 0.2s linear;
      cursor: pointer;

      &:hover {
        background: var(--primary-8);
        color: var(--primary);
      }
    }

    .el-button {
      padding: 6px 25px;
    }
  }
}
</style>
