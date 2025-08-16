package com.transcend.plm.datadriven.apm.monitoring.repository.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.transcend.plm.datadriven.apm.monitoring.repository.po.AiChatRecord;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * AI问答记录Mapper
 *
 * @author AI Assistant
 * @date 2025-01-31
 */
@Mapper
public interface AiChatRecordMapper extends BaseMapper<AiChatRecord> {

    /**
     * 分页查询问答记录
     */
    @Select("SELECT * FROM ai_chat_record WHERE delete_flag = 0 " +
            "AND (#{userId} IS NULL OR user_id = #{userId}) " +
            "AND (#{modelProvider} IS NULL OR model_provider = #{modelProvider}) " +
            "AND (#{chatStatus} IS NULL OR chat_status = #{chatStatus}) " +
            "AND (#{startTime} IS NULL OR created_time >= #{startTime}) " +
            "AND (#{endTime} IS NULL OR created_time <= #{endTime}) " +
            "ORDER BY created_time DESC")
    IPage<AiChatRecord> selectChatRecordPage(Page<AiChatRecord> page,
                                           @Param("userId") String userId,
                                           @Param("modelProvider") String modelProvider,
                                           @Param("chatStatus") String chatStatus,
                                           @Param("startTime") LocalDateTime startTime,
                                           @Param("endTime") LocalDateTime endTime);

    /**
     * 统计用户问答数量
     */
    @Select("SELECT user_id, COUNT(*) as count FROM ai_chat_record " +
            "WHERE delete_flag = 0 " +
            "AND (#{startTime} IS NULL OR created_time >= #{startTime}) " +
            "AND (#{endTime} IS NULL OR created_time <= #{endTime}) " +
            "GROUP BY user_id ORDER BY count DESC")
    List<Map<String, Object>> countByUser(@Param("startTime") LocalDateTime startTime,
                                         @Param("endTime") LocalDateTime endTime);

    /**
     * 统计模型使用情况
     */
    @Select("SELECT model_provider, model_name, COUNT(*) as count FROM ai_chat_record " +
            "WHERE delete_flag = 0 " +
            "AND (#{startTime} IS NULL OR created_time >= #{startTime}) " +
            "AND (#{endTime} IS NULL OR created_time <= #{endTime}) " +
            "GROUP BY model_provider, model_name ORDER BY count DESC")
    List<Map<String, Object>> countByModel(@Param("startTime") LocalDateTime startTime,
                                          @Param("endTime") LocalDateTime endTime);

    /**
     * 统计每小时使用情况
     */
    @Select("SELECT HOUR(created_time) as hour, COUNT(*) as count FROM ai_chat_record " +
            "WHERE delete_flag = 0 " +
            "AND (#{startTime} IS NULL OR created_time >= #{startTime}) " +
            "AND (#{endTime} IS NULL OR created_time <= #{endTime}) " +
            "GROUP BY HOUR(created_time) ORDER BY hour")
    List<Map<String, Object>> countByHour(@Param("startTime") LocalDateTime startTime,
                                         @Param("endTime") LocalDateTime endTime);

    /**
     * 统计响应时间分布
     */
    @Select("SELECT " +
            "CASE " +
            "WHEN response_time < 1000 THEN '< 1s' " +
            "WHEN response_time < 3000 THEN '1-3s' " +
            "WHEN response_time < 5000 THEN '3-5s' " +
            "WHEN response_time < 10000 THEN '5-10s' " +
            "ELSE '> 10s' " +
            "END as time_range, " +
            "COUNT(*) as count " +
            "FROM ai_chat_record " +
            "WHERE delete_flag = 0 AND response_time IS NOT NULL " +
            "AND (#{startTime} IS NULL OR created_time >= #{startTime}) " +
            "AND (#{endTime} IS NULL OR created_time <= #{endTime}) " +
            "GROUP BY time_range")
    List<Map<String, Object>> countByResponseTime(@Param("startTime") LocalDateTime startTime,
                                                 @Param("endTime") LocalDateTime endTime);

    /**
     * 获取系统总体统计
     */
    @Select("SELECT " +
            "COUNT(*) as total_chats, " +
            "COUNT(DISTINCT user_id) as unique_users, " +
            "COUNT(DISTINCT conversation_id) as total_conversations, " +
            "AVG(response_time) as avg_response_time, " +
            "SUM(CASE WHEN chat_status = 'SUCCESS' THEN 1 ELSE 0 END) as successful_chats, " +
            "SUM(CASE WHEN chat_status = 'FAILED' THEN 1 ELSE 0 END) as failed_chats " +
            "FROM ai_chat_record " +
            "WHERE delete_flag = 0 " +
            "AND (#{startTime} IS NULL OR created_time >= #{startTime}) " +
            "AND (#{endTime} IS NULL OR created_time <= #{endTime})")
    Map<String, Object> getOverallStatistics(@Param("startTime") LocalDateTime startTime,
                                            @Param("endTime") LocalDateTime endTime);

    /**
     * 获取最近活跃用户
     */
    @Select("SELECT user_id, user_name, COUNT(*) as chat_count, MAX(created_time) as last_chat_time " +
            "FROM ai_chat_record " +
            "WHERE delete_flag = 0 " +
            "AND created_time >= #{startTime} " +
            "GROUP BY user_id, user_name " +
            "ORDER BY chat_count DESC, last_chat_time DESC " +
            "LIMIT #{limit}")
    List<Map<String, Object>> getActiveUsers(@Param("startTime") LocalDateTime startTime,
                                           @Param("limit") Integer limit);

    /**
     * 获取热门问题关键词
     */
    @Select("SELECT user_message, COUNT(*) as count FROM ai_chat_record " +
            "WHERE delete_flag = 0 " +
            "AND (#{startTime} IS NULL OR created_time >= #{startTime}) " +
            "AND (#{endTime} IS NULL OR created_time <= #{endTime}) " +
            "GROUP BY user_message " +
            "HAVING count > 1 " +
            "ORDER BY count DESC " +
            "LIMIT #{limit}")
    List<Map<String, Object>> getPopularQuestions(@Param("startTime") LocalDateTime startTime,
                                                 @Param("endTime") LocalDateTime endTime,
                                                 @Param("limit") Integer limit);
}
