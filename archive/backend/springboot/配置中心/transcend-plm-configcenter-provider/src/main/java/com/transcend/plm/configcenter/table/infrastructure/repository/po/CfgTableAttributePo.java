package com.transcend.plm.configcenter.table.infrastructure.repository.po;

import com.baomidou.mybatisplus.annotation.TableName;
import com.transcend.plm.configcenter.common.pojo.po.BasePoEntity;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.io.Serializable;

/**
 * @author 表属性定义表
 */
@Data
@TableName("cfg_table_attribute")
public class CfgTableAttributePo extends BasePoEntity implements Serializable {


    /**
     * 表bid
     */
    private String tableBid;

    /**
     * 列
     */
    private String columnName;

    /**
     * 属性
     */
    private String property;

    private Boolean index;

    /**
     * 类型
     */
    private String type;

    /**
     * 处理数据类型
     */
    private String handleType;

    private String defaultValue;

    private String comment;

    private Boolean required;

    /**
     * 基础属性
     */
    private Boolean baseFlag = true;

    private static final long serialVersionUID = 1L;
}