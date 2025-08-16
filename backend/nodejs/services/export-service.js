/**
 * 对话导出服务
 * 支持多种格式导出：PDF, Word, Excel, Markdown, JSON
 */

const fs = require('fs');
const path = require('path');
const moment = require('moment');

class ExportService {
  constructor() {
    this.exportDir = path.join(__dirname, '../exports');
    this.ensureExportDir();
  }

  // 确保导出目录存在
  ensureExportDir() {
    if (!fs.existsSync(this.exportDir)) {
      fs.mkdirSync(this.exportDir, { recursive: true });
    }
  }

  // 导出为JSON格式
  async exportToJSON(conversations, options = {}) {
    try {
      const exportData = {
        export_info: {
          timestamp: new Date().toISOString(),
          format: 'JSON',
          total_conversations: conversations.length,
          exported_by: options.user_info?.username || 'Unknown',
          export_options: options
        },
        conversations: conversations.map(conv => ({
          id: conv.id,
          title: conv.title,
          model_provider: conv.model_provider,
          model_name: conv.model_name,
          created_at: conv.created_at,
          updated_at: conv.updated_at,
          message_count: conv.message_count,
          messages: conv.messages?.map(msg => ({
            id: msg.id,
            type: msg.message_type,
            content: msg.content,
            timestamp: msg.created_at,
            rating: msg.rating,
            feedback: msg.feedback,
            response_time: msg.response_time,
            token_usage: msg.token_usage
          })) || []
        }))
      };

      const filename = `conversations_${moment().format('YYYYMMDD_HHmmss')}.json`;
      const filepath = path.join(this.exportDir, filename);
      
      fs.writeFileSync(filepath, JSON.stringify(exportData, null, 2), 'utf8');
      
      return {
        success: true,
        filename,
        filepath,
        size: fs.statSync(filepath).size
      };
    } catch (error) {
      console.error('JSON导出失败:', error);
      throw new Error('JSON导出失败: ' + error.message);
    }
  }

  // 导出为Markdown格式
  async exportToMarkdown(conversations, options = {}) {
    try {
      let markdown = `# AI对话记录导出\n\n`;
      markdown += `**导出时间**: ${moment().format('YYYY-MM-DD HH:mm:ss')}\n`;
      markdown += `**导出用户**: ${options.user_info?.username || 'Unknown'}\n`;
      markdown += `**对话数量**: ${conversations.length}\n\n`;
      markdown += `---\n\n`;

      conversations.forEach((conv, index) => {
        markdown += `## ${index + 1}. ${conv.title}\n\n`;
        markdown += `- **模型**: ${conv.model_provider} (${conv.model_name})\n`;
        markdown += `- **创建时间**: ${moment(conv.created_at).format('YYYY-MM-DD HH:mm:ss')}\n`;
        markdown += `- **消息数量**: ${conv.message_count}\n\n`;

        if (conv.messages && conv.messages.length > 0) {
          conv.messages.forEach((msg, msgIndex) => {
            const role = msg.message_type === 'user' ? '👤 用户' : '🤖 AI助手';
            markdown += `### ${msgIndex + 1}. ${role}\n\n`;
            markdown += `**时间**: ${moment(msg.created_at).format('YYYY-MM-DD HH:mm:ss')}\n\n`;
            markdown += `${msg.content}\n\n`;
            
            if (msg.message_type === 'assistant') {
              if (msg.response_time) {
                markdown += `*响应时间: ${msg.response_time}ms*\n\n`;
              }
              if (msg.rating) {
                markdown += `**用户评分**: ${'⭐'.repeat(msg.rating)} (${msg.rating}/5)\n\n`;
                if (msg.feedback) {
                  markdown += `**用户反馈**: ${msg.feedback}\n\n`;
                }
              }
            }
            
            markdown += `---\n\n`;
          });
        }
        
        markdown += `\n\n`;
      });

      const filename = `conversations_${moment().format('YYYYMMDD_HHmmss')}.md`;
      const filepath = path.join(this.exportDir, filename);
      
      fs.writeFileSync(filepath, markdown, 'utf8');
      
      return {
        success: true,
        filename,
        filepath,
        size: fs.statSync(filepath).size
      };
    } catch (error) {
      console.error('Markdown导出失败:', error);
      throw new Error('Markdown导出失败: ' + error.message);
    }
  }

