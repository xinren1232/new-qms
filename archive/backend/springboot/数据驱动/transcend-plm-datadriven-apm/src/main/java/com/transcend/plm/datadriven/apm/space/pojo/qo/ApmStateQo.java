package com.transcend.plm.datadriven.apm.space.pojo.qo;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * @author unknown
 */
@Data
public class ApmStateQo {
    @ApiModelProperty(value = "模板bid")
    private String lcTemplBid;

    @ApiModelProperty(value = "生命周期状态版本")
    private String lcTemplVersion;

    @ApiModelProperty(value = "当前状态")
    private String currentState;
    @ApiModelProperty(value = "实例bid")
    private String instanceBid;
    @ApiModelProperty(value = "模型编码")
    private String modelCode;
    @ApiModelProperty(value = "空间应用bid")
    private String spaceAppBid;

}
