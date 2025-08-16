<template>
  <div class="data-source-config">
    <div class="page-header">
      <h2>数据源配置管理</h2>
      <p>配置AI系统的数据源连接、数据处理规则和同步策略</p>
    </div>

    <el-card class="config-card">
      <div slot="header" class="card-header">
        <span>数据源配置</span>
        <div>
          <el-button type="primary" @click="testConnection">测试连接</el-button>
          <el-button type="success" @click="saveConfig">保存配置</el-button>
          <el-button type="warning" @click="pushConfig">推送配置</el-button>
        </div>
      </div>

      <el-tabs v-model="activeTab" type="card">
        <!-- 数据库配置 -->
        <el-tab-pane label="数据库配置" name="database">
          <el-form :model="config.database" label-width="120px">
            <el-form-item label="数据库类型">
              <el-select v-model="config.database.type" placeholder="选择数据库类型">
                <el-option label="MySQL" value="mysql" />
                <el-option label="PostgreSQL" value="postgresql" />
                <el-option label="Oracle" value="oracle" />
                <el-option label="SQL Server" value="sqlserver" />
              </el-select>
            </el-form-item>
            <el-form-item label="服务器地址">
              <el-input v-model="config.database.host" placeholder="数据库服务器地址" />
            </el-form-item>
            <el-form-item label="端口">
              <el-input-number v-model="config.database.port" :min="1" :max="65535" />
            </el-form-item>
            <el-form-item label="数据库名">
              <el-input v-model="config.database.database" placeholder="数据库名称" />
            </el-form-item>
            <el-form-item label="用户名">
              <el-input v-model="config.database.username" placeholder="数据库用户名" />
            </el-form-item>
            <el-form-item label="密码">
              <el-input v-model="config.database.password" type="password" placeholder="数据库密码" show-password />
            </el-form-item>
            <el-form-item label="连接池大小">
              <el-input-number v-model="config.database.poolSize" :min="1" :max="100" />
            </el-form-item>
          </el-form>
        </el-tab-pane>

        <!-- API接口配置 -->
        <el-tab-pane label="API接口配置" name="api">
          <el-form :model="config.api" label-width="120px">
            <el-form-item label="基础URL">
              <el-input v-model="config.api.baseUrl" placeholder="API基础地址" />
            </el-form-item>
            <el-form-item label="认证方式">
              <el-select v-model="config.api.authType" placeholder="选择认证方式">
                <el-option label="无认证" value="none" />
                <el-option label="API Key" value="apikey" />
                <el-option label="Bearer Token" value="bearer" />
                <el-option label="Basic Auth" value="basic" />
              </el-select>
            </el-form-item>
            <el-form-item v-if="config.api.authType === 'apikey'" label="API Key">
              <el-input v-model="config.api.apiKey" placeholder="输入API Key" />
            </el-form-item>
            <el-form-item v-if="config.api.authType === 'bearer'" label="Token">
              <el-input v-model="config.api.token" placeholder="输入Bearer Token" />
            </el-form-item>
            <el-form-item v-if="config.api.authType === 'basic'" label="用户名">
              <el-input v-model="config.api.basicUsername" placeholder="Basic认证用户名" />
            </el-form-item>
            <el-form-item v-if="config.api.authType === 'basic'" label="密码">
              <el-input v-model="config.api.basicPassword" type="password" placeholder="Basic认证密码" show-password />
            </el-form-item>
            <el-form-item label="超时时间(秒)">
              <el-input-number v-model="config.api.timeout" :min="1" :max="300" />
            </el-form-item>
            <el-form-item label="重试次数">
              <el-input-number v-model="config.api.retryCount" :min="0" :max="10" />
            </el-form-item>
          </el-form>
        </el-tab-pane>

        <!-- 文件存储配置 -->
        <el-tab-pane label="文件存储配置" name="storage">
          <el-form :model="config.storage" label-width="120px">
            <el-form-item label="存储类型">
              <el-select v-model="config.storage.type" placeholder="选择存储类型">
                <el-option label="本地存储" value="local" />
                <el-option label="阿里云OSS" value="aliyun" />
                <el-option label="腾讯云COS" value="tencent" />
                <el-option label="AWS S3" value="aws" />
              </el-select>
            </el-form-item>
            <el-form-item v-if="config.storage.type === 'local'" label="存储路径">
              <el-input v-model="config.storage.localPath" placeholder="本地存储路径" />
            </el-form-item>
            <el-form-item v-if="config.storage.type !== 'local'" label="Bucket名称">
              <el-input v-model="config.storage.bucket" placeholder="存储桶名称" />
            </el-form-item>
            <el-form-item v-if="config.storage.type !== 'local'" label="Access Key">
              <el-input v-model="config.storage.accessKey" placeholder="访问密钥" />
            </el-form-item>
            <el-form-item v-if="config.storage.type !== 'local'" label="Secret Key">
              <el-input v-model="config.storage.secretKey" type="password" placeholder="私有密钥" show-password />
            </el-form-item>
            <el-form-item v-if="config.storage.type !== 'local'" label="区域">
              <el-input v-model="config.storage.region" placeholder="存储区域" />
            </el-form-item>
            <el-form-item label="最大文件大小(MB)">
              <el-input-number v-model="config.storage.maxFileSize" :min="1" :max="1024" />
            </el-form-item>
          </el-form>
        </el-tab-pane>

        <!-- 同步策略 -->
        <el-tab-pane label="同步策略" name="sync">
          <el-form :model="config.sync" label-width="120px">
            <el-form-item label="启用自动同步">
              <el-switch v-model="config.sync.autoSync" />
            </el-form-item>
            <el-form-item label="同步间隔(分钟)">
              <el-input-number v-model="config.sync.syncInterval" :min="1" :max="1440" />
            </el-form-item>
            <el-form-item label="同步方式">
              <el-select v-model="config.sync.syncMode" placeholder="选择同步方式">
                <el-option label="增量同步" value="incremental" />
                <el-option label="全量同步" value="full" />
                <el-option label="智能同步" value="smart" />
              </el-select>
            </el-form-item>
            <el-form-item label="冲突处理">
              <el-select v-model="config.sync.conflictResolution" placeholder="选择冲突处理方式">
                <el-option label="源优先" value="source_first" />
                <el-option label="目标优先" value="target_first" />
                <el-option label="时间戳优先" value="timestamp" />
                <el-option label="手动处理" value="manual" />
              </el-select>
            </el-form-item>
            <el-form-item label="数据验证">
              <el-checkbox-group v-model="config.sync.validationRules">
                <el-checkbox label="format">格式验证</el-checkbox>
                <el-checkbox label="integrity">完整性检查</el-checkbox>
                <el-checkbox label="business">业务规则验证</el-checkbox>
                <el-checkbox label="duplicate">重复数据检测</el-checkbox>
              </el-checkbox-group>
            </el-form-item>
            <el-form-item label="错误处理">
              <el-select v-model="config.sync.errorHandling" placeholder="选择错误处理方式">
                <el-option label="跳过错误" value="skip" />
                <el-option label="停止同步" value="stop" />
                <el-option label="记录日志" value="log" />
                <el-option label="发送告警" value="alert" />
              </el-select>
            </el-form-item>
          </el-form>
        </el-tab-pane>
      </el-tabs>
    </el-card>

    <!-- 连接状态 -->
    <el-card class="status-card" style="margin-top: 20px;">
      <div slot="header">
        <span>连接状态</span>
        <el-button type="text" @click="refreshStatus">刷新状态</el-button>
      </div>
      <el-row :gutter="20">
        <el-col :span="6">
          <div class="status-item">
            <div class="status-label">数据库连接</div>
            <div class="status-value" :class="connectionStatus.database ? 'success' : 'error'">
              {{ connectionStatus.database ? '正常' : '异常' }}
            </div>
          </div>
        </el-col>
        <el-col :span="6">
          <div class="status-item">
            <div class="status-label">API接口</div>
            <div class="status-value" :class="connectionStatus.api ? 'success' : 'error'">
              {{ connectionStatus.api ? '正常' : '异常' }}
            </div>
          </div>
        </el-col>
        <el-col :span="6">
          <div class="status-item">
            <div class="status-label">文件存储</div>
            <div class="status-value" :class="connectionStatus.storage ? 'success' : 'error'">
              {{ connectionStatus.storage ? '正常' : '异常' }}
            </div>
          </div>
        </el-col>
        <el-col :span="6">
          <div class="status-item">
            <div class="status-label">同步状态</div>
            <div class="status-value" :class="connectionStatus.sync ? 'success' : 'warning'">
              {{ connectionStatus.sync ? '运行中' : '已停止' }}
            </div>
          </div>
        </el-col>
      </el-row>
    </el-card>
  </div>
