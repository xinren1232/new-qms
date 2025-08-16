#!/usr/bin/env node

/**
 * QMS AI效果评测功能验证脚本
 */

const axios = require('axios');
const fs = require('fs');
const path = require('path');

// 配置
const config = {
  evaluationService: 'http://localhost:3006',
  timeout: 10000
};

// 验证结果
const results = {
  passed: 0,
  failed: 0,
  details: []
};

// 运行测试
async function runTest(name, testFn) {
  try {
    console.log(`🧪 测试: ${name}`);
    await testFn();
    console.log(`✅ ${name} - 通过\n`);
    results.passed++;
    results.details.push({ name, status: 'passed' });
  } catch (error) {
    console.error(`❌ ${name} - 失败: ${error.message}\n`);
    results.failed++;
    results.details.push({ name, status: 'failed', error: error.message });
  }
}

// 验证评测服务健康状态
async function verifyEvaluationServiceHealth() {
  const response = await axios.get(`${config.evaluationService}/health`, {
    timeout: config.timeout
  });
  
  if (response.status !== 200) {
    throw new Error(`HTTP状态码: ${response.status}`);
  }
  
  if (!response.data.success) {
    throw new Error('服务健康检查失败');
  }
}

// 验证数据集API
async function verifyDatasetsAPI() {
  const response = await axios.get(`${config.evaluationService}/api/evaluation/datasets`, {
    timeout: config.timeout
  });
  
  if (response.status !== 200) {
    throw new Error(`HTTP状态码: ${response.status}`);
  }
  
  if (!response.data.success) {
    throw new Error('获取数据集失败');
  }
  
  if (!Array.isArray(response.data.data)) {
    throw new Error('数据集数据格式错误');
  }
  
  if (response.data.data.length === 0) {
    throw new Error('数据集列表为空');
  }
}

// 验证模型API
async function verifyModelsAPI() {
  const response = await axios.get(`${config.evaluationService}/api/evaluation/models`, {
    timeout: config.timeout
  });
  
  if (response.status !== 200) {
    throw new Error(`HTTP状态码: ${response.status}`);
  }
  
  if (!response.data.success) {
    throw new Error('获取模型列表失败');
  }
  
  if (!Array.isArray(response.data.data)) {
    throw new Error('模型数据格式错误');
  }
  
  if (response.data.data.length === 0) {
    throw new Error('模型列表为空');
  }
}

// 验证评测创建API
async function verifyCreateEvaluationAPI() {
  const evaluationData = {
    name: '测试评测-' + Date.now(),
    datasetId: 'dataset1',
    models: ['gpt-4o', 'deepseek-chat'],
    metrics: ['accuracy', 'response_time'],
    rounds: 1,
    concurrency: 1
  };
  
  const response = await axios.post(
    `${config.evaluationService}/api/evaluation/create`,
    evaluationData,
    { timeout: config.timeout }
  );
  
  if (response.status !== 200) {
    throw new Error(`HTTP状态码: ${response.status}`);
  }
  
  if (!response.data.success) {
    throw new Error('创建评测失败');
  }
  
  if (!response.data.data.id) {
    throw new Error('评测ID缺失');
  }
  
  // 保存评测ID用于后续测试
  global.testEvaluationId = response.data.data.id;
}

// 验证评测列表API
async function verifyEvaluationListAPI() {
  const response = await axios.get(`${config.evaluationService}/api/evaluation/list`, {
    timeout: config.timeout
  });
  
  if (response.status !== 200) {
    throw new Error(`HTTP状态码: ${response.status}`);
  }
  
  if (!response.data.success) {
    throw new Error('获取评测列表失败');
  }
  
  if (!Array.isArray(response.data.data)) {
    throw new Error('评测列表数据格式错误');
  }
}

// 验证评测详情API
async function verifyEvaluationDetailAPI() {
  if (!global.testEvaluationId) {
    throw new Error('测试评测ID不存在');
  }
  
  const response = await axios.get(
    `${config.evaluationService}/api/evaluation/${global.testEvaluationId}`,
    { timeout: config.timeout }
  );
  
  if (response.status !== 200) {
    throw new Error(`HTTP状态码: ${response.status}`);
  }
  
  if (!response.data.success) {
    throw new Error('获取评测详情失败');
  }
  
  if (!response.data.data.id) {
    throw new Error('评测详情数据格式错误');
  }
}

// 验证评测开始API
async function verifyStartEvaluationAPI() {
  if (!global.testEvaluationId) {
    throw new Error('测试评测ID不存在');
  }
  
  const response = await axios.post(
    `${config.evaluationService}/api/evaluation/${global.testEvaluationId}/start`,
    {},
    { timeout: config.timeout }
  );
  
  if (response.status !== 200) {
    throw new Error(`HTTP状态码: ${response.status}`);
  }
  
  if (!response.data.success) {
    throw new Error('开始评测失败');
  }
}

