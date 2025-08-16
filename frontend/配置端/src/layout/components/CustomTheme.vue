<script setup>

import { THEME_MAPS } from '../composables/system-theme.js'

const { theme, onChange } = inject('system-theme')

</script>

<template>
  <el-select v-model="theme" :popper-append-to-body="false" @change="onChange">
    <el-option v-for="value, key in THEME_MAPS" :key="key" class="flex items-center" :label="value.name" :value="key">
      <i class="inline-block w-4 h-4 mr-1.5 rounded-$radius" :style="`background-color:${value.color}`" />
      {{ value.name }}
    </el-option>
  </el-select>
</template>
