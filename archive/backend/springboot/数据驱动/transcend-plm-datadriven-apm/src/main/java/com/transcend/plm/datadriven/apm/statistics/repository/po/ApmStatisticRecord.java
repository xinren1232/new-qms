package com.transcend.plm.datadriven.apm.statistics.repository.po;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Builder;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDate;
import java.util.Date;

/**
 * 统计记录表
 *
 * @author unknown
 * @TableName apm_statistic_record
 */
@TableName(value = "apm_statistic_record")
@Data
@Builder
public class ApmStatisticRecord implements Serializable {
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
     * 统计业务
     */
    private String statisticBiz;

    /**
     * 统计类型
     */
    private String statisticType;

    /**
     * 业务bid
     */
    private String bizBid;

    /**
     * 统计时间
     */
    private LocalDate statisticDate;

    /**
     * 理想值
     */
    private String idealValue;

    /**
     * 实际值
     */
    private String actualValue;

    /**
     * 自定义值(json)
     */
    private String customValue;

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
        ApmStatisticRecord other = (ApmStatisticRecord) that;
        return (this.getId() == null ? other.getId() == null : this.getId().equals(other.getId()))
                && (this.getBid() == null ? other.getBid() == null : this.getBid().equals(other.getBid()))
                && (this.getStatisticBiz() == null ? other.getStatisticBiz() == null : this.getStatisticBiz().equals(other.getStatisticBiz()))
                && (this.getStatisticType() == null ? other.getStatisticType() == null : this.getStatisticType().equals(other.getStatisticType()))
                && (this.getBizBid() == null ? other.getBizBid() == null : this.getBizBid().equals(other.getBizBid()))
                && (this.getStatisticDate() == null ? other.getStatisticDate() == null : this.getStatisticDate().equals(other.getStatisticDate()))
                && (this.getIdealValue() == null ? other.getIdealValue() == null : this.getIdealValue().equals(other.getIdealValue()))
                && (this.getActualValue() == null ? other.getActualValue() == null : this.getActualValue().equals(other.getActualValue()))
                && (this.getCustomValue() == null ? other.getCustomValue() == null : this.getCustomValue().equals(other.getCustomValue()))
                && (this.getTenantId() == null ? other.getTenantId() == null : this.getTenantId().equals(other.getTenantId()))
                && (this.getCreatedBy() == null ? other.getCreatedBy() == null : this.getCreatedBy().equals(other.getCreatedBy()))
                && (this.getCreatedTime() == null ? other.getCreatedTime() == null : this.getCreatedTime().equals(other.getCreatedTime()))
                && (this.getUpdatedBy() == null ? other.getUpdatedBy() == null : this.getUpdatedBy().equals(other.getUpdatedBy()))
                && (this.getUpdatedTime() == null ? other.getUpdatedTime() == null : this.getUpdatedTime().equals(other.getUpdatedTime()))
                && (this.getDeleteFlag() == null ? other.getDeleteFlag() == null : this.getDeleteFlag().equals(other.getDeleteFlag()));
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((getId() == null) ? 0 : getId().hashCode());
        result = prime * result + ((getBid() == null) ? 0 : getBid().hashCode());
        result = prime * result + ((getStatisticBiz() == null) ? 0 : getStatisticBiz().hashCode());
        result = prime * result + ((getStatisticType() == null) ? 0 : getStatisticType().hashCode());
        result = prime * result + ((getBizBid() == null) ? 0 : getBizBid().hashCode());
        result = prime * result + ((getStatisticDate() == null) ? 0 : getStatisticDate().hashCode());
        result = prime * result + ((getIdealValue() == null) ? 0 : getIdealValue().hashCode());
        result = prime * result + ((getActualValue() == null) ? 0 : getActualValue().hashCode());
        result = prime * result + ((getCustomValue() == null) ? 0 : getCustomValue().hashCode());
        result = prime * result + ((getTenantId() == null) ? 0 : getTenantId().hashCode());
        result = prime * result + ((getCreatedBy() == null) ? 0 : getCreatedBy().hashCode());
        result = prime * result + ((getCreatedTime() == null) ? 0 : getCreatedTime().hashCode());
        result = prime * result + ((getUpdatedBy() == null) ? 0 : getUpdatedBy().hashCode());
        result = prime * result + ((getUpdatedTime() == null) ? 0 : getUpdatedTime().hashCode());
        result = prime * result + ((getDeleteFlag() == null) ? 0 : getDeleteFlag().hashCode());
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
        sb.append(", statisticBiz=").append(statisticBiz);
        sb.append(", statisticType=").append(statisticType);
        sb.append(", bizBid=").append(bizBid);
        sb.append(", statisticDate=").append(statisticDate);
        sb.append(", idealValue=").append(idealValue);
        sb.append(", actualValue=").append(actualValue);
        sb.append(", customValue=").append(customValue);
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