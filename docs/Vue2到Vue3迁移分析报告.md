# Vue2到Vue3迁移分析报告

**分析日期**: 2025-08-02  
**项目**: QMS-AI配置端  
**当前状态**: 第1天 - 环境准备和依赖分析

---

## 📋 **项目结构深度分析**

### 🏗️ **当前Vue2配置端架构**

```
frontend/配置端/src/
├── 📁 api/                    # API接口层 (11个文件)
│   ├── ai-models.js          # AI模型管理API
│   ├── auth.js               # 认证相关API
│   ├── dict.js               # 数据字典API
│   ├── file-manage.js        # 文件管理API
│   └── ...                   # 其他API文件
│
├── 📁 app/                    # 应用初始化 (4个文件)
│   ├── init.js               # 应用初始化配置
│   ├── request.js            # HTTP请求封装
│   ├── login.js              # 登录逻辑
│   └── cookie.js             # Cookie管理
│
├── 📁 assets/                 # 静态资源
│   ├── css/                  # 样式文件
│   ├── fonts/                # 字体文件
│   └── images/               # 图片资源
│
├── 📁 components/             # 组件库
│   ├── bussiness/            # 业务组件
│   ├── feature/              # 功能组件
│   └── plm/                  # PLM专用组件
│
├── 📁 composables/            # 组合式函数 (8个文件)
│   ├── debounce-ref.js       # 防抖引用
│   ├── lazy-loading.js       # 懒加载
│   ├── model-value.js        # 模型值处理
│   └── ...                   # 其他组合函数
│
├── 📁 config/                 # 配置文件 (8个文件)
│   ├── local-config-center.js # 本地配置中心
│   ├── constraint.config.js   # 约束配置
│   ├── lang.config.js         # 语言配置
│   └── ...                    # 其他配置
│
├── 📁 directives/             # 自定义指令 (5个文件)
│   ├── click.js              # 点击指令
│   ├── drag-dialog.js        # 拖拽对话框
│   └── ...                   # 其他指令
│
├── 📁 hooks/                  # Vue2风格Hooks (5个文件)
│   ├── useExport.js          # 导出功能
│   ├── useObjectList.js      # 对象列表
│   └── ...                   # 其他Hooks
│
├── 📁 i18n/                   # 国际化
│   ├── index.js              # 国际化配置
│   └── lang/                 # 语言包
│
├── 📁 layout/                 # 布局组件
│   ├── default/              # 默认布局
│   ├── blank/                # 空白布局
│   ├── error/                # 错误页布局
│   └── frame/                # 框架布局
│
├── 📁 router/                 # 路由配置
│   ├── index.js              # 路由主文件
│   ├── guard.js              # 路由守卫
│   └── modules/              # 路由模块
│
├── 📁 store/                  # Vuex状态管理
│   ├── index.js              # Store主文件
│   ├── getters.js            # 全局Getters
│   └── modules/              # Store模块
│
├── 📁 utils/                  # 工具函数
│   ├── auth-bypass.js        # 认证绕过
│   ├── dict.js               # 字典工具
│   ├── export/               # 导出工具
│   ├── format/               # 格式化工具
│   └── ...                   # 其他工具
│
├── 📁 views/                  # 页面组件
│   ├── ai-management-config/ # AI管理配置
│   ├── system-manage/        # 系统管理
│   ├── file-manage/          # 文件管理
│   ├── dictionary-manage/    # 字典管理
│   └── ...                   # 其他页面
│
├── App.vue                    # 根组件
└── main.js                    # 应用入口
```

### 📦 **核心依赖包分析**

