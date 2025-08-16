<template>
  <svg
    viewBox="25 25 50 50"
    class="circular"
  >
    <circle
      cx="50"
      cy="50"
      r="20"
      fill="none"
      class="path"
    />
  </svg>
</template>

<style lang="scss" scoped>
.circular {
  height: 30px;
  width: 30px;
  vertical-align: middle;
  animation: loading-rotate 2s linear infinite;

  .path {
    animation: loading-dash 1.5s ease-in-out infinite;
    stroke: var(--primary);
    stroke-dasharray: 90, 150;
    stroke-dashoffset: 0;
    stroke-width: 2;
    stroke-linecap: round;
  }
}
</style>
