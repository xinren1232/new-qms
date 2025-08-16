package com.transcend.plm.datadriven.apm.monitoring.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.transcend.plm.datadriven.apm.monitoring.repository.po.AiChatRecord;
import com.transcend.plm.datadriven.apm.monitoring.repository.po.AiChatFeedback;
import com.transcend.plm.datadriven.apm.monitoring.service.AiMonitoringService;
import com.transcend.plm.datadriven.apm.permission.service.impl.ApmRoleDomainService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * AI监控控制器
 *
 * @author AI Assistant
 * @date 2025-01-31
 */
@Slf4j
@RestController
@RequestMapping("/api/ai-monitoring")
@Api(tags = "AI问答监控管理")
public class AiMonitoringController {

    @Autowired
    private AiMonitoringService aiMonitoringService;

    @Autowired
    private ApmRoleDomainService apmRoleDomainService;

    /**
     * 检查管理员权限
     */
    private boolean checkAdminPermission() {
        try {
            return apmRoleDomainService.isGlobalAdmin() || apmRoleDomainService.isSpaceAdmin(null);
        } catch (Exception e) {
            log.warn("权限检查失败", e);
            return false;
        }
    }

    @PostMapping("/chat-record")
    @ApiOperation("记录AI问答")
    public ResponseEntity<Map<String, Object>> recordChatMessage(@RequestBody AiChatRecord chatRecord) {
        Map<String, Object> result = new HashMap<>();
        try {
            aiMonitoringService.recordChatMessage(chatRecord);
            result.put("success", true);
            result.put("message", "记录成功");
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            log.error("记录AI问答失败", e);
            result.put("success", false);
            result.put("message", "记录失败: " + e.getMessage());
            return ResponseEntity.ok(result);
        }
    }

    @PostMapping("/feedback")
    @ApiOperation("记录用户反馈")
    public ResponseEntity<Map<String, Object>> recordFeedback(@RequestBody AiChatFeedback feedback) {
        Map<String, Object> result = new HashMap<>();
        try {
            aiMonitoringService.recordFeedback(feedback);
            result.put("success", true);
            result.put("message", "反馈记录成功");
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            log.error("记录用户反馈失败", e);
            result.put("success", false);
            result.put("message", "反馈记录失败: " + e.getMessage());
            return ResponseEntity.ok(result);
        }
    }

    @GetMapping("/chat-records")
    @ApiOperation("分页查询问答记录")
    public ResponseEntity<Map<String, Object>> getChatRecords(
            @ApiParam("页码") @RequestParam(defaultValue = "1") Integer current,
            @ApiParam("页大小") @RequestParam(defaultValue = "20") Integer size,
            @ApiParam("用户ID") @RequestParam(required = false) String userId,
            @ApiParam("模型提供商") @RequestParam(required = false) String modelProvider,
            @ApiParam("对话状态") @RequestParam(required = false) String chatStatus,
            @ApiParam("开始时间") @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") LocalDateTime startTime,
            @ApiParam("结束时间") @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") LocalDateTime endTime) {
        
        Map<String, Object> result = new HashMap<>();
        
        // 检查管理员权限
        if (!checkAdminPermission()) {
            result.put("success", false);
            result.put("message", "权限不足，仅管理员可访问");
            return ResponseEntity.ok(result);
        }
        
        try {
            Page<AiChatRecord> page = new Page<>(current, size);
            IPage<AiChatRecord> pageResult = aiMonitoringService.getChatRecordPage(
                    page, userId, modelProvider, chatStatus, startTime, endTime);
            
            result.put("success", true);
            result.put("data", pageResult);
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            log.error("查询问答记录失败", e);
            result.put("success", false);
            result.put("message", "查询失败: " + e.getMessage());
            return ResponseEntity.ok(result);
        }
    }

