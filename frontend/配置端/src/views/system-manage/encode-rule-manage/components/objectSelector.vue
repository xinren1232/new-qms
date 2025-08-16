<template>
  <el-cascader v-model="form.aaa" append-to-body :options="objectTreeData" :props="{label:'name',value:'modelCode'}" />
</template>

<script>
export default {
  name: 'ObjectSelector'
}
</script>

<style scoped>

</style>
