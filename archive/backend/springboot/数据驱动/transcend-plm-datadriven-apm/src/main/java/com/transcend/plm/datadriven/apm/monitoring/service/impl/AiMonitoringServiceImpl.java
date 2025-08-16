package com.transcend.plm.datadriven.apm.monitoring.service.impl;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.transcend.plm.datadriven.apm.monitoring.repository.mapper.AiChatRecordMapper;
import com.transcend.plm.datadriven.apm.monitoring.repository.mapper.AiChatFeedbackMapper;
import com.transcend.plm.datadriven.apm.monitoring.repository.po.AiChatRecord;
import com.transcend.plm.datadriven.apm.monitoring.repository.po.AiChatFeedback;
import com.transcend.plm.datadriven.apm.monitoring.service.AiMonitoringService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;

/**
 * AI监控服务实现类
 *
 * @author AI Assistant
 * @date 2025-01-31
 */
@Slf4j
@Service
public class AiMonitoringServiceImpl implements AiMonitoringService {

    @Autowired
    private AiChatRecordMapper chatRecordMapper;

    @Autowired
    private AiChatFeedbackMapper feedbackMapper;

    @Override
    @Transactional
    public void recordChatMessage(AiChatRecord chatRecord) {
        try {
            // 设置默认值
            if (chatRecord.getBid() == null) {
                chatRecord.setBid(UUID.randomUUID().toString().replace("-", ""));
            }
            if (chatRecord.getCreatedTime() == null) {
                chatRecord.setCreatedTime(LocalDateTime.now());
            }
            if (chatRecord.getChatStatus() == null) {
                chatRecord.setChatStatus(AiChatRecord.ChatStatus.SUCCESS.getCode());
            }
            if (chatRecord.getTenantId() == null) {
                chatRecord.setTenantId(100L);
            }
            
            chatRecordMapper.insert(chatRecord);
            log.info("记录AI问答成功: messageId={}, userId={}", chatRecord.getMessageId(), chatRecord.getUserId());
        } catch (Exception e) {
            log.error("记录AI问答失败: messageId={}, userId={}", chatRecord.getMessageId(), chatRecord.getUserId(), e);
            throw new RuntimeException("记录AI问答失败", e);
        }
    }

    @Override
    @Transactional
    public void recordFeedback(AiChatFeedback feedback) {
        try {
            // 设置默认值
            if (feedback.getBid() == null) {
                feedback.setBid(UUID.randomUUID().toString().replace("-", ""));
            }
            if (feedback.getCreatedTime() == null) {
                feedback.setCreatedTime(LocalDateTime.now());
            }
            if (feedback.getTenantId() == null) {
                feedback.setTenantId(100L);
            }
            
            feedbackMapper.insert(feedback);
            log.info("记录用户反馈成功: messageId={}, userId={}, feedbackType={}", 
                    feedback.getMessageId(), feedback.getUserId(), feedback.getFeedbackType());
        } catch (Exception e) {
            log.error("记录用户反馈失败: messageId={}, userId={}", feedback.getMessageId(), feedback.getUserId(), e);
            throw new RuntimeException("记录用户反馈失败", e);
        }
    }

    @Override
    public IPage<AiChatRecord> getChatRecordPage(Page<AiChatRecord> page, String userId, 
                                               String modelProvider, String chatStatus,
                                               LocalDateTime startTime, LocalDateTime endTime) {
        return chatRecordMapper.selectChatRecordPage(page, userId, modelProvider, chatStatus, startTime, endTime);
    }

    @Override
    public IPage<Map<String, Object>> getFeedbackPage(Page<AiChatFeedback> page, String feedbackType,
                                                     String userId, LocalDateTime startTime, 
                                                     LocalDateTime endTime) {
        return feedbackMapper.selectFeedbackPage(page, feedbackType, userId, startTime, endTime);
    }

