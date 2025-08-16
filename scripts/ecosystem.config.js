// PM2进程管理配置文件
module.exports = {
  apps: [
    // 后端服务
    {
      name: 'qms-chat-service',
      script: './backend/nodejs/chat-service.js',
      cwd: './backend/nodejs',
      instances: 1,
      autorestart: true,
      watch: ['./backend/nodejs'],
      ignore_watch: ['node_modules', 'logs', '*.log'],
      max_memory_restart: '500M',
      env: {
        NODE_ENV: 'development',
        PORT: 3004,
        DB_TYPE: 'sqlite',
        CACHE_ENABLED: 'false'
      },
      env_production: {
        NODE_ENV: 'production',
        PORT: 3004,
        DB_TYPE: 'postgresql',
        CACHE_ENABLED: 'true'
      },
      log_file: './logs/qms-chat-service.log',
      out_file: './logs/qms-chat-service-out.log',
      error_file: './logs/qms-chat-service-error.log',
      log_date_format: 'YYYY-MM-DD HH:mm:ss Z'
    },
    {
      name: 'qms-config-service',
      script: './backend/nodejs/config-center-mock.js',
      cwd: './backend/nodejs',
      instances: 1,
      autorestart: true,
      watch: ['./backend/nodejs'],
      ignore_watch: ['node_modules', 'logs', '*.log'],
      max_memory_restart: '300M',
      env: {
        NODE_ENV: 'development',
        PORT: 8081
      },
      log_file: './logs/qms-config-service.log',
      out_file: './logs/qms-config-service-out.log',
      error_file: './logs/qms-config-service-error.log',
      log_date_format: 'YYYY-MM-DD HH:mm:ss Z'
    },
    {
      name: 'qms-advanced-service',
      script: './backend/nodejs/advanced-features-service.js',
      cwd: './backend/nodejs',
      instances: 1,
      autorestart: true,
      watch: ['./backend/nodejs'],
      ignore_watch: ['node_modules', 'logs', '*.log'],
      max_memory_restart: '400M',
      env: {
        NODE_ENV: 'development',
        PORT: 3009
      },
      log_file: './logs/qms-advanced-service.log',
      out_file: './logs/qms-advanced-service-out.log',
      error_file: './logs/qms-advanced-service-error.log',
      log_date_format: 'YYYY-MM-DD HH:mm:ss Z'
    }
  ]
};
