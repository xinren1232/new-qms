<template>
  <el-card
    v-if="card"
    class="picture-box"
    style="text-align: center"
    :style="{ backgroundColor, color: textColor, fontSize: textSize + 'px' }"
  >
    <tr-svg-icon
      v-if="!hasSvg"
      :style="{ fontSize: iconSize + 'px' }"
      class="default"
      :icon-class="type"
    />
    <img
      v-else
      src="./images/nodata.png"
    >
    <div
      class="title"
      :style="{ top: titlePosition + 'px' }"
    >
      {{ title }}
    </div>
  </el-card>

  <div
    v-else
    class="picture-box"
    style="text-align: center"
    :style="cardStyle"
  >
    <tr-svg-icon
      v-if="!hasSvg"
      :style="{ fontSize: iconSize + 'px' }"
      class="default"
      :icon-class="type"
    />
    <img
      v-else
      src="./images/nodata.png"
      alt=""
    >
    <div
      class="title"
      :style="{ top: titlePosition + 'px' }"
    >
      {{ title }}
    </div>
  </div>
</template>

<script>
export default {
  props: {
    card: {
      type: Boolean,
      default: false // 是否用卡片包裹
    },
    hasSvg: {
      type: Boolean,
      default: false // 是否甘特
    },
    type: {
      type: String,
      default: 'nodata' // 暂无数据呦~
      /**
       *
       * commingsoon  敬请期待
       * loadingfailed 加载失败，刷新下试试~
       * uploadfaild 上传失败，重新试一下哦~
       * noaccessright 没有访问权限呦~
       * nonet 哎呀，网络溜走了~
       * notfoundpage 404 找不到请求页面
       * servererror 500 服务器错误
       * requestrejected 403 拒绝请求
       * systemprogressing 系统升级中~
       */
    },
    boxStyes: {
      type: Object,
      default: () => ({}) // 其他样式
    },
    height: {
      type: String,
      default: '256px' // 盒子高度
    },
    width: {
      type: Number,
      default: 100 // 盒子高度
    },
    textColor: {
      type: String,
      default: '#aaaaab' // 文字颜色
    },
    textSize: {
      type: Number,
      default: 12 // 文字大小
    },
    title: {
      type: String,
      default() {
        return this.$t('storage.noData')
      } // 缺省说明
    },
    iconSize: {
      type: [Number, String],
      default: 200 // svg图标大小
    },
    backgroundColor: {
      type: String,
      default: '#fff' // 背景颜色
    },
    titlePosition: {
      type: Number,
      default: -20
    }
  },
  computed: {
    cardStyle({ backgroundColor, boxStyes, height, textColor, textSize: fontSize, width }) {
      return {
        backgroundColor,
        color: textColor,
        height,
        width: width + '%',
        fontSize: fontSize + 'px',
        ...boxStyes
      }
    }
  }
}
</script>

<style lang="scss" scoped>
.picture-box {
  padding: 10px 0;
  display: flex;
  flex-direction: column;
  justify-content: center;
  align-items: center;

  // .title {
  // position: relative;
  // height: 1.5em;
  // white-space: nowrap;
  // text-overflow: ellipsis;
  // overflow: hidden;
  // }
}
</style>
>
