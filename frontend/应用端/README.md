# QMS配置驱动智能管理系统

## 🎯 项目概述

这是一个基于Vue3的配置驱动前端系统，实现了完全由配置端控制的动态页面渲染。系统采用现代化的前端技术栈，提供了灵活、可扩展的配置驱动架构。

## 🏗️ 技术架构

### 核心技术栈
- **Vue 3** - 渐进式JavaScript框架
- **Vite** - 现代化构建工具
- **Element Plus** - 企业级UI组件库
- **Pinia** - 状态管理
- **Vue Router** - 路由管理
- **Axios** - HTTP客户端

### 架构设计
```
配置端(8081) → 配置同步API → 应用端Mock(8083) → 配置驱动前端(8090)
```

## 🚀 快速开始

### 安装依赖
```bash
npm install
```

### 启动开发服务器
```bash
npm run serve
```

### 构建生产版本
```bash
npm run build
```

## 📋 功能特性

### 🎨 配置驱动渲染
- **动态表格** - 根据配置自动生成表格列
- **可配置搜索** - 动态搜索表单组件
- **权限控制** - 基于配置的权限验证
- **响应式布局** - 自适应不同设备

### 🔧 核心组件
- `ConfigDrivenSearch` - 配置驱动搜索组件
- `ConfigDrivenToolbar` - 配置驱动工具栏
- `ConfigDrivenTable` - 配置驱动表格
- `ConfigDrivenCell` - 配置驱动单元格

### 📊 业务模块
- **AI对话记录管理** - 完整的配置驱动示例
- **数据源配置** - 数据源管理
- **AI规则配置** - 规则引擎配置
- **用户管理** - 用户权限管理
- **系统配置** - 配置管理中心

## 🔄 数据流设计

### 配置同步流程
1. **配置端** 定义页面结构、字段、权限
2. **配置同步API** 将配置推送到应用端
3. **配置驱动前端** 根据配置动态渲染页面
4. **实时更新** 配置变更立即反映到界面

### 状态管理
```javascript
// 配置状态
const configStore = useConfigStore()

// 获取模块配置
const moduleConfig = configStore.getModuleConfig('aiConversation')

// 权限验证
const hasPermission = configStore.hasPermission('aiConversation', 'view')
```

## 📁 项目结构

```
src/
├── api/                    # API接口
│   └── config.js          # 配置相关API
├── components/             # 通用组件
│   └── ConfigDriven/      # 配置驱动组件
├── layout/                # 布局组件
├── router/                # 路由配置
├── stores/                # 状态管理
│   └── config.js          # 配置状态
├── styles/                # 样式文件
├── utils/                 # 工具函数
├── views/                 # 页面组件
│   ├── ai-management/     # AI管理模块
│   ├── dashboard/         # 工作台
│   ├── login/            # 登录页面
│   └── system/           # 系统管理
└── App.vue               # 主应用组件
```

## 🎯 配置驱动原理

### 配置数据结构
```javascript
{
  "aiConversation": {
    "fields": [
      {
        "prop": "conversationId",
        "label": "对话ID",
        "type": "text",
        "width": 150,
        "visible": true,
        "sortable": true
      }
    ],
    "permissions": {
      "view": ["admin", "user"],
      "export": ["admin"]
    },
    "functions": {
      "showSelection": true,
      "pageSize": 20
    }
  }
}
```

### 动态渲染
```vue
<template>
  <el-table-column
    v-for="column in visibleColumns"
    :key="column.prop"
    :prop="column.prop"
    :label="column.label"
    :width="column.width"
    :sortable="column.sortable"
  />
</template>

<script setup>
const visibleColumns = computed(() => {
  return fieldConfig.value.filter(column => 
    column.visible && hasPermission(column.permission)
  )
})
</script>
```

## 🔧 开发指南

### 添加新模块
1. 在配置端定义模块配置
2. 创建对应的Vue页面组件
3. 使用配置驱动组件渲染
4. 添加路由配置

### 自定义组件
```vue
<template>
  <ConfigDrivenTable
    :columns="visibleColumns"
    :data="tableData"
    :loading="loading"
    @row-action="handleRowAction"
  />
</template>
```

### 权限控制
```javascript
// 检查权限
const hasPermission = (permission) => {
  return configStore.hasPermission(moduleKey, permission)
}

// 过滤操作按钮
const allowedActions = computed(() => {
  return actions.filter(action => hasPermission(action.permission))
})
```

## 🌐 API接口

### 配置同步
- `POST /config-sync/ai-management` - 获取AI管理配置
- `POST /config-push/ai-management` - 推送配置到应用端

### 业务数据
- `POST /object-model/{module}/page` - 获取模块数据

## 🎨 主题定制

### 颜色变量
```scss
$primary-color: #409EFF;
$success-color: #67C23A;
$warning-color: #E6A23C;
$danger-color: #F56C6C;
```

### 响应式断点
```scss
@media (max-width: 768px) {
  // 移动端样式
}
```

## 🚀 部署说明

### 环境变量
```bash
VITE_API_BASE_URL=http://localhost:8083
VITE_CONFIG_API_URL=http://localhost:8081
```

### 构建命令
```bash
npm run build
```

### 预览构建结果
```bash
npm run preview
```

## 📞 联系方式

如有问题或建议，请联系开发团队。

---

**QMS配置驱动智能管理系统** - 让配置驱动一切！🎉
