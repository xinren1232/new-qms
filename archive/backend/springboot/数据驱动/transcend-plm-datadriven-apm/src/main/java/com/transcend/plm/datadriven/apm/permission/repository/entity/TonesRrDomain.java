package com.transcend.plm.datadriven.apm.permission.repository.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.experimental.Accessors;

import java.io.Serializable;

/**
 * @author unknown
 * @TableName apm_role_identity
 */
@TableName(value ="transcend_model_rr_domain_20240722_tt")
@Data
@Accessors(chain = true)
public class TonesRrDomain implements Serializable {

    /**
     * 产品经理
     */
    @TableField(value = "pm")
    private String pm;

    /**
     * 模块名称
     */
    @TableField(value = "module_name")
    private String moduleName;


    @TableField(exist = false)
    private static final long serialVersionUID = 1L;


}