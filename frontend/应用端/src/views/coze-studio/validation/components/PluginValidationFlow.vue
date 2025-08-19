<template>
  <el-card class="plugin-validation-card">
    <template #header>
      <div class="card-header">
        <span>{{ pluginInfo.name }} - 功能验证</span>
        <small>{{ pluginInfo.description || '专门针对此插件的验证流程' }}</small>
      </div>
    </template>

    <el-steps :active="step" finish-status="success" align-center>
      <el-step title="准备数据" />
      <el-step :title="`执行验证（${percent}%）`" />
      <el-step title="结果分析" />
    </el-steps>

    <!-- 步骤1: 准备数据 -->
    <div v-show="step === 0" class="step-pane">
      <div class="data-preparation">
        <h4>选择测试数据</h4>
        
        <!-- 文件上传类插件 -->
        <div v-if="isFilePlugin" class="file-upload-section">
          <el-upload
            drag
            :auto-upload="false"
            :on-change="onFileChange"
            :accept="getAcceptTypes()"
          >
            <i class="el-icon-upload" />
            <div class="el-upload__text">拖拽文件到此或点击上传</div>
            <div class="el-upload__tip">支持 {{ getAcceptTypes() }}</div>
          </el-upload>
        </div>

        <!-- 数据输入类插件 -->
        <div v-else class="data-input-section">
          <el-tabs v-model="dataInputTab">
            <el-tab-pane label="示例数据" name="sample">
              <!-- 预制测试文件选择 - 优化版本 -->
              <div class="test-file-section">
                <h5>📁 预制测试文件 ({{ availableTestFiles.length }} 个可用)</h5>
                <!-- 调试信息 -->
                <div style="background: #f0f0f0; padding: 10px; margin: 10px 0; font-size: 12px;">
                  <strong>调试信息:</strong><br>
                  hasAvailableTestFiles: {{ hasAvailableTestFiles }}<br>
                  availableTestFiles.length: {{ availableTestFiles.length }}<br>
                  availableTestFiles: {{ JSON.stringify(availableTestFiles, null, 2) }}
                </div>
                <div v-if="hasAvailableTestFiles" class="test-file-selector">
                  <el-select
                    v-model="selectedTestFile"
                    placeholder="选择测试文件"
                    @change="onTestFileChange"
                    style="width: 350px; margin-right: 10px;"
                  >
                    <el-option
                      v-for="file in availableTestFiles"
                      :key="file.file"
                      :label="file.name"
                      :value="file.file"
                    >
                      <span style="float: left">{{ file.icon }} {{ file.name }}</span>
                      <span style="float: right; color: #8492a6; font-size: 13px">{{ file.format }}</span>
                    </el-option>
                  </el-select>
                  <el-button
                    size="small"
                    type="primary"
                    @click="loadTestFile"
                    :disabled="!selectedTestFile"
                  >
                    <i class="el-icon-download"></i> 加载文件
                  </el-button>
                  <el-button
                    size="small"
                    type="info"
                    @click="previewTestFile"
                    :disabled="!selectedTestFile"
                  >
                    <i class="el-icon-view"></i> 预览
                  </el-button>
                </div>
                <div v-if="!hasAvailableTestFiles" class="no-test-files">
                  <el-alert
                    title="暂无可用的测试文件"
                    :description="`插件 ${pluginId} 暂未配置专用测试文件，请使用内置示例或自定义数据`"
                    type="warning"
                    :closable="false"
                    show-icon
                  />
                </div>
                <div class="test-file-description" v-if="selectedTestFileInfo">
                  <el-alert
                    :title="selectedTestFileInfo.description"
                    type="info"
                    :closable="false"
                    show-icon
                    style="margin-top: 8px;"
                  />
                </div>
              </div>

              <!-- 内置示例数据 -->
              <div class="sample-data">
                <h5>🎯 内置示例</h5>
                <el-button
                  v-for="sample in getSampleData()"
                  :key="sample.name"
                  size="small"
                  @click="loadSampleData(sample)"
                  style="margin: 4px;"
                >
                  {{ sample.name }}
                </el-button>
              </div>
            </el-tab-pane>
            <el-tab-pane label="自定义数据" name="custom">
              <!-- 文件上传区域 -->
              <div v-if="supportsFileUpload" class="custom-file-upload">
                <h5>📁 上传自定义文件</h5>
                <el-upload
                  drag
                  :auto-upload="false"
                  :on-change="onCustomFileChange"
                  :accept="getAcceptTypes()"
                  :show-file-list="true"
                  :limit="1"
                  class="custom-upload"
                >
                  <i class="el-icon-upload" />
                  <div class="el-upload__text">拖拽文件到此或点击上传</div>
                  <div class="el-upload__tip">支持 {{ getAcceptTypes() }}</div>
                </el-upload>
                <div class="upload-divider">
                  <span>或者</span>
                </div>
              </div>

              <!-- 文本输入区域 -->
              <div class="custom-text-input">
                <h5 v-if="supportsFileUpload">✏️ 直接输入数据</h5>
                <el-input
                  v-model="customData"
                  type="textarea"
                  :rows="8"
                  placeholder="请输入测试数据（JSON格式）..."
                />
              </div>
            </el-tab-pane>
          </el-tabs>
        </div>

        <!-- 配置参数 -->
        <div v-if="hasConfigOptions" class="config-section">
          <h4>配置参数</h4>
          <el-form :model="config" label-width="120px" size="small">
            <el-form-item 
              v-for="option in getConfigOptions()" 
              :key="option.key"
              :label="option.label"
            >
              <el-input 
                v-if="option.type === 'text'"
                v-model="config[option.key]" 
                :placeholder="option.placeholder"
              />
              <el-input-number 
                v-else-if="option.type === 'number'"
                v-model="config[option.key]" 
                :min="option.min" 
                :max="option.max"
              />
              <el-switch 
                v-else-if="option.type === 'boolean'"
                v-model="config[option.key]"
              />
            </el-form-item>
          </el-form>
        </div>

        <div class="action-buttons">
          <el-button type="primary" @click="startValidation" :disabled="!isDataReady">
            开始验证
          </el-button>
          <el-button @click="reset">重置</el-button>
        </div>
      </div>
    </div>

    <!-- 步骤2: 执行验证 -->
    <div v-show="step === 1" class="step-pane">
      <el-alert :title="`正在执行验证（${stage}）`" type="info" :closable="false"/>
      <el-progress :percentage="percent" :text-inside="true" style="margin-top: 8px;"/>
      
      <!-- 实时日志 -->
      <div v-if="logs.length > 0" class="real-time-logs">
        <h4>执行日志</h4>
        <div class="log-container">
          <div v-for="(log, index) in logs" :key="index" class="log-item">
            <span class="log-time">{{ log.time }}</span>
            <span :class="['log-level', log.level]">{{ log.level }}</span>
            <span class="log-message">{{ log.message }}</span>
          </div>
        </div>
      </div>
    </div>

    <!-- 步骤3: 结果分析 -->
    <div v-show="step === 2" class="step-pane">
      <el-alert 
        :title="validationResult.success ? '验证成功' : '验证失败'" 
        :type="validationResult.success ? 'success' : 'error'" 
        :closable="false"
      />
      
      <div v-if="validationResult.success" class="result-analysis">
        <div class="result-summary">
          <h4>执行摘要</h4>
          <el-descriptions :column="2" border>
            <el-descriptions-item label="执行时间">{{ validationResult.duration }}ms</el-descriptions-item>
            <el-descriptions-item label="数据类型">{{ validationResult.dataType }}</el-descriptions-item>
            <el-descriptions-item label="处理状态">{{ validationResult.status }}</el-descriptions-item>
            <el-descriptions-item label="结果大小">{{ getResultSize() }}</el-descriptions-item>
          </el-descriptions>
        </div>

        <!-- 根据插件类型显示不同的结果 -->
        <div class="result-content">
          <ValidationResult
            :result="validationResult.data"
            :plugin-id="pluginId"
          />
        </div>
      </div>

      <div v-else class="error-analysis">
        <h4>错误分析</h4>
        <el-alert :title="validationResult.error" type="error" show-icon />
        <div class="error-suggestions">
          <h5>建议解决方案：</h5>
          <ul>
            <li v-for="suggestion in getErrorSuggestions()" :key="suggestion">
              {{ suggestion }}
            </li>
          </ul>
        </div>
      </div>

      <div class="action-buttons">
        <el-button @click="reset">重新验证</el-button>
        <el-button type="primary" @click="exportResult" v-if="validationResult.success">
          导出结果
        </el-button>
      </div>
    </div>
  </el-card>
