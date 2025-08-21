/**
 * QMS质量管理文档专用解析器
 * 专门针对质量管理文档的结构和内容进行优化解析
 */

class QMSDocumentParser {
  constructor() {
    // QMS文档常见模式
    this.qmsPatterns = {
      // 8D问题解决法步骤
      d8Steps: /^D[1-8][\s\.]*(.*)/,
      
      // 5W2H分析法
      w5h2: /^(What|Where|When|Who|Why|How|How\s+Much)[\s\(（]*(.*)/,
      
      // 质量管理关键词
      qmsKeywords: [
        '问题描述', '根本原因', '临时措施', '纠正措施', '预防措施',
        '时间', '地点', '人员', '现象', '原因分析', '解决方案',
        '项目', '任务', '目标', '计划', '执行', '检查', '改进',
        '质量', '管理', '体系', '流程', '标准', '规范', '要求',
        '不合格', '缺陷', '偏差', '异常', '风险', '控制',
        'FMEA', 'MSA', 'SPC', 'PDCA', 'DMAIC'
      ],
      
      // 日期时间格式
      dateTime: [
        /\d{4}年\d{1,2}月\d{1,2}日/,
        /\d{4}[\/\-]\d{1,2}[\/\-]\d{1,2}/,
        /\d{1,2}[\/\-]\d{1,2}[\/\-]\d{4}/
      ],
      
      // 责任人员格式
      personnel: [
        /^(责任人|负责人|执行人|参与人员|涉及人员)[:：]\s*(.*)/,
        /^(姓名|人员|联系人)[:：]\s*(.*)/
      ]
    };
    
    // 文档结构识别
    this.documentStructure = {
      sections: [],
      keyInfo: {},
      timeline: [],
      personnel: [],
      actions: []
    };
  }

  /**
   * 解析QMS文档
   */
  parseQMSDocument(content) {
    const lines = content.split('\n').map(line => line.trim()).filter(line => line);
    
    // 重置文档结构
    this.documentStructure = {
      sections: [],
      keyInfo: {},
      timeline: [],
      personnel: [],
      actions: []
    };
    
    // 逐行分析
    for (let i = 0; i < lines.length; i++) {
      const line = lines[i];
      const nextLine = lines[i + 1] || '';
      
      this.analyzeLine(line, nextLine, i);
    }
    
    // 生成结构化内容
    return this.generateStructuredContent(content);
  }

  /**
   * 分析单行内容
   */
  analyzeLine(line, nextLine, index) {
    // 检查8D步骤
    const d8Match = line.match(this.qmsPatterns.d8Steps);
    if (d8Match) {
      const stepNumber = d8Match[0].match(/D(\d)/)[1];
      let title = d8Match[1] ? d8Match[1].trim() : '';
      let content = '';

      // 如果标题为空或很短，使用下一行作为标题
      if (!title || title.length < 3) {
        if (nextLine && !nextLine.match(/^D\d/) && !nextLine.match(this.qmsPatterns.w5h2)) {
          title = nextLine;
        } else {
          title = `步骤${stepNumber}`;
        }
      }

      // 避免重复添加相同步骤
      const existingStep = this.documentStructure.sections.find(
        s => s.type === '8D步骤' && s.step === stepNumber
      );

      if (!existingStep) {
        this.documentStructure.sections.push({
          type: '8D步骤',
          step: stepNumber,
          title: title,
          content: content,
          index
        });
      }
      return;
    }

    // 检查5W2H分析
    const w5h2Match = line.match(this.qmsPatterns.w5h2);
    if (w5h2Match) {
      let question = w5h2Match[1].trim();
      let content = w5h2Match[2] ? w5h2Match[2].trim() : '';

      // 处理"How Much"的特殊情况
      if (question === 'How' && content.startsWith('Much')) {
        question = 'How Much';
        content = content.substring(4).trim();
      }

      // 如果内容为空，使用下一行
      if (!content && nextLine && !nextLine.match(/^D\d/) && !nextLine.match(this.qmsPatterns.w5h2)) {
        content = nextLine;
      }

      // 避免重复添加相同问题
      const existingQuestion = this.documentStructure.sections.find(
        s => s.type === '5W2H分析' && s.question === question
      );

      if (!existingQuestion && content) {
        this.documentStructure.sections.push({
          type: '5W2H分析',
          question: question,
          content: content,
          index
        });
      }
      return;
    }

    // 检查日期时间
    for (const pattern of this.qmsPatterns.dateTime) {
      if (pattern.test(line)) {
        const dateMatch = line.match(pattern)[0];
        // 避免重复添加相同日期
        const existingDate = this.documentStructure.timeline.find(
          t => t.date === dateMatch
        );

        if (!existingDate) {
          this.documentStructure.timeline.push({
            date: dateMatch,
            context: line.replace(dateMatch, '').trim() || line,
            index
          });
        }
        break;
      }
    }

    // 检查人员信息
    for (const pattern of this.qmsPatterns.personnel) {
      const match = line.match(pattern);
      if (match) {
        this.documentStructure.personnel.push({
          role: match[1],
          name: match[2],
          index
        });
        break;
      }
    }
  }

  /**
   * 生成结构化内容
   */
  generateStructuredContent(originalContent) {
    const sections = [];

    // 添加文档概览
    sections.push('# 质量管理文档解析结果');
    sections.push('');

    // 添加8D步骤
    const d8Steps = this.documentStructure.sections.filter(s => s.type === '8D步骤');
    if (d8Steps.length > 0) {
      sections.push('## 📋 8D问题解决步骤');
      sections.push('');
      d8Steps.sort((a, b) => parseInt(a.step) - parseInt(b.step));
      d8Steps.forEach(step => {
        sections.push(`### D${step.step} ${step.title || '步骤' + step.step}`);
        if (step.content && step.content.trim()) {
          sections.push(`${step.content.trim()}`);
        }
        sections.push('');
      });
    }

    // 添加5W2H分析
    const w5h2Analysis = this.documentStructure.sections.filter(s => s.type === '5W2H分析');
    if (w5h2Analysis.length > 0) {
      sections.push('## 🔍 5W2H分析');
      sections.push('');
      const questionOrder = ['What', 'Where', 'When', 'Who', 'Why', 'How', 'How Much'];
      questionOrder.forEach(q => {
        const item = w5h2Analysis.find(a => a.question === q);
        if (item && item.content && item.content.trim()) {
          sections.push(`### ${item.question} - ${this.getQuestionTranslation(item.question)}`);
          // 清理内容，移除重复的问题标识
          let cleanContent = item.content.trim();
          cleanContent = cleanContent.replace(/^[^：:]*[：:]\s*/, ''); // 移除问题前缀
          sections.push(`${cleanContent}`);
          sections.push('');
        }
      });
    }

    // 添加数据统计信息
    const dataStats = this.extractDataStatistics(originalContent);
    if (dataStats.length > 0) {
      sections.push('## 📊 数据统计');
      sections.push('');
      dataStats.forEach(stat => {
        sections.push(`**${stat.label}:** ${stat.value}`);
      });
      sections.push('');
    }

    // 添加库存信息
    const inventoryData = this.extractInventoryData(originalContent);
    if (inventoryData.length > 0) {
      sections.push('## 📦 库存信息');
      sections.push('');
      inventoryData.forEach(item => {
        sections.push(`- **${item.location}:** ${item.quantity}`);
      });
      sections.push('');
    }

    // 添加时间线
    if (this.documentStructure.timeline.length > 0) {
      sections.push('## ⏰ 时间线');
      sections.push('');
      this.documentStructure.timeline.forEach(item => {
        sections.push(`- **${item.date}:** ${item.context}`);
      });
      sections.push('');
    }

    // 添加人员信息
    if (this.documentStructure.personnel.length > 0) {
      sections.push('## 👥 相关人员');
      sections.push('');
      this.documentStructure.personnel.forEach(person => {
        sections.push(`- **${person.role}:** ${person.name}`);
      });
      sections.push('');
    }

    return sections.join('\n');
  }

  /**
   * 提取数据统计信息
   */
  extractDataStatistics(content) {
    const stats = [];
    const lines = content.split('\n');

    lines.forEach(line => {
      const trimmed = line.trim();

      // 提取不良率数据
      const defectRateMatch = trimmed.match(/不良率[：:]?\s*(\d+%)/);
      if (defectRateMatch) {
        stats.push({
          label: '不良率',
          value: defectRateMatch[1]
        });
      }

      // 提取抽检数据
      const sampleMatch = trimmed.match(/抽检[：:]?\s*(\d+pcs)/);
      if (sampleMatch) {
        stats.push({
          label: '抽检数量',
          value: sampleMatch[1]
        });
      }

      // 提取不良数量
      const defectMatch = trimmed.match(/(\d+pcs中\d+pcs不良)/);
      if (defectMatch) {
        stats.push({
          label: '不良情况',
          value: defectMatch[1]
        });
      }

      // 提取数值数据
      const numberMatch = trimmed.match(/^(\d+)$/);
      if (numberMatch && parseInt(numberMatch[1]) > 100) {
        const prevLine = lines[lines.indexOf(line) - 1];
        if (prevLine && prevLine.trim()) {
          stats.push({
            label: prevLine.trim(),
            value: numberMatch[1]
          });
        }
      }
    });

    return stats;
  }

  /**
   * 提取库存信息
   */
  extractInventoryData(content) {
    const inventory = [];
    const lines = content.split('\n');

    let currentLocation = '';
    lines.forEach((line, index) => {
      const trimmed = line.trim();

      // 识别库存位置
      if (trimmed.includes('库存') || trimmed.includes('存储') || trimmed.includes('工厂存货')) {
        currentLocation = trimmed;
      }

      // 识别具体位置和数量
      if (trimmed.match(/^[^：:]*工厂存货$/) || trimmed.match(/^[^：:]*库存$/)) {
        currentLocation = trimmed;
        // 查找下一行的数量
        const nextLine = lines[index + 1];
        if (nextLine && nextLine.trim().match(/^\d+$/)) {
          inventory.push({
            location: currentLocation,
            quantity: nextLine.trim()
          });
        }
      }
    });

    return inventory;
  }

  /**
   * 获取5W2H问题的中文翻译
   */
  getQuestionTranslation(question) {
    const translations = {
      'What': '是什么问题',
      'Where': '在哪里发生',
      'When': '什么时候发生',
      'Who': '谁发现/负责',
      'Why': '为什么发生',
      'How': '如何发现',
      'How Much': '影响程度'
    };
    return translations[question] || question;
  }

  /**
   * 获取文档统计信息
   */
  getDocumentStats() {
    return {
      d8StepsCount: this.documentStructure.sections.filter(s => s.type === '8D步骤').length,
      w5h2AnalysisCount: this.documentStructure.sections.filter(s => s.type === '5W2H分析').length,
      timelineCount: this.documentStructure.timeline.length,
      personnelCount: this.documentStructure.personnel.length,
      keyInfoCount: Object.keys(this.documentStructure.keyInfo).length,
      totalSections: this.documentStructure.sections.length
    };
  }

  /**
   * 验证文档完整性
   */
  validateDocumentCompleteness() {
    const issues = [];
    const stats = this.getDocumentStats();
    
    // 检查8D步骤完整性
    if (stats.d8StepsCount > 0 && stats.d8StepsCount < 8) {
      const existingSteps = this.documentStructure.sections
        .filter(s => s.type === '8D步骤')
        .map(s => parseInt(s.step));
      const missingSteps = [];
      for (let i = 1; i <= 8; i++) {
        if (!existingSteps.includes(i)) {
          missingSteps.push(`D${i}`);
        }
      }
      issues.push(`8D步骤不完整，缺少: ${missingSteps.join(', ')}`);
    }
    
    // 检查5W2H分析完整性
    if (stats.w5h2AnalysisCount > 0 && stats.w5h2AnalysisCount < 7) {
      const existingQuestions = this.documentStructure.sections
        .filter(s => s.type === '5W2H分析')
        .map(s => s.question);
      const requiredQuestions = ['What', 'Where', 'When', 'Who', 'Why', 'How', 'How Much'];
      const missingQuestions = requiredQuestions.filter(q => !existingQuestions.includes(q));
      issues.push(`5W2H分析不完整，缺少: ${missingQuestions.join(', ')}`);
    }
    
    // 检查基本信息
    if (stats.timelineCount === 0) {
      issues.push('缺少时间信息');
    }
    
    if (stats.personnelCount === 0) {
      issues.push('缺少人员信息');
    }
    
    return {
      isComplete: issues.length === 0,
      issues,
      completeness: Math.max(0, 100 - issues.length * 20) // 每个问题扣20分
    };
  }
}

module.exports = QMSDocumentParser;
