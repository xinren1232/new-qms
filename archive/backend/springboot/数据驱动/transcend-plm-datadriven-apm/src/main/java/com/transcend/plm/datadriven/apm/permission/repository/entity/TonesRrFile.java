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
@TableName(value ="transcend_model_rr_files_20240722_tt")
@Data
@Accessors(chain = true)
public class TonesRrFile implements Serializable {

    /**
     * tones_id
     */
    @TableField(value = "tones_id")
    private String tonesId;

    /**
     * 附件Id
     */
    @TableField(value = "file_name")
    private String fileName;

    /**
     * 附件名称
     */
    @TableField(value = "name")
    private String name;

    @TableField(exist = false)
    private static final long serialVersionUID = 1L;


}