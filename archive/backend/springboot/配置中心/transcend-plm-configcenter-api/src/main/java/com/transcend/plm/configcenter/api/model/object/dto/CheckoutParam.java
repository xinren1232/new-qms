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
 * 对象checkout参数
 *
 * @author jingfang.Luo
 * @version: 1.0
 * @date 2022/11/24 11:41
 */
@Getter
@Setter
@ToString
@Accessors(chain = true)
@ApiModel(value = "对象checkout参数")
public class CheckoutParam implements Serializable {

    public static CheckoutParam of() {
        return new CheckoutParam();
    }

    @NotBlank(message = "模型code不能为空")
    @ApiModelProperty(value = "模型code")
    private String modelCode;

    @NotBlank(message = "业务bid")
    @ApiModelProperty(value = "bid")
    private String bid;

    @ApiModelProperty(value = "对象名称")
    private String name;

    private static final long serialVersionUID = 1L;
}