</template>

<script setup>
import { ref, reactive, computed, onMounted, nextTick } from 'vue'
import { ElMessage } from 'element-plus'
import { executePlugin as apiExecutePlugin } from '@/api/coze-studio'
import ValidationResult from './ValidationResult.vue'

// Props
const props = defineProps({
  pluginId: {
    type: String,
    required: true
  }
})

// 响应式数据
const step = ref(0)
const percent = ref(0)
const stage = ref('准备中')
const file = ref(null)
const customData = ref('')
const dataInputTab = ref('sample')
const config = reactive({})
const logs = ref([])
const validationResult = reactive({
  success: false,
  data: null,
  error: null,
  duration: 0,
  dataType: '',
  status: ''
})

// 测试文件相关
const selectedTestFile = ref('')
const selectedTestFileInfo = ref(null)
const availableTestFiles = ref([])
const testFileIndex = ref(null)

// 插件信息配置
const pluginConfigs = {
  // 文档解析类插件 - 支持预制测试文件和文件上传
  pdf_parser: {
    name: 'PDF解析器',
    description: '验证PDF文件内容提取和解析功能',
    isFilePlugin: false, // 改为false，支持预制测试文件
    acceptTypes: '.pdf',
    samples: [
      {
        name: '模拟PDF内容',
        data: {
          text: '这是一个PDF文档内容示例。\n\n包含多段文本内容：\n- 产品质量检测报告\n- 数据分析结果\n- 图表和表格信息\n\n本文档用于测试PDF解析功能的完整性和准确性。'
        }
      },
      {
        name: '质量报告PDF',
        data: {
          text: '质量管理系统报告\n\n检测项目：产品A批次检验\n检测时间：2024-01-15\n\n检测结果：\n- 尺寸精度：±0.01mm\n- 表面粗糙度：Ra 0.8\n- 硬度：HRC 45-50\n\n结论：产品质量符合标准要求。'
        }
      }
    ],
    configOptions: [
      { key: 'extract_images', label: '提取图像', type: 'boolean', placeholder: false },
      { key: 'extract_tables', label: '提取表格', type: 'boolean', placeholder: true }
    ]
  },
  xlsx_parser: {
    name: 'XLSX解析器',
    description: '验证Excel文件解析功能',
    isFilePlugin: false, // 支持预制测试文件
    acceptTypes: '.xlsx,.xls',
    samples: [
      {
        name: '产品质量数据',
        data: {
          csv: '产品编号,检测项目,测量值,标准值,结果\nP001,硬度,45.2,45±2,合格\nP002,重量,2.48,2.5±0.1,合格\nP003,尺寸,10.05,10±0.05,合格\nP004,表面粗糙度,0.8,≤1.0,合格'
        }
      },
      {
        name: '生产统计表',
        data: {
          csv: '日期,班次,产量,合格数,不合格数,合格率\n2024-01-15,早班,120,118,2,98.33%\n2024-01-15,中班,115,113,2,98.26%\n2024-01-15,晚班,110,108,2,98.18%'
        }
      },
      {
        name: '设备运行数据',
        data: {
          csv: '设备编号,运行时间,故障次数,维护状态,效率\nEQ001,8.5,0,正常,95%\nEQ002,8.2,1,需维护,88%\nEQ003,8.8,0,正常,97%'
        }
      }
    ],
    configOptions: [
      { key: 'sheet_name', label: '工作表名称', type: 'text', placeholder: 'Sheet1' },
      { key: 'header_row', label: '标题行', type: 'number', placeholder: 1 }
    ]
  },
  csv_parser: {
    name: 'CSV解析器',
    description: '验证CSV文件解析功能',
    isFilePlugin: false, // 支持预制测试文件
    acceptTypes: '.csv',
    samples: [
      {
        name: '质量检测数据',
        data: {
          csv: '批次号,检测时间,检测员,硬度,尺寸,重量,结果\nB20240115001,09:30,张三,45.2,10.05,2.48,合格\nB20240115002,10:15,李四,44.8,9.98,2.52,合格\nB20240115003,11:00,王五,46.1,10.02,2.47,合格'
        }
      },
      {
        name: 'SPC控制数据',
        data: {
          csv: '序号,测量值,上控制限,下控制限,中心线\n1,98.5,102,94,98\n2,99.2,102,94,98\n3,100.1,102,94,98\n4,97.8,102,94,98\n5,98.9,102,94,98'
        }
      },
      {
        name: '设备参数记录',
        data: {
          csv: '时间,温度,压力,转速,状态\n08:00,25.2,1.2,1500,正常\n08:30,25.8,1.3,1520,正常\n09:00,26.1,1.25,1510,正常\n09:30,25.9,1.28,1505,正常'
        }
      }
    ],
    configOptions: [
      { key: 'delimiter', label: '分隔符', type: 'text', placeholder: ',' },
      { key: 'encoding', label: '编码', type: 'text', placeholder: 'utf-8' }
    ]
  },
  json_parser: {
    name: 'JSON解析器',
    description: '验证JSON数据解析和格式化功能',
    isFilePlugin: false, // 支持预制测试文件
    acceptTypes: '.json',
    samples: [
      {
        name: '质量检测记录',
        data: {
          text: JSON.stringify({
            "batch_id": "B20240115001",
            "product": "精密零件A",
            "inspector": "张三",
            "test_date": "2024-01-15T09:30:00Z",
            "measurements": [
              {"item": "硬度", "value": 45.2, "unit": "HRC", "standard": "45±2", "result": "合格"},
              {"item": "尺寸", "value": 10.05, "unit": "mm", "standard": "10±0.05", "result": "合格"},
              {"item": "重量", "value": 248.5, "unit": "g", "standard": "250±5", "result": "合格"}
            ],
            "overall_result": "合格",
            "remarks": "所有检测项目均符合标准要求"
          }, null, 2)
        }
      },
      {
        name: '设备状态数据',
        data: {
          text: JSON.stringify({
            "equipment_id": "EQ001",
            "name": "数控机床A",
            "status": "运行中",
            "parameters": {
              "temperature": 25.8,
              "pressure": 1.25,
              "speed": 1500,
              "vibration": 0.02
            },
            "alerts": [],
            "last_maintenance": "2024-01-10",
            "next_maintenance": "2024-02-10",
            "efficiency": 95.2
          }, null, 2)
        }
      },
      {
        name: 'API响应数据',
        data: {
          text: JSON.stringify({
            "success": true,
            "data": {
              "total": 150,
              "qualified": 147,
              "defective": 3,
              "qualification_rate": 98.0,
              "defect_types": [
                {"type": "尺寸超差", "count": 2},
                {"type": "表面缺陷", "count": 1}
              ]
            },
            "timestamp": "2024-01-15T10:30:00Z"
          }, null, 2)
        }
      }
    ],
    configOptions: [
      { key: 'validate_schema', label: '验证JSON格式', type: 'boolean', placeholder: true },
      { key: 'pretty_print', label: '格式化输出', type: 'boolean', placeholder: true }
    ]
  },
  xml_parser: {
    name: 'XML解析器',
    description: '验证XML文档解析和结构分析功能',
    isFilePlugin: true,
    acceptTypes: '.xml',
    samples: [
      {
        name: '质量检测报告XML',
        data: {
          text: `<?xml version="1.0" encoding="UTF-8"?>
<quality_report>
  <header>
    <batch_id>B20240115001</batch_id>
    <product_name>精密零件A</product_name>
    <test_date>2024-01-15</test_date>
    <inspector>张三</inspector>
  </header>
  <measurements>
    <measurement>
      <item>硬度</item>
      <value>45.2</value>
      <unit>HRC</unit>
      <standard>45±2</standard>
      <result>合格</result>
    </measurement>
    <measurement>
      <item>尺寸</item>
      <value>10.05</value>
      <unit>mm</unit>
      <standard>10±0.05</standard>
      <result>合格</result>
    </measurement>
  </measurements>
  <conclusion>合格</conclusion>
</quality_report>`
        }
      },
      {
        name: '设备配置XML',
        data: {
          text: `<?xml version="1.0" encoding="UTF-8"?>
<equipment_config>
  <device id="EQ001">
    <name>数控机床A</name>
    <parameters>
      <temperature max="30" min="20" unit="°C"/>
      <pressure max="1.5" min="1.0" unit="MPa"/>
      <speed max="2000" min="1000" unit="rpm"/>
    </parameters>
    <maintenance>
      <interval>30</interval>
      <last_date>2024-01-10</last_date>
      <next_date>2024-02-10</next_date>
    </maintenance>
  </device>
</equipment_config>`
        }
      },
      {
        name: '工艺流程XML',
        data: {
          text: `<?xml version="1.0" encoding="UTF-8"?>
<process_flow>
  <step id="1">
    <name>原料检验</name>
    <duration>30</duration>
    <quality_check>true</quality_check>
  </step>
  <step id="2">
    <name>机械加工</name>
    <duration>120</duration>
    <equipment>EQ001</equipment>
  </step>
  <step id="3">
    <name>质量检测</name>
    <duration>45</duration>
    <inspector_required>true</inspector_required>
  </step>
</process_flow>`
        }
      }
    ],
    configOptions: [
      { key: 'validate_dtd', label: '验证DTD', type: 'boolean', placeholder: false },
      { key: 'extract_attributes', label: '提取属性', type: 'boolean', placeholder: true }
    ]
  },
  docx_parser: {
    name: 'DOCX解析器',
    description: '验证Word文档内容提取功能',
    isFilePlugin: false, // 支持预制测试文件
    acceptTypes: '.docx',
    samples: [
      { name: '技术文档', data: { text: '这是一个Word文档内容示例。\n\n包含标题、段落、列表等格式化内容。' } },
      { name: '报告模板', data: { text: '质量检测报告\n\n检测项目：硬度测试\n检测结果：合格\n检测日期：2024-01-01' } }
    ]
  },
  excel_analyzer: {
    name: 'Excel分析器',
    description: '验证高级Excel数据分析功能',
    isFilePlugin: false, // 支持预制测试文件
    acceptTypes: '.xlsx,.xls',
    samples: [
      { name: '销售数据', data: { csv: '月份,销售额,目标\n1月,10000,12000\n2月,11000,12000\n3月,13000,12000' } }
    ],
    configOptions: [
      { key: 'generate_charts', label: '生成图表', type: 'boolean', placeholder: true },
      { key: 'analysis_type', label: '分析类型', type: 'text', placeholder: 'trend' }
    ]
  },

  // 统计分析类插件 - 数据输入
  statistical_analyzer: {
    name: '统计分析器',
    description: '验证数据统计分析功能',
    isFilePlugin: false,
    samples: [
      {
        name: '硬度测试数据',
        data: {
          data: [45.2, 44.8, 45.5, 44.9, 45.1, 45.3, 44.7, 45.0, 45.4, 44.6, 45.2, 44.9, 45.1, 45.0, 44.8],
          unit: 'HRC',
          target: 45.0,
          usl: 47.0,
          lsl: 43.0
        }
      },
      {
        name: '尺寸精度数据',
        data: {
          data: [10.05, 9.98, 10.02, 10.01, 9.99, 10.03, 10.00, 9.97, 10.04, 10.01, 9.98, 10.02, 10.00, 9.99, 10.01],
          unit: 'mm',
          target: 10.00,
          usl: 10.05,
          lsl: 9.95
        }
      },
      {
        name: '重量控制数据',
        data: {
          data: [248.5, 249.2, 248.8, 249.0, 248.7, 249.1, 248.9, 248.6, 249.3, 248.4, 249.0, 248.8, 248.9, 249.1, 248.7],
          unit: 'g',
          target: 249.0,
          usl: 254.0,
          lsl: 244.0
        }
      },
      {
        name: '温度监控数据',
        data: {
          data: [25.2, 25.8, 25.5, 25.3, 25.7, 25.4, 25.6, 25.1, 25.9, 25.0, 25.5, 25.3, 25.4, 25.6, 25.2],
          unit: '°C',
          target: 25.0,
          usl: 28.0,
          lsl: 22.0
        }
      }
    ],
    configOptions: [
      { key: 'confidence_level', label: '置信水平', type: 'number', min: 0.8, max: 0.99, placeholder: '0.95' },
      { key: 'include_outliers', label: '包含异常值', type: 'boolean', placeholder: true },
      { key: 'calculate_cpk', label: '计算Cpk', type: 'boolean', placeholder: true }
    ]
  },
  fmea_analyzer: {
    name: 'FMEA失效模式分析',
    description: '验证失效模式与影响分析功能',
    isFilePlugin: false,
    samples: [
      {
        name: '数控机床系统',
        data: {
          process: '精密加工',
          components: [
            {
              name: '主轴电机',
              function: '提供切削动力',
              failure_modes: [
                { mode: '过热', cause: '冷却系统故障', effect: '加工精度下降', severity: 8, occurrence: 3, detection: 4, rpn: 96 },
                { mode: '轴承磨损', cause: '润滑不足', effect: '振动增大', severity: 7, occurrence: 4, detection: 3, rpn: 84 }
              ]
            },
            {
              name: '温度传感器',
              function: '监控设备温度',
              failure_modes: [
                { mode: '信号漂移', cause: '老化', effect: '误报警', severity: 5, occurrence: 3, detection: 6, rpn: 90 },
                { mode: '连接松动', cause: '振动', effect: '信号中断', severity: 6, occurrence: 2, detection: 7, rpn: 84 }
              ]
            }
          ]
        }
      },
      {
        name: '质量检测流程',
        data: {
          process: '产品质量检测',
          components: [
            {
              name: '测量仪器',
              function: '尺寸测量',
              failure_modes: [
                { mode: '校准偏差', cause: '定期校准未执行', effect: '测量误差', severity: 9, occurrence: 2, detection: 3, rpn: 54 },
                { mode: '探头磨损', cause: '使用频繁', effect: '精度降低', severity: 7, occurrence: 4, detection: 5, rpn: 140 }
              ]
            }
          ]
        }
      },
      {
        name: '包装工序',
        data: {
          process: '产品包装',
          components: [
            {
              name: '包装机',
              function: '自动包装',
              failure_modes: [
                { mode: '封口不良', cause: '温度设置错误', effect: '产品泄漏', severity: 8, occurrence: 3, detection: 2, rpn: 48 },
                { mode: '标签错位', cause: '定位系统故障', effect: '标识错误', severity: 6, occurrence: 4, detection: 6, rpn: 144 }
              ]
            }
          ]
        }
      }
    ],
    configOptions: [
      { key: 'rpn_threshold', label: 'RPN阈值', type: 'number', min: 50, max: 200, placeholder: '100' },
      { key: 'severity_weight', label: '严重度权重', type: 'number', min: 1, max: 3, placeholder: '1' },
      { key: 'include_recommendations', label: '包含改进建议', type: 'boolean', placeholder: true }
    ]
  },
  msa_calculator: {
    name: 'MSA测量系统分析',
    description: '验证测量系统的重复性和再现性分析',
    isFilePlugin: false,
    samples: [
      {
        name: '尺寸测量R&R研究',
        data: {
          study_name: '精密零件尺寸测量',
          measurement_unit: 'mm',
          tolerance: 0.1,
          parts: ['零件1', '零件2', '零件3', '零件4', '零件5'],
          operators: ['操作员A', '操作员B', '操作员C'],
          measurements: [
            // 零件1: 操作员A的3次测量, 操作员B的3次测量, 操作员C的3次测量
            [[10.02, 10.01, 10.03], [10.00, 10.02, 10.01], [10.01, 10.00, 10.02]],
            // 零件2
            [[9.98, 9.99, 9.97], [9.99, 9.98, 10.00], [9.97, 9.99, 9.98]],
            // 零件3
            [[10.05, 10.04, 10.06], [10.03, 10.05, 10.04], [10.04, 10.03, 10.05]],
            // 零件4
            [[9.95, 9.96, 9.94], [9.96, 9.95, 9.97], [9.94, 9.96, 9.95]],
            // 零件5
            [[10.08, 10.07, 10.09], [10.06, 10.08, 10.07], [10.07, 10.06, 10.08]]
          ]
        }
      },
      {
        name: '硬度测量R&R研究',
        data: {
          study_name: '材料硬度测量',
          measurement_unit: 'HRC',
          tolerance: 2.0,
          parts: ['样品A', '样品B', '样品C'],
          operators: ['检测员1', '检测员2'],
          measurements: [
            // 样品A: 检测员1的3次测量, 检测员2的3次测量
            [[45.2, 45.1, 45.3], [45.0, 45.2, 45.1]],
            // 样品B
            [[44.8, 44.9, 44.7], [44.9, 44.8, 45.0]],
            // 样品C
            [[45.5, 45.4, 45.6], [45.3, 45.5, 45.4]]
          ]
        }
      }
    ],
    configOptions: [
      { key: 'tolerance', label: '公差范围', type: 'number', min: 0.1, max: 10, placeholder: '1.0' },
      { key: 'confidence_level', label: '置信水平', type: 'number', min: 0.9, max: 0.99, placeholder: '0.95' },
      { key: 'calculate_ndc', label: '计算NDC', type: 'boolean', placeholder: true }
    ]
  },
  spc_controller: {
    name: 'SPC统计过程控制',
    description: '验证统计过程控制图表和分析功能',
    isFilePlugin: false,
    samples: [
      {
        name: 'X-R控制图数据',
        data: {
          chart_type: 'x_r',
          subgroup_size: 5,
          measurements: [
            [10.02, 10.01, 10.03, 9.99, 10.00],
            [9.98, 9.99, 9.97, 10.01, 10.00],
            [10.05, 10.04, 10.06, 10.02, 10.03],
            [9.95, 9.96, 9.94, 9.98, 9.97],
            [10.08, 10.07, 10.09, 10.05, 10.06],
            [10.01, 10.00, 10.02, 9.99, 10.01],
            [9.97, 9.98, 9.96, 10.00, 9.99],
            [10.03, 10.02, 10.04, 10.01, 10.02]
          ],
          specifications: {
            target: 10.0,
            usl: 10.1,
            lsl: 9.9
          }
        }
      },
      {
        name: 'I-MR控制图数据',
        data: {
          chart_type: 'i_mr',
          individual_values: [45.2, 44.8, 45.5, 44.9, 45.1, 45.3, 44.7, 45.0, 45.4, 44.6, 45.2, 44.9, 45.1, 45.0, 44.8],
          specifications: {
            target: 45.0,
            usl: 47.0,
            lsl: 43.0
          }
        }
      },
      {
        name: 'P控制图数据',
        data: {
          chart_type: 'p',
          defective_counts: [2, 1, 3, 0, 2, 1, 4, 2, 1, 3],
          sample_sizes: [100, 100, 100, 100, 100, 100, 100, 100, 100, 100],
          target_proportion: 0.02
        }
      }
    ],
    configOptions: [
      { key: 'control_limits', label: '控制限倍数', type: 'number', min: 2, max: 4, placeholder: '3' },
      { key: 'show_specifications', label: '显示规格限', type: 'boolean', placeholder: true },
      { key: 'calculate_capability', label: '计算过程能力', type: 'boolean', placeholder: true }
    ]
  },
  data_cleaner: {
    name: '数据清洗器',
    description: '验证数据清洗和预处理功能',
    isFilePlugin: false,
    samples: [
      {
        name: '质量数据清洗',
        data: {
          raw_data: [
            { id: 1, measurement: 45.2, operator: '张三', date: '2024-01-15', status: 'valid' },
            { id: 2, measurement: null, operator: '李四', date: '2024-01-15', status: 'invalid' },
            { id: 3, measurement: 999.9, operator: '王五', date: '2024-01-15', status: 'outlier' },
            { id: 4, measurement: 44.8, operator: '张三', date: '', status: 'valid' },
            { id: 5, measurement: 45.5, operator: '李四', date: '2024-01-15', status: 'valid' },
            { id: 6, measurement: -1.0, operator: '', date: '2024-01-15', status: 'invalid' }
          ],
          cleaning_rules: {
            remove_nulls: true,
            remove_outliers: true,
            outlier_method: 'iqr',
            fill_missing_dates: true,
            validate_ranges: { min: 0, max: 100 }
          }
        }
      },
      {
        name: '生产数据标准化',
        data: {
          raw_data: [
            { batch: 'B001', quantity: '120', defects: '2', rate: '98.33%' },
            { batch: 'B002', quantity: '115', defects: '3', rate: '97.39%' },
            { batch: 'B003', quantity: '', defects: '1', rate: '99.13%' },
            { batch: 'B004', quantity: '110', defects: 'N/A', rate: '98.18%' }
          ],
          cleaning_rules: {
            convert_types: { quantity: 'number', defects: 'number' },
            remove_percentage: ['rate'],
            fill_missing: { quantity: 'mean', defects: 0 },
            standardize_text: true
          }
        }
      }
    ],
    configOptions: [
      { key: 'outlier_method', label: '异常值检测方法', type: 'text', placeholder: 'iqr' },
      { key: 'missing_strategy', label: '缺失值处理', type: 'text', placeholder: 'mean' },
      { key: 'remove_duplicates', label: '移除重复项', type: 'boolean', placeholder: true }
    ]
  },
  anomaly_detector: {
    name: '异常检测器',
    description: '验证数据异常检测和预警功能',
    isFilePlugin: false,
    samples: [
      {
        name: '设备参数异常检测',
        data: {
          time_series: [
            { timestamp: '2024-01-15T08:00:00Z', temperature: 25.2, pressure: 1.2, vibration: 0.02 },
            { timestamp: '2024-01-15T08:30:00Z', temperature: 25.8, pressure: 1.3, vibration: 0.03 },
            { timestamp: '2024-01-15T09:00:00Z', temperature: 26.1, pressure: 1.25, vibration: 0.02 },
            { timestamp: '2024-01-15T09:30:00Z', temperature: 28.5, pressure: 1.8, vibration: 0.15 }, // 异常
            { timestamp: '2024-01-15T10:00:00Z', temperature: 25.9, pressure: 1.28, vibration: 0.03 },
            { timestamp: '2024-01-15T10:30:00Z', temperature: 25.7, pressure: 1.22, vibration: 0.02 }
          ],
          thresholds: {
            temperature: { min: 20, max: 28 },
            pressure: { min: 1.0, max: 1.5 },
            vibration: { min: 0, max: 0.1 }
          }
        }
      },
      {
        name: '质量指标异常检测',
        data: {
          measurements: [45.2, 44.8, 45.5, 44.9, 45.1, 45.3, 44.7, 45.0, 48.2, 45.4, 44.6, 45.2],
          detection_method: 'statistical',
          sensitivity: 0.95
        }
      }
    ],
    configOptions: [
      { key: 'detection_method', label: '检测方法', type: 'text', placeholder: 'statistical' },
      { key: 'sensitivity', label: '敏感度', type: 'number', min: 0.8, max: 0.99, placeholder: '0.95' },
      { key: 'window_size', label: '滑动窗口大小', type: 'number', min: 5, max: 50, placeholder: '10' }
    ]
  },
  api_connector: {
    name: 'API连接器',
    description: '验证外部API调用和数据集成功能',
    isFilePlugin: false,
    samples: [
      { name: 'GET请求', data: { url: 'https://api.example.com/data', method: 'GET' } },
      { name: 'POST请求', data: { url: 'https://api.example.com/submit', method: 'POST', body: {key: 'value'} } }
    ],
    configOptions: [
      { key: 'timeout', label: '超时时间(秒)', type: 'number', min: 5, max: 60, placeholder: '30' }
    ]
  },
  database_query: {
    name: '数据库查询器',
    description: '验证数据库查询和数据处理功能',
    isFilePlugin: false,
    samples: [
      { name: '产品查询', data: { query: 'SELECT * FROM products WHERE category = "electronics"', database: 'production' } }
    ],
    configOptions: [
      { key: 'limit', label: '结果限制', type: 'number', min: 10, max: 1000, placeholder: '100' }
    ]
  },
  ocr_reader: {
    name: 'OCR文字识别',
    description: '验证图像文字识别和提取功能',
    isFilePlugin: false, // 支持预制测试文件
    acceptTypes: '.jpg,.jpeg,.png,.bmp,.tiff',
    samples: [
      {
        name: '质量检测报告图片',
        data: {
          image_base64: 'data:image/png;base64,iVBORw0KGgoAAAANSUhEUgAAAAEAAAABCAYAAAAfFcSJAAAADUlEQVR42mNkYPhfDwAChwGA60e6kgAAAABJRU5ErkJggg==',
          expected_text: '产品编号: P001\n检测项目: 硬度测试\n测量值: 45.2 HRC\n标准值: 45±2 HRC\n检测结果: 合格\n检测员: 张三\n检测日期: 2024-01-15'
        }
      },
      {
        name: '设备铭牌图片',
        data: {
          image_base64: 'data:image/png;base64,iVBORw0KGgoAAAANSUhEUgAAAAEAAAABCAYAAAAfFcSJAAAADUlEQVR42mNkYPhfDwAChwGA60e6kgAAAABJRU5ErkJggg==',
          expected_text: '设备型号: CNC-2024\n制造商: 精密机械有限公司\n额定功率: 15KW\n生产日期: 2023-12-01\n序列号: SN20231201001'
        }
      },
      {
        name: '手写记录图片',
        data: {
          image_base64: 'data:image/png;base64,iVBORw0KGgoAAAANSUhEUgAAAAEAAAABCAYAAAAfFcSJAAAADUlEQVR42mNkYPhfDwAChwGA60e6kgAAAABJRU5ErkJggg==',
          expected_text: '班次: 早班\n操作员: 李四\n开始时间: 08:00\n结束时间: 16:00\n产量: 120件\n备注: 设备运行正常'
        }
      }
    ],
    configOptions: [
      { key: 'language', label: '识别语言', type: 'text', placeholder: 'zh-cn' },
      { key: 'confidence_threshold', label: '置信度阈值', type: 'number', min: 0.5, max: 1.0, placeholder: '0.8' },
      { key: 'extract_tables', label: '提取表格', type: 'boolean', placeholder: false }
    ]
  },
  defect_detector: {
    name: '缺陷检测器',
    description: '验证产品缺陷检测和分类功能',
    isFilePlugin: false, // 支持预制测试文件
    acceptTypes: '.jpg,.jpeg,.png,.bmp,.tiff',
    samples: [
      {
        name: '表面缺陷检测',
        data: {
          image_base64: 'data:image/png;base64,iVBORw0KGgoAAAANSUhEUgAAAAEAAAABCAYAAAAfFcSJAAAADUlEQVR42mNkYPhfDwAChwGA60e6kgAAAABJRU5ErkJggg==',
          defect_types: ['划痕', '凹陷', '污渍', '裂纹'],
          inspection_area: { x: 0, y: 0, width: 100, height: 100 }
        }
      },
      {
        name: '焊接缺陷检测',
        data: {
          image_base64: 'data:image/png;base64,iVBORw0KGgoAAAANSUhEUgAAAAEAAAABCAYAAAAfFcSJAAAADUlEQVR42mNkYPhfDwAChwGA60e6kgAAAABJRU5ErkJggg==',
          defect_types: ['气孔', '夹渣', '未焊透', '咬边'],
          inspection_area: { x: 10, y: 10, width: 80, height: 80 }
        }
      }
    ],
    configOptions: [
      { key: 'sensitivity', label: '检测敏感度', type: 'number', min: 0.1, max: 1.0, placeholder: '0.7' },
      { key: 'min_defect_size', label: '最小缺陷尺寸', type: 'number', min: 1, max: 100, placeholder: '5' },
      { key: 'output_format', label: '输出格式', type: 'text', placeholder: 'json' }
    ]
  },
  text_summarizer: {
    name: '文本摘要器',
    description: '验证文本摘要和关键信息提取功能',
    isFilePlugin: false,
    samples: [
      {
        name: '质量报告摘要',
        data: {
          text: `本次质量检测报告涵盖了2024年1月15日生产的精密零件A批次B20240115001的全面检测结果。检测项目包括硬度测试、尺寸精度测量、表面粗糙度检测和重量测量等四个关键指标。

硬度测试结果显示，测量值为45.2 HRC，符合标准要求45±2 HRC的范围内，判定为合格。尺寸精度测量显示，关键尺寸为10.05mm，在标准公差10±0.05mm范围内，精度良好。表面粗糙度测试结果为Ra 0.8μm，满足≤1.0μm的技术要求。重量测量结果为248.5g，在标准重量250±5g的允许范围内。

检测过程中使用了校准合格的精密测量设备，检测环境温度控制在23±2°C，湿度控制在45-65%RH范围内，确保了测量结果的准确性和可靠性。检测员张三具有相应的资质认证，严格按照作业指导书执行检测程序。

综合评定结果：本批次产品质量合格，所有检测项目均符合技术标准要求，可以放行投入下一工序。建议继续保持现有的质量控制水平，定期对检测设备进行校准维护。`
        }
      },
      {
        name: '设备维护记录摘要',
        data: {
          text: `设备编号EQ001数控机床A于2024年1月10日进行了定期维护保养工作。维护内容包括机械部件检查、电气系统测试、润滑系统保养和安全装置验证等项目。

机械部件检查发现主轴轴承运转正常，无异常噪音和振动，导轨润滑良好，传动系统工作稳定。电气系统测试显示各项参数正常，控制系统响应及时，传感器信号稳定可靠。润滑系统保养包括更换润滑油、清洁油路和检查油泵工作状态，系统运行正常。

安全装置验证确认急停按钮、安全门锁、光栅保护等装置功能正常，符合安全操作要求。维护过程中更换了磨损的刀具夹持器，调整了工作台水平度，校准了坐标系统精度。

维护完成后进行了试运行测试，设备运行稳定，加工精度满足要求。下次维护计划安排在2024年2月10日，建议重点关注主轴轴承状态和润滑系统性能。设备当前状态良好，可正常投入生产使用。`
        }
      }
    ],
    configOptions: [
      { key: 'summary_length', label: '摘要长度', type: 'number', min: 50, max: 500, placeholder: '200' },
      { key: 'extract_keywords', label: '提取关键词', type: 'boolean', placeholder: true },
      { key: 'language', label: '文本语言', type: 'text', placeholder: 'zh-cn' }
    ]
  }
}

