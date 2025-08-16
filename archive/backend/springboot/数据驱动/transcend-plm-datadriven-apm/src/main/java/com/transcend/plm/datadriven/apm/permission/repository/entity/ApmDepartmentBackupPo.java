package com.transcend.plm.datadriven.apm.permission.repository.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.transcend.plm.datadriven.common.pojo.po.BasePoEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serializable;

/**
 * 本地部门备份表
 * TableName apm_department_backup
 *
 * @author xin.wu2
 */
@EqualsAndHashCode(callSuper = true)
@Data
@TableName(value = "apm_department_backup")
public class ApmDepartmentBackupPo extends BasePoEntity implements Serializable {

    /**
     * 名称
     */
    private String name;

    /**
     * 上级部门bid
     */
    private String parentBid;

    /**
     * 部门编号
     */
    private String deptNo;

    /**
     * 上级部门编号
     */
    private String parentNo;

    /**
     * 层级
     */
    private Integer level;

}