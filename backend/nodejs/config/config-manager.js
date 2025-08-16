
// 统一配置管理
class ConfigManager {
  constructor() {
    this.config = {
      server: {
        port: process.env.PORT || 3000,
        host: process.env.HOST || 'localhost'
      },
      database: {
        path: process.env.DB_PATH || './database/chat_history.db',
        maxConnections: parseInt(process.env.DB_MAX_CONN) || 10
      },
      ai: {
        transsionApiUrl: process.env.TRANSSION_API_URL || 'http://localhost:3003',
        deepseekApiUrl: process.env.DEEPSEEK_API_URL || 'https://api.deepseek.com',
        timeout: parseInt(process.env.AI_TIMEOUT) || 30000
      },
      cache: {
        ttl: parseInt(process.env.CACHE_TTL) || 3600,
        maxSize: parseInt(process.env.CACHE_MAX_SIZE) || 1000
      }
    };
  }

  get(key) {
    return key.split('.').reduce((obj, k) => obj?.[k], this.config);
  }

  set(key, value) {
    const keys = key.split('.');
    const lastKey = keys.pop();
    const target = keys.reduce((obj, k) => obj[k] = obj[k] || {}, this.config);
    target[lastKey] = value;
  }
}

module.exports = new ConfigManager();