    @GetMapping("/feedback-records")
    @ApiOperation("分页查询反馈记录")
    public ResponseEntity<Map<String, Object>> getFeedbackRecords(
            @ApiParam("页码") @RequestParam(defaultValue = "1") Integer current,
            @ApiParam("页大小") @RequestParam(defaultValue = "20") Integer size,
            @ApiParam("反馈类型") @RequestParam(required = false) String feedbackType,
            @ApiParam("用户ID") @RequestParam(required = false) String userId,
            @ApiParam("开始时间") @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") LocalDateTime startTime,
            @ApiParam("结束时间") @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") LocalDateTime endTime) {
        
        Map<String, Object> result = new HashMap<>();
        
        // 检查管理员权限
        if (!checkAdminPermission()) {
            result.put("success", false);
            result.put("message", "权限不足，仅管理员可访问");
            return ResponseEntity.ok(result);
        }
        
        try {
            Page<AiChatFeedback> page = new Page<>(current, size);
            IPage<Map<String, Object>> pageResult = aiMonitoringService.getFeedbackPage(
                    page, feedbackType, userId, startTime, endTime);
            
            result.put("success", true);
            result.put("data", pageResult);
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            log.error("查询反馈记录失败", e);
            result.put("success", false);
            result.put("message", "查询失败: " + e.getMessage());
            return ResponseEntity.ok(result);
        }
    }

    @GetMapping("/statistics/overall")
    @ApiOperation("获取系统总体统计")
    public ResponseEntity<Map<String, Object>> getOverallStatistics(
            @ApiParam("开始时间") @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") LocalDateTime startTime,
            @ApiParam("结束时间") @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") LocalDateTime endTime) {
        
        Map<String, Object> result = new HashMap<>();
        
        // 检查管理员权限
        if (!checkAdminPermission()) {
            result.put("success", false);
            result.put("message", "权限不足，仅管理员可访问");
            return ResponseEntity.ok(result);
        }
        
        try {
            // 默认查询最近7天的数据
            if (startTime == null) {
                startTime = LocalDateTime.now().minusDays(7);
            }
            if (endTime == null) {
                endTime = LocalDateTime.now();
            }
            
            Map<String, Object> statistics = aiMonitoringService.getOverallStatistics(startTime, endTime);
            result.put("success", true);
            result.put("data", statistics);
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            log.error("获取总体统计失败", e);
            result.put("success", false);
            result.put("message", "获取统计失败: " + e.getMessage());
            return ResponseEntity.ok(result);
        }
    }

    @GetMapping("/statistics/users")
    @ApiOperation("获取用户使用统计")
    public ResponseEntity<Map<String, Object>> getUserStatistics(
            @ApiParam("开始时间") @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") LocalDateTime startTime,
            @ApiParam("结束时间") @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") LocalDateTime endTime) {
        
        Map<String, Object> result = new HashMap<>();
        
        // 检查管理员权限
        if (!checkAdminPermission()) {
            result.put("success", false);
            result.put("message", "权限不足，仅管理员可访问");
            return ResponseEntity.ok(result);
        }
        
        try {
            // 默认查询最近7天的数据
            if (startTime == null) {
                startTime = LocalDateTime.now().minusDays(7);
            }
            if (endTime == null) {
                endTime = LocalDateTime.now();
            }
            
            List<Map<String, Object>> statistics = aiMonitoringService.getUserStatistics(startTime, endTime);
            result.put("success", true);
            result.put("data", statistics);
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            log.error("获取用户统计失败", e);
            result.put("success", false);
            result.put("message", "获取统计失败: " + e.getMessage());
            return ResponseEntity.ok(result);
        }
    }

