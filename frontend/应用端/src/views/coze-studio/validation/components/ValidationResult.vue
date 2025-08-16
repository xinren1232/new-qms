<template>
  <div class="validation-result">
    <!-- 统一的插件信息显示 -->
    <div v-if="result.metadata" class="plugin-metadata">
      <el-descriptions :column="4" border size="small" style="margin-bottom: 16px;">
        <el-descriptions-item label="插件名称">{{ result.metadata.plugin_name || 'N/A' }}</el-descriptions-item>
        <el-descriptions-item label="插件类型">{{ getPluginTypeText(result.metadata.plugin_type) }}</el-descriptions-item>
        <el-descriptions-item label="结果类型">{{ getResultTypeText(result.type) }}</el-descriptions-item>
        <el-descriptions-item label="执行时间">{{ formatExecutionTime(result.metadata.execution_time) }}</el-descriptions-item>
      </el-descriptions>
    </div>

    <!-- 警告信息统一显示 -->
    <div v-if="result.warnings && result.warnings.length > 0" class="unified-warnings">
      <el-alert
        v-for="(warning, index) in result.warnings"
        :key="index"
        :title="warning"
        type="warning"
        :closable="false"
        style="margin-bottom: 8px;"
      />
    </div>

    <!-- PDF解析器专用结果 -->
    <div v-if="pluginId === 'pdf_parser'" class="pdf-result">
      <h4>PDF解析结果</h4>

      <!-- 文档信息 -->
      <div v-if="result.metadata" class="pdf-metadata">
        <h5>文档信息</h5>
        <el-descriptions :column="3" border size="small">
          <el-descriptions-item label="标题">{{ result.metadata.title || '未命名文档' }}</el-descriptions-item>
          <el-descriptions-item label="作者">{{ result.metadata.author || '未知' }}</el-descriptions-item>
          <el-descriptions-item label="创建日期">{{ result.metadata.created_date || 'N/A' }}</el-descriptions-item>
          <el-descriptions-item label="页数">{{ result.pages || 'N/A' }}</el-descriptions-item>
          <el-descriptions-item label="文本长度">{{ result.text ? result.text.length : 0 }} 字符</el-descriptions-item>
          <el-descriptions-item label="数据类型">{{ result.type || 'pdf_content' }}</el-descriptions-item>
        </el-descriptions>
      </div>

      <!-- 提取的文本内容 -->
      <div v-if="result.text" class="pdf-content">
        <h5>提取的文本内容</h5>
        <el-input
          type="textarea"
          :value="result.text"
          :rows="12"
          readonly
          placeholder="未提取到文本内容"
          style="margin-bottom: 16px;"
        />

        <!-- 文本统计 -->
        <div class="text-stats">
          <el-tag type="info" style="margin-right: 8px;">
            字符数: {{ result.text.length }}
          </el-tag>
          <el-tag type="success" style="margin-right: 8px;">
            行数: {{ result.text.split('\n').length }}
          </el-tag>
          <el-tag type="warning">
            词数: {{ result.text.split(/\s+/).filter(word => word.length > 0).length }}
          </el-tag>
        </div>
      </div>

      <!-- 警告信息 -->
      <div v-if="result.warnings && result.warnings.length > 0" class="pdf-warnings">
        <h5>警告信息</h5>
        <el-alert
          v-for="(warning, index) in result.warnings"
          :key="index"
          :title="warning"
          type="warning"
          :closable="false"
          style="margin-bottom: 8px;"
        />
      </div>
    </div>

    <!-- DOCX解析器专用结果 -->
    <div v-if="pluginId === 'docx_parser'" class="docx-result">
      <h4>DOCX解析结果</h4>

      <!-- 文档统计信息 -->
      <div v-if="result.statistics" class="docx-statistics">
        <h5>文档统计</h5>
        <el-descriptions :column="4" border size="small" style="margin-bottom: 16px;">
          <el-descriptions-item label="字符数">{{ result.statistics.character_count || 0 }}</el-descriptions-item>
          <el-descriptions-item label="词数">{{ result.statistics.word_count || 0 }}</el-descriptions-item>
          <el-descriptions-item label="段落数">{{ result.statistics.paragraph_count || 0 }}</el-descriptions-item>
          <el-descriptions-item label="处理时间">{{ result.statistics.processing_time || 0 }}ms</el-descriptions-item>
        </el-descriptions>
      </div>

      <!-- 文档元数据 -->
      <div v-if="result.metadata" class="docx-metadata">
        <h5>文档信息</h5>
        <el-descriptions :column="3" border size="small" style="margin-bottom: 16px;">
          <el-descriptions-item label="提取方法">{{ result.metadata.extraction_method || 'N/A' }}</el-descriptions-item>
          <el-descriptions-item label="文件格式">{{ result.metadata.format || 'docx' }}</el-descriptions-item>
          <el-descriptions-item label="文件大小">{{ result.metadata.file_size || 0 }} bytes</el-descriptions-item>
        </el-descriptions>
      </div>

      <!-- 提取的文本内容 -->
      <div v-if="result.text || result.data?.text" class="docx-content">
        <h5>提取的文本内容</h5>
        <el-input
          type="textarea"
          :value="result.text || result.data?.text || ''"
          :rows="12"
          readonly
          placeholder="未提取到文本内容"
          style="margin-bottom: 16px; font-family: 'Courier New', monospace;"
        />

        <!-- 文本统计 -->
        <div class="text-stats">
          <el-tag type="info" style="margin-right: 8px;">
            字符数: {{ (result.text || result.data?.text || '').length }}
          </el-tag>
          <el-tag type="success" style="margin-right: 8px;">
            行数: {{ (result.text || result.data?.text || '').split('\n').length }}
          </el-tag>
          <el-tag type="warning">
            词数: {{ (result.text || result.data?.text || '').split(/\s+/).filter(word => word.length > 0).length }}
          </el-tag>
        </div>
      </div>

      <!-- HTML格式预览 -->
      <div v-if="result.html || result.data?.html" class="docx-html">
        <h5>HTML格式预览</h5>
        <div style="max-height: 300px; overflow: auto; border: 1px solid #dcdfe6; padding: 12px; background: #f9f9f9;">
          <div v-html="result.html || result.data?.html"></div>
        </div>
      </div>

      <!-- 警告信息 -->
      <div v-if="result.warnings && result.warnings.length > 0" class="docx-warnings">
        <h5>警告信息</h5>
        <el-alert
          v-for="(warning, index) in result.warnings"
          :key="index"
          :title="warning"
          type="warning"
          :closable="false"
          style="margin-bottom: 8px;"
        />
      </div>
    </div>

    <!-- 其他数据解析类插件结果 -->
    <div v-else-if="isParserPlugin" class="parser-result">
      <h4>解析结果</h4>

      <!-- 数据预览 -->
      <div v-if="result.preview" class="data-preview">
        <h5>数据预览 (前{{ Math.min(result.preview.length, 10) }}行)</h5>
        <el-table
          :data="result.preview.slice(0, 10)"
          border
          size="small"
          max-height="300px"
          style="margin-bottom: 16px;"
        >
          <el-table-column
            v-for="(value, key) in (result.preview[0] || {})"
            :key="key"
            :prop="key"
            :label="key"
            show-overflow-tooltip
          />
        </el-table>
      </div>

      <!-- 统计信息 -->
      <div v-if="result.summary" class="data-summary">
        <h5>数据统计</h5>
        <el-descriptions :column="3" border size="small">
          <el-descriptions-item label="总行数">{{ result.summary.total_rows || result.row_count || 0 }}</el-descriptions-item>
          <el-descriptions-item label="总列数">{{ result.summary.total_columns || Object.keys(result.columns || {}).length || 0 }}</el-descriptions-item>
          <el-descriptions-item label="数据类型">{{ result.type || 'unknown' }}</el-descriptions-item>
        </el-descriptions>
      </div>

      <!-- 列信息 -->
      <div v-if="result.columns" class="column-info">
        <h5>列信息</h5>
        <el-table :data="getColumnData()" border size="small">
          <el-table-column prop="name" label="列名" />
          <el-table-column prop="type" label="数据类型" />
          <el-table-column prop="sample" label="示例值" show-overflow-tooltip />
        </el-table>
      </div>
    </div>

    <!-- 统计分析类插件结果 -->
    <div v-else-if="isStatisticalPlugin" class="statistical-result">
      <h4>统计分析结果</h4>
      
      <div v-if="result.stats" class="stats-summary">
        <el-descriptions :column="2" border>
          <el-descriptions-item label="样本数量">{{ result.stats.count || 0 }}</el-descriptions-item>
          <el-descriptions-item label="平均值">{{ formatNumber(result.stats.mean) }}</el-descriptions-item>
          <el-descriptions-item label="标准差">{{ formatNumber(result.stats.std) }}</el-descriptions-item>
          <el-descriptions-item label="最小值">{{ formatNumber(result.stats.min) }}</el-descriptions-item>
          <el-descriptions-item label="最大值">{{ formatNumber(result.stats.max) }}</el-descriptions-item>
          <el-descriptions-item label="中位数">{{ formatNumber(result.stats.median) }}</el-descriptions-item>
        </el-descriptions>
      </div>

      <!-- 分布信息 -->
      <div v-if="result.distribution" class="distribution-info">
        <h5>数据分布</h5>
        <el-tag v-if="result.distribution.type" type="info">
          分布类型: {{ result.distribution.type }}
        </el-tag>
        <div v-if="result.distribution.histogram" class="histogram">
          <!-- 这里可以添加图表组件 -->
          <p>直方图数据: {{ result.distribution.histogram.length }} 个区间</p>
        </div>
      </div>
    </div>

    <!-- FMEA分析结果 -->
    <div v-else-if="pluginId === 'fmea_analyzer'" class="fmea-result">
      <h4>FMEA分析结果</h4>
      
      <div v-if="result.summary" class="fmea-summary">
        <el-descriptions :column="3" border>
          <el-descriptions-item label="组件总数">{{ result.summary.total_components }}</el-descriptions-item>
          <el-descriptions-item label="高风险组件">{{ result.summary.high_risk_count }}</el-descriptions-item>
          <el-descriptions-item label="平均RPN">{{ formatNumber(result.summary.average_rpn) }}</el-descriptions-item>
        </el-descriptions>
      </div>

      <div v-if="result.components" class="fmea-components">
        <h5>组件风险分析</h5>
        <el-table :data="result.components" border>
          <el-table-column prop="component" label="组件名称" />
          <el-table-column prop="severity" label="严重度" width="80" />
          <el-table-column prop="occurrence" label="发生度" width="80" />
          <el-table-column prop="detection" label="检出度" width="80" />
          <el-table-column prop="rpn" label="RPN" width="80" />
          <el-table-column prop="risk_level" label="风险等级" width="100">
            <template #default="{ row }">
              <el-tag :type="getRiskLevelType(row.risk_level)">
                {{ getRiskLevelText(row.risk_level) }}
              </el-tag>
            </template>
          </el-table-column>
          <el-table-column prop="recommendations" label="建议措施">
            <template #default="{ row }">
              <ul style="margin: 0; padding-left: 16px;">
                <li v-for="rec in row.recommendations" :key="rec">{{ rec }}</li>
              </ul>
            </template>
          </el-table-column>
        </el-table>
      </div>
    </div>

    <!-- MSA分析结果 -->
    <div v-else-if="pluginId === 'msa_calculator'" class="msa-result">
      <h4>MSA测量系统分析结果</h4>
      
      <el-descriptions :column="2" border>
        <el-descriptions-item label="重复性">{{ formatNumber(result.repeatability) }}</el-descriptions-item>
        <el-descriptions-item label="再现性">{{ formatNumber(result.reproducibility) }}</el-descriptions-item>
        <el-descriptions-item label="Gage R&R">{{ formatNumber(result.gage_rr) }}</el-descriptions-item>
        <el-descriptions-item label="%R&R">{{ formatNumber(result.percent_rr) }}%</el-descriptions-item>
        <el-descriptions-item label="评估结果" :span="2">
          <el-tag :type="getMSAEvaluationType(result.evaluation)">
            {{ getMSAEvaluationText(result.evaluation) }}
          </el-tag>
        </el-descriptions-item>
      </el-descriptions>

      <div v-if="result.recommendations" class="msa-recommendations">
        <h5>改进建议</h5>
        <ul>
          <li v-for="rec in result.recommendations" :key="rec">{{ rec }}</li>
        </ul>
      </div>
    </div>

    <!-- API连接器结果 -->
    <div v-else-if="pluginId === 'api_connector'" class="api-result">
      <h4>API调用结果</h4>
      
      <el-descriptions :column="2" border>
        <el-descriptions-item label="请求URL">{{ result.url }}</el-descriptions-item>
        <el-descriptions-item label="请求方法">{{ result.method }}</el-descriptions-item>
        <el-descriptions-item label="响应状态">{{ result.status }}</el-descriptions-item>
        <el-descriptions-item label="响应时间">{{ result.duration || 'N/A' }}ms</el-descriptions-item>
      </el-descriptions>

      <div v-if="result.data" class="api-response">
        <h5>响应数据</h5>
        <el-input
          type="textarea"
          :value="JSON.stringify(result.data, null, 2)"
          :rows="8"
          readonly
        />
      </div>
    </div>

    <!-- 数据库查询结果 -->
    <div v-else-if="pluginId === 'database_query'" class="db-result">
      <h4>数据库查询结果</h4>
      
      <el-descriptions :column="2" border>
        <el-descriptions-item label="数据库">{{ result.database }}</el-descriptions-item>
        <el-descriptions-item label="执行时间">{{ formatNumber(result.execution_time) }}ms</el-descriptions-item>
        <el-descriptions-item label="返回行数">{{ result.count }}</el-descriptions-item>
        <el-descriptions-item label="查询语句" :span="2">
          <code>{{ result.query }}</code>
        </el-descriptions-item>
      </el-descriptions>

      <div v-if="result.results" class="db-results">
        <h5>查询结果</h5>
        <el-table :data="result.results" border size="small">
          <el-table-column 
            v-for="(value, key) in (result.results[0] || {})" 
            :key="key"
            :prop="key" 
            :label="key"
          />
        </el-table>
      </div>
    </div>

    <!-- 文档处理器结果 -->
    <div v-else-if="isDocumentProcessor" class="document-processor-result">
      <h4>文档处理结果</h4>

      <!-- 处理统计 -->
      <div v-if="result.statistics" class="processing-stats">
        <h5>处理统计</h5>
        <el-descriptions :column="3" border size="small">
          <el-descriptions-item
            v-for="(value, key) in result.statistics"
            :key="key"
            :label="formatStatLabel(key)"
          >
            {{ formatStatValue(value) }}
          </el-descriptions-item>
        </el-descriptions>
      </div>

      <!-- 文档内容预览 -->
      <div v-if="result.data" class="document-content">
        <h5>处理结果</h5>
        <el-tabs v-model="activeTab" type="border-card">
          <el-tab-pane label="数据预览" name="preview" v-if="result.data.preview">
            <el-table :data="result.data.preview.slice(0, 10)" border size="small" max-height="300px">
              <el-table-column
                v-for="(value, key) in (result.data.preview[0] || {})"
                :key="key"
                :prop="key"
                :label="key"
                show-overflow-tooltip
              />
            </el-table>
          </el-tab-pane>
          <el-tab-pane label="文本内容" name="text" v-if="result.data.text">
            <el-input
              type="textarea"
              :value="result.data.text"
              :rows="8"
              readonly
            />
          </el-tab-pane>
          <el-tab-pane label="原始数据" name="raw">
            <el-input
              type="textarea"
              :value="JSON.stringify(result.data, null, 2)"
              :rows="8"
              readonly
            />
          </el-tab-pane>
        </el-tabs>
      </div>
    </div>

    <!-- 数据分析器结果 -->
    <div v-else-if="isDataAnalyzer" class="data-analyzer-result">
      <h4>数据分析结果</h4>

      <!-- 分析统计 -->
      <div v-if="result.data.stats" class="analysis-stats">
        <h5>统计摘要</h5>
        <el-descriptions :column="3" border size="small">
          <el-descriptions-item
            v-for="(value, key) in result.data.stats"
            :key="key"
            :label="formatStatLabel(key)"
          >
            {{ formatNumber(value) }}
          </el-descriptions-item>
        </el-descriptions>
      </div>

      <!-- 清洗结果 -->
      <div v-if="result.data.cleaned" class="cleaning-results">
        <h5>数据清洗结果</h5>
        <el-descriptions :column="2" border size="small">
          <el-descriptions-item label="原始数据量">{{ result.data.total || 0 }}</el-descriptions-item>
          <el-descriptions-item label="清洗后数据量">{{ result.data.cleaned.length }}</el-descriptions-item>
          <el-descriptions-item label="清洗率">{{ ((result.data.cleaned.length / (result.data.total || 1)) * 100).toFixed(1) }}%</el-descriptions-item>
        </el-descriptions>

        <div style="margin-top: 16px;">
          <h6>清洗后数据预览</h6>
          <el-table :data="result.data.cleaned.slice(0, 5)" border size="small">
            <el-table-column
              v-for="(value, key) in (result.data.cleaned[0] || {})"
              :key="key"
              :prop="key"
              :label="key"
            />
          </el-table>
        </div>
      </div>

      <!-- 异常检测结果 -->
      <div v-if="result.data.anomalies" class="anomaly-results">
        <h5>异常检测结果</h5>
        <el-alert
          :title="`检测到 ${result.data.anomalies.length} 个异常点`"
          :type="result.data.anomalies.length > 0 ? 'warning' : 'success'"
          :closable="false"
          style="margin-bottom: 16px;"
        />

        <el-table v-if="result.data.anomalies.length > 0" :data="result.data.anomalies" border size="small">
          <el-table-column prop="index" label="索引" width="80" />
          <el-table-column prop="value" label="异常值" />
          <el-table-column prop="score" label="异常分数" />
          <el-table-column prop="reason" label="异常原因" />
        </el-table>
      </div>
    </div>

    <!-- 质量工具结果 -->
    <div v-else-if="isQualityTool" class="quality-tool-result">
      <h4>质量分析结果</h4>

      <!-- 质量指标 -->
      <div v-if="result.data.summary" class="quality-summary">
        <h5>质量指标摘要</h5>
        <el-descriptions :column="3" border size="small">
          <el-descriptions-item
            v-for="(value, key) in result.data.summary"
            :key="key"
            :label="formatStatLabel(key)"
          >
            {{ formatQualityValue(value) }}
          </el-descriptions-item>
        </el-descriptions>
      </div>

      <!-- 详细分析结果 -->
      <div v-if="result.data.components || result.data.analysis" class="quality-details">
        <h5>详细分析</h5>
        <el-table :data="result.data.components || result.data.analysis" border size="small">
          <el-table-column
            v-for="(value, key) in ((result.data.components || result.data.analysis)[0] || {})"
            :key="key"
            :prop="key"
            :label="formatStatLabel(key)"
          />
        </el-table>
      </div>
    </div>

    <!-- AI处理器结果 -->
    <div v-else-if="isAIProcessor" class="ai-processor-result">
      <h4>AI处理结果</h4>

      <!-- AI处理统计 -->
      <div v-if="result.data.confidence || result.statistics?.processing_time || result.metadata?.engine" class="ai-stats">
        <h5>处理统计</h5>
        <el-descriptions :column="4" border size="small">
          <el-descriptions-item v-if="result.data.confidence" label="置信度">
            <el-tag :type="getConfidenceType(result.data.confidence)">
              {{ (result.data.confidence * 100).toFixed(1) }}%
            </el-tag>
          </el-descriptions-item>
          <el-descriptions-item v-if="result.statistics?.processing_time" label="处理时间">
            {{ result.statistics.processing_time }}ms
          </el-descriptions-item>
          <el-descriptions-item v-if="result.metadata?.engine" label="处理引擎">
            {{ result.metadata.engine }}
          </el-descriptions-item>
          <el-descriptions-item v-if="result.statistics?.total_words" label="识别词数">
            {{ result.statistics.total_words }}
          </el-descriptions-item>
        </el-descriptions>
      </div>

      <!-- OCR特殊显示 -->
      <div v-if="pluginId === 'ocr_reader'" class="ocr-specific">
        <!-- 词级别识别结果 -->
        <div v-if="result.data.words && result.data.words.length > 0" class="ocr-words">
          <h5>词级别识别结果</h5>
          <div class="words-container" style="max-height: 200px; overflow-y: auto;">
            <el-tag
              v-for="(word, index) in result.data.words.slice(0, 50)"
              :key="index"
              :type="getConfidenceType(word.confidence)"
              style="margin: 2px;"
              :title="`置信度: ${(word.confidence * 100).toFixed(1)}%`"
            >
              {{ word.text }}
            </el-tag>
            <div v-if="result.data.words.length > 50" style="margin-top: 8px;">
              <el-text type="info">... 还有 {{ result.data.words.length - 50 }} 个词</el-text>
            </div>
          </div>
        </div>

        <!-- 图像信息 -->
        <div v-if="result.metadata?.image_info" class="image-info">
          <h5>图像信息</h5>
          <el-descriptions :column="3" border size="small">
            <el-descriptions-item label="图像尺寸">
              {{ result.metadata.image_info.width }} × {{ result.metadata.image_info.height }}
            </el-descriptions-item>
            <el-descriptions-item label="支持语言">
              {{ result.metadata.languages?.join(', ') || 'N/A' }}
            </el-descriptions-item>
            <el-descriptions-item label="内容类型">
              {{ result.metadata.content_type || 'N/A' }}
            </el-descriptions-item>
          </el-descriptions>
        </div>
      </div>

      <!-- AI处理结果 -->
      <div v-if="result.data.text || result.data.summary || result.data.detections" class="ai-content">
        <h5>处理结果</h5>

        <!-- 文本结果 -->
        <div v-if="result.data.text" class="ai-text-result">
          <h6>识别/处理文本</h6>
          <el-input
            type="textarea"
            :value="result.data.text"
            :rows="8"
            readonly
            style="font-family: 'Courier New', monospace;"
          />
          <div style="margin-top: 8px;">
            <el-text type="info">
              字符数: {{ result.data.text.length }} |
              行数: {{ result.data.text.split('\n').length }}
            </el-text>
          </div>
        </div>

        <!-- 摘要结果 -->
        <div v-if="result.data.summary" class="ai-summary-result">
          <h6>文本摘要</h6>
          <el-card>
            <p>{{ result.data.summary }}</p>
            <div v-if="result.statistics?.original_length && result.statistics?.summary_length">
              <el-text type="info">
                压缩比: {{ ((1 - result.statistics.summary_length / result.statistics.original_length) * 100).toFixed(1) }}%
              </el-text>
            </div>
          </el-card>
        </div>

        <!-- 检测结果 -->
        <div v-if="result.data.detections" class="ai-detection-result">
          <h6>检测结果</h6>
          <el-table :data="result.data.detections" border size="small">
            <el-table-column prop="label" label="标签" />
            <el-table-column prop="confidence" label="置信度">
              <template #default="scope">
                <el-tag :type="getConfidenceType(scope.row.confidence)">
                  {{ (scope.row.confidence * 100).toFixed(1) }}%
                </el-tag>
              </template>
            </el-table-column>
            <el-table-column prop="bbox" label="位置" />
          </el-table>
        </div>
      </div>
    </div>

    <!-- 外部连接器结果 -->
    <div v-else-if="isExternalConnector" class="external-connector-result">
      <h4>连接器执行结果</h4>

      <!-- 连接统计 -->
      <div v-if="result.status || result.duration || result.statistics" class="connection-stats">
        <h5>连接统计</h5>
        <el-descriptions :column="4" border size="small">
          <el-descriptions-item v-if="result.status" label="状态码">
            <el-tag :type="getStatusType(result.status)">{{ result.status }}</el-tag>
          </el-descriptions-item>
          <el-descriptions-item v-if="result.duration" label="响应时间">
            <el-tag :type="getDurationTypeByTime(result.duration)">{{ result.duration }}ms</el-tag>
          </el-descriptions-item>
          <el-descriptions-item v-if="result.url" label="请求URL">
            <el-link :href="result.url" target="_blank" style="max-width: 200px; overflow: hidden; text-overflow: ellipsis;">
              {{ result.url }}
            </el-link>
          </el-descriptions-item>
          <el-descriptions-item v-if="result.method" label="请求方法">
            <el-tag type="info">{{ result.method }}</el-tag>
          </el-descriptions-item>
        </el-descriptions>
      </div>

      <!-- 详细统计信息 -->
      <div v-if="result.statistics" class="detailed-stats">
        <h5>详细统计</h5>
        <el-descriptions :column="3" border size="small">
          <el-descriptions-item v-if="result.statistics.response_size" label="响应大小">
            {{ formatBytes(result.statistics.response_size) }}
          </el-descriptions-item>
          <el-descriptions-item v-if="result.statistics.content_type" label="内容类型">
            <el-tag size="small">{{ result.statistics.content_type }}</el-tag>
          </el-descriptions-item>
          <el-descriptions-item v-if="result.metadata?.redirects" label="重定向次数">
            {{ result.metadata.redirects }}
          </el-descriptions-item>
        </el-descriptions>
      </div>

      <!-- 请求/响应头信息 -->
      <div v-if="result.headers || result.metadata?.request_headers" class="headers-info">
        <h5>头信息</h5>
        <el-tabs v-model="headersTab" type="border-card">
          <el-tab-pane v-if="result.metadata?.request_headers" label="请求头" name="request">
            <el-table :data="formatHeaders(result.metadata.request_headers)" border size="small" max-height="200px">
              <el-table-column prop="key" label="字段" width="150" />
              <el-table-column prop="value" label="值" show-overflow-tooltip />
            </el-table>
          </el-tab-pane>
          <el-tab-pane v-if="result.headers" label="响应头" name="response">
            <el-table :data="formatHeaders(result.headers)" border size="small" max-height="200px">
              <el-table-column prop="key" label="字段" width="150" />
              <el-table-column prop="value" label="值" show-overflow-tooltip />
            </el-table>
          </el-tab-pane>
        </el-tabs>
      </div>

      <!-- 响应数据 -->
      <div v-if="result.data" class="response-data">
        <h5>响应数据</h5>
        <el-tabs v-model="responseTab" type="border-card">
          <!-- 格式化显示 -->
          <el-tab-pane label="格式化" name="formatted">
            <div v-if="result.statistics?.is_json" class="json-viewer">
              <pre style="background: #f5f7fa; padding: 12px; border-radius: 4px; max-height: 400px; overflow: auto;">{{ JSON.stringify(result.data, null, 2) }}</pre>
            </div>
            <div v-else-if="result.statistics?.is_html" class="html-viewer">
              <el-alert title="HTML内容" type="info" :closable="false" style="margin-bottom: 8px;" />
              <div style="max-height: 400px; overflow: auto; border: 1px solid #dcdfe6; padding: 8px;">
                <div v-html="result.data"></div>
              </div>
            </div>
            <div v-else class="text-viewer">
              <el-input
                type="textarea"
                :value="typeof result.data === 'string' ? result.data : JSON.stringify(result.data, null, 2)"
                :rows="12"
                readonly
                style="font-family: 'Courier New', monospace;"
              />
            </div>
          </el-tab-pane>

          <!-- 原始数据 -->
          <el-tab-pane label="原始数据" name="raw">
            <el-input
              type="textarea"
              :value="result.data_preview || JSON.stringify(result.data, null, 2)"
              :rows="12"
              readonly
              style="font-family: 'Courier New', monospace;"
            />
          </el-tab-pane>
        </el-tabs>
      </div>

      <!-- 错误信息 -->
      <div v-if="!result.success && result.error" class="error-info">
        <h5>错误信息</h5>
        <el-alert
          :title="result.error"
          :type="result.error_type === 'timeout' ? 'warning' : 'error'"
          :closable="false"
          show-icon
        >
          <template #default>
            <div>
              <p><strong>错误类型:</strong> {{ result.error_type || 'unknown' }}</p>
              <p v-if="result.code"><strong>错误代码:</strong> {{ result.code }}</p>
              <p v-if="result.duration"><strong>耗时:</strong> {{ result.duration }}ms</p>
            </div>
          </template>
        </el-alert>
      </div>
    </div>

    <!-- 通用结果显示 -->
    <div v-else class="generic-result">
      <h4>执行结果</h4>
      <el-input
        type="textarea"
        :value="JSON.stringify(result, null, 2)"
        :rows="10"
        readonly
      />
    </div>
  </div>
