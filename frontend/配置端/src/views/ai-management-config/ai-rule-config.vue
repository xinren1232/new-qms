<template>
  <div class="ai-rule-config">
    <div class="page-header">
      <h2>AI规则配置管理</h2>
      <p>配置AI质量检测规则、预测模型参数和业务逻辑规则</p>
    </div>

    <el-card class="config-card">
      <div slot="header" class="card-header">
        <span>AI规则配置</span>
        <el-button type="primary" @click="saveConfig">保存配置</el-button>
        <el-button type="success" @click="pushConfig">推送到配置驱动端</el-button>
      </div>

      <el-tabs v-model="activeTab" type="card">
        <!-- 质量检测规则 -->
        <el-tab-pane label="质量检测规则" name="quality">
          <el-form :model="config.qualityRules" label-width="150px">
            <el-form-item label="启用自动检测">
              <el-switch v-model="config.qualityRules.autoInspection" />
            </el-form-item>
            <el-form-item label="检测间隔(秒)">
              <el-input-number v-model="config.qualityRules.inspectionInterval" :min="60" :max="86400" />
            </el-form-item>
            <el-form-item label="质量阈值">
              <el-slider v-model="config.qualityRules.qualityThreshold" :min="0" :max="1" :step="0.01" show-input />
            </el-form-item>
            <el-form-item label="告警阈值">
              <el-slider v-model="config.qualityRules.alertThreshold" :min="0" :max="1" :step="0.01" show-input />
            </el-form-item>
            <el-form-item label="检测项目">
              <el-checkbox-group v-model="config.qualityRules.inspectionItems">
                <el-checkbox label="dimensional">尺寸检测</el-checkbox>
                <el-checkbox label="surface">表面质量</el-checkbox>
                <el-checkbox label="material">材料成分</el-checkbox>
                <el-checkbox label="performance">性能测试</el-checkbox>
              </el-checkbox-group>
            </el-form-item>
          </el-form>
        </el-tab-pane>

        <!-- AI预测模型 -->
        <el-tab-pane label="AI预测模型" name="prediction">
          <el-form :model="config.aiModel" label-width="150px">
            <el-form-item label="模型版本">
              <el-select v-model="config.aiModel.modelVersion" placeholder="选择模型版本">
                <el-option label="v1.0 - 基础模型" value="v1.0" />
                <el-option label="v1.1 - 优化模型" value="v1.1" />
                <el-option label="v2.0 - 深度学习模型" value="v2.0" />
              </el-select>
            </el-form-item>
            <el-form-item label="预测置信度">
              <el-slider v-model="config.aiModel.predictionConfidence" :min="0" :max="1" :step="0.01" show-input />
            </el-form-item>
            <el-form-item label="重训练间隔(天)">
              <el-input-number v-model="config.aiModel.retrainInterval" :min="1" :max="365" />
            </el-form-item>
            <el-form-item label="数据保留天数">
              <el-input-number v-model="config.aiModel.dataRetentionDays" :min="7" :max="3650" />
            </el-form-item>
            <el-form-item label="启用AI预测">
              <el-switch v-model="config.aiModel.aiPredictionEnabled" />
            </el-form-item>
          </el-form>
        </el-tab-pane>

        <!-- 业务规则 -->
        <el-tab-pane label="业务规则" name="business">
          <el-form :model="config.businessRules" label-width="150px">
            <el-form-item label="自动处理异常">
              <el-switch v-model="config.businessRules.autoHandleException" />
            </el-form-item>
            <el-form-item label="异常处理策略">
              <el-select v-model="config.businessRules.exceptionStrategy" placeholder="选择处理策略">
                <el-option label="立即停止" value="stop" />
                <el-option label="标记继续" value="mark_continue" />
                <el-option label="人工介入" value="manual" />
              </el-select>
            </el-form-item>
            <el-form-item label="质量等级分类">
              <el-checkbox-group v-model="config.businessRules.qualityLevels">
                <el-checkbox label="A">A级 - 优秀</el-checkbox>
                <el-checkbox label="B">B级 - 良好</el-checkbox>
                <el-checkbox label="C">C级 - 合格</el-checkbox>
                <el-checkbox label="D">D级 - 不合格</el-checkbox>
              </el-checkbox-group>
            </el-form-item>
            <el-form-item label="报告生成频率">
              <el-select v-model="config.businessRules.reportFrequency" placeholder="选择频率">
                <el-option label="实时" value="realtime" />
                <el-option label="每小时" value="hourly" />
                <el-option label="每日" value="daily" />
                <el-option label="每周" value="weekly" />
              </el-select>
            </el-form-item>
          </el-form>
        </el-tab-pane>

        <!-- 通知配置 -->
        <el-tab-pane label="通知配置" name="notification">
          <el-form :model="config.notification" label-width="150px">
            <el-form-item label="启用邮件通知">
              <el-switch v-model="config.notification.emailEnabled" />
            </el-form-item>
            <el-form-item label="启用短信通知">
              <el-switch v-model="config.notification.smsEnabled" />
            </el-form-item>
            <el-form-item label="启用Webhook">
              <el-switch v-model="config.notification.webhookEnabled" />
            </el-form-item>
            <el-form-item label="实时告警">
              <el-switch v-model="config.notification.realTimeAlerts" />
            </el-form-item>
            <el-form-item v-if="config.notification.emailEnabled" label="通知接收人">
              <el-input v-model="config.notification.recipients" placeholder="多个邮箱用逗号分隔" />
            </el-form-item>
            <el-form-item v-if="config.notification.webhookEnabled" label="Webhook URL">
              <el-input v-model="config.notification.webhookUrl" placeholder="输入Webhook地址" />
            </el-form-item>
          </el-form>
        </el-tab-pane>
      </el-tabs>
    </el-card>

    <!-- 配置预览 -->
    <el-card class="preview-card" style="margin-top: 20px;">
      <div slot="header">
        <span>配置预览 (JSON格式)</span>
        <el-button type="text" @click="copyConfig">复制配置</el-button>
      </div>
      <pre class="config-preview">{{ JSON.stringify(config, null, 2) }}</pre>
    </el-card>
  </div>