// 计算属性
const pluginInfo = computed(() => pluginConfigs[props.pluginId] || { name: props.pluginId, description: '' })
const isFilePlugin = computed(() => pluginInfo.value.isFilePlugin || false)
const hasConfigOptions = computed(() => (pluginInfo.value.configOptions || []).length > 0)
const isDataReady = computed(() => {
  if (isFilePlugin.value) return !!file.value
  return !!(customData.value || dataInputTab.value === 'sample')
})

// 测试文件相关计算属性
const hasAvailableTestFiles = computed(() => {
  console.log('计算属性 hasAvailableTestFiles 被调用，availableTestFiles.value.length:', availableTestFiles.value.length)
  return availableTestFiles.value.length > 0
})

// 是否支持文件上传（文档解析类插件）
const supportsFileUpload = computed(() => {
  const documentPlugins = ['pdf_parser', 'xlsx_parser', 'csv_parser', 'docx_parser', 'json_parser', 'xml_parser', 'excel_analyzer', 'ocr_reader', 'defect_detector']
  return documentPlugins.includes(props.pluginId)
})

// 方法
const getAcceptTypes = () => pluginInfo.value.acceptTypes || '*'
const getSampleData = () => pluginInfo.value.samples || []
const getConfigOptions = () => pluginInfo.value.configOptions || []

