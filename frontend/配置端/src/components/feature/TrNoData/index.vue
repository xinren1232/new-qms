<!--
 * @Author: KunQu
 * @Date: 2021-01-28 11:49:51
 * @LastEditTime: 2021-07-26 10:22:23
 * @Description: file content
-->
<template>
  <div
    class="TrNoData"
    :style="{ color: textColor, fontSize: textSize + 'px' }"
  >
    <div
      v-if="type === 'common'"
      class="box"
      :style="{ height: height + 'px' }"
    >
      <img
        src="./images/common-no-data.png"
        alt=""
      >
      <span>暂无数据</span>
    </div>

    <div
      v-if="type === 'list'"
      class="box"
      :style="{ height: height + 'px' }"
    >
      <img
        src="./images/list-no-data.png"
        alt=""
      >
      <span>暂无数据</span>
    </div>

    <div
      v-if="type === 'search'"
      class="box"
      :style="{ height: height + 'px' }"
    >
      <img
        src="./images/search-no-data.png"
        alt=""
      >
      <span>搜索无结果</span>
    </div>
    <div
      v-if="type === 'offline'"
      class="box"
      :style="{ height: height + 'px' }"
    >
      <img
        src="./images/offline.png"
        alt=""
      >
      <span>没有网络啦</span>
    </div>
    <div
      v-if="type === 'permission'"
      class="box"
      :style="{ height: height + 'px' }"
    >
      <img
        src="./images/permission.png"
        alt=""
      >
      <span>温馨提示</span>
      <span>您无数据访问权限</span>
    </div>
    <div
      v-if="type === 'tree'"
      class="box"
      :style="{ height: height + 'px' }"
    >
      <img
        src="./images/permission.png"
        alt=""
      >
      <span>{{ treetitle }}</span>
    </div>
    <div
      v-if="type === 'project'"
      class="box"
      :style="{ height: height + 'px' }"
    >
      <img
        src="./images/common-no-data.png"
        alt=""
      >
      <span>{{ projectTitle }}</span>
    </div>
  </div>
</template>

<script>
export default {
  name: 'TrNoData',
  props: {
    projectTitle: {
      type: String,
      default: ''
    },
    treetitle: {
      type: String,
      default: '角色节点为空'
    },
    type: {
      type: String,
      default: 'common' // common 默认无数据图标||search 搜索 || list 列表 || permission 无权限||offline 无网络
    },
    height: {
      type: Number,
      default: 200 // 盒子高度
    },
    textColor: {
      type: String,
      default: '#aaaaab' // 文字颜色
    },
    textSize: {
      type: Number,
      default: 12 // 文字大小
    }
  }
}
</script>

<style scoped lang="scss">
.TrNoData {
  flex: 1;
  .box {
    display: flex;
    flex-direction: column;
    align-items: center;
    justify-content: center;
    user-select: none;
  }
  img {
    height: 130px;
    margin-bottom: 5px;
  }
}
</style>
