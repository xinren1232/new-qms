# TrEditTableColumn 可编辑表格列

### 组件说明

可编辑表格表列

### 使用场景

配合`TrEditTableColumn`组件使用

### 组件使用

```html
<template>
  <tr-edit-table-column
    :options="{
        label: '地址',
        prop: 'address',
        editable: false,
      }"
    label="日期"
    prop="date"
  />
</template>

<script>
  import TrEditTableColumn from '@@/feature/TrEditTableColumn'

  export default {
    components: {
      TrEditTableColumn
    }
  }
</script>
```

### 注意点

1. slots
   组件只提供一个默认插槽: `default`

   default: 组件默认展示内容

```html
<tr-edit-table-column label="默认">默认插槽内容</tr-edit-table-column>
```

2. props
   该组件支持除了 element-ui Dialog 组件提供的原生属性之外还拓展增加了两个属性，如下：

```js
props: {
  editStatus: {
    type: Object,
    default: () => ({}),
    comment: '组件当前编辑状态' // ? 待确定
  },
  options: {
    type: Object,
    default: () => ({
      prop: '', // 在model中取哪一个字段
      label: '', // 表头的显示的文字
      editable: true, // 表格列是否可编辑(是否展示输入框)
      component: null, // 自定义渲染的组件
      type: 'text', // 使用预设的组件名
      rules: [{ required: true, message: '不能为空' }] // 内容校验的规则
    }) // 组件使用时传入的配置项 接受的参数如上
  },
  disabled: {
    type: Boolean,
    default: false,
    comment: '是否禁用单元格的编辑'
  }
}
```

3. events
   column-select: 表单内容修改时触发
   column-blur: 表单失去焦点时触发
