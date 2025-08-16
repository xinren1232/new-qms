package com.transcend.plm.datadriven.apm.permission.repository.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;
import java.util.Date;

/**
 * 权限规则操作明细表
 * @author unknown
 * @TableName permission_plm_operation_rule_item
 */
@TableName(value ="permission_plm_operation_rule_item")
@Data
public class PermissionPlmOperationRuleItem implements Serializable {
    /**
     * 自增主键id
     */
    @TableId(type = IdType.AUTO)
    private Integer id;

    /**
     * 业务id
     */
    private String bid;

    /**
     * 权限规则bid
     */
    private String permissionBid;

    /**
     * 角色编码，分为系统全局用户穿透角色，系统角色（人员不穿透），空间自定义角色，自定义角色
     */
    private String roleCode;
    /**
     * 角色类型 0，空间角色，1内置角色，2私有角色
     */
    private int roleType;

    /**
     * 来源，如果是重对象拷贝，则带上对象ITEM的BID，否则带上自己的BID
     */
    private String path;

    /**
     * 详情查看，1有，0没用
     */
    private Boolean operationDetail;

    /**
     * 编辑操作（包括检入，检出，草稿保存），1有，0没用
     */
    private Boolean operationEdit;

    /**
     * 删除操作，1有，0没用
     */
    private Boolean operationDelete;

    /**
     * 修订，1有，0没有
     */
    private Boolean operationRevise;

    /**
     * 提升，1有，0没有
     */
    private Boolean operationPromote;
    /**
     * 移动，1有，0没有
     */
    private Boolean operationMove;

    /**
     * 视图模式-列表，1有，0没有
     */
    private Boolean modelViewTableView; //

    /**
     * 视图模式-树，1有，0没有
     */
    private Boolean modelViewTreeView; //

    /**
     * 视图模式-多维表格，1有，0没有
     */
    private Boolean modelViewMultiTreeView; //

    /**
     * 视图模式-概览，1有，0没有
     */
    private Boolean modelViewOverview; //

    /**
     * 视图模式-度量报表，1有，0没有
     */
    private Boolean modelViewMeasureReport; //

    /**
     * 视图模式-看板，1有，0没有
     */
    private Boolean modelViewKanbanView; //

    /**
     * 视图模式-甘特图，1有，0没有
     */
    private Boolean modelViewGanttView; //

    /**
     * 加锁，1有，0没有
     */
    private Boolean instanceLock; //


    /**
     * 解锁，1有，0没有
     */
    private Boolean instanceUnlock; //


    /**
     * 同步，1有，0没有
     */
    private Boolean sync; //



    /**
     * 视图模式-全，1有，0没有
     */
    private Boolean modelViewAll; //

    /**
     * 扩展操作，1有，0没有
     */
    @TableField(value = "operation_1")
    private Boolean operationImport;

    /**
     * 扩展操作，1有，0没有
     */
    @TableField(value = "operation_2")
    private Boolean operationExport;

    /**
     * 扩展操作，1有，0没有
     */
    @TableField(value = "operation_3")
    private Boolean operation3;

    /**
     * 扩展操作，1有，0没有
     */
    @TableField(value = "operation_4")
    private Boolean operation4;

    /**
     * 扩展操作，1有，0没有
     */
    @TableField(value = "operation_5")
    private Boolean operation5;

    /**
     * 扩展操作，1有，0没有
     */
    @TableField(value = "operation_6")
    private Boolean operation6;

    /**
     * 扩展操作，1有，0没有
     */
    @TableField(value = "operation_7")
    private Boolean operation7;

    /**
     * 扩展操作，1有，0没有
     */
    @TableField(value = "operation_8")
    private Boolean operation8;

    /**
     * 扩展操作，1有，0没有
     */
    @TableField(value = "operation_9")
    private Boolean operation9;

    /**
     * 扩展操作，1有，0没有
     */
    @TableField(value = "operation_10")
    private Boolean operation10;

    /**
     * 字符串扩展操作，1有，0没有
     */
    private String operationStr;

    /**
     * 启用标志，0未启用，1启用，2禁用
     */
    private Boolean enableFlag;

