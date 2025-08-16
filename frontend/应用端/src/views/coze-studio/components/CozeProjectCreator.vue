<template>
  <div class="coze-project-creator">
    <el-steps :active="currentStep" align-center>
      <el-step title="选择类型" />
      <el-step title="基本信息" />
      <el-step title="初始配置" />
    </el-steps>
    
    <!-- 步骤1: 选择项目类型 -->
    <div v-if="currentStep === 0" class="step-content">
      <h3>选择项目类型</h3>
      <div class="project-types">
        <div
          v-for="type in projectTypes"
          :key="type.value"
          :class="['type-card', { selected: projectData.type === type.value }]"
          @click="selectType(type.value)"
        >
          <div class="type-icon">
            <el-icon>
              <component :is="type.icon" />
            </el-icon>
          </div>
          <h4>{{ type.name }}</h4>
          <p>{{ type.description }}</p>
          <div class="type-features">
            <el-tag
              v-for="feature in type.features"
              :key="feature"
              size="small"
              type="info"
            >
              {{ feature }}
            </el-tag>
          </div>
        </div>
      </div>
    </div>
    
    <!-- 步骤2: 基本信息 -->
    <div v-if="currentStep === 1" class="step-content">
      <h3>基本信息</h3>
      <el-form :model="projectData" label-width="100px" size="default">
        <el-form-item label="项目名称" required>
          <el-input
            v-model="projectData.name"
            placeholder="输入项目名称"
            maxlength="50"
            show-word-limit
          />
        </el-form-item>
        
        <el-form-item label="项目描述">
          <el-input
            v-model="projectData.description"
            type="textarea"
            :rows="4"
            placeholder="描述项目的功能和用途"
            maxlength="200"
            show-word-limit
          />
        </el-form-item>
        
        <el-form-item label="项目图标">
          <el-upload
            class="avatar-uploader"
            :show-file-list="false"
            :on-success="handleAvatarSuccess"
            action="/api/upload"
          >
            <img v-if="projectData.avatar" :src="projectData.avatar" class="avatar" />
            <el-icon v-else class="avatar-uploader-icon"><Plus /></el-icon>
          </el-upload>
        </el-form-item>
        
        <el-form-item label="标签">
          <el-select
            v-model="projectData.tags"
            multiple
            filterable
            allow-create
            placeholder="选择或创建标签"
            style="width: 100%"
          >
            <el-option
              v-for="tag in availableTags"
              :key="tag"
              :label="tag"
              :value="tag"
            />
          </el-select>
        </el-form-item>
      </el-form>
    </div>
    
    <!-- 步骤3: 初始配置 -->
    <div v-if="currentStep === 2" class="step-content">
      <h3>初始配置</h3>
      
      <!-- Agent配置 -->
      <div v-if="projectData.type === 'agent'" class="config-section">
        <h4>Agent配置</h4>
        <el-form :model="projectData.config" label-width="120px" size="default">
          <el-form-item label="AI模型">
            <el-select v-model="projectData.config.model" placeholder="选择AI模型">
              <el-option label="GPT-4o" value="gpt-4o" />
              <el-option label="Claude-3" value="claude-3" />
              <el-option label="DeepSeek" value="deepseek" />
            </el-select>
          </el-form-item>
          
          <el-form-item label="系统提示词">
            <el-input
              v-model="projectData.config.systemPrompt"
              type="textarea"
              :rows="4"
              placeholder="定义Agent的角色和行为..."
            />
          </el-form-item>
          
          <el-form-item label="启用功能">
            <el-checkbox-group v-model="projectData.config.features">
              <el-checkbox label="memory">记忆功能</el-checkbox>
              <el-checkbox label="webSearch">网络搜索</el-checkbox>
              <el-checkbox label="imageGen">图像生成</el-checkbox>
              <el-checkbox label="codeExec">代码执行</el-checkbox>
            </el-checkbox-group>
          </el-form-item>
        </el-form>
      </div>
      
      <!-- Workflow配置 -->
      <div v-if="projectData.type === 'workflow'" class="config-section">
        <h4>工作流配置</h4>
        <el-form :model="projectData.config" label-width="120px" size="default">
          <el-form-item label="触发方式">
            <el-select v-model="projectData.config.trigger" placeholder="选择触发方式">
              <el-option label="手动触发" value="manual" />
              <el-option label="定时触发" value="schedule" />
              <el-option label="事件触发" value="event" />
              <el-option label="API触发" value="api" />
            </el-select>
          </el-form-item>
          
          <el-form-item v-if="projectData.config.trigger === 'schedule'" label="定时规则">
            <el-input
              v-model="projectData.config.schedule"
              placeholder="例如: 0 9 * * 1-5 (工作日上午9点)"
            />
          </el-form-item>
          
          <el-form-item label="并发限制">
            <el-input-number
              v-model="projectData.config.concurrency"
              :min="1"
              :max="10"
            />
          </el-form-item>
        </el-form>
      </div>
      
      <!-- Knowledge配置 -->
      <div v-if="projectData.type === 'knowledge'" class="config-section">
        <h4>知识库配置</h4>
        <el-form :model="projectData.config" label-width="120px" size="default">
          <el-form-item label="向量模型">
            <el-select v-model="projectData.config.embeddingModel" placeholder="选择向量模型">
              <el-option label="text-embedding-ada-002" value="ada-002" />
              <el-option label="text-embedding-3-small" value="3-small" />
              <el-option label="text-embedding-3-large" value="3-large" />
            </el-select>
          </el-form-item>
          
          <el-form-item label="分块大小">
            <el-input-number
              v-model="projectData.config.chunkSize"
              :min="100"
              :max="2000"
            />
          </el-form-item>
          
          <el-form-item label="分块重叠">
            <el-input-number
              v-model="projectData.config.chunkOverlap"
              :min="0"
              :max="500"
            />
          </el-form-item>
          
          <el-form-item label="自动索引">
            <el-switch v-model="projectData.config.autoIndex" />
          </el-form-item>
        </el-form>
      </div>
      
      <!-- Plugin配置 -->
      <div v-if="projectData.type === 'plugin'" class="config-section">
        <h4>插件配置</h4>
        <el-form :model="projectData.config" label-width="120px" size="default">
          <el-form-item label="插件类型">
            <el-select v-model="projectData.config.pluginType" placeholder="选择插件类型">
              <el-option label="API工具" value="api" />
              <el-option label="数据处理" value="data" />
              <el-option label="文件操作" value="file" />
              <el-option label="通知服务" value="notification" />
            </el-select>
          </el-form-item>
          
          <el-form-item label="入口函数">
            <el-input
              v-model="projectData.config.entryPoint"
              placeholder="例如: main"
            />
          </el-form-item>
          
          <el-form-item label="依赖包">
            <el-input
              v-model="projectData.config.dependencies"
              type="textarea"
              :rows="3"
              placeholder="每行一个依赖包"
            />
          </el-form-item>
        </el-form>
      </div>
    </div>
    
    <!-- 操作按钮 -->
    <div class="step-actions">
      <el-button v-if="currentStep > 0" @click="prevStep">上一步</el-button>
      <el-button v-if="currentStep < 2" type="primary" @click="nextStep">下一步</el-button>
      <el-button v-if="currentStep === 2" type="primary" @click="createProject">创建项目</el-button>
      <el-button @click="$emit('cancel')">取消</el-button>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive } from 'vue'
