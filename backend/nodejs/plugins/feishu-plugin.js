/**
 * 飞书集成插件 - 基于现有架构设计
 * 支持消息推送、文档操作、日程管理等功能
 */

const axios = require('axios');
const crypto = require('crypto');
const { logger } = require('../middleware/error-handler');

class FeishuPlugin {
  constructor(config = {}) {
    this.config = {
      appId: config.appId || process.env.FEISHU_APP_ID,
      appSecret: config.appSecret || process.env.FEISHU_APP_SECRET,
      domain: config.domain || 'https://open.feishu.cn',
      webhookUrl: config.webhookUrl || process.env.FEISHU_WEBHOOK_URL,
      ...config
    };
    
    this.accessToken = null;
    this.tokenExpiry = null;
    this.enabled = false;
    
    // 插件元数据
    this.metadata = {
      id: 'feishu-integration',
      name: '飞书集成插件',
      version: '2.0.0',
      description: '集成飞书API，支持消息推送、文档操作、日程管理',
      category: 'collaboration',
      features: ['webhook', 'message', 'document', 'calendar', 'bot'],
      author: 'QMS AI Team',
      dependencies: ['axios', 'crypto']
    };
  }

  /**
   * 初始化插件
   */
  async initialize() {
    try {
      if (!this.config.appId || !this.config.appSecret) {
        throw new Error('飞书App ID和App Secret未配置');
      }

      // 获取访问令牌
      await this.getAccessToken();
      
      this.enabled = true;
      logger.info('✅ 飞书插件初始化成功');
      
      return {
        success: true,
        message: '飞书插件初始化成功',
        metadata: this.metadata
      };
    } catch (error) {
      logger.error('❌ 飞书插件初始化失败:', error);
      return {
        success: false,
        message: error.message,
        metadata: this.metadata
      };
    }
  }

  /**
   * 获取访问令牌
   */
  async getAccessToken() {
    try {
      const response = await axios.post(
        `${this.config.domain}/open-apis/auth/v3/app_access_token/internal`,
        {
          app_id: this.config.appId,
          app_secret: this.config.appSecret
        },
        {
          headers: {
            'Content-Type': 'application/json'
          },
          timeout: 10000
        }
      );

      if (response.data.code === 0) {
        this.accessToken = response.data.app_access_token;
        this.tokenExpiry = Date.now() + (response.data.expire - 300) * 1000; // 提前5分钟刷新
        logger.info('✅ 飞书访问令牌获取成功');
        return this.accessToken;
      } else {
        throw new Error(`获取访问令牌失败: ${response.data.msg}`);
      }
    } catch (error) {
      logger.error('❌ 获取飞书访问令牌失败:', error);
      throw error;
    }
  }

  /**
   * 检查并刷新访问令牌
   */
  async ensureValidToken() {
    if (!this.accessToken || Date.now() >= this.tokenExpiry) {
      await this.getAccessToken();
    }
    return this.accessToken;
  }

  /**
   * 发送消息到飞书
   * @param {Object} messageData - 消息数据
   */
  async sendMessage(messageData) {
    try {
      await this.ensureValidToken();

      const {
        receive_id_type = 'open_id',
        receive_id,
        msg_type = 'text',
        content,
        uuid = crypto.randomUUID()
      } = messageData;

      const response = await axios.post(
        `${this.config.domain}/open-apis/im/v1/messages`,
        {
          receive_id,
          msg_type,
          content: typeof content === 'string' ? content : JSON.stringify(content),
          uuid
        },
        {
          headers: {
            'Authorization': `Bearer ${this.accessToken}`,
            'Content-Type': 'application/json'
          },
          params: {
            receive_id_type
          },
          timeout: 10000
        }
      );

      if (response.data.code === 0) {
        logger.info('✅ 飞书消息发送成功');
        return {
          success: true,
          message_id: response.data.data.message_id,
          data: response.data.data
        };
      } else {
        throw new Error(`发送消息失败: ${response.data.msg}`);
      }
    } catch (error) {
      logger.error('❌ 飞书消息发送失败:', error);
      throw error;
    }
  }

  /**
   * 通过Webhook发送消息（自定义机器人）
   * @param {Object} messageData - 消息数据
   */
  async sendWebhookMessage(messageData) {
    try {
      if (!this.config.webhookUrl) {
        throw new Error('Webhook URL未配置');
      }

      const {
        msg_type = 'text',
        content
      } = messageData;

      const payload = {
        msg_type,
        content
      };

      const response = await axios.post(this.config.webhookUrl, payload, {
        headers: {
          'Content-Type': 'application/json'
        },
        timeout: 10000
      });

      if (response.data.StatusCode === 0) {
        logger.info('✅ 飞书Webhook消息发送成功');
        return {
          success: true,
          data: response.data
        };
      } else {
        throw new Error(`Webhook消息发送失败: ${response.data.StatusMessage}`);
      }
    } catch (error) {
      logger.error('❌ 飞书Webhook消息发送失败:', error);
      throw error;
    }
  }

