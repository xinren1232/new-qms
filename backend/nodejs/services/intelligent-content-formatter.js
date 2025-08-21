/**
 * 智能文档内容格式化器
 * 用于整理PDF/DOCX解析出的混乱内容，提供结构化和格式化功能
 */

class IntelligentContentFormatter {
  constructor(options = {}) {
    this.options = {
      maxLineLength: 80,
      preserveOriginalStructure: true,
      enableSmartParagraphs: true,
      enableTitleDetection: true,
      enableListFormatting: true,
      enableTableDetection: true,
      minParagraphLength: 20,
      ...options
    };
    
    // 中文标点符号
    this.chinesePunctuation = /[，。！？；：""''（）【】《》]/g;
    
    // 标题模式
    this.titlePatterns = [
      /^第[一二三四五六七八九十\d]+[章节部分]/,
      /^\d+[\.\s]/,
      /^[一二三四五六七八九十]+[\.\s]/,
      /^[A-Z][A-Z\s]{2,}/,
      /^[第\d]+[章节条款]/,
      /^[\d\.]+\s/
    ];
    
    // 列表模式
    this.listPatterns = [
      /^[\s]*[-*•]\s/,
      /^[\s]*\d+[\.\)]\s/,
      /^[\s]*[a-zA-Z][\.\)]\s/,
      /^[\s]*[①②③④⑤⑥⑦⑧⑨⑩]\s/,
      /^[\s]*[一二三四五六七八九十][\.\)]\s/
    ];
  }

  /**
   * 主要格式化方法
   */
  async formatContent(rawContent, metadata = {}) {
    if (!rawContent || typeof rawContent !== 'string') {
      return {
        formattedContent: '',
        structure: { paragraphs: 0, titles: 0, lists: 0, tables: 0 },
        metadata: { ...metadata, formatted: true, formatTime: new Date().toISOString() }
      };
    }

    console.log('🎨 开始智能内容格式化...');
    const startTime = Date.now();

    try {
      // 1. 预处理 - 清理和标准化
      let content = this.preprocessContent(rawContent);
      
      // 2. 段落重组
      if (this.options.enableSmartParagraphs) {
        content = this.reorganizeParagraphs(content);
      }
      
      // 3. 标题识别和格式化
      if (this.options.enableTitleDetection) {
        content = this.formatTitles(content);
      }
      
      // 4. 列表格式化
      if (this.options.enableListFormatting) {
        content = this.formatLists(content);
      }
      
      // 5. 表格检测和格式化
      if (this.options.enableTableDetection) {
        content = this.formatTables(content);
      }
      
      // 6. 最终清理和优化
      content = this.finalCleanup(content);
      
      // 7. 分析结构
      const structure = this.analyzeStructure(content);
      
      const duration = Date.now() - startTime;
      console.log(`✅ 内容格式化完成，耗时: ${duration}ms`);
      
      return {
        formattedContent: content,
        structure,
        metadata: {
          ...metadata,
          formatted: true,
          formatTime: new Date().toISOString(),
          formatDuration: duration,
          originalLength: rawContent.length,
          formattedLength: content.length,
          compressionRatio: (content.length / rawContent.length).toFixed(2)
        }
      };
      
    } catch (error) {
      console.error('❌ 内容格式化失败:', error);
      return {
        formattedContent: rawContent,
        structure: { paragraphs: 0, titles: 0, lists: 0, tables: 0 },
        metadata: { ...metadata, formatted: false, error: error.message }
      };
    }
  }

  /**
   * 预处理内容
   */
  preprocessContent(content) {
    // 移除多余的空白字符
    content = content.replace(/\r\n/g, '\n');
    content = content.replace(/\r/g, '\n');
    
    // 移除多余的空行（保留最多2个连续空行）
    content = content.replace(/\n{4,}/g, '\n\n\n');
    
    // 移除行首行尾的空白字符
    content = content.split('\n').map(line => line.trim()).join('\n');
    
    // 移除重复的空格
    content = content.replace(/[ \t]{2,}/g, ' ');
    
    // 修复常见的编码问题
    content = content.replace(/â€™/g, "'");
    content = content.replace(/â€œ/g, '"');
    content = content.replace(/â€/g, '"');
    content = content.replace(/â€¦/g, '...');
    
    return content;
  }

  /**
   * 重组段落 - 针对质量管理文档优化
   */
  reorganizeParagraphs(content) {
    const lines = content.split('\n');
    const reorganized = [];
    let currentParagraph = '';
    let currentSection = null;

    for (let i = 0; i < lines.length; i++) {
      const line = lines[i].trim();

      // 空行处理
      if (!line) {
        if (currentParagraph.trim()) {
          reorganized.push(currentParagraph.trim());
          currentParagraph = '';
        }
        reorganized.push('');
        continue;
      }

      // 检查是否是标题
      if (this.isTitle(line)) {
        if (currentParagraph.trim()) {
          reorganized.push(currentParagraph.trim());
          currentParagraph = '';
        }

        // 根据标题类型设置不同级别和格式
        let formattedTitle = line;
        if (/^D\d+/.test(line)) {
          formattedTitle = `### ${line}`; // 8D步骤
          currentSection = 'D-step';
        } else if (/^(What|Where|When|Who|Why|How)/.test(line)) {
          formattedTitle = `#### ${line}`; // 5W2H分析
          currentSection = '5W2H';
        } else if (line.length < 30 && !line.includes('：') && !line.includes(':')) {
          formattedTitle = `## ${line}`; // 主要标题
          currentSection = 'main';
        } else {
          formattedTitle = `### ${line}`; // 次级标题
        }

        reorganized.push('');
        reorganized.push(formattedTitle);
        reorganized.push('');
        continue;
      }

      // 检查是否是关键信息行（时间、地点、人员等）
      if (this.isKeyInfoLine(line)) {
        if (currentParagraph.trim()) {
          reorganized.push(currentParagraph.trim());
          currentParagraph = '';
        }
        reorganized.push(`**${line}**`); // 加粗显示关键信息
        continue;
      }

      // 检查是否是列表项
      if (this.isListItem(line)) {
        if (currentParagraph.trim()) {
          reorganized.push(currentParagraph.trim());
          currentParagraph = '';
        }
        reorganized.push(line);
        continue;
      }

      // 检查是否应该开始新段落
      if (this.shouldStartNewParagraph(line, currentParagraph)) {
        if (currentParagraph.trim()) {
          reorganized.push(currentParagraph.trim());
        }
        currentParagraph = line;
      } else {
        // 合并到当前段落
        if (currentParagraph) {
          // 检查是否需要添加空格
          const needSpace = this.needsSpaceBetween(currentParagraph, line);
          currentParagraph += needSpace ? ' ' + line : line;
        } else {
          currentParagraph = line;
        }
      }
    }
    
    // 添加最后一个段落
    if (currentParagraph.trim()) {
      reorganized.push(currentParagraph.trim());
    }
    
    return reorganized.join('\n');
  }

  /**
   * 检查是否是标题
   */
  isTitle(line) {
    if (!line || line.length < 2) return false;

    // 检查标题模式
    for (const pattern of this.titlePatterns) {
      if (pattern.test(line)) return true;
    }

    // 检查是否全大写（英文标题）
    if (/^[A-Z\s\d]{3,}$/.test(line) && line.length < 50) return true;

    // 质量管理相关标题关键词
    const qmsKeywords = [
      'D1', 'D2', 'D3', 'D4', 'D5', 'D6', 'D7', 'D8', // 8D问题解决法
      'What', 'Where', 'When', 'Who', 'Why', 'How', 'How Much', // 5W2H分析法
      '问题描述', '根本原因', '临时措施', '纠正措施', '预防措施',
      '时间', '地点', '人员', '现象', '原因分析', '解决方案',
      '项目', '任务', '目标', '计划', '执行', '检查', '改进',
      '质量', '管理', '体系', '流程', '标准', '规范', '要求'
    ];

    // 检查是否包含QMS关键词的短行
    if (line.length < 50 && !line.endsWith('。') && !line.endsWith('.')) {
      if (qmsKeywords.some(keyword => line.includes(keyword))) return true;
    }

    // 检查是否是编号标题（如：1. 2. 3. 或 D1 D2等）
    if (/^[D]?\d+[\.\s]/.test(line.trim()) && line.length < 100) return true;

    // 检查是否是问题分析格式
    if (/^(What|Where|When|Who|Why|How)\s*[（(]/.test(line)) return true;

    return false;
  }

  /**
   * 检查是否是关键信息行
   */
  isKeyInfoLine(line) {
    const keyPatterns = [
      /^\d{4}年\d{1,2}月\d{1,2}日/, // 日期格式：2024年2月27日
      /^\d{4}[\/\-]\d{1,2}[\/\-]\d{1,2}/, // 日期格式：2024/2/27
      /^时间[:：]/, // 时间标签
      /^地点[:：]/, // 地点标签
      /^人员[:：]/, // 人员标签
      /^责任人[:：]/, // 责任人标签
      /^涉及人员[:：]/, // 涉及人员
      /^参与人员[:：]/, // 参与人员
      /^问题[:：]/, // 问题描述
      /^原因[:：]/, // 原因分析
      /^措施[:：]/, // 措施说明
      /^目标[:：]/, // 目标设定
      /^完成时间[:：]/, // 完成时间
      /^状态[:：]/, // 状态信息
      /^进度[:：]/, // 进度信息
      /^备注[:：]/, // 备注信息
    ];

    return keyPatterns.some(pattern => pattern.test(line));
  }

  /**
   * 检查是否是列表项
   */
  isListItem(line) {
    if (!line) return false;
    
    for (const pattern of this.listPatterns) {
      if (pattern.test(line)) return true;
    }
    
    return false;
  }

  /**
   * 检查是否应该开始新段落
   */
  shouldStartNewParagraph(line, currentParagraph) {
    if (!currentParagraph) return true;
    
    // 如果当前段落已经很长
    if (currentParagraph.length > 200) return true;
    
    // 如果当前段落以句号结尾且新行以大写字母开始
    if (currentParagraph.endsWith('。') || currentParagraph.endsWith('.')) {
      if (/^[A-Z\u4e00-\u9fa5]/.test(line)) return true;
    }
    
    // 如果新行看起来像是新的主题
    const topicIndicators = ['另外', '此外', '然而', '但是', '因此', '所以', '总之', '综上'];
    if (topicIndicators.some(indicator => line.startsWith(indicator))) return true;
    
    return false;
  }

  /**
   * 检查两个文本片段之间是否需要空格
   */
  needsSpaceBetween(text1, text2) {
    if (!text1 || !text2) return false;
    
    const lastChar = text1.slice(-1);
    const firstChar = text2.slice(0, 1);
    
    // 中文字符之间不需要空格
    if (/[\u4e00-\u9fa5]/.test(lastChar) && /[\u4e00-\u9fa5]/.test(firstChar)) {
      return false;
    }
    
    // 标点符号后不需要空格
    if (/[，。！？；：""''（）【】《》]/.test(lastChar)) {
      return false;
    }
    
    // 英文单词之间需要空格
    if (/[a-zA-Z]/.test(lastChar) && /[a-zA-Z]/.test(firstChar)) {
      return true;
    }
    
    return false;
  }

  /**
   * 格式化标题
   */
  formatTitles(content) {
    const lines = content.split('\n');
    const formatted = [];

    for (let i = 0; i < lines.length; i++) {
      const line = lines[i];

      if (this.isTitle(line)) {
        // 确保标题前后有空行
        if (formatted.length > 0 && formatted[formatted.length - 1] !== '') {
          formatted.push('');
        }

        // 格式化标题
        let formattedTitle = line.trim();

        // 添加标题标记
        if (this.titlePatterns[0].test(formattedTitle)) {
          // 章节标题
          formattedTitle = `## ${formattedTitle}`;
        } else if (/^\d+[\.\s]/.test(formattedTitle)) {
          // 数字标题
          formattedTitle = `### ${formattedTitle}`;
        } else {
          // 普通标题
          formattedTitle = `#### ${formattedTitle}`;
        }

        formatted.push(formattedTitle);
        formatted.push('');
      } else {
        formatted.push(line);
      }
    }

    return formatted.join('\n');
  }

  /**
   * 格式化列表
   */
  formatLists(content) {
    const lines = content.split('\n');
    const formatted = [];
    let inList = false;

    for (let i = 0; i < lines.length; i++) {
      const line = lines[i];

      if (this.isListItem(line)) {
        if (!inList) {
          // 开始新列表，确保前面有空行
          if (formatted.length > 0 && formatted[formatted.length - 1] !== '') {
            formatted.push('');
          }
          inList = true;
        }

        // 格式化列表项
        let formattedItem = line.trim();

        // 统一列表符号
        if (/^[\s]*[-*•]\s/.test(formattedItem)) {
          formattedItem = formattedItem.replace(/^[\s]*[-*•]\s/, '• ');
        } else if (/^[\s]*\d+[\.\)]\s/.test(formattedItem)) {
          const match = formattedItem.match(/^[\s]*(\d+)[\.\)]\s(.*)$/);
          if (match) {
            formattedItem = `${match[1]}. ${match[2]}`;
          }
        }

        formatted.push(formattedItem);
      } else {
        if (inList && line.trim() === '') {
          // 列表中的空行
          formatted.push(line);
        } else if (inList && line.trim() !== '') {
          // 列表结束
          inList = false;
          formatted.push('');
          formatted.push(line);
        } else {
          formatted.push(line);
        }
      }
    }

    return formatted.join('\n');
  }

  /**
   * 格式化表格
   */
  formatTables(content) {
    const lines = content.split('\n');
    const formatted = [];

    for (let i = 0; i < lines.length; i++) {
      const line = lines[i];

      // 检测表格行（包含多个制表符或竖线）
      if (this.isTableRow(line)) {
        const tableData = this.parseTableRow(line);
        if (tableData.length > 1) {
          // 格式化为Markdown表格
          const formattedRow = '| ' + tableData.join(' | ') + ' |';
          formatted.push(formattedRow);

          // 如果是第一行，添加分隔符
          if (i === 0 || !this.isTableRow(lines[i - 1])) {
            const separator = '|' + tableData.map(() => ' --- ').join('|') + '|';
            formatted.push(separator);
          }
        } else {
          formatted.push(line);
        }
      } else {
        formatted.push(line);
      }
    }

    return formatted.join('\n');
  }

  /**
   * 检查是否是表格行
   */
  isTableRow(line) {
    if (!line || line.trim().length < 3) return false;

    // 检查制表符分隔
    if (line.includes('\t') && line.split('\t').length > 2) return true;

    // 检查竖线分隔
    if (line.includes('|') && line.split('|').length > 2) return true;

    // 检查多个空格分隔（可能是表格）
    const parts = line.trim().split(/\s{2,}/);
    if (parts.length > 2 && parts.every(part => part.trim().length > 0)) return true;

    return false;
  }

  /**
   * 解析表格行
   */
  parseTableRow(line) {
    // 尝试不同的分隔符
    let parts = [];

    if (line.includes('\t')) {
      parts = line.split('\t');
    } else if (line.includes('|')) {
      parts = line.split('|');
    } else {
      parts = line.trim().split(/\s{2,}/);
    }

    return parts.map(part => part.trim()).filter(part => part.length > 0);
  }

  /**
   * 最终清理
   */
  finalCleanup(content) {
    // 移除多余的空行
    content = content.replace(/\n{3,}/g, '\n\n');

    // 确保文档以换行符结尾
    if (!content.endsWith('\n')) {
      content += '\n';
    }

    // 移除行尾空格
    content = content.split('\n').map(line => line.trimEnd()).join('\n');

    return content;
  }

  /**
   * 分析内容结构
   */
  analyzeStructure(content) {
    const lines = content.split('\n');

    let paragraphs = 0;
    let titles = 0;
    let lists = 0;
    let tables = 0;

    let currentParagraph = '';

    for (const line of lines) {
      const trimmedLine = line.trim();

      if (!trimmedLine) {
        if (currentParagraph) {
          paragraphs++;
          currentParagraph = '';
        }
        continue;
      }

      if (trimmedLine.startsWith('#')) {
        titles++;
        if (currentParagraph) {
          paragraphs++;
          currentParagraph = '';
        }
      } else if (this.isListItem(trimmedLine)) {
        lists++;
        if (currentParagraph) {
          paragraphs++;
          currentParagraph = '';
        }
      } else if (trimmedLine.startsWith('|')) {
        tables++;
        if (currentParagraph) {
          paragraphs++;
          currentParagraph = '';
        }
      } else {
        currentParagraph += trimmedLine + ' ';
      }
    }

    if (currentParagraph) {
      paragraphs++;
    }

    return {
      paragraphs,
      titles,
      lists,
      tables,
      totalLines: lines.length,
      nonEmptyLines: lines.filter(line => line.trim()).length
    };
  }

  /**
   * 创建内容摘要
   */
  createSummary(content, maxLength = 200) {
    const lines = content.split('\n').filter(line => line.trim());

    // 提取前几个非空行作为摘要
    let summary = '';
    for (const line of lines) {
      const trimmedLine = line.trim();
      if (trimmedLine && !trimmedLine.startsWith('#') && !this.isListItem(trimmedLine)) {
        summary += trimmedLine + ' ';
        if (summary.length > maxLength) {
          summary = summary.substring(0, maxLength) + '...';
          break;
        }
      }
    }

    return summary.trim();
  }

  /**
   * 提取关键词
   */
  extractKeywords(content, maxKeywords = 10) {
    // 简单的关键词提取（基于词频）
    const words = content
      .toLowerCase()
      .replace(/[^\u4e00-\u9fa5a-zA-Z\s]/g, ' ')
      .split(/\s+/)
      .filter(word => word.length > 1);

    const wordCount = {};
    words.forEach(word => {
      wordCount[word] = (wordCount[word] || 0) + 1;
    });

    // 排除常见停用词
    const stopWords = ['的', '是', '在', '有', '和', '与', '或', '但', '而', '了', '也', '就', '都', 'the', 'is', 'in', 'and', 'or', 'but', 'to', 'of', 'a', 'an'];

    return Object.entries(wordCount)
      .filter(([word]) => !stopWords.includes(word))
      .sort(([,a], [,b]) => b - a)
      .slice(0, maxKeywords)
      .map(([word]) => word);
  }
}

module.exports = IntelligentContentFormatter;