import { User, Connection, Document, Tools, Plus } from '@element-plus/icons-vue'
import { ElMessage } from 'element-plus'

// Emits
const emit = defineEmits(['create', 'cancel'])

// 响应式数据
const currentStep = ref(0)

// 项目类型定义
const projectTypes = [
  {
    value: 'agent',
    name: 'AI Agent',
    description: '创建智能对话助手',
    icon: User,
    features: ['对话交互', '角色扮演', '任务执行']
  },
  {
    value: 'workflow',
    name: '工作流',
    description: '自动化业务流程',
    icon: Connection,
    features: ['流程自动化', '条件分支', '定时执行']
  },
  {
    value: 'knowledge',
    name: '知识库',
    description: '构建智能问答系统',
    icon: Document,
    features: ['文档索引', '语义搜索', '知识问答']
  },
  {
    value: 'plugin',
    name: '插件',
    description: '扩展系统功能',
    icon: Tools,
    features: ['功能扩展', 'API集成', '自定义工具']
  }
]

// 可用标签
const availableTags = [
  '质量管理', 'AI助手', '自动化', '数据分析', 
  '客服', '文档处理', '通知', '集成'
]

// 项目数据
const projectData = reactive({
  type: '',
  name: '',
  description: '',
  avatar: '',
  tags: [],
  config: {}
})