</template>

<script>
export default {
  name: 'DataSourceConfig',
  data() {
    return {
      activeTab: 'database',
      config: {
        database: {
          type: 'mysql',
          host: 'localhost',
          port: 3306,
          database: 'qms_ai',
          username: '',
          password: '',
          poolSize: 10
        },
        api: {
          baseUrl: '',
          authType: 'none',
          apiKey: '',
          token: '',
          basicUsername: '',
          basicPassword: '',
          timeout: 30,
          retryCount: 3
        },
        storage: {
          type: 'local',
          localPath: './uploads',
          bucket: '',
          accessKey: '',
          secretKey: '',
          region: '',
          maxFileSize: 100
        },
        sync: {
          autoSync: true,
          syncInterval: 30,
          syncMode: 'incremental',
          conflictResolution: 'timestamp',
          validationRules: ['format', 'integrity'],
          errorHandling: 'log'
        }
      },
      connectionStatus: {
        database: false,
        api: false,
        storage: false,
        sync: false
      }
    }
  },
  mounted() {
    this.loadConfig()
    this.refreshStatus()
  },
  methods: {
    loadConfig() {
      console.log('加载数据源配置...')
    },
    saveConfig() {
      this.$message.success('数据源配置保存成功！')
      console.log('保存配置:', this.config)
    },
    pushConfig() {
      this.$message.success('配置已推送到配置驱动端！')
      console.log('推送配置到配置驱动端:', this.config)
    },
    testConnection() {
      this.$message.info('正在测试连接...')
      // 模拟测试连接
      setTimeout(() => {
        this.connectionStatus = {
          database: true,
          api: true,
          storage: true,
          sync: true
        }
        this.$message.success('连接测试成功！')
      }, 2000)
    },
    refreshStatus() {
      console.log('刷新连接状态...')
      // 模拟状态检查
      this.connectionStatus = {
        database: Math.random() > 0.3,
        api: Math.random() > 0.3,
        storage: Math.random() > 0.3,
        sync: Math.random() > 0.5
      }
    }
  }
}
</script>

<style scoped>
.data-source-config {
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

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.status-item {
  text-align: center;
  padding: 15px;
  border: 1px solid #ebeef5;
  border-radius: 4px;
}

.status-label {
  font-size: 14px;
  color: #909399;
  margin-bottom: 8px;
}

.status-value {
  font-size: 16px;
  font-weight: bold;
}

.status-value.success {
  color: #67c23a;
}

.status-value.error {
  color: #f56c6c;
}

.status-value.warning {
  color: #e6a23c;
}
</style>
