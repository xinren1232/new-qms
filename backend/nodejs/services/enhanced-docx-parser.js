/**
 * 增强DOCX解析器
 * 提供完善的DOCX文档解析，包括样式信息、表格处理、图片位置标记等功能
 */

const JSZip = require('jszip');
const xml2js = require('xml2js');
const IntelligentContentFormatter = require('./intelligent-content-formatter');

class EnhancedDOCXParser {
  constructor(options = {}) {
    this.options = {
      extractStyles: true,
      extractTables: true,
      extractImages: true,
      preserveFormatting: true,
      includeComments: false,
      includeFootnotes: true,
      ...options
    };
    
    this.formatter = new IntelligentContentFormatter({
      enableSmartParagraphs: true,
      enableTitleDetection: true,
      enableListFormatting: true,
      enableTableDetection: true
    });
    
    this.xmlParser = new xml2js.Parser({
      explicitArray: false,
      ignoreAttrs: false,
      mergeAttrs: true
    });
  }

  /**
   * 解析DOCX文档
   */
  async parseDOCX(buffer, filename = 'document.docx') {
    console.log('📄 开始增强DOCX解析...');
    const startTime = Date.now();

    try {
      // 1. 解压DOCX文件
      const zip = await JSZip.loadAsync(buffer);
      
      // 2. 提取文档结构
      const documentStructure = await this.extractDocumentStructure(zip);
      
      // 3. 解析主文档内容
      const mainContent = await this.parseMainDocument(zip);
      
      // 4. 提取样式信息
      const styles = this.options.extractStyles ? await this.extractStyles(zip) : {};
      
      // 5. 提取表格
      const tables = this.options.extractTables ? await this.extractTables(mainContent) : [];
      
      // 6. 提取图片信息
      const images = this.options.extractImages ? await this.extractImages(zip) : [];
      
      // 7. 组合内容
      const combinedContent = await this.combineContent(mainContent, tables, images);
      
      // 8. 智能格式化
      const formattedResult = await this.formatter.formatContent(
        combinedContent.text,
        {
          filename,
          parser: 'enhanced-docx',
          hasStyles: Object.keys(styles).length > 0,
          hasImages: images.length > 0,
          hasTables: tables.length > 0
        }
      );

      const duration = Date.now() - startTime;
      console.log(`✅ 增强DOCX解析完成，耗时: ${duration}ms`);

      return {
        success: true,
        content: formattedResult.formattedContent,
        rawContent: combinedContent.text,
        html: combinedContent.html,
        structure: {
          ...formattedResult.structure,
          tables: tables.length,
          images: images.length,
          styles: Object.keys(styles).length
        },
        tables,
        images,
        styles,
        metadata: {
          ...formattedResult.metadata,
          filename,
          fileSize: buffer.length,
          parseTime: new Date().toISOString(),
          parseDuration: duration,
          documentStructure
        },
        summary: this.formatter.createSummary(formattedResult.formattedContent),
        keywords: this.formatter.extractKeywords(formattedResult.formattedContent)
      };

    } catch (error) {
      console.error('❌ 增强DOCX解析失败:', error);
      
      // 降级到简单解析
      try {
        const fallbackContent = await this.simpleDOCXParse(buffer);
        return {
          success: false,
          content: fallbackContent,
          rawContent: fallbackContent,
          html: '',
          structure: { paragraphs: 0, titles: 0, lists: 0, tables: 0 },
          tables: [],
          images: [],
          styles: {},
          metadata: {
            filename,
            fileSize: buffer.length,
            parseTime: new Date().toISOString(),
            parser: 'fallback-docx',
            error: error.message
          },
          summary: '',
          keywords: []
        };
      } catch (fallbackError) {
        throw new Error(`DOCX解析完全失败: ${fallbackError.message}`);
      }
    }
  }

