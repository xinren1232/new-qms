<script setup>
import { ref } from 'vue'
import { getRoleTree } from '@/views/app-manage/api'
import { Message } from 'element-ui'
import { flatTreeData } from '@/utils'
import router from '@/router'

const appList = ref([])

const getAppList = async () => {
  const { data,success,message } = await getRoleTree()

  if (!success) {
    Message.error(message)

    return
  }
  appList.value = flatTreeData(data,'children').map(item => {
    return {
      name: item.name,
      icon: item.icon ,
      url: item.url,
      typeCode: item.typeCode,
      typeValue: item.typeValue
    }
  })
}

getAppList()

const onJump = item => {
  switch (item.typeCode) {
    case 'object':
      {
        let routeData = router.resolve({ path: '/instance-list', query: { typeValue: item.typeValue } })

        window.open(routeData.href, '_blank')
      }
      break
    case '2':
      window.open(item.url)
      break
    case '3':
      window.open(item.url)
      break
  }
}
</script>

<template>
  <!-- 仿Mac 应用桌面，grid布局 -->
  <div class="app-container">
    <div class="app-list">
      <div v-for="item in appList" :key="item.name" class="app-item" @click="onJump(item)">
        <div class="app-item--icon">
          <el-avatar :src="item.icon" shape="square" :size="50" fit="contain" />
        </div>
        <div class="app-item--name">{{ item.name }}</div>
      </div>
    </div>
  </div>
</template>

<style scoped lang="scss">


.app-container {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  height: 100%;
  padding: 20px;
  background: rgba(255, 255, 255, 0.4);
  border-radius: 6px;
  overflow: hidden;
  position: relative;
  .app-list {
    display: grid;
    grid-template-columns: repeat(auto-fill, 100px);
    grid-gap: 15px;
    column-gap: 15px;
    justify-items: center;
    align-items: center;
    justify-content: space-around; /*水平*/
    align-content: flex-start; /*垂直*/
    width: 100%;
    height: 100%;
    overflow: auto;
    z-index: 1;

    .app-item {
      display: flex;
      flex-direction: column;
      align-items: center;
      justify-content: center;
      width: 100px;
      height: 100px;
      border-radius: 6px;
      //background-color: var(--plain-background);
      //box-shadow: 0 0 10px 0 rgba(0, 0, 0, 0.1);
      cursor: pointer;
      transition: all 0.3s ease-in-out;

      &:hover {
        box-shadow: 0 0 10px 0 rgba(0, 0, 0, 0.2);
        transform: scale(1.1);
      }

      .app-item--icon {
        font-size: 40px;
        color: var(--primary-color);
      }

      .app-item--name {
        font-size: 13px;
        color: var(--text-color);
        font-weight: bold;
        max-width: 100px;
        text-align: center;
        overflow: hidden;
        text-overflow: ellipsis;
        white-space: nowrap;
      }
    }
  }
}
.app-container::before {
  content: "";
  position: absolute;
  width: 100%;
  height: 100%;
  backdrop-filter: blur(80px);
  border-radius: 6px;
}

</style>