  /**
   * 创建文档
   * @param {Object} docData - 文档数据
   */
  async createDocument(docData) {
    try {
      await this.ensureValidToken();

      const {
        title,
        content = '',
        folder_token = ''
      } = docData;

      const response = await axios.post(
        `${this.config.domain}/open-apis/docx/v1/documents`,
        {
          title,
          content,
          folder_token
        },
        {
          headers: {
            'Authorization': `Bearer ${this.accessToken}`,
            'Content-Type': 'application/json'
          },
          timeout: 15000
        }
      );

      if (response.data.code === 0) {
        logger.info('✅ 飞书文档创建成功');
        return {
          success: true,
          document_id: response.data.data.document.document_id,
          url: response.data.data.document.url,
          data: response.data.data
        };
      } else {
        throw new Error(`创建文档失败: ${response.data.msg}`);
      }
    } catch (error) {
      logger.error('❌ 飞书文档创建失败:', error);
      throw error;
    }
  }

  /**
   * 创建日程
   * @param {Object} eventData - 日程数据
   */
  async createCalendarEvent(eventData) {
    try {
      await this.ensureValidToken();

      const {
        summary,
        description = '',
        start_time,
        end_time,
        attendees = [],
        calendar_id = 'primary'
      } = eventData;

      const response = await axios.post(
        `${this.config.domain}/open-apis/calendar/v4/calendars/${calendar_id}/events`,
        {
          summary,
          description,
          start_time: {
            timestamp: start_time
          },
          end_time: {
            timestamp: end_time
          },
          attendee_ability: 'can_see_others',
          free_busy_status: 'busy',
          attendees: attendees.map(attendee => ({
            type: 'user',
            attendee_id: attendee.user_id || attendee.open_id,
            rsvp_status: 'needs_action'
          }))
        },
        {
          headers: {
            'Authorization': `Bearer ${this.accessToken}`,
            'Content-Type': 'application/json'
          },
          timeout: 10000
        }
      );

      if (response.data.code === 0) {
        logger.info('✅ 飞书日程创建成功');
        return {
          success: true,
          event_id: response.data.data.event.event_id,
          data: response.data.data
        };
      } else {
        throw new Error(`创建日程失败: ${response.data.msg}`);
      }
    } catch (error) {
      logger.error('❌ 飞书日程创建失败:', error);
      throw error;
    }
  }

  /**
   * 获取用户信息
   * @param {string} userId - 用户ID
   */
  async getUserInfo(userId) {
    try {
      await this.ensureValidToken();

      const response = await axios.get(
        `${this.config.domain}/open-apis/contact/v3/users/${userId}`,
        {
          headers: {
            'Authorization': `Bearer ${this.accessToken}`
          },
          params: {
            user_id_type: 'open_id'
          },
          timeout: 10000
        }
      );

      if (response.data.code === 0) {
        return {
          success: true,
          user: response.data.data.user
        };
      } else {
        throw new Error(`获取用户信息失败: ${response.data.msg}`);
      }
    } catch (error) {
      logger.error('❌ 获取飞书用户信息失败:', error);
      throw error;
    }
  }

  /**
   * 插件健康检查
   */
  async healthCheck() {
    try {
      if (!this.enabled) {
        return {
          status: 'disabled',
          message: '插件未启用'
        };
      }

      await this.ensureValidToken();
      
      return {
        status: 'healthy',
        message: '插件运行正常',
        token_valid: !!this.accessToken,
        token_expiry: this.tokenExpiry
      };
    } catch (error) {
      return {
        status: 'error',
        message: error.message
      };
    }
  }

  /**
   * 获取插件配置
   */
  getConfig() {
    return {
      ...this.metadata,
      enabled: this.enabled,
      config: {
        domain: this.config.domain,
        hasAppId: !!this.config.appId,
        hasAppSecret: !!this.config.appSecret,
        hasWebhookUrl: !!this.config.webhookUrl
      }
    };
  }

  /**
   * 更新插件配置
   */
  async updateConfig(newConfig) {
    try {
      this.config = { ...this.config, ...newConfig };
      
      // 如果更新了关键配置，重新初始化
      if (newConfig.appId || newConfig.appSecret) {
        await this.initialize();
      }
      
      return {
        success: true,
        message: '配置更新成功'
      };
    } catch (error) {
      logger.error('❌ 飞书插件配置更新失败:', error);
      return {
        success: false,
        message: error.message
      };
    }
  }
}

module.exports = FeishuPlugin;
