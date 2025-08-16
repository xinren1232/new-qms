# TrSwitch 状态开关(三合一)

### 组件说明

集未启用、启用和禁用为一体，符合IPM环境的switch开关

### 使用场景

项目中有需要应用状态切换时

### Select Attributes

| 参数       | 说明                   | 类型             | 可选值 | 默认值    |
| ---------- | ---------------------- | ---------------- | ------ | --------- |
| value      | 展示状态的值           | string/Number    | —      | ''        |
| enable     | 启用状态下的值         | string/Number    | —      | 'enable'  |
| disable    | 禁用状态下的值         | string/Number    | —      | 'disable' |
| off        | 未启用状态下的值       | string/Number    | —      | 'off'     |
| httpRequest | 自定义改变该状态的函数 | Promise/Function | —      | null      |
| params     | 自定义改变该状态参数   | Object           | —      | {}        |

### Select Methods

无

### Select Events

无
