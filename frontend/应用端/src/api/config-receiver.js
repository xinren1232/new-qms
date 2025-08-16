/**
 * 配置接收器 - 接收来自配置端的配置推送
 * 实现配置端→应用端的实时配置同步
 */

import { useConfigStore } from '@/stores/config'
import { useAuthStore } from '@/stores/auth'
import { ElMessage, ElNotification } from 'element-plus'

class ConfigReceiver {
  constructor() {
    this.configStore = useConfigStore();
    this.authStore = useAuthStore();
    this.isListening = false;
    this.setupConfigEndpoint();
  }

  // 设置配置接收端点
  setupConfigEndpoint() {
    // 在开发环境中，通过 vite 开发服务器接收配置
    if (import.meta.env.DEV) {
      this.setupDevConfigReceiver();
    } else {
      // 生产环境中，通过 API 接收配置
      this.setupProdConfigReceiver();
    }
  }

  // 开发环境配置接收
  setupDevConfigReceiver() {
    // 通过轮询检查配置更新
    this.startConfigPolling();
    
    // 监听 localStorage 变化（配置端可能直接更新）
    this.setupStorageListener();
  }

  // 生产环境配置接收
  setupProdConfigReceiver() {
    // 仅当显式配置了 VITE_WS_BASE_URL 时启用 WebSocket；否则采用轮询，避免无效的 ws://localhost:8080 报错
    if (import.meta.env.VITE_WS_BASE_URL) {
      this.setupWebSocketListener();
    } else {
      // 无 WS 配置：使用轮询 + 本地存储监听
      this.startConfigPolling();
      this.setupStorageListener();
    }
  }

  // 启动配置轮询
  startConfigPolling() {
    if (this.isListening) return;
    
    this.isListening = true;
    this.pollingInterval = setInterval(async () => {
      await this.checkConfigUpdates();
    }, 5000); // 每5秒检查一次配置更新
  }

  // 停止配置轮询
  stopConfigPolling() {
    if (this.pollingInterval) {
      clearInterval(this.pollingInterval);
      this.pollingInterval = null;
    }
    this.isListening = false;
  }

  // 检查配置更新
  async checkConfigUpdates() {
    try {
      // 检查本地存储中的配置版本
      const configKeys = [
        'ai.config',
        'app.config', 
        'permission.config',
        'system.api',
        'monitoring.config'
      ];

      for (const key of configKeys) {
        const storedConfig = localStorage.getItem(`config_${key}`);
        if (storedConfig) {
          try {
            const config = JSON.parse(storedConfig);
            const currentVersion = this.configStore.getConfigVersion(key);
            const storedVersion = config._version || 0;
            
            if (storedVersion > currentVersion) {
              await this.handleConfigUpdate(key, config);
            }
          } catch (error) {
            console.warn('解析配置失败:', key, error);
          }
        }
      }
    } catch (error) {
      console.error('检查配置更新失败:', error);
    }
  }

  // 设置存储监听器
  setupStorageListener() {
    window.addEventListener('storage', (event) => {
      if (event.key && event.key.startsWith('config_')) {
        const configKey = event.key.replace('config_', '');
        if (event.newValue) {
          try {
            const config = JSON.parse(event.newValue);
            this.handleConfigUpdate(configKey, config);
          } catch (error) {
            console.error('处理存储配置更新失败:', error);
          }
        }
      }
    });
  }

  // 设置 WebSocket 监听器
  setupWebSocketListener() {
    // 只有在明确配置了WebSocket URL时才尝试连接
    if (!import.meta.env.VITE_WS_BASE_URL) {
      console.log('⚠️ 未配置VITE_WS_BASE_URL，跳过WebSocket连接，使用轮询模式');
      this.setupDevConfigReceiver();
      return;
    }

    const wsUrl = `${import.meta.env.VITE_WS_BASE_URL}/config-sync`;

    try {
      this.ws = new WebSocket(wsUrl);
      
      this.ws.onopen = () => {
        console.log('配置同步 WebSocket 连接已建立');
        this.isListening = true;
      };
      
      this.ws.onmessage = (event) => {
        try {
          const message = JSON.parse(event.data);
          if (message.type === 'config_update') {
            this.handleConfigUpdate(message.key, message.value);
          }
        } catch (error) {
          console.error('处理 WebSocket 配置消息失败:', error);
        }
      };
      
      this.ws.onclose = () => {
        console.log('配置同步 WebSocket 连接已关闭');
        this.isListening = false;
        
        // 尝试重连
        setTimeout(() => {
          this.setupWebSocketListener();
        }, 5000);
      };
      
      this.ws.onerror = (error) => {
        console.error('配置同步 WebSocket 错误:', error);
      };
    } catch (error) {
      console.error('建立 WebSocket 连接失败:', error);
      // 回退到轮询模式
      this.setupDevConfigReceiver();
    }
  }

  // 处理配置更新
  async handleConfigUpdate(configKey, configData) {
    try {
      console.log('收到配置更新:', configKey, configData);
      
      switch (configKey) {
        case 'ai.config':
          await this.updateAIConfig(configData);
          break;
        case 'app.config':
          await this.updateAppConfig(configData);
          break;
        case 'permission.config':
          await this.updatePermissionConfig(configData);
          break;
        case 'system.api':
          await this.updateSystemConfig(configData);
          break;
        case 'monitoring.config':
          await this.updateMonitoringConfig(configData);
          break;
        default:
          if (configKey.startsWith('user_permission.')) {
            await this.updateUserPermission(configKey, configData);
          } else {
            console.warn('未知配置类型:', configKey);
          }
      }
      
      // 更新配置版本
      this.configStore.setConfigVersion(configKey, configData._version || Date.now());
      
      // 通知用户配置已更新
      this.notifyConfigUpdate(configKey);
      
      return { success: true };
    } catch (error) {
      console.error('配置更新失败:', configKey, error);
      return { success: false, error: error.message };
    }
  }

