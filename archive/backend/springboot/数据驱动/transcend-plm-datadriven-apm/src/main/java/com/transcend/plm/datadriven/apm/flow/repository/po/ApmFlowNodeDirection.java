package com.transcend.plm.datadriven.apm.flow.repository.po;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.io.Serializable;
import java.util.Date;
import lombok.Data;

/**
 * 流程节点流转表
 * @author unknown
 * @TableName apm_flow_node_direction
 */
@TableName(value ="apm_flow_node_direction")
@Data
public class ApmFlowNodeDirection implements Serializable {
    /**
     * 
     */
    @TableId(type = IdType.AUTO)
    private Integer id;

    /**
     * 
     */
    private String bid;

    /**
     * 生命周期模板id
     */
    private String templateBid;

    /**
     * 版本号
     */
    private String version;

    /**
     * 开始节点bid
     */
    private String sourceNodeBid;

    /**
     * 结束节点bid
     */
    private String targetNodeBid;

    /**
     * 开始节点web bid
     */
    private String sourceNodeWebBid;

    /**
     * 结束节点web bid
     */
    private String targetNodeWebBid;

    /**
     * 连线名称
     */
    private String lineName;

    /**
     * 条件匹配方式，and/all.所有条件匹配，or/any.任一条件匹配
     */
    private String directionMatch;

    /**
     * 存储如坐标等信息
     */
    private Object layout;

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
        ApmFlowNodeDirection other = (ApmFlowNodeDirection) that;
        return (this.getId() == null ? other.getId() == null : this.getId().equals(other.getId()))
            && (this.getBid() == null ? other.getBid() == null : this.getBid().equals(other.getBid()))
            && (this.getTemplateBid() == null ? other.getTemplateBid() == null : this.getTemplateBid().equals(other.getTemplateBid()))
            && (this.getVersion() == null ? other.getVersion() == null : this.getVersion().equals(other.getVersion()))
            && (this.getSourceNodeBid() == null ? other.getSourceNodeBid() == null : this.getSourceNodeBid().equals(other.getSourceNodeBid()))
            && (this.getTargetNodeBid() == null ? other.getTargetNodeBid() == null : this.getTargetNodeBid().equals(other.getTargetNodeBid()))
            && (this.getSourceNodeWebBid() == null ? other.getSourceNodeWebBid() == null : this.getSourceNodeWebBid().equals(other.getSourceNodeWebBid()))
            && (this.getTargetNodeWebBid() == null ? other.getTargetNodeWebBid() == null : this.getTargetNodeWebBid().equals(other.getTargetNodeWebBid()))
            && (this.getLineName() == null ? other.getLineName() == null : this.getLineName().equals(other.getLineName()))
            && (this.getLayout() == null ? other.getLayout() == null : this.getLayout().equals(other.getLayout()))
            && (this.getTenantId() == null ? other.getTenantId() == null : this.getTenantId().equals(other.getTenantId()))
            && (this.getCreatedBy() == null ? other.getCreatedBy() == null : this.getCreatedBy().equals(other.getCreatedBy()))
            && (this.getCreatedTime() == null ? other.getCreatedTime() == null : this.getCreatedTime().equals(other.getCreatedTime()))
            && (this.getUpdatedBy() == null ? other.getUpdatedBy() == null : this.getUpdatedBy().equals(other.getUpdatedBy()))
            && (this.getUpdatedTime() == null ? other.getUpdatedTime() == null : this.getUpdatedTime().equals(other.getUpdatedTime()))
            && (this.getDeleteFlag() == null ? other.getDeleteFlag() == null : this.getDeleteFlag().equals(other.getDeleteFlag()))
            && (this.getEnableFlag() == null ? other.getEnableFlag() == null : this.getEnableFlag().equals(other.getEnableFlag()));
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((getId() == null) ? 0 : getId().hashCode());
        result = prime * result + ((getBid() == null) ? 0 : getBid().hashCode());
        result = prime * result + ((getTemplateBid() == null) ? 0 : getTemplateBid().hashCode());
        result = prime * result + ((getVersion() == null) ? 0 : getVersion().hashCode());
        result = prime * result + ((getSourceNodeBid() == null) ? 0 : getSourceNodeBid().hashCode());
        result = prime * result + ((getTargetNodeBid() == null) ? 0 : getTargetNodeBid().hashCode());
        result = prime * result + ((getSourceNodeWebBid() == null) ? 0 : getSourceNodeWebBid().hashCode());
        result = prime * result + ((getTargetNodeWebBid() == null) ? 0 : getTargetNodeWebBid().hashCode());
        result = prime * result + ((getLineName() == null) ? 0 : getLineName().hashCode());
        result = prime * result + ((getLayout() == null) ? 0 : getLayout().hashCode());
        result = prime * result + ((getTenantId() == null) ? 0 : getTenantId().hashCode());
        result = prime * result + ((getCreatedBy() == null) ? 0 : getCreatedBy().hashCode());
        result = prime * result + ((getCreatedTime() == null) ? 0 : getCreatedTime().hashCode());
        result = prime * result + ((getUpdatedBy() == null) ? 0 : getUpdatedBy().hashCode());
        result = prime * result + ((getUpdatedTime() == null) ? 0 : getUpdatedTime().hashCode());
        result = prime * result + ((getDeleteFlag() == null) ? 0 : getDeleteFlag().hashCode());
        result = prime * result + ((getEnableFlag() == null) ? 0 : getEnableFlag().hashCode());
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
        sb.append(", templateBid=").append(templateBid);
        sb.append(", version=").append(version);
        sb.append(", sourceNodeBid=").append(sourceNodeBid);
        sb.append(", targetNodeBid=").append(targetNodeBid);
        sb.append(", sourceNodeWebBid=").append(sourceNodeWebBid);
        sb.append(", targetNodeWebBid=").append(targetNodeWebBid);
        sb.append(", lineName=").append(lineName);
        sb.append(", layout=").append(layout);
        sb.append(", tenantId=").append(tenantId);
        sb.append(", createdBy=").append(createdBy);
        sb.append(", createdTime=").append(createdTime);
        sb.append(", updatedBy=").append(updatedBy);
        sb.append(", updatedTime=").append(updatedTime);
        sb.append(", deleteFlag=").append(deleteFlag);
        sb.append(", enableFlag=").append(enableFlag);
        sb.append(", serialVersionUID=").append(serialVersionUID);
        sb.append("]");
        return sb.toString();
    }
}