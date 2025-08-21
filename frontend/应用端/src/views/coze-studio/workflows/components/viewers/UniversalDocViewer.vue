<template>
  <div class="enhanced-doc-viewer">
    <!-- 工具栏 -->
    <div class="viewer-toolbar" v-if="showToolbar">
      <div class="toolbar-left">
        <el-button-group size="small">
          <el-button :icon="Search" @click="toggleSearch" :type="searchVisible ? 'primary' : ''">搜索</el-button>
          <el-button :icon="List" @click="toggleNavigation" :type="navigationVisible ? 'primary' : ''">导航</el-button>
          <el-button :icon="Download" @click="exportContent">导出</el-button>
        </el-button-group>
      </div>

      <div class="toolbar-center" v-if="pagination">
        <span class="page-info">
          第 {{ currentPage }} / {{ pagination.totalPages }} 页
        </span>
        <el-button-group size="small">
          <el-button :icon="ArrowLeft" @click="prevPage" :disabled="currentPage <= 1">上一页</el-button>
          <el-button :icon="ArrowRight" @click="nextPage" :disabled="currentPage >= pagination.totalPages">下一页</el-button>
        </el-button-group>
      </div>

      <div class="toolbar-right">
        <el-select v-model="viewMode" size="small" style="width: 100px">
          <el-option label="原始" value="raw" />
          <el-option label="格式化" value="formatted" />
          <el-option label="HTML" value="html" v-if="result.html" />
        </el-select>
      </div>
    </div>

    <!-- 搜索栏 -->
    <div class="search-bar" v-if="searchVisible">
      <el-input
        v-model="searchQuery"
        placeholder="输入搜索关键词..."
        @input="performSearch"
        @keyup.enter="performSearch"
        clearable
      >
        <template #prefix>
          <el-icon><Search /></el-icon>
        </template>
      </el-input>

      <div class="search-results" v-if="searchResults.length > 0">
        <span class="search-count">找到 {{ searchResults.length }} 个结果</span>
        <el-button-group size="small">
          <el-button @click="prevSearchResult" :disabled="currentSearchIndex <= 0">上一个</el-button>
          <el-button @click="nextSearchResult" :disabled="currentSearchIndex >= searchResults.length - 1">下一个</el-button>
        </el-button-group>
      </div>
    </div>

    <!-- 主要内容区域 -->
    <div class="viewer-content">
      <!-- 导航面板 -->
      <div class="navigation-panel" v-if="navigationVisible && navigation">
        <div class="nav-sections">
          <h4>目录</h4>
          <div class="section-list">
            <div
              v-for="section in navigation.sections"
              :key="section.title"
              class="section-item"
              :class="{ active: section.pageNumber === currentPage }"
              @click="jumpToPage(section.pageNumber)"
            >
              <span :class="`level-${section.level}`">{{ section.title }}</span>
            </div>
          </div>
        </div>

        <div class="nav-pages" v-if="pagination">
          <h4>页面</h4>
          <div class="page-list">
            <div
              v-for="page in navigation.pageList"
              :key="page.pageNumber"
              class="page-item"
              :class="{ active: page.pageNumber === currentPage }"
              @click="jumpToPage(page.pageNumber)"
            >
              <div class="page-title">{{ page.title }}</div>
              <div class="page-summary">{{ page.summary }}</div>
              <div class="page-stats">
                <span v-if="page.hasHeaders">📋</span>
                <span v-if="page.hasList">📝</span>
                <span v-if="page.hasTable">📊</span>
                <span class="word-count">{{ page.wordCount }}字</span>
              </div>
            </div>
          </div>
        </div>
      </div>

      <!-- 文档显示区域 -->
      <div class="document-display" :class="{ 'with-navigation': navigationVisible }">
        <!-- 分页显示 -->
        <div v-if="pagination && pagination.pages.length > 0" class="paginated-content">
          <div class="page-content">
            <component
              :is="viewer"
              :result="currentPageResult"
              :search-query="searchQuery"
              :search-highlights="currentPageSearchHighlights"
            />
          </div>

          <!-- 页面导航 -->
          <div class="page-navigation">
            <el-pagination
              v-model:current-page="currentPage"
              :page-size="1"
              :total="pagination.totalPages"
              layout="prev, pager, next, jumper"
              @current-change="handlePageChange"
            />
          </div>
        </div>

        <!-- 非分页显示 -->
        <div v-else class="single-content">
          <component
            :is="viewer"
            :result="displayResult"
            :search-query="searchQuery"
            :search-highlights="searchHighlights"
          />
        </div>
      </div>
    </div>

    <!-- 内容统计 -->
    <div class="content-stats" v-if="showStats">
      <div class="stats-item">
        <span class="label">字符数:</span>
        <span class="value">{{ contentStats.characters }}</span>
      </div>
      <div class="stats-item">
        <span class="label">字数:</span>
        <span class="value">{{ contentStats.words }}</span>
      </div>
      <div class="stats-item" v-if="contentStats.paragraphs">
        <span class="label">段落:</span>
        <span class="value">{{ contentStats.paragraphs }}</span>
      </div>
      <div class="stats-item" v-if="pagination">
        <span class="label">页数:</span>
        <span class="value">{{ pagination.totalPages }}</span>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, watch, onMounted } from 'vue'
