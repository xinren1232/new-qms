package com.transcend.plm.datadriven.domain.object.base.pojo.dto;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.util.Date;

/**
 * @author yuanhu.huang
 * @date 2024/07/24
 */
@Data
public class OperationLog {
    @ApiModelProperty(value="业务ID(编码)")
    private String bid;

    @ApiModelProperty(value="模型code")
    private String modelCode;

    @ApiModelProperty(value="实例data_bid")
    private String instanceDataBid;

    @ApiModelProperty(value="实例-业务id")
    private String instanceBid;

    @ApiModelProperty(value="内置字段-创建人")
    private String content;

    @ApiModelProperty(value="内置字段-创建人")
    private String createdBy;

    @ApiModelProperty(value="内置字段-创建人名字")
    private String createdByName;

    @ApiModelProperty(value="内置字段-更新人")
    private String updatedBy;

    private String tenantCode;

}
