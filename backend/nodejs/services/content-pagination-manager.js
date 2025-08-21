/**
 * 内容分页和截断管理器
 * 提供智能内容分页、懒加载、搜索定位等功能
 */

class ContentPaginationManager {
  constructor(options = {}) {
    this.options = {
      maxPageSize: 5000,        // 每页最大字符数
      maxPreviewSize: 1000,     // 预览最大字符数
      overlapSize: 200,         // 页面重叠字符数
      preserveStructure: true,  // 保持结构完整性
      enableSearch: true,       // 启用搜索功能
      enableLazyLoad: true,     // 启用懒加载
      chunkByParagraph: true,   // 按段落分块
      ...options
    };
    
    this.searchIndex = new Map(); // 搜索索引
  }

  /**
   * 分页处理主方法
   */
  async paginateContent(content, metadata = {}) {
    console.log('📄 开始内容分页处理...');
    const startTime = Date.now();

    try {
      // 1. 内容预处理
      const processedContent = this.preprocessContent(content);
      
      // 2. 智能分页
      const pages = await this.createPages(processedContent);
      
      // 3. 创建预览
      const preview = this.createPreview(processedContent);
      
      // 4. 构建搜索索引
      if (this.options.enableSearch) {
        this.buildSearchIndex(pages);
      }
      
      // 5. 生成导航信息
      const navigation = this.generateNavigation(pages);
      
      const duration = Date.now() - startTime;
      console.log(`✅ 内容分页完成，共${pages.length}页，耗时: ${duration}ms`);

      return {
        success: true,
        totalPages: pages.length,
        pages,
        preview,
        navigation,
        searchIndex: this.options.enableSearch ? Array.from(this.searchIndex.entries()) : [],
        metadata: {
          ...metadata,
          paginated: true,
          paginationTime: new Date().toISOString(),
          paginationDuration: duration,
          totalCharacters: content.length,
          averagePageSize: Math.round(content.length / pages.length),
          options: this.options
        },
        statistics: this.generateStatistics(pages, content)
      };

    } catch (error) {
      console.error('❌ 内容分页失败:', error);
      
      // 降级处理
      return {
        success: false,
        totalPages: 1,
        pages: [{
          pageNumber: 1,
          content: content.substring(0, this.options.maxPageSize),
          startIndex: 0,
          endIndex: Math.min(content.length, this.options.maxPageSize),
          isTruncated: content.length > this.options.maxPageSize
        }],
        preview: content.substring(0, this.options.maxPreviewSize),
        navigation: [],
        searchIndex: [],
        metadata: { ...metadata, paginated: false, error: error.message },
        statistics: { totalCharacters: content.length, pages: 1 }
      };
    }
  }

  /**
   * 内容预处理
   */
  preprocessContent(content) {
    if (!content || typeof content !== 'string') {
      return '';
    }

    // 标准化换行符
    content = content.replace(/\r\n/g, '\n').replace(/\r/g, '\n');
    
    // 移除过多的空行
    content = content.replace(/\n{4,}/g, '\n\n\n');
    
    return content;
  }

  /**
   * 创建页面
   */
  async createPages(content) {
    const pages = [];
    
    if (content.length <= this.options.maxPageSize) {
      // 内容较短，单页显示
      pages.push({
        pageNumber: 1,
        content,
        startIndex: 0,
        endIndex: content.length,
        isTruncated: false,
        structure: this.analyzePageStructure(content),
        summary: this.createPageSummary(content)
      });
      
      return pages;
    }

    // 智能分页
    if (this.options.chunkByParagraph) {
      return this.createParagraphBasedPages(content);
    } else {
      return this.createFixedSizePages(content);
    }
  }

  /**
   * 基于段落的分页
   */
  createParagraphBasedPages(content) {
    const pages = [];
    const paragraphs = content.split(/\n\s*\n/);
    
    let currentPage = '';
    let currentStartIndex = 0;
    let pageNumber = 1;
    let processedLength = 0;

    for (let i = 0; i < paragraphs.length; i++) {
      const paragraph = paragraphs[i];
      const paragraphWithNewlines = i < paragraphs.length - 1 ? paragraph + '\n\n' : paragraph;
      
      // 检查添加当前段落是否会超出页面大小
      if (currentPage.length + paragraphWithNewlines.length > this.options.maxPageSize && currentPage.length > 0) {
        // 创建当前页面
        pages.push({
          pageNumber: pageNumber++,
          content: currentPage.trim(),
          startIndex: currentStartIndex,
          endIndex: currentStartIndex + currentPage.length,
          isTruncated: false,
          structure: this.analyzePageStructure(currentPage),
          summary: this.createPageSummary(currentPage),
          paragraphCount: currentPage.split(/\n\s*\n/).length
        });
        
        // 开始新页面
        currentStartIndex = processedLength;
        currentPage = '';
        
        // 添加重叠内容
        if (this.options.overlapSize > 0 && pages.length > 0) {
          const overlap = this.getOverlapContent(pages[pages.length - 1].content);
          currentPage = overlap;
          currentStartIndex -= overlap.length;
        }
      }
      
      currentPage += paragraphWithNewlines;
      processedLength += paragraphWithNewlines.length;
    }

    // 添加最后一页
    if (currentPage.trim()) {
      pages.push({
        pageNumber: pageNumber,
        content: currentPage.trim(),
        startIndex: currentStartIndex,
        endIndex: processedLength,
        isTruncated: false,
        structure: this.analyzePageStructure(currentPage),
        summary: this.createPageSummary(currentPage),
        paragraphCount: currentPage.split(/\n\s*\n/).length
      });
    }

    return pages;
  }