  /**
   * 提取文档结构
   */
  async extractDocumentStructure(zip) {
    const structure = {
      hasMainDocument: false,
      hasStyles: false,
      hasNumbering: false,
      hasFootnotes: false,
      hasComments: false,
      hasImages: false,
      relationships: []
    };

    // 检查主要文件
    structure.hasMainDocument = !!zip.file('word/document.xml');
    structure.hasStyles = !!zip.file('word/styles.xml');
    structure.hasNumbering = !!zip.file('word/numbering.xml');
    structure.hasFootnotes = !!zip.file('word/footnotes.xml');
    structure.hasComments = !!zip.file('word/comments.xml');

    // 检查图片
    const mediaFolder = zip.folder('word/media');
    if (mediaFolder) {
      structure.hasImages = Object.keys(mediaFolder.files).length > 0;
    }

    // 提取关系信息
    const relsFile = zip.file('word/_rels/document.xml.rels');
    if (relsFile) {
      try {
        const relsContent = await relsFile.async('text');
        const relsData = await this.xmlParser.parseStringPromise(relsContent);
        if (relsData.Relationships && relsData.Relationships.Relationship) {
          structure.relationships = Array.isArray(relsData.Relationships.Relationship) 
            ? relsData.Relationships.Relationship 
            : [relsData.Relationships.Relationship];
        }
      } catch (error) {
        console.warn('解析关系文件失败:', error.message);
      }
    }

    return structure;
  }

  /**
   * 解析主文档
   */
  async parseMainDocument(zip) {
    const documentFile = zip.file('word/document.xml');
    if (!documentFile) {
      throw new Error('找不到主文档文件');
    }

    const documentContent = await documentFile.async('text');
    const documentData = await this.xmlParser.parseStringPromise(documentContent);

    const content = {
      paragraphs: [],
      tables: [],
      text: '',
      html: ''
    };

    // 解析文档体
    if (documentData['w:document'] && documentData['w:document']['w:body']) {
      const body = documentData['w:document']['w:body'];
      content.paragraphs = await this.extractParagraphs(body);
      content.tables = await this.extractTablesFromBody(body);
    }

    // 生成纯文本
    content.text = content.paragraphs.map(p => p.text).join('\n\n');

    // 生成HTML
    content.html = this.generateHTML(content.paragraphs, content.tables);

    return content;
  }

  /**
   * 提取段落
   */
  async extractParagraphs(body) {
    const paragraphs = [];
    
    if (!body['w:p']) return paragraphs;
    
    const pElements = Array.isArray(body['w:p']) ? body['w:p'] : [body['w:p']];
    
    for (const p of pElements) {
      const paragraph = {
        text: '',
        style: '',
        runs: [],
        isTitle: false,
        isList: false
      };

      // 提取段落属性
      if (p['w:pPr']) {
        paragraph.style = this.extractParagraphStyle(p['w:pPr']);
        paragraph.isTitle = this.isParagraphTitle(p['w:pPr']);
        paragraph.isList = this.isParagraphList(p['w:pPr']);
      }

      // 提取文本运行
      if (p['w:r']) {
        const runs = Array.isArray(p['w:r']) ? p['w:r'] : [p['w:r']];
        
        for (const run of runs) {
          const runText = this.extractRunText(run);
          if (runText) {
            paragraph.runs.push({
              text: runText,
              style: this.extractRunStyle(run['w:rPr'])
            });
            paragraph.text += runText;
          }
        }
      }

      if (paragraph.text.trim()) {
        paragraphs.push(paragraph);
      }
    }

    return paragraphs;
  }

  /**
   * 提取文本运行的文本
   */
  extractRunText(run) {
    let text = '';
    
    if (run['w:t']) {
      text += Array.isArray(run['w:t']) ? run['w:t'].join('') : run['w:t'];
    }
    
    if (run['w:tab']) {
      text += '\t';
    }
    
    if (run['w:br']) {
      text += '\n';
    }
    
    return text;
  }

  /**
   * 提取段落样式
   */
  extractParagraphStyle(pPr) {
    const style = {};
    
    if (pPr['w:pStyle'] && pPr['w:pStyle']['w:val']) {
      style.styleName = pPr['w:pStyle']['w:val'];
    }
    
    if (pPr['w:jc'] && pPr['w:jc']['w:val']) {
      style.alignment = pPr['w:jc']['w:val'];
    }
    
    return style;
  }

  /**
   * 检查是否为标题段落
   */
  isParagraphTitle(pPr) {
    if (!pPr) return false;
    
    if (pPr['w:pStyle'] && pPr['w:pStyle']['w:val']) {
      const styleName = pPr['w:pStyle']['w:val'].toLowerCase();
      return styleName.includes('heading') || styleName.includes('title') || styleName.includes('标题');
    }
    
    return false;
  }

