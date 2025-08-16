package com.transcend.plm.configcenter.api.model.object.vo;

import com.transcend.plm.configcenter.api.model.base.BaseDto;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import lombok.experimental.Accessors;

/**
 * 对象生命周期VO
 *
 * @author jingfang.Luo
 * @version: 1.0
 * @date 2022/12/01 10:42
 */
@Getter
@Setter
@ToString
@Accessors(chain = true)
@ApiModel(value = "对象生命周期VO")
public class ObjectModelLifeCycleVO extends BaseDto{

    public static ObjectModelLifeCycleVO of() {
        return new ObjectModelLifeCycleVO();
    }

    @ApiModelProperty(value = "生命周期模板id")
    private String lcTemplBid;

    @ApiModelProperty(value = "生命周期模板名称")
    private String lcTemplName;

    @ApiModelProperty(value = "生命周期状态版本")
    private String lcTemplVersion;

    @ApiModelProperty(value = "生命周期初始状态")
    private String initState;

    @ApiModelProperty(value = "生命周期初始状态中文")
    private String initStateName;

    @ApiModelProperty(value = "说明")
    private String description;

    @ApiModelProperty(value = "阶段生命周期模板id")
    private String lcPhaseTemplBid;

    @ApiModelProperty(value = "阶段生命周期状态版本")
    private String lcPhaseTemplVersion;

    @ApiModelProperty(value = "阶段生命周期初始状态")
    private String phaseInitState;

    @ApiModelProperty(value = "阶段生命周期模版说明")
    private String phaseDescription;

    @ApiModelProperty(value = "模型code")
    private String modelCode;

    @ApiModelProperty(value = "是否自定义")
    private Boolean custom;

    private static final long serialVersionUID = 1L;
}
