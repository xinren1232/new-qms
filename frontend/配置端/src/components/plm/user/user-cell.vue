<template>
  <el-popover
    placement="right"
    width="330px"
    trigger="click"
    popper-class="user-cell-popper-class"
    :visible-arrow="false"
  >
    <div class="user-info-box">
      <el-avatar :src="avatar" :size="60" @error="()=>true">
        <img src="~@/assets/images/default-user.png" alt="">
      </el-avatar>
      <div class="user-info">
        <div class="user-info-item">
          <span class="label">姓名：</span>
          <span class="value">{{ userName }}</span>
        </div>
        <div class="user-info-item">
          <span class="label">工号：</span>
          <span class="value underline" @click="copyNumber">{{ jobNumber }}</span>
        </div>
        <div class="user-info-item">
          <span class="label">部门：</span>
          <span class="value">{{ department }}</span>
        </div>
        <!--        <div class="user-info-item">-->
        <!--          <span class="label">职位：</span>-->
        <!--          <span class="value">{{ position }}</span>-->
        <!--        </div>-->
        <div class="user-info-item">
          <span class="label">邮箱：</span>
          <span class="value underline">
            <a :href="'mailto:'+email">{{ email }}</a>
          </span>
        </div>
      </div>
    </div>
    <div v-if="showChat" class="user-info-item" style="margin: 10px 0 10px 155px;">
      <el-button type="primary" plain icon="el-icon-chat-line-round" @click="openChat">发送消息</el-button>
    </div>
    <div slot="reference" class="user-cell">
      <el-avatar v-if="model !=='NAME'" :class="[model !=='AVATAR'?'m-right':'']" :src="avatar" :size="avatarSize" @error="()=>true">
        <img src="~@/assets/images/default-user.png" alt="">
      </el-avatar>
      <span v-if="model !=='AVATAR'" class="user-name" :style="{fontSize:fontSize+'px'}">{{ userName }}</span>
    </div>
  </el-popover>

</template>

<script setup>
import { computed } from 'vue'
import store from '@/store'
import { Message } from 'element-ui'

const props = defineProps({
  jobNumber: {
    type: [String, Number],
    default: ''
  },
  avatarSize: {
    type: [String, Number],
    default: 28
  },
  fontSize: {
    type: Number,
    default: 14
  },
  userMaps: {
    type: Object,
    default: () => ({})
  },
  model: {
    type: String,
    default: 'ALL' // ALL 头像+姓名，NAME 姓名，AVATAR 头像
  },
  showChat: {
    type: Boolean,
    default: true
  }
})

// 从store中获取用户信息
const userInfoMap = computed(() => {
  return store.getters.userInfoMap || props.userMaps
})

// 计算部门
const department = computed(() => {
  return userInfoMap.value[props.jobNumber]?.deptName
})

// 计算职位
// eslint-disable-next-line no-unused-vars
const position = computed(() => {
  return userInfoMap.value[props.jobNumber]?.positionName
})
// 计算邮箱
const email = computed(() => {
  return userInfoMap.value[props.jobNumber]?.email
})
// 计算头像
const avatar = computed(() => {
  const a1 = props.jobNumber.slice(0, 4)
  const a2 = props.jobNumber.slice(4, 8)

  return `https://pfresource.transsion.com:19997/${a1}/${a2}/H_${props.jobNumber}_Origin.png`
})
// 计算用户名
const userName = computed(() => {
  return userInfoMap.value[props.jobNumber]?.name
})
const userId = computed(() => {
  return userInfoMap.value[props.jobNumber]?.userId
})
const copyNumber = () => {
  const input = document.createElement('input')

  input.value = props.jobNumber
  document.body.appendChild(input)
  input.select()
  document.execCommand('Copy')
  document.body.removeChild(input)
  Message.success('复制成功')
}

const openChat = () => {
  window.open(`https://applink.feishu.cn/client/chat/open?openId=${userId.value}`)
}
</script>

<style scoped lang="scss">
.user-cell{
  display: flex;
  align-items: center;
}
.m-right{
  margin-right: 5px;
}

.col--center{
  .user-cell{
    justify-content: center;
    .user-name{
      cursor: pointer;
      padding: 4px ;
      border-bottom: 1px dashed var(--primary-5);
      &:hover {
        background-color: #EAEDEC;
        border-radius: 4px;
      }
      &:active {
        background-color: #dee1e0;
      }
    }
  }
}

</style>
<style lang="scss">
.user-cell-popper-class{
  //box-shadow: 0 2px 12px 0 rgba(0,0,0,.5) !important;
  box-shadow: none !important;
}
.user-cell-popper-class:after{
  content: '';
  background: url("./bg.jpg") 50% no-repeat !important;
  background-size: cover;
  position: absolute;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  z-index: -1;
  filter: brightness(0.7);
  border-radius: 4px;
}
.el-avatar{
  cursor: pointer;
  transition: all 0.3s;
  &:hover {
  // 移上去毛玻璃效果
    transform: scale(1.1);
    transition: all 0.3s;
  }
}
.user-info-box{
  display: flex;
  align-items: center;
  padding: 0 15px;
  .el-avatar{
    margin-right: 10px;
    width: 80px;
    transform-style: preserve-3d !important;
    animation: rotate1 8s linear infinite !important;
    box-shadow: -1px 1px 10px #ffffff;
  }
  @keyframes rotate1 {
    0% {
      transform: rotateY(0);
    }
    100% {
      transform: rotateY(1turn);
    }
  }
  .user-info{
    padding: 10px;
    .user-info-item{
      display: flex;
      align-items: center;
      line-height: 30px;
      .label{
        width: 60px;
        font-size: 14px;
        color: #FFFFFF;
        font-weight: bold;
      }
      .value{
        font-size: 14px;
        color: #FFFFFF;
        padding: 0 3px;
      }
      .underline{
        text-decoration: underline;
        cursor: pointer;
        text-decoration-style: dotted;
        color: #FFFFFF;
        &:hover {
          background-color: #EAEDEC;
          border-radius: 4px;
          color: var(--primary) ;
        }
        &:active {
          background-color: #dee1e0;
        }
      }
    }
  }
}

</style>
