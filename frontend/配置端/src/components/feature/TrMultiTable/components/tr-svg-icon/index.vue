<template>
  <span
    v-if="isExternal"
    :style="styleExternalIcon"
    class="svg-external-icon svg-icon"
    v-bind="$attrs"
    v-on="$listeners"
  />
  <span
    v-else
    :class="svgClass"
    v-on="$listeners"
  >
    <svg aria-hidden="true">
      <use :xlink:href="iconName" />
    </svg>
  </span>
</template>

<script>
import { isExternal } from '../../utils/index'

export default {
  name: 'TrSvgIcon',
  props: {
    className: {
      type: String,
      default: ''
    },
    iconClass: {
      type: String,
      required: true
    }
  },
  computed: {
    isExternal() {
      return isExternal(this.iconClass)
    },
    iconName() {
      return `#icon-${this.iconClass}`
    },
    svgClass() {
      if (this.className) {
        return 'tr-svg-icon ' + this.className
      } else {
        return 'tr-svg-icon'
      }
    },
    styleExternalIcon() {
      return {
        mask: `url(${this.iconClass}) no-repeat 50% 50%`,
        '-webkit-mask': `url(${this.iconClass}) no-repeat 50% 50%`
      }
    }
  }
}
</script>

<style lang="scss" scoped>
.tr-svg-icon {
  display: inline-flex;
  width: 1em;
  height: 1em;
  color: inherit;
  overflow: hidden;

  svg {
    width: 100%;
    height: 100%;
    pointer-events: none;
    fill: currentColor !important;
  }
}

.svg-external-icon {
  background-color: currentColor;
  mask-size: cover !important;
  display: inline-block;
}
</style>
