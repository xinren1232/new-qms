package com.transcend.plm.datadriven.notify.domain;

import com.alibaba.fastjson.JSONArray;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;

import java.io.Serializable;
import java.util.Date;
import java.util.List;
import java.util.Map;

import com.baomidou.mybatisplus.extension.handlers.JacksonTypeHandler;
import lombok.Data;

/**
 * 通知操作类型配置表
 *
 * @author yuanhu.huang
 * @date 2024/07/24
 */
@TableName(value = "notify_config_operate", autoResultMap = true)
@Data
public class NotifyConfigOperate implements Serializable {
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
     * 业务驱动类型（OBJECT/FEATURE）
     */
    private String type;

    /**
     * 操作类型,CREATE.创建实例，UPDATE.修改实例，UPDATE_REL.修改关联属性,CREATE_OR_DELETE_REL.添加及删除直接子关系
     */
    private String operate;


    /**
     * 所选属性集合
     * 有常规属性和关联属性等
     */
    @TableField(typeHandler = com.baomidou.mybatisplus.extension.handlers.FastjsonTypeHandler.class)
    private List<String> operateAttrs;

    /**
     * 所选属性匹配值集合
     */
    @TableField(typeHandler = com.baomidou.mybatisplus.extension.handlers.FastjsonTypeHandler.class)
    private Map<String,ObjectAttrValue> operateAttrValues;

    /**
     * 操作额外的配置信息（比如type是对象的场景，还需要记录关系的相关内容）
     */
    @TableField(typeHandler = com.baomidou.mybatisplus.extension.handlers.FastjsonTypeHandler.class)
    private Object operateConfigInfo;

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
        NotifyConfigOperate other = (NotifyConfigOperate) that;
        return (this.getId() == null ? other.getId() == null : this.getId().equals(other.getId()))
                && (this.getBid() == null ? other.getBid() == null : this.getBid().equals(other.getBid()))
                && (this.getNotifyConfigBid() == null ? other.getNotifyConfigBid() == null : this.getNotifyConfigBid().equals(other.getNotifyConfigBid()))
                && (this.getType() == null ? other.getType() == null : this.getType().equals(other.getType()))
                && (this.getOperate() == null ? other.getOperate() == null : this.getOperate().equals(other.getOperate()))
                && (this.getOperateConfigInfo() == null ? other.getOperateConfigInfo() == null : this.getOperateConfigInfo().equals(other.getOperateConfigInfo()))
                && (this.getOperateAttrs() == null ? other.getOperateAttrs() == null : this.getOperateAttrs().equals(other.getOperateAttrs()))
                && (this.getEnableFlag() == null ? other.getEnableFlag() == null : this.getEnableFlag().equals(other.getEnableFlag()))
                && (this.getTenantId() == null ? other.getTenantId() == null : this.getTenantId().equals(other.getTenantId()))
                && (this.getCreatedBy() == null ? other.getCreatedBy() == null : this.getCreatedBy().equals(other.getCreatedBy()))
                && (this.getCreatedTime() == null ? other.getCreatedTime() == null : this.getCreatedTime().equals(other.getCreatedTime()))
                && (this.getUpdatedBy() == null ? other.getUpdatedBy() == null : this.getUpdatedBy().equals(other.getUpdatedBy()))
                && (this.getUpdatedTime() == null ? other.getUpdatedTime() == null : this.getUpdatedTime().equals(other.getUpdatedTime()))
                && (this.getDeleteFlag() == null ? other.getDeleteFlag() == null : this.getDeleteFlag().equals(other.getDeleteFlag()));
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
        result = prime * result + ((getType() == null) ? 0 : getType().hashCode());
        result = prime * result + ((getOperate() == null) ? 0 : getOperate().hashCode());
        result = prime * result + ((getOperateConfigInfo() == null) ? 0 : getOperateConfigInfo().hashCode());
        result = prime * result + ((getOperateAttrs() == null) ? 0 : getOperateAttrs().hashCode());
        result = prime * result + ((getEnableFlag() == null) ? 0 : getEnableFlag().hashCode());
        result = prime * result + ((getTenantId() == null) ? 0 : getTenantId().hashCode());
        result = prime * result + ((getCreatedBy() == null) ? 0 : getCreatedBy().hashCode());
        result = prime * result + ((getCreatedTime() == null) ? 0 : getCreatedTime().hashCode());
        result = prime * result + ((getUpdatedBy() == null) ? 0 : getUpdatedBy().hashCode());
        result = prime * result + ((getUpdatedTime() == null) ? 0 : getUpdatedTime().hashCode());
        result = prime * result + ((getDeleteFlag() == null) ? 0 : getDeleteFlag().hashCode());
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
        sb.append(", type=").append(type);
        sb.append(", operate=").append(operate);
        sb.append(", operateConfigInfo=").append(operateConfigInfo);
        sb.append(", operateAttrs=").append(operateAttrs);
        sb.append(", enableFlag=").append(enableFlag);
        sb.append(", tenantId=").append(tenantId);
        sb.append(", createdBy=").append(createdBy);
        sb.append(", createdTime=").append(createdTime);
        sb.append(", updatedBy=").append(updatedBy);
        sb.append(", updatedTime=").append(updatedTime);
        sb.append(", deleteFlag=").append(deleteFlag);
        sb.append(", serialVersionUID=").append(serialVersionUID);
        sb.append("]");
        return sb.toString();
    }
}