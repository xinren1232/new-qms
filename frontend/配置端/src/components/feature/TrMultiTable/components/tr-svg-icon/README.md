# SvgIcon 图标

### 组件说明

通过 icon-class 来控制显示的 SVG 的 ICON 图标组件

### 使用场景

该图标组件非常常见，你在任何地方都可以看到它的踪影

### 组件使用

```html
<template>
  <tr-svg-icon icon-class="404" />
</template>

<script>
  import SVGIcon from '@@/SvgIcon'

  export default {
    components: {
      SVGIcon
    }
  }
</script>
```

### 注意点

1. slots  
   \-

2. props

```js
props: {
  className: {
    type: String,
    default: '',
    comment: '为组件提供额外的class name'
  },
  iconClass: {
    type: String,
    required: true,
    comment: '想要展示的ICON图标名称'
  }
}
```

3. events  
   \-