  /**
   * 检查是否为列表段落
   */
  isParagraphList(pPr) {
    if (!pPr) return false;
    
    return !!(pPr['w:numPr'] || pPr['w:pStyle'] && pPr['w:pStyle']['w:val'] && 
             pPr['w:pStyle']['w:val'].toLowerCase().includes('list'));
  }

  /**
   * 提取文本运行样式
   */
  extractRunStyle(rPr) {
    if (!rPr) return {};
    
    const style = {};
    
    if (rPr['w:b']) style.bold = true;
    if (rPr['w:i']) style.italic = true;
    if (rPr['w:u']) style.underline = true;
    if (rPr['w:color'] && rPr['w:color']['w:val']) {
      style.color = rPr['w:color']['w:val'];
    }
    
    return style;
  }

  /**
   * 简单DOCX解析（降级方案）
   */
  async simpleDOCXParse(buffer) {
    try {
      const zip = await JSZip.loadAsync(buffer);
      const documentFile = zip.file('word/document.xml');
      
      if (!documentFile) {
        return 'DOCX文档解析失败：找不到主文档文件';
      }
      
      const content = await documentFile.async('text');
      
      // 简单的文本提取
      const textMatches = content.match(/<w:t[^>]*>([^<]+)<\/w:t>/g);
      if (textMatches) {
        return textMatches
          .map(match => match.replace(/<[^>]+>/g, ''))
          .filter(text => text.trim())
          .join(' ');
      }
      
      return 'DOCX文档解析成功，但未能提取到可读文本内容';
      
    } catch (error) {
      return `DOCX文档解析失败：${error.message}`;
    }
  }

  /**
   * 从文档体中提取表格
   */
  async extractTablesFromBody(body) {
    const tables = [];

    if (!body['w:tbl']) return tables;

    const tableElements = Array.isArray(body['w:tbl']) ? body['w:tbl'] : [body['w:tbl']];

    for (const tbl of tableElements) {
      const table = {
        rows: [],
        style: {},
        caption: ''
      };

      // 提取表格行
      if (tbl['w:tr']) {
        const rows = Array.isArray(tbl['w:tr']) ? tbl['w:tr'] : [tbl['w:tr']];

        for (const tr of rows) {
          const row = [];

          if (tr['w:tc']) {
            const cells = Array.isArray(tr['w:tc']) ? tr['w:tc'] : [tr['w:tc']];

            for (const tc of cells) {
              const cellText = this.extractCellText(tc);
              row.push(cellText);
            }
          }

          if (row.length > 0) {
            table.rows.push(row);
          }
        }
      }

      if (table.rows.length > 0) {
        tables.push(table);
      }
    }

    return tables;
  }

  /**
   * 提取表格单元格文本
   */
  extractCellText(cell) {
    let text = '';

    if (cell['w:p']) {
      const paragraphs = Array.isArray(cell['w:p']) ? cell['w:p'] : [cell['w:p']];

      for (const p of paragraphs) {
        if (p['w:r']) {
          const runs = Array.isArray(p['w:r']) ? p['w:r'] : [p['w:r']];

          for (const run of runs) {
            text += this.extractRunText(run);
          }
        }
        text += ' ';
      }
    }

    return text.trim();
  }

  /**
   * 提取样式信息
   */
  async extractStyles(zip) {
    const styles = {};

    const stylesFile = zip.file('word/styles.xml');
    if (!stylesFile) return styles;

    try {
      const stylesContent = await stylesFile.async('text');
      const stylesData = await this.xmlParser.parseStringPromise(stylesContent);

      if (stylesData['w:styles'] && stylesData['w:styles']['w:style']) {
        const styleElements = Array.isArray(stylesData['w:styles']['w:style'])
          ? stylesData['w:styles']['w:style']
          : [stylesData['w:styles']['w:style']];

        for (const style of styleElements) {
          if (style['w:styleId']) {
            styles[style['w:styleId']] = {
              name: style['w:name'] ? style['w:name']['w:val'] : '',
              type: style['w:type'] || 'paragraph',
              basedOn: style['w:basedOn'] ? style['w:basedOn']['w:val'] : '',
              properties: this.extractStyleProperties(style)
            };
          }
        }
      }
    } catch (error) {
      console.warn('提取样式信息失败:', error.message);
    }

    return styles;
  }

