/**
 * 文档格式检测器
 * 专门负责精确识别文档格式并选择对应的解析插件
 */

class DocumentFormatDetector {
  constructor() {
    // 文件头魔数映射
    this.fileSignatures = {
      // PDF
      'pdf': [0x25, 0x50, 0x44, 0x46], // %PDF
      
      // Office 2007+ (ZIP-based)
      'office_zip': [0x50, 0x4B, 0x03, 0x04], // PK..
      
      // Office 97-2003
      'office_ole': [0xD0, 0xCF, 0x11, 0xE0, 0xA1, 0xB1, 0x1A, 0xE1],
      
      // Images
      'png': [0x89, 0x50, 0x4E, 0x47, 0x0D, 0x0A, 0x1A, 0x0A],
      'jpg': [0xFF, 0xD8, 0xFF],
      'gif': [0x47, 0x49, 0x46, 0x38],
      'bmp': [0x42, 0x4D],
      'webp': [0x52, 0x49, 0x46, 0x46],
      
      // Text formats
      'utf8_bom': [0xEF, 0xBB, 0xBF],
      'utf16_le': [0xFF, 0xFE],
      'utf16_be': [0xFE, 0xFF]
    };

    // MIME类型到格式的精确映射
    this.mimeTypeMap = {
      // PDF
      'application/pdf': 'pdf',
      
      // Word
      'application/vnd.openxmlformats-officedocument.wordprocessingml.document': 'docx',
      'application/msword': 'doc',
      
      // Excel
      'application/vnd.openxmlformats-officedocument.spreadsheetml.sheet': 'xlsx',
      'application/vnd.ms-excel': 'xls',
      
      // PowerPoint
      'application/vnd.openxmlformats-officedocument.presentationml.presentation': 'pptx',
      'application/vnd.ms-powerpoint': 'ppt',
      
      // Text
      'text/plain': 'text',
      'text/csv': 'csv',
      'application/json': 'json',
      'application/xml': 'xml',
      'text/xml': 'xml',
      
      // Images
      'image/png': 'png',
      'image/jpeg': 'jpg',
      'image/gif': 'gif',
      'image/bmp': 'bmp',
      'image/webp': 'webp'
    };

    // 扩展名到格式映射
    this.extensionMap = {
      'pdf': 'pdf',
      'docx': 'docx',
      'doc': 'doc',
      'xlsx': 'xlsx',
      'xls': 'xls',
      'pptx': 'pptx',
      'ppt': 'ppt',
      'csv': 'csv',
      'json': 'json',
      'xml': 'xml',
      'txt': 'text',
      'md': 'markdown',
      'png': 'png',
      'jpg': 'jpg',
      'jpeg': 'jpg',
      'gif': 'gif',
      'bmp': 'bmp',
      'webp': 'webp'
    };

    // 格式到插件映射
    this.formatToPluginMap = {
      'pdf': 'pdf_parser',
      'docx': 'docx_parser',
      'doc': 'docx_parser',
      'xlsx': 'xlsx_parser',
      'xls': 'xlsx_parser',
      'csv': 'csv_parser',
      'json': 'json_parser',
      'xml': 'xml_parser',
      'text': 'text_parser',
      'markdown': 'text_parser',
      'png': 'image_parser',
      'jpg': 'image_parser',
      'gif': 'image_parser',
      'bmp': 'image_parser',
      'webp': 'image_parser'
    };
  }

  /**
   * 主要检测方法
   */
  async detectFormat(input) {
    const result = {
      format: 'text',
      confidence: 0.5,
      plugin: 'text_parser',
      detectionMethod: 'fallback',
      metadata: {}
    };

    try {
      let buffer = null;
      let fileName = '';
      let mimeType = '';

      // 处理不同输入类型
      if (input.buffer) {
        buffer = input.buffer;
        fileName = input.fileName || '';
        mimeType = input.mimeType || '';
      } else if (input.base64) {
        buffer = Buffer.from(input.base64, 'base64');
        fileName = input.fileName || '';
        mimeType = input.mimeType || '';
      } else if (input.filePath) {
        const fs = require('fs');
        buffer = fs.readFileSync(input.filePath);
        fileName = input.filePath;
      } else if (input.file) {
        // 前端文件对象
        fileName = input.file.name || '';
        mimeType = input.file.type || '';
        if (input.file.base64) {
          buffer = Buffer.from(input.file.base64, 'base64');
        }
      } else {
        // 直接传递的属性
        fileName = input.fileName || '';
        mimeType = input.mimeType || '';
        if (input.base64) {
          buffer = Buffer.from(input.base64, 'base64');
        }
      }

      // 1. 基于MIME类型检测（优先级最高，最准确）
      if (mimeType) {
        const mimeResult = this.detectByMimeType(mimeType);
        if (mimeResult.confidence > result.confidence) {
          Object.assign(result, mimeResult);
          result.detectionMethod = 'mime_type';
        }
      }

      // 2. 基于文件扩展名检测
      if (fileName && result.confidence < 0.9) {
        const extResult = this.detectByExtension(fileName);
        if (extResult.confidence > result.confidence) {
          Object.assign(result, extResult);
          result.detectionMethod = 'file_extension';
        }
      }

      // 3. 基于文件头检测（最可靠，但需要文件内容）
      if (buffer && buffer.length > 0) {
        const headerResult = this.detectByFileHeader(buffer);
        if (headerResult.confidence > result.confidence) {
          Object.assign(result, headerResult);
          result.detectionMethod = 'file_header';
        }
      }

      // 4. Office文档特殊处理
      if (buffer && result.format === 'office_zip') {
        const officeResult = await this.detectOfficeFormat(buffer);
        if (officeResult.confidence > result.confidence) {
          Object.assign(result, officeResult);
          result.detectionMethod = 'office_analysis';
        }
      }

      // 5. 设置对应插件
      result.plugin = this.formatToPluginMap[result.format] || 'text_parser';

      // 6. 添加元数据
      result.metadata = {
        fileName,
        mimeType,
        fileSize: buffer ? buffer.length : 0,
        detectedAt: new Date().toISOString()
      };

      return result;

    } catch (error) {
      console.error('格式检测失败:', error);
      return {
        ...result,
        error: error.message,
        detectionMethod: 'error_fallback'
      };
    }
  }

