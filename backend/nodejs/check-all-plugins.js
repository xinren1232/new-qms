/**
 * 检查所有插件的功能状态
 */

const axios = require('axios');

// 测试配置
const COZE_STUDIO_URL = 'http://localhost:3005';

// 测试数据集
const testData = {
  // 文档解析类插件测试数据
  pdf_parser: {
    text: '这是一个PDF文档内容示例。包含产品质量检测报告、数据分析结果、图表和表格信息。本文档用于测试PDF解析功能的完整性和准确性。'
  },
  docx_parser: {
    base64: Buffer.from('这是一个DOCX文档内容示例。包含标题、段落、列表等格式化内容。').toString('base64'),
    text: '这是一个DOCX文档内容示例。包含标题、段落、列表等格式化内容。'
  },
  xlsx_parser: {
    csv: '产品编号,检测项目,测量值,标准值,结果\nP001,硬度,45.2,45±2,合格\nP002,重量,2.48,2.5±0.1,合格\nP003,尺寸,10.05,10±0.05,合格'
  },
  csv_parser: {
    csv: '批次号,检测时间,检测员,硬度,尺寸,重量,结果\nB20240115001,09:30,张三,45.2,10.05,2.48,合格\nB20240115002,10:15,李四,44.8,9.98,2.52,合格'
  },
  json_parser: {
    text: JSON.stringify({
      "batch_id": "B20240115001",
      "product": "精密零件A",
      "measurements": [
        {"item": "硬度", "value": 45.2, "unit": "HRC", "result": "合格"},
        {"item": "尺寸", "value": 10.05, "unit": "mm", "result": "合格"}
      ]
    })
  },
  xml_parser: {
    text: `<?xml version="1.0" encoding="UTF-8"?>
<quality_report>
  <batch_id>B20240115001</batch_id>
  <product_name>精密零件A</product_name>
  <measurements>
    <measurement>
      <item>硬度</item>
      <value>45.2</value>
      <result>合格</result>
    </measurement>
  </measurements>
</quality_report>`
  },
  
  // 数据分析类插件测试数据
  statistical_analyzer: {
    data: [45.2, 44.8, 45.5, 44.9, 45.1, 45.3, 44.7, 45.0, 45.4, 44.6],
    unit: 'HRC',
    target: 45.0,
    usl: 47.0,
    lsl: 43.0
  },
  excel_analyzer: {
    csv: '月份,销售额,目标\n1月,10000,12000\n2月,11000,12000\n3月,13000,12000'
  },
  spc_controller: {
    chart_type: 'x_r',
    subgroup_size: 5,
    measurements: [
      [10.02, 10.01, 10.03, 9.99, 10.00],
      [9.98, 9.99, 9.97, 10.01, 10.00],
      [10.05, 10.04, 10.06, 10.02, 10.03]
    ]
  },
  fmea_analyzer: {
    process: '精密加工',
    components: [
      {
        name: '主轴电机',
        function: '提供切削动力',
        failure_modes: [
          { mode: '过热', cause: '冷却系统故障', effect: '加工精度下降', severity: 8, occurrence: 3, detection: 4, rpn: 96 }
        ]
      }
    ]
  },
  msa_calculator: {
    study_name: '精密零件尺寸测量',
    measurement_unit: 'mm',
    tolerance: 0.1,
    parts: ['零件1', '零件2', '零件3'],
    operators: ['操作员A', '操作员B'],
    measurements: [
      [[10.02, 10.01, 10.03], [10.00, 10.02, 10.01]],
      [[9.98, 9.99, 9.97], [9.99, 9.98, 10.00]],
      [[10.05, 10.04, 10.06], [10.03, 10.05, 10.04]]
    ]
  },
  data_cleaner: {
    raw_data: [
      { id: 1, measurement: 45.2, operator: '张三', status: 'valid' },
      { id: 2, measurement: null, operator: '李四', status: 'invalid' },
      { id: 3, measurement: 999.9, operator: '王五', status: 'outlier' }
    ],
    cleaning_rules: {
      remove_nulls: true,
      remove_outliers: true,
      outlier_method: 'iqr'
    }
  },
  anomaly_detector: {
    measurements: [45.2, 44.8, 45.5, 44.9, 45.1, 45.3, 44.7, 45.0, 48.2, 45.4],
    detection_method: 'statistical',
    sensitivity: 0.95
  },
  text_summarizer: {
    text: '本次质量检测报告涵盖了2024年1月15日生产的精密零件A批次的全面检测结果。检测项目包括硬度测试、尺寸精度测量、表面粗糙度检测和重量测量等四个关键指标。硬度测试结果显示，测量值为45.2 HRC，符合标准要求。'
  },
  
  // 图像处理类插件测试数据
  ocr_reader: {
    image_base64: 'data:image/png;base64,iVBORw0KGgoAAAANSUhEUgAAAAEAAAABCAYAAAAfFcSJAAAADUlEQVR42mNkYPhfDwAChwGA60e6kgAAAABJRU5ErkJggg==',
    expected_text: '产品编号: P001\n检测结果: 合格'
  },
  defect_detector: {
    image_base64: 'data:image/png;base64,iVBORw0KGgoAAAANSUhEUgAAAAEAAAABCAYAAAAfFcSJAAAADUlEQVR42mNkYPhfDwAChwGA60e6kgAAAABJRU5ErkJggg==',
    defect_types: ['划痕', '凹陷', '污渍'],
    inspection_area: { x: 0, y: 0, width: 100, height: 100 }
  },
  
  // 连接器类插件测试数据
  api_connector: {
    url: 'https://httpbin.org/get',
    method: 'GET',
    headers: { 'Content-Type': 'application/json' }
  },
  database_query: {
    query: 'SELECT COUNT(*) as total FROM products',
    database: 'test'
  }
};