  /**
   * 提取样式属性
   */
  extractStyleProperties(style) {
    const properties = {};

    if (style['w:pPr']) {
      properties.paragraph = this.extractParagraphStyle(style['w:pPr']);
    }

    if (style['w:rPr']) {
      properties.run = this.extractRunStyle(style['w:rPr']);
    }

    return properties;
  }

  /**
   * 提取图片信息
   */
  async extractImages(zip) {
    const images = [];

    const mediaFolder = zip.folder('word/media');
    if (!mediaFolder) return images;

    const imageFiles = Object.keys(mediaFolder.files).filter(filename =>
      /\.(jpg|jpeg|png|gif|bmp)$/i.test(filename)
    );

    for (const filename of imageFiles) {
      const file = mediaFolder.files[filename];

      images.push({
        filename: filename.replace('word/media/', ''),
        size: file._data ? file._data.uncompressedSize : 0,
        type: this.getImageType(filename),
        position: 'inline' // 默认为内联，实际位置需要从文档中解析
      });
    }

    return images;
  }

  /**
   * 获取图片类型
   */
  getImageType(filename) {
    const ext = filename.toLowerCase().split('.').pop();
    const typeMap = {
      'jpg': 'image/jpeg',
      'jpeg': 'image/jpeg',
      'png': 'image/png',
      'gif': 'image/gif',
      'bmp': 'image/bmp'
    };
    return typeMap[ext] || 'image/unknown';
  }

  /**
   * 组合内容
   */
  async combineContent(mainContent, tables, images) {
    let text = mainContent.text;
    let html = mainContent.html;

    // 在文本中标记表格位置
    if (tables.length > 0) {
      text += '\n\n## 表格内容\n\n';

      tables.forEach((table, index) => {
        text += `### 表格 ${index + 1}\n\n`;

        if (table.rows.length > 0) {
          // 转换为Markdown表格格式
          const headers = table.rows[0];
          text += `| ${headers.join(' | ')} |\n`;
          text += `|${headers.map(() => ' --- ').join('|')}|\n`;

          for (let i = 1; i < table.rows.length; i++) {
            text += `| ${table.rows[i].join(' | ')} |\n`;
          }
        }

        text += '\n';
      });
    }

    // 在文本中标记图片位置
    if (images.length > 0) {
      text += '\n\n## 图片内容\n\n';

      images.forEach((image, index) => {
        text += `![图片 ${index + 1}](${image.filename}) - ${image.type}\n\n`;
      });
    }

    return { text, html };
  }

  /**
   * 生成HTML
   */
  generateHTML(paragraphs, tables) {
    let html = '<div class="docx-content">\n';

    for (const paragraph of paragraphs) {
      if (paragraph.isTitle) {
        html += `<h3>${this.escapeHtml(paragraph.text)}</h3>\n`;
      } else if (paragraph.isList) {
        html += `<li>${this.escapeHtml(paragraph.text)}</li>\n`;
      } else {
        html += `<p>${this.escapeHtml(paragraph.text)}</p>\n`;
      }
    }

    // 添加表格
    for (const table of tables) {
      html += '<table border="1">\n';

      if (table.rows.length > 0) {
        html += '<thead><tr>\n';
        for (const header of table.rows[0]) {
          html += `<th>${this.escapeHtml(header)}</th>\n`;
        }
        html += '</tr></thead>\n';

        if (table.rows.length > 1) {
          html += '<tbody>\n';
          for (let i = 1; i < table.rows.length; i++) {
            html += '<tr>\n';
            for (const cell of table.rows[i]) {
              html += `<td>${this.escapeHtml(cell)}</td>\n`;
            }
            html += '</tr>\n';
          }
          html += '</tbody>\n';
        }
      }

      html += '</table>\n';
    }

    html += '</div>';

    return html;
  }

  /**
   * HTML转义
   */
  escapeHtml(text) {
    const map = {
      '&': '&amp;',
      '<': '&lt;',
      '>': '&gt;',
      '"': '&quot;',
      "'": '&#039;'
    };

    return text.replace(/[&<>"']/g, m => map[m]);
  }

  /**
   * 提取表格（公共方法）
   */
  async extractTables(mainContent) {
    return mainContent.tables || [];
  }
}

module.exports = EnhancedDOCXParser;
