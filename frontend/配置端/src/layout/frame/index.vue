<template>
  <router-view :key="$route.path" />
</template>

<script>
import { saveThemeName } from '@@/framework/TrToggleTheme/theme-color'

export default {
  name: 'MyFrame',
  mounted() {
    saveThemeName((this.current = localStorage.savedThemeName || 'default'))
  }
}
</script>

<style lang="scss">
.flow-container {
  padding: var(--padding);
}
</style>
