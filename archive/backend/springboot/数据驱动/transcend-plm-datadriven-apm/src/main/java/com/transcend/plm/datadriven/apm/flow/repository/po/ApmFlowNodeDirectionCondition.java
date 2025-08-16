package com.transcend.plm.datadriven.apm.flow.repository.po;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.io.Serializable;
import java.util.Date;
import lombok.Data;

/**
 * 流程节点流转条件配置表
 * @author unknown
 * @TableName apm_flow_node_direction_condition
 */
@TableName(value ="apm_flow_node_direction_condition")
@Data
public class ApmFlowNodeDirectionCondition implements Serializable {
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
     * 所属流程bid
     */
    private String flowTemplateBid;

    /**
     * 所属流程版本号
     */
    private String version;

    /**
     * 流程节点流转表业务bid
     */
    private String flowNodeDirectionBid;

    /**
     * 对象字段名称
     */
    private String filedName;

    /**
     * 对象字段类型，string.字符串，number.数字，date.日期，role.角色
     */
    private String filedType;

    /**
     * 表字段名称
     */
    private String columnName;

    /**
     * 比较条件：contain.包含，notContain.不包含，equal.等于，notEqual.不等于，null.为空，notNull.不为空，gt.大于，lt.小于，notGt.小于等于，notLt.大于等于，between.在区间 
     */
    private String relationship;

    /**
     * 条件比较值
     */
    private String filedValue;

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
     * 启用标志，0未启用，1启用，2禁用
     */
    private Boolean enableFlag;

    /**
     * 对象字段值类型，string.字符串，number.数字，date.日期，role.角色，now.日期当前时间。loginUser.当前登录人
     */
    private String filedValueType;

    @TableField(exist = false)
    private static final long serialVersionUID = 1L;

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
        ApmFlowNodeDirectionCondition other = (ApmFlowNodeDirectionCondition) that;
        return (this.getId() == null ? other.getId() == null : this.getId().equals(other.getId()))
            && (this.getBid() == null ? other.getBid() == null : this.getBid().equals(other.getBid()))
            && (this.getFlowTemplateBid() == null ? other.getFlowTemplateBid() == null : this.getFlowTemplateBid().equals(other.getFlowTemplateBid()))
            && (this.getVersion() == null ? other.getVersion() == null : this.getVersion().equals(other.getVersion()))
            && (this.getFlowNodeDirectionBid() == null ? other.getFlowNodeDirectionBid() == null : this.getFlowNodeDirectionBid().equals(other.getFlowNodeDirectionBid()))
            && (this.getFiledName() == null ? other.getFiledName() == null : this.getFiledName().equals(other.getFiledName()))
            && (this.getFiledType() == null ? other.getFiledType() == null : this.getFiledType().equals(other.getFiledType()))
            && (this.getColumnName() == null ? other.getColumnName() == null : this.getColumnName().equals(other.getColumnName()))
            && (this.getRelationship() == null ? other.getRelationship() == null : this.getRelationship().equals(other.getRelationship()))
            && (this.getFiledValue() == null ? other.getFiledValue() == null : this.getFiledValue().equals(other.getFiledValue()))
            && (this.getTenantId() == null ? other.getTenantId() == null : this.getTenantId().equals(other.getTenantId()))
            && (this.getCreatedBy() == null ? other.getCreatedBy() == null : this.getCreatedBy().equals(other.getCreatedBy()))
            && (this.getCreatedTime() == null ? other.getCreatedTime() == null : this.getCreatedTime().equals(other.getCreatedTime()))
            && (this.getUpdatedBy() == null ? other.getUpdatedBy() == null : this.getUpdatedBy().equals(other.getUpdatedBy()))
            && (this.getUpdatedTime() == null ? other.getUpdatedTime() == null : this.getUpdatedTime().equals(other.getUpdatedTime()))
            && (this.getDeleteFlag() == null ? other.getDeleteFlag() == null : this.getDeleteFlag().equals(other.getDeleteFlag()))
            && (this.getEnableFlag() == null ? other.getEnableFlag() == null : this.getEnableFlag().equals(other.getEnableFlag()))
            && (this.getFiledValueType() == null ? other.getFiledValueType() == null : this.getFiledValueType().equals(other.getFiledValueType()));
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((getId() == null) ? 0 : getId().hashCode());
        result = prime * result + ((getBid() == null) ? 0 : getBid().hashCode());
        result = prime * result + ((getFlowTemplateBid() == null) ? 0 : getFlowTemplateBid().hashCode());
        result = prime * result + ((getVersion() == null) ? 0 : getVersion().hashCode());
        result = prime * result + ((getFlowNodeDirectionBid() == null) ? 0 : getFlowNodeDirectionBid().hashCode());
        result = prime * result + ((getFiledName() == null) ? 0 : getFiledName().hashCode());
        result = prime * result + ((getFiledType() == null) ? 0 : getFiledType().hashCode());
        result = prime * result + ((getColumnName() == null) ? 0 : getColumnName().hashCode());
        result = prime * result + ((getRelationship() == null) ? 0 : getRelationship().hashCode());
        result = prime * result + ((getFiledValue() == null) ? 0 : getFiledValue().hashCode());
        result = prime * result + ((getTenantId() == null) ? 0 : getTenantId().hashCode());
        result = prime * result + ((getCreatedBy() == null) ? 0 : getCreatedBy().hashCode());
        result = prime * result + ((getCreatedTime() == null) ? 0 : getCreatedTime().hashCode());
        result = prime * result + ((getUpdatedBy() == null) ? 0 : getUpdatedBy().hashCode());
        result = prime * result + ((getUpdatedTime() == null) ? 0 : getUpdatedTime().hashCode());
        result = prime * result + ((getDeleteFlag() == null) ? 0 : getDeleteFlag().hashCode());
        result = prime * result + ((getEnableFlag() == null) ? 0 : getEnableFlag().hashCode());
        result = prime * result + ((getFiledValueType() == null) ? 0 : getFiledValueType().hashCode());
        return result;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(getClass().getSimpleName());
        sb.append(" [");
        sb.append("Hash = ").append(hashCode());
        sb.append(", id=").append(id);
        sb.append(", bid=").append(bid);
        sb.append(", flowTemplateBid=").append(flowTemplateBid);
        sb.append(", version=").append(version);
        sb.append(", flowNodeDirectionBid=").append(flowNodeDirectionBid);
        sb.append(", filedName=").append(filedName);
        sb.append(", filedType=").append(filedType);
        sb.append(", columnName=").append(columnName);
        sb.append(", relationship=").append(relationship);
        sb.append(", filedValue=").append(filedValue);
        sb.append(", tenantId=").append(tenantId);
        sb.append(", createdBy=").append(createdBy);
        sb.append(", createdTime=").append(createdTime);
        sb.append(", updatedBy=").append(updatedBy);
        sb.append(", updatedTime=").append(updatedTime);
        sb.append(", deleteFlag=").append(deleteFlag);
        sb.append(", enableFlag=").append(enableFlag);
        sb.append(", filedValueType=").append(filedValueType);
        sb.append(", serialVersionUID=").append(serialVersionUID);
        sb.append("]");
        return sb.toString();
    }
}