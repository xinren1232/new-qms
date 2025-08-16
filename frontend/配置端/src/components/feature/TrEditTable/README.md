# TrEditTable 可编辑表格

### 组件说明

可编辑的表格，用以增加/删除/修改一个数组数据，单元格里面不仅可以显示文本，还可以显示自定义的组件。

### 使用场景

页面需要展示表格，且需要编辑它

### Table Attributes

除了继承 el-table 的属性，还支持以下属性：

| 参数     | 说明                                           | 类型    | 可选值     | 默认值 |
| -------- | ---------------------------------------------- | ------- | ---------- | ------ |
| columns  | 列配置数组，每条数据对应一个 TrEditTableColumn | array   | —          | []     |
| data     | 列表数据源，v-model 对应的是 data 属性         | array   | —          | []     |
| disabled | 是否禁用                                       | boolean | true/false | false  |

### Table Methods

| 方法名    | 说明                                         | 参数                  |
| --------- | -------------------------------------------- | --------------------- |
| addRow    | 新增一行                                     | Function(rows: array  | object) |
| deleteRow | 删除某一行                                   | Function(row: object) |
| validate  | 校验表格中的表单，若校验不通过则返回错误信息 | Function():string     |

### Table Events

完全继承了 el-table 的 Events

### Column Attributes

| 参数      | 说明                                                                                   | 类型         | 可选值                     | 默认值 |
| --------- | -------------------------------------------------------------------------------------- | ------------ | -------------------------- | ------ |
| prop      | 同 el-table-column 的 prop                                                             | string       | -                          | -      |
| label     | 同 el-table-column 的 label                                                            | string       | -                          | -      |
| editable  | 是否可编辑，若是则显示表单组件；否则显示文本                                           | boolean      | true/false                 | false  |
| validate  | 同 el-form 的 rules，只对应当前列                                                      | array        | —                          | []     |
| type      | 编辑状态下显示的表单组件类型，与组件的映射位于 TrEditTableColumn/factory.js            | -            | input/number/switch/select | -      |
| component | 动态组件：编辑状态下显示的表单组件, 需要实现自定义 v-model                             | VueComponent | —                          | null   |
| listeners | 列监听的事件， 同 Vue 对象的\$listeners                                                | object       | —                          | {}     |
| attrs     | 对显示的组件需要设置的属性, 比如 placeholder type disabled 属性, 支持 element 表单属性 | object       | —                          | {}     |