#### **生产依赖 (24个核心包)**
```json
{
  "vue": "^2.7.16",              // ✅ 目标: Vue 3.5.12
  "element-ui": "2.15.6",        // ⚠️ 目标: Element Plus 2.8.4
  "vuex": "^3.6.2",              // ⚠️ 目标: Vuex 4.x 或 Pinia 2.2.4
  "vue-router": "^3.6.4",        // ⚠️ 目标: Vue Router 4.4.5
  "vue-i18n": "^8.27.2",         // ⚠️ 目标: Vue I18n 10.x
  "vxe-table": "~3.6.1",         // ⚠️ 目标: VXE Table 4.x
  "monaco-editor": "^0.36.1",    // ⚠️ 目标: Monaco Editor 0.52.x
  "wujie-vue2": "^1.0.3",        // ⚠️ 目标: wujie-vue3
  "echarts": "^5.4.3",           // ✅ 兼容 Vue3
  "axios": "^1.10.0",            // ✅ 兼容 Vue3
  "@antv/x6": "^2.11.1",         // ✅ 兼容 Vue3
  "@wangeditor/editor": "^5.1.23" // ✅ 兼容 Vue3
}
```

#### **开发依赖 (25个核心包)**
```json
{
  "@vue/cli-service": "5.0.7",   // ❌ 替换为: Vite 5.x
  "webpack": "内置",              // ❌ 替换为: Vite
  "sass": "1.32.2",              // ✅ 兼容 Vue3
  "eslint": "^8.42.0",           // ✅ 兼容 Vue3
  "@unocss/webpack": "^0.56.0",  // ⚠️ 替换为: @unocss/vite
  "compression-webpack-plugin": "4.0.0" // ❌ 替换为: vite-plugin-compression
}
```

## 🎯 **Vue3版本映射表**

### **核心框架映射**
| Vue2包 | 当前版本 | Vue3目标版本 | 迁移难度 | 关键变化 |
|--------|----------|--------------|----------|----------|
| vue | 2.7.16 | 3.5.12 | 🟡 中等 | Composition API、响应式系统 |
| element-ui | 2.15.6 | element-plus@2.8.4 | 🔴 高 | 组件名、API、样式变化 |
| vuex | 3.6.2 | vuex@4.x | 🟡 中等 | 创建方式变化 |
| vue-router | 3.6.4 | vue-router@4.4.5 | 🟡 中等 | 历史模式、API变化 |
| vue-i18n | 8.27.2 | vue-i18n@10.x | 🟡 中等 | API调整 |

### **UI组件库映射**
| Vue2组件 | Vue3替代 | 兼容性 | 迁移策略 |
|----------|----------|--------|----------|
| vxe-table@3.6.1 | vxe-table@4.x | 🟡 需升级 | 渐进式迁移 |
| monaco-editor | @monaco-editor/react | 🔴 需重新集成 | 重新封装 |
| wujie-vue2 | wujie-vue3 | 🟡 包名变化 | 直接替换 |

### **构建工具映射**
| Vue2工具 | Vue3替代 | 优势 |
|----------|----------|------|
| Vue CLI + Webpack | Vite | 构建速度提升10倍+ |
| webpack插件生态 | Vite插件生态 | 更现代化的插件系统 |

## 📊 **完整依赖包兼容性分析**

### **生产依赖详细分析 (33个包)**

#### **Vue生态核心包**
| 包名 | 当前版本 | Vue3目标版本 | 兼容性 | 迁移策略 |
|------|----------|--------------|--------|----------|
| vue | 2.7.16 | 3.5.12 | 🟡 需升级 | 核心框架升级 |
| vuex | 3.6.2 | 4.1.0 | 🟡 需升级 | 保持API兼容 |
| vue-router | 3.6.4 | 4.4.5 | 🟡 需升级 | 路由配置调整 |
| vue-i18n | 8.27.2 | 10.0.4 | 🟡 需升级 | 国际化API调整 |