  /**
   * 固定大小分页
   */
  createFixedSizePages(content) {
    const pages = [];
    let pageNumber = 1;
    let startIndex = 0;

    while (startIndex < content.length) {
      let endIndex = Math.min(startIndex + this.options.maxPageSize, content.length);
      
      // 尝试在合适的位置断开（避免在单词中间断开）
      if (endIndex < content.length) {
        const breakPoint = this.findGoodBreakPoint(content, startIndex, endIndex);
        if (breakPoint > startIndex) {
          endIndex = breakPoint;
        }
      }
      
      const pageContent = content.substring(startIndex, endIndex);
      
      pages.push({
        pageNumber: pageNumber++,
        content: pageContent,
        startIndex,
        endIndex,
        isTruncated: endIndex < content.length,
        structure: this.analyzePageStructure(pageContent),
        summary: this.createPageSummary(pageContent)
      });
      
      // 计算下一页的起始位置（考虑重叠）
      startIndex = endIndex - this.options.overlapSize;
      if (startIndex < 0) startIndex = endIndex;
    }

    return pages;
  }

  /**
   * 寻找合适的断点
   */
  findGoodBreakPoint(content, startIndex, endIndex) {
    const searchRange = Math.min(200, Math.floor((endIndex - startIndex) * 0.1));
    const searchStart = endIndex - searchRange;
    
    // 优先在段落边界断开
    for (let i = endIndex - 1; i >= searchStart; i--) {
      if (content[i] === '\n' && content[i + 1] === '\n') {
        return i + 2;
      }
    }
    
    // 其次在句子边界断开
    for (let i = endIndex - 1; i >= searchStart; i--) {
      if (/[.!?。！？]/.test(content[i]) && /\s/.test(content[i + 1])) {
        return i + 2;
      }
    }
    
    // 最后在单词边界断开
    for (let i = endIndex - 1; i >= searchStart; i--) {
      if (/\s/.test(content[i])) {
        return i + 1;
      }
    }
    
    return endIndex;
  }

  /**
   * 获取重叠内容
   */
  getOverlapContent(pageContent) {
    if (pageContent.length <= this.options.overlapSize) {
      return pageContent;
    }
    
    const overlapStart = pageContent.length - this.options.overlapSize;
    let overlap = pageContent.substring(overlapStart);
    
    // 尝试从完整的句子开始
    const sentenceStart = overlap.search(/[.!?。！？]\s+/);
    if (sentenceStart > 0) {
      overlap = overlap.substring(sentenceStart + 2);
    }
    
    return overlap;
  }

  /**
   * 分析页面结构
   */
  analyzePageStructure(content) {
    const lines = content.split('\n');
    const structure = {
      lineCount: lines.length,
      paragraphCount: content.split(/\n\s*\n/).length,
      hasHeaders: false,
      hasList: false,
      hasTable: false,
      wordCount: content.split(/\s+/).filter(word => word.length > 0).length
    };
    
    // 检测标题
    structure.hasHeaders = /^#+\s/.test(content) || /^第[一二三四五六七八九十\d]+[章节]/.test(content);
    
    // 检测列表
    structure.hasList = /^[\s]*[-*•]\s/m.test(content) || /^[\s]*\d+[\.\)]\s/m.test(content);
    
    // 检测表格
    structure.hasTable = /\|.*\|/.test(content) || /\t.*\t/.test(content);
    
