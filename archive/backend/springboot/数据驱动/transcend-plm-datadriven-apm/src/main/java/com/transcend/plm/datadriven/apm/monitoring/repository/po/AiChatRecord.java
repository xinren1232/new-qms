package com.transcend.plm.datadriven.apm.monitoring.repository.po;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import lombok.Builder;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * AI问答记录表
 *
 * @author AI Assistant
 * @date 2025-01-31
 */
@TableName(value = "ai_chat_record")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AiChatRecord implements Serializable {

    /**
     * 主键ID
     */
    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 业务ID
     */
    private String bid;

    /**
     * 会话ID
     */
    private String conversationId;

    /**
     * 消息ID
     */
    private String messageId;

    /**
     * 用户ID/工号
     */
    private String userId;

    /**
     * 用户姓名
     */
    private String userName;

    /**
     * 用户问题
     */
    private String userMessage;

    /**
     * AI回答
     */
    private String aiResponse;

    /**
     * 模型提供商
     */
    private String modelProvider;

    /**
     * 模型名称
     */
    private String modelName;

    /**
     * 模型版本
     */
    private String modelVersion;

    /**
     * 响应时间(毫秒)
     */
    private Integer responseTime;

    /**
     * Token使用情况
     */
    private String tokenUsage;

    /**
     * 温度参数
     */
    private BigDecimal temperature;

    /**
     * 最大Token数
     */
    private Integer maxTokens;

    /**
     * 对话状态: SUCCESS, FAILED, TIMEOUT
     */
    private String chatStatus;

    /**
     * 错误信息
     */
    private String errorMessage;

    /**
     * 会话标识
     */
    private String sessionId;

    /**
     * 用户IP地址
     */
    private String ipAddress;

    /**
     * 用户代理
     */
    private String userAgent;

    /**
     * 创建时间
     */
    private LocalDateTime createdTime;

    /**
     * 更新时间
     */
    private LocalDateTime updatedTime;

    /**
     * 创建人
     */
    private String createdBy;

    /**
     * 更新人
     */
    private String updatedBy;

    /**
     * 删除标志(0-未删除；1-已删除)
     */
    @TableLogic
    private Integer deleteFlag;

    /**
     * 租户ID
     */
    private Long tenantId;

    @TableField(exist = false)
    private static final long serialVersionUID = 1L;

    /**
     * 聊天状态枚举
     */
    public enum ChatStatus {
        SUCCESS("SUCCESS", "成功"),
        FAILED("FAILED", "失败"),
        TIMEOUT("TIMEOUT", "超时");

        private final String code;
        private final String description;

        ChatStatus(String code, String description) {
            this.code = code;
            this.description = description;
        }

        public String getCode() {
            return code;
        }

        public String getDescription() {
            return description;
        }
    }
}