  // 导出为Excel格式
  async exportToExcel(conversations, options = {}) {
    try {
      // 动态导入xlsx
      const XLSX = require('xlsx');
      
      // 创建工作簿
      const workbook = XLSX.utils.book_new();
      
      // 对话概览工作表
      const conversationData = conversations.map((conv, index) => ({
        '序号': index + 1,
        '对话标题': conv.title,
        'AI模型': conv.model_provider,
        '模型名称': conv.model_name,
        '创建时间': moment(conv.created_at).format('YYYY-MM-DD HH:mm:ss'),
        '更新时间': moment(conv.updated_at).format('YYYY-MM-DD HH:mm:ss'),
        '消息数量': conv.message_count,
        '平均评分': conv.messages ? 
          (conv.messages.filter(m => m.rating).reduce((sum, m) => sum + m.rating, 0) / 
           conv.messages.filter(m => m.rating).length || 0).toFixed(1) : '0.0'
      }));
      
      const conversationSheet = XLSX.utils.json_to_sheet(conversationData);
      XLSX.utils.book_append_sheet(workbook, conversationSheet, '对话概览');
      
      // 详细消息工作表
      const messageData = [];
      conversations.forEach(conv => {
        if (conv.messages && conv.messages.length > 0) {
          conv.messages.forEach(msg => {
            messageData.push({
              '对话标题': conv.title,
              '消息类型': msg.message_type === 'user' ? '用户' : 'AI助手',
              '消息内容': msg.content,
              '发送时间': moment(msg.created_at).format('YYYY-MM-DD HH:mm:ss'),
              '响应时间(ms)': msg.response_time || '',
              '用户评分': msg.rating || '',
              '用户反馈': msg.feedback || '',
              'Token使用': msg.token_usage ? JSON.stringify(msg.token_usage) : ''
            });
          });
        }
      });
      
      const messageSheet = XLSX.utils.json_to_sheet(messageData);
      XLSX.utils.book_append_sheet(workbook, messageSheet, '详细消息');
      
      // 评分统计工作表
      const ratingStats = this.calculateRatingStats(conversations);
      const ratingData = [
        { '统计项': '总对话数', '数值': conversations.length },
        { '统计项': '总消息数', '数值': messageData.length },
        { '统计项': '已评分消息', '数值': ratingStats.ratedCount },
        { '统计项': '平均评分', '数值': ratingStats.avgRating.toFixed(1) },
        { '统计项': '评分率', '数值': (ratingStats.ratingRate * 100).toFixed(1) + '%' },
        { '统计项': '1星评分', '数值': ratingStats.distribution[1] || 0 },
        { '统计项': '2星评分', '数值': ratingStats.distribution[2] || 0 },
        { '统计项': '3星评分', '数值': ratingStats.distribution[3] || 0 },
        { '统计项': '4星评分', '数值': ratingStats.distribution[4] || 0 },
        { '统计项': '5星评分', '数值': ratingStats.distribution[5] || 0 }
      ];
      
      const ratingSheet = XLSX.utils.json_to_sheet(ratingData);
      XLSX.utils.book_append_sheet(workbook, ratingSheet, '评分统计');
      
      const filename = `conversations_${moment().format('YYYYMMDD_HHmmss')}.xlsx`;
      const filepath = path.join(this.exportDir, filename);
      
      XLSX.writeFile(workbook, filepath);
      
      return {
        success: true,
        filename,
        filepath,
        size: fs.statSync(filepath).size
      };
    } catch (error) {
      console.error('Excel导出失败:', error);
      throw new Error('Excel导出失败: ' + error.message);
    }
  }

  // 计算评分统计
  calculateRatingStats(conversations) {
    let totalRating = 0;
    let ratedCount = 0;
    const distribution = { 1: 0, 2: 0, 3: 0, 4: 0, 5: 0 };
    let totalMessages = 0;

    conversations.forEach(conv => {
      if (conv.messages) {
        conv.messages.forEach(msg => {
          if (msg.message_type === 'assistant') {
            totalMessages++;
            if (msg.rating) {
              totalRating += msg.rating;
              ratedCount++;
              distribution[msg.rating] = (distribution[msg.rating] || 0) + 1;
            }
          }
        });
      }
    });

    return {
      avgRating: ratedCount > 0 ? totalRating / ratedCount : 0,
      ratedCount,
      totalMessages,
      ratingRate: totalMessages > 0 ? ratedCount / totalMessages : 0,
      distribution
    };
  }