    return structure;
  }

  /**
   * 创建页面摘要
   */
  createPageSummary(content, maxLength = 150) {
    // 提取前几句作为摘要
    const sentences = content.split(/[.!?。！？]/).filter(s => s.trim().length > 10);
    
    let summary = '';
    for (const sentence of sentences) {
      const trimmed = sentence.trim();
      if (summary.length + trimmed.length <= maxLength) {
        summary += trimmed + '。';
      } else {
        break;
      }
    }
    
    if (!summary && content.length > 0) {
      summary = content.substring(0, maxLength) + '...';
    }
    
    return summary;
  }

  /**
   * 创建预览
   */
  createPreview(content) {
    if (content.length <= this.options.maxPreviewSize) {
      return {
        content,
        isTruncated: false,
        originalLength: content.length
      };
    }
    
    // 智能截断，尝试在句子边界结束
    let truncated = content.substring(0, this.options.maxPreviewSize);
    const lastSentenceEnd = truncated.lastIndexOf('。');
    const lastPeriodEnd = truncated.lastIndexOf('.');
    
    const cutPoint = Math.max(lastSentenceEnd, lastPeriodEnd);
    if (cutPoint > this.options.maxPreviewSize * 0.8) {
      truncated = truncated.substring(0, cutPoint + 1);
    }
    
    return {
      content: truncated + '...',
      isTruncated: true,
      originalLength: content.length,
      truncatedLength: truncated.length
    };
  }
}

  /**
   * 构建搜索索引
   */
  buildSearchIndex(pages) {
    this.searchIndex.clear();

    pages.forEach((page, pageIndex) => {
      const words = page.content
        .toLowerCase()
        .replace(/[^\u4e00-\u9fa5a-zA-Z0-9\s]/g, ' ')
        .split(/\s+/)
        .filter(word => word.length > 1);

      words.forEach((word, wordIndex) => {
        if (!this.searchIndex.has(word)) {
          this.searchIndex.set(word, []);
        }

        this.searchIndex.get(word).push({
          pageNumber: page.pageNumber,
          pageIndex,
          wordIndex,
          context: this.getWordContext(page.content, word, wordIndex)
        });
      });
    });
  }

  /**
   * 获取单词上下文
   */
  getWordContext(content, word, wordIndex, contextLength = 50) {
    const words = content.split(/\s+/);
    const start = Math.max(0, wordIndex - contextLength / 2);
    const end = Math.min(words.length, wordIndex + contextLength / 2);

    return words.slice(start, end).join(' ');
  }

  /**
   * 搜索内容
   */
  searchContent(query, pages) {
    if (!this.options.enableSearch || !query || query.trim().length < 2) {
      return { results: [], totalMatches: 0 };
    }

    const searchTerms = query.toLowerCase().split(/\s+/).filter(term => term.length > 1);
    const results = [];
    let totalMatches = 0;

    pages.forEach((page, pageIndex) => {
      const pageContent = page.content.toLowerCase();
      const pageResults = [];

      searchTerms.forEach(term => {
        let index = 0;
        while ((index = pageContent.indexOf(term, index)) !== -1) {
          const context = this.getSearchContext(page.content, index, term.length);
          pageResults.push({
            term,
            position: index,
            context,
            highlightedContext: this.highlightTerm(context, term)
          });
          totalMatches++;
          index += term.length;
        }
      });

      if (pageResults.length > 0) {
        results.push({
          pageNumber: page.pageNumber,
          pageIndex,
          matches: pageResults,
          matchCount: pageResults.length,
          summary: page.summary
        });
      }
    });

    return {
      results: results.sort((a, b) => b.matchCount - a.matchCount),
      totalMatches,
      searchTerms
    };
  }

  /**
   * 获取搜索上下文
   */
  getSearchContext(content, position, termLength, contextLength = 100) {
    const start = Math.max(0, position - contextLength / 2);
    const end = Math.min(content.length, position + termLength + contextLength / 2);

    let context = content.substring(start, end);

    // 添加省略号
    if (start > 0) context = '...' + context;
    if (end < content.length) context = context + '...';

    return context;
  }

  /**
   * 高亮搜索词
   */
  highlightTerm(context, term) {
    const regex = new RegExp(`(${term})`, 'gi');
    return context.replace(regex, '<mark>$1</mark>');
  }

  /**
   * 生成导航信息
   */
  generateNavigation(pages) {
    const navigation = {
      totalPages: pages.length,
      pageList: [],
      sections: [],
      quickJumps: []
    };

    // 生成页面列表
    pages.forEach(page => {
      navigation.pageList.push({
        pageNumber: page.pageNumber,
        title: this.generatePageTitle(page),
        summary: page.summary,
        hasHeaders: page.structure?.hasHeaders || false,
        hasList: page.structure?.hasList || false,
        hasTable: page.structure?.hasTable || false,
        wordCount: page.structure?.wordCount || 0
      });
    });

    // 检测章节
    pages.forEach(page => {
      const headers = this.extractHeaders(page.content);
      headers.forEach(header => {
        navigation.sections.push({
          title: header.text,
          level: header.level,
          pageNumber: page.pageNumber,
          position: header.position
        });
      });
    });

    // 生成快速跳转点
    const totalLength = pages.reduce((sum, page) => sum + page.content.length, 0);
    const jumpPoints = [0, 0.25, 0.5, 0.75, 1.0];

    jumpPoints.forEach(ratio => {
      const targetLength = totalLength * ratio;
      let currentLength = 0;

      for (let i = 0; i < pages.length; i++) {
        currentLength += pages[i].content.length;
        if (currentLength >= targetLength) {
          navigation.quickJumps.push({
            label: `${Math.round(ratio * 100)}%`,
            pageNumber: pages[i].pageNumber,
            ratio
          });
          break;
        }
      }
    });

    return navigation;
  }

  /**
   * 生成页面标题
   */
  generatePageTitle(page) {
    // 尝试从页面内容中提取标题
    const lines = page.content.split('\n').filter(line => line.trim());

    if (lines.length > 0) {
      const firstLine = lines[0].trim();

      // 检查是否是标题格式
      if (firstLine.length < 50 && (
        /^#+\s/.test(firstLine) ||
        /^第[一二三四五六七八九十\d]+[章节]/.test(firstLine) ||
        /^\d+[\.\s]/.test(firstLine)
      )) {
        return firstLine.replace(/^#+\s/, '');
      }

      // 使用前几个词作为标题
      const words = firstLine.split(/\s+/).slice(0, 8).join(' ');
      return words.length > 30 ? words.substring(0, 30) + '...' : words;
    }

    return `第 ${page.pageNumber} 页`;
  }

  /**
   * 提取标题
   */
  extractHeaders(content) {
    const headers = [];
    const lines = content.split('\n');

    lines.forEach((line, index) => {
      const trimmed = line.trim();

      if (trimmed.length < 2) return;

      // Markdown标题
      const markdownMatch = trimmed.match(/^(#+)\s(.+)$/);
      if (markdownMatch) {
        headers.push({
          text: markdownMatch[2],
          level: markdownMatch[1].length,
          position: index,
          type: 'markdown'
        });
        return;
      }

      // 中文章节标题
      if (/^第[一二三四五六七八九十\d]+[章节部分]/.test(trimmed)) {
        headers.push({
          text: trimmed,
          level: 1,
          position: index,
          type: 'chinese'
        });
        return;
      }

      // 数字标题
      if (/^\d+[\.\s]/.test(trimmed) && trimmed.length < 50) {
        headers.push({
          text: trimmed,
          level: 2,
          position: index,
          type: 'numbered'
        });
      }
    });

    return headers;
  }

  /**
   * 生成统计信息
   */
  generateStatistics(pages, originalContent) {
    const stats = {
      totalCharacters: originalContent.length,
      totalPages: pages.length,
      averagePageSize: Math.round(originalContent.length / pages.length),
      largestPageSize: Math.max(...pages.map(p => p.content.length)),
      smallestPageSize: Math.min(...pages.map(p => p.content.length)),
      totalParagraphs: pages.reduce((sum, p) => sum + (p.structure?.paragraphCount || 0), 0),
      totalWords: pages.reduce((sum, p) => sum + (p.structure?.wordCount || 0), 0),
      pagesWithHeaders: pages.filter(p => p.structure?.hasHeaders).length,
      pagesWithLists: pages.filter(p => p.structure?.hasList).length,
      pagesWithTables: pages.filter(p => p.structure?.hasTable).length
    };

    return stats;
  }

  /**
   * 获取页面内容（懒加载支持）
   */
  async getPageContent(pageNumber, pages) {
    if (pageNumber < 1 || pageNumber > pages.length) {
      throw new Error(`页面号 ${pageNumber} 超出范围 (1-${pages.length})`);
    }

    const page = pages[pageNumber - 1];

    // 模拟懒加载延迟（实际应用中可能从数据库或文件系统加载）
    if (this.options.enableLazyLoad) {
      await new Promise(resolve => setTimeout(resolve, 10));
    }

    return {
      ...page,
      loadTime: new Date().toISOString(),
      cached: false
    };
  }

  /**
   * 获取页面范围内容
   */
  getPageRange(startPage, endPage, pages) {
    if (startPage < 1 || endPage > pages.length || startPage > endPage) {
      throw new Error('页面范围无效');
    }

    const rangePages = pages.slice(startPage - 1, endPage);
    const combinedContent = rangePages.map(p => p.content).join('\n\n---\n\n');

    return {
      content: combinedContent,
      pages: rangePages,
      startPage,
      endPage,
      totalCharacters: combinedContent.length
    };
  }
}

module.exports = ContentPaginationManager;
