package com.transcend.plm.datadriven.apm.flow.pi.domain;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.io.Serializable;
import java.util.Date;
import lombok.Data;

/**
 * @author unknown
 * @TableName pi_product_bt_config
 */
@TableName(value ="pi_product_bt_config")
@Data
public class PiProductBtConfig implements Serializable {
    /**
     * 主键
     */
    @TableId(type = IdType.AUTO)
    private Integer id;

    /**
     * 
     */
    private String bid;

    /**
     * 产品线编码
     */
    private String productLineCode;

    /**
     * 产品线名称
     */
    private String productLineName;

    /**
     * 产品经理工号
     */
    private String productManagerNumber;

    /**
     * 产品经理名称
     */
    private String productManagerName;

    /**
     * BT部门编码
     */
    private String btDepartmentCode;

    /**
     * BT部门名称
     */
    private String btDepartmentName;

    /**
     * 业务接口人
     */
    private String busInterfacePerson;

    /**
     * 启用标识
     */
    private Integer enableFlag;

    /**
     * 删除标识
     */
    private Integer deleteFlag;

    /**
     * 租户ID
     */
    private String tenantId;

    /**
     * 创建人
     */
    private String createdBy;

    /**
     * 更新人
     */
    private String updatedBy;

    /**
     * 创建时间
     */
    private Date createdTime;

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
        PiProductBtConfig other = (PiProductBtConfig) that;
        return (this.getId() == null ? other.getId() == null : this.getId().equals(other.getId()))
            && (this.getBid() == null ? other.getBid() == null : this.getBid().equals(other.getBid()))
            && (this.getProductLineCode() == null ? other.getProductLineCode() == null : this.getProductLineCode().equals(other.getProductLineCode()))
            && (this.getProductLineName() == null ? other.getProductLineName() == null : this.getProductLineName().equals(other.getProductLineName()))
            && (this.getProductManagerNumber() == null ? other.getProductManagerNumber() == null : this.getProductManagerNumber().equals(other.getProductManagerNumber()))
            && (this.getProductManagerName() == null ? other.getProductManagerName() == null : this.getProductManagerName().equals(other.getProductManagerName()))
            && (this.getBtDepartmentCode() == null ? other.getBtDepartmentCode() == null : this.getBtDepartmentCode().equals(other.getBtDepartmentCode()))
            && (this.getBtDepartmentName() == null ? other.getBtDepartmentName() == null : this.getBtDepartmentName().equals(other.getBtDepartmentName()))
            && (this.getBusInterfacePerson() == null ? other.getBusInterfacePerson() == null : this.getBusInterfacePerson().equals(other.getBusInterfacePerson()))
            && (this.getEnableFlag() == null ? other.getEnableFlag() == null : this.getEnableFlag().equals(other.getEnableFlag()))
            && (this.getDeleteFlag() == null ? other.getDeleteFlag() == null : this.getDeleteFlag().equals(other.getDeleteFlag()))
            && (this.getTenantId() == null ? other.getTenantId() == null : this.getTenantId().equals(other.getTenantId()))
            && (this.getCreatedBy() == null ? other.getCreatedBy() == null : this.getCreatedBy().equals(other.getCreatedBy()))
            && (this.getUpdatedBy() == null ? other.getUpdatedBy() == null : this.getUpdatedBy().equals(other.getUpdatedBy()))
            && (this.getCreatedTime() == null ? other.getCreatedTime() == null : this.getCreatedTime().equals(other.getCreatedTime()))
            && (this.getUpdatedTime() == null ? other.getUpdatedTime() == null : this.getUpdatedTime().equals(other.getUpdatedTime()));
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((getId() == null) ? 0 : getId().hashCode());
        result = prime * result + ((getBid() == null) ? 0 : getBid().hashCode());
        result = prime * result + ((getProductLineCode() == null) ? 0 : getProductLineCode().hashCode());
        result = prime * result + ((getProductLineName() == null) ? 0 : getProductLineName().hashCode());
        result = prime * result + ((getProductManagerNumber() == null) ? 0 : getProductManagerNumber().hashCode());
        result = prime * result + ((getProductManagerName() == null) ? 0 : getProductManagerName().hashCode());
        result = prime * result + ((getBtDepartmentCode() == null) ? 0 : getBtDepartmentCode().hashCode());
        result = prime * result + ((getBtDepartmentName() == null) ? 0 : getBtDepartmentName().hashCode());
        result = prime * result + ((getBusInterfacePerson() == null) ? 0 : getBusInterfacePerson().hashCode());
        result = prime * result + ((getEnableFlag() == null) ? 0 : getEnableFlag().hashCode());
        result = prime * result + ((getDeleteFlag() == null) ? 0 : getDeleteFlag().hashCode());
        result = prime * result + ((getTenantId() == null) ? 0 : getTenantId().hashCode());
        result = prime * result + ((getCreatedBy() == null) ? 0 : getCreatedBy().hashCode());
        result = prime * result + ((getUpdatedBy() == null) ? 0 : getUpdatedBy().hashCode());
        result = prime * result + ((getCreatedTime() == null) ? 0 : getCreatedTime().hashCode());
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
        sb.append(", productLineCode=").append(productLineCode);
        sb.append(", productLineName=").append(productLineName);
        sb.append(", productManagerNumber=").append(productManagerNumber);
        sb.append(", productManagerName=").append(productManagerName);
        sb.append(", btDepartmentCode=").append(btDepartmentCode);
        sb.append(", btDepartmentName=").append(btDepartmentName);
        sb.append(", busInterfacePerson=").append(busInterfacePerson);
        sb.append(", enableFlag=").append(enableFlag);
        sb.append(", deleteFlag=").append(deleteFlag);
        sb.append(", tenantId=").append(tenantId);
        sb.append(", createdBy=").append(createdBy);
        sb.append(", updatedBy=").append(updatedBy);
        sb.append(", createdTime=").append(createdTime);
        sb.append(", updatedTime=").append(updatedTime);
        sb.append(", serialVersionUID=").append(serialVersionUID);
        sb.append("]");
        return sb.toString();
    }
}