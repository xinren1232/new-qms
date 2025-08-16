# RemoteSearch 远程字典选择框

### 组件说明

输入需要搜索的内容 以后会送服务器查询包含输入的内容的数据列表 随后展示

### 使用场景

组件多数用在条件筛选时，当需要获取的数据过多时，则不主动获取，而是再需要调用查询条件时才去主动获取查询的条件数据

### RemoteSelect Attribute
继承了 `element` 框架中的 `el-select` 的所有prop，除此之外，还提供了几个扩展属性，如下：

| 参数        | 说明                          | 类型                         | 默认值 |
| ---------- | ----------------------------- | --------------------------- | -----  |
| value      | 数据双向绑定的值                | Number/String/Array/Object   | -     |
| labelField | 以哪个字段值作为显示             | String                      | label |
| valueField | 以哪个值作为保存                | String                      | value  |
| valueType  | 返回值的对象类型：string/object | String                      | string |
| formatter  | 自定义渲染显示label的文本        | Function                   | null   |


### RemoteSelect Events
| 方法名  | 说明                                    | 参数                           |
| ------ | -------------------------------------- | ------------------------------ |
| input  | 作为组件切换状态时更改 v-model 的 value 值 | Function(value: String/Object) |
| select | 当选择某一个值以后触发 行为和`input`类似    | Function(value: String/Object) |

