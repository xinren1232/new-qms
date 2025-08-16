package com.transcend.plm.datadriven.apm.monitoring.repository.po;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import lombok.Builder;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * AI问答质量评价表
 *
 * @author AI Assistant
 * @date 2025-01-31
 */
@TableName(value = "ai_chat_feedback")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AiChatFeedback implements Serializable {

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
     * 问答记录ID
     */
    private Long chatRecordId;

    /**
     * 消息ID
     */
    private String messageId;

    /**
     * 评价用户ID
     */
    private String userId;

    /**
     * 反馈类型: LIKE, DISLIKE
     */
    private String feedbackType;

    /**
     * 评分(1-5分)
     */
    private Integer feedbackScore;

    /**
     * 反馈原因
     */
    private String feedbackReason;

    /**
     * 详细评价内容
     */
    private String feedbackComment;

    /**
     * 反馈标签
     */
    private String feedbackTags;

    /**
     * 是否有帮助(0-否；1-是)
     */
    private Integer isHelpful;

    /**
     * 改进建议
     */
    private String improvementSuggestion;

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
     * 反馈类型枚举
     */
    public enum FeedbackType {
        LIKE("LIKE", "点赞"),
        DISLIKE("DISLIKE", "点踩");

        private final String code;
        private final String description;

        FeedbackType(String code, String description) {
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

    /**
     * 反馈原因枚举
     */
    public enum FeedbackReason {
        ACCURATE("ACCURATE", "回答准确"),
        HELPFUL("HELPFUL", "很有帮助"),
        CLEAR("CLEAR", "表达清晰"),
        COMPLETE("COMPLETE", "内容完整"),
        INACCURATE("INACCURATE", "回答不准确"),
        UNHELPFUL("UNHELPFUL", "没有帮助"),
        UNCLEAR("UNCLEAR", "表达不清"),
        INCOMPLETE("INCOMPLETE", "内容不完整"),
        IRRELEVANT("IRRELEVANT", "答非所问"),
        TOO_SLOW("TOO_SLOW", "响应太慢");

        private final String code;
        private final String description;

        FeedbackReason(String code, String description) {
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
