# TrSelect 选择框

### 组件说明

集成了 option 组件功能的选择框, 使用时只需要传入 v-model 和 data 的值即可。多选增加了“全选”功能。

### 使用场景

近乎任何选择框都适用

### Select Attributes

除了继承 el-select 的属性，还支持以下属性：

| 参数       | 说明                                                                                  | 类型         | 可选值        | 默认值  |
| ---------- | ------------------------------------------------------------------------------------- | ------------ | ------------- | ------- |
| value      | 同 el-select 的 value                                                                 | string/array | —             | ''/[]   |
| data       | 下拉列表数据源                                                                        | array        | —             | []      |
| multiple   | 同 el-select 的 multiple，主要为了设置默认值                                          | boolean      | true/false    | false   |
| clearable  | 同 el-select 的 clearable，主要为了设置默认值                                         | boolean      | true/false    | true    |
| labelField | 列表项标签文本对应的字段                                                              | string       | —             | 'label' |
| valueField | 列表项值对应的字段                                                                    | string       | —             | 'value' |
| valueType  | 列表项值的类型，主要用于的场景：当设置 select 的 v-model="item"时，item 为一个 object | string       | string/object | ''      |

### Select Methods

无

### Select Events

除了继承了 el-select 的 Events
