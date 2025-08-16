/**
 * 聊天服务调试启动器
 * 逐步加载模块，定位启动失败的具体原因
 */

console.log('🔍 开始调试聊天服务启动过程...\n');

try {
  console.log('1. 加载基础模块...');
  const express = require('express');
  const cors = require('cors');
  const { v4: uuidv4 } = require('uuid');
  console.log('✅ 基础模块加载成功');

  console.log('2. 加载安全模块...');
  const helmet = require('helmet');
  const rateLimit = require('express-rate-limit');
  console.log('✅ 安全模块加载成功');

  console.log('3. 加载数据库适配器...');
  const DatabaseAdapter = require('./database/database-adapter');
  console.log('✅ 数据库适配器加载成功');

  console.log('4. 加载监控服务...');
  const MonitoringService = require('./services/monitoring-service');
  console.log('✅ 监控服务加载成功');

  console.log('5. 加载Prometheus指标...');
  const PrometheusMetrics = require('./services/prometheus-metrics');
  console.log('✅ Prometheus指标加载成功');

  console.log('6. 加载告警管理器...');
  const AlertManager = require('./services/alert-manager');
  console.log('✅ 告警管理器加载成功');

  console.log('7. 加载配置客户端...');
  const ConfigClient = require('./config/config-client');
  console.log('✅ 配置客户端加载成功');

  console.log('8. 加载Redis缓存服务...');
  const RedisCacheService = require('./services/redis-cache-service');
  console.log('✅ Redis缓存服务加载成功');

  console.log('9. 加载插件管理器...');
  const PluginManager = require('./services/plugin-manager');
  console.log('✅ 插件管理器加载成功');

  console.log('10. 加载工作流执行引擎...');
  const WorkflowExecutionEngine = require('./services/workflow-execution-engine');
  console.log('✅ 工作流执行引擎加载成功');

  console.log('11. 加载智能模型管理器...');
  const IntelligentModelManager = require('./services/intelligent-model-manager');
  console.log('✅ 智能模型管理器加载成功');

  console.log('12. 加载安全工具...');
  const { JWTManager, InputValidator, SecurityMiddleware } = require('./utils/security');
  console.log('✅ 安全工具加载成功');

  console.log('13. 加载HTTP客户端...');
  const { httpClient } = require('./utils/http-client');
  console.log('✅ HTTP客户端加载成功');

  console.log('14. 加载错误处理中间件...');
  const {
    QMSError,
    ErrorTypes,
    asyncErrorHandler,
    notFoundHandler,
    globalErrorHandler,
    setupGlobalHandlers,
    errorStatsHandler,
    logger
  } = require('./middleware/error-handler');
  console.log('✅ 错误处理中间件加载成功');

  console.log('15. 加载输入验证中间件...');
  const {
    validators,
    sanitizeInput,
    validateRateLimit
  } = require('./middleware/input-validation');
  console.log('✅ 输入验证中间件加载成功');

  console.log('16. 加载安全配置...');
  const securityConfig = require('./config/security-config');
  console.log('✅ 安全配置加载成功');

  console.log('17. 加载环境配置...');
  const path = require('path');
  require('dotenv').config({ path: path.join(__dirname, '../../.env') });
  console.log('✅ 环境配置加载成功');

  console.log('18. 创建Express应用...');
  const app = express();
  console.log('✅ Express应用创建成功');

  console.log('19. 初始化监控服务...');
  const monitoring = new MonitoringService();
  console.log('✅ 监控服务初始化成功');

  console.log('20. 初始化Prometheus指标...');
  const prometheus = new PrometheusMetrics();
  console.log('✅ Prometheus指标初始化成功');

  console.log('21. 初始化告警管理器...');
  const alertManager = new AlertManager();
  console.log('✅ 告警管理器初始化成功');

  console.log('22. 初始化配置客户端...');
  const configClient = new ConfigClient();
  console.log('✅ 配置客户端初始化成功');

  console.log('23. 初始化数据库适配器...');
  const dbAdapter = new DatabaseAdapter();
  console.log('✅ 数据库适配器初始化成功');

  console.log('24. 测试数据库连接...');
  dbAdapter.initialize().then(() => {
    console.log('✅ 数据库连接测试成功');
    
    console.log('25. 设置基础中间件...');
    app.use(helmet());
    app.use(cors());
    app.use(express.json({ limit: '10mb' }));
    app.use(express.urlencoded({ extended: true, limit: '10mb' }));
    console.log('✅ 基础中间件设置成功');

    console.log('26. 设置健康检查端点...');
    app.get('/health', (req, res) => {
      res.json({
        success: true,
        service: 'QMS-AI聊天服务',
        status: 'healthy',
        timestamp: new Date().toISOString(),
        version: '2.0.0'
      });
    });
    console.log('✅ 健康检查端点设置成功');

    console.log('27. 启动服务器...');
    const PORT = process.env.CHAT_SERVICE_PORT || 3004;
    const server = app.listen(PORT, () => {
      console.log(`\n🎉 聊天服务调试启动成功！`);
      console.log(`📍 服务地址: http://localhost:${PORT}`);
      console.log(`🔍 健康检查: http://localhost:${PORT}/health`);
      console.log(`\n✅ 所有模块加载和初始化完成！`);
    });

    // 错误处理
    server.on('error', (error) => {
      console.error('❌ 服务器启动错误:', error);
      process.exit(1);
    });

  }).catch((error) => {
    console.error('❌ 数据库连接失败:', error);
    process.exit(1);
  });

} catch (error) {
  console.error('❌ 模块加载失败:', error);
  console.error('详细错误信息:', error.stack);
  process.exit(1);
}

// 全局错误处理
process.on('uncaughtException', (error) => {
  console.error('❌ 未捕获的异常:', error);
  process.exit(1);
});

process.on('unhandledRejection', (reason, promise) => {
  console.error('❌ 未处理的Promise拒绝:', reason);
  process.exit(1);
});
