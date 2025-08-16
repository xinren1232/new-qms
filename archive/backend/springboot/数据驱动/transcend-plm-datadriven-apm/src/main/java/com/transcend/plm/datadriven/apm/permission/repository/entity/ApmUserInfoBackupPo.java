package com.transcend.plm.datadriven.apm.permission.repository.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.util.Date;

/**
 * 本地用户备份表,用于BI视图展现
 * TableName apm_user_info_backup
 *
 * @author xin.wu2
 */
@Data
@TableName(value = "apm_user_info_backup")
public class ApmUserInfoBackupPo {
    /**
     * 工号
     */
    @TableId(value = "id")
    private String id;

    /**
     * 人员姓名
     */
    @TableField(value = "name")
    private String name;

    /**
     * 一级部门id
     */
    @TableField(value = "level_1_department_id")
    private String level1DepartmentId;

    /**
     * 一级部门id
     */
    @TableField(value = "level_1_department_name")
    private String level1DepartmentName;

    /**
     * 二级部门id
     */
    @TableField(value = "level_2_department_id")
    private String level2DepartmentId;

    /**
     * 二级部门id
     */
    @TableField(value = "level_2_department_name")
    private String level2DepartmentName;

    /**
     * 三级部门id
     */
    @TableField(value = "level_3_department_id")
    private String level3DepartmentId;

    /**
     * 三级部门id
     */
    @TableField(value = "level_3_department_name")
    private String level3DepartmentName;

    /**
     * 创建时间
     */
    @TableField(value = "created_time")
    private Date createdTime;

    /**
     * 更新时间
     */
    @TableField(value = "updated_time")
    private Date updatedTime;

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
        ApmUserInfoBackupPo other = (ApmUserInfoBackupPo) that;
        return (this.getId() == null ? other.getId() == null : this.getId().equals(other.getId()))
                && (this.getName() == null ? other.getName() == null : this.getName().equals(other.getName()))
                && (this.getLevel1DepartmentId() == null ? other.getLevel1DepartmentId() == null : this.getLevel1DepartmentId().equals(other.getLevel1DepartmentId()))
                && (this.getLevel1DepartmentName() == null ? other.getLevel1DepartmentName() == null : this.getLevel1DepartmentName().equals(other.getLevel1DepartmentName()))
                && (this.getLevel2DepartmentId() == null ? other.getLevel2DepartmentId() == null : this.getLevel2DepartmentId().equals(other.getLevel2DepartmentId()))
                && (this.getLevel2DepartmentName() == null ? other.getLevel2DepartmentName() == null : this.getLevel2DepartmentName().equals(other.getLevel2DepartmentName()))
                && (this.getLevel3DepartmentId() == null ? other.getLevel3DepartmentId() == null : this.getLevel3DepartmentId().equals(other.getLevel3DepartmentId()))
                && (this.getLevel3DepartmentName() == null ? other.getLevel3DepartmentName() == null : this.getLevel3DepartmentName().equals(other.getLevel3DepartmentName()))
                && (this.getCreatedTime() == null ? other.getCreatedTime() == null : this.getCreatedTime().equals(other.getCreatedTime()))
                && (this.getUpdatedTime() == null ? other.getUpdatedTime() == null : this.getUpdatedTime().equals(other.getUpdatedTime()));
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((getId() == null) ? 0 : getId().hashCode());
        result = prime * result + ((getName() == null) ? 0 : getName().hashCode());
        result = prime * result + ((getLevel1DepartmentId() == null) ? 0 : getLevel1DepartmentId().hashCode());
        result = prime * result + ((getLevel1DepartmentName() == null) ? 0 : getLevel1DepartmentName().hashCode());
        result = prime * result + ((getLevel2DepartmentId() == null) ? 0 : getLevel2DepartmentId().hashCode());
        result = prime * result + ((getLevel2DepartmentName() == null) ? 0 : getLevel2DepartmentName().hashCode());
        result = prime * result + ((getLevel3DepartmentId() == null) ? 0 : getLevel3DepartmentId().hashCode());
        result = prime * result + ((getLevel3DepartmentName() == null) ? 0 : getLevel3DepartmentName().hashCode());
        result = prime * result + ((getCreatedTime() == null) ? 0 : getCreatedTime().hashCode());
        result = prime * result + ((getUpdatedTime() == null) ? 0 : getUpdatedTime().hashCode());
        return result;
    }

    @Override
    public String toString() {
        return getClass().getSimpleName() +
                " [" +
                "Hash = " + hashCode() +
                ", id=" + id +
                ", name=" + name +
                ", level1DepartmentId=" + level1DepartmentId +
                ", level1DepartmentName=" + level1DepartmentName +
                ", level2DepartmentId=" + level2DepartmentId +
                ", level2DepartmentName=" + level2DepartmentName +
                ", level3DepartmentId=" + level3DepartmentId +
                ", level3DepartmentName=" + level3DepartmentName +
                ", createdTime=" + createdTime +
                ", updatedTime=" + updatedTime +
                "]";
    }
}