/**
 * 检查插件返回结果的详细结构
 */

const axios = require('axios');

const COZE_STUDIO_URL = 'http://localhost:3005';
const headers = {
  'Content-Type': 'application/json',
  'Authorization': 'Bearer test_token'
};

async function inspectPluginResults() {
  console.log('🔍 检查插件返回结果的详细结构...\n');

  // 测试几个关键插件
  const testCases = [
    {
      pluginId: 'statistical_analyzer',
      name: '统计分析器',
      input: {
        data: [45.2, 44.8, 45.5, 44.9, 45.1, 45.3, 44.7, 45.0, 45.4, 44.6],
        unit: 'HRC',
        target: 45.0,
        usl: 47.0,
        lsl: 43.0
      }
    },
    {
      pluginId: 'spc_controller',
      name: 'SPC控制图',
      input: {
        chart_type: 'x_r',
        subgroup_size: 5,
        measurements: [
          [10.02, 10.01, 10.03, 9.99, 10.00],
          [9.98, 9.99, 9.97, 10.01, 10.00],
          [10.05, 10.04, 10.06, 10.02, 10.03]
        ]
      }
    },
    {
      pluginId: 'fmea_analyzer',
      name: 'FMEA分析器',
      input: {
        process: '精密加工',
        components: [
          {
            name: '主轴电机',
            failure_modes: [
              { mode: '过热', severity: 8, occurrence: 3, detection: 4, rpn: 96 }
            ]
          }
        ]
      }
    }
  ];

  for (const testCase of testCases) {
    console.log(`📊 检查插件: ${testCase.name} (${testCase.pluginId})`);
    console.log('='.repeat(60));

    try {
      const response = await axios.post(
        `${COZE_STUDIO_URL}/api/plugins/${testCase.pluginId}/execute`,
        { input: testCase.input, options: {} },
        { headers, timeout: 10000 }
      );

      if (response.data.success) {
        console.log('✅ 执行成功');
        console.log('\n📋 完整响应结构:');
        console.log(JSON.stringify(response.data, null, 2));
        
        // 分析数据结构
        const result = response.data.data?.result || response.data.data;
        if (result && typeof result === 'object') {
          console.log('\n🔍 结果数据分析:');
          
          // 检查data字段
          if (result.data) {
            console.log('📦 data字段内容:');
            console.log(JSON.stringify(result.data, null, 2));
          }
          
          // 检查statistics字段
          if (result.statistics) {
            console.log('📈 statistics字段内容:');
            console.log(JSON.stringify(result.statistics, null, 2));
          }
          
          // 检查metadata字段
          if (result.metadata) {
            console.log('ℹ️ metadata字段内容:');
            console.log(JSON.stringify(result.metadata, null, 2));
          }
        }
      } else {
        console.log(`❌ 执行失败: ${response.data.message}`);
      }

    } catch (error) {
      console.log(`❌ 请求失败: ${error.message}`);
    }

    console.log('\n' + '='.repeat(60) + '\n');
  }

  console.log('🎯 检查完成！');
}

// 运行检查
inspectPluginResults().catch(console.error);
