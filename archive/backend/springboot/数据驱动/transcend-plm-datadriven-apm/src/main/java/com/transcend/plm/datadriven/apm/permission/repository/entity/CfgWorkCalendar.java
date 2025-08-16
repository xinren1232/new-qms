package com.transcend.plm.datadriven.apm.permission.repository.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;
import java.util.Date;

/**
 * @author unknown
 * sap工作日历
 * @TableName cfg_work_calendar
 */
@TableName(value ="cfg_work_calendar")
@Data
public class CfgWorkCalendar implements Serializable {
    /**
     * 序号,uuid
     */
    @TableId
    private String id;

    /**
     * 年度
     */
    private Integer zkjahr;

    /**
     * 月份
     */
    private Integer zmonat;

    /**
     * 日
     */
    private Integer zdate;

    /**
     * 工作/休息
     */
    private String ztpr;

    /**
     * 工作时间
     */
    private Date workTime;

    /**
     * 创建时间
     */
    private Date creationTime;

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
        CfgWorkCalendar other = (CfgWorkCalendar) that;
        return (this.getId() == null ? other.getId() == null : this.getId().equals(other.getId()))
            && (this.getZkjahr() == null ? other.getZkjahr() == null : this.getZkjahr().equals(other.getZkjahr()))
            && (this.getZmonat() == null ? other.getZmonat() == null : this.getZmonat().equals(other.getZmonat()))
            && (this.getZdate() == null ? other.getZdate() == null : this.getZdate().equals(other.getZdate()))
            && (this.getZtpr() == null ? other.getZtpr() == null : this.getZtpr().equals(other.getZtpr()))
            && (this.getCreationTime() == null ? other.getCreationTime() == null : this.getCreationTime().equals(other.getCreationTime()));
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((getId() == null) ? 0 : getId().hashCode());
        result = prime * result + ((getZkjahr() == null) ? 0 : getZkjahr().hashCode());
        result = prime * result + ((getZmonat() == null) ? 0 : getZmonat().hashCode());
        result = prime * result + ((getZdate() == null) ? 0 : getZdate().hashCode());
        result = prime * result + ((getZtpr() == null) ? 0 : getZtpr().hashCode());
        result = prime * result + ((getCreationTime() == null) ? 0 : getCreationTime().hashCode());
        return result;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(getClass().getSimpleName());
        sb.append(" [");
        sb.append("Hash = ").append(hashCode());
        sb.append(", id=").append(id);
        sb.append(", zkjahr=").append(zkjahr);
        sb.append(", zmonat=").append(zmonat);
        sb.append(", zdate=").append(zdate);
        sb.append(", ztpr=").append(ztpr);
        sb.append(", creationTime=").append(creationTime);
        sb.append(", serialVersionUID=").append(serialVersionUID);
        sb.append("]");
        return sb.toString();
    }
}