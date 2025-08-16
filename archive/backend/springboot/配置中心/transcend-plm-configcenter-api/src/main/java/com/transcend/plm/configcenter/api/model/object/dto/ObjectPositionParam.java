package com.transcend.plm.configcenter.api.model.object.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import lombok.experimental.Accessors;

import javax.validation.constraints.NotBlank;
import java.io.Serializable;

/**
 * 编辑对象参数
 *
 * @author jingfang.Luo
 * @version: 1.0
 * @date 2022/11/29 23:03
 */
@Getter
@Setter
@ToString
@Accessors(chain = true)
@ApiModel(value = "编辑对象参数")
public class ObjectPositionParam implements Serializable {

    public static ObjectPositionParam of() {
        return new ObjectPositionParam();
    }

    @ApiModelProperty(value = "业务bid")
    private String bid;

    @NotBlank(message = "模型code不能为空")
    @ApiModelProperty(value = "模型code")
    private String modelCode;

    @NotBlank(message = "对象名称不能为空")
    @ApiModelProperty(value = "对象名称")
    private String name;

    @NotBlank(message = "对象排序不能为空")
    @ApiModelProperty(value = "排序号")
    private Integer sort;

    private static final long serialVersionUID = 1L;

}
