# TrSelectTree 下拉树选择框

### 组件说明

将选择框及树组件集成在一起，可在多种场景使用，比如在下拉框中筛选树数据

### 使用场景

下拉框 + 树数据展示

### SelectTree Attributes

除了继承 el-select 的属性，还支持以下属性：

| 参数          | 说明                                              | 类型          | 可选值        | 默认值  |
| ------------- | ------------------------------------------------ | ------------- | ------------- | ------- |
| value         | 同 input 的 value                                 | object/array | —             | {}/[]   |
| data          | 树组件的数据源                                     | array        | —             | []      |
| nodeKey       | 数据的唯一标识，必须要保证唯一                       | string       | string        | 'id'     |
| multiple      | 是否可以多选                                       | boolean      | true/false    | false   |
| disabled      | 是否禁用                                          | boolean      | true/false    | false   |
| clearable     | 是否一键清除数据                                   | boolean      | true/false    | true    |
| labelField    | 列表项标签文本对应的字段                            | string       | —             | 'label' |
| childrenField | 列表项值对应的字段                                 | string       | -             | 'children' |
| onSelect      | 数据选择时的事件钩子                               | Function     | -             | function(value){} |
| filterData    | 选择完成以后，过滤数据的钩子                        | Function     | -             | function(value){} |

### SelectTree Methods
 
继承于 TrTree 组件向外提供的所有方法，可通过此组件直接调用 TrTree 组件上的方法。

### SelectTree Events

| 方法名 | 说明                    | 参数 |
|-------|-------------------------|-----|
| input | v-model绑定的值发生变化时 | Function(value: Array, Object) |
