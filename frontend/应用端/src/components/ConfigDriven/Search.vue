<template>
  <el-card class="search-card" shadow="never">
    <el-form
      ref="searchFormRef"
      :model="formData"
      class="search-form"
      label-width="auto"
    >
      <el-row :gutter="20">
        <el-col
          v-for="field in config"
          :key="field.key"
          :span="field.span || 6"
        >
          <el-form-item :label="field.label">
            <!-- 输入框 -->
            <el-input
              v-if="field.type === 'input'"
              v-model="formData[field.key]"
              :placeholder="field.placeholder"
              :prefix-icon="field.icon"
              clearable
            />
            
            <!-- 选择框 -->
            <el-select
              v-else-if="field.type === 'select'"
              v-model="formData[field.key]"
              :placeholder="field.placeholder"
              clearable
              style="width: 100%"
            >
              <el-option
                v-for="option in field.options"
                :key="option.value"
                :label="option.label"
                :value="option.value"
              />
            </el-select>
            
            <!-- 日期范围选择器 -->
            <el-date-picker
              v-else-if="field.type === 'daterange'"
              v-model="formData[field.key]"
              type="daterange"
              range-separator="至"
              :start-placeholder="field.startPlaceholder || '开始日期'"
              :end-placeholder="field.endPlaceholder || '结束日期'"
              format="YYYY-MM-DD"
              value-format="YYYY-MM-DD"
              style="width: 100%"
            />
            
            <!-- 数字输入框 -->
            <el-input-number
              v-else-if="field.type === 'number'"
              v-model="formData[field.key]"
              :placeholder="field.placeholder"
              :min="field.min"
              :max="field.max"
              style="width: 100%"
            />
            
            <!-- 开关 -->
            <el-switch
              v-else-if="field.type === 'switch'"
              v-model="formData[field.key]"
              :active-text="field.activeText"
              :inactive-text="field.inactiveText"
            />
          </el-form-item>
        </el-col>
        
        <!-- 操作按钮 -->
        <el-col :span="6">
          <el-form-item>
            <div class="search-actions">
              <el-button
                type="primary"
                :loading="loading"
                @click="handleSearch"
                icon="Search"
              >
                搜索
              </el-button>
              <el-button
                @click="handleReset"
                icon="Refresh"
              >
                重置
              </el-button>
              <el-button
                v-if="showAdvanced"
                type="text"
                @click="toggleAdvanced"
              >
                {{ advancedVisible ? '收起' : '高级搜索' }}
                <el-icon>
                  <ArrowUp v-if="advancedVisible" />
                  <ArrowDown v-else />
                </el-icon>
              </el-button>
            </div>
          </el-form-item>
        </el-col>
      </el-row>
      
      <!-- 高级搜索区域 -->
      <div v-if="advancedVisible && advancedConfig.length > 0" class="advanced-search">
        <el-divider content-position="left">高级搜索</el-divider>
        <el-row :gutter="20">
          <el-col
            v-for="field in advancedConfig"
            :key="field.key"
            :span="field.span || 8"
          >
            <el-form-item :label="field.label">
              <!-- 这里可以放置更复杂的搜索组件 -->
              <component
                :is="getFieldComponent(field.type)"
                v-model="formData[field.key]"
                v-bind="getFieldProps(field)"
              />
            </el-form-item>
          </el-col>
        </el-row>
      </div>
    </el-form>
  </el-card>
</template>

<script setup>
import { ref, reactive, watch } from 'vue'

const props = defineProps({
  config: {
    type: Array,
    default: () => []
  },
  advancedConfig: {
    type: Array,
    default: () => []
  },
  loading: {
    type: Boolean,
    default: false
  },
  showAdvanced: {
    type: Boolean,
    default: false
  }
})

const emit = defineEmits(['search', 'reset'])

// 表单引用
const searchFormRef = ref()

// 表单数据
const formData = reactive({})

// 高级搜索显示状态
const advancedVisible = ref(false)

// 初始化表单数据
const initFormData = () => {
  const allFields = [...props.config, ...props.advancedConfig]
  allFields.forEach(field => {
    if (!(field.key in formData)) {
      formData[field.key] = field.defaultValue || getDefaultValue(field.type)
    }
  })
}

// 获取默认值
const getDefaultValue = (type) => {
  switch (type) {
    case 'input':
    case 'select':
      return ''
    case 'number':
      return null
    case 'switch':
      return false
    case 'daterange':
      return []
    default:
      return ''
  }
}

// 获取字段组件
const getFieldComponent = (type) => {
  const componentMap = {
    'input': 'el-input',
    'select': 'el-select',
    'daterange': 'el-date-picker',
    'number': 'el-input-number',
    'switch': 'el-switch'
  }
  return componentMap[type] || 'el-input'
}

// 获取字段属性
const getFieldProps = (field) => {
  const baseProps = {
    placeholder: field.placeholder,
    clearable: field.clearable !== false
  }
  
  switch (field.type) {
    case 'select':
      return {
        ...baseProps,
        options: field.options || []
      }
    case 'daterange':
      return {
        ...baseProps,
        type: 'daterange',
        'range-separator': '至',
        'start-placeholder': field.startPlaceholder || '开始日期',
        'end-placeholder': field.endPlaceholder || '结束日期',
        format: 'YYYY-MM-DD',
        'value-format': 'YYYY-MM-DD'
      }
    case 'number':
      return {
        ...baseProps,
        min: field.min,
        max: field.max,
        step: field.step
      }
    case 'switch':
      return {
        'active-text': field.activeText,
        'inactive-text': field.inactiveText
      }
    default:
      return baseProps
  }
}

// 搜索处理
const handleSearch = () => {
  emit('search', { ...formData })
}

// 重置处理
const handleReset = () => {
  searchFormRef.value?.resetFields()
  
  // 重置为默认值
  Object.keys(formData).forEach(key => {
    const field = [...props.config, ...props.advancedConfig].find(f => f.key === key)
    formData[key] = field?.defaultValue || getDefaultValue(field?.type)
  })
  
  emit('reset')
}

// 切换高级搜索
const toggleAdvanced = () => {
  advancedVisible.value = !advancedVisible.value
}

// 监听配置变化，重新初始化表单数据
watch(
  () => [props.config, props.advancedConfig],
  () => {
    initFormData()
  },
  { immediate: true, deep: true }
)
</script>

<style lang="scss" scoped>
.search-card {
  margin-bottom: 16px;
  
  :deep(.el-card__body) {
    padding: 16px;
  }
}

.search-form {
  .el-form-item {
    margin-bottom: 16px;
  }
}

.search-actions {
  display: flex;
  gap: 8px;
  align-items: center;
}

.advanced-search {
  margin-top: 16px;
  
  .el-divider {
    margin: 16px 0;
  }
}
</style>