</template>

<script setup>
import { computed, ref } from 'vue'

const props = defineProps({
  result: {
    type: Object,
    required: true
  },
  pluginId: {
    type: String,
    required: true
  }
})

// 计算属性 - 基于新的插件类型系统
const pluginType = computed(() => {
  return props.result?.metadata?.plugin_type || 'unknown'
})

const resultType = computed(() => {
  return props.result?.type || 'unknown'
})

const isDocumentProcessor = computed(() => {
  return pluginType.value === 'document_processor' || resultType.value === 'document_result'
})

const isDataAnalyzer = computed(() => {
  return pluginType.value === 'data_analyzer' || resultType.value === 'analysis_result'
})

const isQualityTool = computed(() => {
  return pluginType.value === 'quality_tool' || resultType.value === 'quality_result'
})

const isExternalConnector = computed(() => {
  return pluginType.value === 'external_connector' || resultType.value === 'connector_result'
})

const isAIProcessor = computed(() => {
  return pluginType.value === 'ai_processor' || resultType.value === 'ai_result'
})

// 兼容旧版本
const isParserPlugin = computed(() => {
  return ['csv_parser', 'xlsx_parser', 'json_parser', 'xml_parser', 'pdf_parser', 'docx_parser'].includes(props.pluginId) || isDocumentProcessor.value
})

