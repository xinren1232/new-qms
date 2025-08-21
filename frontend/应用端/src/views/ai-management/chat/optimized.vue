<template>
  <div class="optimized-chat-container">
    <!-- 顶部工具栏 -->
    <div class="chat-toolbar">
      <div class="toolbar-left">
        <div class="model-selector">
          <el-select
            v-model="currentModel"
            @change="switchModel"
            placeholder="选择AI模型"
            style="width: 200px"
          >
            <el-option
              v-for="model in availableModels"
              :key="model.provider"
              :label="getModelDisplayName(model)"
              :value="model.provider"
            >
              <div class="model-option">
                <span class="model-name">{{ model.name }}</span>
                <el-tag
                  v-if="model.features && model.features.external"
                  type="success"
                  size="small"
                >
                  外网可用
                </el-tag>
              </div>
            </el-option>
          </el-select>
        </div>

        <!-- 模型状态和特性展示 -->
        <div class="model-status-features">
          <div class="model-status">
            <el-tag :type="modelStatus.status === 'online' ? 'success' : 'danger'" size="small">
              {{ modelStatus.status === 'online' ? '在线' : '离线' }}
            </el-tag>
            <span class="status-text">{{ modelStatus.provider }}</span>
          </div>

          <!-- 当前模型特性指标 -->
          <div class="model-features-inline">
            <div class="feature-item" v-if="currentModelFeatures.reasoning">
              <div class="feature-indicator active"></div>
              <span>推理</span>
            </div>
            <div class="feature-item" v-if="currentModelFeatures.toolCalls">
              <div class="feature-indicator active"></div>
              <span>工具</span>
            </div>
            <div class="feature-item" v-if="currentModelFeatures.vision">
              <div class="feature-indicator active"></div>
              <span>视觉</span>
            </div>
          </div>
        </div>
      </div>

      <!-- 中间状态展示区域 -->
      <div class="toolbar-center">
        <div class="status-metrics-inline">
          <div class="metric-group">
            <div class="metric-item-inline">
              <span class="metric-label-inline">执行情况</span>
              <span class="metric-value-inline">{{ modelStatus.provider }}</span>
            </div>
            <div class="metric-item-inline">
              <span class="metric-label-inline">响应时间</span>
              <span class="metric-value-inline">{{ lastResponseTime }}ms</span>
            </div>
            <div class="metric-item-inline">
              <span class="metric-label-inline">对话轮次</span>
              <span class="metric-value-inline">{{ Math.floor(messages.length / 2) || 0 }}</span>
            </div>
          </div>
          <div class="divider-vertical"></div>
          <div class="metric-group">
            <div class="metric-item-inline">
              <span class="metric-label-inline">使用统计</span>
              <span class="metric-value-inline highlight">{{ userStats.totalConversations || 0 }}</span>
            </div>
            <div class="metric-item-inline">
              <span class="metric-label-inline">总对话数</span>
              <span class="metric-value-inline highlight">{{ userStats.totalMessages || 0 }}</span>
            </div>
          </div>
        </div>
      </div>

      <div class="toolbar-right">
        <el-button @click="exportCurrentChat" size="small" text>
          <el-icon><Download /></el-icon>
          导出对话
        </el-button>
        <el-upload
          :http-request="uploadCaseExcel"
          :show-file-list="false"
          :accept="acceptedFileTypes"
          style="margin: 0 8px;"
        >
          <el-button size="small" text>
            <el-icon><Upload /></el-icon>
            导入案例
          </el-button>
        </el-upload>
        <el-button @click="handleCaseGuidance" size="small" type="success">
          <el-icon><DocumentChecked /></el-icon>
          案例指导
        </el-button>
        <el-button @click="newConversation" size="small" type="primary" style="margin-left: 8px;">
          <el-icon><Plus /></el-icon>
          新对话
        </el-button>
      </div>
    </div>

    <!-- 主要对话区域 -->
    <div class="main-chat-area">
      <!-- 左侧功能面板 -->
      <div class="left-sidebar">
        <!-- 对话管理 -->
        <div class="sidebar-section">
          <div class="section-header centered">
            <el-icon><Document /></el-icon>
            <span>对话管理</span>
            <el-button
              size="small"
              text
              @click="goToFullHistory"
              title="查看完整历史记录"
              style="margin-left: auto;"
            >
              <el-icon><Document /></el-icon>
              历史
            </el-button>
          </div>

          <div class="conversation-list">
            <div
              v-for="conversation in recentConversations.slice(0, 8)"
              :key="conversation.id"
              class="conversation-item"
              :class="{ active: conversation.id === currentConversationId }"
              @click="loadConversation(conversation.id)"
            >
              <div class="conversation-content">
                <div class="conversation-header">
                  <div class="conversation-title">{{ truncateTitle(conversation.title || '新对话') }}</div>
                  <div class="conversation-status">
                    <el-icon class="status-icon"><ChatDotRound /></el-icon>
                  </div>
                </div>
                <div class="conversation-preview">
                  {{ truncatePreview(conversation.last_message || '暂无消息') }}
                </div>
                <div class="conversation-footer">
                  <span class="conversation-time">{{ formatRelativeTime(conversation.updated_at) }}</span>
                  <span class="message-count">{{ conversation.message_count || 0 }}条</span>
                </div>
              </div>
            </div>

            <div v-if="recentConversations.length === 0" class="empty-conversations">
              <el-icon><ChatDotRound /></el-icon>
              <span>暂无对话记录</span>
            </div>

            <!-- 查看更多按钮 -->
            <div v-if="recentConversations.length > 8" class="view-more-conversations">
              <el-button
                text
                size="small"
                @click="goToFullHistory"
                class="view-more-btn"
              >
                <el-icon><ArrowRight /></el-icon>
                查看全部 ({{ recentConversations.length }}条)
              </el-button>
            </div>
          </div>
        </div>

        <!-- 工具调用 -->
        <div class="sidebar-section">
          <div class="section-header centered">
            <el-icon><Tools /></el-icon>
            <span>工具调用</span>
          </div>

          <!-- 工具分类标签 -->
          <div class="tool-categories">
            <el-button
              v-for="category in toolCategories"
              :key="category.key"
              :type="currentToolCategory === category.key ? 'primary' : ''"
              size="small"
              @click="switchToolCategory(category.key)"
              class="category-btn"
            >
              <el-icon><component :is="category.icon" /></el-icon>
              {{ category.name }}
            </el-button>
          </div>

          <!-- 工具列表 -->
          <div class="tools-container">
            <div class="tools-list">
              <div
                v-for="tool in currentPageTools"
                :key="tool.id"
                class="tool-item"
                @click="invokeTool(tool)"
              >
                <div class="tool-icon">
                  <el-icon><component :is="tool.icon" /></el-icon>
                </div>
                <div class="tool-info">
                  <div class="tool-name">{{ tool.name }}</div>
                  <div class="tool-desc">{{ tool.description }}</div>
                </div>
                <div class="tool-status">
                  <el-tag
                    :type="tool.status === 'available' ? 'success' : 'info'"
                    size="small"
                  >
                    {{ tool.status === 'available' ? '可用' : '开发中' }}
                  </el-tag>
                </div>
              </div>
            </div>


            <!-- 分页控制 -->
            <div v-if="totalToolPages > 1" class="tools-pagination">
              <el-button
                size="small"
                :disabled="currentToolPage === 1"
                @click="prevToolPage"
                text
              >
                <el-icon><ArrowLeft /></el-icon>
              </el-button>
              <span class="page-info">{{ currentToolPage }}/{{ totalToolPages }}</span>
              <el-button
                size="small"
                :disabled="currentToolPage === totalToolPages"
                @click="nextToolPage"
                text
              >
                <el-icon><ArrowRight /></el-icon>
              </el-button>
            </div>
          </div>
        </div>

        <!-- 便捷对话 -->
        <div class="sidebar-section">
          <div class="section-header centered">
            <el-icon><ChatLineRound /></el-icon>
            <span>便捷对话</span>
          </div>

          <div class="quick-questions">
            <div
              v-for="category in quickQuestionCategories"
              :key="category.name"
              class="question-category"
            >
              <div class="category-title">{{ category.name }}</div>
              <div class="question-list">
                <div
                  v-for="question in category.questions"
                  :key="question.id"
                  class="question-item"
                  @click="sendQuickQuestion(question.text)"
                >
                  <el-icon><component :is="question.icon" /></el-icon>
                  <span>{{ question.title }}</span>
                </div>
              </div>
            </div>
          </div>
        </div>
      </div>

      <!-- 中央对话区域 -->
      <div class="conversation-area">
        <div class="messages-container" ref="messagesContainer">
          <div v-if="messages.length === 0" class="welcome-message">
            <div class="welcome-content">
              <div class="robot-logo">
                <div class="robot-head">
                  <div class="robot-antenna left"></div>
                  <div class="robot-antenna right"></div>
                  <div class="robot-face">
                    <div class="robot-eyes">
                      <div class="robot-eye left"></div>
                      <div class="robot-eye right"></div>
                    </div>
                    <div class="robot-mouth"></div>
                  </div>
                </div>
              </div>
              <h2 class="welcome-title">欢迎使用QMS AI智能助手</h2>
              <p class="welcome-subtitle">我是您的质量管理专家，可以帮助您解决质量管理相关问题</p>
            </div>
          </div>

          <div
            v-for="(message, index) in messages"
            :key="index"
            class="message-item"
            :class="message.role"
          >
            <div class="message-avatar">
              <el-avatar v-if="message.role === 'user'" :size="32">
                <el-icon><User /></el-icon>
              </el-avatar>
              <el-avatar v-else :size="32" style="background: linear-gradient(135deg, #409EFF, #66b1ff)">
                <el-icon><Cpu /></el-icon>
              </el-avatar>
            </div>

            <div class="message-content">
              <div class="message-header">
                <span class="sender-name">{{ message.role === 'user' ? '您' : 'AI助手' }}</span>
                <span class="message-time">{{ formatTime(message.timestamp) }}</span>
              </div>

              <!-- 文件显示区域 -->
              <div v-if="message.files && message.files.length > 0" class="message-files">
                <div class="files-title">
                  <el-icon><Document /></el-icon>
                  <span>附件 ({{ message.files.length }})</span>
                </div>
                <div class="files-grid">
                  <div
                    v-for="(file, fileIndex) in message.files"
                    :key="fileIndex"
                    class="file-card"
                  >
                    <div class="file-card-icon">
                      <el-icon><component :is="getFileIcon(file.type)" /></el-icon>
                    </div>
                    <div class="file-card-info">
                      <div class="file-card-name">{{ file.name }}</div>
                      <div class="file-card-size">{{ formatFileSize(file.size) }}</div>
                    </div>
                  </div>
                </div>
              </div>

              <div v-if="message.parsed" class="parsed-preview">
                <div class="parsed-header">
                  <el-tag type="info" size="small">{{ message.parsed.pluginId }}</el-tag>
                  <!-- 表格型统计 -->
                  <span v-if="isTabularParsed(message.parsed) && getParsedRowCount(message.parsed) !== null" class="parsed-meta">行：{{ getParsedRowCount(message.parsed) }}</span>
                  <span v-if="isTabularParsed(message.parsed) && getParsedColumns(message.parsed).length" class="parsed-meta">列：{{ getParsedColumns(message.parsed).length }}</span>
                  <span v-if="(message.parsed.res?.sheet_names||[]).length" class="parsed-meta">Sheet：{{ message.parsed.res.sheet_names.join(', ') }}</span>
                  <!-- 文本型统计 -->
                  <span v-if="hasTextParsed(message.parsed)" class="parsed-meta">字数：{{ getTextStats(message.parsed).chars }}</span>
                  <span v-if="hasTextParsed(message.parsed) && getTextStats(message.parsed).paras !== null" class="parsed-meta">段落：{{ getTextStats(message.parsed).paras }}</span>
                  <div class="parsed-actions">
                    <el-button size="small" text @click="openParsedDialog(message.parsed)">展开全部</el-button>
                    <el-button v-if="isTabularParsed(message.parsed)" size="small" text @click="exportParsedCSV(message.parsed)">导出CSV</el-button>
                    <el-button v-if="hasTextParsed(message.parsed)" size="small" text @click="copyParsedText(message.parsed)">复制文本</el-button>
                    <el-button v-if="hasTextParsed(message.parsed)" size="small" text @click="exportParsedText(message.parsed)">导出TXT</el-button>
                    <el-button v-if="hasTextParsed(message.parsed)" size="small" text type="success" @click="exportParsedMarkdown(message.parsed)">导出MD</el-button>
                    <el-button v-if="message.parsed" size="small" text type="primary" @click="copyBasicSummary(message.parsed)">复制汇总</el-button>
                  </div>
                </div>

                <!-- 初步汇总（可折叠） -->
                <el-collapse style="margin-top: 8px;">
                  <el-collapse-item name="basic-summary">
                    <template #title>
                      <span style="font-weight: 600;">初步汇总</span>
                    </template>
                    <pre class="basic-summary-text">{{ stringifyBasicSummary(message.parsed.basicSummary || buildBasicSummary(message.parsed.res)) }}</pre>
                  </el-collapse-item>
                </el-collapse>

                <div v-if="isTabularParsed(message.parsed)">
                  <el-table :data="getParsedPreview(message.parsed, 10)" size="small" border style="width: 100%; margin-top: 6px;">
                    <el-table-column v-for="col in getParsedColumns(message.parsed)" :key="col" :prop="col" :label="col" :min-width="100" />
                  </el-table>
                </div>
                <template v-else>
                  <div class="text-actions" style="margin-top:6px;">
                    <el-input v-model="textPreviewQuery" size="small" placeholder="搜索/高亮原文" clearable style="width:260px" />
                  </div>
                  <div class="text-highlight compact" v-html="getTextPreviewHTML(message.parsed, textPreviewQuery, 1600)"></div>
                </template>
              </div>
              <div v-else class="message-text" v-html="formatMessage(message.content)"></div>


      <!-- 解析结果弹窗（多Tab） -->
      <el-dialog v-model="parsedDialogVisible" title="解析结果" width="80%">
        <el-tabs type="border-card">
          <el-tab-pane label="概览">
            <pre class="basic-summary-text">{{ stringifyBasicSummary(parsedDialogData?.basicSummary || buildBasicSummary(parsedDialogData?.res)) }}</pre>
          </el-tab-pane>
          <el-tab-pane label="预览">
            <el-table v-if="isTabularParsed(parsedDialogData)" :data="getParsedPreview(parsedDialogData, 50)" size="small" border style="width: 100%">
              <el-table-column v-for="col in getParsedColumns(parsedDialogData)" :key="col" :prop="col" :label="col" :min-width="120" />
            </el-table>
            <template v-else>
              <div style="display:flex; gap:12px; align-items:flex-start;">
                <div style="width:220px; flex: 0 0 auto; background:#fff; border:1px solid #e5e7eb; border-radius:6px; padding:8px; max-height:50vh; overflow:auto;">
                  <div v-if="dialogOutline.length" style="font-size:12px; color:#475569; margin-bottom:6px;">文档大纲</div>
                  <el-empty v-else description="无识别到标题" :image-size="50" />
                  <ul style="list-style:none; padding-left:0; margin:0;">
                    <li v-for="(h,i) in dialogOutline" :key="h.id" :style="{ margin:'4px 0', paddingLeft: h.level===3 ? '0' : '12px' }">
                      <a :href="'#'+h.id" :class="{active: h.id===activeHeadingId}" style="font-size:12px; text-decoration:none;">{{ h.title }}</a>
                    </li>
                  </ul>
                </div>
                <div style="flex:1 1 auto;">
                  <div style="display:flex; gap:8px; align-items:center; margin-bottom:6px; flex-wrap:wrap;">
                    <el-input v-model="dialogTextQuery" size="small" placeholder="搜索/高亮原文" clearable style="width:260px" />
                    <el-button size="small" @click="jumpToMark(false)" :disabled="!dialogTextQuery">上一处</el-button>
                    <el-button size="small" @click="jumpToMark(true)" :disabled="!dialogTextQuery">下一处</el-button>
                    <el-divider direction="vertical" />
                    <el-button size="small" @click="dialogFontSize = Math.max(12, dialogFontSize - 1)">A-</el-button>
                    <el-button size="small" @click="dialogFontSize = Math.min(18, dialogFontSize + 1)">A+</el-button>
                    <el-switch v-model="dialogOnlyMatches" active-text="仅命中段落" :disabled="!dialogTextQuery" />
                  </div>
                  <div ref="dialogTextContainerRef" class="text-highlight" :style="{ fontSize: dialogFontSize + 'px' }" v-html="renderHighlightedText(getDialogDisplayText(), dialogTextQuery, true)"></div>
                </div>
              </div>
            </template>
          </el-tab-pane>
          <el-tab-pane label="字段">
            <el-table :data="(parsedDialogData?.basicSummary?.columns || buildBasicSummary(parsedDialogData?.res).columns)" size="small" border>
              <el-table-column prop="name" label="列名" min-width="140" />
              <el-table-column prop="type" label="类型" width="100" />
              <el-table-column prop="missing" label="缺失" width="80" />
              <el-table-column prop="unique" label="唯一" width="80" />
              <el-table-column label="更多" min-width="260">
                <template #default="{ row }">
                  <span v-if="(row.min!==undefined && row.max!==undefined)">范围 {{row.min}} ~ {{row.max}}，均值 {{row.mean}}</span>
                  <span v-else-if="row.start && row.end">起止 {{row.start}} ~ {{row.end}}</span>
                  <span v-else-if="row.top && row.top.length">Top: {{ row.top.map(t=>`${t.value}(${t.count}${t.pct?`/${t.pct}%`:''})`).join('、') }}</span>
                  <span v-else-if="row.text_len_max!==undefined">文本长度 均值 {{row.text_len_mean}}，最大 {{row.text_len_max}}</span>
                  <span v-else>—</span>
                </template>
              </el-table-column>
            </el-table>
          </el-tab-pane>
          <el-tab-pane label="源数据">
            <pre style="white-space: pre-wrap; max-height: 50vh; overflow: auto;">{{ (parsedDialogData?.res && (JSON.stringify(parsedDialogData.res, null, 2).slice(0, 4000) + (JSON.stringify(parsedDialogData.res).length>4000?'\n...（已截断）':''))) || '—' }}</pre>
          </el-tab-pane>
        </el-tabs>
        <template #footer>
          <span class="dialog-footer">
            <el-button @click="exportParsedCSV(parsedDialogData)" type="primary" plain>导出预览CSV</el-button>
            <el-button @click="exportBasicSummaryCSV(parsedDialogData)" type="success" plain>导出初步汇总CSV</el-button>
            <el-button @click="parsedDialogVisible=false">关闭</el-button>
          </span>
        </template>
      </el-dialog>

              <div v-if="message.role === 'assistant' && message.model_info" class="message-meta">
                <el-tag size="small">{{ message.model_info.provider }}</el-tag>
                <span class="response-time">{{ message.response_time }}ms</span>
              </div>

              <!-- AI消息操作按钮 -->
              <div v-if="message.role === 'assistant' && !message.loading" class="message-actions">
                <el-button
                  size="small"
                  text
                  :type="message.feedback === 'like' ? 'success' : ''"
                  @click="handleFeedback(message, 'like')"
                >
                  <el-icon><StarFilled /></el-icon>
                  有帮助
                </el-button>
                <el-button
                  size="small"
                  text
                  :type="message.feedback === 'dislike' ? 'danger' : ''"
                  @click="handleFeedback(message, 'dislike')"
                >
                  <el-icon><Delete /></el-icon>
                  没帮助
                </el-button>
                    <el-button v-if="message.role==='assistant' && message.rag_sources && message.rag_sources.length" size="small" text>
                      引用来源：
                      <el-tag v-for="(s, i) in message.rag_sources" :key="i" size="small" style="margin-left:4px">{{ s.title }}#{{ s.idx }} ({{ s.score }})</el-tag>
                    </el-button>
                <el-button size="small" text @click="copyMessage(message.content)">
                  <el-icon><CopyDocument /></el-icon>
                  复制
                </el-button>
              </div>

              <div v-if="message.loading" class="message-loading">
                <div class="loading-dots">
                  <div class="dot"></div>
                  <div class="dot"></div>
                  <div class="dot"></div>
                </div>
                <span>AI正在思考中...</span>
              </div>
            </div>
          </div>
        </div>

        <!-- 输入区域 -->
        <div class="input-area">
          <!-- 文件上传预览区域 -->
          <div v-if="uploadedFiles.length > 0" class="uploaded-files-preview">
            <div class="files-header">
              <el-icon><Document /></el-icon>
              <span>已上传文件 ({{ uploadedFiles.length }})</span>
              <el-button @click="clearAllFiles" size="small" text type="danger">
                <el-icon><Delete /></el-icon>
                清空
              </el-button>
            </div>
            <div class="files-list">
              <div
                v-for="(file, index) in uploadedFiles"
                :key="index"
                class="file-item"
              >
                <div class="file-info">
                  <el-icon class="file-icon">
                    <component :is="getFileIcon(file.type)" />
                  </el-icon>
                  <div class="file-details">
                    <span class="file-name">{{ file.name }}</span>
                    <span class="file-size">{{ formatFileSize(file.size) }}</span>
                  </div>
                </div>
                <div class="file-actions">
                  <el-button
                    @click="removeFile(index)"
                    size="small"
                    text
                    type="danger"
                  >
                    <el-icon><CircleClose /></el-icon>
                  </el-button>
                </div>
              </div>
            </div>
          </div>

          <div class="input-container">
            <div class="input-wrapper">
              <el-input
                v-model="inputMessage"
                placeholder="请输入您的问题..."
                @keydown.enter.exact="sendMessage"
                :disabled="isLoading"
                class="message-input"
                clearable
              >
                <template #prefix>
                  <div class="voice-controls">
                    <el-button
                      @click="toggleVoiceRecognition"
                      :type="isRecording ? 'danger' : 'info'"
                      size="small"
                      text
                      :class="{ 'recording': isRecording }"
                    >
                      <el-icon><Microphone /></el-icon>
                    </el-button>
                    <el-button
                      @click="toggleVoiceOutput"
                      :type="voiceEnabled ? 'success' : 'info'"
                      size="small"
                      text
                    >
                      <el-icon><component :is="voiceEnabled ? VideoPlay : Mute" /></el-icon>
                    </el-button>
                  </div>
                </template>
                <template #suffix>
                  <div class="input-actions">
                    <!-- 文件上传按钮 -->
                    <el-upload
                      ref="uploadRef"
                      :auto-upload="false"
                      :show-file-list="false"
                      :on-change="handleFileSelect"
                      :accept="acceptedFileTypes"
                      multiple
                      class="file-upload-btn"
                    >
                      <el-button
                        size="small"
                        text
                        type="info"
                        :disabled="isLoading"
                      >
                        <el-icon><Plus /></el-icon>
                      </el-button>
                    </el-upload>

                    <!-- 图像理解按钮（当存在图片文件时可用） -->
                    <el-button
                      size="small"
                      text
                      type="info"
                      :disabled="isLoading || !hasImageFiles"
                      @click="handleVisionAnalysis"
                    >
                      <el-icon><Picture /></el-icon>
                    </el-button>

                    <el-button
                      @click="sendMessage"
                      type="primary"
                      :loading="isLoading"
                      :disabled="!canSendMessage"
                      size="small"
                      style="margin-left: 8px;"
                    >
                      发送
                    </el-button>
                    <el-dropdown style="margin-left: 6px;">
                      <el-button size="small" text>知识库<el-icon class="el-icon--right"><ArrowDown /></el-icon></el-button>
                      <template #dropdown>
                        <el-dropdown-menu>
                          <el-dropdown-item @click="toggleUseRAG">
                            <el-checkbox v-model="useRag">结合知识作答</el-checkbox>
                          </el-dropdown-item>
                          <el-dropdown-item>
                            范围
                            <el-radio-group v-model="kbScope" size="small" style="margin-left: 8px;">
                              <el-radio-button label="user">用户</el-radio-button>
                              <el-radio-button label="session">会话</el-radio-button>
                            </el-radio-group>
                          </el-dropdown-item>
                          <el-dropdown-item @click="ingestSelectedMessages">将选中消息入库</el-dropdown-item>
                          <el-dropdown-item @click="openFileIngest">上传文件入库</el-dropdown-item>
                        </el-dropdown-menu>
                      </template>
                    </el-dropdown>
                  </div>
                </template>
              </el-input>
            </div>
          </div>
        </div>
      </div>

      <!-- 右侧AI执行流程面板 -->
      <div class="info-panel">
        <div class="section-header">
          <el-icon><Cpu /></el-icon>
          <span>AI执行流程</span>
          <div class="drag-hint">
            <el-icon><Operation /></el-icon>
            <span>可拖动查看</span>
          </div>
        </div>

        <!-- 执行流程展示 -->
        <div
          class="execution-flow"
          ref="executionFlowRef"
          @mousedown="startDrag"
          @wheel="onWheel"
        >
          <div
            v-for="(step, index) in executionSteps"
            :key="index"
            class="flow-step"
            :class="{
              'active': step.status === 'running',
              'completed': step.status === 'completed',
              'pending': step.status === 'pending',
              'error': step.status === 'error'
            }"
          >
            <div class="step-indicator">
              <div class="step-number" v-if="step.status === 'pending'">{{ index + 1 }}</div>
              <el-icon v-else-if="step.status === 'running'" class="step-loading">
                <Loading />
              </el-icon>
              <el-icon v-else-if="step.status === 'completed'" class="step-success">
                <CircleCheck />
              </el-icon>
              <el-icon v-else-if="step.status === 'error'" class="step-error">
                <CircleClose />
              </el-icon>
            </div>

            <div class="step-content">
              <div class="step-title">{{ step.title }}</div>
              <div class="step-description">{{ step.description }}</div>

              <div v-if="step.details" class="step-details">
                <div v-for="detail in step.details" :key="detail" class="detail-item">
                  {{ detail }}
                </div>

	              <!-- 初步汇总（右侧看板简版，仅当有 latestBasicSummary 时展示） -->
	              <div v-if="step.title==='初步汇总' && latestBasicSummary" class="step-details">
	                <div class="detail-item" v-for="col in (latestBasicSummary.columns || []).slice(0,4)" :key="col.name">
	                  <span>{{ col.name }} ({{ col.type }})</span>
	                  <span style="color:#64748b;">
	                    <template v-if="col.min!==undefined && col.max!==undefined">{{ col.min }} ~ {{ col.max }}</template>
	                    <template v-else-if="col.start && col.end">{{ col.start }} ~ {{ col.end }}</template>
	                    <template v-else-if="col.top && col.top.length">Top: {{ col.top.map(t=>`${t.value}(${t.count})`).join('、') }}</template>
	                    <template v-else-if="col.text_len_max!==undefined">Len {{ col.text_len_mean }}/{{ col.text_len_max }}</template>
	                    <template v-else>—</template>
	                  </span>
	                </div>
	                <div class="detail-item" v-if="(latestBasicSummary.columns||[]).length>4">
	                  <span>……</span>
	                  <span>其余 {{ latestBasicSummary.columns.length - 4 }} 列</span>
	                </div>
	              </div>

              </div>
              <div v-if="step.duration" class="step-duration">
                耗时: {{ step.duration }}ms
              </div>
            </div>
          </div>
        </div>

        <!-- 执行统计 -->


        <div class="execution-stats" v-if="executionStats.totalSteps > 0">
          <div class="stats-header">执行统计</div>
          <div class="stats-grid">
            <div class="stat-item">
              <div class="stat-value">{{ executionStats.completedSteps }}</div>
              <div class="stat-label">已完成</div>
            </div>
            <div class="stat-item">
              <div class="stat-value">{{ executionStats.totalDuration }}ms</div>
              <div class="stat-label">总耗时</div>
            </div>
          </div>
        </div>


      </div>
    </div>

    <!-- 对话记录抽屉 -->
    <el-drawer
      v-model="showHistoryDrawer"
      title="对话记录"
      direction="rtl"
      size="400px"
    >
      <div class="history-content">
        <div class="history-actions">
          <el-button size="small" @click="refreshConversations">
            <el-icon><Refresh /></el-icon>
            刷新
          </el-button>
          <el-button size="small" @click="goToFullHistory">
            <el-icon><FullScreen /></el-icon>
            完整管理
          </el-button>
        </div>

        <div class="conversation-list">
          <div
            v-for="conversation in recentConversations"
            :key="conversation.id"
            class="conversation-item"
            @click="loadConversation(conversation.id)"
          >
            <div class="conversation-title">{{ conversation.title }}</div>
            <div class="conversation-time">{{ formatTime(conversation.updated_at) }}</div>
            <div class="conversation-preview">{{ conversation.first_message }}</div>
          </div>
        </div>
      </div>
    </el-drawer>
  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted, onUnmounted, nextTick, watch } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import {
  ChatDotRound, User, Cpu, Tools, Document, Download, Plus, Upload,
  StarFilled, CopyDocument, Loading, Delete,
  Refresh, FullScreen,
  DocumentChecked,
  ChatLineRound, ArrowLeft, ArrowRight, Operation,
  Microphone, Mute, VideoPlay, CircleCheck, CircleClose,
  Picture, ArrowDown
} from '@element-plus/icons-vue'

