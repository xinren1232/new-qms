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

      <!-- 增强的文档内容显示 -->
      <div v-if="result.text || result.data?.content" class="pdf-content">
        <h5>文档解析结果</h5>

        <!-- 内容显示选项卡 -->
        <el-tabs v-model="pdfContentTab" type="border-card">
          <!-- 格式化显示 -->
          <el-tab-pane label="格式化显示" name="formatted">
            <div class="formatted-content" v-html="getFormattedContent()"></div>
          </el-tab-pane>

          <!-- 原始文本 -->
          <el-tab-pane label="原始文本" name="raw">
            <el-input
              type="textarea"
              :value="getRawContent()"
              :rows="12"
              readonly
              placeholder="未提取到文本内容"
              style="font-family: 'Courier New', monospace;"
            />
          </el-tab-pane>

          <!-- 结构化视图 -->
          <el-tab-pane label="结构化视图" name="structured" v-if="hasStructuredContent()">
            <div class="structured-view">
              <el-tree
                :data="getStructuredData()"
                :props="{ children: 'children', label: 'label' }"
                default-expand-all
                node-key="id"
              >
                <template #default="{ node, data }">
                  <span class="tree-node">
                    <el-icon v-if="data.type === 'title'"><Document /></el-icon>
                    <el-icon v-else-if="data.type === 'list'"><List /></el-icon>
                    <el-icon v-else><DocumentCopy /></el-icon>
                    {{ data.label }}
                  </span>
                </template>
              </el-tree>
            </div>
          </el-tab-pane>
        </el-tabs>

        <!-- 文档统计信息 -->
        <div class="document-stats" style="margin-top: 16px;">
          <el-descriptions :column="4" border size="small">
            <el-descriptions-item label="字符数">
              <el-tag type="info">{{ getRawContent().length }}</el-tag>
            </el-descriptions-item>
            <el-descriptions-item label="行数">
              <el-tag type="success">{{ getRawContent().split('\n').length }}</el-tag>
            </el-descriptions-item>
            <el-descriptions-item label="段落数">
              <el-tag type="warning">{{ getParagraphCount() }}</el-tag>
            </el-descriptions-item>
            <el-descriptions-item label="解析器">
              <el-tag type="primary">{{ result.metadata?.parser || result.parser || 'PDF解析器' }}</el-tag>
            </el-descriptions-item>
          </el-descriptions>
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

      <!-- 解析质量评估 -->
      <div class="parsing-quality-assessment">
        <h5>📊 解析质量评估</h5>
        <el-row :gutter="16" style="margin-bottom: 16px;">
          <el-col :span="6">
            <el-card class="quality-card">
              <div class="quality-metric">
                <div class="metric-value">{{ getTextExtractionRate() }}%</div>
                <div class="metric-label">文本提取率</div>
                <el-progress
                  :percentage="getTextExtractionRate()"
                  :color="getQualityColor(getTextExtractionRate())"
                  :show-text="false"
                  :stroke-width="6"
                />
              </div>
            </el-card>
          </el-col>
          <el-col :span="6">
            <el-card class="quality-card">
              <div class="quality-metric">
                <div class="metric-value">{{ getFormatPreservationScore() }}%</div>
                <div class="metric-label">格式保持度</div>
                <el-progress
                  :percentage="getFormatPreservationScore()"
                  :color="getQualityColor(getFormatPreservationScore())"
                  :show-text="false"
                  :stroke-width="6"
                />
              </div>
            </el-card>
          </el-col>
          <el-col :span="6">
            <el-card class="quality-card">
              <div class="quality-metric">
                <div class="metric-value">{{ getMultilingualSupport() }}</div>
                <div class="metric-label">语言支持</div>
                <el-tag :type="getLanguageSupportType()">{{ getMultilingualSupport() }}</el-tag>
              </div>
            </el-card>
          </el-col>
          <el-col :span="6">
            <el-card class="quality-card">
              <div class="quality-metric">
                <div class="metric-value">{{ getStructureRecognition() }}</div>
                <div class="metric-label">结构识别</div>
                <el-tag :type="getStructureType()">{{ getStructureRecognition() }}</el-tag>
              </div>
            </el-card>
          </el-col>
        </el-row>
      </div>

      <!-- 文档统计信息 -->
      <div v-if="result.statistics" class="docx-statistics">
        <h5>📈 文档统计</h5>
        <el-descriptions :column="4" border size="small" style="margin-bottom: 16px;">
          <el-descriptions-item label="字符数">{{ result.statistics.character_count || getTextLength() }}</el-descriptions-item>
          <el-descriptions-item label="词数">{{ result.statistics.word_count || getWordCount() }}</el-descriptions-item>
          <el-descriptions-item label="段落数">{{ result.statistics.paragraph_count || getParagraphCount() }}</el-descriptions-item>
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
      <h4>{{ getParserResultTitle() }}</h4>

      <!-- 数据解析质量评估 -->
      <div v-if="isDataParsingPlugin" class="data-parsing-quality">
        <h5>📊 数据解析质量</h5>
        <el-row :gutter="16" style="margin-bottom: 16px;">
          <el-col :span="6">
            <el-card class="quality-card">
              <div class="quality-metric">
                <div class="metric-value">{{ getDataParsingRate() }}%</div>
                <div class="metric-label">数据解析率</div>
                <el-progress
                  :percentage="getDataParsingRate()"
                  :color="getQualityColor(getDataParsingRate())"
                  :show-text="false"
                  :stroke-width="6"
                />
              </div>
            </el-card>
          </el-col>
          <el-col :span="6">
            <el-card class="quality-card">
              <div class="quality-metric">
                <div class="metric-value">{{ getDataIntegrityScore() }}%</div>
                <div class="metric-label">数据完整性</div>
                <el-progress
                  :percentage="getDataIntegrityScore()"
                  :color="getQualityColor(getDataIntegrityScore())"
                  :show-text="false"
                  :stroke-width="6"
                />
              </div>
            </el-card>
          </el-col>
          <el-col :span="6">
            <el-card class="quality-card">
              <div class="quality-metric">
                <div class="metric-value">{{ getDataStructureType() }}</div>
                <div class="metric-label">数据结构</div>
                <el-tag :type="getDataStructureTagType()">{{ getDataStructureType() }}</el-tag>
              </div>
            </el-card>
          </el-col>
          <el-col :span="6">
            <el-card class="quality-card">
              <div class="quality-metric">
                <div class="metric-value">{{ getDataValidationStatus() }}</div>
                <div class="metric-label">数据验证</div>
                <el-tag :type="getValidationTagType()">{{ getDataValidationStatus() }}</el-tag>
              </div>
            </el-card>
          </el-col>
        </el-row>
      </div>

      <!-- 数据预览 -->
      <div v-if="result.preview || result.data?.preview" class="data-preview">
        <h5>📋 数据预览 (前{{ Math.min((result.preview || result.data?.preview || []).length, 10) }}行)</h5>
        <el-table
          :data="(result.preview || result.data?.preview || []).slice(0, 10)"
          border
          size="small"
          max-height="300px"
          style="margin-bottom: 16px;"
        >
          <el-table-column
            v-for="(value, key) in ((result.preview || result.data?.preview || [])[0] || {})"
            :key="key"
            :prop="key"
            :label="key"
            show-overflow-tooltip
          />
        </el-table>

        <!-- 无数据提示 -->
        <div v-if="!(result.preview || result.data?.preview) || (result.preview || result.data?.preview || []).length === 0" class="no-data-hint">
          <el-alert
            title="未检测到结构化数据"
            description="该插件可能需要特定格式的输入数据，请检查输入格式是否正确"
            type="warning"
            :closable="false"
            show-icon
          />
        </div>
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

      <!-- 结果类型检测和智能显示 -->
      <div v-if="isTextResult" class="text-result">
        <h5>文本结果</h5>
        <el-input
          type="textarea"
          :value="getTextContent()"
          :rows="8"
          readonly
          style="font-family: 'Courier New', monospace;"
        />
        <div class="text-stats">
          <el-tag type="info" style="margin-right: 8px;">
            字符数: {{ getTextContent().length }}
          </el-tag>
          <el-tag type="success">
            行数: {{ getTextContent().split('\n').length }}
          </el-tag>
        </div>
      </div>

      <div v-else-if="isTableResult" class="table-result">
        <h5>表格数据</h5>
        <el-table :data="getTableData().slice(0, 10)" border size="small" max-height="300px">
          <el-table-column
            v-for="(value, key) in (getTableData()[0] || {})"
            :key="key"
            :prop="key"
            :label="key"
            show-overflow-tooltip
          />
        </el-table>
        <div v-if="getTableData().length > 10" style="margin-top: 8px;">
          <el-text type="info">显示前10行，共{{ getTableData().length }}行数据</el-text>
        </div>
      </div>

      <div v-else-if="isBinaryResult" class="binary-result">
        <h5>二进制数据</h5>
        <el-alert
          title="检测到二进制数据"
          :description="`数据大小: ${getBinarySize()}`"
          type="info"
          :closable="false"
          show-icon
        />
        <div style="margin-top: 16px;">
          <el-button @click="downloadBinaryData" type="primary" size="small">
            下载数据
          </el-button>
        </div>
      </div>

      <div v-else class="json-result">
        <h5>结构化数据</h5>
        <el-tabs v-model="genericTab" type="border-card">
          <el-tab-pane label="格式化显示" name="formatted">
            <pre class="json-viewer">{{ getFormattedJSON() }}</pre>
          </el-tab-pane>
          <el-tab-pane label="原始数据" name="raw">
            <el-input
              type="textarea"
              :value="getRawJSON()"
              :rows="10"
              readonly
              style="font-family: 'Courier New', monospace;"
            />
          </el-tab-pane>
        </el-tabs>
      </div>
    </div>
  </div>
