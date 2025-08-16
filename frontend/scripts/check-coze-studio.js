#!/usr/bin/env node

/**
 * Coze Studio 功能检查脚本
 * 检查所有组件和功能的完整性
 */

const fs = require('fs');
const path = require('path');

// 颜色输出
const colors = {
  reset: '\x1b[0m',
  bright: '\x1b[1m',
  red: '\x1b[31m',
  green: '\x1b[32m',
  yellow: '\x1b[33m',
  blue: '\x1b[34m',
  magenta: '\x1b[35m',
  cyan: '\x1b[36m'
};

function log(message, color = 'reset') {
  console.log(`${colors[color]}${message}${colors.reset}`);
}

function checkFileExists(filePath, description) {
  const fullPath = path.resolve(filePath);
  const exists = fs.existsSync(fullPath);
  
  if (exists) {
    log(`✅ ${description}`, 'green');
    return true;
  } else {
    log(`❌ ${description} - 文件不存在: ${filePath}`, 'red');
    return false;
  }
}

function checkDirectoryExists(dirPath, description) {
  const fullPath = path.resolve(dirPath);
  const exists = fs.existsSync(fullPath) && fs.statSync(fullPath).isDirectory();
  
  if (exists) {
    log(`✅ ${description}`, 'green');
    return true;
  } else {
    log(`❌ ${description} - 目录不存在: ${dirPath}`, 'red');
    return false;
  }
}

function checkFileContent(filePath, searchText, description) {
  try {
    const content = fs.readFileSync(path.resolve(filePath), 'utf8');
    const found = content.includes(searchText);
    
    if (found) {
      log(`✅ ${description}`, 'green');
      return true;
    } else {
      log(`❌ ${description} - 未找到: ${searchText}`, 'red');
      return false;
    }
  } catch (error) {
    log(`❌ ${description} - 读取文件失败: ${error.message}`, 'red');
    return false;
  }
}

