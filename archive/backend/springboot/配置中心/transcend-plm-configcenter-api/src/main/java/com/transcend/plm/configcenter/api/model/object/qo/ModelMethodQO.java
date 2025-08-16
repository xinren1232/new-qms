package com.transcend.plm.configcenter.api.model.object.qo;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import lombok.experimental.Accessors;

/**
 * @author pingzhang.chen
 * @version: 1.0
 * @date 2021/11/18 09:48
 */
@Accessors(chain = true)
@Getter
@Setter
@ToString
@ApiModel(value = "方法管理")
public class ModelMethodQO {

    @ApiModelProperty(value = "前台展示方法名称",example = "时代发生的发生地方")
    private String name;

    @ApiModelProperty(value = "状态",example = "off:未启用,enable:启用,disable:禁用")
    private Integer enableFlag;
}