    @Override
    public Map<String, Object> getOverallStatistics(LocalDateTime startTime, LocalDateTime endTime) {
        Map<String, Object> statistics = chatRecordMapper.getOverallStatistics(startTime, endTime);
        
        // 添加反馈统计
        List<Map<String, Object>> feedbackStats = feedbackMapper.countByFeedbackType(startTime, endTime);
        int totalLikes = 0;
        int totalDislikes = 0;
        for (Map<String, Object> stat : feedbackStats) {
            String type = (String) stat.get("feedback_type");
            int count = ((Number) stat.get("count")).intValue();
            if ("LIKE".equals(type)) {
                totalLikes = count;
            } else if ("DISLIKE".equals(type)) {
                totalDislikes = count;
            }
        }
        
        statistics.put("total_likes", totalLikes);
        statistics.put("total_dislikes", totalDislikes);
        statistics.put("total_feedback", totalLikes + totalDislikes);
        
        // 计算满意度
        if (totalLikes + totalDislikes > 0) {
            double satisfactionRate = (double) totalLikes / (totalLikes + totalDislikes) * 100;
            statistics.put("satisfaction_rate", Math.round(satisfactionRate * 100.0) / 100.0);
        } else {
            statistics.put("satisfaction_rate", 0.0);
        }
        
        return statistics;
    }

    @Override
    public List<Map<String, Object>> getUserStatistics(LocalDateTime startTime, LocalDateTime endTime) {
        List<Map<String, Object>> userStats = chatRecordMapper.countByUser(startTime, endTime);
        List<Map<String, Object>> feedbackStats = feedbackMapper.countByUser(startTime, endTime);
        
        // 合并用户统计和反馈统计
        Map<String, Map<String, Object>> userStatsMap = new HashMap<>();
        for (Map<String, Object> stat : userStats) {
            String userId = (String) stat.get("user_id");
            userStatsMap.put(userId, new HashMap<>(stat));
        }
        
        for (Map<String, Object> stat : feedbackStats) {
            String userId = (String) stat.get("user_id");
            Map<String, Object> userStat = userStatsMap.get(userId);
            if (userStat != null) {
                userStat.putAll(stat);
            }
        }
        
        return new ArrayList<>(userStatsMap.values());
    }

    @Override
    public List<Map<String, Object>> getModelStatistics(LocalDateTime startTime, LocalDateTime endTime) {
        List<Map<String, Object>> modelStats = chatRecordMapper.countByModel(startTime, endTime);
        List<Map<String, Object>> feedbackStats = feedbackMapper.countByModel(startTime, endTime);
        
        // 合并模型统计和反馈统计
        Map<String, Map<String, Object>> modelStatsMap = new HashMap<>();
        for (Map<String, Object> stat : modelStats) {
            String key = stat.get("model_provider") + "_" + stat.get("model_name");
            modelStatsMap.put(key, new HashMap<>(stat));
        }
        
        for (Map<String, Object> stat : feedbackStats) {
            String key = stat.get("model_provider") + "_" + stat.get("model_name");
            Map<String, Object> modelStat = modelStatsMap.get(key);
            if (modelStat != null) {
                modelStat.putAll(stat);
            }
        }
        
        return new ArrayList<>(modelStatsMap.values());
    }

    @Override
    public Map<String, Object> getFeedbackStatistics(LocalDateTime startTime, LocalDateTime endTime) {
        Map<String, Object> result = new HashMap<>();
        
        // 反馈类型分布
        List<Map<String, Object>> typeStats = feedbackMapper.countByFeedbackType(startTime, endTime);
        result.put("feedback_type_distribution", typeStats);
        
        // 反馈原因分布
        List<Map<String, Object>> reasonStats = feedbackMapper.countByFeedbackReason(startTime, endTime);
        result.put("feedback_reason_distribution", reasonStats);
        
        // 满意度趋势
        List<Map<String, Object>> satisfactionTrend = feedbackMapper.getSatisfactionTrend(startTime, endTime);
        result.put("satisfaction_trend", satisfactionTrend);
        
        return result;
    }

    @Override
    public List<Map<String, Object>> getUsageTrend(LocalDateTime startTime, LocalDateTime endTime, String period) {
        // 根据period参数返回不同粒度的趋势数据
        // 这里简化实现，返回按小时统计的数据
        return chatRecordMapper.countByHour(startTime, endTime);
    }

