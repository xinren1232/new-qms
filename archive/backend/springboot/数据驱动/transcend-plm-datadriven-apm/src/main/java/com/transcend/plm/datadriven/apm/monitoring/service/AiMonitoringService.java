package com.transcend.plm.datadriven.apm.monitoring.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.transcend.plm.datadriven.apm.monitoring.repository.po.AiChatRecord;
import com.transcend.plm.datadriven.apm.monitoring.repository.po.AiChatFeedback;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * AI监控服务接口
 *
 * @author AI Assistant
 * @date 2025-01-31
 */
public interface AiMonitoringService {

    /**
     * 记录AI问答
     */
    void recordChatMessage(AiChatRecord chatRecord);

    /**
     * 记录用户反馈
     */
    void recordFeedback(AiChatFeedback feedback);

    /**
     * 分页查询问答记录
     */
    IPage<AiChatRecord> getChatRecordPage(Page<AiChatRecord> page, String userId,
                                         String modelProvider, String chatStatus,
                                         LocalDateTime startTime, LocalDateTime endTime);

    /**
     * 分页查询反馈记录
     */
    IPage<Map<String, Object>> getFeedbackPage(Page<AiChatFeedback> page, String feedbackType,
                                              String userId, LocalDateTime startTime,
                                              LocalDateTime endTime);

    /**
     * 获取系统总体统计
     */
    Map<String, Object> getOverallStatistics(LocalDateTime startTime, LocalDateTime endTime);

    /**
     * 获取用户使用统计
     */
    List<Map<String, Object>> getUserStatistics(LocalDateTime startTime, LocalDateTime endTime);

    /**
     * 获取模型使用统计
     */
    List<Map<String, Object>> getModelStatistics(LocalDateTime startTime, LocalDateTime endTime);

    /**
     * 获取反馈统计
     */
    Map<String, Object> getFeedbackStatistics(LocalDateTime startTime, LocalDateTime endTime);

    /**
     * 获取使用趋势数据
     */
    List<Map<String, Object>> getUsageTrend(LocalDateTime startTime, LocalDateTime endTime, String period);

    /**
     * 获取满意度趋势
     */
    List<Map<String, Object>> getSatisfactionTrend(LocalDateTime startTime, LocalDateTime endTime);

    /**
     * 获取响应时间分布
     */
    List<Map<String, Object>> getResponseTimeDistribution(LocalDateTime startTime, LocalDateTime endTime);

    /**
     * 获取热门问题
     */
    List<Map<String, Object>> getPopularQuestions(LocalDateTime startTime, LocalDateTime endTime, Integer limit);

    /**
     * 获取最近活跃用户
     */
    List<Map<String, Object>> getActiveUsers(LocalDateTime startTime, Integer limit);

    /**
     * 获取低评分反馈
     */
    List<Map<String, Object>> getLowRatingFeedback(LocalDateTime startTime, LocalDateTime endTime, Integer limit);

    /**
     * 获取改进建议
     */
    List<Map<String, Object>> getImprovementSuggestions(LocalDateTime startTime, LocalDateTime endTime, Integer limit);

    /**
     * 生成监控报告
     */
    Map<String, Object> generateMonitoringReport(LocalDateTime startTime, LocalDateTime endTime);

    /**
     * 获取实时监控数据
     */
    Map<String, Object> getRealTimeMetrics();

    /**
     * 获取用户详细统计
     */
    Map<String, Object> getUserDetailStatistics(String userId, LocalDateTime startTime, LocalDateTime endTime);

    /**
     * 获取模型详细统计
     */
    Map<String, Object> getModelDetailStatistics(String modelProvider, String modelName,
                                                LocalDateTime startTime, LocalDateTime endTime);

    /**
     * 执行统计数据更新
     */
    void updateStatistics(LocalDateTime startTime, LocalDateTime endTime);

    /**
     * 清理过期数据
     */
    void cleanupExpiredData(int retentionDays);
}
