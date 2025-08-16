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
 * 模型使用统计表
 *
 * @author AI Assistant
 * @date 2025-01-31
 */
@TableName(value = "ai_model_statistics")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AiModelStatistics implements Serializable {

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
     * 统计日期
     */
    private LocalDate statisticDate;

    /**
     * 总请求数
     */
    private Integer totalRequests;

    /**
     * 成功请求数
     */
    private Integer successfulRequests;

    /**
     * 失败请求数
     */
    private Integer failedRequests;

    /**
     * 平均响应时间(毫秒)
     */
    private BigDecimal avgResponseTime;

    /**
     * 总Token使用量
     */
    private Long totalTokensUsed;

    /**
     * 输入Token总数
     */
    private Long totalInputTokens;

    /**
     * 输出Token总数
     */
    private Long totalOutputTokens;

    /**
     * 总成本
     */
    private BigDecimal totalCost;

    /**
     * 独立用户数
     */
    private Integer uniqueUsers;

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
     * 使用高峰时段(0-23)
     */
    private Integer peakUsageHour;

    /**
     * 错误率
     */
    private BigDecimal errorRate;

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
        if (totalRequests == null || totalRequests == 0) {
            return BigDecimal.ZERO;
        }
        if (successfulRequests == null) {
            return BigDecimal.ZERO;
        }
        return BigDecimal.valueOf(successfulRequests)
                .divide(BigDecimal.valueOf(totalRequests), 4, BigDecimal.ROUND_HALF_UP)
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

    /**
     * 计算平均Token使用量
     */
    public BigDecimal getAvgTokensPerRequest() {
        if (totalRequests == null || totalRequests == 0) {
            return BigDecimal.ZERO;
        }
        if (totalTokensUsed == null) {
            return BigDecimal.ZERO;
        }
        return BigDecimal.valueOf(totalTokensUsed)
                .divide(BigDecimal.valueOf(totalRequests), 2, BigDecimal.ROUND_HALF_UP);
    }
}
