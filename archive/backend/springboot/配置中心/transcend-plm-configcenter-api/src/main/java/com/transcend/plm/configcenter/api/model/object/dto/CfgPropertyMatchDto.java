package com.transcend.plm.configcenter.api.model.object.dto;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.io.Serializable;
import java.util.List;

/**
 * 匹配参数
 *
 * @author xin.wu2 <xin.wu2@transsion.com>
 * @version 1.0
 * createdAt 2025/2/21 10:45
 */
@Data
public class CfgPropertyMatchDto implements Serializable {

    @ApiModelProperty("是否任意匹配")
    private Boolean anyMatch;

    @ApiModelProperty("匹配条件")
    private List<Expression> expressions;


    @Data
    public static class Expression implements Serializable {

        @ApiModelProperty("参数名称")
        private String property;

        @ApiModelProperty("控制条件")
        private String condition;

        @ApiModelProperty("控制值")
        private Serializable value;

        @ApiModelProperty("字段类型")
        private String fieldType;

        @ApiModelProperty("字段值类型")
        private String fieldValueType;

        @ApiModelProperty("远程字典类型")
        private String remoteDictType;

    }

}
