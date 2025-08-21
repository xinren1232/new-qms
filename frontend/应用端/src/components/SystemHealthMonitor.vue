<template>
  <div class="system-health-monitor">
    <!-- 健康状态指示器 -->
    <div class="health-indicator" :class="overallHealthClass">
      <el-tooltip :content="healthTooltip" placement="bottom">
        <div class="indicator-dot" @click="toggleDetails">
          <i :class="healthIcon"></i>
        </div>
      </el-tooltip>
    </div>

    <!-- 详细状态面板 -->
    <el-drawer
      v-model="showDetails"
      title="系统健康状态"
      direction="rtl"
      size="400px"
      :before-close="handleClose"
    >
      <div class="health-details">
        <!-- 总体状态 -->
        <div class="overall-status">
          <h3>系统状态</h3>
          <div class="status-card" :class="overallHealthClass">
            <div class="status-icon">
              <i :class="healthIcon"></i>
            </div>
            <div class="status-info">
              <div class="status-text">{{ overallHealthText }}</div>
              <div class="status-time">{{ lastCheckTime }}</div>
            </div>
          </div>
        </div>

        <!-- 服务列表 -->
        <div class="services-list">
          <h3>服务状态</h3>
          <div class="service-item" v-for="(service, key) in services" :key="key">
            <div class="service-status" :class="getServiceStatusClass(service)">
              <div class="service-dot"></div>
            </div>
            <div class="service-info">
              <div class="service-name">{{ service.name }}</div>
              <div class="service-details">
                <span class="service-port">:{{ service.port }}</span>
                <span class="service-time" v-if="service.responseTime">
                  {{ service.responseTime }}ms
                </span>
                <span class="service-critical" v-if="service.critical">
                  关键服务
                </span>
              </div>
            </div>
            <div class="service-actions">
              <el-button 
                size="small" 
                @click="checkService(key)"
                :loading="checkingServices.includes(key)"
              >
                检查
              </el-button>
            </div>
          </div>
        </div>

        <!-- 系统指标 -->
        <div class="system-metrics" v-if="metrics">
          <h3>系统指标</h3>
          <div class="metrics-grid">
            <div class="metric-item">
              <div class="metric-label">内存使用</div>
              <div class="metric-value">{{ metrics.memory?.usage || 0 }}%</div>
            </div>
            <div class="metric-item">
              <div class="metric-label">运行时间</div>
              <div class="metric-value">{{ formatUptime(metrics.uptime) }}</div>
            </div>
            <div class="metric-item">
              <div class="metric-label">检查次数</div>
              <div class="metric-value">{{ metrics.checks || 0 }}</div>
            </div>
            <div class="metric-item">
              <div class="metric-label">错误次数</div>
              <div class="metric-value">{{ metrics.errors || 0 }}</div>
            </div>
          </div>
        </div>

        <!-- 操作按钮 -->
        <div class="actions">
          <el-button type="primary" @click="refreshAll" :loading="refreshing">
            刷新所有
          </el-button>
          <el-button @click="exportReport">
            导出报告
          </el-button>
        </div>
      </div>
    </el-drawer>
  </div>
</template>

<script>
import { ref, reactive, computed, onMounted, onUnmounted } from 'vue';
import { ElMessage } from 'element-plus';
import { buildUrl } from '@/config/api';

