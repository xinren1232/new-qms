package com.transcend.plm.datadriven.notify.domain;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.io.Serializable;
import java.util.Date;
import lombok.Data;

/**
 * 通知触发类型规则配置表
 *
 * @author yuanhu.huang
 * @date 2024/07/24
 */
@TableName(value ="notify_trigger_rule",autoResultMap = true)
@Data
public class NotifyTriggerRule implements Serializable {
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
     * 通知触发表业务id
     */
    private String notifyConfigTriggerBid;

    /**
     * 对象属性编码
     */
    private String attrCode;

    /**
     * 对象属性数据库字段
     */
    private String attrDbKey;
    /**
     * 规则类型：1.触发规则，2.通知规则
     */
    private Integer ruleType;
    /**
     * 比较条件：contain.包含，notContain.不包含，equal.等于，notEqual.不等于，null.为空，notNull.不为空，gt.大于，lt.小于，notGt.小于等于，notLt.大于等于
     */
    private String relationship;

    /**
     * 比较条件值 now_time:当前时间
     */
    private String relationValue;

    /**
     * 比较条件值，now_time:与当前时间比较时间值 -2（天 单位不一定）：当前时间减去两天 +当前时间+2天
     */
    private String comparisonValue;

    /**
     * 比较条件值类型，STRING.字符串，NUMBER.数字，DATE.日期，NOW.当天时间
     */
    private String relationValueType;

    /**
     * 字典类型
     */
    private String remoteDictType;

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
        NotifyTriggerRule other = (NotifyTriggerRule) that;
        return (this.getId() == null ? other.getId() == null : this.getId().equals(other.getId()))
            && (this.getBid() == null ? other.getBid() == null : this.getBid().equals(other.getBid()))
            && (this.getNotifyConfigTriggerBid() == null ? other.getNotifyConfigTriggerBid() == null : this.getNotifyConfigTriggerBid().equals(other.getNotifyConfigTriggerBid()))
            && (this.getAttrCode() == null ? other.getAttrCode() == null : this.getAttrCode().equals(other.getAttrCode()))
            && (this.getAttrDbKey() == null ? other.getAttrDbKey() == null : this.getAttrDbKey().equals(other.getAttrDbKey()))
            && (this.getRelationship() == null ? other.getRelationship() == null : this.getRelationship().equals(other.getRelationship()))
            && (this.getRelationValue() == null ? other.getRelationValue() == null : this.getRelationValue().equals(other.getRelationValue()))
            && (this.getRelationValueType() == null ? other.getRelationValueType() == null : this.getRelationValueType().equals(other.getRelationValueType()))
            && (this.getComparisonValue() == null ? other.getComparisonValue() == null : this.getComparisonValue().equals(other.getComparisonValue()))
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
        result = prime * result + ((getNotifyConfigTriggerBid() == null) ? 0 : getNotifyConfigTriggerBid().hashCode());
        result = prime * result + ((getAttrCode() == null) ? 0 : getAttrCode().hashCode());
        result = prime * result + ((getAttrDbKey() == null) ? 0 : getAttrDbKey().hashCode());
        result = prime * result + ((getRelationship() == null) ? 0 : getRelationship().hashCode());
        result = prime * result + ((getRelationValue() == null) ? 0 : getRelationValue().hashCode());
        result = prime * result + ((getRelationValueType() == null) ? 0 : getRelationValueType().hashCode());
        result = prime * result + ((getComparisonValue() == null) ? 0 : getComparisonValue().hashCode());
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
        sb.append(", notifyConfigTriggerBid=").append(notifyConfigTriggerBid);
        sb.append(", attrCode=").append(attrCode);
        sb.append(", attrDbKey=").append(attrDbKey);
        sb.append(", relationship=").append(relationship);
        sb.append(", relationValue=").append(relationValue);
        sb.append(", relationValueType=").append(relationValueType);
        sb.append(", comparisonValue=").append(comparisonValue);
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