  // 导出为PDF格式（使用Puppeteer）
  async exportToPDF(conversations, options = {}) {
    let browser = null;
    try {
      // 动态导入puppeteer
      const puppeteer = require('puppeteer');

      // 生成HTML内容
      const htmlContent = this.generateHTMLForPDF(conversations, options);

      // 启动浏览器
      browser = await puppeteer.launch({
        headless: 'new',
        args: ['--no-sandbox', '--disable-setuid-sandbox']
      });

      const page = await browser.newPage();
      await page.setContent(htmlContent, { waitUntil: 'networkidle0' });

      const filename = `conversations_${moment().format('YYYYMMDD_HHmmss')}.pdf`;
      const filepath = path.join(this.exportDir, filename);

      // 生成PDF
      await page.pdf({
        path: filepath,
        format: 'A4',
        margin: {
          top: '20mm',
          right: '15mm',
          bottom: '20mm',
          left: '15mm'
        },
        printBackground: true
      });

      return {
        success: true,
        filename,
        filepath,
        size: fs.statSync(filepath).size
      };
    } catch (error) {
      console.error('PDF导出失败:', error);
      throw new Error('PDF导出失败: ' + error.message);
    } finally {
      if (browser) {
        await browser.close();
      }
    }
  }

  // 生成PDF用的HTML内容
  generateHTMLForPDF(conversations, options) {
    const ratingStats = this.calculateRatingStats(conversations);

    return `
    <!DOCTYPE html>
    <html lang="zh-CN">
    <head>
        <meta charset="UTF-8">
        <title>AI对话记录导出</title>
        <style>
            body {
                font-family: 'Microsoft YaHei', Arial, sans-serif;
                line-height: 1.6;
                color: #333;
                margin: 0;
                padding: 20px;
            }
            .header {
                text-align: center;
                border-bottom: 2px solid #409eff;
                padding-bottom: 20px;
                margin-bottom: 30px;
            }
            .header h1 {
                color: #409eff;
                margin: 0;
            }
            .export-info {
                background: #f8f9fa;
                padding: 15px;
                border-radius: 5px;
                margin-bottom: 20px;
            }
            .stats-grid {
                display: grid;
                grid-template-columns: repeat(3, 1fr);
                gap: 15px;
                margin-bottom: 30px;
            }
            .stat-item {
                background: #e8f4fd;
                padding: 10px;
                border-radius: 5px;
                text-align: center;
            }
            .stat-number {
                font-size: 24px;
                font-weight: bold;
                color: #409eff;
            }
            .conversation {
                border: 1px solid #e4e7ed;
                border-radius: 8px;
                margin-bottom: 30px;
                page-break-inside: avoid;
            }
            .conversation-header {
                background: #409eff;
                color: white;
                padding: 15px;
                border-radius: 8px 8px 0 0;
            }
            .conversation-title {
                font-size: 18px;
                font-weight: bold;
                margin: 0;
            }
            .conversation-meta {
                font-size: 12px;
                opacity: 0.9;
                margin-top: 5px;
            }
            .message {
                padding: 15px;
                border-bottom: 1px solid #f0f0f0;
            }
            .message:last-child {
                border-bottom: none;
            }
            .message-header {
                display: flex;
                justify-content: space-between;
                align-items: center;
                margin-bottom: 10px;
            }
            .message-role {
                font-weight: bold;
                padding: 3px 8px;
                border-radius: 3px;
                font-size: 12px;
            }
            .message-role.user {
                background: #e8f5e8;
                color: #67c23a;
            }
            .message-role.assistant {
                background: #e8f4fd;
                color: #409eff;
            }
            .message-time {
                font-size: 11px;
                color: #909399;
            }
            .message-content {
                margin: 10px 0;
                white-space: pre-wrap;
            }
            .message-rating {
                background: #fef0e6;
                padding: 8px;
                border-radius: 4px;
                margin-top: 10px;
                font-size: 12px;
            }
            .rating-stars {
                color: #f7ba2a;
            }
            .page-break {
                page-break-before: always;
            }
        </style>
    </head>
    <body>
        <div class="header">
            <h1>🤖 AI对话记录导出</h1>
            <p>导出时间: ${moment().format('YYYY年MM月DD日 HH:mm:ss')}</p>
            <p>导出用户: ${options.user_info?.username || 'Unknown'}</p>
        </div>

        <div class="export-info">
            <h3>📊 导出统计</h3>
            <div class="stats-grid">
                <div class="stat-item">
                    <div class="stat-number">${conversations.length}</div>
                    <div>对话数量</div>
                </div>
                <div class="stat-item">
                    <div class="stat-number">${ratingStats.totalMessages}</div>
                    <div>消息总数</div>
                </div>
                <div class="stat-item">
                    <div class="stat-number">${ratingStats.avgRating.toFixed(1)}</div>
                    <div>平均评分</div>
                </div>
            </div>
        </div>

        ${conversations.map((conv, index) => `
        <div class="conversation ${index > 0 ? 'page-break' : ''}">
            <div class="conversation-header">
                <div class="conversation-title">${index + 1}. ${conv.title}</div>
                <div class="conversation-meta">
                    模型: ${conv.model_provider} | 创建: ${moment(conv.created_at).format('YYYY-MM-DD HH:mm')} | 消息: ${conv.message_count}条
                </div>
            </div>

            ${conv.messages ? conv.messages.map(msg => `
            <div class="message">
                <div class="message-header">
                    <span class="message-role ${msg.message_type}">${msg.message_type === 'user' ? '👤 用户' : '🤖 AI助手'}</span>
                    <span class="message-time">${moment(msg.created_at).format('MM-DD HH:mm:ss')}</span>
                </div>
                <div class="message-content">${msg.content}</div>
                ${msg.message_type === 'assistant' && msg.rating ? `
                <div class="message-rating">
                    <span class="rating-stars">${'⭐'.repeat(msg.rating)}</span> ${msg.rating}/5
                    ${msg.feedback ? `<br>反馈: ${msg.feedback}` : ''}
                    ${msg.response_time ? `<br>响应时间: ${msg.response_time}ms` : ''}
                </div>
                ` : ''}
            </div>
            `).join('') : ''}
        </div>
        `).join('')}
    </body>
    </html>
    `;
  }

