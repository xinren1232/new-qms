package com.transcend.plm.datadriven.apm.log.model.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Builder;
import lombok.Data;

import javax.validation.constraints.NotBlank;

/**
 * @author yinbin
 * @version:
 * @date 2023/10/09 09:16
 */
@Data
@Builder(toBuilder = true)
@ApiModel("通用日志新增参数")
public class GenericLogAddParam {

    @ApiModelProperty("空间bid")
    private String spaceBid;

    @ApiModelProperty("对象modelCode")
    private String modelCode;

    @ApiModelProperty("对象实例bid")
    @NotBlank(message = "对象实例bid")
    private String instanceBid;

    @ApiModelProperty("日志信息")
    @NotBlank(message = "日志信息不能为空")
    private String logMsg;

    @ApiModelProperty("类型")
    @NotBlank(message = "类型不能为空")
    private String type;
}
