/**
 * 团队协作服务 - 使用成熟开源工具
 * 依赖: node-acl, socket.io, express-session
 */

class CollaborationService {
  constructor() {
    this.initializeCollaboration();
  }

  // 初始化协作服务
  async initializeCollaboration() {
    try {
      // 权限管理
      this.acl = require('acl');
      
      // 实时通信 (可选)
      // this.io = require('socket.io');
      
      console.log('✅ 协作服务初始化成功');
    } catch (error) {
      console.log('⚠️ 协作库未安装，使用简化版本');
      this.useSimplifiedCollaboration = true;
    }
  }

  // 权限定义
  getPermissionRoles() {
    return {
      'admin': {
        name: '系统管理员',
        permissions: ['*'], // 所有权限
        description: '拥有系统所有权限'
      },
      'manager': {
        name: '部门经理',
        permissions: [
          'conversation:read:team',
          'conversation:share:team', 
          'conversation:export:team',
          'analytics:view:team',
          'user:manage:team'
        ],
        description: '管理团队对话和用户'
      },
      'senior': {
        name: '高级用户',
        permissions: [
          'conversation:read:own',
          'conversation:share:public',
          'conversation:export:own',
          'analytics:view:own'
        ],
        description: '可分享对话给团队'
      },
      'user': {
        name: '普通用户',
        permissions: [
          'conversation:read:own',
          'conversation:export:own'
        ],
        description: '只能访问自己的对话'
      }
    };
  }

  // 检查用户权限
  async checkPermission(userId, userRole, resource, action, target = 'own') {
    if (this.useSimplifiedCollaboration) {
      return this.simplePermissionCheck(userRole, resource, action, target);
    }

    try {
      const permission = `${resource}:${action}:${target}`;
      const roles = this.getPermissionRoles();
      const rolePermissions = roles[userRole]?.permissions || [];
      
      // 检查是否有通配符权限
      if (rolePermissions.includes('*')) {
        return true;
      }
      
      // 检查具体权限
      return rolePermissions.includes(permission);
    } catch (error) {
      console.error('权限检查失败:', error);
      return false;
    }
  }

  // 简化版权限检查
  simplePermissionCheck(userRole, resource, action, target) {
    const permissions = {
      'admin': true,
      'manager': ['conversation', 'analytics', 'user'].includes(resource),
      'senior': resource === 'conversation' || (resource === 'analytics' && target === 'own'),
      'user': resource === 'conversation' && target === 'own'
    };
    
    return permissions[userRole] || false;
  }

  // 分享对话给团队
  async shareConversation(conversationId, fromUserId, shareOptions) {
    try {
      const shareRecord = {
        id: 'share_' + Date.now(),
        conversation_id: conversationId,
        shared_by: fromUserId,
        shared_at: new Date().toISOString(),
        share_type: shareOptions.type || 'team', // team, public, specific
        target_users: shareOptions.target_users || [],
        target_teams: shareOptions.target_teams || [],
        permissions: shareOptions.permissions || ['read'],
        message: shareOptions.message || '',
        expires_at: shareOptions.expires_at || null
      };

      // 这里应该保存到数据库
      // await this.saveShareRecord(shareRecord);

      // 发送通知 (如果有实时通信)
      // this.notifyUsers(shareRecord);

      return {
        success: true,
        share_id: shareRecord.id,
        message: '对话分享成功'
      };
    } catch (error) {
      console.error('分享对话失败:', error);
      throw new Error('分享对话失败: ' + error.message);
    }
  }

  // 获取团队统计
  async getTeamStatistics(teamId, timeRange = '30d') {
    try {
      // 模拟团队统计数据
      const stats = {
        team_info: {
          id: teamId,
          name: '质量管理团队',
          member_count: 12,
          active_members: 8
        },
        conversation_stats: {
          total_conversations: 156,
          total_messages: 892,
          avg_conversations_per_user: 13,
          most_active_user: 'zhang_san',
          most_active_topics: [
            { topic: '质量体系', count: 45 },
            { topic: '质量控制', count: 38 },
            { topic: '质量改进', count: 32 }
          ]
        },
        sharing_stats: {
          total_shares: 23,
          most_shared_conversations: [
            { title: 'ISO 9001实施指南', shares: 8 },
            { title: '质量成本分析方法', shares: 6 },
            { title: 'PDCA循环应用案例', shares: 5 }
          ]
        },
        rating_stats: {
          team_avg_rating: 4.2,
          rating_distribution: { 1: 2, 2: 5, 3: 18, 4: 45, 5: 38 },
          top_rated_conversations: [
            { title: '六西格玛项目实施', rating: 4.8 },
            { title: '供应商质量管理', rating: 4.6 },
            { title: '客户投诉处理流程', rating: 4.5 }
          ]
        },
        time_analysis: {
          peak_hours: [9, 10, 14, 15],
          peak_days: [1, 2, 3], // 周一到周三
          monthly_trend: this.generateMonthlyTrend()
        }
      };

      return stats;
    } catch (error) {
      console.error('获取团队统计失败:', error);
      throw new Error('获取团队统计失败: ' + error.message);
    }
  }

