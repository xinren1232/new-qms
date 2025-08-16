# FilesUploader 多文件上传

### 组件说明

文件上传组件

### 使用场景

需要上传多个文件时

### Attributes

| 参数        | 说明                   | 类型                         | 可选值     | 默认值 |
| ----------- | ---------------------- | ---------------------------- | ---------- | ------ |
| value       | 用于自定义 v-model     | Array                        | —          | []     |
| httpRequest | 用于上传文件的 request | Function(file: File):Promise | —          | null   |
| disabled    | 是否禁用               | boolean                      | true/false | false  |

### Methods

无

### Events
| 方法名 | 说明                 | 参数                    |
| ----- | -------------------- | ---------------------- |
| input | 在选择/删除文件以后触发 | Function(value: array) |