    @GetMapping("/statistics/models")
    @ApiOperation("获取模型使用统计")
    public ResponseEntity<Map<String, Object>> getModelStatistics(
            @ApiParam("开始时间") @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") LocalDateTime startTime,
            @ApiParam("结束时间") @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") LocalDateTime endTime) {
        
        Map<String, Object> result = new HashMap<>();
        
        // 检查管理员权限
        if (!checkAdminPermission()) {
            result.put("success", false);
            result.put("message", "权限不足，仅管理员可访问");
            return ResponseEntity.ok(result);
        }
        
        try {
            // 默认查询最近7天的数据
            if (startTime == null) {
                startTime = LocalDateTime.now().minusDays(7);
            }
            if (endTime == null) {
                endTime = LocalDateTime.now();
            }
            
            List<Map<String, Object>> statistics = aiMonitoringService.getModelStatistics(startTime, endTime);
            result.put("success", true);
            result.put("data", statistics);
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            log.error("获取模型统计失败", e);
            result.put("success", false);
            result.put("message", "获取统计失败: " + e.getMessage());
            return ResponseEntity.ok(result);
        }
    }

    @GetMapping("/statistics/feedback")
    @ApiOperation("获取反馈统计")
    public ResponseEntity<Map<String, Object>> getFeedbackStatistics(
            @ApiParam("开始时间") @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") LocalDateTime startTime,
            @ApiParam("结束时间") @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") LocalDateTime endTime) {
        
        Map<String, Object> result = new HashMap<>();
        
        // 检查管理员权限
        if (!checkAdminPermission()) {
            result.put("success", false);
            result.put("message", "权限不足，仅管理员可访问");
            return ResponseEntity.ok(result);
        }
        
        try {
            // 默认查询最近7天的数据
            if (startTime == null) {
                startTime = LocalDateTime.now().minusDays(7);
            }
            if (endTime == null) {
                endTime = LocalDateTime.now();
            }
            
            Map<String, Object> statistics = aiMonitoringService.getFeedbackStatistics(startTime, endTime);
            result.put("success", true);
            result.put("data", statistics);
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            log.error("获取反馈统计失败", e);
            result.put("success", false);
            result.put("message", "获取统计失败: " + e.getMessage());
            return ResponseEntity.ok(result);
        }
    }

    @GetMapping("/dashboard")
    @ApiOperation("获取监控仪表板数据")
    public ResponseEntity<Map<String, Object>> getDashboardData(
            @ApiParam("开始时间") @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") LocalDateTime startTime,
            @ApiParam("结束时间") @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") LocalDateTime endTime) {
        
        Map<String, Object> result = new HashMap<>();
        
        // 检查管理员权限
        if (!checkAdminPermission()) {
            result.put("success", false);
            result.put("message", "权限不足，仅管理员可访问");
            return ResponseEntity.ok(result);
        }
        
        try {
            // 默认查询最近7天的数据
            if (startTime == null) {
                startTime = LocalDateTime.now().minusDays(7);
            }
            if (endTime == null) {
                endTime = LocalDateTime.now();
            }
            
            Map<String, Object> dashboardData = aiMonitoringService.generateMonitoringReport(startTime, endTime);
            result.put("success", true);
            result.put("data", dashboardData);
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            log.error("获取仪表板数据失败", e);
            result.put("success", false);
            result.put("message", "获取数据失败: " + e.getMessage());
            return ResponseEntity.ok(result);
        }
    }

    @GetMapping("/realtime-metrics")
    @ApiOperation("获取实时监控指标")
    public ResponseEntity<Map<String, Object>> getRealTimeMetrics() {
        Map<String, Object> result = new HashMap<>();
        
        // 检查管理员权限
        if (!checkAdminPermission()) {
            result.put("success", false);
            result.put("message", "权限不足，仅管理员可访问");
            return ResponseEntity.ok(result);
        }
        
        try {
            Map<String, Object> metrics = aiMonitoringService.getRealTimeMetrics();
            result.put("success", true);
            result.put("data", metrics);
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            log.error("获取实时指标失败", e);
            result.put("success", false);
            result.put("message", "获取指标失败: " + e.getMessage());
            return ResponseEntity.ok(result);
        }
    }
}