// 导入API
import { sendChatMessage, getConversationList, getChatStatistics, getCaseGuidance, chatWithVision } from '@/api/chat'
import axios from 'axios'

// 导入组件
import SmartRecommendations from '@/components/SmartRecommendations.vue'
import cozeStudioAPI from '@/api/coze-studio'

// RAG 控制与入库操作
const useRag = ref(true)
const kbScope = ref('user') // 'user' | 'session'

function toggleUseRAG() {
  useRag.value = !useRag.value
  ElMessage.success(`结合知识作答：${useRag.value ? '开启' : '关闭'}`)
}

async function ingestSelectedMessages() {
  if (!currentConversationId.value) {
    ElMessage.info('请先选择或创建对话')
    return
  }
  const N = 4
  const recent = messages.value.slice(-N).map(m => ({ role: m.role, content: m.content }))
  try {
    await cozeStudioAPI.ingestFromMessages({
      conversation_id: currentConversationId.value,
      messages: recent,
      saveScope: kbScope.value,
      conversation_scope_id: kbScope.value === 'session' ? currentConversationId.value : undefined
    })
    ElMessage.success(`已将最近${N}条消息入库（${kbScope.value}）`)
  } catch (e) {
    ElMessage.error('入库失败：' + (e.response?.data?.message || e.message))
  }
}

function openFileIngest() {
  ElMessage.info('请使用顶部导入按钮上传文件，稍后将接入入库流程')
}

const router = useRouter()

// 响应式数据
const currentModel = ref('GPT-4o')
const inputMessage = ref('')
const isLoading = ref(false)
const messages = ref([])
const showHistoryDrawer = ref(false)
const recentConversations = ref([])
const userStats = ref({})
const lastResponseTime = ref(0)
const currentConversationId = ref(null)

// AI执行流程相关数据
const executionSteps = ref([])
const executionStats = ref({
  totalSteps: 0,
  completedSteps: 0,
  totalDuration: 0
})

const dialogTextQuery = ref('')
const dialogTextContainerRef = ref(null)
const currentMarkIndex = ref(-1)

// 最近一次文件解析的初步汇总（用于右侧看板展示）
const latestBasicSummary = ref(null)