const onFileChange = (uploadFile) => {
  file.value = uploadFile?.raw || null
}

const onCustomFileChange = async (uploadFile) => {
  // 获取实际的文件对象
  const actualFile = uploadFile?.raw || uploadFile

  if (!actualFile || !(actualFile instanceof File || actualFile instanceof Blob)) return

  try {
    const fileData = await readFileAsInput(actualFile)
    customData.value = JSON.stringify(fileData, null, 2)
    ElMessage.success(`文件 ${uploadFile.name} 已加载到自定义数据区域`)
  } catch (error) {
    console.error('文件读取失败:', error)
    ElMessage.error('文件读取失败: ' + error.message)
  }
}

const loadSampleData = (sample) => {
  customData.value = JSON.stringify(sample.data, null, 2)
  ElMessage.success(`已加载示例数据: ${sample.name}`)
}

// 测试文件相关方法
const loadTestFileIndex = async () => {
  try {
    const response = await fetch('/plugin-test-files/file_index.json')
    if (response.ok) {
      testFileIndex.value = await response.json()
      updateAvailableTestFiles()
      console.log('测试文件索引加载成功')
    }
  } catch (error) {
    console.warn('加载测试文件索引失败:', error)
  }
}

const updateAvailableTestFiles = () => {
  console.log('=== updateAvailableTestFiles 开始 ===')
  console.log('testFileIndex.value:', testFileIndex.value)
  console.log('props.pluginId:', props.pluginId)

  if (!testFileIndex.value) {
    console.log('testFileIndex.value 为空，返回')
    availableTestFiles.value = []
    return
  }

  const pluginId = props.pluginId
  const files = testFileIndex.value.plugin_test_files?.files

  console.log('files 结构:', files)

  if (!files) {
    console.log('files 为空，返回')
    availableTestFiles.value = []
    return
  }

  let availableFiles = []

  // 直接在files对象中查找插件ID
  if (files[pluginId]) {
    console.log('✅ 找到匹配的插件测试文件:', files[pluginId])
    availableFiles.push(files[pluginId])
  } else {
    console.log('❌ 未找到匹配的插件测试文件')
    // 尝试模糊匹配
    Object.keys(files).forEach(filePluginId => {
      if (pluginId.includes(filePluginId) || filePluginId.includes(pluginId)) {
        console.log('🔍 模糊匹配成功:', filePluginId, files[filePluginId])
        availableFiles.push(files[filePluginId])
      }
    })
  }

  console.log(`最终结果: 插件 ${pluginId} 找到 ${availableFiles.length} 个测试文件:`, availableFiles)

  // 清空数组并重新填充，确保响应式更新
  availableTestFiles.value.splice(0, availableTestFiles.value.length)
  availableFiles.forEach(file => {
    availableTestFiles.value.push(file)
  })

  selectedTestFile.value = ''
  selectedTestFileInfo.value = null

  // 强制触发响应式更新
  nextTick(() => {
    console.log('nextTick 后 availableTestFiles.value:', availableTestFiles.value)
    console.log('nextTick 后 availableTestFiles.value.length:', availableTestFiles.value.length)
  })
  console.log('=== updateAvailableTestFiles 结束 ===')
}

