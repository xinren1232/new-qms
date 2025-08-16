package com.transcend.plm.datadriven.apm.monitoring.repository.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.transcend.plm.datadriven.apm.monitoring.repository.po.AiChatFeedback;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * AI问答质量评价Mapper
 *
 * @author AI Assistant
 * @date 2025-01-31
 */
@Mapper
public interface AiChatFeedbackMapper extends BaseMapper<AiChatFeedback> {

    /**
     * 分页查询反馈记录
     */
    @Select("SELECT f.*, r.user_message, r.ai_response, r.model_provider, r.model_name " +
            "FROM ai_chat_feedback f " +
            "LEFT JOIN ai_chat_record r ON f.chat_record_id = r.id " +
            "WHERE f.delete_flag = 0 " +
            "AND (#{feedbackType} IS NULL OR f.feedback_type = #{feedbackType}) " +
            "AND (#{userId} IS NULL OR f.user_id = #{userId}) " +
            "AND (#{startTime} IS NULL OR f.created_time >= #{startTime}) " +
            "AND (#{endTime} IS NULL OR f.created_time <= #{endTime}) " +
            "ORDER BY f.created_time DESC")
    IPage<Map<String, Object>> selectFeedbackPage(Page<AiChatFeedback> page,
                                                  @Param("feedbackType") String feedbackType,
                                                  @Param("userId") String userId,
                                                  @Param("startTime") LocalDateTime startTime,
                                                  @Param("endTime") LocalDateTime endTime);

    /**
     * 统计反馈类型分布
     */
    @Select("SELECT feedback_type, COUNT(*) as count FROM ai_chat_feedback " +
            "WHERE delete_flag = 0 " +
            "AND (#{startTime} IS NULL OR created_time >= #{startTime}) " +
            "AND (#{endTime} IS NULL OR created_time <= #{endTime}) " +
            "GROUP BY feedback_type")
    List<Map<String, Object>> countByFeedbackType(@Param("startTime") LocalDateTime startTime,
                                                  @Param("endTime") LocalDateTime endTime);

    /**
     * 统计反馈原因分布
     */
    @Select("SELECT feedback_reason, COUNT(*) as count FROM ai_chat_feedback " +
            "WHERE delete_flag = 0 AND feedback_reason IS NOT NULL " +
            "AND (#{startTime} IS NULL OR created_time >= #{startTime}) " +
            "AND (#{endTime} IS NULL OR created_time <= #{endTime}) " +
            "GROUP BY feedback_reason ORDER BY count DESC")
    List<Map<String, Object>> countByFeedbackReason(@Param("startTime") LocalDateTime startTime,
                                                    @Param("endTime") LocalDateTime endTime);

    /**
     * 按模型统计反馈情况
     */
    @Select("SELECT r.model_provider, r.model_name, " +
            "SUM(CASE WHEN f.feedback_type = 'LIKE' THEN 1 ELSE 0 END) as like_count, " +
            "SUM(CASE WHEN f.feedback_type = 'DISLIKE' THEN 1 ELSE 0 END) as dislike_count, " +
            "COUNT(*) as total_feedback, " +
            "AVG(f.feedback_score) as avg_score " +
            "FROM ai_chat_feedback f " +
            "LEFT JOIN ai_chat_record r ON f.chat_record_id = r.id " +
            "WHERE f.delete_flag = 0 " +
            "AND (#{startTime} IS NULL OR f.created_time >= #{startTime}) " +
            "AND (#{endTime} IS NULL OR f.created_time <= #{endTime}) " +
            "GROUP BY r.model_provider, r.model_name " +
            "ORDER BY total_feedback DESC")
    List<Map<String, Object>> countByModel(@Param("startTime") LocalDateTime startTime,
                                          @Param("endTime") LocalDateTime endTime);

    /**
     * 获取满意度趋势
     */
    @Select("SELECT DATE(created_time) as date, " +
            "SUM(CASE WHEN feedback_type = 'LIKE' THEN 1 ELSE 0 END) as like_count, " +
            "SUM(CASE WHEN feedback_type = 'DISLIKE' THEN 1 ELSE 0 END) as dislike_count, " +
            "COUNT(*) as total_feedback " +
            "FROM ai_chat_feedback " +
            "WHERE delete_flag = 0 " +
            "AND (#{startTime} IS NULL OR created_time >= #{startTime}) " +
            "AND (#{endTime} IS NULL OR created_time <= #{endTime}) " +
            "GROUP BY DATE(created_time) " +
            "ORDER BY date")
    List<Map<String, Object>> getSatisfactionTrend(@Param("startTime") LocalDateTime startTime,
                                                   @Param("endTime") LocalDateTime endTime);

    /**
     * 获取用户反馈统计
     */
    @Select("SELECT user_id, " +
            "SUM(CASE WHEN feedback_type = 'LIKE' THEN 1 ELSE 0 END) as like_count, " +
            "SUM(CASE WHEN feedback_type = 'DISLIKE' THEN 1 ELSE 0 END) as dislike_count, " +
            "COUNT(*) as total_feedback, " +
            "AVG(feedback_score) as avg_score " +
            "FROM ai_chat_feedback " +
            "WHERE delete_flag = 0 " +
            "AND (#{startTime} IS NULL OR created_time >= #{startTime}) " +
            "AND (#{endTime} IS NULL OR created_time <= #{endTime}) " +
            "GROUP BY user_id " +
            "ORDER BY total_feedback DESC")
    List<Map<String, Object>> countByUser(@Param("startTime") LocalDateTime startTime,
                                         @Param("endTime") LocalDateTime endTime);

    /**
     * 获取低评分反馈详情
     */
    @Select("SELECT f.*, r.user_message, r.ai_response, r.model_provider, r.model_name " +
            "FROM ai_chat_feedback f " +
            "LEFT JOIN ai_chat_record r ON f.chat_record_id = r.id " +
            "WHERE f.delete_flag = 0 " +
            "AND (f.feedback_type = 'DISLIKE' OR f.feedback_score <= 2) " +
            "AND (#{startTime} IS NULL OR f.created_time >= #{startTime}) " +
            "AND (#{endTime} IS NULL OR f.created_time <= #{endTime}) " +
            "ORDER BY f.created_time DESC " +
            "LIMIT #{limit}")
    List<Map<String, Object>> getLowRatingFeedback(@Param("startTime") LocalDateTime startTime,
                                                   @Param("endTime") LocalDateTime endTime,
                                                   @Param("limit") Integer limit);

    /**
     * 获取改进建议
     */
    @Select("SELECT improvement_suggestion, COUNT(*) as count " +
            "FROM ai_chat_feedback " +
            "WHERE delete_flag = 0 AND improvement_suggestion IS NOT NULL AND improvement_suggestion != '' " +
            "AND (#{startTime} IS NULL OR created_time >= #{startTime}) " +
            "AND (#{endTime} IS NULL OR created_time <= #{endTime}) " +
            "GROUP BY improvement_suggestion " +
            "ORDER BY count DESC " +
            "LIMIT #{limit}")
    List<Map<String, Object>> getImprovementSuggestions(@Param("startTime") LocalDateTime startTime,
                                                        @Param("endTime") LocalDateTime endTime,
                                                        @Param("limit") Integer limit);
}