</template>

<script setup>
import { computed, ref } from 'vue'
import { ElMessage } from 'element-plus'

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
const genericTab = ref('formatted')
const pdfContentTab = ref('formatted')

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

// DOCX解析质量评估方法
const getTextLength = () => {
  const text = props.result.text || props.result.data?.text || ''
  return text.length
}

const getWordCount = () => {
  const text = props.result.text || props.result.data?.text || ''
  return text.split(/\s+/).filter(word => word.length > 0).length
}

const getParagraphCount = () => {
  const text = props.result.text || props.result.data?.text || ''
  return text.split('\n').filter(line => line.trim().length > 0).length
}

const getTextExtractionRate = () => {
  // 基于文本长度和内容复杂度评估提取率
  const textLength = getTextLength()
  if (textLength === 0) return 0
  if (textLength < 50) return 85
  if (textLength < 200) return 95
  return 100
}

const getFormatPreservationScore = () => {
  // 基于HTML输出和结构保持评估格式保持度
  const hasHtml = !!(props.result.html || props.result.data?.html)
  const hasStructure = getParagraphCount() > 1
  const hasMetadata = !!(props.result.metadata)

  let score = 60 // 基础分
  if (hasHtml) score += 25
  if (hasStructure) score += 10
  if (hasMetadata) score += 5

  return Math.min(100, score)
}

