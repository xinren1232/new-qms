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
@TableName(value ="transcend_model_rr_dict_20240722_tt")
@Data
@Accessors(chain = true)
public class TonesRrDict implements Serializable {

    /**
     * Tones属性名
     */
    @TableField(value = "attr")
    private String attr;

    /**
     * Tones字典key
     */
    @TableField(value = "dict_key")
    private String dictKey;

    /**
     * Tones字典value
     */
    @TableField(value = "dict_value")
    private String dictValue;

    /**
     * 说明
     */
    @TableField(value = "dict_desc")
    private String dictDesc;

    @TableField(exist = false)
    private static final long serialVersionUID = 1L;


}