import { Search, List, Download, ArrowLeft, ArrowRight } from '@element-plus/icons-vue'
import DocxViewer from './doc/DocxViewer.vue'
import PdfViewer from './pdf/PdfViewer.vue'
import ExcelViewer from './sheet/ExcelViewer.vue'
import PptViewer from './ppt/PptViewer.vue'
import TextViewer from './text/TextViewer.vue'

const props = defineProps({
  result: { type: Object, required: true },
  showToolbar: { type: Boolean, default: true },
  showStats: { type: Boolean, default: true },
  enablePagination: { type: Boolean, default: true },
  enableSearch: { type: Boolean, default: true },
  enableNavigation: { type: Boolean, default: true }
})

// 响应式数据
const searchVisible = ref(false)
const navigationVisible = ref(false)
const searchQuery = ref('')
const searchResults = ref([])
const currentSearchIndex = ref(0)
const currentPage = ref(1)
const viewMode = ref('formatted')

// 计算属性
const viewer = computed(() => ({
  docx: DocxViewer,
  pdf: PdfViewer,
  xlsx: ExcelViewer,
  csv: ExcelViewer,
  pptx: PptViewer,
  text: TextViewer,
  image: TextViewer
}[props.result?.metadata?.format || props.result?.format || 'text'] || TextViewer))

const pagination = computed(() => {
  return props.result?.pagination || null
})

const navigation = computed(() => {
  return pagination.value?.navigation || null
})

const contentStats = computed(() => {
  if (pagination.value) {
    return pagination.value.statistics || {}
  }

  const content = props.result?.content || ''
  return {
    characters: content.length,
    words: content.split(/\s+/).filter(w => w.length > 0).length,
    paragraphs: content.split(/\n\s*\n/).length
  }
})

const displayResult = computed(() => {
  if (viewMode.value === 'html' && props.result.html) {
    return { ...props.result, content: props.result.html, format: 'html' }
  }

  if (viewMode.value === 'raw' && props.result.rawContent) {
    return { ...props.result, content: props.result.rawContent }
  }

  return props.result
})

const currentPageResult = computed(() => {
  if (!pagination.value || !pagination.value.pages) {
    return displayResult.value
  }

  const page = pagination.value.pages[currentPage.value - 1]
  if (!page) return displayResult.value

  return {
    ...displayResult.value,
    content: page.content,
    metadata: {
      ...displayResult.value.metadata,
      pageNumber: page.pageNumber,
      pageTitle: page.title || `第 ${page.pageNumber} 页`
    }
  }
})

const currentPageSearchHighlights = computed(() => {
  if (!searchResults.value.length) return []

  return searchResults.value.filter(result =>
    result.pageNumber === currentPage.value
  )
})

const searchHighlights = computed(() => {
  return searchResults.value
})

// 方法
const toggleSearch = () => {
  searchVisible.value = !searchVisible.value
  if (!searchVisible.value) {
    searchQuery.value = ''
    searchResults.value = []
  }
}

const toggleNavigation = () => {
  navigationVisible.value = !navigationVisible.value
}

const performSearch = async () => {
  if (!searchQuery.value.trim()) {
    searchResults.value = []
    return
  }

  try {
    if (pagination.value && pagination.value.pages) {
      // 搜索分页内容
      const response = await fetch('/api/search-paginated-content', {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({
          query: searchQuery.value,
          pages: pagination.value.pages
        })
      })

      const result = await response.json()
      if (result.success) {
        searchResults.value = result.data.results
        currentSearchIndex.value = 0

        // 跳转到第一个搜索结果
        if (searchResults.value.length > 0) {
          jumpToPage(searchResults.value[0].pageNumber)
        }
      }
    } else {
      // 简单文本搜索
      const content = props.result.content || ''
      const regex = new RegExp(searchQuery.value, 'gi')
      const matches = []
      let match

      while ((match = regex.exec(content)) !== null) {
        matches.push({
          position: match.index,
          text: match[0],
          context: content.substring(
            Math.max(0, match.index - 50),
            Math.min(content.length, match.index + match[0].length + 50)
          )
        })
      }

      searchResults.value = matches
      currentSearchIndex.value = 0
    }
  } catch (error) {
    console.error('搜索失败:', error)
  }
}

const prevSearchResult = () => {
  if (currentSearchIndex.value > 0) {
    currentSearchIndex.value--
    const result = searchResults.value[currentSearchIndex.value]
    if (result.pageNumber) {
      jumpToPage(result.pageNumber)
    }
  }
}

const nextSearchResult = () => {
  if (currentSearchIndex.value < searchResults.value.length - 1) {
    currentSearchIndex.value++
    const result = searchResults.value[currentSearchIndex.value]
    if (result.pageNumber) {
      jumpToPage(result.pageNumber)
    }
  }
}

