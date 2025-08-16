<template>
  <div class="image-view-box">
    <el-tooltip v-if="value" :open-delay="400" effect="light">
      <template #content>
        <el-image :src="value" style="width: 200px;height: 140px" fit="contain" />
      </template>
      <el-image :src="value" />
    </el-tooltip>
  </div>
</template>
<script>
export default {
  name: 'PictureUploadSingleView'
}
</script>
<script setup>

defineProps({
  value: {
    type: String,
    default: ''
  }
})
</script>

<style scoped lang="scss">
.image-view-box{
  width: 100%;
  .el-image {
    cursor: pointer;
    margin-right: 10px;
    width: 40px;
    height: 30px;
    transition: all 0.3s;
    border-radius: 2px;
    overflow: hidden;
    vertical-align: middle;

    img {
      width: 44px;
      height: 24px;
    }

    &:hover {
      box-shadow: 0 0 5px 0 rgba(0, 0, 0, 0.2);
      transform: scale(1.1);
    }
  }
}
</style>
