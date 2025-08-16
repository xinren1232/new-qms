<template>
  <div class="image-view-box">
    <DocSuffixIcon v-for="(p,index) in fileList" :key="index" class="el-image" :url="p.url" :size="24" />
  </div>
</template>
<script>
export default {
  name: 'FileUploadView'
}
</script>
<script setup>
import DocSuffixIcon from '../../components/doc-suffix-icon/index.vue'

defineProps({
  fileList: {
    type: Array,
    default: () => ([])
  }
})
</script>

<style scoped lang="scss">
.image-view-box{
  width: 100%;
  .el-image {
    cursor: pointer;
    margin-right: 10px;
    transition: all 0.3s;
    border-radius: 2px;
    overflow: hidden;
    vertical-align: middle;
    display: inline-flex;

    &:hover {
      box-shadow: 0 0 5px 0 rgba(0, 0, 0, 0.2);
      transform: scale(1.1);
    }
  }
}
</style>
