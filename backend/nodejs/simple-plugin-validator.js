/**
 * 简单插件验证器
 * 使用预定义测试数据验证插件功能
 */

const http = require('http');
const fs = require('fs');
const path = require('path');

// 读取测试数据
const testSamplesPath = path.join(__dirname, '../../plugin-test-samples.json');
let testSamples = {};

try {
  const data = fs.readFileSync(testSamplesPath, 'utf8');
  testSamples = JSON.parse(data);
  console.log('✅ 测试数据加载成功');
} catch (error) {
  console.error('❌ 测试数据加载失败:', error.message);
  process.exit(1);
}

async function makeRequest(options, data) {
  return new Promise((resolve, reject) => {
    const req = http.request(options, (res) => {
      let body = '';
      res.on('data', (chunk) => body += chunk);
      res.on('end', () => {
        try {
          resolve(JSON.parse(body));
        } catch (e) {
          resolve({ success: false, error: 'Invalid JSON', body, status: res.statusCode });
        }
      });
    });
    req.on('error', reject);
    if (data) req.write(JSON.stringify(data));
    req.end();
  });
}

async function testPlugin(pluginId, testData) {
  console.log(`\n🔍 测试插件: ${testData.name}`);
  console.log(`   插件ID: ${pluginId}`);
  console.log(`   预期结果: ${testData.expected}`);
  
  const startTime = Date.now();
  
  try {
    const options = {
      hostname: 'localhost',
      port: 3005,
      path: `/api/plugins/${pluginId}/execute`,
      method: 'POST',
      headers: { 'Content-Type': 'application/json' }
    };

    const result = await makeRequest(options, { input: testData.input });
    const executionTime = Date.now() - startTime;
    
    if (result.success) {
      console.log(`✅ 成功 (${executionTime}ms)`);
      
      // 显示结果详情
      if (result.data?.result) {
        const resultData = result.data.result;
        console.log(`   结果类型: ${resultData.type || 'unknown'}`);
        
        // 根据插件类型显示特定信息
        switch (pluginId) {
          case 'statistical_analyzer':
            if (resultData.stats) {
              console.log(`   统计结果: 均值=${resultData.stats.mean?.toFixed(2)}, 标准差=${resultData.stats.std?.toFixed(2)}`);
            }
            break;
          case 'csv_parser':
            if (resultData.rows) {
              console.log(`   解析结果: ${resultData.rows}行数据`);
            }
            break;
          case 'fmea_analyzer':
            if (resultData.analysis) {
              console.log(`   FMEA分析: ${resultData.analysis.total_failure_modes || 0}个失效模式`);
            }
            break;
          case 'spc_controller':
            if (resultData.control_limits) {
              console.log(`   SPC控制: UCL=${resultData.control_limits.ucl?.toFixed(2)}, LCL=${resultData.control_limits.lcl?.toFixed(2)}`);
            }
            break;
          default:
            if (resultData.summary) {
              console.log(`   摘要: ${resultData.summary.substring(0, 100)}...`);
            }
        }
      }
      
      return { pluginId, status: 'success', executionTime, result: result.data };
    } else {
      console.log(`❌ 失败: ${result.message || result.error}`);
      return { pluginId, status: 'failed', executionTime, error: result.message || result.error };
    }
  } catch (error) {
    const executionTime = Date.now() - startTime;
    console.log(`💥 错误: ${error.message}`);
    return { pluginId, status: 'error', executionTime, error: error.message };
  }
}