  // 导出为Word格式（使用docx库）
  async exportToWord(conversations, options = {}) {
    try {
      // 动态导入docx
      const { Document, Packer, Paragraph, TextRun, HeadingLevel, Table, TableRow, TableCell, WidthType } = require('docx');

      const ratingStats = this.calculateRatingStats(conversations);

      // 创建文档
      const doc = new Document({
        sections: [{
          properties: {},
          children: [
            // 标题
            new Paragraph({
              text: "AI对话记录导出",
              heading: HeadingLevel.TITLE,
              alignment: 'center'
            }),

            // 导出信息
            new Paragraph({
              children: [
                new TextRun({
                  text: `导出时间: ${moment().format('YYYY年MM月DD日 HH:mm:ss')}`,
                  break: 1
                }),
                new TextRun({
                  text: `导出用户: ${options.user_info?.username || 'Unknown'}`,
                  break: 1
                }),
                new TextRun({
                  text: `对话数量: ${conversations.length}`,
                  break: 1
                }),
                new TextRun({
                  text: `平均评分: ${ratingStats.avgRating.toFixed(1)}`,
                  break: 1
                })
              ]
            }),

            new Paragraph({ text: "" }), // 空行

            // 统计表格
            new Paragraph({
              text: "📊 导出统计",
              heading: HeadingLevel.HEADING_1
            }),

            new Table({
              width: {
                size: 100,
                type: WidthType.PERCENTAGE
              },
              rows: [
                new TableRow({
                  children: [
                    new TableCell({ children: [new Paragraph("统计项")] }),
                    new TableCell({ children: [new Paragraph("数值")] })
                  ]
                }),
                new TableRow({
                  children: [
                    new TableCell({ children: [new Paragraph("总对话数")] }),
                    new TableCell({ children: [new Paragraph(conversations.length.toString())] })
                  ]
                }),
                new TableRow({
                  children: [
                    new TableCell({ children: [new Paragraph("总消息数")] }),
                    new TableCell({ children: [new Paragraph(ratingStats.totalMessages.toString())] })
                  ]
                }),
                new TableRow({
                  children: [
                    new TableCell({ children: [new Paragraph("已评分消息")] }),
                    new TableCell({ children: [new Paragraph(ratingStats.ratedCount.toString())] })
                  ]
                }),
                new TableRow({
                  children: [
                    new TableCell({ children: [new Paragraph("平均评分")] }),
                    new TableCell({ children: [new Paragraph(ratingStats.avgRating.toFixed(1))] })
                  ]
                })
              ]
            }),

            new Paragraph({ text: "" }), // 空行

            // 对话内容
            ...conversations.flatMap((conv, index) => {
              const convElements = [
                new Paragraph({
                  text: `${index + 1}. ${conv.title}`,
                  heading: HeadingLevel.HEADING_1
                }),

                new Paragraph({
                  children: [
                    new TextRun({
                      text: `模型: ${conv.model_provider} (${conv.model_name})`,
                      break: 1
                    }),
                    new TextRun({
                      text: `创建时间: ${moment(conv.created_at).format('YYYY-MM-DD HH:mm:ss')}`,
                      break: 1
                    }),
                    new TextRun({
                      text: `消息数量: ${conv.message_count}`,
                      break: 1
                    })
                  ]
                }),

                new Paragraph({ text: "" }) // 空行
              ];

              // 添加消息
              if (conv.messages && conv.messages.length > 0) {
                conv.messages.forEach((msg, msgIndex) => {
                  const role = msg.message_type === 'user' ? '👤 用户' : '🤖 AI助手';

                  convElements.push(
                    new Paragraph({
                      text: `${msgIndex + 1}. ${role}`,
                      heading: HeadingLevel.HEADING_2
                    }),

                    new Paragraph({
                      text: `时间: ${moment(msg.created_at).format('YYYY-MM-DD HH:mm:ss')}`
                    }),

                    new Paragraph({
                      text: msg.content
                    })
                  );

                  // 添加评分信息
                  if (msg.message_type === 'assistant' && msg.rating) {
                    convElements.push(
                      new Paragraph({
                        children: [
                          new TextRun({
                            text: `评分: ${'⭐'.repeat(msg.rating)} (${msg.rating}/5)`,
                            bold: true
                          })
                        ]
                      })
                    );

                    if (msg.feedback) {
                      convElements.push(
                        new Paragraph({
                          text: `反馈: ${msg.feedback}`
                        })
                      );
                    }

                    if (msg.response_time) {
                      convElements.push(
                        new Paragraph({
                          text: `响应时间: ${msg.response_time}ms`
                        })
                      );
                    }
                  }

                  convElements.push(new Paragraph({ text: "" })); // 空行
                });
              }

              return convElements;
            })
          ]
        }]
      });

      const filename = `conversations_${moment().format('YYYYMMDD_HHmmss')}.docx`;
      const filepath = path.join(this.exportDir, filename);

      // 生成Word文档
      const buffer = await Packer.toBuffer(doc);
      fs.writeFileSync(filepath, buffer);

      return {
        success: true,
        filename,
        filepath,
        size: fs.statSync(filepath).size
      };
    } catch (error) {
      console.error('Word导出失败:', error);
      throw new Error('Word导出失败: ' + error.message);
    }
  }

  // 清理过期的导出文件
  cleanupOldExports(maxAgeHours = 24) {
    try {
      const files = fs.readdirSync(this.exportDir);
      const now = Date.now();
      const maxAge = maxAgeHours * 60 * 60 * 1000;

      files.forEach(file => {
        const filepath = path.join(this.exportDir, file);
        const stats = fs.statSync(filepath);

        if (now - stats.mtime.getTime() > maxAge) {
          fs.unlinkSync(filepath);
          console.log(`清理过期导出文件: ${file}`);
        }
      });
    } catch (error) {
      console.error('清理导出文件失败:', error);
    }
  }
}

module.exports = ExportService;
