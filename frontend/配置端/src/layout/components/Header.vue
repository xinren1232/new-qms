<script setup>
const LoginUser = defineAsyncComponent(() => import('./LoginUser.vue'))
</script>

<template>
  <header class="layout-header fixed left-0 top-0 z-5 w-full h-14 px-6 shadow-$shadow-sm flex items-center b-(b-1 b-solid $border-color)">
    <router-link class="h-full flex flex-col justify-center outline-none" to="/">
      <img class="app-logo ml-6" src="@/assets/images/app/logo.png" width="100" alt="">
      <h1 class="mt--.5 mb-0 text-xs op-70 text-$white font-normal scale-90">QMS AI管理系统配置中心</h1>
    </router-link>

    <login-user class="ml-auto" />
  </header>
</template>

<style scoped>
.layout-header {
  background: var(--primary) url('@/assets/images/app/navbar-bg.png') no-repeat right center;
}

.app-logo {
  transform: translateX(-180px);
  filter: drop-shadow(160px 0 0 var(--white));
}

.nav-items {
  font-size: 16px;
}

.nav-items:hover a::after {
  transition: transform .2s ease-in-out;
}

.nav-items a::after {
  content: "";
  top: 52px;
  left: 10%;
  width: 80%;
  position: absolute;
  border-top: 6px solid #fff;
  border-radius: 6px;
  transform: translateY(4px)
}

.nav-items a.router-link-exact-active::after {
  transform: translateY(0)
}
</style>
