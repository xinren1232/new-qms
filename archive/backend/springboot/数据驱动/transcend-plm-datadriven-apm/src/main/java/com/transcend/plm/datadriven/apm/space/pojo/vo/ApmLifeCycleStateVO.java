package com.transcend.plm.datadriven.apm.space.pojo.vo;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.util.List;
import java.util.Map;

/**
 * @author unknown
 * 状态VO
 */
@Data
public class ApmLifeCycleStateVO {
    @ApiModelProperty(value = "模板bid")
    private String lcTemplBid;

    @ApiModelProperty(value = "期模板名称")
    private String lcTemplName;

    @ApiModelProperty(value = "生命周期状态版本")
    private String lcTemplVersion;

    @ApiModelProperty(value = "生命周期初始状态")
    private String initState;

    @ApiModelProperty(value = "生命周期初始状态中文")
    private String initStateName;

    @ApiModelProperty(value = "生命周期状态类型，lifyCycle生命周期模板，stateFlow状态流程")

    private String lifeCycleStateType;

    @ApiModelProperty(value = "说明")
    private String description;

    @ApiModelProperty(value = "模型code")
    private String modelCode;

    @ApiModelProperty(value = "所有状态集合")
    private List<ApmStateVO> apmStateVOList;

    @ApiModelProperty(value = "阶段状态映射")
    private Map<String, ApmStateVO> phaseStateMap;
}
