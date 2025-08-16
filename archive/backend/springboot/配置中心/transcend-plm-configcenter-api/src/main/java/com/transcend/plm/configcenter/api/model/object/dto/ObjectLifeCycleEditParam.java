package com.transcend.plm.configcenter.api.model.object.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import lombok.experimental.Accessors;

import java.io.Serializable;

/**
 * 编辑对象生命周期参数
 *
 * @author jingfang.Luo
 * @version: 1.0
 * @date 2022/12/12 18:38
 */
@Getter
@Setter
@ToString
@Accessors(chain = true)
@ApiModel(value = "编辑对象生命周期参数")
public class ObjectLifeCycleEditParam implements Serializable {

    public static ObjectLifeCycleEditParam of() {
        return new ObjectLifeCycleEditParam();
    }

    @ApiModelProperty(value = "生命周期模板id")
    private String lcTemplBid;

    @ApiModelProperty(value = "生命周期状态版本")
    private String lcTemplVersion;

    @ApiModelProperty(value = "生命周期初始状态")
    private String initState;

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

    private static final long serialVersionUID = 1L;

}