const onTestFileChange = (fileName) => {
  if (!fileName) {
    selectedTestFileInfo.value = null
    return
  }

  // 查找文件信息
  const files = testFileIndex.value.plugin_test_files.files
  let fileInfo = null

  // 直接在files对象中查找
  Object.keys(files).forEach(pluginId => {
    if (files[pluginId].file === fileName) {
      fileInfo = files[pluginId]
    }
  })

  selectedTestFileInfo.value = fileInfo
}

const loadTestFile = async () => {
  if (!selectedTestFile.value) {
    ElMessage.warning('请先选择测试文件')
    return
  }

  try {
    const response = await fetch(`/plugin-test-files/${selectedTestFile.value}`)
    if (!response.ok) {
      throw new Error('文件加载失败')
    }

    const content = await response.text()

    // 根据文件格式处理内容
    if (selectedTestFile.value.endsWith('.json')) {
      customData.value = content
    } else if (selectedTestFile.value.endsWith('.csv')) {
      customData.value = JSON.stringify({ csv: content }, null, 2)
    } else if (selectedTestFile.value.endsWith('.xml')) {
      customData.value = JSON.stringify({ xml: content }, null, 2)
    } else {
      customData.value = JSON.stringify({ text: content }, null, 2)
    }

    ElMessage.success('测试文件加载成功')
  } catch (error) {
    console.error('加载测试文件失败:', error)
    ElMessage.error('加载测试文件失败: ' + error.message)
  }
}

