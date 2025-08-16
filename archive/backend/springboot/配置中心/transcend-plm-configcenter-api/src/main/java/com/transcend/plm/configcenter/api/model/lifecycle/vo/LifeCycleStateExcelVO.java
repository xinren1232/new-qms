package com.transcend.plm.configcenter.api.model.lifecycle.vo;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Getter;
import lombok.Setter;

import javax.validation.constraints.NotBlank;
import java.util.Map;

/**
 * 生命周期状态导入VO
 *
 * @author yikai.lian
 * @version: 1.0
 * @date 2020/12/09 16:09
 */
@Setter
@Getter
@ApiModel(value="生命周期状态导入Excel VO", description="生命周期状态导入Excel对应VO")
public class LifeCycleStateExcelVO {
    /**
     * 序号
     */
    @ApiModelProperty(value = "序号")
    private String num;

    @ApiModelProperty(value = "名称")
    @NotBlank(message = "import_lc_tip_001")
    private String name;

    @ApiModelProperty(value = "编码")
    @NotBlank(message = "import_lc_tip_003")
    private String code;

    @ApiModelProperty(value = "状态组")
    @NotBlank(message = "import_lc_tip_006")
    private String groupCode;

    @ApiModelProperty(value = "返回的状态组中英文map")
    private Map<String, String> groupMap;

    @ApiModelProperty(value = "说明")
    private String description;

    @ApiModelProperty("校验结果信息")
    private String checkResultMessage;
}