async function runValidation() {
  console.log('🧪 开始插件功能验证...\n');
  console.log(`📊 共有 ${Object.keys(testSamples).length} 个插件需要测试`);
  
  const results = [];
  const pluginIds = Object.keys(testSamples);
  
  // 按类型分组测试
  const documentProcessors = pluginIds.filter(id => 
    ['pdf_parser', 'csv_parser', 'json_parser', 'xml_parser', 'xlsx_parser', 'docx_parser', 'excel_analyzer'].includes(id)
  );
  const dataAnalyzers = pluginIds.filter(id => 
    ['statistical_analyzer', 'spc_controller', 'data_cleaner', 'anomaly_detector'].includes(id)
  );
  const aiProcessors = pluginIds.filter(id => 
    ['defect_detector', 'ocr_reader', 'text_summarizer'].includes(id)
  );
  const qualityTools = pluginIds.filter(id => 
    ['fmea_analyzer', 'msa_calculator'].includes(id)
  );
  const externalConnectors = pluginIds.filter(id => 
    ['api_connector', 'database_query'].includes(id)
  );

  // 测试文档处理器
  if (documentProcessors.length > 0) {
    console.log('\n📄 测试文档处理器:');
    console.log('='.repeat(40));
    for (const pluginId of documentProcessors) {
      if (testSamples[pluginId]) {
        const result = await testPlugin(pluginId, testSamples[pluginId]);
        results.push(result);
        await new Promise(resolve => setTimeout(resolve, 500));
      }
    }
  }

  // 测试数据分析器
  if (dataAnalyzers.length > 0) {
    console.log('\n📊 测试数据分析器:');
    console.log('='.repeat(40));
    for (const pluginId of dataAnalyzers) {
      if (testSamples[pluginId]) {
        const result = await testPlugin(pluginId, testSamples[pluginId]);
        results.push(result);
        await new Promise(resolve => setTimeout(resolve, 500));
      }
    }
  }

  // 测试AI处理器
  if (aiProcessors.length > 0) {
    console.log('\n🤖 测试AI处理器:');
    console.log('='.repeat(40));
    for (const pluginId of aiProcessors) {
      if (testSamples[pluginId]) {
        const result = await testPlugin(pluginId, testSamples[pluginId]);
        results.push(result);
        await new Promise(resolve => setTimeout(resolve, 500));
      }
    }
  }

  // 测试质量工具
  if (qualityTools.length > 0) {
    console.log('\n🎯 测试质量工具:');
    console.log('='.repeat(40));
    for (const pluginId of qualityTools) {
      if (testSamples[pluginId]) {
        const result = await testPlugin(pluginId, testSamples[pluginId]);
        results.push(result);
        await new Promise(resolve => setTimeout(resolve, 500));
      }
    }
  }

  // 测试外部连接器
  if (externalConnectors.length > 0) {
    console.log('\n🔗 测试外部连接器:');
    console.log('='.repeat(40));
    for (const pluginId of externalConnectors) {
      if (testSamples[pluginId]) {
        const result = await testPlugin(pluginId, testSamples[pluginId]);
        results.push(result);
        await new Promise(resolve => setTimeout(resolve, 500));
      }
    }
  }

  // 生成测试报告
  generateReport(results);
}

function generateReport(results) {
  console.log('\n📊 插件验证报告:');
  console.log('='.repeat(60));
  
  const successful = results.filter(r => r.status === 'success');
  const failed = results.filter(r => r.status === 'failed');
  const errors = results.filter(r => r.status === 'error');
  
  console.log(`✅ 成功: ${successful.length}个`);
  console.log(`❌ 失败: ${failed.length}个`);
  console.log(`💥 错误: ${errors.length}个`);
  console.log(`📈 成功率: ${(successful.length / results.length * 100).toFixed(1)}%`);
  
  // 性能统计
  if (results.length > 0) {
    const avgTime = results.reduce((sum, r) => sum + r.executionTime, 0) / results.length;
    const maxTime = Math.max(...results.map(r => r.executionTime));
    const minTime = Math.min(...results.map(r => r.executionTime));
    
    console.log('\n⏱️  性能统计:');
    console.log(`   平均执行时间: ${avgTime.toFixed(0)}ms`);
    console.log(`   最长执行时间: ${maxTime}ms`);
    console.log(`   最短执行时间: ${minTime}ms`);
  }
  
  if (failed.length > 0) {
    console.log('\n❌ 失败的插件:');
    failed.forEach(r => console.log(`   - ${r.pluginId}: ${r.error}`));
  }
  
  if (errors.length > 0) {
    console.log('\n💥 错误的插件:');
    errors.forEach(r => console.log(`   - ${r.pluginId}: ${r.error}`));
  }

  console.log('\n🎯 验证结论:');
  if (successful.length === results.length) {
    console.log('   🎉 所有插件功能正常，系统运行良好！');
    console.log('   📈 插件生态系统已准备就绪');
  } else if (successful.length > results.length * 0.8) {
    console.log('   ✅ 大部分插件功能正常，少数需要调整');
    console.log('   🔧 建议优先修复失败的插件');
  } else {
    console.log('   ⚠️  多个插件存在问题，需要系统性检查');
    console.log('   🛠️  建议检查服务配置和依赖');
  }
}

// 运行验证
runValidation().catch(console.error);
