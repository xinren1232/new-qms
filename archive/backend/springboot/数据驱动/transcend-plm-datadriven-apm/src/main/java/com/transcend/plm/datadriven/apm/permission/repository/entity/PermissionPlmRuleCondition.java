package com.transcend.plm.datadriven.apm.permission.repository.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.extension.handlers.JacksonTypeHandler;
import lombok.Data;

import java.io.Serializable;
import java.util.Date;

/**
 * 权限规则条件表
 * @author unknown
 * @TableName permission_plm_rule_condition
 */
@TableName(value ="permission_plm_rule_condition")
@Data
public class PermissionPlmRuleCondition implements Serializable {
    /**
     * 自增主键id
     */
    @TableId(type = IdType.AUTO)
    private Integer id;

    /**
     * 业务id
     */
    private String bid;

    /**
     * 权限规则bid
     */
    private String permissionBid;

    /**
     * 对象code
     */
    private String appBid;

    /**
     * 对象字段
     */
    private String attrCode;

    /**
     * 对象字段类型
     */
    private String type;

    /**
     * 对象字段类型
     */
    private String valueType;

    /**
     * 对象字段类型
     */
    private String remoteDictType;


    /**
     * 运算符,EQ等于，GT大于,LT小于，，NULL为空，NOT NULL非空。。。
     */
    private String operator;

    /**
     * 对象字段值
     */
    @TableField(typeHandler = JacksonTypeHandler.class)
    private Object attrCodeValue;

    /**
     * 对象字段值
     */
    private String propertyZh;

    /**
     * 对象字段值
     */
    private String operatorZh;

    /**
     * 对象字段值
     */
    private String valueZh;


    /**
     * 启用标志，0未启用，1启用，2禁用
     */
    private Boolean enableFlag;

    /**
     * 删除标志
     */
    private Boolean deleteFlag;

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
        PermissionPlmRuleCondition other = (PermissionPlmRuleCondition) that;
        return (this.getId() == null ? other.getId() == null : this.getId().equals(other.getId()))
            && (this.getBid() == null ? other.getBid() == null : this.getBid().equals(other.getBid()))
            && (this.getPermissionBid() == null ? other.getPermissionBid() == null : this.getPermissionBid().equals(other.getPermissionBid()))
            && (this.getAppBid() == null ? other.getAppBid() == null : this.getAppBid().equals(other.getAppBid()))
            && (this.getAttrCode() == null ? other.getAttrCode() == null : this.getAttrCode().equals(other.getAttrCode()))
            && (this.getOperator() == null ? other.getOperator() == null : this.getOperator().equals(other.getOperator()))
            && (this.getAttrCodeValue() == null ? other.getAttrCodeValue() == null : this.getAttrCodeValue().equals(other.getAttrCodeValue()))
            && (this.getEnableFlag() == null ? other.getEnableFlag() == null : this.getEnableFlag().equals(other.getEnableFlag()))
            && (this.getDeleteFlag() == null ? other.getDeleteFlag() == null : this.getDeleteFlag().equals(other.getDeleteFlag()))
            && (this.getTenantId() == null ? other.getTenantId() == null : this.getTenantId().equals(other.getTenantId()))
            && (this.getCreatedBy() == null ? other.getCreatedBy() == null : this.getCreatedBy().equals(other.getCreatedBy()))
            && (this.getCreatedTime() == null ? other.getCreatedTime() == null : this.getCreatedTime().equals(other.getCreatedTime()))
            && (this.getUpdatedBy() == null ? other.getUpdatedBy() == null : this.getUpdatedBy().equals(other.getUpdatedBy()))
            && (this.getUpdatedTime() == null ? other.getUpdatedTime() == null : this.getUpdatedTime().equals(other.getUpdatedTime()));
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((getId() == null) ? 0 : getId().hashCode());
        result = prime * result + ((getBid() == null) ? 0 : getBid().hashCode());
        result = prime * result + ((getPermissionBid() == null) ? 0 : getPermissionBid().hashCode());
        result = prime * result + ((getAppBid() == null) ? 0 : getAppBid().hashCode());
        result = prime * result + ((getAttrCode() == null) ? 0 : getAttrCode().hashCode());
        result = prime * result + ((getOperator() == null) ? 0 : getOperator().hashCode());
        result = prime * result + ((getAttrCodeValue() == null) ? 0 : getAttrCodeValue().hashCode());
        result = prime * result + ((getEnableFlag() == null) ? 0 : getEnableFlag().hashCode());
        result = prime * result + ((getDeleteFlag() == null) ? 0 : getDeleteFlag().hashCode());
        result = prime * result + ((getTenantId() == null) ? 0 : getTenantId().hashCode());
        result = prime * result + ((getCreatedBy() == null) ? 0 : getCreatedBy().hashCode());
        result = prime * result + ((getCreatedTime() == null) ? 0 : getCreatedTime().hashCode());
        result = prime * result + ((getUpdatedBy() == null) ? 0 : getUpdatedBy().hashCode());
        result = prime * result + ((getUpdatedTime() == null) ? 0 : getUpdatedTime().hashCode());
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
        sb.append(", permissionBid=").append(permissionBid);
        sb.append(", appBid=").append(appBid);
        sb.append(", attrCode=").append(attrCode);
        sb.append(", operator=").append(operator);
        sb.append(", attrCodeValue=").append(attrCodeValue);
        sb.append(", enableFlag=").append(enableFlag);
        sb.append(", deleteFlag=").append(deleteFlag);
        sb.append(", tenantId=").append(tenantId);
        sb.append(", createdBy=").append(createdBy);
        sb.append(", createdTime=").append(createdTime);
        sb.append(", updatedBy=").append(updatedBy);
        sb.append(", updatedTime=").append(updatedTime);
        sb.append(", serialVersionUID=").append(serialVersionUID);
        sb.append("]");
        return sb.toString();
    }
}