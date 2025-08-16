package com.transcend.plm.datadriven.apm.flow.repository.po;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;
import java.util.Date;
import java.util.List;

/**
 * 流程节点表
 * @author unknown
 * @TableName amp_flow_template_node
 */
@TableName(value ="apm_flow_template_node",autoResultMap = true)
@Data
public class ApmFlowTemplateNode implements Serializable {
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
     * 业务databid
     */
    private String dataBid;
    /**
     * 页面业务bid
     */
    private String webBid;

    /**
     * 前置节点bid集合
     */

    @TableField(typeHandler = com.baomidou.mybatisplus.extension.handlers.FastjsonTypeHandler.class)
    private List<String> beforeNodeBids;

    /**
     * 所属流程bid
     */
    private String flowTemplateBid;

    /**
     * 节点状态，0.未开始，1.进行中，2.完成
     */
    private Integer nodeState;

    /**
     * 所属流程版本号
     */
    private String version;

    /**
     * 节点名称
     */
    private String nodeName;

    /**
     * 节点对应的角色bids
     */
    @TableField(typeHandler = com.baomidou.mybatisplus.extension.handlers.FastjsonTypeHandler.class)
    private List<String> nodeRoleBids;

    /**
     * 模板节点是否允许删除，0.不允许，1.允许
     */
    private Boolean nodeDeleteFlag;

    /**
     * 允许删除的角色bids
     */
    @TableField(typeHandler = com.baomidou.mybatisplus.extension.handlers.FastjsonTypeHandler.class)
    private List<String> nodeDeleteRoleBids;

    /**
     * 节点类型，0.开始节点，1.进行中节点，2.结束节点
     */
    private Integer nodeType;

    /**
     * 完成方式，0.自动完成，1.单人确认完成，2.多人确认完成
     */
    private Integer complateType;

    /**
     * 排序
     */
    private Integer sort;


    /**
     * 是否显示帮助，0否，1是
     */
    private Boolean showHelpTipFlag;

    /**
     * 节点帮助提示内容
     */
    private String nodeHelpTip;

    /**
     * 是否关键路径，0否，1是
     */
    private Boolean keyPathFlag;

    /**
     * 是否允许回退，true允许，false不允许
     */
    private Boolean allowBack;

    /**
     * 是否允许回退 未完成节点，true允许，false不允许
     */
    private Boolean allowBackNotCompleted;

    /**
     * 完成操作授权角色bids
     */
    @TableField(typeHandler = com.baomidou.mybatisplus.extension.handlers.FastjsonTypeHandler.class)
    private List<String> complateRoleBids;

    @TableField(typeHandler = com.baomidou.mybatisplus.extension.handlers.FastjsonTypeHandler.class)
    private List<String> nextNodeBids;

    /**
     * 生命周期状态编码
     */
    private String lifeCycleCode;

    /**
     * 生命周期状态编码类型，1初始状态
     */
    private String lifeCycleCodeType;

    /**
     * 显示条件，1.默认可见，0.在某条件下可见
     */
    private Integer displayCondition;
    /**
     * 条件匹配形式，all.全部匹配，any.满足一个即可。
     */
    private String displayConditionMatch;

    /**
     * 当前节点激活，前面路径匹配方式，all.所有完成，any.任一完成
     */
    private String activeMatch;

    /**
     * 是否进行完成检查
     */
    private Boolean completeCheckFlag;

    /**
     * 完成检查事件
     */
    private String completeCheckEvent;

    /**
     * 节点位置前端使用
     */
    private String layout;

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
     * 通知标识
     */
    private Boolean notifyFlag;

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
        ApmFlowTemplateNode other = (ApmFlowTemplateNode) that;
        return (this.getId() == null ? other.getId() == null : this.getId().equals(other.getId()))
            && (this.getBid() == null ? other.getBid() == null : this.getBid().equals(other.getBid()))
            && (this.getBeforeNodeBids() == null ? other.getBeforeNodeBids() == null : this.getBeforeNodeBids().equals(other.getBeforeNodeBids()))
            && (this.getFlowTemplateBid() == null ? other.getFlowTemplateBid() == null : this.getFlowTemplateBid().equals(other.getFlowTemplateBid()))
            && (this.getNodeState() == null ? other.getNodeState() == null : this.getNodeState().equals(other.getNodeState()))
            && (this.getVersion() == null ? other.getVersion() == null : this.getVersion().equals(other.getVersion()))
            && (this.getNodeName() == null ? other.getNodeName() == null : this.getNodeName().equals(other.getNodeName()))
            && (this.getNodeRoleBids() == null ? other.getNodeRoleBids() == null : this.getNodeRoleBids().equals(other.getNodeRoleBids()))
            && (this.getNodeDeleteFlag() == null ? other.getNodeDeleteFlag() == null : this.getNodeDeleteFlag().equals(other.getNodeDeleteFlag()))
            && (this.getNodeDeleteRoleBids() == null ? other.getNodeDeleteRoleBids() == null : this.getNodeDeleteRoleBids().equals(other.getNodeDeleteRoleBids()))
            && (this.getNodeType() == null ? other.getNodeType() == null : this.getNodeType().equals(other.getNodeType()))
            && (this.getComplateType() == null ? other.getComplateType() == null : this.getComplateType().equals(other.getComplateType()))
            && (this.getComplateRoleBids() == null ? other.getComplateRoleBids() == null : this.getComplateRoleBids().equals(other.getComplateRoleBids()))
            && (this.getDisplayCondition() == null ? other.getDisplayCondition() == null : this.getDisplayCondition().equals(other.getDisplayCondition()))
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
        result = prime * result + ((getBeforeNodeBids() == null) ? 0 : getBeforeNodeBids().hashCode());
        result = prime * result + ((getFlowTemplateBid() == null) ? 0 : getFlowTemplateBid().hashCode());
        result = prime * result + ((getNodeState() == null) ? 0 : getNodeState().hashCode());
        result = prime * result + ((getVersion() == null) ? 0 : getVersion().hashCode());
        result = prime * result + ((getNodeName() == null) ? 0 : getNodeName().hashCode());
        result = prime * result + ((getNodeRoleBids() == null) ? 0 : getNodeRoleBids().hashCode());
        result = prime * result + ((getNodeDeleteFlag() == null) ? 0 : getNodeDeleteFlag().hashCode());
        result = prime * result + ((getNodeDeleteRoleBids() == null) ? 0 : getNodeDeleteRoleBids().hashCode());
        result = prime * result + ((getNodeType() == null) ? 0 : getNodeType().hashCode());
        result = prime * result + ((getComplateType() == null) ? 0 : getComplateType().hashCode());
        result = prime * result + ((getComplateRoleBids() == null) ? 0 : getComplateRoleBids().hashCode());
        result = prime * result + ((getDisplayCondition() == null) ? 0 : getDisplayCondition().hashCode());
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
        sb.append(", beforeNodeBids=").append(beforeNodeBids);
        sb.append(", flowTemplateBid=").append(flowTemplateBid);
        sb.append(", nodeState=").append(nodeState);
        sb.append(", version=").append(version);
        sb.append(", nodeName=").append(nodeName);
        sb.append(", nodeRoleBids=").append(nodeRoleBids);
        sb.append(", nodeDeleteFlag=").append(nodeDeleteFlag);
        sb.append(", nodeDeleteRoleBids=").append(nodeDeleteRoleBids);
        sb.append(", nodeType=").append(nodeType);
        sb.append(", complateType=").append(complateType);
        sb.append(", complateRoleBids=").append(complateRoleBids);
        sb.append(", displayCondition=").append(displayCondition);
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