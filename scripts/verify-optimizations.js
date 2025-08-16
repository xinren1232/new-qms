/**
 * QMS AI系统优化验证脚本
 * 验证所有优化功能是否正常工作
 */

const axios = require('axios')
const fs = require('fs')
const path = require('path')

// 配置
const config = {
  frontend: 'http://localhost:8082',
  chatService: 'http://localhost:3004',
  timeout: 5000
}

// 验证结果
const results = {
  passed: 0,
  failed: 0,
  tests: []
}

// 测试函数
async function runTest(name, testFn) {
  console.log(`🧪 测试: ${name}`)
  try {
    await testFn()
    console.log(`✅ 通过: ${name}`)
    results.passed++
    results.tests.push({ name, status: 'PASS' })
  } catch (error) {
    console.log(`❌ 失败: ${name} - ${error.message}`)
    results.failed++
    results.tests.push({ name, status: 'FAIL', error: error.message })
  }
}

// 验证前端服务
async function verifyFrontendService() {
  const response = await axios.get(config.frontend, { timeout: config.timeout })
  if (response.status !== 200) {
    throw new Error('前端服务响应异常')
  }
}

// 验证聊天服务
async function verifyChatService() {
  const response = await axios.get(`${config.chatService}/health`, { timeout: config.timeout })
  if (response.status !== 200 || response.data.status !== 'healthy') {
    throw new Error('聊天服务健康检查失败')
  }
}

// 验证监控指标
async function verifyMonitoringMetrics() {
  const response = await axios.get(`${config.chatService}/metrics`, { timeout: config.timeout })
  if (response.status !== 200) {
    throw new Error('监控指标端点不可用')
  }
  
  const metrics = response.data
  if (!metrics.includes('http_request_duration_seconds')) {
    throw new Error('缺少请求持续时间指标')
  }
  
  if (!metrics.includes('active_connections')) {
    throw new Error('缺少活跃连接指标')
  }
}

// 验证主题切换组件
async function verifyThemeSwitcher() {
  const themeSwitcherPath = path.join(__dirname, '../frontend/应用端/src/components/ThemeSwitcher.vue')
  if (!fs.existsSync(themeSwitcherPath)) {
    throw new Error('主题切换组件文件不存在')
  }
  
  const content = fs.readFileSync(themeSwitcherPath, 'utf8')
  if (!content.includes('data-theme')) {
    throw new Error('主题切换组件缺少主题属性设置')
  }
}

// 验证主题样式文件
async function verifyThemeStyles() {
  const themesPath = path.join(__dirname, '../frontend/应用端/src/styles/themes.scss')
  if (!fs.existsSync(themesPath)) {
    throw new Error('主题样式文件不存在')
  }
  
  const content = fs.readFileSync(themesPath, 'utf8')
  if (!content.includes('--qms-primary') || !content.includes('[data-theme="dark"]')) {
    throw new Error('主题样式文件内容不完整')
  }
}

// 验证智能推荐组件
async function verifySmartRecommendations() {
  const recommendationsPath = path.join(__dirname, '../frontend/应用端/src/components/SmartRecommendations.vue')
  if (!fs.existsSync(recommendationsPath)) {
    throw new Error('智能推荐组件文件不存在')
  }
  
  const content = fs.readFileSync(recommendationsPath, 'utf8')
  if (!content.includes('personalizedRecommendations') || !content.includes('popularRecommendations')) {
    throw new Error('智能推荐组件功能不完整')
  }
}

// 验证聊天界面集成
async function verifyChatIntegration() {
  const chatPath = path.join(__dirname, '../frontend/应用端/src/views/ai-management/chat/optimized.vue')
  if (!fs.existsSync(chatPath)) {
    throw new Error('聊天界面文件不存在')
  }
  
  const content = fs.readFileSync(chatPath, 'utf8')
  if (!content.includes('SmartRecommendations') || !content.includes('ThemeSwitcher')) {
    throw new Error('聊天界面未正确集成新组件')
  }
}

// 验证监控中间件
async function verifyMonitoringMiddleware() {
  const middlewarePath = path.join(__dirname, '../backend/nodejs/monitoring-middleware.js')
  if (!fs.existsSync(middlewarePath)) {
    throw new Error('监控中间件文件不存在')
  }
  
  const content = fs.readFileSync(middlewarePath, 'utf8')
  if (!content.includes('prometheus') || !content.includes('createMonitoringMiddleware')) {
    throw new Error('监控中间件功能不完整')
  }
}

// 验证API响应时间
async function verifyAPIPerformance() {
  const start = Date.now()
  await axios.get(`${config.chatService}/health`, { timeout: config.timeout })
  const duration = Date.now() - start
  
  if (duration > 1000) {
    throw new Error(`API响应时间过长: ${duration}ms`)
  }
}

// 主验证函数
async function runVerification() {
  console.log('🚀 开始QMS AI系统优化验证...\n')
  
  // 基础服务验证
  await runTest('前端服务可用性', verifyFrontendService)
  await runTest('聊天服务可用性', verifyChatService)
  await runTest('API响应性能', verifyAPIPerformance)
  
  // 监控功能验证
  await runTest('监控指标端点', verifyMonitoringMetrics)
  await runTest('监控中间件', verifyMonitoringMiddleware)
  
  // 前端优化验证
  await runTest('主题切换组件', verifyThemeSwitcher)
  await runTest('主题样式文件', verifyThemeStyles)
  await runTest('智能推荐组件', verifySmartRecommendations)
  await runTest('聊天界面集成', verifyChatIntegration)
  
  // 输出结果
  console.log('\n📊 验证结果汇总:')
  console.log(`✅ 通过: ${results.passed}`)
  console.log(`❌ 失败: ${results.failed}`)
  console.log(`📈 成功率: ${((results.passed / (results.passed + results.failed)) * 100).toFixed(1)}%`)
  
  if (results.failed > 0) {
    console.log('\n❌ 失败的测试:')
    results.tests.filter(t => t.status === 'FAIL').forEach(test => {
      console.log(`   - ${test.name}: ${test.error}`)
    })
  }
  
  // 生成验证报告
  const report = {
    timestamp: new Date().toISOString(),
    summary: {
      total: results.passed + results.failed,
      passed: results.passed,
      failed: results.failed,
      successRate: ((results.passed / (results.passed + results.failed)) * 100).toFixed(1)
    },
    tests: results.tests
  }
  
  const reportPath = path.join(__dirname, '../docs/optimization-verification-report.json')
  fs.writeFileSync(reportPath, JSON.stringify(report, null, 2))
  console.log(`\n📄 验证报告已保存: ${reportPath}`)
  
  if (results.failed === 0) {
    console.log('\n🎉 所有优化功能验证通过！系统已成功优化。')
  } else {
    console.log('\n⚠️ 部分功能验证失败，请检查上述错误并修复。')
    process.exit(1)
  }
}

// 错误处理
process.on('unhandledRejection', (error) => {
  console.error('❌ 验证过程中发生未处理的错误:', error.message)
  process.exit(1)
})

// 运行验证
if (require.main === module) {
  runVerification().catch(error => {
    console.error('❌ 验证失败:', error.message)
    process.exit(1)
  })
}

module.exports = { runVerification }