async function main() {
  log('\n🔍 Coze Studio 功能完整性检查', 'cyan');
  log('=' * 50, 'cyan');
  
  let totalChecks = 0;
  let passedChecks = 0;
  
  // 1. 检查核心文件结构
  log('\n📁 检查文件结构...', 'blue');
  
  const coreFiles = [
    ['frontend/应用端/src/views/coze-studio/index.vue', 'Coze Studio 主界面'],
    ['frontend/应用端/src/views/coze-studio/components/CozeAgentBuilder.vue', 'AI Agent Builder 组件'],
    ['frontend/应用端/src/views/coze-studio/components/CozeWorkflowBuilder.vue', 'Workflow Designer 组件'],
    ['frontend/应用端/src/views/coze-studio/components/CozeKnowledgeManager.vue', 'Knowledge Manager 组件'],
    ['frontend/应用端/src/views/coze-studio/components/CozePluginManager.vue', 'Plugin Manager 组件'],
    ['frontend/应用端/src/views/coze-studio/components/CozeProjectCreator.vue', 'Project Creator 组件'],
    ['frontend/应用端/src/views/coze-studio/components/CozeSettings.vue', 'Settings 组件'],
    ['frontend/应用端/public/test-coze-studio.html', '功能测试页面'],
    ['scripts/start-coze-studio.bat', 'Coze Studio 启动脚本'],
    ['docs/Coze-Studio-User-Guide.md', 'Coze Studio 用户指南']
  ];
  
  coreFiles.forEach(([file, desc]) => {
    totalChecks++;
    if (checkFileExists(file, desc)) {
      passedChecks++;
    }
  });
  
  // 2. 检查路由配置
  log('\n🛣️ 检查路由配置...', 'blue');
  
  const routeChecks = [
    ['frontend/应用端/src/router/index.js', 'coze-studio', '路由配置'],
    ['frontend/应用端/src/router/index.js', 'CozeStudio', '路由组件名'],
    ['frontend/应用端/src/router/index.js', '@/views/coze-studio/index.vue', '路由组件路径']
  ];
  
  routeChecks.forEach(([file, search, desc]) => {
    totalChecks++;
    if (checkFileContent(file, search, desc)) {
      passedChecks++;
    }
  });
  
  // 3. 检查组件导入
  log('\n📦 检查组件导入...', 'blue');
  
  const importChecks = [
    ['frontend/应用端/src/views/coze-studio/index.vue', 'CozeAgentBuilder', 'Agent Builder 导入'],
    ['frontend/应用端/src/views/coze-studio/index.vue', 'CozeWorkflowBuilder', 'Workflow Builder 导入'],
    ['frontend/应用端/src/views/coze-studio/index.vue', 'CozeKnowledgeManager', 'Knowledge Manager 导入'],
    ['frontend/应用端/src/views/coze-studio/index.vue', 'CozePluginManager', 'Plugin Manager 导入'],
    ['frontend/应用端/src/views/coze-studio/index.vue', 'CozeProjectCreator', 'Project Creator 导入'],
    ['frontend/应用端/src/views/coze-studio/index.vue', 'CozeSettings', 'Settings 导入']
  ];
  
  importChecks.forEach(([file, search, desc]) => {
    totalChecks++;
    if (checkFileContent(file, search, desc)) {
      passedChecks++;
    }
  });
  
  // 4. 检查Vue组件结构
  log('\n🔧 检查Vue组件结构...', 'blue');
  
  const componentChecks = [
    ['frontend/应用端/src/views/coze-studio/components/CozeAgentBuilder.vue', '<template>', 'Agent Builder 模板'],
    ['frontend/应用端/src/views/coze-studio/components/CozeAgentBuilder.vue', '<script setup>', 'Agent Builder 脚本'],
    ['frontend/应用端/src/views/coze-studio/components/CozeWorkflowBuilder.vue', 'workflow-canvas', 'Workflow 画布'],
    ['frontend/应用端/src/views/coze-studio/components/CozeKnowledgeManager.vue', 'knowledge-manager', 'Knowledge 管理器'],
    ['frontend/应用端/src/views/coze-studio/components/CozePluginManager.vue', 'plugin-manager', 'Plugin 管理器']
  ];
  
  componentChecks.forEach(([file, search, desc]) => {
    totalChecks++;
    if (checkFileContent(file, search, desc)) {
      passedChecks++;
    }
  });
  
  // 5. 检查Element Plus图标
  log('\n🎨 检查图标导入...', 'blue');
  
  const iconChecks = [
    ['frontend/应用端/src/views/coze-studio/index.vue', '@element-plus/icons-vue', '主界面图标导入'],
    ['frontend/应用端/src/views/coze-studio/components/CozeAgentBuilder.vue', '@element-plus/icons-vue', 'Agent Builder 图标导入'],
    ['frontend/应用端/src/views/coze-studio/components/CozeWorkflowBuilder.vue', '@element-plus/icons-vue', 'Workflow Builder 图标导入']
  ];
  
  iconChecks.forEach(([file, search, desc]) => {
    totalChecks++;
    if (checkFileContent(file, search, desc)) {
      passedChecks++;
    }
  });
  
  // 6. 检查样式文件
  log('\n💄 检查样式配置...', 'blue');
  
  const styleChecks = [
    ['frontend/应用端/src/views/coze-studio/index.vue', '<style lang="scss" scoped>', '主界面样式'],
    ['frontend/应用端/src/views/coze-studio/components/CozeAgentBuilder.vue', '.coze-agent-builder', 'Agent Builder 样式'],
    ['frontend/应用端/src/views/coze-studio/components/CozeWorkflowBuilder.vue', '.coze-workflow-builder', 'Workflow Builder 样式']
  ];
  
  styleChecks.forEach(([file, search, desc]) => {
    totalChecks++;
    if (checkFileContent(file, search, desc)) {
      passedChecks++;
    }
  });
  
  // 7. 检查功能特性
  log('\n⚡ 检查功能特性...', 'blue');
  
  const featureChecks = [
    ['frontend/应用端/src/views/coze-studio/components/CozeAgentBuilder.vue', 'agentConfig', 'Agent 配置功能'],
    ['frontend/应用端/src/views/coze-studio/components/CozeWorkflowBuilder.vue', 'workflowNodes', 'Workflow 节点功能'],
    ['frontend/应用端/src/views/coze-studio/components/CozeKnowledgeManager.vue', 'knowledgeItems', 'Knowledge 项目功能'],
    ['frontend/应用端/src/views/coze-studio/components/CozePluginManager.vue', 'installedPlugins', 'Plugin 安装功能'],
    ['frontend/应用端/src/views/coze-studio/components/CozeProjectCreator.vue', 'projectTypes', 'Project 类型功能']
  ];
  
  featureChecks.forEach(([file, search, desc]) => {
    totalChecks++;
    if (checkFileContent(file, search, desc)) {
      passedChecks++;
    }
  });
  
  // 输出检查结果
  log('\n📊 检查结果汇总', 'cyan');
  log('=' * 50, 'cyan');
  
  const successRate = ((passedChecks / totalChecks) * 100).toFixed(1);
  
  log(`总检查项: ${totalChecks}`, 'blue');
  log(`通过检查: ${passedChecks}`, 'green');
  log(`失败检查: ${totalChecks - passedChecks}`, 'red');
  log(`成功率: ${successRate}%`, successRate >= 90 ? 'green' : successRate >= 70 ? 'yellow' : 'red');
  
  if (successRate >= 90) {
    log('\n🎉 Coze Studio 功能完整性检查通过！', 'green');
    log('✨ 系统已准备就绪，可以正常使用', 'green');
  } else if (successRate >= 70) {
    log('\n⚠️ Coze Studio 基本功能正常，但存在一些问题', 'yellow');
    log('🔧 建议修复失败的检查项以获得最佳体验', 'yellow');
  } else {
    log('\n❌ Coze Studio 存在严重问题', 'red');
    log('🚨 请修复失败的检查项后再使用', 'red');
  }
  
  log('\n🚀 启动建议:', 'blue');
  log('1. 运行: scripts/start-coze-studio.bat', 'cyan');
  log('2. 访问: http://localhost:8081/test-coze-studio.html', 'cyan');
  log('3. 测试: http://localhost:8081/ai-management/coze-studio', 'cyan');
  
  return successRate >= 70;
}

// 运行检查
if (require.main === module) {
  main().then(success => {
    process.exit(success ? 0 : 1);
  }).catch(error => {
    log(`\n💥 检查过程中发生错误: ${error.message}`, 'red');
    process.exit(1);
  });
}

module.exports = { main };
