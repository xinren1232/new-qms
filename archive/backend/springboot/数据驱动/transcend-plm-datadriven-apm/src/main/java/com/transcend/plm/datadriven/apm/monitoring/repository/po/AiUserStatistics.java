package com.transcend.plm.datadriven.apm.monitoring.repository.po;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import lombok.Builder;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 用户问答统计表
 *
 * @author AI Assistant
 * @date 2025-01-31
 */
@TableName(value = "ai_user_statistics")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AiUserStatistics implements Serializable {

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
     * 用户ID
     */
    private String userId;

    /**
     * 用户姓名
     */
    private String userName;

    /**
     * 部门
     */
    private String department;

    /**
     * 统计日期
     */
    private LocalDate statisticDate;

    /**
     * 总提问数
     */
    private Integer totalQuestions;

    /**
     * 总会话数
     */
    private Integer totalConversations;

    /**
     * 平均响应时间(毫秒)
     */
    private BigDecimal avgResponseTime;

    /**
     * 总Token使用量
     */
    private Long totalTokensUsed;

    /**
     * 成功对话数
     */
    private Integer successfulChats;

    /**
     * 失败对话数
     */
    private Integer failedChats;

    /**
     * 点赞数
     */
    private Integer likeCount;

    /**
     * 点踩数
     */
    private Integer dislikeCount;

    /**
     * 平均满意度评分
     */
    private BigDecimal avgSatisfactionScore;

    /**
     * 最常用模型
     */
    private String mostUsedModel;

    /**
     * 使用高峰时段(0-23)
     */
    private Integer peakUsageHour;

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
     * 计算成功率
     */
    public BigDecimal getSuccessRate() {
        if (totalQuestions == null || totalQuestions == 0) {
            return BigDecimal.ZERO;
        }
        if (successfulChats == null) {
            return BigDecimal.ZERO;
        }
        return BigDecimal.valueOf(successfulChats)
                .divide(BigDecimal.valueOf(totalQuestions), 4, BigDecimal.ROUND_HALF_UP)
                .multiply(BigDecimal.valueOf(100));
    }

    /**
     * 计算满意度
     */
    public BigDecimal getSatisfactionRate() {
        int total = (likeCount != null ? likeCount : 0) + (dislikeCount != null ? dislikeCount : 0);
        if (total == 0) {
            return BigDecimal.ZERO;
        }
        return BigDecimal.valueOf(likeCount != null ? likeCount : 0)
                .divide(BigDecimal.valueOf(total), 4, BigDecimal.ROUND_HALF_UP)
                .multiply(BigDecimal.valueOf(100));
    }
}