#### **UI组件库**
| 包名 | 当前版本 | Vue3目标版本 | 兼容性 | 迁移策略 |
|------|----------|--------------|--------|----------|
| element-ui | 2.15.6 | element-plus@2.8.4 | 🔴 重大变更 | 组件名和API全面调整 |
| vxe-table | 3.6.1 | 4.6.0 | 🟡 需升级 | 表格组件API调整 |
| vxe-table-plugin-element | 1.11.4 | 4.x | 🔴 重大变更 | 需要重新配置 |
| el-table-infinite-scroll | 1.0.11 | 替代方案 | 🔴 不兼容 | 寻找Vue3替代 |

#### **编辑器和可视化**
| 包名 | 当前版本 | Vue3目标版本 | 兼容性 | 迁移策略 |
|------|----------|--------------|--------|----------|
| monaco-editor | 0.36.1 | 0.52.0 | 🟡 需调整 | 重新封装集成 |
| vue-monaco-editor | 0.0.19 | @monaco-editor/vue3 | 🔴 包名变更 | 替换为Vue3版本 |
| @wangeditor/editor | 5.1.23 | 5.1.23 | ✅ 兼容 | 直接使用 |
| @wangeditor/editor-for-vue | 1.0.2 | @wangeditor/editor-for-vue3 | 🟡 包名变更 | 替换包名 |
| echarts | 5.4.3 | 5.5.0 | ✅ 兼容 | 直接使用 |

#### **图形和交互库**
| 包名 | 当前版本 | Vue3目标版本 | 兼容性 | 迁移策略 |
|------|----------|--------------|--------|----------|
| @antv/x6 | 2.11.1 | 2.18.1 | ✅ 兼容 | 版本升级 |
| @antv/x6-plugin-* | 2.x | 2.x | ✅ 兼容 | 配套升级 |
| vue-grid-layout | 2.4.0 | vue3-grid-layout | 🔴 包名变更 | 替换为Vue3版本 |
| vuedraggable | 2.24.3 | vuedraggable@4.x | 🟡 需升级 | Vue3版本升级 |

#### **微前端和工具库**
| 包名 | 当前版本 | Vue3目标版本 | 兼容性 | 迁移策略 |
|------|----------|--------------|--------|----------|
| wujie-vue2 | 1.0.3 | wujie-vue3@1.0.3 | 🟡 包名变更 | 直接替换包名 |
| @vueuse/core | 8.9.4 | 11.2.0 | ✅ 兼容 | 版本升级 |

#### **通用工具库 (Vue无关)**
| 包名 | 当前版本 | 兼容性 | 说明 |
|------|----------|--------|------|
| axios | 1.10.0 | ✅ 完全兼容 | HTTP客户端 |
| dayjs | 1.11.5 | ✅ 完全兼容 | 日期处理 |
| file-saver | 2.0.5 | ✅ 完全兼容 | 文件下载 |
| js-cookie | 2.2.1 | ✅ 完全兼容 | Cookie管理 |
| nanoid | 4.0.0 | ✅ 完全兼容 | ID生成 |
| nprogress | 0.2.0 | ✅ 完全兼容 | 进度条 |
| screenfull | 5.2.0 | ✅ 完全兼容 | 全屏API |
| xlsx | 0.17.4 | ✅ 完全兼容 | Excel处理 |
| xe-utils | 3.5.6 | ✅ 完全兼容 | 工具函数 |

### **开发依赖分析**

#### **构建工具替换**
| Vue2工具 | Vue3替代 | 变化说明 |
|----------|----------|----------|
| @vue/cli-service | Vite | 构建工具完全替换 |
| webpack相关插件 | Vite插件 | 插件生态替换 |
| monaco-editor-webpack-plugin | @monaco-editor/vite-plugin | Vite插件替换 |

## 🔧 **迁移工具和脚本准备**

### **自动化迁移工具安装**
```bash
# Vue官方迁移助手
npm install -g @vue/compat

# Element Plus迁移工具
npm install -g element-plus-migration

# 代码转换工具
npm install -g vue-codemod
npm install -g jscodeshift

# Vite项目创建工具
npm install -g create-vue@latest
```

