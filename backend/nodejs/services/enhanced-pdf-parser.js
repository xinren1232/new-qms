/**
 * 增强PDF解析器
 * 提供结构化PDF内容提取，包括标题层级、段落分割、表格识别等功能
 */

const pdfParse = require('pdf-parse');
const IntelligentContentFormatter = require('./intelligent-content-formatter');

class EnhancedPDFParser {
  constructor(options = {}) {
    this.options = {
      preserveLayout: true,
      extractImages: false,
      detectTables: true,
      detectHeaders: true,
      minParagraphLength: 10,
      maxLineLength: 100,
      ...options
    };
    
    this.formatter = new IntelligentContentFormatter({
      enableSmartParagraphs: true,
      enableTitleDetection: true,
      enableListFormatting: true,
      enableTableDetection: this.options.detectTables
    });
  }

  /**
   * 解析PDF文档
   */
  async parsePDF(buffer, filename = 'document.pdf') {
    console.log('📄 开始增强PDF解析...');
    const startTime = Date.now();

    try {
      // 1. 基础PDF解析
      const pdfData = await pdfParse(buffer, {
        max: 0, // 解析所有页面
        version: 'v1.10.100'
      });

      console.log(`📄 PDF基础解析完成: ${pdfData.numpages}页, ${pdfData.text.length}字符`);

      // 2. 结构化处理
      const structuredData = await this.processStructuredContent(pdfData);

      // 3. 智能格式化
      const formattedResult = await this.formatter.formatContent(
        structuredData.content,
        {
          filename,
          pages: pdfData.numpages,
          originalLength: pdfData.text.length,
          parser: 'enhanced-pdf'
        }
      );

      // 4. 页面级别处理
      const pageData = await this.processPages(pdfData, structuredData);

      const duration = Date.now() - startTime;
      console.log(`✅ 增强PDF解析完成，耗时: ${duration}ms`);

      return {
        success: true,
        content: formattedResult.formattedContent,
        rawContent: pdfData.text,
        pages: pageData,
        structure: formattedResult.structure,
        metadata: {
          ...formattedResult.metadata,
          filename,
          fileSize: buffer.length,
          pageCount: pdfData.numpages,
          info: pdfData.info || {},
          parseTime: new Date().toISOString(),
          parseDuration: duration,
          version: pdfData.version
        },
        summary: this.formatter.createSummary(formattedResult.formattedContent),
        keywords: this.formatter.extractKeywords(formattedResult.formattedContent)
      };

    } catch (error) {
      console.error('❌ 增强PDF解析失败:', error);
      
      // 降级到基础解析
      try {
        const fallbackData = await pdfParse(buffer);
        return {
          success: false,
          content: fallbackData.text,
          rawContent: fallbackData.text,
          pages: [],
          structure: { paragraphs: 0, titles: 0, lists: 0, tables: 0 },
          metadata: {
            filename,
            fileSize: buffer.length,
            pageCount: fallbackData.numpages || 0,
            parseTime: new Date().toISOString(),
            parser: 'fallback-pdf',
            error: error.message
          },
          summary: '',
          keywords: []
        };
      } catch (fallbackError) {
        throw new Error(`PDF解析完全失败: ${fallbackError.message}`);
      }
    }
  }

  /**
   * 处理结构化内容
   */
  async processStructuredContent(pdfData) {
    const content = pdfData.text;
    const lines = content.split('\n');
    
    // 分析文档结构
    const structure = {
      headers: [],
      paragraphs: [],
      tables: [],
      lists: [],
      metadata: {}
    };

    let processedContent = '';
    let currentSection = '';
    let lineNumber = 0;

    for (const line of lines) {
      lineNumber++;
      const trimmedLine = line.trim();
      
      if (!trimmedLine) {
        processedContent += '\n';
        continue;
      }

      // 检测标题
      if (this.isHeader(trimmedLine)) {
        const headerLevel = this.getHeaderLevel(trimmedLine);
        structure.headers.push({
          text: trimmedLine,
          level: headerLevel,
          lineNumber,
          section: currentSection
        });
        
        processedContent += `\n${'#'.repeat(headerLevel)} ${trimmedLine}\n\n`;
        currentSection = trimmedLine;
        continue;
      }

      // 检测表格行
      if (this.isTableRow(trimmedLine)) {
        const tableData = this.parseTableRow(trimmedLine);
        structure.tables.push({
          data: tableData,
          lineNumber,
          section: currentSection
        });
        
        processedContent += `| ${tableData.join(' | ')} |\n`;
        continue;
      }

      // 检测列表项
      if (this.isListItem(trimmedLine)) {
        structure.lists.push({
          text: trimmedLine,
          lineNumber,
          section: currentSection
        });
        
        processedContent += `${trimmedLine}\n`;
        continue;
      }

      // 普通段落
      if (trimmedLine.length > this.options.minParagraphLength) {
        structure.paragraphs.push({
          text: trimmedLine,
          lineNumber,
          section: currentSection
        });
      }
      
      processedContent += `${trimmedLine}\n`;
    }

    return {
      content: processedContent,
      structure
    };
  }

