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
@TableName(value ="transcend_model_rr_module_domain_20240722_tt")
@Data
@Accessors(chain = true)
public class TonesRrModuleDomain implements Serializable {

    /**
     * Tones属性名
     */
    @TableField(value = "module_name")
    private String moduleName;

    /**
     * Tones字典key
     */
    @TableField(value = "domain_item_name")
    private String domainItemName;

    /**
     * Tones字典value
     */
    @TableField(value = "domain_name")
    private String domainName;

    @TableField(exist = false)
    private static final long serialVersionUID = 1L;


}