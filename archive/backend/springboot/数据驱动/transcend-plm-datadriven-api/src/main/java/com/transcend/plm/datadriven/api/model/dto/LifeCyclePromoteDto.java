package com.transcend.plm.datadriven.api.model.dto;

import io.swagger.annotations.ApiModelProperty;
import lombok.Builder;
import lombok.Data;

import java.io.Serializable;

/**
 * 生命周期提升入参
 *
 * @author jie.luo <jie.luo1@transsion.com>
 * @date 2023/4/24 17:44
 */
@Data
@Builder
public class LifeCyclePromoteDto implements Serializable {

    @ApiModelProperty("模型编码")
    String modelCode;

    @ApiModelProperty("模型业务编码")
    String bid;

    @ApiModelProperty("之前的生命周期")
    String beforeLifeCycleCode;

    @ApiModelProperty("之后的生命周期")
    String afterLifeCycleCode;

    @ApiModelProperty("是否只是提升数据生命周期，不走关系和生命周期行为逻辑比对")
    Boolean isOnlyChangeLifeCode;
}