  /**
   * 处理页面数据
   */
  async processPages(pdfData, structuredData) {
    const pages = [];
    const totalPages = pdfData.numpages || 1;
    const contentPerPage = Math.ceil(pdfData.text.length / totalPages);

    // 尝试按页面分割内容
    const lines = pdfData.text.split('\n');
    const linesPerPage = Math.ceil(lines.length / totalPages);

    for (let i = 0; i < totalPages; i++) {
      const startLine = i * linesPerPage;
      const endLine = Math.min((i + 1) * linesPerPage, lines.length);
      const pageLines = lines.slice(startLine, endLine);
      const pageContent = pageLines.join('\n');

      // 格式化页面内容
      const formattedPage = await this.formatter.formatContent(pageContent, {
        pageNumber: i + 1,
        totalPages
      });

      pages.push({
        pageNumber: i + 1,
        content: formattedPage.formattedContent,
        rawContent: pageContent,
        structure: formattedPage.structure,
        summary: this.formatter.createSummary(formattedPage.formattedContent, 100),
        lineCount: pageLines.length,
        charCount: pageContent.length
      });
    }

    return pages;
  }

  /**
   * 检测是否为标题
   */
  isHeader(line) {
    if (!line || line.length < 2) return false;

    // 检查常见标题模式
    const headerPatterns = [
      /^第[一二三四五六七八九十\d]+[章节部分]/,
      /^\d+[\.\s]/,
      /^[一二三四五六七八九十]+[\.\s]/,
      /^[A-Z][A-Z\s\d]{2,}$/,
      /^[第\d]+[章节条款]/
    ];

    for (const pattern of headerPatterns) {
      if (pattern.test(line)) return true;
    }

    // 检查短行且全大写
    if (line.length < 50 && /^[A-Z\s\d]+$/.test(line)) return true;

    // 检查是否以常见标题词开头
    const titleWords = ['概述', '介绍', '背景', '目标', '方法', '结果', '结论', '总结', '建议'];
    if (titleWords.some(word => line.startsWith(word)) && line.length < 30) return true;

    return false;
  }

  /**
   * 获取标题级别
   */
  getHeaderLevel(line) {
    if (/^第[一二三四五六七八九十\d]+[章节部分]/.test(line)) return 1;
    if (/^\d+[\.\s]/.test(line)) return 2;
    if (/^[一二三四五六七八九十]+[\.\s]/.test(line)) return 3;
    if (/^[A-Z][A-Z\s\d]{2,}$/.test(line)) return 2;
    return 4;
  }

  /**
   * 检测表格行
   */
  isTableRow(line) {
    if (!line || line.length < 5) return false;

    // 检查制表符分隔
    if (line.includes('\t') && line.split('\t').length > 2) return true;

    // 检查多个空格分隔
    const parts = line.split(/\s{3,}/);
    if (parts.length > 2 && parts.every(part => part.trim().length > 0)) return true;

    // 检查竖线分隔
    if (line.includes('|') && line.split('|').length > 2) return true;

    return false;
  }

  /**
   * 解析表格行
   */
  parseTableRow(line) {
    if (line.includes('\t')) {
      return line.split('\t').map(cell => cell.trim()).filter(cell => cell);
    }
    
    if (line.includes('|')) {
      return line.split('|').map(cell => cell.trim()).filter(cell => cell);
    }
    
    return line.split(/\s{3,}/).map(cell => cell.trim()).filter(cell => cell);
  }

  /**
   * 检测列表项
   */
  isListItem(line) {
    if (!line) return false;

    const listPatterns = [
      /^[\s]*[-*•]\s/,
      /^[\s]*\d+[\.\)]\s/,
      /^[\s]*[a-zA-Z][\.\)]\s/,
      /^[\s]*[①②③④⑤⑥⑦⑧⑨⑩]\s/,
      /^[\s]*[一二三四五六七八九十][\.\)]\s/
    ];

    return listPatterns.some(pattern => pattern.test(line));
  }
}

module.exports = EnhancedPDFParser;
