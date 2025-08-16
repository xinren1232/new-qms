package com.transcend.plm.datadriven.api.model.config;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * @author yuanhu.huang
 * @date 2024/07/24
 */
@Data
public class CloumnDto {

    @ApiModelProperty("列,如name")
    private String cloumn;
    @ApiModelProperty("列类型,如varchar(32)，bit(1),json,一定是数据库可以识别的类型")
    private String cloumnType;
    @ApiModelProperty("是否可为空")
    private boolean isNull;
    @ApiModelProperty("默认值 一定是数据库可以失败的值")
    private String defaultValue;
    @ApiModelProperty("列描述,如name对应描述为名称，可为空")
    private String cloumnDesc;
}
