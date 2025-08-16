package com.transcend.plm.datadriven.notify.vo;


import com.alibaba.fastjson.JSONArray;
import lombok.Data;

import java.util.Date;
import java.util.List;

/**
 * @author yuanhu.huang
 * @date 2024/07/24
 */
@Data
public class NotifyTimeRuleVo {
    private Integer id;

    /**
     * 业务id
     */
    private String bid;

    /**
     * 通知主表业务id
     */
    private String notifyConfigBid;

    /**
     * 时间类型,NOW.实时，DAY.按天，WEEK.按周，MONTH.按月,INTERVAL.间隔时间,SPECIFY.指定时间,BUSINESS.业务时间
     */
    private String timeType;

    /**
     * 几秒
     */
    private Integer second;

    /**
     * 几分
     */
    private Integer minute;

    /**
     * 几点
     */
    private Integer hour;

    private List<Integer> weeks;
    private List<Integer> days;

    /**
     * 几号
     */
    private Integer day;

    /**
     * 周几
     */
    private Integer week;

    /**
     * 规则（未来统一在此维护规则）
     */
    private Object triggerRuleInfo;

    private JSONArray op;

    /**
     * 启用标志，0未启用，1启用，2禁用
     */
    private Boolean enableFlag;

    /**
     * 租户ID
     */
    private Long tenantId;

    /**
     * 创建人
     */
    private String createdBy;

    /**
     * 创建时间
     */
    private Date createdTime;

    /**
     * 更新人
     */
    private String updatedBy;

    /**
     * 更新时间
     */
    private Date updatedTime;

    /**
     * 删除标识
     */
    private Boolean deleteFlag;

    /**
     * 说明
     */
    private String description;

    /**
     * 指定时间
     */
    private Date specifyTime;

    /**
     * 业务属性
     */
    private String businessAttr;
}
