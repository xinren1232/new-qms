package com.transcend.plm.datadriven.notify.domain;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.io.Serializable;
import java.util.Date;
import java.util.List;

import lombok.Data;

/**
 * 通知时间规则配置表
 *
 * @author yuanhu.huang
 * @date 2024/07/24
 */
@TableName(value ="notify_time_rule",autoResultMap = true)
@Data
public class NotifyTimeRule implements Serializable {
    /**
     * 
     */
    @TableId(type = IdType.AUTO)
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
     * 时间类型,NOW.实时，DAY.按天，WEEK.按周，MONTH.按月,INTERVAL.间隔时间,HOUR.按小时
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

    /**
     * 几号
     */
    private Integer day;

    /**
     * 周几
     */
    private Integer week;

    @TableField(typeHandler = com.baomidou.mybatisplus.extension.handlers.FastjsonTypeHandler.class)
    private List<Integer> weeks;

    @TableField(typeHandler = com.baomidou.mybatisplus.extension.handlers.FastjsonTypeHandler.class)
    private List<Integer> days;

    /**
     * 规则（未来统一在此维护规则）
     */
    @TableField(typeHandler = com.baomidou.mybatisplus.extension.handlers.FastjsonTypeHandler.class)
    private Object triggerRuleInfo;

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

    @TableField(exist = false)
    private static final long serialVersionUID = 1L;

    /**
     * @param that
     * @return boolean
     */
    @Override
    public boolean equals(Object that) {
        if (this == that) {
            return true;
        }
        if (that == null) {
            return false;
        }
        if (getClass() != that.getClass()) {
            return false;
        }
        NotifyTimeRule other = (NotifyTimeRule) that;
        return (this.getId() == null ? other.getId() == null : this.getId().equals(other.getId()))
            && (this.getBid() == null ? other.getBid() == null : this.getBid().equals(other.getBid()))
            && (this.getNotifyConfigBid() == null ? other.getNotifyConfigBid() == null : this.getNotifyConfigBid().equals(other.getNotifyConfigBid()))
            && (this.getTimeType() == null ? other.getTimeType() == null : this.getTimeType().equals(other.getTimeType()))
            && (this.getSecond() == null ? other.getSecond() == null : this.getSecond().equals(other.getSecond()))
            && (this.getMinute() == null ? other.getMinute() == null : this.getMinute().equals(other.getMinute()))
            && (this.getHour() == null ? other.getHour() == null : this.getHour().equals(other.getHour()))
            && (this.getDay() == null ? other.getDay() == null : this.getDay().equals(other.getDay()))
            && (this.getWeek() == null ? other.getWeek() == null : this.getWeek().equals(other.getWeek()))
            && (this.getTriggerRuleInfo() == null ? other.getTriggerRuleInfo() == null : this.getTriggerRuleInfo().equals(other.getTriggerRuleInfo()))
            && (this.getEnableFlag() == null ? other.getEnableFlag() == null : this.getEnableFlag().equals(other.getEnableFlag()))
            && (this.getTenantId() == null ? other.getTenantId() == null : this.getTenantId().equals(other.getTenantId()))
            && (this.getCreatedBy() == null ? other.getCreatedBy() == null : this.getCreatedBy().equals(other.getCreatedBy()))
            && (this.getCreatedTime() == null ? other.getCreatedTime() == null : this.getCreatedTime().equals(other.getCreatedTime()))
            && (this.getUpdatedBy() == null ? other.getUpdatedBy() == null : this.getUpdatedBy().equals(other.getUpdatedBy()))
            && (this.getUpdatedTime() == null ? other.getUpdatedTime() == null : this.getUpdatedTime().equals(other.getUpdatedTime()))
            && (this.getDeleteFlag() == null ? other.getDeleteFlag() == null : this.getDeleteFlag().equals(other.getDeleteFlag()))
            && (this.getDescription() == null ? other.getDescription() == null : this.getDescription().equals(other.getDescription()));
    }

    /**
     * @return int
     */
    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((getId() == null) ? 0 : getId().hashCode());
        result = prime * result + ((getBid() == null) ? 0 : getBid().hashCode());
        result = prime * result + ((getNotifyConfigBid() == null) ? 0 : getNotifyConfigBid().hashCode());
        result = prime * result + ((getTimeType() == null) ? 0 : getTimeType().hashCode());
        result = prime * result + ((getSecond() == null) ? 0 : getSecond().hashCode());
        result = prime * result + ((getMinute() == null) ? 0 : getMinute().hashCode());
        result = prime * result + ((getHour() == null) ? 0 : getHour().hashCode());
        result = prime * result + ((getDay() == null) ? 0 : getDay().hashCode());
        result = prime * result + ((getWeek() == null) ? 0 : getWeek().hashCode());
        result = prime * result + ((getTriggerRuleInfo() == null) ? 0 : getTriggerRuleInfo().hashCode());
        result = prime * result + ((getEnableFlag() == null) ? 0 : getEnableFlag().hashCode());
        result = prime * result + ((getTenantId() == null) ? 0 : getTenantId().hashCode());
        result = prime * result + ((getCreatedBy() == null) ? 0 : getCreatedBy().hashCode());
        result = prime * result + ((getCreatedTime() == null) ? 0 : getCreatedTime().hashCode());
        result = prime * result + ((getUpdatedBy() == null) ? 0 : getUpdatedBy().hashCode());
        result = prime * result + ((getUpdatedTime() == null) ? 0 : getUpdatedTime().hashCode());
        result = prime * result + ((getDeleteFlag() == null) ? 0 : getDeleteFlag().hashCode());
        result = prime * result + ((getDescription() == null) ? 0 : getDescription().hashCode());
        return result;
    }

    /**
     * @return {@link String }
     */
    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(getClass().getSimpleName());
        sb.append(" [");
        sb.append("Hash = ").append(hashCode());
        sb.append(", id=").append(id);
        sb.append(", bid=").append(bid);
        sb.append(", notifyConfigBid=").append(notifyConfigBid);
        sb.append(", timeType=").append(timeType);
        sb.append(", second=").append(second);
        sb.append(", minute=").append(minute);
        sb.append(", hour=").append(hour);
        sb.append(", day=").append(day);
        sb.append(", week=").append(week);
        sb.append(", triggerRuleInfo=").append(triggerRuleInfo);
        sb.append(", enableFlag=").append(enableFlag);
        sb.append(", tenantId=").append(tenantId);
        sb.append(", createdBy=").append(createdBy);
        sb.append(", createdTime=").append(createdTime);
        sb.append(", updatedBy=").append(updatedBy);
        sb.append(", updatedTime=").append(updatedTime);
        sb.append(", deleteFlag=").append(deleteFlag);
        sb.append(", description=").append(description);
        sb.append(", serialVersionUID=").append(serialVersionUID);
        sb.append("]");
        return sb.toString();
    }
}