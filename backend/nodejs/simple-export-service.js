/**
 * 简化版导出服务 - 不依赖Puppeteer
 * 支持格式：Word, Excel, Markdown, JSON
 */

const fs = require('fs');
const path = require('path');

class SimpleExportService {
  constructor() {
    this.exportDir = path.join(__dirname, 'exports');
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

      const filename = `conversations_${this.getTimestamp()}.json`;
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
      markdown += `**导出时间**: ${new Date().toLocaleString('zh-CN')}\n`;
      markdown += `**导出用户**: ${options.user_info?.username || 'Unknown'}\n`;
      markdown += `**对话数量**: ${conversations.length}\n\n`;
      markdown += `---\n\n`;

      conversations.forEach((conv, index) => {
        markdown += `## ${index + 1}. ${conv.title}\n\n`;
        markdown += `- **模型**: ${conv.model_provider} (${conv.model_name})\n`;
        markdown += `- **创建时间**: ${new Date(conv.created_at).toLocaleString('zh-CN')}\n`;
        markdown += `- **消息数量**: ${conv.message_count}\n\n`;

        if (conv.messages && conv.messages.length > 0) {
          conv.messages.forEach((msg, msgIndex) => {
            const role = msg.message_type === 'user' ? '👤 用户' : '🤖 AI助手';
            markdown += `### ${msgIndex + 1}. ${role}\n\n`;
            markdown += `**时间**: ${new Date(msg.created_at).toLocaleString('zh-CN')}\n\n`;
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

      const filename = `conversations_${this.getTimestamp()}.md`;
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

  // 导出为Excel格式（使用简单的CSV格式代替）
  async exportToExcel(conversations, options = {}) {
    try {
      // 创建CSV内容
      let csvContent = '\uFEFF'; // BOM for UTF-8
      
      // 对话概览
      csvContent += '对话概览\n';
      csvContent += '序号,对话标题,AI模型,模型名称,创建时间,更新时间,消息数量,平均评分\n';
      
      conversations.forEach((conv, index) => {
        const avgRating = conv.messages ? 
          (conv.messages.filter(m => m.rating).reduce((sum, m) => sum + m.rating, 0) / 
           conv.messages.filter(m => m.rating).length || 0).toFixed(1) : '0.0';
        
        csvContent += `${index + 1},"${conv.title}","${conv.model_provider}","${conv.model_name}",`;
        csvContent += `"${new Date(conv.created_at).toLocaleString('zh-CN')}",`;
        csvContent += `"${new Date(conv.updated_at).toLocaleString('zh-CN')}",`;
        csvContent += `${conv.message_count},${avgRating}\n`;
      });
      
      csvContent += '\n\n详细消息\n';
      csvContent += '对话标题,消息类型,消息内容,发送时间,响应时间(ms),用户评分,用户反馈\n';
      
      conversations.forEach(conv => {
        if (conv.messages && conv.messages.length > 0) {
          conv.messages.forEach(msg => {
            csvContent += `"${conv.title}","${msg.message_type === 'user' ? '用户' : 'AI助手'}",`;
            csvContent += `"${msg.content.replace(/"/g, '""')}",`;
            csvContent += `"${new Date(msg.created_at).toLocaleString('zh-CN')}",`;
            csvContent += `"${msg.response_time || ''}","${msg.rating || ''}","${msg.feedback || ''}"\n`;
          });
        }
      });

      const filename = `conversations_${this.getTimestamp()}.csv`;
      const filepath = path.join(this.exportDir, filename);
      
      fs.writeFileSync(filepath, csvContent, 'utf8');
      
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

  // 导出为Word格式（使用HTML格式代替）
  async exportToWord(conversations, options = {}) {
    try {
      const ratingStats = this.calculateRatingStats(conversations);
      
      let htmlContent = `
<!DOCTYPE html>
<html lang="zh-CN">
<head>
    <meta charset="UTF-8">
    <title>AI对话记录导出</title>
    <style>
        body { font-family: 'Microsoft YaHei', Arial, sans-serif; line-height: 1.6; margin: 20px; }
        .header { text-align: center; border-bottom: 2px solid #409eff; padding-bottom: 20px; margin-bottom: 30px; }
        .stats { background: #f8f9fa; padding: 15px; border-radius: 5px; margin-bottom: 20px; }
        .conversation { border: 1px solid #e4e7ed; border-radius: 8px; margin-bottom: 30px; }
        .conversation-header { background: #409eff; color: white; padding: 15px; }
        .message { padding: 15px; border-bottom: 1px solid #f0f0f0; }
        .message-role { font-weight: bold; margin-bottom: 10px; }
        .user { background: #e8f5e8; }
        .assistant { background: #e8f4fd; }
        .rating { background: #fef0e6; padding: 8px; border-radius: 4px; margin-top: 10px; }
    </style>
</head>
<body>
    <div class="header">
        <h1>🤖 AI对话记录导出</h1>
        <p>导出时间: ${new Date().toLocaleString('zh-CN')}</p>
        <p>导出用户: ${options.user_info?.username || 'Unknown'}</p>
    </div>

    <div class="stats">
        <h3>📊 导出统计</h3>
        <p>对话数量: ${conversations.length}</p>
        <p>消息总数: ${ratingStats.totalMessages}</p>
        <p>平均评分: ${ratingStats.avgRating.toFixed(1)}</p>
    </div>

    ${conversations.map((conv, index) => `
    <div class="conversation">
        <div class="conversation-header">
            <h2>${index + 1}. ${conv.title}</h2>
            <p>模型: ${conv.model_provider} | 创建: ${new Date(conv.created_at).toLocaleString('zh-CN')} | 消息: ${conv.message_count}条</p>
        </div>
        
        ${conv.messages ? conv.messages.map(msg => `
        <div class="message ${msg.message_type}">
            <div class="message-role">${msg.message_type === 'user' ? '👤 用户' : '🤖 AI助手'} - ${new Date(msg.created_at).toLocaleString('zh-CN')}</div>
            <div>${msg.content}</div>
            ${msg.message_type === 'assistant' && msg.rating ? `
            <div class="rating">
                评分: ${'⭐'.repeat(msg.rating)} ${msg.rating}/5
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

      const filename = `conversations_${this.getTimestamp()}.html`;
      const filepath = path.join(this.exportDir, filename);
      
      fs.writeFileSync(filepath, htmlContent, 'utf8');
      
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

  // 导出为PDF格式（使用HTML格式代替）
  async exportToPDF(conversations, options = {}) {
    // 暂时使用HTML格式代替PDF
    return await this.exportToWord(conversations, options);
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

  // 获取时间戳
  getTimestamp() {
    const now = new Date();
    return now.getFullYear() + 
           String(now.getMonth() + 1).padStart(2, '0') + 
           String(now.getDate()).padStart(2, '0') + '_' +
           String(now.getHours()).padStart(2, '0') + 
           String(now.getMinutes()).padStart(2, '0') + 
           String(now.getSeconds()).padStart(2, '0');
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

module.exports = SimpleExportService;