const getMultilingualSupport = () => {
  const text = props.result.text || props.result.data?.text || ''
  const hasEnglish = /[a-zA-Z]/.test(text)
  const hasChinese = /[\u4e00-\u9fff]/.test(text)

  if (hasEnglish && hasChinese) return '中英文混合'
  if (hasChinese) return '中文'
  if (hasEnglish) return '英文'
  return '未检测'
}

const getStructureRecognition = () => {
  const hasHtml = !!(props.result.html || props.result.data?.html)
  const hasMetadata = !!(props.result.metadata)
  const paragraphs = getParagraphCount()

  if (hasHtml && paragraphs > 3) return '完整结构'
  if (hasMetadata && paragraphs > 1) return '基本结构'
  if (paragraphs > 1) return '段落结构'
  return '平面文本'
}

const getQualityColor = (score) => {
  if (score >= 90) return '#67c23a'
  if (score >= 70) return '#e6a23c'
  return '#f56c6c'
}

const getLanguageSupportType = () => {
  const support = getMultilingualSupport()
  if (support === '中英文混合') return 'success'
  if (support === '中文' || support === '英文') return 'warning'
  return 'info'
}

const getStructureType = () => {
  const structure = getStructureRecognition()
  if (structure === '完整结构') return 'success'
  if (structure === '基本结构' || structure === '段落结构') return 'warning'
  return 'info'
}

