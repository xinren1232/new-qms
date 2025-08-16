# TrForm 扩展表单

### 封装的意义

1. 减少模板代码。只算 el-form-item + el-col 的话，每个表单项就会多 4 行代码；10 个表单项就会多 40 行代码了。对于“每个文件不超过 200 行”这个目标来说，这些模板代码不可饶恕。
2. 固化表单项。一个小项目一般有 10 多个页面，一个页面只算 6 个表单项，总共就有 60 个表单项了。但是表单项类型可能就几种，在 tbm 项目中主要就 3 种：文本框、字典下拉选择框、日期选择框。如果在 TrForm 的 components 收集各种表单项组件，通过类型进行映射，就不用到处复制粘贴了，而且使用都可以项 el-input 一样简单。
3. 封装一些通用的功能。比如：展开/收起、重置。
4. 达到“通过配置就能生产表单”的目的，为后续无代码编程做准备。

### 使用场景

后台管理类页面，凡是用到表单的地方，且表单之间没有复杂的交互。

### 限制

1. 存放在 TrForm/components 里的表单项，需要自定义 v-model

### Table Slots

| name   | 说明                            |
| ------ | ------------------------------ |
|  *     | 以`items.prop`为 name 的插槽     |
| before | 在所有表单项前面插入内容           |
| after  | 在所有表单项后面 按钮组前面插入内容  |

### Form Attributes

除了继承 el-form 的属性，还支持以下属性：

| 参数            | 说明                                       | 类型    | 可选值     | 默认值 |
| --------------- | ------------------------------------- | ------- | ---------- | ------ |
| form            | 表单数据对象，v-model 对应的是 data 属性   | object  | —          | {}     |
| items           | 表单项配置，具体配置见`FormItem Attributes` | array   | —          | []     |
| showButtons     | 是否显示操作按钮                           | boolean | true/false | true   |
| collapsible    | 是否可折叠                                 | boolean | true/false | true   |
| defaultExpand   | 默认是否展开                               | boolean | true/false | true   |
| fixedItemsCount | 固定展示(收起面板以后)展示的表单项个数     | number  | —          | 3      |

### Form Methods

| 方法名        | 说明                         | 参数 |
| ------------- | --------------------------- | ---- |
| validate      | 同 el-form 的 validate      | —    |
| validateField | 同 el-form 的 validateField | —    |
| resetFields   | 同 el-form 的 resetFields   | —    |
| clearValidate | 同 el-form 的 clearValidate | —    |

### Form Events

继承了 el-form 的 Events

| 事件名  |说明                           | 参数                                  |
|------- |------------------------------ |------------------------------------- |
| input  | 作为组件点击时切换状态值的事件钩子 | Function(formData: object)           |
| query  | 组件点击查询按钮时触发            | Function(formData: object)          |
| reset  | 组件点击重置按钮时触发            | Function(formData: object)          |
| toggle | 组件展开或者收起表单时触发        | Function(status: string(open/close)) |

### FormItem Attributes

| 参数       | 说明                                                                                   | 类型         | 可选值                     | 默认值 |
| ---------- | -------------------------------------------------------------------------------------- | ------------ | -------------------------- | ------ |
| prop       | 同 el-form-itemn 的 prop                                                               | string       | -                          | -      |
| label      | 同 el-form-item 的 label                                                               | string       | -                          | -      |
| span       | 同 el-form-item 的 span                                                                | number       | —                          | 6      |
| pull       | 同 el-form-item 的 pull                                                                | number       | —                          | 0      |
| push       | 同 el-form-item 的 push                                                                | number       | —                          | 0      |
| offset     | 同 el-form-item 的 offset                                                              | number       | —                          | 0      |
| margin     | 同 el-form-item 的 margin                                                              | number       | —                          | 0      |
| labelWidth | 同 el-form-item 的 labelWidth                                                          | string       | —                          | 0      |
| type       | 编辑状态下显示的表单组件类型，与组件的映射位于 TrEditTableColumn/factory.js            | -            | input/number/switch/select | -      |
| component  | 动态组件：编辑状态下显示的表单组件, 需要实现自定义 v-model                             | VueComponent | —                          | null   |
| listeners  | 列监听的事件， 同 Vue 对象的\$listeners                                                | object       | —                          | {}     |
| attrs      | 对显示的组件需要设置的属性, 比如 placeholder type disabled 属性, 支持 element 表单属性 | object       | —                          | {}     |