const previewTestFile = async () => {
  if (!selectedTestFile.value) {
    ElMessage.warning('请先选择测试文件')
    return
  }

  try {
    const response = await fetch(`/plugin-test-files/${selectedTestFile.value}`)
    if (!response.ok) {
      throw new Error('文件加载失败')
    }

    const content = await response.text()
    const preview = content.length > 500 ? content.substring(0, 500) + '...' : content

    ElMessage({
      message: preview,
      type: 'info',
      duration: 0,
      showClose: true,
      dangerouslyUseHTMLString: false
    })
  } catch (error) {
    console.error('预览测试文件失败:', error)
    ElMessage.error('预览测试文件失败: ' + error.message)
  }
}

const addLog = (level, message) => {
  logs.value.push({
    time: new Date().toLocaleTimeString(),
    level,
    message
  })
}

const startValidation = async () => {
  step.value = 1
  percent.value = 0
  stage.value = '准备数据'
  logs.value = []
  
  try {
    addLog('INFO', '开始验证流程')
    
    // 准备输入数据
    let inputData = {}
    if (isFilePlugin.value && file.value) {
      stage.value = '读取文件'
      percent.value = 20
      addLog('INFO', `读取文件: ${file.value.name}`)
      
      // 文件读取逻辑
      inputData = await readFileAsInput(file.value)
    } else {
      // 使用自定义数据或示例数据
      try {
        inputData = customData.value ? JSON.parse(customData.value) : getSampleData()[0]?.data || {}
      } catch (e) {
        console.error('数据格式错误:', e)
        throw new Error('数据格式错误，请检查JSON格式')
      }
    }
    
    stage.value = '执行插件'
    percent.value = 50
    addLog('INFO', `调用插件: ${props.pluginId}`)

    const startTime = Date.now()
    let response
    try {
      response = await apiExecutePlugin(props.pluginId, { input: inputData, ...config })
    } catch (apiError) {
      console.error('API调用失败:', apiError)
      throw new Error(`插件执行失败: ${apiError.message || '网络错误'}`)
    }

    const duration = Date.now() - startTime

    percent.value = 100
    stage.value = '完成'
    addLog('SUCCESS', `插件执行成功，耗时: ${duration}ms`)

    // 设置结果
    validationResult.success = true
    validationResult.data = response.data?.result || response.data || response
    validationResult.duration = duration
    validationResult.dataType = typeof validationResult.data
    validationResult.status = '成功'

    step.value = 2
    
  } catch (error) {
    addLog('ERROR', `验证失败: ${error.message}`)
    validationResult.success = false
    validationResult.error = error.message
    step.value = 2
  }
}