  // 生成月度趋势数据
  generateMonthlyTrend() {
    const trend = [];
    const now = new Date();
    
    for (let i = 29; i >= 0; i--) {
      const date = new Date(now);
      date.setDate(date.getDate() - i);
      
      trend.push({
        date: date.toISOString().split('T')[0],
        conversations: Math.floor(Math.random() * 10) + 2,
        messages: Math.floor(Math.random() * 50) + 10,
        active_users: Math.floor(Math.random() * 8) + 3
      });
    }
    
    return trend;
  }

  // 获取用户可访问的对话
  async getAccessibleConversations(userId, userRole, teamId) {
    try {
      const conversations = [];
      
      // 根据权限获取不同范围的对话
      if (await this.checkPermission(userId, userRole, 'conversation', 'read', 'team')) {
        // 可以访问团队对话
        conversations.push(...await this.getTeamConversations(teamId));
      }
      
      if (await this.checkPermission(userId, userRole, 'conversation', 'read', 'own')) {
        // 可以访问自己的对话
        conversations.push(...await this.getUserConversations(userId));
      }

      // 获取分享给用户的对话
      conversations.push(...await this.getSharedConversations(userId));

      // 去重并排序
      const uniqueConversations = this.deduplicateConversations(conversations);
      return this.sortConversationsByAccess(uniqueConversations, userId);
    } catch (error) {
      console.error('获取可访问对话失败:', error);
      return [];
    }
  }

  // 获取团队对话 (模拟)
  async getTeamConversations(teamId) {
    return [
      {
        id: 'team_conv_1',
        title: '团队质量改进讨论',
        shared_by: 'manager_001',
        access_type: 'team',
        created_at: '2025-01-15T10:00:00Z'
      },
      {
        id: 'team_conv_2', 
        title: 'ISO标准实施经验分享',
        shared_by: 'senior_002',
        access_type: 'team',
        created_at: '2025-01-14T15:30:00Z'
      }
    ];
  }

  // 获取用户对话 (模拟)
  async getUserConversations(userId) {
    return [
      {
        id: 'user_conv_1',
        title: '个人质量学习笔记',
        user_id: userId,
        access_type: 'own',
        created_at: '2025-01-16T09:00:00Z'
      }
    ];
  }

  // 获取分享对话 (模拟)
  async getSharedConversations(userId) {
    return [
      {
        id: 'shared_conv_1',
        title: '质量成本分析案例',
        shared_by: 'expert_001',
        access_type: 'shared',
        shared_at: '2025-01-13T14:00:00Z'
      }
    ];
  }

  // 去重对话
  deduplicateConversations(conversations) {
    const seen = new Set();
    return conversations.filter(conv => {
      if (seen.has(conv.id)) {
        return false;
      }
      seen.add(conv.id);
      return true;
    });
  }

  // 按访问权限排序
  sortConversationsByAccess(conversations, userId) {
    const accessPriority = { 'own': 3, 'team': 2, 'shared': 1 };
    
    return conversations.sort((a, b) => {
      // 首先按访问类型排序
      const priorityDiff = (accessPriority[b.access_type] || 0) - (accessPriority[a.access_type] || 0);
      if (priorityDiff !== 0) return priorityDiff;
      
      // 然后按时间排序
      return new Date(b.created_at || b.shared_at) - new Date(a.created_at || a.shared_at);
    });
  }

  // 创建团队
  async createTeam(teamData, creatorId) {
    try {
      const team = {
        id: 'team_' + Date.now(),
        name: teamData.name,
        description: teamData.description || '',
        created_by: creatorId,
        created_at: new Date().toISOString(),
        members: [
          {
            user_id: creatorId,
            role: 'admin',
            joined_at: new Date().toISOString()
          }
        ],
        settings: {
          allow_sharing: teamData.allow_sharing !== false,
          require_approval: teamData.require_approval || false,
          max_members: teamData.max_members || 50
        }
      };

      // 这里应该保存到数据库
      // await this.saveTeam(team);

      return {
        success: true,
        team_id: team.id,
        message: '团队创建成功'
      };
    } catch (error) {
      console.error('创建团队失败:', error);
      throw new Error('创建团队失败: ' + error.message);
    }
  }

  // 邀请用户加入团队
  async inviteUserToTeam(teamId, userId, invitedBy, role = 'user') {
    try {
      const invitation = {
        id: 'invite_' + Date.now(),
        team_id: teamId,
        user_id: userId,
        invited_by: invitedBy,
        role: role,
        status: 'pending',
        created_at: new Date().toISOString(),
        expires_at: new Date(Date.now() + 7 * 24 * 60 * 60 * 1000).toISOString() // 7天后过期
      };

      // 这里应该保存到数据库并发送通知
      // await this.saveInvitation(invitation);
      // await this.sendInvitationNotification(invitation);

      return {
        success: true,
        invitation_id: invitation.id,
        message: '邀请发送成功'
      };
    } catch (error) {
      console.error('邀请用户失败:', error);
      throw new Error('邀请用户失败: ' + error.message);
    }
  }

  // 获取用户团队列表
  async getUserTeams(userId) {
    try {
      // 模拟用户团队数据
      return [
        {
          id: 'team_001',
          name: '质量管理团队',
          role: 'manager',
          member_count: 12,
          recent_activity: '2小时前'
        },
        {
          id: 'team_002',
          name: '产品质量小组',
          role: 'user',
          member_count: 8,
          recent_activity: '1天前'
        }
      ];
    } catch (error) {
      console.error('获取用户团队失败:', error);
      return [];
    }
  }
}

module.exports = CollaborationService;