    /**
     * 删除标志
     */
    private Boolean deleteFlag;

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
        PermissionPlmOperationRuleItem other = (PermissionPlmOperationRuleItem) that;
        return (this.getId() == null ? other.getId() == null : this.getId().equals(other.getId()))
            && (this.getBid() == null ? other.getBid() == null : this.getBid().equals(other.getBid()))
            && (this.getPermissionBid() == null ? other.getPermissionBid() == null : this.getPermissionBid().equals(other.getPermissionBid()))
            && (this.getRoleCode() == null ? other.getRoleCode() == null : this.getRoleCode().equals(other.getRoleCode()))
            && (this.getPath() == null ? other.getPath() == null : this.getPath().equals(other.getPath()))
            && (this.getOperationDetail() == null ? other.getOperationDetail() == null : this.getOperationDetail().equals(other.getOperationDetail()))
            && (this.getOperationEdit() == null ? other.getOperationEdit() == null : this.getOperationEdit().equals(other.getOperationEdit()))
            && (this.getOperationDelete() == null ? other.getOperationDelete() == null : this.getOperationDelete().equals(other.getOperationDelete()))
            && (this.getOperationRevise() == null ? other.getOperationRevise() == null : this.getOperationRevise().equals(other.getOperationRevise()))
            && (this.getOperationPromote() == null ? other.getOperationPromote() == null : this.getOperationPromote().equals(other.getOperationPromote()))
            && (this.getOperationMove() == null ? other.getOperationMove() == null : this.getOperationMove().equals(other.getOperationMove()))
            && (this.getOperationImport() == null ? other.getOperationImport() == null : this.getOperationImport().equals(other.getOperationImport()))
            && (this.getOperationExport() == null ? other.getOperationExport() == null : this.getOperationExport().equals(other.getOperationExport()))
            && (this.getOperation3() == null ? other.getOperation3() == null : this.getOperation3().equals(other.getOperation3()))
            && (this.getOperation4() == null ? other.getOperation4() == null : this.getOperation4().equals(other.getOperation4()))
            && (this.getOperation5() == null ? other.getOperation5() == null : this.getOperation5().equals(other.getOperation5()))
            && (this.getOperation6() == null ? other.getOperation6() == null : this.getOperation6().equals(other.getOperation6()))
            && (this.getOperation7() == null ? other.getOperation7() == null : this.getOperation7().equals(other.getOperation7()))
            && (this.getOperation8() == null ? other.getOperation8() == null : this.getOperation8().equals(other.getOperation8()))
            && (this.getOperation9() == null ? other.getOperation9() == null : this.getOperation9().equals(other.getOperation9()))
            && (this.getOperation10() == null ? other.getOperation10() == null : this.getOperation10().equals(other.getOperation10()))
            && (this.getOperationStr() == null ? other.getOperationStr() == null : this.getOperationStr().equals(other.getOperationStr()))
            && (this.getEnableFlag() == null ? other.getEnableFlag() == null : this.getEnableFlag().equals(other.getEnableFlag()))
            && (this.getDeleteFlag() == null ? other.getDeleteFlag() == null : this.getDeleteFlag().equals(other.getDeleteFlag()))
            && (this.getTenantId() == null ? other.getTenantId() == null : this.getTenantId().equals(other.getTenantId()))
            && (this.getCreatedBy() == null ? other.getCreatedBy() == null : this.getCreatedBy().equals(other.getCreatedBy()))
            && (this.getCreatedTime() == null ? other.getCreatedTime() == null : this.getCreatedTime().equals(other.getCreatedTime()))
            && (this.getUpdatedBy() == null ? other.getUpdatedBy() == null : this.getUpdatedBy().equals(other.getUpdatedBy()))
            && (this.getUpdatedTime() == null ? other.getUpdatedTime() == null : this.getUpdatedTime().equals(other.getUpdatedTime()));
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((getId() == null) ? 0 : getId().hashCode());
        result = prime * result + ((getBid() == null) ? 0 : getBid().hashCode());
        result = prime * result + ((getPermissionBid() == null) ? 0 : getPermissionBid().hashCode());
        result = prime * result + ((getRoleCode() == null) ? 0 : getRoleCode().hashCode());
        result = prime * result + ((getPath() == null) ? 0 : getPath().hashCode());
        result = prime * result + ((getOperationDetail() == null) ? 0 : getOperationDetail().hashCode());
        result = prime * result + ((getOperationEdit() == null) ? 0 : getOperationEdit().hashCode());
        result = prime * result + ((getOperationDelete() == null) ? 0 : getOperationDelete().hashCode());
        result = prime * result + ((getOperationRevise() == null) ? 0 : getOperationRevise().hashCode());
        result = prime * result + ((getOperationPromote() == null) ? 0 : getOperationPromote().hashCode());
        result = prime * result + ((getOperationMove() == null) ? 0 : getOperationMove().hashCode());
        result = prime * result + ((getOperationImport() == null) ? 0 : getOperationImport().hashCode());
        result = prime * result + ((getOperationExport() == null) ? 0 : getOperationExport().hashCode());
        result = prime * result + ((getOperation3() == null) ? 0 : getOperation3().hashCode());
        result = prime * result + ((getOperation4() == null) ? 0 : getOperation4().hashCode());
        result = prime * result + ((getOperation5() == null) ? 0 : getOperation5().hashCode());
        result = prime * result + ((getOperation6() == null) ? 0 : getOperation6().hashCode());
        result = prime * result + ((getOperation7() == null) ? 0 : getOperation7().hashCode());
        result = prime * result + ((getOperation8() == null) ? 0 : getOperation8().hashCode());
        result = prime * result + ((getOperation9() == null) ? 0 : getOperation9().hashCode());
        result = prime * result + ((getOperation10() == null) ? 0 : getOperation10().hashCode());
        result = prime * result + ((getOperationStr() == null) ? 0 : getOperationStr().hashCode());
        result = prime * result + ((getEnableFlag() == null) ? 0 : getEnableFlag().hashCode());
        result = prime * result + ((getDeleteFlag() == null) ? 0 : getDeleteFlag().hashCode());
        result = prime * result + ((getTenantId() == null) ? 0 : getTenantId().hashCode());
        result = prime * result + ((getCreatedBy() == null) ? 0 : getCreatedBy().hashCode());
        result = prime * result + ((getCreatedTime() == null) ? 0 : getCreatedTime().hashCode());
        result = prime * result + ((getUpdatedBy() == null) ? 0 : getUpdatedBy().hashCode());
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
        sb.append(", permissionBid=").append(permissionBid);
        sb.append(", roleCode=").append(roleCode);
        sb.append(", path=").append(path);
        sb.append(", operationDetail=").append(operationDetail);
        sb.append(", operationEdit=").append(operationEdit);
        sb.append(", operationDelete=").append(operationDelete);
        sb.append(", operationRevise=").append(operationRevise);
        sb.append(", operationPromote=").append(operationPromote);
        sb.append(", operationMove=").append(operationMove);
        sb.append(", operation1=").append(operationImport);
        sb.append(", operation2=").append(operationExport);
        sb.append(", operation3=").append(operation3);
        sb.append(", operation4=").append(operation4);
        sb.append(", operation5=").append(operation5);
        sb.append(", operation6=").append(operation6);
        sb.append(", operation7=").append(operation7);
        sb.append(", operation8=").append(operation8);
        sb.append(", operation9=").append(operation9);
        sb.append(", operation10=").append(operation10);
        sb.append(", operationStr=").append(operationStr);
        sb.append(", enableFlag=").append(enableFlag);
        sb.append(", deleteFlag=").append(deleteFlag);
        sb.append(", tenantId=").append(tenantId);
        sb.append(", createdBy=").append(createdBy);
        sb.append(", createdTime=").append(createdTime);
        sb.append(", updatedBy=").append(updatedBy);
        sb.append(", updatedTime=").append(updatedTime);
        sb.append(", serialVersionUID=").append(serialVersionUID);
        sb.append("]");
        return sb.toString();
    }
}