const isStatisticalPlugin = computed(() => {
  return ['statistical_analyzer', 'data_cleaner', 'anomaly_detector'].includes(props.pluginId) || isDataAnalyzer.value
})

// 响应式数据
const activeTab = ref('preview')
const headersTab = ref('request')
const responseTab = ref('formatted')

// 方法
const formatNumber = (num) => {
  if (typeof num !== 'number') return 'N/A'
  return num.toFixed(3)
}

const getPluginTypeText = (type) => {
  const typeMap = {
    'document_processor': '文档处理器',
    'data_analyzer': '数据分析器',
    'quality_tool': '质量工具',
    'external_connector': '外部连接器',
    'ai_processor': 'AI处理器'
  }
  return typeMap[type] || type || 'N/A'
}

const getResultTypeText = (type) => {
  const typeMap = {
    'document_result': '文档处理结果',
    'analysis_result': '分析结果',
    'quality_result': '质量分析结果',
    'connector_result': '连接器结果',
    'ai_result': 'AI处理结果',
    'error_result': '错误结果'
  }
  return typeMap[type] || type || 'N/A'
}

const formatExecutionTime = (timeStr) => {
  if (!timeStr) return 'N/A'
  try {
    const date = new Date(timeStr)
    return date.toLocaleString('zh-CN')
  } catch {
    return timeStr
  }
}