export default {
  name: 'SystemHealthMonitor',
  setup() {
    const showDetails = ref(false);
    const refreshing = ref(false);
    const checkingServices = ref([]);
    const services = reactive({});
    const metrics = ref(null);
    const lastCheck = ref(null);
    let healthCheckInterval = null;

    // 计算属性
    const overallHealthClass = computed(() => {
      const serviceList = Object.values(services);
      if (serviceList.length === 0) return 'unknown';
      
      const criticalDown = serviceList.filter(s => s.critical && s.status !== 'healthy').length;
      const totalDown = serviceList.filter(s => s.status !== 'healthy').length;
      
      if (criticalDown > 0) return 'critical';
      if (totalDown > 0) return 'warning';
      return 'healthy';
    });

    const healthIcon = computed(() => {
      switch (overallHealthClass.value) {
        case 'healthy': return 'el-icon-success';
        case 'warning': return 'el-icon-warning';
        case 'critical': return 'el-icon-error';
        default: return 'el-icon-question';
      }
    });

    const overallHealthText = computed(() => {
      switch (overallHealthClass.value) {
        case 'healthy': return '系统正常';
        case 'warning': return '部分异常';
        case 'critical': return '严重异常';
        default: return '状态未知';
      }
    });

    const healthTooltip = computed(() => {
      const serviceList = Object.values(services);
      const total = serviceList.length;
      const healthy = serviceList.filter(s => s.status === 'healthy').length;
      return `${healthy}/${total} 服务正常`;
    });

    const lastCheckTime = computed(() => {
      if (!lastCheck.value) return '未检查';
      return new Date(lastCheck.value).toLocaleTimeString();
    });

    // 方法
    const getServiceStatusClass = (service) => {
      switch (service.status) {
        case 'healthy': return 'healthy';
        case 'unhealthy': return service.critical ? 'critical' : 'warning';
        case 'error': return 'critical';
        default: return 'unknown';
      }
    };

    const formatUptime = (uptime) => {
      if (!uptime) return '未知';
      const hours = Math.floor(uptime / 3600000);
      const minutes = Math.floor((uptime % 3600000) / 60000);
      return `${hours}h ${minutes}m`;
    };

    const checkAllServices = async () => {
      try {
        const response = await fetch(buildUrl('monitor', '/api/services'));
        if (response.ok) {
          const data = await response.json();
          if (data.success) {
            Object.assign(services, data.data);
            lastCheck.value = data.timestamp;
          }
        }
      } catch (error) {
        console.warn('健康检查失败:', error.message);
      }
    };

    const checkService = async (serviceKey) => {
      checkingServices.value.push(serviceKey);
      try {
        const response = await fetch(buildUrl('monitor', `/api/services/${serviceKey}`));
        if (response.ok) {
          const data = await response.json();
          if (data.success) {
            services[serviceKey] = data.data;
          }
        }
      } catch (error) {
        ElMessage.error(`检查服务失败: ${error.message}`);
      } finally {
        const index = checkingServices.value.indexOf(serviceKey);
        if (index > -1) {
          checkingServices.value.splice(index, 1);
        }
      }
    };

    const getMetrics = async () => {
      try {
        const response = await fetch(buildUrl('monitor', '/api/metrics'));
        if (response.ok) {
          const data = await response.json();
          if (data.success) {
            metrics.value = data.data;
          }
        }
      } catch (error) {
        console.warn('获取指标失败:', error.message);
      }
    };

    const refreshAll = async () => {
      refreshing.value = true;
      try {
        await Promise.all([
          checkAllServices(),
          getMetrics()
        ]);
        ElMessage.success('刷新完成');
      } catch (error) {
        ElMessage.error('刷新失败');
      } finally {
        refreshing.value = false;
      }
    };

    const exportReport = () => {
      const report = {
        timestamp: new Date().toISOString(),
        overall: overallHealthText.value,
        services: services,
        metrics: metrics.value
      };
      
      const blob = new Blob([JSON.stringify(report, null, 2)], {
        type: 'application/json'
      });
      
      const url = URL.createObjectURL(blob);
      const a = document.createElement('a');
      a.href = url;
      a.download = `health-report-${Date.now()}.json`;
      a.click();
      URL.revokeObjectURL(url);
    };

    const toggleDetails = () => {
      showDetails.value = !showDetails.value;
    };

    const handleClose = () => {
      showDetails.value = false;
    };

    // 生命周期
    onMounted(() => {
      // 立即执行一次检查
      refreshAll();
      
      // 设置定时检查
      healthCheckInterval = setInterval(() => {
        checkAllServices();
      }, 30000); // 30秒检查一次
    });

    onUnmounted(() => {
      if (healthCheckInterval) {
        clearInterval(healthCheckInterval);
      }
    });

    return {
      showDetails,
      refreshing,
      checkingServices,
      services,
      metrics,
      overallHealthClass,
      healthIcon,
      overallHealthText,
      healthTooltip,
      lastCheckTime,
      getServiceStatusClass,
      formatUptime,
      checkService,
      refreshAll,
      exportReport,
      toggleDetails,
      handleClose
    };
  }
};
</script>

<style scoped>
.system-health-monitor {
  position: fixed;
  top: 20px;
  right: 20px;
  z-index: 1000;
}

.health-indicator {
  width: 40px;
  height: 40px;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  cursor: pointer;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
  transition: all 0.3s ease;
}

.health-indicator:hover {
  transform: scale(1.1);
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.2);
}

.health-indicator.healthy {
  background: #67c23a;
  color: white;
}

.health-indicator.warning {
  background: #e6a23c;
  color: white;
}

.health-indicator.critical {
  background: #f56c6c;
  color: white;
}

.health-indicator.unknown {
  background: #909399;
  color: white;
}

.indicator-dot i {
  font-size: 18px;
}

.health-details {
  padding: 20px;
}

.overall-status h3,
.services-list h3,
.system-metrics h3 {
  margin-bottom: 15px;
  color: #303133;
}

.status-card {
  display: flex;
  align-items: center;
  padding: 15px;
  border-radius: 8px;
  margin-bottom: 20px;
}

.status-card.healthy {
  background: #f0f9ff;
  border: 1px solid #67c23a;
}

.status-card.warning {
  background: #fdf6ec;
  border: 1px solid #e6a23c;
}

.status-card.critical {
  background: #fef0f0;
  border: 1px solid #f56c6c;
}

.status-icon {
  font-size: 24px;
  margin-right: 15px;
}

.status-text {
  font-size: 16px;
  font-weight: bold;
  margin-bottom: 5px;
}

.status-time {
  font-size: 12px;
  color: #909399;
}

.service-item {
  display: flex;
  align-items: center;
  padding: 10px 0;
  border-bottom: 1px solid #ebeef5;
}

.service-status {
  width: 12px;
  height: 12px;
  border-radius: 50%;
  margin-right: 15px;
}

.service-status.healthy {
  background: #67c23a;
}

.service-status.warning {
  background: #e6a23c;
}

.service-status.critical {
  background: #f56c6c;
}

.service-status.unknown {
  background: #909399;
}

.service-info {
  flex: 1;
}

.service-name {
  font-weight: bold;
  margin-bottom: 5px;
}

.service-details {
  font-size: 12px;
  color: #909399;
}

.service-port {
  margin-right: 10px;
}

.service-time {
  margin-right: 10px;
}

.service-critical {
  color: #f56c6c;
  font-weight: bold;
}

.metrics-grid {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 15px;
  margin-bottom: 20px;
}

.metric-item {
  text-align: center;
  padding: 15px;
  background: #f5f7fa;
  border-radius: 8px;
}

.metric-label {
  font-size: 12px;
  color: #909399;
  margin-bottom: 5px;
}

.metric-value {
  font-size: 18px;
  font-weight: bold;
  color: #303133;
}

.actions {
  display: flex;
  gap: 10px;
  justify-content: center;
}
</style>
