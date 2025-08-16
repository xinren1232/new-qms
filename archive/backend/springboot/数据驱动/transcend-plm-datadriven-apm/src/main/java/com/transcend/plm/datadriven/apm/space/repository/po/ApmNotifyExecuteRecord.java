package com.transcend.plm.datadriven.apm.space.repository.po;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.io.Serializable;
import java.util.Date;
import lombok.Data;

/**
 * 通知执行记录表
 * @author unknown
 * @TableName apm_notify_execute_record
 */
@TableName(value ="apm_notify_execute_record")
@Data
public class ApmNotifyExecuteRecord implements Serializable {
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
     * 实例业务id
     */
    private String instanceBid;

    /**
     * 应用bid
     */
    private String spaceAppBid;

    /**
     * 模型编码
     */

    private String modelCode;

    /**
     * 通知主表业务id
     */
    private String notifyConfigBid;

    /**
     * 租户编码
     */
    private String tenantCode;

    /**
     * 通知人员
     */
    private Object notifyJobnumbers;

    /**
     * 通知时间
     */
    private Date nofifyTime;

    /**
     * 通知方式，1.飞书，2.邮件，3.飞书和邮件
     */
    private String notifyWay;

    /**
     * 是否立即通知，0否，1是
     */
    private Boolean nofifyNow;

    /**
     * 通知内容
     */
    private String notifyContent;

    /**
     * 通知标题
     */
    private String notifyTitle;

    /**
     * 详情url
     */
    private String url;

    /**
     * 通知结果，0未通知，1通知成功，2通知失败,3 待处理
     */
    private Integer nofifyResult;

    /**
     * 通知失败重试次数
     */
    private Integer nofifyRetryCount;

    /**
     * 通知结果说明
     */
    private String nofifyResultMsg;

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
        ApmNotifyExecuteRecord other = (ApmNotifyExecuteRecord) that;
        return (this.getId() == null ? other.getId() == null : this.getId().equals(other.getId()))
            && (this.getBid() == null ? other.getBid() == null : this.getBid().equals(other.getBid()))
            && (this.getNotifyConfigBid() == null ? other.getNotifyConfigBid() == null : this.getNotifyConfigBid().equals(other.getNotifyConfigBid()))
            && (this.getTenantCode() == null ? other.getTenantCode() == null : this.getTenantCode().equals(other.getTenantCode()))
            && (this.getNotifyJobnumbers() == null ? other.getNotifyJobnumbers() == null : this.getNotifyJobnumbers().equals(other.getNotifyJobnumbers()))
            && (this.getNofifyTime() == null ? other.getNofifyTime() == null : this.getNofifyTime().equals(other.getNofifyTime()))
            && (this.getNotifyWay() == null ? other.getNotifyWay() == null : this.getNotifyWay().equals(other.getNotifyWay()))
            && (this.getNofifyNow() == null ? other.getNofifyNow() == null : this.getNofifyNow().equals(other.getNofifyNow()))
            && (this.getNotifyContent() == null ? other.getNotifyContent() == null : this.getNotifyContent().equals(other.getNotifyContent()))
            && (this.getNofifyResult() == null ? other.getNofifyResult() == null : this.getNofifyResult().equals(other.getNofifyResult()))
            && (this.getNofifyRetryCount() == null ? other.getNofifyRetryCount() == null : this.getNofifyRetryCount().equals(other.getNofifyRetryCount()))
            && (this.getNofifyResultMsg() == null ? other.getNofifyResultMsg() == null : this.getNofifyResultMsg().equals(other.getNofifyResultMsg()))
            && (this.getEnableFlag() == null ? other.getEnableFlag() == null : this.getEnableFlag().equals(other.getEnableFlag()))
            && (this.getTenantId() == null ? other.getTenantId() == null : this.getTenantId().equals(other.getTenantId()))
            && (this.getCreatedBy() == null ? other.getCreatedBy() == null : this.getCreatedBy().equals(other.getCreatedBy()))
            && (this.getCreatedTime() == null ? other.getCreatedTime() == null : this.getCreatedTime().equals(other.getCreatedTime()))
            && (this.getUpdatedBy() == null ? other.getUpdatedBy() == null : this.getUpdatedBy().equals(other.getUpdatedBy()))
            && (this.getUpdatedTime() == null ? other.getUpdatedTime() == null : this.getUpdatedTime().equals(other.getUpdatedTime()))
            && (this.getDeleteFlag() == null ? other.getDeleteFlag() == null : this.getDeleteFlag().equals(other.getDeleteFlag()))
            && (this.getDescription() == null ? other.getDescription() == null : this.getDescription().equals(other.getDescription()));
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((getId() == null) ? 0 : getId().hashCode());
        result = prime * result + ((getBid() == null) ? 0 : getBid().hashCode());
        result = prime * result + ((getNotifyConfigBid() == null) ? 0 : getNotifyConfigBid().hashCode());
        result = prime * result + ((getTenantCode() == null) ? 0 : getTenantCode().hashCode());
        result = prime * result + ((getNotifyJobnumbers() == null) ? 0 : getNotifyJobnumbers().hashCode());
        result = prime * result + ((getNofifyTime() == null) ? 0 : getNofifyTime().hashCode());
        result = prime * result + ((getNotifyWay() == null) ? 0 : getNotifyWay().hashCode());
        result = prime * result + ((getNofifyNow() == null) ? 0 : getNofifyNow().hashCode());
        result = prime * result + ((getNotifyContent() == null) ? 0 : getNotifyContent().hashCode());
        result = prime * result + ((getNofifyResult() == null) ? 0 : getNofifyResult().hashCode());
        result = prime * result + ((getNofifyResultMsg() == null) ? 0 : getNofifyResultMsg().hashCode());
        result = prime * result + ((getEnableFlag() == null) ? 0 : getEnableFlag().hashCode());
        result = prime * result + ((getTenantId() == null) ? 0 : getTenantId().hashCode());
        result = prime * result + ((getCreatedBy() == null) ? 0 : getCreatedBy().hashCode());
        result = prime * result + ((getCreatedTime() == null) ? 0 : getCreatedTime().hashCode());
        result = prime * result + ((getUpdatedBy() == null) ? 0 : getUpdatedBy().hashCode());
        result = prime * result + ((getUpdatedTime() == null) ? 0 : getUpdatedTime().hashCode());
        result = prime * result + ((getDeleteFlag() == null) ? 0 : getDeleteFlag().hashCode());
        result = prime * result + ((getDescription() == null) ? 0 : getDescription().hashCode());
        result = prime * result + ((getNofifyRetryCount() == null) ? 0 : getNofifyRetryCount().hashCode());
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
        sb.append(", notifyConfigBid=").append(notifyConfigBid);
        sb.append(", tenantCode=").append(tenantCode);
        sb.append(", notifyJobnumbers=").append(notifyJobnumbers);
        sb.append(", nofifyTime=").append(nofifyTime);
        sb.append(", notifyWay=").append(notifyWay);
        sb.append(", nofifyNow=").append(nofifyNow);
        sb.append(", notifyContent=").append(notifyContent);
        sb.append(", nofifyResult=").append(nofifyResult);
        sb.append(", nofifyRetryCount=").append(nofifyRetryCount);
        sb.append(", nofifyResultMsg=").append(nofifyResultMsg);
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