async function checkAllPlugins() {
  console.log('🔍 开始检查所有插件功能状态...\n');

  try {
    // 1. 获取插件列表
    console.log('1️⃣ 获取插件列表...');
    const pluginsResponse = await axios.get(`${COZE_STUDIO_URL}/api/plugins`);
    const plugins = pluginsResponse.data.data?.plugins || [];
    console.log(`✅ 发现 ${plugins.length} 个插件\n`);

    // 2. 逐个测试插件
    const results = [];
    const headers = {
      'Content-Type': 'application/json',
      'Authorization': 'Bearer test_token'
    };

    for (let i = 0; i < plugins.length; i++) {
      const plugin = plugins[i];
      const pluginId = plugin.id;
      
      console.log(`${i + 1}/${plugins.length} 测试插件: ${plugin.name} (${pluginId})`);
      
      try {
        // 获取测试数据
        const inputData = testData[pluginId] || { text: '测试数据' };
        
        // 执行插件
        const startTime = Date.now();
        const response = await axios.post(
          `${COZE_STUDIO_URL}/api/plugins/${pluginId}/execute`,
          { input: inputData, options: {} },
          { headers, timeout: 10000 }
        );
        const duration = Date.now() - startTime;

        // 分析结果
        const success = response.data.success;
        const hasResult = !!(response.data.data?.result || response.data.data);
        const resultSize = JSON.stringify(response.data.data || {}).length;

        results.push({
          id: pluginId,
          name: plugin.name,
          status: success ? '✅ 正常' : '❌ 失败',
          duration: `${duration}ms`,
          hasResult,
          resultSize: `${(resultSize / 1024).toFixed(1)}KB`,
          error: success ? null : (response.data.message || '未知错误')
        });

        console.log(`   ${success ? '✅' : '❌'} ${success ? '正常' : '失败'} (${duration}ms)`);
        
      } catch (error) {
        results.push({
          id: pluginId,
          name: plugin.name,
          status: '❌ 错误',
          duration: 'N/A',
          hasResult: false,
          resultSize: '0KB',
          error: error.message
        });
        
        console.log(`   ❌ 错误: ${error.message}`);
      }
    }

    // 3. 生成报告
    console.log('\n📊 插件功能检查报告');
    console.log('='.repeat(80));
    
    const successCount = results.filter(r => r.status === '✅ 正常').length;
    const failureCount = results.filter(r => r.status.includes('❌')).length;
    
    console.log(`总插件数: ${results.length}`);
    console.log(`正常工作: ${successCount} (${(successCount/results.length*100).toFixed(1)}%)`);
    console.log(`存在问题: ${failureCount} (${(failureCount/results.length*100).toFixed(1)}%)`);
    console.log('');

    // 详细结果表格
    console.log('详细结果:');
    console.log('插件ID'.padEnd(20) + '插件名称'.padEnd(15) + '状态'.padEnd(8) + '耗时'.padEnd(10) + '结果大小'.padEnd(10) + '错误信息');
    console.log('-'.repeat(80));
    
    results.forEach(result => {
      const line = result.id.padEnd(20) + 
                   result.name.padEnd(15) + 
                   result.status.padEnd(8) + 
                   result.duration.padEnd(10) + 
                   result.resultSize.padEnd(10) + 
                   (result.error || '');
      console.log(line);
    });

    // 4. 问题分析
    const problemPlugins = results.filter(r => r.status.includes('❌'));
    if (problemPlugins.length > 0) {
      console.log('\n🚨 存在问题的插件:');
      problemPlugins.forEach(plugin => {
        console.log(`- ${plugin.name} (${plugin.id}): ${plugin.error}`);
      });
    }

    // 5. 性能分析
    const workingPlugins = results.filter(r => r.status === '✅ 正常');
    if (workingPlugins.length > 0) {
      const avgDuration = workingPlugins.reduce((sum, p) => sum + parseInt(p.duration), 0) / workingPlugins.length;
      console.log(`\n⚡ 平均响应时间: ${avgDuration.toFixed(0)}ms`);
      
      const slowPlugins = workingPlugins.filter(p => parseInt(p.duration) > 1000);
      if (slowPlugins.length > 0) {
        console.log('🐌 响应较慢的插件 (>1s):');
        slowPlugins.forEach(plugin => {
          console.log(`- ${plugin.name}: ${plugin.duration}`);
        });
      }
    }

    console.log('\n🎯 检查完成！');

  } catch (error) {
    console.error('❌ 检查过程中发生错误:', error.message);
  }
}

// 运行检查
checkAllPlugins().catch(console.error);