const formatStatLabel = (key) => {
  const labelMap = {
    'mean': '平均值',
    'median': '中位数',
    'std': '标准差',
    'min': '最小值',
    'max': '最大值',
    'count': '数量',
    'total': '总计',
    'cleaned': '清洗后',
    'removed': '移除',
    'duplicates': '重复项',
    'missing': '缺失值',
    'total_rows': '总行数',
    'total_columns': '总列数',
    'processing_time': '处理时间',
    'confidence': '置信度',
    'accuracy': '准确率'
  }
  return labelMap[key] || key.replace(/_/g, ' ').replace(/\b\w/g, l => l.toUpperCase())
}

const formatStatValue = (value) => {
  if (typeof value === 'number') {
    return formatNumber(value)
  }
  return value || 'N/A'
}

const formatQualityValue = (value) => {
  if (typeof value === 'number') {
    return value.toFixed(2)
  }
  return value || 'N/A'
}

const getStatusType = (status) => {
  if (status >= 200 && status < 300) return 'success'
  if (status >= 400 && status < 500) return 'warning'
  if (status >= 500) return 'danger'
  return 'info'
}

const getConfidenceType = (confidence) => {
  if (confidence >= 0.9) return 'success'
  if (confidence >= 0.7) return 'warning'
  return 'danger'
}