const prevPage = () => {
  if (currentPage.value > 1) {
    currentPage.value--
  }
}

const nextPage = () => {
  if (pagination.value && currentPage.value < pagination.value.totalPages) {
    currentPage.value++
  }
}

const jumpToPage = (pageNumber) => {
  if (pagination.value && pageNumber >= 1 && pageNumber <= pagination.value.totalPages) {
    currentPage.value = pageNumber
  }
}

const handlePageChange = (page) => {
  currentPage.value = page
}

const exportContent = () => {
  const content = pagination.value
    ? pagination.value.pages.map(p => p.content).join('\n\n---\n\n')
    : props.result.content || ''

  const blob = new Blob([content], { type: 'text/plain;charset=utf-8' })
  const url = URL.createObjectURL(blob)
  const link = document.createElement('a')
  link.href = url
  link.download = `document_${Date.now()}.txt`
  document.body.appendChild(link)
  link.click()
  document.body.removeChild(link)
  URL.revokeObjectURL(url)
}
</script>

<style scoped>
.enhanced-doc-viewer {
  display: flex;
  flex-direction: column;
  height: 100%;
  background: #fff;
  border-radius: 8px;
  overflow: hidden;
}

/* 工具栏 */
.viewer-toolbar {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 12px 16px;
  background: #f8f9fa;
  border-bottom: 1px solid #e9ecef;
}

.toolbar-left, .toolbar-center, .toolbar-right {
  display: flex;
  align-items: center;
  gap: 8px;
}

.page-info {
  font-size: 14px;
  color: #666;
  margin: 0 12px;
}

/* 搜索栏 */
.search-bar {
  padding: 12px 16px;
  background: #fff;
  border-bottom: 1px solid #e9ecef;
}

.search-results {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-top: 8px;
}

.search-count {
  font-size: 12px;
  color: #666;
}

/* 主要内容区域 */
.viewer-content {
  display: flex;
  flex: 1;
  overflow: hidden;
}

/* 导航面板 */
.navigation-panel {
  width: 280px;
  background: #f8f9fa;
  border-right: 1px solid #e9ecef;
  overflow-y: auto;
  padding: 16px;
}

.nav-sections, .nav-pages {
  margin-bottom: 24px;
}

.nav-sections h4, .nav-pages h4 {
  margin: 0 0 12px 0;
  font-size: 14px;
  font-weight: 600;
  color: #333;
}

.section-list, .page-list {
  display: flex;
  flex-direction: column;
  gap: 4px;
}

.section-item, .page-item {
  padding: 8px 12px;
  border-radius: 6px;
  cursor: pointer;
  transition: all 0.2s;
}

.section-item:hover, .page-item:hover {
  background: #e9ecef;
}

.section-item.active, .page-item.active {
  background: #007bff;
  color: white;
}

.level-1 { font-weight: 600; }
.level-2 { margin-left: 12px; font-weight: 500; }
.level-3 { margin-left: 24px; }
.level-4 { margin-left: 36px; font-size: 12px; }

.page-title {
  font-weight: 500;
  margin-bottom: 4px;
}

.page-summary {
  font-size: 12px;
  color: #666;
  margin-bottom: 4px;
  line-height: 1.4;
}

.page-stats {
  display: flex;
  align-items: center;
  gap: 6px;
  font-size: 11px;
  color: #999;
}

.word-count {
  margin-left: auto;
}

/* 文档显示区域 */
.document-display {
  flex: 1;
  display: flex;
  flex-direction: column;
  overflow: hidden;
}

.document-display.with-navigation {
  border-left: 1px solid #e9ecef;
}

/* 分页内容 */
.paginated-content {
  display: flex;
  flex-direction: column;
  height: 100%;
}

.page-content {
  flex: 1;
  overflow-y: auto;
  padding: 16px;
}

.page-navigation {
  padding: 12px 16px;
  border-top: 1px solid #e9ecef;
  background: #f8f9fa;
  display: flex;
  justify-content: center;
}

/* 单页内容 */
.single-content {
  flex: 1;
  overflow-y: auto;
  padding: 16px;
}

/* 内容统计 */
.content-stats {
  display: flex;
  gap: 16px;
  padding: 12px 16px;
  background: #f8f9fa;
  border-top: 1px solid #e9ecef;
  font-size: 12px;
}

.stats-item {
  display: flex;
  align-items: center;
  gap: 4px;
}

.stats-item .label {
  color: #666;
}

.stats-item .value {
  font-weight: 500;
  color: #333;
}

/* 响应式设计 */
@media (max-width: 768px) {
  .viewer-toolbar {
    flex-direction: column;
    gap: 8px;
    align-items: stretch;
  }

  .toolbar-left, .toolbar-center, .toolbar-right {
    justify-content: center;
  }

  .navigation-panel {
    width: 100%;
    max-height: 200px;
  }

  .viewer-content {
    flex-direction: column;
  }

  .content-stats {
    flex-wrap: wrap;
    gap: 8px;
  }
}
</style>

