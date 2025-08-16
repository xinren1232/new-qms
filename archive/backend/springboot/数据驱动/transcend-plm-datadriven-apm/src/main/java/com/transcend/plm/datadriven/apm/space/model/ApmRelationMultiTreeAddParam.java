package com.transcend.plm.datadriven.apm.space.model;

import com.transcend.plm.datadriven.api.model.MObject;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Builder;
import lombok.Data;

import java.util.List;

/**
 * 空间应用 关系多对象树新增参数
 * @author yinbin
 * @version:
 * @date 2023/10/24 20:05
 */
@Data
@Builder(toBuilder = true)
@ApiModel("空间应用 关系多对象树新增参数")
public class ApmRelationMultiTreeAddParam {

    @ApiModelProperty("目标实例数据")
    private List<MObject> targetList;

    @ApiModelProperty("源对象模型编码")
    private String sourceModelCode;

    @ApiModelProperty("目标模型编码")
    private String targetModelCode;

    @ApiModelProperty("源数据bid")
    private String sourceBid;

    @ApiModelProperty("源数据dataBid")
    private String sourceDataBid;

    @ApiModelProperty("关系模型编码")
    private String relationModelCode;
}
