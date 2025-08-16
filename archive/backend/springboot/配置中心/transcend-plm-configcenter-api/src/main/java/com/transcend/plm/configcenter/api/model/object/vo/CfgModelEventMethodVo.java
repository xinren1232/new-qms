package com.transcend.plm.configcenter.api.model.object.vo;

import com.transcend.plm.configcenter.api.model.base.BaseDto;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import lombok.experimental.Accessors;

/**
 * 模型事件方法渲染对象
 *
 * @author shuangzhi.zeng
 * @version: 1.0
 * @date 2021/11/18 09:48
 */
@Accessors(chain = true)
@Getter
@Setter
@ToString
@ApiModel(value = "模型事件方法渲染对象")
public class CfgModelEventMethodVo extends BaseDto {

    public static CfgModelEventMethodVo of() {
        return new CfgModelEventMethodVo();
    }

    @ApiModelProperty(value = "事件BID", example = "SDLFSDF")
    private String eventBid;

    @ApiModelProperty(value = "描述", example = "这是前置事件方法")
    private String description;

    @ApiModelProperty(value = "模型对象id", example = "dddddddd")
    private String modelBid;

    @ApiModelProperty(value = "模型对象code", example = "dddddddd")
    private String modelCode;

    @ApiModelProperty(value = "方法bid", example = "sdfdsfsd")
    private String methodBid;

    @ApiModelProperty(value = "方法类型（1 前置 2 后置）")
    private Integer executeType;
}