const readFileAsInput = async (file) => {
  return new Promise((resolve, reject) => {
    if (!file || !(file instanceof File || file instanceof Blob)) {
      reject(new Error('无效的文件对象'))
      return
    }

    const reader = new FileReader()

    reader.onload = () => {
      try {
        const result = reader.result
        if (file.name && file.name.toLowerCase().endsWith('.csv')) {
          resolve({ csv: result })
        } else {
          // Excel等二进制文件转base64
          const base64 = result.split(',')[1] || ''
          resolve({ base64, fileName: file.name || 'unknown', fileType: file.type || 'application/octet-stream' })
        }
      } catch (error) {
        console.error('文件处理错误:', error)
        reject(new Error(`文件处理失败: ${error.message}`))
      }
    }

    reader.onerror = (error) => {
      console.error('文件读取错误:', error)
      reject(new Error('文件读取失败'))
    }

    reader.onabort = () => {
      console.error('文件读取被中断')
      reject(new Error('文件读取被中断'))
    }

    try {
      if (file.name && file.name.toLowerCase().endsWith('.csv')) {
        reader.readAsText(file)
      } else {
        reader.readAsDataURL(file)
      }
    } catch (error) {
      console.error('启动文件读取失败:', error)
      reject(new Error(`启动文件读取失败: ${error.message}`))
    }
  })
}



