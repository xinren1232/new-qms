package com.transcend.plm.datadriven.api.model.config;

import io.swagger.annotations.ApiModelProperty;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

/**
 * 表属性定义
 *
 * @author jie.luo <jie.luo1@transsion.com>
 * @version V1.0.0
 * @date 2023/4/24 17:44
 * @since 1.0
 */
@Getter
@Setter
@Accessors(chain = true)
public class TableAttributeDefinition {

    @ApiModelProperty("列")
    private String columnName;

    @ApiModelProperty("属性")
    private String property;

    @ApiModelProperty("属性类型")
    private String type;

    @ApiModelProperty("基础属性")
    private Boolean baseFlag;

    @ApiModelProperty("默认值")
    private String defaultValue;

    /**
     * @param k
     * @return {@link TableAttributeDefinition }
     */
    public TableAttributeDefinition setPropertyAndColumn(String k){
        setProperty(k);
        setColumnName(k);
        return this;
    }


    /**
     * @return {@link TableAttributeDefinition }
     */
    public static TableAttributeDefinition of() {
        return new TableAttributeDefinition();
    }
}