// 数据解析质量评估方法
const isDataParsingPlugin = computed(() => {
  return ['csv_parser', 'xlsx_parser', 'json_parser', 'xml_parser'].includes(props.pluginId)
})

const getParserResultTitle = () => {
  const titleMap = {
    'csv_parser': 'CSV解析结果',
    'xlsx_parser': 'Excel解析结果',
    'json_parser': 'JSON解析结果',
    'xml_parser': 'XML解析结果',
    'pdf_parser': 'PDF解析结果'
  }
  return titleMap[props.pluginId] || '解析结果'
}

const getDataParsingRate = () => {
  const preview = props.result.preview || props.result.data?.preview || []
  const hasData = preview.length > 0
  const hasColumns = Object.keys(preview[0] || {}).length > 0

  if (!hasData) return 0
  if (hasColumns && preview.length >= 3) return 100
  if (hasColumns && preview.length >= 1) return 80
  return 50
}

const getDataIntegrityScore = () => {
  const preview = props.result.preview || props.result.data?.preview || []
  if (preview.length === 0) return 0

  const totalCells = preview.length * Object.keys(preview[0] || {}).length
  let validCells = 0

  preview.forEach(row => {
    Object.values(row).forEach(value => {
      if (value !== null && value !== undefined && value !== '') {
        validCells++
      }
    })
  })

  return totalCells > 0 ? Math.round((validCells / totalCells) * 100) : 0
}

const getDataStructureType = () => {
  const preview = props.result.preview || props.result.data?.preview || []
  if (preview.length === 0) return '无结构'

  const columnCount = Object.keys(preview[0] || {}).length
  const rowCount = preview.length

  if (columnCount >= 5 && rowCount >= 10) return '复杂表格'
  if (columnCount >= 3 && rowCount >= 5) return '标准表格'
  if (columnCount >= 2) return '简单表格'
  return '列表数据'
}

const getDataValidationStatus = () => {
  const hasMetadata = !!(props.result.metadata)
  const hasWarnings = !!(props.result.warnings && props.result.warnings.length > 0)
  const hasData = !!(props.result.preview || props.result.data?.preview)

  if (hasData && hasMetadata && !hasWarnings) return '验证通过'
  if (hasData && hasWarnings) return '有警告'
  if (!hasData) return '验证失败'
  return '部分验证'
}

const getDataStructureTagType = () => {
  const type = getDataStructureType()
  if (type === '复杂表格' || type === '标准表格') return 'success'
  if (type === '简单表格') return 'warning'
  return 'info'
}

const getValidationTagType = () => {
  const status = getDataValidationStatus()
  if (status === '验证通过') return 'success'
  if (status === '有警告' || status === '部分验证') return 'warning'
  return 'danger'
}

// 通用结果智能检测
const isTextResult = computed(() => {
  // 检测是否为纯文本结果
  if (typeof props.result === 'string') return true
  if (props.result.text && typeof props.result.text === 'string') return true
  if (props.result.data?.text && typeof props.result.data.text === 'string') return true
  return false
})

const isTableResult = computed(() => {
  // 检测是否为表格数据
  const data = getTableData()
  return Array.isArray(data) && data.length > 0 && typeof data[0] === 'object'
})

const isBinaryResult = computed(() => {
  // 检测是否为二进制数据
  if (props.result.base64) return true
  if (props.result.data?.base64) return true
  if (props.result.buffer) return true
  if (props.result.data?.buffer) return true
  return false
})

const getTextContent = () => {
  if (typeof props.result === 'string') return props.result
  if (props.result.text) return props.result.text
  if (props.result.data?.text) return props.result.data.text
  if (props.result.content) return props.result.content
  if (props.result.data?.content) return props.result.data.content
  return ''
}

const getTableData = () => {
  if (Array.isArray(props.result)) return props.result
  if (Array.isArray(props.result.data)) return props.result.data
  if (Array.isArray(props.result.preview)) return props.result.preview
  if (Array.isArray(props.result.data?.preview)) return props.result.data.preview
  if (Array.isArray(props.result.results)) return props.result.results
  if (Array.isArray(props.result.data?.results)) return props.result.data.results
  return []
}