const getResultSize = () => {
  if (!validationResult.data) return '0'
  const str = JSON.stringify(validationResult.data)
  return `${(str.length / 1024).toFixed(1)} KB`
}

const getErrorSuggestions = () => {
  const suggestions = [
    '检查输入数据格式是否正确',
    '确认插件是否已正确安装',
    '查看服务器日志获取详细错误信息'
  ]
  
  if (validationResult.error?.includes('格式')) {
    suggestions.unshift('数据格式不符合要求，请参考示例数据')
  }
  
  return suggestions
}

const exportResult = () => {
  const dataStr = JSON.stringify(validationResult.data, null, 2)
  const blob = new Blob([dataStr], { type: 'application/json' })
  const url = URL.createObjectURL(blob)
  const a = document.createElement('a')
  a.href = url
  a.download = `${props.pluginId}_validation_result.json`
  a.click()
  URL.revokeObjectURL(url)
  ElMessage.success('结果已导出')
}

const reset = () => {
  step.value = 0
  percent.value = 0
  file.value = null
  customData.value = ''
  logs.value = []
  Object.keys(config).forEach(key => delete config[key])
  validationResult.success = false
  validationResult.data = null
  validationResult.error = null
}

onMounted(() => {
  // 初始化配置默认值
  getConfigOptions().forEach(option => {
    if (option.placeholder) {
      config[option.key] = option.placeholder
    }
  })

  // 加载测试文件索引
  loadTestFileIndex()
})
</script>

<style scoped>
.plugin-validation-card {
  margin-bottom: 16px;
}

.step-pane {
  margin-top: 16px;
  padding: 16px;
}

.data-preparation h4 {
  margin-bottom: 12px;
  color: #303133;
}

.file-upload-section {
  margin-bottom: 16px;
}

.sample-data {
  margin-bottom: 16px;
}

.config-section {
  margin-top: 20px;
  padding-top: 16px;
  border-top: 1px solid #ebeef5;
}

.action-buttons {
  margin-top: 20px;
  text-align: center;
}

.real-time-logs {
  margin-top: 16px;
}

.log-container {
  background: #0b1020;
  color: #d6e1ff;
  padding: 12px;
  border-radius: 6px;
  max-height: 200px;
  overflow-y: auto;
  font-family: 'Courier New', monospace;
  font-size: 12px;
}

.log-item {
  margin-bottom: 4px;
}

.log-time {
  color: #909399;
  margin-right: 8px;
}

.log-level {
  margin-right: 8px;
  font-weight: bold;
}

.log-level.INFO {
  color: #409eff;
}

.log-level.SUCCESS {
  color: #67c23a;
}

.log-level.ERROR {
  color: #f56c6c;
}

.result-analysis {
  margin-top: 16px;
}

.result-summary {
  margin-bottom: 20px;
}

.error-analysis {
  margin-top: 16px;
}

.error-suggestions {
  margin-top: 12px;
}

.error-suggestions ul {
  margin-left: 20px;
}

.error-suggestions li {
  margin-bottom: 4px;
}

/* 测试文件选择器样式 */
.test-file-section {
  margin-bottom: 20px;
  padding: 20px;
  background: linear-gradient(135deg, #f0f9ff 0%, #e0f2fe 100%);
  border-radius: 8px;
  border: 2px solid #409EFF;
  box-shadow: 0 2px 8px rgba(64, 158, 255, 0.1);
}

.test-file-section h5 {
  margin: 0 0 16px 0;
  color: #1f2937;
  font-weight: 600;
  font-size: 16px;
}

.test-file-selector {
  display: flex;
  align-items: center;
  flex-wrap: wrap;
  gap: 12px;
}

.test-file-description {
  margin-top: 12px;
}

.no-test-files {
  margin-top: 8px;
}

/* 自定义文件上传样式 */
.custom-file-upload {
  margin-bottom: 20px;
  padding: 16px;
  background: #f8fafc;
  border-radius: 6px;
  border: 1px dashed #d1d5db;
}

.custom-file-upload h5 {
  margin: 0 0 12px 0;
  color: #374151;
  font-weight: 600;
}

.custom-upload {
  margin-bottom: 12px;
}

.upload-divider {
  text-align: center;
  position: relative;
  margin: 16px 0;
}

.upload-divider::before {
  content: '';
  position: absolute;
  top: 50%;
  left: 0;
  right: 0;
  height: 1px;
  background: #e5e7eb;
}

.upload-divider span {
  background: #f8fafc;
  padding: 0 12px;
  color: #6b7280;
  font-size: 14px;
}

.custom-text-input h5 {
  margin: 0 0 12px 0;
  color: #374151;
  font-weight: 600;
}


</style>
