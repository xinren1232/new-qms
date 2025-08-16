package com.transcend.plm.datadriven.apm.space.repository.po;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.io.Serializable;
import java.util.Date;
import lombok.Data;

/**
 * 空间下对象应用表
 * @author unknown
 * @TableName apm_space_user_object
 */
@TableName(value ="apm_space_user_object")
@Data
public class ApmSpaceUserObject implements Serializable {
    /**
     * 主键ID
     */
    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 业务id
     */
    private String bid;

    /**
     * 空间业务id
     */
    private String spaceBid;

    /**
     * 模型code
     */
    private String modelCode;

    /**
     * 用户工号
     */
    private String userNo;

    /**
     * 查询表头支持动态定义配置
     */
    private Object headersDynamics;

    /**
     * 表头的冻结配置
     */
    private Object headersFreeze;

    /**
     * 表格的刷选功能
     */
    private Object headersScreen;

    /**
     * 表格的排序功能
     */
    private Object headersSort;

    /**
     * 创建时间
     */
    private Date createdTime;

    /**
     * 更新时间
     */
    private Date updatedTime;

    /**
     * 创建人
     */
    private String createdBy;

    /**
     * 更新人
     */
    private String updatedBy;

    /**
     * 租户ID
     */
    private Integer tenantId;

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
        ApmSpaceUserObject other = (ApmSpaceUserObject) that;
        return (this.getId() == null ? other.getId() == null : this.getId().equals(other.getId()))
            && (this.getBid() == null ? other.getBid() == null : this.getBid().equals(other.getBid()))
            && (this.getSpaceBid() == null ? other.getSpaceBid() == null : this.getSpaceBid().equals(other.getSpaceBid()))
            && (this.getModelCode() == null ? other.getModelCode() == null : this.getModelCode().equals(other.getModelCode()))
            && (this.getUserNo() == null ? other.getUserNo() == null : this.getUserNo().equals(other.getUserNo()))
            && (this.getHeadersDynamics() == null ? other.getHeadersDynamics() == null : this.getHeadersDynamics().equals(other.getHeadersDynamics()))
            && (this.getHeadersFreeze() == null ? other.getHeadersFreeze() == null : this.getHeadersFreeze().equals(other.getHeadersFreeze()))
            && (this.getHeadersScreen() == null ? other.getHeadersScreen() == null : this.getHeadersScreen().equals(other.getHeadersScreen()))
            && (this.getHeadersSort() == null ? other.getHeadersSort() == null : this.getHeadersSort().equals(other.getHeadersSort()))
            && (this.getCreatedTime() == null ? other.getCreatedTime() == null : this.getCreatedTime().equals(other.getCreatedTime()))
            && (this.getUpdatedTime() == null ? other.getUpdatedTime() == null : this.getUpdatedTime().equals(other.getUpdatedTime()))
            && (this.getCreatedBy() == null ? other.getCreatedBy() == null : this.getCreatedBy().equals(other.getCreatedBy()))
            && (this.getUpdatedBy() == null ? other.getUpdatedBy() == null : this.getUpdatedBy().equals(other.getUpdatedBy()))
            && (this.getTenantId() == null ? other.getTenantId() == null : this.getTenantId().equals(other.getTenantId()));
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((getId() == null) ? 0 : getId().hashCode());
        result = prime * result + ((getBid() == null) ? 0 : getBid().hashCode());
        result = prime * result + ((getSpaceBid() == null) ? 0 : getSpaceBid().hashCode());
        result = prime * result + ((getModelCode() == null) ? 0 : getModelCode().hashCode());
        result = prime * result + ((getUserNo() == null) ? 0 : getUserNo().hashCode());
        result = prime * result + ((getHeadersDynamics() == null) ? 0 : getHeadersDynamics().hashCode());
        result = prime * result + ((getHeadersFreeze() == null) ? 0 : getHeadersFreeze().hashCode());
        result = prime * result + ((getHeadersScreen() == null) ? 0 : getHeadersScreen().hashCode());
        result = prime * result + ((getHeadersSort() == null) ? 0 : getHeadersSort().hashCode());
        result = prime * result + ((getCreatedTime() == null) ? 0 : getCreatedTime().hashCode());
        result = prime * result + ((getUpdatedTime() == null) ? 0 : getUpdatedTime().hashCode());
        result = prime * result + ((getCreatedBy() == null) ? 0 : getCreatedBy().hashCode());
        result = prime * result + ((getUpdatedBy() == null) ? 0 : getUpdatedBy().hashCode());
        result = prime * result + ((getTenantId() == null) ? 0 : getTenantId().hashCode());
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
        sb.append(", modelCode=").append(modelCode);
        sb.append(", userNo=").append(userNo);
        sb.append(", headersDynamics=").append(headersDynamics);
        sb.append(", headersFreeze=").append(headersFreeze);
        sb.append(", headersScreen=").append(headersScreen);
        sb.append(", headersSort=").append(headersSort);
        sb.append(", createdTime=").append(createdTime);
        sb.append(", updatedTime=").append(updatedTime);
        sb.append(", createdBy=").append(createdBy);
        sb.append(", updatedBy=").append(updatedBy);
        sb.append(", tenantId=").append(tenantId);
        sb.append(", serialVersionUID=").append(serialVersionUID);
        sb.append("]");
        return sb.toString();
    }
}