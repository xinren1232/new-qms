<template>
  <div class="conversation-config">
    <div class="page-header">
      <h2>对话配置</h2>
      <p class="text-muted">配置AI对话相关参数，包括对话模板、上下文管理等</p>
    </div>

    <!-- 配置表单 -->
    <div class="config-container">
      <el-card class="config-card">
        <template #header>
          <div class="card-header">
            <span>对话基础配置</span>
            <el-button type="primary" @click="handleSave">保存配置</el-button>
          </div>
        </template>

        <el-form :model="configForm" label-width="150px" class="config-form">
          <el-row :gutter="20">
            <el-col :span="12">
              <el-form-item label="最大对话轮数">
                <el-input-number
                  v-model="configForm.maxTurns"
                  :min="1"
                  :max="100"
                  placeholder="请输入最大对话轮数"
                />
              </el-form-item>
            </el-col>
            <el-col :span="12">
              <el-form-item label="上下文长度">
                <el-input-number
                  v-model="configForm.contextLength"
                  :min="100"
                  :max="10000"
                  placeholder="请输入上下文长度"
                />
              </el-form-item>
            </el-col>
          </el-row>

          <el-row :gutter="20">
            <el-col :span="12">
              <el-form-item label="温度参数">
                <el-slider
                  v-model="configForm.temperature"
                  :min="0"
                  :max="2"
                  :step="0.1"
                  show-input
                />
              </el-form-item>
            </el-col>
            <el-col :span="12">
              <el-form-item label="Top-P参数">
                <el-slider
                  v-model="configForm.topP"
                  :min="0"
                  :max="1"
                  :step="0.1"
                  show-input
                />
              </el-form-item>
            </el-col>
          </el-row>

          <el-form-item label="系统提示词">
            <el-input
              v-model="configForm.systemPrompt"
              type="textarea"
              :rows="4"
              placeholder="请输入系统提示词"
            />
          </el-form-item>

          <el-form-item label="对话模板">
            <el-select v-model="configForm.conversationTemplate" placeholder="请选择对话模板" style="width: 100%">
              <el-option label="通用对话模板" value="general" />
              <el-option label="技术支持模板" value="technical" />
              <el-option label="客服模板" value="customer_service" />
              <el-option label="教育模板" value="education" />
            </el-select>
          </el-form-item>

          <el-form-item label="启用上下文记忆">
            <el-switch v-model="configForm.enableContextMemory" />
          </el-form-item>

          <el-form-item label="启用敏感词过滤">
            <el-switch v-model="configForm.enableSensitiveFilter" />
          </el-form-item>
        </el-form>
      </el-card>

      <!-- 对话历史管理 -->
      <el-card class="config-card">
        <template #header>
          <span>对话历史管理</span>
        </template>

        <el-form :model="historyForm" label-width="150px" class="config-form">
          <el-row :gutter="20">
            <el-col :span="12">
              <el-form-item label="历史保存天数">
                <el-input-number
                  v-model="historyForm.retentionDays"
                  :min="1"
                  :max="365"
                  placeholder="请输入保存天数"
                />
              </el-form-item>
            </el-col>
            <el-col :span="12">
              <el-form-item label="最大历史条数">
                <el-input-number
                  v-model="historyForm.maxHistoryCount"
                  :min="10"
                  :max="10000"
                  placeholder="请输入最大条数"
                />
              </el-form-item>
            </el-col>
          </el-row>

          <el-form-item label="自动清理历史">
            <el-switch v-model="historyForm.autoCleanup" />
          </el-form-item>

          <el-form-item label="导出历史格式">
            <el-checkbox-group v-model="historyForm.exportFormats">
              <el-checkbox label="JSON">JSON格式</el-checkbox>
              <el-checkbox label="CSV">CSV格式</el-checkbox>
              <el-checkbox label="TXT">文本格式</el-checkbox>
            </el-checkbox-group>
          </el-form-item>
        </el-form>
      </el-card>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { ElMessage } from 'element-plus'

// 响应式数据
const configForm = reactive({
  maxTurns: 10,
  contextLength: 2000,
  temperature: 0.7,
  topP: 0.9,
  systemPrompt: '你是一个专业的AI助手，请根据用户的问题提供准确、有用的回答。',
  conversationTemplate: 'general',
  enableContextMemory: true,
  enableSensitiveFilter: true
})

const historyForm = reactive({
  retentionDays: 30,
  maxHistoryCount: 1000,
  autoCleanup: true,
  exportFormats: ['JSON']
})

// 方法
const handleSave = () => {
  ElMessage.success('对话配置保存成功')
}

// 生命周期
onMounted(() => {
  // 初始化配置
})
</script>

<style scoped>
.conversation-config {
  padding: 20px;
}

.page-header {
  margin-bottom: 20px;
}

.page-header h2 {
  margin: 0 0 8px 0;
  color: #303133;
}

.text-muted {
  color: #909399;
  margin: 0;
}

.config-container {
  display: flex;
  flex-direction: column;
  gap: 20px;
}

.config-card {
  width: 100%;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.config-form {
  padding: 20px 0;
}
</style>