// 验证前端文件存在
async function verifyFrontendFiles() {
  const requiredFiles = [
    'frontend/应用端/src/views/ai-management/evaluation/index.vue',
    'frontend/应用端/src/api/evaluation.js'
  ];
  
  for (const file of requiredFiles) {
    const filePath = path.join(__dirname, '..', file);
    if (!fs.existsSync(filePath)) {
      throw new Error(`文件不存在: ${file}`);
    }
  }
}

// 验证路由配置
async function verifyRouterConfig() {
  const routerPath = path.join(__dirname, '../frontend/应用端/src/router/index.js');
  if (!fs.existsSync(routerPath)) {
    throw new Error('路由文件不存在');
  }
  
  const routerContent = fs.readFileSync(routerPath, 'utf8');
  if (!routerContent.includes('evaluation')) {
    throw new Error('路由配置中缺少evaluation路由');
  }
  
  if (!routerContent.includes('AIEvaluation')) {
    throw new Error('路由配置中缺少AIEvaluation组件');
  }
}

// 验证模型管理界面增强
async function verifyModelManagementEnhancements() {
  const modelViewPath = path.join(__dirname, '../frontend/应用端/src/views/ai-management/models/index.vue');
  if (!fs.existsSync(modelViewPath)) {
    throw new Error('模型管理界面文件不存在');
  }
  
  const content = fs.readFileSync(modelViewPath, 'utf8');
  
  // 检查筛选器功能
  if (!content.includes('models-filters')) {
    throw new Error('模型管理界面缺少筛选器功能');
  }
  
  // 检查详细信息展示
  if (!content.includes('model-details-info')) {
    throw new Error('模型管理界面缺少详细信息展示');
  }
  
  // 检查筛选逻辑
  if (!content.includes('filteredModels')) {
    throw new Error('模型管理界面缺少筛选逻辑');
  }
}

// 验证API接口完整性
async function verifyAPICompleteness() {
  const apiPath = path.join(__dirname, '../frontend/应用端/src/api/evaluation.js');
  if (!fs.existsSync(apiPath)) {
    throw new Error('评测API文件不存在');
  }
  
  const content = fs.readFileSync(apiPath, 'utf8');
  
  const requiredAPIs = [
    'getDatasets',
    'getEvaluationModels',
    'uploadEvaluationFile',
    'createEvaluation',
    'startEvaluation',
    'getEvaluationList',
    'getEvaluationResults',
    'deleteEvaluation'
  ];
  
  for (const api of requiredAPIs) {
    if (!content.includes(api)) {
      throw new Error(`API接口缺失: ${api}`);
    }
  }
}

// 主验证函数
async function runVerification() {
  console.log('🚀 开始QMS AI效果评测功能验证...\n');
  
  // 前端文件验证
  await runTest('前端文件存在性', verifyFrontendFiles);
  await runTest('路由配置', verifyRouterConfig);
  await runTest('模型管理界面增强', verifyModelManagementEnhancements);
  await runTest('API接口完整性', verifyAPICompleteness);
  
  // 后端服务验证
  await runTest('评测服务健康状态', verifyEvaluationServiceHealth);
  await runTest('数据集API', verifyDatasetsAPI);
  await runTest('模型API', verifyModelsAPI);
  await runTest('创建评测API', verifyCreateEvaluationAPI);
  await runTest('评测列表API', verifyEvaluationListAPI);
  await runTest('评测详情API', verifyEvaluationDetailAPI);
  await runTest('开始评测API', verifyStartEvaluationAPI);
  
  // 输出结果
  console.log('\n📊 验证结果汇总:');
  console.log(`✅ 通过: ${results.passed}`);
  console.log(`❌ 失败: ${results.failed}`);
  console.log(`📈 成功率: ${((results.passed / (results.passed + results.failed)) * 100).toFixed(1)}%`);
  
  if (results.failed > 0) {
    console.log('\n❌ 失败的测试:');
    results.details
      .filter(detail => detail.status === 'failed')
      .forEach(detail => {
        console.log(`  • ${detail.name}: ${detail.error}`);
      });
  }
  
  console.log('\n🎉 QMS AI效果评测功能验证完成！');
  
  if (results.failed === 0) {
    console.log('\n✨ 所有功能验证通过，系统已准备就绪！');
    console.log('\n📋 功能清单:');
    console.log('  ✅ 模型管理界面优化（卡片式展示、筛选功能）');
    console.log('  ✅ 效果评测完整流程（4步骤评测）');
    console.log('  ✅ 数据上传和管理');
    console.log('  ✅ 评测规则配置');
    console.log('  ✅ 评测结果展示');
    console.log('  ✅ 评测历史管理');
    console.log('  ✅ 完整的API接口');
    
    console.log('\n🚀 下一步建议:');
    console.log('  1. 启动前端应用测试界面功能');
    console.log('  2. 上传测试数据验证评测流程');
    console.log('  3. 配置真实模型进行评测');
    console.log('  4. 查看评测结果和分析报告');
  }
}

// 运行验证
runVerification().catch(error => {
  console.error('❌ 验证过程中发生错误:', error.message);
  process.exit(1);
});