const getBinarySize = () => {
  const base64 = props.result.base64 || props.result.data?.base64
  if (base64) {
    const bytes = Math.ceil(base64.length * 3 / 4)
    return formatBytes(bytes)
  }
  return '未知大小'
}

const downloadBinaryData = () => {
  const base64 = props.result.base64 || props.result.data?.base64
  if (!base64) {
    ElMessage.warning('没有可下载的二进制数据')
    return
  }

  try {
    const byteCharacters = atob(base64)
    const byteNumbers = new Array(byteCharacters.length)
    for (let i = 0; i < byteCharacters.length; i++) {
      byteNumbers[i] = byteCharacters.charCodeAt(i)
    }
    const byteArray = new Uint8Array(byteNumbers)
    const blob = new Blob([byteArray])

    const url = URL.createObjectURL(blob)
    const a = document.createElement('a')
    a.href = url
    a.download = `${props.pluginId}_result_${Date.now()}.bin`
    a.click()
    URL.revokeObjectURL(url)

    ElMessage.success('文件下载已开始')
  } catch (error) {
    ElMessage.error('下载失败: ' + error.message)
  }
}

const getFormattedJSON = () => {
  try {
    // 过滤掉可能导致显示问题的字段
    const filteredResult = filterDisplayData(props.result)
    return JSON.stringify(filteredResult, null, 2)
  } catch (error) {
    return '数据格式化失败: ' + error.message
  }
}

const getRawJSON = () => {
  try {
    return JSON.stringify(props.result, null, 2)
  } catch (error) {
    return '数据序列化失败: ' + error.message
  }
}

const filterDisplayData = (data) => {
  if (!data || typeof data !== 'object') return data

  const filtered = {}
  for (const [key, value] of Object.entries(data)) {
    // 跳过可能包含二进制数据的字段
    if (key === 'base64' || key === 'buffer' || key === 'raw') {
      filtered[key] = `[${typeof value}数据 - 已隐藏显示]`
      continue
    }

    // 处理字符串值
    if (typeof value === 'string') {
      // 检测是否包含大量特殊字符或乱码
      if (value.length > 1000) {
        filtered[key] = value.substring(0, 500) + '... [内容过长，已截断]'
      } else if (isLikelyBinaryString(value)) {
        filtered[key] = '[二进制字符串 - 已隐藏显示]'
      } else {
        filtered[key] = value
      }
    } else if (Array.isArray(value)) {
      // 限制数组显示长度
      if (value.length > 20) {
        filtered[key] = [...value.slice(0, 20), `... 还有${value.length - 20}项`]
      } else {
        filtered[key] = value.map(item =>
          typeof item === 'object' ? filterDisplayData(item) : item
        )
      }
    } else if (typeof value === 'object' && value !== null) {
      filtered[key] = filterDisplayData(value)
    } else {
      filtered[key] = value
    }
  }

  return filtered
}

const isLikelyBinaryString = (str) => {
  // 检测字符串是否可能是二进制数据
  if (str.length === 0) return false

  // 检查是否包含大量不可打印字符
  let nonPrintableCount = 0
  for (let i = 0; i < Math.min(str.length, 100); i++) {
    const charCode = str.charCodeAt(i)
    if (charCode < 32 && charCode !== 9 && charCode !== 10 && charCode !== 13) {
      nonPrintableCount++
    }
  }

  // 如果超过20%的字符是不可打印字符，认为是二进制数据
  return (nonPrintableCount / Math.min(str.length, 100)) > 0.2
}

// PDF/文档解析相关方法
const getRawContent = () => {
  return props.result.text || props.result.data?.content || props.result.data?.rawContent || ''
}