    @Override
    public List<Map<String, Object>> getSatisfactionTrend(LocalDateTime startTime, LocalDateTime endTime) {
        return feedbackMapper.getSatisfactionTrend(startTime, endTime);
    }

    @Override
    public List<Map<String, Object>> getResponseTimeDistribution(LocalDateTime startTime, LocalDateTime endTime) {
        return chatRecordMapper.countByResponseTime(startTime, endTime);
    }

    @Override
    public List<Map<String, Object>> getPopularQuestions(LocalDateTime startTime, LocalDateTime endTime, Integer limit) {
        return chatRecordMapper.getPopularQuestions(startTime, endTime, limit);
    }

    @Override
    public List<Map<String, Object>> getActiveUsers(LocalDateTime startTime, Integer limit) {
        return chatRecordMapper.getActiveUsers(startTime, limit);
    }

    @Override
    public List<Map<String, Object>> getLowRatingFeedback(LocalDateTime startTime, LocalDateTime endTime, Integer limit) {
        return feedbackMapper.getLowRatingFeedback(startTime, endTime, limit);
    }

    @Override
    public List<Map<String, Object>> getImprovementSuggestions(LocalDateTime startTime, LocalDateTime endTime, Integer limit) {
        return feedbackMapper.getImprovementSuggestions(startTime, endTime, limit);
    }

    @Override
    public Map<String, Object> generateMonitoringReport(LocalDateTime startTime, LocalDateTime endTime) {
        Map<String, Object> report = new HashMap<>();
        
        // 总体统计
        report.put("overall_statistics", getOverallStatistics(startTime, endTime));
        
        // 用户统计
        report.put("user_statistics", getUserStatistics(startTime, endTime));
        
        // 模型统计
        report.put("model_statistics", getModelStatistics(startTime, endTime));
        
        // 反馈统计
        report.put("feedback_statistics", getFeedbackStatistics(startTime, endTime));
        
        // 趋势数据
        report.put("usage_trend", getUsageTrend(startTime, endTime, "hour"));
        report.put("satisfaction_trend", getSatisfactionTrend(startTime, endTime));
        
        // 响应时间分布
        report.put("response_time_distribution", getResponseTimeDistribution(startTime, endTime));
        
        // 热门问题
        report.put("popular_questions", getPopularQuestions(startTime, endTime, 10));
        
        // 活跃用户
        report.put("active_users", getActiveUsers(startTime, 10));
        
        // 低评分反馈
        report.put("low_rating_feedback", getLowRatingFeedback(startTime, endTime, 10));
        
        // 改进建议
        report.put("improvement_suggestions", getImprovementSuggestions(startTime, endTime, 10));
        
        report.put("report_generated_time", LocalDateTime.now());
        report.put("report_period", Map.of("start_time", startTime, "end_time", endTime));
        
        return report;
    }

    @Override
    public Map<String, Object> getRealTimeMetrics() {
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime oneHourAgo = now.minusHours(1);
        
        return getOverallStatistics(oneHourAgo, now);
    }

    @Override
    public Map<String, Object> getUserDetailStatistics(String userId, LocalDateTime startTime, LocalDateTime endTime) {
        // 实现用户详细统计逻辑
        Map<String, Object> result = new HashMap<>();
        // TODO: 实现具体逻辑
        return result;
    }

    @Override
    public Map<String, Object> getModelDetailStatistics(String modelProvider, String modelName, 
                                                       LocalDateTime startTime, LocalDateTime endTime) {
        // 实现模型详细统计逻辑
        Map<String, Object> result = new HashMap<>();
        // TODO: 实现具体逻辑
        return result;
    }

    @Override
    @Transactional
    public void updateStatistics(LocalDateTime startTime, LocalDateTime endTime) {
        // 实现统计数据更新逻辑
        log.info("更新统计数据: {} - {}", startTime, endTime);
        // TODO: 实现具体逻辑
    }

    @Override
    @Transactional
    public void cleanupExpiredData(int retentionDays) {
        // 实现过期数据清理逻辑
        LocalDateTime cutoffTime = LocalDateTime.now().minusDays(retentionDays);
        log.info("清理过期数据，截止时间: {}", cutoffTime);
        // TODO: 实现具体逻辑
    }
}
