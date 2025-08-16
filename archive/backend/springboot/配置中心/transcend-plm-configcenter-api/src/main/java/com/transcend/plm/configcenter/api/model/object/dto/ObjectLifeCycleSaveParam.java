package com.transcend.plm.configcenter.api.model.object.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import lombok.experimental.Accessors;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.io.Serializable;

/**
 * 保存对象生命周期参数
 *
 * @author jingfang.Luo
 * @version: 1.0
 * @date 2022/12/12 10:37
 */
@Getter
@Setter
@ToString
@Accessors(chain = true)
@ApiModel(value = "保存对象生命周期参数")
public class ObjectLifeCycleSaveParam implements Serializable {

    public static ObjectLifeCycleSaveParam of() {
        return new ObjectLifeCycleSaveParam();
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

    @NotBlank(message = "modelCode不能为空")
    @ApiModelProperty(value = "模型code")
    private String modelCode;

    @NotNull(message = "是否自定义不能为空")
    @ApiModelProperty(value = "是否自定义")
    private Boolean custom;

    private static final long serialVersionUID = 1L;

}
