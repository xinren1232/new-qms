### 全局导入模块
#### 说明
全局导入模块，通过业务类型TYPE区分，统一API接口
#### 参数props
| 参数           | 说明                        | 类型 | 可选值 | 默认值   |
|--------------|---------------------------| --- | --- |-------|
| title        | 标题                        | String | — | —     |
| visible      | 是否显示                      | Boolean | — | false |
| width        | 宽度                        | String | — | 900px |
| height       | 高度                        | String | — | 500px |
| type         | 导入的业务类型                   | String | — | null  |
| api          | 自定义导入的API地址，优先级最高，会覆盖type | String | — | null  |
| apiParams    | 自定义导入的API参数，优先级最高，会覆盖type | Object | — | null  |
| showValidate | 是否显示校验结果                  | Boolean | — | true  |




#### 事件
| 事件名称 | 说明 | 回调参数  |
|------|----|-------|
| on-close | 关闭时触发 | —     |
| on-success | 导入成功时触发 | LIST  |
