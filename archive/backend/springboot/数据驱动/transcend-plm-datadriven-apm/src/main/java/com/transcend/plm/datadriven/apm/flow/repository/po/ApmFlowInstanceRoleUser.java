package com.transcend.plm.datadriven.apm.flow.repository.po;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.io.Serializable;
import java.util.Date;

/**
 * 流程实例角色人员表
 * @author unknown
 * @TableName apm_flow_instance_role_user
 */
@TableName(value ="apm_flow_instance_role_user")
@Data
public class ApmFlowInstanceRoleUser implements Serializable {
    /**
     * 
     */
    @TableId(type = IdType.AUTO)
    private Integer id;

    /**
     * 对象模型编码
     */
    private String modelCode;

    /**
     * 实例bid
     */
    private String instanceBid;

    /**
     * 角色bid
     */
    private String roleBid;

    /**
     * 
     */
    private String userNo;

    /**
     * 创建时间
     */
    private Date createdTime;

    /**
     * 生命周期编码
     */
    private String lifeCycleCode;

    /**
     * 创建人
     */
    private String createdBy;

    /**
     * 租户ID
     */
    private Integer tenantId;

    /**
     * 空间bid
     */
    private String spaceBid;

    /**
     * 空间应用bid
     */
    private String spaceAppBid;

    /**
     * 启用标志
     */
    private Byte enableFlag;

    /**
     * 删除标志
     */
    @TableLogic
    private Byte deleteFlag;

    /**
     * 处理标志(历史数据处理)
     */
    private String handleFlag;

    /**
     * 名称
     */
    private String name;

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
        ApmFlowInstanceRoleUser other = (ApmFlowInstanceRoleUser) that;
        return (this.getId() == null ? other.getId() == null : this.getId().equals(other.getId()))
            && (this.getModelCode() == null ? other.getModelCode() == null : this.getModelCode().equals(other.getModelCode()))
            && (this.getInstanceBid() == null ? other.getInstanceBid() == null : this.getInstanceBid().equals(other.getInstanceBid()))
            && (this.getRoleBid() == null ? other.getRoleBid() == null : this.getRoleBid().equals(other.getRoleBid()))
            && (this.getUserNo() == null ? other.getUserNo() == null : this.getUserNo().equals(other.getUserNo()))
            && (this.getCreatedTime() == null ? other.getCreatedTime() == null : this.getCreatedTime().equals(other.getCreatedTime()))
            && (this.getCreatedBy() == null ? other.getCreatedBy() == null : this.getCreatedBy().equals(other.getCreatedBy()))
            && (this.getTenantId() == null ? other.getTenantId() == null : this.getTenantId().equals(other.getTenantId()))
            && (this.getEnableFlag() == null ? other.getEnableFlag() == null : this.getEnableFlag().equals(other.getEnableFlag()))
            && (this.getDeleteFlag() == null ? other.getDeleteFlag() == null : this.getDeleteFlag().equals(other.getDeleteFlag()))
            && (this.getName() == null ? other.getName() == null : this.getName().equals(other.getName()));
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((getId() == null) ? 0 : getId().hashCode());
        result = prime * result + ((getModelCode() == null) ? 0 : getModelCode().hashCode());
        result = prime * result + ((getInstanceBid() == null) ? 0 : getInstanceBid().hashCode());
        result = prime * result + ((getRoleBid() == null) ? 0 : getRoleBid().hashCode());
        result = prime * result + ((getUserNo() == null) ? 0 : getUserNo().hashCode());
        result = prime * result + ((getCreatedTime() == null) ? 0 : getCreatedTime().hashCode());
        result = prime * result + ((getCreatedBy() == null) ? 0 : getCreatedBy().hashCode());
        result = prime * result + ((getTenantId() == null) ? 0 : getTenantId().hashCode());
        result = prime * result + ((getEnableFlag() == null) ? 0 : getEnableFlag().hashCode());
        result = prime * result + ((getDeleteFlag() == null) ? 0 : getDeleteFlag().hashCode());
        result = prime * result + ((getName() == null) ? 0 : getName().hashCode());
        return result;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(getClass().getSimpleName());
        sb.append(" [");
        sb.append("Hash = ").append(hashCode());
        sb.append(", id=").append(id);
        sb.append(", modelCode=").append(modelCode);
        sb.append(", instanceBid=").append(instanceBid);
        sb.append(", roleBid=").append(roleBid);
        sb.append(", userNo=").append(userNo);
        sb.append(", createdTime=").append(createdTime);
        sb.append(", createdBy=").append(createdBy);
        sb.append(", tenantId=").append(tenantId);
        sb.append(", enableFlag=").append(enableFlag);
        sb.append(", deleteFlag=").append(deleteFlag);
        sb.append(", name=").append(name);
        sb.append(", serialVersionUID=").append(serialVersionUID);
        sb.append("]");
        return sb.toString();
    }
}