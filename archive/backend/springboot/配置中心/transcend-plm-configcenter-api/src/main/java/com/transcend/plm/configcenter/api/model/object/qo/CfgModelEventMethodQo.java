package com.transcend.plm.configcenter.api.model.object.qo;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiModelProperty;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import lombok.experimental.Accessors;

/**
 * ModelEventMethodQO
 *
 * @author pingzhang.chen
 * @version: 1.0
 * @date 2021/11/29 11:16
 */
@Getter
@Setter
@ToString
@Accessors(chain = true)
@Api(value = "操作领域对象属性QO",tags = "操作领域对象QO")
public class CfgModelEventMethodQo {

    @ApiModelProperty(value = "对象code")
    private String modelCode;

    @ApiModelProperty(value = "事件bid")
    private String eventBid;

    @ApiModelProperty(value = "状态",example = "0:未启用,1:启用,2:禁用")
    private Integer enableFlag;

    @ApiModelProperty(value = "执行类型",example = "1:前置,2:后置")
    private Integer executeType;

    private String methodBid;

    private String modelBid;
}