</template>

<script>
export default {
  name: 'AiRuleConfig',
  data() {
    return {
      activeTab: 'quality',
      config: {
        qualityRules: {
          autoInspection: true,
          inspectionInterval: 3600,
          qualityThreshold: 0.95,
          alertThreshold: 0.85,
          inspectionItems: ['dimensional', 'surface', 'material']
        },
        aiModel: {
          modelVersion: 'v1.0',
          predictionConfidence: 0.8,
          retrainInterval: 30,
          dataRetentionDays: 90,
          aiPredictionEnabled: true
        },
        businessRules: {
          autoHandleException: false,
          exceptionStrategy: 'manual',
          qualityLevels: ['A', 'B', 'C'],
          reportFrequency: 'daily'
        },
        notification: {
          emailEnabled: true,
          smsEnabled: false,
          webhookEnabled: true,
          realTimeAlerts: true,
          recipients: '',
          webhookUrl: ''
        }
      }
    }
  },
  mounted() {
    this.loadConfig()
  },
  methods: {
    loadConfig() {
      // 从后端加载配置
      console.log('加载AI规则配置...')
    },
    saveConfig() {
      this.$message.success('AI规则配置保存成功！')
      console.log('保存配置:', this.config)
    },
    pushConfig() {
      // 推送配置到配置驱动端
      this.$message.success('配置已推送到配置驱动端！')
      console.log('推送配置到配置驱动端:', this.config)
    },
    copyConfig() {
      const configText = JSON.stringify(this.config, null, 2)

      navigator.clipboard.writeText(configText).then(() => {
        this.$message.success('配置已复制到剪贴板！')
      })
    }
  }
}
</script>

<style scoped>
.ai-rule-config {
  padding: 20px;
}

.page-header {
  margin-bottom: 20px;
}

.page-header h2 {
  margin: 0 0 8px 0;
  color: #303133;
}

.page-header p {
  margin: 0;
  color: #909399;
  font-size: 14px;
}

.config-card {
  margin-bottom: 20px;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.config-preview {
  background: #f5f7fa;
  padding: 15px;
  border-radius: 4px;
  font-size: 12px;
  max-height: 400px;
  overflow-y: auto;
}

.el-form-item {
  margin-bottom: 20px;
}
</style>
