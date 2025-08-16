package com.transcend.plm.configcenter.code.infrastructure.repository.po;

import com.baomidou.mybatisplus.annotation.TableName;
import com.transcend.plm.configcenter.common.annotation.TableUniqueValue;
import com.transcend.plm.configcenter.common.pojo.po.BasePoEntity;
import lombok.Data;

import java.io.Serializable;

/**
 * 属性表
 * @TableName cfg_code_rule
 * @Program transcend-plm-configcenter
 * @Description
 * @Author peng.qin
 * @Version 1.0
 * @Date 2023-03-01 15:16
 **/
@Data
@TableName(value = "cfg_code_rule", autoResultMap = true)
public class CfgCodeRulePo extends BasePoEntity implements Serializable {

    /**
     * 编码
     */
    private String code;

    /**
     * 名称
     */
    @TableUniqueValue(
            message = "名称存在重复值",
            tableName = "cfg_code_rule",
            columnName = "name")
    private String name;

    /**
     * 描述说明
     */
    private String description;

    /**
     * 是否已被绑定
     */
    private boolean binding;

    private static final long serialVersionUID = 1L;

}