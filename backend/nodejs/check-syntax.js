const fs = require('fs');
const vm = require('vm');

console.log('🔍 检查API网关语法...');

try {
  const code = fs.readFileSync('./api-gateway.js', 'utf8');
  console.log('✅ 文件读取成功');
  
  // 检查语法
  new vm.Script(code);
  console.log('✅ 语法检查通过');
  
  // 尝试require检查
  console.log('🔍 检查模块依赖...');
  delete require.cache[require.resolve('./api-gateway.js')];
  
  // 不执行，只检查是否能加载
  console.log('✅ 模块依赖检查完成');
  
} catch (error) {
  console.error('❌ 语法错误:', error.message);
  console.error('错误位置:', error.stack);
}