  /**
   * 基于文件头检测
   */
  detectByFileHeader(buffer) {
    const result = { format: 'unknown', confidence: 0 };

    // 检查PDF
    if (this.matchSignature(buffer, this.fileSignatures.pdf)) {
      return { format: 'pdf', confidence: 0.95 };
    }

    // 检查PNG
    if (this.matchSignature(buffer, this.fileSignatures.png)) {
      return { format: 'png', confidence: 0.95 };
    }

    // 检查JPEG
    if (this.matchSignature(buffer, this.fileSignatures.jpg)) {
      return { format: 'jpg', confidence: 0.95 };
    }

    // 检查GIF
    if (this.matchSignature(buffer, this.fileSignatures.gif)) {
      return { format: 'gif', confidence: 0.95 };
    }

    // 检查BMP
    if (this.matchSignature(buffer, this.fileSignatures.bmp)) {
      return { format: 'bmp', confidence: 0.95 };
    }

    // 检查Office ZIP格式 (DOCX/XLSX/PPTX)
    if (this.matchSignature(buffer, this.fileSignatures.office_zip)) {
      return { format: 'office_zip', confidence: 0.8 };
    }

    // 检查Office OLE格式 (DOC/XLS/PPT)
    if (this.matchSignature(buffer, this.fileSignatures.office_ole)) {
      return { format: 'office_ole', confidence: 0.8 };
    }

    // 检查文本编码
    if (this.matchSignature(buffer, this.fileSignatures.utf8_bom)) {
      return { format: 'text', confidence: 0.7 };
    }

    return result;
  }

  /**
   * 基于MIME类型检测
   */
  detectByMimeType(mimeType) {
    const format = this.mimeTypeMap[mimeType];
    if (format) {
      return { format, confidence: 0.9 };
    }

    // 模糊匹配
    if (mimeType.includes('wordprocessingml')) {
      return { format: 'docx', confidence: 0.85 };
    }
    if (mimeType.includes('spreadsheetml')) {
      return { format: 'xlsx', confidence: 0.85 };
    }
    if (mimeType.includes('image')) {
      return { format: 'image', confidence: 0.7 };
    }
    if (mimeType.includes('text')) {
      return { format: 'text', confidence: 0.7 };
    }

    return { format: 'unknown', confidence: 0 };
  }

  /**
   * 基于文件扩展名检测
   */
  detectByExtension(fileName) {
    const ext = fileName.split('.').pop()?.toLowerCase();
    const format = this.extensionMap[ext];
    
    if (format) {
      return { format, confidence: 0.8 };
    }

    return { format: 'unknown', confidence: 0 };
  }

  /**
   * Office文档格式细分检测
   */
  async detectOfficeFormat(buffer) {
    try {
      const bufferStr = buffer.toString('binary');
      
      // 检查DOCX特征
      if (bufferStr.includes('word/document.xml') || 
          bufferStr.includes('wordprocessingml')) {
        return { format: 'docx', confidence: 0.9 };
      }
      
      // 检查XLSX特征
      if (bufferStr.includes('xl/workbook.xml') || 
          bufferStr.includes('spreadsheetml')) {
        return { format: 'xlsx', confidence: 0.9 };
      }
      
      // 检查PPTX特征
      if (bufferStr.includes('ppt/presentation.xml') || 
          bufferStr.includes('presentationml')) {
        return { format: 'pptx', confidence: 0.9 };
      }

      return { format: 'office_zip', confidence: 0.6 };
    } catch (error) {
      return { format: 'office_zip', confidence: 0.5 };
    }
  }

  /**
   * 检查文件签名匹配
   */
  matchSignature(buffer, signature) {
    if (buffer.length < signature.length) return false;
    
    for (let i = 0; i < signature.length; i++) {
      if (buffer[i] !== signature[i]) return false;
    }
    
    return true;
  }

  /**
   * 获取支持的格式列表
   */
  getSupportedFormats() {
    return Object.keys(this.formatToPluginMap);
  }

  /**
   * 获取格式对应的插件
   */
  getPluginForFormat(format) {
    return this.formatToPluginMap[format] || 'text_parser';
  }
}

module.exports = DocumentFormatDetector;