// 方法
const selectType = (type) => {
  projectData.type = type
  
  // 初始化配置
  switch (type) {
    case 'agent':
      projectData.config = {
        model: 'gpt-4o',
        systemPrompt: '',
        features: ['memory']
      }
      break
    case 'workflow':
      projectData.config = {
        trigger: 'manual',
        schedule: '',
        concurrency: 1
      }
      break
    case 'knowledge':
      projectData.config = {
        embeddingModel: 'ada-002',
        chunkSize: 1000,
        chunkOverlap: 200,
        autoIndex: true
      }
      break
    case 'plugin':
      projectData.config = {
        pluginType: 'api',
        entryPoint: 'main',
        dependencies: ''
      }
      break
  }
}

const nextStep = () => {
  if (currentStep.value === 0 && !projectData.type) {
    ElMessage.warning('请选择项目类型')
    return
  }
  
  if (currentStep.value === 1 && !projectData.name) {
    ElMessage.warning('请输入项目名称')
    return
  }
  
  currentStep.value++
}

const prevStep = () => {
  currentStep.value--
}

const handleAvatarSuccess = (response) => {
  projectData.avatar = response.url
  ElMessage.success('图标上传成功')
}

const createProject = () => {
  if (!projectData.name) {
    ElMessage.error('请输入项目名称')
    return
  }
  
  emit('create', { ...projectData })
}
</script>

<style lang="scss" scoped>
.coze-project-creator {
  .el-steps {
    margin-bottom: 32px;
  }
  
  .step-content {
    min-height: 400px;
    padding: 20px 0;
    
    h3 {
      margin: 0 0 24px 0;
      font-size: 18px;
      color: #1f2937;
      text-align: center;
    }
    
    .project-types {
      display: grid;
      grid-template-columns: repeat(2, 1fr);
      gap: 20px;
      
      .type-card {
        padding: 24px;
        border: 2px solid #e5e7eb;
        border-radius: 12px;
        text-align: center;
        cursor: pointer;
        transition: all 0.3s ease;
        
        &:hover {
          border-color: #3b82f6;
          box-shadow: 0 4px 12px rgba(59, 130, 246, 0.15);
        }
        
        &.selected {
          border-color: #3b82f6;
          background: #eff6ff;
          box-shadow: 0 0 0 3px rgba(59, 130, 246, 0.1);
        }
        
        .type-icon {
          width: 64px;
          height: 64px;
          margin: 0 auto 16px;
          background: #f3f4f6;
          border-radius: 12px;
          display: flex;
          align-items: center;
          justify-content: center;
          font-size: 32px;
          color: #6b7280;
        }
        
        h4 {
          margin: 0 0 8px 0;
          font-size: 16px;
          font-weight: 600;
          color: #1f2937;
        }
        
        p {
          margin: 0 0 16px 0;
          font-size: 14px;
          color: #6b7280;
          line-height: 1.4;
        }
        
        .type-features {
          display: flex;
          flex-wrap: wrap;
          gap: 6px;
          justify-content: center;
        }
      }
    }
    
    .config-section {
      h4 {
        margin: 0 0 16px 0;
        font-size: 16px;
        font-weight: 600;
        color: #1f2937;
      }
    }
  }
  
  .step-actions {
    margin-top: 32px;
    text-align: center;
    
    .el-button {
      margin: 0 8px;
    }
  }
}

.avatar-uploader {
  :deep(.el-upload) {
    border: 1px dashed #d9d9d9;
    border-radius: 6px;
    cursor: pointer;
    position: relative;
    overflow: hidden;
    transition: all 0.3s;
    
    &:hover {
      border-color: #409eff;
    }
  }
  
  .avatar-uploader-icon {
    font-size: 28px;
    color: #8c939d;
    width: 80px;
    height: 80px;
    text-align: center;
    line-height: 80px;
  }
  
  .avatar {
    width: 80px;
    height: 80px;
    display: block;
  }
}
</style>