const getDurationTypeByTime = (duration) => {
  if (duration < 1000) return 'success'
  if (duration < 5000) return 'warning'
  return 'danger'
}

const formatBytes = (bytes) => {
  if (bytes === 0) return '0 Bytes'
  const k = 1024
  const sizes = ['Bytes', 'KB', 'MB', 'GB']
  const i = Math.floor(Math.log(bytes) / Math.log(k))
  return parseFloat((bytes / Math.pow(k, i)).toFixed(2)) + ' ' + sizes[i]
}

const formatHeaders = (headers) => {
  if (!headers || typeof headers !== 'object') return []
  return Object.entries(headers).map(([key, value]) => ({
    key,
    value: typeof value === 'string' ? value : JSON.stringify(value)
  }))
}

const getColumnData = () => {
  if (!props.result.columns) return []
  return Object.entries(props.result.columns).map(([name, info]) => ({
    name,
    type: info.type || 'unknown',
    sample: info.sample || 'N/A'
  }))
}

const getRiskLevelType = (level) => {
  switch (level) {
    case 'high': return 'danger'
    case 'medium': return 'warning'
    case 'low': return 'success'
    default: return 'info'
  }
}

const getRiskLevelText = (level) => {
  switch (level) {
    case 'high': return '高风险'
    case 'medium': return '中风险'
    case 'low': return '低风险'
    default: return '未知'
  }
}