const getFormattedContent = () => {
  const content = props.result.data?.content || props.result.text || ''
  if (!content) return '<p>暂无内容</p>'

  // 将Markdown格式转换为HTML
  return content
    .replace(/### (.*)/g, '<h3 style="color: #409EFF; margin: 16px 0 8px 0;">$1</h3>')
    .replace(/## (.*)/g, '<h2 style="color: #67C23A; margin: 20px 0 12px 0;">$1</h2>')
    .replace(/#### (.*)/g, '<h4 style="color: #E6A23C; margin: 12px 0 6px 0;">$1</h4>')
    .replace(/\*\*(.*?)\*\*/g, '<strong style="color: #F56C6C;">$1</strong>')
    .replace(/\n\n/g, '</p><p style="margin: 8px 0; line-height: 1.6;">')
    .replace(/^/, '<p style="margin: 8px 0; line-height: 1.6;">')
    .replace(/$/, '</p>')
}

const hasStructuredContent = () => {
  const content = getRawContent()
  return content.includes('##') || content.includes('**') || content.includes('D1') || content.includes('What')
}

const getStructuredData = () => {
  const content = getRawContent()
  const lines = content.split('\n').filter(line => line.trim())
  const structured = []
  let id = 1

  lines.forEach(line => {
    const trimmed = line.trim()
    if (trimmed.startsWith('##')) {
      structured.push({
        id: id++,
        label: trimmed.replace(/^#+\s*/, ''),
        type: 'title',
        children: []
      })
    } else if (trimmed.startsWith('**') && trimmed.endsWith('**')) {
      structured.push({
        id: id++,
        label: trimmed.replace(/\*\*/g, ''),
        type: 'key-info',
        children: []
      })
    } else if (trimmed.match(/^[•\-\*]\s/)) {
      structured.push({
        id: id++,
        label: trimmed.replace(/^[•\-\*]\s/, ''),
        type: 'list',
        children: []
      })
    }
  })

  return structured
}

const getParagraphCount = () => {
  const content = getRawContent()
  return content.split('\n\n').filter(p => p.trim()).length
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

/* 解析质量评估样式 */
.parsing-quality-assessment {
  margin-bottom: 24px;
  padding: 16px;
  background: linear-gradient(135deg, #f0f9ff 0%, #e0f2fe 100%);
  border-radius: 8px;
  border: 1px solid #409eff;
}

.quality-card {
  text-align: center;
  border: none;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
}

.quality-card .el-card__body {
  padding: 16px 12px;
}

.quality-metric {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 8px;
}

.metric-value {
  font-size: 24px;
  font-weight: bold;
  color: #303133;
  line-height: 1;
}

.metric-label {
  font-size: 12px;
  color: #606266;
  margin-bottom: 4px;
}

.quality-card .el-progress {
  width: 100%;
}

.quality-card .el-tag {
  font-size: 11px;
  padding: 2px 6px;
}

/* 通用结果显示样式 */
.generic-result {
  margin-top: 16px;
}

.text-result,
.table-result,
.binary-result,
.json-result {
  margin-bottom: 20px;
}

.json-viewer {
  background: #f5f7fa;
  padding: 16px;
  border-radius: 4px;
  max-height: 400px;
  overflow: auto;
  font-family: 'Courier New', monospace;
  font-size: 13px;
  line-height: 1.4;
  white-space: pre-wrap;
  word-break: break-all;
}

.generic-result .text-stats {
  margin-top: 12px;
  padding: 12px;
  background: #f5f7fa;
  border-radius: 4px;
}

.binary-result .el-alert {
  margin-bottom: 16px;
}

/* 防止长文本溢出 */
.generic-result .el-textarea__inner {
  font-family: 'Courier New', monospace;
  font-size: 13px;
  line-height: 1.4;
  word-break: break-all;
}

/* 表格样式优化 */
.table-result .el-table {
  margin-bottom: 16px;
}

.table-result .el-table .cell {
  word-break: break-all;
  max-width: 200px;
}

/* 格式化内容样式 */
.formatted-content {
  max-height: 400px;
  overflow-y: auto;
  padding: 16px;
  background: #fafafa;
  border: 1px solid #e4e7ed;
  border-radius: 4px;
  line-height: 1.6;
}

.formatted-content h2 {
  border-bottom: 2px solid #67C23A;
  padding-bottom: 8px;
}

.formatted-content h3 {
  border-left: 4px solid #409EFF;
  padding-left: 12px;
}

.formatted-content h4 {
  border-left: 3px solid #E6A23C;
  padding-left: 10px;
}

.structured-view {
  max-height: 400px;
  overflow-y: auto;
  border: 1px solid #e4e7ed;
  border-radius: 4px;
  padding: 12px;
}

.tree-node {
  display: flex;
  align-items: center;
  gap: 8px;
}

.document-stats .el-descriptions {
  background: #f8f9fa;
}
</style>
