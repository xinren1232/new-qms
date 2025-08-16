module.exports = {
  apps: [
    {
      name: 'qms-config',
      cwd: __dirname,
      script: 'lightweight-config-service.js',
      env: {
        PORT: 3003
      },
      instances: 1,
      exec_mode: 'fork',
      autorestart: true,
      max_restarts: 5,
      out_file: '/var/log/qms-config.out.log',
      error_file: '/var/log/qms-config.err.log',
      log_date_format: 'YYYY-MM-DD HH:mm:ss'
    },
    {
      name: 'qms-chat',
      cwd: __dirname,
      script: 'chat-service.js',
      env: {
        PORT: 3004,
        CONFIG_SERVICE_URL: 'http://localhost:3003'
      },
      instances: 1,
      exec_mode: 'fork',
      autorestart: true,
      max_restarts: 5,
      out_file: '/var/log/qms-chat.out.log',
      error_file: '/var/log/qms-chat.err.log',
      log_date_format: 'YYYY-MM-DD HH:mm:ss'
    },
    {
      name: 'qms-auth',
      cwd: __dirname,
      script: 'auth-service.js',
      env: {
        AUTH_SERVICE_PORT: 8084
      },
      instances: 1,
      exec_mode: 'fork',
      autorestart: true,
      max_restarts: 5,
      max_memory_restart: '300M',
      out_file: '/var/log/qms-auth.out.log',
      error_file: '/var/log/qms-auth.err.log',
      log_date_format: 'YYYY-MM-DD HH:mm:ss'
    },
    {
      name: 'qms-gateway',
      cwd: __dirname,
      script: 'api-gateway.js',
      env: {
        PORT: 8085
      },
      instances: 1,
      exec_mode: 'fork',
      autorestart: true,
      max_restarts: 5,
      max_memory_restart: '300M',
      out_file: '/var/log/qms-gateway.out.log',
      error_file: '/var/log/qms-gateway.err.log',
      log_date_format: 'YYYY-MM-DD HH:mm:ss'
    }
  ]
};

