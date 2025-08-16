<template>
  <div class="el-card h-full">
    <TrDefaultPage type="noaccessright" :title="$route.query.type" height="100%" />
  </div>
</template>

<script setup>
import TrDefaultPage from '@@/feature/TrDefaultPage'
</script>
