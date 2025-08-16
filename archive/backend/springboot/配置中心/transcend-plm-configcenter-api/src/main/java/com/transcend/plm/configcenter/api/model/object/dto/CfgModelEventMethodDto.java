package com.transcend.plm.configcenter.api.model.object.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import lombok.experimental.Accessors;

/**
 * 模型事件方法传输对象
 *
 * @author shuangzhi.zeng
 * @version: 1.0
 * @date 2021/11/18 09:48
 */
@Accessors(chain = true)
@Getter
@Setter
@ToString
@ApiModel(value = "模型事件方法传输对象")
public class CfgModelEventMethodDto {

    public static CfgModelEventMethodDto of(){
        return  new CfgModelEventMethodDto();
    }

    @ApiModelProperty(value = "业务id",example = "1111")
    private String bid;

    @ApiModelProperty(value = "事件ID" ,example = "eventId")
    private String eventBid;

    @ApiModelProperty(value = "描述",example = "这是前置事件方法")
    private String description;

    @ApiModelProperty(value = "状态",example = "off:未启用,enable:启用,disable:禁用")
    private Integer enableFlag;

    @ApiModelProperty(value = "模型对象id",example = "dddddddd")
    private String modelBid;

    @ApiModelProperty(value = "模型对象id",example = "dddddddd")
    private String modelCode;

    @ApiModelProperty(value = "方法bid",example = "sdfdsfsd")
    private String methodBid;

    @ApiModelProperty(value = "执行类型",example = "1:前置,2:后置")
    private Integer executeType;
}