### **批量替换脚本**
```javascript
// 1. 组件名称映射 (Element UI → Element Plus)
const componentMapping = {
  // 表格组件
  'el-table': 'ElTable',
  'el-table-column': 'ElTableColumn',

  // 表单组件
  'el-form': 'ElForm',
  'el-form-item': 'ElFormItem',
  'el-input': 'ElInput',
  'el-select': 'ElSelect',
  'el-option': 'ElOption',
  'el-button': 'ElButton',

  // 布局组件
  'el-row': 'ElRow',
  'el-col': 'ElCol',
  'el-container': 'ElContainer',
  'el-header': 'ElHeader',
  'el-aside': 'ElAside',
  'el-main': 'ElMain',

  // 导航组件
  'el-menu': 'ElMenu',
  'el-menu-item': 'ElMenuItem',
  'el-submenu': 'ElSubmenu',
  'el-breadcrumb': 'ElBreadcrumb',
  'el-breadcrumb-item': 'ElBreadcrumbItem',

  // 反馈组件
  'el-dialog': 'ElDialog',
  'el-drawer': 'ElDrawer',
  'el-popover': 'ElPopover',
  'el-tooltip': 'ElTooltip',

  // 数据展示
  'el-tag': 'ElTag',
  'el-badge': 'ElBadge',
  'el-card': 'ElCard',
  'el-collapse': 'ElCollapse',
  'el-collapse-item': 'ElCollapseItem'
}

// 2. API调用映射
const apiMapping = {
  // 消息提示
  'this.$message': 'ElMessage',
  'this.$notify': 'ElNotification',
  'this.$confirm': 'ElMessageBox.confirm',
  'this.$alert': 'ElMessageBox.alert',
  'this.$prompt': 'ElMessageBox.prompt',

  // 加载状态
  'this.$loading': 'ElLoading.service',

  // 路由
  'this.$router': 'useRouter()',
  'this.$route': 'useRoute()'
}

// 3. 事件名称映射
const eventMapping = {
  // 表单事件
  '@input': '@update:modelValue',
  '@change': '@change',

  // 表格事件
  '@selection-change': '@selection-change',
  '@sort-change': '@sort-change',
  '@filter-change': '@filter-change'
}
```

## ⚠️ **风险识别和缓解策略**

### **高风险项目**
1. **VXE Table升级** 🔴
   - 风险: 表格是配置端核心组件，API变化较大
   - 缓解: 创建兼容层，逐步迁移

2. **Element UI → Element Plus** 🔴
   - 风险: 组件API和样式变化
   - 缓解: 使用自动化工具+手动调整

3. **Monaco Editor重新集成** 🔴
   - 风险: 代码编辑器功能复杂
   - 缓解: 重新封装，保持API一致

### **中风险项目**
1. **Vuex状态管理** 🟡
   - 风险: Store结构需要调整
   - 缓解: 保持Vuex4，后续考虑Pinia

2. **路由配置** 🟡
   - 风险: 路由守卫和配置语法变化
   - 缓解: 逐步迁移，保持功能一致

### **低风险项目**
1. **工具函数** 🟢
   - 风险: 大部分可直接复用
   - 策略: 直接迁移

2. **样式文件** 🟢
   - 风险: CSS/SCSS可直接使用
   - 策略: 直接复制

## 📅 **下一步计划**

### **第2天计划：Vue3项目初始化**
1. 创建Vue3版本配置端项目
2. 配置Vite构建工具
3. 安装核心依赖包
4. 配置开发环境

### **第3天计划：基础架构迁移**
1. 迁移路由配置
2. 迁移状态管理
3. 迁移API请求封装
4. 迁移工具函数

---

## ✅ **第1天完成情况**

- ✅ 项目结构深度分析完成
- ✅ 依赖包清单和版本映射完成
- ✅ 风险识别和缓解策略制定
- ✅ 迁移工具准备完成
- ✅ 下一步计划制定完成

**准备就绪，可以开始第2天的Vue3项目初始化工作！**