const textPreviewQuery = ref('')
function escapeHtml(str=''){return String(str).replace(/[&<>"']/g,s=>({'&':'&amp;','<':'&lt;','>':'&gt;','"':'&quot;','\'':'&#39;'}[s]))}
function makeAnchorId(title, idx){
  const base = String(title).trim().slice(0,40).replace(/\s+/g,'-').replace(/[^\w\-\u4e00-\u9fa5]/g,'') || 'section'
  return `h${idx}-${base}`
}
function extractHeadings(text=''){
  const lines = String(text).split('\n')
  const out = []
  const reMd1 = /^#\s(.*)$/
  const reMd2 = /^##\s(.*)$/
  const reCn = /^(第[一二三四五六七八九十百千0-9]+[章节条项])\s*(.*)$/
  let idx = 0
  for(const line of lines){
    let m
    if((m = line.match(reMd1))){ out.push({ level:3, title:m[1], id: makeAnchorId(m[1], ++idx) }); continue }
    if((m = line.match(reMd2))){ out.push({ level:4, title:m[1], id: makeAnchorId(m[1], ++idx) }); continue }
    if((m = line.match(reCn))){ const title = `${m[1]} ${m[2]||''}`; out.push({ level:4, title, id: makeAnchorId(title, ++idx) }); continue }
  }
  return out
}
function renderHighlightedText(text, keyword, withAnchors=false){
  if(!text) return ''
  let html = escapeHtml(text)
  // 标题高亮：Markdown 和常见中文标题
  let idx = 0
  html = html
    .replace(/^#\s(.*)$/gm, (_,t)=> withAnchors ? `<h3 id="${makeAnchorId(t, ++idx)}">${t}</h3>` : `<h3>${t}</h3>`)
    .replace(/^##\s(.*)$/gm,(_,t)=> withAnchors ? `<h4 id="${makeAnchorId(t, ++idx)}">${t}</h4>` : `<h4>${t}</h4>`)
    .replace(/^(第[一二三四五六七八九十百千0-9]+[章节条项])\s*(.*)$/gm,(_,a,b)=>{
      const title = `${a} ${b||''}`
      const id = withAnchors ? ` id=\"${makeAnchorId(title, ++idx)}\"` : ''
      return `<h4${id}>${a}</h4><p>${b||''}</p>`
    })
  // 关键字高亮
  if (keyword && keyword.trim()){
    const safe = keyword.replace(/[.*+?^${}()|[\]\\]/g,'\\$&')
    const re = new RegExp(safe,'gi')
    html = html.replace(re, m=>`<mark>${escapeHtml(m)}</mark>`)
const activeHeadingId = ref('')
let headingObserver = null
function setupHeadingObserver(){
  // 延迟到渲染完成后绑定
  nextTick(()=>{
    const box = dialogTextContainerRef.value
    if(!box) return
    // 清理旧的
    if(headingObserver){ headingObserver.disconnect(); headingObserver = null }
    const options = { root: box, rootMargin: '0px 0px -70% 0px', threshold: [0, 1.0] }
    headingObserver = new IntersectionObserver((entries)=>{
      // 选择最靠上的可见标题
      const visible = entries.filter(e=>e.isIntersecting)
      if(!visible.length) return
      visible.sort((a,b)=> a.boundingClientRect.top - b.boundingClientRect.top)
      const target = visible[0].target
      activeHeadingId.value = target.id || ''
    }, options)
    box.querySelectorAll('h3,h4').forEach(el=> headingObserver.observe(el))
  })
}
watch(()=>parsedDialogVisible.value, (v)=>{ if(v) setupHeadingObserver() })
watch(()=>dialogTextQuery.value, ()=> setupHeadingObserver())

const dialogOnlyMatches = ref(false)
const dialogFontSize = ref(13)
function getDialogDisplayText(){
  const t = parsedDialogData?.value?.res?.text || ''
  if(!dialogOnlyMatches.value || !dialogTextQuery.value?.trim()) return t
  // 仅保留含关键词的段落：按空行区分
  const blocks = t.split(/\n{2,}/)
  const safe = dialogTextQuery.value.replace(/[.*+?^${}()|[\]\\]/g,'\\$&')
  const re = new RegExp(safe,'i')
  return blocks.filter(b=>re.test(b)).join('\n\n')
}

  }
  // 换行 -> 段落
  html = html.replace(/\n{2,}/g,'</p><p>').replace(/\n/g,'<br/>')
  return `<p>${html}</p>`
}
const dialogOutline = computed(()=>{
  const text = parsedDialogData?.value?.res?.text || ''
  return extractHeadings(text)
})
function jumpToMark(next=true){
  const box = dialogTextContainerRef.value
  if(!box) return
  const marks = box.querySelectorAll('mark')
  if(!marks.length) return
  if(next){ currentMarkIndex.value = (currentMarkIndex.value + 1) % marks.length }
  else { currentMarkIndex.value = (currentMarkIndex.value - 1 + marks.length) % marks.length }
  marks[currentMarkIndex.value].scrollIntoView({ behavior:'smooth', block:'center' })
}

// 拖动相关数据
const executionFlowRef = ref(null)
const isDragging = ref(false)
const dragStart = ref({ x: 0, y: 0 })
const scrollStart = ref({ top: 0, left: 0 })

// 语音功能相关数据
const isRecording = ref(false)
const voiceEnabled = ref(true)
const speechRecognition = ref(null)
const speechSynthesis = ref(null)

// 文件上传相关数据
const uploadedFiles = ref([])
const uploadRef = ref(null)
const acceptedFileTypes = ref('.pdf,.doc,.docx,.txt,.md,.json,.xml,.jpg,.jpeg,.png,.gif,.bmp,.webp,.xls,.xlsx,.csv')
const maxFileSize = ref(20 * 1024 * 1024) // 20MB
const maxFileCount = ref(5) // 最多5个文件

// 当前用户信息
const currentUser = reactive({
  id: 'user-001',
  username: '管理员',
  preferences: {}
})

// 模型相关数据
const availableModels = ref([])
const modelStatus = reactive({
  status: 'online',
  provider: 'GPT-4o'
})

// 当前模型特性
const currentModelFeatures = computed(() => {
  const model = availableModels.value.find(m => m.provider === currentModel.value)
  return model?.features || { reasoning: true, toolCalls: false, vision: false }
})

// 文件上传相关计算属性
const canSendMessage = computed(() => {
  return inputMessage.value.trim() || uploadedFiles.value.length > 0
})

const hasImageFiles = computed(() => {
  return uploadedFiles.value.some(file => file.type.startsWith('image/'))
})

const hasDocumentFiles = computed(() => {
  const docTypes = ['application/pdf', 'application/msword', 'application/vnd.openxmlformats-officedocument.wordprocessingml.document', 'text/plain', 'text/markdown']
  return uploadedFiles.value.some(file => docTypes.includes(file.type))
})

// 欢迎建议
const welcomeSuggestions = [
  '请介绍ISO 9001质量管理体系的核心要素',
  '如何建立有效的质量控制流程？',
  '产品缺陷分析的方法有哪些？',
  'SPC统计过程控制的实施步骤',
  'FMEA失效模式分析怎么做？',
  '如何制定抽样检验方案？'
]

// 工具分类配置
const toolCategories = ref([
  { key: 'all', name: '全部', icon: 'Grid' },
  { key: 'analysis', name: '分析', icon: 'DataAnalysis' },
  { key: 'document', name: '文档', icon: 'Document' },
  { key: 'inspection', name: '检验', icon: 'Search' },
  { key: 'management', name: '管理', icon: 'Setting' }
])

// 可用工具配置
const availableTools = ref([
  {
    id: 'quality-analysis',
    name: '质量分析工具',
    description: '提供SPC、FMEA等质量分析功能',
    icon: 'DataAnalysis',
    status: 'available',
    category: 'analysis'
  },
  {
    id: 'spc-control',
    name: 'SPC控制图',
    description: '统计过程控制图生成和分析',
    icon: 'TrendCharts',
    status: 'available',
    category: 'analysis'
  },
  {
    id: 'document-generator',
    name: '文档生成器',
    description: '自动生成质量管理文档模板',
    icon: 'Document',
    status: 'available',
    category: 'document'
  },
  {
    id: 'procedure-writer',
    name: '程序文件助手',
    description: '协助编写标准作业程序',
    icon: 'DocumentChecked',
    status: 'available',
    category: 'document'
  },
  {
    id: 'inspection-planner',
    name: '检验计划器',
    description: '制定抽样检验和测试计划',
    icon: 'Search',
    status: 'available',
    category: 'inspection'
  },
  {
    id: 'sampling-calculator',
    name: '抽样计算器',
    description: '计算AQL抽样方案',
    icon: 'Operation',
    status: 'available',
    category: 'inspection'
  },
  {
    id: 'risk-assessment',
    name: '风险评估',
    description: '质量风险识别和评估工具',
    icon: 'Warning',
    status: 'development',
    category: 'management'
  },
  {
    id: 'training-assistant',
    name: '培训助手',
    description: '质量管理培训内容生成',
    icon: 'User',
    status: 'development',
    category: 'management'
  },
  {
    id: 'audit-planner',
    name: '审核计划',
    description: '内部审核计划制定工具',
    icon: 'Monitor',
    status: 'development',
    category: 'management'
  }
])

// 工具相关状态
const currentToolCategory = ref('all')
const currentToolPage = ref(1)
const toolsPerPage = 3

// 便捷对话分类
const quickQuestionCategories = ref([
  {
    name: '质量体系',
    questions: [
      { id: 1, title: 'ISO 9001介绍', icon: 'DocumentChecked', text: '请详细介绍ISO 9001质量管理体系的核心要素和实施要点' },
      { id: 2, title: 'PDCA循环', icon: 'Refresh', text: '质量改进的PDCA循环是什么？如何在实际工作中应用？' }
    ]
  },
  {
    name: '数据分析',
    questions: [
      { id: 3, title: 'SPC分析', icon: 'DataAnalysis', text: '如何进行SPC统计过程控制？请详细说明控制图的使用方法' },
      { id: 4, title: 'FMEA分析', icon: 'Document', text: 'FMEA失效模式分析怎么做？请提供详细的分析步骤和模板' }
    ]
  },
  {
    name: '检验测试',
    questions: [
      { id: 5, title: '抽样检验', icon: 'Search', text: '如何制定合理的抽样检验方案？AQL标准如何应用？' },
      { id: 6, title: '测量系统', icon: 'Monitor', text: '测量系统分析(MSA)的方法和步骤是什么？' }
    ]
  }
])

// 计算属性
const filteredTools = computed(() => {
  if (currentToolCategory.value === 'all') {
    return availableTools.value
  }
  return availableTools.value.filter(tool => tool.category === currentToolCategory.value)
})

const totalToolPages = computed(() => {
  return Math.ceil(filteredTools.value.length / toolsPerPage)
})

const currentPageTools = computed(() => {
  const start = (currentToolPage.value - 1) * toolsPerPage
  const end = start + toolsPerPage
  return filteredTools.value.slice(start, end)
})



// 方法定义
const getModelDisplayName = (model) => {
  return `${model.name} - ${model.model}`
}

const switchModel = async (modelProvider) => {
  console.log('切换模型:', modelProvider)
  try {
    // 调用后端API切换模型
    const response = await axios.post('/api/chat/switch-model', {
      model: modelProvider
    })

    if (response.data.success) {
      currentModel.value = modelProvider
      // 更新模型状态
      const selectedModel = availableModels.value.find(m => m.provider === modelProvider)
      if (selectedModel) {
        modelStatus.status = 'online'
        modelStatus.provider = selectedModel.name
      }
      ElMessage.success(`已切换到 ${selectedModel?.name || modelProvider}`)
    } else {
      ElMessage.error('模型切换失败: ' + (response.data.message || '未知错误'))
    }
  } catch (error) {
    console.error('模型切换失败:', error)
    ElMessage.error('模型切换失败: ' + (error.response?.data?.message || error.message))
  }
}

const sendQuickQuestion = (question) => {
  inputMessage.value = question
  sendMessage()
}

// 处理推荐选择
const handleRecommendationSelect = (recommendation) => {
  inputMessage.value = recommendation.question
  sendMessage()
}

// 对话管理方法
const refreshConversations = async () => {
  try {
    const response = await getConversationList({ limit: 10 })
    if (response.success) {
      recentConversations.value = response.data.conversations.map(conv => ({
        id: conv.id,
        title: conv.title || conv.first_message?.substring(0, 30) + '...' || '新对话',
        updated_at: conv.updated_at,
        message_count: conv.message_count || 0,
        first_message: conv.first_message
      }))
    }
  } catch (error) {
    console.error('获取对话列表失败:', error)
  }
}

const loadConversation = async (conversationId) => {
  try {
    currentConversationId.value = conversationId
    // 这里可以加载具体的对话内容
    ElMessage.success('对话加载成功')
  } catch (error) {
    console.error('加载对话失败:', error)
    ElMessage.error('加载对话失败')
  }
}

// 工具调用方法
// 工具分类切换
const switchToolCategory = (category) => {
  currentToolCategory.value = category
  currentToolPage.value = 1 // 重置到第一页
}

// 工具分页方法
const prevToolPage = () => {
  if (currentToolPage.value > 1) {
    currentToolPage.value--
  }
}

const nextToolPage = () => {
  if (currentToolPage.value < totalToolPages.value) {
    currentToolPage.value++
  }
}



const invokeTool = async (tool) => {
  if (tool.status !== 'available') {
    ElMessage.info(`${tool.name} 正在开发中，敬请期待`)
    return
  }

  // 文档类工具：如已选择文件，直接走文件解析流程
  const isDocTool = tool.category === 'document' || ['document-generator','procedure-writer'].includes(tool.id)
  if (isDocTool && (hasDocumentFiles.value || hasImageFiles.value)) {
    try {
      if (!currentConversationId.value) currentConversationId.value = 'conv_' + Date.now()
      await processUploadedFiles(uploadedFiles.value, inputMessage.value)
      // 解析走完，这里不发送纯文本消息
      return
    } catch (e) {
      console.error('工具触发解析失败:', e)
      ElMessage.error('解析失败：' + (e.message || '未知错误'))
      return
    }
  }

  // 其他工具按原逻辑注入提示语
  switch (tool.id) {
    case 'quality-analysis':
      inputMessage.value = '请帮我进行质量数据分析，我需要使用SPC控制图分析生产过程的稳定性'
      break
    case 'spc-control':
      inputMessage.value = '请详细介绍SPC统计过程控制图的类型、制作方法和判断准则'
      break
    case 'document-generator':
      inputMessage.value = '请帮我生成一个ISO 9001质量管理体系文件模板，包括质量手册的基本结构'
      break
    case 'procedure-writer':
      inputMessage.value = '请帮我编写一个标准作业程序(SOP)模板，包括目的、适用范围、职责和具体步骤'
      break
    case 'inspection-planner':
      inputMessage.value = '请帮我制定一个产品抽样检验计划，包括AQL标准的应用和样本量的确定'
      break
    case 'sampling-calculator':
      inputMessage.value = '请帮我计算AQL抽样方案，包括不同检验水平下的样本量和接收拒收数'
      break
    default:
      ElMessage.info('工具功能开发中')
      return
  }

  sendMessage()
}

const sendMessage = async () => {
  if (!canSendMessage.value) return

  // 构建用户消息
  const userMessage = {
    role: 'user',
    content: inputMessage.value || '发送了文件',
    timestamp: new Date(),
    files: uploadedFiles.value.length > 0 ? [...uploadedFiles.value] : undefined
  }

  messages.value.push(userMessage)
  const currentInput = inputMessage.value
  const currentFiles = [...uploadedFiles.value]

  // 清空输入和文件
  inputMessage.value = ''
  uploadedFiles.value = []
  isLoading.value = true

  // 启动AI执行流程展示（根据是否包含文件选择流程）
  if (currentFiles.length > 0) {
    initializeFileExecutionFlow(currentFiles)
  } else {
    simulateExecutionFlow(currentInput || '智能问答')
  }

  // 添加加载消息
  const loadingMessage = {
    role: 'assistant',
    content: '',
    loading: true,
    timestamp: new Date()
  }
  messages.value.push(loadingMessage)

  try {
    const startTime = Date.now()


    // 如果包含文件，则优先走“插件解析”流程并直接返回
    if (currentFiles.length > 0) {
      // 确保会话ID存在，避免traces API返回400错误
      if (!currentConversationId.value) {
        currentConversationId.value = 'conv_' + Date.now()
      }
      await processUploadedFiles(currentFiles, currentInput)
      // 精确移除本次的加载消息（按对象引用定位）
      const idx = messages.value.indexOf(loadingMessage)
      if (idx !== -1) messages.value.splice(idx, 1)
      isLoading.value = false
      scrollToBottom()
      return
    }

    // 准备请求数据
    // 确保会话ID一致，便于Traces聚合
    if (!currentConversationId.value) {
      currentConversationId.value = 'conv_' + Date.now()
    }
    const requestData = {
      message: currentInput || '请分析上传的文件',
      conversation_id: currentConversationId.value,
      model: currentModel.value,
      files: currentFiles.map(file => ({
        name: file.name,
        type: file.type,
        size: file.size
      }))
    }

    // 结合知识配置
    requestData.use_rag = useRag.value
    requestData.kb_scope = kbScope.value
    // TODO: 如存在后端 agentId 适配，可按需传递


// 删除重复的processUploadedFiles函数定义，统一使用下方的插件系统版本

  function inferFormatFromName(name=''){
    const n = String(name).toLowerCase()
    if(n.endsWith('.pdf')) return 'pdf'
    if(n.endsWith('.xlsx')||n.endsWith('.xls')) return 'xlsx'
    if(n.endsWith('.csv')) return 'csv'
    if(n.endsWith('.json')) return 'json'
    if(n.endsWith('.xml')) return 'xml'
    if(/\.(png|jpg|jpeg|bmp|gif|webp)$/.test(n)) return 'image'
    return 'text'
  }

  // 删除未使用的waitParsingExecution函数




    const response = await sendChatMessage(requestData)

    const responseTime = Date.now() - startTime
    lastResponseTime.value = responseTime

    // 移除加载消息
    messages.value.pop()

    if (response.success) {
      const assistantMessage = {
        role: 'assistant',
        content: response.data.response,
        timestamp: new Date(),
        model_info: { provider: currentModel.value },
        response_time: responseTime
      }
      messages.value.push(assistantMessage)

      // 自动播放AI回复的语音
      if (voiceEnabled.value) {
        setTimeout(() => {
          speakText(response.data.response)
        }, 500) // 延迟500ms播放，确保消息已渲染
      }
    } else {
      throw new Error(response.message || '发送失败')
    }
  } catch (error) {
    messages.value.pop() // 移除加载消息
    ElMessage.error('发送失败: ' + error.message)
    messages.value.push({
      role: 'assistant',
      content: '抱歉，发送失败了，请稍后再试。',
      timestamp: new Date()
    })
  } finally {
    isLoading.value = false
    scrollToBottom()
  }
}

const handleFeedback = (message, type) => {
  message.feedback = type
  ElMessage.success(type === 'like' ? '感谢您的反馈！' : '我们会继续改进')
}

const copyMessage = (content) => {
  navigator.clipboard.writeText(content)
  ElMessage.success('已复制到剪贴板')
}

const formatMessage = (content) => {
  return content.replace(/\n/g, '<br>')
}

const formatTime = (time) => {
  if (!time) return ''
  const date = new Date(time)
  return date.toLocaleString()
}

// 文件处理方法
const handleFileSelect = (uploadFile) => {
  // 规范化 Element Plus 的回调入参（UploadFile），优先使用 raw File/Blob
  const raw = uploadFile?.raw || uploadFile
  const name = uploadFile?.name || raw?.name || '未命名文件'
  const size = raw?.size ?? uploadFile?.size ?? 0
  let type = raw?.type || ''

  // 检查文件数量限制
  if (uploadedFiles.value.length >= maxFileCount.value) {
    ElMessage.warning(`最多只能上传 ${maxFileCount.value} 个文件`)
    return false
  }

  // 当浏览器未提供 MIME 时，按扩展名推断
  if (!type && name.includes('.')) {
    const ext = name.split('.').pop().toLowerCase()
    const mimeMap = {
      pdf: 'application/pdf',
      doc: 'application/msword',
      docx: 'application/vnd.openxmlformats-officedocument.wordprocessingml.document',
      txt: 'text/plain',
      md: 'text/markdown',
      json: 'application/json',
      xml: 'application/xml',
      jpg: 'image/jpeg',
      jpeg: 'image/jpeg',
      png: 'image/png',
      gif: 'image/gif',
      bmp: 'image/bmp',
      webp: 'image/webp',
      xls: 'application/vnd.ms-excel',
      xlsx: 'application/vnd.openxmlformats-officedocument.spreadsheetml.sheet',
      csv: 'text/csv'
    }
    type = mimeMap[ext] || 'application/octet-stream'
  }

  // 检查文件大小
  if (size > maxFileSize.value) {
    ElMessage.warning(`文件大小不能超过 ${formatFileSize(maxFileSize.value)}`)
    return false
  }

  // 检查文件类型
  const allowedTypes = [
    // 文档
    'application/pdf',
    'application/msword',
    'application/vnd.openxmlformats-officedocument.wordprocessingml.document',
    'text/plain',
    'text/markdown',
    'application/json',
    'application/xml',
    'text/xml',
    // 图片（OCR）
    'image/jpeg', 'image/jpg', 'image/png', 'image/gif', 'image/bmp', 'image/webp',
    // 表格
    'application/vnd.ms-excel',
    'application/vnd.openxmlformats-officedocument.spreadsheetml.sheet',
    'text/csv'
  ]

  if (!allowedTypes.includes(type)) {
    ElMessage.warning('不支持的文件类型')
    return false
  }

  // 添加文件到列表（保存原始 Blob 以便后续读取/上传）
  uploadedFiles.value.push({
    name,
    size,
    type,
    file: raw,
    id: Date.now() + Math.random()
  })

  ElMessage.success(`文件 "${name}" 上传成功`)
  return false // 阻止自动上传
}

// 刷新右侧过程栏（Traces）
const refreshTraces = async () => {
  try {
    if (!currentConversationId.value) return
    const res = await cozeStudioAPI.getTraces({ conversation_id: currentConversationId.value, limit: 50 })
    if (!res.success) return
    const traces = res.data?.items || []
    // 映射到界面步骤
    const titleMap = {
      parse: '解析文件/文本',
      ingest_kb: '写入知识库',
      search_kb: '检索知识片段',
      case_guidance: '生成经验指导',
      respond: '组装返回'
    }
    executionSteps.value = traces.map(t => ({
      title: titleMap[t.category] || t.name || t.category,
      description: t.input?.query ? `query: ${String(t.input.query).slice(0, 50)}` : '',
      status: t.status === 'error' ? 'error' : 'completed',
      details: [
        t.output?.added ? `新增片段: ${t.output.added}` : '',
        Array.isArray(t.output?.hits) ? `命中: ${t.output.hits.length}` : ''
      ].filter(Boolean),
      duration: null
    }))
    executionStats.value = {
      totalSteps: executionSteps.value.length,
      completedSteps: executionSteps.value.filter(s => s.status === 'completed').length,
      totalDuration: executionSteps.value.reduce((a, b) => a + (b.duration || 0), 0)
    }
  } catch (e) {
    console.warn('刷新Traces失败', e)
  }
}

const removeFile = (index) => {
  const file = uploadedFiles.value[index]
  uploadedFiles.value.splice(index, 1)
  ElMessage.info(`已移除文件 "${file.name}"`)
}

// === 文件解析：自动识别并调用插件 ===
const detectPluginForFile = (file) => {
  const type = (file.type || '').toLowerCase()
  const name = (file.name || '').toLowerCase()
  const ext = name.includes('.') ? name.split('.').pop() : ''
  if (type.includes('pdf') || ext === 'pdf') return 'pdf_parser'
  if (type.includes('word') || ext === 'docx' || ext === 'doc') return 'docx_parser'
  if (type.includes('sheet') || type.includes('excel') || ext === 'xlsx' || ext === 'xls') return 'xlsx_parser'
  if (ext === 'csv' || type.includes('csv')) return 'csv_parser'
  if (ext === 'json' || type.includes('json')) return 'json_parser'
  if (ext === 'xml' || type.includes('xml')) return 'xml_parser'
  if (type.startsWith('image/') || ['jpg','jpeg','png','bmp','gif','webp'].includes(ext)) return 'ocr_reader'
  return null
}

const fileToBase64 = (blob) => new Promise((resolve, reject) => {
  const r = new FileReader()
  r.onload = () => resolve(String(r.result).split(',').pop())
  r.onerror = reject
  r.readAsDataURL(blob)
})

const fileToText = (blob) => new Promise((resolve, reject) => {
  const r = new FileReader()
  r.onload = () => resolve(String(r.result))
  r.onerror = reject
  r.readAsText(blob)
})

// 初步数据汇总：在展示前基于解析结果做轻量统计（非分析）
function guessBasicType(values = []) {
  // 采样最多200条非空值
  const sample = values.filter(v => v !== null && v !== undefined && v !== '').slice(0, 200)
  if (sample.length === 0) return 'string'
  const asNumber = sample.map(v => (typeof v === 'number' ? v : Number(String(v).replace(/,/g, ''))))
  const numberRatio = asNumber.filter(v => !Number.isNaN(v) && Number.isFinite(v)).length / sample.length
  if (numberRatio >= 0.7) {
    const intRatio = asNumber.filter(v => Number.isInteger(v)).length / asNumber.length
    return intRatio >= 0.9 ? 'integer' : 'number'
  }
  const asDate = sample.map(v => Date.parse(String(v)))
  const dateRatio = asDate.filter(ts => !Number.isNaN(ts)).length / sample.length
  if (dateRatio >= 0.7) return 'date'
  const asBool = sample.map(v => String(v).toLowerCase().trim())
  const boolRatio = asBool.filter(v => ['true','false','yes','no','0','1'].includes(v)).length / sample.length
  if (boolRatio >= 0.7) return 'boolean'
  return 'string'
}

function buildBasicSummary(res) {
  // 1) 表格型（preview/rows）优先
  let rows = Array.isArray(res?.preview) ? res.preview : (Array.isArray(res?.rows) ? res.rows : [])
  // 2) 文本型（如 pdf/docx/ocr）：从 text 生成一行 content
  if ((!rows || rows.length === 0) && typeof res?.text === 'string' && res.text.trim()) {
    rows = [{ content: res.text }]
  }
  const rowCount = rows.length
  const headers = rowCount > 0 ? Object.keys(rows[0]) : (res?.columns ? Object.keys(res.columns) : [])
  const columnSummaries = headers.map(h => {
    const vals = rows.map(r => r?.[h])
    const missing = vals.filter(v => v === null || v === undefined || v === '').length
    const present = vals.length - missing
    const uniq = new Set(vals.filter(v => v !== null && v !== undefined && v !== '')).size
    const type = guessBasicType(vals)
    const example = vals.find(v => v !== null && v !== undefined && v !== '')
    const info = { name: h, type, missing, unique: uniq, present }

    if (type === 'integer' || type === 'number') {
      const nums = vals.map(v => (typeof v === 'number' ? v : Number(String(v).replace(/,/g, '')))).filter(v => Number.isFinite(v))
      if (nums.length) {
        const min = Math.min(...nums), max = Math.max(...nums)
        const mean = nums.reduce((a,b)=>a+b,0) / nums.length
        info.min = min; info.max = max; info.mean = Number(mean.toFixed(2))
      }
    } else if (type === 'date') {
      const ts = vals.map(v => Date.parse(String(v))).filter(v => !Number.isNaN(v))
      if (ts.length) {
        const min = new Date(Math.min(...ts)), max = new Date(Math.max(...ts))
        info.start = min.toISOString().slice(0,19).replace('T',' ')
        info.end = max.toISOString().slice(0,19).replace('T',' ')
      }
    } else if (type === 'boolean') {
      const norm = vals.map(v => String(v).toLowerCase().trim())
      const t = norm.filter(v => ['true','yes','1'].includes(v)).length
      const f = norm.filter(v => ['false','no','0'].includes(v)).length
      info.true = t; info.false = f
    } else {
      // 视为类别/文本：给出Top-N与占比
      const freq = new Map()
      vals.forEach(v => {
        const k = (v === null || v === undefined || v === '') ? '' : String(v)
        if (!k) return
        freq.set(k, (freq.get(k) || 0) + 1)
      })
      const total = Array.from(freq.values()).reduce((a,b)=>a+b,0)
      const top = Array.from(freq.entries()).sort((a,b)=>b[1]-a[1]).slice(0,5).map(([k,c])=>({ value:k, count:c, pct: Number(((c/Math.max(total,1))*100).toFixed(1)) }))
      if (top.length) {
        info.top = top
        const covered = top.reduce((a,t)=>a+t.count,0)
        info.coverage_pct = Number(((covered/Math.max(total,1))*100).toFixed(1))
      }
      // 文本长度统计
      const lengths = vals.map(v => (v === null || v === undefined) ? 0 : String(v).length)
      if (lengths.length) {
        const maxLen = Math.max(...lengths)
        const meanLen = lengths.reduce((a,b)=>a+b,0) / lengths.length
        info.text_len_max = maxLen
        info.text_len_mean = Number(meanLen.toFixed(1))
      }
    }

    return { ...info, example }
  })
  return { row_count: rowCount, column_count: headers.length, columns: columnSummaries }
}

function stringifyBasicSummary(summary, maxCols = 6) {
  if (!summary) return '（无可汇总数据）'
  const lines = [`初步汇总：${summary.row_count} 行 × ${summary.column_count} 列`]
  const cols = (summary.columns || []).slice(0, maxCols)
  cols.forEach(c => {
    let extra = ''
    if ((c.type === 'integer' || c.type === 'number') && typeof c.min === 'number') {
      extra = `，范围 ${c.min} ~ ${c.max}，均值 ${c.mean}`
    } else if (c.type === 'date' && c.start && c.end) {
      extra = `，起止 ${c.start} ~ ${c.end}`
    } else if (Array.isArray(c.top) && c.top.length) {
      const head = `，Top${c.top.length}：` + c.top.map(t => `${t.value}(${t.count}/${t.pct}%)`).join('、')
      const tail = (typeof c.coverage_pct === 'number') ? `，累计覆盖 ${c.coverage_pct}%` : ''
      extra = head + tail
    } else if (typeof c.text_len_max === 'number') {
      extra = `，文本长度 均值 ${c.text_len_mean}，最大 ${c.text_len_max}`
    }
    const base = `- ${c.name} (${c.type})：缺失 ${c.missing}，唯一 ${c.unique}`
    lines.push(base + extra)
  })
  if ((summary.columns || []).length > maxCols) {
    lines.push(`… 其余 ${summary.columns.length - maxCols} 列已省略`)
  }
  return lines.join('\n')
}


const summarizePluginResult = (pluginId, data) => {
  const payload = data?.result || data
  // 统一兼容：后端返回 { success, result, ... }；也可能直接是结果对象
  const res = payload && (payload.result || payload)
  if (!res) return '未获取到解析结果'

  // 小工具：将表格数据前几行渲染为文本表格
  const renderTable = (rows = [], maxRows = 5) => {
    const arr = Array.isArray(rows) ? rows.slice(0, maxRows) : []
    if (arr.length === 0) return '（无可预览数据）'
    const headers = Object.keys(arr[0] || {})
    const line = (cols) => `| ${cols.join(' | ')} |`
    const header = line(headers)
    const sep = line(headers.map(() => '---'))
    const body = arr.map(r => line(headers.map(h => String(r[h] ?? '')))).join('\n')
    return `${header}\n${sep}\n${body}`
  }

  // 根据类型与插件生成更“真实”的解析输出
  if (pluginId === 'pdf_parser' || res.type === 'pdf_content') {
    const text = (res.text || '').trim()
    const preview = text.slice(0, 800)
    return `PDF解析成功（${res.pages || '?'}页）。\n内容预览：\n${preview}${text.length>800 ? '\n...（已截断）' : ''}`
  }

  if (pluginId === 'docx_parser' || res.type === 'docx_content') {
    const text = (res.text || '').trim()
    const preview = text.slice(0, 800)
    return `DOCX解析成功。\n内容预览：\n${preview}${text.length>800 ? '\n...（已截断）' : ''}`
  }

  // 表格类：xlsx/csv/json/xml 返回统一结构：preview(数组)、columns、summary
  if (pluginId === 'xlsx_parser' || res.type === 'xlsx') {
    const rows = res.preview || res.rows || []
    const stats = res.summary || {}
    const headers = rows[0] ? Object.keys(rows[0]) : []
    const table = renderTable(rows, 8)
    return [
      `Excel解析成功。列：${headers.join(', ') || '未知'}，预览行数：${rows.length}`,
      '',
      table,
      '',
      Object.keys(stats).length ? `列统计（示例）：${Object.keys(stats).slice(0,3).join(', ')}` : ''
    ].filter(Boolean).join('\n')
  }

  if (pluginId === 'csv_parser' || res.type === 'csv') {
    const rows = res.preview || res.rows || []
    return `CSV解析成功。预览行数：${rows.length}\n\n${renderTable(rows, 8)}`
  }

  if (pluginId === 'json_parser' || res.type === 'json') {
    const rows = res.preview || []
    const keys = Object.keys(res.columns || {})
    return `JSON解析成功。字段：${keys.slice(0,10).join(', ')}\n\n${renderTable(rows, 8)}`
  }

  if (pluginId === 'xml_parser' || res.type === 'xml') {
    const rows = res.preview || []
    return `XML解析成功。预览行数：${rows.length}\n\n${renderTable(rows, 8)}`
  }

  if (pluginId === 'ocr_reader' || res.type === 'ocr') {
    const text = (res.text || '').trim()
    const preview = text.slice(0, 800)
    return `OCR识别成功。内容预览：\n${preview}${text.length>800 ? '\n...（已截断）' : ''}`
  }

  try { return JSON.stringify(res, null, 2).slice(0, 1200) } catch { return String(res) }
}

const processUploadedFiles = async (files, userInput) => {
  // 确保会话ID存在，避免traces API返回400错误
  if (!currentConversationId.value) {
    currentConversationId.value = 'conv_' + Date.now()
  }

  for (const f of files) {
    // Step 0: 文件类别识别
    startStep(0, [`${f.name} (${f.type || 'unknown'})`])
    const pluginId = detectPluginForFile(f)
    finishStep(0, !!pluginId, [pluginId ? `识别为：${pluginId}` : '未识别到可用插件'])
    if (!pluginId) {
      messages.value.push({ role:'assistant', content:`暂不支持解析该格式：${f.name}`, timestamp:new Date() })
      continue
    }

    // Step 1: 选择解析工具
    startStep(1, [`匹配插件：${pluginId}`])
    finishStep(1, true)

    try {
      // Step 2: 调用插件解析
      startStep(2, ['准备读取数据'])
      let input = {}
      const raw = f.file || f.raw || f
      if (['pdf_parser','docx_parser','xlsx_parser'].includes(pluginId)) {
        input.base64 = await fileToBase64(raw)
      } else if (['csv_parser','json_parser','xml_parser'].includes(pluginId)) {
        input.text = await fileToText(raw)
      } else if (pluginId === 'ocr_reader') {
        input.base64 = await fileToBase64(raw)
      }
      const execRes = await cozeStudioAPI.executePlugin(pluginId, { input })
      const ok = execRes?.success ?? execRes?.data?.success ?? true
      const resObj = (execRes?.data?.result || execRes?.result || execRes?.data || null)
      // 统计信息
      const rows = resObj?.preview || resObj?.rows || []
      const rowCount = resObj?.row_count ?? (Array.isArray(rows) ? rows.length : 0)
      const cols = resObj?.columns_list ?? (rows[0] ? Object.keys(rows[0]) : [])
      const sheets = Array.isArray(resObj?.sheet_names) ? resObj.sheet_names : []
      finishStep(2, ok, [
        ok ? `解析完成：行${rowCount} 列${cols.length}` : (execRes?.message || execRes?.error || '解析失败'),
        sheets.length ? `工作表：${sheets.join(', ')}` : ''
      ].filter(Boolean))
      if (!ok) {
        const msg = execRes?.message || execRes?.error || execRes?.data?.message || '未知错误'
        messages.value.push({ role:'assistant', content:`解析失败（${f.name}）：${msg}` , timestamp:new Date() })
        continue
      }

      // Step 3: 结构优化
      startStep(3, ['生成结构化摘要与统计'])
      const summary = summarizePluginResult(pluginId, execRes?.data || execRes)
      const statKeys = resObj?.summary ? Object.keys(resObj.summary).slice(0,3) : []
      finishStep(3, true, [
        `摘要长度：${summary.length}`,
        statKeys.length ? `列统计示例：${statKeys.join(', ')}` : ''
      ].filter(Boolean))

      // Step 4: 初步汇总（在展示之前）
      startStep(4, ['整理基础统计'])
      const basicSummary = buildBasicSummary(resObj)
      latestBasicSummary.value = basicSummary
      const basicSummaryText = stringifyBasicSummary(basicSummary)
      finishStep(4, true, [`汇总列数：${basicSummary.column_count}`])

      // Step 5: 注入聊天（先展示初步汇总，再展示预览/解析内容）
      startStep(5)
      messages.value.push({
        role:'assistant',
        content: `【文件解析】${f.name}\n使用插件：${pluginId}\n${basicSummaryText}\n\n—— 预览 ——\n${summary}`,
        timestamp:new Date(),
        plugin_info: { plugin_id: pluginId },
        parsed: { pluginId, fileName: f.name, res: resObj, basicSummary }
      })
      finishStep(5, true, ['已输出至对话区'])
    } catch (e) {
      finishStep(2, false, [e.message])
      messages.value.push({ role:'assistant', content:`解析失败（${f.name}）：${e.message}`, timestamp:new Date() })
    }
  }
  scrollToBottom()
}


const clearAllFiles = () => {
  uploadedFiles.value = []
  ElMessage.info('已清空所有文件')
}

const formatFileSize = (bytes) => {
  if (bytes === 0) return '0 B'
  const k = 1024
  const sizes = ['B', 'KB', 'MB', 'GB']
  const i = Math.floor(Math.log(bytes) / Math.log(k))
  return parseFloat((bytes / Math.pow(k, i)).toFixed(2)) + ' ' + sizes[i]
}

const getFileIcon = (fileType) => {
  if (fileType.startsWith('image/')) {
    return 'Picture'
  } else if (fileType.includes('pdf')) {
    return 'Document'
  } else if (fileType.includes('word') || fileType.includes('text')) {
    return 'Document'
  } else if (fileType.includes('excel') || fileType.includes('csv')) {
    return 'Grid'
  } else if (fileType.includes('zip') || fileType.includes('rar')) {
    return 'FolderOpened'
  } else {
    return 'Document'
  }
}

// === 案例导入与经验指导 ===
const uploadCaseExcel = async ({ file }) => {
  try {
    // 读取为 base64
    const toBase64 = (f) => new Promise((resolve, reject) => {
      const reader = new FileReader()
      reader.onload = () => resolve(String(reader.result).split(',').pop())
      reader.onerror = reject
      reader.readAsDataURL(f)
    })
    const base64 = await toBase64(file)
    const isIncoming = /来料|incoming/i.test(file.name)

    // 执行“案例应用指导”工作流
    const res = await cozeStudioAPI.executeCaseGuidanceWorkflow({
      fileName: file.name,
      fileType: file.type || 'application/octet-stream',
      base64,
      saveScope: kbScope.value,
      conversation_id: currentConversationId.value,
      query: inputMessage.value || undefined,
      category: isIncoming ? 'incoming' : 'manufacturing'
    })

    if (res.success) {
      ElMessage.success('案例导入并执行工作流成功')
      const guidance = res.data?.guidance
      if (guidance) messages.value.push({ role: 'assistant', content: guidance.summary || '已生成指导', timestamp: Date.now() })
      await refreshTraces()
    } else {
      ElMessage.error('执行工作流失败')
    }
  } catch (e) {
    ElMessage.error('案例导入失败：' + (e.message || '网络错误'))
  }
}

// 案例指导执行流程（右侧看板专用）
const initGuidanceFlow = (queryText) => {
  executionSteps.value = [
    { title: '解析问题', description: '提取关键字与分类', status: 'pending', details: [queryText.slice(0, 50)], duration: null },
    { title: '检索历史案例', description: '匹配相似案例与标签', status: 'pending', details: [], duration: null },
    { title: '生成经验指导', description: '形成summary与排查清单', status: 'pending', details: [], duration: null },
  ]
  executionStats.value = { totalSteps: 3, completedSteps: 0, totalDuration: 0 }
}

const markStep = (idx, status, details = []) => {
  const start = Date.now()
  executionSteps.value[idx].status = status
  if (details?.length) executionSteps.value[idx].details = details
  return () => {
    const d = Date.now() - start
    executionSteps.value[idx].duration = d
    executionStats.value.totalDuration += d
    if (status !== 'error') executionStats.value.completedSteps++
    executionSteps.value[idx].status = status === 'running' ? 'completed' : status
  }
}

const handleCaseGuidance = async () => {
  try {
    const queryText = inputMessage.value.trim() || (messages.value.filter(m=>m.role==='user').slice(-1)[0]?.content || '')
    if (!queryText) {
      ElMessage.info('请先输入问题或从上文选择文本')
      return
    }

    if (!currentConversationId.value) {
      currentConversationId.value = 'conv_' + Date.now()
    }

    const category = /来料|供应商|IQC/.test(queryText) ? 'incoming' : 'manufacturing'

    // 直接调用Coze工作流（无文件），后端会：search_kb→case_guidance→respond，并写入Traces
    const res = await cozeStudioAPI.executeCaseGuidanceWorkflow({
      query: queryText,
      saveScope: kbScope.value,
      conversation_id: currentConversationId.value,
      category
    })

    if (res.success) {
      const guidance = res.data?.guidance
      const kb_hits = res.data?.kb_hits || []
      if (guidance) {
        messages.value.push({ role: 'assistant', content: guidance.summary || '已生成指导', timestamp: Date.now(), rag_sources: kb_hits })
      }
      ElMessage.success('已生成经验指导')
      await refreshTraces()
    } else {
      ElMessage.error('经验指导生成失败')
    }
  } catch (error) {
    ElMessage.error('经验指导失败：' + (error.message || '网络错误'))
  }
}

// 图像理解（当消息包含图片时）
const handleVisionAnalysis = async () => {
  try {
    if (!hasImageFiles.value) {
      ElMessage.info('请先选择图片文件')
      return
    }
    // 仅取第一张图做示例
    const firstImg = uploadedFiles.value.find(f => f.type.startsWith('image/'))
    if (!firstImg) {
      ElMessage.error('未找到图片文件')
      return
    }

    // 获取实际的文件对象
    const actualFile = firstImg.file || firstImg.raw || firstImg
    if (!actualFile || !(actualFile instanceof File || actualFile instanceof Blob)) {
      ElMessage.error('无效的图片文件对象')
      return
    }

    const reader = new FileReader()
    reader.onload = async () => {
      try {
        const dataUrl = reader.result // data:image/...;base64,....
        // 调用后端视觉接口（我们已兼容 dataURL/base64/URL）
        initGuidanceFlow('图片异常识别')
        let done = markStep(0, 'running', ['图片已加载'])
        done()
        done = markStep(1, 'running', ['准备调用视觉模型'])
        const visionRes = await chatWithVision({ message: '分析图片中的质量异常', image_data_url: String(dataUrl) })
        const visionText = visionRes?.data?.response || '未识别到明显异常，请补充描述'
        done()
        done = markStep(2, 'running')
        const cat = /来料|供应商|IQC/.test(visionText) ? 'incoming' : 'manufacturing'
        const res = await getCaseGuidance({ query: visionText, category: cat, limit: 8 })
        const g = res.data?.guidance
        done()
        messages.value.push({
          role: 'assistant',
          content: `【图片分析结果】\n${visionText}\n\n【历史案例经验指导】\n${g?.summary || '未找到相关案例'}\n\n排查清单：\n- ${(g?.checklist||[]).join('\n- ')}`,
          timestamp: new Date(),
          model_info: { provider: 'Vision+CaseGuidance' }
        })
        scrollToBottom()
      } catch (error) {
        ElMessage.error('图像分析失败：' + (error.message || '网络错误'))
      }
    }
    reader.onerror = () => {
      ElMessage.error('文件读取失败')
    }
    reader.readAsDataURL(actualFile)
  } catch (e) {
    ElMessage.error('图像理解失败：' + (e.message || '网络错误'))
  }
}


// 格式化相对时间
const formatRelativeTime = (time) => {
  if (!time) return ''
  const now = new Date()
  const date = new Date(time)
  const diff = now - date

  const minutes = Math.floor(diff / (1000 * 60))
  const hours = Math.floor(diff / (1000 * 60 * 60))
  const days = Math.floor(diff / (1000 * 60 * 60 * 24))

  if (minutes < 1) return '刚刚'
  if (minutes < 60) return `${minutes}分钟前`
  if (hours < 24) return `${hours}小时前`
  if (days < 7) return `${days}天前`

  return date.toLocaleDateString()
}

// 截断标题
const truncateTitle = (title) => {
  if (!title) return '新对话'
  return title.length > 12 ? title.substring(0, 12) + '...' : title
}

// 截断预览内容
const truncatePreview = (content) => {
  if (!content) return '暂无消息'
  return content.length > 20 ? content.substring(0, 20) + '...' : content
}

const clearMessages = () => {
  ElMessageBox.confirm('确定要清空当前对话吗？', '提示', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    type: 'warning'
  }).then(() => {
    messages.value = []
    ElMessage.success('对话已清空')
  })
}

// 语音功能方法
const initSpeechRecognition = () => {
  if ('webkitSpeechRecognition' in window || 'SpeechRecognition' in window) {
    const SpeechRecognition = window.SpeechRecognition || window.webkitSpeechRecognition
    speechRecognition.value = new SpeechRecognition()

    speechRecognition.value.continuous = false
    speechRecognition.value.interimResults = false
    speechRecognition.value.lang = 'zh-CN'

    speechRecognition.value.onstart = () => {
      isRecording.value = true
      ElMessage.info('开始语音识别，请说话...')
    }

    speechRecognition.value.onresult = (event) => {
      const transcript = event.results[0][0].transcript
      inputMessage.value = transcript
      ElMessage.success('语音识别完成')
    }

    speechRecognition.value.onerror = (event) => {
      console.error('语音识别错误:', event.error)
      ElMessage.error('语音识别失败: ' + event.error)
      isRecording.value = false
    }

    speechRecognition.value.onend = () => {
      isRecording.value = false
    }
  } else {
    ElMessage.warning('您的浏览器不支持语音识别功能')
  }
}

const toggleVoiceRecognition = () => {
  if (!speechRecognition.value) {
    initSpeechRecognition()
    return
  }

  if (isRecording.value) {
    speechRecognition.value.stop()
  } else {
    speechRecognition.value.start()
  }
}

const toggleVoiceOutput = () => {
  voiceEnabled.value = !voiceEnabled.value
  ElMessage.info(voiceEnabled.value ? '语音播放已开启' : '语音播放已关闭')
}

const speakText = (text) => {
  if (!voiceEnabled.value || !('speechSynthesis' in window)) {
    return
  }

  // 停止当前播放
  window.speechSynthesis.cancel()

  const utterance = new SpeechSynthesisUtterance(text)
  utterance.lang = 'zh-CN'
  utterance.rate = 0.9
  utterance.pitch = 1
  utterance.volume = 0.8

  // 尝试使用中文语音
  const voices = window.speechSynthesis.getVoices()
  const chineseVoice = voices.find(voice =>
    voice.lang.includes('zh') || voice.name.includes('Chinese')
  )
  if (chineseVoice) {
    utterance.voice = chineseVoice
  }

  window.speechSynthesis.speak(utterance)
}

const newConversation = () => {
  clearMessages()
}

const showConversationHistory = () => {
  showHistoryDrawer.value = true
  refreshConversations()
}





const goToFullHistory = () => {
  router.push('/ai-management/conversations')
}

const exportCurrentChat = () => {
  ElMessage.info('导出功能开发中...')
}

const clearCurrentChat = () => {
  clearMessages()
}

// AI执行流程方法
const initializeExecutionFlow = (userInput) => {
  executionSteps.value = [
    {
      title: '输入预处理',
      description: '清理和标准化用户输入',
      status: 'pending',
      details: [],
      duration: null
    },
    {
      title: '需求解析',
      description: '分析用户输入，理解具体需求',
      status: 'pending',
      details: [],
      duration: null
    },
    {
      title: '意图识别',
      description: '识别用户意图和任务类型',
      status: 'pending',
      details: [],
      duration: null
    },
    {
      title: '上下文分析',
      description: '分析对话历史和上下文信息',
      status: 'pending',
      details: [],
      duration: null
    },
    {
      title: '工具评估',
      description: '评估需要使用的工具和资源',
      status: 'pending',
      details: [],
      duration: null
    },
    {
      title: '知识检索',
      description: '从知识库中检索相关信息',
      status: 'pending',
      details: [],
      duration: null,
    },
    {
      title: '信息整合',
      description: '整合检索到的多源信息',
      status: 'pending',
      details: [],
      duration: null
    },
    {
      title: '内容生成',
      description: '基于检索结果生成回答',
      status: 'pending',
      details: [],
      duration: null
    },
    {
      title: '质量检查',
      description: '验证回答的准确性和完整性',
      status: 'pending',
      details: [],
      duration: null
    },
    {
      title: '输出优化',
      description: '优化回答格式和表达方式',
      status: 'pending',
      details: [],
      duration: null
    }
  ]

  executionStats.value = {
    totalSteps: executionSteps.value.length,
    completedSteps: 0,
    totalDuration: 0
  }
}

const updateExecutionStep = (stepIndex, status, details = [], duration = null) => {
  if (stepIndex < executionSteps.value.length) {
    executionSteps.value[stepIndex].status = status
    if (details.length > 0) {
      executionSteps.value[stepIndex].details = details
    }
    if (duration !== null) {
      executionSteps.value[stepIndex].duration = duration
      executionStats.value.totalDuration += duration
    }
    if (status === 'completed') {
      executionStats.value.completedSteps++
    }
  }
}

const simulateExecutionFlow = async (userInput) => {
  initializeExecutionFlow(userInput)

  // 模拟执行流程
  for (let i = 0; i < executionSteps.value.length; i++) {
    updateExecutionStep(i, 'running')

    // 模拟处理时间
    const processingTime = Math.random() * 1000 + 500 // 500-1500ms
    await new Promise(resolve => setTimeout(resolve, processingTime))

    // 添加模拟的执行详情
    const details = getStepDetails(i, userInput)
    updateExecutionStep(i, 'completed', details, Math.round(processingTime))
  }
}

const getStepDetails = (stepIndex, userInput) => {
  const stepDetails = [
    [
      '清理特殊字符和格式',
      '标准化文本编码',
      '检测语言类型: 中文'
    ],
    [
      `解析输入: "${userInput.substring(0, 30)}..."`,




      '识别关键词和实体',
      '确定问题类型: 质量管理咨询'
    ],
    [
      '意图类型: 信息查询',
      '任务复杂度: 中等',
      '需要专业知识: 质量管理'
    ],
    [
      '分析对话历史: 3条记录',
      '提取上下文关键信息',
      '识别话题连续性'
    ],
    [
      '选择工具: 知识库检索',
      '评估资源: QMS文档库',
      '确定检索策略: 语义匹配'
    ],
    [
      '检索范围: ISO标准文档',
      '匹配度阈值: 0.8',
      '返回结果: 5个相关条目'
    ],
    [
      '合并多源信息',
      '去重和排序',
      '建立信息关联图'
    ],
    [
      '整合检索结果',
      '生成结构化回答',
      '添加实例和建议'
    ],
    [
      '检查内容完整性',
      '验证专业术语准确性',
      '确保回答相关性'
    ],
    [
      '优化语言表达',
      '调整回答结构',
      '添加格式化标记'
    ]
  ]

  return stepDetails[stepIndex] || []
}

// 拖动相关方法
const startDrag = (e) => {
  if (!executionFlowRef.value) return

  // 阻止事件冒泡，防止与页面滚动冲突
  e.preventDefault()
  e.stopPropagation()

  isDragging.value = true
  dragStart.value = { x: e.clientX, y: e.clientY }
  scrollStart.value = {
    top: executionFlowRef.value.scrollTop,
    left: executionFlowRef.value.scrollLeft
  }

  // 防止文本选择
  document.body.style.userSelect = 'none'
  executionFlowRef.value.style.cursor = 'grabbing'

  // 添加全局事件监听器
  document.addEventListener('mousemove', onDrag)
  document.addEventListener('mouseup', endDrag)
}

const onDrag = (e) => {
  if (!isDragging.value || !executionFlowRef.value) return

  e.preventDefault()
  e.stopPropagation()

  const deltaX = e.clientX - dragStart.value.x
  const deltaY = e.clientY - dragStart.value.y

  executionFlowRef.value.scrollTop = scrollStart.value.top - deltaY
  executionFlowRef.value.scrollLeft = scrollStart.value.left - deltaX
}

const endDrag = (e) => {
  if (!isDragging.value) return
  isDragging.value = false
  // 恢复样式
  document.body.style.userSelect = ''
  if (executionFlowRef.value) executionFlowRef.value.style.cursor = 'grab'
  // 移除事件监听
  document.removeEventListener('mousemove', onDrag)
  document.removeEventListener('mouseup', endDrag)
}


// 文件解析流程：初始化与打点（放在模拟流程定义之后，避免解析器冲突）
function initializeFileExecutionFlow(files = []) {
  const names = files.map(f => f.name).join(', ')
  executionSteps.value = [
    { title: '文件类别识别', description: `待识别：${names}`, status: 'pending', details: [], duration: null },
    { title: '选择解析工具', description: '根据文件类型匹配插件', status: 'pending', details: [], duration: null },
    { title: '调用插件解析', description: '读取数据并生成预览', status: 'pending', details: [], duration: null },
    { title: '结构化摘要', description: '提取列名与预览、生成简要摘要', status: 'pending', details: [], duration: null },
    { title: '初步汇总', description: '基础统计（行/列、缺失、唯一、范围）', status: 'pending', details: [], duration: null },
    { title: '结果注入聊天', description: '输出解析结果消息', status: 'pending', details: [], duration: null },
  ]
  executionStats.value = { totalSteps: executionSteps.value.length, completedSteps: 0, totalDuration: 0 }
}
function startStep(i, extra = []) {
  if (i >= executionSteps.value.length) return
  executionSteps.value[i].status = 'running'
  if (extra.length) executionSteps.value[i].details = extra
  executionSteps.value[i]._t = Date.now()
}
function finishStep(i, ok = true, extra = []) {
  if (i >= executionSteps.value.length) return
  const d = Date.now() - (executionSteps.value[i]._t || Date.now())
  executionSteps.value[i].duration = d
  executionStats.value.totalDuration += d
  if (extra.length) executionSteps.value[i].details = extra
  executionSteps.value[i].status = ok ? 'completed' : 'error'
  if (ok) executionStats.value.completedSteps++
}

// 解析结果展示：弹窗与导出
const parsedDialogVisible = ref(false)
const parsedDialogData = ref(null)
function openParsedDialog(parsed) {
  parsedDialogData.value = parsed
  parsedDialogVisible.value = true
}
function getParsedColumns(parsed) {
  const rows = (parsed?.res?.preview || parsed?.res?.rows || [])
  if (rows.length > 0) return Object.keys(rows[0])
  const cols = parsed?.res?.columns || {}
  const keys = Object.keys(cols)
  if (keys.length) return keys
  if (typeof parsed?.res?.text === 'string' && parsed.res.text.trim()) return ['content']
  return []
}
function getParsedPreview(parsed, limit = 20) {
  const rows = (parsed?.res?.preview || parsed?.res?.rows)
  if (Array.isArray(rows) && rows.length) return rows.slice(0, limit)
  const cols = parsed?.res?.columns || {}
  const keys = Object.keys(cols)
  if (keys.length) {
    const length = Math.min(limit, ...(keys.map(k => (Array.isArray(cols[k]) ? cols[k].length : 0)))) || 0
    const output = []
    for (let i = 0; i < length; i++) {
      const row = {}
      keys.forEach(k => { row[k] = Array.isArray(cols[k]) ? cols[k][i] : '' })
      output.push(row)
    }
function detectTextType(parsed){
  const t = String(parsed?.res?.type || '').toLowerCase()
  const name = String(parsed?.fileName || '').toLowerCase()
  if (t.includes('pdf') || name.endsWith('.pdf')) return 'pdf'
  if (t.includes('docx') || t.includes('doc') || name.endsWith('.docx') || name.endsWith('.doc')) return 'docx'
  if (t.includes('ocr') || t.includes('image')) return 'ocr'
  return 'text'
}
function normalizeTextForType(type, text=''){
  const s = String(text)
  switch(type){
    case 'pdf':
      // 将单换行合并为空格，保留空行作为段落
      return s.replace(/([^\n])\n(?!\n)/g, '$1 ')
    default:
      return s
  }
}
function getParsedText(parsed){
  const raw = parsed?.res?.text || ''
  const type = detectTextType(parsed)
  return normalizeTextForType(type, raw)
}

    return output
  }
  if (typeof parsed?.res?.text === 'string' && parsed.res.text.trim()) {
    return [{ content: parsed.res.text.slice(0, 800) }]
  }
  return []
}

function exportParsedMarkdown(parsed){
  const text = parsed?.res?.text || ''
  if(!text){ ElMessage.info('无可导出的文本'); return }
  const md = text
  const blob = new Blob([md], { type: 'text/markdown;charset=utf-8;' })
  const url = URL.createObjectURL(blob)
  const a = document.createElement('a'); a.href=url; a.download=(parsed?.fileName||'parsed')+'.md'; a.click(); URL.revokeObjectURL(url)
}

function getParsedRowCount(parsed) {
  if (parsed?.res?.row_count !== undefined) return parsed.res.row_count
  const arr = parsed?.res?.preview || parsed?.res?.rows
  if (Array.isArray(arr)) return arr.length
  if (typeof parsed?.res?.text === 'string' && parsed.res.text.trim()) return 1
  return null
}

function isTabularParsed(parsed){
  const rows = parsed?.res?.preview || parsed?.res?.rows
  const cols = parsed?.res?.columns
  return (Array.isArray(rows) && rows.length>0) || (cols && Object.keys(cols).length>0)
}

function hasTextParsed(parsed){
  return typeof parsed?.res?.text === 'string' && parsed.res.text.trim().length>0
}

function copyParsedText(parsed){
  try{
    const t = parsed?.res?.text || ''
    navigator.clipboard?.writeText(t)
    ElMessage.success('已复制文本')
  }catch(e){ ElMessage.error('复制失败') }
}

function exportParsedText(parsed){
  const t = parsed?.res?.text || ''
  const blob = new Blob([t], { type: 'text/plain;charset=utf-8;' })
  const url = URL.createObjectURL(blob)
  const a = document.createElement('a'); a.href=url; a.download=(parsed?.fileName||'parsed')+'.txt'; a.click(); URL.revokeObjectURL(url)
}

function exportParsedCSV(parsed) {
  const cols = getParsedColumns(parsed)
  const rows = parsed?.res?.preview || parsed?.res?.rows || getParsedPreview(parsed, 1000)
  const header = cols.join(',')
  const body = rows.map(r => cols.map(c => String(r[c] ?? '').replace(/"/g, '""')).map(v => /,|"|\n/.test(v) ? `"${v}"` : v).join(',')).join('\n')
  const csv = header + '\n' + body
  const blob = new Blob([csv], { type: 'text/csv;charset=utf-8;' })
  const url = URL.createObjectURL(blob)
  const a = document.createElement('a')
  a.href = url
  a.download = (parsed?.fileName || 'parsed') + '.csv'
  a.click()
  URL.revokeObjectURL(url)
}

function getTextStats(parsed){
  const t = parsed?.res?.text || ''
  const chars = t.length
  if(!t.trim()) return { chars: 0, paras: null }
  const paras = t.split(/\n{2,}/).filter(s=>s.trim().length>0).length
  return { chars, paras }
}

function getTextPreviewHTML(parsed, keyword='', limit=1600){
  const text = parsed?.res?.text || ''
  const clipped = text.length>limit ? (text.slice(0, limit) + '\n...（已截断预览，点击“展开全部”查看）') : text
  return renderHighlightedText(clipped, keyword)
}


const onWheel = (e) => {
  if (!executionFlowRef.value) return

  // 阻止事件冒泡
  e.stopPropagation()

  // 支持水平滚动
  if (e.shiftKey) {
    e.preventDefault()
    executionFlowRef.value.scrollLeft += e.deltaY
  }
}

const scrollToBottom = () => {
  nextTick(() => {
    const container = document.querySelector('.messages-container')
    if (container) {
      container.scrollTop = container.scrollHeight
    }
  })
}

// 生命周期
onMounted(async () => {
  // 加载用户统计
  try {
    const statsResponse = await getChatStatistics()

function copyBasicSummary(parsed) {
  try {
    const text = stringifyBasicSummary(parsed.basicSummary || buildBasicSummary(parsed.res))
    navigator.clipboard?.writeText(text)
    ElMessage.success('已复制初步汇总')
  } catch (e) {
    ElMessage.error('复制失败')
  }
}

function exportBasicSummaryCSV(parsed) {
  const sum = parsed.basicSummary || buildBasicSummary(parsed.res)
  const header = ['列名','类型','缺失','唯一','示例','最小值','最大值','均值']
  const lines = [header.join(',')]
  ;(sum.columns||[]).forEach(c => {
    const row = [
      c.name,
      c.type,
      c.missing,
      c.unique,
      (c.example ?? '').toString().replace(/"/g,'""'),
      c.min ?? '',
      c.max ?? '',
      c.mean ?? ''
    ]
    lines.push(row.map(v => /,|"|\n/.test(String(v)) ? `"${String(v).replace(/"/g,'""')}"` : v).join(','))
  })
  const blob = new Blob([lines.join('\n')], { type: 'text/csv;charset=utf-8;' })
  const url = URL.createObjectURL(blob)
  const a = document.createElement('a')
  a.href = url
  a.download = (parsed?.fileName || 'summary') + '_basic_summary.csv'
  a.click()
  URL.revokeObjectURL(url)
  ElMessage.success('已导出初步汇总CSV')
}

    if (statsResponse.success) {
      userStats.value = statsResponse.data
    }
  } catch (error) {
    console.error('获取统计信息失败:', error)
  }

  // 获取当前模型状态
  try {
    const statusResponse = await axios.get('/api/chat/model-status')
    if (statusResponse.data.success) {
      const currentModelInfo = statusResponse.data.data
      currentModel.value = currentModelInfo.provider || 'GPT-4o'
      modelStatus.provider = currentModelInfo.name || 'GPT-4o'
      modelStatus.status = 'online'
    }
  } catch (error) {
    console.error('获取当前模型状态失败:', error)
  }

  // 加载完整的8个AI模型
  try {
    const response = await axios.get('/api/chat/models')
    if (response.data.success) {
      availableModels.value = response.data.data.models || []
    } else {
      // 使用默认的8个模型配置（与后端保持一致）
      availableModels.value = [
        { provider: 'GPT-4o', name: 'GPT-4o', model: 'gpt-4o', features: { reasoning: false, toolCalls: true, vision: true }, enabled: true },
        { provider: 'Claude-3.5-Sonnet', name: 'Claude-3.5-Sonnet', model: 'claude-3-5-sonnet-20241022', features: { reasoning: false, toolCalls: true, vision: false }, enabled: true },
        { provider: 'DeepSeek-Chat', name: 'DeepSeek-Chat', model: 'deepseek-chat', features: { reasoning: false, toolCalls: true, vision: false, external: true }, enabled: true },
        { provider: 'DeepSeek-Coder', name: 'DeepSeek-Coder', model: 'deepseek-coder', features: { reasoning: false, toolCalls: true, vision: false, external: true }, enabled: true },
        { provider: 'Gemini-1.5-Pro', name: 'Gemini-1.5-Pro', model: 'gemini-1.5-pro', features: { reasoning: false, toolCalls: true, vision: true }, enabled: true },
        { provider: 'GPT-4-Turbo', name: 'GPT-4-Turbo', model: 'gpt-4-turbo', features: { reasoning: false, toolCalls: true, vision: true }, enabled: true },
        { provider: 'Claude-3-Haiku', name: 'Claude-3-Haiku', model: 'claude-3-haiku-20240307', features: { reasoning: false, toolCalls: true, vision: false }, enabled: true },
        { provider: 'GPT-3.5-Turbo', name: 'GPT-3.5-Turbo', model: 'gpt-3.5-turbo', features: { reasoning: false, toolCalls: true, vision: false }, enabled: true }
      ]
    }
  } catch (error) {
    console.error('加载AI模型配置失败:', error)
    // 使用默认配置（与后端保持一致）
    availableModels.value = [
      { provider: 'GPT-4o', name: 'GPT-4o', model: 'gpt-4o', features: { reasoning: false, toolCalls: true, vision: true }, enabled: true },
      { provider: 'Claude-3.5-Sonnet', name: 'Claude-3.5-Sonnet', model: 'claude-3-5-sonnet-20241022', features: { reasoning: false, toolCalls: true, vision: false }, enabled: true },
      { provider: 'DeepSeek-Chat', name: 'DeepSeek-Chat', model: 'deepseek-chat', features: { reasoning: false, toolCalls: true, vision: false, external: true }, enabled: true },
      { provider: 'DeepSeek-Coder', name: 'DeepSeek-Coder', model: 'deepseek-coder', features: { reasoning: false, toolCalls: true, vision: false, external: true }, enabled: true },
      { provider: 'Gemini-1.5-Pro', name: 'Gemini-1.5-Pro', model: 'gemini-1.5-pro', features: { reasoning: false, toolCalls: true, vision: true }, enabled: true },
      { provider: 'GPT-4-Turbo', name: 'GPT-4-Turbo', model: 'gpt-4-turbo', features: { reasoning: false, toolCalls: true, vision: true }, enabled: true },
      { provider: 'Claude-3-Haiku', name: 'Claude-3-Haiku', model: 'claude-3-haiku-20240307', features: { reasoning: false, toolCalls: true, vision: false }, enabled: true },
      { provider: 'GPT-3.5-Turbo', name: 'GPT-3.5-Turbo', model: 'gpt-3.5-turbo', features: { reasoning: false, toolCalls: true, vision: false }, enabled: true }
    ]
  }

  // 初始化对话列表
  await refreshConversations()

  // 加载用户统计
  try {
    const statsResponse = await getChatStatistics()
    if (statsResponse.success) {
      userStats.value = statsResponse.data
    }
  } catch (error) {
    console.error('加载用户统计失败:', error)
  }

  // 初始化语音功能
  initSpeechRecognition()

  // 初始化语音合成
  if ('speechSynthesis' in window) {
    // 预加载语音列表
    window.speechSynthesis.getVoices()
    // 监听语音列表变化
    window.speechSynthesis.onvoiceschanged = () => {
      window.speechSynthesis.getVoices()
    }
  }
})

// 组件卸载时清理事件监听器
onUnmounted(() => {
  if (isDragging.value) {
    document.removeEventListener('mousemove', onDrag)
    document.removeEventListener('mouseup', endDrag)
  }
})
</script>

<style scoped>
.optimized-chat-container {
  height: 100vh;
  display: flex;
  flex-direction: column;
  background: linear-gradient(135deg, #f5f7fa 0%, #c3cfe2 100%);
  position: relative;
  overflow: hidden;
}

.optimized-chat-container::before {
  content: '';
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background:
    radial-gradient(circle at 20% 80%, rgba(120, 119, 198, 0.3) 0%, transparent 50%),
    radial-gradient(circle at 80% 20%, rgba(255, 119, 198, 0.15) 0%, transparent 50%),
    radial-gradient(circle at 40% 40%, rgba(120, 219, 255, 0.1) 0%, transparent 50%);
  pointer-events: none;
  z-index: 0;
}

.optimized-chat-container > * {
  position: relative;
  z-index: 1;
}

/* 顶部工具栏 */
.chat-toolbar {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 16px 24px;
  background: rgba(255, 255, 255, 0.95);
  backdrop-filter: blur(10px);
  border-bottom: 1px solid rgba(228, 231, 237, 0.6);
  box-shadow: 0 4px 20px rgba(0,0,0,0.08);
  border-radius: 0 0 12px 12px;
}

.toolbar-left {
  display: flex;
  align-items: center;
  gap: 16px;
}

.toolbar-center {
  flex: 1;
  display: flex;
  justify-content: center;
  align-items: center;
  margin: 0 24px;
}

.status-metrics-inline {
  display: flex;
  align-items: center;
  gap: 20px;
  background: rgba(248, 250, 252, 0.8);
  border-radius: 8px;
  padding: 8px 16px;
  border: 1px solid rgba(226, 232, 240, 0.5);
}

.metric-group {
  display: flex;
  align-items: center;
  gap: 16px;
}

.metric-item-inline {
  display: flex;
  flex-direction: column;
  align-items: center;
  min-width: 60px;
}

.metric-label-inline {
  font-size: 10px;
  color: #6b7280;
  font-weight: 500;
  margin-bottom: 2px;
}

.metric-value-inline {
  font-size: 13px;
  font-weight: 600;
  color: #374151;
}

.metric-value-inline.highlight {
  color: #3b82f6;
  font-weight: 700;
}

.divider-vertical {
  width: 1px;
  height: 24px;
  background: rgba(226, 232, 240, 0.8);
}

.toolbar-right {
  display: flex;
  align-items: center;
  gap: 8px;
}

.model-status {
  display: flex;
  align-items: center;
  gap: 8px;
  font-size: 14px;
  color: #606266;
}

.model-option {
  display: flex;
  align-items: center;
  justify-content: space-between;
  width: 100%;
}

.model-name {
  font-weight: 500;
}



/* 主要对话区域 */
.main-chat-area {
  flex: 1;
  display: flex;
  gap: 14px;
  padding: 16px;
  overflow: hidden;
}

/* 左侧快捷功能面板 */
.quick-actions-panel {
  width: 300px;
  background: rgba(255, 255, 255, 0.95);
  backdrop-filter: blur(10px);
  border-radius: 16px;
  padding: 24px;
  box-shadow: 0 8px 32px rgba(0,0,0,0.12);
  overflow-y: auto;
  border: 1px solid rgba(255, 255, 255, 0.2);
}

.parsed-preview .text-preview {
  margin-top: 6px;
  background: #fff;
  border: 1px solid #e5e7eb;
  border-radius: 6px;
}
.parsed-preview .text-block {
  white-space: pre-wrap;
  font-family: ui-monospace, SFMono-Regular, Menlo, Monaco, Consolas, "Liberation Mono", "Courier New", monospace;
  font-size: 12px;
  line-height: 1.6;
  padding: 10px 12px;
  color: #334155;
  max-height: 240px;
  overflow: auto;
}

.panel-header {
  display: flex;
  align-items: center;
  gap: 8px;
  font-size: 16px;
  font-weight: 600;
  color: #303133;
  margin-bottom: 20px;
}

.action-categories {
  margin-bottom: 24px;
}

.category-item {
  margin-bottom: 20px;
}

.category-title {
  font-size: 14px;
  font-weight: 500;
  color: #606266;
  margin-bottom: 12px;
}

.action-buttons {
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.action-btn {
  justify-content: flex-start;
  width: 100%;
  height: 40px;
  border: 1px solid rgba(228, 231, 237, 0.6);
  background: rgba(250, 250, 250, 0.8);
  backdrop-filter: blur(5px);
  transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
  border-radius: 8px;
  font-weight: 500;
}

.action-btn:hover {
  background: linear-gradient(135deg, #ecf5ff 0%, #e1f3ff 100%);
  border-color: #409eff;
  color: #409eff;
  transform: translateY(-2px);
  box-shadow: 0 4px 12px rgba(64, 158, 255, 0.2);
}

/* 模型特性 */
.model-features {
  border-top: 1px solid #e4e7ed;
  padding-top: 20px;
}

.features-title {
  font-size: 14px;
  font-weight: 500;
  color: #606266;
  margin-bottom: 12px;
}

.feature-indicators {
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.feature-item {
  display: flex;
  align-items: center;
  gap: 8px;
  font-size: 13px;
  color: #909399;
}

.feature-indicator {
  width: 8px;
  height: 8px;
  border-radius: 50%;
  background: #dcdfe6;
  transition: background 0.3s;
}

.feature-indicator.active {
  background: #67c23a;
}

/* 中央对话区域 */
.conversation-area {
  flex: 1;
  min-width: 450px;
  display: flex;
  flex-direction: column;
  background: rgba(255, 255, 255, 0.95);
  backdrop-filter: blur(10px);
  border-radius: 16px;
  box-shadow: 0 8px 32px rgba(0,0,0,0.12);
  overflow: hidden;
  border: 1px solid rgba(255, 255, 255, 0.2);
  height: calc(100vh - 160px);
}

.messages-container {
  flex: 1;
  padding: 24px 32px;
  overflow-y: auto;
  scroll-behavior: smooth;
}

.welcome-message {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  height: 100%;
  text-align: center;
  padding: 40px 20px;
}

.welcome-content {
  max-width: 500px;
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 20px;
}

.robot-logo {
  margin-bottom: 24px;
  display: flex;
  justify-content: center;
}

.robot-head {
  position: relative;
  width: 80px;
  height: 80px;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  border-radius: 20px;
  display: flex;
  align-items: center;
  justify-content: center;
  box-shadow: 0 8px 32px rgba(102, 126, 234, 0.3);
  animation: robotFloat 3s ease-in-out infinite;
}

.robot-antenna {
  position: absolute;
  width: 3px;
  height: 15px;
  background: #ff4757;
  border-radius: 2px;
  top: -12px;
}

.robot-antenna::after {
  content: '';
  position: absolute;
  top: -4px;
  left: 50%;
  transform: translateX(-50%);
  width: 6px;
  height: 6px;
  background: #ff4757;
  border-radius: 50%;
}

.robot-antenna.left {
  left: 20px;
  transform: rotate(-15deg);
}

.robot-antenna.right {
  right: 20px;
  transform: rotate(15deg);
}

.robot-face {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 8px;
}

.robot-eyes {
  display: flex;
  gap: 12px;
}

.robot-eye {
  width: 12px;
  height: 12px;
  background: #ffffff;
  border-radius: 50%;
  position: relative;
}

.robot-eye::after {
  content: '';
  position: absolute;
  top: 2px;
  left: 2px;
  width: 8px;
  height: 8px;
  background: #2f3542;
  border-radius: 50%;
  animation: robotBlink 4s ease-in-out infinite;
}

.robot-mouth {
  width: 20px;
  height: 10px;
  border: 2px solid #ffffff;
  border-top: none;
  border-radius: 0 0 20px 20px;
}

@keyframes robotFloat {
  0%, 100% {
    transform: translateY(0px);
  }
  50% {
    transform: translateY(-8px);
  }
}

@keyframes robotBlink {
  0%, 90%, 100% {
    transform: scaleY(1);
  }
  95% {
    transform: scaleY(0.1);
  }
}

.welcome-title {
  color: #303133;
  font-size: 24px;
  font-weight: 600;
  margin: 0;
  line-height: 1.4;
}

.welcome-subtitle {
  color: #909399;
  font-size: 14px;
  margin: 0;
  line-height: 1.6;
  max-width: 400px;
}

/* 消息样式 */
.message-item {
  display: flex;
  gap: 16px;
  margin-bottom: 24px;
  animation: messageSlideIn 0.3s ease-out;
}

@keyframes messageSlideIn {
  from {
    opacity: 0;
    transform: translateY(20px);
  }
  to {
    opacity: 1;
    transform: translateY(0);
  }
}

.message-item.user {
  flex-direction: row-reverse;
}

.message-content {
  max-width: 80%;
  min-width: 240px;
}

.message-item.user .message-content {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  color: white;
  border-radius: 20px 20px 6px 20px;
  padding: 16px 22px;
  box-shadow: 0 4px 12px rgba(102, 126, 234, 0.3);
}

.message-item.assistant .message-content {
  background: rgba(245, 247, 250, 0.8);
  backdrop-filter: blur(10px);
  border-radius: 20px 20px 20px 6px;
  padding: 16px 22px;
  border: 1px solid rgba(228, 231, 237, 0.5);
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.05);
}

.message-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 8px;
.parsed-header{ display:flex; align-items:center; gap:8px; flex-wrap:wrap; }
.parsed-meta{ color:#64748b; font-size:12px; margin-left:4px; }
.parsed-actions{ margin-left:auto; display:flex; gap:4px; }
.basic-summary-text{ white-space:pre-wrap; font-size:12px; line-height:1.6; color:#334155; background:#fff; border:1px solid #e5e7eb; border-radius:6px; padding:10px 12px; }
.text-actions{ display:flex; align-items:center; gap:8px; }
.text-highlight{ background:#fff; border:1px solid #e5e7eb; border-radius:6px; padding:10px 12px; color:#334155; line-height:1.7; }
.text-highlight.compact{ max-height:240px; overflow:auto; }
.text-highlight h3{ font-size:14px; margin:12px 0 6px; color:#0f172a; }
.text-highlight h4{ font-size:13px; margin:10px 0 4px; color:#1f2937; }
.text-highlight mark{ background:#fef3c7; color:#111827; padding:0 2px; border-radius:2px; }

  font-size: 12px;
  opacity: 0.8;
}

.message-text {
  line-height: 1.6;
  word-wrap: break-word;
}

/* 消息中的文件显示 */
.message-files {
  margin-bottom: 12px;
  padding: 12px;
  background: rgba(255, 255, 255, 0.6);
  border-radius: 8px;
  border: 1px solid rgba(228, 231, 237, 0.5);
}

.message-item.user .message-files {
  background: rgba(255, 255, 255, 0.2);
  border-color: rgba(255, 255, 255, 0.3);
}

.files-title {
  display: flex;
  align-items: center;
  gap: 6px;
  font-size: 12px;
  font-weight: 500;
  color: #606266;
  margin-bottom: 8px;
}

.message-item.user .files-title {
  color: rgba(255, 255, 255, 0.9);
}

.files-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(120px, 1fr));
  gap: 8px;
}

.file-card {
  display: flex;
  flex-direction: column;
  align-items: center;
  padding: 8px;
  background: rgba(255, 255, 255, 0.8);
  border-radius: 6px;
  border: 1px solid rgba(228, 231, 237, 0.3);
  transition: all 0.2s ease;
}

.message-item.user .file-card {
  background: rgba(255, 255, 255, 0.15);
  border-color: rgba(255, 255, 255, 0.2);
}

.file-card:hover {
  transform: translateY(-1px);
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
}

.file-card-icon {
  font-size: 24px;
  color: #409eff;
  margin-bottom: 4px;
}

.message-item.user .file-card-icon {
  color: rgba(255, 255, 255, 0.9);
}

.file-card-info {
  text-align: center;
  width: 100%;
}

.file-card-name {
  font-size: 11px;
  font-weight: 500;
  color: #303133;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
  margin-bottom: 2px;
}

.message-item.user .file-card-name {
  color: rgba(255, 255, 255, 0.9);
}

.file-card-size {
  font-size: 10px;
  color: #909399;
}

.message-item.user .file-card-size {
  color: rgba(255, 255, 255, 0.7);
}

.message-meta {
  display: flex;
  align-items: center;
  gap: 8px;
  margin-top: 8px;
  font-size: 12px;
  opacity: 0.7;
}

.message-actions {
  display: flex;
  gap: 8px;
  margin-top: 8px;
}

.message-loading {
  display: flex;
  align-items: center;
  gap: 12px;
  color: #909399;
  font-size: 14px;
  padding: 8px 0;
}

.loading-dots {
  display: flex;
  gap: 4px;
}

.loading-dots .dot {
  width: 8px;
  height: 8px;
  border-radius: 50%;
  background: #409eff;
  animation: loadingPulse 1.4s infinite ease-in-out;
}

.loading-dots .dot:nth-child(1) {
  animation-delay: -0.32s;
}

.loading-dots .dot:nth-child(2) {
  animation-delay: -0.16s;
}

@keyframes loadingPulse {
  0%, 80%, 100% {
    transform: scale(0.8);
    opacity: 0.5;
  }
  40% {
    transform: scale(1);
    opacity: 1;
  }
}

/* 输入区域 */
.input-area {
  padding: 28px 32px;
  background: transparent;
}

/* 文件上传预览区域 */
.uploaded-files-preview {
  max-width: 800px;
  margin: 0 auto 16px;
  background: rgba(255, 255, 255, 0.95);
  border-radius: 12px;
  border: 1px solid rgba(64, 158, 255, 0.2);
  overflow: hidden;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
}

.files-header {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 12px 16px;
  background: rgba(64, 158, 255, 0.05);
  border-bottom: 1px solid rgba(64, 158, 255, 0.1);
  font-size: 14px;
  font-weight: 500;
  color: #303133;
}

.files-list {
  padding: 8px;
  max-height: 200px;
  overflow-y: auto;
}

.file-item {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 8px 12px;
  margin-bottom: 4px;
  background: rgba(255, 255, 255, 0.8);
  border-radius: 8px;
  border: 1px solid rgba(228, 231, 237, 0.5);
  transition: all 0.2s ease;
}

.file-item:hover {
  background: rgba(64, 158, 255, 0.05);
  border-color: rgba(64, 158, 255, 0.3);
}

.file-item:last-child {
  margin-bottom: 0;
}

.file-info {
  display: flex;
  align-items: center;
  gap: 10px;
  flex: 1;
  min-width: 0;
}

.file-icon {
  font-size: 18px;
  color: #409eff;
  flex-shrink: 0;
}

.file-details {
  display: flex;
  flex-direction: column;
  min-width: 0;
  flex: 1;
}

.file-name {
  font-size: 13px;
  font-weight: 500;
  color: #303133;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

.file-size {
  font-size: 11px;
  color: #909399;
  margin-top: 2px;
}

.file-actions {
  display: flex;
  align-items: center;
  gap: 4px;
  flex-shrink: 0;
}

.input-container {
  max-width: 800px;
  margin: 0 auto;
}

.input-wrapper {
  position: relative;
}

.input-actions {
  display: flex;
  align-items: center;
  gap: 4px;
}

.file-upload-btn {
  display: inline-block;
}

.file-upload-btn .el-upload {
  display: inline-block;
}

.message-input {
  border-radius: 24px;
  border: 1px solid #e4e7ed;
  background: rgba(255, 255, 255, 0.9);
  backdrop-filter: blur(10px);
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.1);
  transition: all 0.3s ease;
}

.message-input:hover {
  border-color: #409eff;
  box-shadow: 0 4px 20px rgba(64, 158, 255, 0.15);
}

.message-input:focus-within {
  border-color: #409eff;
  box-shadow: 0 4px 20px rgba(64, 158, 255, 0.2);
}

.message-input .el-input__wrapper {
  border-radius: 24px;
  padding: 16px 24px;
  min-height: 56px;
  box-shadow: none;
  border: none;
  background: transparent;
}

.message-input .el-input__inner {
  font-size: 14px;
  line-height: 1.5;
}

/* 语音控件样式 */
.voice-controls {
  display: flex;
  align-items: center;
  gap: 4px;
  margin-right: 8px;
}

.voice-controls .el-button {
  border: none;
  background: transparent;
  padding: 4px;
  min-height: auto;
  transition: all 0.3s ease;
}

.voice-controls .el-button:hover {
  background: rgba(64, 158, 255, 0.1);
  border-radius: 50%;
}

.voice-controls .el-button.recording {
  color: #f56c6c;
  animation: recordingPulse 1.5s ease-in-out infinite;
}

@keyframes recordingPulse {
  0%, 100% {
    opacity: 1;
    transform: scale(1);
  }
  50% {
    opacity: 0.7;
    transform: scale(1.1);
  }
}

/* 右侧信息面板 */
.info-panel {
  width: 400px;
  display: flex;
  flex-direction: column;
  gap: 16px;
  background: rgba(255, 255, 255, 0.95);
  backdrop-filter: blur(10px);
  border-radius: 16px;
  box-shadow: 0 8px 32px rgba(0,0,0,0.12);
  overflow: hidden;
  border: 1px solid rgba(255, 255, 255, 0.2);
  height: calc(100vh - 160px);
}



/* AI执行流程样式 */
.execution-flow {
  flex: 1;
  overflow: auto;
  padding: 20px;
  cursor: grab;
  user-select: none;
  position: relative;
  min-height: 0; /* 允许flex收缩 */

  /* 自定义滚动条 */
  scrollbar-width: thin;
  scrollbar-color: rgba(99, 102, 241, 0.3) rgba(248, 250, 252, 0.5);
}

.execution-flow::-webkit-scrollbar {
  width: 8px;
  height: 8px;
}

.execution-flow::-webkit-scrollbar-track {
  background: rgba(248, 250, 252, 0.5);
  border-radius: 4px;
}

.execution-flow::-webkit-scrollbar-thumb {
  background: rgba(99, 102, 241, 0.3);
  border-radius: 4px;
  transition: background 0.3s ease;
}

.execution-flow::-webkit-scrollbar-thumb:hover {
  background: rgba(99, 102, 241, 0.5);
}

.execution-flow::-webkit-scrollbar-corner {
  background: rgba(248, 250, 252, 0.5);
}

.execution-flow:active {
  cursor: grabbing;
}

.flow-step {
  display: flex;
  align-items: flex-start;
  gap: 16px;
  margin-bottom: 20px;
  padding: 16px 20px;
  border-radius: 12px;
  transition: all 0.4s cubic-bezier(0.4, 0, 0.2, 1);
  position: relative;
  backdrop-filter: blur(8px);
}

.flow-step:not(:last-child)::after {
  content: '';
  position: absolute;
  left: 31px;
  top: 60px;
  width: 3px;
  height: 24px;
  background: linear-gradient(180deg, #e5e7eb 0%, rgba(229, 231, 235, 0.3) 100%);
  border-radius: 2px;
  transition: all 0.4s ease;
}

.flow-step.completed::after {
  background: linear-gradient(180deg, #10b981 0%, rgba(16, 185, 129, 0.3) 100%);
  box-shadow: 0 0 8px rgba(16, 185, 129, 0.3);
}

.flow-step.active::after {
  background: linear-gradient(180deg, #3b82f6 0%, rgba(59, 130, 246, 0.3) 100%);
  box-shadow: 0 0 8px rgba(59, 130, 246, 0.3);
}

.flow-step.pending {
  background: rgba(248, 250, 252, 0.9);
  border: 1px solid rgba(226, 232, 240, 0.6);
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.04);
}

.flow-step.active {
  background: linear-gradient(135deg, rgba(59, 130, 246, 0.12) 0%, rgba(147, 197, 253, 0.08) 100%);
  border: 1px solid rgba(59, 130, 246, 0.25);
  box-shadow: 0 4px 20px rgba(59, 130, 246, 0.15);
  animation: activeGlow 3s ease-in-out infinite;
  transform: translateX(2px);
}

.flow-step.completed {
  background: linear-gradient(135deg, rgba(16, 185, 129, 0.12) 0%, rgba(110, 231, 183, 0.08) 100%);
  border: 1px solid rgba(16, 185, 129, 0.25);
  box-shadow: 0 4px 16px rgba(16, 185, 129, 0.12);
}

.flow-step.error {
  background: linear-gradient(135deg, rgba(239, 68, 68, 0.12) 0%, rgba(252, 165, 165, 0.08) 100%);
  border: 1px solid rgba(239, 68, 68, 0.25);
  box-shadow: 0 4px 16px rgba(239, 68, 68, 0.12);
}

.step-indicator {
  width: 40px;
  height: 40px;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;
  font-weight: 700;
  font-size: 14px;
  transition: all 0.4s cubic-bezier(0.4, 0, 0.2, 1);
  position: relative;
  overflow: hidden;
}

.step-indicator::before {
  content: '';
  position: absolute;
  inset: 0;
  border-radius: 50%;
  padding: 2px;
  background: linear-gradient(135deg, transparent, rgba(255, 255, 255, 0.3));
  mask: linear-gradient(#fff 0 0) content-box, linear-gradient(#fff 0 0);
  mask-composite: xor;
}

.flow-step.pending .step-indicator {
  background: linear-gradient(135deg, #f8fafc 0%, #e2e8f0 100%);
  color: #64748b;
  border: 2px solid #cbd5e1;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.08);
}

.flow-step.active .step-indicator {
  background: linear-gradient(135deg, #3b82f6 0%, #1d4ed8 100%);
  color: white;
  border: 2px solid #2563eb;
  box-shadow: 0 4px 16px rgba(59, 130, 246, 0.4);
  animation: indicatorPulse 2s ease-in-out infinite;
}

.flow-step.completed .step-indicator {
  background: linear-gradient(135deg, #10b981 0%, #059669 100%);
  color: white;
  border: 2px solid #047857;
  box-shadow: 0 4px 16px rgba(16, 185, 129, 0.3);
}

.flow-step.error .step-indicator {
  background: linear-gradient(135deg, #ef4444 0%, #dc2626 100%);
  color: white;
  border: 2px solid #b91c1c;
  box-shadow: 0 4px 16px rgba(239, 68, 68, 0.3);
}

.step-number {
  font-size: 12px;
  font-weight: 700;
}

.step-loading {
  animation: spin 1s linear infinite;
}

.step-success,
.step-error {
  font-size: 16px;
}

.step-content {
  flex: 1;
  min-width: 0;
  padding-top: 2px;
}

.step-title {
  font-weight: 700;
  color: #1f2937;
  margin-bottom: 6px;
  font-size: 15px;
  line-height: 1.3;
  letter-spacing: -0.01em;
}

.flow-step.active .step-title {
  color: #1e40af;
}

.flow-step.completed .step-title {
  color: #065f46;
}

.step-description {
  color: #6b7280;
  font-size: 13px;
  margin-bottom: 12px;
  line-height: 1.5;
  font-weight: 400;
}

.step-details {
  margin-top: 12px;
  background: rgba(248, 250, 252, 0.6);
  border-radius: 8px;
  padding: 10px 12px;
  border: 1px solid rgba(226, 232, 240, 0.5);
}

.detail-item {
  font-size: 12px;
  color: #64748b;
  margin-bottom: 4px;
  padding-left: 12px;
  border-left: 3px solid #e2e8f0;
  line-height: 1.4;
  position: relative;
}

.detail-item::before {
  content: '•';
  position: absolute;
  left: -8px;
  color: #94a3b8;
  font-weight: bold;
}

.flow-step.completed .detail-item {
  border-left-color: #10b981;
  color: #047857;
}

.flow-step.completed .detail-item::before {
  color: #10b981;
}

.flow-step.active .detail-item {
  border-left-color: #3b82f6;
  color: #1e40af;
}

.flow-step.active .detail-item::before {
  color: #3b82f6;
}

.step-duration {
  font-size: 11px;
  color: #94a3b8;
  margin-top: 8px;
  font-weight: 500;
  background: rgba(248, 250, 252, 0.8);
  padding: 4px 8px;
  border-radius: 6px;
  display: inline-block;
  border: 1px solid rgba(226, 232, 240, 0.5);
}

/* 执行统计样式 */
.execution-stats {
  background: linear-gradient(135deg, rgba(248, 250, 252, 0.95) 0%, rgba(241, 245, 249, 0.9) 100%);
  border-radius: 12px;
  padding: 20px;
  border: 1px solid rgba(226, 232, 240, 0.6);
  margin: 0 20px 20px 20px; /* 只保留左右和底部margin */
  backdrop-filter: blur(8px);
  box-shadow: 0 4px 16px rgba(0, 0, 0, 0.06);
  flex-shrink: 0; /* 防止被压缩 */
}

.stats-header {
  font-weight: 700;
  color: #1f2937;
  margin-bottom: 16px;
  font-size: 15px;
  display: flex;
  align-items: center;
  gap: 8px;
}

.stats-header::before {
  content: '📊';
  font-size: 16px;
}

.stats-grid {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 16px;
}

.execution-stats .stat-item {
  text-align: center;
  padding: 16px 12px;
  background: linear-gradient(135deg, rgba(255, 255, 255, 0.9) 0%, rgba(248, 250, 252, 0.8) 100%);
  border-radius: 10px;
  border: 1px solid rgba(226, 232, 240, 0.4);
  transition: all 0.3s ease;
  position: relative;
  overflow: hidden;
}

.execution-stats .stat-item::before {
  content: '';
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  height: 3px;
  background: linear-gradient(90deg, #3b82f6, #8b5cf6);
  border-radius: 10px 10px 0 0;
}

.execution-stats .stat-item:hover {
  transform: translateY(-2px);
  box-shadow: 0 8px 24px rgba(0, 0, 0, 0.1);
}

.execution-stats .stat-value {
  font-size: 20px;
  font-weight: 800;
  color: #1f2937;
  margin-bottom: 4px;
  background: linear-gradient(135deg, #3b82f6, #8b5cf6);
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
  background-clip: text;
}

.execution-stats .stat-label {
  font-size: 11px;
  color: #64748b;
  font-weight: 600;
  text-transform: uppercase;
  letter-spacing: 0.5px;
}

@keyframes activeGlow {
  0%, 100% {
    box-shadow: 0 4px 20px rgba(59, 130, 246, 0.15);
  }
  50% {
    box-shadow: 0 8px 32px rgba(59, 130, 246, 0.25);
  }
}

@keyframes indicatorPulse {
  0%, 100% {
    box-shadow: 0 4px 16px rgba(59, 130, 246, 0.4);
    transform: scale(1);
  }
  50% {
    box-shadow: 0 6px 24px rgba(59, 130, 246, 0.6);
    transform: scale(1.05);
  }
}

@keyframes spin {
  from {
    transform: rotate(0deg);
  }
  to {
    transform: rotate(360deg);
  }
}

@keyframes fadeInUp {
  from {
    opacity: 0;
    transform: translateY(20px);
  }
  to {
    opacity: 1;
    transform: translateY(0);
  }
}

.panel-section {
  background: rgba(255, 255, 255, 0.95);
  backdrop-filter: blur(10px);
  border-radius: 16px;
  padding: 24px;
  box-shadow: 0 8px 32px rgba(0,0,0,0.12);
  border: 1px solid rgba(255, 255, 255, 0.2);
}

.info-panel .section-header {
  display: flex;
  align-items: center;
  gap: 12px;
  font-weight: 700;
  color: #1f2937;
  margin-bottom: 0;
  font-size: 16px;
  padding: 20px;
  background: linear-gradient(135deg, rgba(99, 102, 241, 0.1) 0%, rgba(139, 92, 246, 0.08) 100%);
  border-radius: 0;
  border-bottom: 1px solid rgba(99, 102, 241, 0.15);
  position: relative;
  overflow: hidden;
}

.info-panel .section-header::before {
  content: '';
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  height: 3px;
  background: linear-gradient(90deg, #6366f1, #8b5cf6);
}

.info-panel .section-header .el-icon {
  color: #6366f1;
  font-size: 18px;
  background: rgba(99, 102, 241, 0.1);
  padding: 6px;
  border-radius: 8px;
}

.drag-hint {
  display: flex;
  align-items: center;
  gap: 4px;
  margin-left: auto;
  font-size: 11px;
  color: #94a3b8;
  font-weight: 500;
  opacity: 0.8;
  transition: opacity 0.3s ease;
}

.drag-hint:hover {
  opacity: 1;
}

.drag-hint .el-icon {
  font-size: 12px;
  color: #94a3b8;
  background: rgba(148, 163, 184, 0.1);
  padding: 2px;
  border-radius: 4px;
}

.execution-details {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.detail-item {
  display: flex;
  justify-content: space-between;
  align-items: center;
  font-size: 14px;
}

.detail-item label {
  color: #606266;
  font-weight: 500;
}

.detail-item span {
  color: #303133;
}

.usage-stats {
  display: flex;
  justify-content: space-around;
}

.stat-item {
  text-align: center;
}

.stat-number {
  font-size: 24px;
  font-weight: 600;
  color: #409eff;
  margin-bottom: 4px;
}

.stat-label {
  font-size: 12px;
  color: #909399;
}

/* 对话记录抽屉 */
.history-content {
  padding: 16px;
}

.history-actions {
  display: flex;
  gap: 8px;
  margin-bottom: 16px;
}

.conversation-list {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.conversation-item {
  padding: 12px;
  border: 1px solid #e4e7ed;
  border-radius: 8px;
  cursor: pointer;
  transition: all 0.3s;
}

.conversation-item:hover {
  border-color: #409eff;
  background: #ecf5ff;
}

.conversation-title {
  font-weight: 500;
  color: #303133;
  margin-bottom: 4px;
}

.conversation-time {
  font-size: 12px;
  color: #909399;
  margin-bottom: 4px;
}

.conversation-preview {
  font-size: 13px;
  color: #606266;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

/* 左侧功能面板样式 */
.left-sidebar {
  width: 310px;
  background: rgba(255, 255, 255, 0.95);
  backdrop-filter: blur(10px);
  border-radius: 12px;
  box-shadow: 0 8px 32px rgba(0, 0, 0, 0.1);
  border: 1px solid rgba(255, 255, 255, 0.2);
  height: calc(100vh - 160px);
  overflow-y: auto;
  display: flex;
  flex-direction: column;
}

.sidebar-section {
  border-bottom: 1px solid rgba(228, 231, 237, 0.3);
  flex-shrink: 0;
}

.sidebar-section:last-child {
  border-bottom: none;
  flex: 1;
}

.section-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 16px 20px 12px;
  font-weight: 600;
  color: #303133;
  font-size: 14px;
  background: rgba(64, 158, 255, 0.05);
}

.section-header.centered {
  justify-content: center;
  gap: 8px;
}

.section-header.centered span {
  flex: 1;
  text-align: center;
}

/* 对话管理样式 */
.conversation-list {
  max-height: 320px;
  overflow-y: auto;
  padding: 0 16px 12px;
}

.conversation-item {
  padding: 8px 10px;
  border-radius: 6px;
  cursor: pointer;
  transition: all 0.2s ease;
  margin-bottom: 4px;
  border: 1px solid transparent;
  background: rgba(255, 255, 255, 0.3);
  position: relative;
}

.conversation-item:hover {
  background: rgba(64, 158, 255, 0.08);
  border-color: rgba(64, 158, 255, 0.2);
  transform: translateX(2px);
}

.conversation-item.active {
  background: rgba(64, 158, 255, 0.15);
  border-color: rgba(64, 158, 255, 0.4);
  box-shadow: 0 2px 8px rgba(64, 158, 255, 0.2);
}

.conversation-item:last-child {
  margin-bottom: 0;
}

.conversation-content {
  width: 100%;
}

.conversation-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 4px;
}

.conversation-title {
  font-size: 12px;
  font-weight: 600;
  color: #303133;
  line-height: 1.2;
  flex: 1;
}

.conversation-status {
  flex-shrink: 0;
}

.status-icon {
  font-size: 10px;
  color: #67c23a;
}

.conversation-preview {
  font-size: 10px;
  color: #909399;
  line-height: 1.3;
  margin-bottom: 4px;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.conversation-footer {
  display: flex;
  justify-content: space-between;
  align-items: center;
  font-size: 9px;
  color: #c0c4cc;
}

.conversation-time {
  flex: 1;
}

.message-count {
  background: rgba(64, 158, 255, 0.1);
  color: #409eff;
  padding: 1px 4px;
  border-radius: 8px;
  font-size: 9px;
}

.view-more-conversations {
  padding: 8px 0;
  border-top: 1px solid rgba(228, 231, 237, 0.3);
  margin-top: 8px;
}

.view-more-btn {
  width: 100%;
  font-size: 11px;
  color: #409eff;
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 4px;
}

.empty-conversations {
  display: flex;
  flex-direction: column;
  align-items: center;
  padding: 16px;
  color: #c0c4cc;
  font-size: 11px;
  gap: 6px;
}

.empty-conversations .el-icon {
  font-size: 24px;
  color: #e4e7ed;
}

/* 工具调用样式 */
.tool-categories {
  padding: 0 16px 12px;
  display: flex;
  flex-wrap: wrap;
  gap: 6px;
}

.category-btn {
  flex: 1;
  min-width: 0;
  font-size: 10px;
  padding: 4px 6px;
  height: 28px;
}

.category-btn .el-icon {
  font-size: 12px;
  margin-right: 2px;
}

.tools-container {
  padding: 0 16px 12px;
}

.tools-list {
  max-height: 200px;
  overflow-y: auto;
  margin-bottom: 8px;
}

.tools-pagination {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 8px;
  padding: 8px 0;
  border-top: 1px solid rgba(228, 231, 237, 0.3);
}

.page-info {
  font-size: 11px;
  color: #909399;
  min-width: 40px;
  text-align: center;
}

.tool-item {
  display: flex;
  align-items: center;
  padding: 8px 10px;
  border-radius: 6px;
  cursor: pointer;
  transition: all 0.2s ease;
  margin-bottom: 6px;
  border: 1px solid rgba(228, 231, 237, 0.3);
  background: rgba(255, 255, 255, 0.5);
}

.tool-item:hover {
  background: rgba(64, 158, 255, 0.08);
  border-color: rgba(64, 158, 255, 0.3);
  transform: translateX(2px);
  box-shadow: 0 2px 8px rgba(64, 158, 255, 0.15);
}

.tool-item:last-child {
  margin-bottom: 0;
}

.tool-icon {
  width: 28px;
  height: 28px;
  border-radius: 6px;
  background: linear-gradient(135deg, #409eff, #66b1ff);
  display: flex;
  align-items: center;
  justify-content: center;
  color: white;
  margin-right: 10px;
  flex-shrink: 0;
}

.tool-icon .el-icon {
  font-size: 14px;
}

.tool-info {
  flex: 1;
  min-width: 0;
}

.tool-name {
  font-size: 12px;
  font-weight: 500;
  color: #303133;
  margin-bottom: 2px;
  line-height: 1.2;
}

.tool-desc {
  font-size: 10px;
  color: #909399;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
  line-height: 1.2;
}

.tool-status {
  flex-shrink: 0;
}

.tool-status .el-tag {
  font-size: 9px;
  padding: 1px 4px;
  height: 18px;
}

/* 便捷对话样式 */
.quick-questions {
  padding: 0 16px 12px;
  flex: 1;
  overflow-y: auto;
  max-height: 280px;
}

.question-category {
  margin-bottom: 12px;
}

.question-category:last-child {
  margin-bottom: 0;
}

.category-title {
  font-size: 12px;
  font-weight: 600;
  color: #303133;
  margin-bottom: 8px;
  padding: 0 4px;
  border-left: 3px solid #409eff;
  padding-left: 8px;
}

.question-list {
  display: flex;
  flex-direction: column;
  gap: 4px;
}

.question-item {
  display: flex;
  align-items: center;
  gap: 6px;
  padding: 6px 10px;
  border-radius: 6px;
  cursor: pointer;
  transition: all 0.2s ease;
  font-size: 11px;
  color: #606266;
  border: 1px solid transparent;
  background: rgba(0, 0, 0, 0.02);
}

.question-item:hover {
  background: rgba(64, 158, 255, 0.08);
  border-color: rgba(64, 158, 255, 0.2);
  color: #409eff;
  transform: translateX(2px);
}

.question-item .el-icon {
  font-size: 12px;
  flex-shrink: 0;
}

/* 顶部模型特性展示 */
.model-status-features {
  display: flex;
  align-items: center;
  gap: 16px;
}

.model-features-inline {
  display: flex;
  align-items: center;
  gap: 12px;
}

.model-features-inline .feature-item {
  display: flex;
  align-items: center;
  gap: 4px;
  font-size: 12px;
  color: #606266;
}

/* 响应式设计 */
@media (max-width: 1200px) {
  .toolbar-center {
    margin: 0 16px;
  }

  .status-metrics-inline {
    gap: 16px;
    padding: 6px 12px;
  }

  .metric-group {
    gap: 12px;
  }

  .metric-item-inline {
    min-width: 50px;
  }

  .metric-value-inline {
    font-size: 12px;
  }

  .main-chat-area {
    gap: 10px;
  }

  .left-sidebar {
    width: 290px;
  }

  .info-panel {
    width: 380px;
  }
}

@media (max-width: 768px) {
  .chat-toolbar {
    flex-direction: column;
    gap: 12px;
    padding: 12px 16px;
  }

  .toolbar-center {
    margin: 0;
    order: 2;
  }

  .toolbar-left {
    order: 1;
  }

  .toolbar-right {
    order: 3;
  }

  .status-metrics-inline {
    flex-direction: column;
    gap: 12px;
    padding: 8px 12px;
  }

  .metric-group {
    gap: 8px;
  }

  .metric-item-inline {
    min-width: 45px;
  }

  .metric-value-inline {
    font-size: 11px;
  }

  .metric-label-inline {
    font-size: 9px;
  }

  .divider-vertical {
    display: none;
  }

  .main-chat-area {
    flex-direction: column;
  }

  .left-sidebar,
  .info-panel {
    width: 100%;
    order: 2;
  }

  .conversation-area {
    order: 1;
    min-height: 500px;
  }
}
</style>