const getMSAEvaluationType = (evaluation) => {
  switch (evaluation) {
    case 'acceptable': return 'success'
    case 'marginal': return 'warning'
    case 'unacceptable': return 'danger'
    default: return 'info'
  }
}

const getMSAEvaluationText = (evaluation) => {
  switch (evaluation) {
    case 'acceptable': return '可接受'
    case 'marginal': return '边缘可接受'
    case 'unacceptable': return '不可接受'
    default: return '未知'
  }
}
</script>

<style scoped>
.validation-result {
  padding: 16px;
}

.validation-result h4 {
  margin-bottom: 16px;
  color: #303133;
  border-bottom: 2px solid #409eff;
  padding-bottom: 8px;
}

.validation-result h5 {
  margin: 16px 0 8px 0;
  color: #606266;
}

.pdf-metadata,
.pdf-content,
.pdf-warnings,
.data-preview,
.data-summary,
.column-info,
.stats-summary,
.distribution-info,
.fmea-summary,
.fmea-components,
.msa-recommendations,
.api-response,
.db-results {
  margin-bottom: 20px;
}

.text-stats {
  margin-top: 12px;
  padding: 12px;
  background: #f5f7fa;
  border-radius: 4px;
}

.pdf-content .el-textarea {
  font-family: 'Courier New', monospace;
  font-size: 13px;
  line-height: 1.5;
}

.histogram {
  margin-top: 8px;
  padding: 12px;
  background: #f5f7fa;
  border-radius: 4px;
}

.msa-recommendations ul,
.fmea-result ul {
  margin: 8px 0;
  padding-left: 20px;
}

.msa-recommendations li,
.fmea-result li {
  margin-bottom: 4px;
}

code {
  background: #f5f7fa;
  padding: 2px 4px;
  border-radius: 3px;
  font-family: 'Courier New', monospace;
}
</style>