  // 更新AI配置
  async updateAIConfig(configData) {
    const currentModel = this.configStore.getCurrentModel();
    
    // 更新AI配置
    this.configStore.updateAIConfig(configData);
    
    // 检查当前使用的模型是否仍然可用
    if (configData.models && !configData.models[currentModel]?.enabled) {
      // 切换到默认模型
      const defaultModel = configData.default_model || 'gpt-4o-mini';
      this.configStore.setCurrentModel(defaultModel);
      
      ElNotification({
        title: '模型切换',
        message: `当前模型已被禁用，已自动切换到 ${defaultModel}`,
        type: 'warning',
        duration: 5000
      });
    }
    
    // 更新用户配额限制
    if (configData.rate_limits) {
      this.configStore.updateRateLimits(configData.rate_limits);
    }
  }

  // 更新应用配置
  async updateAppConfig(configData) {
    // 更新主题配置
    if (configData.theme) {
      this.configStore.updateTheme(configData.theme);
      
      // 应用主题变化
      if (configData.theme.primary_color) {
        document.documentElement.style.setProperty('--el-color-primary', configData.theme.primary_color);
      }
      
      if (configData.theme.dark_mode !== undefined) {
        document.documentElement.classList.toggle('dark', configData.theme.dark_mode);
      }
    }
    
    // 更新布局配置
    if (configData.layout) {
      this.configStore.updateLayout(configData.layout);
    }
    
    // 更新功能开关
    if (configData.features) {
      this.configStore.updateFeatures(configData.features);
    }
    
    // 更新聊天配置
    if (configData.chat) {
      this.configStore.updateChatConfig(configData.chat);
    }
  }

  // 更新权限配置
  async updatePermissionConfig(configData) {
    // 刷新用户权限
    await this.authStore.refreshUserPermissions();
    
    // 更新权限配置
    this.configStore.updatePermissionConfig(configData);
  }

  // 更新系统配置
  async updateSystemConfig(configData) {
    // 更新API配置
    this.configStore.updateSystemConfig(configData);
    
    // 如果API地址发生变化，可能需要重新初始化API客户端
    if (configData.base_api) {
      // 这里可以重新配置 axios 实例
      console.log('API地址已更新:', configData.base_api);
    }
  }

  // 更新监控配置
  async updateMonitoringConfig(configData) {
    this.configStore.updateMonitoringConfig(configData);
    
    // 如果监控被禁用，停止相关监控功能
    if (!configData.enabled) {
      // 停止性能监控等
      console.log('监控功能已禁用');
    }
  }

  // 更新用户权限
  async updateUserPermission(configKey, configData) {
    const userId = configKey.replace('user_permission.', '');
    const currentUser = this.authStore.user;
    
    // 只处理当前用户的权限更新
    if (currentUser && currentUser.id === userId) {
      // 更新用户的AI模型权限
      if (configData.ai_models_allowed) {
        this.configStore.updateUserAIModels(configData.ai_models_allowed);
      }
      
      // 更新用户配额
      if (configData.ai_quota_daily) {
        this.configStore.updateUserQuota(configData.ai_quota_daily);
      }
      
      // 更新功能权限
      if (configData.feature_permissions) {
        this.authStore.updateUserPermissions(configData.feature_permissions);
      }
      
      // 更新对话保留期
      if (configData.conversation_retention_days) {
        this.configStore.updateRetentionDays(configData.conversation_retention_days);
      }
      
      ElNotification({
        title: '权限更新',
        message: '您的使用权限已更新，部分功能可能发生变化',
        type: 'info',
        duration: 5000
      });
    }
  }

  // 通知配置更新
  notifyConfigUpdate(configKey) {
    const configNames = {
      'ai.config': 'AI模型配置',
      'app.config': '应用配置',
      'permission.config': '权限配置',
      'system.api': '系统配置',
      'monitoring.config': '监控配置'
    };
    
    const configName = configNames[configKey] || configKey;
    
    ElMessage({
      message: `${configName}已更新`,
      type: 'success',
      duration: 3000,
      showClose: true
    });
  }

  // 手动同步配置
  async syncConfigs() {
    try {
      ElMessage.info('正在同步配置...');
      await this.checkConfigUpdates();
      ElMessage.success('配置同步完成');
    } catch (error) {
      ElMessage.error('配置同步失败: ' + error.message);
    }
  }

  // 销毁配置接收器
  destroy() {
    this.stopConfigPolling();
    
    if (this.ws) {
      this.ws.close();
      this.ws = null;
    }
  }
}

// 创建全局配置接收器实例
let configReceiver = null;

export const initConfigReceiver = () => {
  if (!configReceiver) {
    configReceiver = new ConfigReceiver();
  }
  return configReceiver;
};

export const getConfigReceiver = () => {
  return configReceiver;
};

export const destroyConfigReceiver = () => {
  if (configReceiver) {
    configReceiver.destroy();
    configReceiver = null;
  }
};

export default ConfigReceiver;
