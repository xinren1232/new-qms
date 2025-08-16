package com.transcend.plm.datadriven.apm.space.repository.po;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.io.Serializable;
import java.util.Date;
import java.util.List;
import java.util.Map;

import lombok.Data;

/**
 * 视图配置表
 * @author unknown
 * @TableName apm_app_view_config
 */
@TableName(value ="apm_app_view_config",autoResultMap = true)
@Data
public class ApmAppViewConfig implements Serializable {
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
     * 空间BID
     */
    private String spaceBid;

    /**
     * 应用BID
     */
    private String spaceAppBid;


    /**
     * tabBID
     */
    private String tabBid;

    /**
     * 视图名称
     */
    private String viewName;

    /**
     * 视图描述
     */
    private String viewDesc;
    /**
     * 排序
     */
    private int sort;

    /**
     * 视图类型，1.条件视图，2.导航视图
     */
    private Integer viewType;
    /**
     * 导航应用BID
     */
    private String navSpaceAppBid;

    /**
     * 分享视图配置BID
     */
    private String shareViewConfigBid;

    /**
     * 导航显示编码，tableView：表格，multiTreeView：多维表格，treeView：层级
     */
    private String navCode;

    /**
     * 导航视图配置内容（各个模式自定义）
     */
    @TableField(value = "nav_config_content", typeHandler = com.baomidou.mybatisplus.extension.handlers.JacksonTypeHandler.class)
    private Map<String, Object> navConfigContent;

    /**
     * 导航视图和源对象属性映射关系,KEY 为导航对象bid,value为目标对象属性
     */
    @TableField(typeHandler = com.baomidou.mybatisplus.extension.handlers.FastjsonTypeHandler.class)
    private Map<String,String> navAttrConfig;

    /**
     * 显示类型，labe.标签显示，menu.菜单显示
     */
    private String showType;

    /**
     * 视图配置条件
     */
    @TableField(typeHandler = com.baomidou.mybatisplus.extension.handlers.FastjsonTypeHandler.class)
    private Map<String,Object> viewCondition;

    /**
     * 协作者
     */
    @TableField(typeHandler = com.baomidou.mybatisplus.extension.handlers.FastjsonTypeHandler.class)
    private List<String> teamWorkers;

    /**
     * 空间角色bid
     */
    @TableField(typeHandler = com.baomidou.mybatisplus.extension.handlers.FastjsonTypeHandler.class)
    private List<String> spaceRoleBids;

    /**
     * 部门id
     */
    @TableField(typeHandler = com.baomidou.mybatisplus.extension.handlers.FastjsonTypeHandler.class)
    private List<String> departmentIds;

    /**
     * 人员id
     */
    @TableField(typeHandler = com.baomidou.mybatisplus.extension.handlers.FastjsonTypeHandler.class)
    private List<String> userIds;

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
        ApmAppViewConfig other = (ApmAppViewConfig) that;
        return (this.getId() == null ? other.getId() == null : this.getId().equals(other.getId()))
            && (this.getBid() == null ? other.getBid() == null : this.getBid().equals(other.getBid()))
            && (this.getSpaceBid() == null ? other.getSpaceBid() == null : this.getSpaceBid().equals(other.getSpaceBid()))
            && (this.getSpaceAppBid() == null ? other.getSpaceAppBid() == null : this.getSpaceAppBid().equals(other.getSpaceAppBid()))
            && (this.getViewName() == null ? other.getViewName() == null : this.getViewName().equals(other.getViewName()))
            && (this.getViewDesc() == null ? other.getViewDesc() == null : this.getViewDesc().equals(other.getViewDesc()))
            && (this.getViewType() == null ? other.getViewType() == null : this.getViewType().equals(other.getViewType()))
            && (this.getShowType() == null ? other.getShowType() == null : this.getShowType().equals(other.getShowType()))
            && (this.getViewCondition() == null ? other.getViewCondition() == null : this.getViewCondition().equals(other.getViewCondition()))
            && (this.getTeamWorkers() == null ? other.getTeamWorkers() == null : this.getTeamWorkers().equals(other.getTeamWorkers()))
            && (this.getSpaceRoleBids() == null ? other.getSpaceRoleBids() == null : this.getSpaceRoleBids().equals(other.getSpaceRoleBids()))
            && (this.getDepartmentIds() == null ? other.getDepartmentIds() == null : this.getDepartmentIds().equals(other.getDepartmentIds()))
            && (this.getUserIds() == null ? other.getUserIds() == null : this.getUserIds().equals(other.getUserIds()))
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
        result = prime * result + ((getSpaceBid() == null) ? 0 : getSpaceBid().hashCode());
        result = prime * result + ((getSpaceAppBid() == null) ? 0 : getSpaceAppBid().hashCode());
        result = prime * result + ((getViewName() == null) ? 0 : getViewName().hashCode());
        result = prime * result + ((getViewDesc() == null) ? 0 : getViewDesc().hashCode());
        result = prime * result + ((getViewType() == null) ? 0 : getViewType().hashCode());
        result = prime * result + ((getShowType() == null) ? 0 : getShowType().hashCode());
        result = prime * result + ((getViewCondition() == null) ? 0 : getViewCondition().hashCode());
        result = prime * result + ((getTeamWorkers() == null) ? 0 : getTeamWorkers().hashCode());
        result = prime * result + ((getSpaceRoleBids() == null) ? 0 : getSpaceRoleBids().hashCode());
        result = prime * result + ((getDepartmentIds() == null) ? 0 : getDepartmentIds().hashCode());
        result = prime * result + ((getUserIds() == null) ? 0 : getUserIds().hashCode());
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
        sb.append(", spaceBid=").append(spaceBid);
        sb.append(", spaceAppBid=").append(spaceAppBid);
        sb.append(", viewName=").append(viewName);
        sb.append(", viewDesc=").append(viewDesc);
        sb.append(", viewType=").append(viewType);
        sb.append(", showType=").append(showType);
        sb.append(", viewCondition=").append(viewCondition);
        sb.append(", teamWorkers=").append(teamWorkers);
        sb.append(", spaceRoleBids=").append(spaceRoleBids);
        sb.append(", departmentIds=").append(departmentIds);
        sb.append(", userIds=").append(userIds);
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