package com.transcend.plm.datadriven.api.model.dto;

import io.swagger.annotations.ApiModelProperty;
import lombok.Builder;
import lombok.Data;

/**
 * @author jinpeng.bai
 * @date 2024/01/23 14:43
 */

@Data
@Builder(toBuilder = true)
public class ReviseDto {

    @ApiModelProperty("模型code")
    private String modelCode;

    @ApiModelProperty("数据bid")
    private String bid;

    @ApiModelProperty("只变更版本和初始状态，并且复制数据，不走关系生命周期比对")
    private Boolean onlyChaneVersionAndCopyRelation;

    @ApiModelProperty("初始生命周期编码")
    private String initLifeCode;
}
