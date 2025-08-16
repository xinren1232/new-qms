package com.transcend.plm.configcenter.object.infrastructure.po;

import com.baomidou.mybatisplus.annotation.TableName;
import com.transcend.plm.configcenter.common.pojo.po.BasePoEntity;
import io.swagger.annotations.ApiModelProperty;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * ObjectModelLifeCyclePO
 *
 * @author jingfang.Luo
 * @version: 1.0
 * @date 2022/11/25 11:31
 */
@Setter
@Getter
@ToString
@TableName("cfg_object_lifecycle")
public class CfgObjectLifeCyclePo extends BasePoEntity{


    @ApiModelProperty(value = "生命周期状态ID")
    private String lcTemplBid;

    @ApiModelProperty(value = "生命周期模板版本")
    private String lcTemplVersion;

    @ApiModelProperty(value = "初始状态")
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
